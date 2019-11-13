package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
public class LinkCellTransmission extends NTMLink
{
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private ArrayList<FlowCell> cells;

    /** SPEEDAB class java.lang.Double 120.0. */
    private Duration actualTime;

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
     * @throws NetworkException
     */

    public LinkCellTransmission(final Network network, final OTSSimulatorInterface simulator, OTSLine3D geometry, String nr,
            Length length, NTMNode startNode, NTMNode endNode, Speed speed, Duration time, Frequency capacity,
            TrafficBehaviourType behaviourType, LinkData linkData, ArrayList<FlowCell> cells, int hierarchy)
            throws NetworkException
    {
        super(network, simulator, geometry, nr, length, startNode, endNode, speed, time, capacity, behaviourType, linkData);
        this.cells = cells;
    }

    /**
     * @param link NTMLink; original Link
     * @param cells ArrayList&lt;FlowCell&gt;; to add
     * @throws NetworkException
     */
    public LinkCellTransmission(final NTMLink link, final ArrayList<FlowCell> cells) throws NetworkException
    {
        super(link.getNetwork(), link.getSimulator(), link.getDesignLine(), link.getId(), link.getLength(),
                (NTMNode) link.getStartNode(), (NTMNode) link.getEndNode(), link.getFreeSpeed(), link.getDuration(),
                link.getCapacity(), link.getBehaviourType(), link.getLinkData());
        this.cells = cells;
    }

    /**
     * @param link NTMLink; original Link
     * @param startNode BoundedNode;
     * @param endNode BoundedNode;
     * @param cells ArrayList&lt;FlowCell&gt;; to add
     * @throws NetworkException
     */
    public LinkCellTransmission(final NTMLink link, BoundedNode startNode, BoundedNode endNode, final ArrayList<FlowCell> cells)
            throws NetworkException
    {
        super(link.getNetwork(), link.getSimulator(), link.getDesignLine(), link.getId(), link.getLength(), startNode, endNode,
                link.getFreeSpeed(), link.getDuration(), link.getCapacity(), link.getBehaviourType(), link.getLinkData());
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
     * @param cells ArrayList&lt;FlowCell&gt;; set cells.
     */
    public final void setCells(final ArrayList<FlowCell> cells)
    {
        this.cells = cells;
    }

    /**
     * @param link NTMLink;
     * @param timeStepDurationCellTransmission Duration;
     * @return
     */
    public static final ArrayList<FlowCell> createCells(final NTMLink link, Duration timeStepDurationCellTransmission)
    {
        ArrayList<FlowCell> flowCells = new ArrayList<FlowCell>();
        // the length of the cell depends on the speed and simulation time step
        Speed speed = link.getFreeSpeed();
        Length cellLength = new Length(
                speed.getInUnit(SpeedUnit.KM_PER_HOUR) * timeStepDurationCellTransmission.getInUnit(DurationUnit.HOUR),
                LengthUnit.KILOMETER);
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
    public Duration retrieveActualTime()
    {
        Double timeDouble = new Double(0.0);
        for (FlowCell cell : this.cells)
        {
            timeDouble += cell.retrieveCurrentTravelDuration().getInUnit(DurationUnit.HOUR);
        }
        this.actualTime = new Duration(timeDouble, DurationUnit.HOUR);
        return this.actualTime;
    }

}
