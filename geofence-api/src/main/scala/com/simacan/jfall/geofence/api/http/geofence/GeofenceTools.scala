package com.simacan.jfall.geofence.api.http.geofence

import com.simacan.jfall.geofence.api.GeofenceAPI.Coordinates

object GeofenceTools {

  //This object describes a North oriented, square shaped geofence,
  //thus both upper corners are on the same lat, bottom corners are on the same lat,
  //both left corners on the same lon and both right corners on the same lon.
  case class NorthOrientedSquareGeofence(cornerUpLeft: Coordinates, cornerBottomRight: Coordinates)

  private lazy val northOrientedSquareGeofence: NorthOrientedSquareGeofence =
    GeofenceTools.NorthOrientedSquareGeofence(
      Coordinates(52.00702321126474, 5.6270599365234375),
      Coordinates(52.027729487209015, 5.6667137145996085))

  //This method checks if the given coordinates lie within the northOrientedSquareGeofence
  def checkOnNorthOrientedSquareGeofence(coordinates: Coordinates): Boolean = {
    coordinates.lat >= northOrientedSquareGeofence.cornerUpLeft.lat &&
      coordinates.lat <= northOrientedSquareGeofence.cornerBottomRight.lat &&
      coordinates.lon >= northOrientedSquareGeofence.cornerUpLeft.lon &&
      coordinates.lon <= northOrientedSquareGeofence.cornerBottomRight.lon
  }
}
