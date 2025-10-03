package org.opentrafficsim.demo.doc.tutorials;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.profile.Profile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OtsReplication;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.perception.AbstractHistorical;
import org.opentrafficsim.core.perception.AbstractHistorical.EventValue;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("all")
public class Development
{

    static
    {
        // @docs/08-tutorials/development.md#how-to-profile-code-performance
        Profile.start();
        // profiled code
        Profile.end();

        Profile.start("Lane structure update");
        // profiled code
        Profile.end("Lane structure update");

        Profile.setPrintInterval(2000);
        Profile.print();
    }

    // @docs/08-tutorials/development.md#how-to-create-a-junit-test
    @Test
    public final void defaultsTest()
    {
        Parameters params = new ParameterSet().setDefaultParameters(ParameterTypes.class);
        try
        {
            assertTrue(params.getParameter(ParameterTypes.A).equals(ParameterTypes.A.getDefaultValue()),
                    "Default value is not correctly set.");
        }
        catch (ParameterException exception)
        {
            fail("Default value is not set at all.");
        }
    }

    static class JUnitTest
    {
        OtsReplication replication = null;

        Time time = null;

        private void mock()
        {
            // @docs/08-tutorials/development.md#how-to-create-a-junit-test
            OtsSimulatorInterface simulatorMock = Mockito.mock(OtsSimulatorInterface.class);
            Mockito.when(simulatorMock.getSimulatorTime()).then((invocation) -> this.time);
            Mockito.when(simulatorMock.getReplication()).thenReturn(this.replication);
        }
    }

    // @docs/08-tutorials/development.md#how-to-make-a-property-historical
    public class HistoricalMatrix extends AbstractHistorical<double[][], EventMatrix>
    {

        private final double[][] matrix;

        protected HistoricalMatrix(final HistoryManager historyManager, final Object owner, final double[][] matrix)
        {
            super(historyManager, owner);
            this.matrix = matrix;
        }

        public double[][] getMatrix()
        {
            double[][] out = new double[this.matrix.length][];
            for (int i = 0; i < this.matrix.length; i++)
            {
                out[i] = new double[this.matrix[i].length];
                System.arraycopy(this.matrix[i], 0, out[i], 0, this.matrix[i].length);
            }
            return out;
        }

        // @docs/08-tutorials/development.md#how-to-make-a-property-historical
        public void setValue(final int i, final int j, final double value)
        {
            addEvent(new EventMatrix(now().si, i, j, this.matrix[i][j]));
            this.matrix[i][j] = value;
        }

        // @docs/08-tutorials/development.md#how-to-make-a-property-historical
        public double[][] getMatrix(final Duration time)
        {
            double[][] out = getMatrix();
            for (EventMatrix event : getEvents(time))
            {
                event.restore(out);
            }
            return out;
        }

    }

    // @docs/08-tutorials/development.md#how-to-make-a-property-historical
    public class EventMatrix extends EventValue<Double>
    {

        private final int i;

        private final int j;

        public EventMatrix(final double time, final int i, final int j, final double value)
        {
            super(time, value);
            this.i = i;
            this.j = j;
        }

        public void restore(final double[][] matrix)
        {
            matrix[this.i][this.j] = getValue();
        }

    }

}
