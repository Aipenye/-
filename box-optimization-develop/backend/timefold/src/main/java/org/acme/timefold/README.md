This readme explains all folders in the subdirectory src/main/java/org/acme/timefold
It is where most of the programming stuff is happening, and where you should go when you want to adjust behavior of timefold.


# Domain
Under domain, we find all our classes that represent all relevant objects during optimization.
-An AirCube represents an abstract cube of space (e.g., it can represent a cubic metre, or cubic centimetre, depending on how the end user specifies their dimensions).
It is the equivalent of a bed, in the original timefold file structure that we used as a starting point.
-A box represents any physical object with specified dimensions. It has the @PlanningEntity tag, to let timefold know that this object is what we want to optimize.
-A container has fields for all aircubes and boxes. It represents a set of aircubes in a cuboid shape where boxes can be put in.
-A boxplan has the @PlanningSolution tag, because it will contain the final solution returned by timefold. It is an object that is overarching over the container and all aircubes and boxes. When timefold is done, the boxplan object will contain the configuration of boxes that timefold returned.
-Dimensions is a class that represents the dimensions of a box that has been placed by timefold. This is used by the boxsolver class
-The Dimensionskeydeserializer is a class that is wrapped around Dimensions class and is necessary for turning a string into a Dimensions object when reading a problem set from a text file.
-BoxContainerWrapper is a class that wraps around a the container class, it is used when writing the solution to json format to write it to a file.

# Resources
Under resources, we again have a solverConfig.xml that is used by the boxsolver class that we will get to in a moment.
Here, you can change the settings of timefold. Although, we do recommend not changing the .xml file, but changing timefold settings programmatically in the boxsolver file, e.g., using solverConfig.setMoveThreadCount("AUTO"); to use more cores (this setting requires the paid version of timefold). Changing timefold settings programmatically worked most reliably in our experience.

# Rest/exception
Under rest/exception, we have some methods for error handling.
The files directly under rest are basically the backend of the timefold introductory library. The bedschedulingdemoresource.java defines a REST API endpoint that provides demo data to timefold. It does this by making a call to the demogenerator which returns a boxplan object (unsolved).
The demogenerator generates data for this object.
The bedscheduling resource allows users to submit scheduling problems, retrieve solutions and analyze scores. The function schedule_to_file was added by us to write the solution to a file. This function is not used anymore but is useful for testing purposes when timefold is isolated.
The function schedule_to_file is called whenever timefold returns a solution when it is ran from the bedschedulingresource.java. Timefold is not ran from the bedschedulingresource.java in our app.
These files do not influence our app at the moment, but they are still used for the test cases ran when you run mvn package.

# Solver
Finally, we have the solver folder.

Bedallocationconstraintprovider.java contains definitions and exports all constraints that timefold needs to follow. A constraint is either a reward or a punishment, and there are three types of both. In the comments of this file one can find more details about constraints.
This file is where you can tell timefold what you think is a good solution, and what is bad. You can punish it and reward it here. The functions we provided to make the box fitter work should be enough examples to be able to write some constraints yourself.
Boxsolver: this object was added by us. An instance of boxsolver is created in the backend with the specifications of the unsolved problem (warehouse dimensions, all boxes and their dimensions). The solve function calls timefold to solve the specified problem. In the solve function it is possible to specify some settings for timefold, as has been mentioned before.
