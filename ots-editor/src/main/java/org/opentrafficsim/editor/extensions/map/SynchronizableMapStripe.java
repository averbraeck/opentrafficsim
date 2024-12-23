package org.opentrafficsim.editor.extensions.map;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.factory.xml.utils.StripeSynchronization.SynchronizableStripe;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.StripeData;
import org.opentrafficsim.road.network.lane.StripeData.StripePhaseSync;

/**
 * Representation of a stripe that can be synchronized as used within the editor.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
        return ((this.data.getCenterLine().getLength() + this.data.getDashOffset().si) % getPeriod()) / getPeriod();
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
    public MapStripeData getUpstreamStripe()
    {
        XsdTreeNode fromNode = this.linkData.getNode().getCoupledKeyrefNodeAttribute("NodeStart");
        if (fromNode == null)
        {
            return null;
        }
        for (XsdTreeNode networkChild : this.linkData.getNode().getParent().getChildren())
        {
            if (networkChild.getNodeName().equals("Link")
                    && fromNode.equals(networkChild.getCoupledKeyrefNodeAttribute("NodeEnd")))
            {
                MapLinkData otherLink = (MapLinkData) this.linkData.getMap().getData(networkChild);
                if (otherLink == null)
                {
                    return null;
                }
                for (MapStripeData otherStripe : otherLink.getStripeData())
                {
                    if (otherStripe.getCenterLine().getLast().distance(this.data.getCenterLine().getFirst()) < Lane.MARGIN.si)
                    {
                        return otherStripe;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public MapStripeData getDownstreamStripe()
    {
        XsdTreeNode fromNode = this.linkData.getNode().getCoupledKeyrefNodeAttribute("NodeEnd");
        if (fromNode == null)
        {
            return null;
        }
        for (XsdTreeNode networkChild : this.linkData.getNode().getParent().getChildren())
        {
            if (networkChild.getNodeName().equals("Link")
                    && fromNode.equals(networkChild.getCoupledKeyrefNodeAttribute("NodeStart")))
            {
                MapLinkData otherLink = (MapLinkData) this.linkData.getMap().getData(networkChild);
                if (otherLink == null)
                {
                    return null;
                }
                for (MapStripeData otherStripe : otherLink.getStripeData())
                {
                    if (otherStripe.getCenterLine().getFirst().distance(this.data.getCenterLine().getLast()) < Lane.MARGIN.si)
                    {
                        return otherStripe;
                    }
                }
            }
        }
        return null;
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
        this.data.setDashOffset(Length.instantiateSI(phase * getPeriod()));
    }

    @Override
    public void setEndPhase(final double phase)
    {
        double len = getPeriod() - ((this.data.getCenterLine().getLength() - phase * getPeriod()) % getPeriod());
        this.data.setDashOffset(Length.instantiateSI(len));
    }

}
