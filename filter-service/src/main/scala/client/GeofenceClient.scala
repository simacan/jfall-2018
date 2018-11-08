package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

import scala.concurrent.Future

case class GeofenceClient(geofenceUri: Uri)(implicit val system: ActorSystem, materializer: Materializer) {

  import system.dispatcher

  def isInGeofence(lat: Double, lon: Double): Future[Boolean] = {
    val getUri = geofenceUri
      .withQuery(Query(
          "lat" -> lat.toString,
          "lon" -> lon.toString
        )
      )

    import akka.http.scaladsl.unmarshalling.Unmarshaller._

    for {
      httpResponse: HttpResponse <- Http().singleRequest(HttpRequest(
        uri = getUri,
        method = HttpMethods.GET
      ))
      string <- Unmarshal(httpResponse).to[String]
      response <- Unmarshal(string).to[Boolean]
    } yield response
  }
}
