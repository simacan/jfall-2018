

class GeofenceTools {

    //This object describes a North oriented, square shaped geofence,
    //thus both upper corners are on the same lat, bottom corners are on the same lat,
    //both left corners on the same lon and both right corners on the same lon
    private static class NorthOrientedSquareGeofence {
        private Coordinates cornerUpLeft;
        private Coordinates cornerBottomRight;

        NorthOrientedSquareGeofence(Coordinates cornerUpLeft, Coordinates cornerBottomRight) {
            this.cornerUpLeft = cornerUpLeft;
            this.cornerBottomRight = cornerBottomRight;
        }
    }

    private static NorthOrientedSquareGeofence northOrientedSquareGeofence = new NorthOrientedSquareGeofence(
            new Coordinates(52.00702321126474, 5.6270599365234375),
            new Coordinates(52.027729487209015, 5.6667137145996085));

    //This method checks if the given coordinates lie within the northOrientedSquareGeofence
    static boolean checkOnNorthOrientedSquareGeofence(Coordinates coordinates) {
        return coordinates.getLat() >= northOrientedSquareGeofence.cornerUpLeft.getLat() &&
                coordinates.getLat() <= northOrientedSquareGeofence.cornerBottomRight.getLat() &&
                coordinates.getLon() >= northOrientedSquareGeofence.cornerUpLeft.getLon() &&
                coordinates.getLon() <= northOrientedSquareGeofence.cornerBottomRight.getLon();
    }
}
