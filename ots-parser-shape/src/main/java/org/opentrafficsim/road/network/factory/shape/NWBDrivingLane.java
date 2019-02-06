/**
 *
 */
package org.opentrafficsim.road.network.factory.shape;

import org.locationtech.jts.geom.Geometry;
import org.opentrafficsim.core.network.OTSNode;

/**
 * @author P070518
 */
public class NWBDrivingLane extends AbstractNWBRoadElement
{

    // number of driving lanes at start of link
    private Integer startNumberOfLanes;

    // number of driving lanes at end of link
    private Integer endNumberOfLanes;

    // kantCode: "H" of "T" generally shoukld equal the drivingDirection of the corresponding NWB link
    private String sideCode;

    /**
     * @param myGeom Geometry;
     * @param startNode OTSNode;
     * @param endNode OTSNode;
     * @param roadId String;
     * @param beginDistance Double;
     * @param endDistance Double;
     * @param startNumberOfLanes Integer;
     * @param endNumberOfLanes Integer;
     * @param sideCode String;
     */
    public NWBDrivingLane(Geometry myGeom, OTSNode startNode, OTSNode endNode, String roadId, Double beginDistance,
            Double endDistance, Integer startNumberOfLanes, Integer endNumberOfLanes, String sideCode)
    {
        super(myGeom, startNode, endNode, roadId, beginDistance, endDistance);
        this.startNumberOfLanes = startNumberOfLanes;
        this.endNumberOfLanes = endNumberOfLanes;
        this.sideCode = sideCode;
    }

    public Integer getStartNumberOfLanes()
    {
        return startNumberOfLanes;
    }

    public Integer getEndNumberOfLanes()
    {
        return endNumberOfLanes;
    }

    public String getSideCode()
    {
        return sideCode;
    }

}
