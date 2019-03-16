package org.opentrafficsim.water.network.infra;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.water.network.WaterwayLocation;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <br>
 * Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving. All rights reserved. <br>
 * Some parts of the software (c) 2011-2013 TU Delft, Faculty of TBM, Systems and Simulation <br>
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br>
 * @version Sep 28, 2012 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 */
public class Lock extends Obstacle implements OperatedObstacle
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the simulator to schedule on. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** hours per day. */
    private int operationHoursPerDay;

    /** days per week. */
    private int operationDaysPerWeek;

    /** length. */
    private Length length;

    /** width. */
    private Length width;

    /** number of lock chambers. */
    private int numberChambers;

    /** priority for cargo? */
    private boolean cargoPriority;

    /** estimated average opening time. */
    private Duration estimatedAverageLockageTime;

    /**
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @param name String; the name of the lock
     * @param waterwayLocation WaterwayLocation; the location along the waterway
     * @param numberChambers int; the number of lock chambers
     * @param operationHoursPerDay int; hours per day
     * @param operationDaysPerWeek int; days per week
     * @param length Length; the length
     * @param width Length; the width
     * @param cargoPriority boolean; does cargo have priority?
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lock(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final String name,
            final WaterwayLocation waterwayLocation, final int numberChambers, final int operationHoursPerDay,
            final int operationDaysPerWeek, final Length length, final Length width, final boolean cargoPriority)
    {
        super(name, waterwayLocation);
        this.simulator = simulator;
        this.operationHoursPerDay = operationHoursPerDay;
        this.operationDaysPerWeek = operationDaysPerWeek;
        this.numberChambers = numberChambers;
        this.length = length;
        this.width = width;
        this.cargoPriority = cargoPriority;
    }

    /**
     * @return the estimated lockage delay in hours during normal opening time
     */
    public final Duration estimateLockageDelay()
    {
        return this.estimatedAverageLockageTime;
    }

    /**
     * @return the lockage delay in hours during normal opening time
     */
    public final Duration drawLockageDelay()
    {
        return this.estimatedAverageLockageTime;
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
     * @return the length
     */
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * @return the width
     */
    public final Length getWidth()
    {
        return this.width;
    }

    /**
     * @return numberChambers
     */
    public final int getNumberChambers()
    {
        return this.numberChambers;
    }

    /**
     * @return the cargoPriority
     */
    public final boolean isCargoPriority()
    {
        return this.cargoPriority;
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
        return "Lock " + this.getName() + " at " + this.getWaterwayLocation();
    }

}
