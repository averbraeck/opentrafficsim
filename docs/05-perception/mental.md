# Mental models

Perception is a mandatory layer between the modeling environment and tactical planner models. But depending on the category implementations used, it may or may not contain human factors such as estimation errors, anticipation, reaction time, etc. If such human factors are implemented, there are two ways to control it.

1.  _Exogenous_; parameters that describe human factors are pre-determined, such as for example when using a fixed reaction time.
2.  _Endogenous_; parameters that describe human factors are determined with models, possibly varying over time.

For the latter approach, a module `Mental` is used. It is the first step of perception applied in `AbstractLanePerception` (which may also function without `Mental`), but clearly other perception implementations can do the same. Module `Mental` is only an interface which needs implementations. These implementations have to determine values for:

* Parameters for human factors in perception, such as estimation errors and reaction time.
* Parameters for human factors in tactical models, such as desired headway and desired speed.

Note that as `Mental` only determines parameter values, it does not force these parameters to be used. Only if perception categories and tactical models actually use these parameters, will `Mental` have an effect in simulation. Perception, including the mental module, and models thus require close coordination.

## Fuller

One implementation of `Mental` is `Fuller`, which follows the theory of [Fuller](/references#reference-fuller). This theory states that drivers have a balance between task-demand, and task-capability. If these are not in balance (driving is too demanding or too boring), drivers show behavioral adaptations. For instance, when driving is too demanding, the desired headway may be increased.

<pre>
GTU
&lfloor; Tactical planner
  &lfloor; Perception
    &lfloor; <i>Mental</i>
      &lfloor; <b><i>Fuller</i></b>
</pre>
 
The `Fuller` module is flexible in usage, as it uses:

* A set of `Task`’s, where each task describes a fundamental relation between information that can be obtained directly from the simulation environment, and a level of task-demand from the task.
* A set of `BehavioralAdaptation`s, where each describes some response to task saturation (total task-demand divided by the driver’s task-capability) by means of changing parameter values. This can be both in terms of perception parameters (e.g. estimation errors, reaction time) and tactical planner parameters (e.g. desired headway, desired speed).

The concept of _situational awareness_ is implemented by letting a `BehavioralAdaptation` set the appropriate parameter values. There is a default implementation for this (`Fuller.DEFAULT_SA`), which sets a value for the situational awareness parameter (`Fuller.SA`), and the reaction time (`ParameterTypes.TR`). Perception categories implementing situational awareness are advised to use these parameters. The level of situational awareness can be normalized by using parameters `Fuller.SA_MIN` and `Fuller.SA_MAX`.
