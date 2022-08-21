# Lane-based objects

GTUs and objects along the road can interact in various ways. In OTS such objects are located at any position on a lane, pertaining to some or all of the possible directions of travel on the lane.

<pre>
OTS Link
&lfloor; Cross section link
  &lfloor; Lane
    &lfloor; {<b>Lane-based object</b>}
      &lfloor; Lane
      &lfloor; Direction
      &lfloor; Longitudinal position
</pre>

Many types of objects exist or can be created by extending `AbstractLaneBasedObject`. The next few sections will discuss sensors, traffic lights and conflicts. Other lane-based objects include a bus stop, distraction or a speed sign, but these are not discussed in more detail here.


## Sensors
Sensors are treated differently from other lane-based objects in that they are stored in a separate list at the lane. When GTUs move they automatically trigger any sensor they cross. Such triggers pertain to a specific `RelativePosition` of the GTU, e.g. the front for a detector.

An important type of sensor is the `Detector`, which is a type of sensor that can be geared to measure any set of measurements. There are two types of such measurements as defined in `DetectorMeasurement`, namely periodic (or aggregate) measurements, and meso measurements with growing arrays of individual measurements. Typical periodic measurements are mean speed, harmonic mean speed and occupancy, for which implementations are defined under the fields `MEAN_SPEED`, `HARMONIC_MEAN_SPEED` and `OCCUPANCY`. Note that flow is measured intrinsically by the detector.

<pre>
<b>Sensor</b>
&lfloor; Detector
  &lfloor; {Measurements}
    &lfloor; <i>Mean speed measurement</i>
    &lfloor; <i>Harmonic mean speed measurement</i>
    &lfloor; <i>Occupancy</i>
    &lfloor; <i>Passages</i>
    &lfloor; <i>Platoon sizes</i>
</pre>

Some meso measurements are also available such as `PASSAGES` to remember all individual passage times, and the class `PlatoonSizes` which requires a threshold time headway.

When a detector is triggered, it successively triggers all the measurements it has. This happens for both the GTU front and rear, as detectors have a secondary sensor that triggers when the rear leaves the detector. (Due to lane changes, it is possible that only the front or only the rear of a GTU triggers the detector. Measurements should be able to cope with this.) Each measurement can accumulate its paired measurement data based on these triggers. Furthermore, for periodic measurements, the detector invokes an aggregation. A detector has a single aggregation period for all periodic measurements. The actual data of all measurements is maintained by the detector. Many measurements can therefore be stateless entities.

The data that detectors store can easily be exported to compressed .csv files using the `writeToFile(…)` methods. This will either write all periodic, or all meso measurements to a file, for all detectors in a network.

A final type of sensor that occurs often is the `SinkSensor`. It has the simple job of destroying GTUs from the simulation when they pass the sensor.


## Traffic lights and control

Traffic lights are simple lane-based objects of which the light color can be set or retrieved. The traffic light color can also be black, meaning that the traffic light is inactive. A fixed-time controller for traffic lights is available under `TrafficLightControllerFixedDuration`. Control can be much more complex and in OTS there is even a separate java project for traffic control (ots-trafficcontrol). This contains classes for CCOL and TrafCOD traffic light controllers.


## Conflicts

Conflicts are areas where two lanes, usually from different links, overlap, and hence GTUs should regard traffic on both lanes. In OTS one such conflict area is represented by two `Conflict`s, one on each of the conflicting lanes. The `ConflictType` determines the geometrical nature of the conflict, being either a crossing, merge on split. This has different implications on behavior. The conflict rule determines priority. Both the conflict type and priority are common for both conflicts in a pair. Finally, conflicts can provide upstream and downstream GTUs, as well as the other conflict in the pair. Note that upstream and downstream GTUs of the other conflict are of interest when considering a conflict.

<pre>
<b>Conflict</b>
&lfloor; Conflict type
  &lfloor; <i>Crossing</i>
  &lfloor; <i>Merge</i>
  &lfloor; <i>Split</i>
&lfloor; Conflict rule
  &lfloor; Conflict priority
&lfloor; Upstream GTUs
&lfloor; Downstream GTUs
&lfloor; Other conflict
</pre>

There are different conflict rules that can be used. Conflict rules provide a priority to a GTU such that it can perform the correct behavior. There are six kinds of priority, where ‘priority’, ‘yield’ and ‘stop’ are self-explanatory. Priorities ‘turn on red’ and ‘all stop’ are used in some countries. Turn on red means that drivers are allowed to make a turn while a traffic light shows red. All stop means that everyone has to stop and the first person to arrive has right of way, etc. Finally, priority ‘split’ is a placeholder for split conflicts where priority is not an issue, but where GTUs on another lane may influence GTU movement.

<pre>
Conflict
&lfloor; Conflict rule
  &lfloor; <b>Conflict priority</b>
    &lfloor; Priority
    &lfloor; Turn on red
    &lfloor; Yield
    &lfloor; Stop
    &lfloor; All stop
    &lfloor; Split
</pre>

The conflict priority that conflict rules provide may be dynamic (switching priority) and may be based on information at the link. The list of available priority values with a link is very similar to the available priority values of a conflict rule, but they are fundamentally different. The link priority is information that is used to determine which rule applies and which direction has priority. The conflict priority is a piece of information that directly tells a GTU how to behave.

<pre>
Cross-section link
&lfloor; <b>Priority</b>
  &lfloor; Priority
  &lfloor; None
  &lfloor; Turn on red
  &lfloor; Yield
  &lfloor; Stop
  &lfloor; All stop
  &lfloor; Bus stop
</pre>

In OTS there are three types of conflict rules: split, bus stop and default. The split rule is a placeholder as with conflict priority. The bus stop conflict rule looks for a link with priority value ‘bus stop’, and then whether the first upstream GTU on that link is of GTU type bus with the left indicator on. In that case the bus has priority, otherwise not.

<pre>
<b>Conflict rule</b>
&lfloor; Split
&lfloor; Bus stop
&lfloor; Default
</pre>

The default conflict rule implements all ‘regular’ priority rules using the link priority. In particular the following conflict rules apply from the discussed link priorities:

* _All stop_; if both conflicts have priority all stop.
* _Right-hand has priority_; if both conflicts have the same priority (excluding ‘bus stop’ and ‘turn on red’), or they are a pair of ‘stop’ and ‘yield’ priorities.
* _Priority and yield_; if one has ‘priority’ or ‘none’, and the other has ‘none’ or ‘yield’ (the case with both ‘none’ is already captured above).
* _Priority and stop_; if one has ‘priority’ or ‘none’, and the other has ‘stop’.
* _Priority and turn on red_; remaining cases.

With the link priorities defined, OTS allows automated generation of conflict areas, preventing much manual labor. The utility `ConflictBuilder` provides the tools to do this. It has several methods named `buildConflicts(…)` which do so with various input. The network elements that can be provided is either the entire network, a set of lanes, or two particular lanes. Overlap between all lanes contained in the input is automatically determined, except for any lane combination determined in the ignore list. The permitted list contains lane combinations for which the conflict is a permitted conflict during a traffic light cycle, a property that will be stored with the generated conflict between the lanes.

<pre>
<b>Conflict builder</b>
&lfloor; Network / Lanes
&lfloor; Width generator
&lfloor; Ignore list
&lfloor; Permitted list
</pre>

Whether and where lanes overlap/cross depends on the width that is used. Given that lanes are wider than GTUs, and given that lane edges of adjacent lanes may not perfectly align (causing overlap that shouldn’t be there), it is not reasonable nor practical to use the full lane width. The width of a GTU is determined with the provided width generator. There is a default width generator under the field `DEFAULT_WIDTH_GENERATOR` which uses 80% of the lane width. Other options are to create an instance of `FixedWidthGenerator` or `RelativeWidthGenerator` with either a fixed width value or a fraction of the lane width. The algorithm by which the region where lanes overlap is determined is explained in [Appendix A](/appendix-a).
