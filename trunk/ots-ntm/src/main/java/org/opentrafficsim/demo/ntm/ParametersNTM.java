package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version26 Sep 2014 <br>
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
    private final DoubleScalar.Rel<LengthUnit> roadLength;

    /**
     * @param accCritical
     * @param freeSpeed
     * @param roadLength
     */
    public ParametersNTM(ArrayList<Double> accCritical, Abs<SpeedUnit> freeSpeed, Rel<LengthUnit> roadLength)
    {
        super();
        this.setAccCritical(accCritical);
        this.setFreeSpeed(freeSpeed);
        this.roadLength = roadLength;
        double capacity = this.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) * this.getAccCritical().get(0);
        this.setCapacityPerUnit(new DoubleScalar.Abs<FrequencyUnit>(capacity, FrequencyUnit.PER_HOUR));
    }

    /**
     * @param accCritical
     * @param capacityPerLaneLength
     * @param roadLength
     */
    public ParametersNTM(ArrayList<Double> accCritical, double capacityPerLaneLength, Rel<LengthUnit> roadLength)
    {
        super();
        // TODO parameters should depend on area characteristics
        this.setAccCritical(accCritical);
        this.setCapacityPerUnit(new DoubleScalar.Abs<FrequencyUnit>(capacityPerLaneLength, FrequencyUnit.PER_HOUR));
        double freeSpeed = this.getCapacity().getInUnit(FrequencyUnit.PER_HOUR) / this.getAccCritical().get(0);
        this.setFreeSpeed(new DoubleScalar.Abs<SpeedUnit>(freeSpeed, SpeedUnit.KM_PER_HOUR));
        this.roadLength = roadLength;
    }

    /**
     * @param accCritical
     * @param capacityPerLaneLength
     * @param roadLength
     */
    public ParametersNTM(ArrayList<Double> accCritical)
    {
        super();
        // TODO parameters should depend on area characteristics
        this.setAccCritical(accCritical);
        this.roadLength = new Rel<LengthUnit>(0, LengthUnit.KILOMETER);
    }

    /**
     * @param accCritical
     * @param freeSpeed
     * @param roadLength
     */
    public ParametersNTM(Abs<SpeedUnit> freeSpeed, Rel<LengthUnit> roadLength)
    {
        super();
        // TODO parameters should depend on area characteristics
        ArrayList<Double> accCritical = new ArrayList<Double>();
        accCritical.add(25.0);
        accCritical.add(50.0);
        accCritical.add(100.0);
        this.setAccCritical(accCritical);
        this.setFreeSpeed(freeSpeed);
        this.roadLength = roadLength;
        double capacity = this.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) * this.getAccCritical().get(0);
        this.setCapacityPerUnit(new DoubleScalar.Abs<FrequencyUnit>(capacity, FrequencyUnit.PER_HOUR));
    }

    /**
     * @param accCritical
     * @param freeSpeed
     * @param roadLength
     */
    public ParametersNTM()
    {
        super();
        Abs<SpeedUnit> freeSpeed = new Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        // TODO parameters should depend on area characteristics
        ArrayList<Double> accCritical = new ArrayList<Double>();
        accCritical.add(25.0);
        accCritical.add(50.0);
        accCritical.add(100.0);
        this.setAccCritical(accCritical);
        this.setFreeSpeed(freeSpeed);
        this.roadLength = new Rel<LengthUnit>(0, LengthUnit.KILOMETER);
        double capacity = this.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) * this.getAccCritical().get(0);
        this.setCapacityPerUnit(new DoubleScalar.Abs<FrequencyUnit>(capacity, FrequencyUnit.PER_HOUR));
    }

    /**
     * @return roadLength.
     */
    public Rel<LengthUnit> getRoadLength()
    {
        return this.roadLength;
    }

}
