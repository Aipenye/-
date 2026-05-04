package org.acme.timefold.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;




@JsonIdentityInfo(scope = AirCube.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AirCube {

    @PlanningId
    private String id;

    private int x;
    private int y;
    private int z;
    

    @ProblemFactProperty
    private Container container;
    

    public AirCube() {
        // Default constructor
    }

    @JsonCreator
    public AirCube(String id) {
        this.id = id;
    }

    public AirCube(@JsonProperty("id") String id, 
                   @JsonProperty("container") Container container, 
                   @JsonProperty("x") int x, 
                   @JsonProperty("y") int y, 
                   @JsonProperty("z") int z) {
        this.id = id;
        this.container = container;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "air" + " (" + x + "," + y + "," + z + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }


    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AirCube air)) return false;
        return Objects.equals(getId(), air.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
