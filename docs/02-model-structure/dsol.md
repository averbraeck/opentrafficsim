# DSOL event-based simulation

OTS is built upon a simulation engine called [DSOL](https://simulation.tudelft.nl). It offers many functionalities, some of the most important for OTS being:

* Event-based simulation engine
* Experiment/replication control
* Random number streams `StreamInterface` and random distributions
* 2D animation

When creating functionality within OTS, a basic understanding of the underlying event-based simulation engine is required. The next section will discuss this.


## Event-based simulation

The event based engine is provided by DSOL in a simulator which has an internal queue of events. Each event is scheduled to occur at a specific time, and hence, time progresses as the events are performed. An event has the information as shown below. The target object, method, and input pertain directly to invoking a java method.

<pre>
<b>Event</b>
&lfloor; Execution time
&lfloor; Source object
&lfloor; Target object
&lfloor; Method to invoke on target object
&lfloor; Input for the method invocation
</pre>
 
How this can be used to keep objects active in simulation is explained with a GTU generator.
When a `LaneBasedGtuGenerator` is created, it registers an event in the simulator to draw characteristics for the next GTU.

```java
    Duration headway = this.interarrivelTimeGenerator.draw();
    if (headway != null)
    {
        this.simulator.scheduleEventRel(headway, this, "generateCharacteristics", new Object[] {});
    }
```

Time of the event will be an inter-arrival time of GTUs, relative to ‘now’. The target is the generator itself, and the method that should be invoked is `generateCharacteristics()`. As this method has no input, an empty object array is given with the event as input. To make sure more than one GTU is created, the `generateCharacteristics()` method also schedules itself repeatedly. This initialization and repeated scheduling ensures that characteristics for a GTU are generated whenever a new GTU arrives in the simulation. If no headway is returned (`null`), demand is over and the repeated scheduling stops.

The generator has a separate chain of events to actually place the GTUs on the network. It is started inside `queueGtu` invoked by `generateCharacteristics()` whenever it results in exactly one GTU in the queue. This holds for the first GTU, but also for any GTU that is created after the queue was depleted as all GTUs could be placed on the network. This event chain invokes the method `tryToPlaceGtu(…)`, which has a `GeneratorLanePosition` as input such that it is known where the GTU should be placed on the network. The position is added to the event in the input array. 

```java
    if (queue.size() == 1)
    {
        this.simulator.scheduleEventNow(this, "tryToPlaceGtu", new Object[] {lanePosition});
    }
```

The method `tryToPlaceGtu(…)` attempts to place the GTU on the network. If successful, the GTU is removed from the queue. If other GTUs remain in the queue, an event for them is scheduled at `this.reTryInterval`, i.e. 0.1s, relative to ‘now’. If the queue is empty, the method `generateCharacteristics()` will restart the event chain to place GTUs on the network as soon as the next GTU arrives. (This is a slight simplification. If a GTU is successfully placed and other GTUs remain in the queue, the event is scheduled at the same time for the next GTU. If a GTU cannot be placed, it is scheduled in `this.reTryInterval`).

```java
    if (queue.size() > 0)
    {
        this.simulator.scheduleEventRel(this.reTryInterval, this, this, 
                "tryToPlaceGTU", new Object[] { position });
    }
```

This example shows the important role of the simulator, and how it can be used to keep objects functioning over time during the simulation.