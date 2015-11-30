package org.opentrafficsim.road.gtu.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.route.LaneBasedRouteGenerator;

/**
 * Generate GTUs at times prescribed in a text file.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 7 jul. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class ListGTUGenerator
{
    /** Name of this ListGTUGenerator. */
    private final String name;

    /** Lane on which the generated GTUs are placed. */
    private final Lane lane;

    /** The type of GTUs generated. */
    private final GTUType gtuType;

    /** The GTU following model used by all generated GTUs. */
    private final GTUFollowingModel gtuFollowingModel;

    /** The lane change model used by all generated GTUs. */
    private final LaneChangeModel laneChangeModel;

    /** The lane change model used by all generated GTUs. */
    private final LaneBasedRouteGenerator routeGenerator;

    /** Initial speed of the generated GTUs. */
    private final Speed initialSpeed;

    /** The GTU colorer that will be linked to each generated GTU. */
    private final GTUColorer gtuColorer;

    /** The simulator that controls everything. */
    private final OTSDEVSSimulatorInterface simulator;

    /** Reader for the event list. */
    private BufferedReader reader;

    /** Number of GTUs created. */
    private int carsCreated = 0;

    /**
     * Construct a GTU generator that takes the times to generate another GTU from an external source. <br>
     * Currently the external input is a text file in the local file system. This should be replaced by a more general
     * mechanism. Currently, the format of the input is one floating point value per line. This may be changed into an XML
     * format that can also specify the GTUType, etc.
     * @param name String; name if this generator
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param gtuType GTUType&lt;ID&gt;; the GTUType of the generated GTUs
     * @param gtuFollowingModel GTUFollowingModel; the GTU following model of the generated GTUs
     * @param laneChangeModel LaneChangeModel; the lane change model of the generated GTUs
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the generated GTUs
     * @param lane Lane; the lane on which the generated GTUs are placed
     * @param position DoubleScalar.Rel&lt;LengthUnit&gt;; the position on the lane where the generated GTUs are placed
     * @param direction the direction on the lane in which the GTU has to be generated (DIR_PLUS, or DIR_MINUS)
     * @param routeGenerator RouteGenerator; the route generator that generates the routes of the generated GTUs
     * @param gtuColorer GTUColorere; the GTUColorer of the generated GTUs
     * @param fileName String; name of file with the times when another GTU is to be generated (XXXX STUB)
     * @throws SimRuntimeException on
     * @throws NetworkException on
     */
    public ListGTUGenerator(final String name, final OTSDEVSSimulatorInterface simulator, final GTUType gtuType,
        final GTUFollowingModel gtuFollowingModel, final LaneChangeModel laneChangeModel, final Speed initialSpeed,
        final Lane lane, final Length.Rel position, final GTUDirectionality direction, final LaneBasedRouteGenerator routeGenerator,
        final GTUColorer gtuColorer, final String fileName) throws SimRuntimeException, NetworkException
    {
        if (null == lane)
        {
            throw new NetworkException("lane may not be null");
        }
        this.name = name;
        this.lane = lane;
        this.gtuType = gtuType;
        this.gtuFollowingModel = gtuFollowingModel;
        this.laneChangeModel = laneChangeModel;
        this.initialSpeed = initialSpeed;
        this.simulator = simulator;
        this.gtuColorer = gtuColorer;
        this.routeGenerator = routeGenerator;
        try
        {
            this.reader = new BufferedReader(new FileReader(new File(fileName)));
            scheduleNextVehicle();
        }
        catch (FileNotFoundException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Schedule generation of the next GTU.
     */
    private void scheduleNextVehicle()
    {
        try
        {
            String line = null;
            do
            {
                line = this.reader.readLine();
                if (null == line)
                {
                    return; // End of input; do not re-schedule
                }
            }
            while (line.equals("")); // ignore blank lines
            double when = Double.parseDouble(line);
            this.simulator.scheduleEventAbs(new Time.Abs(when, TimeUnit.SI), this, this, "generateCar", null);
        }
        catch (NumberFormatException exception)
        {
            exception.printStackTrace();
            scheduleNextVehicle();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Generate one car and re-schedule this method.
     */
    protected final void generateCar()
    {
        // TODO use given position in the constructor?
        Length.Rel initialPosition = new Length.Rel(0, LengthUnit.METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>();
        // TODO use given directionality in the constructor?
        initialPositions.add(new DirectedLanePosition(this.lane, initialPosition, GTUDirectionality.DIR_PLUS));
        try
        {
            Length.Rel vehicleLength = new Length.Rel(4, LengthUnit.METER);
            new LaneBasedIndividualCar("" + (++this.carsCreated), this.gtuType, this.gtuFollowingModel,
                this.laneChangeModel, initialPositions, this.initialSpeed, vehicleLength, new Length.Rel(1.8, LengthUnit.METER),
                new Speed(200, SpeedUnit.KM_PER_HOUR), this.routeGenerator.generateRouteNavigator(), this.simulator,
                DefaultCarAnimation.class, this.gtuColorer);
            scheduleNextVehicle();
        }
        catch (SimRuntimeException | NamingException | NetworkException | GTUException exception)
        {
            exception.printStackTrace();
        }
    }

}
