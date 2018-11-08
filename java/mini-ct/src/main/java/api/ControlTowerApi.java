package api;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import domain.GPSUpdate;

import java.util.HashMap;
import java.util.concurrent.CompletionStage;

import static akka.pattern.PatternsCS.ask;
import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;

public class ControlTowerApi extends AllDirectives {

    private ActorRef gpsUpdateActor;

    public ControlTowerApi(ActorRef gpsUpdateActor) {
        this.gpsUpdateActor = gpsUpdateActor;
    }

    public Route getRoute() {

        return cors(() -> route(
                pathPrefix("health", () ->
                        get(() -> complete(StatusCodes.OK))
                ),
                pathPrefix("getLatestUpdates", () ->
                        get(() -> {
                            CompletionStage<HashMap<String, GPSUpdate>> updates = ask(gpsUpdateActor, "getLatestUpdates", 5000).thenApply(HashMap.class::cast);
                            return completeOKWithFuture(updates, Jackson.marshaller());
                        })
                ))
        );
    }
}
