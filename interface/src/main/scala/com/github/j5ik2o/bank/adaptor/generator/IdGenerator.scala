package com.github.j5ik2o.bank.adaptor.generator

import com.github.j5ik2o.bank.domain.model.BankAccountEventId
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

object IdGenerator {
  def ofBankAccountEventId(profile: JdbcProfile, db: JdbcProfile#Backend#Database): IdGenerator[BankAccountEventId] =
    new BankAccountEventIdGeneratorOnJDBC(profile, db)
}

trait IdGenerator[ID] {
  def generateId()(implicit ec: ExecutionContext): Future[ID]
}
