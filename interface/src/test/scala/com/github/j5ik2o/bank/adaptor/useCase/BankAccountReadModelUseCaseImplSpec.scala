package com.github.j5ik2o.bank.adaptor.useCase

import scala.concurrent.duration._

import akka.actor.ActorSystem
import com.github.j5ik2o.bank.adaptor.dao.BankAccountReadModelFlowsImpl
import com.github.j5ik2o.bank.adaptor.readJournal.JournalReaderImpl
import com.github.j5ik2o.bank.adaptor.util.{ ActorSpec, BankAccountSpecSupport, FlywayWithMySQLSpecSupport }
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol._
import com.github.j5ik2o.bank.useCase.BankAccountReadModelUseCase
import com.github.j5ik2o.scalatestplus.db.{ MySQLdConfig, UserWithPassword }
import com.typesafe.config.ConfigFactory
import com.wix.mysql.distribution.Version.v5_6_21
import org.scalatest.time.{ Millis, Seconds, Span }

class BankAccountReadModelUseCaseImplSpec
    extends ActorSpec(
      ActorSystem("BankAccountReadModelUseCaseImplSpec", ConfigFactory.load("bank-account-use-case-spec.conf"))
    )
    with FlywayWithMySQLSpecSupport
    with BankAccountSpecSupport {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(200, Millis))

  override protected lazy val mySQLdConfig: MySQLdConfig = MySQLdConfig(
    version = v5_6_21,
    port = Some(12345),
    userWithPassword = Some(UserWithPassword("bank", "passwd")),
    timeout = Some((30 seconds) * sys.env.getOrElse("SBT_TEST_TIME_FACTOR", "1").toDouble)
  )

  import system.dispatcher

  "BankAccountReadModelUseCaseImpl" - {
    "should be able to read read-model" in {
      val bankAccountReadModelUseCase = new BankAccountReadModelUseCase(
        new BankAccountReadModelFlowsImpl(dbConfig.profile, dbConfig.db),
        new JournalReaderImpl()
      )
      bankAccountReadModelUseCase.execute()
      awaitAssert(
        {
          val resolveBankAccountEventsSucceeded = bankAccountReadModelUseCase
            .resolveBankAccountEventsById(ResolveBankAccountEventsRequest())
            .futureValue
            .asInstanceOf[ResolveBankAccountEventsSucceeded]
          resolveBankAccountEventsSucceeded.events.head.amount shouldBe 1000
        },
        3 seconds,
        50 milliseconds
      )
    }
  }
}
