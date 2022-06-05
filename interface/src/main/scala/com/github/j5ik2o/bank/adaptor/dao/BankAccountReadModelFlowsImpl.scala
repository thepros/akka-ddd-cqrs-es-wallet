package com.github.j5ik2o.bank.adaptor.dao

import java.time.{ Instant, ZoneOffset }

import scala.concurrent.ExecutionContext

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Source }
import com.github.j5ik2o.bank.useCase.BankAccountAggregateUseCase.Protocol.{
  BankAccountEventBody,
  ResolveBankAccountEventsRequest,
  ResolveBankAccountEventsResponse,
  ResolveBankAccountEventsSucceeded
}
import com.github.j5ik2o.bank.useCase.port.BankAccountReadModelFlows
import slick.jdbc.JdbcProfile

class BankAccountReadModelFlowsImpl(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
    extends BankAccountEventComponent
    with BankAccountReadModelFlows {
  import profile.api._

  override def resolveBankAccountEventByIdFlow(
      implicit ec: ExecutionContext
  ): Flow[ResolveBankAccountEventsRequest, ResolveBankAccountEventsResponse, NotUsed] =
    Flow[ResolveBankAccountEventsRequest]
      .mapAsync(1) { _request =>
        db.run(BankAccountEventDao.result).map((_request, _))
      }
      .map {
        case (_request, result) =>
          val values = result.map { v =>
            BankAccountEventBody(v.amount, v.createdAt)
          }
          ResolveBankAccountEventsSucceeded(values)
      }

  def resolveLastSeqNrSource(implicit ec: ExecutionContext): Source[Long, NotUsed] =
    Source.single(1).mapAsync(1) { _ =>
      db.run(BankAccountEventDao.map(_.sequenceNr).max.result)
        .map(_.getOrElse(0L))
    }

  override def depositBankAccountFlow(
      implicit ec: ExecutionContext
  ): Flow[(BigDecimal, Long, Instant), Int, NotUsed] =
    Flow[(BigDecimal, Long, Instant)].mapAsync(1) {
      case (deposit, sequenceNr, occurredAt) =>
        val query = (for {
          bankAccountEventInsertResult <- BankAccountEventDao.forceInsert(
            BankAccountEventRecord(
              deposit.toLong,
              sequenceNr,
              occurredAt.atZone(ZoneOffset.UTC)
            )
          )
        } yield bankAccountEventInsertResult).transactionally
        db.run(query).map(_ => 1)

    }

}
