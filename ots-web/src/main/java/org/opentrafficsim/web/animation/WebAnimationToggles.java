package org.opentrafficsim.web.animation;

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
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.web.animation.d2.HtmlAnimationPanel;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Set the default animation toggles for the HTML animation panel.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param panel the Animation panel to add the toggle to.
     */
    public static void setTextAnimationTogglesFull(final HtmlAnimationPanel panel)
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
        panel.addToggleAnimationButtonText("Detector", DetectorData.class, "Show/hide detector", true);
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
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel the Animation panel to add the toggle to.
     */
    public static void setTextAnimationTogglesStandard(final HtmlAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", NodeData.class, "Show/hide nodes", false);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", LinkData.class, "Show/hide links", false);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", LaneData.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("LaneCenter", LaneAnimation.CenterLine.class, "Show/hide lane center lines", false);
        panel.addToggleAnimationButtonText("Stripe", StripeData.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", CrossSectionElementData.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", GtuData.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Detector", DetectorData.class, "Show/hide detector", false);
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
     * Set a class to be shown in the animation to true.
     * @param panel the HTMLAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass the class for which the animation has to be shown.
     */
    public final static void showAnimationClass(final HtmlAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.showClass(locatableClass);
    }

    /**
     * Set a class to be shown in the animation to false.
     * @param panel the HTMLAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass the class for which the animation has to be shown.
     */
    public final static void hideAnimationClass(final HtmlAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.hideClass(locatableClass);
    }

    /**
     * Set all commonly used animation on, and create the toggles on the left hand side.
     * @param panel the HTMLAnimationPanel where classes are shown or not.
     */
    public static void showAnimationFull(final HtmlAnimationPanel panel)
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
     * @param panel the WrappableAnimation.
     */
    public static void showAnimationStandard(final HtmlAnimationPanel panel)
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
