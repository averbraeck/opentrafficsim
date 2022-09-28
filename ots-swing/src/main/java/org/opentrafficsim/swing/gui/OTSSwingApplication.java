package org.opentrafficsim.swing.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

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
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.dsol.OTSModelInterface;

import nl.tudelft.simulation.dsol.swing.animation.D2.AnimationPanel;

/**
 * Wrap a DSOL simulation model, or any (descendant of a) JPanel in a JFrame (wrap it in a window). The window will be
 * maximized.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-09-19 13:55:45 +0200 (Wed, 19 Sep 2018) $, @version $Revision: 4006 $, by $Author: averbraeck $,
 * initial version 16 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> model type
 */
public class OTSSwingApplication<T extends OTSModelInterface> extends JFrame
{
    /** */
    private static final long serialVersionUID = 20141216L;

    /** Single instance of default colorer, reachable from various places. */
    public static final GTUColorer DEFAULT_COLORER = new DefaultSwitchableGTUColorer();

    /** the model. */
    private final T model;

    /** whether the application has been closed or not. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean closed = false;

    /** Properties for the frame appearance (not simulation related). */
    protected Properties frameProperties;

    /** Current appearance. */
    private Appearance appearance = Appearance.GRAY;

    /**
     * Wrap an OTSModel in a JFrame. Uses a default GTU colorer.
     * @param model T; the model that will be shown in the JFrame
     * @param panel JPanel; this should be the JPanel of the simulation
     */
    public OTSSwingApplication(final T model, final JPanel panel)
    {
        this.model = model;
        setTitle("OTS | The Open Traffic Simulator | " + model.getDescription());
        setContentPane(panel);
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);

        setExitOnClose(true);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent windowEvent)
            {
                OTSSwingApplication.this.closed = true;
                super.windowClosing(windowEvent);
            }
        });

        //////////////////////
        ///// Appearance /////
        //////////////////////

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
                    OTSSwingApplication.this.frameProperties.store(writer, "OTS user settings");
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

        /** Menu class to only accept the font of an Appearance */
        class AppearanceControlMenu extends JMenu implements AppearanceControl
        {
            /** */
            private static final long serialVersionUID = 20180206L;

            /**
             * Constructor.
             * @param string String; string
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
        ButtonGroup appGroup = new ButtonGroup();
        for (Appearance appearanceValue : Appearance.values())
        {
            appGroup.add(addAppearance(app, appearanceValue));
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
        JPopupMenu popMenu = new AppearanceControlPopupMenu();
        popMenu.add(app);
        panel.setComponentPopupMenu(popMenu);

        // Set the Appearance as by frame properties
        setAppearance(getAppearance()); // color elements that were just added
    }

    /**
     * Sets an appearance.
     * @param appearance Appearance; appearance
     */
    public void setAppearance(final Appearance appearance)
    {
        this.appearance = appearance;
        setAppearance(this.getContentPane(), appearance);
        this.frameProperties.setProperty("Appearance", appearance.toString());
    }

    /**
     * Sets an appearance recursively on components.
     * @param c Component; visual component
     * @param appear Appearance; look and feel
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
        }
        else if (c instanceof AnimationPanel)
        {
            // animation backdrop
            c.setBackground(appear.getBackdrop()); // not background
            c.setForeground(appear.getForeground());
            changeFont(c, appear.getFont());
        }
        else
        {
            // default
            c.setBackground(appear.getBackground());
            c.setForeground(appear.getForeground());
            changeFont(c, appear.getFont());
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
     * @param c Component; component
     * @param font String; font name
     */
    private void changeFont(final Component c, final String font)
    {
        Font prev = c.getFont();
        c.setFont(new Font(font, prev.getStyle(), prev.getSize()));
    }

    /**
     * Returns the appearance.
     * @return Appearance; appearance
     */
    public Appearance getAppearance()
    {
        return this.appearance;
    }

    /**
     * Adds an appearance to the menu.
     * @param group JMenu; menu to add item to
     * @param appear Appearance; appearance this item selects
     * @return JMenuItem; menu item
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
     * Return the initial 'home' extent for the animation. The 'Home' button returns to this extent. Override this method when a
     * smaller or larger part of the infra should be shown. In the default setting, all currently visible objects are shown.
     * @return the initial and 'home' rectangle for the animation.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Rectangle2D makeAnimationRectangle()
    {
        return this.model.getNetwork().getExtent();
    }

    /**
     * @param exitOnClose boolean; set exitOnClose
     */
    public final void setExitOnClose(final boolean exitOnClose)
    {
        if (exitOnClose)
        {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        else
        {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
    }

    /**
     * @return closed
     */
    public final boolean isClosed()
    {
        return this.closed;
    }

    /**
     * @return model
     */
    public final T getModel()
    {
        return this.model;
    }

    /**
     * Mouse listener which shows the submenu when the mouse enters the button.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class SubMenuShower extends MouseAdapter
    {
        /** The menu. */
        private JMenu menu;

        /**
         * Constructor.
         * @param menu JMenu; menu
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class StayOpenCheckBoxMenuItem extends JCheckBoxMenuItem implements AppearanceControl
    {
        /** */
        private static final long serialVersionUID = 20180206L;

        /** Stored selection path. */
        private static MenuElement[] path;

        {
            getModel().addChangeListener(new ChangeListener()
            {

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
         * @param path MenuElement[]; path
         */
        public static void setPath(final MenuElement[] path)
        {
            StayOpenCheckBoxMenuItem.path = path;
        }

        /**
         * Constructor.
         * @param text String; menu item text
         * @param selected boolean; if the item is selected
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
            for (MenuElement element : path)
            {
                if (element instanceof JComponent)
                {
                    ((JComponent) element).setVisible(true);
                }
            }
            JMenu menu = (JMenu) path[path.length - 3];
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
