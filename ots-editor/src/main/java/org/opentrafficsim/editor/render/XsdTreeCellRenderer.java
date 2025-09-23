package org.opentrafficsim.editor.render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.opentrafficsim.base.Resource;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Renderer for the nodes in the tree.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdTreeCellRenderer extends DefaultTreeCellRenderer
{

    /** */
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
        this.consumer =
                ImageIO.read(Resource.getResourceAsStream("./Application.png")).getScaledInstance(12, 12, Image.SCALE_SMOOTH);
        this.description =
                ImageIO.read(Resource.getResourceAsStream("./Info.png")).getScaledInstance(10, 10, Image.SCALE_SMOOTH);
        this.dropdown = ImageIO.read(Resource.getResourceAsStream("./dropdown.png"));

        this.leafIcon = new ImageIcon(ImageIO.read(Resource.getResourceAsStream("/Eclipse_file.png")));
        this.openIcon = new ImageIcon(ImageIO.read(Resource.getResourceAsStream("/Eclipse_folder_open.png")));
        this.closedIcon = new ImageIcon(ImageIO.read(Resource.getResourceAsStream("/Eclipse_folder.png")));
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
            final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        XsdTreeNode node = (XsdTreeNode) value;
        Icon customIcon = this.editor.getCustomIcon(node.getPathString());
        if (customIcon != null)
        {
            setIcon(customIcon);
        }
        if (node.hasConsumer())
        {
            preAppend(this.consumer, false);
        }
        if (node.getDescription() != null)
        {
            preAppend(this.description, false);
        }
        if (node.isChoice() && !node.isIncluded())
        {
            preAppend(this.dropdown, false);
        }
        if (node.isActive())
        {
            if (node.isIncluded())
            {
                setForeground(OtsEditor.INACTIVE_COLOR);
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
            setForeground(OtsEditor.INACTIVE_COLOR);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        return this;
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
        setIcon(new ImageIcon(image));
    }

}
