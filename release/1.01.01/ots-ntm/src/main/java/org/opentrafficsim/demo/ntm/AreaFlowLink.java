package org.opentrafficsim.demo.ntm;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 2 Mar 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class AreaFlowLink extends Area
{
    /** */
    private LinkCellTransmission flowLink;

    /** */
    private int indexCell;

    /**
     * @param geometry Geometry;
     * @param centroidNr String;
     * @param name String;
     * @param gemeente String;
     * @param gebied String;
     * @param regio String;
     * @param dhb double;
     * @param centroid Coordinate;
     * @param trafficBehaviourType TrafficBehaviourType;
     * @param roadLength Length;
     * @param averageSpeed Speed;
     * @param increaseDemandByFactor double;
     * @param parametersNTM ParametersNTM;
     * @param flowLink LinkCellTransmission;
     * @param indexCell int;
     */
    public AreaFlowLink(Geometry geometry, String centroidNr, String name, String gemeente, String gebied, String regio,
            double dhb, Coordinate centroid, TrafficBehaviourType trafficBehaviourType, Length roadLength, Speed averageSpeed,
            double increaseDemandByFactor, ParametersNTM parametersNTM, LinkCellTransmission flowLink, int indexCell)
    {
        super(geometry, centroidNr, name, gemeente, gebied, regio, dhb, centroid, trafficBehaviourType, roadLength,
                averageSpeed, increaseDemandByFactor, parametersNTM);
        this.flowLink = flowLink;
        this.indexCell = indexCell;
    }

    /**
     * @return flowLink.
     */
    public LinkCellTransmission getFlowLink()
    {
        return this.flowLink;
    }

    /**
     * @param flowLink LinkCellTransmission; set flowLink.
     */
    public void setFlowLink(LinkCellTransmission flowLink)
    {
        this.flowLink = flowLink;
    }

    /**
     * @return indexCell.
     */
    public int getIndexCell()
    {
        return indexCell;
    }

    /**
     * @param indexCell int; set indexCell.
     */
    public void setIndexCell(int indexCell)
    {
        this.indexCell = indexCell;
    }

}
