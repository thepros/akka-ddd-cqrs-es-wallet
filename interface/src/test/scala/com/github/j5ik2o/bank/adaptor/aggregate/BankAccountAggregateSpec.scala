package com.github.j5ik2o.bank.adaptor.aggregate

import akka.stream.ActorMaterializer
import com.github.j5ik2o.bank.adaptor.aggregate.BankAccountAggregate.Protocol._
import com.github.j5ik2o.bank.adaptor.util.BankAccountSpecSupport
import com.github.j5ik2o.scalatestplus.db.{ MySQLdConfig, UserWithPassword }
import com.typesafe.config.ConfigFactory
import com.wix.mysql.distribution.Version.v5_6_21
import org.scalacheck.Shrink

import scala.concurrent.duration._

class BankAccountAggregateSpec
    extends PersistenceSpec(
      "BankAccountAggregateSpec",
      ConfigFactory.load("bank-account-aggregate-spec.conf")
    )
    with BankAccountSpecSupport {

  import system.dispatcher

  override protected lazy val mySQLdConfig: MySQLdConfig = MySQLdConfig(
    version = v5_6_21,
    port = Some(12345),
    userWithPassword = Some(UserWithPassword("bank", "passwd")),
    timeout = Some((30 seconds) * sys.env.getOrElse("SBT_TEST_TIME_FACTOR", "1").toDouble)
  )

  implicit val mat = ActorMaterializer()

//  "The BankAccountAggregate" - {
//    "should deposit to BankAccount" in {
//      implicit val noShrink: Shrink[(String, Money)] = Shrink.shrinkAny
//      forAll(bankAccountNameAndDepositMoneyGen) { depositMoney =>
//        val ba = system.actorOf(BankAccountAggregate.props, BankAccountAggregate.name())
//
//        ba ! DepositRequest(depositMoney)
//        expectMsgClass(classOf[DepositSucceeded]).bankAccountId shouldBe id
//      }
//    }
//    "should replay a state of BankAccountAggregate after kill BankAccountAggregate" in {
//      implicit val noShrink: Shrink[(String, Money, Money)] = Shrink.shrinkAny
//      forAll(bankAccountNameAndDepositMoneyGen) {
//        case (depositMoney) =>
//          val id = bankAccountIdGenerator.generateId().futureValue
//
//          val ba1 = system.actorOf(BankAccountAggregate.props, BankAccountAggregate.name(id))
//
//          ba1 ! DepositRequest(id, depositMoney)
//          expectMsgClass(classOf[DepositSucceeded]).bankAccountId shouldBe id
//
//          ba1 ! GetBalanceRequest(id)
//          val getBalanceResponse1 = expectMsgClass(classOf[GetBalanceResponse])
//          getBalanceResponse1.bankAccountId shouldBe id
//
//          killActors(ba1)
//
//          val ba2 = system.actorOf(BankAccountAggregate.props, BankAccountAggregate.name(id))
//
//          ba2 ! GetBalanceRequest(id)
//          val getBalanceResponse2 = expectMsgClass(classOf[GetBalanceResponse])
//
//          getBalanceResponse2.balance shouldBe getBalanceResponse1.balance
//      }
//    }
//  }
}
