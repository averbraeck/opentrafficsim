package org.opentrafficsim.swing.gui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Code taken from stack overflow.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @see <a href=
 *      "https://stackoverflow.com/questions/10506789/how-to-display-faint-gray-ghost-text-in-a-jtextfield">stackoverflow</a>
 */
public class GhostText implements FocusListener, DocumentListener, PropertyChangeListener
{
    /** Text component. */
    private final JTextComponent textComp;

    /** Whether its empty. */
    private boolean isEmpty;

    /** Ghost color. */
    private Color ghostColor;

    /** Regular color. */
    private Color foregroundColor;

    /** Ghost text. */
    private final String ghostText;

    /**
     * Constructor.
     * @param textComp JTextComponent; text component to receive ghost text.
     * @param ghostText String; ghost text.
     */
    public GhostText(final JTextComponent textComp, final String ghostText)
    {
        this.textComp = textComp;
        this.ghostText = ghostText;
        this.ghostColor = Color.LIGHT_GRAY;
        textComp.addFocusListener(this);
        registerListeners();
        updateState();
        if (!this.textComp.hasFocus())
        {
            focusLost(null);
        }
    }

    /**
     * Delete ghost text.
     */
    public void delete()
    {
        unregisterListeners();
        this.textComp.removeFocusListener(this);
    }

    /**
     * Register listeners.
     */
    private void registerListeners()
    {
        this.textComp.getDocument().addDocumentListener(this);
        this.textComp.addPropertyChangeListener("foreground", this);
    }

    /**
     * Unregister listeners.
     */
    private void unregisterListeners()
    {
        this.textComp.getDocument().removeDocumentListener(this);
        this.textComp.removePropertyChangeListener("foreground", this);
    }

    /**
     * Get ghost color.
     * @return Color; ghost color.
     */
    public Color getGhostColor()
    {
        return this.ghostColor;
    }

    /**
     * Set ghost color.
     * @param ghostColor Color; ghost color.
     */
    public void setGhostColor(final Color ghostColor)
    {
        this.ghostColor = ghostColor;
    }

    /**
     * Update state.
     */
    private void updateState()
    {
        this.isEmpty = this.textComp.getText().length() == 0;
        this.foregroundColor = this.textComp.getForeground();
    }

    /** {@inheritDoc} */
    @Override
    public void focusGained(final FocusEvent e)
    {
        if (this.isEmpty)
        {
            unregisterListeners();
            try
            {
                this.textComp.setText("");
                this.textComp.setForeground(this.foregroundColor);
            }
            finally
            {
                registerListeners();
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public void focusLost(final FocusEvent e)
    {
        if (this.isEmpty)
        {
            unregisterListeners();
            try
            {
                this.textComp.setText(this.ghostText);
                this.textComp.setForeground(this.ghostColor);
            }
            finally
            {
                registerListeners();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt)
    {
        updateState();
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(final DocumentEvent e)
    {
        updateState();
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(final DocumentEvent e)
    {
        updateState();
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(final DocumentEvent e)
    {
        updateState();
    }

}
