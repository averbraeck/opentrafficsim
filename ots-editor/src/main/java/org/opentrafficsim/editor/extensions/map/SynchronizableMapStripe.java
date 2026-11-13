package org.opentrafficsim.editor.extensions.map;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.StripeData;
import org.opentrafficsim.road.network.StripeData.StripePhaseSync;
import org.opentrafficsim.road.network.factory.xml.utils.StripeSynchronization.SynchronizableStripe;

/**
 * Representation of a stripe that can be synchronized as used within the editor.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class SynchronizableMapStripe implements SynchronizableStripe<MapStripeData>
{
    /** Link data. */
    private final MapLinkData linkData;

    /** Stripe data. */
    private final MapStripeData data;

    /** Synchronization. */
    private final StripePhaseSync phaseSync;

    /** Cached period. */
    private Double period = null;

    /** Dash offset as defined, before any synchronization. */
    private final Length initialDashOffset;

    /**
     * Constructor.
     * @param linkData link data
     * @param data stripe data
     * @param phaseSync synchronization
     */
    public SynchronizableMapStripe(final MapLinkData linkData, final MapStripeData data, final StripePhaseSync phaseSync)
    {
        this.linkData = linkData;
        this.data = data;
        this.phaseSync = phaseSync;
        this.initialDashOffset = data.getDashOffset();
    }

    /**
     * Resets the dash offset to the defined offset. This is required before each synchronization, as a stripe that is an anchor
     * (e.g. at a merge with multiple upstream stripes) is not synchronized and should keep its defined offset, rather than an
     * offset from a previous synchronization with incomplete geometry.
     */
    public void resetDashOffset()
    {
        this.data.setDashOffset(this.initialDashOffset);
    }

    @Override
    public double getStartPhase()
    {
        if (getPeriod() < 0.0)
        {
            return 0.0;
        }
        return (this.data.getDashOffset().si % getPeriod()) / getPeriod();
    }

    @Override
    public double getEndPhase()
    {
        if (getPeriod() < 0.0)
        {
            return 0.0;
        }
        return ((this.data.getReferenceLine().getLength() + this.data.getDashOffset().si) % getPeriod()) / getPeriod();
    }

    @Override
    public double getPeriod()
    {
        if (this.period == null)
        {
            this.period = StripeData.getPeriod(this.data.getElements());
        }
        return this.period;
    }

    @Override
    public MapStripeData getObject()
    {
        return this.data;
    }

    @Override
    public Optional<MapStripeData> getUpstreamStripe()
    {
        return getAdjacentStripe(true);
    }

    @Override
    public Optional<MapStripeData> getDownstreamStripe()
    {
        return getAdjacentStripe(false);
    }

    /**
     * Returns the stripe on an adjacent link that connects to this stripe at the node given by <cite>nodeAttribute</cite>.
     * @param fromStart {@code true} to get adjacent upstream stripe, {@code false} for adjacent downstream stripe
     * @return adjacent stripe, or empty if there is no adjacent stripe or multiple stripes connect
     */
    private Optional<MapStripeData> getAdjacentStripe(final boolean fromStart)
    {
        final String nodeAttribute = fromStart ? "NodeStart" : "NodeEnd";
        final String adjacentNodeAttribute = fromStart ? "NodeEnd" : "NodeStart";
        Optional<XsdTreeNode> node = this.linkData.getNode().getCoupledNodeAttribute(nodeAttribute);
        if (node.isEmpty())
        {
            return Optional.empty();
        }
        MapStripeData adjacent = null;
        for (XsdTreeNode networkChild : this.linkData.getNode().getParent().getChildren())
        {
            if (networkChild.getNodeName().equals("Link")
                    && node.get().equals(networkChild.getCoupledNodeAttribute(adjacentNodeAttribute).orElse(null)))
            {
                Optional<MapData> otherLink = this.linkData.getMap().getData(networkChild);
                if (otherLink.isEmpty())
                {
                    continue;
                }
                for (MapStripeData otherStripe : ((MapLinkData) otherLink.get()).getStripeData())
                {
                    Point2d otherPoint =
                            fromStart ? otherStripe.getCenterLine().getLast() : otherStripe.getCenterLine().getFirst();
                    Point2d thisPoint = fromStart ? this.data.getCenterLine().getFirst() : this.data.getCenterLine().getLast();
                    if (otherPoint.distance(thisPoint) < Lane.MARGIN.si)
                    {
                        if (adjacent != null)
                        {
                            // multiple stripes connect; no point in synchronizing, make stripe an anchor
                            return Optional.empty();
                        }
                        adjacent = otherStripe;
                    }
                }
            }
        }
        return Optional.ofNullable(adjacent);
    }

    @Override
    public Set<MapStripeData> getCommonPhaseStripes()
    {
        Set<MapStripeData> out = new LinkedHashSet<>();
        for (MapStripeData stripe : this.linkData.getStripeData())
        {
            SynchronizableMapStripe otherStripe = this.linkData.getMap().getSynchronizableStripes().get(stripe);
            if (otherStripe != null && !otherStripe.equals(this))
            {
                boolean equalPhaseSync = getSynchronization().equals(otherStripe.getSynchronization());
                boolean bothSyncUpOrDown = getSynchronization().isSync();
                boolean samePeriod = getPeriod() == otherStripe.getPeriod();
                boolean bothLatSyncToLink = this.data.getLateralSync().isLinkBased() && stripe.getLateralSync().isLinkBased();
                if (equalPhaseSync && bothSyncUpOrDown && samePeriod && bothLatSyncToLink)
                {
                    out.add(stripe);
                }
            }
        }
        return out;
    }

    @Override
    public StripePhaseSync getSynchronization()
    {
        return this.phaseSync;
    }

    @Override
    public void setStartPhase(final double phase)
    {
        this.data.setDashOffset(Length.ofSI(phase * getPeriod()));
    }

    @Override
    public void setEndPhase(final double phase)
    {
        double len = getPeriod() - ((this.data.getReferenceLine().getLength() - phase * getPeriod()) % getPeriod());
        this.data.setDashOffset(Length.ofSI(len));
    }

}
