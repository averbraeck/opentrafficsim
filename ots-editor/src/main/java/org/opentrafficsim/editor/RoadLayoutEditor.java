package org.opentrafficsim.editor;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

/**
 * Editor for road layouts.
 * @author wjschakel
 */
public class RoadLayoutEditor implements EventListener, Consumer<XsdTreeNode>
{

    /** */
    private static final long serialVersionUID = 20230313L;
    
    /** Editor. */
    private final OtsEditor editor;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws IOException if icon cannot be loaded or listener cannot be added.
     */
    public RoadLayoutEditor(final OtsEditor editor) throws IOException
    {
        ImageIcon roadIcon = OtsEditor.loadIcon("./OTS_road.png", -1, -1, -1, -1);
        editor.addTab("Road layout", roadIcon, buildRoadLayoutPane(), null);
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.editor = editor;
    }

    /**
     * Temporary stub to create road layout pane.
     * @return JComponent; component.
     */
    private static JComponent buildRoadLayoutPane()
    {
        JLabel roadLayout = new JLabel("road layout");
        roadLayout.setOpaque(true);
        roadLayout.setHorizontalAlignment(JLabel.CENTER);
        return roadLayout;
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
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) event.getContent();
            if (node.getPathString().equals("OTS.Definitions.RoadLayouts.RoadLayout")
                    || node.getPathString().equals("OTS.Network.LINK.RoadLayout"))
            {
                node.addConsumer("Edit...", this);
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void accept(final XsdTreeNode t)
    {
        // TODO: this is a dummy implementation
        JLabel label = (JLabel) this.editor.getTab("Road layout");
        label.setText(LocalDateTime.now().toString());
        this.editor.focusTab("Road layout");
    }

}
