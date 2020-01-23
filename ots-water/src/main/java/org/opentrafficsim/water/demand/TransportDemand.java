/**
 * 
 */
package org.opentrafficsim.water.demand;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.water.role.Company;
import org.opentrafficsim.water.transfer.Terminal;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * Demand the between terminals in an operating area (region) and other terminals.
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
public class TransportDemand implements Serializable
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the simulator to schedule on. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** the operating area for the demand. */
    private Region operatingArea;

    /** the demand from and to terminals. */
    private Map<Terminal, Map<Terminal, DemandCell>> demandMap = new LinkedHashMap<Terminal, Map<Terminal, DemandCell>>();

    /**
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator for scheduling the demand
     * @param operatingArea Region; the region for which this demand holds
     */
    public TransportDemand(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final Region operatingArea)
    {
        this.simulator = simulator;
        this.operatingArea = operatingArea;
    }

    /**
     * @return the operatingArea
     */
    public final Region getOperatingArea()
    {
        return this.operatingArea;
    }

    /**
     * @return the demandCells
     */
    public final Map<Terminal, Map<Terminal, DemandCell>> getDemandMap()
    {
        return this.demandMap;
    }

    /**
     * add a cell to the demandmap.
     * @param demandCell DemandCell; the cell to add
     */
    public final void addDemand(final DemandCell demandCell)
    {
        Map<Terminal, DemandCell> partMap = this.demandMap.get(demandCell.getTerminalFrom());
        if (partMap == null)
        {
            partMap = new LinkedHashMap<Terminal, DemandCell>();
            this.demandMap.put(demandCell.getTerminalFrom(), partMap);
        }
        partMap.put(demandCell.getTerminalTo(), demandCell);
    }

    /**
     * add a cell to the demandmap.
     * @param terminalFrom Terminal; origin terminal
     * @param terminalTo Terminal; destination terminal
     * @param numberAnnual int; annual number of containers per year
     * @param fraction20ft double; fraction of 20 ft containers (rest is 40 ft)
     * @param fractionEmpty double; fraction of empty containers (rest is full)
     * @param fractionOwners Map&lt;Company,Double&gt;; map of owners with fraction (adding up to 1.0) indicating who owns the
     *            containers
     */
    public final void addDemand(final Terminal terminalFrom, final Terminal terminalTo, final int numberAnnual,
            final double fraction20ft, final double fractionEmpty, final Map<Company, Double> fractionOwners)
    {
        DemandCell demandCell =
                new DemandCell(terminalFrom, terminalTo, numberAnnual, fraction20ft, fractionEmpty, fractionOwners);
        addDemand(demandCell);
    }

    /**
     * @param terminalFrom Terminal; origin terminal
     * @param terminalTo Terminal; destination terminal
     * @return the demand between two terminals.
     */
    public final DemandCell getDemand(final Terminal terminalFrom, final Terminal terminalTo)
    {
        if (this.demandMap.containsKey(terminalFrom))
        {
            return this.demandMap.get(terminalFrom).get(terminalTo);
        }
        return null;
    }

    /**
     * @param terminalFrom Terminal; origin terminal
     * @return the map of demands originating at a terminal.
     */
    public final Map<Terminal, DemandCell> getDemandMapFrom(final Terminal terminalFrom)
    {
        return this.demandMap.get(terminalFrom);
    }

    /**
     * @param terminalTo Terminal; destination terminal
     * @return the map of demands for a destination terminal.
     */
    public final Map<Terminal, DemandCell> getDemandMapTo(final Terminal terminalTo)
    {
        Map<Terminal, DemandCell> toMap = new LinkedHashMap<Terminal, DemandCell>();
        for (Terminal from : this.demandMap.keySet())
        {
            for (Terminal to : this.demandMap.get(from).keySet())
            {
                if (terminalTo.equals(to))
                {
                    toMap.put(from, this.demandMap.get(from).get(to));
                }
            }
        }
        return toMap;
    }

    /**
     * @param terminalFrom Terminal; origin terminal
     * @return the demands originating at a terminal as a set.
     */
    public final Set<DemandCell> getDemandSetFrom(final Terminal terminalFrom)
    {
        return new LinkedHashSet<DemandCell>(this.demandMap.get(terminalFrom).values());
    }

    /**
     * @param terminalTo Terminal; destination terminal
     * @return the demands for a destination terminal as a set.
     */
    public final Set<DemandCell> getDemandSetTo(final Terminal terminalTo)
    {
        Set<DemandCell> toSet = new LinkedHashSet<DemandCell>();
        for (Terminal from : this.demandMap.keySet())
        {
            for (Terminal to : this.demandMap.get(from).keySet())
            {
                if (terminalTo.equals(to))
                {
                    toSet.add(this.demandMap.get(from).get(to));
                }
            }
        }
        return toSet;
    }

    /**
     * @return the simulator
     */
    public final DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
    {
        return this.simulator;
    }

    /**
     * A cell of demand from a terminal to a terminal.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * </p>
     * <p>
     * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en
     * Leefomgeving and licensed without restrictions to Delft University of Technology, including the right to sub-license
     * sources and derived products to third parties.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Nov 6, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    public class DemandCell
    {
        /** origin terminal. */
        private Terminal terminalFrom;

        /** destination terminal. */
        private Terminal terminalTo;

        /** number of containers per year. */
        private int numberAnnual;

        /** fraction of 20 ft containers of total (rest is 40 ft). */
        private double fraction20ft;

        /** fraction of empty containers (rest is full). */
        private double fractionEmpty;

        /** table with fractions (adding up to 1.0) who owns the containers. */
        private Map<Company, Double> fractionOwners = new LinkedHashMap<Company, Double>();

        /**
         * @param terminalFrom Terminal; origin terminal
         * @param terminalTo Terminal; destination terminal
         * @param numberAnnual int; annual number of containers per year
         * @param fraction20ft double; fraction of 20 ft containers (rest is 40 ft)
         * @param fractionEmpty double; fraction of empty containers (rest is full)
         * @param fractionOwners Map&lt;Company,Double&gt;; map of owners with fraction (adding up to 1.0) indicating who owns
         *            the containers
         */
        public DemandCell(final Terminal terminalFrom, final Terminal terminalTo, final int numberAnnual,
                final double fraction20ft, final double fractionEmpty, final Map<Company, Double> fractionOwners)
        {
            super();
            this.terminalFrom = terminalFrom;
            this.terminalTo = terminalTo;
            this.numberAnnual = numberAnnual;
            this.fraction20ft = fraction20ft;
            this.fractionEmpty = fractionEmpty;
            this.fractionOwners = fractionOwners;
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
         * @return the numberAnnual
         */
        public final int getNumberAnnual()
        {
            return this.numberAnnual;
        }

        /**
         * @return the fraction20ft
         */
        public final double getFraction20ft()
        {
            return this.fraction20ft;
        }

        /**
         * @return the fractionEmpty
         */
        public final double getFractionEmpty()
        {
            return this.fractionEmpty;
        }

        /**
         * @return the fractionOwners
         */
        public final Map<Company, Double> getFractionOwners()
        {
            return this.fractionOwners;
        }

    }

}
