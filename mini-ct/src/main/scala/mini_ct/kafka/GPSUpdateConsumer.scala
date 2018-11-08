package mini_ct.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import mini_ct.domain.GPSUpdate
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

/**
  * Simple Kafka consumer that reads gpsUpdates from Kafka as JSON and create Scala objects from them.
  */
class GPSUpdateConsumer(system: ActorSystem, bootstrapServers: String, topic: String) {

  def source: Source[GPSUpdate, Consumer.Control] = {

    val msgDeserializer: Deserializer[GPSUpdate] = new KafkaJSONDeserializer[GPSUpdate]
    val consumerSettings =
      ConsumerSettings(system, new StringDeserializer, msgDeserializer)
      .withGroupId("my-group-id")
      .withBootstrapServers(bootstrapServers)

    val subscription = Subscriptions.topics(topic)

    Consumer
      .plainSource(consumerSettings, subscription)
      .map(consumerRecord => consumerRecord.value())
  }

}
