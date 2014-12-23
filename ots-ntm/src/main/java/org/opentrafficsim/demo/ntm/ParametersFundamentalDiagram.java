package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 26 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class ParametersFundamentalDiagram
{

    /**
     * parameters accCritical: when production stops to increase (at maxProduction) or decreases (accJam).
     */
    private ArrayList<Double> accCritical;

    /**
     * freeSpeed: average free speed of cars in CELL.
     */
    private final DoubleScalar.Abs<SpeedUnit> freeSpeed;

    /**
     * @param accCritical
     * @param freeSpeed
     */
    public ParametersFundamentalDiagram(ArrayList<Double> accCritical, Abs<SpeedUnit> freeSpeed)
    {
        super();
        this.accCritical = accCritical;
        this.freeSpeed = freeSpeed;
    }

    /**
     * @param accCritical defined per lane!! 
     * @param freeSpeed
     */
    public ParametersFundamentalDiagram(Abs<SpeedUnit> freeSpeed)
    {
        super();
        ArrayList<Double> accCritical = new ArrayList<Double>(); 
        accCritical.add(20.0);
        accCritical.add(150.0);
        this.accCritical = accCritical;
        this.freeSpeed = freeSpeed;
    }


    /**
     * @return freeSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @return accCritical1.
     */
    public ArrayList<Double> getAccCritical()
    {
        return this.accCritical;
    }

    /**
     * @param accCritical1 set accCritical1.
     */
    public void setAccCritical1(ArrayList<Double> accCritical)
    {
        this.accCritical = accCritical;
    }

}
