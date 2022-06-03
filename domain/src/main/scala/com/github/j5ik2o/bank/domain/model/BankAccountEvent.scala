package com.github.j5ik2o.bank.domain.model

import org.sisioh.baseunits.scala.time.TimePoint
import org.sisioh.baseunits.scala.timeutil.Clock

sealed trait BankAccountEvent {
  val occurredAt: TimePoint
}

case class BankAccountDeposited(deposit: BigDecimal, occurredAt: TimePoint = Clock.now) extends BankAccountEvent
