#  GTU - Generalized Travel Unit

A Generalized Travel Unit, or GTU, is a basis of travelling objects in OTS. It is purposefully generic, as it can represent many different things such as: pedestrians, cyclists, cars, trucks, trains, boats, etc., either controlled by a human or a computer. It has some basic properties such as location, speed, acceleration, length, shape, etc., as well as a set of important relative positions such as the front and the back, all relative to the reference point. Such `RelativePosition`s are key in interactions with other GTUs and objects such as sensors. GTUs move according to an operational plan, which describes the movement. The operational plan is determined by a tactical planner, which has the task of executing the overall strategy of a strategical planner.

GTUs are unbounded in their movement, which allows space-continuous modelling. On roads traffic is often simulated along lanes. OTS has `LaneBasedGTU`s for this purpose. It defines some lane-specific features, but movement is still free. This is useful for instance for lane changes. OTS does provide utilities to define movement along lanes, where a 2D path is automatically derived from lane curvature.

GTUs may also be part of another GTU in a parent-child(ren) relation. Drivers and passengers of vehicles for example may be explicitly modelled when walking to or from the vehicle. Another example where GTUs are part of another GTU is train carriages and truck trailers, which may be uncoupled. If such complexity is used for GTUs, animations and models regarding these GTUs need to account for the GTU being part of another GTU or not.

GTUs are of a given GTUType, such as car, pedestrian, truck, etc. Rules, such as road accessibility, are defined for GTU types. GTU types are hierarchical, as some rules may apply to very specific types, while other rules apply to a broader group. The following GTU types are pre-defined in OTS. Users can define new GTU types as subtype of any of the existing GTU types.

<pre>
<b>GTU type</b>
&lfloor; Road user
  &lfloor; Pedestrian
  &lfloor; Bicycle
    &lfloor; Moped
  &lfloor; Vehicle
    &lfloor; Car
    &lfloor; Van
    &lfloor; Bus
      &lfloor; Scheduled bus
    &lfloor; Truck
    &lfloor; Emergency vehicle
&lfloor; Water way user
  &lfloor; Ship
&lfloor; Rail way user
  &lfloor; Train
</pre>

The next sections describe how the hierarchical approach of Michon [2] is applied in OTS regarding the strategical, tactical and operational level. The model components at these levels are related as is depicted in Figure 2.1.

![](../images/OTS_Figure_2.1.png)
Figure 2.1: Relation between high-level model components.


## Strategical planner

On the strategic level, the overall goals of the GTU are determined or defined. This involves destination choice, departure time choice, mode choice and route choice. Some of these may simply be defined from demand, while others are actively determined by a strategical model in a simulation. In either case the route of a GTU is the constraint on which the tactical planner operates. The route may be changed during simulation, at which point the tactical planner will adhere to the new route. In some cases, GTUs can move without a route, for instance on a corridor network without splits. Then, the tactical planner will simply never encounter a situation where it needs the strategical planner.


## Tactical planner

The tactical planner determines the intended movement of a GTU. Given the route of the strategical planner, the tactical planner determines an operational plan valid for some limited duration. A new operational plan may be determined before the validity of the previous plan expires, or otherwise it is determined when the previous plan ends. On the tactical level one can find car-following models and lane change models for vehicular traffic, or any model that determines similar aspects of movement for other modes. Note that OTS does not in any way prescribe how such models are categorized and separated; the tactical planner simply needs to determine an operational plan. There are however tactical planner classes set up that one may use to base tactical movement for instance on a car-following model. A separate lane change model is not defined, as this is too integrated with the car-following model and too diverse in required information.


## Operational plan

The operational plan defines movement along a trajectory. The trajectory is not bound; GTUs are free to move in continuous space. The trajectory may however be derived from lane curvature. An operational plan is defined in segments, where each segment defines speed over the trajectory for instance as a constant acceleration over the segment time. Classical microscopic models can be seen as having one constant acceleration segment per time step / operational plan. As the next section will explain, OTS is event based. Hence, times when GTUs determine their operational plan may not be synchronized. When GTUs determine their operational plan they require information of surrounding GTUs. This means that GTUs should be able to report their position, speed, acceleration, etc., at any given time during their current operational plan. The operational plan provides this functionality by interpolating on the active segment of the plan.

The operational plan may overrule the intent of the tactical planner by bounds from the vehicle, such as maximum deceleration, or drifting on slippery surfaces.
