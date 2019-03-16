/**
 * 
 */
package org.opentrafficsim.water.statistics;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.statistics.Tally;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * <br>
 * Copyright (c) 2011-2013 TU Delft, Faculty of TBM, Systems and Simulation <br>
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br>
 * @version Mar 24, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 */
@XStreamAlias("tally")
@SuppressWarnings("checkstyle:visibilitymodifier")
public class XTally implements Serializable
{
    /** */
    @XStreamOmitField
    private static final long serialVersionUID = 1L;

    /** sum refers to the sum of the tally. */
    protected double sum = Double.NaN;

    /** min refers to the min of the tally. */
    protected double min = Double.NaN;

    /** maxrefers to the max of the tally. */
    protected double max = Double.NaN;

    /** varianceSum refers to the varianceSum of the tally. */
    protected double varianceSum = Double.NaN;

    /** n refers to the number of measurements. */
    protected long n = Long.MIN_VALUE;

    /** description refers to the description of this tally. */
    protected String description;

    /** the confidenceDistribution. */
    @XStreamOmitField
    private DistNormal confidenceDistribution = new DistNormal(new MersenneTwister());

    /** the semaphore. */
    @XStreamOmitField
    protected Object semaphore = new Object();

    /** LEFT_SIDE_CONFIDENCE refers to the left side confidence. */
    @XStreamOmitField
    public static final short LEFT_SIDE_CONFIDENCE = -1;

    /** BOTH_SIDE_CONFIDENCE refers to both sides of the confidence. */
    @XStreamOmitField
    public static final short BOTH_SIDE_CONFIDENCE = 0;

    /** RIGTH_SIDE_CONFIDENCE refers to the right side confidence. */
    @XStreamOmitField
    public static final short RIGTH_SIDE_CONFIDENCE = 1;

    /**
     * @param description String; description of the statistic
     */
    public XTally(final String description)
    {
        super();
        this.description = description;
    }

    /**
     * Returns the sampleMean of all oberservations since the initialization.
     * @return double the sampleMean
     */
    public double getSampleMean()
    {
        if (this.n > 0)
            return this.sum / this.n;
        else
            return Double.NaN;
    }

    /**
     * returns the confidence interval on either side of the mean.
     * @param alpha double; Alpha is the significance level used to compute the confidence level. The confidence level equals
     *            100*(1 - alpha)%, or in other words, an alpha of 0.05 indicates a 95 percent confidence level.
     * @return double[] the confidence interval of this tally
     */
    public double[] getConfidenceInterval(final double alpha)
    {
        return this.getConfidenceInterval(alpha, Tally.BOTH_SIDE_CONFIDENCE);
    }

    /**
     * returns the confidence interval based of the mean.
     * @param alpha double; Alpha is the significance level used to compute the confidence level. The confidence level equals
     *            100*(1 - alpha)%, or in other words, an alpha of 0.05 indicates a 95 percent confidence level.
     * @param side short; the side of the confidence interval with respect to the mean
     * @return double[] the confidence interval of this tally
     */
    public double[] getConfidenceInterval(final double alpha, final short side)
    {
        if (!(side == LEFT_SIDE_CONFIDENCE || side == BOTH_SIDE_CONFIDENCE || side == RIGTH_SIDE_CONFIDENCE))
        {
            throw new IllegalArgumentException("side of confidence level is not defined");
        }
        if (alpha < 0 || alpha > 1)
        {
            throw new IllegalArgumentException("1 >= confidenceLevel >= 0");
        }
        synchronized (this.semaphore)
        {
            if (Double.isNaN(this.getSampleMean()) || Double.isNaN(this.getStdDev()))
            {
                return null;
            }
            double level = 1 - alpha;
            if (side == Tally.BOTH_SIDE_CONFIDENCE)
            {
                level = 1 - alpha / 2.0;
            }
            double z = this.confidenceDistribution.getInverseCumulativeProbability(level);
            double confidence = z * Math.sqrt(this.getSampleVariance() / this.n);
            double[] result = {this.getSampleMean() - confidence, this.getSampleMean() + confidence};
            if (side == Tally.LEFT_SIDE_CONFIDENCE)
            {
                result[1] = this.getSampleMean();
            }
            if (side == Tally.RIGTH_SIDE_CONFIDENCE)
            {
                result[0] = this.getSampleMean();
            }
            result[0] = Math.max(result[0], this.min);
            result[1] = Math.min(result[1], this.max);
            return result;
        }
    }

    /**
     * returns the description of this tally.
     * @return Sting description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the max.
     * @return double
     */
    public double getMax()
    {
        return this.max;
    }

    /**
     * Returns the min.
     * @return double
     */
    public double getMin()
    {
        return this.min;
    }

    /**
     * Returns the number of observations.
     * @return long n
     */
    public long getN()
    {
        return this.n;
    }

    /**
     * Returns the current tally standard deviation.
     * @return double the standard deviation
     */
    public double getStdDev()
    {
        synchronized (this.semaphore)
        {
            if (this.n > 1)
            {
                return Math.sqrt(this.varianceSum / (1.0 * this.n - 1.0));
            }
            return Double.NaN;
        }
    }

    /**
     * returns the sum of the values of the observations.
     * @return double sum
     */
    public double getSum()
    {
        return this.sum;
    }

    /**
     * Returns the current tally variance.
     * @return double samplevariance
     */
    public double getSampleVariance()
    {
        synchronized (this.semaphore)
        {
            if (this.n > 1)
            {
                return this.varianceSum / (1.0 * this.n - 1.0);
            }
            return Double.NaN;
        }
    }

    /**
     * initializes the Tally. This methods sets the max, min, n, sum and variance values to their initial values.
     */
    public void initialize()
    {
        synchronized (this.semaphore)
        {
            this.max = -Double.MAX_VALUE;
            this.min = Double.MAX_VALUE;
            this.n = 0;
            this.sum = 0.0;
            this.varianceSum = 0.0;
        }
    }

    /**
     * is this tally initialized?
     * @return true whenever this.initialize is invoked.
     */
    public boolean isInitialized()
    {
        return !Double.isNaN(this.max);
    }

    /**
     * tally.
     * @param value double; the value
     */
    public void tally(final double value)
    {
        if (!Double.isNaN(value))
        {
            synchronized (this.semaphore)
            {
                this.varianceSum += value * value;
                this.sum += value;
                this.n += 1;
                if (value < this.min)
                    this.min = value;
                if (value > this.max)
                    this.max = value;
            }
        }
    }

    /**
     * Write statistics to an excel spreadsheet, starting on row "startRow".
     * @param sheet Sheet; the excel sheet to write to
     * @param startRow int; the first row of writing
     * @return first free row after writing
     */
    public int writeToExcel(final Sheet sheet, final int startRow)
    {
        DataFormat format = sheet.getWorkbook().createDataFormat();
        CellStyle style = sheet.getWorkbook().createCellStyle();
        style.setDataFormat(format.getFormat("0.00"));

        int rownr = startRow;
        Row row = sheet.createRow(rownr);

        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue("tally [n, gem, stdev, min, max]");
        row.createCell(3).setCellValue(getN());
        if (getN() > 0)
        {
            row.createCell(4).setCellValue(getSampleMean());
            row.getCell(4).setCellStyle(style);
            if (getN() > 1 && !Double.isNaN(getStdDev()))
            {
                row.createCell(5).setCellValue(getStdDev());
                row.getCell(5).setCellStyle(style);
            }
            row.createCell(6).setCellValue(getMin());
            row.getCell(6).setCellStyle(style);
            row.createCell(7).setCellValue(getMax());
            row.getCell(7).setCellStyle(style);
        }

        return rownr + 1;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.description;
    }

}
