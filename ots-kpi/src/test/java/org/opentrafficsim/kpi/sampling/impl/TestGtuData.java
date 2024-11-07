package org.opentrafficsim.kpi.sampling.impl;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Test GtuData class. 
 */
public class TestGtuData implements GtuData
{

    /** Id. */
    private final String id;

    /** Origin id. */
    private final String originId;

    /** Destination id. */
    private final String destinationId;

    /** GTU type id. */
    private final String gtuTypeId;

    /** Route id. */
    private final String routeId;

    /** Reference speed. */
    private final Speed referenceSpeed;

    /**
     * Constructor.
     * @param id id
     * @param originId origin id
     * @param destinationId destination id
     * @param gtuTypeId GTU type id
     * @param routeId route id
     * @param referenceSpeed reference speed
     */
    public TestGtuData(final String id, final String originId, final String destinationId, final String gtuTypeId,
            final String routeId, final Speed referenceSpeed)
    {
        this.id = id;
        this.originId = originId;
        this.destinationId = destinationId;
        this.gtuTypeId = gtuTypeId;
        this.routeId = routeId;
        this.referenceSpeed = referenceSpeed;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getOriginId()
    {
        return this.originId;
    }

    @Override
    public String getDestinationId()
    {
        return this.destinationId;
    }

    @Override
    public String getGtuTypeId()
    {
        return this.gtuTypeId;
    }

    @Override
    public String getRouteId()
    {
        return this.routeId;
    }

    @Override
    public Speed getReferenceSpeed()
    {
        return this.referenceSpeed;
    }

}
