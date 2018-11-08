package com.simacan.jfall.geofence.api

import com.simacan.jfall.geofence.api.GeofenceAPI.Coordinates
import org.scalatest._

class GeofenceToolsSpec extends WordSpec with Matchers {

  import com.simacan.jfall.geofence.api.http.geofence.GeofenceTools._

  "Geofence tools" should {
    "conclude the location is within the geofence" in {
      checkOnNorthOrientedSquareGeofence(Coordinates(52.01702321126474, 5.6370599365234375)) shouldBe true
    }

    "conclude the location is outside the geofence" in {
      checkOnNorthOrientedSquareGeofence(Coordinates(52.04702321126474, 5.6770599365234375)) shouldBe false
    }
  }

}
