package org.opentrafficsim.core.gtu;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Characteristics of a GTU. This class is used to store all characteristics of a (not-yet constructed) GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUCharacteristics
{

    /** The type of the GTU. */
    private final GTUType gtuType;

    /** The id generator that will generate the id of the GTU. */
    private final IdGenerator idGenerator;

    /** Length of the GTU. */
    private final Length.Rel length;

    /** Width of the GTU. */
    private final Length.Rel width;

    /** Maximum velocity of the GTU. */
    private final Speed maximumVelocity;

    /** The simulator that controls the GTU. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The OTSNetwork that all generated GTUs will be registered in. */
    private final OTSNetwork network;

    /**
     * Construct a new set of GTUCharacteristics.
     * @param gtuType GTUType; type of the (not yet constructed) GTU
     * @param idGenerator IdGenerator; the id generator for the (not yet constructed) GTU
     * @param length Length.Rel; the length of the (non yet constructed) GTU
     * @param width Length.Rel; the width of the (non yet constructed) GTU
     * @param maximumVelocity Length.Rel; the maximum velocity of the (non yet constructed) GTU
     * @param simulator OTSDEVSSimulatorInterface; the simulator that controls the (not yet constructed) GTU
     * @param network OTSNetwork; the network that will contain the GTU
     */
    public GTUCharacteristics(final GTUType gtuType, final IdGenerator idGenerator, final Length.Rel length,
            final Length.Rel width, final Speed maximumVelocity, final OTSDEVSSimulatorInterface simulator,
            final OTSNetwork network)
    {
        this.gtuType = gtuType;
        this.idGenerator = idGenerator;
        this.length = length;
        this.width = width;
        this.maximumVelocity = maximumVelocity;
        this.simulator = simulator;
        this.network = network;
    }

    /**
     * Retrieve the GTU type.
     * @return GTUType.
     */
    public final GTUType getGTUType()
    {
        return this.gtuType;
    }

    /**
     * Retrieve the id generator.
     * @return IdGenerator
     */
    public final IdGenerator getIdGenerator()
    {
        return this.idGenerator;
    }

    /**
     * Retrieve the length.
     * @return Length.Rel
     */
    public final Length.Rel getLength()
    {
        return this.length;
    }

    /**
     * Retrieve the width.
     * @return Width.Rel
     */
    public final Length.Rel getWidth()
    {
        return this.width;
    }

    /**
     * Retrieve the maximum velocity.
     * @return Speed
     */
    public final Speed getMaximumVelocity()
    {
        return this.maximumVelocity;
    }

    /**
     * Retrieve the simulator.
     * @return OTSDEVSSimulatorInterface
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Retrieve the network.
     * @return OTSNetwork
     */
    public final OTSNetwork getNetwork()
    {
        return this.network;
    }

}
