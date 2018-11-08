package kafka

import java.io.IOException

import com.sun.xml.internal.ws.encoding.soap.DeserializationException
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import play.api.libs.json.{Json, Reads}

/**
  * Read GPSUpdates as JSON converted to a byte list and convert them to Scala objects.
  */
class KafkaJSONDeserializer[T >: Null](implicit val reads: Reads[T]) extends Deserializer[T] {
  private[this] val stringDeserializer = new StringDeserializer()

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    stringDeserializer.configure(configs, isKey)
  }

  override def deserialize(topic: String, data: Array[Byte]): T = {
    if (data == null) {
      return null
    }
    try {
      Json.fromJson(Json.parse(stringDeserializer.deserialize(topic, data)))(reads).getOrElse {
        throw new DeserializationException("Error when deserializing byte[] to json type, could not build json type from json")
      }
    } catch {
      case e: IOException =>
        throw new DeserializationException("Error when deserializing byte[] to json type, could not parse input to json")
    }
  }

  override def close(): Unit = {
    stringDeserializer.close()
  }
}
