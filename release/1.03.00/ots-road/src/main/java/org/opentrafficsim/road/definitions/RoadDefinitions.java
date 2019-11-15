package org.opentrafficsim.road.definitions;

import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.road.network.lane.LaneType;

/**
 * The RoadDefinitions interface contains access to the core definitions that can be used to interpret the RoadNetwork and the
 * RoadPerceivableContext. An example interface allows for the retrieval of LaneTypes.<br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public interface RoadDefinitions extends Definitions
{
    /***************************************************************************************/
    /************************************** LaneTypes **************************************/
    /***************************************************************************************/

    /**
     * Add the default LaneTypes that have been defined in the enum LaneType.DEFAULTS to the network. It is not necessary to
     * call this method on every network; when the LaneTypes are for instance defined in an XML file, adding the default types
     * might not be needed.
     */
    void addDefaultLaneTypes();

    /**
     * Add a Lane type to the map. This method is automatically called from the LaneType constructor.
     * @param laneType the LaneType to add
     */
    void addLaneType(LaneType laneType);

    /**
     * Retrieve a defined LaneType based on its id.
     * @param laneTypeId the id to search for
     * @return the LaneType or null in case it could not be found
     */
    LaneType getLaneType(String laneTypeId);

    /**
     * Retrieve a defined default LaneType based on its enum.
     * @param laneTypeEnum the enum to search for
     * @return the LaneType or null in case it could not be found
     */
    LaneType getLaneType(LaneType.DEFAULTS laneTypeEnum);

    /**
     * Retrieve a safe copy of the map of defined LaneTypes in this network.
     * @return the map of defined LaneTypes
     */
    ImmutableMap<String, LaneType> getLaneTypes();
}
