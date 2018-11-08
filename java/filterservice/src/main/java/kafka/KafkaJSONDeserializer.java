package kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.util.Map;

public class KafkaJSONDeserializer<T> implements Deserializer<T> {
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private final Class<T> type;

    public KafkaJSONDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        stringDeserializer.configure(configs, isKey);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        String deseralizedString = stringDeserializer.deserialize(topic, data);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(deseralizedString, type);
        } catch (IOException e) {
            // Could use better error handling, do not use in production!
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        stringDeserializer.close();
    }
}
