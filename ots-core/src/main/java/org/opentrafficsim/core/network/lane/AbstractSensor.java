package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractSensor implements Sensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The lane for which this is a sensor. */
    private final Lane<?, ?> lane;

    /** The position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane in SI units. */
    private final double longitudinalPositionSI;

    /** the relative position of the vehicle that triggers the sensor. */
    private final RelativePosition.TYPE positionType;

    /** the name of the sensor. */
    private final String name;

    /**
     * @param lane The lane for which this is a sensor.
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the position (between 0.0 and the length of the Lane) of
     *            the sensor on the design line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the sensor.
     * @param name the name of the sensor.
     */
    public AbstractSensor(final Lane<?, ?> lane, final DoubleScalar.Rel<LengthUnit> longitudinalPosition,
        final RelativePosition.TYPE positionType, final String name)
    {
        this.lane = lane;
        this.longitudinalPositionSI = longitudinalPosition.getSI();
        this.positionType = positionType;
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public final Lane<?, ?> getLane()
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
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        try
        {
            double fraction = this.longitudinalPositionSI / this.lane.getLength().getSI();
            LineString line = this.lane.getCenterLine().getLineString();
            LengthIndexedLine lil = new LengthIndexedLine(line);
            /*
             * fraction is the relative position on the center line of the lane. fraction may be slightly outside the range
             * 0..1. When that happens we'll extrapolate in the direction of the center line at the end of the lane. This
             * direction is obtained (approximated) by using the last or first percent of the center line.
             */
            double useFraction = fraction;
            boolean fractionAdjusted = false; // Indicate if extrapolation is needed
            if (fraction < 0)
            {
                useFraction = 0;
                fractionAdjusted = true;
            }
            if (fraction > 0.99)
            {
                useFraction = 0.99;
                fractionAdjusted = true;
            }
            // DO NOT MODIFY THE RESULT OF extractPoint (it may be one of the coordinates in line).
            Coordinate c = new Coordinate(lil.extractPoint(useFraction * line.getLength()));
            c.z = 0d;
            Coordinate cb = lil.extractPoint((useFraction + 0.01) * line.getLength());
            double angle = Math.atan2(cb.y - c.y, cb.x - c.x);
            if (fractionAdjusted)
            {
                c =
                    new Coordinate(c.x + (fraction - useFraction) * 100 * (cb.x - c.x), c.y + (fraction - useFraction) * 100
                        * (cb.y - c.y), c.z);
            }
            if (Double.isNaN(c.x))
            {
                System.out.println("Bad");
            }
            return new DirectedPoint(c.x, c.y, c.z + 0.01 /* raise it slightly above the lane surface */, 0.0, 0.0, angle);
        }
        catch (Exception ne)
        {
            System.err.println(this);
            ne.printStackTrace();
            return new DirectedPoint(0, 0, 0);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(new Point3d(-0.25, -1.8, 0.0), new Point3d(0.25, 1.8, 0.0));
    }

    /**
     * @return name.
     */
    public final String getName()
    {
        return this.name;
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
        if (Double.doubleToLongBits(this.longitudinalPositionSI) != Double.doubleToLongBits(other.longitudinalPositionSI))
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
