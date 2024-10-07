package org.opentrafficsim.swing.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opentrafficsim.base.Resource;

import nl.tudelft.simulation.dsol.swing.animation.d2.VisualizationPanel;

/**
 * Application with global appearance control. Subclasses should call {@code AppearanceApplication.setDefaultFont();} before any
 * GUI elements are created (unless this is the first GUI element). Subclasses should call
 * {@code setAppearance(getAppearance());} once all elements have been added to the GUI.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AppearanceApplication extends JFrame
{

    /** */
    private static final long serialVersionUID = 20231017L;

    /** Font. */
    private static final Font FONT = new Font("Dialog", Font.PLAIN, AppearanceControl.DEFAULT_FONT_SIZE);

    /** Map of font scales. */
    private static final Map<String, Double> FONT_SCALES = new LinkedHashMap<>();

    static
    {
        FONT_SCALES.put("Small", 10.0 / 12.0);
        FONT_SCALES.put("Normal", 1.0);
        FONT_SCALES.put("Large", 14.0 / 12.0);
        FONT_SCALES.put("Very large", 16.0 / 12.0);
    }

    /** Properties for the frame appearance (not simulation related). */
    protected Properties frameProperties;

    /** Popup menu with options. */
    private final JPopupMenu popMenu;

    /** Group of appearance items. */
    private final ButtonGroup appGroup;

    /** Group of font scale items. */
    private final ButtonGroup scaleGroup;

    /** Current appearance. */
    private Appearance appearance = Appearance.GRAY;

    /** Current font scale. */
    private String fontScaleName = "Normal";

    /**
     * Constructor that uses the default content pane.
     */
    public AppearanceApplication()
    {
        this(null);
    }

    /**
     * Constructor that sets the content pane.
     * @param panel content pane.
     */
    public AppearanceApplication(final JPanel panel)
    {
        /*
         * Any application is supposed to invoke this before any GUI element is made. However, this may be the first GUI element
         * whereas no subclass can call this as there are only calls to super. Hence we need to call it here too.
         */
        AppearanceApplication.setDefaultFont();

        if (panel != null)
        {
            setContentPane(panel);
        }
        try
        {
            setIconImage(ImageIO.read(Resource.getResourceAsStream("/OTS_merge.png")));
        }
        catch (IOException io)
        {
            // accept no icon set
        }

        // Listener to write frame properties on frame close
        String sep = System.getProperty("file.separator");
        String propertiesFile = System.getProperty("user.home") + sep + "OTS" + sep + "properties.ini";
        addWindowListener(new WindowAdapter()
        {
            /** {@inheritDoce} */
            @Override
            public void windowClosing(final WindowEvent windowEvent)
            {
                try
                {
                    File f = new File(propertiesFile);
                    f.getParentFile().mkdirs();
                    FileWriter writer = new FileWriter(f);
                    AppearanceApplication.this.frameProperties.store(writer, "OTS user settings");
                }
                catch (IOException exception)
                {
                    System.err.println("Could not store properties at " + propertiesFile + ".");
                }
            }
        });

        // Set default frame properties and load properties from file (if any)
        Properties defaults = new Properties();
        defaults.setProperty("Appearance", "GRAY");
        defaults.setProperty("FontScale", "Normal");
        this.frameProperties = new Properties(defaults);
        try
        {
            FileReader reader = new FileReader(propertiesFile);
            this.frameProperties.load(reader);
        }
        catch (IOException ioe)
        {
            // ok, use defaults
        }
        this.appearance = Appearance.valueOf(this.frameProperties.getProperty("Appearance").toUpperCase());
        this.fontScaleName = this.frameProperties.getProperty("FontScale");

        /** Menu class to only accept the font of an Appearance */
        class AppearanceControlMenu extends JMenu implements AppearanceControl
        {
            /** */
            private static final long serialVersionUID = 20180206L;

            /**
             * Constructor.
             * @param string string
             */
            AppearanceControlMenu(final String string)
            {
                super(string);
            }

            /** {@inheritDoc} */
            @Override
            public boolean isFont()
            {
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public String toString()
            {
                return "AppearanceControlMenu []";
            }
        }

        // Appearance menu
        JMenu app = new AppearanceControlMenu("Appearance");
        app.addMouseListener(new SubMenuShower(app));
        this.appGroup = new ButtonGroup();
        for (Appearance appearanceValue : Appearance.values())
        {
            this.appGroup.add(addAppearance(app, appearanceValue));
        }
        JMenu scale = new AppearanceControlMenu("Font size");
        scale.addMouseListener(new SubMenuShower(scale));
        this.scaleGroup = new ButtonGroup();
        for (String fontScaleName : FONT_SCALES.keySet())
        {
            this.scaleGroup.add(addFontsize(scale, fontScaleName));
        }

        /** PopupMenu class to only accept the font of an Appearance */
        class AppearanceControlPopupMenu extends JPopupMenu implements AppearanceControl
        {
            /** */
            private static final long serialVersionUID = 20180206L;

            /** {@inheritDoc} */
            @Override
            public boolean isFont()
            {
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public String toString()
            {
                return "AppearanceControlPopupMenu []";
            }
        }

        // Popup menu to change appearance
        this.popMenu = new AppearanceControlPopupMenu();
        this.popMenu.add(app);
        this.popMenu.add(scale);
        ((JPanel) getContentPane()).setComponentPopupMenu(this.popMenu);
    }

    /**
     * Set font scale.
     * @param fontScaleName font scale name.
     */
    public void setFontScale(final String fontScaleName)
    {
        this.fontScaleName = fontScaleName;
        setAppearance(getAppearance());
    }

    /**
     * Sets an appearance.
     * @param appearance appearance
     */
    public void setAppearance(final Appearance appearance)
    {
        this.appearance = appearance;
        setAppearance(this.popMenu, appearance);
        for (Enumeration<AbstractButton> c = this.appGroup.getElements(); c.hasMoreElements();)
        {
            setAppearance(c.nextElement(), appearance);
        }
        for (Enumeration<AbstractButton> c = this.scaleGroup.getElements(); c.hasMoreElements();)
        {
            setAppearance(c.nextElement(), appearance);
        }
        setAppearance(getContentPane(), appearance);
        this.frameProperties.setProperty("Appearance", appearance.toString());
        this.frameProperties.setProperty("FontScale", this.fontScaleName);
    }

    /**
     * Sets an appearance recursively on components.
     * @param c visual component
     * @param appear look and feel
     */
    private void setAppearance(final Component c, final Appearance appear)
    {
        if (c instanceof AppearanceControl)
        {
            AppearanceControl ac = (AppearanceControl) c;
            if (ac.isBackground())
            {
                c.setBackground(appear.getBackground());
            }
            if (ac.isForeground())
            {
                c.setForeground(appear.getForeground());
            }
            if (ac.isFont())
            {
                changeFont(c, appear.getFont());
            }
            if (ac.getFontSize() != null)
            {
                changeFontSize(c);
            }
        }
        else if (VisualizationPanel.class.isAssignableFrom(c.getClass()))
        {
            // animation backdrop
            c.setBackground(appear.getBackdrop()); // not background
            c.setForeground(appear.getForeground());
            changeFont(c, appear.getFont());
            changeFontSize(c);
        }
        else
        {
            // default
            c.setBackground(appear.getBackground());
            c.setForeground(appear.getForeground());
            changeFont(c, appear.getFont());
            changeFontSize(c);
        }
        if (c instanceof JSlider)
        {
            // labels of the slider
            Dictionary<?, ?> dictionary = ((JSlider) c).getLabelTable();
            Enumeration<?> keys = dictionary.keys();
            while (keys.hasMoreElements())
            {
                JLabel label = (JLabel) dictionary.get(keys.nextElement());
                label.setForeground(appear.getForeground());
                label.setBackground(appear.getBackground());
            }
        }
        // children
        if (c instanceof JComponent)
        {
            for (Component child : ((JComponent) c).getComponents())
            {
                setAppearance(child, appear);
            }
        }
    }

    /**
     * Change font on component.
     * @param c component
     * @param font font name
     */
    protected void changeFont(final Component c, final String font)
    {
        Font prev = c.getFont();
        c.setFont(new Font(font, prev.getStyle(), prev.getSize()));
    }

    /**
     * Changes the font size of the component.
     * @param c component.
     */
    protected void changeFontSize(final Component c)
    {
        Font prev = c.getFont();
        int size;
        if (c instanceof AppearanceControl)
        {
            AppearanceControl a = (AppearanceControl) c;
            if (a.getFontSize() != null)
            {
                size = (int) (a.getFontSize() * FONT_SCALES.get(this.fontScaleName));
            }
            else
            {
                size = prev.getSize();
            }
        }
        else
        {
            size = (int) (AppearanceControl.DEFAULT_FONT_SIZE * FONT_SCALES.get(this.fontScaleName));
        }
        c.setFont(new Font(prev.getFontName(), prev.getStyle(), size));
    }

    /**
     * Returns the appearance.
     * @return appearance
     */
    public Appearance getAppearance()
    {
        return this.appearance;
    }

    /**
     * Adds an appearance to the menu.
     * @param group menu to add item to
     * @param appear appearance this item selects
     * @return menu item
     */
    private JMenuItem addAppearance(final JMenu group, final Appearance appear)
    {
        JCheckBoxMenuItem check = new StayOpenCheckBoxMenuItem(appear.getName(), appear.equals(getAppearance()));
        check.addMouseListener(new MouseAdapter()
        {
            /** {@inheritDoc} */
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                setAppearance(appear);
            }
        });
        return group.add(check);
    }

    /**
     * Adds an appearance to the menu.
     * @param group menu to add item to
     * @param fontScaleName font scale name
     * @return menu item
     */
    private JMenuItem addFontsize(final JMenu group, final String fontScaleName)
    {
        JCheckBoxMenuItem check = new StayOpenCheckBoxMenuItem(fontScaleName, this.fontScaleName.equals(fontScaleName));
        check.addMouseListener(new MouseAdapter()
        {
            /** {@inheritDoc} */
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                setFontScale(fontScaleName);
            }
        });
        return group.add(check);
    }

    /**
     * Sets default font in the UIManager. This should be invoked by any application before any GUI element is created.
     */
    public static void setDefaultFont()
    {
        UIManager.put("Label.font", FONT);
        UIManager.put("Menu.font", FONT);
        UIManager.put("MenuItem.font", FONT);
        UIManager.put("TabbedPane.font", FONT);
        UIManager.put("Table.font", FONT);
        UIManager.put("TableHeader.font", FONT);
        UIManager.put("TextField.font", FONT);
        UIManager.put("Button.font", FONT);
        UIManager.put("ComboBox.font", FONT);
        UIManager.put("CheckBox.font", FONT);
        UIManager.put("CheckBoxMenuItem.font", FONT);
        // for full list: https://stackoverflow.com/questions/7434845/setting-the-default-font-of-swing-program
    }

    /**
     * Mouse listener which shows the submenu when the mouse enters the button.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class SubMenuShower extends MouseAdapter
    {
        /** The menu. */
        private JMenu menu;

        /**
         * Constructor.
         * @param menu menu
         */
        SubMenuShower(final JMenu menu)
        {
            this.menu = menu;
        }

        /** {@inheritDoc} */
        @Override
        public void mouseEntered(final MouseEvent e)
        {
            MenuSelectionManager.defaultManager().setSelectedPath(
                    new MenuElement[] {(MenuElement) this.menu.getParent(), this.menu, this.menu.getPopupMenu()});
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "SubMenuShower [menu=" + this.menu + "]";
        }
    }

    /**
     * Check box item that keeps the popup menu visible after clicking, so the user can click and try some options.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private static class StayOpenCheckBoxMenuItem extends JCheckBoxMenuItem implements AppearanceControl
    {
        /** */
        private static final long serialVersionUID = 20180206L;

        /** Stored selection path. */
        private static MenuElement[] PATH;
        {
            getModel().addChangeListener(new ChangeListener()
            {
                /** {@inheritDoc} */
                @Override
                public void stateChanged(final ChangeEvent e)
                {
                    if (getModel().isArmed() && isShowing())
                    {
                        setPath(MenuSelectionManager.defaultManager().getSelectedPath());
                    }
                }
            });
        }

        /**
         * Sets the path.
         * @param path path
         */
        public static void setPath(final MenuElement[] path)
        {
            StayOpenCheckBoxMenuItem.PATH = path;
        }

        /**
         * Constructor.
         * @param text menu item text
         * @param selected if the item is selected
         */
        StayOpenCheckBoxMenuItem(final String text, final boolean selected)
        {
            super(text, selected);
        }

        /** {@inheritDoc} */
        @Override
        public void doClick(final int pressTime)
        {
            super.doClick(pressTime);
            for (MenuElement element : PATH)
            {
                if (element instanceof JComponent)
                {
                    ((JComponent) element).setVisible(true);
                }
            }
            JMenu menu = (JMenu) PATH[PATH.length - 3];
            MenuSelectionManager.defaultManager()
                    .setSelectedPath(new MenuElement[] {(MenuElement) menu.getParent(), menu, menu.getPopupMenu()});
        }

        /** {@inheritDoc} */
        @Override
        public boolean isFont()
        {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "StayOpenCheckBoxMenuItem []";
        }
    }

}
