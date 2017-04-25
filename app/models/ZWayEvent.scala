package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ZWayEvent(
  foreignDeviceId:String,
  status:Option[String] = None,
  eventType: Option[String] = None,
  updateTime:Option[DateTime] = None,
  value: Option[String] = None,
  deviceType: Option[String] = None,
  deviceName: Option[String] = None,
  cmdClass: Option[Int] = None,
  instanceId: Option[Int] = None,
  meterType: Option[Int] = None,
  sensorType: Option[String] = None,
  scaleUnit: Option[String] = None,
  probeType: Option[String] = None
)

object ZWayEvent {

  implicit val reads: Reads[ZWayEvent] = (
    (JsPath \ "foreignDeviceId").read[String] and
    (JsPath \ "status").readNullable[String] and
    (JsPath \ "eventType").readNullable[String] and
    (JsPath \ "updateTime").readNullable[Int].map(epochToDate(_)) and
    (JsPath \ "value").readNullable[String] and
    (JsPath \ "deviceType").readNullable[String] and
    (JsPath \ "deviceName").readNullable[String] and
    (JsPath \ "cmdClass").readNullable[Int] and
    (JsPath \ "instanceId").readNullable[Int] and
    (JsPath \ "meterType").readNullable[Int] and
    (JsPath \ "sensorType").readNullable[String] and
    (JsPath \ "scaleUnit").readNullable[String] and
    (JsPath \ "probeType").readNullable[String]
  )( ZWayEvent.apply _ )

  val epochWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(d: DateTime): JsValue = {
      val sec:Long = d.getMillis / 1000
      JsNumber(sec)
    }
  }

  implicit var writes: Writes[ZWayEvent] = (
    (JsPath \ "foreignDeviceId").write[String] and
    (JsPath \ "status").writeNullable[String] and
    (JsPath \ "eventType").writeNullable[String] and
    (JsPath \ "updateTime").writeNullable[DateTime](epochWrites) and
    (JsPath \ "value").writeNullable[String] and
    (JsPath \ "deviceType").writeNullable[String] and
    (JsPath \ "deviceName").writeNullable[String] and
    (JsPath \ "cmdClass").writeNullable[Int] and
    (JsPath \ "instanceId").writeNullable[Int] and
    (JsPath \ "meterType").writeNullable[Int] and
    (JsPath \ "sensorType").writeNullable[String] and
    (JsPath \ "scaleUnit").writeNullable[String] and
    (JsPath \ "probeType").writeNullable[String]
  ) ( unlift(ZWayEvent.unapply) )

  private def epochToDate(epoch:Option[Int]): Option[DateTime] = {
    epoch match {
      case Some(e) => {
        val milis = e.toLong*1000
        Some(new DateTime(milis))
      }
      case None => None
    }
  }

}