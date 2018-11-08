package http_endpoint_service.kafka

import org.apache.kafka.common.serialization.{Serializer, StringSerializer}
import play.api.libs.json.{Json, Writes}

/**
  * To be able to write JSON to Kafka we need to translate the JSON to an array of bytes.
  */
class KafkaJSONSerializer[T: Writes] extends Serializer[T] {
  private[this] val stringSerializer = new StringSerializer()

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    stringSerializer.configure(configs, isKey)
  }

  override def serialize(topic: String, data: T): Array[Byte] = {
    if (data != null)
      stringSerializer.serialize(topic, Json.toJson(data).toString)
    else
      null
  }

  override def close(): Unit = {
    stringSerializer.close()
  }
}
