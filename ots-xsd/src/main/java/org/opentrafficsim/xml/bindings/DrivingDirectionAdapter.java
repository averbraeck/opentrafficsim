package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.DrivingDirectionType;

/**
 * DrivingDirectionAdapter to convert between XML representations of a driving direction, and an enum type.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DrivingDirectionAdapter extends XmlAdapter<String, DrivingDirectionType>
{
    /** {@inheritDoc} */
    @Override
    public DrivingDirectionType unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String clean = field.replaceAll("\\s", "");
            if (clean.equals("FORWARD"))
            {
                return DrivingDirectionType.DIR_PLUS;
            }
            if (clean.equals("BACKWARD"))
            {
                return DrivingDirectionType.DIR_MINUS;
            }
            if (clean.equals("BOTH"))
            {
                return DrivingDirectionType.DIR_BOTH;
            }
            if (clean.equals("NONE"))
            {
                return DrivingDirectionType.DIR_NONE;
            }
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing DrivingDirection '" + field + "'");
            throw new IllegalArgumentException("Error parsing DrivingDirection " + field, exception);
        }
        CategoryLogger.always().error("Problem parsing DrivingDirextion '" + field + "'");
        throw new IllegalArgumentException("Error parsing DrivingDirection " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final DrivingDirectionType drivingDirection) throws IllegalArgumentException
    {
        if (drivingDirection.equals(DrivingDirectionType.DIR_PLUS))
            return "FORWARD";
        if (drivingDirection.equals(DrivingDirectionType.DIR_MINUS))
            return "BACKWARD";
        if (drivingDirection.equals(DrivingDirectionType.DIR_BOTH))
            return "BOTH";
        return "NONE";
    }

}
