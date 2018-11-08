package http_endpoint_service

import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import http_endpoint_service.api.HttpEndpointServiceApi
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContext

/**
  * The Main of this application. Create endpoints to receive gps updates, setup a connection with Kafka and put
  * the gps updates there.
  */
object HttpEndpointService extends App {

  val port = 8080
  val bootstrapServers = scala.util.Properties.envOrElse("BOOTSTRAP_URL", "localhost:9092")
  val topic = scala.util.Properties.envOrElse("TOPIC", "jfall-gps-updates")

  println("Starting the HTTP endpoint service")

  // Some setup to get Akka HTTP up and running
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()

  // create the producer
  val producerProperties = new Properties()
  producerProperties.put("bootstrap.servers", bootstrapServers)
  producerProperties.put("key.serializer", classOf[StringSerializer].getName)
  producerProperties.put("value.serializer", classOf[StringSerializer].getName)
  val producer = new KafkaProducer[String, String](producerProperties)

  startServer()

  /**
    * Create endpoints and make them available so you can retrieve gps updates.
    */
  def startServer()= {
    println(s"Start endpoint on port $port")
    val api = new HttpEndpointServiceApi(producer, topic)
    Http().bindAndHandle(
      handler = api.route,
      interface = "0.0.0.0",
      port = port
    )
  }

}
