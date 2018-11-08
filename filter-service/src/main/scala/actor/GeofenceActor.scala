package actor

import akka.actor.{Actor, Props}
import client.GeofenceClient
import domain.GPSUpdate

class GeofenceActor(geofenceClient: GeofenceClient) extends Actor {

  import GeofenceActor._
  import context.dispatcher

  override def receive: Receive = {
    case GeofenceRequest(update) =>
      val senderToReturnTo = sender()
      geofenceClient.isInGeofence(update.latitude, update.longitude)
        .foreach{
          case true => senderToReturnTo ! true
          case false => senderToReturnTo ! false
        }
  }
}

object GeofenceActor {
  case class GeofenceRequest(gpsUpdate: GPSUpdate)

  def props(geofenceClient: GeofenceClient) = Props(new GeofenceActor(geofenceClient))
}
