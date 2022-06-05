package com.github.j5ik2o.bank.adaptor.controller

import java.time.Instant

import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol._
import org.hashids.Hashids

object BankAccountConverter {

  import Routes._

  val convertToAddBankAccountEventUseCaseModel
    : AddBankAccountEventRequestJson => Protocol.AddBankAccountEventRequest = {
    (json: AddBankAccountEventRequestJson) =>
      BankAccountAggregateUseCase.Protocol
        .DepositRequest(BigDecimal(json.amount), Instant.parse(json.datetime))
  }

  def convertToAddBankAccountEventInterfaceModel(
      implicit hashIds: Hashids
  ): PartialFunction[BankAccountAggregateUseCase.Protocol.AddBankAccountEventResponse,
                     AddBankAccountEventResponseJson] = {
    case _: BankAccountAggregateUseCase.Protocol.DepositSucceeded =>
      AddBankAccountEventResponseJson()

    case response: BankAccountAggregateUseCase.Protocol.DepositFailed =>
      AddBankAccountEventResponseJson(Seq(response.error.message))
  }

  def convertToResolveInterfaceModel(
      implicit hashIds: Hashids
  ): PartialFunction[BankAccountAggregateUseCase.Protocol.ResolveBankAccountEventsResponse,
                     ResolveBankAccountEventsResponseJson] = {
    case response: ResolveBankAccountEventsSucceeded =>
      ResolveBankAccountEventsResponseJson(
        response.events.map(v => BankAccountEventJson(v.amount, v.createAt.toEpochSecond))
      )

    case response: ResolveBankAccountEventsFailed =>
      ResolveBankAccountEventsResponseJson(
        Seq.empty,
        Seq(response.error.message)
      )

  }

}
