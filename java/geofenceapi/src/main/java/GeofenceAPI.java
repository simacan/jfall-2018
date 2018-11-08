import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

public class GeofenceAPI extends AllDirectives {
    private static final int PORT = 9000;

    public static void main(String args[]) {
        ActorSystem system = ActorSystem.create("routes");
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Http http = Http.get(system);

        final GeofenceAPI geofenceAPI = new GeofenceAPI();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = geofenceAPI.createRoute().flow(system, materializer);

        System.out.println("Start endpoint on port " + PORT);
        http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", PORT), materializer);
    }

    private Route createRoute() {
        return route(
            get(() ->
                pathPrefix("health", () ->
                    complete(StatusCodes.OK, "Hello from the jfall-geofence-api")
                )
            ),
            get(() ->
                path("coordinates", () ->
                    parameter(StringUnmarshallers.DOUBLE, "lat", lat ->
                        parameter(StringUnmarshallers.DOUBLE, "lon", lon -> {
                            //It waits some time to be deliberately slow for the demo, like it is an api with more complex algorithms and/or dependencies.
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return complete(StatusCodes.OK, String.valueOf(GeofenceTools.checkOnNorthOrientedSquareGeofence(new Coordinates(lat, lon))));
                        })
                    )
                )
            )
        );
    }
}
