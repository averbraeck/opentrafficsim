package org.opentrafficsim.web.animation;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.gtu.GtuGeneratorQueueAnimation;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.SensorAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.web.animation.D2.HTMLAnimationPanel;

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
    public static void setTextAnimationTogglesFull(final HTMLAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", NodeAnimation.ElevatedNode.class, "Show/hide nodes", true);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", OTSLink.class, "Show/hide links", true);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", Lane.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("LaneCenter", LaneAnimation.CenterLine.class, "Show/hide lane center lines", false);
        panel.addToggleAnimationButtonText("Stripe", Stripe.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", GTU.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Sensor", SingleSensor.class, "Show/hide sensors", true);
        panel.addToggleAnimationButtonText("SensorId", SensorAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorQueueAnimation.class, "Show/hide generators", false);
        panel.addToggleAnimationButtonText("Bus", BusStop.class, "Show/hide bus stops", false);
        panel.addToggleAnimationButtonText("BusId", BusStopAnimation.Text.class, "Show/hide bus stop Ids", false);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel HTMLAnimationPanel; the Animation panel to add the toggle to.
     */
    public static void setTextAnimationTogglesStandard(final HTMLAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", NodeAnimation.ElevatedNode.class, "Show/hide nodes", false);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", OTSLink.class, "Show/hide links", false);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", Lane.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("LaneCenter", LaneAnimation.CenterLine.class, "Show/hide lane center lines", false);
        panel.addToggleAnimationButtonText("Stripe", Stripe.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", GTU.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Sensor", Sensor.class, "Show/hide sensors", false);
        panel.addToggleAnimationButtonText("SensorId", SensorAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GtuGeneratorQueueAnimation.class, "Show/hide generators", false);
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param panel OTSAnimationPanel; the HTMLAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public final static void showAnimationClass(final HTMLAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.showClass(locatableClass);
    }

    /**
     * Set a class to be shown in the animation to false.
     * @param panel HTMLAnimationPanel; the HTMLAnimationPanel where the animation of a class has to be switched off
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public final static void hideAnimationClass(final HTMLAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.hideClass(locatableClass);
    }

    /**
     * Set all commonly used animation on, and create the toggles on the left hand side.
     * @param panel HTMLAnimationPanel; the HTMLAnimationPanel where classes are shown or not.
     */
    public static void showAnimationFull(final HTMLAnimationPanel panel)
    {
        showAnimationClass(panel, OTSNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        showAnimationClass(panel, OTSLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        hideAnimationClass(panel, LaneAnimation.CenterLine.class);
        showAnimationClass(panel, Stripe.class);
        showAnimationClass(panel, Shoulder.class);
        showAnimationClass(panel, GTU.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        showAnimationClass(panel, SingleSensor.class);
        hideAnimationClass(panel, SensorAnimation.Text.class);
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
    public static void showAnimationStandard(final HTMLAnimationPanel panel)
    {
        hideAnimationClass(panel, OTSNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        hideAnimationClass(panel, OTSLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
        hideAnimationClass(panel, LaneAnimation.CenterLine.class);
        showAnimationClass(panel, Stripe.class);
        showAnimationClass(panel, Shoulder.class);
        showAnimationClass(panel, GTU.class);
        hideAnimationClass(panel, DefaultCarAnimation.Text.class);
        hideAnimationClass(panel, SingleSensor.class);
        hideAnimationClass(panel, SensorAnimation.Text.class);
        showAnimationClass(panel, TrafficLight.class);
        hideAnimationClass(panel, TrafficLightAnimation.Text.class);
        hideAnimationClass(panel, Conflict.class);
        hideAnimationClass(panel, BusStop.class);
        hideAnimationClass(panel, BusStopAnimation.Text.class);
        hideAnimationClass(panel, GtuGeneratorQueueAnimation.class);
    }

}
