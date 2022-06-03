package com.github.j5ik2o.bank.adaptor.util

import akka.actor.ActorSystem
import org.scalacheck.Gen
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait BankAccountSpecSupport {

  val system: ActorSystem

  lazy val dbConfig: DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig[JdbcProfile](path = "slick", system.settings.config)

  val depositMoneyGen: Gen[BigDecimal] = Gen.choose(1L, 100L).map(v => BigDecimal(v))

  val bankAccountNameAndDepositMoneyGen: Gen[BigDecimal] = for {
    deposit <- depositMoneyGen
  } yield deposit

}
