package org.opentrafficsim.road.network.factory.vissim;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.IDGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
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

    /**
     * Parses the right GTUColorer from ID|SPEED|ACCELERATION|LANECHANGEURGE|SWITCHABLE.
     * @param name String; name of the GTUColorer
     * @param globalTag GlobalTag; to define the default parameters of the colorers
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
     * @param globalTag GlobalTag; to define the default parameters of the colorers
     * @return the corresponding GTUColorer
     */
    static GTUColorer makeSpeedGTUColorer(final GlobalTag globalTag)
    {
        if (globalTag.speedGTUColorerMaxSpeed != null)
        {
            return new SpeedGTUColorer(globalTag.speedGTUColorerMaxSpeed);
        }
        return new SpeedGTUColorer(new Speed(100.0, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * @param globalTag GlobalTag; to define the default parameters of the colorers
     * @return the corresponding GTUColorer
     */
    static GTUColorer makeAccelerationGTUColorer(final GlobalTag globalTag)
    {
        // TODO use parameters for AccelerationGTUColorer
        return new AccelerationGTUColorer(new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2));
    }

    /**
     * @param globalTag GlobalTag; to define the default parameters of the colorers
     * @return the corresponding GTUColorer
     */
    static GTUColorer makeSwitchableGTUColorer(final GlobalTag globalTag)
    {
        GTUColorer[] gtuColorers =
                new GTUColorer[] {new IDGTUColorer(), makeSpeedGTUColorer(globalTag), makeAccelerationGTUColorer(globalTag)};
        // TODO default colorer
        return new SwitchableGTUColorer(0, gtuColorers);
    }
}
