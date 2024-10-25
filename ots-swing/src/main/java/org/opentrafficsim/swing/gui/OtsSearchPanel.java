package org.opentrafficsim.swing.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.djutils.base.Identifiable;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * The OTS search panel.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class OtsSearchPanel extends JPanel implements ActionListener, FocusListener, DocumentListener
{
    /** ... */
    private static final long serialVersionUID = 20200127L;

    /** The animation panel. */
    private final OtsAnimationPanel otsAnimationPanel;

    /** The type-of-object-to-search-for selector. */
    private final JComboBox<ObjectKind<?>> typeToSearch;

    /** Id of the object to search for. */
    private final JTextField idTextField;

    /** Track object check box. */
    private final JCheckBox trackObject;

    /**
     * Construct a new OtsSearchPanel.
     * @param otsAnimationPanel the animation panel
     */
    public OtsSearchPanel(final OtsAnimationPanel otsAnimationPanel)
    {
        this.otsAnimationPanel = otsAnimationPanel;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(Box.createHorizontalStrut(10));
        this.add(new JLabel(OtsControlPanel.loadIcon("/View.png")));
        this.add(Box.createHorizontalStrut(5));
        ObjectKind<?>[] objectKinds = new ObjectKind[] {new ObjectKind<Gtu>("GTU")
        {
            @Override
            Gtu searchNetwork(final Network network, final String id)
            {
                return network.getGTU(id);
            }
        }, new ObjectKind<Node>("Node")
        {
            @Override
            Node searchNetwork(final Network network, final String id)
            {
                return network.getNode(id);
            }
        }, new ObjectKind<Link>("Link")
        {
            @Override
            Link searchNetwork(final Network network, final String id)
            {
                return network.getLink(id);
            }
        }};
        this.typeToSearch = new JComboBox<ObjectKind<?>>(objectKinds);
        this.add(this.typeToSearch);

        /** Text field with appearance control. */
        this.idTextField = new AppearanceControlTextField();
        this.idTextField.setPreferredSize(new Dimension(100, 0));
        this.add(this.idTextField);
        this.trackObject = new JCheckBox("Track");
        this.add(this.trackObject);
        this.trackObject.setActionCommand("Tracking status changed");
        this.idTextField.setActionCommand("Id changed");
        this.typeToSearch.setActionCommand("Type changed");
        this.trackObject.addActionListener(this);
        this.idTextField.addActionListener(this);
        this.typeToSearch.addActionListener(this);
        this.idTextField.addFocusListener(this);
        this.idTextField.getDocument().addDocumentListener(this);
        new GhostText(this.idTextField, "Id...").setGhostColor(Color.GRAY);
    }

    /**
     * Update all values at once.
     * @param objectKey key of the object type to search
     * @param id id of object to search
     * @param track if true; track continuously; if false; center on it, but do not track
     */
    public void selectAndTrackObject(final String objectKey, final String id, final boolean track)
    {
        for (int index = this.typeToSearch.getItemCount(); --index >= 0;)
        {
            if (this.typeToSearch.getItemAt(index).getKey().equals(objectKey))
            {
                this.typeToSearch.setSelectedIndex(index);
            }
        }
        this.trackObject.setSelected(track);
        this.idTextField.setText(id);
        actionPerformed(null);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        this.otsAnimationPanel.setAutoPan(this.idTextField.getText(), (ObjectKind<?>) this.typeToSearch.getSelectedItem(),
                this.trackObject.isSelected());
    }

    @Override
    public final void focusGained(final FocusEvent e)
    {
        actionPerformed(null);
    }

    @Override
    public final void focusLost(final FocusEvent e)
    {
        // Do nothing
    }

    @Override
    public final void insertUpdate(final DocumentEvent e)
    {
        actionPerformed(null);
    }

    @Override
    public final void removeUpdate(final DocumentEvent e)
    {
        actionPerformed(null);
    }

    @Override
    public final void changedUpdate(final DocumentEvent e)
    {
        actionPerformed(null);
    }

    /**
     * Entries in the typeToSearch JComboBox of the OTS search panel.
     * @param <T> Type of object identified by key
     */
    abstract static class ObjectKind<T extends Locatable & Identifiable>
    {
        /** The key of this ObjectKind. */
        private final String key;

        /**
         * Construct a new ObjectKind (entry in the combo box).
         * @param key the key of the new ObjectKind
         */
        ObjectKind(final String key)
        {
            this.key = key;
        }

        /**
         * Retrieve the key.
         * @return the key
         */
        public Object getKey()
        {
            return this.key;
        }

        /**
         * Lookup an object of type T in an OTS network.
         * @param network the OTS network
         * @param id id of the object to return
         * @return the object in the network of the correct type and matching id, or null if no matching object was found.
         */
        abstract T searchNetwork(Network network, String id);

        /**
         * Produce the text that will appear in the combo box. This method should be overridden to implement localization.
         */
        @Override
        public String toString()
        {
            return this.key;
        }
    }
}
