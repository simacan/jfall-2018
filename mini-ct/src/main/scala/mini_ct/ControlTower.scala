package mini_ct

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import mini_ct.api.ControlTowerApi
import mini_ct.kafka.GPSUpdateConsumer
import mini_ct.updates.GPSUpdatesActor

import scala.concurrent.ExecutionContext
import scala.util.Properties

object ControlTower extends App {

  import GPSUpdatesActor._

  val port = 8888
  val bootstrapServers = Properties.envOrElse("BOOTSTRAP_URL", "localhost:9092")
  val topic = if(args.length == 1) args(0) else Properties.envOrElse("TOPIC", "jfall-gps-updates")

  println("Starting the Control Tower")

  // Some setup to get Akka HTTP up and running
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()

  // Actor
  val supervisorActor: ActorRef = system.actorOf(GPSUpdatesActor.props)

  // Kafka
  val GPSUpdateConsumer = new GPSUpdateConsumer(system, bootstrapServers, topic)
  val supervisorSink = Sink.actorRefWithAck(supervisorActor, Init, Ack, Complete)

  startApplication()
  createEndpoints()

  /**
    * Start reading from Kafka and process every sensor update
    */
  def startApplication() = {
    GPSUpdateConsumer.source
      .to(supervisorSink)
      .run
  }

  /**
    * Create endpoints for this application so you can retrieve updates
    */
  def createEndpoints()= {
    println("Start endpoint")
    val api = new ControlTowerApi(supervisorActor)
    Http().bindAndHandle(
      handler = api.route,
      interface = "0.0.0.0",
      port = port
    )
  }
}
