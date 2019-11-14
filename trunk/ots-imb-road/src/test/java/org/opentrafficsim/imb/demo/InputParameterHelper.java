package org.opentrafficsim.imb.demo;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterString;

/**
 * InputParameterHelper.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class InputParameterHelper
{
    /** */
    private InputParameterHelper()
    {
        // static class
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

            InputParameterMap carMap;
            if (map.getValue().containsKey("car"))
            {
                carMap = (InputParameterMap) map.get("car");
            }
            else
            {
                carMap = new InputParameterMap("car", "Car", "Car parameters", 2.0);
                map.add(carMap);
            }

            InputParameterMap truckMap;
            if (map.getValue().containsKey("truck"))
            {
                truckMap = (InputParameterMap) map.get("truck");
            }
            else
            {
                truckMap = new InputParameterMap("truck", "Truck", "Truck parameters", 3.0);
                map.add(truckMap);
            }

            genericMap.add(new InputParameterDouble("carProbability", "Car probability",
                    "Probability that the next generated GTU is a passenger car", 0.8, 0.0, 1.0, true, true, "%.00f",
                    probabilityDisplayPriority));

            carMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("a", "Maximum acceleration (m/s2)",
                    "Maximum acceleration (m/s2)", Acceleration.instantiateSI(1.56), Acceleration.instantiateSI(0.5),
                    Acceleration.instantiateSI(5.0), true, true, "%.0f", 1.0));
            carMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("b",
                    "Maximum comfortable deceleration (m/s2)", "Maximum comfortable deceleration (m/s2)",
                    Acceleration.instantiateSI(2.09), Acceleration.instantiateSI(1.0), Acceleration.instantiateSI(4.0), true, true, "%.0f",
                    2.0));
            carMap.add(new InputParameterDoubleScalar<LengthUnit, Length>("s0", "Distance headway (m)", "Distance headway (m)",
                    Length.instantiateSI(3.0), Length.instantiateSI(1.0), Length.instantiateSI(10.0), true, true, "%.0f", 3.0));
            carMap.add(new InputParameterDoubleScalar<DurationUnit, Duration>("tSafe", "Time headway (s)", "Time headway (s)",
                    Duration.instantiateSI(1.2), Duration.instantiateSI(1.0), Duration.instantiateSI(4.0), true, true, "%.0f", 4.0));

            truckMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("a", "Maximum acceleration (m/s2)",
                    "Maximum acceleration (m/s2)", Acceleration.instantiateSI(0.75), Acceleration.instantiateSI(0.5),
                    Acceleration.instantiateSI(5.0), true, true, "%.0f", 1.0));
            truckMap.add(new InputParameterDoubleScalar<AccelerationUnit, Acceleration>("b",
                    "Maximum comfortable deceleration (m/s2)", "Maximum comfortable deceleration (m/s2)",
                    Acceleration.instantiateSI(1.25), Acceleration.instantiateSI(1.0), Acceleration.instantiateSI(4.0), true, true, "%.0f",
                    2.0));
            truckMap.add(
                    new InputParameterDoubleScalar<LengthUnit, Length>("s0", "Distance headway (m)", "Distance headway (m)",
                            Length.instantiateSI(3.0), Length.instantiateSI(1.0), Length.instantiateSI(10.0), true, true, "%.0f", 3.0));
            truckMap.add(new InputParameterDoubleScalar<DurationUnit, Duration>("tSafe", "Time headway (s)", "Time headway (s)",
                    Duration.instantiateSI(1.2), Duration.instantiateSI(1.0), Duration.instantiateSI(4.0), true, true, "%.0f", 4.0));

        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Make a map of input parameters for a demo that needs IMB parameters. Give the displayPriority a low value so it shows up
     * as the first input tab.
     * @param map InputParameterMap; the map to add the IMB input parameters to
     */
    public static void makeInputParameterMapIMB(final InputParameterMap map)
    {
        try
        {
            InputParameterMap imbMap;
            if (map.getValue().containsKey("IMB"))
            {
                imbMap = (InputParameterMap) map.get("IMB");
            }
            else
            {
                imbMap = new InputParameterMap("imb", "IMB", "IMB parameters", 0.0);
                map.add(imbMap);
            }
            imbMap.add(new InputParameterString("IMBHost", "Host name", "Host name or IP address", "127.0.0.1", 1.0));
            imbMap.add(new InputParameterInteger("IMBPort", "Port", "Port of the host [1-65535]", 4000, 1, 65535, "%d", 2.0));
            imbMap.add(new InputParameterInteger("IMBModelId", "ModelId", "Id of the model [1-65535]", 1, 1, 65535, "%d", 2.0));
            imbMap.add(new InputParameterString("IMBFederation", "Federation name", "Name of the federation", "OTS_RT", 1.0));
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }
}
