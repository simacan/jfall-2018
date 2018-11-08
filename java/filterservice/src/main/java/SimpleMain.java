import actor.GeofenceActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import client.GeofenceClient;
import domain.GPSUpdate;
import kafka.KafkaJSONDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import scala.concurrent.Future;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SimpleMain {

    private static KafkaProducer<String, String> producer;
    private static final String KAFKA_TOPIC_OUT = "jfall-filtered-updates";
    private static final String KAFKA_TOPIC_IN = "jfall-gps-updates";
    private static final String GEOFENCE_URI = "http://localhost:9000/coordinates";

    public static void main(String args[]) {
        ActorSystem system = ActorSystem.create("routes");

        createProducer();
        KafkaConsumer<String, GPSUpdate> consumer = createConsumer();

        final Timeout askTimeout = new Timeout(5, TimeUnit.SECONDS);

        // initialize the logic required to filter updates
        GeofenceClient geofenceClient = new GeofenceClient(GEOFENCE_URI, system);
        ActorRef geofenceActor = system.actorOf(GeofenceActor.props(geofenceClient));

        Duration kafkaPollTimeout = Duration.ofSeconds(1);

        while (true) {
            ConsumerRecords<String, GPSUpdate> records = consumer.poll(kafkaPollTimeout);
            for (ConsumerRecord<String, GPSUpdate> record : records) {
                GPSUpdate gpsUpdate = record.value();
                Future<Object> futureResponse = Patterns.ask(geofenceActor, new GeofenceActor.GeofenceRequest(gpsUpdate), askTimeout);

                futureResponse.onComplete(new OnComplete<Object>() {
                    @Override
                    public void onComplete(Throwable failure, Object success) {
                        if (failure != null) {
                            System.out.println("Timeout occurred!");
                        } else {
                            if((Boolean) success) {
                                System.out.println("In geofence!");
                                produceItem(gpsUpdate);
                            } else {
                                System.out.println("Not in geofence!");
                            }
                        }
                    }
                }, system.dispatcher());
            }
        }
    }

    private static KafkaConsumer<String, GPSUpdate> createConsumer() {
        // create the consumer
        Properties consumerProperties = new Properties();
        consumerProperties.put("bootstrap.servers", "localhost:9092");
        consumerProperties.put("group.id", "filter-service-actor");
        KafkaConsumer<String, GPSUpdate> consumer = new KafkaConsumer<>(consumerProperties, new StringDeserializer(), new KafkaJSONDeserializer<>(GPSUpdate.class));
        consumer.subscribe(Collections.singleton(KAFKA_TOPIC_IN));
        return consumer;
    }

    private static void createProducer() {
        // create the producer
        Properties producerProperties = new Properties();
        producerProperties.put("bootstrap.servers", "localhost:9092");
        producerProperties.put("key.serializer", StringSerializer.class);
        producerProperties.put("value.serializer", StringSerializer.class);
        producer = new KafkaProducer<>(producerProperties);
    }

    private static void produceItem(GPSUpdate location) {
        producer.send(new ProducerRecord<>(KAFKA_TOPIC_OUT, location.toJson()));
    }
}
