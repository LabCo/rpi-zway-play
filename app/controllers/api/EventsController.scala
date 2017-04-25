package controllers.api

import javax.inject.{Inject, Singleton}

import models.ZWayEvent
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Action

import scala.concurrent.Future

@Singleton
class EventsController @Inject()(
  val messagesApi: MessagesApi
) extends ApiController {

  var events:List[ZWayEvent] = List.empty

  def home() = Action {
    Ok("RPi Play Framework ZWay demo")
  }

  def all() = Action {
    Ok(Json.toJson(events))
  }

  def create() = Action.async(apiJson[ZWayEvent]) { req =>
    val event = req.body
    logger.debug("postEvent: " + event)

    events = events :+ event
    Future.successful(Ok(Json.obj()))
  }

}