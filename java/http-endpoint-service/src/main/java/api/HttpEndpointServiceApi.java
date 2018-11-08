package api;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import domain.GPSUpdate;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import akka.http.javadsl.marshallers.jackson.Jackson;

import java.util.LinkedHashMap;
import java.util.List;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;

public class HttpEndpointServiceApi extends AllDirectives {

    private KafkaProducer<String, String> producer;
    private String kafkaTopicOut;

    public HttpEndpointServiceApi(KafkaProducer<String, String> producer, String kafkaTopicOut) {
        this.kafkaTopicOut = kafkaTopicOut;
        this.producer = producer;
    }

    public Route getRoute() {

        return cors(() -> route(
            pathPrefix("health", () ->
                get(() -> complete(StatusCodes.OK))
            ),
            pathPrefix("addGPSUpdate", () ->
                post(() ->
                    entity(Jackson.unmarshaller(GPSUpdate.class), gpsUpdate -> {
                        System.out.println("received: " + gpsUpdate);

                        // Send the gps update to the actor who puts it on Kafka
                        produceItem(gpsUpdate);
                        return complete(StatusCodes.OK);
                    })
                )
            ),
            pathPrefix("addMultipleUpdates", () ->
                    post(() ->
                            entity(Jackson.unmarshaller(List.class), gpsUpdates -> {
                                // Send the gps update to the actor who puts it on Kafka
                                // Obviously you don't want the unmarshalling in this state on production, but it serves the demo purpose
                                // Unmarshalling a json array with the scaladsl is a bit easier
                                gpsUpdates.forEach(update -> {
                                    LinkedHashMap<String, Object> castUpdate = (LinkedHashMap<String, Object>) update;
                                    produceItem(
                                        new GPSUpdate(
                                            (String) castUpdate.get("uniqueId"),
                                            (double) castUpdate.get("latitude"),
                                            (double) castUpdate.get("longitude"),
                                            (long) ((int) castUpdate.get("timestamp"))
                                        )
                                    );
                                });
                                return complete(StatusCodes.OK);
                            })
                    )
            )
        ));
    }

    private void produceItem(GPSUpdate loc) {
        producer.send(new ProducerRecord<>(kafkaTopicOut, loc.toJson()));
    }
}
