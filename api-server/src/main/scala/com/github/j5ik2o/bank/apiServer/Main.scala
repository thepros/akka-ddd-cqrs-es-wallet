package com.github.j5ik2o.bank.apiServer

import scala.concurrent.ExecutionContextExecutor

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.j5ik2o.bank.adaptor.aggregate.{ BankAccountAggregate, BankAccountAggregateFlowsImpl }
import com.github.j5ik2o.bank.adaptor.controller.Routes
import com.github.j5ik2o.bank.adaptor.dao.BankAccountReadModelFlowsImpl
import com.github.j5ik2o.bank.adaptor.readJournal.JournalReaderImpl
import com.github.j5ik2o.bank.useCase.{ BankAccountAggregateUseCase, BankAccountReadModelUseCase }
import com.typesafe.config.{ Config, ConfigFactory }
import pureconfig._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object Main extends App {
  val rootConfig: Config                    = ConfigFactory.load()
  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](path = "slick", rootConfig)

  implicit val system: ActorSystem                        = ActorSystem("bank-system", config = rootConfig)
  implicit val materializer: ActorMaterializer            = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val bankAccountAggregatesRef: ActorRef =
    system.actorOf(BankAccountAggregate.props, BankAccountAggregate.name)

  val bankAccountAggregateUseCase: BankAccountAggregateUseCase = new BankAccountAggregateUseCase(
    new BankAccountAggregateFlowsImpl(bankAccountAggregatesRef)
  )

  val bankAccountReadModelUseCase: BankAccountReadModelUseCase =
    new BankAccountReadModelUseCase(new BankAccountReadModelFlowsImpl(dbConfig.profile, dbConfig.db),
                                    new JournalReaderImpl())

  val routes: Routes = Routes(bankAccountAggregateUseCase, bankAccountReadModelUseCase)

  val ApiServerConfig(host, port) =
    loadConfigOrThrow[ApiServerConfig](system.settings.config.getConfig("bank.api-server"))

  val bindingFuture = Http().bindAndHandle(routes.root, host, port)

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
