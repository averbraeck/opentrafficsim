package org.opentrafficsim.core.gtu.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DistContinuousDoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Generate GTUs at times prescribed in a text file.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 7 jul. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> id type of the GTUs that are generated
 */
public class ListGTUGenerator<ID>
{
    /** Name of this ListGTUGenerator. */
    final String name;

    /** Lane on which the generated GTUs are placed. */
    final Lane lane;

    /** The type of GTUs generated. */
    final GTUType<ID> gtuType;

    /** The GTU following model used by all generated GTUs. */
    final GTUFollowingModel gtuFollowingModel;

    /** The lane change model used by all generated GTUs. */
    final LaneChangeModel laneChangeModel;
    
    /** Initial speed of the generated GTUs. */
    final DoubleScalar.Abs<SpeedUnit> initialSpeed;

    /** The GTU colorer that will be linked to each generated GTU. */
    final GTUColorer gtuColorer;

    /** The simulator that controls everything. */
    final OTSDEVSSimulatorInterface simulator;

    /** Reader for the event list. */
    BufferedReader reader;

    /** Number of GTUs created. */
    int carsCreated = 0;

    /**
     * @param name
     * @param simulator
     * @param gtuType
     * @param gtuFollowingModel
     * @param laneChangeModel
     * @param initialSpeed
     * @param lane
     * @param position
     * @param routeGenerator
     * @param gtuColorer
     * @param fileName
     * @throws RemoteException
     * @throws SimRuntimeException
     */
    public ListGTUGenerator(String name, OTSDEVSSimulatorInterface simulator, GTUType<ID> gtuType,
            GTUFollowingModel gtuFollowingModel, LaneChangeModel laneChangeModel,
            DoubleScalar.Abs<SpeedUnit> initialSpeed, Lane lane,
            org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<LengthUnit> position,
            RouteGenerator routeGenerator, GTUColorer gtuColorer, String fileName) throws RemoteException,
            SimRuntimeException
    {
        this.name = name;
        this.lane = lane;
        this.gtuType = gtuType;
        this.gtuFollowingModel = gtuFollowingModel;
        this.laneChangeModel = laneChangeModel;
        this.initialSpeed = initialSpeed;
        this.simulator = simulator;
        this.gtuColorer = gtuColorer;
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
            String line = this.reader.readLine();
            double when = Double.parseDouble(line);
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(when, TimeUnit.SECOND), this, this,
                    "generateCar", null);
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
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions =
                new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lane, initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
            new LaneBasedIndividualCar<Integer>(++this.carsCreated, this.gtuType, this.gtuFollowingModel,
                    this.laneChangeModel, initialPositions, this.initialSpeed, vehicleLength,
                    new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                            SpeedUnit.KM_PER_HOUR), new Route(new ArrayList<Node<?, ?>>()), this.simulator,
                    DefaultCarAnimation.class, this.gtuColorer);
            scheduleNextVehicle();
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
        {
            exception.printStackTrace();
        }
    }

}
