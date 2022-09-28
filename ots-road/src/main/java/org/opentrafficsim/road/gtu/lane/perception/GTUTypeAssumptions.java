package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.lane.LaneType;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTypeAssumptions implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160527L;

    /** stored car following model of the observed GTU. */
    private final Map<GtuType, CarFollowingModel> carFollowingModelMap = new LinkedHashMap<>();

    /** stored parameters of the observed GTU. */
    private final Map<GtuType, Parameters> parametersMap = new LinkedHashMap<>();

    /** stored speed limit info of the observed GTU. */
    private final Map<GtuType, Map<LaneType, Speed>> laneTypeSpeedMap = new LinkedHashMap<>();

    /**
     * Set the car following model for a certain GtuType as an assumption for that GtuType.
     * @param gtuType GtuType; the GtuType to set the model for
     * @param carFollowingModel CarFollowingModel; the model to set for the GtuType
     */
    public final void setCarFollowingModel(final GtuType gtuType, final CarFollowingModel carFollowingModel)
    {
        Throw.whenNull(gtuType, "gtuType cannot be null");
        Throw.whenNull(carFollowingModel, "carFollowingModel cannot be null");
        this.carFollowingModelMap.put(gtuType, carFollowingModel);
    }

    /**
     * Set the parameters for a certain GtuType as an assumption for that GtuType.
     * @param gtuType GtuType; the GtuType to set the model for
     * @param parameters Parameters; the model to set for the GtuType
     */
    public final void setParameters(final GtuType gtuType, final Parameters parameters)
    {
        Throw.whenNull(gtuType, "gtuType cannot be null");
        Throw.whenNull(parameters, "parameters cannot be null");
        this.parametersMap.put(gtuType, parameters);
    }

    /**
     * Set the maximum speed for a certain GtuType on a certain LaneType as an assumption for that GtuType.
     * @param gtuType GtuType; the GtuType to set the model for
     * @param laneType LaneType; the laneType to set the speed for
     * @param maxSpeed Speed; the maximum speed on the laneType for the given GtuType
     */
    public final void setLaneTypeMaxSpeed(final GtuType gtuType, final LaneType laneType, final Speed maxSpeed)
    {
        Throw.whenNull(gtuType, "gtuType cannot be null");
        Throw.whenNull(laneType, "laneType cannot be null");
        Throw.whenNull(maxSpeed, "maxSpeed cannot be null");
        Map<LaneType, Speed> maxLaneTypeSpeed = this.laneTypeSpeedMap.get(gtuType);
        if (maxLaneTypeSpeed == null)
        {
            maxLaneTypeSpeed = new LinkedHashMap<>();
            this.laneTypeSpeedMap.put(gtuType, maxLaneTypeSpeed);
        }
        maxLaneTypeSpeed.put(laneType, maxSpeed);
    }

    /**
     * Return the car following model for a certain GtuType as an assumption for that GtuType.
     * @param gtuType GtuType; the GtuType to get the model for
     * @return the car following model for the GtuType, or <b>null</b> when there is no information for the gtuType
     */
    public final CarFollowingModel getCarFollowingModel(final GtuType gtuType)
    {
        return this.carFollowingModelMap.get(gtuType);
    }

    /**
     * Return the parameters model for a certain GtuType as an assumption for that GtuType.
     * @param gtuType GtuType; the GtuType to get the model for
     * @return the parameters for the GtuType, or <b>null</b> when there is no information for the gtuType
     */
    public final Parameters getParameters(final GtuType gtuType)
    {
        return this.parametersMap.get(gtuType);
    }

    /**
     * Return the maximum speed on a LaneType for a certain GtuType as an assumption for that GtuType.
     * @param gtuType GtuType; the GtuType to get the maximum speed for
     * @param laneType LaneType; the LaneType to get the maximum speed for
     * @return the maximum speed for the GtuType on the LaneType, or <b>null</b> when there is no information for the
     *         combination of gtuType and laneType
     */
    public final Speed getLaneTypeMaxSpeed(final GtuType gtuType, final LaneType laneType)
    {
        if (!this.laneTypeSpeedMap.containsKey(gtuType))
        {
            return null;
        }
        return this.laneTypeSpeedMap.get(gtuType).get(laneType);
    }

    /**
     * Return a safe copy of the maximum speed for all LaneTypes for a certain GtuType as an assumption for that GtuType.
     * @param gtuType GtuType; the GtuType to get the maximum speed for
     * @return a map with a safe copy of the maximum speed for the GtuType on all LaneTypes, or <b>null</b> when there is no
     *         information for the gtuType
     */
    public final Map<LaneType, Speed> getMaxSpeeds(final GtuType gtuType)
    {
        if (!this.laneTypeSpeedMap.containsKey(gtuType))
        {
            return null;
        }
        Map<LaneType, Speed> maxSpeeds = new LinkedHashMap<>();
        maxSpeeds.putAll(this.laneTypeSpeedMap.get(gtuType));
        return maxSpeeds;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.parametersMap == null) ? 0 : this.parametersMap.hashCode());
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
        if (this.parametersMap == null)
        {
            if (other.parametersMap != null)
                return false;
        }
        else if (!this.parametersMap.equals(other.parametersMap))
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
        return "GTUTypeAssumptions [carFollowingModelMap=" + this.carFollowingModelMap + ", parametersMap=" + this.parametersMap
                + ", laneTypeSpeedMap=" + this.laneTypeSpeedMap + "]";
    }

}
