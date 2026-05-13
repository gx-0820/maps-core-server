package com.example.coreserver.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class GeofenceUtils {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final int SEGMENTS = 32; // 圆形近似为32边形

    public static Polygon createCircle(double lon, double lat, double radius) {
        Coordinate[] coords = new Coordinate[SEGMENTS + 1];
        for (int i = 0; i < SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / SEGMENTS;
            double dx = radius * Math.cos(angle);
            double dy = radius * Math.sin(angle);
            coords[i] = new Coordinate(lon + dx, lat + dy);
        }
        coords[SEGMENTS] = coords[0];
        return GEOMETRY_FACTORY.createPolygon(coords);
    }
}