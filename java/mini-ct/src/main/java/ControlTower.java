import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import api.ControlTowerApi;
import domain.GPSUpdate;
import kafka.KafkaGPSConsumer;
import updates.GPSUpdatesActor;

public class ControlTower {
    private static final int PORT = 9081;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "jfall-gps-updates";

    public static void main(String args[]) {

        System.out.println("Starting the Control Tower");

        // Default Akka setup
        ActorSystem system = ActorSystem.create("routes");
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Http http = Http.get(system);

        // Create an actor that keeps track of the latest updates per device
        Props actorProps = Props.create(GPSUpdatesActor.class);
        ActorRef gpsUpdatesActor = system.actorOf(actorProps);

        // Create a Kafka consumer that reads messages from Kafka and sends them to the actor
        KafkaGPSConsumer consumer = new KafkaGPSConsumer(system, BOOTSTRAP_SERVERS, TOPIC);
        Sink<GPSUpdate, NotUsed> sink = Sink.actorRef(gpsUpdatesActor, "done");
        consumer.createSource().to(sink).run(materializer);

        // Create an endpoint so you can retrieve the latest updates per device
        System.out.println("Start endpoint on port " + PORT);
        ControlTowerApi api = new ControlTowerApi(gpsUpdatesActor);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = api.getRoute().flow(system, materializer);
        http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", PORT), materializer);
    }
}

