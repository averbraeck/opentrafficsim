package org.opentrafficsim.trafficcontrol.trafcod;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.trafficcontrol.TrafficController;

/**
 * Functions that can draw a schematic diagram of an intersection given the list of traffic streams. The traffic stream numbers
 * must follow the Dutch conventions.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Diagram
{
    /** Numbering of the lateral objects/positions from the median to the shoulder. */
    /** Central divider. */
    static final int DIVIDER_1 = 0;

    /** Left turn area on roundabout. */
    static final int CAR_ROUNDABOUT_LEFT = 1;

    /** Public transit between divider and left turn lane. */
    static final int PT_DIV_L = 3;

    /** Divider between center public transit and left turn lane. */
    static final int DIVIDER_2 = 4;

    /** Left turn lane(s). */
    static final int CAR_LEFT = 5;

    /** No turn (center) lane(s). */
    static final int CAR_CENTER = 7;

    /** Right turn lane(s). */
    static final int CAR_RIGHT = 9;

    /** Divider between right turn lane and bicycle lane. */
    static final int DIVIDER_3 = 10;

    /** Public transit between right turn lane and bicycle lane. */
    static final int PT_RIGHT_BICYCLE = 11;

    /** Divider. */
    static final int DIVIDER_4 = 12;

    /** Bicycle lane. */
    static final int BICYCLE = 13;

    /** Divider. */
    static final int DIVIDER_5 = 14;

    /** Public transit between bicycle lane and right sidewalk. */
    static final int PT_BICYCLE_SIDEWALK = 15;

    /** Divider. */
    static final int DIVIDER_6 = 16;

    /** Sidewalk. */
    static final int SIDEWALK = 17;

    /** Divider. */
    static final int DIVIDER_7 = 18;

    /** Public transit right of right sidewalk. */
    static final int PT_SIDEWALK_SHOULDER = 19;

    /** Shoulder right of right sidewalk. */
    static final int SHOULDER = 20;

    /** Boundary of schematic intersection. */
    static final int BOUNDARY = 21;

    /** The streams crossing the intersection. */
    private final List<Short> streams;

    /** The routes through the intersection. */
    private final Map<Short, XYPair[]> routes = new LinkedHashMap<>();

    /**
     * Construct a new diagram.
     * @param streams Set&lt;Short&gt;; the streams (numbered according to the Dutch standard) that cross the intersection.
     * @throws TrafficControlException when a route is invalid
     */
    public Diagram(final Set<Short> streams) throws TrafficControlException
    {
        this.streams = new ArrayList<Short>(streams); // make a deep copy and sort by stream number
        this.streams.sort(new Comparator<Short>()
        {

            @Override
            public int compare(final Short o1, final Short o2)
            {
                return o1 - o2;
            }
        });
        // System.out.println("streams:");
        // for (short stream : this.streams)
        // {
        // System.out.print(String.format(" %02d", stream));
        // }
        // System.out.println("");

        // Primary car streams
        //@formatter:off
        for (short stream = 1; stream <= 12; stream += 3)
        {
            int quadrant = (stream - 1) / 3;
            this.routes.put(stream, rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, CAR_RIGHT), 
                    new RouteStep(-SHOULDER, CAR_RIGHT, Command.STOP_LINE_AND_ICON), 
                    new RouteStep(-CAR_CENTER, CAR_RIGHT), 
                    new RouteStep(-CAR_CENTER, BOUNDARY))));
            this.routes.put((short) (stream + 1), rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, CAR_CENTER), 
                    new RouteStep(-SHOULDER, CAR_CENTER, Command.STOP_LINE_AND_ICON), 
                    new RouteStep(Command.IF, stream + 1 + 60), 
                        new RouteStep(-CAR_ROUNDABOUT_LEFT, CAR_CENTER), 
                    new RouteStep(Command.ELSE), 
                        new RouteStep(BOUNDARY, CAR_CENTER), 
                    new RouteStep(Command.END_IF))));
            this.routes.put((short) (stream + 2), rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, CAR_LEFT), 
                    new RouteStep(-SHOULDER, CAR_LEFT, Command.STOP_LINE_AND_ICON), 
                    new RouteStep(Command.IF, stream + 2 + 60), 
                        new RouteStep(-CAR_ROUNDABOUT_LEFT, CAR_LEFT), 
                    new RouteStep(Command.ELSE_IF, (stream + 10) % 12 + 60),
                        new RouteStep(CAR_CENTER, CAR_LEFT), 
                        new RouteStep(CAR_CENTER, CAR_ROUNDABOUT_LEFT),
                    new RouteStep(Command.ELSE), 
                        new RouteStep(-CAR_LEFT, CAR_LEFT), 
                        new RouteStep(-CAR_LEFT, PT_DIV_L), 
                        new RouteStep(-CAR_ROUNDABOUT_LEFT, PT_DIV_L), 
                        new RouteStep(-CAR_ROUNDABOUT_LEFT, -CAR_LEFT), 
                        new RouteStep(PT_DIV_L, -CAR_LEFT),
                        new RouteStep(PT_DIV_L, -CAR_CENTER), 
                        new RouteStep(CAR_CENTER, -CAR_CENTER),
                        new RouteStep(CAR_CENTER, -BOUNDARY),
                    new RouteStep(Command.END_IF))));
        }
        // Bicycle streams
        for (short stream = 21; stream <= 28; stream += 2)
        {
            int quadrant = (stream - 19) / 2 % 4;
            this.routes.put(stream, rotateRoute(quadrant, assembleRoute(
                    new RouteStep(DIVIDER_1, BICYCLE, Command.ICON), 
                    new RouteStep(SHOULDER, BICYCLE),
                    new RouteStep(BOUNDARY, BICYCLE, Command.ICON))));
            this.routes.put((short) (stream + 1), rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, BICYCLE), 
                    new RouteStep(-DIVIDER_3, BICYCLE, Command.ICON),
                    new RouteStep(Command.IF, stream), 
                    new RouteStep(-DIVIDER_1, BICYCLE, Command.ICON),
                    new RouteStep(Command.ELSE), 
                        new RouteStep(SHOULDER, BICYCLE), 
                        new RouteStep(BOUNDARY, BICYCLE, Command.ICON), 
                    new RouteStep(Command.END_IF))));
        }
        // Pedestrian streams
        for (short stream = 31; stream <= 38; stream += 2)
        {
            int quadrant = (stream - 29) / 2 % 4;
            this.routes.put(stream, rotateRoute(quadrant, assembleRoute(
                    new RouteStep(DIVIDER_1, SIDEWALK), 
                    new RouteStep(BOUNDARY, SIDEWALK))));
            this.routes.put((short) (stream + 1), rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, SIDEWALK), 
                    new RouteStep(Command.IF, stream), 
                        new RouteStep(-DIVIDER_1, SIDEWALK), 
                    new RouteStep(Command.ELSE), 
                        new RouteStep(BOUNDARY, SIDEWALK),
                    new RouteStep(Command.END_IF))));
        }
        // Public transit streams
        for (short stream = 41; stream <= 52; stream += 3)
        {
            int quadrant = (stream - 41) / 3;
            this.routes.put(stream, rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, PT_DIV_L), 
                    new RouteStep(-SHOULDER, PT_DIV_L, Command.STOP_LINE), 
                    new RouteStep(-PT_SIDEWALK_SHOULDER, PT_DIV_L, Command.ICON),
                    new RouteStep(-CAR_RIGHT, PT_DIV_L), 
                    new RouteStep(-CAR_RIGHT, CAR_LEFT), 
                    new RouteStep(-PT_DIV_L, CAR_LEFT), 
                    new RouteStep(-PT_DIV_L, SHOULDER), 
                    new RouteStep(-PT_DIV_L, BOUNDARY, Command.ICON))));
            this.routes.put((short) (stream + 1), rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, PT_DIV_L), 
                    new RouteStep(-SHOULDER, PT_DIV_L, Command.STOP_LINE), 
                    new RouteStep(-PT_SIDEWALK_SHOULDER, PT_DIV_L, Command.ICON),
                    new RouteStep(SHOULDER, PT_DIV_L), 
                    new RouteStep(BOUNDARY, PT_DIV_L))));
            this.routes.put((short) (stream + 2), rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-BOUNDARY, PT_DIV_L), 
                    new RouteStep(-SHOULDER, PT_DIV_L, Command.STOP_LINE), 
                    new RouteStep(-PT_SIDEWALK_SHOULDER, PT_DIV_L, Command.ICON),
                    new RouteStep(-CAR_RIGHT, PT_DIV_L), 
                    new RouteStep(-CAR_RIGHT, CAR_ROUNDABOUT_LEFT), 
                    new RouteStep(-PT_DIV_L, CAR_ROUNDABOUT_LEFT), 
                    new RouteStep(Command.IF, (stream + 2 - 40) % 12 + 60), 
                        new RouteStep(-PT_DIV_L, -PT_DIV_L), 
                        new RouteStep(PT_DIV_L, -PT_DIV_L), 
                    new RouteStep(Command.ELSE), 
                        new RouteStep(-PT_DIV_L, -CAR_CENTER), 
                        new RouteStep(CAR_ROUNDABOUT_LEFT, -CAR_CENTER), 
                        new RouteStep(CAR_ROUNDABOUT_LEFT, -CAR_RIGHT), 
                        new RouteStep(PT_DIV_L, -CAR_RIGHT), 
                    new RouteStep(Command.END_IF),
                    new RouteStep(PT_DIV_L, -SHOULDER), 
                    new RouteStep(PT_DIV_L, -BOUNDARY, Command.ICON))));
        }
        // Secondary car streams
        for (short stream = 62; stream <= 72; stream += 3)
        {
            int quadrant = (stream - 61) / 3;
            this.routes.put(stream, rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-CAR_ROUNDABOUT_LEFT, CAR_CENTER), 
                    new RouteStep(CAR_ROUNDABOUT_LEFT, CAR_CENTER, Command.STOP_LINE_AND_ICON), 
                    new RouteStep(BOUNDARY, CAR_CENTER))));
            this.routes.put((short) (stream + 1), rotateRoute(quadrant, assembleRoute(
                    new RouteStep(-CAR_ROUNDABOUT_LEFT, CAR_LEFT), 
                    new RouteStep(CAR_ROUNDABOUT_LEFT, CAR_LEFT, Command.STOP_LINE_AND_ICON), 
                    new RouteStep(CAR_CENTER, CAR_LEFT), 
                    new RouteStep(Command.IF, ((stream - 61) + 11) % 12 + 60),
                    new RouteStep(CAR_CENTER, CAR_ROUNDABOUT_LEFT), 
                    new RouteStep(Command.ELSE), 
                    new RouteStep(CAR_CENTER, -BOUNDARY), 
                    new RouteStep(Command.END_IF))));
        }
       // @formatter:on
    }

    /**
     * Check that a particular stream exists. Beware that the keys in this.streams are Short.
     * @param stream short; the number of the stream to check
     * @return boolean; true if the stream exists; false if it does not exist
     */
    private boolean streamExists(final short stream)
    {
        return this.streams.contains(stream);
    }

    /**
     * Report if object is inaccessible to all traffic.
     * @param i int; the number of the object
     * @return boolean; true if the object is inaccessible to all traffic
     */
    public static final boolean isGrass(final int i)
    {
        return i == DIVIDER_1 || i == DIVIDER_2 || i == DIVIDER_3 || i == DIVIDER_4 || i == DIVIDER_5 || i == DIVIDER_6
                || i == DIVIDER_7 || i == SHOULDER;
    }

    /**
     * Return the LaneType for a stream number.
     * @param streamNumber int; the standard Dutch traffic stream number
     * @return LaneType; the lane type of the stream; or null if the stream number is reserved or invalid
     */
    final LaneType laneType(final int streamNumber)
    {
        if (streamNumber < 20 || streamNumber > 60 && streamNumber <= 80)
        {
            return LaneType.CAR_LANE;
        }
        if (streamNumber >= 20 && streamNumber < 30)
        {
            return LaneType.BICYCLE_LANE;
        }
        if (streamNumber >= 30 && streamNumber < 40)
        {
            return LaneType.PEDESTRIAN_LANE;
        }
        if (streamNumber > 40 && streamNumber <= 52 || streamNumber >= 81 && streamNumber <= 92)
        {
            return LaneType.PUBLIC_TRANSIT_LANE;
        }
        return null;
    }

    /**
     * Types of lanes.
     */
    enum LaneType
    {
        /** Car. */
        CAR_LANE,
        /** BICYCLE. */
        BICYCLE_LANE,
        /** Public transit. */
        PUBLIC_TRANSIT_LANE,
        /** Pedestrian. */
        PEDESTRIAN_LANE,
    }

    /**
     * Return the rotated x value.
     * @param xyPair XYPair; the XYPair
     * @param rotation int; rotation in multiples of 90 degrees
     * @return int; the x component of the rotated coordinates
     */
    final int rotatedX(final XYPair xyPair, final int rotation)
    {
        switch (rotation % 4)
        {
            case 0:
                return xyPair.getX();
            case 1:
                return -xyPair.getY();
            case 2:
                return -xyPair.getX();
            case 3:
                return xyPair.getY();
            default:
                break; // cannot happen
        }
        return 0; // cannot happen
    }

    /**
     * Return the rotated y value.
     * @param xyPair XYPair; the XYPair
     * @param rotation int; rotation in multiples of 90 degrees
     * @return int; the y component of the rotated coordinates
     */
    final int rotatedY(final XYPair xyPair, final int rotation)
    {
        switch (rotation % 4)
        {
            case 0:
                return xyPair.getY();
            case 1:
                return xyPair.getX();
            case 2:
                return -xyPair.getY();
            case 3:
                return -xyPair.getX();
            default:
                break; // cannot happen
        }
        return 0; // cannot happen
    }

    /**
     * Commands used in RouteStep.
     */
    enum Command
    {
        /** No operation. */
        NO_OP,
        /** If. */
        IF,
        /** Else. */
        ELSE,
        /** Else if. */
        ELSE_IF,
        /** End if. */
        END_IF,
        /** Stop line. */
        STOP_LINE,
        /** Icon (bus, bicycle symbol). */
        ICON,
        /** Stop line AND icon. */
        STOP_LINE_AND_ICON,
    }

    /**
     * Step in a schematic route through the intersection.
     */
    class RouteStep
    {
        /** X object. */
        private final int x;

        /** Y object. */
        private final int y;

        /** Command of this step. */
        private final Command command;

        /** Condition for IF and ELSE_IF commands. */
        private final int streamCondition;

        /**
         * Construct a RouteStep that has a NO_OP command.
         * @param x int; the X object at the end of this route step
         * @param y int; the Y object at the end of this route step
         */
        RouteStep(final int x, final int y)
        {
            this.x = x;
            this.y = y;
            this.command = Command.NO_OP;
            this.streamCondition = TrafficController.NO_STREAM;
        }

        /**
         * Construct a RouteStep with a command condition.
         * @param x int; the X object at the end of this route step
         * @param y int; the Y object at the end of this route step
         * @param command Command; a STOP_LINE or NO_OP command
         * @throws TrafficControlException when an IF or ELSE_IF has an invalid streamCondition, or when an ELSE or END_IF has a
         *             valid streamCOndition
         */
        RouteStep(final int x, final int y, final Command command) throws TrafficControlException
        {
            Throw.when(
                    Command.STOP_LINE != command && Command.NO_OP != command && Command.ICON != command
                            && Command.STOP_LINE_AND_ICON != command,
                    TrafficControlException.class,
                    "X and Y should only be provided with a NO_OP, STOP_LINE, ICON, or STOP_LINE_AND_ICON command; not with "
                            + command);
            this.x = x;
            this.y = y;
            this.command = command;
            this.streamCondition = TrafficController.NO_STREAM;
        }

        /**
         * Construct a RouteStep with a command condition.
         * @param command Command; an IF, ELSE, ENDIF, or ELSE_IF command
         * @param streamCondition int; the stream that must exist for the condition to be true
         * @throws TrafficControlException when an IF or ELSE_IF has an invalid streamCondition, or when an ELSE or END_IF has a
         *             valid streamCOndition
         */
        RouteStep(final Command command, final int streamCondition) throws TrafficControlException
        {
            Throw.when(Command.IF != command && Command.ELSE_IF != command, TrafficControlException.class,
                    "RouteStep constructor with stream condition must use command IF or ELSE_IF");
            this.x = TrafficController.NO_STREAM;
            this.y = TrafficController.NO_STREAM;
            this.command = command;
            Throw.when(streamCondition == TrafficController.NO_STREAM, TrafficControlException.class,
                    "IF or ELSE_IF need a valid traffic stream number");
            this.streamCondition = streamCondition;
        }

        /**
         * Construct a RouteStep for ELSE or END_IF command.
         * @param command Command; either <code>Command.ELSE</code> or <code>Command.END_IF</code>
         * @throws TrafficControlException when the Command is not ELSE or END_IF
         */
        RouteStep(final Command command) throws TrafficControlException
        {
            Throw.when(Command.ELSE != command && Command.END_IF != command, TrafficControlException.class,
                    "RouteStep constructor with single command parameter requires ELSE or END_IF command");
            this.x = TrafficController.NO_STREAM;
            this.y = TrafficController.NO_STREAM;
            this.command = command;
            this.streamCondition = TrafficController.NO_STREAM;
        }

        /**
         * Retrieve the X object.
         * @return int; the X object
         */
        public int getX()
        {
            return this.x;
        }

        /**
         * Retrieve the Y object.
         * @return int; the Y object
         */
        public int getY()
        {
            return this.y;
        }

        /**
         * Retrieve the command.
         * @return Command
         */
        public Command getCommand()
        {
            return this.command;
        }

        /**
         * Retrieve the stream condition.
         * @return int; the streamCondition
         */
        public int getStreamCondition()
        {
            return this.streamCondition;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "RouteStep [x=" + this.x + ", y=" + this.y + ", command=" + this.command + ", streamCondition="
                    + this.streamCondition + "]";
        }

    }

    /**
     * Pack two integer coordinates in one object.
     */
    class XYPair
    {
        /** X. */
        private final int x;

        /** Y. */
        private final int y;

        /**
         * Construct a new XY pair.
         * @param x int; the X value
         * @param y int; the Y value
         */
        XYPair(final int x, final int y)
        {
            this.x = x;
            this.y = y;
        }

        /**
         * Construct a new XY pair from a route step.
         * @param routeStep RouteStep; the route step
         */
        XYPair(final RouteStep routeStep)
        {
            this.x = routeStep.getX();
            this.y = routeStep.getY();
        }

        /**
         * Construct a rotated version of an XYPair.
         * @param in XYPair; the initial version
         * @param quadrant int; the quadrant
         */
        XYPair(final XYPair in, final int quadrant)
        {
            this.x = rotatedX(in, quadrant);
            this.y = rotatedY(in, quadrant);
        }

        /**
         * Retrieve the X value.
         * @return int; the X value
         */
        public int getX()
        {
            return this.x;
        }

        /**
         * Retrieve the Y value.
         * @return int; the Y value
         */
        public int getY()
        {
            return this.y;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "XYPair [x=" + this.x + ", y=" + this.y + "]";
        }

    }

    /**
     * Construct a route.
     * @param quadrant int; the quadrant to assemble the route for
     * @param steps RouteStep...; an array, or series of arguments of type RouteStep
     * @return XYPair[]; an array of XY pairs describing the route through the intersection
     * @throws TrafficLightException when the route contains commands other than NO_OP and STOP_LINE
     */
    private XYPair[] rotateRoute(final int quadrant, final RouteStep... steps) throws TrafficControlException
    {
        List<XYPair> route = new ArrayList<>();
        boolean on = true;

        for (RouteStep step : steps)
        {
            switch (step.getCommand())
            {
                case NO_OP:
                case STOP_LINE:
                case ICON:
                case STOP_LINE_AND_ICON:
                    if (on)
                    {
                        route.add(new XYPair(new XYPair(step), quadrant));
                    }
                    break;

                default:
                    throw new TrafficControlException("Bad command in rotateRoute: " + step.getCommand());

            }
        }
        return route.toArray(new XYPair[route.size()]);
    }

    /**
     * Construct a route through the intersection.
     * @param steps RouteStep...; the steps of the route description
     * @return RouteStep[]; the route through the intersection
     * @throws TrafficControlException when something is very wrong
     */
    private RouteStep[] assembleRoute(final RouteStep... steps) throws TrafficControlException
    {
        List<RouteStep> result = new ArrayList<>();
        RouteStep step;
        for (int pointNo = 0; null != (step = routePoint(pointNo, steps)); pointNo++)
        {
            result.add(step);
        }
        return result.toArray(new RouteStep[result.size()]);
    }

    /**
     * Return the Nth step in a route.
     * @param pointNo int; the rank of the requested step
     * @param steps RouteStep...; RouteStep... the steps
     * @return RouteStep; the Nth step in the route or null if the route does not have <code>pointNo</code> steps
     * @throws TrafficControlException when the command in a routestep is not recognized
     */
    private RouteStep routePoint(final int pointNo, final RouteStep... steps) throws TrafficControlException
    {
        boolean active = true;
        boolean beenActive = false;
        int index = 0;

        for (RouteStep routeStep : steps)
        {
            switch (routeStep.getCommand())
            {
                case NO_OP:
                case STOP_LINE:
                case ICON:
                case STOP_LINE_AND_ICON:
                    if (active)
                    {
                        if (index++ == pointNo)
                        {
                            return routeStep;
                        }
                    }
                    break;

                case IF:
                    active = streamExists((short) routeStep.getStreamCondition());
                    beenActive = active;
                    break;

                case ELSE_IF:
                    if (active)
                    {
                        active = false;
                    }
                    else if (!beenActive)
                    {
                        active = this.streams.contains(routeStep.getStreamCondition());
                    }
                    if (active)
                    {
                        beenActive = true;
                    }
                    break;

                case ELSE:
                    active = !beenActive;
                    break;

                case END_IF:
                    active = true;
                    break;

                default:
                    throw new TrafficControlException("Bad switch: " + routeStep);

            }
        }
        return null;
    }

    /**
     * Create a BufferedImage and render the schematic on it.
     * @return BufferedImage
     */
    public BufferedImage render()
    {
        int range = 2 * BOUNDARY + 1;
        int cellSize = 10;
        BufferedImage result = new BufferedImage(range * cellSize, range * cellSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) result.getGraphics();
        graphics.setColor(Color.GREEN);
        graphics.fillRect(0, 0, result.getWidth(), result.getHeight());
        for (Short stream : this.streams)
        {
            switch (laneType(stream))
            {
                case BICYCLE_LANE:
                    graphics.setColor(Color.RED);
                    break;

                case CAR_LANE:
                    graphics.setColor(Color.BLACK);
                    break;

                case PEDESTRIAN_LANE:
                    graphics.setColor(Color.BLUE);
                    break;

                case PUBLIC_TRANSIT_LANE:
                    graphics.setColor(Color.BLACK);
                    break;

                default:
                    graphics.setColor(Color.WHITE);
                    break;

            }
            XYPair[] path = this.routes.get(stream);
            if (null == path)
            {
                System.err.println("Cannot find path for stream " + stream);
                continue;
            }
            XYPair prevPair = null;
            for (XYPair xyPair : path)
            {
                if (null != prevPair)
                {
                    int dx = (int) Math.signum(xyPair.getX() - prevPair.getX());
                    int dy = (int) Math.signum(xyPair.getY() - prevPair.getY());
                    int x = prevPair.getX() + dx;
                    int y = prevPair.getY() + dy;
                    while (x != xyPair.getX() || y != xyPair.getY())
                    {
                        fillXYPair(graphics, new XYPair(x, y));
                        if (x != xyPair.getX())
                        {
                            x += dx;
                        }
                        if (y != xyPair.getY())
                        {
                            y += dy;
                        }
                    }

                }
                fillXYPair(graphics, xyPair);
                prevPair = xyPair;
            }
        }
        return result;
    }

    /**
     * Fill one box taking care to rotate to display conventions.
     * @param graphics Graphics2D; the graphics environment
     * @param xyPair XYPair; the box to fill
     */
    private void fillXYPair(final Graphics2D graphics, final XYPair xyPair)
    {
        int cellSize = 10;
        graphics.fillRect(cellSize * (BOUNDARY - xyPair.getX()), cellSize * (BOUNDARY - xyPair.getY()), cellSize, cellSize);
    }

    /**
     * Test the Diagram code.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JFrame frame = new JFrame("Diagram test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setMinimumSize(new Dimension(1000, 1000));
                JPanel mainPanel = new JPanel(new BorderLayout());
                frame.add(mainPanel);
                checkBoxPanel = new JPanel();
                checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
                JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
                scrollPane.setPreferredSize(new Dimension(150, 1000));
                mainPanel.add(scrollPane, BorderLayout.LINE_START);
                for (int stream = 1; stream <= 12; stream++)
                {
                    checkBoxPanel.add(makeCheckBox(stream, stream % 3 == 2));
                }
                for (int stream = 21; stream <= 28; stream++)
                {
                    checkBoxPanel.add(makeCheckBox(stream, false));
                }
                for (int stream = 31; stream <= 38; stream++)
                {
                    checkBoxPanel.add(makeCheckBox(stream, false));
                }
                for (int stream = 41; stream <= 52; stream++)
                {
                    checkBoxPanel.add(makeCheckBox(stream, false));
                }
                for (int stream = 61; stream <= 72; stream++)
                {
                    if (stream % 3 == 1)
                    {
                        continue;
                    }
                    checkBoxPanel.add(makeCheckBox(stream, false));
                }
                testPanel = new JPanel();
                rebuildTestPanel();
                mainPanel.add(testPanel, BorderLayout.CENTER);
                frame.setVisible(true);
            }
        });

    }

    /**
     * Make a check box to switch a particular stream number on or off.
     * @param stream int; the stream number
     * @param initialState boolean; if true; the check box will be checked
     * @return JCheckBox
     */
    public static JCheckBox makeCheckBox(final int stream, final boolean initialState)
    {
        JCheckBox result = new JCheckBox(String.format("Stream %02d", stream));
        result.setSelected(initialState);
        result.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(final ActionEvent e)
            {
                rebuildTestPanel();
            }
        });
        return result;
    }

    /** JPanel used to render the intersection for testing. */
    private static JPanel testPanel = null;

    /** JPanel that holds all the check boxes. */
    private static JPanel checkBoxPanel = null;

    /**
     * Render the intersection.
     */
    static void rebuildTestPanel()
    {
        testPanel.removeAll();
        Set<Short> streamList = new LinkedHashSet<>();
        for (Component c : checkBoxPanel.getComponents())
        {
            if (c instanceof JCheckBox)
            {
                JCheckBox checkBox = (JCheckBox) c;
                if (checkBox.isSelected())
                {
                    String caption = checkBox.getText();
                    String streamText = caption.substring(caption.length() - 2);
                    Short stream = Short.parseShort(streamText);
                    streamList.add(stream);
                }
            }
        }
        try
        {
            Diagram diagram = new Diagram(streamList);
            testPanel.add(new JLabel(new ImageIcon(diagram.render())));
        }
        catch (TrafficControlException exception)
        {
            exception.printStackTrace();
        }
        testPanel.repaint();
        testPanel.revalidate();
    }

}
