package com.github.j5ik2o.bank.adaptor.serialization

import java.time.Instant

import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import com.github.j5ik2o.bank.adaptor.util.ActorSpec
import com.github.j5ik2o.bank.domain.model._
import com.typesafe.config.ConfigFactory

class BankAccountEventJSONSerializerSpec
    extends ActorSpec(
      ActorSystem("BankAccountEventJSONSerializerSpec", ConfigFactory.load("bank-account-aggregate-spec.conf"))
    ) {
  val extension = SerializationExtension(system)

  "BankAccountEventJSONSerializer" - {
    "should encode DepositEvent" in {
      val serializer    = extension.serializerFor(classOf[BankAccountDeposited])
      val now           = Instant.now
      val expectedEvent = BankAccountDeposited(100, now)
      val byteArray     = serializer.toBinary(expectedEvent)
      val event         = serializer.fromBinary(byteArray, Some(classOf[BankAccountDeposited]))
      event shouldBe expectedEvent
    }
  }
}
