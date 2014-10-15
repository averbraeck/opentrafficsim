package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 15 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class CellTransmissionLink extends Link
{
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private ArrayList<FlowCell> cells;

    /**
     * @param geometry
     * @param nr
     * @param length
     * @param startNode
     * @param endNode
     * @param speed
     * @param capacity
     * @param behaviourType
     * @param linkData
     * @param cells
     */
    public CellTransmissionLink(Geometry geometry, String nr, DoubleScalar<LengthUnit> length, Node startNode,
            Node endNode, double speed, DoubleScalar<FrequencyUnit> capacity, TrafficBehaviourType behaviourType,
            LinkData linkData, ArrayList<FlowCell> cells)
    {
        super(geometry, nr, length, startNode, endNode, speed, capacity, behaviourType, linkData);
        this.cells = cells;
    }

    /**
     * @return cells.
     */
    public final ArrayList<FlowCell> getCells()
    {
        return this.cells;
    }

    /**
     * @param cells set cells.
     */
    public final void setCells(final ArrayList<FlowCell> cells)
    {
        this.cells = cells;
    }

}
