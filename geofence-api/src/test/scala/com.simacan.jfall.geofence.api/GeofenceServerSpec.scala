package com.simacan.jfall.geofence.api

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.simacan.jfall.geofence.api.GeofenceAPI.routes
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class GeofenceServerSpec extends WordSpec with Matchers with ScalatestRouteTest {

  implicit val timeout = RouteTestTimeout(5.seconds)

  "The GeofenceService" should {

    "return a greeting for a call on the health endpoint" in {
      Get("/health") ~> routes ~> check {
        responseAs[String] shouldEqual "Hello from the jfall-geofence-api"
      }
    }

    "return an answer for a call on the coordinates endpoint" in {
      Get("/coordinates?lat=52.01702321126474&lon=5.6370599365234375") ~> routes ~> check {
        responseAs[String] shouldEqual "true"
      }
    }

    "reject calls on other paths" in {
      Get("/hello") ~> routes ~> check {
        handled shouldBe false
      }
    }
  }
}
