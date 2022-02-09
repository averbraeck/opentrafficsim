package org.opentrafficsim.road.gtu.generator.headway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;

/**
 * Headway generator that takes it's input from a file.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ListHeadways implements Generator<Duration>
{

    /** Reader for the event list. */
    private final BufferedReader reader;

    /** Time of previous GTU. */
    private double prev = 0.0;

    /**
     * Constructor using file.
     * @param fileName String; file with arrival times
     */
    public ListHeadways(final String fileName)
    {
        try
        {
            // TODO: from URI as defined in xsd
            this.reader = new BufferedReader(new FileReader(new File(fileName)));
        }
        catch (FileNotFoundException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Duration draw() throws ProbabilityException, ParameterException
    {
        try
        {
            String line = null;
            do
            {
                line = this.reader.readLine();
                if (null == line)
                {
                    return null; // End of input; do not re-schedule
                }
            }
            while (line.equals("")); // ignore blank lines
            double when = Double.parseDouble(line);
            if (when < this.prev)
            {
                throw new RuntimeException(
                        "Arrival times from file are not in chronological order (" + when + "<" + this.prev + ").");
            }
            Duration headway = Duration.instantiateSI(when - this.prev);
            this.prev = when;
            return headway;
        }
        catch (NumberFormatException | IOException exception)
        {
            throw new RuntimeException("Unable to read arrival time from file.", exception);
        }
    }

}
