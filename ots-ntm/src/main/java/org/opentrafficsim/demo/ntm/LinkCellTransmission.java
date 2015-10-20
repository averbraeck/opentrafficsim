package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class LinkCellTransmission extends Link
{
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private ArrayList<FlowCell> cells;

    /** SPEEDAB class java.lang.Double 120.0. */
    private Rel<TimeUnit> actualTime;

    /**
     * @param geometry
     * @param nr
     * @param length
     * @param startNode
     * @param endNode
     * @param speed
     * @param time
     * @param capacity
     * @param behaviourType
     * @param linkData
     * @param cells
     * @param hierarchy
     */

    public LinkCellTransmission(OTSLine3D geometry, String nr, DoubleScalar.Rel<LengthUnit> length, Node startNode,
        Node endNode, DoubleScalar.Abs<SpeedUnit> speed, DoubleScalar.Rel<TimeUnit> time,
        Frequency capacity, TrafficBehaviourType behaviourType, LinkData linkData,
        ArrayList<FlowCell> cells, int hierarchy)
    {
        super(geometry, nr, length, startNode, endNode, speed, time, capacity, behaviourType, linkData);
        this.cells = cells;
    }

    /**
     * @param link original Link
     * @param cells to add
     */
    public LinkCellTransmission(final Link link, final ArrayList<FlowCell> cells)
    {
        super(link.getDesignLine(), link.getId(), link.getLength(), (Node) link.getStartNode(), (Node) link.getEndNode(),
            link.getFreeSpeed(), link.getTime(), link.getCapacity(), link.getBehaviourType(), link.getLinkData());
        this.cells = cells;
    }

    /**
     * @param link original Link
     * @param startNode
     * @param endNode
     * @param cells to add
     */
    public LinkCellTransmission(final Link link, BoundedNode startNode, BoundedNode endNode, final ArrayList<FlowCell> cells)
    {
        super(link.getDesignLine(), link.getId(), link.getLength(), startNode, endNode, link.getFreeSpeed(), link.getTime(),
            link.getCapacity(), link.getBehaviourType(), link.getLinkData());
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
    public static final ArrayList<FlowCell> createCells(final Link link, Rel<TimeUnit> timeStepDurationCellTransmission)
    {
        ArrayList<FlowCell> flowCells = new ArrayList<FlowCell>();
        // the length of the cell depends on the speed and simulation time step
        DoubleScalar.Abs<SpeedUnit> speed = link.getFreeSpeed();
        Rel<LengthUnit> cellLength =
            new DoubleScalar.Rel<LengthUnit>(speed.getInUnit(SpeedUnit.KM_PER_HOUR)
                * timeStepDurationCellTransmission.getInUnit(TimeUnit.HOUR), LengthUnit.KILOMETER);
        // find out how many Cells fit into this Link
        double numberOfCells = Math.max(Math.rint(link.getLength().getSI() / cellLength.getSI()), 1);
        // Frequency capPerLane =
        // new Frequency(link.getCapacity().getSI() * 3600 / link.getNumberOfLanes(),
        // FrequencyUnit.PER_HOUR);
        // compute the amount of cells
        for (int i = 0; i < numberOfCells; i++)
        {
            FlowCell cell =
                new FlowCell(cellLength, link.getCapacity(), speed, link.getNumberOfLanes(), TrafficBehaviourType.FLOW);
            flowCells.add(cell);
        }
        return flowCells;
    }

    /**
     * @param accumulatedCars
     * @return actualTime.
     */
    public Rel<TimeUnit> retrieveActualTime()
    {
        Double timeDouble = new Double(0.0);
        for (FlowCell cell : this.cells)
        {
            timeDouble += cell.retrieveCurrentTravelTime().getInUnit(TimeUnit.HOUR);
        }
        this.actualTime = new Rel<TimeUnit>(timeDouble, TimeUnit.HOUR);
        return this.actualTime;
    }

}
