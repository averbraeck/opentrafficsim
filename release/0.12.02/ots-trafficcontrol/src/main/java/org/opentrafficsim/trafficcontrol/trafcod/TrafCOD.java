package org.opentrafficsim.trafficcontrol.trafcod;

import java.awt.Container;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.Throw;

import org.djunits.unit.TimeUnit;
import org.djunits.value.formatter.EngineeringFormatter;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.road.network.lane.object.sensor.NonDirectionalOccupancySensor;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.trafficcontrol.TrafficController;

/**
 * TrafCOD evaluator. TrafCOD is a language for writing traffic control programs. A TrafCOD program consists of a set of rules
 * that must be evaluated repeatedly (until no more changes occurr) every time step. The time step size is 0.1 seconds.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 5, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCOD extends EventProducer implements TrafficController
{
    /** */
    private static final long serialVersionUID = 20161014L;

    /** Name of this TrafCod controller. */
    final String controllerName;

    /** Version of the supported TrafCOD files. */
    final static int TRAFCOD_VERSION = 100;

    /** The evaluation interval of TrafCOD. */
    final static Duration EVALUATION_INTERVAL = new Duration(0.1, TimeUnit.SECOND);

    /** Text leading up to the TrafCOD version number. */
    private final static String VERSION_PREFIX = "trafcod-version=";

    /** Text on line before the sequence line. */
    private final static String SEQUENCE_KEY = "Sequence";

    /** Text leading up to the control program structure. */
    private final static String STRUCTURE_PREFIX = "Structure:";

    /** Constant to select variables that have no associated traffic stream. */
    public final static int NO_STREAM = -1;

    /** The original rules. */
    final List<String> trafcodRules = new ArrayList<>();

    /** The tokenized rules. */
    final List<Object[]> tokenisedRules = new ArrayList<>();

    /** The TrafCOD variables. */
    final Map<String, Variable> variables = new HashMap<>();

    /** The TrafCOD variables in order of definition. */
    final List<Variable> variablesInDefinitionOrder = new ArrayList<>();

    /** The detectors. */
    final Map<String, Variable> detectors = new HashMap<>();

    /** Comment starter in TrafCOD. */
    final static String COMMENT_PREFIX = "#";

    /** Prefix for initialization rules. */
    private final static String INIT_PREFIX = "%init ";

    /** Prefix for time initializer rules. */
    private final static String TIME_PREFIX = "%time ";

    /** Prefix for export rules. */
    private final static String EXPORT_PREFIX = "%export ";

    /** Sequence information; number of conflict groups. */
    private int conflictGroups = -1;

    /** Sequence information; size of conflict group. */
    private int conflictGroupSize = -1;

    /** Chosen structure number (as assigned by VRIGen). */
    private int structureNumber = -1;

    /** The conflict groups in order that they will be served. */
    @SuppressWarnings("unused")
    private List<List<Short>> conflictgroups = new ArrayList<>();

    /** Maximum number of evaluation loops. */
    private int maxLoopCount = 10;

    /** Position in current expression. */
    private int currentToken;

    /** The expression evaluation stack. */
    private List<Integer> stack = new ArrayList<Integer>();

    /** Rule that is currently being evaluated. */
    private Object[] currentRule;

    /** The current time in units of 0.1 s */
    private int currentTime10 = 0;

    /** Event that is fired whenever a traffic light changes state. */
    public static final EventType TRAFFIC_LIGHT_CHANGED = new EventType("TrafficLightChanged");

    /** The simulation engine. */
    private final DEVSSimulator<Time, Duration, OTSSimTimeDouble> simulator;

    /**
     * Construct a new TrafCOD traffic light controller.
     * @param controllerName String; name of this TrafCOD traffic light controller
     * @param trafCodURL String; the URL of the TrafCOD rules
     * @param trafficLights Set&lt;TrafficLight&gt;; the traffic lights. The ids of the traffic lights must end with two digits
     *            that match the stream numbers as used in the traffic control program
     * @param sensors Set&lt;TrafficLightSensor&gt;; the traffic sensors. The ids of the traffic sensors must end with three
     *            digits; the first two of those must match the stream and sensor numbers used in the traffic control program
     * @param simulator DEVSSimulator&lt;Time, Duration, OTSSimTimeDouble&gt;; the simulation engine
     * @param display Container; if non-null, a controller display is constructed and shown in the supplied container
     * @throws TrafficControlException when a rule cannot be parsed
     * @throws SimRuntimeException when scheduling the first evaluation event fails
     */
    public TrafCOD(String controllerName, final String trafCodURL, final Set<TrafficLight> trafficLights,
            final Set<TrafficLightSensor> sensors, final DEVSSimulator<Time, Duration, OTSSimTimeDouble> simulator,
            Container display) throws TrafficControlException, SimRuntimeException
    {
        Throw.whenNull(trafCodURL, "trafCodURL may not be null");
        Throw.whenNull(controllerName, "controllerName may not be null");
        Throw.whenNull(trafficLights, "trafficLights may not be null");
        Throw.whenNull(simulator, "simulator may not be null");
        this.simulator = simulator;
        this.controllerName = controllerName;
        try
        {
            parseTrafCOD(trafCodURL, trafficLights, sensors);
        }
        catch (IOException exception)
        {
            throw new TrafficControlException(exception);
        }
        if (null != display)
        {
            String tfgFileURL = trafCodURL.replaceFirst("\\.[Tt][Ff][Cc]$", ".tfg");
            // System.out.println("mapFileURL is \"" + tfgFileURL + "\"");
            TrafCODDisplay tcd = makeDisplay(tfgFileURL, sensors);
            if (null != tcd)
            {
                display.add(tcd);
            }
        }
        // Initialize the variables that have a non-zero initial value
        for (Variable v : this.variablesInDefinitionOrder)
        {
            v.initialize();
        }
        // The first rule evaluation should occur at t=0.1s
        this.simulator.scheduleEventRel(EVALUATION_INTERVAL, this, this, "evalExprs", null);
    }

    /**
     * Read and parse the TrafCOD traffic control program.
     * @param trafCodURL String; the URL where the TrafCOD file is to be read from
     * @param trafficLights Set&lt;TrafficLight&gt;; the traffic lights that may be referenced from the TrafCOD file
     * @param sensors Set&lt;TrafficLightSensor&gt;; the detectors that may be referenced from the TrafCOD file
     * @throws MalformedURLException when the URL is invalid
     * @throws IOException when the TrafCOD file could not be read
     * @throws TrafficControlException when the TrafCOD file contains errors
     */
    private void parseTrafCOD(final String trafCodURL, final Set<TrafficLight> trafficLights,
            final Set<TrafficLightSensor> sensors) throws MalformedURLException, IOException, TrafficControlException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(trafCodURL).openStream()));
        String inputLine;
        int lineno = 0;
        while ((inputLine = in.readLine()) != null)
        {
            ++lineno;
            // System.out.println(lineno + ":\t" + inputLine);
            String trimmedLine = inputLine.trim();
            if (trimmedLine.length() == 0)
            {
                continue;
            }
            String locationDescription = trafCodURL + "(" + lineno + ") ";
            if (trimmedLine.startsWith(COMMENT_PREFIX))
            {
                String commentStripped = trimmedLine.substring(1).trim();
                if (stringBeginsWithIgnoreCase(VERSION_PREFIX, commentStripped))
                {
                    String versionString = commentStripped.substring(VERSION_PREFIX.length());
                    try
                    {
                        int observedVersion = Integer.parseInt(versionString);
                        if (TRAFCOD_VERSION != observedVersion)
                        {
                            throw new TrafficControlException("Wrong TrafCOD version (expected " + TRAFCOD_VERSION + ", got "
                                    + observedVersion + ")");
                        }
                    }
                    catch (NumberFormatException nfe)
                    {
                        nfe.printStackTrace();
                        throw new TrafficControlException("Could not parse TrafCOD version (got \"" + versionString + ")");
                    }
                }
                else if (stringBeginsWithIgnoreCase(SEQUENCE_KEY, commentStripped))
                {
                    while (trimmedLine.startsWith(COMMENT_PREFIX))
                    {
                        inputLine = in.readLine();
                        if (null == inputLine)
                        {
                            throw new TrafficControlException("Unexpected EOF (reading sequence key at " + locationDescription
                                    + ")");
                        }
                        ++lineno;
                        trimmedLine = inputLine.trim();
                    }
                    String[] fields = inputLine.split("\t");
                    if (fields.length != 2)
                    {
                        throw new TrafficControlException("Wrong number of fields in Sequence information");
                    }
                    try
                    {
                        this.conflictGroups = Integer.parseInt(fields[0]);
                        this.conflictGroupSize = Integer.parseInt(fields[1]);
                    }
                    catch (NumberFormatException nfe)
                    {
                        nfe.printStackTrace();
                        throw new TrafficControlException("Bad number of conflict groups or bad conflict group size");
                    }
                }
                else if (stringBeginsWithIgnoreCase(STRUCTURE_PREFIX, commentStripped))
                {
                    String structureNumberString = commentStripped.substring(STRUCTURE_PREFIX.length()).trim();
                    try
                    {
                        this.structureNumber = Integer.parseInt(structureNumberString);
                    }
                    catch (NumberFormatException nfe)
                    {
                        nfe.printStackTrace();
                        throw new TrafficControlException("Bad structure number (got \"" + structureNumberString + "\" at "
                                + locationDescription + ")");
                    }
                    for (int conflictMemberLine = 0; conflictMemberLine < this.conflictGroups; conflictMemberLine++)
                    {
                        while (trimmedLine.startsWith(COMMENT_PREFIX))
                        {
                            inputLine = in.readLine();
                            if (null == inputLine)
                            {
                                throw new TrafficControlException("Unexpected EOF (reading sequence key at "
                                        + locationDescription + ")");
                            }
                            ++lineno;
                            trimmedLine = inputLine.trim();
                        }
                        String[] fields = inputLine.split("\t");
                        if (fields.length != this.conflictGroupSize)
                        {
                            throw new TrafficControlException("Wrong number of conflict groups in Structure information");
                        }
                        List<Short> row = new ArrayList<>(this.conflictGroupSize);
                        for (int col = 0; col < this.conflictGroupSize; col++)
                        {
                            try
                            {
                                Short stream = Short.parseShort(fields[col]);
                                row.add(stream);
                            }
                            catch (NumberFormatException nfe)
                            {
                                nfe.printStackTrace();
                                throw new TrafficControlException("Wrong number of streams in conflict group " + trimmedLine);
                            }
                        }
                    }
                }
                continue;
            }
            if (stringBeginsWithIgnoreCase(INIT_PREFIX, trimmedLine))
            {
                String varNameAndInitialValue = trimmedLine.substring(INIT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = varNameAndInitialValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0], locationDescription);
                installVariable(nameAndStream.getName(), nameAndStream.getStream(), EnumSet.noneOf(Flags.class),
                        locationDescription).setFlag(Flags.INITED);
                // The supplied initial value is ignored (in this version of the TrafCOD interpreter)!
                continue;
            }
            if (stringBeginsWithIgnoreCase(TIME_PREFIX, trimmedLine))
            {
                String timerNameAndMaximumValue = trimmedLine.substring(INIT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = timerNameAndMaximumValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0], locationDescription);
                Variable variable =
                        installVariable(nameAndStream.getName(), nameAndStream.getStream(), EnumSet.noneOf(Flags.class),
                                locationDescription);
                int value10 = Integer.parseInt(fields[1]);
                variable.setTimerMax(value10);
                continue;
            }
            if (stringBeginsWithIgnoreCase(EXPORT_PREFIX, trimmedLine))
            {
                String varNameAndOutputValue = trimmedLine.substring(EXPORT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = varNameAndOutputValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0], locationDescription);
                Variable variable =
                        installVariable(nameAndStream.getName(), nameAndStream.getStream(), EnumSet.noneOf(Flags.class),
                                locationDescription);
                int value = Integer.parseInt(fields[1]);
                variable.setOutput(value);
                int added = 0;
                // TODO create the set of traffic lights of this stream only once (not repeat for each possible color)
                for (TrafficLight trafficLight : trafficLights)
                {
                    String id = trafficLight.getId();
                    if (id.length() < 2)
                    {
                        throw new TrafficControlException("Id of traffic light " + trafficLight + " does not end on two digits");
                    }
                    String streamLetters = id.substring(id.length() - 2);
                    if (!Character.isDigit(streamLetters.charAt(0)) || !Character.isDigit(streamLetters.charAt(1)))
                    {
                        throw new TrafficControlException("Id of traffic light " + trafficLight + " does not end on two digits");
                    }
                    int stream = Integer.parseInt(streamLetters);
                    if (variable.getStream() == stream)
                    {
                        variable.addOutput(trafficLight);
                        added++;
                    }
                }
                if (0 == added)
                {
                    throw new TrafficControlException("No traffic light provided that matches stream " + variable.getStream());
                }
                continue;
            }
            this.trafcodRules.add(trimmedLine);
            Object[] tokenisedRule = parse(trimmedLine, locationDescription);
            if (null != tokenisedRule)
            {
                this.tokenisedRules.add(tokenisedRule);
                // System.out.println(printRule(tokenisedRule, false));
            }
        }
        in.close();
        for (Variable variable : this.variables.values())
        {
            if (variable.isDetector())
            {
                String detectorName = variable.toString(EnumSet.of(PrintFlags.ID));
                int detectorNumber = variable.getStream() * 10 + detectorName.charAt(detectorName.length() - 1) - '0';
                TrafficLightSensor sensor = null;
                for (TrafficLightSensor tls : sensors)
                {
                    if (tls.getId().endsWith(detectorName))
                    {
                        sensor = tls;
                    }
                }
                if (null == sensor)
                {
                    throw new TrafficControlException("Cannot find detector " + detectorName + " with number " + detectorNumber
                            + " among the provided sensors");
                }
                variable.subscribeToDetector(sensor);
            }
        }
        // System.out.println("Installed " + this.variables.size() + " variables");
        // for (String key : this.variables.keySet())
        // {
        // Variable v = this.variables.get(key);
        // System.out.println(key
        // + ":\t"
        // + v.toString(EnumSet.of(PrintFlags.ID, PrintFlags.VALUE, PrintFlags.INITTIMER, PrintFlags.REINITTIMER,
        // PrintFlags.S, PrintFlags.E)));
        // }
    }

    /**
     * Construct the display of this TrafCOD machine and connect the displayed traffic lights and sensors to this TrafCOD
     * machine.
     * @param tfgFileURL String; the URL where the display information is to be read from
     * @param sensors Set&lt;TrafficLightSensor&gt;; the traffic light sensors
     * @return TrafCODDisplay, or null when the display information could not be read, was incomplete, or invalid
     * @throws TrafficControlException when the tfg file could not be read or is invalid
     */
    private TrafCODDisplay makeDisplay(final String tfgFileURL, Set<TrafficLightSensor> sensors) throws TrafficControlException
    {
        TrafCODDisplay result = null;
        boolean useFirstCoordinates = true;
        try
        {
            BufferedReader mapReader = new BufferedReader(new InputStreamReader(new URL(tfgFileURL).openStream()));
            int lineno = 0;
            String inputLine;
            while ((inputLine = mapReader.readLine()) != null)
            {
                ++lineno;
                inputLine = inputLine.trim();
                if (inputLine.startsWith("mapfile="))
                {
                    // System.out.println("map file description is " + inputLine);
                    String mapFileURL = tfgFileURL;
                    // Make a decent attempt at constructing the URL of the map file
                    int pos = mapFileURL.length();
                    while (--pos > 0)
                    {
                        char c = mapFileURL.charAt(pos);
                        if ('/' == c || '\\' == c || ':' == c)
                        {
                            pos++;
                            break;
                        }
                    }
                    mapFileURL = mapFileURL.substring(0, pos) + inputLine.substring(8);
                    BufferedImage image = ImageIO.read(new URL(mapFileURL));
                    result = new TrafCODDisplay(image);
                    if (mapFileURL.matches("[Bb][Mm][Pp]|[Pp][Nn][Gg]$"))
                    {
                        useFirstCoordinates = false;
                    }
                }
                else if (inputLine.startsWith("light="))
                {
                    if (null == result)
                    {
                        throw new TrafficControlException("tfg file defines light before mapfile");
                    }
                    // Extract the stream number
                    int streamNumber;
                    try
                    {
                        streamNumber = Integer.parseInt(inputLine.substring(6, 8));
                    }
                    catch (NumberFormatException nfe)
                    {
                        throw new TrafficControlException("Bad traffic light number in tfg file: " + inputLine);
                    }
                    // Extract the coordinates and create the image
                    TrafficLightImage tli =
                            new TrafficLightImage(result, getCoordinates(inputLine.substring(9), useFirstCoordinates),
                                    String.format("Traffic Light %02d", streamNumber));
                    for (Variable v : this.variablesInDefinitionOrder)
                    {
                        if (v.isOutput() && v.getStream() == streamNumber)
                        {
                            v.addOutput(tli);
                        }
                    }
                }
                else if (inputLine.startsWith("detector="))
                {
                    if (null == result)
                    {
                        throw new TrafficControlException("tfg file defines detector before mapfile");
                    }

                    int detectorStream;
                    int detectorSubNumber;
                    try
                    {
                        detectorStream = Integer.parseInt(inputLine.substring(9, 11));
                        detectorSubNumber = Integer.parseInt(inputLine.substring(12, 13));
                    }
                    catch (NumberFormatException nfe)
                    {
                        throw new TrafficControlException("Cannot parse detector number in " + inputLine);
                    }
                    String detectorName = String.format("D%02d%d", detectorStream, detectorSubNumber);
                    Variable detectorVariable = this.variables.get(detectorName);
                    if (null == detectorVariable)
                    {
                        throw new TrafficControlException("tfg file defines detector " + detectorName
                                + " which does not exist in the TrafCOD program");
                    }
                    DetectorImage di =
                            new DetectorImage(result, getCoordinates(inputLine.substring(14), useFirstCoordinates),
                                    String.format("Detector %02d%d", detectorStream, detectorSubNumber));
                    TrafficLightSensor sensor = null;
                    for (TrafficLightSensor tls : sensors)
                    {
                        if (tls.getId().endsWith(detectorName))
                        {
                            sensor = tls;
                        }
                    }
                    if (null == sensor)
                    {
                        throw new TrafficControlException("Cannot find detector " + detectorName + " with number "
                                + detectorName + " among the provided sensors");
                    }
                    sensor.addListener(di, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT);
                    sensor.addListener(di, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT);
                }
                else
                {
                    System.out.println("Ignoring tfg line(" + lineno + ") \"" + inputLine + "\"");
                }
            }
        }
        catch (IOException e)
        {
            throw new TrafficControlException(e);
        }
        return result;
    }

    /**
     * Extract two coordinates from a line of text.
     * @param line String; the text
     * @param useFirstCoordinates boolean; if true; process the first pair of integer values; if false; use the second pair of
     *            integer values
     * @return Point2D
     * @throws TrafficControlException when the coordinates could not be parsed
     */
    private Point2D getCoordinates(final String line, final boolean useFirstCoordinates) throws TrafficControlException
    {
        String work = line.replaceAll("[ ,\t]+", "\t").trim();
        int x;
        int y;
        String[] fields = work.split("\t");
        if (fields.length < (useFirstCoordinates ? 2 : 4))
        {
            throw new TrafficControlException("not enough fields in tfg line \"" + line + "\"");
        }
        try
        {
            x = Integer.parseInt(fields[useFirstCoordinates ? 0 : 2]);
            y = Integer.parseInt(fields[useFirstCoordinates ? 1 : 3]);
        }
        catch (NumberFormatException nfe)
        {
            throw new TrafficControlException("Bad value in tfg line \"" + line + "\"");
        }
        return new Point2D.Double(x, y);
    }

    /**
     * Decrement all running timers.
     * @return int; the total number of timers that expired
     * @throws TrafficControlException Should never happen
     */
    private int decrementTimers() throws TrafficControlException
    {
        // System.out.println("Decrement running timers");
        int changeCount = 0;
        for (Variable v : this.variables.values())
        {
            if (v.isTimer() && v.getValue() > 0 && v.decrementTimer(this.currentTime10))
            {
                changeCount++;
            }
        }
        return changeCount;
    }

    /**
     * Evaluate all expressions until no more changes occur.
     * @throws TrafficControlException when evaluation of a rule fails
     * @throws SimRuntimeException when scheduling the next evaluation fails
     */
    @SuppressWarnings("unused")
    private void evalExprs() throws TrafficControlException, SimRuntimeException
    {
        System.out.println("evalExprs: time is " + EngineeringFormatter.format(this.simulator.getSimulatorTime().get().si));
        // insert some delay for testing; without this the simulation runs too fast
        // try
        // {
        // Thread.sleep(10);
        // }
        // catch (InterruptedException exception)
        // {
        // System.out.println("Sleep in evalExprs was interrupted");
        // // exception.printStackTrace();
        // }
        // Contrary to the C++ builder version; this implementation decrements the times at the start of evalExprs
        // By doing it before updating this.currentTime10; the debugging output should be very similar
        decrementTimers();
        this.currentTime10 = (int) (this.simulator.getSimulatorTime().get().si * 10);
        int loop;
        for (loop = 0; loop < this.maxLoopCount; loop++)
        {
            if (evalExpressionsOnce() == 0)
            {
                break;
            }
        }
        System.out.println("Executed " + (loop + 1) + " iteration(s)");
        this.simulator.scheduleEventRel(EVALUATION_INTERVAL, this, this, "evalExprs", null);
    }

    /**
     * Evaluate all expressions and return the number of changed variables.
     * @return int; the number of changed variables
     * @throws TrafficControlException when evaluation of a rule fails
     */
    private int evalExpressionsOnce() throws TrafficControlException
    {
        for (Variable variable : this.variables.values())
        {
            variable.clearChangedFlag();
        }
        int changeCount = 0;
        for (Object[] rule : this.tokenisedRules)
        {
            if (evalRule(rule))
            {
                changeCount++;
            }
        }
        return changeCount;
    }

    /**
     * Evaluate a rule.
     * @param rule Object[]; the tokenised rule
     * @return boolean; true if the variable that is affected by the rule has changed; false if no variable was changed
     * @throws TrafficControlException when evaluation of the rule fails
     */
    private boolean evalRule(final Object[] rule) throws TrafficControlException
    {
        boolean result = false;
        Token ruleType = (Token) rule[0];
        Variable destination = (Variable) rule[1];
        if (destination.isTimer())
        {
            if (destination.getFlags().contains(Flags.TIMEREXPIRED))
            {
                destination.clearFlag(Flags.TIMEREXPIRED);
                destination.setFlag(Flags.END);
            }
            else if (destination.getFlags().contains(Flags.START) || destination.getFlags().contains(Flags.END))
            {
                destination.clearFlag(Flags.START);
                destination.clearFlag(Flags.END);
                destination.setFlag(Flags.CHANGED);
            }
        }
        else
        {
            // Normal Variable or detector
            if (Token.START_RULE == ruleType)
            {
                destination.clearFlag(Flags.START);
            }
            else if (Token.END_RULE == ruleType)
            {
                destination.clearFlag(Flags.END);
            }
            else
            {
                destination.clearFlag(Flags.START);
                destination.clearFlag(Flags.END);
            }
        }

        int currentValue = destination.getValue();
        if (Token.START_RULE == ruleType && currentValue != 0 || Token.END == ruleType && currentValue == 0
                || Token.INIT_TIMER == ruleType && currentValue != 0)
        {
            return false; // Value cannot change from zero to nonzero or vice versa due to evaluating the expression
        }
        this.currentRule = rule;
        this.currentToken = 2; // Point to first token of the RHS
        this.stack.clear();
        evalExpr(0);
        if (this.currentToken < this.currentRule.length && Token.CLOSE_PAREN == this.currentRule[this.currentToken])
        {
            throw new TrafficControlException("Too many closing parentheses");
        }
        int resultValue = pop();
        if (Token.END_RULE == ruleType)
        {
            // Invert the result
            if (0 == resultValue)
            {
                resultValue = destination.getValue(); // preserve the current value
            }
            else
            {
                resultValue = 0;
            }
        }
        if (resultValue != 0 && destination.getValue() == 0)
        {
            destination.setFlag(Flags.START);
        }
        else if (resultValue == 0 && destination.getValue() != 0)
        {
            destination.setFlag(Flags.END);
        }
        if (destination.isTimer())
        {
            if (resultValue != 0 && Token.END_RULE != ruleType)
            {
                if (destination.getValue() == 0)
                {
                    result = true;
                }
                int timerValue10 = destination.getTimerMax();
                if (timerValue10 < 1)
                {
                    // Cheat; ensure it will property expire on the next timer tick
                    timerValue10 = 1;
                }
                if (destination.getValue() != timerValue10)
                {
                    result = true;
                }
                destination.setValue(timerValue10, this.currentTime10, new CausePrinter(rule));
            }
            else if (0 == resultValue && Token.END_RULE == ruleType && destination.getValue() != 0)
            {
                result = true;
                destination.setValue(0, this.currentTime10, new CausePrinter(rule));
            }
        }
        else if (destination.getValue() != resultValue)
        {
            result = true;
            destination.setValue(resultValue, this.currentTime10, new CausePrinter(rule));
            if (destination.isOutput())
            {
                fireEvent(TRAFFIC_LIGHT_CHANGED,
                        new Object[] { this.controllerName, destination.getStartSource(), destination.getColor() });
            }
        }
        return result;
    }

    /** Binding strength of relational operators. */
    private static final int BIND_RELATIONAL_OPERATOR = 1;

    /** Binding strength of addition and subtraction. */
    private static final int BIND_ADDITION = 2;

    /** Binding strength of multiplication and division. */
    private static final int BIND_MULTIPLY = 3;

    /** Binding strength of unary minus. */
    private static int BIND_UNARY_MINUS = 4;

    /**
     * Evaluate an expression. <br>
     * The methods evalExpr and evalRHS together evaluate an expression. This is done using recursion and a stack. The argument
     * bindingStrength that is passed around is the binding strength of the last preceding pending operator. if a binary
     * operator with the same or a lower strength is encountered, the pending operator must be applied first. On the other hand
     * of a binary operator with higher binding strength is encountered, that operator takes precedence over the pending
     * operator. To evaluate an expression, call evalExpr with a bindingStrength value of 0. On return verify that currentToken
     * has incremented to the end of the expression and that there is one value (the result) on the stack.
     * @param bindingStrength int; the binding strength of a not yet applied binary operator (higher value must be applied
     *            first)
     * @throws TrafficControlException when the expression is not valid
     */
    private void evalExpr(final int bindingStrength) throws TrafficControlException
    {
        if (this.currentToken >= this.currentRule.length)
        {
            throw new TrafficControlException("Missing operand at end of expression " + printRule(this.currentRule, false));
        }
        Token token = (Token) this.currentRule[this.currentToken++];
        Object nextToken = null;
        if (this.currentToken < this.currentRule.length)
        {
            nextToken = this.currentRule[this.currentToken];
        }
        switch (token)
        {
            case UNARY_MINUS:
                if (Token.OPEN_PAREN != nextToken && Token.VARIABLE != nextToken && Token.NEG_VARIABLE != nextToken
                        && Token.CONSTANT != nextToken && Token.START != nextToken && Token.END != nextToken)
                {
                    throw new TrafficControlException("Operand expected after unary minus");
                }
                evalExpr(BIND_UNARY_MINUS);
                push(-pop());
                break;

            case OPEN_PAREN:
                evalExpr(0);
                if (Token.CLOSE_PAREN != this.currentRule[this.currentToken])
                {
                    throw new TrafficControlException("Missing closing parenthesis");
                }
                this.currentToken++;
                break;

            case START:
                if (Token.VARIABLE != nextToken || this.currentToken >= this.currentRule.length - 1)
                {
                    throw new TrafficControlException("Missing variable after S");
                }
                nextToken = this.currentRule[++this.currentToken];
                if (!(nextToken instanceof Variable))
                {
                    throw new TrafficControlException("Missing variable after S");
                }
                push(((Variable) nextToken).getFlags().contains(Flags.START) ? 1 : 0);
                this.currentToken++;
                break;

            case END:
                if (Token.VARIABLE != nextToken || this.currentToken >= this.currentRule.length - 1)
                {
                    throw new TrafficControlException("Missing variable after E");
                }
                nextToken = this.currentRule[++this.currentToken];
                if (!(nextToken instanceof Variable))
                {
                    throw new TrafficControlException("Missing variable after E");
                }
                push(((Variable) nextToken).getFlags().contains(Flags.END) ? 1 : 0);
                this.currentToken++;
                break;

            case VARIABLE:
            {
                Variable operand = (Variable) nextToken;
                if (operand.isTimer())
                {
                    push(operand.getValue() == 0 ? 0 : 1);
                }
                else
                {
                    push(operand.getValue());
                }
                this.currentToken++;
                break;
            }

            case CONSTANT:
                push((Integer) nextToken);
                this.currentToken++;
                break;

            case NEG_VARIABLE:
                Variable operand = (Variable) nextToken;
                push(operand.getValue() == 0 ? 1 : 0);
                this.currentToken++;
                break;

            default:
                throw new TrafficControlException("Operand missing");
        }
        evalRHS(bindingStrength);
    }

    /**
     * Evaluate the right-hand-side of an expression.
     * @param bindingStrength int; the binding strength of the most recent, not yet applied, binary operator
     * @throws TrafficControlException when the RHS of an expression is invalid
     */
    private void evalRHS(final int bindingStrength) throws TrafficControlException
    {
        while (true)
        {
            if (this.currentToken >= this.currentRule.length)
            {
                return;
            }
            Token token = (Token) this.currentRule[this.currentToken];
            switch (token)
            {
                case CLOSE_PAREN:
                    return;

                case TIMES:
                    if (BIND_MULTIPLY <= bindingStrength)
                    {
                        return; // apply pending operator now
                    }
                    /*-
                     * apply pending operator later 
                     * 1: evaluate the RHS operand. 
                     * 2: multiply the top-most two operands on the stack and push the result on the stack.
                     */
                    this.currentToken++;
                    evalExpr(BIND_MULTIPLY);
                    push(pop() * pop() == 0 ? 0 : 1);
                    break;

                case EQ:
                case NOTEQ:
                case LE:
                case LEEQ:
                case GT:
                case GTEQ:
                    if (BIND_RELATIONAL_OPERATOR <= bindingStrength)
                    {
                        return; // apply pending operator now
                    }
                    /*-
                     * apply pending operator later 
                     * 1: evaluate the RHS operand. 
                     * 2: compare the top-most two operands on the stack and push the result on the stack.
                     */
                    this.currentToken++;
                    evalExpr(BIND_RELATIONAL_OPERATOR);
                    switch (token)
                    {
                        case EQ:
                            push(pop() == pop() ? 1 : 0);
                            break;

                        case NOTEQ:
                            push(pop() != pop() ? 1 : 0);
                            break;

                        case GT:
                            push(pop() < pop() ? 1 : 0);
                            break;

                        case GTEQ:
                            push(pop() <= pop() ? 1 : 0);
                            break;

                        case LE:
                            push(pop() > pop() ? 1 : 0);
                            break;

                        case LEEQ:
                            push(pop() >= pop() ? 1 : 0);
                            break;

                        default:
                            throw new TrafficControlException("Bad relational operator");
                    }
                    break;

                case PLUS:
                    if (BIND_ADDITION <= bindingStrength)
                    {
                        return; // apply pending operator now
                    }
                    /*-
                     * apply pending operator later 
                     * 1: evaluate the RHS operand. 
                     * 2: add (OR) the top-most two operands on the stack and push the result on the stack.
                     */
                    this.currentToken++;
                    evalExpr(BIND_ADDITION);
                    push(pop() + pop() == 0 ? 0 : 1);
                    break;

                case MINUS:
                    if (BIND_ADDITION <= bindingStrength)
                    {
                        return; // apply pending operator now
                    }
                    /*-
                     * apply pending operator later 
                     * 1: evaluate the RHS operand. 
                     * 2: subtract the top-most two operands on the stack and push the result on the stack.
                     */
                    this.currentToken++;
                    evalExpr(BIND_ADDITION);
                    push(-pop() + pop());
                    break;

                default:
                    throw new TrafficControlException("Missing binary operator");
            }
        }
    }

    /**
     * Push a value on the evaluation stack.
     * @param value int; the value to push on the evaluation stack
     */
    private void push(final int value)
    {
        this.stack.add(value);
    }

    /**
     * Remove the last not-yet-removed value from the evaluation stack and return it.
     * @return int; the last non-yet-removed value on the evaluation stack
     * @throws TrafficControlException when the stack is empty
     */
    private int pop() throws TrafficControlException
    {
        if (this.stack.size() < 1)
        {
            throw new TrafficControlException("Stack empty");
        }
        return this.stack.remove(this.stack.size() - 1);
    }

    /**
     * Print a tokenized rule.
     * @param tokens Object[]; the tokens
     * @param printValues boolean; if true; print the values of all encountered variable; if false; do not print the values of
     *            all encountered variable
     * @return String; a textual approximation of the original rule
     * @throws TrafficControlException when tokens does not match the expected grammar
     */
    static String printRule(Object[] tokens, final boolean printValues) throws TrafficControlException
    {
        EnumSet<PrintFlags> variableFlags = EnumSet.of(PrintFlags.ID);
        if (printValues)
        {
            variableFlags.add(PrintFlags.VALUE);
        }
        EnumSet<PrintFlags> negatedVariableFlags = EnumSet.copyOf(variableFlags);
        negatedVariableFlags.add(PrintFlags.NEGATED);
        StringBuilder result = new StringBuilder();
        for (int inPos = 0; inPos < tokens.length; inPos++)
        {
            Object token = tokens[inPos];
            if (token instanceof Token)
            {
                switch ((Token) token)
                {
                    case EQUALS_RULE:
                        result.append(((Variable) tokens[++inPos]).toString(variableFlags));
                        result.append("=");
                        break;

                    case NEG_EQUALS_RULE:
                        result.append(((Variable) tokens[++inPos]).toString(negatedVariableFlags));
                        result.append("=");
                        break;

                    case START_RULE:
                        result.append(((Variable) tokens[++inPos]).toString(variableFlags));
                        result.append(".=");
                        break;

                    case END_RULE:
                        result.append(((Variable) tokens[++inPos]).toString(variableFlags));
                        result.append("N.=");
                        break;

                    case INIT_TIMER:
                        result.append(((Variable) tokens[++inPos]).toString(EnumSet.of(PrintFlags.ID, PrintFlags.INITTIMER)));
                        result.append(".=");
                        break;

                    case REINIT_TIMER:
                        result.append(((Variable) tokens[++inPos]).toString(EnumSet.of(PrintFlags.ID, PrintFlags.REINITTIMER)));
                        result.append(".=");
                        break;

                    case START:
                        result.append("S");
                        break;

                    case END:
                        result.append("E");
                        break;

                    case VARIABLE:
                        result.append(((Variable) tokens[++inPos]).toString(variableFlags));
                        break;

                    case NEG_VARIABLE:
                        result.append(((Variable) tokens[++inPos]).toString(variableFlags));
                        result.append("N");
                        break;

                    case CONSTANT:
                        result.append(tokens[++inPos]).toString();
                        break;

                    case UNARY_MINUS:
                    case MINUS:
                        result.append("-");
                        break;

                    case PLUS:
                        result.append("+");
                        break;

                    case TIMES:
                        result.append(".");
                        break;

                    case EQ:
                        result.append("=");
                        break;

                    case NOTEQ:
                        result.append("<>");
                        break;

                    case GT:
                        result.append(">");
                        break;

                    case GTEQ:
                        result.append(">=");
                        break;

                    case LE:
                        result.append("<");
                        break;

                    case LEEQ:
                        result.append("<=");
                        break;

                    case OPEN_PAREN:
                        result.append("(");
                        break;

                    case CLOSE_PAREN:
                        result.append(")");
                        break;

                    default:
                        System.out.println("<<<ERROR>>> encountered a non-Token object: " + token + " after "
                                + result.toString());
                        throw new TrafficControlException("Unknown token");
                }
            }
            else
            {
                System.out.println("<<<ERROR>>> encountered a non-Token object: " + token + " after " + result.toString());
                throw new TrafficControlException("Not a token");
            }
        }
        return result.toString();
    }

    /**
     * States of the rule parser.
     * <p>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    enum ParserState
    {
        /** Looking for the left hand side of an assignment. */
        FIND_LHS,
        /** Looking for an assignment operator. */
        FIND_ASSIGN,
        /** Looking for the right hand side of an assignment. */
        FIND_RHS,
        /** Looking for an optional unary minus. */
        MAY_UMINUS,
        /** Looking for an expression. */
        FIND_EXPR,
    }

    /**
     * Types of TrafCOD tokens.
     * <p>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    enum Token
    {
        /** Equals rule. */
        EQUALS_RULE,
        /** Not equals rule. */
        NEG_EQUALS_RULE,
        /** Assignment rule. */
        ASSIGNMENT,
        /** Start rule. */
        START_RULE,
        /** End rule. */
        END_RULE,
        /** Timer initialize rule. */
        INIT_TIMER,
        /** Timer re-initialize rule. */
        REINIT_TIMER,
        /** Unary minus operator. */
        UNARY_MINUS,
        /** Less than or equal to (&lt;=). */
        LEEQ,
        /** Not equal to (!=). */
        NOTEQ,
        /** Less than (&lt;). */
        LE,
        /** Greater than or equal to (&gt;=). */
        GTEQ,
        /** Greater than (&gt;). */
        GT,
        /** Equals to (=). */
        EQ,
        /** True if following variable has just started. */
        START,
        /** True if following variable has just ended. */
        END,
        /** Variable follows. */
        VARIABLE,
        /** Variable that follows must be logically negated. */
        NEG_VARIABLE,
        /** Integer follows. */
        CONSTANT,
        /** Addition operator. */
        PLUS,
        /** Subtraction operator. */
        MINUS,
        /** Multiplication operator. */
        TIMES,
        /** Opening parenthesis. */
        OPEN_PAREN,
        /** Closing parenthesis. */
        CLOSE_PAREN,
    }

    /**
     * Parse one TrafCOD rule.
     * @param rawRule String; the TrafCOD rule
     * @param locationDescription String; description of the location (file, line) where the rule was found
     * @return Object[]; array filled with the tokenized rule
     * @throws TrafficControlException when the rule is not a valid TrafCOD rule
     */
    private Object[] parse(final String rawRule, final String locationDescription) throws TrafficControlException
    {
        if (rawRule.length() == 0)
        {
            throw new TrafficControlException("empty rule at " + locationDescription);
        }
        ParserState state = ParserState.FIND_LHS;
        String rule = rawRule.toUpperCase(Locale.US);
        Token ruleType = Token.ASSIGNMENT;
        int inPos = 0;
        NameAndStream lhsNameAndStream = null;
        List<Object> tokens = new ArrayList<>();
        while (inPos < rule.length())
        {
            char character = rule.charAt(inPos);
            if (Character.isWhitespace(character))
            {
                inPos++;
                continue;
            }
            switch (state)
            {
                case FIND_LHS:
                {
                    if ('S' == character)
                    {
                        ruleType = Token.START_RULE;
                        inPos++;
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('E' == character)
                    {
                        ruleType = Token.END_RULE;
                        inPos++;
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('I' == character && 'T' == rule.charAt(inPos + 1))
                    {
                        ruleType = Token.INIT_TIMER;
                        inPos++; // The 'T' is part of the name of the time; do not consume it
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('R' == character && 'I' == rule.charAt(inPos + 1) && 'T' == rule.charAt(inPos + 2))
                    {
                        ruleType = Token.REINIT_TIMER;
                        inPos += 2; // The 'T' is part of the name of the timer; do not consume it
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('T' == character && rule.indexOf('=') >= 0
                            && (rule.indexOf('N') < 0 || rule.indexOf('N') > rule.indexOf('=')))
                    {
                        throw new TrafficControlException("Bad time initialization at " + locationDescription);
                    }
                    else
                    {
                        ruleType = Token.EQUALS_RULE;
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                        if (lhsNameAndStream.isNegated())
                        {
                            ruleType = Token.NEG_EQUALS_RULE;
                        }
                    }
                    state = ParserState.FIND_ASSIGN;
                    break;
                }

                case FIND_ASSIGN:
                {
                    if ('.' == character && '=' == rule.charAt(inPos + 1))
                    {
                        if (Token.EQUALS_RULE == ruleType)
                        {
                            ruleType = Token.START_RULE;
                        }
                        else if (Token.NEG_EQUALS_RULE == ruleType)
                        {
                            ruleType = Token.END_RULE;
                        }
                        inPos += 2;
                    }
                    else if ('=' == character)
                    {
                        if (Token.START_RULE == ruleType || Token.END_RULE == ruleType || Token.INIT_TIMER == ruleType
                                || Token.REINIT_TIMER == ruleType)
                        {
                            throw new TrafficControlException("Bad assignment at " + locationDescription);
                        }
                        inPos++;
                    }
                    tokens.add(ruleType);
                    EnumSet<Flags> lhsFlags = EnumSet.noneOf(Flags.class);
                    if (Token.START_RULE == ruleType || Token.EQUALS_RULE == ruleType || Token.NEG_EQUALS_RULE == ruleType
                            || Token.INIT_TIMER == ruleType || Token.REINIT_TIMER == ruleType)
                    {
                        lhsFlags.add(Flags.HAS_START_RULE);
                    }
                    if (Token.END_RULE == ruleType || Token.EQUALS_RULE == ruleType || Token.NEG_EQUALS_RULE == ruleType)
                    {
                        lhsFlags.add(Flags.HAS_END_RULE);
                    }
                    Variable lhsVariable =
                            installVariable(lhsNameAndStream.getName(), lhsNameAndStream.getStream(), lhsFlags,
                                    locationDescription);
                    tokens.add(lhsVariable);
                    state = ParserState.MAY_UMINUS;
                    break;
                }

                case MAY_UMINUS:
                    if ('-' == character)
                    {
                        tokens.add(Token.UNARY_MINUS);
                        inPos++;
                    }
                    state = ParserState.FIND_EXPR;
                    break;

                case FIND_EXPR:
                {
                    if (Character.isDigit(character))
                    {
                        int constValue = 0;
                        while (inPos < rule.length() && Character.isDigit(rule.charAt(inPos)))
                        {
                            int digit = rule.charAt(inPos) - '0';
                            if (constValue >= (Integer.MAX_VALUE - digit) / 10)
                            {
                                throw new TrafficControlException("Number too large at " + locationDescription);
                            }
                            constValue = 10 * constValue + digit;
                            inPos++;
                        }
                        tokens.add(Token.CONSTANT);
                        tokens.add(new Integer(constValue));
                    }
                    if (inPos >= rule.length())
                    {
                        return tokens.toArray();
                    }
                    character = rule.charAt(inPos);
                    switch (character)
                    {
                        case '+':
                            tokens.add(Token.PLUS);
                            inPos++;
                            break;

                        case '-':
                            tokens.add(Token.MINUS);
                            inPos++;
                            break;

                        case '.':
                            tokens.add(Token.TIMES);
                            inPos++;
                            break;

                        case ')':
                            tokens.add(Token.CLOSE_PAREN);
                            inPos++;
                            break;

                        case '<':
                        {
                            Character nextChar = rule.charAt(++inPos);
                            if ('=' == nextChar)
                            {
                                tokens.add(Token.LEEQ);
                                inPos++;
                            }
                            else if ('>' == nextChar)
                            {
                                tokens.add(Token.NOTEQ);
                                inPos++;
                            }
                            else
                            {
                                tokens.add(Token.LE);
                            }
                            break;
                        }

                        case '>':
                        {
                            Character nextChar = rule.charAt(++inPos);
                            if ('=' == nextChar)
                            {
                                tokens.add(Token.GTEQ);
                                inPos++;
                            }
                            else if ('<' == nextChar)
                            {
                                tokens.add(Token.NOTEQ);
                                inPos++;
                            }
                            else
                            {
                                tokens.add(Token.GT);
                            }
                            break;
                        }

                        case '=':
                        {
                            Character nextChar = rule.charAt(++inPos);
                            if ('<' == nextChar)
                            {
                                tokens.add(Token.LEEQ);
                                inPos++;
                            }
                            else if ('>' == nextChar)
                            {
                                tokens.add(Token.GTEQ);
                                inPos++;
                            }
                            else
                            {
                                tokens.add(Token.EQ);
                            }
                            break;
                        }

                        case '(':
                        {
                            inPos++;
                            tokens.add(Token.OPEN_PAREN);
                            state = ParserState.MAY_UMINUS;
                            break;
                        }

                        default:
                        {
                            if ('S' == character)
                            {
                                tokens.add(Token.START);
                                inPos++;
                            }
                            else if ('E' == character)
                            {
                                tokens.add(Token.END);
                                inPos++;
                            }
                            NameAndStream nas = new NameAndStream(rule.substring(inPos), locationDescription);
                            inPos += nas.getNumberOfChars();
                            if (nas.isNegated())
                            {
                                tokens.add(Token.NEG_VARIABLE);
                            }
                            else
                            {
                                tokens.add(Token.VARIABLE);
                            }
                            Variable variable =
                                    installVariable(nas.getName(), nas.getStream(), EnumSet.noneOf(Flags.class),
                                            locationDescription);
                            variable.incrementReferenceCount();
                            tokens.add(variable);
                        }
                    }
                    break;
                }
                default:
                    throw new TrafficControlException("Error: bad switch; case " + state + " should not happen");
            }
        }
        return tokens.toArray();
    }

    /**
     * Check if a String begins with the text of a supplied String (ignoring case).
     * @param sought String; the sought pattern (NOT a regular expression)
     * @param supplied String; the String that might start with the sought string
     * @return boolean; true if the supplied String begins with the sought String (case insensitive)
     */
    private boolean stringBeginsWithIgnoreCase(final String sought, final String supplied)
    {
        if (sought.length() > supplied.length())
        {
            return false;
        }
        return (sought.equalsIgnoreCase(supplied.substring(0, sought.length())));
    }

    /**
     * Generate the key for a variable name and stream for use in this.variables.
     * @param name String; name of the variable
     * @param stream short; stream of the variable
     * @return String
     */
    private String variableKey(final String name, final short stream)
    {
        if (name.startsWith("D"))
        {
            return String.format("D%02d%s", stream, name.substring(1));
        }
        return String.format("%s%02d", name.toUpperCase(Locale.US), stream);
    }

    /**
     * Lookup or create a new Variable.
     * @param name String; name of the variable
     * @param stream short; stream number of the variable
     * @param flags EnumSet&lt;Flags&gt;; some (possibly empty) combination of Flags.HAS_START_RULE and Flags.HAS_END_RULE; no
     *            other flags are allowed
     * @param location String; description of the location in the TrafCOD file that triggered the call to this method
     * @return Variable; the new (or already existing) variable
     * @throws TrafficControlException if the variable already exists and already has (one of) the specified flag(s)
     */
    private Variable installVariable(String name, short stream, EnumSet<Flags> flags, String location)
            throws TrafficControlException
    {
        EnumSet<Flags> forbidden = EnumSet.complementOf(EnumSet.of(Flags.HAS_START_RULE, Flags.HAS_END_RULE));
        EnumSet<Flags> badFlags = EnumSet.copyOf(forbidden);
        badFlags.retainAll(flags);
        if (badFlags.size() > 0)
        {
            throw new TrafficControlException("installVariable was called with wrong flag(s): " + badFlags);
        }
        String key = variableKey(name, stream);
        Variable variable = this.variables.get(key);
        if (null == variable)
        {
            // Create and install a new variable
            variable = new Variable(name, stream);
            this.variables.put(key, variable);
            this.variablesInDefinitionOrder.add(variable);
            if (variable.isDetector())
            {
                this.detectors.put(key, variable);
            }
        }
        if (flags.contains(Flags.HAS_START_RULE))
        {
            variable.setStartSource(location);
        }
        if (flags.contains(Flags.HAS_END_RULE))
        {
            variable.setEndSource(location);
        }
        return variable;
    }

    /**
     * Retrieve the simulator.
     * @return SimulatorInterface&lt;Time, Duration, OTSSimTimeDouble&gt;
     */
    protected SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()
    {
        return this.simulator;
    }

    /**
     * Retrieve the structure number.
     * @return int; the structureNumber
     */
    public int getStructureNumber()
    {
        return this.structureNumber;
    }

    /** {@inheritDoc} */
    @Override
    public void updateDetector(String detectorId, boolean detectingGTU)
    {
        Variable detector = this.detectors.get(detectorId);
        detector.setValue(
                detectingGTU ? 1 : 0,
                this.currentTime10,
                new CausePrinter(String.format("Detector %s becoming %s", detectorId,
                        (detectingGTU ? "occupied" : "unoccupied"))));
    }

    /**
     * Switch tracing of all variables of a particular traffic stream, or all variables that do not have an associated traffic
     * stream on or off.
     * @param stream int; the traffic stream number, or <code>TrafCOD.NO_STREAM</code> to affect all variables that do not have
     *            an associated traffic stream
     * @param trace boolean; if true; switch on tracing; if false; switch off tracing
     */
    public void traceVariablesOfStream(final int stream, final boolean trace)
    {
        for (Variable v : this.variablesInDefinitionOrder)
        {
            if (v.getStream() == stream)
            {
                if (trace)
                {
                    v.setFlag(Flags.TRACED);
                }
                else
                {
                    v.clearFlag(Flags.TRACED);
                }
            }
        }
    }

    /**
     * Switch tracing of one variable on or off.
     * @param variableName String; name of the variable
     * @param stream int; traffic stream of the variable, or <code>TrafCOD.NO_STREAM</code> to select a variable that does not
     *            have an associated traffic stream
     * @param trace boolean; if true; switch on tracing; if false; switch off tracing
     */
    public void traceVariable(final String variableName, final int stream, final boolean trace)
    {
        for (Variable v : this.variablesInDefinitionOrder)
        {
            if (v.getStream() == stream && variableName.equals(v.getName()))
            {
                if (trace)
                {
                    v.setFlag(Flags.TRACED);
                }
                else
                {
                    v.clearFlag(Flags.TRACED);
                }
            }
        }
    }

}

/**
 * Store a variable name, stream, isTimer, isNegated and number characters consumed information.
 */
class NameAndStream
{
    /** The name. */
    private final String name;

    /** The stream number. */
    private short stream = TrafCOD.NO_STREAM;

    /** Number characters parsed. */
    private int numberOfChars = 0;

    /** Was a letter N consumed while parsing the name?. */
    private boolean negated = false;

    /**
     * Parse a TrafCOD identifier and extract all required information.
     * @param text String; the TrafCOD identifier (may be followed by more text)
     * @param locationDescription String; description of the location in the input file
     * @throws TrafficControlException when text is not a valid TrafCOD variable name
     */
    public NameAndStream(final String text, final String locationDescription) throws TrafficControlException
    {
        int pos = 0;
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
        {
            pos++;
        }
        while (pos < text.length())
        {
            char character = text.charAt(pos);
            if (!Character.isLetterOrDigit(character))
            {
                break;
            }
            pos++;
        }
        this.numberOfChars = pos;
        String trimmed = text.substring(0, pos).replaceAll(" ", "");
        if (trimmed.length() == 0)
        {
            throw new TrafficControlException("missing variable at " + locationDescription);
        }
        if (trimmed.matches("^D([Nn]?\\d\\d\\d)|(\\d\\d\\d[Nn])"))
        {
            // Handle a detector
            if (trimmed.charAt(1) == 'N' || trimmed.charAt(1) == 'n')
            {
                // Move the 'N' to the end
                trimmed = "D" + trimmed.substring(2, 5) + "N" + trimmed.substring(5);
                this.negated = true;
            }
            this.name = "D" + trimmed.charAt(3);
            this.stream = (short) (10 * (trimmed.charAt(1) - '0') + trimmed.charAt(2) - '0');
            return;
        }
        StringBuilder nameBuilder = new StringBuilder();
        for (pos = 0; pos < trimmed.length(); pos++)
        {
            char nextChar = trimmed.charAt(pos);
            if (pos < trimmed.length() - 1 && Character.isDigit(nextChar) && Character.isDigit(trimmed.charAt(pos + 1))
                    && TrafCOD.NO_STREAM == this.stream)
            {
                if (0 == pos || (1 == pos && trimmed.startsWith("N")))
                {
                    throw new TrafficControlException("Bad variable name: " + trimmed + " at " + locationDescription);
                }
                if (trimmed.charAt(pos - 1) == 'N')
                {
                    // Previous N was NOT part of the name
                    nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                    // Move the 'N' after the digits
                    trimmed =
                            trimmed.substring(0, pos - 1) + trimmed.substring(pos, pos + 2) + trimmed.substring(pos + 2) + "N";
                    pos--;
                }
                this.stream = (short) (10 * (trimmed.charAt(pos) - '0') + trimmed.charAt(pos + 1) - '0');
                pos++;
            }
            else
            {
                nameBuilder.append(nextChar);
            }
        }
        if (trimmed.endsWith("N"))
        {
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
            this.negated = true;
        }
        this.name = nameBuilder.toString();
    }

    /**
     * Was a negation operator ('N') embedded in the name?
     * @return boolean
     */
    public boolean isNegated()
    {
        return this.negated;
    }

    /**
     * Retrieve the stream number.
     * @return short; the stream number
     */
    public short getStream()
    {
        return this.stream;
    }

    /**
     * Retrieve the name.
     * @return String; the name (without the stream number)
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Retrieve the number of characters consumed from the input.
     * @return int; the number of characters consumed from the input
     */
    public int getNumberOfChars()
    {
        return this.numberOfChars;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NameAndStream [name=" + this.name + ", stream=" + this.stream + ", numberOfChars=" + this.numberOfChars
                + ", negated=" + this.negated + "]";
    }

}

/**
 * A TrafCOD variable.
 */
class Variable implements EventListenerInterface
{
    /** Flags. */
    EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

    /** The current value. */
    int value;

    /** Limit value (if this is a timer variable). */
    int timerMax10;

    /** Output color (if this is an export variable). */
    TrafficLightColor color;

    /** Name of this variable (without the traffic stream). */
    final String name;

    /** Position in the debugging list. */
    char listPos;

    /** Traffic stream number */
    final short stream;

    /** Number of rules that refer to this variable. */
    int refCount;

    /** Time of last update in tenth of second. */
    int updateTime10;

    /** Source of start rule. */
    String startSource;

    /** Source of end rule. */
    String endSource;

    /** The traffic light (only set if this Variable is an output(. */
    private Set<TrafficLight> trafficLights;

    /**
     * Construct a new Variable.
     * @param name String; name of the new variable (without the stream number)
     * @param stream short; stream number to which the new Variable is associated
     */
    public Variable(final String name, final short stream)
    {
        this.name = name.toUpperCase(Locale.US);
        this.stream = stream;
        if (this.name.startsWith("T"))
        {
            this.flags.add(Flags.IS_TIMER);
        }
        if (this.name.length() == 2 && this.name.startsWith("D") && Character.isDigit(this.name.charAt(1)))
        {
            this.flags.add(Flags.IS_DETECTOR);
        }
    }

    /**
     * Retrieve the name of this variable.
     * @return String; the name (without the stream number) of this Variable
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Link a detector variable to a sensor.
     * @param sensor TrafficLightSensor; the sensor
     * @throws TrafficControlException when this variable is not a detector
     */
    public void subscribeToDetector(TrafficLightSensor sensor) throws TrafficControlException
    {
        if (!isDetector())
        {
            throw new TrafficControlException("Cannot subscribe a non-detector to a TrafficLightSensor");
        }
        sensor.addListener(this, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT);
        sensor.addListener(this, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT);
    }

    /**
     * Initialize this variable if it has the INITED flag set.
     */
    public void initialize()
    {
        if (this.flags.contains(Flags.INITED))
        {
            if (isTimer())
            {
                setValue(this.timerMax10, 0, new CausePrinter("Timer initialization rule"));
            }
            else
            {
                setValue(1, 0, new CausePrinter("Variable initialization rule"));
            }
        }
    }

    /**
     * Decrement the value of a timer.
     * @param timeStamp10 int; the current simulator time in tenths of a second
     * @return boolean; true if the timer expired due to this call; false if the timer is still running, or expired before this
     *         call
     * @throws TrafficControlException when this Variable is not a timer
     */
    public boolean decrementTimer(final int timeStamp10) throws TrafficControlException
    {
        if (!isTimer())
        {
            throw new TrafficControlException("Variable " + this + " is not a timer");
        }
        if (this.value <= 0)
        {
            return false;
        }
        if (0 == --this.value)
        {
            this.flags.add(Flags.CHANGED);
            this.flags.add(Flags.END);
            this.value = 0;
            this.updateTime10 = timeStamp10;
            if (this.flags.contains(Flags.TRACED))
            {
                System.out.println("Timer " + toString() + " expired");
            }
            return true;
        }
        return false;
    }

    /**
     * Retrieve the color for an output Variable.
     * @return int; the color code for this Variable
     * @throws TrafficControlException if this Variable is not an output
     */
    public TrafficLightColor getColor() throws TrafficControlException
    {
        if (!this.flags.contains(Flags.IS_OUTPUT))
        {
            throw new TrafficControlException("Stream " + this.toString() + "is not an output");
        }
        return this.color;
    }

    /**
     * Report whether a change in this variable must be published.
     * @return boolean; true if this Variable is an output; false if this Variable is not an output
     */
    public boolean isOutput()
    {
        return this.flags.contains(Flags.IS_OUTPUT);
    }

    /**
     * Report if this Variable is a detector.
     * @return boolean; true if this Variable is a detector; false if this Variable is not a detector
     */
    public boolean isDetector()
    {
        return this.flags.contains(Flags.IS_DETECTOR);
    }

    /**
     * @param newValue int; the new value of this Variable
     * @param timeStamp10 int; the time stamp of this update
     * @param cause CausePrinter; rule, timer, or detector that caused the change
     */
    public void setValue(int newValue, int timeStamp10, CausePrinter cause)
    {
        if (this.value != newValue)
        {
            this.updateTime10 = timeStamp10;
            setFlag(Flags.CHANGED);
            if (0 == newValue)
            {
                setFlag(Flags.END);
            }
            else
            {
                setFlag(Flags.START);
            }
            if (isOutput() && newValue != 0)
            {
                for (TrafficLight trafficLight : this.trafficLights)
                {
                    trafficLight.setTrafficLightColor(this.color);
                }
            }
        }
        if (this.flags.contains(Flags.TRACED))
        {
            System.out.println("Variable " + this.name + this.stream + " changes from " + this.value + " to " + newValue
                    + " due to " + cause.toString());
        }
        this.value = newValue;
    }

    /**
     * Retrieve the start value of this timer in units of 0.1 seconds (1 second is represented by the value 10).
     * @return int; the timerMax10 value
     * @throws TrafficControlException when this class is not a Timer
     */
    public int getTimerMax() throws TrafficControlException
    {
        if (!this.isTimer())
        {
            throw new TrafficControlException("This is not a timer");
        }
        return this.timerMax10;
    }

    /**
     * Retrieve the current value of this Variable.
     * @return int; the value of this Variable
     */
    public int getValue()
    {
        return this.value;
    }

    /**
     * Set one flag.
     * @param flag Flags
     */
    public void setFlag(final Flags flag)
    {
        this.flags.add(flag);
    }

    /**
     * Clear one flag.
     * @param flag Flags; the flag to clear
     */
    public void clearFlag(final Flags flag)
    {
        this.flags.remove(flag);
    }

    /**
     * Report whether this Variable is a timer.
     * @return boolean; true if this Variable is a timer; false if this variable is not a timer
     */
    public boolean isTimer()
    {
        return this.flags.contains(Flags.IS_TIMER);
    }

    /**
     * Clear the CHANGED flag of this Variable.
     */
    public void clearChangedFlag()
    {
        this.flags.remove(Flags.CHANGED);
    }

    /**
     * Increment the reference counter of this variable. The reference counter counts the number of rules where this variable
     * occurs on the right hand side of the assignment operator.
     */
    public void incrementReferenceCount()
    {
        this.refCount++;
    }

    /**
     * Return a safe copy of the flags.
     * @return EnumSet&lt;Flags&gt;
     */
    public EnumSet<Flags> getFlags()
    {
        return EnumSet.copyOf(this.flags);
    }

    /**
     * Make this variable an output variable and set the color value.
     * @param colorValue int; the output value (as used in the TrafCOD file)
     * @throws TrafficControlException when the colorValue is invalid, or this method is called more than once for this variable
     */
    public void setOutput(int colorValue) throws TrafficControlException
    {
        if (null != this.color)
        {
            throw new TrafficControlException("setOutput has already been called for " + this);
        }
        if (null == this.trafficLights)
        {
            this.trafficLights = new HashSet<>();
        }
        // Convert the TrafCOD color value to the corresponding TrafficLightColor
        TrafficLightColor newColor;
        switch (colorValue)
        {
            case 'R':
                newColor = TrafficLightColor.RED;
                break;
            case 'G':
                newColor = TrafficLightColor.GREEN;
                break;
            case 'Y':
                newColor = TrafficLightColor.YELLOW;
                break;
            default:
                throw new TrafficControlException("Bad color value: " + colorValue);
        }
        this.color = newColor;
        this.flags.add(Flags.IS_OUTPUT);
    }

    /**
     * Add a traffic light to this variable.
     * @param trafficLight TrafficLight; the traffic light to add
     * @throws TrafficControlException when this variable is not an output
     */
    public void addOutput(final TrafficLight trafficLight) throws TrafficControlException
    {
        if (!this.isOutput())
        {
            throw new TrafficControlException("Cannot add an output to an non-output variable");
        }
        this.trafficLights.add(trafficLight);
    }

    /**
     * Set the maximum time of this timer.
     * @param value10 int; the maximum time in 0.1 s
     * @throws TrafficControlException when this Variable is not a timer
     */
    public void setTimerMax(int value10) throws TrafficControlException
    {
        if (!this.flags.contains(Flags.IS_TIMER))
        {
            throw new TrafficControlException("Cannot set maximum timer value of " + this.toString()
                    + " because this is not a timer");
        }
        this.timerMax10 = value10;
    }

    /**
     * Describe the rule that starts this variable.
     * @return String
     */
    public String getStartSource()
    {
        return this.startSource;
    }

    /**
     * Set the description of the rule that starts this variable.
     * @param startSource String; description of the rule that starts this variable
     * @throws TrafficControlException when a start source has already been set
     */
    public void setStartSource(String startSource) throws TrafficControlException
    {
        if (null != this.startSource)
        {
            throw new TrafficControlException("Conflicting rules: " + this.startSource + " vs " + startSource);
        }
        this.startSource = startSource;
        this.flags.add(Flags.HAS_START_RULE);
    }

    /**
     * Describe the rule that ends this variable.
     * @return String
     */
    public String getEndSource()
    {
        return this.endSource;
    }

    /**
     * Set the description of the rule that ends this variable.
     * @param endSource String; description of the rule that ends this variable
     * @throws TrafficControlException when an end source has already been set
     */
    public void setEndSource(String endSource) throws TrafficControlException
    {
        if (null != this.endSource)
        {
            throw new TrafficControlException("Conflicting rules: " + this.startSource + " vs " + endSource);
        }
        this.endSource = endSource;
        this.flags.add(Flags.HAS_END_RULE);
    }

    /**
     * Retrieve the stream to which this variable belongs.
     * @return short; the stream to which this variable belongs
     */
    public short getStream()
    {
        return this.stream;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Variable [" + toString(EnumSet.of(PrintFlags.ID, PrintFlags.VALUE, PrintFlags.FLAGS)) + "]";
    }

    /**
     * Convert selected fields to a String.
     * @param printFlags EnumSet&lt;PrintFlags&gt;; the set of fields to convert
     * @return String
     */
    public String toString(EnumSet<PrintFlags> printFlags)
    {
        StringBuilder result = new StringBuilder();
        if (printFlags.contains(PrintFlags.ID))
        {
            if (this.flags.contains(Flags.IS_DETECTOR))
            {
                result.append("D");
            }
            else if (isTimer() && printFlags.contains(PrintFlags.INITTIMER))
            {
                result.append("I");
                result.append(this.name);
            }
            else if (isTimer() && printFlags.contains(PrintFlags.REINITTIMER))
            {
                result.append("RI");
                result.append(this.name);
            }
            else
            {
                result.append(this.name);
            }
            if (this.stream > 0)
            {
                // Insert the stream BEFORE the first digit in the name (if any); otherwise append
                int pos;
                for (pos = 0; pos < result.length(); pos++)
                {
                    if (Character.isDigit(result.charAt(pos)))
                    {
                        break;
                    }
                }
                result.insert(pos, String.format("%02d", this.stream));
            }
            if (this.flags.contains(Flags.IS_DETECTOR))
            {
                result.append(this.name.substring(1));
            }
            if (printFlags.contains(PrintFlags.NEGATED))
            {
                result.append("N");
            }
        }
        int printValue = Integer.MIN_VALUE; // That value should stand out if not changed by the code below this line.
        if (printFlags.contains(PrintFlags.VALUE))
        {
            if (printFlags.contains(PrintFlags.NEGATED))
            {
                printValue = 0 == this.value ? 1 : 0;
            }
            else
            {
                printValue = this.value;
            }
            if (printFlags.contains(PrintFlags.S))
            {
                if (this.flags.contains(Flags.START))
                {
                    printValue = 1;
                }
                else
                {
                    printValue = 0;
                }
            }
            if (printFlags.contains(PrintFlags.E))
            {
                if (this.flags.contains(Flags.END))
                {
                    printValue = 1;
                }
                else
                {
                    printValue = 0;
                }
            }
        }
        if (printFlags.contains(PrintFlags.VALUE) || printFlags.contains(PrintFlags.S) || printFlags.contains(PrintFlags.E)
                || printFlags.contains(PrintFlags.FLAGS))
        {
            result.append("<");
            if (printFlags.contains(PrintFlags.VALUE) || printFlags.contains(PrintFlags.S) || printFlags.contains(PrintFlags.E))
            {
                result.append(printValue);
            }
            if (printFlags.contains(PrintFlags.FLAGS))
            {
                if (this.flags.contains(Flags.START))
                {
                    result.append("S");
                }
                if (this.flags.contains(Flags.END))
                {
                    result.append("E");
                }
            }
            result.append(">");
        }
        if (printFlags.contains(PrintFlags.MODIFY_TIME))
        {
            result.append(String.format(" (%d.%d)", this.updateTime10 / 10, this.updateTime10 % 10));
        }
        return result.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void notify(EventInterface event) throws RemoteException
    {
        if (event.getType().equals(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT))
        {
            setValue(1, this.updateTime10, new CausePrinter("Detector became occupied"));
        }
        else if (event.getType().equals(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT))
        {
            setValue(0, this.updateTime10, new CausePrinter("Detector became unoccupied"));
        }
    }

}

/**
 * Class that can print a text version describing why a variable changed. Any work that has to be done (such as a call to
 * <code>TrafCOD.printRule</code>) is deferred until the <code>toString</code> method is called.
 */
class CausePrinter
{
    /** Object that describes the cause of the variable change. */
    final Object cause;

    /**
     * Construct a new CausePrinter object.
     * @param cause Object; this should be either a String, or a Object[] that contains a tokenized TrafCOD rule.
     */
    public CausePrinter(final Object cause)
    {
        this.cause = cause;
    }

    public String toString()
    {
        if (this.cause instanceof String)
        {
            return (String) this.cause;
        }
        else if (this.cause instanceof Object[])
        {
            try
            {
                return TrafCOD.printRule((Object[]) this.cause, true);
            }
            catch (TrafficControlException exception)
            {
                exception.printStackTrace();
                return ("printRule failed");
            }
        }
        return this.cause.toString();
    }
}

/**
 * Flags for toString method of a Variable.
 */
enum PrintFlags
{
    /** The name and stream of the Variable. */
    ID,
    /** The current Variable. */
    VALUE,
    /** Print "I" before the name (to indicate that a timer is initialized). */
    INITTIMER,
    /** Print "RI" before the name (to indicate that a timer is re-initialized). */
    REINITTIMER,
    /** Print "1" if just set, else print "0". */
    S,
    /** Print "1" if just reset, else print "0". */
    E,
    /** Print the negated Variable. */
    NEGATED,
    /** Print the flags of the Variable. */
    FLAGS,
    /** Print the time of last modification of the Variable. */
    MODIFY_TIME,
}

/**
 * Flags of a TrafCOD variable.
 */
enum Flags
{
    /** Variable becomes active. */
    START,
    /** Variable becomes inactive. */
    END,
    /** Timer has just expired. */
    TIMEREXPIRED,
    /** Variable has just changed value. */
    CHANGED,
    /** Variable is a timer. */
    IS_TIMER,
    /** Variable is a detector. */
    IS_DETECTOR,
    /** Variable has a start rule. */
    HAS_START_RULE,
    /** Variable has an end rule. */
    HAS_END_RULE,
    /** Variable is an output. */
    IS_OUTPUT,
    /** Variable must be initialized to 1 at start of control program. */
    INITED,
    /** Variable is traced; all changes must be printed. */
    TRACED,
}
