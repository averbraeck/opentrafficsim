package org.opentrafficsim.core.math;

import org.djunits.unit.AngleUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.vector.AngleVector;

/**
 * 3D-angles, RPY coded, also called phi-theta-psi coded.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Angle3D
{
    /**
     * The absolute RPY coded 3D-angle.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Dec 10, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Abs
    {
        /** the angles in 3D (YPR coded). */
        private final AngleVector.Abs angle;

        /**
         * @param angle the angles in 3D (YPR coded)
         * @throws ValueException in case the vector does not have exactly three elements
         */
        public Abs(final AngleVector.Abs angle) throws ValueException
        {
            super();
            if (angle.size() != 3)
            {
                throw new ValueException("Size of an RPY-angle vector should be exactly 3. Got: " + angle);
            }
            this.angle = angle;
        }

        /**
         * @param roll (phi) the angle around the x-axis
         * @param pitch (theta) the angle around the y-axis
         * @param yaw (psi) the angle around the z-axis
         * @throws ValueException in case the units are incorrect
         */
        public Abs(final Angle.Abs roll, final Angle.Abs pitch, final Angle.Abs yaw) throws ValueException
        {
            super();
            this.angle = new AngleVector.Abs(new Angle.Abs[]{roll, pitch, yaw}, StorageType.DENSE);
        }

        /**
         * @param roll (phi) the angle around the x-axis
         * @param pitch (theta) the angle around the y-axis
         * @param yaw (psi) the angle around the z-axis
         * @param unit the unit of the YPR parameters
         * @throws ValueException in case the units are incorrect
         */
        public Abs(final double roll, final double pitch, final double yaw, final AngleUnit unit) throws ValueException
        {
            super();
            this.angle = new AngleVector.Abs(new double[]{roll, pitch, yaw}, unit, StorageType.DENSE);
        }

        /**
         * @return the roll.
         */
        public Angle.Abs getRoll()
        {
            try
            {
                return this.angle.get(0);
            }
            catch (ValueException exception)
            {
                // should be impossible as we constructed the vector always with three elements
                throw new RuntimeException("getRoll() gave an exception; apparently vector " + this.angle
                    + " was not constructed right", exception);
            }
        }

        /**
         * @return the pitch.
         */
        public Angle.Abs getPitch()
        {
            try
            {
                return this.angle.get(1);
            }
            catch (ValueException exception)
            {
                // should be impossible as we constructed the vector always with three elements
                throw new RuntimeException("getPitch() gave an exception; apparently vector " + this.angle
                    + " was not constructed right", exception);
            }
        }

        /**
         * @return the yaw.
         */
        public Angle.Abs getYaw()
        {
            try
            {
                return this.angle.get(2);
            }
            catch (ValueException exception)
            {
                // should be impossible as we constructed the vector always with three elements
                throw new RuntimeException("getYaw() gave an exception; apparently vector " + this.angle
                    + " was not constructed right", exception);
            }
        }
    }

    /**
     * The relative RPY coded 3D-angle.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Dec 10, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Rel
    {
        /** the angles in 3D (YPR coded). */
        private final AngleVector.Rel angle;

        /**
         * @param angle the angles in 3D (YPR coded)
         * @throws ValueException in case the vector does not have exactly three elements
         */
        public Rel(final AngleVector.Rel angle) throws ValueException
        {
            super();
            if (angle.size() != 3)
            {
                throw new ValueException("Size of an RPY-angle vector should be exactly 3. Got: " + angle);
            }
            this.angle = angle;
        }

        /**
         * @param roll (phi) the angle around the x-axis
         * @param pitch (theta) the angle around the y-axis
         * @param yaw (psi) the angle around the z-axis
         * @throws ValueException in case the units are incorrect
         */
        public Rel(final Angle.Rel roll, final Angle.Rel pitch, final Angle.Rel yaw) throws ValueException
        {
            super();
            this.angle = new AngleVector.Rel(new Angle.Rel[]{roll, pitch, yaw}, StorageType.DENSE);
        }

        /**
         * @param roll (phi) the angle around the x-axis
         * @param pitch (theta) the angle around the y-axis
         * @param yaw (psi) the angle around the z-axis
         * @param unit the unit of the YPR parameters
         * @throws ValueException in case the units are incorrect
         */
        public Rel(final double roll, final double pitch, final double yaw, final AngleUnit unit) throws ValueException
        {
            super();
            this.angle = new AngleVector.Rel(new double[]{roll, pitch, yaw}, unit, StorageType.DENSE);
        }

        /**
         * @return the roll.
         */
        public Angle.Rel getRoll()
        {
            try
            {
                return this.angle.get(0);
            }
            catch (ValueException exception)
            {
                // should be impossible as we constructed the vector always with three elements
                throw new RuntimeException("getRoll() gave an exception; apparently vector " + this.angle
                    + " was not constructed right", exception);
            }
        }

        /**
         * @return the pitch.
         */
        public Angle.Rel getPitch()
        {
            try
            {
                return this.angle.get(1);
            }
            catch (ValueException exception)
            {
                // should be impossible as we constructed the vector always with three elements
                throw new RuntimeException("getPitch() gave an exception; apparently vector " + this.angle
                    + " was not constructed right", exception);
            }
        }

        /**
         * @return the yaw.
         */
        public Angle.Rel getYaw()
        {
            try
            {
                return this.angle.get(2);
            }
            catch (ValueException exception)
            {
                // should be impossible as we constructed the vector always with three elements
                throw new RuntimeException("getYaw() gave an exception; apparently vector " + this.angle
                    + " was not constructed right", exception);
            }
        }
    }

}
