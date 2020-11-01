/**
 * 
 */
package org.opentrafficsim.water.statistics;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

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
@XStreamAlias("persistent")
public class XPersistent extends XTally
{
    /** */
    @XStreamOmitField
    private static final long serialVersionUID = 1L;

    /** */
    @XStreamOmitField
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** startTime defines the time of the first event. */
    @XStreamOmitField
    private double startTime = Double.NaN;

    /** elapsedTime tracks the elapsed time. */
    private double elapsedTime = Double.NaN;

    /** lastvalue tracks the last value. */
    @XStreamOmitField
    private double lastValue = Double.NaN;

    /**
     * @param description String; description of the statistic
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     */
    public XPersistent(String description, DEVSSimulatorInterface.TimeDoubleUnit simulator)
    {
        super(description);
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public double getSampleMean()
    {
        if (super.n > 1 && this.elapsedTime > 0)
            return super.sum / elapsedTime;
        else
            return Double.NaN;
    }

    /** {@inheritDoc} */
    @Override
    public double getStdDev()
    {
        synchronized (this.semaphore)
        {
            if (super.n > 1)
            {
                return Math.sqrt(super.varianceSum / this.elapsedTime);
            }
            return Double.NaN;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getSampleVariance()
    {
        synchronized (this.semaphore)
        {
            if (super.n > 1)
            {
                return super.varianceSum / this.elapsedTime;
            }
            return Double.NaN;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void initialize()
    {
        synchronized (this.semaphore)
        {
            super.initialize();
            this.elapsedTime = 0.0;
            this.lastValue = 0.0;
        }
    }

    /**
     * dstat.
     * @param value double; value
     */
    public void persist(final double value)
    {
        if (!Double.isNaN(value))
        {
            synchronized (this.semaphore)
            {
                double simTime = this.simulator.getSimulatorTime().si;
                super.n++;
                if (value < super.min)
                    super.min = value;
                if (value > super.max)
                    super.max = value;

                if (super.n == 1)
                    this.startTime = simTime;
                else
                {
                    double deltaTime = simTime - (this.elapsedTime + this.startTime);
                    if (deltaTime > 0.0)
                    {
                        super.sum += lastValue * deltaTime;
                        super.varianceSum += lastValue * lastValue * deltaTime;
                        this.elapsedTime = this.elapsedTime + deltaTime;
                    }
                }
                this.lastValue = value;
            }
        }
    }

    /**
     * @see org.opentrafficsim.water.statistics.XTally#writeToExcel(org.apache.poi.ss.usermodel.Sheet, int)
     */
    @Override
    public int writeToExcel(final Sheet sheet, final int startRow)
    {
        DataFormat format = sheet.getWorkbook().createDataFormat();
        CellStyle style = sheet.getWorkbook().createCellStyle();
        style.setDataFormat(format.getFormat("0.00"));

        int rownr = startRow;
        Row row = sheet.createRow(rownr);

        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue("tijdgewogen [n, gem, stdev, min, max]");
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

}
