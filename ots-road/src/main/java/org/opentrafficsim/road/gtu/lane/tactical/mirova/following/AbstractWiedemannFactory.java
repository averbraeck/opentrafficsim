package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import java.util.function.Supplier;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractCarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

public class AbstractWiedemannFactory<T extends AbstractWiedemannModel> implements CarFollowingModelFactory<T>
{

    /** Single instance as it is state-less. */
    private final Supplier<T> wiedemann;

    /** Distribution for fSpeed parameter. */
    private final DistContinuous fSpeed;

    private final StreamInterface randomStream;

    /**
     * Sets the w99 model.
     * @param w99 w99 model supplier
     * @param randomStream random number stream
     */
    public AbstractWiedemannFactory(final Supplier<T> wiedemann, final StreamInterface randomStream)
    {
        this.wiedemann = wiedemann;
        this.randomStream = randomStream;
        this.fSpeed = new DistNormal(randomStream, 123.7 / 120.0, 0.1);
    }

    @Override
    public final T generateCarFollowingModel()
    {
        return this.wiedemann.get();
    }

    @Override
    public Parameters getParameters() throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        parameters.setDefaultParameters(AbstractWiedemannModel.class);
        parameters.setParameter(AbstractWiedemannModel.FSPEED, this.fSpeed.draw());
        return parameters;
    }

}