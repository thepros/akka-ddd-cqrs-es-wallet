package com.github.j5ik2o.bank.useCase.port

import java.time.Instant

import scala.concurrent.ExecutionContext

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Source }
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol.{
  ResolveBankAccountEventsRequest,
  ResolveBankAccountEventsResponse
}

trait BankAccountReadModelFlows {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def depositBankAccountFlow(
      implicit ec: ExecutionContext
  ): Flow[(BigDecimal, Long, Instant), Int, NotUsed]

  def resolveBankAccountEventByIdFlow(
      implicit ec: ExecutionContext
  ): Flow[ResolveBankAccountEventsRequest, ResolveBankAccountEventsResponse, NotUsed]
}
