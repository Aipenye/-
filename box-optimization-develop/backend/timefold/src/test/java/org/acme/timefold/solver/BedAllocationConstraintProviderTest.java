package org.acme.timefold.solver;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;


import org.acme.timefold.domain.AirCube;
import org.acme.timefold.domain.Box;
import org.acme.timefold.domain.BoxPlan;
import org.acme.timefold.domain.Container;
import org.acme.timefold.solver.BedAllocationConstraintProvider;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.common.constraint.Assert;

@QuarkusTest
class BedAllocationConstraintProviderTest {
    @Inject
    ConstraintVerifier<BedAllocationConstraintProvider, BoxPlan> constraintVerifier;

    private List<AirCube> make_airCubes(Container container, int xbound, int ybound, int zbound) {
        List<AirCube> airCubes = new LinkedList<>();
        for (int x = 0; x<xbound;x++) {
                for (int y = 0; y<ybound; y++) {
                        for (int z = 0; z<zbound; z++) {
                                AirCube newCube = new AirCube("air%d %d %d".formatted(x, y, z), container, x, y, z);
                                airCubes.add(newCube);
                        }
                }
        }
        return airCubes;
    }

    private List<AirCube> make_airCubes(Container container) {
        return make_airCubes(container, container.getXbound(), container.getYbound(), container.getZbound());
    }

    private int coords_to_index(int xbound, int ybound, int zbound, int x, int y, int z) {
        return x*ybound*zbound+y*zbound+z;
    }

    private int coords_to_index(Container container, int x, int y, int z) {
        return coords_to_index(container.getXbound(), container.getYbound(), container.getZbound(), x, y, z);
    }

    @Test
    void insideBound() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();

        Container container = new Container("1", "container-1", 2, 2, 2, x_paths, z_paths);
            

        List<AirCube> air = make_airCubes(container, 2, 2, 2);
        container.setAirCubes(air);

        Box normalBox = new Box("box", "box", 2, 2, 2, container);
        normalBox.setAirCube(air.get(0));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::notOutOfBoundsX)
                .given(normalBox)
                .penalizesBy(0);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::notOutOfBoundsY)
                .given(normalBox)
                .penalizesBy(0);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::notOutOfBoundsZ)
                .given(normalBox)
                .penalizesBy(0);
    }

    @Test
    void outsideBoundX() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 1, 1, 1, x_paths, z_paths);

        List<AirCube> air = make_airCubes(container, 1, 1, 1);
        container.setAirCubes(air);

        Box bigBox = new Box("box0", "box0", 2, 1, 1, container);
        bigBox.setAirCube(air.get(0));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::notOutOfBoundsX)
                .given(bigBox)
                .penalizesBy(1);
    }

    


    @Test
    void outsideboundsY() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("container1", "container1", 2, 2, 2, x_paths, z_paths);

        List<AirCube> aircubes = make_airCubes(container, 2, 2, 2);
        container.setAirCubes(aircubes);

        Box box1 = new Box("1", "one", 3, 3, 3, container );
        box1.setAirCube(aircubes.get(0));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::notOutOfBoundsY)
            .given(box1)
            .penalizesBy(1);
        
    }
    
    @Test
    void outsideBoundZ() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 3, 3, 3, x_paths, z_paths);
            
        List<AirCube> air = make_airCubes(container, 3, 3, 3);
        container.setAirCubes(air);

        Box bigBox = new Box("box0", "box0", 3, 3, 4, container);
        bigBox.setAirCube(air.get(0));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::notOutOfBoundsZ)
                .given(bigBox)
                .penalizesBy(1);
    }
    
    @Test
    void overlap1() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 3, 1, 1, x_paths, z_paths);

        List<AirCube> air = make_airCubes(container, 3, 1, 1);
        container.setAirCubes(air);

        Box leftBox = new Box("leftBox", "leftBox", 2, 1, 1, container);
        leftBox.setAirCube(air.get(0));
        Box rightBox = new Box("rightBox", "rightBox", 2, 1, 1, container);
        rightBox.setAirCube(air.get(1));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::noOverlap)
                .given(leftBox, rightBox)
                .penalizesBy(1);
    }

    @Test
    void stable1() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 2, 4, 2, x_paths, z_paths);
        
        List<AirCube> air = make_airCubes(container);
        container.setAirCubes(air);

        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(0));
        Box upperBox = new Box("box2", "box2", 2, 2, 2, container);
        upperBox.setAirCube(air.get(coords_to_index(container, 0, 2, 0)));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::stability)
                .given(upperBox, lowerBox)
                .penalizesBy(0);
    }
    @Test
    void coords_to_index_test() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 3, 4, 3, x_paths, z_paths);     
        assertEquals(coords_to_index(container, 0, 0, 0),0);
        assertEquals(coords_to_index(container, 1, 0, 0), 12);
        assertEquals(coords_to_index(container, 2, 2, 2), 32);
    }

    @Test 
    void standing_on_test() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 3, 4, 3, x_paths, z_paths);     

        List<AirCube> air = make_airCubes(container);
        container.setAirCubes(air);

        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(coords_to_index(container, 0, 0, 0)));
        


        Box upperBox = new Box("box2", "box2", 2, 2, 2, container);
        upperBox.setAirCube(air.get(coords_to_index(container, 0, 2, 0)));
        assertEquals(Box.isStandingOn(upperBox, lowerBox), 4);
    }
    @Test
    void standing_on_test2() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 4, 4, 3, x_paths, z_paths);     

        List<AirCube> air = make_airCubes(container);
        container.setAirCubes(air);

        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(coords_to_index(container, 0, 0, 0)));
        
        Box upperBox = new Box("box2", "box2", 1, 1, 3, container);
        upperBox.setAirCube(air.get(coords_to_index(container, 0, 2, 0)));

        assertEquals(Box.isStandingOn(upperBox, lowerBox), 2);
    }

    @Test
    void is_stable_test() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 3, 4, 3, x_paths, z_paths);  
        container.setStabilityNumber(1.0);   
        Box upperBox = new Box("box2", "box2", 2, 2, 2, container);
        assertFalse(upperBox.isStable(1));
    }

    @Test
    void instable1() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 3, 4, 3, x_paths, z_paths);     
        container.setStabilityNumber(1.0);

        List<AirCube> air = make_airCubes(container);
        container.setAirCubes(air);

        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(coords_to_index(container, 0, 0, 0)));
        


        Box upperBox = new Box("box2", "box2", 2, 2, 2, container);
        upperBox.setAirCube(air.get(coords_to_index(container, 1, 2, 0)));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::stability)
                .given(upperBox, lowerBox)
                .penalizesBy(2);
    }

    @Test
    void stable2() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 4, 4, 3, x_paths, z_paths);  
        container.setStabilityNumber(1.0);   

        List<AirCube> air = make_airCubes(container);
        container.setAirCubes(air);

        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(coords_to_index(container, 0, 0, 0)));
        


        Box upperBox = new Box("box2", "box2", 2, 2, 2, container);
        upperBox.setAirCube(air.get(coords_to_index(container, 1, 2, 0)));

        Box lowerBox2 = new Box("box3", "box3", 2, 2, 2, container);
        lowerBox2.setAirCube(air.get(coords_to_index(container, 2, 0, 0)));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::stability)
                .given(upperBox, lowerBox, lowerBox2)
                .penalizesBy(0);
    }
    @Test
    void instable2() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 4, 4, 3, x_paths, z_paths);    
        container.setStabilityNumber(1.0); 

        List<AirCube> air = make_airCubes(container);
        container.setAirCubes(air);

        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(coords_to_index(container, 0, 0, 0)));
        
        Box upperBox = new Box("box2", "box2", 1, 1, 3, container);
        upperBox.setAirCube(air.get(coords_to_index(container, 0, 2, 0)));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::stability)
                .given(upperBox, lowerBox)
                .penalizesBy(1);    
    }
}
