# Categorical perception

Perception is at its most generic level `Perception` defined to exist out of `PerceptionCategory`s. Each category is intended to perceive a certain part of the world. For instance we have:

* `InfrastructurePerception`; contains per lane information on required lane changes, speed limits, and both legal and physical lane change possibility.
* `NeighborsPerception`; contains per lane information on first leaders/followers (may be multiple due to lane merges/splits) and whether a GTU is alongside for gap-acceptance, and leaders/followers within a certain perception range.
* `IntersectionPerception`; contains information per lane on traffic lights and intersection conflicts.

More categories are available or could be created. Perception to a large extent determines the simulation speed in OTS, so only perception categories that are required for the model should be included. With perception working in categories, any mixture of implementations for the categories may be used. A model can be made tolerant to any implementation of a perception category by requesting the category at the most generic level. For instance, `NeighborsPerception` is an interface which may have various implementations, e.g. with or without perception errors and reaction time. Regardless of the implementation, the category can be obtained as:

```java
    NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
```

If a model relies on a more specific implementation, the class of said implementation can be used instead. An exception is thrown whenever the implementation is not available.

Each perception category has an `updateAll()` method. It is called by the perception if the tactical planner calls `Perception.perceive()`, which usually occurs at a first step of a tactical planner determining an operational plan. This is however a voluntary path. A tactical planner may also not call `Perception.perceive()`, or `updateAll()` may choose to do nothing or a partial perception. In such cases the implementations of required perception categories in the tactical model should be able to determine the information when it’s requested. For efficiency reasons, this should not occur every time the information is requested, but only if the information is required at a different simulation time.

Perception categories are free to have any method in order to supply information to models. Typically the following design pattern is used.

* `getX()`; method to obtain information `X`.
* `updateX()`; method to update information `X`.
* `getX(RelativeLane lane, …)`; method to obtain information `X` pertaining to a given lane, or other specification.
* `updateX(RelativeLane lane, …)`; method to update information `X` pertaining to a given lane, or other specification.
* `updateAll()`; implemented to call all `updateX(…)` methods, using a loop over `RelativeLane`s on methods requiring a lane, or additional loops or separate lines for other specifications. Available lanes can be obtained using `getPerception().getLaneStructure().getCrossSection()`.

Some implementations of the most common categories are available as listed below. For a full list see package `org.opentrafficsim.road.gtu.lane.perception.categories`.

* `InfrastructurePerception`
    * `DirectInfrastructurePerception`; non-delayed exact information
* `NeighborsPerception`
    * `DirectNeighborsPercetion`; non-delayed exact information.
* `IntersectionPerception`
    * `DirectIntersectionPerception`; non-delayed exact information.
