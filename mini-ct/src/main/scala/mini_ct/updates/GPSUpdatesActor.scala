package mini_ct.updates

import akka.actor.{Actor, Props}
import mini_ct.domain.{GPSUpdate, LatestGPSUpdates}

import scala.concurrent.ExecutionContext

class GPSUpdatesActor extends Actor {

  import GPSUpdatesActor._

  implicit val ec: ExecutionContext = context.dispatcher

  private var latestUpdatesMap = Map.empty[String, GPSUpdate]

  override def receive: Receive = {
    case Init =>
      sender ! Ack

    case gpsUpdate: GPSUpdate =>
      println(s"Received update $gpsUpdate")
      // Update the latest update for the unique provider that came in
      latestUpdatesMap = latestUpdatesMap + (gpsUpdate.uniqueId -> gpsUpdate)
      sender ! Ack


    case GetLatestGPSUpdates =>
      sender ! LatestGPSUpdates(latestUpdatesMap)
  }

}

object GPSUpdatesActor {

  def props: Props = {
    Props(new GPSUpdatesActor)
  }

  // Some objects to signal that this actor can receive new updates to work correctly with a stream
  object Ack
  object Init
  object Complete

  // Message to which this actor send back all latest updates
  object GetLatestGPSUpdates

}
