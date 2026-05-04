package org.acme.timefold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.acme.timefold.domain.AirCube;
import org.acme.timefold.domain.Box;
import org.acme.timefold.domain.Container;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.constraints.AssertTrue;

@QuarkusTest
class BoxTest {
    private List<AirCube> makeAircubes(Container container) {
        List<AirCube> airCubes = new LinkedList<>();
        for (int x = 0; x<container.getXbound();x++) {
                for (int y = 0; y<container.getYbound(); y++) {
                        for (int z = 0; z<container.getZbound(); z++) {
                                AirCube newCube = new AirCube("air%d %d %d".formatted(x, y, z), container, x, y, z);
                                airCubes.add(newCube);
                        }
                }
        }
        return airCubes;
    }

    // Helper testing function that takes a coordinate input and calculates the corresponding 1-dimensional index
    private int coordsToIndex(int xbound, int ybound, int zbound, int x, int y, int z) {
        return x * ybound * zbound + y * zbound + z;
    }

    private int coordsToIndex(Container container, int x, int y, int z) {
        return coordsToIndex(container.getXbound(), container.getYbound(), container.getZbound(), x, y, z);
    }

    @Test
    void testCoordsToIndex0() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container1 = new Container("container1", "container1", 2, 3, 4, x_paths, z_paths);
        List<AirCube> air = makeAircubes(container1);
        container1.setAirCubes(air);

        int x = 0, y = 0, z = 0;
        assertEquals(0, air.get(coordsToIndex(container1, x, y, z)).getX());
        assertEquals(0, air.get(coordsToIndex(container1, x, y, z)).getY());
        assertEquals(0, air.get(coordsToIndex(container1, x, y, z)).getZ());
    }

    @Test
    void testCoordsToIndex123(){
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container1 = new Container("container1", "container1", 2, 3, 4, x_paths, z_paths);
        List<AirCube> air = makeAircubes(container1);
        container1.setAirCubes(air);

        int x = 1, y = 2, z = 3;
        assertEquals(1, air.get(coordsToIndex(container1, x, y, z)).getX());
        assertEquals(2, air.get(coordsToIndex(container1, x, y, z)).getY());
        assertEquals(3, air.get(coordsToIndex(container1, x, y, z)).getZ());
    }

    @Test
    void stability1() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 2, 4, 2, x_paths, z_paths);
        
        List<AirCube> air = makeAircubes(container);
        container.setAirCubes(air);
        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(0));
        Box upperBox = new Box("box2", "box2", 2, 2, 2, container);
        upperBox.setAirCube(air.get(coordsToIndex(container, 0, 2, 0)));

        assertEquals(4, Box.isStandingOn(upperBox, lowerBox));
    }
    @Test
    void stability2() {
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        Container container = new Container("1", "container-1", 3, 4, 3, x_paths, z_paths);     

        List<AirCube> air = makeAircubes(container);
        container.setAirCubes(air);

        Box lowerBox = new Box("box1", "box1", 2, 2, 2, container);
        lowerBox.setAirCube(air.get(coordsToIndex(container, 0, 0, 0)));

        Box upperBox = new Box("box2", "box2", 2, 2, 2, container);
        upperBox.setAirCube(air.get(coordsToIndex(container, 1, 2, 1)));

        assertEquals(1, Box.isStandingOn(upperBox, lowerBox));
    }

    @Test
    void isStandingOnFull() {
        Container cont = new Container("container", "cont", 8, 9, 10, new ArrayList<List<Integer>>(), new ArrayList<List<Integer>>());
        AirCube ac1 = new AirCube("ac1", cont, 0, 0, 0);
        AirCube ac2 = new AirCube("ac1", cont, 0, 2, 0);
        Box lowerBox = new Box("box1", "boxA", 1, 2, 3, ac1, cont);
        Box upperBox = new Box("box2", "boxB", 1, 2, 3, ac2, cont);
        assertEquals(3, Box.isStandingOn(upperBox, lowerBox));
    }

    @Test
    void isStandingOnReverse() {
        Container cont = new Container("container", "cont", 8, 9, 10, new ArrayList<List<Integer>>(), new ArrayList<List<Integer>>());
        AirCube ac1 = new AirCube("ac1", cont, 0, 3, 0);
        AirCube ac2 = new AirCube("ac1", cont, 0, 1, 0);
        Box lowerBox = new Box("box1", "boxA", 1, 2, 3, ac1, cont);
        Box upperBox = new Box("box2", "boxB", 1, 2, 3, ac2, cont);
        assertEquals(0, Box.isStandingOn(upperBox, lowerBox));
    }

    @Test
    void isStandingOnHalf() {
        Container cont = new Container("container", "cont", 8, 9, 10, new ArrayList<List<Integer>>(), new ArrayList<List<Integer>>());
        AirCube ac1 = new AirCube("ac1", cont, 0, 0, 0);
        AirCube ac2 = new AirCube("ac1", cont, 0, 2, 0);
        Box lowerBox = new Box("box1", "boxA", 1, 2, 3, ac1, cont);
        Box upperBox = new Box("box2", "boxB", 2, 2, 2, ac2, cont);
        assertEquals(2, Box.isStandingOn(upperBox, lowerBox));
    }

    @Test
    void isStandingOnFloor() {
        Container cont = new Container("container", "cont", 8, 9, 10, new ArrayList<List<Integer>>(), new ArrayList<List<Integer>>());
        AirCube ac1 = new AirCube("ac1", cont, 0, 2, 0);
        AirCube ac2 = new AirCube("ac1", cont, 0, 0, 0);
        Box lowerBox = new Box("box1", "boxA", 1, 2, 3, ac1, cont);
        Box upperBox = new Box("box2", "boxB", 2, 2, 2, ac2, cont);
        assertEquals(4, Box.isStandingOn(upperBox, lowerBox));
    }

    @Test
    void isStandingOnOverlap() {
        Container cont = new Container("container", "cont", 8, 9, 10, new ArrayList<List<Integer>>(), new ArrayList<List<Integer>>());
        AirCube ac1 = new AirCube("ac1", cont, 0, 0, 0);
        AirCube ac2 = new AirCube("ac1", cont, 0, 1, 0);
        Box lowerBox = new Box("box1", "boxA", 1, 2, 3, ac1, cont);
        Box upperBox = new Box("box2", "boxB", 2, 2, 2, ac2, cont);
        assertEquals(0, Box.isStandingOn(upperBox, lowerBox));
    }

    @Test
    void inPathXinFirstPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        List<Integer> x_path1 = new ArrayList<>();
        x_path1.add(1);
        x_path1.add(2);
        x_paths.add(x_path1);

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 0, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(true, box.inPathX(x_paths));
    }

    @Test
    void inPathXinSecondPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        List<Integer> x_path1 = new ArrayList<>();
        List<Integer> x_path2 = new ArrayList<>();

        x_path1.add(1);
        x_path1.add(2);
        x_paths.add(x_path1);

        x_path2.add(3);
        x_path2.add(4);
        x_paths.add(x_path2);

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 3, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(true, box.inPathX(x_paths));
    }

    @Test
    void inPathXnoPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 0, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(false, box.inPathX(x_paths));
    }

    @Test
    void inPathXnonsensicalPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        List<Integer> x_path1 = new ArrayList<>();

        x_path1.add(1);
        x_paths.add(x_path1);

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 0, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(false, box.inPathX(x_paths));
    } 

    @Test
    void inPathXinBetweenPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        List<Integer> x_path1 = new ArrayList<>();
        List<Integer> x_path2 = new ArrayList<>();

        x_path1.add(1);
        x_path1.add(2);
        x_paths.add(x_path1);

        x_path2.add(3);
        x_path2.add(4);
        x_paths.add(x_path2);

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 2, 0, 0);
        Box box = new Box("box", "boxA", 1, 2, 2, ac, cont);
        assertEquals(false, box.inPathX(x_paths));
    }

        @Test
    void inPathZinFirstPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        List<Integer> z_path1 = new ArrayList<>();
        z_path1.add(1);
        z_path1.add(2);
        z_paths.add(z_path1);

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 0, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(true, box.inPathZ(z_paths));
    }

    @Test
    void inPathZinSecondPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        List<Integer> z_path1 = new ArrayList<>();
        List<Integer> z_path2 = new ArrayList<>();

        z_path1.add(1);
        z_path1.add(2);
        z_paths.add(z_path1);

        z_path2.add(3);
        z_path2.add(4);
        z_paths.add(z_path2);

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 3, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(true, box.inPathZ(z_paths));
    }

    @Test
    void inPathZnoPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 0, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(false, box.inPathZ(z_paths));
    }

    @Test
    void inPathZnonsensicalPath() {

        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();
        List<Integer> z_path1 = new ArrayList<>();

        z_path1.add(1);
        z_paths.add(z_path1);

        Container cont = new Container("container", "cont", 8, 9, 10, x_paths, z_paths);
        AirCube ac = new AirCube("ac", cont, 0, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 2, ac, cont);
        assertEquals(false, box.inPathZ(z_paths));
    } 

    @Test
    void inPathZinBetweenPath() {

        List<List<Integer>> x_pathways = new ArrayList<>();
        List<List<Integer>> z_pathways = new ArrayList<>();
        List<Integer> z_path1 = new ArrayList<>();
        List<Integer> z_path2 = new ArrayList<>();

        z_path1.add(1);
        z_path1.add(2);
        z_pathways.add(z_path1);

        z_path2.add(3);
        z_path2.add(4);
        z_pathways.add(z_path2);

        Container cont = new Container("container", "cont", 8, 9, 10, x_pathways, z_pathways);
        AirCube ac = new AirCube("ac", cont, 2, 0, 0);
        Box box = new Box("box", "boxA", 2, 2, 1, ac, cont);
        assertEquals(false, box.inPathZ(z_pathways));
    }
}
