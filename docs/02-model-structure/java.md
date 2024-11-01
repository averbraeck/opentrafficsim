# Java programming standards

OTS is programmed in java. The strictly typed and object oriented nature provides the necessary tools for large scale software developed by a team. Java is multi-platform and offers automatic garbage collection (freeing up memory by discarding data that is no longer part of the active program). These features reduce the required effort to make OTS run on different computers. This section discusses some of the standards of how java is used in OTS. It is by no means a utility to learn java. In fact, a basic understanding of java is a prerequisite. Developers of OTS are advised to read ‘Effective Java’ by Joshua Bloch. It deepens a basic understanding of java by explaining good and bad design practices. Another source is the YouTube channel [CodeAesthetic](https://www.youtube.com/@CodeAesthetic), a channel on good design practices, presented in a somewhat black-and-white fashion to challenge your habits.


## Eclipse

Eclipse is an Integrated Development Environment (IDE), which is a tool that offers many features for software development. It is the advised tool to develop and use OTS. A full guide on installing and setting up Eclipse can be found [here](../01-technical/installation-developers.md). Here, we describe some important components for OTS.

* Maven; Maven is a tool in which several java projects are coupled, and which determines how a project is built in to a stand-alone application. The several separate java projects in OTS, such as ots-core and ots-road, depend on one another as defined with Maven. This also determines which parts need to be included in a stand-alone compiled version of OTS. An overview of the most important project dependencies is given in Figure 2.2. The file central to Maven in each project is called pom.xml.
* JDK; the Java Development Kit is a low-level distribution for java development. It contains a Java Runtime Environment (JRE), which is required to run java. Additionally, it contains compilers to compile source code in to byte code, and other tools like Javadoc, a debugger and jvisualvm. The latter is a tool to evaluate CPU and memory usage of a java application.
* Checkstyle; checkstyle is a plugin for Eclipse which prescribes a consistent way of formatting code between developers of OTS. Think about line length, bracket placement, etc.

<table border=1 id="figure-2.2" style="text-align: center">
    <tr>
        <td colspan=4><b>ots-demo</b> | <b>ots-editor</b><br><i>demo's and user interface, this should be the starting point for getting to know OTS</i></td>
    </tr>
    <tr>
        <td colspan=2 width=50%><b>ots-parser-xml</b><br><i>native xml import</i></td>
        <td colspan=2><b>ots-swing</b><br><i>java based animation</i></td>
    </tr>
    <tr>
        <td colspan=2><b>ots-trafficcontrol</b><br><i>event-based traffic control</i></td>
        <td colspan=2><b>ots-animation</b><br><i>animation functionality independent of implementation</i></td>
    </tr>
    <tr>
        <td colspan=3 width=75%><b>ots-road</b><br><i>microscopic simulation of vehicular traffic</i></td>
        <td colspan=1><b>ots-draw</b><br><i>swing mimicking fuctionality</i></td>
    </tr>
    <tr>
        <td colspan=2><b>ots-core</b><br><i>core of traffic simulation including network representation and macroscopic models</i></td>
        <td colspan=2><b>ots-kpi</b><br><i>stand-alone key-performance-indicator module, including trajectory sampling</i></td>
    </tr>
    <tr>
        <td colspan=4><b>ots-base</b><br><i>contains some generic simulation utilities such as parameter management</i></td>
    </tr>
    <tr>
        <td colspan=4><b>ots</b><br><i> the main project containing compilation standards, github workflows, documentation source code and general settings files</i></td>
    </tr>
</table>
<i>Figure 2.2: OTS project dependencies. Projects depend on the projects directly below them.</i>

Other dependencies are:
<ul>
    <li><b>ots-web</b> <i>web-based visualization</i>
        <ul>
            <li><b>ots-parser-xml</b></li>
            <li><b>ots-animation</b></li>
        </ul>
    </li>
    <li><b>ots-sim0mq-swing</b> <i>databus communication examples</i>
        <ul>
            <li><b>ots-sim0mq</b></li>
            <li><b>ots-swing</b></li>
        </ul>
    </li>
    <li><b>ots-sim0mq-kpi</b> <i>databus communication kpi examples</i>
        <ul>
            <li><b>ots-sim0mq</b></li>
        </ul>
    </li>
    <li><b>ots-sim0mq</b> <i>databus communication</i>
        <ul>
            <li><b>ots-parser-xml</b></li>
        </ul>
    </li>
</ul>

## Commit checklist

The following list defines some checks that code has to meet in order to be eligible for inclusion in OTS.

1. _Checkstyle and code formatter_; Have you considered all checkstyle warnings (and solved or knowingly suppressed them), and have you formatted/indented the code (Ctrl+Shift+F in Eclipse) according to the default OTS formatting? (The default OTS formatting is based on `dsol-checks.xml` in the main project under config.)
2. _Compiler warnings and errors_; Committing code with compiler errors should be avoided at all times. Warnings should be payed attention to.
3. _Override `toString()`_; For most classes, the `toString()` method should be overridden. For purposes of debugging and understanding, objects should be able to meaningfully report what they are. In some cases, a superclass may have a sufficient implementation.
4. _Override `equals()`?_; The equals method should be implemented for all classes that are probable to be used for equality checks. The default java implementation checks for pointer equivalence, which can in many cases give a wrong result, especially in distributed serialization/deserialization or multiple JVM settings. Note that usage as key in a `Map` means `equals()` will be used.
5. _Then also override `hashCode()`_; Whenever an `equals()` method is defined, a `hashCode()` method should also be defined, consistently with attributes considered in `equals()`.
6. _Implement `Comparable<Class>`?_; For classes that are likely to be ordered, the `Comparable<Class>` interface should be implemented.
7. _Uncaught exceptions in @throws_; All uncaught exceptions that a method may throw, should be documented in the Javadoc under the `@throws` tag.
8. _Pre- and post-conditions_; Checking of input arguments should be done when it adds information relative to an exception occurring otherwise, and providing context from the contract of the method. Or when it prevents OTS from running without exception but incorrectly in terms of modeling. Input arguments should be checked where they matter, i.e. in the method where the problem occurs. Calling methods should not unnecessarily double check the arguments. For input checks OTS provides several easy and short `Throw.when(…)` or `Throw.whenNull(…)` methods. Exceptions that are thrown should be java library exceptions when appropriate, such as `IllegalArgumentException`.
9. _Records_; Use `record` for final objects when possible. Also return e.g. a `record MyOutput(double x, double y)` rather than a `double[]` with length 2.
10. _Long methods_; Prevent long methods (checkstyle gives a warning for methods longer than 150 lines). Refactor parts to helper methods with a clear name (in Eclipse, select relevant code block: refactor > extract method). In this way the algorithm a method performs also becomes more readable. If this is not possible without many input arguments, consider grouping input arguments in a small data class, possibly a `record`.
11. _Unnecessary arguments_; Avoid unnecessary input arguments that can be obtained through other input arguments. For example `Simulator sim, Lane lane`, while the simulator can be obtained with `lane.getSimulator()`.
12. _Var_; For code readability and in case of verbose type definitions that are known from context, use `var`. For example `Stream<Entry<ParameterType<?>, Set<?>>> paramValues = map.entrySet().stream();` can be written as `var paramValues = map.entrySet().stream();`.
13. _Inner classes_; The above rules also hold for inner-classes. The Javadoc for internal classes can skip the copyright and authors.

When java library exceptions do not cover the exception well, OTS has a set of exceptions that can be thrown. These exceptions are:

OtsException
<ul>
  <li>XmlParserException</li>
  <li>SamplingException</li>
  <li>ParameterException</li>
  <li>NetworkException</li>
  <li>ProbabilityException</li>
  <li>TrafficControlException</li>
  <li>GtuException</li>
  <ul>
    <li>OperationalPlanException</li>
    <li>MissingComponentException</li>
  </ul>
</ul>

OtsRuntimeException
<ul>
  <li>OtsGeometryException</li>
  <li>CircularDependencyException</li>
  <li>CollisionException</li>
</ul>

## Java generics

Java generics can initially seem hard and unwieldly, especially when classes are defined with multiple type arguments, which itself may also have type arguments. Generics however provides excellent ways in which code can be made as generic and re-usable as possible. Therefore, it is applied often in OTS and can be found in much of the source code. Hence, some understanding of generics is favorable. Note however that in case of good design, java generics are present in highly flexible low-level classes, while higher level classes that users mostly interact with hide much of the generics involved. This section is not meant to introduce the concept of java generics. Information to learn about java generics can easily be found elsewhere. Here a basic understanding of java generics is assumed, to further explain some design patterns with java generics as used in OTS.

Below is an example of _recursive generics_, where `HierarchicalType` has type parameter `T`, which is defined to be a subclass of `HierarchicalType` itself. This pattern is useful when a subclass implementation needs to accept or return objects of its own type, while we want to define the functionality only once. For instance, we have `GtuType` which is a subclass of `HierarchicalType`. Without re-specifying the `getParent()` method, it is guaranteed that for a `GtuType`, it will return a `GtuType`. This holds for any subclass, and the pattern allows us to define the functionality once.

```java
    public abstract class HierarchicalType<T extends HierarchicalType<T>> 
        implements Type<T>
    {
        private T parent;
        public final T getParent()
        {
            return this.parent;
        }
    }

    public final class GtuType extends HierarchicalType<GtuType>
    {
    }
```

The next example shows how one object can house multiple objects of the same class, that have different type parameters, but offer no difficulty to the outside user. We do this by applying _safe casting_ within a limited scope which guarantees the cast is safe. In this case we have `ParameterSet`, which has multiple parameters, each having a certain type for the value. The method `setParameter(…)` assures that for a set parameter value, the value type matches the parameter type `T` (which may differ between different calls of the method). To store the values, there is a map between parameters of any type `<?>`, to simply an object of any type. The `getParameter(…)` method performs a cast of the value obtained from the internal map. Given this context, where only the method `setParameter(…)` has write access to the `Map`, we are certain the value object matches the parameter type, and the cast is safe. Hence we can suppress the warning.

```java
    public class ParameterSet
    {
        private Map<ParameterType<?>, Object> parameters;
        public final <T> void setParameter(final ParameterType<T> parameterType, 
            final T value)
        {
            this.parameters.put(parameterType, value);
        }
        @SuppressWarnings("unchecked")
        public <T> T getParameter(final ParameterType<T> parameterType)
        {
            return (T) this.parameters.get(parameterType);
        }
    }
```

Next, we give an example of _super_ generics, which are often considered vague, but make sense in an actual use of the super keyword with java generics. In this example we discuss a `ParameterType<T>` as in the previous example. Any parameter type may be defined with some constraint, for example that the value has to be above 0. We may have many parameter types which would have such a constraint, but have different type arguments. For example, parameter types using `Length`, `Duration`, `Acceleration`, etc. Since all these types are a subclass of `Number`, returning the SI value as number, it would be convenient to have one single `Constraint<Number>` and use this on all mentioned parameter types. This is possible by letting the parameter type implementations accept a constraint typed with the parameter value type or a super class of it. For example, `ParamaterTypeLength` which is typed with `Length`, can receive any constraint that can operate on a `Length` or any of its super classes. A constraint on `Number`, or `Object`, would thus be acceptable, as `Length` is both a `Number` and an `Object`. 

```java
    public class ParameterTypeLength extends ParameterTypeNumeric<Length>
    {    
        private Constraint<? super Length>
    }
```

Next, we show an example using quite some type arguments, to show that although at first glance this may seem unclear, it makes perfect sense. The example discusses a `PerceptionCollectable`. It is designed to allow iteration over perceived representations of type `H` regarding underlying objects in simulation of type `U`. Furthermore, it can perceive a collected result of type `C`, resulting from considering all objects of type `U` together. Finally, as the collected (accumulated) result is determined, there is an intermediate result of type `I`. As a concrete example, density may be determined by considering leaders of type `U = Gtu`, perceived as `H = HeadwayGtu`, resulting in a density `C = LinearDensity`, with an intermediate type `I` which is some class that stores a cumulative GTU count, and the distance over which these GTUs are found. The identity returns an initial value for this (count = 0), while the accumulator increases the result for every next GTU, and the finalizer translates the last intermediate result in a density. We again see the `super` keyword, as for instance we could have a `PerceptionCollectable` of lane-based GTUs with `U = LaneBasedGtu`. From the set of lane-based GTUs we could use an accumulator of GTUs (so a superclass of `LaneBasedGtu`) as for instance only speed is used, which GTUs also have. Without the super keyword, a `PerceptionAccumulator<Gtu, ?>` could not be used.

```java
    public interface PerceptionCollectable<H extends Headway, U> 
            extends PerceptionIterable<H>
    {
        <C, I> C collect(Supplier<I> identity, 
                PerceptionAccumulator<? super U, I> accumulator, 
                Function<I, C> finalizer);
    }
```

At the beginning of this section it was mentioned that good design should hide much of the underlying java generics being used in lower level classes. Though this is true, it doesn’t excuse the developer of low-level functionality from using comprehensive java generics. As an example of how comprehensive java generics can be hidden, consider the class `Length`. Below the definition of its 2nd comprehensive super class is shown. Users of `Length` have to define _no type argument_, but lower level classes make sure that correct calculations are made (e.g. adding `Length` and not `Speed`) and only units pertaining to the length quantity are used.

```java
public abstract class DoubleScalarRelWithAbs<AU extends AbsoluteLinearUnit<AU, RU>,
        A extends DoubleScalarAbs<AU, A, RU, R>, RU extends Unit<RU>,
        R extends DoubleScalarRelWithAbs<AU, A, RU, R>> extends DoubleScalarRel<RU, R>
        implements RelWithAbs<AU, A, RU, R>
```

These are just a few examples of how java generics is used in OTS. If done well, it’s only a ‘relative headache’ at one location, while providing high flexibility, reusability and cleaner and more intuitive code elsewhere.


## JUnit tests

JUnit is a framework that is used in Eclipse to perform ‘unit tests’ for OTS. A unit test is code, outside of the functional code of a project, that tests functionality of the functional code at a unit level. A typical example is to invoke a method with prescribed input, and checking whether the method output is as expected. This is then repeated for a range of input values. As such the unit test verifies that the method functions as it should. Ideally every part of functional code is subject to a unit test such that proper functioning of OTS is guaranteed. Even still it does not exclude bugs that arise from complex interactions between methods or regarding unforeseen cases. Nonetheless, unit tests help to keep a complex project robust and bug free. A [tutorial in section 8](../08-tutorials/development.md#how-to-create-a-junit-test) discusses how JUnit tests are created.

