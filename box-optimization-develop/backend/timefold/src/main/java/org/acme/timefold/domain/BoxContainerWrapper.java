package org.acme.timefold.domain;
import java.util.List;

// Simple wrapper that exists for the JSON reader
public class BoxContainerWrapper {
    private Container container;
    private List<Box> boxes;

    // Getters and setters
    public Container getContainer() { return container; }
    public void setContainer(Container container) { this.container = container; }

    public List<Box> getBoxes() { return boxes; }
    public void setBoxes(List<Box> boxes) { this.boxes = boxes; }
}