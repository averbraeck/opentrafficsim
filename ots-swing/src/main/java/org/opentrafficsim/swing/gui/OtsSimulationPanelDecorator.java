package org.opentrafficsim.swing.gui;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.animation.DefaultAnimationFactory;
import org.opentrafficsim.animation.gtu.colorer.AccelerationGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.IdGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SpeedGtuColorer;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.draw.Colors;
import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.draw.colorer.FixedColorer;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData.GtuMarker;

/**
 * Decorator for the {@link OtsSimulationPanel}. Default method implementations are provided which sub-classes may implement
 * with different decoration, e.g. additional tabs.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface OtsSimulationPanelDecorator
{

    /** Default GTU colorers. */
    // List.of(...) results in an unmodifiable list
    List<Colorer<? super Gtu>> DEFAULT_GTU_COLORERS = List.of(new FixedColorer<>(Colors.OTS_BLUE, "Blue"), new IdGtuColorer(),
            new SpeedGtuColorer(), new AccelerationGtuColorer());

    /** Standard drawing colors for GTU types. */
    @SuppressWarnings("serial")
    ImmutableMap<GtuType, Color> GTU_TYPE_COLORS = new ImmutableLinkedHashMap<>(new LinkedHashMap<>()
    {
        {
            put(DefaultsNl.CAR, Color.BLUE);
            put(DefaultsNl.TRUCK, Color.RED);
            put(DefaultsNl.VEHICLE, Color.GRAY);
            put(DefaultsNl.PEDESTRIAN, Color.YELLOW);
            put(DefaultsNl.MOTORCYCLE, Color.PINK);
            put(DefaultsNl.BICYCLE, Color.GREEN);
        }
    }, Immutable.WRAP);

    /** Standard markers for GTU types. */
    @SuppressWarnings("serial")
    ImmutableMap<GtuType, GtuMarker> GTU_TYPE_MARKERS = new ImmutableLinkedHashMap<>(new LinkedHashMap<>()
    {
        {
            put(DefaultsNl.TRUCK, GtuMarker.SQUARE);
        }
    }, Immutable.WRAP);

    /**
     * Invokes all decoration methods.
     * @param simulationPanel simulation panel
     * @param network network
     */
    default void decorate(final OtsSimulationPanel simulationPanel, final Network network)
    {
        setAnimationToggles(simulationPanel);
        animateSimulation(simulationPanel, network);
        setupDemo(simulationPanel, network);
        addTabs(simulationPanel, network);
    }

    /**
     * Set simulation toggles. The default implementation sets standard icon toggles.
     * @param simulationPanel simulation panel
     */
    default void setAnimationToggles(final OtsSimulationPanel simulationPanel)
    {
        AnimationToggles.setIconAnimationTogglesStandard(simulationPanel);
    }

    /**
     * Creates the animation objects. This method is overridable. The default uses {@link DefaultAnimationFactory}.
     * @param simulationPanel simulation panel
     * @param network network
     */
    default void animateSimulation(final OtsSimulationPanel simulationPanel, final Network network)
    {
        DefaultAnimationFactory.animateNetwork(network, network.getSimulator(), simulationPanel.getGtuColorerManager(),
                getGtuMarkers());
    }

    /**
     * Adds tabs. The default implementation does nothing.
     * @param simulationPanel simulation panel
     * @param network network
     */
    default void addTabs(final OtsSimulationPanel simulationPanel, final Network network)
    {
        //
    }

    /**
     * Setup a demo panel within the simulation panel. The default implementation does nothing.
     * @param simulationPanel simulation panel
     * @param network network
     */
    default void setupDemo(final OtsSimulationPanel simulationPanel, final Network network)
    {
        //
    }

    /**
     * Return GTU colorers. The default implementation returns the default colorers.
     * @return GTU colorers
     */
    default List<Colorer<? super Gtu>> getGtuColorers()
    {
        return DEFAULT_GTU_COLORERS;
    }

    /**
     * Returns a map of markers to use on GTU types. The default implementation returns default markers (NL.TRUCK as square).
     * @return map of markers to use on GTU types
     */
    default Map<GtuType, GtuMarker> getGtuMarkers()
    {
        return GTU_TYPE_MARKERS.toMap();
    }

}
