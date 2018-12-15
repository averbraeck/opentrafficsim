package org.opentrafficsim.swing.gui;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.draw.swing.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.swing.gtu.GTUGeneratorAnimation;
import org.opentrafficsim.draw.swing.network.LinkAnimation;
import org.opentrafficsim.draw.swing.network.NodeAnimation;
import org.opentrafficsim.draw.swing.road.BusStopAnimation;
import org.opentrafficsim.draw.swing.road.LaneAnimation;
import org.opentrafficsim.draw.swing.road.SensorAnimation;
import org.opentrafficsim.draw.swing.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Set the default animation toggles for the animation panel.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setTextAnimationTogglesFull(final OTSAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", OTSNode.class, "Show/hide nodes", true);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", OTSLink.class, "Show/hide links", true);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", Lane.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("Stripe", Stripe.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", GTU.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Sensor", SingleSensor.class, "Show/hide sensors", true);
        panel.addToggleAnimationButtonText("SensorId", SensorAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GTUGeneratorAnimation.class, "Show/hide generators", false);
        panel.addToggleAnimationButtonText("Bus", BusStop.class, "Show/hide bus stops", false);
        panel.addToggleAnimationButtonText("BusId", BusStopAnimation.Text.class, "Show/hide bus stop Ids", false);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setTextAnimationTogglesStandard(final OTSAnimationPanel panel)
    {
        panel.addToggleAnimationButtonText("Node", OTSNode.class, "Show/hide nodes", false);
        panel.addToggleAnimationButtonText("NodeId", NodeAnimation.Text.class, "Show/hide node Ids", false);
        panel.addToggleAnimationButtonText("Link", OTSLink.class, "Show/hide links", false);
        panel.addToggleAnimationButtonText("LinkId", LinkAnimation.Text.class, "Show/hide link Ids", false);
        panel.addToggleAnimationButtonText("Lane", Lane.class, "Show/hide lanes", true);
        panel.addToggleAnimationButtonText("LaneId", LaneAnimation.Text.class, "Show/hide lane Ids", false);
        panel.addToggleAnimationButtonText("Stripe", Stripe.class, "Show/hide stripes", true);
        panel.addToggleAnimationButtonText("Shoulder", Shoulder.class, "Show/hide shoulders", true);
        panel.addToggleAnimationButtonText("GTU", GTU.class, "Show/hide GTUs", true);
        panel.addToggleAnimationButtonText("GTUId", DefaultCarAnimation.Text.class, "Show/hide GTU Ids", false);
        panel.addToggleAnimationButtonText("Sensor", Sensor.class, "Show/hide sensors", false);
        panel.addToggleAnimationButtonText("SensorId", SensorAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Light", TrafficLight.class, "Show/hide traffic lights", true);
        panel.addToggleAnimationButtonText("LightId", TrafficLightAnimation.Text.class, "Show/hide sensors Ids", false);
        panel.addToggleAnimationButtonText("Conflict", Conflict.class, "Show/hide conflicts", false);
        panel.addToggleAnimationButtonText("Generator", GTUGeneratorAnimation.class, "Show/hide generators", false);
    }

    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setIconAnimationTogglesFull(final OTSAnimationPanel panel)
    {
        panel.addToggleAnimationButtonIcon("Node", OTSNode.class, "/icons/Node24.png", "Show/hide nodes", true, false);
        panel.addToggleAnimationButtonIcon("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Link", OTSLink.class, "/icons/Link24.png", "Show/hide links", true, false);
        panel.addToggleAnimationButtonIcon("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Lane", Lane.class, "/icons/Lane24.png", "Show/hide lanes", true, false);
        panel.addToggleAnimationButtonIcon("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Stripe", Stripe.class, "/icons/Stripe24.png", "Show/hide stripes", true, false);
        panel.addToggleAnimationButtonIcon("Shoulder", Shoulder.class, "/icons/Shoulder24.png", "Show/hide shoulders", true,
                false);
        panel.addToggleAnimationButtonIcon("GTU", GTU.class, "/icons/Gtu24.png", "Show/hide GTUs", true, false);
        panel.addToggleAnimationButtonIcon("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Sensor", Sensor.class, "/icons/Sensor24.png", "Show/hide sensors", true, false);
        panel.addToggleAnimationButtonIcon("SensorId", SensorAnimation.Text.class, "/icons/Id24.png", "Show/hide sensors Ids",
                false, true);
        panel.addToggleAnimationButtonIcon("Light", TrafficLight.class, "/icons/TrafficLight24.png", "Show/hide traffic lights",
                true, false);
        panel.addToggleAnimationButtonIcon("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png",
                "Show/hide sensors Ids", false, true);
        panel.addToggleAnimationButtonIcon("Conflict", Conflict.class, "/icons/Conflict24.png", "Show/hide conflicts", false,
                false);
        panel.addToggleAnimationButtonIcon("Generator", GTUGeneratorAnimation.class, "/icons/Generator24.png",
                "Show/hide generators", false, false);
        panel.addToggleAnimationButtonIcon("Bus", BusStop.class, "/icons/BusStop24.png", "Show/hide bus stops", true, false);
        panel.addToggleAnimationButtonIcon("BusId", BusStopAnimation.Text.class, "/icons/Id24.png", "Show/hide bus stops", false,
                true);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void setIconAnimationTogglesStandard(final OTSAnimationPanel panel)
    {
        panel.addToggleAnimationButtonIcon("Node", OTSNode.class, "/icons/Node24.png", "Show/hide nodes", false, false);
        panel.addToggleAnimationButtonIcon("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Link", OTSLink.class, "/icons/Link24.png", "Show/hide links", false, false);
        panel.addToggleAnimationButtonIcon("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Lane", Lane.class, "/icons/Lane24.png", "Show/hide lanes", true, false);
        panel.addToggleAnimationButtonIcon("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Stripe", Stripe.class, "/icons/Stripe24.png", "Show/hide stripes", true, false);
        panel.addToggleAnimationButtonIcon("Shoulder", Shoulder.class, "/icons/Shoulder24.png", "Show/hide shoulders", true,
                false);
        panel.addToggleAnimationButtonIcon("GTU", GTU.class, "/icons/Gtu24.png", "Show/hide GTUs", true, false);
        panel.addToggleAnimationButtonIcon("GTUId", DefaultCarAnimation.Text.class, "/icons/Id24.png", "Show/hide GTU Ids", false,
                true);
        panel.addToggleAnimationButtonIcon("Sensor", Sensor.class, "/icons/Sensor24.png", "Show/hide sensors", false, false);
        panel.addToggleAnimationButtonIcon("SensorId", SensorAnimation.Text.class, "/icons/Id24.png", "Show/hide sensors Ids",
                false, true);
        panel.addToggleAnimationButtonIcon("Light", TrafficLight.class, "/icons/TrafficLight24.png", "Show/hide traffic lights",
                true, false);
        panel.addToggleAnimationButtonIcon("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png",
                "Show/hide sensors Ids", false, true);
        panel.addToggleAnimationButtonIcon("Conflict", Conflict.class, "/icons/Conflict24.png", "Show/hide conflicts", false,
                false);
        panel.addToggleAnimationButtonIcon("Generator", GTUGeneratorAnimation.class, "/icons/Generator24.png",
                "Show/hide generators", false, false);
        panel.addToggleAnimationButtonIcon("Bus", BusStop.class, "/icons/BusStop24.png", "Show/hide bus stops", false, false);
        panel.addToggleAnimationButtonIcon("BusId", BusStopAnimation.Text.class, "/icons/Id24.png", "Show/hide bus stops", false,
                true);
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public final static void showAnimationClass(final OTSAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().showClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }
    
    /**
     * Set a class to be shown in the animation to true.
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public final static void hideAnimationClass(final OTSAnimationPanel panel, final Class<? extends Locatable> locatableClass)
    {
        panel.getAnimationPanel().hideClass(locatableClass);
        panel.updateAnimationClassCheckBox(locatableClass);
    }
    
    /**
     * Set all animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void showAnimationFull(final OTSAnimationPanel panel)
    {
        showAnimationClass(panel, OTSNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        showAnimationClass(panel, OTSLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
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
        showAnimationClass(panel, GTUGeneratorAnimation.class);
    }

    /**
     * Set the most common animation on, and create the toggles on the left hand side.
     * @param panel OTSAnimationPanel; the WrappableAnimation.
     */
    public static void showAnimationStandard(final OTSAnimationPanel panel)
    {
        hideAnimationClass(panel, OTSNode.class);
        hideAnimationClass(panel, NodeAnimation.Text.class);
        hideAnimationClass(panel, OTSLink.class);
        hideAnimationClass(panel, LinkAnimation.Text.class);
        showAnimationClass(panel, Lane.class);
        hideAnimationClass(panel, LaneAnimation.Text.class);
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
        hideAnimationClass(panel, GTUGeneratorAnimation.class);
    }

}
