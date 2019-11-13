/**
 *
 */
package org.opentrafficsim.road.network.factory.shape;

import org.locationtech.jts.geom.Geometry;
import org.opentrafficsim.core.network.OTSNode;

/**
 * @author P070518
 */
public class NWBSpecialLane extends AbstractNWBRoadElement
{

    private String laneType;

    // number of driving lanes
    private Integer numberOfLanes;

    // kantCode: "H" of "T" generally shoukld equal the drivingDirection of the corresponding NWB link
    private String sideCode;

    /**
     * Specific lane types such as on-ramp and off-ramp or lanes at weaving area
     * @param myGeom Geometry;
     * @param startNode OTSNode;
     * @param endNode OTSNode;
     * @param roadId String;
     * @param beginDistance Double;
     * @param endDistance Double;
     * @param laneType String;
     * @param numberOfLanes Integer;
     * @param sideCode String;
     */
    public NWBSpecialLane(Geometry myGeom, OTSNode startNode, OTSNode endNode, String roadId, Double beginDistance,
            Double endDistance, String laneType, Integer numberOfLanes, String sideCode)
    {
        super(myGeom, startNode, endNode, roadId, beginDistance, endDistance);
        this.laneType = laneType;
        this.numberOfLanes = numberOfLanes;
        this.sideCode = sideCode;
    }

    public String getLaneType()
    {
        return laneType;
    }

    public Integer getNumberOfLanes()
    {
        return numberOfLanes;
    }

    public String getSideCode()
    {
        return sideCode;
    }

}
