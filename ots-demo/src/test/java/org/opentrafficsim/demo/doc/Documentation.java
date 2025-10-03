package org.opentrafficsim.demo.doc;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantSupplier;
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

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings({"unused", "javadoc", "null"})
public class Documentation
{

    static
    {
        // @docs/04-demand/iat-generator.md
        Supplier<Duration> constant = new ConstantSupplier<>(Duration.instantiateSI(2.0));

        // @docs/04-demand/traffic-od.md#nested-markov-chain-for-gtu-types
        MarkovCorrelation<GtuType, Frequency> markov = new MarkovCorrelation<>();
        GtuType truck = DefaultsNl.TRUCK;
        GtuType caccTruck = new GtuType("caccTruck", truck);
        markov.addState(truck, 0.4);
        markov.addState(truck, caccTruck, 0.64);
    }

    private void neigbors(final LanePerception perception) throws OperationalPlanException
    {
        // @docs/05-perception/categorial.md
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
    }

    public abstract static class Hist<T, E extends EventValue<T>> extends AbstractHistorical<T, E>
    {
        /** */
        // @docs/05-perception/historical.md
        private Historical<Length> odometer;

        // @docs/05-perception/historical.md (without 'public abstract')
        abstract void set(T value);

        abstract T get();

        abstract T get(Time time);

        private Hist(final HistoryManager historyManager, final Object owner)
        {
            super(historyManager, owner);
            // @docs/05-perception/historical.md
            this.odometer = new HistoricalValue<>(historyManager, this, Length.ZERO);
        }

        T getValue(final Duration time)
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
