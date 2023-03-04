package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;

/**
 * A RelativePosition is a position on a GTU; e.g. the front, rear, position of the driver, etc. <br>
 * A RelativePosition stores the offset of the position from the reference position of the GTU.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Dec 30, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class RelativePosition implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** Positive x is in the normal direction of movement. */
    private final Length dx;

    /** Positive y is left compared to the normal direction of movement (seen from the top). */
    private final Length dy;

    /** Positive z is up. */
    private final Length dz;

    /** Type of relative position (FRONT, BACK, etc.). */
    private final TYPE type;

    /** Standard relative position type FRONT. */
    public static final TYPE FRONT = new TYPE("FRONT");

    /** Standard relative position type BACK. */
    public static final TYPE REAR = new TYPE("REAR");

    /** Standard relative position type CENTER. */
    public static final TYPE CENTER = new TYPE("CENTER");

    /** Standard relative position type REFERENCE. */
    public static final TYPE REFERENCE = new TYPE("REFERENCE");

    /** Standard relative position type DRIVER. */
    public static final TYPE DRIVER = new TYPE("DRIVER");

    /** Standard relative position type CONTOUR. There can be multiple points of type CONTOUR for one GTU. */
    public static final TYPE CONTOUR = new TYPE("CONTOUR");

    /** The reference position (always 0, 0, 0). */
    public static final RelativePosition REFERENCE_POSITION =
            new RelativePosition(Length.ZERO, Length.ZERO, Length.ZERO, RelativePosition.REFERENCE);

    /** the cached hash code. */
    private final int hash;

    /**
     * @param dx Length; positive x is in the normal direction of movement.
     * @param dy Length; positive y is left compared to the normal direction of movement (seen from the top).
     * @param dz Length; positive z is up.
     * @param type TYPE; type of relative position (FRONT, BACK, etc.).
     */
    public RelativePosition(final Length dx, final Length dy, final Length dz, final TYPE type)
    {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.type = type;

        this.hash = calcHashCode();
    }

    /**
     * @param p RelativePosition; a relative position to make a deep copy of.
     */
    public RelativePosition(final RelativePosition p)
    {
        this.dx = p.getDx();
        this.dy = p.getDy();
        this.dz = p.getDz();
        this.type = p.getType();

        this.hash = calcHashCode();
    }

    /**
     * @return dx.
     */
    public final Length getDx()
    {
        return this.dx;
    }

    /**
     * @return dy.
     */
    public final Length getDy()
    {
        return this.dy;
    }

    /**
     * @return dz.
     */
    public final Length getDz()
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
     * Calculate the hash code once.
     * @return the hash code.
     */
    public final int calcHashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.dx == null) ? 0 : this.dx.hashCode());
        result = prime * result + ((this.dy == null) ? 0 : this.dy.hashCode());
        result = prime * result + ((this.dz == null) ? 0 : this.dz.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        return this.hash;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RelativePosition other = (RelativePosition) obj;
        if (this.dx == null)
        {
            if (other.dx != null)
                return false;
        }
        else if (!this.dx.equals(other.dx))
            return false;
        if (this.dy == null)
        {
            if (other.dy != null)
                return false;
        }
        else if (!this.dy.equals(other.dy))
            return false;
        if (this.dz == null)
        {
            if (other.dz != null)
                return false;
        }
        else if (!this.dz.equals(other.dz))
            return false;
        if (this.type == null)
        {
            if (other.type != null)
                return false;
        }
        else if (!this.type.equals(other.type))
            return false;
        return true;
    }

    /**
     * The type of relative position, e.g., Front, Back, etc.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$, initial version ec 31, 2014 <br>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    public static class TYPE implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20141231L;

        /** The type name. */
        private final String name;

        /** the cached hashcode. */
        private final int hash;

        /**
         * @param name String; the type name.
         */
        public TYPE(final String name)
        {
            this.name = name;
            this.hash = 31 + ((this.name == null) ? 0 : this.name.hashCode());
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

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public int hashCode()
        {
            return this.hash;
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
        public boolean equals(final Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TYPE other = (TYPE) obj;
            if (this.name == null)
            {
                if (other.name != null)
                    return false;
            }
            else if (!this.name.equals(other.name))
                return false;
            return true;
        }

    }

}
