/**
 *
 */
package org.opentrafficsim.road.network.factory.shape;

import org.locationtech.jts.geom.Geometry;
import org.opentrafficsim.core.network.OTSNode;

/**
 * @author P070518
 */
public abstract class AbstractNWBRoadElement
{

    // the geometry
    private Geometry myGeom;

    private OTSNode startNode;

    private OTSNode endNode;

    // the unique NWB wegvak ID
    private String roadId;

    // relative distance at begin of link
    private Double beginDistance;

    // relative distance at end of link
    private Double endDistance;

    /**
     * @param myGeom Geometry;
     * @param startNode OTSNode;
     * @param endNode OTSNode;
     * @param roadId String;
     * @param beginDistance Double;
     * @param endDistance Double;
     */
    public AbstractNWBRoadElement(Geometry myGeom, OTSNode startNode, OTSNode endNode, String roadId, Double beginDistance,
            Double endDistance)
    {
        super();
        this.myGeom = myGeom;
        this.startNode = startNode;
        this.endNode = endNode;
        this.roadId = roadId;
        this.beginDistance = beginDistance;
        this.endDistance = endDistance;
    }

    public Geometry getMyGeom()
    {
        return myGeom;
    }

    public OTSNode getStartNode()
    {
        return startNode;
    }

    public OTSNode getEndNode()
    {
        return endNode;
    }

    public String getRoadId()
    {
        return roadId;
    }

    public Double getBeginDistance()
    {
        return beginDistance;
    }

    public Double getEndDistance()
    {
        return endDistance;
    }

}
