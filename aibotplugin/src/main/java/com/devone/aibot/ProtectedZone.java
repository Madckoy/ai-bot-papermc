package com.devone.aibot;

import org.bukkit.Location;

public class ProtectedZone {
    private final double x, y, z;
    private final int radius;

    public ProtectedZone(double x, double y, double z, int radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    public boolean isInside(Location location) {
        double dx = location.getX() - x;
        double dy = location.getY() - y;
        double dz = location.getZ() - z;
        return (dx * dx + dy * dy + dz * dz) <= (radius * radius);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public int getRadius() { return radius; }

    @Override
    public String toString() {
        return "Zone at (" + x + ", " + y + ", " + z + ") with radius " + radius;
    }
}
