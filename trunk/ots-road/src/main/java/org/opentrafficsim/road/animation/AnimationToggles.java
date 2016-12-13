package org.opentrafficsim.road.animation;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LinkAnimation;
import org.opentrafficsim.core.network.animation.NodeAnimation;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.SensorAnimation;
import org.opentrafficsim.road.network.animation.TrafficLightAnimation;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;

/**
 * Set the default animation toggles for the animation panel.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @param aws the WrappableAnimation.
     */
    public static void setTextAnimationTogglesFull(final AbstractWrappableAnimation aws)
    {
        aws.addToggleAnimationButton("Node", OTSNode.class, "Show/hide nodes", true);
        aws.addToggleAnimationButton("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        aws.addToggleAnimationButton("Link", OTSLink.class, "Show/hide links", true);
        aws.addToggleAnimationButton("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        aws.addToggleAnimationButton("Lane", Lane.class, "Show/hide lanes", true);
        aws.addToggleAnimationButton("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        aws.addToggleAnimationButton("Stripe", Stripe.class, "Show/hide stripes", true);
        aws.addToggleAnimationButton("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        aws.addToggleAnimationButton("GTU", GTU.class, "Show/hide GTUs", true);
        aws.addToggleAnimationButton("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        aws.addToggleAnimationButton("Sensor", SingleSensor.class, "Show/hide sensors", true);
        aws.addToggleAnimationButton("SensorId", SensorAnimation.Text.class, "Show/hide sensors Ids", false);
        aws.addToggleAnimationButton("Light", TrafficLight.class, "Show/hide traffic lights", true);
        aws.addToggleAnimationButton("LightId", TrafficLightAnimation.Text.class, "Show/hide sensors Ids", false);
        aws.addToggleAnimationButton("Conflict", Conflict.class, "Show/hide conflicts", true);
        // aws.addToggleAnimationButton("Generator", LaneBasedGTUGenerator.class, "Show/hide generators", true);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param aws the WrappableAnimation.
     */
    public static void setTextAnimationTogglesStandard(final AbstractWrappableAnimation aws)
    {
        aws.addToggleAnimationButton("Node", OTSNode.class, "Show/hide nodes", false);
        aws.addToggleAnimationButton("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        aws.addToggleAnimationButton("Link", OTSLink.class, "Show/hide links", false);
        aws.addToggleAnimationButton("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        aws.addToggleAnimationButton("Lane", Lane.class, "Show/hide lanes", true);
        aws.addToggleAnimationButton("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        aws.addToggleAnimationButton("Stripe", Stripe.class, "Show/hide stripes", true);
        aws.addToggleAnimationButton("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        aws.addToggleAnimationButton("GTU", GTU.class, "Show/hide GTUs", true);
        aws.addToggleAnimationButton("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        aws.addToggleAnimationButton("Sensor", Sensor.class, "Show/hide sensors", false);
        aws.addToggleAnimationButton("SensorId", SensorAnimation.Text.class, "Show/hide sensors Ids", false);
        aws.addToggleAnimationButton("Light", TrafficLight.class, "Show/hide traffic lights", true);
        aws.addToggleAnimationButton("LightId", TrafficLightAnimation.Text.class, "Show/hide sensors Ids", false);
        aws.addToggleAnimationButton("Conflict", Conflict.class, "Show/hide conflicts", false);
        // aws.addToggleAnimationButton("Generator", LaneBasedGTUGenerator.class, "Show/hide generators", false);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param aws the WrappableAnimation.
     */
    public static void setIconAnimationTogglesFull(final AbstractWrappableAnimation aws)
    {
        aws.addToggleAnimationButton("Node", OTSNode.class, "/icons/Node24.png", "Show/hide nodes", true);
        aws.addToggleAnimationButton("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false);
        aws.addToggleAnimationButton("Link", OTSLink.class, "/icons/Link24.png", "Show/hide links", true);
        aws.addToggleAnimationButton("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false);
        aws.addToggleAnimationButton("Lane", Lane.class, "/icons/Lane24.png", "Show/hide lanes", true);
        aws.addToggleAnimationButton("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false);
        aws.addToggleAnimationButton("Stripe", Stripe.class, "/icons/Stripe24.png", "Show/hide stripes", true);
        aws.addToggleAnimationButton("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        aws.addToggleAnimationButton("GTU", GTU.class, "Show/hide GTUs", true);
        aws.addToggleAnimationButton("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids", false);
        aws.addToggleAnimationButton("Sensor", Sensor.class, "/icons/Sensor24.png", "Show/hide sensors", true);
        aws.addToggleAnimationButton("SensorId", SensorAnimation.Text.class, "/icons/Id24.png", "Show/hide sensors Ids", false);
        aws.addToggleAnimationButton("Light", TrafficLight.class, "/icons/TrafficLight24.png", "Show/hide traffic lights",
                true);
        aws.addToggleAnimationButton("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png", "Show/hide sensors Ids",
                false);
        aws.addToggleAnimationButton("Conflict", Conflict.class, "Show/hide conflicts", true);
        // aws.addToggleAnimationButton("Generator", LaneBasedGTUGenerator.class, "Show/hide generators", true);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param aws the WrappableAnimation.
     */
    public static void setIconAnimationTogglesStandard(final AbstractWrappableAnimation aws)
    {
        aws.addToggleAnimationButton("Node", OTSNode.class, "/icons/Node24.png", "Show/hide nodes", false);
        aws.addToggleAnimationButton("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false);
        aws.addToggleAnimationButton("Link", OTSLink.class, "/icons/Link24.png", "Show/hide links", false);
        aws.addToggleAnimationButton("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false);
        aws.addToggleAnimationButton("Lane", Lane.class, "/icons/Lane24.png", "Show/hide lanes", true);
        aws.addToggleAnimationButton("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false);
        aws.addToggleAnimationButton("Stripe", Stripe.class, "/icons/Stripe24.png", "Show/hide stripes", true);
        aws.addToggleAnimationButton("Shoulder", Shoulder.class, "/icons/Shoulder24.png", "Show/hide shoulders", true);
        aws.addToggleAnimationButton("GTU", GTU.class, "Show/hide GTUs", true);
        aws.addToggleAnimationButton("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids", false);
        aws.addToggleAnimationButton("Sensor", Sensor.class, "/icons/Sensor24.png", "Show/hide sensors", false);
        aws.addToggleAnimationButton("SensorId", SensorAnimation.Text.class, "/icons/Id24.png", "Show/hide sensors Ids", false);
        aws.addToggleAnimationButton("Light", TrafficLight.class, "/icons/TrafficLight24.png", "Show/hide traffic lights",
                true);
        aws.addToggleAnimationButton("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png", "Show/hide sensors Ids",
                false);
        aws.addToggleAnimationButton("Conflict", Conflict.class, "Show/hide conflicts", false);
        // aws.addToggleAnimationButton("Generator", LaneBasedGTUGenerator.class, "Show/hide generators", false);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param aws the WrappableAnimation.
     */
    public static void showAnimationFull(final AbstractWrappableAnimation aws)
    {
        aws.showAnimationClass(OTSNode.class);
        aws.hideAnimationClass(NodeAnimation.Text.class);
        aws.showAnimationClass(OTSLink.class);
        aws.hideAnimationClass(LinkAnimation.Text.class);
        aws.showAnimationClass(Lane.class);
        aws.hideAnimationClass(LaneAnimation.Text.class);
        aws.showAnimationClass(Stripe.class);
        aws.showAnimationClass(Shoulder.class);
        aws.showAnimationClass(GTU.class);
        aws.hideAnimationClass(DefaultCarAnimation.Text.class);
        aws.showAnimationClass(SingleSensor.class);
        aws.hideAnimationClass(SensorAnimation.Text.class);
        aws.showAnimationClass(TrafficLight.class);
        aws.hideAnimationClass(TrafficLightAnimation.Text.class);
        aws.showAnimationClass(Conflict.class);
        // aws.showAnimationClass(LaneBasedGTUGenerator.class);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param aws the WrappableAnimation.
     */
    public static void showAnimationStandard(final AbstractWrappableAnimation aws)
    {
        aws.hideAnimationClass(OTSNode.class);
        aws.hideAnimationClass(NodeAnimation.Text.class);
        aws.hideAnimationClass(OTSLink.class);
        aws.hideAnimationClass(LinkAnimation.Text.class);
        aws.showAnimationClass(Lane.class);
        aws.hideAnimationClass(LaneAnimation.Text.class);
        aws.showAnimationClass(Stripe.class);
        aws.showAnimationClass(Shoulder.class);
        aws.showAnimationClass(GTU.class);
        aws.hideAnimationClass(DefaultCarAnimation.Text.class);
        aws.hideAnimationClass(SingleSensor.class);
        aws.hideAnimationClass(SensorAnimation.Text.class);
        aws.showAnimationClass(TrafficLight.class);
        aws.hideAnimationClass(TrafficLightAnimation.Text.class);
        aws.hideAnimationClass(Conflict.class);
        // aws.showAnimationClass(LaneBasedGTUGenerator.class);
    }

}
