package org.opentrafficsim.editor.extensions;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Editor for route.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RouteEditor implements EventListener, Consumer<XsdTreeNode>
{
    /** */
    private static final long serialVersionUID = 20230313L;

    /** Editor. */
    private OtsEditor editor;

    /**
     * Constructor.
     * @param editor editor.
     * @throws RemoteException if listener cannot be added.
     */
    public RouteEditor(final OtsEditor editor) throws RemoteException
    {
        editor.addTab("Route", null, buildRoutePane(), null);
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.editor = editor;
    }

    /**
     * Temporary stub to create route pane.
     * @return component.
     */
    private static JComponent buildRoutePane()
    {
        JLabel route = new JLabel("route");
        route.setOpaque(true);
        route.setHorizontalAlignment(JLabel.CENTER);
        return route;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        // TODO: this is a dummy implementation
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (node.isType("Ots.Demand.Route"))
            {
                node.addConsumer("Show in panel...", this);
                node.addConsumer("Compute shortest...", new Consumer<XsdTreeNode>()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void accept(final XsdTreeNode t)
                    {
                        System.out.println("We are not going to do that.");
                    }
                });
            }
        }
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
