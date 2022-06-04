package com.github.j5ik2o.bank.adaptor.controller

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import com.github.j5ik2o.bank.useCase.{ BankAccountAggregateUseCase, BankAccountReadModelUseCase }
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol.ResolveBankAccountEventsRequest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

trait BankAccountController extends BankAccountValidateDirectives {
  import BankAccountConverter._
  import ControllerBase._
  import Routes._

  private val bankAccountsRouteName = "bank-accounts"

  protected val bankAccountAggregateUseCase: BankAccountAggregateUseCase

  protected val bankAccountReadModelUseCase: BankAccountReadModelUseCase

  def toBankAccountRoutes(implicit ec: ExecutionContext): Route = handleRejections(RejectionHandlers.default) {
    addBankAccountEvent ~ resolveBankAccountEvents
  }

  def addBankAccountEvent(implicit ec: ExecutionContext): Route =
    pathPrefix(bankAccountsRouteName / "deposit") {
      pathEndOrSingleSlash {
        post {
          entity(as[AddBankAccountEventRequestJson]) { json =>
            validateBankAccountRequestJson(json).apply { validatedJson =>
              val future = bankAccountAggregateUseCase
                .addBankAccountEvent(convertToAddBankAccountEventUseCaseModel(validatedJson))
                .map(convertToAddBankAccountEventInterfaceModel)
              onSuccess(future) { response =>
                complete(response)
              }
            }
          }
        }
      }
    }

  def resolveBankAccountEvents(implicit ec: ExecutionContext): Route =
    pathPrefix(bankAccountsRouteName) {
      pathEndOrSingleSlash {
        get {
          val future = bankAccountReadModelUseCase
            .resolveBankAccountEvents(ResolveBankAccountEventsRequest())
            .map(convertToResolveInterfaceModel)
          onSuccess(future) { response =>
            complete(response)
          }
        }
      }
    }

}
