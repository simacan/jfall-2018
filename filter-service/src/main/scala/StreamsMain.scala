import actor.GeofenceActor
import actor.GeofenceActor.GeofenceRequest
import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import client.GeofenceClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import play.api.libs.json._
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import domain.GPSUpdate
import kafka.KafkaJSONDeserializer

import scala.concurrent.duration._

object StreamsMain extends App {

  val kafkaTopicIn = "jfall-gps-updates"
  val kafkaTopicOut = "jfall-filtered-updates"
  val geofenceUri = Uri("http://localhost:9000/coordinates")

  // just print the error and attempt to resume
  val decider: Supervision.Decider = (ex: Throwable) => {
    println("Uncaught exception in akka kafka stream. Resuming.", ex)
    Supervision.Resume
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )
  import system.dispatcher

  val geofenceClient = GeofenceClient(geofenceUri)
  val geofenceActor = system.actorOf(GeofenceActor.props(geofenceClient))

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new KafkaJSONDeserializer[GPSUpdate])
  val subscription = Subscriptions.topics(kafkaTopicIn)
  val updatesConsumerSource = Consumer.plainSource(consumerSettings, subscription)

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
  val producerSink = Producer.plainSink(producerSettings)


  implicit val timeout: Timeout = Timeout(5 seconds)

  val geofenceDeterminationGraph = updatesConsumerSource
    .map(record => record.value())

    .alsoTo(Sink.foreach(_ => println("processing an item")))

    .mapAsync(5)(location => geofenceActor.ask(GeofenceRequest(location)).map(a => (location, a.asInstanceOf[Boolean])))
    .collect { case (update, true) => update }

    .map(value => new ProducerRecord[String, String](kafkaTopicOut, Json.toJson(value).toString))
    .to(producerSink)

  geofenceDeterminationGraph.run()
}
