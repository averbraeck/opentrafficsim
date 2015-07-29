package org.opentrafficsim.demo.ntm;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 2 Mar 2015 <br>
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
     * @param geometry
     * @param centroidNr
     * @param name
     * @param gemeente
     * @param gebied
     * @param regio
     * @param dhb
     * @param centroid
     * @param trafficBehaviourType
     * @param roadLength
     * @param averageSpeed
     * @param increaseDemandByFactor
     * @param parametersNTM
     * @param flowLink
     * @param indexCell
     */
    public AreaFlowLink(Geometry geometry, String centroidNr, String name, String gemeente, String gebied,
            String regio, double dhb, Coordinate centroid, TrafficBehaviourType trafficBehaviourType,
            Rel<LengthUnit> roadLength, Abs<SpeedUnit> averageSpeed, double increaseDemandByFactor,
            ParametersNTM parametersNTM, LinkCellTransmission flowLink, int indexCell)
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
     * @param flowLink set flowLink.
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
     * @param indexCell set indexCell.
     */
    public void setIndexCell(int indexCell)
    {
        this.indexCell = indexCell;
    }

}
