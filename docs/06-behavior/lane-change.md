# Lane change Model with Relaxation and Sychronization

The default tactical planner is the Lane change Model with Relaxation and Synchronization (LMRS) ([Schakel et al., 2012](../10-references/references.md)). This section discusses what modules it uses and how it can be extended.


## Lane change incentives

The LMRS is based on lane change incentives that together determine a level of lane change desire. There are two types of incentives: mandatory and voluntary. The LMRS can use multiple of each, but needs at least one mandatory incentive. The LMRS treats mandatory and voluntary incentives differently. For each lateral direction it will use the value furthest from zero from all mandatory incentives. Voluntary incentives are added together and weighted with a factor that decreases as the mandatory desire is more dominant (further away from zero). The total is then the total lane change desire.

Each incentive returns lane change desire, where incentives are not fully independent. Mandatory incentives receive the mandatory desire that has been determined so far, and voluntary incentives receive the resulting mandatory desire and the voluntary desire so far. Based on these values incentives may decide what the desire from the incentive is. This means that the order in which the incentives are provided to the LMRS is important. Two such dependences are:

* `IncentiveKeep`; is 0 if there is any negative voluntary desire so far (or negative mandatory desire).
* `IncentiveSocioSpeed`; is 0 if mandatory desire is towards the other lateral direction.

Whenever possible, using either the mandatory or voluntary desire so far should be avoided. Using the resulting mandatory desire in voluntary incentives is not dependent on order.

Additional lane change incentives can easily be added to the LMRS, and will result in the appropriate relaxation and synchronization behavior based on the resulting lane change desire.


## Acceleration incentives

Although not a formal part of the LMRS, the implementation allows the inclusion of a number of acceleration incentives. These are incentives that, besides car-following, cooperation and synchronization within the LMRS, may influence acceleration. There are incentives available for traffic lights, conflicts, speed limit transitions, bus stops and to not overtake on the right. Additional incentives can easily be added to the LMRS, and influence the acceleration through the `SimpleOperationalPlan` that acceleration incentives receive.


## Behaviors

The LMRS is further specified with four behaviors: `Synchronization`, `Cooperation`, `GapAcceptance` and `Tailgating`. Each of these four behaviors can be set with a specific implementation, and new implementation can be designed. Both synchronization and cooperation have a default value which is `PASSIVE`, referring to responding to the nearest GTUs without further future anticipation. For gap acceptance the default is INFORMED, meaning the driver is fully aware of all the preferences of surrounding GTUs. Finally, tailgating has default NONE. 
