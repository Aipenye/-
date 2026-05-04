package com.example.boxpackaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.github.skjolber.packing.api.Box;
import com.github.skjolber.packing.api.Container;
import com.github.skjolber.packing.api.ContainerItem;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.packer.laff.LargestAreaFitFirstPackager;
import com.github.skjolber.packing.visualizer.packaging.DefaultPackagingResultVisualizerFactory;

public class App {
        public static void main(String[] args) throws Exception {

                // Create some items to pack.
                List<StackableItem> products = new ArrayList<>();
                products.add(new StackableItem(
                                Box.newBuilder()
                                                .withId("Foot")
                                                .withSize(1, 3, 1)
                                                .withRotate3D()
                                                .withWeight(25)
                                                .build(),
                                10));
                products.add(new StackableItem(
                                Box.newBuilder()
                                                .withId("Leg")
                                                .withSize(5, 3, 1)
                                                .withRotate3D()
                                                .withWeight(25)
                                                .build(),
                                5));
                products.add(new StackableItem(
                                Box.newBuilder()
                                                .withId("Arm")
                                                .withSize(1, 3, 5)
                                                .withRotate3D()
                                                .withWeight(50)
                                                .build(),
                                5));

                products.add(new StackableItem(
                                Box.newBuilder()
                                                .withId("Arm")
                                                .withSize(2, 2, 2)
                                                .withRotate3D()
                                                .withWeight(50)
                                                .build(),
                                2));

                // Create a container.
                Container container = Container.newBuilder()
                                .withDescription("Container 1")
                                .withSize(10, 10, 10)
                                .withEmptyWeight(1)
                                .withMaxLoadWeight(1000000)
                                .build();

                // Prepare container items (here we assume an unlimited supply of this container
                // type)
                List<ContainerItem> containerItems = ContainerItem.newListBuilder()
                                .withContainer(container)
                                .build();

                // Obtain a packager instance (Largest Area Fit First algorithm)
                LargestAreaFitFirstPackager packager = LargestAreaFitFirstPackager.newBuilder().build();

                // Pack the items into the containers
                PackagerResult result = packager.newResultBuilder()
                                .withContainers(containerItems)
                                .withStackables(products)
                                .build();

                // Check if packing was successful and print the result
                if (result.isSuccess()) {
                        // Here result.get(0) returns the first packed container

                        writePackingResultToJson(result, "result.json");
                        System.out.println("Packing successful! Packed container details:");
                } else {
                        System.out.println("Packing failed.");
                }
        }

        private static void writePackingResultToJson(PackagerResult result, String filename) throws Exception {
                // Extract the packed containers from the result
                List<Container> packedContainers = result.getContainers();

                // Create a visualizer factory (set `true` to calculate points)
                DefaultPackagingResultVisualizerFactory factory = new DefaultPackagingResultVisualizerFactory();

                // Write JSON to file
                File outputFile = new File(filename);
                try (OutputStream out = new FileOutputStream(outputFile)) {
                        factory.visualize(packedContainers, out);
                }
        }

}
