package org.acme.timefold.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONArray;
import org.json.JSONObject;

@PlanningSolution
public class BoxPlan {

    @ProblemFactProperty
    private Container container;
    @PlanningEntityCollectionProperty
    private List<Box> boxes;
    @JsonIgnore
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<AirCube> aircubes;

    @PlanningScore
    private HardMediumSoftScore score;

    private SolverStatus solverStatus;

    // No-arg constructor required for Timefold
    public BoxPlan() {
    }

    @JsonCreator
    public BoxPlan(@JsonProperty("container") Container container, @JsonProperty("boxes") List<Box> boxes) {
        this.container = container;
        this.boxes = boxes;
    }

    public BoxPlan(HardMediumSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }
    public String toString() {
        String res = "%d Boxes in container %s, with dimensions %dx%dx%d".formatted(boxes.size(), container.getId(), container.getXbound(), container.getYbound(), container.getZbound());

        for (Box box : boxes) {
            res += ("Box: " + box.getId() + " length: " + box.getLength() + " height:" + box.getWidth() + " width:" + box.getHeight() + ", x: "
                    + box.getX() + " y:" + box.getY() + " z:" + box.getZ()) + "\n";
        }
        return res;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    public List<AirCube> getAirCubes() {
        return aircubes;
    }

    public void setAirCubes(List<AirCube> aircubes) {
        this.aircubes = aircubes;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    public String to_data() {
        // This function is used by the bedschedulingresource to write the data of a solution to file.
        String result = "%d,%d,%d\n".formatted(container.getXbound(), container.getYbound(), container.getZbound());
        for (Box box : boxes) {
            if (box.getAirCube() != null) {
                result += "%s;%d,%d,%d;%d,%d,%d\n".formatted(box.getId(), box.getLength(), box.getHeight(), box.getWidth(), box.getX(), box.getY(), box.getZ());
            }
        }
        return result;
    }

    public JSONObject toJson() {
        // create a json object of this object (boxplan) containing the container and all the boxes 
        // 
        JSONObject root = new JSONObject();

        // Add container dimensions
        JSONObject containerObj = new JSONObject();
        containerObj.put("width", container.getXbound());
        containerObj.put("height", container.getYbound());
        containerObj.put("length", container.getZbound());
        root.put("container", containerObj);

        // Add boxes array
        JSONArray boxesArray = new JSONArray();
        for (Box box : boxes) {
            if (box.getAirCube() != null) {
                JSONObject boxObj = new JSONObject();
                boxObj.put("id", box.getId());
                boxObj.put("length", box.getLength());
                boxObj.put("height", box.getHeight());
                boxObj.put("width", box.getWidth());
                boxObj.put("x", box.getX());
                boxObj.put("y", box.getY());
                boxObj.put("z", box.getZ());

                boxesArray.put(boxObj);
            }
        }
        root.put("boxes", boxesArray);
        return root;
    }

}
