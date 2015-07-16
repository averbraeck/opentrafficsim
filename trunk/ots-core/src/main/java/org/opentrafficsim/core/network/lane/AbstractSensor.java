package org.opentrafficsim.core.network.lane;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractSensor implements Sensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The lane for which this is a sensor. */
    private final Lane lane;

    /** The position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane in SI units. */
    private final double longitudinalPositionSI;

    /** the relative position of the vehicle that triggers the sensor. */
    private final RelativePosition.TYPE positionType;

    /**
     * @param lane The lane for which this is a sensor.
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the position (between 0.0 and the length of the
     *            Lane) of the sensor on the design line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that
     *            triggers the sensor.
     */
    public AbstractSensor(final Lane lane, final DoubleScalar.Rel<LengthUnit> longitudinalPosition,
            final RelativePosition.TYPE positionType)
    {
        this.lane = lane;
        this.longitudinalPositionSI = longitudinalPosition.getSI();
        this.positionType = positionType;
    }

    /** {@inheritDoc} */
    @Override
    public final Lane getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final Rel<LengthUnit> getLongitudinalPosition()
    {
        return new DoubleScalar.Rel<LengthUnit>(this.longitudinalPositionSI, LengthUnit.METER);
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition.TYPE getPositionType()
    {
        return this.positionType;
    }

    /** {@inheritDoc} */
    @Override
    public final double getLongitudinalPositionSI()
    {
        return this.longitudinalPositionSI;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        long temp;
        temp = Double.doubleToLongBits(this.longitudinalPositionSI);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((this.positionType == null) ? 0 : this.positionType.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:designforextension"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractSensor other = (AbstractSensor) obj;
        if (this.lane == null)
        {
            if (other.lane != null)
                return false;
        }
        else if (!this.lane.equals(other.lane))
            return false;
        if (Double.doubleToLongBits(this.longitudinalPositionSI) != Double
                .doubleToLongBits(other.longitudinalPositionSI))
            return false;
        if (this.positionType == null)
        {
            if (other.positionType != null)
                return false;
        }
        else if (!this.positionType.equals(other.positionType))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int compareTo(final Sensor o)
    {
        if (this.lane != o.getLane())
        {
            return this.lane.hashCode() < o.getLane().hashCode() ? -1 : 1;
        }
        if (this.longitudinalPositionSI != o.getLongitudinalPositionSI())
        {
            return this.longitudinalPositionSI < o.getLongitudinalPositionSI() ? -1 : 1;
        }
        if (!this.positionType.equals(o.getPositionType()))
        {
            return this.positionType.hashCode() < o.getPositionType().hashCode() ? -1 : 1;
        }
        if (!this.equals(o))
        {
            return this.hashCode() < o.hashCode() ? -1 : 1;
        }
        return 0;
    }

}
