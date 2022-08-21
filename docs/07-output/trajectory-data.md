# Trajectory data

Trajectory data is data where the state of individual GTUs is stored at some (ir)regular interval. The state can be a number of properties of the GTU such as position and speed, but also any other user-specified information. To gather trajectory data there is a separate java project `ots-kpi`. The sampler is also used to derive key performance indicators from trajectory data, which is discussed in section [Performance indicators](/simulation-output/performance-indicators). This project is independent from the OTS simulation and is also used to gather trajectories and key performance indicators with other simulation environments. Before key performance indicators are discussed, this section discusses how trajectory data is gathered and defined using the sampler.

The `Sampler` is the central coordinator of trajectory data. As it operates stand-alone, it uses a set of light-weight interfaces to represent GTUs, nodes, routes, lanes, etc. A specific implementation of `Sampler` that connects the sampler with a specific simulation environment provides content for such interfaces. Furthermore, the implementation implements a few abstract methods that the sampler uses, as such implementations are simulation environment specific. For OTS itself the implementation is `RoadSampler` and accompanying classes, each of which wraps an OTS class. For example the interface `GtuDataInterface` is implemented by `GtuData`, which wraps a `LaneBasedGTU` to supply the information as requested by the sampler. Similar wrapping classes can be used for other simulation environments, possibly working with a data bus rather than directly with the simulation environment.

The sampler stores trajectory information for regions of space-time that are registered to it as `SpaceTimeRegion`. A space-time region is spatially confined to a single lane and direction of travel on the lane, i.e. a lane direction. Gathering information over multiple space-time regions is done using a `Query`, which is discussed in the next section. Many space-time regions can be registered to the sampler, including space-time regions that overlap in space and/or time. When a space-time region is registered to the sampler, the sampler will request the implementing class to schedule start and stop recording commands at the right time on a particular lane direction as a whole. When receiving such commands from the implementation, the sampler will check if recording was not already started, or whether stopping should be skipped for an overlapping space-time region. 

<pre>
<b>Sampler</b>
&lfloor; {Space-time region}
  &lfloor; Lane direction
    &lfloor; Trajectory group
      &lfloor; {Trajectory}
&lfloor; {Extended data types}
&lfloor; {Meta data types}
</pre>

As GTUs move in, over and out of a lane, they record a `Trajectory` within a `TrajectoryGroup` coupled with the lane direction. This means that one such trajectory only covers the part that a GTU moves over the lane consecutively. Due to lane changes, one GTU can thus result in multiple `Trajectory`'s within a `TrajectoryGroup`. Moving in, over or out of a lane happens as the simulation environment specific implementation calls methods `processGtuAddEvent(…)`, `processGtuMoveEvent(…)` or `processGtuRemoveEvent(…)` of the `Sampler`. The `RoadSampler` does so as a response to internal OTS events as discussed in section [Event producers and listeners](/model-structure/dsol-and-event-based-simulation#event-producers-and-listeners). A trajectory has several arrays of equal length inside that contain position, speed, acceleration and time data. Additional data of equal length is found in optional extended data types. The `Trajectory` can supply individual values at a particular index, or entire arrays. This includes an underlying storage type for the base and extended data types, which may be different from the eventual output type. For example, internally values may be stored in a `float[]`, while the output is `FloatDurationVector` from DJUNITS. The `Trajectory` can also supply total length and total duration, which is sufficient for some basic performance indicators, and doesn’t require the whole data to be processed.

<pre>
<b>Trajectory</b>
&lfloor; {Position} 
&lfloor; {Speed}
&lfloor; {Acceleration}
&lfloor; {Time}
&lfloor; {Extended data}
  &lfloor; {Data}
</pre>

Extended data types are sub classes of `ExtendedDataType`. They are added to all trajectories by the `Sampler` upon creation of a new `Trajectory` (in `processGtuAddEvent(…)`) if the extended data type is registered to the `Sampler`. The `Trajectory` stores the data, but the extended data type manages the storage in its methods. For most purposes the class `ExtendedDataTypeFloat` or one of its available sub classes is suitable, in particular for DJUNITS data types. It takes care of storage management, increasing the storage array when needed. The sub classes only need to implement two conversion methods from float to the specific type, for a single value or for the entire array. The classes shown below for duration, length and speed do exactly this. These classes are abstract as it is still not determined what is actually stored in the extended data type. Extensions of these classes can for example determine the time-to-collision, the driver workload, or any other value based on a GTU. With the super classes taking care of data management, these implementation only need to define a single `getValue(…)` method to obtain the value from a GTU. Class `ExtendedDataTypeNumber` is useful for dimensionless values. Finally, `ExtendedDataTypeList` stores values in an internal `List`, which is conceptually easier but less efficient.

<pre>
<b>Extended data type</b>
&lfloor; Extended data type float
  &lfloor; Extended data type duration
  &lfloor; Extended data type length
  &lfloor; Extended data type speed
&lfloor; Extended data type number
&lfloor; Extended data type list
</pre>

A final important part of the `Sampler` are the meta data types. These define data attached to a `Trajectory` that is singular by nature, rather than the time-sampled trajectory data. The primary purpose of this data is to allow trajectory filtering. For example, one could be interested in the travel time along a certain route, or between a given origin-destination pair. To this end meta data types have an `accept(…)` method. This is closely related to the `Query`’s as explained in the next section. Similarly to extended data types, meta data types are registered to a `Sampler`, and included with a `Trajectory` when it is created in `processGtuAddEvent(…)`.

<pre>
<b>Meta data type</b>
&lfloor; Meta data cross-section
&lfloor; Meta data destination
&lfloor; Meta data GTU type
&lfloor; Meta data origin
&lfloor; Meta data route
</pre>

So far this section has described how trajectories are created, and how extended data and meta data can be added to the trajectories. The next section discusses how key performance indicators can be derived from the trajectories. Another approach is ex post processing of trajectories. The `Sampler` has methods named `writeToFile(…)`, which stores all trajectory data within the sampler to a comma-separated text file. Each line contains: trajectory number, link id, lane id with direction (+/-), GTU id, time, position, speed, acceleration, extended data…, meta data…. Note that the trajectory number is a simple counter to identify individual trajectories. Usually the movement of a single GTU is spread over multiple such trajectories. Meta data is only stored in the first row of each trajectory. Data can be stored in a given format, which is 3 decimal places by default. Finally there are three compression options: none, zip or omit-duplicate-info. The zip method puts the resulting file in a zip file. Omit-duplicate-info puts link id, lane id with direction and GTU id only on the first line of each trajectory. Skipped data always results in consecutive separators, e.g. “108,,,,25,…” for trajectory number 108 at time 25s. Below is some example output of two trajectories. The trajectories include meta data type ‘Length’ and extended data types for ‘Rho’, ‘V0’, and ‘T’.

<pre>
    traj#,linkId,laneId&dir,gtuId,t,x,v,a,Length,Rho,V0,T
    …
    4323,AB,laneAB2+,2398,3267.500,3967.532,33.433,-0.199,,0.000,34.135,1.057
    4323,AB,laneAB2+,2398,3268.000,3984.224,33.334,-0.194,,0.000,34.135,1.060
    4323,AB,laneAB2+,2398,3268.500,4000.867,33.237,-0.189,,0.000,34.135,1.062
    4324,AB,laneAB2+,2396,3251.000,3784.761,30.041,0.727,4.190,0.000,37.346,0.676
    4324,AB,laneAB2+,2396,3251.500,3799.873,30.404,0.701,,0.000,37.346,0.686
    4324,AB,laneAB2+,2396,3252.000,3815.162,30.755,0.675,,0.000,37.346,0.697
    …
</pre>
