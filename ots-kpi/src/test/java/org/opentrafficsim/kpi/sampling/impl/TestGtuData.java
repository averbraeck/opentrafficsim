package org.opentrafficsim.kpi.sampling.impl;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Test GtuData class.
 * @param getId id
 * @param getOriginId origin id
 * @param getDestinationId destination id
 * @param getGtuTypeId GTU type id
 * @param getRouteId route id
 * @param getReferenceSpeed reference speed
 */
public record TestGtuData(String getId, String getOriginId, String getDestinationId, String getGtuTypeId, String getRouteId,
        Speed getReferenceSpeed) implements GtuData
{

}
