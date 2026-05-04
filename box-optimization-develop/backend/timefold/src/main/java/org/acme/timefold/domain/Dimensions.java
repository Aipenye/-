package org.acme.timefold.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(keyUsing = DimensionsKeyDeserializer.class)
public class Dimensions {
    private int x;
    private int y;
    private int z;

    // Default constructor for Jackson
    public Dimensions() {
    }

    // Primary constructor
    @JsonCreator
    public Dimensions(
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getters and setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    // Critical for using as Map key
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Dimensions that = (Dimensions) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Dimensions{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}