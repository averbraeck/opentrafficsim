/**
 *
 */
package org.opentrafficsim.road.network.factory.shape;

import org.opentrafficsim.core.network.OTSNode;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author P070518
 */
public class NWBSpecialLane extends AbstractNWBRoadElement {

    private String laneType;

    // number of driving lanes
    private Integer numberOfLanes;

    // kantCode: "H" of "T" generally shoukld equal the drivingDirection of the corresponding NWB link
    private String sideCode;

    /**
     * Specific lane types such as on-ramp and off-ramp or lanes at weaving area
     * @param myGeom
     * @param startNode
     * @param endNode
     * @param roadId
     * @param beginDistance
     * @param endDistance
     * @param laneType
     * @param numberOfLanes
     * @param sideCode
     */
    public NWBSpecialLane(Geometry myGeom, OTSNode startNode, OTSNode endNode, String roadId, Double beginDistance,
        Double endDistance, String laneType, Integer numberOfLanes, String sideCode) {
        super(myGeom, startNode, endNode, roadId, beginDistance, endDistance);
        this.laneType = laneType;
        this.numberOfLanes = numberOfLanes;
        this.sideCode = sideCode;
    }

    public String getLaneType() {
        return laneType;
    }

    public Integer getNumberOfLanes() {
        return numberOfLanes;
    }

    public String getSideCode() {
        return sideCode;
    }

}
