package com.github.j5ik2o.bank.adaptor.aggregate

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol
import com.github.j5ik2o.bank.useCase.port.BankAccountAggregateFlows
import pureconfig._

class BankAccountAggregateFlowsImpl(aggregateRef: ActorRef)(
    implicit val system: ActorSystem
) extends BankAccountAggregateFlows {

  import Protocol._

  private val config = loadConfigOrThrow[BankAccountAggregateFlowsConfig](
    system.settings.config.getConfig("bank.interface.bank-account-aggregate-flows")
  )

  private implicit val to: Timeout = Timeout(config.callTimeout)

  override def addBankAccountEventFlow: Flow[AddBankAccountEventRequest, AddBankAccountEventResponse, NotUsed] =
    Flow[AddBankAccountEventRequest]
      .map {
        case request: DepositRequest =>
          BankAccountAggregate.Protocol.DepositRequest(request.deposit)
      }
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case response: BankAccountAggregate.Protocol.DepositSucceeded =>
          DepositSucceeded()
        case response: BankAccountAggregate.Protocol.DepositFailed =>
          DepositFailed(response.error)
      }

}
