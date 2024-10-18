package org.opentrafficsim.road.gtu.generator;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuErrorHandler;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.LanePosition;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simple class to spawn GTUs.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @author Christoph Thees
 */
public class GtuSpawner
{

    /** Use default NL GTU templates. */
    private boolean useDefaultGtuTemplate = true;

    /** Random stream. */
    private StreamInterface stream = new MersenneTwister(123L);

    /** Length over which no lane changes will happen. */
    private Length noLaneChangeDistance = Length.instantiateSI(100.0);

    /** Instantaneous lane changes. */
    private boolean instantaneousLaneChanges = false;

    /** Error handler. */
    private GtuErrorHandler errorHandler = GtuErrorHandler.THROW;

    /**
     * Sets the use of default NL GTU templates.
     * @param useDefaultGtuTemplate use of default NL GTU templates.
     * @return for method chaining.
     */
    public GtuSpawner setUseDefaultGtuTemplate(final boolean useDefaultGtuTemplate)
    {
        this.useDefaultGtuTemplate = useDefaultGtuTemplate;
        return this;
    }

    /**
     * Sets random stream.
     * @param stream random stream.
     * @return for method chaining.
     */
    public GtuSpawner setStream(final StreamInterface stream)
    {
        this.stream = stream;
        return this;
    }

    /**
     * Sets no lane change distance.
     * @param noLaneChangeDistance no lane change distance.
     * @return for method chaining.
     */
    public GtuSpawner setNoLaneChangeDistance(final Length noLaneChangeDistance)
    {
        this.noLaneChangeDistance = noLaneChangeDistance;
        return this;
    }

    /**
     * Sets instantaneous lane changes.
     * @param instantaneousLaneChanges instantaneous lane changes.
     * @return for method chaining.
     */
    public GtuSpawner setInstantaneousLaneChanges(final boolean instantaneousLaneChanges)
    {
        this.instantaneousLaneChanges = instantaneousLaneChanges;
        return this;
    }

    /**
     * Sets the error handler.
     * @param errorHandler error handler.
     * @return for method chaining.
     */
    public GtuSpawner setErrorHandler(final GtuErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Create a single GTU.
     * @param id id.
     * @param templateGtuType characteristics.
     * @param network network.
     * @param speed speed.
     * @param position position.
     * @throws GtuException when initial GTU values are not correct
     * @throws OtsGeometryException when the initial path is wrong
     * @throws NetworkException when the GTU cannot be placed on the given lane
     */
    public void spawnGtu(final String id, final LaneBasedGtuCharacteristics templateGtuType, final RoadNetwork network,
            final Speed speed, final LanePosition position) throws GtuException, OtsGeometryException, NetworkException
    {

        if (this.useDefaultGtuTemplate)
        {
            GtuType.registerTemplateSupplier(templateGtuType.getGtuType(), Defaults.NL);
        }
        GtuCharacteristics defaultCharacteristics =
                Try.assign(() -> GtuType.defaultCharacteristics(templateGtuType.getGtuType(), network, this.stream),
                        "Failed getting default Characteristics");

        LaneBasedGtu gtu =
                new LaneBasedGtu(id, templateGtuType.getGtuType(), templateGtuType.getLength(), templateGtuType.getWidth(),
                        defaultCharacteristics.getMaximumSpeed(), templateGtuType.getLength().divide(2), network);

        gtu.setMaximumAcceleration(defaultCharacteristics.getMaximumAcceleration());
        gtu.setMaximumDeceleration(defaultCharacteristics.getMaximumDeceleration());
        gtu.setVehicleModel(templateGtuType.getVehicleModel());
        gtu.setNoLaneChangeDistance(this.noLaneChangeDistance);
        gtu.setInstantaneousLaneChange(this.instantaneousLaneChanges);
        gtu.setErrorHandler(this.errorHandler);

        gtu.init(templateGtuType.getStrategicalPlannerFactory().create(gtu, templateGtuType.getRoute(),
                templateGtuType.getOrigin(), templateGtuType.getDestination()), position, speed);
    }

}
