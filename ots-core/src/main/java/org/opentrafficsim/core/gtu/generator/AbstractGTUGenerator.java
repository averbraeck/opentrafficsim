package org.opentrafficsim.core.gtu.generator;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.car.LaneBasedIndividualCar.LaneBasedIndividualCarBuilder;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DistContinuousDoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> the ID type of the GTU (e.g., String or Integer)
 */
public abstract class AbstractGTUGenerator<ID>
{
    /** the generator name. Will be used for generated GTUs as Name:# where # is the id of the gtu when ID is a String. */
    private final String name;

    /** the type of GTU to generate. */
    private final GTUType<ID> gtuType;

    /** the gtu class to instantiate. */
    private final Class<GTU<ID>> gtuClass;

    /** the GTU following model to use. */
    private final GTUFollowingModel gtuFollowingModel;

    /** distribution of the initial speed of the GTU. */
    private final DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist;

    /** distribution of the interarrival time. */
    private final DistContinuousDoubleScalar.Rel<TimeUnit> interarrivelTimeDist;

    /** maximum number of GTUs to generate. */
    private final long maxGTUs;

    /** start time of generation (delayed start). */
    private final OTSSimTimeDouble startTime;

    /** end time of generation. */
    private final OTSSimTimeDouble endTime;

    /** lane to generate the GTU on -- at the end for now. */
    private final Lane lane;

    /** number of generated GTUs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected long numberGTUs = 0;

    /**
     * @param name the name of the generator.
     * @param simulator the simulator to schedule the start of the generation.
     * @param gtuType the type of GTU to generate.
     * @param gtuClass the gtu class to instantiate.
     * @param gtuFollowingModel the GTU following model to use.
     * @param initialSpeedDist distribution of the initial speed of the GTU.
     * @param interarrivelTimeDist distribution of the interarrival time.
     * @param maxGTUs maximum number of GTUs to generate.
     * @param startTime start time of generation (delayed start).
     * @param endTime end time of generation.
     * @param lane the lane to generate the GTU on -- at the end for now.
     * @throws SimRuntimeException when simulation scheduling fails
     * @throws RemoteException when remote simulator cannot be reached
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGTUGenerator(final String name, final OTSDEVSSimulatorInterface simulator, final GTUType<ID> gtuType,
        final Class<GTU<ID>> gtuClass, final GTUFollowingModel gtuFollowingModel,
        final DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist,
        final DistContinuousDoubleScalar.Rel<TimeUnit> interarrivelTimeDist, final long maxGTUs,
        final OTSSimTimeDouble startTime, final OTSSimTimeDouble endTime, final Lane lane) throws RemoteException,
        SimRuntimeException
    {
        super();
        this.name = name;
        this.gtuType = gtuType;
        this.gtuClass = gtuClass;
        this.gtuFollowingModel = gtuFollowingModel;
        this.initialSpeedDist = initialSpeedDist;
        this.interarrivelTimeDist = interarrivelTimeDist;
        this.maxGTUs = maxGTUs;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lane = lane;

        simulator.scheduleEventAbs(startTime, this, this, "generate", null);
    }

    /**
     * Generate a GTU.
     * @throws SimRuntimeException when simulation scheduling fails
     * @throws RemoteException when remote simulator cannot be reached
     * @throws NetworkException when getId() method not found or when generation failed.
     * @throws NamingException when animation of the GTU could not be attached to the Context.
     */
    @SuppressWarnings("unchecked")
    protected final void generate() throws RemoteException, SimRuntimeException, NetworkException, NamingException
    {
        // get the return type of the getID() method of the GTU class
        Class<?> getidtype = String.class;
        try
        {
            Method getid = ClassUtil.resolveMethod(this.gtuClass, "getId", new Class<?>[] {});
            getidtype = getid.getReturnType();
        }
        catch (NoSuchMethodException exception)
        {
            throw new NetworkException("GTU class " + this.gtuClass.getName() + " does not have getId() method.", exception);
        }

        // create a unique id
        ID id = null;
        this.numberGTUs++;
        if (String.class.isAssignableFrom(getidtype))
        {
            id = (ID) new String(this.name + ":" + this.numberGTUs);
        }
        else if (int.class.isAssignableFrom(getidtype))
        {
            id = (ID) new Integer((int) this.numberGTUs);
        }
        else if (long.class.isAssignableFrom(getidtype))
        {
            id = (ID) new Long(this.numberGTUs);
        }
        else
        {
            throw new NetworkException("GTU ID class " + getidtype.getName() + ": cannot instantiate.");
        }

        // create the GTU
        if (LaneBasedIndividualCar.class.isAssignableFrom(getGtuClass()))
        {
            LaneBasedIndividualCarBuilder<ID> carBuilder = new LaneBasedIndividualCarBuilder<ID>();
            carBuilder.setId(id);
            carBuilder.setGtuType(getGtuType());
            carBuilder.setLength(getLengthDist().draw());
            carBuilder.setWidth(getWidthDist().draw());
            carBuilder.setMaximumVelocity(getMaximumSpeedDist().draw());
            carBuilder.setInitialSpeed(getInitialSpeedDist().draw());
            carBuilder.setSimulator(getSimulator());
            // TODO carBuilder.setInitialLongitudinalPositions(initialLongitudinalPositions);
            LaneBasedIndividualCar<ID> car = carBuilder.build();
        }
        else
        {
            throw new NetworkException("GTU class " + getGtuClass().getName() + ": cannot instantiate, no builder.");
        }

        // reschedule next arrival
        OTSSimTimeDouble nextTime = getSimulator().getSimulatorTime().plus(this.interarrivelTimeDist.draw());
        if (nextTime.le(this.endTime))
        {
            getSimulator().scheduleEventAbs(nextTime, this, this, "generate", null);
        }
    }

    /** @return simulator. */
    public abstract OTSDEVSSimulatorInterface getSimulator();

    /** @return lengthDist. */
    public abstract DistContinuousDoubleScalar.Rel<LengthUnit> getLengthDist();

    /** @return widthDist. */
    public abstract DistContinuousDoubleScalar.Rel<LengthUnit> getWidthDist();

    /** @return maximumSpeedDist. */
    public abstract DistContinuousDoubleScalar.Abs<SpeedUnit> getMaximumSpeedDist();

    /**
     * @return name.
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return gtuType.
     */
    public final GTUType<ID> getGtuType()
    {
        return this.gtuType;
    }

    /**
     * @return gtuClass.
     */
    public final Class<GTU<ID>> getGtuClass()
    {
        return this.gtuClass;
    }

    /**
     * @return gtuFollowingModel.
     */
    public final GTUFollowingModel getGtuFollowingModel()
    {
        return this.gtuFollowingModel;
    }

    /**
     * @return initialSpeedDist.
     */
    public final DistContinuousDoubleScalar.Abs<SpeedUnit> getInitialSpeedDist()
    {
        return this.initialSpeedDist;
    }

    /**
     * @return interarrivelTimeDist.
     */
    public final DistContinuousDoubleScalar.Rel<TimeUnit> getInterarrivelTimeDist()
    {
        return this.interarrivelTimeDist;
    }

    /**
     * @return maxGTUs.
     */
    public final long getMaxGTUs()
    {
        return this.maxGTUs;
    }

    /**
     * @return startTime.
     */
    public final OTSSimTimeDouble getStartTime()
    {
        return this.startTime;
    }

    /**
     * @return endTime.
     */
    public final OTSSimTimeDouble getEndTime()
    {
        return this.endTime;
    }

}
