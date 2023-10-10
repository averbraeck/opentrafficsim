package org.opentrafficsim.editor.decoration;

import java.io.IOException;

import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.string.AttributesStringFunction;
import org.opentrafficsim.editor.decoration.string.ChoiceNodeStringFunction;
import org.opentrafficsim.editor.decoration.string.ClassNameTypeStringFunction;
import org.opentrafficsim.editor.decoration.string.OdOptionsItemStringFunction;
import org.opentrafficsim.editor.decoration.string.XiIncludeStringFunction;
import org.opentrafficsim.editor.decoration.validation.AttributesNotEqualValidator;
import org.opentrafficsim.editor.decoration.validation.NoDuplicateChildrenValidator;
import org.opentrafficsim.editor.decoration.validation.ParentValidator;
import org.opentrafficsim.editor.extensions.DefinitionsSaver;
import org.opentrafficsim.editor.extensions.OdEditor;
import org.opentrafficsim.editor.extensions.RoadLayoutEditor;
import org.opentrafficsim.editor.extensions.RouteEditor;
import org.opentrafficsim.editor.extensions.TrafCodEditor;
import org.opentrafficsim.editor.extensions.map.Map;

/**
 * Decorates the editor with custom icons, tabs, string functions and custom editors.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * @param editor OtsEditor; editor.
     * @throws IOException if a resource cannot be loaded.
     * @throws NamingException when registering objects does not work
     */
    public static void decorate(final OtsEditor editor) throws IOException, NamingException
    {
        ImageIcon roadIcon = OtsEditor.loadIcon("./OTS_road.png", -1, -1, -1, -1);
        ImageIcon networkIcon = OtsEditor.loadIcon("./OTS_network.png", -1, -1, -1, -1);
        ImageIcon nodeIcon = OtsEditor.loadIcon("./OTS_node.png", -1, -1, -1, -1);

        editor.setCustomIcon("Ots", OtsEditor.loadIcon("./OTS_merge.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Definitions", OtsEditor.loadIcon("./Database.png", 14, 14, 16, 16));
        editor.setCustomIcon(".RoadLayout", roadIcon);
        editor.setCustomIcon("Ots.Network.Link.DefinedLayout", roadIcon);
        editor.setCustomIcon("Ots.Network", networkIcon);
        editor.setCustomIcon(".Node", nodeIcon);
        editor.setCustomIcon(".Centroid", OtsEditor.loadIcon("./OTS_centroid.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Network.Connector", OtsEditor.loadIcon("./OTS_connector.png", -1, -1, -1, -1));
        editor.setCustomIcon(".Link", OtsEditor.loadIcon("./OTS_link.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Demand", OtsEditor.loadIcon("./Calendar.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Demand.ShortestRoute.From", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.To", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.Via", nodeIcon);
        editor.setCustomIcon("Ots.Demand.OdOptions.OdOptionsItem.Origin", nodeIcon);
        editor.setCustomIcon("Ots.Demand.Od", OtsEditor.loadIcon("./Table_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Models", OtsEditor.loadIcon("./Component_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Scenarios", OtsEditor.loadIcon("./Film.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Control", OtsEditor.loadIcon("./OTS_control.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Run", OtsEditor.loadIcon("./Stopwatch.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Animation", OtsEditor.loadIcon("./Play.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Output", OtsEditor.loadIcon("./Report.png", 14, 14, 16, 16)); // does not exist yet

        editor.addTab("Map", networkIcon, Map.build(editor), "Map editor");
        editor.addTab("Parameters", null, buildParameterPane(), null);
        editor.addTab("Text", null, buildTextPane(), null);

        // string functions
        new AttributesStringFunction(editor, "Ots.Network.Link.LaneOverride", "Lane");
        new AttributesStringFunction(editor, ".SpeedLimit", "GtuType", "LegalSpeedLimit");
        new AttributesStringFunction(editor, "Ots.Demand.Od.Cell", "Origin", "Category", "Destination").setSeparator(" > ");
        new AttributesStringFunction(editor, "Ots.Demand.OdOptions.OdOptionsItem.Markov.State", "GtuType", "Parent",
                "Correlation");
        new AttributesStringFunction(editor, "Ots.Demand.Generator", "Link", "Lane");
        new AttributesStringFunction(editor, ".LaneBias", "GtuType");
        new OdOptionsItemStringFunction(editor);
        new ClassNameTypeStringFunction(editor);
        new XiIncludeStringFunction(editor);
        new ChoiceNodeStringFunction(editor);

        // validators
        new ParentValidator(editor, "Ots.Definitions.GtuTypes.GtuType");
        new ParentValidator(editor, "Ots.Definitions.LinkTypes.LinkType");
        new ParentValidator(editor, "Ots.Definitions.LaneTypes.LaneType");
        new ParentValidator(editor, "Ots.Definitions.DetectorTypes.DetectorType");
        new ParentValidator(editor, "Ots.Demand.OdOptions.OdOptionsItem.Markov.State")
                .setContext("Ots.Demand.OdOptions.OdOptionsItem").setIdAttribute("GtuType");
        new AttributesNotEqualValidator(editor, "Ots.Network.Link", "NodeStart", "NodeEnd");
        new NoDuplicateChildrenValidator(editor, "Ots.Models.Model.TacticalPlanner.Lmrs.MandatoryIncentives");
        new NoDuplicateChildrenValidator(editor, "Ots.Models.Model.TacticalPlanner.Lmrs.VoluntaryIncentives");
        new NoDuplicateChildrenValidator(editor, "Ots.Models.Model.TacticalPlanner.Lmrs.AccelerationIncentives");

        new AutomaticLinkId(editor);
        new AutomaticConnectorId(editor);
        new DefinitionsSaver(editor);

        // new NodeCreatedRemovedPrinter(editor);
        new RoadLayoutEditor(editor);
        new OdEditor(editor);
        new RouteEditor(editor);
        new TrafCodEditor(editor);
    }

    /**
     * Temporary stub to create parameters pane.
     * @return JComponent; component.
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
     * @return JComponent; component.
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
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    // Leave this class for debugging. It can be added by a line above that is commented out.
    private static class NodeCreatedRemovedPrinter extends AbstractNodeDecoratorRemove
    {
        /** */
        private static final long serialVersionUID = 20230910L;

        /**
         * Constructor.
         * @param editor OtsEditor; editor.
         */
        public NodeCreatedRemovedPrinter(final OtsEditor editor)
        {
            super(editor);
        }

        /** {@inheritDoc} */
        @Override
        public void notifyCreated(final XsdTreeNode node)
        {
            System.out.println("Created: " + node.getPathString());
        }

        /** {@inheritDoc} */
        @Override
        public void notifyRemoved(final XsdTreeNode node)
        {
            System.out.println("Removed: " + node.getPathString());
        }
    }

}
