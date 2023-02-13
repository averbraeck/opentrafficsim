# GTU generators

In principle any object in simulation can create GTUs on the network, and a few pre-defined generators are available in OTS. Here only one such generator is discussed as is it the most used generator, and as it is general purpose and modular. This generator is `LaneBasedGtuGenerator`. It has the following components:

* _Inter-arrival time generator_. Draws (random) headways. This is explained in section [Inter-arrival time generator](iat-generator.md).
* _GTU characteristics generator_. Draws (random) characteristics based on which the next GTU is created. This includes all GTU properties such as GTU type, length, tactical planner, parameters, etc. This is explained in section [GTU characteristics generator](gtu-characteristics.md).
* _Positions_. One or more positions, over one or more links. Drawing a next position is explained in section [Positions](positions.md).
* _Room checker_. Is used to check if the next GTU fits at the position. Otherwise GTU generation is postponed. This is explained in section [Room checker](room-checker.md).
* _ID generator_. Simple component of type `IdGenerator` that generates an id for the generated GTUs. This can be shared between GTU generators.

<pre>
<b>Lane-based GTU generator</b>
&lfloor; Inter-arrival time generator
&lfloor; GTU characteristics generator
&lfloor; Positions
&lfloor; Room checker
&lfloor; ID generator
</pre>

The `LaneBasedGtuGenerator` has three methods that define a few simulation choices. These are listed below, and further explained in section [Options for GTU generation](traffic-od.md#options-for-gtu-generation).

* `setNoLcDistance(Length)` to set the distance over which GTUs may not change lane after the generator.
* `setInstantaneousLaneChange(boolean)` to use instantaneous lane changes or not.
* `setErrorHandler(GtuErrorHandler)` to set the handler of GTU errors.
