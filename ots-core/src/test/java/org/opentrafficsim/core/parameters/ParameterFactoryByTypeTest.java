package org.opentrafficsim.core.parameters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.core.parameters.ParameterFactoryByType.Correlation;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class ParameterFactoryByTypeTest
{

    /** Test parameter a. */
    private static final ParameterTypeDouble A = new ParameterTypeDouble("a", "test parameter a", 0.0);

    /** Test parameter b. */
    private static final ParameterTypeDouble B = new ParameterTypeDouble("b", "test parameter b", 0.0);

    /**
     * Tests whether parameters are set correctly.
     * @throws ParameterException if parameter 'a' is not set
     */
    @Test
    public void testParameterFactoryByType() throws ParameterException
    {

        GtuType roadUser = DefaultsNl.ROAD_USER;
        GtuType bicycle = DefaultsNl.BICYCLE;
        GtuType vehicle = DefaultsNl.VEHICLE;
        GtuType car = DefaultsNl.CAR;
        GtuType truck = DefaultsNl.TRUCK;

        ParameterFactoryByType parameterFactory = new ParameterFactoryByType();

        parameterFactory.addParameter(A, 1.0);
        testParameterValue(parameterFactory, A, roadUser, 1.0);
        testParameterValue(parameterFactory, A, bicycle, 1.0);
        testParameterValue(parameterFactory, A, vehicle, 1.0);
        testParameterValue(parameterFactory, A, car, 1.0);
        testParameterValue(parameterFactory, A, truck, 1.0);

        parameterFactory.addParameter(roadUser, A, 2.0);
        testParameterValue(parameterFactory, A, roadUser, 2.0);
        testParameterValue(parameterFactory, A, bicycle, 2.0);
        testParameterValue(parameterFactory, A, vehicle, 2.0);
        testParameterValue(parameterFactory, A, car, 2.0);
        testParameterValue(parameterFactory, A, truck, 2.0);

        parameterFactory.addParameter(vehicle, A, 3.0);
        testParameterValue(parameterFactory, A, roadUser, 2.0);
        testParameterValue(parameterFactory, A, bicycle, 2.0);
        testParameterValue(parameterFactory, A, vehicle, 3.0);
        testParameterValue(parameterFactory, A, car, 3.0);
        testParameterValue(parameterFactory, A, truck, 3.0);

        parameterFactory.addParameter(bicycle, A, 4.0);
        testParameterValue(parameterFactory, A, roadUser, 2.0);
        testParameterValue(parameterFactory, A, bicycle, 4.0);
        testParameterValue(parameterFactory, A, vehicle, 3.0);
        testParameterValue(parameterFactory, A, car, 3.0);
        testParameterValue(parameterFactory, A, truck, 3.0);

        parameterFactory.addParameter(car, A, 5.0);
        testParameterValue(parameterFactory, A, roadUser, 2.0);
        testParameterValue(parameterFactory, A, bicycle, 4.0);
        testParameterValue(parameterFactory, A, vehicle, 3.0);
        testParameterValue(parameterFactory, A, car, 5.0);
        testParameterValue(parameterFactory, A, truck, 3.0);

        parameterFactory.addParameter(truck, A, 6.0);
        testParameterValue(parameterFactory, A, roadUser, 2.0);
        testParameterValue(parameterFactory, A, bicycle, 4.0);
        testParameterValue(parameterFactory, A, vehicle, 3.0);
        testParameterValue(parameterFactory, A, car, 5.0);
        testParameterValue(parameterFactory, A, truck, 6.0);

        parameterFactory.addParameter(B, 0.0);
        addCorrelation(parameterFactory, null, 2.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 2.0 + 0.5);

        addCorrelation(parameterFactory, roadUser, 3.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 3.0 + 0.5);

        addCorrelation(parameterFactory, vehicle, 4.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 4.0 + 0.5);

        addCorrelation(parameterFactory, bicycle, 5.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 5.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 4.0 + 0.5);

        addCorrelation(parameterFactory, car, 6.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 5.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 6.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 4.0 + 0.5);

        addCorrelation(parameterFactory, truck, 7.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 5.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 6.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 7.0 + 0.5);
    }

    /**
     * Tests a single parameter value.
     * @param parameterFactory ParameterFactory; parameter factory
     * @param parameterType ParameterTypeDouble; parameter type
     * @param gtuType GtuType; GTU type
     * @param value double; value
     * @throws ParameterException if parameter 'a' is not set
     */
    private static void testParameterValue(final ParameterFactory parameterFactory, final ParameterTypeDouble parameterType,
            final GtuType gtuType, final Double value) throws ParameterException
    {
        Parameters parameters = new ParameterSet();
        parameterFactory.setValues(parameters, gtuType);
        assertEquals("Parameter does not have the correct value.", parameters.getParameter(parameterType), value);
    }

    /**
     * Adds a correlation B = A * factor + 0.5.
     * @param parameterFactory ParameterFactoryByType; parameter factory
     * @param gtuType GtuType; GTU type
     * @param factor double; factor
     */
    private static void addCorrelation(final ParameterFactoryByType parameterFactory, final GtuType gtuType,
            final double factor)
    {
        Correlation<Double, Double> correlation = new Correlation<Double, Double>()
        {
            /** {@inheritDoc} */
            @Override
            public Double correlate(final Double first, final Double then)
            {
                return first * factor + 0.5;
            }
        };
        parameterFactory.addCorrelation(gtuType, A, B, correlation);
    }

}
