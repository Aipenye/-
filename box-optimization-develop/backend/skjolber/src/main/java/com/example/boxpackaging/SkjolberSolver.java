package com.example.boxpackaging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import com.github.skjolber.packing.api.Box;
import com.github.skjolber.packing.api.Container;
import com.github.skjolber.packing.api.ContainerItem;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.packer.plain.PlainPackager;

public class SkjolberSolver {
    private List<Container> containers;
    private LinkedList<StackableItem> boxes; //to allow getLast and removeLast to work.
    private List<StackableItem> unplacedItems;

    // sorts the Boxes from biggest to smallest area
    private Comparator<StackableItem> comparator = Comparator
            .<StackableItem>comparingLong(s -> s.getStackable().getMaximumArea()).reversed();

    public SkjolberSolver() {
        this.containers = new ArrayList<>();
        this.boxes = new LinkedList<>();
        this.unplacedItems = new ArrayList<>();
    }

    // IN SKJOLBER the box dimensions are length, width and height. Instead of
    // length height and width
    public void addContainer(String name, int length, int height, int width) {
        Container container = Container.newBuilder()
                .withDescription(name)
                .withSize(length, width, height)
                .withEmptyWeight(1)
                .withMaxLoadWeight(Integer.MAX_VALUE)
                .build();
        containers.add(container);
    }

    // IN SKJOLBER the box dimensions are length, width and height. Instead of
    // length height and width
    public void addBox(String name, int length, int height, int width) {
        StackableItem box = new StackableItem(
                Box.newBuilder()
                        .withId(name)
                        .withSize(length, width, height)
                        .withRotate3D()
                        .withWeight(50)
                        .build(),
                1);
        boxes.add(box);
    }

    public List<StackableItem> getUnplacedItems() {
        if (this.unplacedItems == null) {
            throw new IllegalStateException("Call this function after solving. No unplaced items");
        } else
            return this.unplacedItems;
    }

    public List<StackableItem> getBoxes () {
        return this.boxes;
    }

    public List<Container> getContainers() {
        return this.containers;
    }

    public PackagerResult solve() {

        validateInput();
        boxes.sort(comparator);
        List<ContainerItem> containerItems = buildContainerItems();

        PackagerResult result = tryPacking(containerItems);

        while (!result.isSuccess() && !boxes.isEmpty()) {
            unplacedItems.add(boxes.getLast());
            boxes.removeLast();
            result = tryPacking(containerItems);

        }

        return result.isSuccess() ? result : null;
    }

    private void validateInput() {
        if (this.containers == null || this.containers.isEmpty()) {
            throw new IllegalStateException("No containers defined.");
        }
        if (this.boxes == null || this.boxes.isEmpty()) {
            throw new IllegalStateException("No boxes defined.");
        }
    }

    private List<ContainerItem> buildContainerItems() {
        return ContainerItem.newListBuilder().withContainers(containers).build();
    }

    private PackagerResult tryPacking(List<ContainerItem> containerItems) {
        PlainPackager packager = PlainPackager.newBuilder().build();
        return packager.newResultBuilder().withContainers(containerItems).withStackables(boxes).build();
    }

}
