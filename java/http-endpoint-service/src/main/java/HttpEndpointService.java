import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import api.HttpEndpointServiceApi;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class HttpEndpointService {
    private static final int PORT = 8080;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "jfall-gps-updates";

    public static void main(String args[]) {

        System.out.println("Starting the HTTP endpoint service");
        ActorSystem system = ActorSystem.create("routes");
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Http http = Http.get(system);

        // create the producer
        Properties producerProperties = new Properties();
        producerProperties.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        producerProperties.put("key.serializer", StringSerializer.class);
        producerProperties.put("value.serializer", StringSerializer.class);
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties);

        HttpEndpointServiceApi api = new HttpEndpointServiceApi(producer, TOPIC);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = api.getRoute().flow(system, materializer);

        System.out.println("Start endpoint on port " + PORT);
        http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", PORT), materializer);
    }
}
