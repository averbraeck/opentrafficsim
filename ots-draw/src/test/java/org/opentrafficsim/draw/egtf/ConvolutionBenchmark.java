package org.opentrafficsim.draw.egtf;

import org.opentrafficsim.base.OtsRuntimeException;

/**
 * Performs a benchmark on Convolution using the classic or FFT method.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ConvolutionBenchmark
{

    /**
     * Constructor.
     */
    private ConvolutionBenchmark()
    {
        //
    }

    /**
     * Program entry point.
     * @param args the command line arguments (not used)
     */
    public static void main(final String... args)
    {
        int[] size = new int[] {10, 12, 15, 18, 20, 25, 30, 35, 50, 100, 200, 500, 1000};
        for (int i = 0; i < size.length; i++)
        {
            for (int j = 0; j < size.length; j++)
            {
                if (size[i] * size[j] <= 100000)
                {
                    double[][] a = new double[size[i]][size[i]];
                    for (int k = 0; k < size[i]; k++)
                    {
                        for (int l = 0; l < size[i]; l++)
                        {
                            a[k][l] = Math.random();
                        }
                    }
                    double[][] b = new double[size[j]][size[j]];
                    for (int k = 0; k < size[j]; k++)
                    {
                        for (int l = 0; l < size[j]; l++)
                        {
                            b[k][l] = Math.random() * 35.0;
                        }
                    }
                    long t1 = System.currentTimeMillis();
                    double[][] out1 = conv(a, b);
                    t1 = System.currentTimeMillis() - t1;
                    long t2 = System.currentTimeMillis();
                    double[][] out2 = Convolution.convolution(a, b);
                    t2 = System.currentTimeMillis() - t2;
                    for (int k = 0; k < size[j]; k++)
                    {
                        for (int l = 0; l < size[j]; l++)
                        {
                            if (Math.abs(out1[k][l] - out2[k][l]) > 1e-6)
                            {
                                throw new OtsRuntimeException(
                                        String.format("output unequal: %.16f vs. %.16f", out1[k][l], out2[k][l]));
                            }
                        }
                    }
                    System.out.println(String.format("a = %d, b = %d: tConv = %dms, tFft = %dms, gain = %dms", size[i], size[j],
                            t1, t2, t2 - t1));
                }
            }
        }
    }

    /**
     * Convolution of two matrices using classical method.
     * @param a the kernel matrix
     * @param b the data matrix
     * @return convolution of a over b, same size as b
     */
    private static double[][] conv(final double[][] a, final double[][] b)
    {
        double[][] out2 = new double[b.length][b[0].length];
        int fromRow2 = a.length / 2;
        int fromCol2 = a[0].length / 2;
        for (int i = 0; i < b.length; i++)
        {
            for (int j = 0; j < b[0].length; j++)
            {
                for (int k = 0; k < a.length; k++)
                {
                    for (int l = 0; l < a[0].length; l++)
                    {
                        int m = i - k + fromRow2;
                        int n = j - l + fromCol2;
                        if (m >= 0 && n >= 0 && m < b.length && n < b[0].length)
                        {
                            out2[i][j] += a[k][l] * b[m][n];
                        }
                    }
                }
            }
        }
        return out2;
    }

}
