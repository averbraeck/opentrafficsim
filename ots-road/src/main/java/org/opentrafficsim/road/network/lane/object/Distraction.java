package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Distraction following a distance profile.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Distraction extends AbstractLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20180405L;

    /** Distraction profile. */
    private final DistractionProfile profile;

    /**
     * @param id String; id
     * @param lane Lane; lane
     * @param longitudinalPosition Length; longitudinal position
     * @param simulator OTSSimulatorInterface; simulator
     * @param profile DistractionProfile; distraction profile
     * @throws NetworkException on network exception
     */
    public Distraction(final String id, final Lane lane, final Length longitudinalPosition,
            final OTSSimulatorInterface simulator, final DistractionProfile profile) throws NetworkException
    {
        super(id, lane, LongitudinalDirectionality.DIR_PLUS, longitudinalPosition,
                LaneBasedObject.makeGeometry(lane, longitudinalPosition), Length.ZERO);
        this.profile = profile;

        init();
    }

    /** {@inheritDoc} */
    @Override
    public AbstractLaneBasedObject clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator)
            throws NetworkException
    {
        return new Distraction(getId(), (Lane) newCSE, getLongitudinalPosition(), newSimulator, this.profile);
    }

    /**
     * Returns the level of distraction at the given distance.
     * @param distance Distance to distraction; negative when approaching
     * @return Double; level of distraction (task-demand), or {@code null} if the distraction is no longer important
     */
    public Double getDistraction(final Length distance)
    {
        return this.profile.getDistraction(distance);
    }

    /**
     * Describes the profile around the distraction.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    @FunctionalInterface
    public interface DistractionProfile
    {
        /**
         * Returns the level of distraction at the given distance.
         * @param distance Distance to distraction; negative when approaching
         * @return Double; level of distraction (task-demand), or {@code null} if the distraction is no longer important
         */
        Double getDistraction(Length distance);
    }

    /**
     * Distraction profile with trapezoid shape. The constant part is from the location of the distraction downstream.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class TrapezoidProfile implements DistractionProfile
    {
        /** Maximum distraction. */
        private final double maxDistraction;

        /** Distance before distraction where distraction starts to have effect. */
        private final Length dMin;

        /** Distance beyond distraction where distraction has maximum effect. */
        private final Length dMed;

        /** Distance beyond distraction where distraction no longer has effect. */
        private final Length dMax;

        /**
         * @param maxDistraction double; maximum distraction (task-demand)
         * @param dMin Length; distance before distraction where distraction starts to have effect (&lt;0)
         * @param dMed Length; distance beyond distraction where distraction has maximum effect (&gt;0)
         * @param dMax Length; distance beyond distraction where distraction no longer has effect (&gt;dMed)
         */
        public TrapezoidProfile(final double maxDistraction, final Length dMin, final Length dMed, final Length dMax)
        {
            Throw.when(dMin.si > 0 || dMed.si < 0 || dMax.si < dMed.si, IllegalArgumentException.class,
                    "dMin < 0 < dMed < dMax does not hold");
            Throw.when(maxDistraction < 0 || maxDistraction > 1, IllegalArgumentException.class,
                    "0 <= maxDistraction <= 1 does not hold");
            this.maxDistraction = maxDistraction;
            this.dMin = dMin;
            this.dMed = dMed;
            this.dMax = dMax;
        }

        /** {@inheritDoc} */
        @Override
        public Double getDistraction(final Length distance)
        {
            if (distance.si < this.dMin.si)
            {
                // before scope
                return 0.0;
            }
            else if (distance.si < 0)
            {
                // increasing distraction on approach
                return this.maxDistraction * (1.0 - distance.si / this.dMin.si);
            }
            else if (distance.si < this.dMed.si)
            {
                // max distraction at location (defined over a distance dMed)
                return this.maxDistraction;
            }
            else if (distance.si < this.dMax.si)
            {
                // reducing distraction beyond location
                return this.maxDistraction * (1.0 - (distance.si - this.dMed.si) / (this.dMax.si - this.dMed.si));
            }
            else
            {
                // beyond scope
                return null;
            }
        }
    }

}
