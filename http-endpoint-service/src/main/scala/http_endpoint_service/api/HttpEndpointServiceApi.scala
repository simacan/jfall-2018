package http_endpoint_service.api

import java.util.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import http_endpoint_service.domain.GPSUpdate
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

/**
  * Create endpoints and send all incoming messages to the actor given.
  */
class HttpEndpointServiceApi(producer: KafkaProducer[String, String], kafkaTopicOut: String)(
    implicit
    system: ActorSystem,
    mat: Materializer,
    ex: ExecutionContext
) extends PlayJsonSupport {

  import GPSUpdate._

  val route: Route = cors() {
    pathPrefix("health") {
      get {
        complete(StatusCodes.OK)
      }
    } ~
      pathPrefix("addGPSUpdate") {
        post {
          entity(as[GPSUpdate]) { gpsUpdate =>
            println(s"received: $gpsUpdate")

            // Send the gps update to the actor who puts it on Kafka
            produceItem(gpsUpdate)
            complete(StatusCodes.OK)
          }
        }
      } ~
      pathPrefix("addMultipleUpdates") {
        post {
          entity(as[Seq[GPSUpdate]]) { gpsUpdates =>
            println(s"received $gpsUpdates")
            // Send each update as a separate message to the actor putting it on Kafka
            gpsUpdates.foreach(gpsUpdate => produceItem(gpsUpdate))
            complete(StatusCodes.OK)
          }
        }
      }
  }

  def produceItem(loc: GPSUpdate): Future[RecordMetadata] = {
    println(s"Producing: $loc")
    producer.send(new ProducerRecord[String, String](kafkaTopicOut, Json.toJson(loc).toString))
  }
}
