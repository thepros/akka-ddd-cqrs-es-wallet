package com.github.j5ik2o.bank.useCase.port

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol._

trait BankAccountAggregateFlows {

  def addBankAccountEventFlow: Flow[AddBankAccountEventRequest, AddBankAccountEventResponse, NotUsed]

}
