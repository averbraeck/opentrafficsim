package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * This class generates CACC tactical planners for its user.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CaccTacticalPlannerFactory extends AbstractLaneBasedTacticalPlannerFactory<CaccTacticalPlanner>
{

    /** Car-following model factory. */
    private CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory;

    /** Longitudinal controller. */
    private final LongitudinalControllerFactory<? extends CaccController> longitudinalControllerFactory;

    /** Stream for random numbers. */
    private final StreamInterface randomStream;
    
    /** The GTU type that has CACC. */
    private final GTUType caccGTUType;

    /**
     * Constructor.
     * @param carFollowingModelFactory CarFollowingModelFactory&;t;? extends CarFollowingModel&gt;; car-following model factory
     * @param longitudinalControllerFactory LongitudinalControllerFactory&lt;CACC&gt;; longitudinal controller factory
     * @param simulator OTSSimulatorInterface; simulator
     * @param caccGTUType GTUType; the GTU type that implements CACC
     */
    public CaccTacticalPlannerFactory(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final LongitudinalControllerFactory<? extends CaccController> longitudinalControllerFactory,
            final OTSSimulatorInterface simulator, final GTUType caccGTUType)
    {
        super(carFollowingModelFactory, new PerceptionFactory()
        {
            /** {@inheritDoc} */
            @Override
            public Parameters getParameters() throws ParameterException
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public LanePerception generatePerception(final LaneBasedGTU gtu)
            {
                CategoricalLanePerception perception = new CategoricalLanePerception(gtu);
                perception.addPerceptionCategory(
                        new CaccPerceptionCategory(perception, simulator.getReplication().getStream("default"), simulator));
                perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
                perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
                return perception;
            }

        });
        this.carFollowingModelFactory = carFollowingModelFactory;
        this.longitudinalControllerFactory = longitudinalControllerFactory;
        this.randomStream = simulator.getReplication().getStream("default");
        Throw.whenNull(caccGTUType, "caccGTUType may not be null");
        this.caccGTUType = caccGTUType;
    }

    /** {@inheritDoc} */
    @Override
    public CaccTacticalPlanner create(final LaneBasedGTU gtu) throws GTUException
    {
        CaccController lcf = this.longitudinalControllerFactory.create();
        lcf.setCACCGTUType(this.caccGTUType);
        return new CaccTacticalPlanner(this.carFollowingModelFactory.generateCarFollowingModel(), gtu,
                getPerceptionFactory().generatePerception(gtu), lcf);
    }

    /** {@inheritDoc} */
    @Override
    public Parameters getParameters() throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        this.carFollowingModelFactory.getParameters().setAllIn(parameters);
        parameters.setDefaultParameter(LongitudinalController.SENSOR_RANGE);
        parameters.setDefaultParameter(ParameterTypes.PERCEPTION);
        parameters.setDefaultParameter(ParameterTypes.LOOKBACK);
        parameters.setDefaultParameter(ParameterTypes.LOOKAHEAD);
        // parameters.setDefaultParameter(ParameterTypes.DT);
        parameters.setParameter(ParameterTypes.DT, Duration.instantiateSI(0.5)); // Time step of model
        parameters.setDefaultParameter(ParameterTypes.T0); // Look-ahead time for mandatory lane changes
        parameters.setParameter(ParameterTypes.BCRIT, Acceleration.instantiateSI(2.5)); // Critical deceleration, when other
                                                                                   // deceleration kicks in!
        // parameters.setDefaultParameter(ParameterTypes.LCDUR); // Lane-change duration

        parameters.setDefaultParameters(CaccController.class);

        // Parameters the LMRS needs to interact with surrounding traffic
        parameters.setParameter(LmrsParameters.DLEFT, 0.0);
        parameters.setParameter(LmrsParameters.DRIGHT, 0.0);
        // TODO find a way to let regular vehicles trigger relaxation, without platooning trucks over reacting
        parameters.setParameter(ParameterTypes.TMIN, Duration.instantiateSI(0.56)); // 0.49
        parameters.setParameter(ParameterTypes.TMAX, Duration.instantiateSI(1.2)); // 0.5
        parameters.setParameter(ParameterTypes.T, Duration.instantiateSI(1.2));
        parameters.setDefaultParameter(CaccParameters.T_GAP);
        parameters.setDefaultParameter(CaccParameters.A_REDUCED);

        // TODO add default values for CACC specific parameters to 'parameters' here
        return parameters;
    }

}
