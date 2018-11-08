package kafka;

import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.GPSUpdate;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Consumer that attaches to Kafka, reads messages and de-serializes it to GPSUpdates
 */
public class KafkaGPSConsumer {

    private ActorSystem system;
    private String bootstrapServers;
    private String topic;

    public KafkaGPSConsumer(ActorSystem system, String bootstrapServers, String topic) {
        this.system = system;
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
    }

    private ConsumerSettings<String, String> createConsumerSettings() {
        return ConsumerSettings
                .create(system, new StringDeserializer(), new StringDeserializer())
                .withGroupId("my-group-id")
                .withBootstrapServers(bootstrapServers);
    }

    public Source<GPSUpdate, Consumer.Control> createSource() {
        ConsumerSettings<String, String> consumerSettings = createConsumerSettings();
        return Consumer.plainSource(
                consumerSettings,
                Subscriptions.assignment(new TopicPartition(topic, 0))
        ).map(c -> {
            System.out.println("Got a device");
            String json =  c.value();
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(json, GPSUpdate.class);
        });
    }

}