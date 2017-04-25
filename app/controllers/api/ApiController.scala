package controllers.api

import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import play.api.i18n.I18nSupport

import play.api.libs.concurrent.Execution.Implicits.defaultContext


case class ApiException(msg: String, keys:Any*) extends Exception(s"api exception happened: ${msg}, ${keys}")

case class ApiError(message:Option[String] = None, obj: Option[JsValue] = None)
object ApiError {

  def jsErrorWrites(messages:Messages) = Writes[JsError] { jsonError =>
    RichJsError.toI18NJson(jsonError)(messages)
  }

  implicit val writes = Writes[ApiError] { r =>
    Json.obj("error" -> r.obj, "status" -> "error", "message" -> r.message)
  }

  def withObj[A](obj: A)(implicit writer:Writes[A]) = ApiError(obj = Some(writer.writes(obj)))

  def withJsError(jsError: JsError)(messages:Messages) = {
    withObj(jsError)(jsErrorWrites(messages))
  }

  def withMessage(messages:Messages)(msg: String, keys:Any*) = {
    ApiError(message = Some(play.api.i18n.Messages(msg, keys:_*)(messages)))
  }
}

abstract class ApiController extends Controller with I18nSupport {

  val controllerName = this.getClass.getSimpleName.filter(_ != '$')
  def logger:Logger = Logger(controllerName)

  def apiJson[A](implicit reader: Reads[A]): BodyParser[A] = {
    BodyParser("json reader") { request =>
      import play.api.libs.iteratee.Execution.Implicits.trampoline

      parse.json(request).mapFuture {
        case Left(simpleResult) => {
          logger.debug(s"simpleResult: $simpleResult")
          Future.successful(Left(simpleResult))
        }
        case Right(jsValue) => {
          logger.debug(s"Received Json: ${Json.prettyPrint(jsValue)}")
          jsValue.validate(reader) map { a =>
            Future.successful(Right(a))
          } recoverTotal { jsError =>
            val messages = request2Messages(request)
            val json = Json.toJson(ApiError.withJsError(jsError)(messages))
            logger.debug(s"BadRequest: $json")
            Future.successful(Left(BadRequest(json)))
          }
        }
      }
    }
  }

}