package org.opentrafficsim.web.test;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.parameters.ParameterFactory;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;

/**
 * InputParameterHelper.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class InputParameterHelper implements ParameterFactory
{

    /** Input parameter map. */
    private final InputParameterMap rootMap;

    /**
     * Constructor.
     * @param rootMap InputParameterMap; input parameter map
     */
    public InputParameterHelper(final InputParameterMap rootMap)
    {
        this.rootMap = rootMap;
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final Parameters parameters, final GTUType gtuType) throws ParameterException
    {
        try
        {
            if (gtuType.isOfType(GTUType.DEFAULTS.CAR))
            {
                getParametersCar(this.rootMap).setAllIn(parameters);
            }
            else if (gtuType.isOfType(GTUType.DEFAULTS.TRUCK))
            {
                getParametersTruck(this.rootMap).setAllIn(parameters);
            }
            else
            {
                throw new ParameterException("GTUType " + gtuType + " not supported in demo parameter factory.");
            }
        }
        catch (InputParameterException exception)
        {
            throw new ParameterException(exception);
        }
    }

    /**
     * Make a map of input parameters for a demo with a car/truck ratio and car/truck tabs with parameters.
     * @param map InputParameterMap; the map to add the car/truck input parameters to
     * @param probabilityDisplayPriority double; the display priority to use for the car probability in the generic map
     */
    public static void makeInputParameterMapCarTruck(final InputParameterMap map, final double probabilityDisplayPriority)
    {
        try
        {
            InputParameterMap genericMap;
            if (map.getValue().containsKey("generic"))
            {
                genericMap = (InputParameterMap) map.get("generic");
            }
            else
            {
                genericMap = new InputParameterMap("generic", "Generic", "Generic parameters", 1.0);
                map.add(genericMap);
            }
            genericMap.add(new InputParameterDouble("carProbability", "Car probability",
                    "Probability that the next generated GTU is a passenger car", 0.8, 0.0, 1.0, true, true, "%.00f",
                    probabilityDisplayPriority));

            makeInputParameterMapCar(map, 2.0);

            makeInputParameterMapTruck(map, 3.0);

        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Make a map of input parameters for a demo with a car tabs with parameters.
     * @param map InputParameterMap; the map to add the car input tab to
     * @param displayPriority double; the display priority to use for the car tab in the generic map
     */
    public static void makeInputParameterMapCar(final InputParameterMap map, final double displayPriority)
    {
        try
        {
            InputParameterMap carMap;
            if (map.getValue().containsKey("car"))
            {
                carMap = (InputParameterMap) map.get("car");
            }
            else
            {
                carMap = new InputParameterMap("car", "Car", "Car parameters", displayPriority);
                map.add(carMap);
            }

            carMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("a", "Maximum acceleration (m/s2)",
                    "Maximum acceleration (m/s2)", Acceleration.instantiateSI(1.56), Acceleration.instantiateSI(0.5),
                    Acceleration.instantiateSI(5.0), true, true, "%.0f", 1.0));
            carMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("b",
                    "Maximum comfortable deceleration (m/s2)", "Maximum comfortable deceleration (m/s2)",
                    Acceleration.instantiateSI(2.09), Acceleration.instantiateSI(1.0), Acceleration.instantiateSI(4.0), true,
                    true, "%.0f", 2.0));
            carMap.add(new InputParameterDoubleScalar<LengthUnit, Length>("s0", "Distance headway (m)", "Distance headway (m)",
                    Length.instantiateSI(3.0), Length.instantiateSI(1.0), Length.instantiateSI(10.0), true, true, "%.0f", 3.0));
            carMap.add(new InputParameterDoubleScalar<DurationUnit, Duration>("tSafe", "Time headway (s)", "Time headway (s)",
                    Duration.instantiateSI(1.2), Duration.instantiateSI(1.0), Duration.instantiateSI(4.0), true, true, "%.0f",
                    4.0));
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Make a map of input parameters for a demo with a truck tabs with parameters.
     * @param map InputParameterMap; the map to add the truck input tab to
     * @param displayPriority double; the display priority to use for the truck map in the generic map
     */
    public static void makeInputParameterMapTruck(final InputParameterMap map, final double displayPriority)
    {
        try
        {
            InputParameterMap truckMap;
            if (map.getValue().containsKey("truck"))
            {
                truckMap = (InputParameterMap) map.get("truck");
            }
            else
            {
                truckMap = new InputParameterMap("truck", "Truck", "Truck parameters", displayPriority);
                map.add(truckMap);
            }

            truckMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("a", "Maximum acceleration (m/s2)",
                    "Maximum acceleration (m/s2)", Acceleration.instantiateSI(0.75), Acceleration.instantiateSI(0.5),
                    Acceleration.instantiateSI(5.0), true, true, "%.0f", 1.0));
            truckMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("b",
                    "Maximum comfortable deceleration (m/s2)", "Maximum comfortable deceleration (m/s2)",
                    Acceleration.instantiateSI(1.25), Acceleration.instantiateSI(1.0), Acceleration.instantiateSI(4.0), true,
                    true, "%.0f", 2.0));
            truckMap.add(new InputParameterDoubleScalar<LengthUnit, Length>("s0", "Distance headway (m)",
                    "Distance headway (m)", Length.instantiateSI(3.0), Length.instantiateSI(1.0), Length.instantiateSI(10.0),
                    true, true, "%.0f", 3.0));
            truckMap.add(new InputParameterDoubleScalar<DurationUnit, Duration>("tSafe", "Time headway (s)", "Time headway (s)",
                    Duration.instantiateSI(1.2), Duration.instantiateSI(1.0), Duration.instantiateSI(4.0), true, true, "%.0f",
                    4.0));
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Get the car parameters as entered.
     * @param rootMap InputParameterMap; the root map of the model with a 'car' tab with the parameters
     * @return the parameters where a, b, s0 and tSafe have been updated with the user's choices
     * @throws ParameterException when the parameter was given an illegal setting
     * @throws InputParameterException when the input parameter could not be found
     */
    public static Parameters getParametersCar(final InputParameterMap rootMap)
            throws ParameterException, InputParameterException
    {
        Parameters parametersCar = DefaultsFactory.getDefaultParameters();
        Acceleration aCar = (Acceleration) rootMap.get("car.a").getCalculatedValue();
        Acceleration bCar = (Acceleration) rootMap.get("car.b").getCalculatedValue();
        Length s0Car = (Length) rootMap.get("car.s0").getCalculatedValue();
        Duration tSafeCar = (Duration) rootMap.get("car.tSafe").getCalculatedValue();
        parametersCar.setParameter(ParameterTypes.A, aCar);
        parametersCar.setParameter(ParameterTypes.B, bCar);
        parametersCar.setParameter(ParameterTypes.S0, s0Car);
        parametersCar.setParameter(ParameterTypes.T, tSafeCar);
        return parametersCar;
    }

    /**
     * Get the truck parameters as entered.
     * @param rootMap InputParameterMap; the root map of the model with a 'truck' tab with the parameters
     * @return the parameters where a, b, s0 and tSafe have been updated with the user's choices
     * @throws ParameterException when the parameter was given an illegal setting
     * @throws InputParameterException when the input parameter could not be found
     */
    public static Parameters getParametersTruck(final InputParameterMap rootMap)
            throws ParameterException, InputParameterException
    {
        Parameters parametersTruck = DefaultsFactory.getDefaultParameters();
        Acceleration aTruck = (Acceleration) rootMap.get("truck.a").getCalculatedValue();
        Acceleration bTruck = (Acceleration) rootMap.get("truck.b").getCalculatedValue();
        Length s0Truck = (Length) rootMap.get("truck.s0").getCalculatedValue();
        Duration tSafeTruck = (Duration) rootMap.get("truck.tSafe").getCalculatedValue();
        parametersTruck.setParameter(ParameterTypes.A, aTruck);
        parametersTruck.setParameter(ParameterTypes.B, bTruck);
        parametersTruck.setParameter(ParameterTypes.S0, s0Truck);
        parametersTruck.setParameter(ParameterTypes.T, tSafeTruck);
        return parametersTruck;
    }

}
