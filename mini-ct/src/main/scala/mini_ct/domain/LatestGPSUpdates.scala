package mini_ct.domain

import play.api.libs.json._

case class LatestGPSUpdates(uniqueUpdates: Map[String, GPSUpdate])

object LatestGPSUpdates {

  implicit val mapWrites: Writes[LatestGPSUpdates] = new Writes[LatestGPSUpdates] {
    def writes(updates: LatestGPSUpdates): JsValue =
      Json.arr(updates.uniqueUpdates.values)
  }

}