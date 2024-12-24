# Historical information

In Java, objects have properties which have values that are usually obtained and changed with get/set methods, or used internally in the class. For a simulation the values of properties typically represent the current state. In case of perception it may be required to know a past state, for instance to account for a reaction time. In OTS a small toolkit is available to make properties _historical_ within the `org.opentrafficsim.core.perception` package. The basic concept is that the property is defined as a typed `Historical`. For example, below is the odometer property of a GTU.

```java
    private Historical<Length> odometer;
```

Any information that perception may require in the past, needs to be defined like this. The interface `Historical` defines three simple methods, where `T` is the generic type of the value which is `Length` in the above example:

```java
    void set(T value);
    T get();
    T get(Time time);
```

The first two set and get the value at the current time, while the latter obtains an historical value, i.e. from the past. The class `AbstractHistorical` performs most of the hard work regarding bookkeeping of previous values. Internally it maintains a list of timed events from which past values can be reconstructed. Each event represents a mutation to the value. An important part of the functionality is coupling with a `HistoryManager`, which performs cleaning up of old events. The default implementation within OTS is `HistoryManagerDevs`, which uses events, a clean-up interval, and a guaranteed history duration. (Only if the object itself exists for a shorter time in simulation, is the history duration not guaranteed. In these cases the oldest state is returned.) This history duration should be set to a value that is larger than the maximum perception delay that can occur in simulation, while being as small as possible to limit memory usage.

Obviously, the example of a GTUs odometer value is of a type that has a single immutable value (rather than being a set or a complex object) at any time, which is the easiest case. We can use the implementation `HistoricalValue` for this.

```java
    this.odometer = new HistoricalValue<>(historyManager, this, Length.ZERO);
```

The `Historical` is automatically coupled to the `HistoryManager`. The class `HistoricalValue` internally uses the events from `AbstractHistorical` (its superclass) to return the value at any particular time.

```java
    EventValue<T> event = getEvent(time);
    return event == null ? null : event.getValue();
```

Besides the method `getEvent(Time)`, several other methods are available for subclasses of `AbstractHistorical` to reconstruct the value at the given time. For properties that are of a type from the Java Collections Framework (like `Set`s and `Map`s), there is a shadow package available for historical versions of all of these utility classes in `org.opentrafficsim.core.perception.collections`. At both interface and implementation level historical shadows are available. For example we have `HistoricalCollection<E>` and `HistoricalSet<E>` which respectively extend the interfaces `Collection<E>` and `Set<E>`. Because the shadow package extends the Java Collections Framework, all the regular methods still work and function at the current time, such as `add(…)`, `remove(…)`, `clear()`, etc.. 
Converting a property of type `Set<E>` to `HistoricalSet<E>`, or any other collection or map, thus requires little change in code. Additionally the interfaces define a method `get(Time)` to obtain a paste state of the collection or map.


## Historical complex objects

For usages other than an immutable value, collection or map, it may be required to have a new implementation of `AbstractHistorical`. Creating such a class requires some comprehension of the events that are used to reconstruct past states of values. To this end available event classes in OTS are explained.

The class `AbstractHistorical` has a generic type argument for the events used such that utility methods for sub-classes to return events, return the correct type of events. Such events have to extend the interface `AbstractHistorical.Event`. A simple implementation of this is `AbstractHistorical.EventValue`, which stores a time and a value. This information is sufficient for `HistoricalValue`, which uses this event class. 

For maps this class is further extended by `AbstractHistoricalMap.EventMap`, which additionally stores whether the map contained a key prior to a mutation, and optionally the previously associated value (with the key stored as the value in superclass `AbstractHistorical.EventValue`). This information is sufficient to reverse any mutation, which is done by the `restore(…)` method by putting the previous value or by removing the key and associated value (whichever is the reverse of the original mutation). Reversing events is done on a copy of the current map, such that the current map is not affected. Filling an empty map with the contents of the current map and then reversing all applicable events is done in the `fill(…)` method. This method is used in implementations of the `getTime(…)` method of subclasses (e.g. `HistoricalHashMap`), and expects an empty `Map` of the correct `Map` implementation (which is unknown to `AbstractHistoricalMap` due to type erasure in java generics).

For collections the class `AbstractHistorical.EventValue` is extended by `AbstractHistoricalCollection.EventCollection`, which defines an abstract `restore(…)` method. The class is further extended by `AbstractHistoricalCollection.AddEvent` and `AbstractHistoricalCollection.RemoveEvent`. The implementation of `restore(…)` performs a `remove(…)` for the former, and an `add(…)` for the latter, so the reverse of the original mutation. Note that events are only created if there is an effective mutation. Adding an entry to a set that already contains the entry will not result in an event. Finally `AbstractHistoricalCollection.EventCollection` is further extended by `AbstractHistoricalList.EventList` which adds an index and is further extended by `AbstractHistoricalList.AddEvent` and `AbstractHistoricalList.RemoveEvent`. All this operates the same as explained above, with the addition that entries are removed or added at specific indices. All these event classes are sufficient for the shadow collections framework.

<pre>
AbstractHistorical
&lfloor; Event
  &lfloor; EventValue
    &lfloor; EventMap
    &lfloor; EventCollection
      &lfloor; AddEvent
      &lfloor; RemoveEvent
      &lfloor; EventList
        &lfloor; AddEvent
        &lfloor; RemoveEvent
</pre>
