package mini_ct.domain

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

/**
  * Simple container of gps update data
  *
  * @param uniqueId The unique ID by which a sender or device can be coupled to the update of its location
  * @param latitude The latitude of the location update
  * @param longitude The longitude of the location update
  * @param timestamp The time stamp at which the update occurred
  */
case class GPSUpdate(uniqueId: String, latitude: Double, longitude: Double, timestamp: Long)

object GPSUpdate {

  // This allows a GPSUpdate to be converted to and from JSON.
  implicit val gpsUpdateFormat: Format[GPSUpdate] = (
    (JsPath \ "uniqueId").format[String] and
      (JsPath \ "latitude").format[Double] and
      (JsPath \ "longitude").format[Double] and
      (JsPath \ "timestamp").format[Long]
    )(GPSUpdate.apply, unlift(GPSUpdate.unapply))

}