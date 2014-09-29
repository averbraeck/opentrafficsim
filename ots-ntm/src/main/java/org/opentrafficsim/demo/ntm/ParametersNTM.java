package org.opentrafficsim.demo.ntm;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 26 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class ParametersNTM
{

    /** parameter accCritical1: when production stops to increase (at maxProduction). */
    private double accCritical1;

    /** parameter accCritical2: when production starts to decrease (from maxProduction to zero). */
    private double accCritical2;

    /** parameter accJam: complete grid lock. */
    private double accJam;

    /**
     * freeSpeed: average free speed of cars in CELL.
     */
    private final DoubleScalar.Abs<SpeedUnit> freeSpeed;

    /**
     * freeSpeed: average free speed of cars in CELL.
     */
    private final DoubleScalar.Abs<LengthUnit> roadLength;

    /**
     * @param accCritical1
     * @param accCritical2
     * @param accJam
     * @param freeSpeed
     * @param roadLength
     */
    public ParametersNTM(double accCritical1, double accCritical2, double accJam, Abs<SpeedUnit> freeSpeed,
            Abs<LengthUnit> roadLength)
    {
        super();
        this.accCritical1 = accCritical1;
        this.accCritical2 = accCritical2;
        this.accJam = accJam;
        this.freeSpeed = freeSpeed;
        this.roadLength = roadLength;
    }

    /**
     * @return accCritical1.
     */
    public double getAccCritical1()
    {
        return this.accCritical1;
    }

    /**
     * @param accCritical1 set accCritical1.
     */
    public void setAccCritical1(double accCritical1)
    {
        this.accCritical1 = accCritical1;
    }

    /**
     * @return accCritical2.
     */
    public double getAccCritical2()
    {
        return this.accCritical2;
    }

    /**
     * @param accCritical2 set accCritical2.
     */
    public void setAccCritical2(double accCritical2)
    {
        this.accCritical2 = accCritical2;
    }

    /**
     * @return accJam.
     */
    public double getAccJam()
    {
        return this.accJam;
    }

    /**
     * @param accJam set accJam.
     */
    public void setAccJam(double accJam)
    {
        this.accJam = accJam;
    }

    /**
     * @return freeSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @return roadLength.
     */
    public DoubleScalar.Abs<LengthUnit> getRoadLength()
    {
        return this.roadLength;
    }

    
    
}
