package com.github.j5ik2o.bank.useCase

import java.time.{ Instant, ZonedDateTime }

import scala.concurrent.{ ExecutionContext, Future, Promise }

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{ Keep, Source, SourceQueueWithComplete }
import com.github.j5ik2o.bank.domain.model.BankAccount.BankAccountError
import com.github.j5ik2o.bank.useCase.port.BankAccountAggregateFlows
import pureconfig._

object BankAccountAggregateUseCase {

  object Protocol {

    sealed trait BankAccountCommandRequest {}

    sealed trait BankAccountCommandResponse {}

    // ---

    sealed trait AddBankAccountEventRequest  extends BankAccountCommandRequest
    sealed trait AddBankAccountEventResponse extends BankAccountCommandResponse

    case class DepositRequest(deposit: BigDecimal, datetime: Instant) extends AddBankAccountEventRequest

    sealed trait DepositResponse extends AddBankAccountEventResponse

    case class DepositSucceeded() extends DepositResponse

    case class DepositFailed(error: BankAccountError) extends DepositResponse

    // ---

    case class GetBalanceRequest() extends BankAccountCommandRequest

    case class GetBalanceResponse(balance: BigDecimal) extends BankAccountCommandRequest

    // ---

    case class ResolveBankAccountEventsRequest() extends BankAccountCommandRequest

    sealed trait ResolveBankAccountEventsResponse extends BankAccountCommandResponse

    case class BankAccountEventBody(amount: Long, createAt: ZonedDateTime)

    case class ResolveBankAccountEventsSucceeded(events: Seq[BankAccountEventBody])
        extends ResolveBankAccountEventsResponse

    case class ResolveBankAccountEventsFailed(error: BankAccountError) extends ResolveBankAccountEventsResponse

  }

}

class BankAccountAggregateUseCase(bankAccountAggregateFlows: BankAccountAggregateFlows)(implicit system: ActorSystem)
    extends UseCaseSupport {
  import BankAccountAggregateUseCase.Protocol._
  import UseCaseSupport._

  implicit val mat: Materializer = ActorMaterializer()

  private val config = loadConfigOrThrow[BankAccountAggregateUseCaseConfig]("bank.use-case.bank-account-use-case")

  private val bufferSize: Int = config.bufferSize

  def addBankAccountEvent(
      request: AddBankAccountEventRequest
  )(implicit ec: ExecutionContext): Future[AddBankAccountEventResponse] =
    offerToQueue(addBankAccountEventQueue)(request, Promise())

  private val addBankAccountEventQueue
    : SourceQueueWithComplete[(AddBankAccountEventRequest, Promise[AddBankAccountEventResponse])] =
    Source
      .queue[(AddBankAccountEventRequest, Promise[AddBankAccountEventResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(bankAccountAggregateFlows.addBankAccountEventFlow.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

}
