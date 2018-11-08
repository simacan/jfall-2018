package domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GPSUpdate {
    private String uniqueId;
    private Double latitude;
    private Double longitude;
    private Long timestamp;

    @JsonCreator
    public GPSUpdate(@JsonProperty("uniqueId") String uniqueId,
                     @JsonProperty("latitude") Double latitude,
                     @JsonProperty("longitude") Double longitude,
                     @JsonProperty("timestamp") Long timestamp) {
        this.uniqueId = uniqueId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String toJson() {
        return "{" +
                "\"uniqueId\":\"" + uniqueId + "\"," +
                "\"latitude\":" + latitude + "," +
                "\"longitude\":" + longitude + "," +
                "\"timestamp\":" + timestamp +
                "}";
    }

    public String toString() {
        return "[uniqueId = " + uniqueId + ", latitude = " + latitude + ", longitude = " + longitude + ", timestamp = " + timestamp + "]";
    }
}
