package play.api.libs.json

import play.api.data.validation.ValidationError
import play.api.i18n.Messages

object RichJsError {

  def toI18NJson(e: JsError)(messages: Messages): JsObject = toI18NJson(e.errors)(messages)

  private def toI18NJson(errors: Seq[(JsPath, Seq[ValidationError])])(messages: Messages): JsObject = {
    val argsWrite = Writes.traversableWrites[Any](Writes.anyWrites)
    errors.foldLeft(Json.obj()) { (obj, error) =>
      obj ++ Json.obj(error._1.toJsonString -> error._2.foldLeft(Json.arr()) { (arr, err) =>
        arr :+ JsString( play.api.i18n.Messages(err.messages, err.args)(messages) )
      })
    }
  }

}