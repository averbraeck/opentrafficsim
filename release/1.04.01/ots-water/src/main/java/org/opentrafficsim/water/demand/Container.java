/**
 * 
 */
package org.opentrafficsim.water.demand;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.water.role.Company;
import org.opentrafficsim.water.role.ShippingLine;
import org.opentrafficsim.water.statistics.ContainerTransportCO2BreakdownEnum;
import org.opentrafficsim.water.statistics.ContainerTransportCostBreakdownEnum;
import org.opentrafficsim.water.statistics.ContainerTransportFeeBreakdownEnum;
import org.opentrafficsim.water.statistics.ContainerTransportTimeBreakdownEnum;
import org.opentrafficsim.water.statistics.FullEmptyEnum;
import org.opentrafficsim.water.transfer.Terminal;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * A container is exactly 1 TEU. It collects statistics along the way, which are reported to the statistics objects just before
 * the container disappears from the model at the inland client or deep sea terminal / empty depot.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
public class Container implements Serializable
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the simulator. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** origin terminal. */
    private Terminal terminalFrom;

    /** destination terminal. */
    private Terminal terminalTo;

    /** terminal where the container was stacked last. */
    private Terminal terminalLastStacked;

    /** empty? */
    private boolean empty;

    /** shipping line. */
    private ShippingLine shippingLine;

    /** creation time of container. */
    private Time creationTime;

    /** time of arrival at last terminal in the stack (for statistics about terminal staying time). */
    private Time stackArrivalTime;

    /** time when container loaded on ship. */
    private Time onShipTime;

    /** cost breakdown (based on actual costs) of ALL handling of the container (could be multiple services). */
    private float[] transportCosts = new float[ContainerTransportCostBreakdownEnum.values().length];

    /** fee breakdown (based on fixed costs for actions) of ALL handling of the container. */
    private float[] transportFee = new float[ContainerTransportFeeBreakdownEnum.values().length];

    /** co2 breakdown (in si unit) for ALL handling of the container (could be multiple services). */
    private float[] transportKgCO2 = new float[ContainerTransportCO2BreakdownEnum.values().length];

    /** time breakdown (in si unit) for ALL handling of the container (could be multiple services). */
    private float[] transportTime = new float[ContainerTransportTimeBreakdownEnum.values().length];

    /**
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param terminalFrom Terminal; origin terminal
     * @param terminalTo Terminal; destination terminal
     * @param empty boolean; empty or full
     * @param shippingLine ShippingLine; shipping line of the container
     */
    public Container(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final Terminal terminalFrom,
            final Terminal terminalTo, final boolean empty, final ShippingLine shippingLine)
    {
        super();
        this.simulator = simulator;
        this.terminalFrom = terminalFrom;
        this.terminalLastStacked = terminalFrom;
        this.terminalTo = terminalTo;
        this.empty = empty;
        this.shippingLine = shippingLine;
        this.creationTime = this.simulator.getSimulatorTime();
        for (int i = 0; i < ContainerTransportCostBreakdownEnum.values().length; i++)
        {
            this.transportCosts[i] = 0.0f;
        }
        for (int i = 0; i < ContainerTransportFeeBreakdownEnum.values().length; i++)
        {
            this.transportFee[i] = 0.0f;
        }
        for (int i = 0; i < ContainerTransportCO2BreakdownEnum.values().length; i++)
        {
            this.transportKgCO2[i] = 0.0f;
        }
        for (int i = 0; i < ContainerTransportTimeBreakdownEnum.values().length; i++)
        {
            this.transportTime[i] = 0.0f;
        }
    }

    /**
     * @param costEnum ContainerTransportCostBreakdownEnum; cost category
     * @param cost double; cost to add
     */
    public final void addTransportCost(final ContainerTransportCostBreakdownEnum costEnum, final double cost)
    {
        this.transportCosts[costEnum.ordinal()] += cost;
    }

    /**
     * @param feeEnum ContainerTransportFeeBreakdownEnum; cost category
     * @param fee double; the fee to add
     */
    public final void addTransportFee(final ContainerTransportFeeBreakdownEnum feeEnum, final double fee)
    {
        this.transportFee[feeEnum.ordinal()] += fee;
    }

    /**
     * @param co2Enum ContainerTransportCO2BreakdownEnum; CO2 category
     * @param kgCO2 double; the amount of CO2 to add
     */
    public final void addTransportKgCO2(final ContainerTransportCO2BreakdownEnum co2Enum, final double kgCO2)
    {
        this.transportKgCO2[co2Enum.ordinal()] += kgCO2;
    }

    /**
     * @param timeEnum ContainerTransportTimeBreakdownEnum; time category
     * @param time double; the time in hours to add
     */
    public final void addTransportTime(final ContainerTransportTimeBreakdownEnum timeEnum, final double time)
    {
        this.transportTime[timeEnum.ordinal()] += time;
    }

    /**
     * @param costEnum ContainerTransportCostBreakdownEnum; cost category
     * @return costs of this cost breakdown category until now
     */
    public final double getTransportCost(final ContainerTransportCostBreakdownEnum costEnum)
    {
        return this.transportCosts[costEnum.ordinal()];
    }

    /**
     * @param feeEnum ContainerTransportFeeBreakdownEnum; cost category
     * @return fee until now of this cost breakdown category until now
     */
    public final double getTransportFee(final ContainerTransportFeeBreakdownEnum feeEnum)
    {
        return this.transportFee[feeEnum.ordinal()];
    }

    /**
     * @param co2Enum ContainerTransportCO2BreakdownEnum; CO2 category
     * @return kg CO2 of this CO2 breakdown category until now
     */
    public final double getTransportKgCO2(final ContainerTransportCO2BreakdownEnum co2Enum)
    {
        return this.transportKgCO2[co2Enum.ordinal()];
    }

    /**
     * @param timeEnum ContainerTransportTimeBreakdownEnum; time category
     * @return the time in hours
     */
    public final double getTransportTime(final ContainerTransportTimeBreakdownEnum timeEnum)
    {
        return this.transportTime[timeEnum.ordinal()];
    }

    /**
     * @return summed total costs
     */
    public final double getSumTransportCost()
    {
        double sum = 0.0;
        for (int i = 0; i < ContainerTransportCostBreakdownEnum.values().length; i++)
        {
            sum += this.transportCosts[i];
        }
        return sum;
    }

    /**
     * @return summed total fee
     */
    public final double getSumTransportFee()
    {
        double sum = 0.0;
        for (int i = 0; i < ContainerTransportFeeBreakdownEnum.values().length; i++)
        {
            sum += this.transportFee[i];
        }
        return sum;
    }

    /**
     * @return summed total kg CO2
     */
    public final double getSumTransportKgCO2()
    {
        double sum = 0.0;
        for (int i = 0; i < ContainerTransportCO2BreakdownEnum.values().length; i++)
        {
            sum += this.transportKgCO2[i];
        }
        return sum;
    }

    /**
     * @return summed total time
     */
    public final double getSumTransportTime()
    {
        double sum = 0.0;
        for (int i = 0; i < ContainerTransportTimeBreakdownEnum.values().length; i++)
        {
            sum += this.transportTime[i];
        }
        return sum;
    }

    /**
     * Collect the final statistics when the container arrived at its destination.
     */
    @SuppressWarnings("checkstyle:needbraces")
    public final void collectTerminalStatisticsAtContainerDestination()
    {
        // Terminal terminal = this.terminalTo.isRtmTerminal() ? this.terminalFrom : this.terminalTo;
        // TODO: add at terminal: totalCosts += terminal.getAnnualFixedCostsTerminalPerTEU();
        // TerminalStatistics stats = terminal.getTerminalStatistics();
        //
        // stats.costPerTEU.get(DirectionEnum.TOTAL).tally(getSumTransportCost());
        // stats.feePerTEU.get(DirectionEnum.TOTAL).tally(getSumTransportFee());
        // stats.kgCO2PerTEU.get(DirectionEnum.TOTAL).tally(getSumTransportKgCO2());
        // stats.transportTime.get(DirectionEnum.TOTAL).tally(getSumTransportTime());
        // for (ContainerTransportCostBreakdownEnum ctcb : ContainerTransportCostBreakdownEnum.values())
        // stats.addBreakdownCost(DirectionEnum.TOTAL, ctcb, this.getTransportCost(ctcb));
        // for (ContainerTransportFeeBreakdownEnum ctfb : ContainerTransportFeeBreakdownEnum.values())
        // stats.addBreakdownFee(DirectionEnum.TOTAL, ctfb, this.getTransportFee(ctfb));
        // for (ContainerTransportCO2BreakdownEnum ctcb : ContainerTransportCO2BreakdownEnum.values())
        // stats.addBreakdownCO2(DirectionEnum.TOTAL, ctcb, this.getTransportKgCO2(ctcb));
        // for (ContainerTransportTimeBreakdownEnum cttb : ContainerTransportTimeBreakdownEnum.values())
        // stats.addBreakdownTime(DirectionEnum.TOTAL, cttb, this.getTransportTime(cttb));
        //
        // if (this.terminalTo.isRtmTerminal())
        // {
        // stats.costPerTEU.get(DirectionEnum.TORTM).tally(getSumTransportCost());
        // stats.feePerTEU.get(DirectionEnum.TORTM).tally(getSumTransportFee());
        // stats.kgCO2PerTEU.get(DirectionEnum.TORTM).tally(getSumTransportKgCO2());
        // stats.transportTime.get(DirectionEnum.TORTM).tally(getSumTransportTime());
        // for (ContainerTransportCostBreakdownEnum ctcb : ContainerTransportCostBreakdownEnum.values())
        // stats.addBreakdownCost(DirectionEnum.TORTM, ctcb, this.getTransportCost(ctcb));
        // for (ContainerTransportFeeBreakdownEnum ctfb : ContainerTransportFeeBreakdownEnum.values())
        // stats.addBreakdownFee(DirectionEnum.TORTM, ctfb, this.getTransportFee(ctfb));
        // for (ContainerTransportCO2BreakdownEnum ctcb : ContainerTransportCO2BreakdownEnum.values())
        // stats.addBreakdownCO2(DirectionEnum.TORTM, ctcb, this.getTransportKgCO2(ctcb));
        // for (ContainerTransportTimeBreakdownEnum cttb : ContainerTransportTimeBreakdownEnum.values())
        // stats.addBreakdownTime(DirectionEnum.TORTM, cttb, this.getTransportTime(cttb));
        // }
        //
        // else
        //
        // {
        // stats.costPerTEU.get(DirectionEnum.FROMRTM).tally(getSumTransportCost());
        // stats.feePerTEU.get(DirectionEnum.FROMRTM).tally(getSumTransportFee());
        // stats.kgCO2PerTEU.get(DirectionEnum.FROMRTM).tally(getSumTransportKgCO2());
        // stats.transportTime.get(DirectionEnum.FROMRTM).tally(getSumTransportTime());
        // for (ContainerTransportCostBreakdownEnum ctcb : ContainerTransportCostBreakdownEnum.values())
        // stats.addBreakdownCost(DirectionEnum.FROMRTM, ctcb, this.getTransportCost(ctcb));
        // for (ContainerTransportFeeBreakdownEnum ctfb : ContainerTransportFeeBreakdownEnum.values())
        // stats.addBreakdownFee(DirectionEnum.FROMRTM, ctfb, this.getTransportFee(ctfb));
        // for (ContainerTransportCO2BreakdownEnum ctcb : ContainerTransportCO2BreakdownEnum.values())
        // stats.addBreakdownCO2(DirectionEnum.FROMRTM, ctcb, this.getTransportKgCO2(ctcb));
        // for (ContainerTransportTimeBreakdownEnum cttb : ContainerTransportTimeBreakdownEnum.values())
        // stats.addBreakdownTime(DirectionEnum.FROMRTM, cttb, this.getTransportTime(cttb));
        // }
    }

    /**
     * @return the terminalFrom
     */
    public final Terminal getTerminalFrom()
    {
        return this.terminalFrom;
    }

    /**
     * @return the terminalTo
     */
    public final Terminal getTerminalTo()
    {
        return this.terminalTo;
    }

    /**
     * @return the terminalCurrent
     */
    public final Terminal getTerminalLastStacked()
    {
        return this.terminalLastStacked;
    }

    /**
     * @param terminalFrom Terminal; the terminalFrom to set
     */
    public final void setTerminalFrom(final Terminal terminalFrom)
    {
        this.terminalFrom = terminalFrom;
    }

    /**
     * @param terminalTo Terminal; the terminalTo to set
     */
    public final void setTerminalTo(final Terminal terminalTo)
    {
        this.terminalTo = terminalTo;
    }

    /**
     * @param terminalLastStacked Terminal; the terminalCurrent to set
     */
    public final void setTerminalLastStacked(final Terminal terminalLastStacked)
    {
        this.terminalLastStacked = terminalLastStacked;
    }

    /**
     * @return the empty
     */
    public final boolean isEmpty()
    {
        return this.empty;
    }

    /**
     * @return full or empty
     */
    public final FullEmptyEnum fullEmpty()
    {
        if (this.empty)
        {
            return FullEmptyEnum.EMPTY;
        }
        return FullEmptyEnum.FULL;
    }

    /**
     * @return the owner
     */
    public final Company getOwner()
    {
        return this.shippingLine;
    }

    /**
     * @return the shippingLine
     */
    public final ShippingLine getShippingLine()
    {
        return this.shippingLine;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        String full = this.empty ? "[EMPTY]" : "[FULL]";
        return "Container from " + this.terminalFrom.getName() + " to " + this.terminalTo.getName() + " at "
                + this.terminalLastStacked.getName() + " (owner " + this.shippingLine.getCode() + ") " + full;
    }

    /**
     * @return short info
     */
    public final String toShortString()
    {
        String full = this.empty ? "[E]" : "[F]";
        return this.terminalFrom.getName() + "->" + this.terminalTo.getName() + "@" + this.terminalLastStacked.getName() + full;
    }

    /**
     * @return the arrivalTime
     */
    public final Time getStackArrivalTime()
    {
        return this.stackArrivalTime;
    }

    /**
     * set the arrivalTime.
     */
    public final void setStackArrivalTime()
    {
        this.stackArrivalTime = this.simulator.getSimulatorTime();
    }

    /**
     * @return the creationTime
     */
    public final Time getCreationTime()
    {
        return this.creationTime;
    }

    /**
     * @return the onShipTime
     */
    public final Time getOnShipTime()
    {
        return this.onShipTime;
    }

    /**
     * set onShipTime.
     */
    public final void setOnShipTime()
    {
        this.onShipTime = this.simulator.getSimulatorTime();
    }

}
