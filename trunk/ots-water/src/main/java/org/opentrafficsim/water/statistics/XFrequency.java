/**
 * 
 */
package org.opentrafficsim.water.statistics;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

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
 * @version Mar 24, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 */
@XStreamAlias("frequency")
public class XFrequency implements Serializable
{
    /** */
    @XStreamOmitField
    private static final long serialVersionUID = 1L;

    /** count represents the values of the counters */
    protected SortedMap<String, Long> frequency = new TreeMap<String, Long>();

    /** n represents the number of measurements */
    protected long n = Long.MIN_VALUE;

    /** description refers to the title of this counter */
    protected String description;

    /** the semaphore */
    @XStreamOmitField
    private Object semaphore = new Object();

    /**
     * @param description String; the description of the statistic
     */
    public XFrequency(String description)
    {
        super();
        this.description = description;
    }

    /**
     * Returns the current counter value for a key
     * @param key String; the string key for the counter
     * @return long the counter value
     */
    public long getFrequency(final String key)
    {
        return this.frequency.get(key);
    }

    /**
     * Returns all counters
     * @return the counter values
     */
    public SortedMap<String, Long> getFrequencies()
    {
        return this.frequency;
    }

    /**
     * Returns the current number of observations
     * @return long the number of observations
     */
    public long getN()
    {
        return this.n;
    }

    /**
     * count frequency
     * @param key String; the key to count the value under
     * @param value long; the value
     */
    public void count(final String key, final long value)
    {
        synchronized (this.semaphore)
        {
            if (!this.frequency.containsKey(key))
                this.frequency.put(key, value);
            else
                this.frequency.put(key, this.frequency.get(key) + value);
            this.setN(this.n + 1);
        }
    }

    /**
     * count 1
     * @param key String; key
     */
    public void count(final String key)
    {
        this.count(key, 1L);
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
     * initializes the counter
     */
    public void initialize()
    {
        synchronized (this.semaphore)
        {
            this.setN(0);
            this.frequency.clear();
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
     * sets n
     * @param n long; the number of measurements
     */
    private void setN(final long n)
    {
        this.n = n;
    }

    /**
     * returns the description of the counter
     * @return String the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Write statistics to an excel spreadsheet, starting on row "startRow"
     * @param sheet Sheet; the excel sheet to write to
     * @param startRow int; the first row of writing
     * @return first free row after writing
     */
    public int writeToExcel(final Sheet sheet, final int startRow)
    {
        int rownr = startRow;
        Row row = sheet.createRow(rownr);
        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue("naam");
        row.createCell(3).setCellValue("frequentie");
        for (String key : frequency.keySet())
        {
            rownr++;
            row = sheet.createRow(rownr);
            row.createCell(2).setCellValue(key);
            row.createCell(3).setCellValue(frequency.get(key));
        }
        return rownr + 1;
    }

}
