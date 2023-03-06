# Link level

The network is represented as a directed graph, using links and nodes. These are defined in classes `OtsLink` and `Node`. The class `OtsNetwork` has a list of all links and nodes, as well as other objects in simulation and shortest-path utilities. Nodes are simply points with a direction and id, while links go from one node to another along some design line. The design line does not necessarily start or end in the node locations, allowing some lateral play to accommodate lanes on the micro level. Connections between links and nodes is by reference, and not (necessarily) spatially.

<pre>
<b>OTS Link</b>
&lfloor; Id
&lfloor; Start node (Node)
&lfloor; End node (Node)
&lfloor; Design line
&lfloor; GTUs
&lfloor; Link type
</pre>
 
An essential part of each link is the `LinkType`. This defines which `GtuType`s are allowed. The class `LinkType` is hierarchical, meaning that it can have a parent type. Allowance of GTU types is as such inherited from the parent and partially overwritten. For example, the type `ROAD` allows all GTU types of type `ROAD_USER` or any sub-type. Link type `FREEWAY` is defined as a sub-type of `ROAD`, and adds a prohibition for `PEDESTRIAN` and `BICYCLE` GTU types.


## Routes and shortest routes

A route in class `Route` is defined as a series of nodes. All nodes need to be defined (not just at the splits).

A convenient way to obtain a route is by using `OtsNetwork.getShortestRouteBetween(…)`. There are a few versions, depending on whether there are intermediate nodes specified, and whether a link weight is provided. By default, a link weight using the link length is used, providing the proper shortest path. The default also gives a high weight to connectors, such that no routes are defined over two connectors forming a shortcut, unless absolutely necessary. Any link weights can however be used by extending interface `LinkWeight`, which returns a weight (the cost of traveling) for each link. It may additionally provide a heuristic for the A\* algorithm. If this is provided (not `null`) A\* will be used. Otherwise Dijkstra's shortest path algorithm is used.
