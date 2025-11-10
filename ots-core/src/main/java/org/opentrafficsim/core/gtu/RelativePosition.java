package org.opentrafficsim.core.gtu;

import org.djunits.value.vdouble.scalar.Length;

/**
 * A RelativePosition is a position on a GTU; e.g. the front, rear, position of the driver, etc. <br>
 * A RelativePosition stores the offset of the position from the reference position of the GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Dec 30, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param dx positive x is in the normal direction of movement.
 * @param dy positive y is left compared to the normal direction of movement (seen from the top).
 * @param dz positive z is up.
 * @param type type of relative position (FRONT, BACK, etc.).
 */
public record RelativePosition(Length dx, Length dy, Length dz, Type type)
{

    /** Standard relative position type FRONT. */
    public static final Type FRONT = new Type("FRONT");

    /** Standard relative position type BACK. */
    public static final Type REAR = new Type("REAR");

    /** Standard relative position type CENTER. */
    public static final Type CENTER = new Type("CENTER");

    /** Standard relative position type REFERENCE. */
    public static final Type REFERENCE = new Type("REFERENCE");

    /** Standard relative position type DRIVER. */
    public static final Type DRIVER = new Type("DRIVER");

    /** Standard relative position type CONTOUR. There can be multiple points of type CONTOUR for one GTU. */
    public static final Type CONTOUR = new Type("CONTOUR");

    /** The reference position (always 0, 0, 0). */
    public static final RelativePosition REFERENCE_POSITION =
            new RelativePosition(Length.ZERO, Length.ZERO, Length.ZERO, RelativePosition.REFERENCE);

    /**
     * Constructor.
     * @param p a relative position to make a deep copy of.
     */
    public RelativePosition(final RelativePosition p)
    {
        this(p.dx(), p.dy(), p.dz(), p.type());
    }

    /**
     * The type of relative position, e.g., Front, Back, etc.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$, initial version ec 31, 2014 <br>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     */
    public static class Type
    {
        /** The type name. */
        private final String name;

        /** the cached hashcode. */
        private final int hash;

        /**
         * Constructor.
         * @param name the type name.
         */
        public Type(final String name)
        {
            this.name = name;
            this.hash = 31 + ((this.name == null) ? 0 : this.name.hashCode());
        }

        /**
         * Return name.
         * @return name.
         */
        public final String getName()
        {
            return this.name;
        }

        @Override
        public final String toString()
        {
            return this.name;
        }

        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public int hashCode()
        {
            return this.hash;
        }

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
            Type other = (Type) obj;
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
