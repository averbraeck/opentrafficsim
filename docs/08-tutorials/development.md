# Development

## Eclipse and development tips

Knowing a few tricks in Eclipse can make life as a programmer a lot easier. The following tips should be helpful when developing OTS.

#### 1. Abbreviated package names.
The package explorer can become difficult to navigate as OTS has many projects and packages, many of which have long names. Package names, or parts thereof, can be abbreviated. Go to Window -> Preferences -> Java -> Appearance -> Abbreviate package names. The following abbreviations are suggested:
<table>
    <tr><td>a)</td><td>nl.tudelft.simulation = [n.t.s]</td></tr>
    <tr><td>b)</td><td>org.opentrafficsim.base = [base]</td></tr>
    <tr><td>c)</td><td>org.opentrafficsim.core = [core]</td></tr>
    <tr><td>d)</td><td>org.opentrafficsim.demo = [demo]</td></tr>
    <tr><td>e)</td><td>org.opentrafficsim.road = [road]</td></tr>
</table>

#### 2. Open declaration.
When you see a method or property being used, you can jump to where it’s defined by right clicking and selecting ‘Open Declaration’ or pressing F3. During development this allows comprehension of what a method or property does or means in a quick manner.

#### 3. Open type hierarchy.
Identifying inheritance relations of java classes is best done using a type hierarchy. In a java file, right click within a full class definition and select ‘Open Type Hierarchy’ or press F4 to show the type hierarchy of the class.

#### 4.  Open call hierarchy.
Likewise for type hierarchy on a class, a call hierarchy of a method or property can be shown. This is useful to get an understanding of what classes and methods interact with the subject method or property. Open a call hierarchy by right clicking a method or property and selecting ‘Open Call Hierarchy’ or by pressing Ctrl+Alt+H.

#### 5. Textual search across projects.
When you go to Search -> Search… (or press Ctrl+H) you open the search dialog. The first tab ‘File Search’ allows you to find text in any file from the package explorer. This is a handy tool to find and navigate to places of code of interest, especially if the code development you are working on has destroyed (for the time being) consistency.

#### 6. Organize imports, format, save.
In most cases Eclipse can manage imports for you by navigating to Source -> Organize Imports…. When you use a new class in your code, this is an easy way to include the correct imports. In case Eclipse finds multiple files with the same name, a selection dialog will appear. Similarly, the code can be formatted by navigating to Source -> Format. Any overlong line, misplaced bracket, or misaligned comment, will automatically be formatted. Both these actions have keyboard shortcuts as well, and it’s good practice to press Ctrl+Shift+O, Ctrl+Shift+F and Ctrl+S to organize imports, format and save the file, whenever you are (temporarily) done with a file. 

#### 7. Automatic code generation.
For some mostly standard methods there are automated ways to insert code. In a java file, right click and navigate to Source. When creating a sub-class, ‘Override/Implement Methods…’ is an easy way to create empty methods where you only need to add the content. ‘Generate Constructors from Superclass…’ creates constructors that mimic those of the superclass, where you may then add some additional input to. If you define some properties (fields) in the class ‘Generate Getters and Setters…’ creates a `getX()` end `setX()` method for each selected property `X`. The set method is not applicable on final properties. ‘Generate Constructor using Fields…’ uses the properties to create a constructor using input that is equal to the properties, which are set as the values of the properties. Finally, automatically generating `hashCode()` with `equals()` or a `toString()` method can be done.

#### 8. Refactor rename.
If you need to rename a class, property or method, you should do this via refactoring. Right click what needs a new name and navigate to Refactor -> Rename…, or press Alt+Shift+R. In this way Eclipse makes sure that anywhere where the class, property or method is used, the new name is used.

#### 9. Break.on.
When debugging in OTS one often has to deal with a particular GTU creating an exception after some time. The static utility `Break` has several methods that help you define when to trigger a breakpoint. The breakpoint should be set in the method `Break.trigger()`. A method is available to break on a GTU or on perception after a given time. Depending on available context, a combination of `onSuper()` and `onSub()` can be used, for example if at the `onSub()` no GTU or perception is available.


## How to find your way through the code

The functionality of OTS is scattered throughout several projects, each containing various packages. Classes within a package are usually closely related. But in OTS many package built forth based on similar packages in more fundamental projects. For example the package `org.opentrafficsim.road.gtu.lane` in `ots-road` is based on `org.opentrafficsim.core.gtu` in the `ots-core` project. Furthermore the modular setup of OTS spreads functionality across classes. We strive for simplicity, but sometimes there is a place where an interface is used, the interface itself, an abstract implementation of the interface, and several implementations. Consequently it is not straightforward to browse through the code of OTS to become familiar with the inner-workings. Below are some tips to better navigate the structural relations in order to quickly arrive at the code block of interest, i.e. code that actually performs an operation that needs to be understood.

#### 1. Open declaration, open type hierarchy and open call hierarchy.
These operations as discussed in tutorial [Eclipse and development tips](#eclipse-and-development-tips) and are quickly found in the context menu of the Eclipse editor, or by using keyboard shortcuts. Open declaration brings you to the class or variable that you highlight in the editor. If you see a class being used, but do not have a good comprehension of what the class is, a quick look at the class may provide the understanding required to understand an algorithm using it. The type hierarchy gives you a tree of class inheritance. This is a good tool to understand what a class is based on. And for example whether there is an abstract layer performing parts of the functionality. Finally the call hierarchy helps in understanding how classes interact. It can for example tell you whether a certain method is or is not used by GTUs, or by lane-based objects, etc.

#### 2. Parent declaration.
By clicking on the up arrow next to a line that defines an overriding method, you jump to the parent implementation or the method declaration in an interface. It provides a quick glance in to the functionality that is overridden, or that is invoked using the super keyword.

#### 3. Show in package explorer.
For attributes, methods and classes you can right-click, select ‘Show In’ and then ‘Package Explorer’. This is a great way to find out in what package and project a class is, and hence which classes from the same package are closely related to it.

These tools allow navigation between classes over a few dimensions. This is usually quicker than just browsing in the Package Explorer in the hope to find the correct block of code.


## How to profile code performance

There are several ways in which you can get information on the execution time and memory usage of your code. The first is that your IDE (Integrated Development Environment) might have tools to run code with profiling. Netbeans has built-in profiling tools. For Eclipse a plug-in may be an option.

<a href="https://www.graalvm.org/">GraalVM</a> may be used to profile code across the entire library on method level. It does require installation.

If you only require CPU time of up to a few methods, or any part of a method, OTS provides a utility called `Profile` (from DJUTILS). The tool essentially takes care of gathering and presenting execution time information obtained through `System.nanoTime()`. Note that this is the only way to profile parts of methods. The simplest way to time a part of the code is to precede and follow that part with a line invoking the tool. Both these lines have to be in the same method.

```java
    Profile.start();
    // profiled code
    Profile.end();
```

The tool can maintain statistics of many code blocks simultaneously. To do this, each block needs an internal unique identifier. If the user provides no identifier, one is created using the class end method name as “full classname:method name:line number”. For instance `org.opentrafficsim.road.gtu.lane.perception.RollingLaneStructure:update:206`. The line number corresponds to the call of `start()` and allows different parts in one method to be individually profiled. Alternatively the user can provide a name of both calls.

```java
    Profile.start("Lane structure update");
    // profiled code
    Profile.end("Lane structure update");
```

The above code makes sure the profiling information is gathered. To see the information it can be printed to the console using `print()`. The print method needs to be called repeatedly, for example at the end of each method with profile statements. The information will only actually be printed if the previous print was more than the print interval ago. The default print interval is 1000&nbsp;ms.

```java
    Profile.setPrintInterval(2000);
    Profile.print();
```

The tool will print the following information for each profiled code block:

* Percentage CPU time of all profiled blocks
* Number of calls
* Total time
* Minimum time
* Maximum time
* Average time
* Standard deviation time
* Name (identification)

After profiling the calls to `Profile` should be removed.


## How to create a JUnit test

JUnit tests are blocks of code that perform a test on the functioning of a ‘unit’ of a program. Typically this is testing the outcome of a method given prescribed input. The code for JUnit tests is found in equally named packages as the functional code that is tested, but within the Maven project structure it is found under a folder named ‘test’ rather than ‘main’ where the functional code is. Within this package classes can be defined that have methods just like other classes, in particular test methods, and other methods that are used in those test methods. Below is an example of a test method. It tests whether default parameter values are correctly set. The `@Test` annotation tells JUnit that this method should be invoked for testing. This test method uses two tools from JUnit: `assertTrue(…)` and `fail(…)`. With these tools JUnit can report outcome of tests by running the test code as a JUnit test. (To run a JUnit test in Eclipse right click a test class, test package or project, select Run As, and click JUnit Test.) 

```java
    @Test
    public final void defaultsTest()
    {
        Parameters params = new ParameterSet().setDefaultParameters(ParameterTypes.class);
        try
        {
            assertTrue("Default value is not correctly set.", params.getParameter(ParameterTypes.A)
                .equals(ParameterTypes.A.getDefaultValue()));
        }
        catch (ParameterException exception)
        {
            fail("Default value is not set at all.");
        }
    }
```

JUnit has many more checks available. These are made accessible in the test code by importing them. Furthermore, if the test method itself results in an exception, this is also reported in the JUnit test.

```java
    import static org.junit.Assert.fail;
    import org.junit.Test;
```

In some cases it can be a hassle to setup interdependent objects just to test a single method. For example, if you want to test functionality of a utility that creates GTU generators from an OD matrix, it is required to have a network, a simulator, etc. Instead of setting this up one can also mock the functionality. For this [Mockito](https://site.mockito.org/) is used. The code below mocks an object of type `OtsSimulatorInterface` in a test class `JUnitTest`. The mocked implementation is defined to return two values for two methods. The method `getSimulatorTime()` will return the result from the created `Answer<Time>` which dynamically accesses the value `JUnitTest.this.time`, and the method `getReplication()` will return the current value of `this.replication` (which is separately set).

```java
    OtsSimulatorInterface simulatorMock = Mockito.mock(OtsSimulatorInterface.class);
    Mockito.when(simulatorMock.getSimulatorTime()).then((invocation) -> this.time);
    Mockito.when(simulatorMock.getReplication()).thenReturn(this.replication);
```


## How to make a property historical

For delayed perception of information from the simulation environment, the simulation environment needs to be able to provide past information. Information is stored in properties of java classes. By converting a property in to a historical version, this can be achieved. In many cases this is little work. Below three separated cases are discussed. Which one is valid depends on the type of the attribute.

_Is the property immutable (e.g. `Length`, `double`, `String`) or a (indirect) pointer to an object with its own historical properties?_<br/>
In these cases the property can simply be changed to type `Historical<E>`, with `E` being the original type, and using implementation `HistoricalValue<E>`. Any code referring to the property directly now needs to use `get()` and `set()` on the property. For external code nothing changes, as this should use a setter and getter method that are themselves changed. To make a historical value available to external code, a getter method with `Time` input should be added, which calls `get(Time)` on the property.

_Is the property of a type from the Collections Framework (e.g. `Set`, `List`, `Map`)?_<br/>
For collection framework types one only needs to add `Historical` in front of the type. For example a `Set<E>` becomes `HistoricalSet<E>`. All methods of the original class are also implemented by the historical class. To obtain a historical state of the collection, a getter method with `Time` input should be added, which calls `get(Time)` on the property.

_Is the property of any other type?_<br/>
For these cases one first has to consider whether individual properties of the property object may need to be historical, rather than the object as a whole. If the object itself can better be made historical a new implementation of `AbstractHistorical` is required. In case the property type class already has a super class, the property type class will need to implement `HistoryManager.HistoricalElement` in order to interact with the `HistoryManager`, and manage its own history and interaction with the `HistoryManager`. The reader is referred to section [Historical complex objects](../05-perception/historical.md#historical-complex-objects) for a baseline understanding of events as used in `AbstractHistorical`. The functionality that should be created is that the state of the property at any given time in the past can be provided (limited in duration by a `HistoryManager`). To this end an implementation can keep the current object, make a copy of it, and reverse all applicable events on the copy. Thus, such events should have sufficient information such that they can (or can be used to) reverse the mutation. To obtain applicable events `AbstractHistorical` has some utility methods for sub-classes, such as `getEvents(Time)` which returns an ordered list of events between the provided `Time` and the current time in reversed order.

As an example let’s assume we have a class for a 2-dimensional double (`double[][]`) matrix with history called `HistoricalMatrix`. It extends `AbstractHistorical` and is coupled with an event class called `EventMatrix`. Below the class is shown with a property of the current state of the matrix `double[][]`, a constructor coupling it with a `HistoryManager`, and the method `getMatrix()` which returns a safe copy of the current state.

```java
    public class HistoricalMatrix extends 
        AbstractHistorical<double[][], EventMatrix>
    {
        
        private final double[][] matrix;
        
        protected HistoricalMatrix(final HistoryManager historyManager, final Object owner, final double[][] matrix)
        {
            super(historyManager, owner);
            this.matrix = matrix;
        }

        public double[][] getMatrix()
        {
            double[][] out = new double[this.matrix.length][];
            for (int i = 0; i < this.matrix.length; i++)
            {
                out[i] = new double[this.matrix[i].length];
                System.arraycopy(this.matrix[i], 0, out[i], 0, this.matrix[i].length);
            }
            return out;
        }

    }
```

Note that access to the property `matrix` should be restricted to inside the class, as any mutation should encompass creation of an event. The class will additionally get a `setValue(…)` and `getMatrix(Time)` method, but before these are explained the class `EventMatrix` is discussed. In order to restore a value, the previous value, the row and the column need to be stored. Thus, the class for events of the matrix stores this information. The class extends `EventValue` which stores time for the functionality in `AbstractHistorical` and the (previous) value. Finally, it has a method called `restore(double[][])` which restores the mutation in the given matrix.

```java
    public class EventMatrix extends EventValue<Double>
    {

        private final int i;
        
        private final int j;
        
        public EventMatrix(final double time, final int i, final int j, 
            final double value)
        {
            super(time, value);
            this.i = i;
            this.j = j;
        }
        
        public void restore(final double[][] matrix)
        {
            matrix[this.i][this.j] = getValue();
        }
        
    }
```

The class `HistoricalMatrix` gets the following `setValue(…)` method, which adds a new event using `addEvent(…)` of `AbstractHistorical`, and then performs the mutation. Time stored with the event is obtained using the method `now()` from `AbstractHistorical`. The value in the event is the previous value, as this is required to restore the event.

```java
    public void setValue(final int i, final int j, final double value)
    {
        addEvent(new EventMatrix(now().si, i, j, this.matrix[i][j]));
        this.matrix[i][j] = value;
    }
```

Finally the `getMatrix(Time)` method can be added. It obtains a safe copy of the current state of the matrix, then loops over all events from now until the given time in the past as returned by `getEvents(Time)` of `AbstractHistorical`. For each event the `restore()` operation is invoked, resulting in a `double[][]` in the state at the given time.

```java
    public double[][] getMatrix(final Time time)
    {
        double[][] out = getMatrix();
        for (EventMatrix event : getEvents(time))
        {
            event.restore(out);
        }
        return out;
    }
```

This concludes the example. It should be pointed out that the example shows only one way to create a historical. In case of different ways to mutate an object, e.g. also performing several calculations on the matrix, one can decide to declare `EventMatrix` as a super class or interface of several event implementations for different mutations, where some store a single value and coordinates, and others know the reverse mathematical operation or store the entire matrix from before the mutation. Different methods that perform different mutations then create instances of different specific event classes.
