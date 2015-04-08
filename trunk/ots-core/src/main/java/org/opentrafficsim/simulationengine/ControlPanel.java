package org.opentrafficsim.simulationengine;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.MaskFormatter;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.gui.swing.SimulatorControlPanel;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Peter's improved simulation control panel.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 11 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ControlPanel implements ActionListener, PropertyChangeListener
{
    /** The simulator. */
    private final SimpleSimulator simulator;

    /** The SimulatorInterface that is controlled by the buttons. */
    private final SimulatorInterface<?, ?, ?> target;

    /** Logger. */
    private final Logger logger;

    /** The clock. */
    private final ClockPanel clockPanel;

    /** The time warp control. */
    private final TimeWarpPanel timeWarpPanel;

    /** The control buttons. */
    private final ArrayList<JButton> buttons = new ArrayList<JButton>();

    /** Font used to display the clock and the stop time. */
    private final Font timeFont = new Font("SansSerif", Font.BOLD, 18);

    /** The TimeEdit that lets the user set a time when the simulation will be stopped. */
    private final TimeEdit timeEdit;

    /** The currently registered stop at event. */
    private SimEvent<OTSSimTimeDouble> stopAtEvent = null;

    /**
     * Decorate a SimpleSimulator with a different set of control buttons.
     * @param simulator SimpleSimulator; the simulator.
     */
    public ControlPanel(final SimpleSimulator simulator)
    {
        this.simulator = simulator;
        this.target = simulator.getSimulator();
        this.logger = Logger.getLogger("nl.tudelft.opentrafficsim");

        DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel =
                simulator.getPanel();
        SimulatorControlPanel controlPanel =
                (SimulatorControlPanel) ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        JPanel buttonPanel = (JPanel) controlPanel.getComponent(0);
        buttonPanel.removeAll();
        buttonPanel.add(makeButton("stepButton", "/Last_recor.png", "Step", "Execute one event", true));
        buttonPanel.add(makeButton("nextTimeButton", "/NextTrack.png", "NextTime",
                "Execute all events scheduled for the current time", true));
        buttonPanel.add(makeButton("runButton", "/Play.png", "Run", "Run the simulation at maximum speed", true));
        buttonPanel.add(makeButton("pauseButton", "/Pause.png", "Pause", "Pause the simulator", false));
        this.timeWarpPanel = new TimeWarpPanel(0.1, 100, 1, 3);
        buttonPanel.add(this.timeWarpPanel);
        buttonPanel.add(makeButton("resetButton", "/Undo.png", "Reset", null, false));
        this.clockPanel = new ClockPanel();
        buttonPanel.add(this.clockPanel);
        this.timeEdit = new TimeEdit(new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND));
        this.timeEdit.addPropertyChangeListener("value", this);
        buttonPanel.add(this.timeEdit);
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
    private JButton makeButton(final String name, final String iconPath, final String actionCommand,
            final String toolTipText, final boolean enabled)
    {
        // JButton result = new JButton(new ImageIcon(this.getClass().getResource(iconPath)));
        JButton result = new JButton(new ImageIcon(URLResource.getResource(iconPath)));
        result.setName(name);
        result.setEnabled(enabled);
        result.setActionCommand(actionCommand);
        result.setToolTipText(toolTipText);
        result.addActionListener(this);
        this.buttons.add(result);
        return result;
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
                if (getSimulator().isRunning())
                {
                    getSimulator().stop();
                }
                this.target.step();
            }
            if (actionCommand.equals("Run"))
            {
                this.target.start();
            }
            if (actionCommand.equals("NextTime"))
            {
                if (getSimulator().isRunning())
                {
                    getSimulator().stop();
                }
                double now = getSimulator().getSimulatorTime().get().getSI();
                // System.out.println("now is " + now);
                try
                {
                    this.stopAtEvent =
                            this.simulator.scheduleEvent(new DoubleScalar.Abs<TimeUnit>(now, TimeUnit.SI),
                                    SimEventInterface.MIN_PRIORITY, this, this, "autoPauseSimulator", null);
                }
                catch (SimRuntimeException exception)
                {
                    this.logger.logp(Level.SEVERE, "ControlPanel", "autoPauseSimulator", "Caught an exception "
                            + "while trying to schedule an autoPauseSimulator event at the current simulator time");
                }
                this.target.start();
            }
            if (actionCommand.equals("Pause"))
            {
                this.target.stop();
            }
            if (actionCommand.equals("Reset"))
            {
                if (getSimulator().isRunning())
                {
                    getSimulator().stop();
                }
                // Should this create a new replication?
            }
            fixButtons();
        }
        catch (Exception exception)
        {
            this.logger.logp(Level.SEVERE, "ControlPanel", "actionPerformed", "", exception);
        }
    }

    /**
     * Update the enabled state of all the buttons.
     */
    protected final void fixButtons()
    {
        System.out.println("FixButtons entered");
        final boolean moreWorkToDo = getSimulator().getEventList().size() > 0;
        for (JButton button : this.buttons)
        {
            final String actionCommand = button.getActionCommand();
            if (actionCommand.equals("Step"))
            {
                button.setEnabled(moreWorkToDo);
            }
            else if (actionCommand.equals("Run"))
            {
                button.setEnabled(moreWorkToDo && !getSimulator().isRunning());
            }
            else if (actionCommand.equals("NextTime"))
            {
                button.setEnabled(moreWorkToDo);
            }
            else if (actionCommand.equals("Pause"))
            {
                button.setEnabled(getSimulator().isRunning());
            }
            else if (actionCommand.equals("Reset"))
            {
                button.setEnabled(true); // FIXME: should be disabled when the simulator was just reset or initialized
            }
            else
            {
                this.logger.logp(Level.SEVERE, "ControlPanel", "fixButtons", "", new Exception("Unknown button?"));
            }
        }
        System.out.println("FixButtons finishing");
    }

    /**
     * Pause the simulator.
     */
    public final void autoPauseSimulator()
    {
        if (getSimulator().isRunning())
        {
            getSimulator().stop();
            double currentTick = getSimulator().getSimulatorTime().get().getSI();
            double nextTick = getSimulator().getEventList().first().getAbsoluteExecutionTime().get().getSI();
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
                    this.stopAtEvent =
                            this.simulator.scheduleEvent(new DoubleScalar.Abs<TimeUnit>(nextTick, TimeUnit.SI),
                                    SimEventInterface.MAX_PRIORITY, this, this, "autoPauseSimulator", null);
                    getSimulator().start();
                }
                catch (SimRuntimeException exception)
                {
                    this.logger.logp(Level.SEVERE, "ControlPanel", "autoPauseSimulator",
                            "Caught an exception while trying to re-schedule an autoPauseEvent at the next real event");
                }
            }
            else
            {
                // System.out.println("Not re-scheduling");
                if (SwingUtilities.isEventDispatchThread())
                {
                    System.out.println("Already on EventDispatchThread");
                    fixButtons();
                }
                else
                {
                    try
                    {
                        System.out.println("Current thread is NOT EventDispatchThread: " + Thread.currentThread());
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                System.out.println("Runnable started");
                                fixButtons();
                                System.out.println("Runnable finishing");
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        if (e instanceof InterruptedException)
                        {
                            System.out.println("Caught " + e);
                            e.printStackTrace();
                        }
                        else
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
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
        if (stopTime < getSimulator().getSimulatorTime().get().getSI())
        {
            return;
        }
        else
        {
            try
            {
                this.stopAtEvent =
                        this.simulator.scheduleEvent(new DoubleScalar.Abs<TimeUnit>(stopTime, TimeUnit.SECOND),
                                SimEventInterface.MAX_PRIORITY, this, this, "autoPauseSimulator", null);
            }
            catch (SimRuntimeException exception)
            {
                this.logger.logp(Level.SEVERE, "ControlPanel", "propertyChange",
                        "Caught an exception while trying to schedule an autoPauseSimulator event");
            }
        }

    }

    /**
     * @return simulator.
     */
    public final DEVSSimulator<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
    {
        return this.simulator.getSimulator();
    }

    /**
     * @return timeFont.
     */
    public final Font getTimeFont()
    {
        return this.timeFont;
    }

    /** JPanel that contains a JSider that uses a logarithmic scale. */
    class TimeWarpPanel extends JPanel
    {
        /** */
        private static final long serialVersionUID = 20150408L;

        /** The JSlider that the user sees. */
        private final JSlider slider;

        /** The ratios used in each decade. */
        private final int[] ratios;

        /**
         * Construct a new TimeWarpPanel.
         * @param minimum double; the minimum value on the scale (the displayed scale may extend a little further than
         *            this value)
         * @param maximum double; the maximum value on the scale (the displayed scale may extend a little further than
         *            this value)
         * @param initialValue double; the initially selected value on the scale
         * @param ticksPerDecade int; the number of steps per decade
         */
        public TimeWarpPanel(final double minimum, final double maximum, final double initialValue,
                final int ticksPerDecade)
        {
            if (minimum <= 0 || minimum > initialValue || initialValue > maximum)
            {
                throw new Error("Bad (combination of) minimum, maximum and initialValue; "
                        + "(restrictions: 0 < minimum <= initialValue <= maximum)");
            }
            switch (ticksPerDecade)
            {
                case 1:
                    this.ratios = new int[]{1};
                    break;
                case 2:
                    this.ratios = new int[]{1, 3};
                    break;
                case 3:
                    this.ratios = new int[]{1, 2, 5};
                    break;
                default:
                    throw new Error("Bad ticksPerDecade value (must be 1, 2 or 3)");
            }
            int minimumTick = (int) Math.floor(Math.log10(minimum / initialValue) * ticksPerDecade);
            int maximumTick = (int) Math.ceil(Math.log10(maximum / initialValue) * ticksPerDecade);
            this.slider = new JSlider(SwingConstants.HORIZONTAL, minimumTick, maximumTick, 0);
            Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
            for (int step = 0; step <= maximumTick; step++)
            {
                StringBuilder text = new StringBuilder();
                text.append(this.ratios[step % this.ratios.length]);
                for (int decade = 0; decade < step / this.ratios.length; decade++)
                {
                    text.append("0");
                }
                labels.put(step, new JLabel(text.toString()));
                System.out.println("Label " + step + " is \"" + text.toString() + "\"");
            }
            // Figure out the DecimalSymbol
            String decimalSeparator =
                    "" + ((DecimalFormat) NumberFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();
            for (int step = -1; step >= minimumTick; step--)
            {
                StringBuilder text = new StringBuilder();
                text.append("0");
                text.append(decimalSeparator);
                for (int decade = (int) Math.floor((step + 1) / this.ratios.length); decade < 0; decade++)
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
                System.out.println("Label " + step + " is \"" + text.toString() + "\"");
            }
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
            
            // TODO: add event listener handling
        }

        /**
         * Convert a position on the slider to a factor.
         * @param step int; the position on the slider
         * @return double; the factor that corresponds to step
         */
        private final double stepToFactor(final int step)
        {
            int index = step % this.ratios.length;
            if (index < 0)
            {
                index += this.ratios.length;
            }
            double result = this.ratios[index];
            // Make positive to avoid trouble with negative values that round towards 0 on division
            int power = (step + 1000 * this.ratios.length) / this.ratios.length - 1000;// This is ugly
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
    }

    /** JLabel that displays the simulation time. */
    class ClockPanel extends JLabel
    {
        /** */
        private static final long serialVersionUID = 20141211L;

        /** The JLabel that displays the time. */
        private final JLabel clockLabel;

        /** timer update in msec. */
        private static final long UPDATEINTERVAL = 1000;

        /** Construct a clock panel. */
        ClockPanel()
        {
            super("00:00:00.000");
            this.clockLabel = this;
            this.setFont(getTimeFont());
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimeUpdateTask(), 0, ClockPanel.UPDATEINTERVAL);

        }

        /** Updater for the clock panel. */
        private class TimeUpdateTask extends TimerTask
        {
            /**
             * Create a TimeUpdateTask.
             */
            public TimeUpdateTask()
            {
            }

            /** {@inheritDoc} */
            @Override
            public void run()
            {
                double now = Math.round(getSimulator().getSimulatorTime().get().getSI() * 1000) / 1000d;
                int seconds = (int) Math.floor(now);
                int fractionalSeconds = (int) Math.floor(1000 * (now - seconds));
                getClockLabel().setText(
                        String.format("  %02d:%02d:%02d.%03d  ", seconds / 3600, seconds / 60 % 60, seconds % 60,
                                fractionalSeconds));
                getClockLabel().repaint();
            }
        }

        /**
         * @return clockLabel.
         */
        protected JLabel getClockLabel()
        {
            return this.clockLabel;
        }

    }

    /** Entry field for time. */
    class TimeEdit extends JFormattedTextField
    {
        /** */
        private static final long serialVersionUID = 20141212L;

        /**
         * Construct a new TimeEdit.
         * @param initialValue DoubleScalar.Abs&lt;TimeUnit&gt;; the initial value for the TimeEdit
         */
        TimeEdit(final DoubleScalar.Abs<TimeUnit> initialValue)
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
         * @param newValue DoubleScalar.Abs&lt;TimeUnit&gt;; the (new) value to set/show in this TimeEdit
         */
        public void setTime(final DoubleScalar.Abs<TimeUnit> newValue)
        {
            double v = newValue.getSI();
            int integerPart = (int) Math.floor(v);
            int fraction = (int) Math.floor((v - integerPart) * 1000);
            String text =
                    String.format("%04d:%02d:%02d.%03d", integerPart / 3600, integerPart / 60 % 60, integerPart % 60,
                            fraction);
            this.setText(text);
        }
    }

    /**
     * Extension of a DefaultFormatter that uses a regular expression. <br>
     * Derived from <a
     * href="http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm">
     * http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm</a>
     * <p>
     * @version 12 dec. 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
        public RegexFormatter(final String pattern)
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
    }

}
