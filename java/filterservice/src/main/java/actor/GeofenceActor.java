package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import client.GeofenceClient;
import domain.GPSUpdate;

public class GeofenceActor extends AbstractActor {

    private GeofenceClient geofenceClient;

    public GeofenceActor(GeofenceClient geofenceClient) {

        this.geofenceClient = geofenceClient;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GeofenceRequest.class, request -> {
                    ActorRef senderToReturnTo = this.getSender();
                    geofenceClient.isInGeofence(request.gpsUpdate.getLatitude(), request.gpsUpdate.getLongitude())
                            .thenAccept(result -> senderToReturnTo.tell(result, this.getSelf()));
                })
                .build();
    }

    static public class GeofenceRequest {
        public final GPSUpdate gpsUpdate;

        public GeofenceRequest(GPSUpdate gpsUpdate) {
            this.gpsUpdate = gpsUpdate;
        }
    }

    public static Props props(GeofenceClient geofenceClient){
        return Props.create(GeofenceActor.class, geofenceClient);
    }
}