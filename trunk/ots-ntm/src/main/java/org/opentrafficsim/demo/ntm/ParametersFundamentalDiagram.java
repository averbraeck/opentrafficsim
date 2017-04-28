package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Frequency;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 26 Sep 2014 <br>
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
    private DoubleScalar.Abs<SpeedUnit> freeSpeed;

    /** */
    private Frequency capacity;

    /**
     * @param accCritical
     * @param speed
     */
    public ParametersFundamentalDiagram()
    {
    }

    /**
     * @param freeSpeed
     * @param capacity
     * @param numberOfLanes
     */
    public ParametersFundamentalDiagram(final DoubleScalar.Abs<SpeedUnit> freeSpeed,
        final Frequency capacity, final int numberOfLanes)
    {
        super();
        this.accCritical = new ArrayList<Double>();
        this.accCritical.add((capacity.getInUnit(FrequencyUnit.PER_HOUR) / freeSpeed.getInUnit(SpeedUnit.KM_PER_HOUR)));
        // * cellLength.getInUnit(LengthUnit.KILOMETER));
        this.accCritical.add(150.0 * numberOfLanes);
        // * cellLength.getInUnit(LengthUnit.KILOMETER));
        this.freeSpeed = freeSpeed;
        this.capacity = capacity;
    }

    /**
     * @return freeSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @param freeSpeed set freeSpeed.
     */
    public void setFreeSpeed(DoubleScalar.Abs<SpeedUnit> freeSpeed)
    {
        this.freeSpeed = freeSpeed;
    }

    /**
     * @return accCritical1.
     */
    public ArrayList<Double> getAccCritical()
    {
        return this.accCritical;
    }

    /**
     * @param accCritical
     */
    public void setAccCritical(ArrayList<Double> accCritical)
    {
        this.accCritical = accCritical;
    }

    /**
     * @return capacity.
     */
    public Frequency getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set capacity.
     */
    public void setCapacityPerUnit(Frequency capacity)
    {
        this.capacity = capacity;
    }

}
