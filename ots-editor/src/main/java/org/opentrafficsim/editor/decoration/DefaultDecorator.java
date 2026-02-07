package org.opentrafficsim.editor.decoration;

import java.io.IOException;

import javax.naming.NamingException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.editor.OtsEditor;
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
import org.opentrafficsim.swing.gui.IconUtil;

/**
 * Decorates the editor with custom icons, tabs, string functions and custom editors.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        Icon roadIcon = IconUtil.of("RoadLayout24.png").imageSize(16, 16).get();
        Icon nodeIcon = IconUtil.of("Node24.png").imageSize(16, 16).get();

        editor.setCustomIcon("Ots", IconUtil.of("Ots32.png").imageSize(14, 14).iconSize(16, 16).get());
        editor.setCustomIcon("Ots.Definitions", IconUtil.of("Database24.png").imageSize(16, 16).get());
        editor.setCustomIcon(".RoadLayout", roadIcon);
        editor.setCustomIcon("Ots.Network.Link.DefinedLayout", roadIcon);
        editor.setCustomIcon("Ots.Network", IconUtil.of("Network24.png").imageSize(16, 16).get());
        editor.setCustomIcon(".Node", nodeIcon);
        editor.setCustomIcon(".Centroid", IconUtil.of("Centroid24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Network.Connector", IconUtil.of("Connector24.png").imageSize(16, 16).get());
        editor.setCustomIcon(".Link", IconUtil.of("Link24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Demand", IconUtil.of("Calendar24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Demand.ShortestRoute.From", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.To", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.Via", nodeIcon);
        editor.setCustomIcon("Ots.Demand.OdOptions.OdOptionsItem.Origin", nodeIcon);
        editor.setCustomIcon("Ots.Demand.Od", IconUtil.of("Table24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Models", IconUtil.of("Component24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Scenarios", IconUtil.of("Scenario24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Control", IconUtil.of("TrafficLight24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Run", IconUtil.of("Run24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Animation", IconUtil.of("Play24.png").imageSize(16, 16).get());
        editor.setCustomIcon("Ots.Animation.Connector", IconUtil.of("Connector24.png").imageSize(16, 16).get());
        // does not exist yet
        editor.setCustomIcon("Ots.Output", IconUtil.of("./Output24.png").imageSize(16, 16).get());

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
        new AttributesStringFunction(editor, "Ots.Demand.Generator", "Link", "Lane");
        new AttributesStringFunction(editor, "Ots.Control.FixedTime.SignalGroup.TrafficLight", "Link", "Lane",
                "TrafficLightId");
        new AttributesStringFunction(editor, "Ots.Control.FixedTime.Cycle", "SignalGroupId", "Offset", "PreGreen", "Green",
                "Yellow");
        new AttributesStringFunction(editor, ".LaneBias", "GtuType");
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
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
