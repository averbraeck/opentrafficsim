# Parameters

Parameters quantify movement of GTUs and can be obtained from the GTU using `getParameters()`, which returns an object of class `Parameters`. This will mostly be used by the models of the GTU itself. The parameters are pairs of parameter types and the respective value. Each parameter type can be seen as ‘a parameter’ within the context of a model and is defined as (extensions of) a `ParameterType<T>` where `T` is the type of the value. For instance, a `ParameterType<Length>` is coupled to a `Length` value inside the `Parameters`. Setting and getting a value of a specific parameter is done by providing the right parameter type.

```java
    <T> void setParameter(ParameterType<T> parameterType, T value);
    <T> T getParameter(ParameterType<T> parameterType);
```

In actual usage the java generics disappear.

```java
    parameters.setParameter(ParameterTypes.A, Acceleration.createSI(1.4));
    Acceleration a = parameters.getParameter(ParameterTypes.A);
```

Here we see `ParameterTypes.A` being used, which is the maximum car-following acceleration. It is possible to set a parameter value tentatively, for example as one applies a temporary different context on the own GTU (e.g. applying the car-following model on a traffic light, where larger decelerations are accepted), or when assessing behavior of surrounding GTUs (e.g. setting the desired headway of a surrounding GTU to a short value for evaluation of a gap when urgently changing lane). After such cases, the parameter value may be set back to the previous value, whatever it was. This can be achieved with the following two method calls. (The reason that `setParameter()` does not remember the previous value is efficiency.) If the reset is not called, the tentative value becomes permanent (until it is set again).

```java
    parameters.setParameterResettable(ParameterTypes.B, 
        Acceleration.createSI(3.5));
    parameters.resetParameter(ParameterTypes.B);
```

There are two methods to obtain a parameter value, `getParameter(…)` and `getParameterOrNull(…)`. The first throws an exception when a parameter is not defined, while the latter returns `null` in that case and never throws an exception. The latter method is only advised in performance-heavy code. For instance it is used in the constraint checks discussed in the following sections, which are done every time a parameter value is set (which is often).

A number of frequently used parameters is defined in static fields of the `ParameterTypes` class. For all other cases the recommendation is to define the parameter as a static field in the class where it is most used. Even when using a default parameter the recommendation is to provide a forwarding reference in the class where it is used, e.g. `A` in the example below. In defining parameters, for many value types (`T`) there are helper classes that take care of the java generics. For instance `ParameterTypeDuration` implements `ParameterType<Duration>`. Below an example of a regular (unit-less) double value.

```java
    public abstract class AbstractIDM extends AbstractCarFollowingModel
    {
        protected static final ParameterTypeAcceleration A = ParameterTypes.A;

        public static final ParameterTypeDouble DELTA = 
            new ParameterTypeDouble("delta", 
            "Acceleration flattening exponent towards desired speed.", 4.0,     
            ConstraintInterface.POSITIVE);
    }
```

The example shows that parameter `DELTA` is defined with an id, description, default value, and a constraint. The next section explains parameter constraints.


## Parameter constraints

Using constraints on parameter values is a simple way to assure that models behave decently. For example, one parameter value may be nonsensical if it’s value is lower than another parameter, while other parameters are nonsensical with negative values. The class `ParameterType` provides two ways of applying constraints to parameter values. 

The first is to provide a `Constraint<T>` in any of the constructors. This is an interface that simply has an `accept(T)` method, and can be implemented to form any constraint that is self-contained (i.e. not dependent on other parameter values). The following constraints are readily available.

* `NumericConstraint`; an `enum` with several frequently applicable constraints for any parameter type that extends `Number`, including DJUNIT types. 
* `SingleBound`; has static methods to create bounds for any parameter type that extends `Number`, including `lowerInculsive(…)`, `lowerExclusive(…)`, `upperInclusive(…)` and `upperExclusive(…)`.
* `DualBound`; has static methods to create dual-bounds for any parameter type that extends `Number`, including `closed(…)`, `open(…)`, `leftOpenRightClosed(…)` and `leftClosedRightOpen(…)`.
* `CollectionConstraint`; constraint that checks whether the value is any from a pre-defined collection.
* `SubCollectionConstraint`; constraints that checks for parameter types with (extensions of) `Collection` value types, whether the value (a collection) is a subset of a pre-defined collection.
* `MultiConstraint`; a constraint in to which other independent constraints can be combined.
* `ConstraintInterface`; a convenience interface with several frequently applicable constraints. It has `POSITIVE`, `NEGATIVE`, `POSITIVEZERO`, `NEGATIVEZERO`, `NONZERO`, `ATLEASTONE` and `UNITINTERVAL`.

If a parameter value needs to be checked relative to another parameter value, both parameter types should overwrite the `check(…)` method of `ParameterType`. For instance, the below example checks that <i>d<sub>free</sub></i>&nbsp;&lt;&nbsp;<i>d<sub>sync</sub></i>. Note that one parameter can be set at a time, so both parameter checks should be able to deal with the other parameter not yet being present. In any case, the latter of the two should find the error. The `check(…)` method can contain any logic to check whether a parameter value meets constraints.

```java
    ParameterTypeDouble DFREE = new ParameterTypeDouble(…)
    {
        public void check(final Double value, final Parameters params) 
        {
            Double dSync = params.getParameterOrNull(DSYNC);
            Throw.when(dSync != null && value >= dSync, ParameterException.class,
                "Value of dFree is above or equal to dSync.");
        }
    };

    ParameterTypeDouble DSYNC = new ParameterTypeDouble(…)
    {
        public void check(final Double value, final Parameters params)
        {
            Double dFree = params.getParameterOrNull(DFREE);
            Throw.when(dFree != null && value <= dFree, ParameterException.class, 
                "Value of dSync is below or equal to dFree.");
        }
    };
```


## Setting default parameters in factories

A task of the strategical planner factory is to deliver the parameters for a new GTU. By convention, each sub-component is a `ModelComponentFactory` which supplies the parameters the sub-component (and its sub-components) requires. This means that (default) parameters are set and gathered at several layers. The main implementation of `Parameters` is `ParameterSet` which provides some methods to easily set default values of parameters. That is, default values as determined by the parameter types themselves. If a parameter value is considered default in the context of the given model component, but different from the default value the parameter type has itself, then the parameter value should simply be set in the factory of the component. Methods for default parameter type values are `setDefaultParameter(ParameterType)` and `setDefaultParameters(Class)`. The first sets the default value of one parameter type, while the latter sets the default values of all parameters defined as static fields in the provided class. This is why it is advised to define or reference used parameters in a component using static fields in a class. To merge parameter sets from several sub-components one can use `setAllIn(ParameterSet)`. The parameter set on which this method is called, will set all contained parameters in to the provided set. The example below creates a new parameter set, sets all default values of the LMRS utility, and merges the default parameters for car-following in to the parameter set.

```java
    ParameterSet parameters = new ParameterSet();
    parameters.setDefaultParameters(LmrsUtil.class);
    getCarFollowingParameters().setAllIn(parameters);
```
