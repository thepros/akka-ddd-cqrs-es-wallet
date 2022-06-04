package com.github.j5ik2o.bank.adaptor.aggregate

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, RecoveryCompleted, SaveSnapshotSuccess, SnapshotOffer }
import cats.implicits._
import com.github.j5ik2o.bank.domain.model._
import com.github.j5ik2o.bank.domain.model.BankAccount.{ BankAccountError, InvalidStateError }
import pureconfig._

object BankAccountAggregate {

  def props: Props = Props(new BankAccountAggregate())

  def name: String = "BankAccount"

  final val AggregateName = "BankAccount"

  object Protocol {

    sealed trait BankAccountCommandRequest {}

    sealed trait BankAccountCommandResponse {}

    // ---

    case class GetBalanceRequest() extends BankAccountCommandRequest

    case class GetBalanceResponse(balance: BigDecimal) extends BankAccountCommandRequest

    // ---

    case class DepositRequest(deposit: BigDecimal) extends BankAccountCommandRequest

    sealed trait DepositResponse extends BankAccountCommandResponse

    case class DepositSucceeded() extends DepositResponse

    case class DepositFailed(error: BankAccountError) extends DepositResponse

  }

  implicit class EitherOps(val self: Either[BankAccountError, BankAccount]) {
    def toSomeOrThrow: Option[BankAccount] = self.fold(error => throw new IllegalStateException(error.message), Some(_))
  }

}

class BankAccountAggregate extends PersistentActor with ActorLogging {

  import BankAccountAggregate._
  import BankAccountAggregate.Protocol._

  private val config = loadConfigOrThrow[BankAccountAggregateConfig](
    context.system.settings.config.getConfig("bank.interface.bank-account-aggregate")
  )

  context.setReceiveTimeout(config.receiveTimeout)

  override def persistenceId: String = s"$AggregateName-${self.path.name}"

  private var stateOpt: Option[BankAccount] = applyState()

  private def tryToSaveSnapshot(): Unit =
    if (lastSequenceNr % config.numOfEventsToSnapshot == 0) {
      foreachState(saveSnapshot)
    }

  private def applyState(): Option[BankAccount] =
    Some(BankAccount(BankAccount.DEFAULT_MONEY_ZERO))

  private def mapState(
      f: BankAccount => Either[BankAccountError, BankAccount]
  ): Either[BankAccountError, BankAccount] =
    for {
      state    <- Either.fromOption(stateOpt, InvalidStateError())
      newState <- f(state)
    } yield newState

  private def foreachState(f: BankAccount => Unit): Unit =
    Either.fromOption(stateOpt, InvalidStateError()).foreach(f)

  /**
    * Recovery handler that receives persisted events during recovery. If a state snapshot
    * has been captured and saved, this handler will receive a [[SnapshotOffer]] message
    * followed by events that are younger than the offered snapshot.
    */
  override def receiveRecover: Receive = {
    case event: BankAccountDeposited =>
      stateOpt = mapState(_.deposit(event.deposit, event.occurredAt)).toSomeOrThrow
    case SnapshotOffer(_, _state: BankAccount) =>
      stateOpt = Some(_state)
    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveRecover: SaveSnapshotSuccess succeeded: $metadata")
    case RecoveryCompleted =>
      log.debug(s"Recovery completed: $persistenceId")
  }

  /**
    * Command handler. Typically validates commands against current state (and/or by
    * communication with other actors). On successful validation, one or more events are
    * derived from a command and these events are then persisted by calling `persist`.
    */
  override def receiveCommand: Receive = {
    case GetBalanceRequest() =>
      foreachState { state =>
        sender() ! GetBalanceResponse(state.balance)
      }

    case DepositRequest(deposit) =>
      mapState(_.deposit(deposit)) match {
        case Left(error) =>
          sender() ! DepositFailed(error)
        case Right(newState) =>
          persist(BankAccountDeposited(deposit)) { _ =>
            stateOpt = Some(newState)
            sender() ! DepositSucceeded()
            tryToSaveSnapshot()
          }
      }

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveCommand: SaveSnapshotSuccess succeeded: $metadata")
  }
}
