# Tactical planner

A tactical planner governs the short-term movement of the GTU by determining an operational plan. To understand when the tactical planner is requested to provide an operational plan, such events are now explained. Upon the creation of a GTU a call to `Gtu.move(…)` takes place. This method then repeatedly schedules itself at any (ir)regular interval. The method requests a tactical planner from the strategical planner, and an operational plan from the tactical planner using the below method. 

```java
    OperationalPlan generateOperationalPlan(Time startTime, 
        DirectedPoint locationAtStartTime)
```
This operational plan has a certain duration which determines the interval between move events. The plan can be interrupted by the GTU itself at any time using the method `interruptMove()`, which will cancel the future move event and invoke the move method to create a new operational plan starting at the current time, and schedule a future move event at the end of that plan.

<pre>
<b>Tactical planner</b>
&lfloor; Generate operational plan
&lfloor; Perception
&lfloor; <i>Car-following model</i>
</pre>

The next sub-sections will discuss what an operational plan contains and how it can be created after the intentions of the GTU are determined by a model. Next, the relation between the tactical planner and perception is discussed. The use of modular utilities are explained, which are model parts that can be easily plugged-in in any tactical planner. The final section discusses car-following models, which are a mandatory part of a lane-based tactical planner (not a tactical planner in general).


## Operational plan

The operational plan defines the movement of a GTU over the applicable time span. In the event-based simulation it is able to report location, speed and acceleration at any point in time. Movement of the reference point of the GTU occurs over a 3D-path. There are no restrictions to this path, though tactical planners may self-impose restrictions such as staying on ground level and following lane center lines.

How far along the GTU is over the path since the start of the path depends on three things: time since the start, the start speed, and the segments. Each segment defines for some partial duration of the plan what constant acceleration is applicable. The `SpeedSegment` defines constant speed and implies no acceleration, whereas an `AccelerationSegment` defines a particular acceleration value. Segments are consecutive, so starting with the start speed, the entire progression through the operational plan can be build up one segment at a time. In classical vehicular models the operational plan has a single acceleration segment and its duration is equal to the model time step.

Finally, the lane-based variant of an operational plan is defined as deviative or not. A deviative plan does not strictly follow the lane center lines, which makes some calculations concerning position more complex. For non-deviate plans simpler approaches are sufficient.

<pre>
<b>Operational plan</b>
&lfloor; Path
&lfloor; Start time
&lfloor; Start speed
&lfloor; Segments
&lfloor; <i>Deviative</i>
</pre>

To create tactical plans the utilities `(Lane)OperationalPlanBuilder` can be used. One of its tools creates a `LaneBasedOperationalPlan` from a `SimpleOperationalPlan`. The simple plan has a duration, acceleration and optionally an initiated lane change direction. These values are typical outcomes of microscopic models.

<pre>
<b>Simple operational plan</b>
&lfloor; Duration
&lfloor; Acceleration
&lfloor; <i>Lane change direction</i>
</pre>
 

## Perception

Perception, which is discussed in detail in [the previous chapter](../05-perception/introduction.md), is positioned under the tactical planner. This is because the tactical planner is the most likely component where information from perception is required. Within the logic of the tactical planner the method `getPerception()` can be used to retrieve information from it, or to forward the perception to any sub-routine.


## Modular utilities

Tactical planners can be thought of as a collection of behaviors. Individual behaviors can be equal (other than parameter values) between different models. Java inheritance does not lend itself well to combine components as a class can have one super-class only. This means that behaviors from two existing tactical planners can never be combined. A different approach that is better suitable for tactical planners is modularity. This is implemented in OTS by using static utilities, found under `org.opentrafficsim.road.gtu.lane.tactical.util` and further sub-packages. Static utilities are static methods that supply some results by applying some logic on some input. A tactical planner can invoke such a method to incorporate the logic. For example, `TrafficLightUtil` has the following method which lets a GTU respond to a traffic light by providing some input concerning the current circumstances, preferences and a car-following model that describes longitudinal responses.

```java
    public static Acceleration respondToTrafficLight(final Parameters parameters, 
        final HeadwayTrafficLight headwayTrafficLight, 
        final CarFollowingModel carFollowingModel, final Speed speed, 
        final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        …
    }
```

In some cases the utility may need to store information such that it can remember this in the next time step of the model. As the utility is static, the class containing the utility cannot store the information that is accessible to the static utility. In such cases the utility can accept some object as input in which it stores such information, while the GTU invoking the utility stores the information object itself. For instance, `ConflictUtil` has the following method. 

```java
    public static Acceleration approachConflicts(…, 
        final ConflictPlans conflictPlans, …)
            throws GTUException, ParameterException
    {
        …
    }
```

The class `ConflictPlans` contains the information that the utility needs to remember. All the invoking class has to do is store it.

```java
    private final ConflictPlans conflictdPlans = new ConflictPlans();
```

Note that the invoking class does not need any information from the information object, it only needs to store it and provide it as input to the static utility. Therefore, methods in the information object are usually not public. Exposure of underlying complexity is thus prevented, preventing the invoking class from becoming needlessly complex. 


## Car-following model

For lane-based GTUs a car-following model is mandatory. Its main purpose is to translate parameters, speed information and information regarding the leaders in to an acceleration. This method may be used by the tactical planner for many reasons, such as following the leader, gap-acceptance and stopping for a traffic light. The car-following model also needs to supply a desired headway and a desired speed. This is not only useful for the inner-workings, but the vehicle generator also needs to peek desired headway and speed as discussed in the [section on peeking](../04-demand/gtu-characteristics.md#peeking-for-gtu-generation). Desired headway and desired speed may be determined by sub-modules. To this end there is an interface `DesiredHeadwayModel` and an interface `DesiredSpeedModel`. The interface `CarFollowingModel` extends both interfaces, but whether the implementation actually uses sub-models to provide this information, is up to the implementation. 

<pre>
Lane-based tactical planner
&lfloor; <b>Car-following model</b>
  &lfloor; Following acceleration
  &lfloor; Desired headway
    &lfloor; <i>Desired headway model</i>
  &lfloor; Desired speed
    &lfloor; <i>Desired speed model</i>
</pre>
 

The class `AbstractCarFollowingModel` provides a skeleton implementation with sub-models for the desired headway and speed. Furthermore, it gathers the desired headway and speed and provides this to a lower-level method to determine acceleration that sub-classes should implement.

The default car-following model is the Intelligent Driver Model+ (IDM+) ([Schakel et al., 2010](../10-references/references.md)). It is based on the IDM ([Treiber et al., 2000](../10-references/references.md)), and the common aspects are defined in `AbstractIDM`. Car-following models may be defined not to have sub-models for the desired headway and speed, by implementing `CarFollowingModel` directly. 

<pre>
Car-following model
&lfloor; Abstract car-following Model
  &lfloor; <b>Abstract IDM</b>
    &lfloor; IDM
    &lfloor; IDM+
</pre>

Complex version of desired headway and desired speed models may depend on more than the basic input provided through their methods. These models can implement interface `Initialisable`, which supplies the GTU to the models once the GTU is initialized. For peeking by the vehicle generator, which occurs prior to GTU initialization, such models are not yet connected to the GTU. This means that models implementing `Initialisable` should function without this connection by returning a value assuming no context.


## Intersection behavior

Models are available to simulate traffic in limited urban settings. This involves both controlled and uncontrolled intersections. Traffic lights in the yellow or red phase are regarded as a stand-still vehicle using the car-following model, but using a larger allowable deceleration. However, if required deceleration is beyond the threshold, the traffic light is ignored and is passed, which should occur during the yellow phase for a well-designed traffic light. At uncontrolled intersections, the area where lanes cross are known as conflicts. Drivers respond to conflicts by considering traffic rules and traffic on the conflicting lane. The model for such situations considers several events, such as when the vehicle itself or another vehicle is expected to enter or leave the conflict. Depending on the timing and order of such events, the driver will pass the conflict, slow down for the conflict, or will stop for the conflict. Many details need to be considered for such situations. More on this is explained in [Schakel and van Arem (2012)](../10-references/references.md).
