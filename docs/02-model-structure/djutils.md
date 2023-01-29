# DJUTILS - Delft Java Utils

OTS uses many of the utilities available in [DJUTILS](https://djutils.org), which include:

* Event producers and listeners
* `Throw` and `Try` (compact way to throw exceptions and implement a `try` code block)
* Immutable collections (similar to Java collections, but immutable)
* Means (a few classes to calculate a mean and to which data can be added incrementally)
* `Profile` (utility to perform detailed code profiling)
* Command line interface (allows standardized an easy integration of command line arguments)

The way in which event producers and listeners work is explained below. For further descriptions, see the [DJUTILS](https://djutils.org) website.


## Event producers and listeners

\[[List of all event types in OTS](../02-model-structure/events/)\]

The events between event producers and listeners should not be confused with the events in event-based simulation. Here, specific actions that may occur result in events, and any listener that registered itselft for certain events being produced by certain event producers, will then be notified of the event. We have:

* _Event type_; event types define different events. They form an understanding between producer and listener with regards to what happened for a specific event, and what sort of information comes with the event. All events are defined as `public static EventType` fields at the producer. Note that the included information with the event is not stipulated with the event type, but both producer and listener simply have to assume the same information, which is usually mentioned in the Javadoc regarding the static public field of the event type.
* _Event producer_; classes that implement `EventProducerInterface` are event producers. This interface has methods to add (`addListener(…)`) and remove (`removeListener(…)`) listeners. When a certain event occurs, and there are listeners for such an event, the event producer creates an event with payload and of a certain `EventType`, which gets send to all the listeners. Classes can extend `EventProducer` to take care of bookkeeping of listeners. This minimizes the effort on the producer to calling `fireEvent(…)` or `fireTimedEvent(…)`.

<pre>
<b>Event producer</b>
&lfloor; addListener(…)
&lfloor; removeListener(…)
</pre>

* _Event listeners_; classes that implement `EventListenerInterface` are event listeners. This interface defines one method; `notify(…)`, which event producers use to notify the listeners for a specific event. For producer and listener to interact, the listener has to add itself to the producer with `addListener(…)`.

<pre>
<b>Event listener</b>
&lfloor; notify(…)
</pre>

As an example, we show some of the interaction that a lane (producer) may have with a data sampler (listener). The sampler is explained in detail in section 7. When the sampler starts to record data on a specific lane, it adds itself as a listener on the lane, for both add and remove events of GTUs on that lane.

```java
    roadLane.addListener(this, Lane.GTU_ADD_EVENT, ReferenceType.WEAK);
    roadLane.addListener(this, Lane.GTU_REMOVE_EVENT, ReferenceType.WEAK);
```

Now, whenever a GTU enters or leaves the lane, the sampler will be notified by the lane. In `Lane` we have the following two methods. They fire events through a method of the super class `EventProducer.fireTimedEvent(…)`, and provide the payload that comes with the event as an object array.

```java
    public final int addGTU(final LaneBasedGTU gtu, 
            final double fractionalPosition) throws GTUException
    {
        …
        fireTimedEvent(Lane.GTU_ADD_EVENT, new Object[] {gtu.getId(), 
                this.gtuList.size(), getId(), getParentLink().getId()},
                gtu.getSimulator().getSimulatorTime());
        …
    }

    public final void removeGTU(final LaneBasedGTU gtu, 
            final boolean removeFromParentLink, final Length position)
    {
        …
        fireTimedEvent(Lane.GTU_REMOVE_EVENT, 
                new Object[] {gtu.getId(), gtu, this.gtuList.size(), position}, 
                gtu.getSimulator().getSimulatorTime());
        …
    }
```

The sampler has registered itself as a listener and will be notified. To process the events, the sampler has the following code. We leave out the details, but clearly this allows the sampler to respond to the GTU being on the lane. And in fact for as long as the GTU is on the lane, the sampler registers itself as a listener for the GTU’s `LaneBasedGTU.LANEBASED_MOVE_EVENT` event, using `addListener(…)` and `removeListener(…)` on the GTU. This means that every movement step of each GTU on the lane on which data sampling was started, is recorded. (The sampler can also operate using its own sampling frequency. In that case, subscribing to the GTU move events is not done. Instead, the sampler maintains a list of GTUs that are being sampled and schedules a sampling event in the simulator event queue.)

```java
    public final void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(LaneBasedGTU.LANEBASED_MOVE_EVENT))
        {
            …
        }
        else if (event.getType().equals(Lane.GTU_ADD_EVENT))
        {
            …
            gtu.addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT, ReferenceType.WEAK);
            …
        }
        else if (event.getType().equals(Lane.GTU_REMOVE_EVENT))
        {
            …
            gtu.removeListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
            …
        }
    }
```
