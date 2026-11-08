package org.opentrafficsim.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.opentrafficsim.animation.data.util.IconUtil;

/**
 * Adds search support to any {@link JComboBox}. When a user start typing, or ctrl+clicks, on the {@link JComboBox}, a small
 * dialog appears. This dialog contains an edit field where the user can type the searched text, and a list of values from the
 * {@link JComboBox} that contain the searched text (case-insensitive). The user may resize the window. This size is stored
 * under a properties key such that the size is restored on later usage. Use as:
 *
 * <pre>
 * JComboBox myComboBox = new JComboBox();
 * ComboBoxSearchSupport.install(myComboBox, "myComboBox");
 * </pre>
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public final class ComboBoxSearchSupport
{

    /** Combo box. */
    private final JComboBox<String> comboBox;

    /** Popup dialog. */
    private final JDialog popup;

    /** Search field. */
    private final JTextField searchField = new JTextField();

    /** Result list. */
    private final JList<String> resultList = new JList<>();

    /** Model of result list. */
    private final DefaultListModel<String> listModel = new DefaultListModel<>();

    /**
     * Constructor.
     * @param comboBox combo box
     * @param propertiesKey properties key, may be {@code null} in which case the size is not stored
     */
    private ComboBoxSearchSupport(final JComboBox<String> comboBox, final String propertiesKey)
    {
        Window owner = SwingUtilities.getWindowAncestor(comboBox);
        this.popup = new JDialog(owner);
        try
        {
            this.popup.setIconImage(IconUtil.asImage("Search24.png"));
        }
        catch (IOException ex)
        {
            //
        }
        this.popup.setAlwaysOnTop(true);
        this.popup.setResizable(true);
        this.popup.addWindowFocusListener(new WindowAdapter()
        {
            @Override
            public void windowLostFocus(final WindowEvent e)
            {
                ComboBoxSearchSupport.this.popup.setVisible(false);
            }
        });
        this.comboBox = comboBox;
        this.resultList.setModel(this.listModel);
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 3, 2, 2));
        panel.add(this.searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(this.resultList), BorderLayout.CENTER);
        Dimension dim = new Dimension(250, 210);
        panel.setPreferredSize(dim);
        this.popup.setContentPane(panel);
        this.popup.pack();
        if (propertiesKey != null)
        {
            String wKey = "search." + propertiesKey + ".width";
            String hKey = "search." + propertiesKey + ".height";
            int w = OtsEditor.PROPERTIES_STORE.getOptionalInteger(wKey).orElse(dim.width);
            int h = OtsEditor.PROPERTIES_STORE.getOptionalInteger(hKey).orElse(dim.height);
            Timer resizeTimer = new Timer(200, (e) ->
            {
                Dimension size = this.popup.getSize();
                OtsEditor.PROPERTIES_STORE.setInt(wKey, size.width);
                OtsEditor.PROPERTIES_STORE.setInt(hKey, size.height);
            });
            resizeTimer.setRepeats(false);
            this.popup.setSize(new Dimension(w, h));
            this.popup.addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(final ComponentEvent e)
                {
                    resizeTimer.restart();
                }
            });
        }
        this.resultList.setCellRenderer(new HighlightRenderer(this.searchField));
        this.resultList.setFont(this.resultList.getFont().deriveFont(Font.PLAIN));
        this.searchField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(final DocumentEvent e)
            {
                updateFilter();
            }

            @Override
            public void removeUpdate(final DocumentEvent e)
            {
                updateFilter();
            }

            @Override
            public void changedUpdate(final DocumentEvent e)
            {
                updateFilter();
            }
        });
        this.searchField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(final KeyEvent e)
            {
                switch (e.getKeyCode())
                {
                    case KeyEvent.VK_DOWN:
                        moveSelection(+1);
                        break;
                    case KeyEvent.VK_UP:
                        moveSelection(-1);
                        break;
                    case KeyEvent.VK_ENTER:
                        acceptValue();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        ComboBoxSearchSupport.this.popup.setVisible(false);
                        break;
                    default:
                        // ignore
                }
            }
        });
        this.resultList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    acceptValue();
                }
            }
        });
    }

    /**
     * Show with initial text.
     * @param initialText initial text (empty, or that which started the popup)
     */
    private void show(final String initialText)
    {
        if (this.comboBox.getModel().getSize() <= 0)
        {
            return;
        }
        this.searchField.setText(initialText);
        updateFilter();
        Point p = this.comboBox.getLocationOnScreen();
        Rectangle screen = this.comboBox.getGraphicsConfiguration().getBounds();
        Dimension size = this.popup.getSize();
        int x = p.x;
        int y = p.y + this.comboBox.getHeight();
        if (y + size.height > screen.y + screen.height)
        {
            y = p.y - size.height;
        }
        this.popup.setLocation(x, y);
        this.popup.setVisible(true);
        SwingUtilities.invokeLater(() -> this.searchField.requestFocusInWindow());
    }

    /**
     * Update the value filter.
     */
    private void updateFilter()
    {
        String filter = this.searchField.getText().toLowerCase();
        this.listModel.clear();
        ComboBoxModel<String> model = this.comboBox.getModel();
        for (int i = 0; i < model.getSize(); i++)
        {
            String value = model.getElementAt(i);
            if (filter.isBlank() || value.toLowerCase().contains(filter))
            {
                this.listModel.addElement(value);
            }
        }
        if (!this.listModel.isEmpty())
        {
            this.resultList.setSelectedIndex(0);
        }
        this.resultList.repaint();
    }

    /**
     * Change list selection.
     * @param delta delta
     */
    private void moveSelection(final int delta)
    {
        int index = this.resultList.getSelectedIndex();
        if (index < 0)
        {
            index = 0;
        }
        index = Math.max(0, Math.min(this.listModel.size() - 1, index + delta));
        this.resultList.setSelectedIndex(index);
        this.resultList.ensureIndexIsVisible(index);
    }

    /**
     * Accept selected value.
     */
    private void acceptValue()
    {
        String value = this.resultList.getSelectedValue();
        if (value != null)
        {
            this.comboBox.setSelectedItem(value);
        }
        this.popup.setVisible(false);
    }

    /**
     * Install search support.
     * @param comboBox combo box
     * @param propertiesKey properties key, may be {@code null} in which case the size is not stored
     */
    public static void install(final JComboBox<String> comboBox, final String propertiesKey)
    {
        ComboBoxSearchSupport search = new ComboBoxSearchSupport(comboBox, propertiesKey);
        comboBox.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(final KeyEvent e)
            {
                char c = e.getKeyChar();
                if (Character.isISOControl(c))
                {
                    return;
                }
                search.show(String.valueOf(c));
                e.consume();
            }
        });
        comboBox.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                if (e.isControlDown())
                {
                    search.show("");
                }
            }
        });
    }

    /**
     * Highlight renderer (matched text as bold).
     */
    private static class HighlightRenderer extends DefaultListCellRenderer
    {
        /** Serial id. */
        private static final long serialVersionUID = 20261107L;

        /** Search field. */
        private final JTextField searchField;

        /**
         * Constructor.
         * @param searchField search field
         */
        HighlightRenderer(final JTextField searchField)
        {
            this.searchField = searchField;
        }

        @Override
        public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = String.valueOf(value);
            String filter = this.searchField.getText();
            if (!filter.isBlank())
            {
                int pos = text.toLowerCase().indexOf(filter.toLowerCase());
                if (pos >= 0)
                {
                    String before = text.substring(0, pos);
                    String match = text.substring(pos, pos + filter.length());
                    String after = text.substring(pos + filter.length());
                    label.setText("<html>" + before + "<b>" + match + "</b>" + after + "</html>");
                }
            }
            return label;
        }
    }
}
