/**
 *
 */
package org.opentrafficsim.road.network.factory.shape;

import org.opentrafficsim.core.network.OTSNode;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author P070518
 */
public class NWBRoadElement extends AbstractNWBRoadElement {

    // the geometry
    // junctionStart
    private String junctionIdBegin;

    // junctionEnd
    private String junctionIdEnd;

    // administrative direction
    private String adminDirection;

    // driving direction
    private String drivingDirection;

    // distance at begin of link
    private Double beginKM;

    // distance at begin of link
    private Double endKM;

    /**
     * A road element from the Dutch NDW road map
     * @param myGeom
     * @param startNode
     * @param endNode
     * @param roadId
     * @param beginDistance
     * @param endDistance
     * @param junctionIdBegin
     * @param junctionIdEnd
     * @param adminDirection
     * @param drivingDirection
     * @param beginKM
     * @param endKM
     */
    public NWBRoadElement(Geometry myGeom, OTSNode startNode, OTSNode endNode, String roadId, Double beginDistance,
        Double endDistance, String junctionIdBegin, String junctionIdEnd, String adminDirection, String drivingDirection,
        Double beginKM, Double endKM) {
        super(myGeom, startNode, endNode, roadId, beginDistance, endDistance);
        this.junctionIdBegin = junctionIdBegin;
        this.junctionIdEnd = junctionIdEnd;
        this.adminDirection = adminDirection;
        this.drivingDirection = drivingDirection;
        this.beginKM = beginKM;
        this.endKM = endKM;
    }

    public String getJunctionIdBegin() {
        return junctionIdBegin;
    }

    public String getJunctionIdEnd() {
        return junctionIdEnd;
    }

    public String getAdminDirection() {
        return adminDirection;
    }

    public String getDrivingDirection() {
        return drivingDirection;
    }

    public Double getBeginKM() {
        return beginKM;
    }

    public Double getEndKM() {
        return endKM;
    }

}
