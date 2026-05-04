package org.acme.timefold.solver;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.acme.timefold.domain.AirCube;
import org.acme.timefold.domain.Box;
import org.acme.timefold.domain.BoxPlan;
import org.acme.timefold.domain.Container;
import org.acme.timefold.domain.Dimensions;


import org.apache.commons.math3.util.Pair;

public class BoxSolver {

    private BoxPlan problem;
    private List<Box> boxes = new LinkedList<>();
    private SolverFactory<BoxPlan> solverFactory;
    Solver<BoxPlan> solver;
    

    public BoxSolver(String warehouseName, int warehouseLength, int warehouseHeight, int warehouseWidth,
            List<List<Integer>> x_paths, List<List<Integer>> z_paths) {
        this.problem = new BoxPlan();

        Container container = new Container(warehouseName, warehouseName, warehouseLength, warehouseHeight,
                warehouseWidth, x_paths, z_paths);

        // Creating air cubes inside the container
        List<AirCube> airCubes = init_aircubes(container);

        container.setStabilityNumber(1.0); // specifying the lower bound for the stability score the boxes are allowed
                                           // to have
        container.setAirCubes(airCubes);
        this.problem.setContainer(container);
        this.problem.setAirCubes(airCubes);
        this.problem.setContainer(container);

    }

    public void addBox(String id, int length, int height, int width) {
        Box newBox = new Box(id, id, length, height, width, this.problem.getContainer());
        boxes.add(newBox);
    }

    public void addLockedBox(String id, int length, int height, int width, int x, int y, int z) {
        Box newBox = new Box(id, id, length, height, width, this.problem.getContainer());
        newBox.lockBox(x, y, z);
        boxes.add(newBox);
    }

    private List<AirCube> init_aircubes(Container container) {
        List<AirCube> airCubes = new LinkedList<>();
        for (int x = 0; x < container.getXbound(); x++) {
            for (int y = 0; y < container.getYbound(); y++) {
                for (int z = 0; z < container.getZbound(); z++) {
                    airCubes.add(new AirCube("air%d_%d_%d".formatted(x, y, z), container, x, y, z));
                }
            }
        }
        return airCubes;
    }

    private int coords_to_index(int xbound, int ybound, int zbound, int x, int y, int z) {
        return x*ybound*zbound+y*zbound+z;
    }

    private int coords_to_index(Container container, int x, int y, int z) {
        return coords_to_index(container.getXbound(), container.getYbound(), container.getZbound(), x, y, z);
    }

    public BoxPlan getProblem() {
        return this.problem;
    }

    public BoxPlan solve() {
        //update boxplan
        boxes.sort(Comparator.comparingInt((Box box) -> box.getLength() * box.getWidth()).reversed());
        this.problem.setBoxes(boxes);
        for (Box box : this.problem.getBoxes()) {
            if (box.isLocked()) {
                box.setAirCube(problem.getAirCubes().get(coords_to_index(problem.getContainer(), 
                                                            box.getLockX(), 
                                                            box.getLockY(),
                                                            box.getLockZ())));
            }
        }

        SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");

        // solverConfig.setMoveThreadCount("AUTO");
            // this setting should make the solving run in parallel on the available threads on the machine it is run on.
            // apparantly, this is only available with the paid "enterprise" version of timefold... so I'll leave it here in case anyone
            // ever gets the paid timefold version ...

        SolverFactory<BoxPlan> solverFactory = SolverFactory.create(solverConfig);


        Solver<BoxPlan> solver = solverFactory.buildSolver();
        
        //solve
        BoxPlan bestSolution = solver.solve(this.problem);
        return bestSolution;
    }
}