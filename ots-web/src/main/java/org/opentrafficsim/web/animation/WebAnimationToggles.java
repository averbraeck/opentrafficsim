package org.opentrafficsim.web.animation;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuGenerator.GtuGeneratorPosition;
import org.opentrafficsim.core.network.OtsLink;
import org.opentrafficsim.core.network.OtsNode;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.DetectorAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.lane.object.detector.Detector;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.web.animation.d2.HtmlAnimationPanel;

/**
 * Set the default animation toggles for the HTML animation panel.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class WebAnimationToggles
{
    /**
     * Do not instantiate this class.
     */
    private WebAnimationToggles()
    {
        // static class.
    }

    /**
     * Set all commonly used animation on, and create the toggles on the left hand side.
     * @param panel HTMLAnimationPanel; the Animation panel to add the toggle to.
     */
    public static void setTextAnimationTogglesFull(final HtmlAnimationPanel panel)
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
        panel.addToggleAnimationButtonText("Detector", Detector.class, "Show/hide detector", true);
        panel.addToggleAnimationButtonText("DetectorId", DetectorAnimation.Text.class, "Show/hide detector Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide traffic light Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorPosition.class, "Show/hide generators", false);
        panel.addToggleAnimationButtonText("GeneratorQ", GtuGeneratorPositionAnimation.Queue.class,
                "Show/hide generator queues", false);
        panel.addToggleAnimationButtonText("Bus", BusStop.class, "Show/hide bus stops", false);
        panel.addToggleAnimationButtonText("BusId", BusStopAnimation.Text.class, "Show/hide bus stop Ids", false);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel HTMLAnimationPanel; the Animation panel to add the toggle to.
     */
    public static void setTextAnimationTogglesStandard(final HtmlAnimationPanel panel)
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
        panel.addToggleAnimationButtonText("Detector", Detector.class, "Show/hide detector", false);
        panel.addToggleAnimationButtonText("DetectorId", DetectorAnimation.Text.class, "Show/hide detector Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide traffic light Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorPosition.class, "Show/hide generators", false);
        panel.addToggleAnimationButtonText("GeneratorQ", GtuGeneratorPositionAnimation.Queue.class,
                "Show/hide generator queues", false);
        panel.addToggleAnimationButtonText("Bus", BusStop.class, "Show/hide bus stops", false);
        panel.addToggleAnimationButtonText("BusId", BusStopAnimation.Text.class, "Show/hide bus stop Ids", false);
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param panel OtsAnimationPanel; the HTMLAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public final static void showAnimationClass(final HtmlAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.showClass(locatableClass);
    }

    /**
     * Set a class to be shown in the animation to false.
     * @param panel HTMLAnimationPanel; the HTMLAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public final static void hideAnimationClass(final HtmlAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.hideClass(locatableClass);
    }

    /**
     * Set all commonly used animation on, and create the toggles on the left hand side.
     * @param panel HTMLAnimationPanel; the HTMLAnimationPanel where classes are shown or not.
     */
    public static void showAnimationFull(final HtmlAnimationPanel panel)
    {
        showAnimationClass(panel, OtsNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        showAnimationClass(panel, OtsLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        hideAnimationClass(panel, LaneAnimation.CenterLine.class);
        showAnimationClass(panel, Stripe.class);
        showAnimationClass(panel, Shoulder.class);
        showAnimationClass(panel, Gtu.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        showAnimationClass(panel, Detector.class);
        hideAnimationClass(panel, DetectorAnimation.Text.class);
        showAnimationClass(panel, TrafficLight.class);
        hideAnimationClass(panel, TrafficLightAnimation.Text.class);
        showAnimationClass(panel, Conflict.class);
        showAnimationClass(panel, GtuGeneratorPosition.class);
        hideAnimationClass(panel, GtuGeneratorPositionAnimation.Queue.class);
        showAnimationClass(panel, BusStop.class);
        hideAnimationClass(panel, BusStopAnimation.Text.class);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OtsAnimationPanel; the WrappableAnimation.
     */
    public static void showAnimationStandard(final HtmlAnimationPanel panel)
    {
        hideAnimationClass(panel, OtsNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        hideAnimationClass(panel, OtsLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        hideAnimationClass(panel, LaneAnimation.CenterLine.class);
        showAnimationClass(panel, Stripe.class);
        showAnimationClass(panel, Shoulder.class);
        showAnimationClass(panel, Gtu.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        hideAnimationClass(panel, Detector.class);
        hideAnimationClass(panel, DetectorAnimation.Text.class);
        showAnimationClass(panel, TrafficLight.class);
        hideAnimationClass(panel, TrafficLightAnimation.Text.class);
        hideAnimationClass(panel, Conflict.class);
        hideAnimationClass(panel, GtuGeneratorPosition.class);
        hideAnimationClass(panel, GtuGeneratorPositionAnimation.Queue.class);
        hideAnimationClass(panel, BusStop.class);
        hideAnimationClass(panel, BusStopAnimation.Text.class);
    }

}
