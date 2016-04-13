package org.opentrafficsim.road.network.lane.changing;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

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
 * <b>Note:</b> The class only checks whether it is <b>allowed</b> to overtake another GTU on this road, not whether it is
 * possible or safe to do so. That has to be checked by the GTU itself based on e.g., gap acceptance.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 13, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface OvertakingConditions
{
    /**
     * Implementation of the overtaking conditions. E.g., is a car allowed on this road to overtake a tractor? If so, on which
     * side(s)?
     * @param lane the lane for which to evaluate the overtaking conditions
     * @param gtu the GTU that might overtake another GTU
     * @param predecessorGTU the GTU in front of the GTU that might want to overtake
     * @return an overtaking direction: LEFT, RIGHT, BOTH or NONE
     */
    OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu, final LaneBasedGTU predecessorGTU);

    /********************************************************************************************************************/
    /********************** IMPLEMENTATION CLASSES OF MOST COMMON OVERTAKING CONDITIONS *****************************/
    /********************************************************************************************************************/

    /**
     * Overtaking on the left allowed for all GTUs. Note: overtaking on the right not allowed, so vehicles will stall on a
     * multilane road near a traffic light. Also, bicycles will overtake cars on the "wrong" side of the road in this simple
     * condition!
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class LeftOnly implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            return OvertakingDirection.LEFT;
        }

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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class RightOnly implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            return OvertakingDirection.RIGHT;
        }

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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class None implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            return OvertakingDirection.NONE;
        }

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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class LeftAndRight implements OvertakingConditions
    {
        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            return OvertakingDirection.BOTH;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftAndRight []";
        }
    }

    /**
     * Overtaking on the left allowed for all GTUs, and overtaking on the right allowed under a given speed.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class LeftAlwaysRightSpeed implements OvertakingConditions
    {
        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed rightOvertakingSpeedMax;

        /**
         * @param rightOvertakingSpeedMax the speed under which overtaking on the "wrong" side is allowed
         */
        public LeftAlwaysRightSpeed(final Speed rightOvertakingSpeedMax)
        {
            this.rightOvertakingSpeedMax = rightOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            return gtu.getVelocity().lt(this.rightOvertakingSpeedMax) ? OvertakingDirection.BOTH
                : OvertakingDirection.LEFT;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftAlwaysRightSpeed [rightOvertakingSpeedMax=" + this.rightOvertakingSpeedMax + "]";
        }
    }

    /**
     * Overtaking on the right allowed for all GTUs, and overtaking on the left allowed under a given speed.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class RightAlwaysLeftSpeed implements OvertakingConditions
    {
        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed leftOvertakingSpeedMax;

        /**
         * @param leftOvertakingSpeedMax the speed under which overtaking on the "wrong" side is allowed
         */
        public RightAlwaysLeftSpeed(final Speed leftOvertakingSpeedMax)
        {
            this.leftOvertakingSpeedMax = leftOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            return gtu.getVelocity().lt(this.leftOvertakingSpeedMax) ? OvertakingDirection.BOTH
                : OvertakingDirection.RIGHT;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightAlwaysLeftSpeed [leftOvertakingSpeedMax=" + this.leftOvertakingSpeedMax + "]";
        }
    }

    /**
     * Provide a collection of GTUs that can overtake another collection of GTUs on the left side, but not vice versa. Example:
     * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE, SCOOTER}.
     * In the latter case, cars, trucks and busses can overtake all other GTUs, but bicycles and scooters cannot overtake cars,
     * trucks or busses. Another example is a lane where cars and motors can overtake all other road users, but trucks are not
     * allowed to overtake. In that case, we would allow {CAR, MOTOR} to overtake {ALL} or {CAR, MOTOR} to overtake {CAR, MOTOR,
     * TRUCK} in that lane.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class LeftSet implements OvertakingConditions
    {
        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUs;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUs;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}.
         * @param overtakingGTUs the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK. If overtakingGTUs contains
         *            GTUType.ALL, all GTUs can overtake.
         * @param overtakenGTUs the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER. If overtakenGTUs contains GTUType.ALL,
         *            all GTUs can be overtaken.
         */
        public LeftSet(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs)
        {
            this.overtakingGTUs = overtakingGTUs;
            this.overtakenGTUs = overtakenGTUs;
        }

        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            if ((this.overtakingGTUs.contains(GTUType.ALL) || this.overtakingGTUs.contains(gtu.getGTUType())
                && (this.overtakenGTUs.contains(GTUType.ALL) || this.overtakenGTUs
                    .contains(predecessorGTU.getGTUType()))))
            {
                return OvertakingDirection.LEFT;
            }
            return OvertakingDirection.NONE;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftSet [overtakingGTUs=" + this.overtakingGTUs + ", overtakenGTUs=" + this.overtakenGTUs + "]";
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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class RightSet implements OvertakingConditions
    {
        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUs;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUs;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the right, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}.
         * @param overtakingGTUs the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK. If overtakingGTUs contains
         *            GTUType.ALL, all GTUs can overtake.
         * @param overtakenGTUs the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER. If overtakenGTUs contains GTUType.ALL,
         *            all GTUs can be overtaken.
         */
        public RightSet(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs)
        {
            this.overtakingGTUs = overtakingGTUs;
            this.overtakenGTUs = overtakenGTUs;
        }

        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            if ((this.overtakingGTUs.contains(GTUType.ALL) || this.overtakingGTUs.contains(gtu.getGTUType())
                && (this.overtakenGTUs.contains(GTUType.ALL) || this.overtakenGTUs
                    .contains(predecessorGTU.getGTUType()))))
            {
                return OvertakingDirection.RIGHT;
            }
            return OvertakingDirection.NONE;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightSet [overtakingGTUs=" + this.overtakingGTUs + ", overtakenGTUs=" + this.overtakenGTUs + "]";
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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class LeftSetRightSpeed implements OvertakingConditions
    {
        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUs;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUs;

        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed rightOvertakingSpeedMax;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}. In addition, overtaking on the other side is allowed under a
         * given driving speed.
         * @param overtakingGTUs the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK. If overtakingGTUs contains
         *            GTUType.ALL, all GTUs can overtake.
         * @param overtakenGTUs the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER. If overtakenGTUs contains GTUType.ALL,
         *            all GTUs can be overtaken.
         * @param rightOvertakingSpeedMax the speed under which overtaking on the "wrong" side is allowed
         */
        public LeftSetRightSpeed(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs,
            final Speed rightOvertakingSpeedMax)
        {
            this.overtakingGTUs = overtakingGTUs;
            this.overtakenGTUs = overtakenGTUs;
            this.rightOvertakingSpeedMax = rightOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            boolean left =
                ((this.overtakingGTUs.contains(GTUType.ALL) || this.overtakingGTUs.contains(gtu.getGTUType())
                    && (this.overtakenGTUs.contains(GTUType.ALL) || this.overtakenGTUs.contains(predecessorGTU
                        .getGTUType()))));
            boolean right = gtu.getVelocity().lt(this.rightOvertakingSpeedMax);
            if (left)
            {
                return right ? OvertakingDirection.BOTH : OvertakingDirection.LEFT;
            }
            return right ? OvertakingDirection.RIGHT : OvertakingDirection.NONE;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LeftSetRightSpeed [overtakingGTUs=" + this.overtakingGTUs + ", overtakenGTUs=" + this.overtakenGTUs
                    + ", rightOvertakingSpeedMax=" + this.rightOvertakingSpeedMax + "]";
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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 13, 2015
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public static class RightSetLeftSpeed implements OvertakingConditions
    {
        /** A collection of GTUs that can overtake another collection of GTUs. */
        private final Collection<GTUType> overtakingGTUs;

        /** A collection of GTUs that can be overtaken by another collection of GTUs. */
        private final Collection<GTUType> overtakenGTUs;

        /** The speed under which overtaking on the "wrong" side is allowed. */
        private final Speed leftOvertakingSpeedMax;

        /**
         * Provide a collection of GTUs that can overtake another collection of GTUs on the left, but not vice versa. Example:
         * {CAR, TRUCK, BUS} can overtake {BICYCLE, SCOOTER}, or {CAR, TRUCK, BUS} can overtake {CAR, TRUCK, BUS, BICYCLE,
         * SCOOTER}, or {CAR, TRUCK, BUS} can overtake {TRACTOR}. In addition, overtaking on the other side is allowed under a
         * given driving speed.
         * @param overtakingGTUs the GTUs that can overtake a set of other GTUs, e.g., CAR, TRUCK. If overtakingGTUs contains
         *            GTUType.ALL, all GTUs can overtake.
         * @param overtakenGTUs the GTUs that can be overtaken, e.g., BICYCLE, SCOOTER. If overtakenGTUs contains GTUType.ALL,
         *            all GTUs can be overtaken.
         * @param leftOvertakingSpeedMax the speed under which overtaking on the "wrong" side is allowed
         */
        public RightSetLeftSpeed(final Collection<GTUType> overtakingGTUs, final Collection<GTUType> overtakenGTUs,
            final Speed leftOvertakingSpeedMax)
        {
            this.overtakingGTUs = overtakingGTUs;
            this.overtakenGTUs = overtakenGTUs;
            this.leftOvertakingSpeedMax = leftOvertakingSpeedMax;
        }

        /** {@inheritDoc} */
        @Override
        public final OvertakingDirection checkOvertaking(final Lane lane, final LaneBasedGTU gtu,
            final LaneBasedGTU predecessorGTU)
        {
            boolean right =
                ((this.overtakingGTUs.contains(GTUType.ALL) || this.overtakingGTUs.contains(gtu.getGTUType())
                    && (this.overtakenGTUs.contains(GTUType.ALL) || this.overtakenGTUs.contains(predecessorGTU
                        .getGTUType()))));
            boolean left = gtu.getVelocity().lt(this.leftOvertakingSpeedMax);
            if (right)
            {
                return left ? OvertakingDirection.BOTH : OvertakingDirection.RIGHT;
            }
            return left ? OvertakingDirection.LEFT : OvertakingDirection.NONE;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RightSetLeftSpeed [overtakingGTUs=" + this.overtakingGTUs + ", overtakenGTUs=" + this.overtakenGTUs
                    + ", leftOvertakingSpeedMax=" + this.leftOvertakingSpeedMax + "]";
        }
    }

}
