# Lane structure

The lane structure is a grid representation of the road network that facilitates perception in lane-based simulations. The grid representation simplifies the OTS network representation. The lane structure is made up out of _lane structure records_. Each lane structure record represents a lane, i.e. a cell in the grid. The records are connected laterally and longitudinally, having possibly multiple upstream and downstream connected records in case of merges and splits.

The lane structure functions as a limited scope that a driver considers. It spans a given range upstream and downstream as can be seen in Figure 3, and is the world as a driver knows it. It is dynamically built and demolished at the upstream and downstream edges as the GTU moves, and is completely rebuilt if the route changes. It has methods to obtain objects on the network: `getDownstreamObjects(…)`, `getDownstreamObjectsOnRoute(…)` and `getUpstreamObjects(…)`. The lane structure is defined with a look-ahead distance, within which the objects are given.

![](../images/OTS_Figure_5.1.png)
_Figure 5.1: Lane-structure example._

For perceiving the network itself, or GTUs on the network, methods are available to obtain the lane structure record at a given lane, adjacent to the GTU. A relatively simple search algorithm may then search upstream or downstream from that location, by traveling over the structure. Each lane structure record has a start distance relative to the GTU, where negative values mean upstream. These distances are also automatically updated and can be used to determine the distance between any object related to a lane structure record, and the GTU. 

The lane-structure is extended along the route, but also over some given distance downstream of splits on links not in the route, as well as upstream of merges on links not in the route. This allows the driver to respond to traffic on these parts, for instance to adjust speed while approaching a merge, or to slow down as there is congestion just downstream of a split, although the GTU will take another branch at the split.
