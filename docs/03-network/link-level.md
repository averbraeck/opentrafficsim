# Link level

The network is represented as a directed graph, using links and nodes. These are defined in classes `OTSLink` and `OTSNode`. The class `OTSNetwork` has a list of all links and nodes, as well as other objects in simulation and shortest-path utilities. Nodes are simply points with a direction and id, while links go from one node to another along some design line. The design line does not necessarily start or end in the node locations, allowing some lateral play to accommodate lanes on the micro level. Connections between links and nodes is by reference, and not (necessarily) spatially.

<pre>
<b>OTS Link</b>
&lfloor; Id
&lfloor; Start node (Node)
&lfloor; End node (Node)
&lfloor; Design line
&lfloor; GTUs
&lfloor; Link type
</pre>
 
An essential part of each link is the `LinkType`. This defines which `GTUTypes` are allowed in which direction (`LongitudinalDirectionality`). This direction can be plus, minus, both or none, and differ among GTU types. For example, a one-way street may allow motor vehicles in only one direction, but cyclists in both. The class `LinkType` is hierarchical, meaning that it can have a parent type. Allowance of GTU types is as such inherited from the parent and partially overwritten. For example, the type `ROAD` allows all GTU types of type `ROAD_USER` or any sub-type. Link type `FREEWAY` is defined as a sub-type of `ROAD`, and adds a prohibition for `PEDESTRIAN` and `BICYCLE` GTU types.


## Routes and shortest routes

A route in class `Route` is defined as a series of nodes. It needs sufficient node information such that at every split in the network graph the next link can be determined. An extension is `CompleteRoute` in which all nodes need to be defined.

A convenient way to obtain a route is by using `OTSNetwork.getShortestRouteBetween(…)`. There are a few versions, depending on whether there are intermediate nodes specified, and whether a link weight is provided. By default, a link weight using the link length is used, providing the proper shortest path. Any set of link weights can however be used by extending interface `LinkWeight`, which returns a weight (the cost of travelling) for each link.
