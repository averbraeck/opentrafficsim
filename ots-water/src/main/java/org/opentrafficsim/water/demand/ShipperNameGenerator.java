package org.opentrafficsim.water.demand;

import java.util.ArrayList;
import java.util.List;

/**
 * Give the shipper a name.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ShipperNameGenerator
{
    /** counter. */
    private static int counter = 0;

    /** names to choose from. */
    private static List<String> names = new ArrayList<String>();

    /** specific instance of the generator. */
    private static ShipperNameGenerator instance = null;

    /**
     * Create a name generator.
     * @return a new generator.
     */
    public static ShipperNameGenerator getInstance()
    {
        if (instance == null)
        {
            instance = new ShipperNameGenerator();
        }
        return instance;
    }

    /**
     * Add a name, e.g. from a large name file.
     * @param name String; the name to add
     */
    public final void addShipperName(final String name)
    {
        names.add(name);
    }

    /**
     * Give a name.
     * @return name
     */
    public final String getShipperName()
    {
        String name = "RandomName" + counter;
        if (counter < names.size())
        {
            name = names.get(counter);
        }

        counter++;
        return name;
    }
}
