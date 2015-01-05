package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Dec 30, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class RelativePosition implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** positive x is in the normal direction of movement. */
    private final DoubleScalar.Rel<LengthUnit> dx;

    /** positive y is left compared to the normal direction of movement (seen from the top). */
    private final DoubleScalar.Rel<LengthUnit> dy;

    /** positive z is up. */
    private final DoubleScalar.Rel<LengthUnit> dz;

    /** type of relative position (FRONT, BACK, etc.). */
    private final TYPE type;

    /** Standard relative position type FRONT. */
    public static final TYPE FRONT = new TYPE("FRONT");

    /** Standard relative position type BACK. */
    public static final TYPE BACK = new TYPE("BACK");

    /** Standard relative position type CENTER. */
    public static final TYPE CENTER = new TYPE("CENTER");

    /** Standard relative position type DRIVER. */
    public static final TYPE DRIVER = new TYPE("DRIVER");

    /** the reference position (always 0, 0, 0). */
    public static final RelativePosition REFERENCE = new RelativePosition(new DoubleScalar.Rel<LengthUnit>(0.0d,
        LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(0.0d, LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(0.0d,
        LengthUnit.METER), RelativePosition.CENTER);

    /**
     * @param dx positive x is in the normal direction of movement.
     * @param dy positive y is left compared to the normal direction of movement (seen from the top).
     * @param dz positive z is up.
     * @param type type of relative position (FRONT, BACK, etc.).
     */
    public RelativePosition(final DoubleScalar.Rel<LengthUnit> dx, final DoubleScalar.Rel<LengthUnit> dy,
        final DoubleScalar.Rel<LengthUnit> dz, final TYPE type)
    {
        super();
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.type = type;
    }

    /**
     * @param p a relative position to make a deep copy of.
     */
    public RelativePosition(final RelativePosition p)
    {
        super();
        this.dx = p.getDx().copy();
        this.dy = p.getDy().copy();
        this.dz = p.getDz().copy();
        this.type = p.getType();
    }

    /**
     * @return dx.
     */
    public final DoubleScalar.Rel<LengthUnit> getDx()
    {
        return this.dx;
    }

    /**
     * @return dy.
     */
    public final DoubleScalar.Rel<LengthUnit> getDy()
    {
        return this.dy;
    }

    /**
     * @return dz.
     */
    public final DoubleScalar.Rel<LengthUnit> getDz()
    {
        return this.dz;
    }

    /**
     * @return type.
     */
    public final TYPE getType()
    {
        return this.type;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "(" + this.dx + ", " + this.dy + ", " + this.dz + "): " + this.type;
    }

    /**
     * The type of relative position, e.g., Front, Back, etc.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Dec 31, 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class TYPE implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20141231L;

        /** the type name. */
        private final String name;

        /**
         * @param name the type name.
         */
        public TYPE(final String name)
        {
            super();
            this.name = name;
        }

        /**
         * @return name.
         */
        public final String getName()
        {
            return this.name;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return this.name;
        }
        
        
    }
}
