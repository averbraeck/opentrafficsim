/**
 * 
 */
package org.opentrafficsim.water.statistics;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * <br>
 * Copyright (c) 2011-2013 TU Delft, Faculty of TBM, Systems and Simulation <br>
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br>
 * @version Apr 1, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 */
@XStreamAlias("counter")
public class XCounter implements Serializable
{
    /** */
    @XStreamOmitField
    private static final long serialVersionUID = 1L;

    /** count represents the value of the counter. */
    protected long count = Long.MIN_VALUE;

    /** n represents the number of measurements. */
    protected long n = Long.MIN_VALUE;

    /** description refers to the title of this counter. */
    protected String description;

    /** the semaphore. */
    @XStreamOmitField
    private Object semaphore = new Object();

    /**
     * constructs a new CounterTest.
     * @param description String; the description for this counter
     */
    public XCounter(final String description)
    {
        this.description = description;
    }

    /**
     * Returns the current counter value.
     * @return long the counter value
     */
    public long getCount()
    {
        return this.count;
    }

    /**
     * Returns the current number of observations.
     * @return long the number of observations
     */
    public long getN()
    {
        return this.n;
    }

    /**
     * @param value long; the value
     */
    public void count(final long value)
    {
        synchronized (this.semaphore)
        {
            this.setCount(this.count + value);
            this.setN(this.n + 1);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.description;
    }

    /**
     * initializes the counter.
     */
    public void initialize()
    {
        synchronized (this.semaphore)
        {
            this.setN(0);
            this.setCount(0);
        }
    }

    /**
     * is the counter initialized?
     * @return returns whether the counter is initialized
     */
    public boolean isInitialized()
    {
        return this.n != Long.MIN_VALUE;
    }

    /**
     * sets the count.
     * @param count long; the value
     */
    private void setCount(final long count)
    {
        this.count = count;
    }

    /**
     * sets n.
     * @param n long; the number of measurements.
     */
    private void setN(final long n)
    {
        this.n = n;
    }

    /**
     * returns the description of the counter.
     * @return String the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Write statistics to an excel spreadsheet, starting on row "startRow".
     * @param sheet Sheet; the sheet
     * @param startRow int; the first row
     * @return first free row after writing
     */
    public int writeToExcel(final Sheet sheet, final int startRow)
    {
        int rownr = startRow;
        Row row = sheet.createRow(rownr);
        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue("count [n, telling]");
        row.createCell(3).setCellValue(getN());
        row.createCell(4).setCellValue(getCount());
        return rownr + 1;
    }
}
