package org.opentrafficsim.editor.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;

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

    /** Key listener. */
    private final KeyListener fieldKeyListener;

    /** Pop-up menu. */
    private final JPopupMenu popup;

    /** Text editor field within a table. */
    private final JTextField field;

    /**
     *
     * @param fieldKeyListener key listener
     * @param popup pop-up
     * @param field text editor field
     */
    public FieldListener(final KeyListener fieldKeyListener, final JPopupMenu popup, final JTextField field)
    {
        this.fieldKeyListener = fieldKeyListener;
        this.popup = popup;
        this.field = field;
        this.field.addKeyListener(this.fieldKeyListener);
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
        // remove listeners on editing field, otherwise popup appears on different cell when typing there
        this.popup.setVisible(false);
        this.field.removeKeyListener(this.fieldKeyListener);
        this.field.removeActionListener(this);
    }

}