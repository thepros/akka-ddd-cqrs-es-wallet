package com.github.j5ik2o.bank.useCase.port

import scala.concurrent.ExecutionContext

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Source }
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol.{
  ResolveBankAccountEventsRequest,
  ResolveBankAccountEventsResponse
}
import org.sisioh.baseunits.scala.time.TimePoint

trait BankAccountReadModelFlows {

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed]

  def depositBankAccountFlow(
      implicit ec: ExecutionContext
  ): Flow[(BigDecimal, Long, TimePoint), Int, NotUsed]

  def resolveBankAccountEventByIdFlow(
      implicit ec: ExecutionContext
  ): Flow[ResolveBankAccountEventsRequest, ResolveBankAccountEventsResponse, NotUsed]
}
