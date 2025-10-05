package org.opentrafficsim.demo.doc.tutorials;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Mass;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.data.DoubleVectorData;
import org.mockito.Mockito;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantSupplier;
import org.opentrafficsim.core.distributions.FrequencyAndObject;
import org.opentrafficsim.core.distributions.ObjectDistribution;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.VehicleModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdMatrix;

import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.unit.DistContinuousMass;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings({"javadoc", "designForExtension", "unused"})
public class OdExample
{

    public void howToCreateAnOdMatrixAndAddDemandData()
    {
        Node nodeA = Mockito.mock(Node.class);
        Node nodeB = Mockito.mock(Node.class);
        Node nodeC = Mockito.mock(Node.class);
        Mockito.when(nodeA.getId()).thenReturn("A");
        Mockito.when(nodeB.getId()).thenReturn("B");
        Mockito.when(nodeC.getId()).thenReturn("C");

        // @docs/08-tutorials/simulation-setup.md#how-to-create-an-od-matrix-and-add-demand-data
        List<Node> origins = new ArrayList<>();
        origins.add(nodeA);
        origins.add(nodeB);
        List<Node> destinations = new ArrayList<>();
        destinations.add(nodeB);
        destinations.add(nodeC);

        Categorization categorization = new Categorization("MyCategorization", GtuType.class);

        DoubleVectorData data =
                DoubleVectorData.instantiate(new double[] {0.0, 0.5, 1.0}, TimeUnit.BASE_HOUR.getScale(), StorageType.DENSE);
        DurationVector timeVector = new DurationVector(data, DurationUnit.HOUR);
        Interpolation interpolation = Interpolation.STEPWISE;

        OdMatrix odMatrix = new OdMatrix("MyOD", origins, destinations, categorization, timeVector, interpolation);

        Category carCategory = new Category(categorization, DefaultsNl.CAR);
        Category truckCategory = new Category(categorization, DefaultsNl.TRUCK);

        data = DoubleVectorData.instantiate(new double[] {1000.0, 2000.0, 0.0}, FrequencyUnit.PER_HOUR.getScale(),
                StorageType.DENSE);
        FrequencyVector demandABCar = new FrequencyVector(data, FrequencyUnit.PER_HOUR);
        odMatrix.putDemandVector(nodeA, nodeB, carCategory, demandABCar);

        data = DoubleVectorData.instantiate(new double[] {0.0, 1.0}, TimeUnit.BASE_HOUR.getScale(), StorageType.DENSE);
        DurationVector truckTime = new DurationVector(data, DurationUnit.HOUR);
        data = DoubleVectorData.instantiate(new double[] {100.0, 150.0}, FrequencyUnit.PER_HOUR.getScale(), StorageType.DENSE);
        FrequencyVector demandABTruck = new FrequencyVector(data, FrequencyUnit.PER_HOUR);
        odMatrix.putDemandVector(nodeA, nodeB, truckCategory, demandABTruck, truckTime, Interpolation.LINEAR);

        data = DoubleVectorData.instantiate(new double[] {1200.0, 1500.0, 0.0}, FrequencyUnit.PER_HOUR.getScale(),
                StorageType.DENSE);
        FrequencyVector demandBC = new FrequencyVector(data, FrequencyUnit.PER_HOUR);
        odMatrix.putDemandVector(nodeB, nodeC, carCategory, demandBC, timeVector, interpolation, 0.9);
        odMatrix.putDemandVector(nodeB, nodeC, truckCategory, demandBC, timeVector, interpolation, 0.1);

        odMatrix.putTripsVector(nodeA, nodeC, carCategory, new int[] {300, 400});
        odMatrix.putTripsVector(nodeA, nodeC, truckCategory, new int[] {100}, truckTime);

        odMatrix.print();
    }

    public void howToSetUpModelFactoriesWhenUsingAnOdMatrix()
    {
        // @docs/08-tutorials/simulation-setup.md#How-to-set-up-model-factories-when-using-an-od-matrix
        LaneBasedGtuCharacteristicsGeneratorOd characteristicsGenerator = new LaneBasedGtuCharacteristicsGeneratorOd()
        {
            @Override
            public LaneBasedGtuCharacteristics draw(final Node origin, final Node destination, final Category category,
                    final StreamInterface randomStream) throws GtuException
            {
                // implementation code

                // @docs/08-tutorials/simulation-setup.md#How-to-set-up-model-factories-when-using-an-od-matrix
                GtuType gtuType = category.get(GtuType.class);
                Route route = category.get(Route.class);
                GtuCharacteristics gtuCharacteristics = Defaults.NL.apply(gtuType, randomStream).get();
                VehicleModel vehicleModel = VehicleModel.NONE;

                LaneBasedTacticalPlannerFactory<?> tactical =
                        new LmrsFactory.Factory().withDefaultIncentives().build(randomStream);
                LaneBasedStrategicalPlannerFactory<?> strategical = new LaneBasedStrategicalRoutePlannerFactory(tactical);

                return new LaneBasedGtuCharacteristics(gtuCharacteristics, strategical, route, origin, destination,
                        vehicleModel);
            }
        };
    }

    static
    {
        StreamInterface randomStream = null;

        // @docs/08-tutorials/simulation-setup.md#How-to-set-up-model-factories-when-using-an-od-matrix
        LaneBasedStrategicalRoutePlannerFactory lmrs = DefaultLaneBasedGtuCharacteristicsGeneratorOd.defaultLmrs(randomStream);
        DefaultLaneBasedGtuCharacteristicsGeneratorOd generator =
                new DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory(lmrs).create();
    }

    private void factories()
    {
        StreamInterface randomStream = null;

        // @docs/08-tutorials/simulation-setup.md#How-to-set-up-model-factories-when-using-an-od-matrix
        GtuType car = DefaultsNl.CAR;
        GtuType truck = DefaultsNl.TRUCK;

        LmrsFactory tactical = new LmrsFactory.Factory().withDefaultIncentives().build(randomStream);
        ParameterFactoryByType params = new ParameterFactoryByType();
        params.addParameter(truck, ParameterTypes.A, Acceleration.instantiateSI(0.8));
        LaneBasedStrategicalPlannerFactory<?> strategical = new LaneBasedStrategicalRoutePlannerFactory(tactical, params);
        Factory factoryOD = new DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory(strategical);

        ObjectDistribution<GtuType> gtuTypeGenerator = new ObjectDistribution<>(randomStream);
        gtuTypeGenerator.add(new FrequencyAndObject<>(0.9, car));
        gtuTypeGenerator.add(new FrequencyAndObject<>(0.1, truck));
        factoryOD.setGtuTypeGenerator(gtuTypeGenerator);

        Set<GtuTemplate> templates = new LinkedHashSet<>();
        templates.add(new GtuTemplate(car, new ConstantSupplier<>(Length.instantiateSI(4.5)),
                new ConstantSupplier<>(Length.instantiateSI(1.9)), new ConstantSupplier<>(Speed.instantiateSI(50))));
        factoryOD.setTemplates(templates);

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

        DefaultLaneBasedGtuCharacteristicsGeneratorOd generator = factoryOD.create();
    }
}
