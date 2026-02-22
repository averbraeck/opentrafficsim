package org.opentrafficsim.editor.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

/**
 * Listener to any event on a table editor field that can remove itself.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FieldListener implements ActionListener
{

    /** Document listener. */
    private final DocumentListener documentListener;

    /** Pop-up menu. */
    private final JPopupMenu popup;

    /** Text editor field within a table. */
    private final JTextField field;

    /**
     * Constructor.
     * @param documentListener document listener
     * @param popup pop-up
     * @param field text editor field
     */
    public FieldListener(final DocumentListener documentListener, final JPopupMenu popup, final JTextField field)
    {
        this.documentListener = documentListener;
        this.popup = popup;
        this.field = field;
        this.field.getDocument().addDocumentListener(documentListener);
        this.field.addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        // remove pop-up when finalizing editing (hitting enter)
        remove();
    }

    /**
     * Removes this listener and the contained KeyListener from the field.
     */
    public void remove()
    {
        // remove listeners on editing field, otherwise pop-up appears on different cell when typing there
        this.popup.setVisible(false);
        this.field.getDocument().removeDocumentListener(this.documentListener);
        this.field.removeActionListener(this);
    }

}
