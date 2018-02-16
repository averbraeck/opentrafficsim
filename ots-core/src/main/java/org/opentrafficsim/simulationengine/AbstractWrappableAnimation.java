package org.opentrafficsim.simulationengine;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3d;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.gui.Appearance;
import org.opentrafficsim.gui.AppearanceControl;
import org.opentrafficsim.gui.OTSAnimationPanel;
import org.opentrafficsim.gui.SimulatorFrame;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractWrappableAnimation implements WrappableAnimation, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The properties exhibited by this simulation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected List<Property<?>> properties = new ArrayList<>();

    /** The properties after (possible) editing by the user. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected List<Property<?>> savedUserModifiedProperties;

    /** Properties for the frame appearance (not simulation related). */
    protected Properties frameProperties;

    /** Use EXIT_ON_CLOSE when true, DISPOSE_ON_CLOSE when false on closing of the window. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean exitOnClose;

    /** The tabbed panel so other tabs can be added by the classes that extend this class. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected OTSAnimationPanel panel;

    /** Save the startTime for restarting the simulation. */
    private Time savedStartTime;

    /** Save the startTime for restarting the simulation. */
    private Duration savedWarmupPeriod;

    /** Save the runLength for restarting the simulation. */
    private Duration savedRunLength;

    /** The model. */
    private OTSModelInterface model;

    /** Override the replication number by this value if non-null. */
    private Integer replication = null;

    /** Current appearance. */
    private Appearance appearance = Appearance.GRAY;

    /** Colorer. */
    private GTUColorer colorer = new DefaultSwitchableGTUColorer();

    /**
     * Build the animator.
     * @param startTime Time; the start time
     * @param warmupPeriod Duration; the warm up period
     * @param runLength Duration; the duration of the simulation / animation
     * @param otsModel OTSModelInterface; the simulation model
     * @return SimpleAnimator; a newly constructed animator
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected SimpleAnimator buildSimpleAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface otsModel) throws SimRuntimeException, NamingException, PropertyException
    {
        return new SimpleAnimator(startTime, warmupPeriod, runLength, otsModel);
    }

    /**
     * Build the animator with the specified replication number.
     * @param startTime Time; the start time
     * @param warmupPeriod Duration; the warm up period
     * @param runLength Duration; the duration of the simulation / animation
     * @param otsModel OTSModelInterface; the simulation model
     * @param replicationNumber int; the replication number
     * @return SimpleAnimator; a newly constructed animator
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected SimpleAnimator buildSimpleAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface otsModel, final int replicationNumber)
            throws SimRuntimeException, NamingException, PropertyException
    {
        return new SimpleAnimator(startTime, warmupPeriod, runLength, otsModel, replicationNumber);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimpleAnimator buildAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final List<Property<?>> userModifiedProperties, final Rectangle rect, final boolean eoc)
            throws SimRuntimeException, NamingException, OTSSimulationException, PropertyException
    {

        this.savedUserModifiedProperties = userModifiedProperties;
        this.exitOnClose = eoc;
        this.savedStartTime = startTime;
        this.savedWarmupPeriod = warmupPeriod;
        this.savedRunLength = runLength;
        this.model = makeModel();
        if (null == this.model)
        {
            return null; // Happens when the user cancels a file open dialog
        }

        // Animator
        final SimpleAnimator simulator =
                null == this.replication ? buildSimpleAnimator(startTime, warmupPeriod, runLength, this.model)
                        : buildSimpleAnimator(startTime, warmupPeriod, runLength, this.model, this.replication);
        try
        {
            this.panel = new OTSAnimationPanel(makeAnimationRectangle(), new Dimension(1024, 768), simulator, this,
                    getColorer(), this.model.getNetwork());
        }
        catch (RemoteException exception)
        {
            throw new SimRuntimeException(exception);
        }

        // Case specific GUI elements
        addAnimationToggles();
        addTabs(simulator);

        // Frame
        SimulatorFrame frame = new SimulatorFrame(shortName(), this.panel);
        if (rect != null)
        {
            frame.setBounds(rect);
        }
        else
        {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        frame.setDefaultCloseOperation(this.exitOnClose ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);

        ////////////////////////////////////////
        ///// Look and Feel and Appearance /////
        ////////////////////////////////////////

        // Listener to write frame properties on frame close
        String sep = System.getProperty("file.separator");
        String propertiesFile = System.getProperty("user.home") + sep + "OTS" + sep + "properties.ini";
        frame.addWindowListener(new WindowAdapter()
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
                    AbstractWrappableAnimation.this.frameProperties.store(writer, "OTS user settings");
                }
                catch (@SuppressWarnings("unused") IOException exception)
                {
                    System.err.println("Could not store properties at " + propertiesFile + ".");
                }
            }
        });

        // Set default frame properties and load properties from file (if any)
        Properties defaults = new Properties();
        defaults.setProperty("Appearance", "GRAY");
        defaults.setProperty("LookAndFeel", "javax.swing.plaf.metal.MetalLookAndFeel");
        this.frameProperties = new Properties(defaults);
        try
        {
            FileReader reader = new FileReader(propertiesFile);
            this.frameProperties.load(reader);
        }
        catch (@SuppressWarnings("unused") IOException ioe)
        {
            // ok, use defaults
        }
        this.appearance = Appearance.valueOf(this.frameProperties.getProperty("Appearance").toUpperCase());

        // Menu class to only accept the font of an Appearance
        class AppearanceControlMenu extends JMenu implements AppearanceControl
        {
            /** */
            private static final long serialVersionUID = 20180206L;

            public AppearanceControlMenu(final String string)
            {
                super(string);
            }

            /** {@inheritDoc} */
            @Override
            public boolean isFont()
            {
                return true;
            }
        }

        // Look and feel menu
        JMenu laf = new AppearanceControlMenu("Look and feel");
        laf.addMouseListener(new SubMenuShower(laf));
        ButtonGroup lafGroup = new ButtonGroup();
        lafGroup.add(addLookAndFeel(frame, laf, "javax.swing.plaf.metal.MetalLookAndFeel", "Metal"));
        lafGroup.add(addLookAndFeel(frame, laf, "com.sun.java.swing.plaf.motif.MotifLookAndFeel", "Motif"));
        lafGroup.add(addLookAndFeel(frame, laf, "javax.swing.plaf.nimbus.NimbusLookAndFeel", "Nimbus"));
        lafGroup.add(addLookAndFeel(frame, laf, "com.sun.java.swing.plaf.windows.WindowsLookAndFeel", "Windows"));
        lafGroup.add(
                addLookAndFeel(frame, laf, "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel", "Windows classic"));
        lafGroup.add(addLookAndFeel(frame, laf, UIManager.getSystemLookAndFeelClassName(), "System default"));

        // Appearance menu
        JMenu app = new AppearanceControlMenu("Appearance");
        app.addMouseListener(new SubMenuShower(app));
        ButtonGroup appGroup = new ButtonGroup();
        for (Appearance appearanceValue : Appearance.values())
        {
            appGroup.add(addAppearance(app, appearanceValue));
        }
        
        // PopupMenu class to only accept the font of an Appearance
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
        }
        
        // Popup menu to change the Look and Feel or Appearance
        JPopupMenu popMenu = new AppearanceControlPopupMenu();
        popMenu.add(laf);
        popMenu.add(app);
        this.getPanel().getOtsControlPanel().setComponentPopupMenu(popMenu);

        // Set the Look and Feel and Appearance as by frame properties
        setAppearance(getAppearance()); // color elements that were just added
        Try.execute(() -> UIManager.setLookAndFeel(this.frameProperties.getProperty("LookAndFeel")),
                "Could not set look-and-feel %s", laf);
        SwingUtilities.invokeLater(() -> SwingUtilities.updateComponentTreeUI(frame));

        return simulator;
    }

    /**
     * Adds a look-and-feel item.
     * @param frame JFrame; frame to set the look-and-feel to
     * @param group JMenu; menu to add item to
     * @param laf String; full path of LookAndFeel
     * @param name String; name on menu item
     * @return JMenuItem; menu item
     */
    private JCheckBoxMenuItem addLookAndFeel(final JFrame frame, final JMenu group, final String laf, final String name)
    {
        boolean checked = this.frameProperties.getProperty("LookAndFeel").equals(laf);
        JCheckBoxMenuItem check = new StayOpenCheckBoxMenuItem(name, checked);
        check.addMouseListener(new MouseAdapter()
        {
            /** {@inheritDoc} */
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                Try.execute(() -> UIManager.setLookAndFeel(laf), "Could not set look-and-feel %s", laf);
                SwingUtilities.updateComponentTreeUI(frame);
                AbstractWrappableAnimation.this.frameProperties.setProperty("LookAndFeel", laf);
            }
        });
        group.add(check);
        return check;
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
            public void mouseClicked(MouseEvent e)
            {
                setAppearance(appear);
            }
        });
        return group.add(check);
    }

    /**
     * Sets an appearance.
     * @param appearance Appearance; appearance
     */
    public void setAppearance(final Appearance appearance)
    {
        this.appearance = appearance;
        setAppearance(this.panel.getParent(), appearance);
        this.frameProperties.setProperty("Appearance", appearance.toString());
    }

    /**
     * Sets an appearance recursively on components.
     * @param c Component; visual component
     * @param appearance Appearance; look and feel
     */
    private void setAppearance(final Component c, final Appearance appearance)
    {
        if (c instanceof AppearanceControl)
        {
            AppearanceControl ac = (AppearanceControl) c;
            if (ac.isBackground())
            {
                c.setBackground(appearance.getBackground());
            }
            if (ac.isForeground())
            {
                c.setForeground(appearance.getForeground());
            }
            if (ac.isFont())
            {
                changeFont(c, appearance.getFont());
            }
        }
        else if (c instanceof AnimationPanel)
        {
            // animation backdrop
            c.setBackground(appearance.getBackdrop()); // not background
            c.setForeground(appearance.getForeground());
            changeFont(c, appearance.getFont());
        }
        else
        {
            // default
            c.setBackground(appearance.getBackground());
            c.setForeground(appearance.getForeground());
            changeFont(c, appearance.getFont());
        }
        if (c instanceof JSlider)
        {
            // labels of the slider
            Dictionary<?, ?> dictionary = ((JSlider) c).getLabelTable();
            Enumeration<?> keys = dictionary.keys();
            while (keys.hasMoreElements())
            {
                JLabel label = (JLabel) dictionary.get(keys.nextElement());
                label.setForeground(appearance.getForeground());
                label.setBackground(appearance.getBackground());
            }
        }
        // children
        if (c instanceof JComponent)
        {
            for (Component child : ((JComponent) c).getComponents())
            {
                setAppearance(child, appearance);
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
     * Overridable method to return GTU colorer.
     * @return GTU colorer
     */
    @SuppressWarnings("checkstyle:designforextension")
    public GTUColorer getColorer()
    {
        return this.colorer;
    }

    /**
     * Make additional tabs in the main simulation window.
     * @param simulator SimpleSimulatorInterface; the simulator
     * @throws OTSSimulationException in case the chart, axes or legend cannot be generated
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    protected void addTabs(final SimpleSimulatorInterface simulator) throws OTSSimulationException, PropertyException
    {
        // Override this method to add custom tabs
    }

    /**
     * Placeholder method to place animation buttons or to show/hide classes on the animation.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void addAnimationToggles()
    {
        // overridable placeholder to place animation buttons or to show/hide classes on the animation.
    }

    /**
     * Add a button for toggling an animatable class on or off. Button icons for which 'idButton' is true will be placed to the
     * right of the previous button, which should be the corresponding button without the id. An example is an icon for
     * showing/hiding the class 'Lane' followed by the button to show/hide the Lane ids.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param iconPath the path to the 24x24 icon to display
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     * @param idButton id button that needs to be placed next to the previous button
     */
    public final void addToggleAnimationButtonIcon(final String name, final Class<? extends Locatable> locatableClass,
            final String iconPath, final String toolTipText, final boolean initiallyVisible, final boolean idButton)
    {
        this.panel.addToggleAnimationButtonIcon(name, locatableClass, iconPath, toolTipText, initiallyVisible, idButton);
    }

    /**
     * Add a button for toggling an animatable class on or off.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     */
    public final void addToggleAnimationButtonText(final String name, final Class<? extends Locatable> locatableClass,
            final String toolTipText, final boolean initiallyVisible)
    {
        this.panel.addToggleAnimationButtonText(name, locatableClass, toolTipText, initiallyVisible);
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param locatableClass the class for which the animation has to be shown.
     */
    public final void showAnimationClass(final Class<? extends Locatable> locatableClass)
    {
        this.panel.getAnimationPanel().showClass(locatableClass);
        this.panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Set a class to be hidden in the animation to true.
     * @param locatableClass the class for which the animation has to be hidden.
     */
    public final void hideAnimationClass(final Class<? extends Locatable> locatableClass)
    {
        this.panel.getAnimationPanel().hideClass(locatableClass);
        this.panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Toggle a class to be displayed in the animation to its reverse value.
     * @param locatableClass the class for which a visible animation has to be turned off or vice versa.
     */
    public final void toggleAnimationClass(final Class<? extends Locatable> locatableClass)
    {
        this.panel.getAnimationPanel().toggleClass(locatableClass);
        this.panel.updateAnimationClassCheckBox(locatableClass);
    }

    /**
     * Add a button for toggling a GIS class on or off.
     * @param header the name of the group of layers
     * @param gisMap the GIS map for which the toggles have to be added
     * @param toolTipText the tool tip text to show when hovering over the button
     */
    public final void addToggleGISButtonText(final String header, final GisRenderable2D gisMap, final String toolTipText)
    {
        this.panel.addToggleText(" ");
        this.panel.addToggleText(header);
        try
        {
            for (String layerName : gisMap.getMap().getLayerMap().keySet())
            {
                this.panel.addToggleGISButtonText(layerName, layerName, gisMap, toolTipText);
            }
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Set a GIS layer to be shown in the animation to true.
     * @param layerName the name of the GIS-layer that has to be shown.
     */
    public final void showGISLayer(final String layerName)
    {
        this.panel.showGISLayer(layerName);
    }

    /**
     * Set a GIS layer to be hidden in the animation to true.
     * @param layerName the name of the GIS-layer that has to be hidden.
     */
    public final void hideGISLayer(final String layerName)
    {
        this.panel.hideGISLayer(layerName);
    }

    /**
     * Toggle a GIS layer to be displayed in the animation to its reverse value.
     * @param layerName the name of the GIS-layer that has to be turned off or vice versa.
     */
    public final void toggleGISLayer(final String layerName)
    {
        this.panel.toggleGISLayer(layerName);
    }

    /**
     * @return the demo model. Don't forget to keep a local copy.
     * @throws OTSSimulationException in case the construction of the model fails
     */
    protected abstract OTSModelInterface makeModel() throws OTSSimulationException;

    /**
     * Return the initial 'home' extent for the animation. The 'Home' button returns to this extent. Override this method when a
     * smaller or larger part of the infra should be shown. In the default setting, all currently visible objects are shown.
     * @return the initial and 'home' rectangle for the animation.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Rectangle2D makeAnimationRectangle()
    {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        Point3d p3dL = new Point3d();
        Point3d p3dU = new Point3d();
        try
        {
            for (Link link : this.model.getNetwork().getLinkMap().values())
            {
                DirectedPoint l = link.getLocation();
                BoundingBox b = new BoundingBox(link.getBounds());
                b.getLower(p3dL);
                b.getUpper(p3dU);
                minX = Math.min(minX, l.x + Math.min(p3dL.x, p3dU.x));
                minY = Math.min(minY, l.y + Math.min(p3dL.y, p3dU.y));
                maxX = Math.max(maxX, l.x + Math.max(p3dL.x, p3dU.x));
                maxY = Math.max(maxY, l.y + Math.max(p3dL.y, p3dU.y));
            }
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // ignore
        }
        double relativeMargin = 0.05;
        double xMargin = relativeMargin * (maxX - minX);
        double yMargin = relativeMargin * (maxY - minY);
        minX = minX - xMargin;
        minY = minY - yMargin;
        maxX = maxX + xMargin;
        maxY = maxY + yMargin;

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /** {@inheritDoc} */
    @Override
    public final ArrayList<Property<?>> getProperties()
    {
        return new ArrayList<>(this.properties);
    }

    /** {@inheritDoc} */
    @Override
    public final SimpleSimulatorInterface rebuildSimulator(final Rectangle rect)
            throws SimRuntimeException, NetworkException, NamingException, OTSSimulationException, PropertyException
    {
        return buildAnimator(this.savedStartTime, this.savedWarmupPeriod, this.savedRunLength, this.savedUserModifiedProperties,
                rect, this.exitOnClose);
    }

    /** {@inheritDoc} */
    @Override
    public final List<Property<?>> getUserModifiedProperties()
    {
        return this.savedUserModifiedProperties;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void stopTimersThreads()
    {
        if (this.panel != null && this.panel.getStatusBar() != null)
        {
            this.panel.getStatusBar().cancelTimer();
        }
        this.panel = null;
    }

    /**
     * @return panel
     */
    public final OTSAnimationPanel getPanel()
    {
        return this.panel;
    }

    /**
     * Add a tab to the simulation window. This method can not be called from constructModel because the TabbedPane has not yet
     * been constructed at that time; recommended: override addTabs and call this method from there.
     * @param index int; index of the new tab; use <code>getTabCount()</code> to obtain the valid range
     * @param caption String; caption of the new tab
     * @param container Container; content of the new tab
     */
    public final void addTab(final int index, final String caption, final Container container)
    {
        this.panel.getTabbedPane().addTab(index, caption, container);
    }

    /**
     * Report the current number of tabs in the simulation window. This method can not be called from constructModel because the
     * TabbedPane has not yet been constructed at that time; recommended: override addTabs and call this method from there.
     * @return int; the number of tabs in the simulation window
     */
    public final int getTabCount()
    {
        return this.panel.getTabbedPane().getTabCount();
    }

    /** {@inheritDoc} */
    @Override
    public final void setNextReplication(final Integer nextReplication)
    {
        this.replication = nextReplication;
    }

    /**
     * Mouse listener which shows the submenu when the mouse enters the button.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$,
     *          initial version 6 feb. 2018 <br>
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
        public SubMenuShower(final JMenu menu)
        {
            this.menu = menu;
        }

        /** {@inheritDoc} */
        @Override
        public void mouseEntered(MouseEvent e)
        {
            MenuSelectionManager.defaultManager().setSelectedPath(
                    new MenuElement[] { (MenuElement) this.menu.getParent(), this.menu, this.menu.getPopupMenu() });
        }
    }

    /**
     * Check box item that keeps the popup menu visible after clicking, so the user can click and try some options.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$,
     *          initial version 6 feb. 2018 <br>
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
                public void stateChanged(ChangeEvent e)
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
        public StayOpenCheckBoxMenuItem(final String text, final boolean selected)
        {
            super(text, selected);
        }

        /** {@inheritDoc} */
        @Override
        public void doClick(int pressTime)
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
                    .setSelectedPath(new MenuElement[] { (MenuElement) menu.getParent(), menu, menu.getPopupMenu() });
        }

        /** {@inheritDoc} */
        @Override
        public boolean isFont()
        {
            return true;
        }
    }

}
