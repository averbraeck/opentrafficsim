package org.opentrafficsim.demo.doc;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.perception.AbstractHistorical;
import org.opentrafficsim.core.perception.AbstractHistorical.EventValue;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;

// Collection of various small code examples in the documentation.
public class Documentation
{

    static
    {
        // @docs/04-demand/iat-generator.md
        Generator<Duration> constant = new ConstantGenerator<>(Duration.instantiateSI(2.0));

        // @docs/04-demand/traffic-od.md#nested-markov-chain-for-gtu-types
        MarkovCorrelation<GtuType, Frequency> markov = new MarkovCorrelation<>();
        GtuType truck = DefaultsNl.TRUCK;
        GtuType caccTruck = new GtuType("caccTruck", truck);
        markov.addState(truck, 0.4);
        markov.addState(truck, caccTruck, 0.64);
    }

    private void Neigbors(final LanePerception perception) throws OperationalPlanException
    {
        // @docs/05-perception/categorial.md
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
    }

    public static abstract class Hist<T, E extends EventValue<T>> extends AbstractHistorical<T, E>
    {
        // @docs/05-perception/historical.md
        private Historical<Length> odometer;

        // @docs/05-perception/historical.md (without 'public abstract')
        abstract void set(T value);

        abstract T get();

        abstract T get(Time time);

        private Hist(final HistoryManager historyManager)
        {
            super(historyManager);
            // @docs/05-perception/historical.md
            this.odometer = new HistoricalValue<>(historyManager, Length.ZERO);
        }

        T getValue(final Time time)
        {
            // @docs/05-perception/historical.md
            EventValue<T> event = getEvent(time);
            return event == null ? null : event.getValue();
        }
    }

    private void parameters() throws ParameterException
    {
        Parameters parameters = null;

        // @docs/06-behavior/parameters.md
        parameters.setParameter(ParameterTypes.A, Acceleration.instantiateSI(1.4));
        Acceleration a = parameters.getParameter(ParameterTypes.A);

        // @docs/06-behavior/parameters.md
        parameters.setParameterResettable(ParameterTypes.B, Acceleration.instantiateSI(3.5));
        parameters.resetParameter(ParameterTypes.B);
    }
}
