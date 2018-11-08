package updates;

import akka.actor.AbstractActor;
import domain.GPSUpdate;

import java.util.HashMap;

/**
 * Actor that receives updates and stores them per device (each unique ID that comes in). All latest updates can be
 * retrieved with one actor message 'getLatestUpdates'
 */
public class GPSUpdatesActor extends AbstractActor {

    private HashMap<String, GPSUpdate> latestUpdateMap = new HashMap<String, GPSUpdate>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GPSUpdate.class, update -> {
                    System.out.println("Received update " + update);
                    latestUpdateMap.put(update.getUniqueId(), update);
                })
                .match(String.class, s -> {
                    if (s.equals("getLatestUpdates")) {
                        getSender().tell(latestUpdateMap, getSelf());
                    } else {
                        System.out.println("I could not handle message " + s);
                    }
                })
                .build();
    }
}
