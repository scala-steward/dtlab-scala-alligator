package dtlaboratory.dtlab.actors.functions

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.http.scaladsl.{Http, HttpExt}
import dtlaboratory.dtlab.Conf._
import dtlaboratory.dtlab.HttpSupport
import dtlaboratory.dtlab.models.{DtEvent, DtWebHook, JsonSupport}
import spray.json._

import scala.concurrent.Future

object PostWebhook extends JsonSupport with HttpSupport {

  val http: HttpExt = Http(system)

  def apply(webhook: DtWebHook, event: DtEvent): Future[StatusCode] = {
    val newUri =
      HttpRequest(
        entity = HttpEntity(ContentTypes.`application/json`,
                            event.toJson.compactPrint),
        uri = Uri()
          .withHost(webhook.target.host)
          .withPort(webhook.target.port)
          .withScheme(if (webhook.target.tls) "https" else "http")
          .withPath(Path(webhook.target.path))
      )
    logger.debug(s"posting webhook ${webhook.name} to " + newUri)

    http.singleRequest(newUri).map(_.status)
  }

}
