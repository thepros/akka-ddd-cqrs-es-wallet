package com.github.j5ik2o.bank.adaptor.controller

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import com.github.j5ik2o.bank.useCase.{ BankAccountAggregateUseCase, BankAccountReadModelUseCase }
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
    addBankAccountEvent
  }

  def addBankAccountEvent(implicit ec: ExecutionContext): Route =
    pathPrefix(bankAccountsRouteName / Segment / "events") { _ =>
      pathEndOrSingleSlash {
        put {
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

}
