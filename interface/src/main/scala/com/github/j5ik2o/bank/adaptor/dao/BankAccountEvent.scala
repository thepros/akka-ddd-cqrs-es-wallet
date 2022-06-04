package com.github.j5ik2o.bank.adaptor.dao

trait BankAccountEventComponent extends ComponentSupport with BankAccountEventComponentSupport {

  import profile.api._

  case class BankAccountEventRecord(
      amount: Long,
      sequenceNr: Long,
      createdAt: java.time.ZonedDateTime
  ) extends Record

  case class BankAccountEvents(tag: Tag) extends TableBase[BankAccountEventRecord](tag, "bank_account_event") {

    def amount     = column[Long]("amount")
    def sequenceNr = column[Long]("sequence_nr")
    def createdAt  = column[java.time.ZonedDateTime]("created_at")
    override def * = (amount, sequenceNr, createdAt) <> (BankAccountEventRecord.tupled, BankAccountEventRecord.unapply)
  }

  object BankAccountEventDao
      extends TableQuery(BankAccountEvents)
      with DaoSupport[Long, BankAccountEventRecord]
      with BankAccountEventDaoSupport

}
