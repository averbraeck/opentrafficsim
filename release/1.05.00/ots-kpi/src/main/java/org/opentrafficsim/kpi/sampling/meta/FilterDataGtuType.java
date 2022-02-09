package org.opentrafficsim.kpi.sampling.meta;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;

/**
 * Accepts trajectories with a GTUType included in a set in a query.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FilterDataGtuType extends FilterDataType<GtuTypeDataInterface>
{

    /**
     * Constructor.
     */
    public FilterDataGtuType()
    {
        super("gtuType");
    }

    /** {@inheritDoc} */
    @Override
    public final GtuTypeDataInterface getValue(final GtuDataInterface gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        return gtu.getGtuTypeData();
    }

    /** {@inheritDoc} */
    @Override
    public String formatValue(final String format, final GtuTypeDataInterface value)
    {
        return value.getId();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "FilterDataGtuType: [id=" + getId() + "]";
    }

}
