package org.opentrafficsim.swing.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.MaskFormatter;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.base.Resource;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationInterface;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Peter's improved simulation control panel.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OtsControlPanel extends JPanel implements ActionListener, PropertyChangeListener, WindowListener, EventListener
{
    /** */
    private static final long serialVersionUID = 20150617L;

    /** The simulator. */
    private OtsSimulatorInterface simulator;

    /** The model, needed for its properties. */
    private final OtsModelInterface model;

    /** The clock. */
    private final ClockLabel clockPanel;

    /** The time warp control. */
    private final TimeWarpPanel timeWarpPanel;

    /** The control buttons. */
    private final ArrayList<JButton> buttons = new ArrayList<>();

    /** Font used to display the clock and the stop time. */
    private final Font timeFont = new Font("SansSerif", Font.BOLD, 18);

    /** The TimeEdit that lets the user set a time when the simulation will be stopped. */
    private final TimeEdit timeEdit;

    /** The OTS search panel. */
    private final OtsSearchPanel otsSearchPanel;

    /** The currently registered stop at event. */
    private SimEvent<Duration> stopAtEvent = null;

    /** The current enabled state of the buttons. */
    private boolean buttonsEnabled = false;

    /** Has the window close handler been registered? */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean closeHandlerRegistered = false;

    /** Has cleanup taken place? */
    private boolean isCleanUp = false;

    /**
     * Decorate a SimpleSimulator with a different set of control buttons.
     * @param simulator OtsSimulatorInterface; the simulator
     * @param model OtsModelInterface; if non-null, the restart button should work
     * @param otsAnimationPanel OtsAnimationPanel; the OTS animation panel
     * @throws RemoteException when simulator cannot be accessed for listener attachment
     */
    public OtsControlPanel(final OtsSimulatorInterface simulator, final OtsModelInterface model,
            final OtsAnimationPanel otsAnimationPanel) throws RemoteException
    {
        this.simulator = simulator;
        this.model = model;

        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(makeButton("stepButton", "/Last_recor.png", "Step", "Execute one event", true));
        buttonPanel.add(makeButton("nextTimeButton", "/NextTrack.png", "NextTime",
                "Execute all events scheduled for the current time", true));
        buttonPanel.add(makeButton("runPauseButton", "/Play.png", "RunPause", "XXX", true));
        this.timeWarpPanel = new TimeWarpPanel(0.1, 1000, 1, 3, simulator);
        buttonPanel.add(this.timeWarpPanel);
        // buttonPanel.add(makeButton("resetButton", "/Undo.png", "Reset", "Reset the simulation", false));
        /** Label with appearance control. */
        class AppearanceControlLabel extends JLabel implements AppearanceControl
        {
            /** */
            private static final long serialVersionUID = 20180207L;

            /** {@inheritDoc} */
            @Override
            public boolean isForeground()
            {
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isBackground()
            {
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public String toString()
            {
                return "AppearanceControlLabel []";
            }
        }
        JLabel speedLabel = new AppearanceControlLabel();
        this.clockPanel = new ClockLabel(speedLabel);
        this.clockPanel.setMaximumSize(new Dimension(133, 35));
        buttonPanel.add(this.clockPanel);
        speedLabel.setMaximumSize(new Dimension(66, 35));
        buttonPanel.add(speedLabel);
        this.timeEdit = new TimeEdit(new Time(0, TimeUnit.DEFAULT));
        this.timeEdit.setMaximumSize(new Dimension(133, 35));
        this.timeEdit.addPropertyChangeListener("value", this);
        buttonPanel.add(this.timeEdit);
        this.add(buttonPanel);
        this.otsSearchPanel = new OtsSearchPanel(otsAnimationPanel);
        this.add(this.otsSearchPanel, BorderLayout.SOUTH);
        fixButtons();
        installWindowCloseHandler();
        this.simulator.addListener(this, ReplicationInterface.END_REPLICATION_EVENT);
        this.simulator.addListener(this, SimulatorInterface.START_EVENT);
        this.simulator.addListener(this, SimulatorInterface.STOP_EVENT);
        this.simulator.addListener(this, DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT);
    }

    /**
     * Change the enabled/disabled state of the various simulation control buttons.
     * @param newState boolean; true if the buttons should become enabled; false if the buttons should become disabled
     */
    public void setSimulationControlButtons(final boolean newState)
    {
        this.buttonsEnabled = newState;
        fixButtons();
    }

    /**
     * Provide access to the search panel.
     * @return OtsSearchPanel; the OTS search panel
     */
    public OtsSearchPanel getOtsSearchPanel()
    {
        return this.otsSearchPanel;
    }

    /**
     * Create a button.
     * @param name String; name of the button
     * @param iconPath String; path to the resource
     * @param actionCommand String; the action command
     * @param toolTipText String; the hint to show when the mouse hovers over the button
     * @param enabled boolean; true if the new button must initially be enable; false if it must initially be disabled
     * @return JButton
     */
    private JButton makeButton(final String name, final String iconPath, final String actionCommand, final String toolTipText,
            final boolean enabled)
    {
        /** Button with appearance control. */
        class AppearanceControlButton extends JButton implements AppearanceControl
        {
            /** */
            private static final long serialVersionUID = 20180206L;

            /**
             * @param loadIcon Icon; icon
             */
            AppearanceControlButton(final Icon loadIcon)
            {
                super(loadIcon);
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
                return "AppearanceControlButton []";
            }
        }
        JButton result = new AppearanceControlButton(loadIcon(iconPath));
        result.setName(name);
        result.setEnabled(enabled);
        result.setActionCommand(actionCommand);
        result.setToolTipText(toolTipText);
        result.addActionListener(this);
        this.buttons.add(result);
        return result;
    }

    /**
     * Attempt to load and return an icon.
     * @param iconPath String; the path that is used to load the icon
     * @return Icon; or null if loading failed
     */
    public static final Icon loadIcon(final String iconPath)
    {
        try
        {
            return new ImageIcon(ImageIO.read(Resource.getResourceAsStream(iconPath)));
        }
        catch (NullPointerException | IOException npe)
        {
            System.err.println("Could not load icon from path " + iconPath);
            return null;
        }
    }

    /**
     * Attempt to load and return an icon, which will be made gray-scale.
     * @param iconPath String; the path that is used to load the icon
     * @return Icon; or null if loading failed
     */
    public static final Icon loadGrayscaleIcon(final String iconPath)
    {
        try
        {
            return new ImageIcon(GrayFilter.createDisabledImage(ImageIO.read(Resource.getResourceAsStream(iconPath))));
        }
        catch (NullPointerException | IOException e)
        {
            System.err.println("Could not load icon from path " + iconPath);
            return null;
        }
    }

    /**
     * Construct and schedule a SimEvent using a Time to specify the execution time.
     * @param executionTime Time; the time at which the event must happen
     * @param priority short; should be between <cite>SimEventInterface.MAX_PRIORITY</cite> and
     *            <cite>SimEventInterface.MIN_PRIORITY</cite>; most normal events should use
     *            <cite>SimEventInterface.NORMAL_PRIORITY</cite>
     * @param source Object; the object that creates/schedules the event
     * @param eventTarget Object; the object that must execute the event
     * @param method String; the name of the method of <code>target</code> that must execute the event
     * @param args Object[]; the arguments of the <code>method</code> that must execute the event
     * @return SimEvent&lt;Duration&gt;; the event that was scheduled (the caller should save this if a need to cancel the event
     *         may arise later)
     * @throws SimRuntimeException when the <code>executionTime</code> is in the past
     */
    private SimEvent<Duration> scheduleEvent(final Time executionTime, final short priority, final Object source,
            final Object eventTarget, final String method, final Object[] args) throws SimRuntimeException
    {
        SimEvent<Duration> simEvent =
                new SimEvent<>(executionTime.minus(getSimulator().getStartTimeAbs()), priority, eventTarget, method, args);
        this.simulator.scheduleEvent(simEvent);
        return simEvent;
    }

    /**
     * Install a handler for the window closed event that stops the simulator (if it is running).
     */
    public final void installWindowCloseHandler()
    {
        if (this.closeHandlerRegistered)
        {
            return;
        }

        // make sure the root frame gets disposed of when the closing X icon is pressed.
        new DisposeOnCloseThread(this).start();
    }

    /** Install the dispose on close when the OtsControlPanel is registered as part of a frame. */
    protected class DisposeOnCloseThread extends Thread
    {
        /** The current container. */
        private OtsControlPanel panel;

        /**
         * @param panel OtsControlPanel; the OTSControlpanel container.
         */
        public DisposeOnCloseThread(final OtsControlPanel panel)
        {
            this.panel = panel;
        }

        /** {@inheritDoc} */
        @Override
        public final void run()
        {
            Container root = this.panel;
            while (!(root instanceof JFrame))
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException exception)
                {
                    // nothing to do
                }

                // Search towards the root of the Swing components until we find a JFrame
                root = this.panel;
                while (null != root.getParent() && !(root instanceof JFrame))
                {
                    root = root.getParent();
                }
            }
            JFrame frame = (JFrame) root;
            frame.addWindowListener(this.panel);
            this.panel.closeHandlerRegistered = true;
            // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "DisposeOnCloseThread [panel=" + this.panel + "]";
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        // System.out.println("actionCommand: " + actionCommand);
        try
        {
            if (actionCommand.equals("Step"))
            {
                if (getSimulator().isStartingOrRunning())
                {
                    getSimulator().stop();
                }
                this.simulator.step();
            }
            if (actionCommand.equals("RunPause"))
            {
                if (this.simulator.isStartingOrRunning())
                {
                    // System.out.println("RunPause: Stopping simulator");
                    this.simulator.stop();
                }
                else if (getSimulator().getEventList().size() > 0)
                {
                    // System.out.println("RunPause: Starting simulator");
                    this.simulator.start();
                }
            }
            if (actionCommand.equals("NextTime"))
            {
                if (getSimulator().isStartingOrRunning())
                {
                    // System.out.println("NextTime: Stopping simulator");
                    getSimulator().stop();
                }
                double now = getSimulator().getSimulatorTime().getSI();
                // System.out.println("now is " + now);
                try
                {
                    this.stopAtEvent = scheduleEvent(new Time(now, TimeUnit.DEFAULT), SimEventInterface.MIN_PRIORITY, this,
                            this, "autoPauseSimulator", null);
                }
                catch (SimRuntimeException exception)
                {
                    this.simulator.getLogger().always()
                            .error("Caught an exception while trying to schedule an autoPauseSimulator event "
                                    + "at the current simulator time");
                }
                // System.out.println("NextTime: Starting simulator");
                this.simulator.start();
            }
            if (actionCommand.equals("Reset"))
            {
                if (getSimulator().isStartingOrRunning())
                {
                    getSimulator().stop();
                }

                if (null == OtsControlPanel.this.model)
                {
                    throw new RuntimeException("Do not know how to restart this simulation");
                }

                // find the JFrame position and dimensions
                Container root = OtsControlPanel.this;
                while (!(root instanceof JFrame))
                {
                    root = root.getParent();
                }
                JFrame frame = (JFrame) root;
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.dispose();
                OtsControlPanel.this.cleanup();
                // TODO: maybe rebuild model...
            }
            fixButtons();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * clean up timers, contexts, threads, etc. that could prevent garbage collection.
     */
    private void cleanup()
    {
        if (!this.isCleanUp)
        {
            this.isCleanUp = true;
            try
            {
                if (this.simulator != null)
                {
                    if (this.simulator.isStartingOrRunning())
                    {
                        this.simulator.stop();
                    }

                    // unbind the old animation and statistics
                    // TODO: change getExperiment().removeFromContext() so it works properly...
                    // Now: ConcurrentModificationException...
                    if (getSimulator().getReplication().getContext().hasKey("animation"))
                    {
                        getSimulator().getReplication().getContext().destroySubcontext("animation");
                    }
                    if (getSimulator().getReplication().getContext().hasKey("statistics"))
                    {
                        getSimulator().getReplication().getContext().destroySubcontext("statistics");
                    }
                    if (getSimulator().getReplication().getContext().hasKey("statistics"))
                    {
                        getSimulator().getReplication().getContext().destroySubcontext("statistics");
                    }
                    // TODO: this is implemented completely different in latest DSOL versions
                    getSimulator().cleanUp();
                }

                if (this.clockPanel != null)
                {
                    this.clockPanel.cancelTimer(); // cancel the timer on the clock panel.
                }
                // TODO: are there timers or threads we need to stop?
            }
            catch (Throwable exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Update the enabled state of all the buttons.
     */
    protected final void fixButtons()
    {
        // System.out.println("FixButtons entered");
        final boolean moreWorkToDo = getSimulator().getEventList().size() > 0;
        for (JButton button : this.buttons)
        {
            final String actionCommand = button.getActionCommand();
            if (actionCommand.equals("Step"))
            {
                button.setEnabled(moreWorkToDo && this.buttonsEnabled);
            }
            else if (actionCommand.equals("RunPause"))
            {
                button.setEnabled(moreWorkToDo && this.buttonsEnabled);
                if (this.simulator.isStartingOrRunning())
                {
                    button.setToolTipText("Pause the simulation");
                    button.setIcon(OtsControlPanel.loadIcon("/Pause.png"));
                }
                else
                {
                    button.setToolTipText("Run the simulation at the indicated speed");
                    button.setIcon(loadIcon("/Play.png"));
                }
                button.setEnabled(moreWorkToDo && this.buttonsEnabled);
            }
            else if (actionCommand.equals("NextTime"))
            {
                button.setEnabled(moreWorkToDo && this.buttonsEnabled);
            }
            // else if (actionCommand.equals("Reset"))
            // {
            // button.setEnabled(true); // FIXME: should be disabled when the simulator was just reset or initialized
            // }
            else
            {
                this.simulator.getLogger().always().error(new Exception("Unknown button?"));
            }
        }
        // System.out.println("FixButtons finishing");
    }

    /**
     * Pause the simulator.
     */
    public final void autoPauseSimulator()
    {
        // System.out.println("OtsControlPanel.autoPauseSimulator entered");
        if (getSimulator().isStartingOrRunning())
        {
            try
            {
                // System.out.println("AutoPauseSimulator: stopping simulator");
                getSimulator().stop();
            }
            catch (SimRuntimeException exception1)
            {
                exception1.printStackTrace();
            }
            double currentTick = getSimulator().getSimulatorTime().getSI();
            double nextTick = getSimulator().getEventList().first().getAbsoluteExecutionTime().getSI();
            // System.out.println("currentTick is " + currentTick);
            // System.out.println("nextTick is " + nextTick);
            if (nextTick > currentTick)
            {
                // The clock is now just beyond where it was when the user requested the NextTime operation
                // Insert another autoPauseSimulator event just before what is now the time of the next event
                // and let the simulator time increment to that time
                // System.out.println("Re-Scheduling at " + nextTick);
                try
                {
                    this.stopAtEvent = scheduleEvent(new Time(nextTick, TimeUnit.DEFAULT), SimEventInterface.MAX_PRIORITY, this,
                            this, "autoPauseSimulator", null);
                    // System.out.println("AutoPauseSimulator: starting simulator");
                    getSimulator().start();
                }
                catch (SimRuntimeException exception)
                {
                    this.simulator.getLogger().always()
                            .error("Caught an exception while trying to re-schedule an autoPauseEvent at the next real event");
                }
            }
            else
            {
                // System.out.println("Not re-scheduling");
                if (SwingUtilities.isEventDispatchThread())
                {
                    // System.out.println("Already on EventDispatchThread");
                    fixButtons();
                }
                else
                {
                    try
                    {
                        // System.out.println("Current thread is NOT EventDispatchThread: " + Thread.currentThread());
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // System.out.println("Runnable started");
                                fixButtons();
                                // System.out.println("Runnable finishing");
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        if (e instanceof InterruptedException)
                        {
                            System.out.println("Caught " + e);
                            // e.printStackTrace();
                        }
                        else
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        // System.out.println("OtsControlPanel.autoPauseSimulator finished");
    }

    /** {@inheritDoc} */
    @Override
    public final void propertyChange(final PropertyChangeEvent evt)
    {
        // System.out.println("PropertyChanged: " + evt);
        if (null != this.stopAtEvent)
        {
            getSimulator().cancelEvent(this.stopAtEvent); // silently ignore false result
            this.stopAtEvent = null;
        }
        String newValue = (String) evt.getNewValue();
        String[] fields = newValue.split("[:\\.]");
        int hours = Integer.parseInt(fields[0]);
        int minutes = Integer.parseInt(fields[1]);
        int seconds = Integer.parseInt(fields[2]);
        int fraction = Integer.parseInt(fields[3]);
        double stopTime = hours * 3600 + minutes * 60 + seconds + fraction / 1000d;
        if (stopTime < getSimulator().getSimulatorTime().getSI())
        {
            return;
        }
        else
        {
            try
            {
                this.stopAtEvent = scheduleEvent(new Time(stopTime, TimeUnit.DEFAULT), SimEventInterface.MAX_PRIORITY, this,
                        this, "autoPauseSimulator", null);
            }
            catch (SimRuntimeException exception)
            {
                this.simulator.getLogger().always()
                        .error("Caught an exception while trying to schedule an autoPauseSimulator event");
            }
        }
    }

    /**
     * @return simulator.
     */
    @SuppressWarnings("unchecked")
    public final OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public void windowOpened(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowClosing(final WindowEvent e)
    {
        if (this.simulator != null)
        {
            try
            {
                if (this.simulator.isStartingOrRunning())
                {
                    this.simulator.stop();
                }
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void windowClosed(final WindowEvent e)
    {
        cleanup();
    }

    /** {@inheritDoc} */
    @Override
    public final void windowIconified(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowDeiconified(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowActivated(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowDeactivated(final WindowEvent e)
    {
        // No action
    }

    /**
     * @return timeFont.
     */
    public final Font getTimeFont()
    {
        return this.timeFont;
    }

    /** JPanel that contains a JSider that uses a logarithmic scale. */
    static class TimeWarpPanel extends JPanel
    {
        /** */
        private static final long serialVersionUID = 20150408L;

        /** The JSlider that the user sees. */
        private final JSlider slider;

        /** The ratios used in each decade. */
        private final int[] ratios;

        /** The values at each tick. */
        private Map<Integer, Double> tickValues = new LinkedHashMap<>();

        /**
         * Construct a new TimeWarpPanel.
         * @param minimum double; the minimum value on the scale (the displayed scale may extend a little further than this
         *            value)
         * @param maximum double; the maximum value on the scale (the displayed scale may extend a little further than this
         *            value)
         * @param initialValue double; the initially selected value on the scale
         * @param ticksPerDecade int; the number of steps per decade
         * @param simulator DevsSimulatorInterface&lt;?, ?, ?&gt;; the simulator to change the speed of
         */
        TimeWarpPanel(final double minimum, final double maximum, final double initialValue, final int ticksPerDecade,
                final OtsSimulatorInterface simulator)
        {
            if (minimum <= 0 || minimum > initialValue || initialValue > maximum)
            {
                throw new RuntimeException("Bad (combination of) minimum, maximum and initialValue; "
                        + "(restrictions: 0 < minimum <= initialValue <= maximum)");
            }
            switch (ticksPerDecade)
            {
                case 1:
                    this.ratios = new int[] {1};
                    break;
                case 2:
                    this.ratios = new int[] {1, 3};
                    break;
                case 3:
                    this.ratios = new int[] {1, 2, 5};
                    break;
                default:
                    throw new RuntimeException("Bad ticksPerDecade value (must be 1, 2 or 3)");
            }
            int minimumTick = (int) Math.floor(Math.log10(minimum / initialValue) * ticksPerDecade);
            int maximumTick = (int) Math.ceil(Math.log10(maximum / initialValue) * ticksPerDecade);
            this.slider = new JSlider(SwingConstants.HORIZONTAL, minimumTick, maximumTick + 1, 0);
            this.slider.setPreferredSize(new Dimension(350, 45));
            Hashtable<Integer, JLabel> labels = new Hashtable<>();
            for (int step = 0; step <= maximumTick; step++)
            {
                StringBuilder text = new StringBuilder();
                text.append(this.ratios[step % this.ratios.length]);
                for (int decade = 0; decade < step / this.ratios.length; decade++)
                {
                    text.append("0");
                }
                this.tickValues.put(step, Double.parseDouble(text.toString()));
                labels.put(step, new JLabel(text.toString().replace("000", "K")));
                // System.out.println("Label " + step + " is \"" + text.toString() + "\"");
            }
            // Figure out the DecimalSymbol
            String decimalSeparator =
                    "" + ((DecimalFormat) NumberFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();
            for (int step = -1; step >= minimumTick; step--)
            {
                StringBuilder text = new StringBuilder();
                text.append("0");
                text.append(decimalSeparator);
                for (int decade = (step + 1) / this.ratios.length; decade < 0; decade++)
                {
                    text.append("0");
                }
                int index = step % this.ratios.length;
                if (index < 0)
                {
                    index += this.ratios.length;
                }
                text.append(this.ratios[index]);
                labels.put(step, new JLabel(text.toString()));
                this.tickValues.put(step, Double.parseDouble(text.toString()));
                // System.out.println("Label " + step + " is \"" + text.toString() + "\"");
            }
            labels.put(maximumTick + 1, new JLabel("\u221E"));
            this.tickValues.put(maximumTick + 1, 1E9);
            this.slider.setLabelTable(labels);
            this.slider.setPaintLabels(true);
            this.slider.setPaintTicks(true);
            this.slider.setMajorTickSpacing(1);
            this.add(this.slider);
            /*- Uncomment to verify the stepToFactor method.
            for (int i = this.slider.getMinimum(); i <= this.slider.getMaximum(); i++)
            {
                System.out.println("pos=" + i + " value is " + stepToFactor(i));
            }
             */

            // initial value of simulation speed
            if (simulator instanceof DevsRealTimeAnimator)
            {
                DevsRealTimeAnimator<Duration> clock = (DevsRealTimeAnimator<Duration>) simulator;
                clock.setSpeedFactor(TimeWarpPanel.this.tickValues.get(this.slider.getValue()));
            }

            // adjust the simulation speed
            this.slider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(final ChangeEvent ce)
                {
                    JSlider source = (JSlider) ce.getSource();
                    if (!source.getValueIsAdjusting() && simulator instanceof DevsRealTimeAnimator)
                    {
                        DevsRealTimeAnimator<Duration> clock = (DevsRealTimeAnimator<Duration>) simulator;
                        clock.setSpeedFactor(((TimeWarpPanel) source.getParent()).getTickValues().get(source.getValue()));
                    }
                }
            });
        }

        /**
         * Access to tickValues map from within the event handler.
         * @return Map&lt;Integer, Double&gt; the tickValues map of this TimeWarpPanel
         */
        protected Map<Integer, Double> getTickValues()
        {
            return this.tickValues;
        }

        /**
         * Convert a position on the slider to a factor.
         * @param step int; the position on the slider
         * @return double; the factor that corresponds to step
         */
        private double stepToFactor(final int step)
        {
            int index = step % this.ratios.length;
            if (index < 0)
            {
                index += this.ratios.length;
            }
            double result = this.ratios[index];
            // Make positive to avoid trouble with negative values that round towards 0 on division
            int power = (step + 1000 * this.ratios.length) / this.ratios.length - 1000; // This is ugly
            while (power > 0)
            {
                result *= 10;
                power--;
            }
            while (power < 0)
            {
                result /= 10;
                power++;
            }
            return result;
        }

        /**
         * Retrieve the current TimeWarp factor.
         * @return double; the current TimeWarp factor
         */
        public final double getFactor()
        {
            return stepToFactor(this.slider.getValue());
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TimeWarpPanel [timeWarp=" + this.getFactor() + "]";
        }

        /**
         * Set the time warp factor to the best possible approximation of a given value.
         * @param factor double; the requested speed factor
         */
        public void setSpeedFactor(final double factor)
        {
            int bestStep = -1;
            double bestError = Double.MAX_VALUE;
            double logOfFactor = Math.log(factor);
            for (int step = this.slider.getMinimum(); step <= this.slider.getMaximum(); step++)
            {
                double ratio = getTickValues().get(step); // stepToFactor(step);
                double logError = Math.abs(logOfFactor - Math.log(ratio));
                if (logError < bestError)
                {
                    bestStep = step;
                    bestError = logError;
                }
            }
            // System.out.println("setSpeedfactor: factor is " + factor + ", best slider value is " + bestStep
            // + " current value is " + this.slider.getValue());
            if (this.slider.getValue() != bestStep)
            {
                this.slider.setValue(bestStep);
            }
        }
    }

    /** JLabel that displays the simulation time. */
    public class ClockLabel extends JLabel implements AppearanceControl
    {
        /** */
        private static final long serialVersionUID = 20141211L;

        /** The JLabel that displays the time. */
        private final JLabel speedLabel;

        /** The timer (so we can cancel it). */
        private Timer timer;

        /** Timer update interval in msec. */
        private static final long UPDATEINTERVAL = 1000;

        /** Simulation time time. */
        private double prevSimTime = 0;

        /**
         * Construct a clock panel.
         * @param speedLabel JLabel; speed label
         */
        ClockLabel(final JLabel speedLabel)
        {
            super("00:00:00.000");
            this.speedLabel = speedLabel;
            speedLabel.setFont(getTimeFont());
            this.setFont(getTimeFont());
            this.timer = new Timer();
            this.timer.scheduleAtFixedRate(new TimeUpdateTask(), 0, ClockLabel.UPDATEINTERVAL);
        }

        /**
         * Cancel the timer task.
         */
        public void cancelTimer()
        {
            if (this.timer != null)
            {
                this.timer.cancel();
            }
            this.timer = null;
        }

        /** Updater for the clock panel. */
        private class TimeUpdateTask extends TimerTask implements Serializable
        {
            /** */
            private static final long serialVersionUID = 20140000L;

            /**
             * Create a TimeUpdateTask.
             */
            TimeUpdateTask()
            {
            }

            /** {@inheritDoc} */
            @Override
            public void run()
            {
                double now = Math.round(getSimulator().getSimulatorTime().getSI() * 1000) / 1000d;
                int seconds = (int) Math.floor(now);
                int fractionalSeconds = (int) Math.floor(1000 * (now - seconds));
                ClockLabel.this.setText(String.format("  %02d:%02d:%02d.%03d  ", seconds / 3600, seconds / 60 % 60,
                        seconds % 60, fractionalSeconds));
                ClockLabel.this.repaint();
                double speed = getSpeed(now);
                if (Double.isNaN(speed))
                {
                    getSpeedLabel().setText("");
                }
                else
                {
                    getSpeedLabel().setText(String.format("% 5.2fx  ", speed));
                }
                getSpeedLabel().repaint();
            }

            /** {@inheritDoc} */
            @Override
            public final String toString()
            {
                return "TimeUpdateTask of ClockPanel";
            }
        }

        /**
         * @return speedLabel.
         */
        protected JLabel getSpeedLabel()
        {
            return this.speedLabel;
        }

        /**
         * Returns the simulation speed.
         * @param t double; simulation time
         * @return simulation speed
         */
        protected double getSpeed(final double t)
        {
            double speed = (t - this.prevSimTime) / (0.001 * UPDATEINTERVAL);
            this.prevSimTime = t;
            return speed;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isForeground()
        {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ClockPanel";
        }

    }

    /** Entry field for time. */
    public class TimeEdit extends JFormattedTextField implements AppearanceControl
    {
        /** */
        private static final long serialVersionUID = 20141212L;

        /**
         * Construct a new TimeEdit.
         * @param initialValue Time; the initial value for the TimeEdit
         */
        TimeEdit(final Time initialValue)
        {
            super(new RegexFormatter("\\d\\d\\d\\d:[0-5]\\d:[0-5]\\d\\.\\d\\d\\d"));
            MaskFormatter mf = null;
            try
            {
                mf = new MaskFormatter("####:##:##.###");
                mf.setPlaceholderCharacter('0');
                mf.setAllowsInvalid(false);
                mf.setCommitsOnValidEdit(true);
                mf.setOverwriteMode(true);
                mf.install(this);
            }
            catch (ParseException exception)
            {
                exception.printStackTrace();
            }
            setTime(initialValue);
            setFont(getTimeFont());
        }

        /**
         * Set or update the time shown in this TimeEdit.
         * @param newValue Time; the (new) value to set/show in this TimeEdit
         */
        public void setTime(final Time newValue)
        {
            double v = newValue.getSI();
            int integerPart = (int) Math.floor(v);
            int fraction = (int) Math.floor((v - integerPart) * 1000);
            String text =
                    String.format("%04d:%02d:%02d.%03d", integerPart / 3600, integerPart / 60 % 60, integerPart % 60, fraction);
            this.setText(text);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TimeEdit [time=" + getText() + "]";
        }
    }

    /**
     * Extension of a DefaultFormatter that uses a regular expression. <br>
     * Derived from <a href="http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm">
     * http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    static class RegexFormatter extends DefaultFormatter
    {
        /** */
        private static final long serialVersionUID = 20141212L;

        /** The regular expression pattern. */
        private Pattern pattern;

        /**
         * Create a new RegexFormatter.
         * @param pattern String; regular expression pattern that defines what this RexexFormatter will accept
         */
        RegexFormatter(final String pattern)
        {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public Object stringToValue(final String text) throws ParseException
        {
            Matcher matcher = this.pattern.matcher(text);
            if (matcher.matches())
            {
                // System.out.println("String \"" + text + "\" matches");
                return super.stringToValue(text);
            }
            // System.out.println("String \"" + text + "\" does not match");
            throw new ParseException("Pattern did not match", 0);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RegexFormatter [pattern=" + this.pattern + "]";
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(ReplicationInterface.END_REPLICATION_EVENT)
                || event.getType().equals(SimulatorInterface.START_EVENT)
                || event.getType().equals(SimulatorInterface.STOP_EVENT)
                || event.getType().equals(DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT))
        {
            // System.out.println("OtsControlPanel receive event " + event);
            if (event.getType().equals(DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT))
            {
                this.timeWarpPanel.setSpeedFactor((Double) event.getContent());
            }
            fixButtons();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OtsControlPanel [simulatorTime=" + this.simulator.getSimulatorTime() + ", timeWarp="
                + this.timeWarpPanel.getFactor() + ", stopAtEvent=" + this.stopAtEvent + "]";
    }

}
