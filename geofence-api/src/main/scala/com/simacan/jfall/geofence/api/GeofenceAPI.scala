package com.simacan.jfall.geofence.api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{HttpApp, Route}
import com.simacan.jfall.geofence.api.http.geofence.GeofenceTools
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

object GeofenceAPI extends HttpApp {

  case class Coordinates(lat: Double, lon: Double)

  override def routes: Route = cors() {
    path("health") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Hello from the jfall-geofence-api"))
      }
    } ~
    path("coordinates") {
      get {
        parameters((
          'lat.as[Double],
          'lon.as[Double],
        )) {
        case (lat, lon) =>
          //It waits some time to be deliberately slow for the demo, like it is an api with more complex algorithms and/or dependencies.
          Thread.sleep(100)
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`,
            GeofenceTools.checkOnNorthOrientedSquareGeofence(Coordinates(lat, lon)).toString))
        }
      }
    }
  }
}

object Main extends App {
  GeofenceAPI.startServer("localhost", 9000)
  println("JFALL Geofence API started")
}