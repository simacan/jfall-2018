package client;

import akka.actor.ActorSystem;
import akka.dispatch.ExecutionContexts;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import java.util.concurrent.CompletionStage;

public class GeofenceClient {

    private String geofenceUri;
    private ActorSystem system;
    private Materializer materializer;

    public GeofenceClient(String geofenceUri, ActorSystem system) {
        this.geofenceUri = geofenceUri;
        this.system = system;
        this.materializer = ActorMaterializer.create(system);
    }

    public CompletionStage<Boolean> isInGeofence(Double lat, Double lon) {
        return Http.get(system)
            .singleRequest(HttpRequest.create(geofenceUri + "?lat=" + lat + "&lon=" + lon))
            .thenCompose(response -> Unmarshaller.entityToString().unmarshal(response.entity(), ExecutionContexts.global(), materializer))
            .thenApply(Boolean::parseBoolean);
    }
}