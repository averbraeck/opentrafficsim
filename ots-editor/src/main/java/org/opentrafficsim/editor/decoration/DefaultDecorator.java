package org.opentrafficsim.editor.decoration;

import java.io.IOException;

import javax.naming.NamingException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.opentrafficsim.animation.IconUtil;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.string.AttributesStringFunction;
import org.opentrafficsim.editor.decoration.string.ChoiceNodeStringFunction;
import org.opentrafficsim.editor.decoration.string.ClassNameTypeStringFunction;
import org.opentrafficsim.editor.decoration.string.CorrelationStringFunction;
import org.opentrafficsim.editor.decoration.string.OdOptionsItemStringFunction;
import org.opentrafficsim.editor.decoration.string.XiIncludeStringFunction;
import org.opentrafficsim.editor.decoration.validation.AttributesNotEqualValidator;
import org.opentrafficsim.editor.decoration.validation.ParentValidator;
import org.opentrafficsim.editor.decoration.validation.RoadLayoutElementValidator;
import org.opentrafficsim.editor.decoration.validation.RoadLayoutElementValidator.LayoutCoupling;
import org.opentrafficsim.editor.decoration.validation.RoadLayoutElementValidator.RoadLayoutElementAttribute;
import org.opentrafficsim.editor.decoration.validation.TrafficLightValidator;
import org.opentrafficsim.editor.extensions.DefinitionsSaver;
import org.opentrafficsim.editor.extensions.OdEditor;
import org.opentrafficsim.editor.extensions.RoadLayoutEditor;
import org.opentrafficsim.editor.extensions.RouteEditor;
import org.opentrafficsim.editor.extensions.TrafCodEditor;
import org.opentrafficsim.editor.extensions.map.EditorMap;

/**
 * Decorates the editor with custom icons, tabs, string functions and custom editors.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DefaultDecorator
{

    /**
     * Private constructor.
     */
    private DefaultDecorator()
    {

    }

    /**
     * Decorates the editor with custom icons, tabs, string functions and custom editors.
     * @param editor editor.
     * @throws IOException if a resource cannot be loaded.
     * @throws NamingException when registering objects does not work
     */
    public static void decorate(final OtsEditor editor) throws IOException, NamingException
    {
        int s = 16;
        Icon gtuIcon = IconUtil.of("Gtu24.png").imageSize(s, s).get();
        Icon roadIcon = IconUtil.of("RoadLayout24.png").imageSize(s, s).get();
        Icon nodeIcon = IconUtil.of("Node24.png").imageSize(s, s).get();
        Icon linkIcon = IconUtil.of("Link24.png").imageSize(s, s).get();
        Icon laneIcon = IconUtil.of("Lane24.png").imageSize(s, s).get();
        Icon stripeIcon = IconUtil.of("Stripe24.png").imageSize(s, s).get();
        Icon polyLineIcon = IconUtil.of("PolyLine24.png").imageSize(s, s).get();
        Icon detectorIcon = IconUtil.of("Detector24.png").imageSize(s, s).get();
        Icon trafficLightIcon = IconUtil.of("TrafficLight24.png").imageSize(s, s).get();
        Icon routeIcon = IconUtil.of("Route24.png").imageSize(s, s).get();
        Icon generatorIcon = IconUtil.of("Generator24.png").imageSize(s, s).get();
        Icon parameterIcon = IconUtil.of("Parameter24.png").imageSize(s, s).get();

        editor.setCustomIcon("Ots", IconUtil.of("Ots32.png").imageSize(14, 14).iconSize(s, s).get());
        editor.setCustomIcon(XsdPaths.DEFINITIONS, IconUtil.of("Database24.png").imageSize(s, s).get());
        editor.setCustomIcon(".xi:include", IconUtil.of("Import24.png").imageSize(s, s).get());
        editor.setCustomIcon(".GtuType", gtuIcon);
        editor.setCustomIcon(".GtuTemplate", gtuIcon);
        editor.setCustomIcon(".RoadLayout", roadIcon);
        editor.setCustomIcon(XsdPaths.LINK + ".DefinedLayout", roadIcon);
        editor.setCustomIcon(XsdPaths.NETWORK, IconUtil.of("Network24.png").imageSize(s, s).get());
        editor.setCustomIcon(".Node", nodeIcon);
        editor.setCustomIcon(".Centroid", IconUtil.of("Centroid24.png").imageSize(s, s).get());
        editor.setCustomIcon(".Link", linkIcon);
        editor.setCustomIcon(".LinkType", linkIcon);
        editor.setCustomIcon(".Connector", IconUtil.of("Connector24.png").imageSize(s, s).get());
        editor.setCustomIcon(".Lane", laneIcon);
        editor.setCustomIcon(".LaneType", laneIcon);
        editor.setCustomIcon(".Stripe", stripeIcon);
        editor.setCustomIcon(".StripeType", stripeIcon);
        editor.setCustomIcon(".Shoulder", IconUtil.of("Shoulder24.png").imageSize(s, s).get());
        editor.setCustomIcon(".Detector", detectorIcon);
        editor.setCustomIcon(".DetectorType", detectorIcon);
        editor.setCustomIcon(".Flattener", polyLineIcon);
        editor.setCustomIcon(XsdPaths.LINK + ".Straight", IconUtil.of("Straight24.png").imageSize(s, s).get());
        editor.setCustomIcon(XsdPaths.LINK + ".Bezier", IconUtil.of("Bezier24.png").imageSize(s, s).get());
        editor.setCustomIcon(XsdPaths.LINK + ".Clothoid", IconUtil.of("Clothoid24.png").imageSize(s, s).get());
        editor.setCustomIcon(XsdPaths.LINK + ".Arc", IconUtil.of("Arc24.png").imageSize(s, s).get());
        editor.setCustomIcon(XsdPaths.LINK + ".PolyLine", polyLineIcon);
        editor.setCustomIcon(".TrafficLightSensor", detectorIcon);
        editor.setCustomIcon(".TrafficLight", trafficLightIcon);
        editor.setCustomIcon(".Route", routeIcon);
        editor.setCustomIcon(".ShortestRoute", routeIcon);
        editor.setCustomIcon(XsdPaths.NETWORK + ".Conflicts", IconUtil.of("Conflict24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Demand", IconUtil.of("Calendar24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Demand.Generator", generatorIcon);
        editor.setCustomIcon("Ots.Demand.InjectionGenerator", generatorIcon);
        editor.setCustomIcon("Ots.Demand.Sink", IconUtil.of("Sink24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Demand.ShortestRoute.From", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.To", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.Via", nodeIcon);
        editor.setCustomIcon("Ots.Demand.OdOptions.OdOptionsItem.Origin", nodeIcon);
        editor.setCustomIcon(".Od", IconUtil.of("Table24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Models", IconUtil.of("Component24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Models.Model.ModelParameters", parameterIcon);
        editor.setCustomIcon(XsdPaths.CORRELATION + ".First", parameterIcon);
        editor.setCustomIcon(XsdPaths.CORRELATION + ".Then", parameterIcon);
        editor.setCustomIcon(XsdPaths.SCENARIOS, IconUtil.of("Scenario24.png").imageSize(s, s).get());
        editor.setCustomIcon(XsdPaths.DEFAULT_INPUT_PARAMETERS, parameterIcon);
        editor.setCustomIcon(XsdPaths.INPUT_PARAMETERS, parameterIcon);
        editor.setCustomIcon(".Control", IconUtil.of("TrafficLight24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Models.Model.TacticalPlanner.Lmrs.CarFollowingModel",
                IconUtil.of("Queue24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Models.Model.TacticalPlanner.Lmrs.Perception",
                IconUtil.of("Eye24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Run", IconUtil.of("Run24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Animation", IconUtil.of("Play24.png").imageSize(s, s).get());
        editor.setCustomIcon("Ots.Animation.Connector", IconUtil.of("Connector24.png").imageSize(s, s).get());
        // does not exist yet
        editor.setCustomIcon("Ots.Output", IconUtil.of("./Output24.png").imageSize(s, s).get());

        editor.addTab("Map", IconUtil.of("Network24.png").imageSize(18, 18).get(), EditorMap.build(editor), "Map editor");
        editor.addTab("Parameters", IconUtil.of("Parameter24.png").imageSize(18, 18).get(), buildParameterPane(), null);
        editor.addTab("Text", IconUtil.of("Text24.png").imageSize(18, 18).get(), buildTextPane(), null);

        // string functions
        new AttributesStringFunction(editor, "Ots.Network.Link.LaneOverride", "Lane");
        new AttributesStringFunction(editor, "Ots.Network.Link.StripeOverride", "Stripe");
        new AttributesStringFunction(editor, "Ots.Network.Link.TrafficLight", "Lane");
        new AttributesStringFunction(editor, ".SpeedLimit", "GtuType", "LegalSpeedLimit");
        new AttributesStringFunction(editor, "Ots.Demand.Od.Cell", "Origin", "Destination", "Category");
        new AttributesStringFunction(editor, "Ots.Demand.OdOptions.OdOptionsItem.Markov.State", "GtuType", "Parent",
                "Correlation");
        new AttributesStringFunction(editor, "Ots.Demand.OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias", "GtuType");
        new AttributesStringFunction(editor, "Ots.Demand.Generator", "Link", "Lane");
        new AttributesStringFunction(editor, "Ots.Control.FixedTime.SignalGroup.TrafficLight", "Link", "Lane",
                "TrafficLightId");
        new AttributesStringFunction(editor, "Ots.Control.FixedTime.Cycle", "SignalGroupId", "Offset", "PreGreen", "Green",
                "Yellow");
        new AttributesStringFunction(editor, ".LaneBias", "GtuType");
        new AttributesStringFunction(editor, ".Compatibility", "GtuType", "Compatible");
        new AttributesStringFunction(editor, "Ots.Models.Model.GtuTypeParameters", "GtuType");
        new AttributesStringFunction(editor, "Ots.Models.Model.TacticalPlanner.Lmrs.GtuTypeModel", "GtuType");
        new OdOptionsItemStringFunction(editor);
        new ClassNameTypeStringFunction(editor);
        new XiIncludeStringFunction(editor);
        new ChoiceNodeStringFunction(editor);
        new CorrelationStringFunction(editor);

        // validators
        new ParentValidator(editor, "Ots.Definitions.GtuTypes.GtuType");
        new ParentValidator(editor, "Ots.Definitions.LinkTypes.LinkType");
        new ParentValidator(editor, "Ots.Definitions.LaneTypes.LaneType");
        new ParentValidator(editor, "Ots.Definitions.DetectorTypes.DetectorType");
        new ParentValidator(editor, "Ots.Demand.OdOptions.OdOptionsItem.Markov.State")
                .setContext("Ots.Demand.OdOptions.OdOptionsItem").setIdAttribute("GtuType");
        new AttributesNotEqualValidator(editor, "Ots.Network.Link", "NodeStart", "NodeEnd");
        new AttributesNotEqualValidator(editor, "Ots.Demand.Cell", "Origin", "Destination");
        new AttributesNotEqualValidator(editor, "Ots.Demand.InjectionGenerator.Arrivals.Arrival", "Origin", "Destination");
        new RoadLayoutElementValidator(editor, "Ots.Network.Link.LaneOverride", LayoutCoupling.PARENT_IS_LINK,
                RoadLayoutElementAttribute.LANE);
        new RoadLayoutElementValidator(editor, "Ots.Network.Link.StripeOverride", LayoutCoupling.PARENT_IS_LINK,
                RoadLayoutElementAttribute.STRIPE);
        new RoadLayoutElementValidator(editor, "Ots.Network.Link.TrafficLight", LayoutCoupling.PARENT_IS_LINK,
                RoadLayoutElementAttribute.LANE);
        new RoadLayoutElementValidator(editor, "Ots.Demand.Od.Category.Lane", LayoutCoupling.LINK_ATTRIBUTE,
                RoadLayoutElementAttribute.LANE);
        new RoadLayoutElementValidator(editor, "Ots.Demand.OdOptions.OdOptionsItem.Lane", LayoutCoupling.LINK_ATTRIBUTE,
                RoadLayoutElementAttribute.LANE);
        new RoadLayoutElementValidator(editor, "Ots.Demand.Generator", LayoutCoupling.LINK_ATTRIBUTE,
                RoadLayoutElementAttribute.LANE);
        new RoadLayoutElementValidator(editor, "Ots.Demand.InjectionGenerator.Arrivals.Arrival", LayoutCoupling.LINK_ATTRIBUTE,
                RoadLayoutElementAttribute.LANE);
        new RoadLayoutElementValidator(editor, "Ots.Demand.Sink", LayoutCoupling.LINK_ATTRIBUTE,
                RoadLayoutElementAttribute.LANE);
        new RoadLayoutElementValidator(editor, "Ots.Animation.RoadLayout.Lane", LayoutCoupling.LAYOUT_BY_PARENT_ID,
                RoadLayoutElementAttribute.ID);
        new RoadLayoutElementValidator(editor, "Ots.Animation.RoadLayout.Stripe", LayoutCoupling.LAYOUT_BY_PARENT_ID,
                RoadLayoutElementAttribute.ID);
        new RoadLayoutElementValidator(editor, "Ots.Animation.RoadLayout.Shoulder", LayoutCoupling.LAYOUT_BY_PARENT_ID,
                RoadLayoutElementAttribute.ID);
        new RoadLayoutElementValidator(editor, "Ots.Animation.Link.Lane", LayoutCoupling.LINK_BY_PARENT_ID,
                RoadLayoutElementAttribute.ID);
        new RoadLayoutElementValidator(editor, "Ots.Animation.Link.Stripe", LayoutCoupling.LINK_BY_PARENT_ID,
                RoadLayoutElementAttribute.ID);
        new RoadLayoutElementValidator(editor, "Ots.Animation.Link.Shoulder", LayoutCoupling.LINK_BY_PARENT_ID,
                RoadLayoutElementAttribute.ID);
        new TrafficLightValidator(editor, ".SignalGroup.TrafficLight");

        new AutomaticLinkId(editor);
        new AutomaticConnectorId(editor);
        new DefinitionsSaver(editor);
        new LayoutCustomizer(editor);

        // new NodeCreatedRemovedPrinter(editor);
        new RoadLayoutEditor(editor);
        new OdEditor(editor);
        new RouteEditor(editor);
        new TrafCodEditor(editor);
    }

    /**
     * Temporary stub to create parameters pane.
     * @return component.
     */
    private static JComponent buildParameterPane()
    {
        JLabel parameters = new JLabel("parameters");
        parameters.setOpaque(true);
        parameters.setHorizontalAlignment(JLabel.CENTER);
        return parameters;
    }

    /**
     * Temporary stub to create text pane.
     * @return component.
     */
    private static JComponent buildTextPane()
    {
        JLabel text = new JLabel("text");
        text.setOpaque(true);
        text.setHorizontalAlignment(JLabel.CENTER);
        return text;
    }

    /**
     * Prints nodes that are created or removed.
     * <p>
     * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    @SuppressWarnings("unused") // Leave this class for debugging. It can be added by a line above that is commented out.
    private static class NodeCreatedRemovedPrinter extends AbstractNodeDecoratorRemove
    {
        /**
         * Constructor.
         * @param editor editor.
         */
        NodeCreatedRemovedPrinter(final OtsEditor editor)
        {
            super(editor, (n) -> true);
        }

        @Override
        public void notifyCreated(final XsdTreeNode node)
        {
            Logger.ots().trace("Created: " + node.getPathString());
        }

        @Override
        public void notifyRemoved(final XsdTreeNode node)
        {
            Logger.ots().trace("Removed: " + node.getPathString());
        }
    }

}
