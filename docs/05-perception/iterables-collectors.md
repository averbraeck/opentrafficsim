# Perception iterables and collectors

Perception is a performance- and memory-heavy task. Some tools in OTS are available to alleviate this when possible, in the form of perception iterables and collectors. The iterables allow looping over several objects in simulation, without performing perception on each. For example, for a regular car-following model perception may occur on the first leader, and then stop, while a more advanced car-following model may perceive multiple leaders. In both cases it is a waste of computer power to perceive those leaders within a certain distance that the model never uses. Hence, a simple `Set` of all perceived leaders is not efficient. Instead the perception category `NeighborsPerception` returns `PerceptionCollectable` (a type of perception iterable). Generally, iterables perform perception on-demand, rather than supplying a fully perceived set.

Perception collectors allow to get an aggregated quantity over a set of objects. Rather than perceiving each individual object and then aggregating those results, collectors consider the simulation objects directly and may possibly perceive only the aggregated result erroneously. This circumvents expensive perception of the individual objects. For example, when estimating the density based on vehicles ahead, it’s a waste of computer power to perceive all individual vehicles, especially if no other part of the model needs all those perceived vehicles.

The following sub-sections explain perception iterables and collectors in more depth. It is important to understand the difference between collectables (a type of  iterable allowing collection) and a collector (which performs the collection in cooperation with a collectable).


## Perception iterables and collectables

The `PerceptionIterable` interface extends `Iterable` and can thus be used in loops. It defines the iteration order of objects of a type extending `Headway` (`H`) as being from close to far. Like an ordinary iterator it does not guarantee to be iterable multiple times. Collectors, which are explained in the next section, work closely with a `PerceptionCollectable`. This interface defines methods to collect multiple underlying objects of type `U` in to a single perceived result, and also contains interfaces for the tools that make up a collector. 

The class `AbstractPerceptionReiterable` defines functionality at the highest level, namely a methodology of using two levels of iterators. The first iterator functions as a search algorithm, implemented by a sub-class in `getPrimaryIterator()`. For instance, this can be a search downstream on the network, assuring that the next nearest object is found. The second iterator is what makes the class re-iterable. Each time an iterator is requested, a new secondary iterator (`PerceptionIterator`) is created. Each instance uses the same linked-list of entries concerning the found objects. This includes a perceived version of the object. In case any instance runs to the end of the linked-list, the primary iterator is requested to find and return the next object, and an element is added to the linked-list for all secondary iterators to use. Thus, both the search and perception occur once for required objects, no matter how often a model iterates over the set of objects at a specific time. No search is performed for objects that are not used, and no perception is done on objects that are only collected. `AbstractPerceptionReiterable` also implements the `collect(…)` method for collectors.

The class on the next level is `AbstractPerceptionIterable`. Besides a headway type `H` and an underlying object type `U`, it also defines a lane record type `R` and a counter type `C`. The class defines a primary iterator that can search along a lane structure as explained in the [lane-structure section](/perception/lane-structure). It makes sure that in case of branches, the nearest next object is always returned. The specific type of lane record can be specified in `R`, allowing a search on the readily available `LaneStructure` with `LaneStructureRecords`, as well as outside of this using `LaneDirectionRecords`. The latter is for instance useful when searching for conflicting GTUs on a crossing road not part of the `LaneStructure`. The `LaneStructure` should be used whenever possible for efficiency, as `LaneDirectionRecords` build up a new tree every time. The counter of type `C` can be used in the search to quickly find a next object on a lane. For instance, when searching for GTUs on a lane it is efficient to find the next GTU using its index (using Integer for `C`) in a sorted list of GTUs of the lane.

<pre>
PerceptionIterable&lt;H&gt;
&lfloor; PerceptionCollectable&lt;H, U&gt;
  &lfloor; AbstractPerceptionReiterable&lt;H, U&gt;
    &lfloor; primaryIterator()
    &lfloor; PerceptionIterator()
    &lfloor; AbstractPerceptionIterable&lt;H, U, R, C&gt;
      &lfloor; DownstreamNeighborsIterable&lt;R&gt;
      &lfloor; UpstreamNeighborsIterable&lt;R&gt;
      &lfloor; LaneBasedObjectIterable&lt;H, L, R&gt;
    &lfloor; ConflictGtuIterable
    &lfloor; MultiLanePerceptionIterable&lt;H, U&gt;
&lfloor; PerceptionIterableSet&lt;H&gt;
</pre>
 

Classes `DownstreamNeighborsIterable` and `UpstreamNeighborsIterable` are very similar. They implement a single-branch downstream or upstream search in the method `getNext(…)`, which the primary iterator uses, they implement the method `getDistance(…)` required in the search, and finally they implement the method `perceive(…)` to return a perceived version of type `H` of an underlying object of type `U`. For the latter method both classes are modular as they use a `HeadwayGtuType` to return any kind of perceived version. Class `LaneBasedObjectIterable` is similar too, though the search is slightly different and perception is left to a sub-class. Such sub-classes specify `L`, the type of the underlying lane based object.

Class `ConflictGtuIterable` is a utility that a conflict supplies. It too uses two layers of iterators as defined in `AbstractPerceptionReiterable`, now possibly facilitating multiple GTUs perceiving a conflict at the same time step. At each time, the conflict sets up (if requested) a base iterator for downstream GTUs and a base iterator for upstream GTUs, of types `DownstreamNeighborsIterable` and `UpstreamNeighborsIterable` but using `LaneDirectionRecords`. These primary iterators only function to provide the underlying objects, which they do through light-weight wrapper perception classes. This is done as only individual GTUs should perform the perception. Every time a `ConflictGtuIterable` is requested, it is initialized with a possibly different `HeadwayGtuType`, specified by the perceiving GTU.

To search on multiple lanes the class `MultiLanePerceptionIterable` is useful for either GTUs or lane-based objects. One can add an `AbstractPerceptionReiterable` per lane to consider. An internal primary iterator considers all lanes.

Finally, class `PerceptionIterableSet` is a utility class which wraps no, a single or a set of perceived objects in a `PerceptionIterable` form. This is useful whenever a model has either of this and a utility expecting a `PerceptionIterable` needs to be used. For example, when using a car-following model to stop for a traffic light, the model has a single `HeadwayTrafficLight`, while the method `followingAcceleration(…)` of `CarFollowingModel` expects a `PerceptionIterable<? extends Headway>`.


## Perception collectors

Collectors allow efficient perception of an aggregated result over a set of objects, without perceiving each individual object in the set. The mechanism is similar to `Stream.collect(…)` which is part of java streams. The ‘stream’ is provided by a `PerceptionCollectable`, which extends `PerceptionIterable`. The collectable provides objects of underlying type `U`, which are only searched once at a particular time even if multiple collections are performed. Perception of the individual objects is not triggered in this process.

The collector of type `PerceptionCollector` is merely a combination of three components, an identity, an accumulator, and a finalizer. Together these form a process that ends in a single collected result of type `C`. The process is implemented in the method `AbstractPerceptionReiterable.collect(…)`. It starts by obtaining an initial intermediate result, the identity, from a supplier. Then, for each underlying object of type `U` the intermediate result is updated. The final intermediate result is translated to a collected result of type `C` by the finalizer.

<pre>
PerceptionCollector&lt;C, U, I&gt;
&lfloor; Identity (Supplier&lt;I&gt;)
&lfloor; Accumulator (PerceptionAccumulator&lt;U, I&gt;)
&lfloor; Finalizer (PerceptionFinalizer&lt;C, I&gt;)
</pre>

As an example, suppose the density needs to be derived from a set of leading GTUs. The intermediate type is a small class that has a number of GTUs and a distance. The identity supplies one such object with a zero count and a `null` distance. The accumulator adds 1 to the count, and sets the distance to the considered GTU (note that we iterate close to far). Once all GTUs are considered, the finalizer divides the distance by the GTU count, and returns a density value. For other implementations the intermediate type may be the same as the collected type, meaning that the finalizer simply forwards the last intermediate result.
