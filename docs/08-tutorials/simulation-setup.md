# Simulation setup

## How to set up a simulation

Setting up a simulation may involve quite some work depending on the case. To minimize this effort one can extend `AbstractSimulationScript`. This is a utility that enables simulation with or without animation and that can be controlled using command line arguments. The proposed structure is as below. The main method accepts command line arguments that are given to `CliUtil` together with the created `SimpleSimulation`. Next, the main method simply invokes `start()` to start the simulation.

```java
    public class SimpleSimulation extends AbstractSimulationScript
    {
    
        @Option(names = "--output", description = "Generate output.", negatable = true, defaultValue = "false")
        private boolean output;
    
        protected SimpleSimulation()
        {
            super("Simple simulation", "Example simple simulation");
        }
    
        public static void main(final String[] args) throws Exception
        {
            SimpleSimulation simpleSimulation = new SimpleSimulation();
            CliUtil.execute(simpleSimulation, args);
            simpleSimulation.start();
        }
    
    }
```

Command line arguments are used to set values of properties of `AbstractSimulationScript` and of the sub class, in this case `SimpleSimulation`. This is done by `CliUtil` which uses [picocli](https://picocli.info/). For example the command line arguments `--output true` is equivalent to setting `SimpleSimulation.output = true`. The property `SimpleSimulation.output` may be used anywhere inside the class `SimpleSimulation`. Some properties are available using get-methods from `AbstractSimulationScript`. These are:

* `--seed`; `getSeed()`; default 1
* `--startTime`; `getStartTime()`; default 0s
* `--warmupTime`; `getWarmupTime()`; default 0s
* `--simulationTime`; `getSimulationTime()`; default 3600s
* `autorun`; `isAutorun()`; default `false`

The autorun parameter triggers animation when false. These, and other, parameters can be determined with the command line arguments. The example `SimpleSimulation` has to implement one more method to actually define the simulation content. This is given below for a simple network of two nodes and one link with two lanes. An alternative to this is to use the XML parser to read an XML file and create a network including animation from it.

```java
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim)
            throws NullPointerException, DrawRuntimeException, NetworkException, OtsGeometryException
    {
        RoadNetwork network = new RoadNetwork("Simple network", sim);
        Point2d pointA = new Point2d(0, 0);
        Point2d pointB = new Point2d(500, 0);
        Node nodeA = new Node(network, "A", pointA, Direction.ZERO);
        Node nodeB = new Node(network, "B", pointB, Direction.ZERO);
        GtuType car = DefaultsNl.CAR;
        GtuType.registerTemplateSupplier(car, Defaults.NL);
        LinkType freewayLink = DefaultsNl.FREEWAY;
        LaneType freewayLane = DefaultsRoadNl.FREEWAY;
        CrossSectionLink link = new CrossSectionLink(network, "AB", nodeA, nodeB, freewayLink, new OtsLine3d(pointA, pointB),
                LaneKeepingPolicy.KEEPRIGHT);
        LaneGeometryUtil.createStraightLane(link, "Left", Length.instantiateSI(1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(car, new Speed(120, SpeedUnit.KM_PER_HOUR)));
        LaneGeometryUtil.createStraightLane(link, "Right", Length.instantiateSI(-1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(car, new Speed(120, SpeedUnit.KM_PER_HOUR)));
        LaneGeometryUtil.createStraightStripe(Type.SOLID, link, Length.instantiateSI(3.5), Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(Type.DASHED, link, Length.instantiateSI(0.0), Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(Type.SOLID, link, Length.instantiateSI(-3.5), Length.instantiateSI(0.2));
        return network;
    }
```

With this in place the simulation can run, but without traffic. The next tutorial deals with creating an OD matrix. To apply the OD on the network one can use `OdApplier` within the `setupSimulation(…)` method. Section [How to set up model factories when using an OD matrix](#how-to-set-up-model-factories-when-using-an-od-matrix) gives a tutorial on how to setup up model factories when using `OdApplier`. This method has the task to set everything up that is required for the simulation. This includes for example a `RoadSampler`, `Detector`s, or some dedicated data collection.

Some elements of simulation can be specified by overriding methods of `AbstractSimulationScript`. These methods are:

* `animateNetwork(…)`; defines what animations are used for the various types of objects.
* `addTabs(…)`; adds taps for additional visualization, e.g. time-space plots, next to the only default tap named ‘animation’.
* `onSimulationEnd()`; a trigger that is called on the simulation end, can for example be used to save gathered data.
* `setupDemo(…)`; intended to optionally add elements to the demo panel on the right side of the animation screen (which is hidden by default).
* `setAnimationToggles(…)`; defines the buttons on the left side of the animation screen.

The default implementations of these methods are empty, except for `animateNetwork(…)` which uses `DefaultAnimationFactory`, and `setAnimationToggles(…)` which sets a default set of animation toggles. In order to animate objects that appear during simulation, `DefaultAnimationFactory` registers itself as a listener to the appropriate events using the [event producers and listeners system](../02-model-structure/djutils.md#event-producers-and-listeners).


## How to create an OD matrix and add demand data

This section explains how an OD matrix can be created using code. Section [Origin-destination matrix](../04-demand/od-matrix.md) explains OD matrices in OTS. We first create a list of origins (nodes A and B) and destinations (nodes B and C).

```java
    List<Node> origins = new ArrayList<>();
    origins.add(nodeA);
    origins.add(nodeB);
    List<Node> destinations = new ArrayList<>();
    destinations.add(nodeB);
    destinations.add(nodeC);
```

In this case we specify demand per GTU type, and create a categorization for this.

```java
    Categorization categorization = new Categorization("MyCategorization", GtuType.class);
```

And we use stepwise demand with half-hour periods.

```java
    DoubleVectorData data =
            DoubleVectorData.instantiate(new double[] {0.0, 0.5, 1.0}, TimeUnit.BASE_HOUR.getScale(), StorageType.DENSE);
    TimeVector timeVector = new TimeVector(data, TimeUnit.BASE_HOUR);
    Interpolation interpolation = Interpolation.STEPWISE;
```

Now we can create the OD matrix, still without demand data.

```java
    OdMatrix odMatrix = new OdMatrix("MyOD", origins, destinations, categorization, timeVector, interpolation);
```

To add demand we need categories. In our case these are not OD specific as we do not specify demand per lane or route. So we only need two categories for all demand.

```java
    Category carCategory = new Category(categorization, DefaultsNl.CAR);
    Category truckCategory = new Category(categorization, DefaultsNl.TRUCK);
```

Demand for cars from A to B follows the matrix time periods and interpolation. We have half an hour with 1000&nbsp;veh/h, and another with 2000&nbsp;veh/h. The third demand value is not used with stepwise demand.

```java
    data = DoubleVectorData.instantiate(new double[] {1000.0, 2000.0, 0.0}, FrequencyUnit.PER_HOUR.getScale(),
            StorageType.DENSE);
    FrequencyVector demandABCar = new FrequencyVector(data, FrequencyUnit.PER_HOUR);
    odMatrix.putDemandVector(nodeA, nodeB, carCategory, demandABCar);
```

Suppose we have truck data in 1-hour periods, but with piecewise linear demand. We add this by specifying a separate time vector and interpolation.

```java
    data = DoubleVectorData.instantiate(new double[] {0.0, 1.0}, TimeUnit.BASE_HOUR.getScale(), StorageType.DENSE);
    TimeVector truckTime = new TimeVector(data, TimeUnit.BASE_HOUR);
    data = DoubleVectorData.instantiate(new double[] {100.0, 150.0}, FrequencyUnit.PER_HOUR.getScale(), StorageType.DENSE);
    FrequencyVector demandABTruck = new FrequencyVector(data, FrequencyUnit.PER_HOUR);
    odMatrix.putDemandVector(nodeA, nodeB, truckCategory, demandABTruck, truckTime, Interpolation.LINEAR);
```

Now we also add demand from node B to node C, where we only know that on average 10% of traffic is trucks. We specify a single demand pattern, and apply it with a factor on each category. In this case we use the same time vector and interpolation as the matrix.

```java
    data = DoubleVectorData.instantiate(new double[] {1200.0, 1500.0, 0.0}, FrequencyUnit.PER_HOUR.getScale(),
            StorageType.DENSE);
    FrequencyVector demandBC = new FrequencyVector(data, FrequencyUnit.PER_HOUR);
    odMatrix.putDemandVector(nodeB, nodeC, carCategory, demandBC, timeVector, interpolation, 0.9);
    odMatrix.putDemandVector(nodeB, nodeC, truckCategory, demandBC, timeVector, interpolation, 0.1);
```

Demand can also be specified in number of trips per time period, rather than a frequency. Again, this can either be done using the matrix time, or a specified time. Note that the interpolation is always stepwise for trips, and can therefore not be specified.

```java
    odMatrix.putTripsVector(nodeA, nodeC, carCategory, new int[]{300, 400});
    odMatrix.putTripsVector(nodeA, nodeC, truckCategory, new int[]{100}, truckTime);
```

The command `odMatrix.print()` will output the following. As we can see, trips have internally been transformed into frequencies.

<pre>
A -> B | [GtuType: CAR] | [ 1000.00000 2000.00000 0.00000000] /h
A -> B | [GtuType: TRUCK] | [ 100.000000 150.000000] /h
A -> C | [GtuType: CAR] | [ 600.000000 800.000000 0.00000000] /h
A -> C | [GtuType: TRUCK] | [ 100.000000 0.00000000] /h
B -> B | -no data-
B -> C | [GtuType: CAR] | [ 1080.00000 1350.00000 0.00000000] /h
B -> C | [GtuType: TRUCK] | [ 120.000000 150.000000 0.00000000] /h
</pre>


## How to set up model factories when using an OD matrix

When using an OD matrix, vehicle generation can bet set up using `OdApplier`. This utility uses an instance of `OdOptions` to define specifics of vehicle generation. One option is `OdOptions.GTU_TYPE` which has a value of type `LaneBasedGtuCharacteristicsGeneratorOd`. This tutorial covers some ways to define this. Section [GTU characteristics generator](../04-demand/gtu-characteristics.md) covers the structure for factories in a non-OD matrix context. The structure for characteristics within an OD matrix context is similar and discussed in section [GTU characteristics](../04-demand/traffic-od.md#gtu-characteristics). The difference is that the OD matrix defines the origin and the destination, and possibly the route and/or GTU type.

In this tutorial the factories are defined in anonymous classes, as factories are often used in specifics contexts. For every model component this could however also be a separate class. Anonymous classes are an in-place manner to define extensions of classes, including extensions of interfaces and abstract classes. Below is example code where an instance of an anonymous extension of `LaneBasedGtuCharacteristicsGeneratorOd` is created. 

```java
    LaneBasedGtuCharacteristicsGeneratorOd characteristicsGenerator = new LaneBasedGtuCharacteristicsGeneratorOd()
    {
        @Override
        public LaneBasedGtuCharacteristics draw(final Node origin, final Node destination, final Category category,
                final StreamInterface randomStream) throws GtuException
        {
            // implementation code
        }
    };
```

Let us assume that for this simulation the GTU type and route are defined in the OD matrix, we use default GTU dimensions and maximum speed, and use no vehicle model. This can be achieved by adding the following code in the `draw(…)` method.

```java
    GtuType gtuType = category.get(GtuType.class);
    Route route = category.get(Route.class);
    GtuCharacteristics gtuCharacteristics = GtuType.defaultCharacteristics(gtuType, origin.getNetwork(), randomStream);
    VehicleModel vehicleModel = VehicleModel.NONE;
```

What remains to be defined is a `LaneBasedStrategicalPlannerFactory`. Defining components bottom up the following code uses some standard components. Note that the strategical planner factory can be defined once, for example as a property of the anonymous class, rather that for every call to the `draw()` method.

```java
    CarFollowingModelFactory<?> carFollowing = new IdmPlusFactory(randomStream);
    PerceptionFactory perception = new DefaultLmrsPerceptionFactory();
    LaneBasedTacticalPlannerFactory<?> tactical = new LmrsFactory(carFollowing, perception);
    LaneBasedStrategicalPlannerFactory<?> strategical = new LaneBasedStrategicalRoutePlannerFactory(tactical);
```

Finally all defined aspects of the characteristics can be returned.

```java
    return new LaneBasedGtuCharacteristics(gtuCharacteristics, strategical, route, origin, destination, vehicleModel);
```

Setup code like this is usually more elaborate as more specific, non-default characteristics are used. Extensions to this approach could be:

* The GTU type and route may not be defined in the OD, but instead be drawn from a distribution or be derived by generators, or specifically for the route by a `RouteGenerator`. (The route can be determined in different places. If the OD matrix defines a route, it is used. A `LaneBasedStrategicalRoutePlannerFactory` can also be assigned a `RouteGenerator` in its constructor. It will be forwarded to all strategical planner instances it creates. It is used whenever the route turns out to be `null`, including ‘en-route’. For clarity it is preferred to prevent redundancy, i.e. it is advised to determine routes at one place, and rely on an exception to be thrown when routes are not properly defined.)
* The GTU dimensions and maximum speed may be drawn from non-default distributions.
* The vehicle model may be determined by a factory, varying e.g. vehicle mass.
* The car-following factory could use lower-level factories for desired headway and desired speed models.
* A perception factory may be required which includes specific perception categories in the perception.
* The LMRS factory can be specified with many more choices.

Another way to go about this process is to use `DefaultLaneBasedGtuCharacteristicsGeneratorOd`, which is discussed in section [GTU characteristics](../04-demand/traffic-od.md#gtu-characteristics). It can be created by setting several optional elements with a factory. For example the lines below create a `DefaultLaneBasedGtuCharacteristicsGeneratorOd` with only a default LMRS model set. Besides the `create()` method, the `Factory` has many methods to set the elements. These methods allow method chaining, i.e. `Factory().setX(…).setY(…).setZ(…).create()`.

```java
    LaneBasedStrategicalRoutePlannerFactory lmrs = DefaultLaneBasedGtuCharacteristicsGeneratorOd.defaultLmrs(randomStream);
    DefaultLaneBasedGtuCharacteristicsGeneratorOd generator = new DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory(lmrs).create();
```

For clarity the rest of the example code does not use method chaining. It should be noted that the following code uses a variable `randomStream`. The context, however, is any method that sets up a simulation, and not `LaneBasedGtuCharacteristicsGeneratorOd.draw(…)` as presented before. Nonetheless it is likely the same stream of random numbers used for vehicle generation, being passed on from one context to another. It should also be noted that the example code sets many standard elements, which are equal to the default elements that are used when a particular element is not specified. This is for the sake of illustration. First, let us define the GTU types.

```java
    GtuType car = DefaultsNl.CAR;
    GtuType truck = DefaultsNl.TRUCK;
```

Next, factories for model components are set up similar to how this was done before. A `ParameterFactory` is included to set a different acceleration value for trucks. The highest level factory (strategical) is used on the GTU characteristics generator factory. Note that this is a simple case. Depending on the GTU type, and possibly the origin or destination, the strategical planner factory may return various models, possibly based on a set of delegate strategical planner factories.

```java
    CarFollowingModelFactory<?> carFollowing = new IdmPlusFactory(randomStream);
    PerceptionFactory perception = new DefaultLmrsPerceptionFactory();
    LmrsFactory tactical = new LmrsFactory(carFollowing, perception);
    ParameterFactoryByType params = new ParameterFactoryByType();
    params.addParameter(truck, ParameterTypes.A, Acceleration.instantiateSI(0.8));
    LaneBasedStrategicalPlannerFactory<?> strategical = new LaneBasedStrategicalRoutePlannerFactory(tactical, params);
    Factory factoryOD = new DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory(strategical);
```

Next, we create and set a distribution such that 90% of the GTUs are cars, and the rest trucks.

```java
    Distribution<GtuType> gtuTypeGenerator = new Distribution<>(randomStream);
    gtuTypeGenerator.add(new FrequencyAndObject<>(0.9, car));
    gtuTypeGenerator.add(new FrequencyAndObject<>(0.1, truck));
    factoryOD.setGtuTypeGenerator(gtuTypeGenerator);
```

Suppose we want non-default dimensions for cars, but for trucks the defaults are ok. For this we supply a template for cars.

```java
    Set<GtuTemplate> templates = new LinkedHashSet<>();
    templates.add(new GtuTemplate(car,
            new ConstantGenerator<>(Length.instantiateSI(4.5)),
            new ConstantGenerator<>(Length.instantiateSI(1.9)), 
            new ConstantGenerator<>(Speed.instantiateSI(50))));
    factoryOD.setTemplates(templates);
    GtuType.registerTemplateSupplier(truck, Defaults.NL);
```

As vehicle model we require, in this case, the mass to be known and distributed. Hence, two distributions are created and used in an anonymous vehicle model factory. Note that the implementation returns a value from either distribution based on the input GTU type.

```java
    DistContinuousMass carMass = new DistContinuousMass(new DistUniform(randomStream, 500, 1500));
    DistContinuousMass truckMass = new DistContinuousMass(new DistUniform(randomStream, 800, 10000));
    factoryOD.setVehicleModelGenerator(new VehicleModelFactory()
    {
        @Override
        public VehicleModel create(final GtuType gtuType)
        {
            Mass mass = gtuType.isOfType(car) ? carMass.draw() : truckMass.draw();
            double momentOfInertiaAboutZ = 0.0;
            return new VehicleModel.MassBased(mass, momentOfInertiaAboutZ);
        }
    });
```

Finally, lets create the GTU characteristics generator.

```java
    DefaultLaneBasedGtuCharacteristicsGeneratorOd generator = factoryOD.create();
```

This concludes the example code. It is recommended to use `DefaultLaneBasedGtuCharacteristicsGeneratorOd` when possible. It is very flexible and requires the user to only specify what is non-default. Before creating the set up code, the user should be aware of the model structure of the components to use. OTS only enforces the use of a strategical factory at the highest level. What lower levels are used is entirely up to specific implementations, ranging from tactical planners, including perception, to desired speed and headway models.


## How to set up bus traffic

OTS is mostly concerned with regular vehicular traffic in its current form. There are however some components for bus traffic. These can be integrated in a simulation.

* GTU type SCHEDULED_BUS can be used to define bus lanes and other traffic rules, bus demand, etc.
* To let a bus follow a schedule there is class `BusSchedule`. It is an extension of `Route` with additional bus stop information. For each bus stop it has:
    * Bus stop id.
    * Scheduled departure time.
    * Dwell time at the stop.
    * Whether the schedule is enforced (i.e. do not leave early).
    * A few methods to get and set the departure time.
* Perception category `BusStopPerception` lets the GTU spot bus stops.
* For inclusion in the LMRS there is acceleration incentive `AccelerationBusStop`. It evaluates the bus stops ahead and considers the schedule and dwell times. This occurs in close cooperation with `BusSchedule`.
* For inclusion in the LMRS there is lane change incentive `IncentiveBusStop`. It calculates lane change desire considering a bus stop ahead.
* `BusStop` is a lane-based object that represents the bus stop. It contains the lines that stop at the bus stop, and automatically connects itself to nearby conflicts where priority may be based on the bus leaving the bus stop.
* Conflicts can be set up with a `BusStopConflictRule` that checks whether a bus is leaving or not. 
* Enum `Priority` in `CrossSectionLink` has a field named `BUS_STOP`. This can be set as the priority of a link to tell the `ConflictBuilder` to assign a bus conflict rule.
