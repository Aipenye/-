package org.acme.timefold.rest;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

import org.acme.timefold.domain.AirCube;
import org.acme.timefold.domain.Box;
import org.acme.timefold.domain.BoxPlan;
import org.acme.timefold.domain.Container;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DemoDataGenerator {

    public BoxPlan generateDemoData() {
        LinkedList<String> lines = new LinkedList<>();
        try {
                File setupFile = new File("boxSets/problem.txt");
                Scanner myReader = new Scanner(setupFile);
                while (myReader.hasNextLine()) {
                        String data = myReader.nextLine();
                        lines.add(data);
                }
                myReader.close();
        } catch (FileNotFoundException e) {
                System.out.println("File not found");
                e.printStackTrace();
                lines.add("10,10,10");
        }

        BoxPlan schedule = new BoxPlan();
        // Department

        String[] containerSize = lines.pop().split(",");
        
        List<List<Integer>> x_paths = new ArrayList<>();
        List<List<Integer>> z_paths = new ArrayList<>();

        Container container = new Container("1", 
                                            "container-1", 
                                            Integer.parseInt(containerSize[0]), 
                                            Integer.parseInt(containerSize[1]), 
                                            Integer.parseInt(containerSize[2]), x_paths, z_paths);
        schedule.setContainer(container);

// Initialize air cubes

        // Creating air cubes inside the container
        List<AirCube> airCubes = new LinkedList<>();
        for (int x = 0; x < container.getXbound(); x++) {
            for (int y = 0; y < container.getYbound(); y++) {
                for (int z = 0; z < container.getZbound(); z++) {
                    airCubes.add(new AirCube("air%d_%d_%d".formatted(x, y, z), container, x, y, z));
                }
            }
        }
        container.setStabilityNumber(1.0);
        container.setAirCubes(airCubes);
        schedule.setAirCubes(airCubes);



        List<Box> boxes = new LinkedList<>();
        for (String boxString : lines) {
                String[] boxTemplate = boxString.split(",|;");
                boxes.add(new Box(boxTemplate[0], 
                                  boxTemplate[0], 
                                  Integer.parseInt(boxTemplate[1]),
                                  Integer.parseInt(boxTemplate[2]),
                                  Integer.parseInt(boxTemplate[3]),
                                  container));
        }

        LocalDate firstMonthMonday = LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)); // First Monday of the month
        List<LocalDate> dates = new ArrayList<>(7);
        dates.add(firstMonthMonday);
        int countDays = 2;
        for (int i = 1; i < countDays; i++)     {
            dates.add(firstMonthMonday.with(firstInMonth(DayOfWeek.MONDAY)).plusDays(i));
        }



        boxes.sort(Comparator.comparingInt((Box box) -> box.getLength() * box.getWidth()).reversed());



        schedule.setBoxes(boxes);

        return schedule;
    }
}
