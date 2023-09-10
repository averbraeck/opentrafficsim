package org.opentrafficsim.editor.decoration;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.editor.extensions.OdEditor;
import org.opentrafficsim.editor.extensions.RoadLayoutEditor;
import org.opentrafficsim.editor.extensions.RouteEditor;
import org.opentrafficsim.editor.extensions.TrafCodEditor;
import org.opentrafficsim.editor.validation.ParentValidator;
import org.opentrafficsim.editor.validation.StartEndNodeValidator;

/**
 * Decorates the editor with custom icons, tabs, string functions and custom editors.
 * @author wjschakel
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
     */
    public static void decorate(final OtsEditor editor) throws IOException
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
        editor.setCustomIcon("Ots.Demand.Od", OtsEditor.loadIcon("./Table_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Models", OtsEditor.loadIcon("./Component_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Scenarios", OtsEditor.loadIcon("./Film.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Control", OtsEditor.loadIcon("./OTS_control.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Run", OtsEditor.loadIcon("./Stopwatch.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Animation", OtsEditor.loadIcon("./Play.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Output", OtsEditor.loadIcon("./Report.png", 14, 14, 16, 16)); // does not exist yet

        editor.addTab("Map", networkIcon, buildMapPane(), null);
        editor.addTab("Parameters", null, buildParameterPane(), null);
        editor.addTab("Text", null, buildTextPane(), null);

        new AttributesStringFunction(editor, "Ots.Demand.Generator", "Link", "Lane");
        new AttributesStringFunction(editor, "Ots.Demand.Od.Cell", "Origin", "Destination").setSeparator(" > ");
        new AttributesStringFunction(editor, ".SpeedLimit", "GtuType", "LegalSpeedLimit");
        new AttributesStringFunction(editor, "Ots.Network.Link.LaneOverride", "Lane");
        new ClassNameTypeStringFunction(editor);
        
        editor.addListener(new ParentValidator("Ots.Definitions.GtuTypes.GtuType"), OtsEditor.NEW_FILE);
        editor.addListener(new ParentValidator("Ots.Definitions.LinkTypes.LinkType"), OtsEditor.NEW_FILE);
        editor.addListener(new ParentValidator("Ots.Definitions.LaneTypes.LaneType"), OtsEditor.NEW_FILE);
        editor.addListener(new ParentValidator("Ots.Definitions.DetectorTypes.DetectorType"), OtsEditor.NEW_FILE);
        editor.addListener(new StartEndNodeValidator(), OtsEditor.NEW_FILE);

        editor.addListener(new AutomaticLinkId(), OtsEditor.NEW_FILE);
        editor.addListener(new XiIncludeStringFunction(), OtsEditor.NEW_FILE);
        //editor.addListener(new NodeCreatedRemovedPrinter(), OtsEditor.NEW_FILE);
        new RoadLayoutEditor(editor);
        new OdEditor(editor);
        new RouteEditor(editor);
        new TrafCodEditor(editor);
    }

    /**
     * Temporary stub to create map pane.
     * @return JComponent; component.
     */
    private static JComponent buildMapPane()
    {
        JLabel map = new JLabel("map");
        map.setOpaque(true);
        map.setHorizontalAlignment(JLabel.CENTER);
        return map;
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
     * @author wjschakel
     */
    // Leave this class for debugging. It can be added by a line above that is commented out.
    private static class NodeCreatedRemovedPrinter implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20230313L;

        /** {@inheritDoc} */
        @Override
        public void notify(final Event event) throws RemoteException
        {
            EventListener listener = new EventListener()
            {
                /** */
                private static final long serialVersionUID = 20230313L;

                /** {@inheritDoc} */
                @Override
                public void notify(final Event event) throws RemoteException
                {
                    XsdTreeNode node = (XsdTreeNode) event.getContent();
                    if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
                    {
                        System.out.println("Created: " + node.getPathString());
                    }
                    else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
                    {
                        System.out.println("Removed: " + node.getPathString());
                    }
                }
            };
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(listener, XsdTreeNodeRoot.NODE_CREATED);
            root.addListener(listener, XsdTreeNodeRoot.NODE_REMOVED);
        }
    }

}
