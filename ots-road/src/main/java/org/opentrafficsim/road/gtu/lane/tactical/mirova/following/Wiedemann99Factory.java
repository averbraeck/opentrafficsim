package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import java.util.function.Supplier;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

public class Wiedemann99Factory extends AbstractWiedemannFactory<Wiedemann99>
{
    /**
     * Constructor.
     * @param randomStream random number stream
     */
    public Wiedemann99Factory(final StreamInterface randomStream)
    {
        super(() -> new Wiedemann99(randomStream), randomStream);
    }

    @Override
    public final String toString()
    {
        return "IDMPlusFactory";
    }

}
