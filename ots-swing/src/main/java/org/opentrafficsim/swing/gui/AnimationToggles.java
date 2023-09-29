package org.opentrafficsim.swing.gui;

import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.draw.road.DetectorData;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.LaneAnimation.CenterLine;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Set the default animation toggles for the animation panel.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class AnimationToggles
{

    /**
     * Do not instantiate this class.
     */
    private AnimationToggles()
    {
        // static class.
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel OtsAnimationPanel; the WrappableAnimation.
     */
    public static void setTextAnimationTogglesFull(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", NodeData.class, "Show/hide nodes", true);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", LinkData.class, "Show/hide links", true);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", LaneData.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("LaneCenter", LaneAnimation.CenterLine.class, "Show/hide lane center lines", false);
        panel.addToggleAnimationButtonText("Stripe", StripeData.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", CrossSectionElementData.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", GtuData.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Detector", DetectorData.class, "Show/hide detectors", true);
        panel.addToggleAnimationButtonText("DetectorId", DetectorData.Text.class, "Show/hide detector Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLightData.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide traffic light Ids", false);
        panel.addToggleAnimationButtonText("Conflict", ConflictData.class, "Show/hide conflicts", true);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorPositionData.class, "Show/hide generators", true);
        panel.addToggleAnimationButtonText("GeneratorQ", GtuGeneratorPositionAnimation.Queue.class,
                "Show/hide generator queues", false);
        panel.addToggleAnimationButtonText("Bus", BusStopData.class, "Show/hide bus stops", true);
        panel.addToggleAnimationButtonText("BusId", BusStopAnimation.Text.class, "Show/hide bus stop Ids", false);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OtsAnimationPanel; the WrappableAnimation.
     */
    public static void setTextAnimationTogglesStandard(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", NodeData.class, "Show/hide nodes", false);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", LinkData.class, "Show/hide links", false);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", LaneData.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("LaneCenter", LaneAnimation.CenterLine.class, "Show/hide lane center lines", false);
        panel.addToggleAnimationButtonText("Stripe", StripeData.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", CrossSectionElementData.class, "Show/hide shoulders", true); // Shoulder
        panel.addToggleAnimationButtonText("GTU", GtuData.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Detector", DetectorData.class, "Show/hide detectors", false);
        panel.addToggleAnimationButtonText("DetectorId", DetectorData.Text.class, "Show/hide detector Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLightData.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide traffic light Ids", false);
        panel.addToggleAnimationButtonText("Conflict", ConflictData.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorPositionData.class, "Show/hide generators", false);
        panel.addToggleAnimationButtonText("GeneratorQ", GtuGeneratorPositionAnimation.Queue.class,
                "Show/hide generator queues", false);
        panel.addToggleAnimationButtonText("Bus", BusStopData.class, "Show/hide bus stops", false);
        panel.addToggleAnimationButtonText("BusId", BusStopAnimation.Text.class, "Show/hide bus stop Ids", false);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel OtsAnimationPanel; the WrappableAnimation.
     */
    public static void setIconAnimationTogglesFull(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonIcon("Node", NodeData.class, "/icons/Node24.png", "Show/hide nodes", true, false);
        panel.addToggleAnimationButtonIcon("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Link", LinkData.class, "/icons/Link24.png", "Show/hide links", true, false);
        panel.addToggleAnimationButtonIcon("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Lane", LaneData.class, "/icons/Lane24.png", "Show/hide lanes", true, false);
        panel.addToggleAnimationButtonIcon("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("LaneCenter", CenterLine.class, "/icons/CenterLine24.png",
                "Show/hide lane center lines", false, false);
        panel.addToggleAnimationButtonIcon("Stripe", StripeData.class, "/icons/Stripe24.png", "Show/hide stripes", true, false);
        panel.addToggleAnimationButtonIcon("Shoulder", CrossSectionElementData.class, "/icons/Shoulder24.png",
                "Show/hide shoulders", true, false); // Shoulder
        panel.addToggleAnimationButtonIcon("GTU", GtuData.class, "/icons/Gtu24.png", "Show/hide GTUs", true, false);
        panel.addToggleAnimationButtonIcon("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids",
                false, true);
        panel.addToggleAnimationButtonIcon("Detector", DetectorData.class, "/icons/Detector24.png", "Show/hide detectors", true,
                false);
        panel.addToggleAnimationButtonIcon("DetectorId", DetectorData.Text.class, "/icons/Id24.png",
                "Show/hide detector Ids", false, true);
        panel.addToggleAnimationButtonIcon("Light", TrafficLightData.class, "/icons/TrafficLight24.png",
                "Show/hide traffic lights", true, false);
        panel.addToggleAnimationButtonIcon("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png",
                "Show/hide traffic light Ids", false, true);
        panel.addToggleAnimationButtonIcon("Conflict", ConflictData.class, "/icons/Conflict24.png", "Show/hide conflicts", true,
                false);
        panel.addToggleAnimationButtonIcon("Generator", GtuGeneratorPositionData.class, "/icons/Generator24.png",
                "Show/hide generators", true, false);
        panel.addToggleAnimationButtonIcon("GeneratorQ", GtuGeneratorPositionAnimation.Queue.class, "/icons/Queue24.png",
                "Show/hide generator queues", false, true);
        panel.addToggleAnimationButtonIcon("Bus", BusStopData.class, "/icons/BusStop24.png", "Show/hide bus stops", true,
                false);
        panel.addToggleAnimationButtonIcon("BusId", BusStopAnimation.Text.class, "/icons/Id24.png", "Show/hide bus stops Ids",
                false, true);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OtsAnimationPanel; the WrappableAnimation.
     */
    public static void setIconAnimationTogglesStandard(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonIcon("Node", NodeData.class, "/icons/Node24.png", "Show/hide nodes", false, false);
        panel.addToggleAnimationButtonIcon("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Link", LinkData.class, "/icons/Link24.png", "Show/hide links", false, false);
        panel.addToggleAnimationButtonIcon("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Lane", LaneData.class, "/icons/Lane24.png", "Show/hide lanes", true, false);
        panel.addToggleAnimationButtonIcon("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("LaneCenter", CenterLine.class, "/icons/CenterLine24.png",
                "Show/hide lane center lines", false, false);
        panel.addToggleAnimationButtonIcon("Stripe", StripeData.class, "/icons/Stripe24.png", "Show/hide stripes", true, false);
        panel.addToggleAnimationButtonIcon("Shoulder", CrossSectionElementData.class, "/icons/Shoulder24.png",
                "Show/hide shoulders", true, false); // Shoulder
        panel.addToggleAnimationButtonIcon("GTU", GtuData.class, "/icons/Gtu24.png", "Show/hide GTUs", true, false);
        panel.addToggleAnimationButtonIcon("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids",
                false, true);
        panel.addToggleAnimationButtonIcon("Detector", DetectorData.class, "/icons/Detector24.png", "Show/hide detectors",
                false, false);
        panel.addToggleAnimationButtonIcon("DetectorId", DetectorData.Text.class, "/icons/Id24.png",
                "Show/hide detector Ids", false, true);
        panel.addToggleAnimationButtonIcon("Light", TrafficLightData.class, "/icons/TrafficLight24.png",
                "Show/hide traffic lights", true, false);
        panel.addToggleAnimationButtonIcon("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png",
                "Show/hide traffic light Ids", false, true);
        panel.addToggleAnimationButtonIcon("Conflict", ConflictData.class, "/icons/Conflict24.png", "Show/hide conflicts",
                false, false);
        panel.addToggleAnimationButtonIcon("Generator", GtuGeneratorPositionData.class, "/icons/Generator24.png",
                "Show/hide generators", false, false);
        panel.addToggleAnimationButtonIcon("GeneratorQ", GtuGeneratorPositionAnimation.Queue.class, "/icons/Queue24.png",
                "Show/hide generator queues", false, true);
        panel.addToggleAnimationButtonIcon("Bus", BusStopData.class, "/icons/BusStop24.png", "Show/hide bus stops", false,
                false);
        panel.addToggleAnimationButtonIcon("BusId", BusStopAnimation.Text.class, "/icons/Id24.png", "Show/hide bus stops Ids",
                false, true);
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param panel OtsAnimationPanel; the OtsAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public static void showAnimationClass(final OtsAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().showClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Set a class to be shown in the animation to false.
     * @param panel OtsAnimationPanel; the OtsAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public static void hideAnimationClass(final OtsAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().hideClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel OtsAnimationPanel; the WrappableAnimation.
     */
    public static void showAnimationFull(final OtsAnimationPanel panel)
    {
        showAnimationClass(panel, NodeData.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        showAnimationClass(panel, LinkData.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, LaneData.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        hideAnimationClass(panel, LaneAnimation.CenterLine.class);
        showAnimationClass(panel, StripeData.class);
        showAnimationClass(panel, CrossSectionElementData.class); // Shoulder
        showAnimationClass(panel, GtuData.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        showAnimationClass(panel, DetectorData.class);
        hideAnimationClass(panel, DetectorData.Text.class);
        showAnimationClass(panel, TrafficLightData.class);
        hideAnimationClass(panel, TrafficLightAnimation.Text.class);
        showAnimationClass(panel, ConflictData.class);
        showAnimationClass(panel, GtuGeneratorPositionData.class);
        hideAnimationClass(panel, GtuGeneratorPositionAnimation.Queue.class);
        showAnimationClass(panel, BusStopData.class);
        hideAnimationClass(panel, BusStopAnimation.Text.class);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OtsAnimationPanel; the WrappableAnimation.
     */
    public static void showAnimationStandard(final OtsAnimationPanel panel)
    {
        hideAnimationClass(panel, NodeData.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        hideAnimationClass(panel, LinkData.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, LaneData.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        hideAnimationClass(panel, LaneAnimation.CenterLine.class);
        showAnimationClass(panel, StripeData.class);
        showAnimationClass(panel, CrossSectionElementData.class); // Shoulder
        showAnimationClass(panel, GtuData.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        hideAnimationClass(panel, DetectorData.class);
        hideAnimationClass(panel, DetectorData.Text.class);
        showAnimationClass(panel, TrafficLightData.class);
        hideAnimationClass(panel, TrafficLightAnimation.Text.class);
        hideAnimationClass(panel, ConflictData.class);
        hideAnimationClass(panel, GtuGeneratorPositionData.class);
        hideAnimationClass(panel, GtuGeneratorPositionAnimation.Queue.class);
        hideAnimationClass(panel, BusStopData.class);
        hideAnimationClass(panel, BusStopAnimation.Text.class);
    }

}
