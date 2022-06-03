package com.github.j5ik2o.bank.adaptor.serialization

import com.github.j5ik2o.bank.domain.model._
import org.sisioh.baseunits.scala.time.TimePoint

case class BankAccountDepositedJson(amount: Long, occurredAt: Long)

object BankAccountCreatedJson {

  implicit object BankAccountDepositedIso extends EventToJsonReprIso[BankAccountDeposited, BankAccountDepositedJson] {
    override def convertTo(event: BankAccountDeposited): BankAccountDepositedJson = {
      BankAccountDepositedJson(
        amount = event.deposit.toLong,
        occurredAt = event.occurredAt.millisecondsFromEpoc
      )
    }

    override def convertFrom(json: BankAccountDepositedJson): BankAccountDeposited = {
      BankAccountDeposited(
        deposit = BigDecimal(json.amount),
        occurredAt = TimePoint.from(json.occurredAt)
      )
    }
  }

}
