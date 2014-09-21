package org.opentrafficsim.demo.ntm.fundamentaldiagrams;

import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 9 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 * @param <ID> the ID type.
 */
public abstract class FundamentalDiagram<ID>
{
    /** */
    private ID id;

    /** carProduction: numbers of Cars produced from this CELL */
    private double carProduction;

    /** currentSpeed: average current speed of Cars in this CELL */
    private DoubleScalar.Abs<SpeedUnit> currentSpeed;

    /**
     * freeSpeed: average free speed of cars in Network element (lane, link, network zone)
     */
    private DoubleScalar.Abs<SpeedUnit> freeSpeed;

    /**
     * @param id
     */
    public FundamentalDiagram(ID id)
    {
        this.id = id;
    }

    /**
     * @return id
     */
    public ID getId()
    {
        return this.id;
    }

    /**
     * @return carProduction
     */
    public double getCarProduction()
    {
        return this.carProduction;
    }

    /**
     * @param carProduction set carProduction
     */
    public void setCarProduction(double carProduction)
    {
        this.carProduction = carProduction;
    }

    /**
     * @return currentSpeed
     */
    public DoubleScalar.Abs<SpeedUnit> getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @return freeSpeed
     */
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @param freeSpeed set freeSpeed
     */
    public void setFreeSpeed(DoubleScalar.Abs<SpeedUnit> freeSpeed)
    {
        this.freeSpeed = freeSpeed;
    }

}
