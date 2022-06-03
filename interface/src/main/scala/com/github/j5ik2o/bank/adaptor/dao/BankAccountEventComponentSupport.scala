package com.github.j5ik2o.bank.adaptor.dao

trait BankAccountEventComponentSupport { this: BankAccountEventComponent =>
  trait BankAccountEventDaoSupport { this: DaoSupport[Long, BankAccountEventRecord] =>

  }
}
