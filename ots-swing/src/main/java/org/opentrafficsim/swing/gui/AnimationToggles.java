package org.opentrafficsim.swing.gui;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.OtsLink;
import org.opentrafficsim.core.network.OtsNode;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.gtu.GtuGeneratorQueueAnimation;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.DetectorAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.LaneAnimation.CenterLine;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.lane.object.detector.Detector;
import org.opentrafficsim.road.network.lane.object.detector.DetectorAnimationToggle;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Set the default animation toggles for the animation panel.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setTextAnimationTogglesFull(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", NodeAnimation.ElevatedNode.class, "Show/hide nodes", true);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", OtsLink.class, "Show/hide links", true);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", Lane.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("LaneCenter", LaneAnimation.CenterLine.class, "Show/hide lane center lines", false);
        panel.addToggleAnimationButtonText("Stripe", Stripe.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", Gtu.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Detector", Detector.class, "Show/hide detectors", true);
        panel.addToggleAnimationButtonText("DetectorId", DetectorAnimation.Text.class, "Show/hide detector Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide traffic light Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorQueueAnimation.class, "Show/hide generators", false);
        panel.addToggleAnimationButtonText("Bus", BusStop.class, "Show/hide bus stops", false);
        panel.addToggleAnimationButtonText("BusId", BusStopAnimation.Text.class, "Show/hide bus stop Ids", false);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setTextAnimationTogglesStandard(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", NodeAnimation.ElevatedNode.class, "Show/hide nodes", false);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", OtsLink.class, "Show/hide links", false);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", Lane.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("LaneCenter", LaneAnimation.CenterLine.class, "Show/hide lane center lines", false);
        panel.addToggleAnimationButtonText("Stripe", Stripe.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", Gtu.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Detector", DetectorAnimationToggle.class, "Show/hide detectors", false);
        panel.addToggleAnimationButtonText("DetectorId", DetectorAnimation.Text.class, "Show/hide detector Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide traffic light Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorQueueAnimation.class, "Show/hide generators", false);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setIconAnimationTogglesFull(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonIcon("Node", NodeAnimation.ElevatedNode.class, "/icons/Node24.png", "Show/hide nodes",
                true, false);
        panel.addToggleAnimationButtonIcon("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Link", OtsLink.class, "/icons/Link24.png", "Show/hide links", true, false);
        panel.addToggleAnimationButtonIcon("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Lane", Lane.class, "/icons/Lane24.png", "Show/hide lanes", true, false);
        panel.addToggleAnimationButtonIcon("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("LaneCenter", CenterLine.class, "/icons/CenterLine24.png",
                "Show/hide lane center lines", false, false);
        panel.addToggleAnimationButtonIcon("Stripe", Stripe.class, "/icons/Stripe24.png", "Show/hide stripes", true, false);
        panel.addToggleAnimationButtonIcon("Shoulder", Shoulder.class, "/icons/Shoulder24.png", "Show/hide shoulders", true,
                false);
        panel.addToggleAnimationButtonIcon("GTU", Gtu.class, "/icons/Gtu24.png", "Show/hide GTUs", true, false);
        panel.addToggleAnimationButtonIcon("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids",
                false, true);
        panel.addToggleAnimationButtonIcon("Detector", DetectorAnimationToggle.class, "/icons/Detector24.png",
                "Show/hide detectors", true, false);
        panel.addToggleAnimationButtonIcon("DetectorId", DetectorAnimation.Text.class, "/icons/Id24.png",
                "Show/hide detector Ids", false, true);
        panel.addToggleAnimationButtonIcon("Light", TrafficLight.class, "/icons/TrafficLight24.png", "Show/hide traffic lights",
                true, false);
        panel.addToggleAnimationButtonIcon("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png",
                "Show/hide traffic light Ids", false, true);
        panel.addToggleAnimationButtonIcon("Conflict", Conflict.class, "/icons/Conflict24.png", "Show/hide conflicts", false,
                false);
        panel.addToggleAnimationButtonIcon("Generator", GtuGeneratorQueueAnimation.class, "/icons/Generator24.png",
                "Show/hide generators", false, false);
        panel.addToggleAnimationButtonIcon("Bus", BusStop.class, "/icons/BusStop24.png", "Show/hide bus stops", true, false);
        panel.addToggleAnimationButtonIcon("BusId", BusStopAnimation.Text.class, "/icons/Id24.png", "Show/hide bus stops Ids",
                false, true);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setIconAnimationTogglesStandard(final OtsAnimationPanel panel)
    {
        panel.addToggleAnimationButtonIcon("Node", NodeAnimation.ElevatedNode.class, "/icons/Node24.png", "Show/hide nodes",
                false, false);
        panel.addToggleAnimationButtonIcon("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Link", OtsLink.class, "/icons/Link24.png", "Show/hide links", false, false);
        panel.addToggleAnimationButtonIcon("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Lane", Lane.class, "/icons/Lane24.png", "Show/hide lanes", true, false);
        panel.addToggleAnimationButtonIcon("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("LaneCenter", CenterLine.class, "/icons/CenterLine24.png",
                "Show/hide lane center lines", false, false);
        panel.addToggleAnimationButtonIcon("Stripe", Stripe.class, "/icons/Stripe24.png", "Show/hide stripes", true, false);
        panel.addToggleAnimationButtonIcon("Shoulder", Shoulder.class, "/icons/Shoulder24.png", "Show/hide shoulders", true,
                false);
        panel.addToggleAnimationButtonIcon("GTU", Gtu.class, "/icons/Gtu24.png", "Show/hide GTUs", true, false);
        panel.addToggleAnimationButtonIcon("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids",
                false, true);
        panel.addToggleAnimationButtonIcon("Detector", DetectorAnimationToggle.class, "/icons/Detector24.png",
                "Show/hide detectors", false, false);
        panel.addToggleAnimationButtonIcon("DetectorId", DetectorAnimation.Text.class, "/icons/Id24.png",
                "Show/hide detector Ids", false, true);
        panel.addToggleAnimationButtonIcon("Light", TrafficLight.class, "/icons/TrafficLight24.png", "Show/hide traffic lights",
                true, false);
        panel.addToggleAnimationButtonIcon("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png",
                "Show/hide traffic light Ids", false, true);
        panel.addToggleAnimationButtonIcon("Conflict", Conflict.class, "/icons/Conflict24.png", "Show/hide conflicts", false,
                false);
        panel.addToggleAnimationButtonIcon("Generator", GtuGeneratorQueueAnimation.class, "/icons/Generator24.png",
                "Show/hide generators", false, false);
        panel.addToggleAnimationButtonIcon("Bus", BusStop.class, "/icons/BusStop24.png", "Show/hide bus stops", false, false);
        panel.addToggleAnimationButtonIcon("BusId", BusStopAnimation.Text.class, "/icons/Id24.png", "Show/hide bus stops Ids",
                false, true);
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param panel OTSAnimationPanel; the OTSAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public static void showAnimationClass(final OtsAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().showClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Set a class to be shown in the animation to false.
     * @param panel OTSAnimationPanel; the OTSAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public static void hideAnimationClass(final OtsAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().hideClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void showAnimationFull(final OtsAnimationPanel panel)
    {
        showAnimationClass(panel, OtsNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        showAnimationClass(panel, OtsLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        showAnimationClass(panel, Stripe.class);
        showAnimationClass(panel, Shoulder.class);
        showAnimationClass(panel, Gtu.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        showAnimationClass(panel, Detector.class);
        hideAnimationClass(panel, DetectorAnimation.Text.class);
        showAnimationClass(panel, TrafficLight.class);
        hideAnimationClass(panel, TrafficLightAnimation.Text.class);
        showAnimationClass(panel, Conflict.class);
        hideAnimationClass(panel, BusStop.class);
        hideAnimationClass(panel, BusStopAnimation.Text.class);
        showAnimationClass(panel, GtuGeneratorQueueAnimation.class);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void showAnimationStandard(final OtsAnimationPanel panel)
    {
        hideAnimationClass(panel, OtsNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        hideAnimationClass(panel, OtsLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        showAnimationClass(panel, Stripe.class);
        showAnimationClass(panel, Shoulder.class);
        showAnimationClass(panel, Gtu.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        hideAnimationClass(panel, Detector.class);
        hideAnimationClass(panel, DetectorAnimation.Text.class);
        showAnimationClass(panel, TrafficLight.class);
        hideAnimationClass(panel, TrafficLightAnimation.Text.class);
        hideAnimationClass(panel, Conflict.class);
        hideAnimationClass(panel, BusStop.class);
        hideAnimationClass(panel, BusStopAnimation.Text.class);
        hideAnimationClass(panel, GtuGeneratorQueueAnimation.class);
    }

}
