import actor.GeofenceActor;
import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.kafka.AutoSubscription;
import akka.kafka.ConsumerSettings;
import akka.kafka.ProducerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.kafka.javadsl.Producer;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.Timeout;
import client.GeofenceClient;
import domain.GPSUpdate;
import kafka.KafkaJSONDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import scala.compat.java8.FutureConverters;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class StreamsMain {

    private static final String KAFKA_TOPIC_OUT = "jfall-filtered-updates";
    private static final String KAFKA_TOPIC_IN = "jfall-gps-updates";
    private static final String GEOFENCE_URI = "http://localhost:9000/coordinates";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static class Tuple<X, Y> {
        final X x;
        final Y y;
        Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String args[]) {

        ActorSystem system = ActorSystem.create("routes");
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        Source<ConsumerRecord<String, GPSUpdate>, Consumer.Control> updatesConsumerSource = createConsumerSource(system);

        Sink<ProducerRecord<String, String>, CompletionStage<Done>> producerSink = createProducerSink(system);

        // initialize the logic required to filter updates
        GeofenceClient geofenceClient = new GeofenceClient(GEOFENCE_URI, system);
        ActorRef geofenceActor = system.actorOf(GeofenceActor.props(geofenceClient));

        final Timeout askTimeout = new Timeout(5, TimeUnit.SECONDS);

        RunnableGraph geofenceDeterminationGraph = updatesConsumerSource
            .map(ConsumerRecord::value)
            .alsoTo(Sink.foreach(a -> System.out.println("processing an item")))
            .mapAsync(5, location -> {
                return FutureConverters.toJava(Patterns.ask(geofenceActor, new GeofenceActor.GeofenceRequest(location), askTimeout))
                        .thenApply(b -> new Tuple<>(location, (Boolean) b));
            })
            .filter(item -> item.y)
            .map(item -> new ProducerRecord<String, String>(KAFKA_TOPIC_OUT, item.x.toJson()))
            .to(producerSink);

        geofenceDeterminationGraph.run(materializer);
    }

    private static Sink<ProducerRecord<String, String>, CompletionStage<Done>> createProducerSink(ActorSystem system) {
        ProducerSettings<String, String> producerSettings = ProducerSettings
                .create(system, new StringSerializer(), new StringSerializer())
                .withBootstrapServers(BOOTSTRAP_SERVERS);

        return Producer.plainSink(producerSettings);
    }

    private static Source<ConsumerRecord<String, GPSUpdate>, Consumer.Control> createConsumerSource(ActorSystem system) {
        ConsumerSettings<String, GPSUpdate> consumerSettings = ConsumerSettings
                .create(system, new StringDeserializer(), new KafkaJSONDeserializer<>(GPSUpdate.class))
                .withGroupId("my-group-id")
                .withBootstrapServers(BOOTSTRAP_SERVERS);

        AutoSubscription subscription = Subscriptions.topics(KAFKA_TOPIC_IN);
        return Consumer.plainSource(consumerSettings, subscription);
    }
}