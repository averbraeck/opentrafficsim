package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
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
            Node endNode, DoubleScalar<SpeedUnit> speed, DoubleScalar<FrequencyUnit> capacity,
            TrafficBehaviourType behaviourType, LinkData linkData, ArrayList<FlowCell> cells)
    {
        super(geometry, nr, length, startNode, endNode, speed, capacity, behaviourType, linkData);
        this.cells = cells;
    }
    
    /**
     * @param link original Link
     * @param cells to add
     */
    public CellTransmissionLink(final Link link, final ArrayList<FlowCell> cells)
    {
        super(link.getGeometry(), link.getId(), link.getLength(), link.getStartNode(), link.getEndNode(), 
                link.getSpeed(), link.getCapacity(), link.getBehaviourType(), link.getLinkData());
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

    /**
     * @param link
     * @param timeStepDurationCellTransmission
     * @return
     */
    public ArrayList<FlowCell> createCells(CellTransmissionLink link, Rel<TimeUnit> timeStepDurationCellTransmission)
    {
        ArrayList<FlowCell> flowCells = new ArrayList<FlowCell>();
        // the length of the cell depends on the speed and simulation time step
        DoubleScalar<SpeedUnit> speed = link.getSpeed();
        Rel<TimeUnit> timeStep = timeStepDurationCellTransmission;
        DoubleScalar<LengthUnit> cellLength =
                new DoubleScalar.Abs<LengthUnit>(speed.getSI() * timeStep.getSI(), LengthUnit.KILOMETER);
        // find out how many Cells fit into this Link
        double numberOfCells = Math.rint(link.getLength().getSI() / cellLength.getSI());
        //compute the amount of cells 
        for (int i = 0; i < numberOfCells; i++)
        {
            FlowCell cell = new FlowCell(cellLength, link.getCapacity());
            flowCells.add(cell);
        }
        return flowCells;
    }

}
