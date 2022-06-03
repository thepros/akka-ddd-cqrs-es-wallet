package com.github.j5ik2o.bank.adaptor.controller

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import cats.data.ValidatedNel
import cats.implicits._

sealed trait Error {
  val message: String
  val cause: Option[Throwable]
}

case class BankAccountNameError(message: String, cause: Option[Throwable] = None)      extends Error
case class BankAccountEventTypeError(message: String, cause: Option[Throwable] = None) extends Error
case class BankAccountMoneyError(message: String, cause: Option[Throwable] = None)     extends Error

object ValidateUtils {
  import Routes._

  type ValidationResult[A] = ValidatedNel[Error, A]

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  implicit object AddBankAccountEventRequestJsonValidator extends Validator[AddBankAccountEventRequestJson] {
    override def validate(value: AddBankAccountEventRequestJson): ValidationResult[AddBankAccountEventRequestJson] = {
      validateMoney(value.amount).map { _: BigDecimal =>
        value
      }
    }
  }

  def validateMoney(amount: Long): ValidationResult[BigDecimal] = {
    try {
      BigDecimal(amount).validNel
    } catch {
      case ex: Throwable => BankAccountMoneyError("", Some(ex)).invalidNel
    }
  }

}

trait BankAccountValidateDirectives {
  import ValidateUtils._
  protected def validateBankAccountRequestJson[A: Validator](value: A): Directive1[A] =
    implicitly[Validator[A]]
      .validate(value)
      .fold({ errors =>
        reject(ValidationsRejection(errors))
      }, provide)
}
