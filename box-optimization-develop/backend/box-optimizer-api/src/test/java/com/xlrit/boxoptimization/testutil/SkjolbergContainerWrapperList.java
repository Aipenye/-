package com.xlrit.boxoptimization.testutil;

import com.github.skjolber.packing.api.Container;
import java.util.List;

public class SkjolbergContainerWrapperList {
  List<Container> containerList;

  public SkjolbergContainerWrapperList(List<Container> containerList) {
    this.containerList = containerList;
  }

  public List<Container> getContainers() {
    return this.containerList;
  }

  public Boolean equals(SkjolbergContainerWrapperList otherList) {
    List<Container> trueOtherList = otherList.getContainers();
    for (int i = 0; i < containerList.size(); i++) {
      if (!equalContainers(containerList.get(i), trueOtherList.get(i))) return false;
    }

    return true;
  }

  private Boolean equalContainers(Container a, Container b) {
    if (!a.getDescription().equals(b.getDescription())
        || a.getEmptyWeight() != b.getEmptyWeight()
        || a.getLoadVolume() != b.getLoadVolume()
        || a.getMaxLoadWeight() != b.getMaxLoadWeight()
        || a.getLoadWeight() != b.getLoadWeight()
        || a.getMaxLoadVolume() != b.getMaxLoadVolume()
        || a.getVolume() != b.getVolume()) return false;

    return true;
  }
}
