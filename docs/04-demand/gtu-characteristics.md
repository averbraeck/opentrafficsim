# GTU characteristics generator

There are many ways in which GTU characteristics can be generated, from specific ad-hoc implementations for a specific simulation, to interchangeable factories depending on which behavioral model is selected. One implementation that might be useful is `LaneBasedTemplateGTUTypeDistribution`, which draws from a set of `LaneBasedTemplateGTUType`s, each having some fixed probability. Each `LaneBasedTemplateGTUType` itself draws a strategical planner and route. Another implementation `GTUCharacteristicsGeneratorOD` is used when generating traffic using an OD matrix, as explained in section [Traffic from an Origin-Destination matrix](/traffic-demand-and-vehicle-generation/traffic-from-an-origin-destination-matrix). It follows a similar underlying structure of factories, except that the origin and destination are given, and possibly the route and/or GTU type as well. Hence, rather than drawing characteristics including these, the remaining characteristics are drawn based on these. In any case, these implementations produce the information as detailed below.

<pre>
Lane-based GTU generator
&lfloor; <b>GTU characteristics generator</b>
  &lfloor; GTU characteristics
    &lfloor; GTU type
    &lfloor; Length
    &lfloor; Width
    &lfloor; Maximum speed
    &lfloor; Maximum acceleration
    &lfloor; Maximum deceleration
  &lfloor; Strategical planner factory
    &lfloor; <i>Tactical planner factory</i>
      &lfloor; <i>Car-following model factory</i>
      &lfloor; <i>Perception factory</i>
    &lfloor; <i>Parameter factory</i>
  &lfloor; <i>Route</i>
  &lfloor; <i>Origin</i>
  &lfloor; <i>Destination</i>
  &lfloor; <i>Vehicle model</i>
</pre>
 

## Factories and parameters

In the description here we focus further on the use of factories, which produce the models and parameters. Typically, a strategical model interacts with a tactical model, while the tactical model uses a car-following model. Additionally, the car-following model may use a desired speed model and a desired headway model. Along the same hierarchy, we can find factories for such models. For example, a strategical planner factory uses a tactical planner factory, such that a drawn strategical planner is created with a specific drawn tactical planner. A tactical planner factory is however not necessarily required, as a strategical planner may be designed to only operate with a specific tactical planner.

All factories below the strategical planner factory are typically a `ModelComponentFactory`, which is defined to be able to return a set of used parameters. How parameters are defined is discussed in section 5.4.2. Each model component factory is responsible for returning all parameters its corresponding model uses, as well as all parameters its sub-components use. The strategical planner is ultimately responsible for providing the parameters for the next GTU. Parameters from the sub-components may be amended with some for the strategical planner itself. Additionally, the strategical planner factory may use a parameter factory to override some parameters, possibly by GTU type, and randomly drawn from several random distributions. Sub-components can easily return default values for parameters, while the parameter factory is a convenient place to define all non-default and randomly distributed parameters. Implementations of `ModelComponentFactory` can use some methods to easily set all default values, which is further explained in section TODO FIX LINK X.

A common implementation of a parameter factory used in the strategical planner factory is `ParameterFactoryByType`. In this class parameters values can be stored per GTU type. This includes constant values and randomly distributed parameters. Package `org.opentrafficsim.core.units.distributions` contains many implementations of input for the parameter factory, where a random distribution from DSOL (`DistContinuous`, `DistDiscrete`, or any sub class) is coupled to a unit in DJUNITS.


## Peeking for GTU generation

Another important role of the strategical planner factory is to allow the GTU generator (and its room checker) to _peek_ the desired speed and desired headway of a GTU that is to be generated. GTU generation will also function without this information, in which case the GTU generator itself will make some assumptions. Note that in this way GTU generation and model properties are inconsistent (e.g. a truck generated at 100km/h), and that without this information the GTU generator is less robust to spillback of congestion. 

The strategical planner factory may forward the peeking to the tactical planner, but not before it has determined (peeked) the parameters for the next GTU. The tactical planner requires the parameters to peek a desired speed and desired headway. In turn, the tactical planner may peek the next car-following model, and use it together with the parameters to determine desired speed and desired headway. To this end, the car-following model should be able to return such information without the GTU being placed on the network. For car-following models in which desired speed and desired headway may depend on surrounding traffic, this traffic has to be assumed not to be present when peeking. Car-following models, and possibly desired speed and desired headway models, are `Initialisable`. This means that upon successful placement of the GTU, they may be coupled to the GTU if required. For peeking however, this coupling is not yet available, and the desired speed and desired headway should be provided without it. Note that in case no desired speed can be peeked, a lane bias for the drawn position may not be based on desired speed, as explained in the following section.
