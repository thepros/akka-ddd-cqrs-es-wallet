package com.github.j5ik2o.bank.domain.model

import java.time.Instant

sealed trait BankAccountEvent {
  val datetime: Instant
}

case class BankAccountDeposited(deposit: BigDecimal, datetime: Instant) extends BankAccountEvent
