package org.opentrafficsim.editor.render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.opentrafficsim.animation.data.util.IconUtil;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeUtil;
import org.opentrafficsim.editor.XsdTreeTableModel;

/**
 * Renderer for the nodes in the tree.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class XsdTreeCellRenderer extends DefaultTreeCellRenderer
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20230221L;

    /** Number of pixels between icons when they are combined. */
    private static final int ICON_MARGIN = 3;

    /** Editor. */
    private final OtsEditor editor;

    /** Image for nodes with a consumer, typically an editor. */
    private final Image consumer;

    /** Image for nodes with a description. */
    private final Image description;

    /** Icon for option nodes, indicating a drop-down can be show. */
    private final Image dropdown;

    /**
     * Constructor. Loads icons.
     * @param editor editor.
     * @throws IOException if an icon could not be loaded from resources.
     */
    public XsdTreeCellRenderer(final OtsEditor editor) throws IOException
    {
        this.editor = editor;
        this.consumer = IconUtil.asImage("Application24.png").getScaledInstance(12, 12, Image.SCALE_SMOOTH);
        this.description = IconUtil.asImage("Information24.png").getScaledInstance(12, 12, Image.SCALE_SMOOTH);
        this.dropdown = IconUtil.asImage("Dropdown24.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH);

        this.leafIcon = IconUtil.of("File24.png").imageSize(16, 16).get();
        this.openIcon = IconUtil.of("FolderOpen24.png").imageSize(16, 16).get();
        this.closedIcon = IconUtil.of("Folder24.png").imageSize(16, 16).get();
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
            final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        XsdTreeNode node = (XsdTreeNode) value;
        String filter = ((XsdTreeTableModel) tree.getModel()).getActiveFilterText();
        if (filter != null)
        {
            setText(highlightNodeText(node, filter));
        }
        Optional<Icon> customIcon = this.editor.getCustomIcon(node.getPathString());
        if (customIcon.isPresent())
        {
            setIcon(customIcon.get());
        }
        if (node.hasConsumer())
        {
            preAppend(this.consumer, false);
        }
        if (node.getDescription().isPresent())
        {
            preAppend(this.description, false);
        }
        if (filter == null && node.isChoice() && !node.isIncluded()
                && !node.equals(((XsdTreeTableModel) tree.getModel()).getRoot()))
        {
            preAppend(this.dropdown, false);
        }
        if (node.isActive())
        {
            if (node.isIncluded())
            {
                setForeground(OtsEditor.getInactiveColor());
            }
            else
            {
                setForeground(UIManager.getColor("Table.foreground"));
            }
            if (node.equals(this.editor.getChoiceNode()))
            {
                // draws cell to mimic header bar above choice menu
                setOpaque(true);
                setBackground(new Color(UIManager.getColor("Panel.background").getRGB())); // ColorUIResource is ignored
                setBorder(new LineBorder(UIManager.getColor("Menu.acceleratorForeground"), 1, false));
            }
            else
            {
                if (node.isValid())
                {
                    if (node.hasExpression())
                    {
                        setOpaque(true);
                        setBackground(OtsEditor.getExpressionColor());
                    }
                    else
                    {
                        setOpaque(false);
                    }
                }
                else
                {
                    setOpaque(true);
                    setBackground(OtsEditor.getInvalidColor());
                }
                setBorder(new EmptyBorder(0, 0, 0, 0));
            }
        }
        else
        {
            setOpaque(false);
            setForeground(OtsEditor.getInactiveColor());
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        return this;
    }

    /**
     * Returns the display text with the matching filter part in bold. This only affects rendering.
     * @param node node
     * @param filter active filter text (lower case)
     * @return display text with highlighted filter match
     */
    private static String highlightNodeText(final XsdTreeNode node, final String filter)
    {
        String fullText = node.toString();
        int[] range = filterRange(fullText, filter);
        if (range == null)
        {
            return htmlEscape(fullText);
        }
        return "<html>" + htmlEscape(fullText.substring(0, range[0])) + "<b>"
                + htmlEscape(fullText.substring(range[0], range[1])) + "</b>" + htmlEscape(fullText.substring(range[1]))
                + "</html>";
    }

    /**
     * Returns start/end indices in the original label for the first filter match, or {@code null} if there is no match.
     * @param label label to search in
     * @param filter normalized lower-case filter text
     * @return start/end indices in the original label for the first filter match, or {@code null} if there is no match
     */
    private static int[] filterRange(final String label, final String filter)
    {
        String lower = label.toLowerCase();
        String thinAsSpace = lower.replace(XsdTreeNodeUtil.THIN_SPACE, " ");
        int direct = thinAsSpace.indexOf(filter);
        if (direct >= 0)
        {
            return new int[] {direct, direct + filter.length()};
        }
        String thinRemoved = lower.replace(XsdTreeNodeUtil.THIN_SPACE, "");
        int compact = thinRemoved.indexOf(filter);
        if (compact < 0)
        {
            return null;
        }
        return compactRangeToOriginal(label, compact, filter.length());
    }

    /**
     * Maps a match range in the label with removed thin spaces back to indices in the original label.
     * @param label original label
     * @param startCompact start index in compact label
     * @param lengthCompact match length in compact label
     * @return start/end range in original label
     */
    private static int[] compactRangeToOriginal(final String label, final int startCompact, final int lengthCompact)
    {
        char thin = XsdTreeNodeUtil.THIN_SPACE.charAt(0);
        int compactPos = 0;
        int start = -1;
        int end = label.length();
        for (int i = 0; i < label.length(); i++)
        {
            if (label.charAt(i) == thin)
            {
                continue;
            }
            if (compactPos == startCompact)
            {
                start = i;
            }
            compactPos++;
            if (compactPos == startCompact + lengthCompact)
            {
                end = i + 1;
                break;
            }
        }
        if (start < 0)
        {
            start = 0;
        }
        return new int[] {start, end};
    }

    /**
     * Escapes HTML-sensitive characters in plain text.
     * @param text plain text
     * @return escaped text
     */
    private static String htmlEscape(final String text)
    {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Pre-/appends the provided image to the left-hand or right-hand side of the current icon.
     * @param iconImage image to pre-/append.
     * @param prepend when true, the image is prepended rather than appended.
     */
    private void preAppend(final Image iconImage, final boolean prepend)
    {
        Icon base = getIcon();
        int w = base.getIconWidth() + ICON_MARGIN + iconImage.getWidth(null);
        int h = Math.max(base.getIconHeight(), iconImage.getHeight(null));
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        int dy = (h - base.getIconHeight()) / 2;
        int x = prepend ? iconImage.getWidth(null) + ICON_MARGIN : 0;
        base.paintIcon(this, g, x, dy);
        dy = (h - iconImage.getHeight(null)) / 2;
        x = prepend ? 0 : base.getIconWidth() + ICON_MARGIN;
        g.drawImage(iconImage, x, dy, null);
        setIcon(new IconUtil.InterpolatedImageIcon(image));
    }

}
