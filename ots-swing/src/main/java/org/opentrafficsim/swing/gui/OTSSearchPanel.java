package org.opentrafficsim.swing.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The OTS search panel.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-10-11 22:54:04 +0200 (Thu, 11 Oct 2018) $, @version $Revision: 4696 $, by $Author: averbraeck $,
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSearchPanel extends JPanel implements ActionListener, FocusListener, DocumentListener
{
    /** ... */
    private static final long serialVersionUID = 20200127L;

    /** The animation panel. */
    private final OTSAnimationPanel otsAnimationPanel;
    
    /** The type-of-object-to-search-for selector. */
    private final JComboBox<String> typeToSearch;
    
    /** Id of the object to search for. */
    private final JTextField idTextField;
    
    /** Track object check box. */
    private final JCheckBox trackObject;

    /**
     * Construct a new OTSSearchPanel.
     * @param otsAnimationPanel OTSAnimationPanel; the animation panel
     */
    public OTSSearchPanel(final OTSAnimationPanel otsAnimationPanel)
    {
        this.otsAnimationPanel = otsAnimationPanel;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(new JLabel("    ")); // insert some white space in the GUI
        this.add(new JLabel(OTSControlPanel.loadIcon("/View.png")));
        String[] items = new String[] {"GTU", "Node", "Link"};
        this.typeToSearch = new JComboBox<String>(items);
        this.add(this.typeToSearch);
        this.idTextField = new JTextField("");
        this.idTextField.setPreferredSize(new Dimension(100, 0));
        this.add(this.idTextField);
        this.trackObject = new JCheckBox("track");
        this.add(this.trackObject);
        this.trackObject.setActionCommand("Tracking status changed");
        this.idTextField.setActionCommand("Id changed");
        this.typeToSearch.setActionCommand("Id changed");
        this.trackObject.addActionListener(this);
        this.idTextField.addActionListener(this);
        this.typeToSearch.addActionListener(this);
        this.idTextField.addFocusListener(this);
        this.idTextField.getDocument().addDocumentListener(this);
    }
    
    /**
     * Update all values at once.
     * @param objectType String; type of object to search
     * @param id String; id of object to search
     * @param track boolean; if true; track continuously; if false; center on it, but do not track
     */
    public void selectAndTrackObject(final String objectType, final String id, final boolean track)
    {
        for (int index = this.typeToSearch.getItemCount(); --index >= 0;)
        {
            if (this.typeToSearch.getItemAt(index).equals(objectType))
            {
                this.typeToSearch.setSelectedIndex(index);
            }
        }
        this.trackObject.setSelected(track);
        this.idTextField.setText(id);
        actionPerformed(null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        // System.out.println("1 check box isSelected=" + this.trackObject.isSelected());
        this.otsAnimationPanel.setAutoPan(this.idTextField.getText(), (String) this.typeToSearch.getSelectedItem(),
                this.trackObject.isSelected());
        // System.out.println("2 check box isSelected=" + this.trackObject.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public final void focusGained(final FocusEvent e)
    {
        actionPerformed(null);
    }

    /** {@inheritDoc} */
    @Override
    public final void focusLost(final FocusEvent e)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final void insertUpdate(final DocumentEvent e)
    {
        actionPerformed(null);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeUpdate(final DocumentEvent e)
    {
        actionPerformed(null);
    }

    /** {@inheritDoc} */
    @Override
    public final void changedUpdate(final DocumentEvent e)
    {
        actionPerformed(null);
    }

}
