package org.opentrafficsim.editor;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

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
        editor.setCustomIcon("Ots.Network.Centroid", OtsEditor.loadIcon("./OTS_centroid.png", -1, -1, -1, -1)); // not exist yet
        editor.setCustomIcon("Ots.Network.Connector", OtsEditor.loadIcon("./OTS_connector.png", -1, -1, -1, -1));
        editor.setCustomIcon(".Link", OtsEditor.loadIcon("./OTS_link.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Demand", OtsEditor.loadIcon("./Calendar.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Demand.ShortestRoute.From", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.To", nodeIcon);
        editor.setCustomIcon("Ots.Demand.ShortestRoute.Via", nodeIcon);
        editor.setCustomIcon("Ots.Demand.Od", OtsEditor.loadIcon("./Table_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Model", OtsEditor.loadIcon("./Component_blue.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Scenario", OtsEditor.loadIcon("./Film.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Control", OtsEditor.loadIcon("./OTS_control.png", -1, -1, -1, -1));
        editor.setCustomIcon("Ots.Run", OtsEditor.loadIcon("./Stopwatch.png", 16, 16, -1, -1));
        editor.setCustomIcon("Ots.Animation", OtsEditor.loadIcon("./Play.png", 14, 14, 16, 16));
        editor.setCustomIcon("Ots.Output", OtsEditor.loadIcon("./Report.png", 14, 14, 16, 16)); // does not exist yet

        editor.addTab("Map", networkIcon, buildMapPane(), null);
        editor.addTab("Parameters", null, buildParameterPane(), null);
        editor.addTab("Text", null, buildTextPane(), null);

        new GenericStringFunction(editor, "Ots.Demand.Generator", "Link", "Lane");
        new GenericStringFunction(editor, "Ots.Demand.Od.Cell", "Origin", "Destination").setSeparator(" > ");
        new GenericStringFunction(editor, ".SpeedLimit", "GtuType", "LegalSpeedLimit");
        new GenericStringFunction(editor, "Ots.Network.Link.LaneOverride", "Lane");
        
        editor.addListener(new ParentValidator("Ots.Definitions.GtuTypes.GtuType"), OtsEditor.NEW_FILE);
        editor.addListener(new ParentValidator("Ots.Definitions.LinkTypes.LinkType"), OtsEditor.NEW_FILE);
        editor.addListener(new ParentValidator("Ots.Definitions.LaneTypes.LaneType"), OtsEditor.NEW_FILE);

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
     * Sets the ID of a link if it is not defined yet, and both NODESTART and NODEEND are defined.
     * @author wjschakel
     */
    private static class AutomaticLinkId implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20230313L;

        /** {@inheritDoc} */
        @Override
        public void notify(final Event event) throws RemoteException
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(new EventListener()
            {
                /** */
                private static final long serialVersionUID = 20230313L;

                /** {@inheritDoc} */
                @Override
                public void notify(final Event event) throws RemoteException
                {
                    if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
                    {
                        XsdTreeNode node = (XsdTreeNode) event.getContent();
                        if (node.getPathString().equals("Ots.Network.Link"))
                        {
                            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                        }
                    }
                    else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
                    {
                        Object[] content = (Object[]) event.getContent();
                        String attribute = (String) content[1];
                        if (attribute.equals("NodeStart") || attribute.equals("NodeEnd"))
                        {
                            XsdTreeNode node = (XsdTreeNode) content[0];
                            String nodeStart = node.getAttributeValue("NodeStart");
                            String nodeEnd = node.getAttributeValue("NodeEnd");
                            String id = node.getAttributeValue("Id");
                            if (nodeStart != null && !nodeStart.isBlank() && nodeEnd != null && !nodeEnd.isBlank()
                                    && (id == null || id.isBlank()))
                            {
                                node.setAttributeValue("Id", nodeStart + "-" + nodeEnd);
                            }
                        }
                    }
                }
            }, XsdTreeNodeRoot.NODE_CREATED);
        }
    }

    /**
     * Adds the included file name to the include node.
     * @author wjschakel
     */
    private static class XiIncludeStringFunction implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20230313L;

        /** {@inheritDoc} */
        @Override
        public void notify(final Event event) throws RemoteException
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(new EventListener()
            {
                /** */
                private static final long serialVersionUID = 20230313L;

                /** {@inheritDoc} */
                @Override
                public void notify(final Event event) throws RemoteException
                {
                    XsdTreeNode node = (XsdTreeNode) event.getContent();
                    if (node.getNodeName().equals("xi:include"))
                    {
                        node.setStringFunction(new Function<XsdTreeNode, String>()
                        {
                            /** {@inheritDoc} */
                            @Override
                            public String apply(final XsdTreeNode t)
                            {
                                if (t.getAttributeValue(0) == null)
                                {
                                    return "";
                                }
                                return new File(t.getAttributeValue(0)).getName();
                            }
                        });
                    }
                }
            }, XsdTreeNodeRoot.NODE_CREATED);
        }
    }
    
    /**
     * Prints nodes that are created or removed.
     * @author wjschakel
     */
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
