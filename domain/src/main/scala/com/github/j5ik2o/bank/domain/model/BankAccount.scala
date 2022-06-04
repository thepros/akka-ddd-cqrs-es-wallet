package com.github.j5ik2o.bank.domain.model

import com.github.j5ik2o.bank.domain.model.BankAccount.BankAccountError
import org.sisioh.baseunits.scala.time.TimePoint
import org.sisioh.baseunits.scala.timeutil.Clock

trait BankAccount {

  def balance: BigDecimal

  def deposit(money: BigDecimal, occurredAt: TimePoint = Clock.now): Either[BankAccountError, BankAccount]

}

object BankAccount {
  final val DEFAULT_MONEY_ZERO = 0

  sealed abstract class BankAccountError(val message: String)

  case class InvalidStateError() extends BankAccountError(s"Invalid state")

  case class DepositZeroError(money: BigDecimal)
      extends BankAccountError(s"A deposited money amount 0 is illegal: money = $money")

  case class NegativeBalanceError(money: BigDecimal)
      extends BankAccountError(s"Forbidden that deposit amount to negative: money = $money")

  def apply(balance: BigDecimal): BankAccount =
    BankAccountImpl(balance)

  def unapply(self: BankAccount): Option[(BigDecimal)] =
    Some(self.balance)

  private case class BankAccountImpl(balance: BigDecimal) extends BankAccount {

    override def deposit(money: BigDecimal, occurredAt: TimePoint): Either[BankAccountError, BankAccount] = {
      money match {
        case d if d == 0 =>
          Left(DepositZeroError(money))
        case d if (balance + d) < 0 =>
          Left(NegativeBalanceError(money))
        case _ =>
          Right(copy(balance = balance + money))
      }
    }
  }

}
