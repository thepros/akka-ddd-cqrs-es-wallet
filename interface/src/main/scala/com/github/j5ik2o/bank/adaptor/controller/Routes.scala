package com.github.j5ik2o.bank.adaptor.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import cats.syntax.either._
import com.github.j5ik2o.bank.useCase.{ BankAccountAggregateUseCase, BankAccountReadModelUseCase }
import org.hashids.Hashids

object Routes {

  trait ResponseJson {
    val isSuccessful: Boolean
    val errorMessages: Seq[String]
  }

  case class AddBankAccountEventRequestJson(amount: Long, datetime: String)

  case class AddBankAccountEventResponseJson(errorMessages: Seq[String] = Seq.empty) extends ResponseJson {
    override val isSuccessful: Boolean = errorMessages.isEmpty
  }

  case class BankAccountEventJson(amount: Long, createAt: Long)

  case class ResolveBankAccountEventsResponseJson(values: Seq[BankAccountEventJson],
                                                  errorMessages: Seq[String] = Seq.empty)
      extends ResponseJson {
    override val isSuccessful: Boolean = errorMessages.isEmpty
  }

  case class ValidationErrorsResponseJson(errorMessages: Seq[String]) extends ResponseJson {
    override val isSuccessful: Boolean = false
  }

  implicit class HashidsStringOps(val self: String) extends AnyVal {
    def decodeFromHashid(implicit hashIds: Hashids): Either[Throwable, Long] = {
      Either.catchNonFatal(hashIds.decode(self)(0))
    }
  }

  implicit class HashidsLongOps(val self: Long) extends AnyVal {
    def encodeToHashid(implicit hashIds: Hashids): Either[Throwable, String] =
      Either.catchNonFatal(hashIds.encode(self))
  }

}

case class Routes(bankAccountAggregateUseCase: BankAccountAggregateUseCase,
                  bankAccountReadModelUseCase: BankAccountReadModelUseCase)(
    implicit system: ActorSystem,
    mat: Materializer
) extends BankAccountController {

  implicit val ec = system.dispatcher

  def root: Route = RouteLogging.default.httpLogRequestResult {
    pathEndOrSingleSlash {
      get {
        index()
      }
    } ~ toBankAccountRoutes
  }

  private def index() = complete(
    HttpResponse(
      entity = HttpEntity(
        ContentTypes.`text/plain(UTF-8)`,
        "Welcome to Bank API"
      )
    )
  )

}
