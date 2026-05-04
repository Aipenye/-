package org.acme.timefold.domain;

import java.util.List;
import java.lang.Integer;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Box {

    @PlanningId
    private String id;
    private int length;
    private int height;
    private int width;
    @ProblemFactProperty
    private String name;

    @ProblemFactProperty
    private Container container;
    @PlanningVariable(allowsUnassigned = true)
    private AirCube airCube;

    //preventing boxes from moving/being reassigned
    private boolean locked = false; //default false
    private int lockX;
    private int lockY;
    private int lockZ;

    public Box() {

    }

    public Box(String id, String name, int length, int height, int width, Container container) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.height = height;
        this.width = width;
        this.container = container;
    }

    public Box(String id, String name, int length, int height, int width, AirCube airCube, Container container) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.height = height;
        this.width = width;
        this.airCube = airCube;
        this.container = container;
    }

    @JsonIgnore
    public Container getContainer() {
        if (airCube == null) {
            return null;
        }
        return airCube.getContainer();
    }

    @JsonIgnore
    public int getxbound() {
        if (airCube == null) {
            return Integer.MIN_VALUE;
        }
        return airCube.getContainer().getXbound();
    }

    @JsonIgnore
    public int getybound() {
        if (airCube == null) {
            return Integer.MIN_VALUE;
        }
        return airCube.getContainer().getYbound();
    }

    @JsonIgnore
    public int getzbound() {
        if (airCube == null) {
            return Integer.MIN_VALUE;
        }
        return airCube.getContainer().getZbound();
    }

    @JsonIgnore
    public boolean isStable() {
        // Standard call for isStable, isStable with 0 support.
        return isStable(0);
    }

    @JsonIgnore
    public boolean isStable(int standingOnCount) {
        // standingOnCount is the number of surface area that the box is supported by,
        // this
        // function simply divides this number by the total surface area of the box to
        // get
        // a score for how stable the box is currently.
        if (airCube == null) {
            return (((double) standingOnCount) / ((double) length * (double) width)) >= 1.0;
        } else {
            return (((double) standingOnCount) / ((double) length * (double) width)) >= airCube.getContainer()
                    .getStabilityNumber();
        }

    }

    public static int isStandingOn(Box upperBox, Box lowerBox) {
        if (lowerBox.getY() + lowerBox.getHeight() == upperBox.getY()) {
            // Mathematical expression to calculate the overlap in the x direction between
            // upperbox and lowerbox.
            int xOverlap = Math.max(0,
                    Math.min(lowerBox.getX() + lowerBox.getLength(), upperBox.getX() + upperBox.getLength())
                            - Math.max(lowerBox.getX(), upperBox.getX()));
            if (xOverlap < 0) {
                xOverlap = 0;
            }
            // Calculating the overlap in the z direction.
            int zOverlap = Math.max(0,
                    Math.min(lowerBox.getZ() + lowerBox.getWidth(), upperBox.getZ() + upperBox.getWidth())
                            - Math.max(lowerBox.getZ(), upperBox.getZ()));
            if (zOverlap < 0) {
                zOverlap = 0;
            }
            // Area of overlap is simply the product of the zOverlap and the xOverlap
            return xOverlap * zOverlap;
        } else if (upperBox.getY() == 0) {
            return upperBox.getLength() * upperBox.getWidth();
        } else {
            return 0;
        }
    }

    public Boolean inPathX(List<List<Integer>> x_paths) {
        if (x_paths == null || this.getX() == null) {
            return false;
        }
        for (List<Integer> x_path : x_paths) {
            if (x_path.size() == 2) {
                int x1 = x_path.get(0);
                int x2 = x_path.get(1);
                if (x1 <= this.getX() && this.getX() < x2) {
                    return true;
                }
                if (this.getX() < x1 && this.getX() + this.getLength() > x1) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean inPathZ(List<List<Integer>> z_paths) {
        if (z_paths == null || this.getX() == null) {
            return false;
        }
        for (List<Integer> z_path : z_paths) {
            if (z_path.size() == 2) {
                int z1 = z_path.get(0);
                int z2 = z_path.get(1);
                if (z1 <= this.getZ() && this.getZ() < z2) {
                    return true;
                }
                if (this.getZ() < z1 && this.getZ() + this.getWidth() > z1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String result = "box \"%s\" of size %dx%dx%d".formatted(this.id, this.length, this.height, this.width);
        if (this.airCube != null) {
            result += " at %d,%d,%d".formatted(this.getX(), this.getY(), this.getZ());
        }
        if (isLocked()) {
            result += " is locked at %d,%d,%d".formatted(lockX, lockY, lockZ);
        }
        return result;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    @JsonIgnore
    public Integer getX() {
        return this.airCube != null ? this.airCube.getX() : null;
    }

    @JsonIgnore
    public Integer getY() {
        return this.airCube != null ? this.airCube.getY() : null;
    }

    @JsonIgnore
    public Integer getZ() {
        return this.airCube != null ? this.airCube.getZ() : null;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getLockX() {
        return lockX;
    }

    public int getLockY() {
        return lockY;
    }

    public int getLockZ() {
        return lockZ;
    }

    @JsonIgnore
    public boolean isInLockedPosition() {
        return this.airCube != null && getX() == lockX && getY() == lockY && getZ() == lockZ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLength(int len) {
        this.length = len;
    }

    public void setWidth(int wid) {
        this.width = wid;
    }

    public void setHeight(int hei) {
        this.height = hei;
    }

    public AirCube getAirCube() {
        return airCube;
    }

    public void setAirCube(AirCube air) {
        this.airCube = air;
    }

    public void lockBox(int x, int y, int z) {
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
        this.locked = true;
    }
}
