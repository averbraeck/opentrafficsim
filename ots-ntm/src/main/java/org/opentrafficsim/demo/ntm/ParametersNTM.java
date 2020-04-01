package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

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
public class ParametersNTM extends ParametersFundamentalDiagram
{
    /**
     * roadLength: aggregated road lengths in CELL.
     */
    private final Length roadLength;

    /**
     * @param accCritical ArrayList&lt;Double&gt;;
     * @param freeSpeed Speed;
     * @param roadLength Length;
     */
    public ParametersNTM(ArrayList<Double> accCritical, Speed freeSpeed, Length roadLength)
    {
        this.setAccCritical(accCritical);
        this.setFreeSpeed(freeSpeed);
        this.roadLength = roadLength;
        double capacity = this.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) * this.getAccCritical().get(0);
        this.setCapacityPerUnit(new Frequency(capacity, FrequencyUnit.PER_HOUR));
    }

    /**
     * @param accCritical ArrayList&lt;Double&gt;;
     * @param capacityPerLaneLength double;
     * @param roadLength Length;
     */
    public ParametersNTM(ArrayList<Double> accCritical, double capacityPerLaneLength, Length roadLength)
    {
        // TODO parameters should depend on area characteristics
        this.setAccCritical(accCritical);
        this.setCapacityPerUnit(new Frequency(capacityPerLaneLength, FrequencyUnit.PER_HOUR));
        double freeSpeed = this.getCapacity().getInUnit(FrequencyUnit.PER_HOUR) / this.getAccCritical().get(0);
        this.setFreeSpeed(new Speed(freeSpeed, SpeedUnit.KM_PER_HOUR));
        this.roadLength = roadLength;
    }

    /**
     * @param accCritical ArrayList&lt;Double&gt;;
     * @param capacityPerLaneLength
     * @param roadLength
     */
    public ParametersNTM(ArrayList<Double> accCritical)
    {
        // TODO parameters should depend on area characteristics
        this.setAccCritical(accCritical);
        this.roadLength = new Length(0, LengthUnit.KILOMETER);
    }

    /**
     * @param accCritical
     * @param freeSpeed Speed;
     * @param roadLength Length;
     */
    public ParametersNTM(Speed freeSpeed, Length roadLength)
    {
        // TODO parameters should depend on area characteristics
        ArrayList<Double> accCritical = new ArrayList<Double>();
        accCritical.add(25.0);
        accCritical.add(50.0);
        accCritical.add(100.0);
        this.setAccCritical(accCritical);
        this.setFreeSpeed(freeSpeed);
        this.roadLength = roadLength;
        double capacity = this.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) * this.getAccCritical().get(0);
        this.setCapacityPerUnit(new Frequency(capacity, FrequencyUnit.PER_HOUR));
    }

    /**
     * @param accCritical
     * @param freeSpeed
     * @param roadLength
     */
    public ParametersNTM()
    {
        Speed freeSpeed = new Speed(50, SpeedUnit.KM_PER_HOUR);
        // TODO parameters should depend on area characteristics
        ArrayList<Double> accCritical = new ArrayList<Double>();
        accCritical.add(25.0);
        accCritical.add(50.0);
        accCritical.add(100.0);
        this.setAccCritical(accCritical);
        this.setFreeSpeed(freeSpeed);
        this.roadLength = new Length(0, LengthUnit.KILOMETER);
        double capacity = this.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) * this.getAccCritical().get(0);
        this.setCapacityPerUnit(new Frequency(capacity, FrequencyUnit.PER_HOUR));
    }

    /**
     * @return roadLength.
     */
    public Length getRoadLength()
    {
        return this.roadLength;
    }

}
