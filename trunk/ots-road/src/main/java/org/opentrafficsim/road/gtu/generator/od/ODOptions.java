package org.opentrafficsim.road.gtu.generator.od;

import java.util.HashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.road.gtu.generator.ArrivalsHeadwayGenerator.HeadwayRandomization;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.Bias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.TTCRoomChecker;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;

import nl.tudelft.simulation.language.Throw;

/**
 * Options for vehicle generation based on an OD matrix.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ODOptions
{

    /** Read-only. */
    private boolean readOnly = false;

    /** Headway randomization option. */
    // TODO this may be origin / generator specific
    public static final Option<HeadwayRandomization> HEADWAY =
            new Option<>(new ConstantGenerator<>(HeadwayRandomization.EXPONENTIAL));

    /** ID generator option. */
    public static final Option<IdGenerator> ID = new Option<>(new ConstantGenerator<>(new IdGenerator("")));

    /** GTU colorer option. */
    public static final Option<GTUColorer> COLORER = new Option<>(new ConstantGenerator<>(new DefaultSwitchableGTUColorer()));

    /** GTU characteristics generator option. */
    public static final Option<GTUCharacteristicsGeneratorOD> GTU_TYPE =
            new Option<>(new ConstantGenerator<>(new DefaultGTUCharacteristicsGeneratorOD()));

    /** Room checker option. */
    public static final Option<RoomChecker> ROOM =
            new Option<>(new ConstantGenerator<>(new TTCRoomChecker(Duration.createSI(10.0))));

    // TODO this may be origin / generator specific, or even road/lane type specific
    /** Markov chain for GTU type option. */
    public static final Option<MarkovCorrelation<GTUType, Frequency>> MARKOV = new Option<>(new ConstantGenerator<>(null));

    // TODO this may be origin / generator specific, or even road type specific
    /** Lane bias. Default is Truck: truck right (strong right, max 2 lanes), Vehicle (other): weak left. */
    public static final Option<LaneBiases> BIAS = new Option<>(new ConstantGenerator<>(
            new LaneBiases().addBias(GTUType.TRUCK, Bias.TRUCK_RIGHT).addBias(GTUType.VEHICLE, Bias.WEAK_LEFT)));

    /** Options. */
    private Map<Option<?>, Object> options = new HashMap<>();

    /**
     * Set option value.
     * @param option Option&lt;K&gt;; option
     * @param value K; option value
     * @param <K> value type
     * @return this option set
     */
    public final <K> ODOptions set(final Option<K> option, final K value)
    {
        Throw.when(this.readOnly, IllegalStateException.class, "Setting option on read-only options.");
        this.options.put(option, value);
        return this;
    }

    /**
     * Get option value.
     * @param option Option&lt;K&gt;; option
     * @param <K> value type
     * @return K; option value
     */
    public final <K> K get(final Option<K> option)
    {
        Throw.when(!this.readOnly, IllegalStateException.class, "Getting option from ODGenerationOptions that is not read-only."
                + " Use ODGenerationOptions.setReadOnly() to set ODGenerationOptions to read-only.");
        Throw.whenNull(option, "Option may not be null.");
        @SuppressWarnings("unchecked")
        K value = (K) this.options.get(option);
        if (value == null)
        {
            value = option.getDefaultValue();
            this.options.put(option, value);
        }
        return value;
    }

    /**
     * Sets the options to read-only, after which no new options may be set, and the options may be used.
     * @return this option set
     */
    public ODOptions setReadOnly()
    {
        this.readOnly = true;
        return this;
    }

    /**
     * Utility class to store options.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <K> option value type
     */
    private static final class Option<K>
    {

        /** Default value supplier. */
        private final ConstantGenerator<K> defaultSupplier;

        /**
         * Constructor.
         * @param defaultSupplier K; default value supplier
         */
        Option(final ConstantGenerator<K> defaultSupplier)
        {
            this.defaultSupplier = defaultSupplier;
        }

        /**
         * Returns the default value.
         * @return default value
         */
        public K getDefaultValue()
        {
            return this.defaultSupplier.draw();
        }
    }

    /**
     * Default generator for {@code LaneBasedGTUCharacteristics}.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static final class DefaultGTUCharacteristicsGeneratorOD implements GTUCharacteristicsGeneratorOD
    {
        /** Constructor. */
        DefaultGTUCharacteristicsGeneratorOD()
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGTUCharacteristics draw(final Node origin, final Node destination, final Category category)
                throws GTUException
        {
            Categorization categorization = category.getCategorization();
            // GTU characteristics
            GTUType gtuType;
            if (categorization.entails(GTUType.class))
            {
                gtuType = category.get(GTUType.class);
            }
            else
            {
                gtuType = GTUType.CAR;
            }
            GTUCharacteristics gtuCharacteristics;
            try
            {
                gtuCharacteristics = GTUType.defaultCharacteristics(gtuType);
            }
            catch (GTUException exception)
            {
                throw new RuntimeException(exception);
            }
            // strategical factory
            LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LMRSFactory(new IDMPlusFactory(), new DefaultLMRSPerceptionFactory()));
            // route
            Route route;
            if (categorization.entails(Route.class))
            {
                route = category.get(Route.class);
            }
            else
            {
                try
                {
                    route = origin.getNetwork().getShortestRouteBetween(gtuCharacteristics.getGTUType(), origin, destination);
                }
                catch (NetworkException exception)
                {
                    throw new GTUException("No possible route for demand from " + origin + " to " + destination
                            + " for GTU of type " + gtuType, exception);
                }
            }
            return new LaneBasedGTUCharacteristics(gtuCharacteristics, laneBasedStrategicalPlannerFactory, route);
        }

    }

}
