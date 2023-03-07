package org.opentrafficsim.editor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.xml.parsers.ParserConfigurationException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.xml.sax.SAXException;

/**
 * Example editor with some dummy listeners to node events for showcasing purposes.
 * @author wjschakel
 */
public final class RunEditor
{

    /**
     * Private constructor.
     */
    private RunEditor()
    {

    }

    /**
     * Runs the editor.
     * @param args String[]; arguments.
     * @throws IOException exception
     * @throws SAXException exception
     * @throws ParserConfigurationException exception
     * @throws InterruptedException exception
     * @throws URISyntaxException exception
     */
    public static void main(final String[] args)
            throws IOException, SAXException, ParserConfigurationException, InterruptedException, URISyntaxException
    {
        OtsEditor editor = new OtsEditor();

        new GenericStringFunction(editor, "OTS.NETWORKDEMAND.GENERATOR", "LINK", "LANE");

        EventListener nodeListener = new NodeListener(editor, new RouteEditor(editor));
        EventListener editorListener = new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230226L;

            @Override
            public void notify(final Event event) throws RemoteException
            {
                if (event.getType().equals(OtsEditor.SCHEMA_LOADED))
                {
                    XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
                    root.addListener(nodeListener, XsdTreeNodeRoot.NODE_CREATED);
                    root.addListener(nodeListener, XsdTreeNodeRoot.NODE_REMOVED);
                }
                else if (event.getType().equals(OtsEditor.SELECTION_CHANGED))
                {
                    JLabel label = ((JLabel) editor.getTab("OD"));
                    XsdTreeNode node = (XsdTreeNode) event.getContent();
                    if (node.getPathString().startsWith("OTS.NETWORKDEMAND.OD"))
                    {
                        label.setText(node.getPathString());
                    }
                    else
                    {
                        label.setText("od");
                    }
                }
            }
        };
        editor.addListener(editorListener, OtsEditor.SCHEMA_LOADED);
        editor.addListener(editorListener, OtsEditor.SELECTION_CHANGED);

        URL url = RunEditor.class.getResource("/resources/xsd/ots.xsd");
        editor.setSchema(XsdReader.open(url.toURI()));

    }

    /**
     * Example route editor.
     * @author wjschakel
     */
    static class RouteEditor implements Consumer<XsdTreeNode>
    {
        /** Editor. */
        private OtsEditor editor;

        /**
         * Constructor.
         * @param editor OtsEdito; editor.
         */
        RouteEditor(final OtsEditor editor)
        {
            this.editor = editor;
            JLabel route = new JLabel("route");
            route.setOpaque(true);
            route.setHorizontalAlignment(JLabel.CENTER);
            this.editor.addTab("Route", null, route, null); // adds tab that OtsEditor does not have itself
        }

        /** {@inheritDoc} */
        @Override
        public void accept(final XsdTreeNode t)
        {
            JLabel label = ((JLabel) this.editor.getTab("Route"));
            label.setText(LocalDateTime.now().toString());
            this.editor.focusTab("Route");
        }
    }

    /**
     * Node listener.
     * @author wjschakel
     */
    static class NodeListener implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20230226L;

        /** Editor. */
        private OtsEditor editor;

        /** Route editor. */
        private RouteEditor routeEditor;

        /**
         * Constructor.
         * @param editor OtsEditor; editor.
         * @param routeEditor RouteEdditor; route editor.
         */
        NodeListener(final OtsEditor editor, final RouteEditor routeEditor)
        {
            this.editor = editor;
            this.routeEditor = routeEditor;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final Event event) throws RemoteException
        {
            if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
            {
                System.out.println("Created: " + ((XsdTreeNode) event.getContent()).getPathString());

                // register string function
                XsdTreeNode node = (XsdTreeNode) event.getContent();
                if (node.getPathString().equals("OTS.NETWORKDEMAND.OD.DEMAND"))
                {
                    node.setStringFunction(new Function<XsdTreeNode, String>()
                    {
                        @Override
                        public String apply(final XsdTreeNode t)
                        {
                            String origin = t.getAttributeValue("ORIGIN");
                            String destination = t.getAttributeValue("DESTINATION");
                            return origin + " > " + destination;
                        }
                    });
                }
                if (node.getPathString().endsWith(".SPEEDLIMIT"))
                {
                    node.setStringFunction(new Function<XsdTreeNode, String>()
                    {
                        @Override
                        public String apply(final XsdTreeNode t)
                        {
                            String gtuType = t.getAttributeValue("GTUTYPE");
                            String legalSpeedLimit = t.getAttributeValue("LEGALSPEEDLIMIT");
                            return gtuType + " " + legalSpeedLimit;
                        }
                    });
                }
                if (node.getPathString().equals("OTS.NETWORK.LINK.LANEOVERRIDE"))
                {
                    node.setStringFunction(new Function<XsdTreeNode, String>()
                    {
                        @Override
                        public String apply(final XsdTreeNode t)
                        {
                            return t.getAttributeValue("LANE");
                        }
                    });
                }
                
                // register editors
                if (node.getPathString().equals("OTS.CONTROL.TRAFCOD.PROGRAM"))
                {
                    node.addConsumer("Configure...", new Consumer<XsdTreeNode>()
                    {
                        @Override
                        public void accept(final XsdTreeNode t)
                        {
                            JLabel label = (JLabel) NodeListener.this.editor.getTab("Text");
                            label.setText(LocalDateTime.now().toString());
                            NodeListener.this.editor.focusTab("Text");
                        }
                    });
                }
                if (node.getPathString().equals("OTS.DEFINITIONS.ROADLAYOUTS.ROADLAYOUT")
                        || node.getPathString().equals("OTS.NETWORK.LINK.ROADLAYOUT"))
                {
                    node.addConsumer("Edit...", new Consumer<XsdTreeNode>()
                    {
                        @Override
                        public void accept(final XsdTreeNode t)
                        {
                            JLabel label = (JLabel) NodeListener.this.editor.getTab("Road layout");
                            label.setText(LocalDateTime.now().toString());
                            NodeListener.this.editor.focusTab("Road layout");
                        }
                    });
                }
                if (node.getPathString().equals("OTS.NETWORKDEMAND.ROUTE"))
                {
                    node.addConsumer("Show in panel...", NodeListener.this.routeEditor);
                    node.addConsumer("Compute shortest...", new Consumer<XsdTreeNode>()
                    {
                        @Override
                        public void accept(final XsdTreeNode t)
                        {
                            System.out.println("We are not going to do that.");
                        }
                    });
                }

                // register value listeners
                if (node.getPathString().equals("OTS.NETWORK.LINK"))
                {
                    node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                }
            }
            else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
            {
                System.out.println("Removed: " + ((XsdTreeNode) event.getContent()).getPathString());
            }
            else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
            {

                Object[] content = (Object[]) event.getContent();
                String attribute = (String) content[1];

                if (attribute.equals("NODESTART") || attribute.equals("NODEEND"))
                {
                    XsdTreeNode node = (XsdTreeNode) content[0];
                    String nodeStart = node.getAttributeValue("NODESTART");
                    String nodeEnd = node.getAttributeValue("NODEEND");
                    String id = node.getAttributeValue("ID");
                    if (nodeStart != null && !nodeStart.isBlank() && nodeEnd != null && !nodeEnd.isBlank()
                            && (id == null || id.isBlank()))
                    {
                        node.setAttributeValue("ID", nodeStart + "-" + nodeEnd);
                    }
                }
            }
        }
    }

}
