package org.opentrafficsim.editor.decoration;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.opentrafficsim.base.Resource;
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
        ImageIcon roadIcon = loadIcon("./OTS_road.png", -1, -1, -1, -1);
        ImageIcon networkIcon = loadIcon("./OTS_network.png", -1, -1, -1, -1);
        ImageIcon nodeIcon = loadIcon("./OTS_node.png", -1, -1, -1, -1);

        editor.setCustomIcon("Ots", loadIcon("./OTS_merge.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Definitions", loadIcon("./Database.png", 14, 14, 16, 16));
        editor.setCustomIcon(".RoadLayout", roadIcon);
        editor.setCustomIcon("Ots.Network.Link.DefinedLayout", roadIcon);
        editor.setCustomIcon("Ots.Network", networkIcon);
        editor.setCustomIcon(".Node", nodeIcon);
        editor.setCustomIcon(".Centroid", loadIcon("./OTS_centroid.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Network.Connector", loadIcon("./OTS_connector.png", -1, -1, -1, -1));
        editor.setCustomIcon(".Link", loadIcon("./OTS_link.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Demand", loadIcon("./Calendar.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Demand.ShortestRoute.From", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.To", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.Via", nodeIcon);
        editor.setCustomIcon("Ots.Demand.OdOptions.OdOptionsItem.Origin", nodeIcon);
        editor.setCustomIcon("Ots.Demand.Od", loadIcon("./Table_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Models", loadIcon("./Component_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Scenarios", loadIcon("./Film.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Control", loadIcon("./OTS_control.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Run", loadIcon("./Stopwatch.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Animation", loadIcon("./Play.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Animation.Connector", loadIcon("./OTS_connector.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Output", loadIcon("./Report.png", 14, 14, 16, 16)); // does not exist yet

        editor.addTab("Map", networkIcon, EditorMap.build(editor), "Map editor");
        editor.addTab("Parameters", null, buildParameterPane(), null);
        editor.addTab("Text", null, buildTextPane(), null);

        // string functions
        new AttributesStringFunction(editor, "Ots.Network.Link.LaneOverride", "Lane");
        new AttributesStringFunction(editor, "Ots.Network.Link.StripeOverride", "Stripe");
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
        // new NoDuplicateChildrenValidator(editor, "Ots.Models.Model.TacticalPlanner.Lmrs.MandatoryIncentives");
        // new NoDuplicateChildrenValidator(editor, "Ots.Models.Model.TacticalPlanner.Lmrs.VoluntaryIncentives");
        // new NoDuplicateChildrenValidator(editor, "Ots.Models.Model.TacticalPlanner.Lmrs.AccelerationIncentives");
        new RoadLayoutElementValidator(editor, "Ots.Network.Link.LaneOverride", LayoutCoupling.PARENT_PARENT_IS_LINK, "Lane");
        new RoadLayoutElementValidator(editor, "Ots.Network.Link.StripeOverride", LayoutCoupling.PARENT_PARENT_IS_LINK,
                "Stripe");
        new RoadLayoutElementValidator(editor, "Ots.Network.Link.TrafficLight", LayoutCoupling.PARENT_IS_LINK, "Lane");
        new RoadLayoutElementValidator(editor, "Ots.Demand.Od.Category.Lane", LayoutCoupling.LINK_ATTRIBUTE, "Lane");
        new RoadLayoutElementValidator(editor, "Ots.Demand.OdOptions.OdOptionsItem.Lane", LayoutCoupling.LINK_ATTRIBUTE,
                "Lane");
        new RoadLayoutElementValidator(editor, "Ots.Demand.Generator", LayoutCoupling.LINK_ATTRIBUTE, "Lane");
        new RoadLayoutElementValidator(editor, "Ots.Demand.ListGenerator", LayoutCoupling.LINK_ATTRIBUTE, "Lane");
        new RoadLayoutElementValidator(editor, "Ots.Demand.Sink", LayoutCoupling.LINK_ATTRIBUTE, "Lane");
        new RoadLayoutElementValidator(editor, "Ots.Animation.RoadLayout.Lane", LayoutCoupling.LAYOUT_BY_PARENT_ID, "Id");
        new RoadLayoutElementValidator(editor, "Ots.Animation.RoadLayout.Stripe", LayoutCoupling.LAYOUT_BY_PARENT_ID, "Id");
        new RoadLayoutElementValidator(editor, "Ots.Animation.RoadLayout.Shoulder", LayoutCoupling.LAYOUT_BY_PARENT_ID, "Id");
        new RoadLayoutElementValidator(editor, "Ots.Animation.RoadLayout.NoTrafficLane", LayoutCoupling.LAYOUT_BY_PARENT_ID,
                "Id");
        new RoadLayoutElementValidator(editor, "Ots.Animation.Link.Lane", LayoutCoupling.LINK_BY_PARENT_ID, "Id");
        new RoadLayoutElementValidator(editor, "Ots.Animation.Link.Stripe", LayoutCoupling.LINK_BY_PARENT_ID, "Id");
        new RoadLayoutElementValidator(editor, "Ots.Animation.Link.Shoulder", LayoutCoupling.LINK_BY_PARENT_ID, "Id");
        new RoadLayoutElementValidator(editor, "Ots.Animation.Link.NoTrafficLane", LayoutCoupling.LINK_BY_PARENT_ID, "Id");
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
     * Loads an icon, possibly rescaled.
     * @param image image filename, relative in resources.
     * @param width width to resize to, may be -1 to leave as is.
     * @param height width to resize to, may be -1 to leave as is.
     * @param bgWidth background image width icon will be centered in, may be -1 to leave as is.
     * @param bgHeight background image height icon will be centered in, may be -1 to leave as is.
     * @return image icon.
     * @throws IOException if the file is not in resources.
     */
    public static ImageIcon loadIcon(final String image, final int width, final int height, final int bgWidth,
            final int bgHeight) throws IOException
    {
        Image im = ImageIO.read(Resource.getResourceAsStream(image));
        if (width > 0 || height > 0)
        {
            im = im.getScaledInstance(width > 0 ? width : im.getWidth(null), height > 0 ? height : im.getHeight(null),
                    Image.SCALE_SMOOTH);
        }
        if (bgWidth > 0 && bgHeight > 0)
        {
            BufferedImage bg = new BufferedImage(bgWidth > 0 ? bgWidth : im.getWidth(null),
                    bgHeight > 0 ? bgHeight : im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bg.getGraphics();
            g.drawImage(im, (bg.getWidth() - im.getWidth(null)) / 2, (bg.getHeight() - im.getHeight(null)) / 2, null);
            im = bg;
        }
        return new ImageIcon(im);
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
        /** */
        private static final long serialVersionUID = 20230910L;

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
            System.out.println("Created: " + node.getPathString());
        }

        @Override
        public void notifyRemoved(final XsdTreeNode node)
        {
            System.out.println("Removed: " + node.getPathString());
        }
    }

}
