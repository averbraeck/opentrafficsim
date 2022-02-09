package org.opentrafficsim.road.network.lane.changing;

import java.io.Serializable;
import java.util.Collection;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * This class implements the overtaking conditions. Examples are:
 * <ul>
 * <li>Overtaking on the left is allowed for all GTU types (not likely when there are e.g., both cars and bicycles in the
 * model).</li>
 * <li>A GTU type CAR can overtake a GTU type TRACTOR on the left on this road, but no other types of GTUs.</li>
 * <li>When the speed of the GTU you try to overtake is lower than 50 km/h, you can overtake on the left.</li>
 * <li>When the speed of the GTU you try to overtake is 25 km/h less than the maximum speed of the lane on which you and the
 * other GTU are driving, you can overtake on the left or right.</li>
 * <li>only overtake vehicles that have a maximum speed of under 25 km/h.</li>
 * <li>Overtaking on the left is allowed for all GTU types, but overtaking on the right is also allowed when traffic density is
 * below a certain number</li>
 * <li>Overtaking on the left is allowed for all GTU types, but overtaking on the right is also allowed when the distance to a
 * traffic light is less than 200 m</li>
 * <li>Overtaking on the left and the right is allowed for all GTUs. This can e.g. be used on an American highway where all GTUs
 * that are allowed on the highway can indeed overtake on the right or the left.</li>
 * </ul>
 * <b>Note:</b> The class does not check whether it is <b>allowed</b> to overtake another GTU on this road, neither whether it
 * is possible or safe to do so. That has to be checked by the GTU itself based on e.g., gap acceptance and other behavioral
 * rules.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 13, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface OvertakingConditions
{
    /********************************************************************************************************************/
    /********************** IMPLEMENTATION CLASSES OF MOST COMMON OVERTAKING CONDITIONS *****************************/
    /********************************************************************************************************************/

    /**
     * Overtaking on the left allowed for all GTUs. Note: overtaking on the right not allowed, so vehicles will stall on a
     * multilane road near a traffic light. Also, bicycles will overtake cars on the "wrong" side of the road in this simple
     * condition!
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class LeftOnly implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftOnly []";
        }
    }

    /**
     * Overtaking on the right allowed for all GTUs. Note: overtaking on the left not allowed, so vehicles will stall on a
     * multilane road near a traffic light. Also, bicycles will overtake cars on the "wrong" side of the road in this simple
     * condition!
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class RightOnly implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightOnly []";
        }
    }

    /**
     * No overtaking allowed. Note if there are multiple lanes, vehicles will stall near a traffic light.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class None implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "None []";
        }
    }

    /**
     * Overtaking on both sides allowed. This is, e.g., the situation for an American highway.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class LeftAndRight implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftAndRight []";
        }
    }

    /**
     * Overtaking on the left allowed for all GTUs; they stay on the same lane (e.g., bicycles).
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class SameLaneLeft implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "SameLaneLeft []";
        }
    }

    /**
     * Overtaking on the right allowed for all GTUs; they stay on the same lane (e.g., bicycles).
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class SameLaneRight implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "SameLaneRight []";
        }
    }

    /**
     * Overtaking on both sides allowed for all GTUs; they stay on the same lane (e.g., pedestrians).
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class SameLaneBoth implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "SameLaneBoth []";
        }
    }

    /**
     * Overtaking on the left allowed for all GTUs, and overtaking on the right allowed under a given speed.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class LeftAlwaysRightSpeed implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed rightOvertakingSpeedMax;

        /**
         * @param rightOvertakingSpeedMax Speed; the speed under which overtaking on the "wrong" side is allowed
         */
        public LeftAlwaysRightSpeed(final Speed rightOvertakingSpeedMax)
        {
            this.rightOvertakingSpeedMax = rightOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftAlwaysRightSpeed [rightOvertakingSpeedMax=" + this.rightOvertakingSpeedMax + "]";
        }
    }

    /**
     * Overtaking on the left allowed for all GTUs, and overtaking on the right allowed when there is a traffic jam.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class LeftAlwaysRightJam implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftAlwaysRightJam []";
        }
    }

    /**
     * Overtaking on the right allowed for all GTUs, and overtaking on the left allowed under a given speed.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class RightAlwaysLeftSpeed implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed leftOvertakingSpeedMax;

        /**
         * @param leftOvertakingSpeedMax Speed; the speed under which overtaking on the "wrong" side is allowed
         */
        public RightAlwaysLeftSpeed(final Speed leftOvertakingSpeedMax)
        {
            this.leftOvertakingSpeedMax = leftOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightAlwaysLeftSpeed [leftOvertakingSpeedMax=" + this.leftOvertakingSpeedMax + "]";
        }
    }

    /**
     * Overtaking on the right allowed for all GTUs, and overtaking on the left allowed when there is a traffic jam.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class RightAlwaysLeftJam implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightAlwaysLeftJam []";
        }
    }

    /**
     * Provide a collection of GTUs that can overtake another collection of GTUs on the left side, but not vice versa. Example:
     * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE, SCOOTER}.
     * In the latter case, cars, trucks and busses can overtake all other GTUs, but bicycles and scooters cannot overtake cars,
     * trucks or busses. Another example is a lane where cars and motors can overtake all other road users, but trucks are not
     * allowed to overtake. In that case, we would allow {CAR, MOTOR} to overtake {ALL} or {CAR, MOTOR} to overtake {CAR, MOTOR,
     * TRUCK} in that lane.<br>
     * TODO: All these "Right/LeftSet" classes should probably use Compatibility instead of full sets.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class LeftSet implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUTypes;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUTypes;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}.
         * @param overtakingGTUTypes Collection&lt;GTUType&gt;; the GTUs that can overtake a set of other GTUs, e.g., CAR,
         *            TRUCK.
         * @param overtakenGTUTYpes Collection&lt;GTUType&gt;; the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER.
         */
        public LeftSet(final Collection<GTUType> overtakingGTUTypes, final Collection<GTUType> overtakenGTUTYpes)
        {
            this.overtakingGTUTypes = overtakingGTUTypes;
            this.overtakenGTUTypes = overtakenGTUTYpes;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftSet [overtakingGTUTypes=" + this.overtakingGTUTypes + ", overtakenGTUTypes=" + this.overtakenGTUTypes
                    + "]";
        }
    }

    /**
     * Provide a collection of GTUs that can overtake another collection of GTUs on the right side, but not vice versa. Example:
     * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE, SCOOTER}.
     * In the latter case, cars, trucks and busses can overtake all other GTUs, but bicycles and scooters cannot overtake cars,
     * trucks or busses. Another example is a lane where cars and motors can overtake all other road users, but trucks are not
     * allowed to overtake. In that case, we would allow {CAR, MOTOR} to overtake {ALL} or {CAR, MOTOR} to overtake {CAR, MOTOR,
     * TRUCK} in that lane.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class RightSet implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUTypes;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUTypes;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the right, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}.
         * @param overtakingGTUs Collection&lt;GTUType&gt;; the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK.
         * @param overtakenGTUs Collection&lt;GTUType&gt;; the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER.
         */
        public RightSet(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs)
        {
            this.overtakingGTUTypes = overtakingGTUs;
            this.overtakenGTUTypes = overtakenGTUs;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightSet [overtakingGTUTypes=" + this.overtakingGTUTypes + ", overtakenGTUTypes=" + this.overtakenGTUTypes
                    + "]";
        }
    }

    /**
     * Provide a collection of GTUs that can overtake another collection of GTUs on the left side, but not vice versa. Example:
     * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE, SCOOTER}.
     * In the latter case, cars, trucks and busses can overtake all other GTUs, but bicycles and scooters cannot overtake cars,
     * trucks or busses. Another example is a lane where cars and motors can overtake all other road users, but trucks are not
     * allowed to overtake. In that case, we would allow {CAR, MOTOR} to overtake {ALL} or {CAR, MOTOR} to overtake {CAR, MOTOR,
     * TRUCK} in that lane. In addition, overtaking on the other side is allowed under a given driving speed.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class LeftSetRightSpeed implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUTypes;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUTypes;

        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed rightOvertakingSpeedMax;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}. In addition, overtaking on the other side is allowed under a
         * given driving speed.
         * @param overtakingGTUs Collection&lt;GTUType&gt;; the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK.
         * @param overtakenGTUs Collection&lt;GTUType&gt;; the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER.
         * @param rightOvertakingSpeedMax Speed; the speed under which overtaking on the "wrong" side is allowed
         */
        public LeftSetRightSpeed(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs,
                final Speed rightOvertakingSpeedMax)
        {
            this.overtakingGTUTypes = overtakingGTUs;
            this.overtakenGTUTypes = overtakenGTUs;
            this.rightOvertakingSpeedMax = rightOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftSetRightSpeed [overtakingGTUTypes=" + this.overtakingGTUTypes + ", overtakenGTUTypes="
                    + this.overtakenGTUTypes + ", rightOvertakingSpeedMax=" + this.rightOvertakingSpeedMax + "]";
        }
    }

    /**
     * Provide a collection of GTUs that can overtake another collection of GTUs on the left side, but not vice versa. Example:
     * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE, SCOOTER}.
     * In the latter case, cars, trucks and busses can overtake all other GTUs, but bicycles and scooters cannot overtake cars,
     * trucks or busses. Another example is a lane where cars and motors can overtake all other road users, but trucks are not
     * allowed to overtake. In that case, we would allow {CAR, MOTOR} to overtake {ALL} or {CAR, MOTOR} to overtake {CAR, MOTOR,
     * TRUCK} in that lane. In addition, overtaking on the other side is allowed when there is a traffic jam.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class LeftSetRightJam implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUTypes;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUTypes;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}. In addition, overtaking on the other side is allowed when
         * there is a traffic jam.
         * @param overtakingGTUs Collection&lt;GTUType&gt;; the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK.
         * @param overtakenGTUs Collection&lt;GTUType&gt;; the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER.
         */
        public LeftSetRightJam(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs)
        {
            this.overtakingGTUTypes = overtakingGTUs;
            this.overtakenGTUTypes = overtakenGTUs;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftSetRightSpeed [overtakingGTUTypes=" + this.overtakingGTUTypes + ", overtakenGTUTypes="
                    + this.overtakenGTUTypes + "]";
        }
    }

    /**
     * Provide a collection of GTUs that can overtake another collection of GTUs on the right side, but not vice versa. Example:
     * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE, SCOOTER}.
     * In the latter case, cars, trucks and busses can overtake all other GTUs, but bicycles and scooters cannot overtake cars,
     * trucks or busses. Another example is a lane where cars and motors can overtake all other road users, but trucks are not
     * allowed to overtake. In that case, we would allow {CAR, MOTOR} to overtake {ALL} or {CAR, MOTOR} to overtake {CAR, MOTOR,
     * TRUCK} in that lane. In addition, overtaking on the other side is allowed under a given driving speed.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class RightSetLeftSpeed implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUTypes;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUTypes;

        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed leftOvertakingSpeedMax;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}. In addition, overtaking on the other side is allowed under a
         * given driving speed.
         * @param overtakingGTUs Collection&lt;GTUType&gt;; the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK.
         * @param overtakenGTUs Collection&lt;GTUType&gt;; the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER.
         * @param leftOvertakingSpeedMax Speed; the speed under which overtaking on the "wrong" side is allowed
         */
        public RightSetLeftSpeed(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs,
                final Speed leftOvertakingSpeedMax)
        {
            this.overtakingGTUTypes = overtakingGTUs;
            this.overtakenGTUTypes = overtakenGTUs;
            this.leftOvertakingSpeedMax = leftOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightSetLeftSpeed [overtakingGTUTypes=" + this.overtakingGTUTypes + ", overtakenGTUTypes="
                    + this.overtakenGTUTypes + ", leftOvertakingSpeedMax=" + this.leftOvertakingSpeedMax + "]";
        }
    }

    /**
     * Provide a collection of GTUs that can overtake another collection of GTUs on the right side, but not vice versa. Example:
     * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE, SCOOTER}.
     * In the latter case, cars, trucks and busses can overtake all other GTUs, but bicycles and scooters cannot overtake cars,
     * trucks or busses. Another example is a lane where cars and motors can overtake all other road users, but trucks are not
     * allowed to overtake. In that case, we would allow {CAR, MOTOR} to overtake {ALL} or {CAR, MOTOR} to overtake {CAR, MOTOR,
     * TRUCK} in that lane. In addition, overtaking on the other side is allowed when there is a traffic jam.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    class RightSetLeftJam implements OvertakingConditions, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150913L;

        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUTypes;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUTypes;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}. In addition, overtaking on the other side is allowed when
         * there is a traffic jam.
         * @param overtakingGTUs Collection&lt;GTUType&gt;; the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK.
         * @param overtakenGTUs Collection&lt;GTUType&gt;; the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER.
         */
        public RightSetLeftJam(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs)
        {
            this.overtakingGTUTypes = overtakingGTUs;
            this.overtakenGTUTypes = overtakenGTUs;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightSetLeftJam [overtakingGTUTypes=" + this.overtakingGTUTypes + ", overtakenGTUTypes="
                    + this.overtakenGTUTypes + "]";
        }
    }

}
