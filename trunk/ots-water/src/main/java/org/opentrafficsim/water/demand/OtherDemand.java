/**
 * 
 */
package org.opentrafficsim.water.demand;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opentrafficsim.water.Dynamic;
import org.opentrafficsim.water.network.WaterwayLocation;
import org.opentrafficsim.water.transport.ShipType;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * Describes the demand from locations along waterways to other locations.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
public class OtherDemand implements Dynamic, Serializable
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the simulator to schedule on. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** the demand from and to locations. */
    private Set<OtherDemandCell> otherDemandSet = new LinkedHashSet<OtherDemandCell>();

    /**
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the finulator
     */
    public OtherDemand(final DEVSSimulatorInterface.TimeDoubleUnit simulator)
    {
        super();
        this.simulator = simulator;
    }

    /**
     * @return the demandCells
     */
    public final Set<OtherDemandCell> getOtherDemandSet()
    {
        return this.otherDemandSet;
    }

    /**
     * add a cell to the demandmap.
     * @param otherDemandCell OtherDemandCell; demand to add
     */
    public final void addDemand(final OtherDemandCell otherDemandCell)
    {
        this.otherDemandSet.add(otherDemandCell);
    }

    /**
     * add a cell to the demand map.
     * @param origin WaterwayLocation; the origin location
     * @param destination WaterwayLocation; the destination location
     * @param annualMoves int; the annual number of moves
     * @param shipType ShipType; the type of ship to use
     */
    public final void addDemand(final WaterwayLocation origin, final WaterwayLocation destination, final int annualMoves,
            final ShipType shipType)
    {
        OtherDemandCell otherDemandCell = new OtherDemandCell(origin, destination, annualMoves, shipType);
        addDemand(otherDemandCell);
    }

    /**
     * A cell of demand from a location along a waterway to another location. <br>
     * Copyright (c) 2012 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
     * for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>. The source code and
     * binary code of this software is proprietary information of Delft University of Technology.
     * @version Sep 29, 2012 <br>
     * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
     */
    public class OtherDemandCell
    {
        /** origin. */
        private WaterwayLocation origin;

        /** destination. */
        private WaterwayLocation destination;

        /** number of moves per year. */
        private int annualMoves;

        /** type of ship. */
        private ShipType shipType;

        /**
         * @param origin WaterwayLocation; the origin location
         * @param destination WaterwayLocation; the destination location
         * @param annualMoves int; the annual number of moves
         * @param shipType ShipType; the type of ship to use
         */
        public OtherDemandCell(final WaterwayLocation origin, final WaterwayLocation destination, final int annualMoves,
                final ShipType shipType)
        {
            this.origin = origin;
            this.destination = destination;
            this.annualMoves = annualMoves;
            this.shipType = shipType;
        }

        /**
         * @return the origin
         */
        public final WaterwayLocation getOrigin()
        {
            return this.origin;
        }

        /**
         * @return the destination
         */
        public final WaterwayLocation getDestination()
        {
            return this.destination;
        }

        /**
         * @return the annualMoves
         */
        public final int getAnnualMoves()
        {
            return this.annualMoves;
        }

        /**
         * @return the shipType
         */
        public final ShipType getShipType()
        {
            return this.shipType;
        }

    }

    /** {@inheritDoc} */
    @Override
    public final DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
    {
        return this.simulator;
    }
}
