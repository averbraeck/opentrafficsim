package org.opentrafficsim.swing.script;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;

import picocli.CommandLine.Option;

/**
 * Class containing a set of command line options for the intelligent driver model (IDM). To integrate in a program, give the
 * program the following property:
 * 
 * <pre>
 *     {@code @}ArgGroup 
 *     private IdmOptions idmOptions = new IdmOptions();
 * </pre>
 * 
 * Note that the variable initiation is only required if default values are changed using
 * {@code CliUtil.changeOptionDefault(...)}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class IdmOptions
{

    /** Maximum acceleration of cars. */
    @Option(names = "--aCar", description = "Maximum acceleration of cars.", defaultValue = "1.25m/s^2")
    private Acceleration aCar;

    /** Maximum acceleration of trucks. */
    @Option(names = "--aTruck", description = "Maximum acceleration of trucks.", defaultValue = "0.4m/s^2")
    private Acceleration aTruck;

    /** Maximum comfortable deceleration. */
    @Option(names = "--maxDecel", description = "Maximum comfortable deceleration.", defaultValue = "2.09m/s^2")
    private Acceleration b;

    /** Minimum desired headway. */
    @Option(names = "--Tmin", description = "Minimum desired headway.", defaultValue = "0.56s")
    private Duration tMin;

    /** Normal desired headway. */
    @Option(names = "--Tmax", description = "Normal desired headway.", defaultValue = "1.2s")
    private Duration tMax;

    /**
     * Returns the maximum acceleration of cars.
     * @return Acceleration; maximum acceleration of cars
     */
    public Acceleration getACar()
    {
        return this.aCar;
    }

    /**
     * Returns the maximum acceleration of trucks.
     * @return Acceleration; maximum acceleration of truck
     */
    public Acceleration getATruck()
    {
        return this.aTruck;
    }

    /**
     * Returns the maximum comfortable deceleration.
     * @return Acceleration; maximum comfortable deceleration
     */
    public Acceleration getB()
    {
        return this.b;
    }

    /**
     * Returns the minimum desired headway.
     * @return Duration; minimum desired headway
     */
    public Duration getTMin()
    {
        return this.tMin;
    }

    /**
     * Returns the normal desired headway.
     * @return Duration; normal desired headway
     */
    public Duration getTMax()
    {
        return this.tMax;
    }

}
