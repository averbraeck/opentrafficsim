package org.opentrafficsim.core.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.parameters.ParameterFactoryByType.Correlation;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ParameterFactoryByTypeTest
{

    /** Test parameter a. */
    private static final ParameterTypeDouble A = new ParameterTypeDouble("a", "test parameter a", 0.0);

    /** Test parameter b. */
    private static final ParameterTypeDouble B = new ParameterTypeDouble("b", "test parameter b", 0.0);

    /** Test parameter c. */
    private static final ParameterTypeDouble C = new ParameterTypeDouble("c", "test parameter c", 0.0);

    /** */
    private ParameterFactoryByTypeTest()
    {
        // do not instantiate test class
    }

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

        // Various values across GTU type hierarchy

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

        // Various correlated values across GTU type hierarchy

        parameterFactory.addParameter(B, 0.0);
        addCorrelation(parameterFactory, null, A, B, 2.0, 0.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 2.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 2.0 + 0.5);

        addCorrelation(parameterFactory, roadUser, A, B, 3.0, 0.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 3.0 + 0.5);

        addCorrelation(parameterFactory, vehicle, A, B, 4.0, 0.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 4.0 + 0.5);

        addCorrelation(parameterFactory, bicycle, A, B, 5.0, 0.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 5.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 4.0 + 0.5);

        addCorrelation(parameterFactory, car, A, B, 6.0, 0.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 5.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 6.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 4.0 + 0.5);

        addCorrelation(parameterFactory, truck, A, B, 7.0, 0.0);
        testParameterValue(parameterFactory, B, roadUser, 2.0 * 3.0 + 0.5);
        testParameterValue(parameterFactory, B, bicycle, 4.0 * 5.0 + 0.5);
        testParameterValue(parameterFactory, B, vehicle, 3.0 * 4.0 + 0.5);
        testParameterValue(parameterFactory, B, car, 5.0 * 6.0 + 0.5);
        testParameterValue(parameterFactory, B, truck, 6.0 * 7.0 + 0.5);

        parameterFactory.addParameter(C, 3.14);
        addCorrelation(parameterFactory, null, B, C, 1.5, 4.25);
        testParameterValue(parameterFactory, C, roadUser, (2.0 * 3.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25);
        testParameterValue(parameterFactory, C, bicycle, (4.0 * 5.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25);
        testParameterValue(parameterFactory, C, vehicle, (3.0 * 4.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25);
        testParameterValue(parameterFactory, C, car, (5.0 * 6.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25);
        testParameterValue(parameterFactory, C, truck, (6.0 * 7.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25);

        // Chained dependency a -> b -> c

        ParameterFactoryByType parameterFactory2 = new ParameterFactoryByType();
        parameterFactory2.addParameter(B, 5.4);
        parameterFactory2.addParameter(C, 3.14);
        addCorrelation(parameterFactory2, null, A, B, 3.0, 0.0);
        addCorrelation(parameterFactory2, null, B, C, 1.5, 4.25);
        // no a specified
        UnitTest.testFail(() -> testParameterValue(parameterFactory2, C, roadUser, (2.0 * 3.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25),
                "Dependency a -> b -> c with a not specified in parameters should fail.", ParameterException.class);
        // a from given parameter set
        ParameterSet parameters = new ParameterSet();
        parameters.setParameter(A, 2.0);
        testParameterValue(parameterFactory2, C, roadUser, (2.0 * 3.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25, parameters);
        // a from factory itself
        parameterFactory2.addParameter(A, 2.0);
        testParameterValue(parameterFactory2, C, roadUser, (2.0 * 3.0 + 0.5) * 1.5 + 0.5 + 3.14 * 4.25);

        // Circular dependency a -> b -> a

        ParameterFactoryByType parameterFactory3 = new ParameterFactoryByType();
        parameterFactory3.addParameter(A, 1.0);
        parameterFactory3.addParameter(B, 2.0);
        addCorrelation(parameterFactory3, null, A, B, 1.0, 1.0);
        addCorrelation(parameterFactory3, null, B, A, 1.0, 1.0);
        UnitTest.testFail(() -> parameterFactory3.setValues(new ParameterSet(), roadUser),
                "Circular dependency a -> b -> a should fail.", ParameterException.class);

        // Multiple dependency a & b -> c

        ParameterFactoryByType parameterFactory4 = new ParameterFactoryByType();
        parameterFactory4.addParameter(A, 1.0);
        parameterFactory4.addParameter(B, 2.0);
        parameterFactory4.addParameter(C, 3.0);
        addCorrelation(parameterFactory4, null, A, C, 1.0, 1.0);
        addCorrelation(parameterFactory4, null, B, C, 1.0, 1.0);
        UnitTest.testFail(() -> parameterFactory4.setValues(new ParameterSet(), roadUser),
                "Multiple dependency a & b -> c should fail.", ParameterException.class);

        // Check that bounds can be solved: Tmin < Tmax => 0.56 < 1.2 => 2.5 * 0.56 < 2.5 * 1.2

        ParameterFactoryByType parameterFactory5 = new ParameterFactoryByType();
        parameters = new ParameterSet();
        parameters.setDefaultParameter(ParameterTypes.TMIN);
        parameters.setDefaultParameter(ParameterTypes.TMAX);
        parameters.setParameter(A, 2.5);
        parameterFactory5.addCorrelation(A, ParameterTypes.TMIN, (a, t) -> t.times(a));
        parameterFactory5.addCorrelation(A, ParameterTypes.TMAX, (a, t) -> t.times(a));
        parameterFactory5.setValues(parameters, roadUser);
        assertEquals(ParameterTypes.TMIN.getDefaultValue().si * 2.5, parameters.getParameter(ParameterTypes.TMIN).doubleValue(),
                "Parameter Tmin does not have the correct value.");
        assertEquals(ParameterTypes.TMAX.getDefaultValue().si * 2.5, parameters.getParameter(ParameterTypes.TMAX).doubleValue(),
                "Parameter Tmin does not have the correct value.");

        // Correlation beyond bounds

        ParameterFactoryByType parameterFactory6 = new ParameterFactoryByType();
        parameterFactory6.addParameter(ParameterTypes.TMIN, Duration.ofSI(2.0));
        parameterFactory6.addParameter(A, -0.5);
        parameterFactory6.addCorrelation(A, ParameterTypes.TMIN, (a, t) -> t.times(a));
        UnitTest.testFail(() -> parameterFactory6.setValues(new ParameterSet(), roadUser),
                "Correlation out of bounds should fail.", ParameterException.class);

    }

    /**
     * Tests a single parameter value.
     * @param parameterFactory parameter factory
     * @param parameterType parameter type
     * @param gtuType GTU type
     * @param value value
     * @throws ParameterException if parameter 'a' is not set
     */
    private static void testParameterValue(final ParameterFactory parameterFactory, final ParameterTypeDouble parameterType,
            final GtuType gtuType, final Double value) throws ParameterException
    {
        testParameterValue(parameterFactory, parameterType, gtuType, value, new ParameterSet());
    }

    /**
     * Tests a single parameter value.
     * @param parameterFactory parameter factory
     * @param parameterType parameter type
     * @param gtuType GTU type
     * @param value value
     * @param parameters predetermined parameters
     * @throws ParameterException if parameter 'a' is not set
     */
    private static void testParameterValue(final ParameterFactory parameterFactory, final ParameterTypeNumeric<?> parameterType,
            final GtuType gtuType, final Double value, final Parameters parameters) throws ParameterException
    {
        parameterFactory.setValues(parameters, gtuType);
        assertEquals(value, parameters.getParameter(parameterType).doubleValue(), "Parameter does not have the correct value.");
    }

    /**
     * Adds a correlation B = A * factorFirst + 0.5 + B * factorThen.
     * @param parameterFactory parameter factory
     * @param gtuType GTU type
     * @param first independent parameter
     * @param then dependent parameter
     * @param factorFirst factor on first parameter
     * @param factorThen factor on then parameter
     */
    private static void addCorrelation(final ParameterFactoryByType parameterFactory, final GtuType gtuType,
            final ParameterTypeDouble first, final ParameterTypeDouble then, final double factorFirst, final double factorThen)
    {
        Correlation<Double, Double> correlation = (f, t) -> f * factorFirst + 0.5 + t * factorThen;
        parameterFactory.addCorrelation(gtuType, first, then, correlation);
    }

}
