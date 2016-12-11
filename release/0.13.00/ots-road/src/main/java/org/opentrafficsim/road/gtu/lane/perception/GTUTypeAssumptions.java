package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 27, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTypeAssumptions implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160527L;

    /** stored car following model of the observed GTU. */
    private final Map<GTUType, CarFollowingModel> carFollowingModelMap = new HashMap<>();

    /** stored behavioral characteristics of the observed GTU. */
    private final Map<GTUType, BehavioralCharacteristics> behavioralCharacteristicsMap = new HashMap<>();

    /** stored speed limit info of the observed GTU. */
    private final Map<GTUType, Map<LaneType, Speed>> laneTypeSpeedMap = new HashMap<>();

    /**
     * Set the car following model for a certain GTUType as an assumption for that GTUType.
     * @param gtuType the GTUType to set the model for
     * @param carFollowingModel the model to set for the GTUType
     */
    public final void setCarFollowingModel(final GTUType gtuType, final CarFollowingModel carFollowingModel)
    {
        Throw.whenNull(gtuType, "gtuType cannot be null");
        Throw.whenNull(carFollowingModel, "carFollowingModel cannot be null");
        this.carFollowingModelMap.put(gtuType, carFollowingModel);
    }

    /**
     * Set the behavioral characteristics for a certain GTUType as an assumption for that GTUType.
     * @param gtuType the GTUType to set the model for
     * @param behavioralCharacteristics the model to set for the GTUType
     */
    public final void setBehavioralCharacteristics(final GTUType gtuType,
            final BehavioralCharacteristics behavioralCharacteristics)
    {
        Throw.whenNull(gtuType, "gtuType cannot be null");
        Throw.whenNull(behavioralCharacteristics, "behavioralCharacteristics cannot be null");
        this.behavioralCharacteristicsMap.put(gtuType, behavioralCharacteristics);
    }

    /**
     * Set the maximum speed for a certain GTUType on a certain LaneType as an assumption for that GTUType.
     * @param gtuType the GTUType to set the model for
     * @param laneType the laneType to set the speed for
     * @param maxSpeed the maximum speed on the laneType for the given GTUType
     */
    public final void setLaneTypeMaxSpeed(final GTUType gtuType, final LaneType laneType, final Speed maxSpeed)
    {
        Throw.whenNull(gtuType, "gtuType cannot be null");
        Throw.whenNull(laneType, "laneType cannot be null");
        Throw.whenNull(maxSpeed, "maxSpeed cannot be null");
        Map<LaneType, Speed> maxLaneTypeSpeed = this.laneTypeSpeedMap.get(gtuType);
        if (maxLaneTypeSpeed == null)
        {
            maxLaneTypeSpeed = new HashMap<>();
            this.laneTypeSpeedMap.put(gtuType, maxLaneTypeSpeed);
        }
        maxLaneTypeSpeed.put(laneType, maxSpeed);
    }

    /**
     * Return the car following model for a certain GTUType as an assumption for that GTUType.
     * @param gtuType the GTUType to get the model for
     * @return the car following model for the GTUType, or <b>null</b> when there is no information for the gtuType
     */
    public final CarFollowingModel getCarFollowingModel(final GTUType gtuType)
    {
        return this.carFollowingModelMap.get(gtuType);
    }

    /**
     * Return the behavioral characteristics model for a certain GTUType as an assumption for that GTUType.
     * @param gtuType the GTUType to get the model for
     * @return the behavioral characteristics for the GTUType, or <b>null</b> when there is no information for the gtuType
     */
    public final BehavioralCharacteristics getBehavioralCharacteristics(final GTUType gtuType)
    {
        return this.behavioralCharacteristicsMap.get(gtuType);
    }

    /**
     * Return the maximum speed on a LaneType for a certain GTUType as an assumption for that GTUType.
     * @param gtuType the GTUType to get the maximum speed for
     * @param laneType the LaneType to get the maximum speed for
     * @return the maximum speed for the GTUType on the LaneType, or <b>null</b> when there is no information for the
     *         combination of gtuType and laneType
     */
    public final Speed getLaneTypeMaxSpeed(final GTUType gtuType, final LaneType laneType)
    {
        if (!this.laneTypeSpeedMap.containsKey(gtuType))
        {
            return null;
        }
        return this.laneTypeSpeedMap.get(gtuType).get(laneType);
    }

    /**
     * Return a safe copy of the maximum speed for all LaneTypes for a certain GTUType as an assumption for that GTUType.
     * @param gtuType the GTUType to get the maximum speed for
     * @return a map with a safe copy of the maximum speed for the GTUType on all LaneTypes, or <b>null</b> when there is no
     *         information for the gtuType
     */
    public final Map<LaneType, Speed> getMaxSpeeds(final GTUType gtuType)
    {
        if (!this.laneTypeSpeedMap.containsKey(gtuType))
        {
            return null;
        }
        Map<LaneType, Speed> maxSpeeds = new HashMap<>();
        maxSpeeds.putAll(this.laneTypeSpeedMap.get(gtuType));
        return maxSpeeds;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((this.behavioralCharacteristicsMap == null) ? 0 : this.behavioralCharacteristicsMap.hashCode());
        result = prime * result + ((this.carFollowingModelMap == null) ? 0 : this.carFollowingModelMap.hashCode());
        result = prime * result + ((this.laneTypeSpeedMap == null) ? 0 : this.laneTypeSpeedMap.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public final boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GTUTypeAssumptions other = (GTUTypeAssumptions) obj;
        if (this.behavioralCharacteristicsMap == null)
        {
            if (other.behavioralCharacteristicsMap != null)
                return false;
        }
        else if (!this.behavioralCharacteristicsMap.equals(other.behavioralCharacteristicsMap))
            return false;
        if (this.carFollowingModelMap == null)
        {
            if (other.carFollowingModelMap != null)
                return false;
        }
        else if (!this.carFollowingModelMap.equals(other.carFollowingModelMap))
            return false;
        if (this.laneTypeSpeedMap == null)
        {
            if (other.laneTypeSpeedMap != null)
                return false;
        }
        else if (!this.laneTypeSpeedMap.equals(other.laneTypeSpeedMap))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUTypeAssumptions [carFollowingModelMap=" + this.carFollowingModelMap + ", behavioralCharacteristicsMap="
                + this.behavioralCharacteristicsMap + ", laneTypeSpeedMap=" + this.laneTypeSpeedMap + "]";
    }

}
