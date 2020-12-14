package somind.dtlab.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.pattern._
import com.typesafe.scalalogging.LazyLogging
import somind.dtlab.Conf._
import somind.dtlab.HttpSupport
import somind.dtlab.models._
import somind.dtlab.observe.Observer
import somind.dtlab.routes.functions._

import scala.language.postfixOps

/**
  * Enables CRUD for actors and their states.
  *
  * Actors are automatically created if you post telemetry to
  * them.  If they already exist, the new telemetry gets added
  * to their journal and is reflected in the current state view.
  *
  * (When implemented), DELETE will remove the journals of the actor.
  *
  * Telemetry may be expressed in 3 ways:
  *   1. indexed (the native internal format)
  *   2. named
  *   3. pathed
  *
  * When addressing an actor, suffix the path with named or pathed to
  * use the named or pathed telemetry model.
  *
  */
object ActorApiRoute
    extends JsonSupport
    with LazyLogging
    with HttpSupport
    with GetJrnlTrait
    with OperatorApiTrait
    with StateApiTrait
    with GetChildNamesTrait
    with Directives {

  def applyTelemetryMsg(dtp: DtPath, telemetry: Telemetry): Route = {
    onSuccess(dtDirectory ask TelemetryMsg(dtp, telemetry)) {
      case DtOk() =>
        Observer("actor_route_post_success")
        complete(StatusCodes.Accepted)
      case DtErr(emsg) =>
        Observer("actor_route_post_failure")
        logger.debug(s"unable to post telemetry: $emsg")
        complete(StatusCodes.BadRequest, emsg)
      case e =>
        Observer("actor_route_post_unk_err")
        logger.warn(s"unable to handle: $e")
        complete(StatusCodes.InternalServerError)
    }
  }

  private def applyProps(segs: List[String],
                         limit: Option[Int],
                         offset: Option[Int]): Route = {
    (limit, offset) match {
      case _ if limit.nonEmpty =>
        handleGetJrnl(segs, limit.get, offset.getOrElse(0))
      case _ =>
        handleStateApi(segs)
    }
  }

  def apply: Route =
    pathPrefix("actor") {
      parameters('limit.as[Int].?, 'offset.as[Int] ?) { (limit, offset) =>
        {
          pathPrefix(Segments(20)) { segs: List[String] =>
            applyProps(segs, limit, offset)
          } ~
            pathPrefix(Segments(18) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(19)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(18)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(16) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(17)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(16)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(14) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(15)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(14)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(12) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(13)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(12)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(10) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(11)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(10)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(8) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(9)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(8)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(6) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(7)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(6)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(4) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(5)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(4)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(2) / "operator" ~ Slash.?) {
              segs: List[String] =>
                handleOperatorApi(segs)
            } ~
            pathPrefix(Segments(3)) { segs: List[String] =>
              handleGetChildNames(segs)
            } ~
            pathPrefix(Segments(2)) { segs: List[String] =>
              applyProps(segs, limit, offset)
            } ~
            pathPrefix(Segments(1)) { segs: List[String] =>
              handleGetChildNames(segs)
            }
        }
      }

    }

}
