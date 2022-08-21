# Origin-destination matrix

Demand is usually defined in Origin-Destination matrices, or OD matrices for short. This means that the starting point and end point of a trip are specified. OD matrices are the most common way to define demand for simulation. Each origin and each destination is a node in the network. Classically, for each origin the OD matrix has a row, and for each destination the OD matrix has a column. In each cell of the matrix the demand for the specific Origin-Destination pair (OD pair) is given. Additionally, a dynamic OD matrix can be seen as a set of stacked OD matrices, each defining demand over some time period.

In OTS the OD matrix is not an actual matrix, but a more flexible structure that can represent the above simple matrices, as well as more complex demand mixtures. The flexibility is provided by the following:

* <i>Category</i>. Demand is specified not only for a specific OD-pair, but also for a category. A category contains a flexible set of objects that defines for what the demand is applicable. This can virtually by anything, but typically this may be demand per GTU type, per route, and/or per lane. Each category within an OD matrix has to match the OD matrix’ categorization, which specifies the type of objects a category should define. This assures that all demand in the matrix is for example specified per GTU type. It is possible to use no further categorization per OD-pair by using `Categorization.UNCATEGORIZED` and `Category.UNCATEGORIZED`, which are empty.
* <i>Time</i>. Demand is specified over time, which cuts the simulation time in demand periods. For every instance of demand data provided to the OD matrix, a separate time array may be provided. This means that for example 5-minute car data can easily be combined with 15-minute truck data. If no time is provided, the time array of the OD matrix is used. The provided frequency data should be of equal length as the time data used (one frequency value for each time period).
* <i>Interpolation</i>. Demand data can be specified to have either of two forms of interpolation. This can either be stepwise (`Interpolation.STEPWISE`), or linear (`Interpolation.LINEAR`). For stepwise demand, demand is constant within a time period. The last provided frequency has no effect, as demand in the last period is fully defined by the before-last frequency. Linearly interpolated demand allows piecewise linear demand patterns. Similar to time, interpolation can be specified for each instance of demand data provided to the OD matrix, and the interpolation of the OD matrix is used if no interpolation is provided.

This data structure allows both simple and complex OD matrix definitions. In the simplest case, demand is uncategorized, and all demand data follows the time period(s) and interpolation as defined at the level of the OD matrix. In the most complex case, each instance of provided demand data has its own category, time vector and interpolation.

<pre>
<b>Origin-destination matrix</b>
&lfloor; Origins {Node}
&lfloor; Destinations {Node}
&lfloor; Categorization
&lfloor; Time {Time}
&lfloor; Interpolation
&lfloor; {Demand}
  &lfloor; Origin (Node)
  &lfloor; Destination (Node)
  &lfloor; Category
    &lfloor; <i>GTU type</i>
    &lfloor; <i>Route</i>
    &lfloor; <i>Lane</i>
  &lfloor; Frequencies {Frequency}
  &lfloor; Time {Time}
  &lfloor; Interpolation
</pre>
