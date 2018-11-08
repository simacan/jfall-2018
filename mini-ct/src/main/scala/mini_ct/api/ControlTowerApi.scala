package mini_ct.api

import akka.actor.ActorRef
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import mini_ct.updates.GPSUpdatesActor
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import mini_ct.domain._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.util.{Failure, Success}

/**
  * Create an endpoint so a frontend can retrieve the latest updates per unique sender
  */
class ControlTowerApi(supervisor: ActorRef) extends PlayJsonSupport {

  import GPSUpdatesActor._

  val route: Route = cors() {
    pathPrefix("health") {
      get {
        complete(StatusCodes.OK)
      }
    } ~
      pathPrefix("getLatestUpdates") {
        get {
          // How long are we willing to wait on a response
          import concurrent.duration._
          implicit val timeout: Timeout = Timeout(5.second)

          onComplete((supervisor ? GetLatestGPSUpdates).mapTo[LatestGPSUpdates]) {
            case Success(value) =>
              encodeResponseWith(Gzip) {
                complete(StatusCodes.OK -> value)
              }
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError -> ex)
          }
        }
      }
  }
}
