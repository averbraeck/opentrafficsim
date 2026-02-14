package org.opentrafficsim.editor.extensions;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.animation.IconUtil;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Editor for road layouts.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RoadLayoutEditor implements EventListener, Consumer<XsdTreeNode>
{

    /** Editor. */
    private final OtsEditor editor;

    /**
     * Constructor.
     * @param editor editor.
     * @throws IOException if icon cannot be loaded or listener cannot be added.
     */
    public RoadLayoutEditor(final OtsEditor editor) throws IOException
    {
        Icon roadIcon = IconUtil.of("RoadLayout24.png").imageSize(18, 18).get();
        editor.addTab("Road layout", roadIcon, buildRoadLayoutPane(), null);
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.editor = editor;
    }

    /**
     * Temporary stub to create road layout pane.
     * @return component.
     */
    private static JComponent buildRoadLayoutPane()
    {
        JLabel roadLayout = new JLabel("road layout");
        roadLayout.setOpaque(true);
        roadLayout.setHorizontalAlignment(JLabel.CENTER);
        return roadLayout;
    }

    @Override
    public void notify(final Event event)
    {
        // TODO: this is a dummy implementation
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT) || node.getPathString().equals(XsdPaths.ROADLAYOUT))
            {
                node.addConsumer("Edit...", this);
            }
        }
    }

    @Override
    public void accept(final XsdTreeNode t)
    {
        // TODO: this is a dummy implementation
        JLabel label = (JLabel) this.editor.getTab("Road layout").get();
        label.setText(LocalDateTime.now().toString());
        this.editor.focusTab("Road layout");
    }

}
