package org.acme.timefold.domain;

import java.util.Objects;
import java.util.List;
import java.util.LinkedList;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Container.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Container {

    @PlanningId
    private String id;
    private String name;
    private int xbound;
    private int ybound;
    private int zbound;
    private double stabilityNumber;
    private List<List<Integer>> x_paths;
    private List<List<Integer>> z_paths;

    @JsonIgnore
    private List<AirCube> airCubes;



    public Container() {
        this.airCubes = new LinkedList<>();
    }

    public Container(String id, String name, int xbound, int ybound, int zbound, List<List<Integer>> x_paths, List<List<Integer>> z_paths) {
        this.id = id;
        this.name = name;
        this.airCubes = new LinkedList<>();
        this.xbound = xbound;
        this.ybound = ybound;
        this.zbound = zbound;
        this.x_paths = x_paths;
        this.z_paths = z_paths;
        
    }

    public void addAirCube(AirCube airCube) {
        if (!airCubes.contains(airCube)) {

            airCubes.add(airCube);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************
    public List<List<Integer>> getX_paths() {
        return x_paths;
    }

    public void setX_paths(List<List<Integer>> x_paths) {
        this.x_paths = x_paths;
    }

    public List<List<Integer>> getZ_paths() {
        return z_paths;
    }

    public void setZ_paths(List<List<Integer>> z_paths) {
        this.z_paths = z_paths;
    }
    public String getId() {
        return id;
    }

    public double getStabilityNumber() {
        return stabilityNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getXbound() {
        return xbound;
    }

    public int getYbound() {
        return ybound;
    }

    public int getZbound() {
        return zbound;
    }

    public void setStabilityNumber(double stab) {
        this.stabilityNumber = stab;
    }

    public void setxbound(int xbound) {
        this.xbound = xbound;
    }

    public void setybound(int ybound) {
        this.ybound = ybound;
    }

    public void setzbound(int zbound) {
        this.zbound = zbound;
    }

    public List<AirCube> getAirCubes() {
        return airCubes;
    }

    public void setAirCubes(List<AirCube> airCubes) {
        this.airCubes = airCubes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Container that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
