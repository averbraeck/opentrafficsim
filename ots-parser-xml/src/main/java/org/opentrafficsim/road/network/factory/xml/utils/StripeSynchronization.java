package org.opentrafficsim.road.network.factory.xml.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.StripePhaseSync;

/**
 * Utility which allows synchronization of stripes.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> underlying stripe object type
 */
public final class StripeSynchronization<T>
{

    /** Stripes to consider. */
    private final Map<T, SynchronizableStripe<T>> stripes = new LinkedHashMap<>();

    /**
     * Constructor. To synchronize stripes use {@code StripeSynchronization.synchronize(Map<T, SynchronizableStripe<T>>)}.
     * @param stripes stripes
     */
    private StripeSynchronization(final Map<T, SynchronizableStripe<T>> stripes)
    {
        this.stripes.putAll(stripes);
    }

    /**
     * Synchronizes all given stripes.
     * @param stripes stripes
     * @param <T> underlying stripe object type
     */
    public static <T> void synchronize(final Map<T, SynchronizableStripe<T>> stripes)
    {
        new StripeSynchronization<>(stripes).synchronize();
    }

    /**
     * Synchronizes the stripes added to this synchronization.
     */
    private void synchronize()
    {
        List<SynchronizableStripe<T>> stripesCopy = new ArrayList<>();
        this.stripes.values().stream().filter((s) -> !s.getSynchronization().equals(StripePhaseSync.NONE))
                .forEach(stripesCopy::add);
        while (!stripesCopy.isEmpty())
        {
            synchronize(stripesCopy.get(0), stripesCopy);
        }
    }

    /**
     * Synchronizes the stripe.
     * @param stripe stripe
     * @param stripesToDo stripes still to process
     */
    private void synchronize(final SynchronizableStripe<T> stripe, final List<SynchronizableStripe<T>> stripesToDo)
    {
        if (!stripesToDo.contains(stripe))
        {
            // already synchronized
            return;
        }
        if (stripe.getSynchronization().equals(StripePhaseSync.DOWNSTREAM))
        {
            SynchronizableStripe<T> down = this.stripes.get(stripe.getDownstreamStripe());
            if (down != null)
            {
                if (stripesToDo.contains(down))
                {
                    // circular dependency; use as anchor instead
                    stripesToDo.remove(down);
                }
                else
                {
                    synchronize(down, stripesToDo);
                }
                stripe.setEndPhase(down.getStartPhase());
                for (T commonOffsetStripe : stripe.getCommonPhaseStripes())
                {
                    if (this.stripes.containsKey(commonOffsetStripe))
                    {
                        this.stripes.get(commonOffsetStripe).setEndPhase(down.getStartPhase());
                        stripesToDo.remove(commonOffsetStripe);
                    }
                }
            }
        }
        else if (stripe.getSynchronization().equals(StripePhaseSync.UPSTREAM))
        {
            SynchronizableStripe<T> up = this.stripes.get(stripe.getUpstreamStripe());
            if (up != null)
            {
                if (stripesToDo.contains(up))
                {
                    // circular dependency; use as anchor instead
                    stripesToDo.remove(up);
                }
                else
                {
                    synchronize(up, stripesToDo);
                }
                stripe.setStartPhase(up.getEndPhase());
                for (T commonOffsetStripe : stripe.getCommonPhaseStripes())
                {
                    if (this.stripes.containsKey(commonOffsetStripe))
                    {
                        this.stripes.get(commonOffsetStripe).setStartPhase(up.getEndPhase());
                        stripesToDo.remove(commonOffsetStripe);
                    }
                }
            }
        }
        stripesToDo.remove(stripe);
    }

    /**
     * Interface for a stripe such that the stripe synchronization can work with it.
     * @param <T> underlying stripe object type
     */
    public interface SynchronizableStripe<T>
    {
        /**
         * Returns the start phase.
         * @return start phase
         */
        double getStartPhase();

        /**
         * Returns the end phase.
         * @return end phase
         */
        double getEndPhase();

        /**
         * Returns the period as per {@code StripeSynchronization.getPeriod()}.
         * @return period
         */
        double getPeriod();

        /**
         * Returns the underlying object.
         * @return underlying object
         */
        T getObject();

        /**
         * Returns the upstream stripe.
         * @return upstream stripe
         */
        T getUpstreamStripe();

        /**
         * Returns the downstream stripe.
         * @return downstream stripe
         */
        T getDownstreamStripe();

        /**
         * Returns the stripes in the same link that have the same period, and which are synchronized upstream or downstream in
         * the same manner as this stripe. If this stripe is not synchronized, and empty set is returned.
         * @return set of stripes that should get the same phase
         */
        Set<T> getCommonPhaseStripes();

        /**
         * Returns the synchronization.
         * @return synchronization
         */
        StripePhaseSync getSynchronization();

        /**
         * Synchronizes the stripe by setting the start phase.
         * @param phase start phase
         */
        void setStartPhase(double phase);

        /**
         * Synchronizes the stripe by setting the end phase.
         * @param phase end phase
         */
        void setEndPhase(double phase);
    }

    /**
     * Returns synchronizable stripe from a {@code Stripe}.
     * @param stripe stripe
     * @return synchronizable stripe
     */
    public static SynchronizableStripe<Stripe> of(final Stripe stripe)
    {
        return new SynchronizableStripe<Stripe>()
        {
            @Override
            public double getStartPhase()
            {
                if (getPeriod() < 0.0)
                {
                    return 0.0;
                }
                return (stripe.getDashOffset().si % getPeriod()) / getPeriod();
            }

            @Override
            public double getEndPhase()
            {
                if (getPeriod() < 0.0)
                {
                    return 0.0;
                }
                return ((stripe.getLength().si + stripe.getDashOffset().si) % getPeriod()) / getPeriod();
            }

            @Override
            public Stripe getObject()
            {
                return stripe;
            }

            @Override
            public double getPeriod()
            {
                return stripe.getPeriod();
            }

            @Override
            public Stripe getUpstreamStripe()
            {
                Stripe upstream = null;
                for (Link upstreamLink : stripe.getLink().getStartNode().getLinks())
                {
                    if (!upstreamLink.equals(stripe.getLink())
                            && upstreamLink.getEndNode().equals(stripe.getLink().getStartNode())
                            && upstreamLink instanceof CrossSectionLink crossSectionLink)
                    {
                        for (CrossSectionElement element : crossSectionLink.getCrossSectionElementList())
                        {
                            if (element instanceof Stripe upstreamStripe && upstreamStripe.getCenterLine().getLast()
                                    .distance(stripe.getCenterLine().getFirst()) < Lane.MARGIN.si)
                            {
                                if (upstream != null)
                                {
                                    // multiple upstream stripes, no point in synchronizing, make stripe an anchor
                                    return null;
                                }
                                upstream = upstreamStripe;
                            }
                        }
                    }
                }
                return upstream;
            }

            @Override
            public Stripe getDownstreamStripe()
            {
                Stripe downstream = null;
                for (Link downstreamLink : stripe.getLink().getEndNode().getLinks())
                {
                    if (!downstreamLink.equals(stripe.getLink())
                            && downstreamLink.getStartNode().equals(stripe.getLink().getEndNode())
                            && downstreamLink instanceof CrossSectionLink crossSectionLink)
                    {
                        for (CrossSectionElement element : crossSectionLink.getCrossSectionElementList())
                        {
                            if (element instanceof Stripe downstreamStripe && downstreamStripe.getCenterLine().getFirst()
                                    .distance(stripe.getCenterLine().getLast()) < Lane.MARGIN.si)
                            {
                                if (downstream != null)
                                {
                                    // multiple downstream stripes, no point in synchronizing, make stripe an anchor
                                    return null;
                                }
                                downstream = downstreamStripe;
                            }
                        }
                    }
                }
                return downstream;
            }

            @Override
            public Set<Stripe> getCommonPhaseStripes()
            {
                Set<Stripe> out = new LinkedHashSet<>();
                for (CrossSectionElement element : stripe.getLink().getCrossSectionElementList())
                {
                    if (element instanceof Stripe otherStripe && !otherStripe.equals(stripe))
                    {
                        boolean equalPhaseSync = stripe.getPhaseSync().equals(otherStripe.getPhaseSync());
                        boolean bothSyncUpOrDown = stripe.getPhaseSync().isSync();
                        boolean samePeriod = stripe.getPeriod() == otherStripe.getPeriod();
                        boolean bothLatSyncToLink =
                                stripe.getLateralSync().isLinkBased() && otherStripe.getLateralSync().isLinkBased();
                        if (equalPhaseSync && bothSyncUpOrDown && samePeriod && bothLatSyncToLink)
                        {
                            out.add(otherStripe);
                        }
                    }
                }
                return out;
            }

            @Override
            public StripePhaseSync getSynchronization()
            {
                return stripe.getPhaseSync();
            }

            @Override
            public void setStartPhase(final double phase)
            {
                stripe.setDashOffset(Length.instantiateSI(phase * getPeriod()));
            }

            @Override
            public void setEndPhase(final double phase)
            {
                double len = getPeriod() - ((stripe.getCenterLine().getLength() - phase * getPeriod()) % getPeriod());
                stripe.setDashOffset(Length.instantiateSI(len));
            }
        };
    }

}
