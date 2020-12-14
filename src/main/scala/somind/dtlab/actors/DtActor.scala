package somind.dtlab.actors

import akka.persistence._
import com.typesafe.scalalogging.LazyLogging
import somind.dtlab.models._
import somind.dtlab.observe.Observer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object DtActor extends LazyLogging {
  def name: String = this.getClass.getName
}

class DtActor extends DtPersistentActorBase[DtState, Telemetry] {

  override var state: DtState = DtState()

  override def receiveCommand: Receive = {

    case _: TakeSnapshot =>
      takeSnapshot(true)

    case m: DtMsg[Any @unchecked]
        if m.path().trail.nonEmpty && !m
          .path()
          .trail
          .exists(leaf =>
            leaf.typeId == self.path.name && leaf.instanceId == "children" && leaf.trail.isEmpty) =>
      logger.debug(s"${self.path.name} forwarding $m")
      upsert(m)

    case m: GetJrnl =>
      val result = grabJrnl(m.limit, m.offset)
      val sndr = sender()
      result onComplete {
        case Success(r) =>
          sndr ! r
        case Failure(exception) =>
          sndr ! DtErr(exception.getMessage)
      }

    case _: GetOperators =>
      sender() ! operators

    case _: GetChildrenNames =>
      sender ! children

    case op: OperatorMsg =>
      operators = OperatorMap(operators.operators + (op.c.name -> op.c))
      persistAsync(op.c) { _ =>
        sender ! operators
        takeSnapshot()
      }

    case tm: TelemetryMsg =>
      state = DtState(state.state + (tm.c.idx -> tm.c))
      persistAsync(tm.c) { _ =>
        sender ! DtOk()
        takeSnapshot()
      }

    case _: GetState =>
      sender ! state

    case _: SaveSnapshotSuccess =>
    case m =>
      logger.warn(s"unexpected message: $m")
      sender ! None

  }

  override def receiveRecover: Receive = {

    case t: Telemetry =>
      state = DtState(state.state + (t.idx -> t))

    case SnapshotOffer(_, snapshot: DtStateHolder[DtState] @unchecked) =>
      state = snapshot.state
      children = snapshot.children
      operators = snapshot.operators
      Observer("recovered_dt_actor_state_from_snapshot")

    case _: RecoveryCompleted =>
      logger.debug(
        s"${self.path}: Recovery completed. State: $state Children: $children")
      Observer("resurrected_dt_actor")

    case x =>
      logger.warn(s"unexpected recover msg: $x")

  }

}
