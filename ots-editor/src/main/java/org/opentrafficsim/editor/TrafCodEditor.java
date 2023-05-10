package org.opentrafficsim.editor;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

/**
 * Editor for TrafCod program.
 * @author wjschakel
 */
public class TrafCodEditor implements EventListener, Consumer<XsdTreeNode>
{
    /** */
    private static final long serialVersionUID = 20230313L;

    /** Editor. */
    private OtsEditor editor;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if listener cannot be added.
     */
    public TrafCodEditor(final OtsEditor editor) throws RemoteException
    {
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.editor = editor;
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
            XsdTreeNode node = (XsdTreeNode) event.getContent();
            if (node.getPathString().equals("OTS.CONTROL.TrafCod.PROGRAM"))
            {
                node.addConsumer("Configure...", this);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void accept(final XsdTreeNode t)
    {
        JLabel label = (JLabel) this.editor.getTab("Text");
        label.setText(LocalDateTime.now().toString());
        this.editor.focusTab("Text");
    }
}
