package org.opentrafficsim.road.network.factory.xml;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SpeedGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 28, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
final class GTUColorerTag
{
    /** Utility class. */
    private GTUColorerTag()
    {
        // do not instantiate
    }

    /** default colorer, optionally set by XMLNetworkLaneParser */
    // TODO solve hack in better xml spec
    static GTUColorer defaultColorer = null;

    /**
     * Parses the right GTUColorer from ID|SPEED|ACCELERATION|LANECHANGEURGE|SWITCHABLE.
     * @param name name of the GTUColorer
     * @param globalTag to define the default parameters of the colorers
     * @return the corresponding GTUColorer
     * @throws SAXException in case the name does not correspond to a known GTUColorer
     */
    static GTUColorer parseGTUColorer(final String name, final GlobalTag globalTag) throws SAXException
    {
        switch (name)
        {
            case "ID":
                return new IDGTUColorer();

            case "SPEED":
                return makeSpeedGTUColorer(globalTag);

            case "ACCELERATION":
                return makeAccelerationGTUColorer(globalTag);

            case "SWITCHABLE":
                return makeSwitchableGTUColorer(globalTag);

            default:
                throw new SAXException("GTUCOLORER: unknown name " + name + " not one of ID|SPEED|ACCELERATION|SWITCHABLE");
        }
    }

    /**
     * @param globalTag to define the default parameters of the colorers
     * @return the corresponding GTUColorer
     */
    static GTUColorer makeSpeedGTUColorer(final GlobalTag globalTag)
    {
        if (defaultColorer != null)
            return defaultColorer;
        if (globalTag.speedGTUColorerMaxSpeed != null)
        {
            return new SpeedGTUColorer(globalTag.speedGTUColorerMaxSpeed);
        }
        return new SpeedGTUColorer(new Speed(100.0, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * @param globalTag to define the default parameters of the colorers
     * @return the corresponding GTUColorer
     */
    static GTUColorer makeAccelerationGTUColorer(final GlobalTag globalTag)
    {
        if (defaultColorer != null)
            return defaultColorer;
        // TODO use parameters for AccelerationGTUColorer
        return new AccelerationGTUColorer(new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2));
    }

    /**
     * @param globalTag to define the default parameters of the colorers
     * @return the corresponding GTUColorer
     */
    static GTUColorer makeSwitchableGTUColorer(final GlobalTag globalTag)
    {
        if (defaultColorer != null)
            return defaultColorer;
        GTUColorer[] gtuColorers =
                new GTUColorer[] { new IDGTUColorer(), makeSpeedGTUColorer(globalTag), makeAccelerationGTUColorer(globalTag) };
        // TODO default colorer
        return new SwitchableGTUColorer(0, gtuColorers);
    }
}
