import java.util.Properties

import actor.GeofenceActor
import actor.GeofenceActor.GeofenceRequest
import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import client.GeofenceClient
import domain.GPSUpdate
import kafka.KafkaJSONDeserializer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object SimpleMain extends App {

  val kafkaTopicIn = "jfall-gps-updates"
  val kafkaTopicOut = "jfall-filtered-updates"
  val geofenceUri = Uri("http://localhost:9000/coordinates")

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // create the producer
  val producerProperties = new Properties()
  producerProperties.put("bootstrap.servers", "localhost:9092")
  producerProperties.put("key.serializer", classOf[StringSerializer].getName)
  producerProperties.put("value.serializer", classOf[StringSerializer].getName)
  val producer = new KafkaProducer[String, String](producerProperties)


  // create the consumer
  val consumerProperties = new Properties()
  consumerProperties.put("bootstrap.servers", "localhost:9092")
  consumerProperties.put("group.id", "filter-service-actor")
  val consumer = new KafkaConsumer[String, GPSUpdate](consumerProperties, new StringDeserializer, new KafkaJSONDeserializer[GPSUpdate]())
  consumer.subscribe(List(kafkaTopicIn).asJava)

  import system.dispatcher
  implicit val timeout: Timeout = Timeout(5 seconds)

  // initialize the logic required to filter updates
  val geofenceClient = GeofenceClient(geofenceUri)
  val geofenceActor = system.actorOf(GeofenceActor.props(geofenceClient))

  val kafkaPollTimeout = 1000

  println("Started filter service")

  while (true) {
    val records = consumer.poll(kafkaPollTimeout)
    records.forEach(record => {

      val gpsUpdate = record.value()
      geofenceActor.ask(GeofenceRequest(gpsUpdate)).onComplete {
        case Success(true) =>
          println("in geofence!")
          produceItem(gpsUpdate)
        case Success(false) => println("Not in geofence")
        case Failure(_) => println("Timeout occurred!")
      }

    })
  }

  def produceItem(loc: GPSUpdate) = {
    producer.send(new ProducerRecord[String, String](kafkaTopicOut, Json.toJson(loc).toString))
  }
}
