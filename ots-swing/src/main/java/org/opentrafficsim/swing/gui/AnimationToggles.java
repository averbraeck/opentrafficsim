package org.opentrafficsim.swing.gui;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.animation.PerceptionAnimation;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.ShoulderData;
import org.opentrafficsim.draw.road.DetectorData;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.LaneAnimation.CenterLine;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.draw.road.PriorityAnimation.PriorityData;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Set the default animation toggles for the simulation panel.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AnimationToggles
{

    /** Toggles. */
    private static final List<Toggle> TOGGLES = new ArrayList<>();

    static
    {
        TOGGLES.add(new Toggle("Node", NodeData.class, "Node24.png", "Show/hide nodes", true, false, false));
        TOGGLES.add(new Toggle("NodeId", NodeAnimation.Text.class, "Id24.png", "Show/hide node ids", false, false, true));
        TOGGLES.add(new Toggle("Link", LinkData.class, "Link24.png", "Show/hide links", true, false, false));
        TOGGLES.add(new Toggle("LinkId", LinkAnimation.Text.class, "Id24.png", "Show/hide link ids", false, false, true));
        TOGGLES.add(
                new Toggle("Priority", PriorityData.class, "Priority24.png", "Show/hide link priority", true, false, false));
        TOGGLES.add(new Toggle("Lane", LaneData.class, "Lane24.png", "Show/hide lanes", true, true, false));
        TOGGLES.add(new Toggle("LaneId", LaneAnimation.Text.class, "Id24.png", "Show/hide lane ids", false, false, true));
        TOGGLES.add(new Toggle("Stripe", StripeData.class, "Stripe24.png", "Show/hide stripes", true, true, false));
        TOGGLES.add(new Toggle("LaneCenter", CenterLine.class, "CenterLine24.png", "Show/hide lane center lines", false, false,
                true));
        TOGGLES.add(new Toggle("Shoulder", ShoulderData.class, "Shoulder24.png", "Show/hide shoulders", true, true, false));
        TOGGLES.add(new Toggle("GTU", GtuData.class, "Gtu24.png", "Show/hide GTUs", true, true, false));
        TOGGLES.add(new Toggle("GTUId", DefaultCarAnimation.Text.class, "Id24.png", "Show/hide GTU ids", false, false, true));
        TOGGLES.add(new Toggle("Perception", PerceptionAnimation.ChannelAttention.class, "Perception24.png",
                "Show/hide perception (circle = attention, color = perception delay)", false, false, false));
        TOGGLES.add(new Toggle("Detector", DetectorData.class, "Detector24.png", "Show/hide detectors", true, false, false));
        TOGGLES.add(
                new Toggle("DetectorId", DetectorData.Text.class, "Id24.png", "Show/hide detector ids", false, false, true));
        TOGGLES.add(new Toggle("Light", TrafficLightData.class, "TrafficLight24.png", "Show/hide traffic lights", true, true,
                false));
        TOGGLES.add(new Toggle("LightId", TrafficLightAnimation.Text.class, "Id24.png", "Show/hide traffic light ids", false,
                false, true));
        TOGGLES.add(new Toggle("Conflict", ConflictData.class, "Conflict24.png", "Show/hide conflicts", true, false, false));
        TOGGLES.add(new Toggle("Generator", GtuGeneratorPositionData.class, "Generator24.png", "Show/hide generators", true,
                false, false));
        TOGGLES.add(new Toggle("GeneratorQ", GtuGeneratorPositionAnimation.Queue.class, "Queue24.png",
                "Show/hide generator queues", false, false, true));
        TOGGLES.add(new Toggle("Bus", BusStopData.class, "BusStop24.png", "Show/hide bus stops", true, false, false));
        TOGGLES.add(new Toggle("BusId", BusStopAnimation.Text.class, "Id24.png", "Show/hide bus stop ids", false, false, true));
    }

    /**
     * Do not instantiate this class.
     */
    private AnimationToggles()
    {
        // static class.
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel simulation panel
     */
    public static void setTextAnimationTogglesFull(final OtsSimulationPanel panel)
    {
        TOGGLES.forEach((t) -> panel.addToggleAnimationButtonText(t.name(), t.locatableClass(), t.tooltip(), t.visibleFull()));
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel the simulation panel
     */
    public static void setTextAnimationTogglesStandard(final OtsSimulationPanel panel)
    {
        TOGGLES.forEach(
                (t) -> panel.addToggleAnimationButtonText(t.name(), t.locatableClass(), t.tooltip(), t.visibleStandard()));
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel the simulation panel
     */
    public static void setIconAnimationTogglesFull(final OtsSimulationPanel panel)
    {
        TOGGLES.forEach((t) -> panel.addToggleAnimationButtonIcon(t.name(), t.locatableClass(), t.icon(), t.tooltip(),
                t.visibleFull(), t.nextToPrevious()));
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel the simulation panel
     */
    public static void setIconAnimationTogglesStandard(final OtsSimulationPanel panel)
    {
        TOGGLES.forEach((t) -> panel.addToggleAnimationButtonIcon(t.name(), t.locatableClass(), t.icon(), t.tooltip(),
                t.visibleStandard(), t.nextToPrevious()));
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param panel the simulation panel
     * @param locatableClass the class for which the animation has to be shown
     */
    public static void showAnimationClass(final OtsSimulationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().showClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Set a class to be shown in the animation to false.
     * @param panel the simulation panel
     * @param locatableClass the class for which the animation has to be hidden
     */
    public static void hideAnimationClass(final OtsSimulationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().hideClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel the simulation panel
     */
    public static void showAnimationFull(final OtsSimulationPanel panel)
    {
        TOGGLES.forEach((t) ->
        {
            if (t.visibleFull())
            {
                showAnimationClass(panel, t.locatableClass());
            }
            else
            {
                hideAnimationClass(panel, t.locatableClass());
            }
        });
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel the simulation panel
     */
    public static void showAnimationStandard(final OtsSimulationPanel panel)
    {
        TOGGLES.forEach((t) ->
        {
            if (t.visibleStandard())
            {
                showAnimationClass(panel, t.locatableClass());
            }
            else
            {
                hideAnimationClass(panel, t.locatableClass());
            }
        });
    }

    /**
     * Record to hold data pertaining to a toggle.
     * @param name name of the toggle
     * @param locatableClass type of the locatable that is toggled
     * @param icon name of the icon
     * @param tooltip tooltip on the toggle
     * @param visibleFull visible under full toggles
     * @param visibleStandard visible under standard toggles
     * @param nextToPrevious whether to place the icon toggle next to the previous
     */
    private record Toggle(String name, Class<? extends Locatable> locatableClass, String icon, String tooltip,
            boolean visibleFull, boolean visibleStandard, boolean nextToPrevious)
    {
    };
}
