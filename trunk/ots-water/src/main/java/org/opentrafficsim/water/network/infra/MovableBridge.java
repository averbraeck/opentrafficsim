package org.opentrafficsim.water.network.infra;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.water.network.Waterway;
import org.opentrafficsim.water.network.WaterwayLocation;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * Movable bridge which can cause delay.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class MovableBridge extends FixedBridge implements OperatedObstacle
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the simulator to schedule on. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** hours per day. */
    private int operationHoursPerDay;

    /** days per week. */
    private int operationDaysPerWeek;

    /** max height when bridge is closed. */
    private Length maxHeightClosed;

    /** max height when bridge is opened. */
    private Length maxHeightOpened;

    /** average waiting time. */
    private Duration waitingTime;

    /**
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @param name String; the name of the bridge
     * @param waterwayLocation WaterwayLocation; the location along a waterway
     * @param operationHoursPerDay int; hours per day operated
     * @param operationDaysPerWeek int; days per week operated
     * @param maxHeightClosed Length; max height when bridge is closed
     * @param maxHeightOpened Length; max height when bridge is opened
     */
    public MovableBridge(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final String name,
            final WaterwayLocation waterwayLocation, final int operationHoursPerDay, final int operationDaysPerWeek,
            final Length maxHeightClosed, final Length maxHeightOpened)
    {
        super(name, waterwayLocation, maxHeightOpened);
        this.simulator = simulator;
        this.operationHoursPerDay = operationHoursPerDay;
        this.operationDaysPerWeek = operationDaysPerWeek;
        this.maxHeightClosed = maxHeightClosed;
        this.maxHeightOpened = maxHeightOpened;

        // this.waitingTime = GlobalIDVV.theModel.getNumberParameter("WTBR") / 60.0;
        // if (this.operationHoursPerDay < 18)
        // this.waitingTime *= 2.0;
        // else if (this.operationHoursPerDay < 24)
        // this.waitingTime *= 1.5;
    }

    /**
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @param name String; the name of the bridge
     * @param waterway Waterway; the waterway
     * @param distance Length; the distance along the waterway in the design direction
     * @param operationHoursPerDay int; hours per day operated
     * @param operationDaysPerWeek int; days per week operated
     * @param maxHeightClosed Length; max height when bridge is closed
     * @param maxHeightOpened Length; max height when bridge is opened
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public MovableBridge(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final String name, final Waterway waterway,
            final Length distance, final int operationHoursPerDay, final int operationDaysPerWeek, final Length maxHeightClosed,
            final Length maxHeightOpened)
    {
        this(simulator, name, new WaterwayLocation(waterway, distance), operationHoursPerDay, operationDaysPerWeek,
                maxHeightClosed, maxHeightOpened);
    }

    /**
     * @return an estimate of the delay for opening the bridge
     */
    public final Duration estimateOpeningDelay()
    {
        return this.waitingTime;
    }

    /**
     * @return an estimate of the delay for opening the bridge
     */
    public final Duration drawOpeningDelay()
    {
        return this.waitingTime;
    }

    /** {@inheritDoc} */
    @Override
    public final int getOperationHoursPerDay()
    {
        return this.operationHoursPerDay;
    }

    /** {@inheritDoc} */
    @Override
    public final int getOperationDaysPerWeek()
    {
        return this.operationDaysPerWeek;
    }

    /**
     * @return maxHeightClosed
     */
    public final Length getMaxHeightClosed()
    {
        return this.maxHeightClosed;
    }

    /**
     * @return maxHeightOpened
     */
    public final Length getMaxHeightOpened()
    {
        return this.maxHeightOpened;
    }

    /** {@inheritDoc} */
    @Override
    public final DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Movable Bridge " + this.getName() + " at " + this.getWaterwayLocation();
    }

}
