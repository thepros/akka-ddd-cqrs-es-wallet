package com.github.j5ik2o.bank.adaptor.serialization

import java.time.Instant

import com.github.j5ik2o.bank.domain.model._

case class BankAccountDepositedJson(amount: Long, occurredAt: Long)

object BankAccountCreatedJson {

  implicit object BankAccountDepositedIso extends EventToJsonReprIso[BankAccountDeposited, BankAccountDepositedJson] {
    override def convertTo(event: BankAccountDeposited): BankAccountDepositedJson = {
      BankAccountDepositedJson(
        amount = event.deposit.toLong,
        occurredAt = event.datetime.toEpochMilli
      )
    }

    override def convertFrom(json: BankAccountDepositedJson): BankAccountDeposited = {
      BankAccountDeposited(
        deposit = BigDecimal(json.amount),
        datetime = Instant.ofEpochMilli(json.occurredAt)
      )
    }
  }

}
