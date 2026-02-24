package org.opentrafficsim.swing.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.animation.IconUtil;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.Executable;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.DevsRealTimeAnimator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Peter's and Wouter's improved simulation control panel.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsSimulationControlPanel extends JPanel implements ActionListener, PropertyChangeListener, EventListener
{

    /** */
    private static final long serialVersionUID = 20150617L;

    /** Pause icon. */
    private static final Icon PAUSE_ICON = IconUtil.of("Pause24.png").get();

    /** Play icon. */
    private static final Icon PLAY_ICON = IconUtil.of("Play24.png").get();

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Control buttons. */
    private final ArrayList<JButton> buttons = new ArrayList<>();

    /** Decimal separator. */
    private final String decimalSeparator;

    /** Slider for simulation speed. */
    private final JSlider speedSlider;

    /** Ratios used in each decade of the speed slider. */
    private final int[] simulationSpeedRatios;

    /** Tick values of the speed slider. */
    private final Map<Integer, Double> tickValues = new LinkedHashMap<>();

    /** Font used to display the clock and the stop time. */
    private final Font timeFont = new Font("SansSerif", Font.BOLD, 18);

    /** Clock label. */
    private final ClockLabel clockLabel;

    /** Simulate until edit field. */
    private final TimeEdit timeEdit;

    /** Current stop event to simulate until. */
    private SimEventInterface<Duration> stopAtEvent = null;

    /** Overruled buttons enabled state (for when simulation is controlled otherwise). */
    private boolean buttonsEnabled = false;

    /** Has cleanup taken place? */
    private boolean isCleanUp = false;

    /**
     * Decorate a SimpleSimulator with a different set of control buttons.
     * @param simulator the simulator
     * @param otsAnimationPanel the OTS animation panel
     * @throws RemoteException when simulator cannot be accessed for listener attachment
     */
    public OtsSimulationControlPanel(final OtsSimulatorInterface simulator, final OtsSimulationPanel otsAnimationPanel)
            throws RemoteException
    {
        this.simulator = simulator;
        this.decimalSeparator =
                "" + ((DecimalFormat) NumberFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // buttons
        add(makeButton("stepButton", "Next24.png", "Step", "Execute one event", true));
        add(makeButton("nextTimeButton", "Step24.png", "NextTime", "Execute all events scheduled for the current time", true));
        add(makeButton("runPauseButton", "Play24.png", "RunPause", "XXX", true));
        add(Box.createHorizontalStrut(5));

        // simulation speed slider
        this.simulationSpeedRatios = new int[] {1, 2, 5};
        this.speedSlider = setupSlider(0.1, 1000, 1, simulator);
        add(this.speedSlider);
        add(Box.createHorizontalStrut(5));

        /** Label with appearance control. */
        class AppearanceControlLabel extends JLabel implements AppearanceControl
        {
            /** */
            private static final long serialVersionUID = 20180207L;

            @Override
            public boolean isForeground()
            {
                return true;
            }

            @Override
            public boolean isBackground()
            {
                return true;
            }

            @Override
            public OptionalInt getFontSize()
            {
                return OptionalInt.empty();
            }

            @Override
            public String toString()
            {
                return "AppearanceControlLabel []";
            }
        }

        // clock/timeEdit (exchanged) and speed label
        JLabel speedLabel = new AppearanceControlLabel();
        speedLabel.setMinimumSize(new Dimension(85, 25));
        speedLabel.setPreferredSize(new Dimension(85, 25));
        speedLabel.setMaximumSize(new Dimension(85, 25));
        this.clockLabel = new ClockLabel(speedLabel);
        this.clockLabel.setMinimumSize(new Dimension(130, 25));
        this.clockLabel.setPreferredSize(new Dimension(130, 25));
        this.clockLabel.setMaximumSize(new Dimension(130, 25));
        this.timeEdit = new TimeEdit(new Time(0, TimeUnit.DEFAULT));
        this.timeEdit.setMinimumSize(new Dimension(130, 25));
        this.timeEdit.setPreferredSize(new Dimension(130, 25));
        this.timeEdit.setMaximumSize(new Dimension(130, 25));
        this.timeEdit.addPropertyChangeListener("value", this); // adds event at 'simulate until' time
        add(this.clockLabel);
        add(this.timeEdit);
        add(speedLabel);

        setButtonsEnabledState();
        prepareCleanup();
        this.simulator.addListener(this, Replication.END_REPLICATION_EVENT);
        this.simulator.addListener(this, SimulatorInterface.START_EVENT);
        this.simulator.addListener(this, SimulatorInterface.STOP_EVENT);
        this.simulator.addListener(this, DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT);
    }

    /**
     * Create a button.
     * @param name name of the button
     * @param iconFile name of the icon file
     * @param actionCommand the action command
     * @param toolTipText the hint to show when the mouse hovers over the button
     * @param enabled true if the new button must initially be enable; false if it must initially be disabled
     * @return created button
     */
    private JButton makeButton(final String name, final String iconFile, final String actionCommand, final String toolTipText,
            final boolean enabled)
    {
        JButton result = new AppearanceControlButton(IconUtil.of(iconFile).get());
        result.setName(name);
        result.setEnabled(enabled);
        result.setActionCommand(actionCommand);
        result.setToolTipText(toolTipText);
        result.addActionListener(this);
        Dimension dimension = new Dimension(50, 30);
        result.setMinimumSize(dimension);
        result.setPreferredSize(dimension);
        result.setMaximumSize(dimension);
        this.buttons.add(result);
        return result;
    }

    /**
     * Setup the slider.
     * @param minimum the minimum value on the scale (the displayed scale may extend a little further than this value)
     * @param maximum the maximum value on the scale (the displayed scale may extend a little further than this value)
     * @param initialValue the initially selected value on the scale
     * @param sim the simulator to change the speed of
     * @return slider
     */
    private JSlider setupSlider(final double minimum, final double maximum, final double initialValue,
            final OtsSimulatorInterface sim)
    {
        Throw.when(minimum <= 0 || minimum > initialValue || initialValue > maximum || maximum > 9999,
                OtsRuntimeException.class, "Bad (combination of) minimum, maximum and initialValue; "
                        + "(restrictions: 0 < minimum <= initialValue <= maximum <= 9999)");

        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        int maximumTick = -1;
        int minimumTick = 0;
        int ratioIndex = 0;
        int scale = 0;
        while (this.simulationSpeedRatios[ratioIndex] * Math.pow(10, scale) <= maximum)
        {
            maximumTick++;
            this.tickValues.put(maximumTick, this.simulationSpeedRatios[ratioIndex] * Math.pow(10, scale));
            StringBuilder text = new StringBuilder();
            text.append(this.simulationSpeedRatios[ratioIndex]);
            for (int i = 0; i < scale; i++)
            {
                text.append("0");
            }
            labels.put(maximumTick, new JLabel(text.toString().replace("000", "K")));
            ratioIndex++;
            if (ratioIndex == this.simulationSpeedRatios.length)
            {
                ratioIndex = 0;
                scale += 1;
            }
        }
        ratioIndex = this.simulationSpeedRatios.length - 1;
        scale = 1;
        while (this.simulationSpeedRatios[ratioIndex] * Math.pow(0.1, scale) >= minimum)
        {
            minimumTick--;
            this.tickValues.put(minimumTick, this.simulationSpeedRatios[ratioIndex] * Math.pow(0.1, scale));
            StringBuilder text = new StringBuilder("0").append(OtsSimulationControlPanel.this.decimalSeparator);
            for (int i = 1; i < scale; i++)
            {
                text.append("0");
            }
            text.append(this.simulationSpeedRatios[ratioIndex]);
            labels.put(minimumTick, new JLabel(text.toString()));
            ratioIndex--;
            if (ratioIndex < 0)
            {
                ratioIndex = this.simulationSpeedRatios.length - 1;
                scale += 1;
            }
        }
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL, minimumTick, maximumTick + 1, 0);
        slider.setMinimumSize(new Dimension(350, 45));
        slider.setPreferredSize(new Dimension(350, 45));
        slider.setMaximumSize(new Dimension(350, 45));
        labels.put(maximumTick + 1, new JLabel("\u221E"));
        this.tickValues.put(maximumTick + 1, 1E9);
        slider.setLabelTable(labels);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(1);
        /*- Uncomment to verify the stepToFactor method.
        for (int i = slider.getMinimum(); i <= slider.getMaximum(); i++)
        {
            System.out.println("pos=" + i + " value is " + stepToFactor(i));
        }
         */

        // initial value of simulation speed
        if (sim instanceof DevsRealTimeAnimator)
        {
            @SuppressWarnings("unchecked")
            DevsRealTimeAnimator<Duration> clock = (DevsRealTimeAnimator<Duration>) sim;
            clock.setSpeedFactor(this.tickValues.get(slider.getValue()));
        }

        // adjust the simulation speed
        slider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(final ChangeEvent ce)
            {
                JSlider source = (JSlider) ce.getSource();
                if (!source.getValueIsAdjusting() && sim instanceof DevsRealTimeAnimator)
                {
                    @SuppressWarnings("unchecked")
                    DevsRealTimeAnimator<Duration> clock = (DevsRealTimeAnimator<Duration>) sim;
                    clock.setSpeedFactor(OtsSimulationControlPanel.this.tickValues.get(source.getValue()));
                }
            }
        });

        return slider;
    }

    /**
     * Prepare the clean-up; stopping the simulator when going to close, and calling {@link #cleanup} when closed.
     */
    private void prepareCleanup()
    {
        // The JFrame is not yet created, as panels are prepared as an argument to it, so run a Thread until we find it
        new Thread("OtsSimulationControlPanel cleanup preparation")
        {
            @Override
            public void run()
            {
                JFrame root = null;
                int n = 0;
                while (root == null && n++ < 500) // should wait for about 5s max
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException exception)
                    {
                        // nothing to do
                    }
                    root = (JFrame) SwingUtilities.getRoot(OtsSimulationControlPanel.this);
                }
                root.addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosing(final WindowEvent e)
                    {
                        if (OtsSimulationControlPanel.this.simulator != null)
                        {
                            try
                            {
                                if (OtsSimulationControlPanel.this.simulator.isStartingOrRunning())
                                {
                                    OtsSimulationControlPanel.this.simulator.stop();
                                }
                            }
                            catch (SimRuntimeException exception)
                            {
                                exception.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void windowClosed(final WindowEvent e)
                    {
                        cleanup();
                    }
                });
            }
        }.start();
    }

    /**
     * Set the time warp factor to the best possible approximation of a given value.
     * @param factor the requested speed factor
     */
    public void setSpeedFactor(final double factor)
    {
        int bestStep = -1;
        double bestError = Double.MAX_VALUE;
        double logOfFactor = Math.log(factor);
        for (int step = this.speedSlider.getMinimum(); step <= this.speedSlider.getMaximum(); step++)
        {
            double ratio = this.tickValues.get(step); // stepToFactor(step);
            double logError = Math.abs(logOfFactor - Math.log(ratio));
            if (logError < bestError)
            {
                bestStep = step;
                bestError = logError;
            }
        }
        Logger.ots().trace("setSpeedfactor: factor is {}, best slider value is {} current value is {}", factor, bestStep,
                this.speedSlider.getValue());
        if (this.speedSlider.getValue() != bestStep)
        {
            this.speedSlider.setValue(bestStep);
        }
    }

    /**
     * Change the enabled/disabled state of the various simulation control buttons.
     * @param newState true if the buttons should become enabled; false if the buttons should become disabled
     */
    public void setSimulationControlButtons(final boolean newState)
    {
        this.buttonsEnabled = newState;
        setButtonsEnabledState();
    }

    /**
     * Construct and schedule a SimEvent using a Time to specify the execution time.
     * @param executionTime the time at which the event must happen
     * @param priority should be between {@code SimEventInterface.MAX_PRIORITY} and {@code SimEventInterface.MIN_PRIORITY}; most
     *            normal events should use {@code SimEventInterface.NORMAL_PRIORITY}
     * @param executable executable
     * @return the event that was scheduled (the caller should save this if a need to cancel the event may arise later)
     * @throws SimRuntimeException when the {@code executionTime} is in the past
     */
    private SimEventInterface<Duration> scheduleEvent(final Duration executionTime, final short priority,
            final Executable executable) throws SimRuntimeException
    {
        return this.simulator.scheduleEventAbs(executionTime, priority, executable);
    }

    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        Logger.ots().trace("actionCommand: " + actionCommand);
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
                    Logger.ots().trace("RunPause: Stopping simulator");
                    this.simulator.stop();
                }
                else if (getSimulator().getEventList().size() > 0)
                {
                    Logger.ots().trace("RunPause: Starting simulator");
                    this.simulator.start();
                }
            }
            if (actionCommand.equals("NextTime"))
            {
                if (getSimulator().isStartingOrRunning())
                {
                    Logger.ots().trace("NextTime: Stopping simulator");
                    getSimulator().stop();
                }
                try
                {
                    this.stopAtEvent = scheduleEvent(getSimulator().getSimulatorTime(), SimEventInterface.MIN_PRIORITY,
                            () -> autoPauseSimulator());
                }
                catch (SimRuntimeException exception)
                {
                    Logger.ots().error("Caught an exception while trying to schedule an autoPauseSimulator event "
                            + "at the current simulator time");
                }
                Logger.ots().trace("NextTime: Starting simulator");
                this.simulator.start();
            }
            setButtonsEnabledState();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Clean up timers, contexts, threads, etc. that could prevent garbage collection.
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
                        System.out.println("Clean-up: stopping simulator.");
                        this.simulator.stop();
                    }
                    getSimulator().cleanUp();
                }

                System.out.println("Clock timer cancelled.");
                if (this.clockLabel != null)
                {
                    this.clockLabel.cancelTimer(); // cancel the timer on the clock panel.
                }
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
    private void setButtonsEnabledState()
    {
        Logger.ots().trace("FixButtons entered");
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
                    button.setIcon(PAUSE_ICON);
                }
                else
                {
                    button.setToolTipText("Run the simulation at the indicated speed");
                    button.setIcon(PLAY_ICON);
                }
                button.setEnabled(moreWorkToDo && this.buttonsEnabled);
            }
            else if (actionCommand.equals("NextTime"))
            {
                button.setEnabled(moreWorkToDo && this.buttonsEnabled);
            }
            else
            {
                Logger.ots().error(new Exception("Unknown button?"));
            }
        }
        this.speedSlider.setEnabled(this.buttonsEnabled);
        Logger.ots().trace("FixButtons finishing");
    }

    /**
     * Pause the simulator.
     */
    public final void autoPauseSimulator()
    {
        Logger.ots().trace("OtsControlPanel.autoPauseSimulator entered");
        if (getSimulator().isStartingOrRunning())
        {
            Duration currentTick = getSimulator().getSimulatorTime();
            Duration nextTick = getSimulator().getEventList().first().getAbsoluteExecutionTime();
            Logger.ots().trace("currentTick is {}", currentTick);
            Logger.ots().trace("nextTick is {}", nextTick);
            if (nextTick.gt(currentTick))
            {
                // The clock is now just beyond where it was when the user requested the NextTime operation
                // Insert another autoPauseSimulator event just before what is now the time of the next event
                // and let the simulator time increment to that time
                Logger.ots().trace("Re-Scheduling at " + nextTick);
                try
                {
                    this.stopAtEvent = scheduleEvent(nextTick, SimEventInterface.MAX_PRIORITY, () -> autoPauseSimulator());
                    Logger.ots().trace("AutoPauseSimulator: starting simulator");
                }
                catch (SimRuntimeException exception)
                {
                    Logger.ots()
                            .error("Caught an exception while trying to re-schedule an autoPauseEvent at the next real event");
                }
            }
            else
            {
                try
                {
                    Logger.ots().trace("AutoPauseSimulator: stopping simulator");
                    getSimulator().stop();
                }
                catch (SimRuntimeException exception1)
                {
                    exception1.printStackTrace();
                }
                Logger.ots().trace("Not re-scheduling");
                if (SwingUtilities.isEventDispatchThread())
                {
                    Logger.ots().trace("Already on EventDispatchThread");
                    setButtonsEnabledState();
                }
                else
                {
                    try
                    {
                        Logger.ots().trace("Current thread is NOT EventDispatchThread: " + Thread.currentThread());
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Logger.ots().trace("Runnable started");
                                setButtonsEnabledState();
                                Logger.ots().trace("Runnable finishing");
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        if (e instanceof InterruptedException)
                        {
                            Logger.ots().error(e);
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
        Logger.ots().trace("OtsControlPanel.autoPauseSimulator finished");
    }

    @Override
    public final void propertyChange(final PropertyChangeEvent evt)
    {
        // timeEdit value changed, schedule stop event (and cancel possible previous event)
        Logger.ots().trace("PropertyChanged: " + evt);
        if (null != this.stopAtEvent)
        {
            getSimulator().cancelEvent(this.stopAtEvent); // silently ignore false result
            this.stopAtEvent = null;
        }
        String newValue = (String) evt.getNewValue();
        String[] fields = newValue.split("[:\\" + this.decimalSeparator + "]");
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
                this.stopAtEvent =
                        scheduleEvent(Duration.ofSI(stopTime), SimEventInterface.MAX_PRIORITY, () -> autoPauseSimulator());
            }
            catch (SimRuntimeException exception)
            {
                Logger.ots().error("Caught an exception while trying to schedule an autoPauseSimulator event");
            }
        }
    }

    /**
     * Return simulator.
     * @return simulator
     */
    public final OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Return time font.
     * @return font for the time display
     */
    public final Font getTimeFont()
    {
        return this.timeFont;
    }

    @Override
    public final void notify(final Event event)
    {
        if (event.getType().equals(Replication.END_REPLICATION_EVENT) || event.getType().equals(SimulatorInterface.START_EVENT)
                || event.getType().equals(SimulatorInterface.STOP_EVENT)
                || event.getType().equals(DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT))
        {
            Logger.ots().trace("OtsControlPanel receive event " + event);
            if (event.getType().equals(DevsRealTimeAnimator.CHANGE_SPEED_FACTOR_EVENT))
            {
                setSpeedFactor((Double) event.getContent());
                return;
            }
            else if (event.getType().equals(Replication.END_REPLICATION_EVENT))
            {
                this.buttonsEnabled = false;
            }
            setButtonsEnabledState();
        }
    }

    @Override
    public final String toString()
    {
        return "OtsControlPanel [simulatorTime=" + this.simulator.getSimulatorTime() + "]";
    }

    /**
     * {@link JLabel} that displays the simulation time.
     */
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
         * @param speedLabel speed label
         */
        ClockLabel(final JLabel speedLabel)
        {
            super("00:00:00" + OtsSimulationControlPanel.this.decimalSeparator + "000");
            this.speedLabel = speedLabel;
            speedLabel.setFont(getTimeFont());
            setFont(getTimeFont());
            setHorizontalAlignment(SwingConstants.RIGHT);
            setOpaque(true);
            this.timer = new Timer();
            this.timer.scheduleAtFixedRate(new TimeUpdateTask(), 0, ClockLabel.UPDATEINTERVAL);
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(final MouseEvent e)
                {
                    if (!OtsSimulationControlPanel.this.buttonsEnabled)
                    {
                        return;
                    }
                    setVisible(false);
                    OtsSimulationControlPanel.this.timeEdit.setVisible(true);
                    OtsSimulationControlPanel.this.timeEdit.requestFocus();
                    getParent().invalidate();
                }
            });
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
        private class TimeUpdateTask extends TimerTask
        {
            /**
             * Constructor.
             */
            TimeUpdateTask()
            {
            }

            @Override
            public void run()
            {
                double now = Math.round(getSimulator().getSimulatorTime().getSI() * 1000) / 1000d;
                int seconds = (int) Math.floor(now);
                int h = (int) seconds / 3600;
                int m = (int) (seconds - h * 3600) / 60;
                double s = now - h * 3600 - m * 60;
                ClockLabel.this.setText(String.format("  %02d:%02d:%06.3f ", h, m, s));
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

            @Override
            public final String toString()
            {
                return "TimeUpdateTask of ClockPanel";
            }
        }

        /**
         * Return speed label.
         * @return speed label
         */
        protected JLabel getSpeedLabel()
        {
            return this.speedLabel;
        }

        /**
         * Returns the simulation speed.
         * @param t simulation time
         * @return simulation speed
         */
        protected double getSpeed(final double t)
        {
            double speed = (t - this.prevSimTime) / (0.001 * UPDATEINTERVAL);
            this.prevSimTime = t;
            return speed;
        }

        @Override
        public boolean isForeground()
        {
            return true;
        }

        @Override
        public boolean isBackground()
        {
            return true;
        }

        @Override
        public void setBackground(final Color color)
        {
            double f = 0.92;
            super.setBackground(
                    new Color((int) (color.getRed() * f), (int) (color.getGreen() * f), (int) (color.getBlue() * f)));
        }

        @Override
        public OptionalInt getFontSize()
        {
            return OptionalInt.empty();
        }

        @Override
        public final String toString()
        {
            return "ClockPanel";
        }

    }

    /** Entry field for simulate until time. */
    private final class TimeEdit extends JFormattedTextField implements AppearanceControl
    {

        /** */
        private static final long serialVersionUID = 20141212L;

        /** Last caret position in the time editor. USed to know whether to skip left or right around ':', '.' or ','. */
        private int lastCaretPosition = -1;

        /**
         * Construct a new TimeEdit.
         * @param initialValue the initial value for the TimeEdit
         */
        private TimeEdit(final Time initialValue)
        {
            super(new RegexFormatter(
                    "\\d{2,}:[0-5]\\d:[0-5]\\d\\" + OtsSimulationControlPanel.this.decimalSeparator + "\\d\\d\\d"));
            addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(final KeyEvent e)
                {
                    String value = getText();
                    int caretPosition = getCaretPosition();
                    ((RegexFormatter) getFormatter()).setOverwriteMode(caretPosition > value.indexOf(':') - 2);
                }
            });
            addCaretListener((e) ->
            {
                String value = getText();
                int caretPosition = getCaretPosition();
                if (value.length() - 1 > caretPosition && (value.charAt(caretPosition) == ':'
                        || value.charAt(caretPosition) == '.' || value.charAt(caretPosition) == ','))
                {
                    caretPosition = caretPosition + (this.lastCaretPosition <= caretPosition ? 1 : -1);
                    this.lastCaretPosition = caretPosition;
                    this.setCaretPosition(caretPosition);
                }
                else if (e.getDot() != e.getMark())
                {
                    this.lastCaretPosition = caretPosition;
                    this.setCaretPosition(caretPosition);
                }
            });
            addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(final FocusEvent e)
                {
                    OtsSimulationControlPanel.this.clockLabel.setVisible(true);
                    setVisible(false);
                    getParent().invalidate();
                }
            });
            OtsSimulationControlPanel.this.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(final MouseEvent e)
                {
                    if (OtsSimulationControlPanel.this.timeEdit.hasFocus())
                    {
                        // removes focus from time edit when the user clicks anywhere on the control panel
                        TimeEdit.this.setFocusable(false);
                        TimeEdit.this.setFocusable(true);
                    }
                    // this listener prevents events from reaching the main panel with pop-up menu, make it appear from here
                    JPanel mainPanel = (JPanel) ((AppearanceApplication) SwingUtilities
                            .getAncestorOfClass(AppearanceApplication.class, OtsSimulationControlPanel.this)).getContentPane();
                    if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1
                            && mainPanel.getComponentPopupMenu() != null)
                    {
                        mainPanel.getComponentPopupMenu().show(mainPanel, e.getX(), e.getY());
                    }
                }
            });
            RegexFormatter formatter = (RegexFormatter) getFormatter();
            formatter.setAllowsInvalid(false);
            formatter.setCommitsOnValidEdit(true);
            formatter.setOverwriteMode(true);
            setTime(initialValue);
            setFont(getTimeFont());
            setHorizontalAlignment(SwingConstants.RIGHT);
            setVisible(false);
        }

        /**
         * Set or update the time shown in this TimeEdit.
         * @param newValue the (new) value to set/show in this TimeEdit
         */
        public void setTime(final Time newValue)
        {
            double v = newValue.getSI();
            int seconds = (int) Math.floor(v);
            int h = (int) seconds / 3600;
            int m = (int) (seconds - h * 3600) / 60;
            double s = v - h * 3600 - m * 60;
            this.setText(String.format("%02d:%02d:%06.3f", h, m, s));
        }

        @Override
        public OptionalInt getFontSize()
        {
            return OptionalInt.empty();
        }

        @Override
        public String toString()
        {
            return "TimeEdit [time=" + getText() + "]";
        }

    }

    /**
     * Extension of a {@link DefaultFormatter} that uses a regular expression. For use in the simulate until time edit. <br>
     * Derived from <a href="http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm">
     * http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     */
    private static final class RegexFormatter extends DefaultFormatter
    {

        /** */
        private static final long serialVersionUID = 20141212L;

        /** The regular expression pattern. */
        private Pattern pattern;

        /**
         * Create a new RegexFormatter.
         * @param pattern regular expression pattern that defines what this RexexFormatter will accept
         */
        private RegexFormatter(final String pattern)
        {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public Object stringToValue(final String text) throws ParseException
        {
            Matcher matcher = this.pattern.matcher(text);
            if (matcher.matches())
            {
                Logger.ots().trace("String \"" + text + "\" matches");
                return super.stringToValue(text);
            }
            Logger.ots().trace("String \"" + text + "\" does not match");
            throw new ParseException("Pattern did not match", 0);
        }

        @Override
        public String toString()
        {
            return "RegexFormatter [pattern=" + this.pattern + "]";
        }

    }

}
