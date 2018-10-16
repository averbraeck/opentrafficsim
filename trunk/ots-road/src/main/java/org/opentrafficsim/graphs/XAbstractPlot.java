package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.gui.JFileChooserWithSettings;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.event.EventType;

/**
 * Super class of all plots. This schedules regular updates, creates menus and deals with listeners. There are a number of
 * delegate methods for sub classes to implement.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class XAbstractPlot extends JFrame implements Identifiable, Dataset
{

    /** */
    private static final long serialVersionUID = 20181004L;

    /**
     * The (regular, not timed) event type for pub/sub indicating the addition of a graph. Not used internally.<br>
     * Payload: String graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_ADD_EVENT = new EventType("GRAPH.ADD");

    /**
     * The (regular, not timed) event type for pub/sub indicating the removal of a graph. Not used internally.<br>
     * Payload: String Graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_REMOVE_EVENT = new EventType("GRAPH.REMOVE");

    /** Initial upper bound for the time scale. */
    public static final Time DEFAULT_INITIAL_UPPER_TIME_BOUND = Time.createSI(300.0);

    /** Caption. */
    private final String caption;

    /** Update interval. */
    private Duration updateInterval;

    /** Delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories. */
    private final Duration delay;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Time of last data update. */
    private Time updateTime;

    /** Number of updates. */
    private int updates = 0;

    /** Unique ID of the chart. */
    private final String id = UUID.randomUUID().toString();

    /** The chart, so we can export it. */
    private JFreeChart chart;

    /** Status label. */
    private JLabel statusLabel;

    /** Detach menu item. */
    private JMenuItem detach;

    /** List of parties interested in changes of this plot. */
    private Set<DatasetChangeListener> listeners = new LinkedHashSet<>();

    /** Event of next update. */
    private SimEventInterface<SimTimeDoubleUnit> updateEvent;

    /**
     * Constructor.
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param simulator OTSSimulatorInterface; simulator
     * @param delay Duration; delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories
     */
    public XAbstractPlot(final String caption, final Duration updateInterval, final OTSSimulatorInterface simulator,
            final Duration delay)
    {
        this.caption = caption;
        this.updateInterval = updateInterval;
        this.simulator = simulator;
        this.delay = delay;
        update(); // start redraw chain
    }

    /**
     * Sets the chart and adds menus and listeners.
     * @param chart JFreeChart; chart
     */
    @SuppressWarnings("methodlength")
    protected void setChart(final JFreeChart chart)
    {
        this.chart = chart;

        // make title somewhat smaller
        chart.setTitle(new TextTitle(chart.getTitle().getText(), new Font("SansSerif", java.awt.Font.BOLD, 16)));

        // default colors and zoom behavior
        chart.getPlot().setBackgroundPaint(Color.LIGHT_GRAY);
        if (chart.getPlot() instanceof XYPlot)
        {
            XYPlot xyPlot = chart.getXYPlot();
            xyPlot.setDomainGridlinePaint(Color.WHITE);
            xyPlot.setRangeGridlinePaint(Color.WHITE);
        }
        chart.setBackgroundPaint(Color.WHITE);

        // status label
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        add(this.statusLabel, BorderLayout.SOUTH);

        // override to gain some control over the auto bounds
        ChartPanel chartPanel = new ChartPanel(chart)
        {
            /** */
            private static final long serialVersionUID = 20181006L;

            /** {@inheritDoc} */
            @Override
            public void restoreAutoDomainBounds()
            {
                super.restoreAutoDomainBounds();
                applyAutoBoundDomain(chart.getPlot());
            }

            /** {@inheritDoc} */
            @Override
            public void restoreAutoRangeBounds()
            {
                super.restoreAutoRangeBounds();
                applyAutoBoundRange(chart.getPlot());
            }

            /** {@inheritDoc} This implementation adds control over the PNG image size and font size. */
            @Override
            public void doSaveAs() throws IOException
            {
                // the code in this method is based on the code in the super implementation

                // create setting components
                JLabel fontSizeLabel = new JLabel("font size");
                JTextField fontSize = new JTextField("16");
                fontSize.setToolTipText("Font size of title (other fonts are scaled)");
                fontSize.setPreferredSize(new Dimension(40, 20));
                JTextField width = new JTextField("960");
                width.setToolTipText("Width [pixels]");
                width.setPreferredSize(new Dimension(40, 20));
                JLabel x = new JLabel("x");
                JTextField height = new JTextField("540");
                height.setToolTipText("Height [pixels]");
                height.setPreferredSize(new Dimension(40, 20));

                // create file chooser with these components
                JFileChooser fileChooser = new JFileChooserWithSettings(fontSizeLabel, fontSize, width, x, height);
                fileChooser.setCurrentDirectory(getDefaultDirectoryForSaveAs());
                FileNameExtensionFilter filter =
                        new FileNameExtensionFilter(localizationResources.getString("PNG_Image_Files"), "png");
                fileChooser.addChoosableFileFilter(filter);
                fileChooser.setFileFilter(filter);

                int option = fileChooser.showSaveDialog(this);
                if (option == JFileChooser.APPROVE_OPTION)
                {
                    String filename = fileChooser.getSelectedFile().getPath();
                    if (isEnforceFileExtensions())
                    {
                        if (!filename.endsWith(".png"))
                        {
                            filename = filename + ".png";
                        }
                    }

                    // get settings from setting components
                    double fs; // relative scale
                    try
                    {
                        fs = Double.parseDouble(fontSize.getText()) / 16.0;
                    }
                    catch (@SuppressWarnings("unused") NumberFormatException exception)
                    {
                        fs = 1.0;
                    }
                    int w;
                    try
                    {
                        w = Integer.parseInt(width.getText());
                    }
                    catch (@SuppressWarnings("unused") NumberFormatException exception)
                    {
                        w = getWidth();
                    }
                    int h;
                    try
                    {
                        h = Integer.parseInt(height.getText());
                    }
                    catch (@SuppressWarnings("unused") NumberFormatException exception)
                    {
                        h = getHeight();
                    }
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filename)));
                    // to double the font size, we halve the base dimensions
                    // JFreeChart will the assign more area (relatively) to the fixed actual font size
                    double baseWidth = w / fs;
                    double baseHeight = h / fs;
                    // this code is from ChartUtils.writeScaledChartAsPNG
                    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = image.createGraphics();
                    // to compensate for the base dimensions which are not w x h, we scale the drawing
                    AffineTransform saved = g2.getTransform();
                    g2.transform(AffineTransform.getScaleInstance(w / baseWidth, h / baseHeight));
                    chart.draw(g2, new Rectangle2D.Double(0, 0, baseWidth, baseHeight), null, null);
                    g2.setTransform(saved);
                    g2.dispose();
                    out.write(ChartUtils.encodeAsPNG(image));
                    out.close();
                }
            }
        };
        ChartMouseListener chartListener = getChartMouseListener();
        if (chartListener != null)
        {
            chartPanel.addChartMouseListener(chartListener);
        }

        // pointer handler
        final PointerHandler ph = new PointerHandler()
        {
            /** {@inheritDoc} */
            @Override
            void updateHint(final double domainValue, final double rangeValue)
            {
                if (Double.isNaN(domainValue))
                {
                    setStatusLabel(" ");
                }
                else
                {
                    setStatusLabel(getStatusLabel(domainValue, rangeValue));
                }
            }
        };
        chartPanel.addMouseMotionListener(ph);
        chartPanel.addMouseListener(ph);
        add(chartPanel, BorderLayout.CENTER);
        chartPanel.setMouseWheelEnabled(true);

        // pop up
        JPopupMenu popupMenu = chartPanel.getPopupMenu();
        popupMenu.add(new JPopupMenu.Separator());
        this.detach = new JMenuItem("Show in detached window");
        this.detach.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                XAbstractPlot.this.detach.setEnabled(false);
                JFrame window = new JFrame(XAbstractPlot.this.caption);
                window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                window.add(chartPanel, BorderLayout.CENTER);
                window.add(XAbstractPlot.this.statusLabel, BorderLayout.SOUTH);
                window.addWindowListener(new WindowAdapter()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void windowClosing(@SuppressWarnings("hiding") final WindowEvent e)
                    {
                        add(chartPanel, BorderLayout.CENTER);
                        add(XAbstractPlot.this.statusLabel, BorderLayout.SOUTH);
                        XAbstractPlot.this.detach.setEnabled(true);
                    }
                });
                window.pack();
                window.setVisible(true);
            }
        });
        popupMenu.add(this.detach);
        addPopUpMenuItems(popupMenu);
    }

    /** {@inheritDoc} */
    @Override
    public final DatasetGroup getGroup()
    {
        return null; // not used
    }

    /** {@inheritDoc} */
    @Override
    public final void setGroup(final DatasetGroup group)
    {
        // not used
    }

    /**
     * Overridable method to add pop up items.
     * @param popupMenu JPopupMenu; pop up menu
     */
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        //
    }

    /**
     * Overridable; activates auto bounds on domain axis from user input.
     * @param plot Plot; plot
     */
    protected void applyAutoBoundDomain(final Plot plot)
    {
        //
    }

    /**
     * Overridable; activates auto bounds on range axis from user input.
     * @param plot Plot; plot
     */
    protected void applyAutoBoundRange(final Plot plot)
    {
        //
    }

    /**
     * Overridable; may return a chart listener.
     * @return ChartMouseListener, {@code null} by default
     */
    protected ChartMouseListener getChartMouseListener()
    {
        return null;
    }

    /**
     * Return the graph type for transceiver.
     * @return GraphType; the graph type.
     */
    public abstract GraphType getGraphType();

    /**
     * Returns the status label when the mouse is over the given location.
     * @param domainValue double; domain value (x-axis)
     * @param rangeValue double; range value (y-axis)
     * @return String; status label when the mouse is over the given location
     */
    protected abstract String getStatusLabel(double domainValue, double rangeValue);

    /**
     * Increase the time span.
     * @param time Time; time to increase to
     */
    protected abstract void increaseTime(Time time);

    /**
     * Redraws the plot and schedules the next update.
     */
    protected void update()
    {
        this.updateTime = this.simulator.getSimulatorTime();
        increaseTime(this.updateTime.minus(this.delay));
        notifyPlotChange();
        scheduleNextUpdateEvent();
    }

    /**
     * Schedules the next update event.
     */
    private void scheduleNextUpdateEvent()
    {
        try
        {
            this.updates++;
            // events are scheduled slightly later, so all influencing movements have occurred
            this.updateEvent = this.simulator.scheduleEventAbs(
                    Time.createSI(this.updateInterval.si * this.updates + this.delay.si), this, this, "update", null);
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Unexpected exception while updating plot.", exception);
        }
    }

    /**
     * Notify all change listeners.
     */
    public final void notifyPlotChange()
    {
        DatasetChangeEvent event = new DatasetChangeEvent(this, this);
        for (DatasetChangeListener dcl : this.listeners)
        {
            dcl.datasetChanged(event);
        }
    }

    /**
     * Sets a new update interval.
     * @param interval Duration; update interval
     */
    protected final void setUpdateInterval(final Duration interval)
    {
        if (this.updateEvent != null)
        {
            this.simulator.cancelEvent(this.updateEvent);
        }
        this.updates = (int) (this.simulator.getSimulatorTime().si / interval.si);
        this.updateInterval = interval;
        scheduleNextUpdateEvent();
    }

    /**
     * Returns time until which data should be plotted.
     * @return Time; time until which data should be plotted
     */
    protected final Time getUpdateTime()
    {
        return this.updateTime;
    }

    /**
     * Returns the chart.
     * @return JFreeChart; chart
     */
    protected final JFreeChart getChart()
    {
        return this.chart;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final void addChangeListener(final DatasetChangeListener listener)
    {
        this.listeners.add(listener);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Manually set status label from sub class. Will be overwritten by a moving mouse pointer over the axes.
     * @param label String; label to set
     */
    protected final void setStatusLabel(final String label)
    {
        if (this.statusLabel != null)
        {
            this.statusLabel.setText(label);
        }
    }

    /**
     * Return the caption of this graph.
     * @return String; the caption of this graph
     */
    public final String getCaption()
    {
        return this.caption;
    }

}
