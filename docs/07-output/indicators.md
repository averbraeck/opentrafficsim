# Performance indicators

Performance indicators as calculated based on trajectories are extensions of `AbstractIndicator`. They integrate easily with a `Sampler` using a `Query`. It is up to the specific simulation to report the calculated indicator values either during or after the simulation, there is no standard way for this in OTS. The reporting code can report a number of indicators for a number of queries. For instance average travel time and travel time delay, for different parts of the network, different time slices, different GTU types, different routes, etc. The subset of all data over which the indicator should be calculated is defined in a `Query`. It has meta data, which are pairs of a meta data type (e.g. for GTU type, route) and applicable values in the meta data set (e.g. trucks & cars, and a route from A to B). Using meta data types, categorization of indicator values is possible. For segregation in time and/or space, a number of applicable space-time regions is attached to the query. With the query being coupled to a sampler, the method `getTrajectoryGroups(…)` can provide all applicable (parts of) trajectories.

<pre>
<b>Query</b>
&lfloor; Sampler
&lfloor; Meta data
  &lfloor; {Meta data type}
  &lfloor; {Meta data set}
&lfloor; {Space-time region}
</pre>

The reporting code obtains the trajectory groups of a query, and forwards these to an indicator. (The indicator also receives the query so that calculated values can internally be stored per unique input to prevent recalculating the same value. This is especially useful if some indicators are derived using other indicators. The indicator thus has all information to derive the trajectory groups. It is still given as separate input by the reporting code as calculating the applicable trajectory groups is expensive, and should only be done once for multiple indicators based on the same query.) The indicator calculates the value over the provided trajectory groups. As an example, the code below calculates the value for `MeanTripLength`. It loops all trajectory groups and trajectories to sum the total travelled distance. As one GTU is likely to have multiple `Trajectory`’s (due to lane changes and different sections), the total travelled distance is divided by the number of unique GTU ids encountered (not the total number of `Trajectory`’s).

```java
    protected Length calculate(…, final List<TrajectoryGroup> trajectoryGroups)
    {
        Length sum = Length.ZERO;
        Set<String> gtuIds = new HashSet<>();
        for (TrajectoryGroup trajectoryGroup : trajectoryGroups)
        {
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                sum = sum.plus(trajectory.getTotalLength());
                gtuIds.add(trajectory.getGtuId());
            }
        }
        return sum.divideBy(gtuIds.size());
    }
```

Indicators can also use other indicators. For instance, the code below uses total travel time from an indicator which was given to the class in the constructor. Mean density is calculated by dividing total travel time by the total space-time area.

```java
    protected LinearDensity calculate(…, final Time startTime, final Time endTime, 
        final List<TrajectoryGroup> trajectoryGroups)
    {
        double ttt = this.travelTime.getValue(
            query, startTime, endTime, trajectoryGroups).si;
        double area = 0;
        for (TrajectoryGroup trajectoryGroup : trajectoryGroups)
        {
            area += trajectoryGroup.getLength().si * (endTime.si - startTime.si);
        }
        return new LinearDensity(ttt / area, LinearDensityUnit.SI);
    }
```
