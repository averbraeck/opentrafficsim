package org.opentrafficsim.core.egtf;

import static org.junit.Assert.assertFalse;

import java.util.Locale;

import org.djutils.exceptions.Try;
import org.junit.Test;
import org.opentrafficsim.core.egtf.typed.TypedQuantity;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class EgtfTest
{

    /** Margin for bigger/smaller-than relations. */
    private static final double MARGIN = 1e-6;

    /** Tests if combined data results in sensible outcomes, e.g. more towards other sources if source less reliable. */
    @Test
    public void combineSourcesTest()
    {
        EGTF egtf = new EGTF();
        DataSource det = egtf.getDataSource("detectors");
        DataSource fcd = egtf.getDataSource("fcd");
        DataStream<?> detSpeed = det.addStreamSI(TypedQuantity.SPEED, 10.0, 5.0);
        DataStream<?> detFlow = det.addStreamSI(TypedQuantity.FLOW, 1.0, 1.0);
        DataStream<?> fcdSpeed = fcd.addStreamSI(TypedQuantity.SPEED, 2.0, 1.0);
        // second EGTF with less reliable FCD data
        EGTF egtf2 = new EGTF();
        DataSource det2 = egtf2.getDataSource("detectors");
        DataSource fcd2 = egtf2.getDataSource("fcd");
        DataStream<?> detSpeed2 = det2.addStreamSI(TypedQuantity.SPEED, 10.0, 5.0);
        DataStream<?> detFlow2 = det2.addStreamSI(TypedQuantity.FLOW, 1.0, 1.0);
        DataStream<?> fcdSpeed2 = fcd2.addStreamSI(TypedQuantity.SPEED, 5.0, 2.5);
        // add uniform detector data
        double dx = 500;
        double dt = 60;
        double xmax = 2000;
        double tmax = 300;
        double[] x = new double[1 + (int) (xmax / dx)];
        double[] t = new double[1 + (int) (tmax / dt)];
        for (int i = 0; i < x.length; i++)
        {
            x[i] = i * dx;
        }
        for (int j = 0; j < t.length; j++)
        {
            t[j] = j * dt;
        }
        double vFree = 33.0;
        double qFree = 0.5;
        double[][] vFreeGrid = new double[x.length][t.length];
        for (int i = 0; i < x.length; i++)
        {
            double location = x[i];
            double[] qFreeVector = new double[t.length];
            double[] qFreeTime = new double[t.length];
            double[] qFreeSpace = new double[t.length];
            for (int j = 0; j < t.length; j++)
            {
                double time = t[j];
                vFreeGrid[i][j] = vFree;
                qFreeVector[j] = qFree;
                qFreeTime[j] = time;
                qFreeSpace[j] = location;
                egtf2.addPointDataSI(detSpeed2, location, time, vFree);
                egtf2.addPointDataSI(detFlow2, location, time, qFree);
            }
            egtf.addVectorDataSI(detFlow, qFreeSpace, qFreeTime, qFreeVector);
        }
        egtf.addGridDataSI(detSpeed, x, t, vFreeGrid);
        // Filter filterDet = egtf.filterSI(x, t, TypedQuantity.SPEED, TypedQuantity.FLOW);
        Filter filterDet = egtf.filterFastSI(0.0, dx, xmax, 0.0, dt, tmax, TypedQuantity.SPEED, TypedQuantity.FLOW);
        // add FCD data
        double location = 1200;
        double time = 130;
        double v = 10;
        for (int i = 0; i < 20; i++)
        {
            egtf.addPointDataSI(fcdSpeed, location, time, v);
            egtf2.addPointDataSI(fcdSpeed2, location, time, v);
            location += v;
            time += 1.0;
        }
        Filter filterCom1 = egtf.filterSI(x, t, TypedQuantity.SPEED, TypedQuantity.FLOW);
        Filter filterCom12 = egtf2.filterSI(x, t, TypedQuantity.SPEED, TypedQuantity.FLOW);
        // add some more FCD data
        location = 1100;
        time = 140;
        for (int i = 0; i < 20; i++)
        {
            egtf.addPointDataSI(fcdSpeed, location, time, v);
            location += v;
            time += 1.0;
        }
        Filter filterCom2 = egtf.filterSI(x, t, TypedQuantity.SPEED, TypedQuantity.FLOW);
        // compare
        double[][] vDet = filterDet.getSI(TypedQuantity.SPEED);
        double[][] qDet = filterDet.getSI(TypedQuantity.FLOW);
        double[][] vCom1 = filterCom1.getSI(TypedQuantity.SPEED);
        double[][] qCom1 = filterCom1.getSI(TypedQuantity.FLOW);
        double[][] vCom12 = filterCom12.getSI(TypedQuantity.SPEED);
        double[][] qCom12 = filterCom12.getSI(TypedQuantity.FLOW);
        double[][] vCom2 = filterCom2.getSI(TypedQuantity.SPEED);
        double[][] qCom2 = filterCom2.getSI(TypedQuantity.FLOW);
        for (int i = 0; i < x.length; i++)
        {
            for (int j = 0; j < t.length; j++)
            {
                assertFalse("Speed filtered with detector data is not equal to uniform input speed",
                        Math.abs(vDet[i][j] - vFree) > MARGIN);
                assertFalse("Speed filtered with FCD data is above speed filtered with detector data",
                        vCom1[i][j] > vDet[i][j]);
                assertFalse("Speed filtered with FCD data is below minimum speed data", vCom1[i][j] < v);
                assertFalse("Speed filtered with unreliable FCD data is below speed filtered FCD data",
                        vCom12[i][j] < vCom1[i][j]);
                assertFalse("Speed filtered with additional FCD data is above speed filtered FCD data",
                        vCom2[i][j] > vCom1[i][j]);
                assertFalse("Flow filtered with detector data is not equal to uniform input flow",
                        Math.abs(qDet[i][j] - 0.5) > MARGIN);
                assertFalse("Flow filtered with FCD data is not equal to uniform input flow",
                        Math.abs(qCom1[i][j] - 0.5) > MARGIN);
                assertFalse("Flow filtered with unreliable FCD data is not equal to uniform input flow",
                        Math.abs(qCom12[i][j] - 0.5) > MARGIN);
                assertFalse("Flow filtered with additional FCD data is not equal to uniform input flow",
                        Math.abs(qCom2[i][j] - 0.5) > MARGIN);
            }
        }
        // printMatrix(vDet, "vDet");
        // printMatrix(vCom1, "vCom1");
        // printMatrix(vCom12, "vCom12");
        // printMatrix(vCom2, "vCom2");
        // printMatrix(qDet, "qDet");
        // printMatrix(qCom1, "qCom1");
        // printMatrix(qCom12, "qCom12");
        // printMatrix(qCom2, "qCom2");
    }

    /**
     * Prints a matrix, also useful for Matlab copy/paste.
     * @param matrix matrix
     * @param name name
     */
    @SuppressWarnings("unused")
    private void printMatrix(final double[][] matrix, final String name)
    {
        System.out.println(name + " = [");
        for (double[] arr : matrix)
        {
            StringBuilder str = new StringBuilder();
            String sep = "";
            for (double d : arr)
            {
                str.append(sep).append(String.format(Locale.ROOT, "%.2f", d));
                sep = ", ";
            }
            System.out.println(" " + str.append(";"));
        }
        System.out.println("];");
    }

    /** Tests whether exception are thrown with inappropriate usages. */
    @Test
    public void exceptionsTest()
    {
        EGTF egtf = new EGTF();
        DataSource det = egtf.getDataSource("detectors");
        DataStream<?> detSpeed = det.addStreamSI(TypedQuantity.SPEED, 10.0, 5.0);
        Try.testFail(() -> det.addStreamSI(TypedQuantity.SPEED, 5.0, 2.5), "Double speed quantity for detectors should fail.");
        Try.testFail(() -> egtf.addVectorDataSI(detSpeed, new double[2], new double[3], new double[3]),
                "Unequal lengths should result in a fail.", IllegalArgumentException.class);
        Try.testFail(() -> egtf.addVectorDataSI(detSpeed, new double[3], new double[2], new double[3]),
                "Unequal lengths should result in a fail.", IllegalArgumentException.class);
        Try.testFail(() -> egtf.addVectorDataSI(detSpeed, new double[3], new double[3], new double[2]),
                "Unequal lengths should result in a fail.", IllegalArgumentException.class);
    }

}
