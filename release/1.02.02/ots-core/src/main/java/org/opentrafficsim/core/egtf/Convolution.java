package org.opentrafficsim.core.egtf;

import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Utility class for convolution using fast fourier transformation. This utility is specifically tailored to EGTF and not for
 * general fast fourier purposes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 31 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Convolution
{

    /**
     * Private constructor.
     */
    private Convolution()
    {
        //
    }

    /**
     * Program entry point.
     * @param args String...; the command line arguments (not used)
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
                    double[][] out2 = convolution(a, b);
                    t2 = System.currentTimeMillis() - t2;
                    for (int k = 0; k < size[j]; k++)
                    {
                        for (int l = 0; l < size[j]; l++)
                        {
                            if (Math.abs(out1[k][l] - out2[k][l]) > 1e-6)
                            {
                                throw new RuntimeException(
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
     * Convolution of two matrices using fast fourier transform.
     * @param a double[][]; the kernel matrix
     * @param b double[][]; the data matrix
     * @return double[][]; convolution of a over b, same size as b
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

    /**
     * Convolution of two matrices using fast fourier transform.
     * @param a double[][]; the kernel matrix
     * @param b double[][]; the data matrix
     * @return double[][]; convolution of a over b, same size as b
     */
    public static double[][] convolution(final double[][] a, final double[][] b)
    {
        // create zero-padded matrices with dimensions as a power of 2
        int i = a.length + b.length - 1;
        int j = a[0].length + b[0].length - 1;
        int i2 = (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(i));
        int j2 = (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(j));
        double[][] a2 = zeroPadding(a, i2, j2); // copying matrix is also safe, so this effort is worthwhile
        double[][] b2 = zeroPadding(b, i2, j2);
        // fft
        Complex[] a3 = fft2(a2);
        Complex[] b3 = fft2(b2);
        // element-wise product (store in a3)
        for (int k = 0; k < i2; k++)
        {
            for (int m = 0; m < j2; m++)
            {
                double re = a3[k].re[m] * b3[k].re[m] - a3[k].im[m] * b3[k].im[m]; // im depends on re, so need tmp variable
                a3[k].im[m] = a3[k].re[m] * b3[k].im[m] + a3[k].im[m] * b3[k].re[m];
                a3[k].re[m] = re;
            }
        }
        // inverse fft
        ifft2(a3);
        // crop padded zeros (note that the convolution is centered in the resulting matrix, we start at half the size of 'a')
        double[][] out = new double[b.length][b[0].length];
        int fromRow = a.length / 2;
        int fromCol = a[0].length / 2;
        for (int k = 0; k < b.length; k++)
        {
            System.arraycopy(a3[fromRow + k].re, fromCol, out[k], 0, out[k].length);
        }
        return out;
    }

    /**
     * Adds zeros to a matrix to obtain size {@code i x j}.
     * @param x double[][]; original matrix
     * @param i int; number of desired rows
     * @param j int; number of desired columns
     * @return double[][]; {@code x} padded with zeros
     */
    private static double[][] zeroPadding(final double[][] x, final int i, final int j)
    {
        double[][] x2 = new double[i][j];
        for (int k = 0; k < i; k++)
        {
            if (k < x.length)
            {
                System.arraycopy(x[k], 0, x2[k], 0, x[k].length);
            }
        }
        return x2;
    }

    /**
     * Two-dimensional fast fourier transform.
     * @param x double[][]; matrix, this data is affected by the method
     * @return Complex[]; array of complex objects, each representing a row of complex values
     */
    private static Complex[] fft2(final double[][] x)
    {
        Complex[] xComp = new Complex[x.length];
        // create complex objects and perform the row-fft
        for (int i = 0; i < x.length; i++)
        {
            xComp[i] = fft(new Complex(x[i]));
        }
        // perform the column fft
        for (int i = 0; i < x[0].length; i++)
        {
            double[] re = new double[x.length];
            double[] im = new double[x.length];
            for (int j = 0; j < x.length; j++)
            {
                re[j] = xComp[j].re[i];
                im[j] = xComp[j].im[i];
            }
            fft(new Complex(re, im));
            for (int j = 0; j < x.length; j++)
            {
                xComp[j].re[i] = re[j];
                xComp[j].im[i] = im[j];
            }
        }
        return xComp;
    }

    /**
     * Fast fourier transform using Cooley–Tukey algorithm. This method is based on
     * https://introcs.cs.princeton.edu/java/97data/FFT.java.html.
     * @param x Complex; vector of complex objects
     * @return Complex; vector after fourier transform
     */
    private static Complex fft(final Complex x)
    {
        // bit reversal permutation (this simply rearranges the order in a way that happens to work for the butterfly updates)
        int n = x.re.length;
        int shift = 1 + Integer.numberOfLeadingZeros(n);
        for (int k = 0; k < n; k++)
        {
            int j = Integer.reverse(k) >>> shift;
            if (j > k)
            {
                double temp = x.re[j];
                x.re[j] = x.re[k];
                x.re[k] = temp;
                temp = x.im[j];
                x.im[j] = x.im[k];
                x.im[k] = temp;
            }
        }
        // butterfly updates
        for (int l = 2; l <= n; l = l + l)
        {
            double pil = -2.0 * Math.PI / l;
            for (int k = 0; k < l / 2; k++)
            {
                double kth = k * pil;
                double wReal = Math.cos(kth);
                double wImag = Math.sin(kth);
                for (int j = 0; j < n / l; j++)
                {
                    int jlk = j * l + k;
                    int jlkl2 = jlk + l / 2;
                    double xReal = x.re[jlkl2];
                    double xImag = x.im[jlkl2];
                    double taoReal = wReal * xReal - wImag * xImag;
                    double taoImag = wReal * xImag + wImag * xReal;
                    x.re[jlkl2] = x.re[jlk] - taoReal;
                    x.im[jlkl2] = x.im[jlk] - taoImag;
                    x.re[jlk] = x.re[jlk] + taoReal;
                    x.im[jlk] = x.im[jlk] + taoImag;
                }
            }
        }
        return x;
    }

    /**
     * Two-dimensional inverse fourier transform. Result is stored in the input objects.
     * @param x Complex[]; array of complex objects, each representing a row of complex values
     */
    private static void ifft2(final Complex[] x)
    {
        // perform the row ifft
        for (int i = 0; i < x.length; i++)
        {
            ifft(x[i]);
        }
        // perform the column ifft
        for (int i = 0; i < x[0].re.length; i++)
        {
            double[] re = new double[x.length];
            double[] im = new double[x.length];
            int col = i; // effective final
            IntStream.range(0, x.length).forEach(j ->
            {
                re[j] = x[j].re[col];
                im[j] = x[j].im[col];
            });
            ifft(new Complex(re, im));
            IntStream.range(0, x.length).forEach(j ->
            {
                x[j].re[col] = re[j];
                x[j].im[col] = im[j];
            });
        }
    }

    /**
     * Inverse fourier transform. Result is stored in the input object.
     * @param x Complex; vector of complex values
     */
    private static void ifft(final Complex x)
    {
        // conjugate
        int n = x.im.length;
        for (int i = 0; i < n; i++)
        {
            x.im[i] = -x.im[i];
        }
        // forward fft
        fft(x);
        // conjugate and scaling
        for (int i = 0; i < n; i++)
        {
            x.im[i] = -x.im[i] / n;
            x.re[i] = x.re[i] / n;
        }
    }

    /**
     * Class that contains a vector of complex values.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 31 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class Complex
    {

        /** Real part. */
        @SuppressWarnings("visibilitymodifier")
        public final double[] re;

        /** Imaginary part. */
        @SuppressWarnings("visibilitymodifier")
        public final double[] im;

        /**
         * Constructor for zero imaginary part.
         * @param x double[]; real part
         */
        Complex(final double[] x)
        {
            this.re = x;
            this.im = new double[x.length];
        }

        /**
         * Constructor.
         * @param re double[]; real part
         * @param im double[]; imaginary part;
         */
        Complex(final double[] re, final double[] im)
        {
            this.re = re;
            this.im = im;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            StringBuilder str = new StringBuilder("[");
            String sep = "";
            for (int i = 0; i < this.re.length; i++)
            {
                str.append(sep);
                sep = ", ";
                if (this.im[i] >= 0)
                {
                    str.append(String.format(Locale.US, "%.2f+%.2fi", this.re[i], this.im[i]));
                }
                else
                {
                    str.append(String.format(Locale.US, "%.2f-%.2fi", this.re[i], -this.im[i]));
                }
            }
            str.append("]");
            return str.toString();
        }

    }
}
