package org.opentrafficsim.demo.ntm.animation;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 23 Mar 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
import java.awt.Font;
import java.io.File;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class TimeSeriesChart
{
    public static void TimeSeries(String fileName, TimeSeries series) throws Exception
    {
        final XYDataset dataset = (XYDataset) new TimeSeriesCollection(series);
        final DefaultCategoryDataset datasetCat = new DefaultCategoryDataset();
        JFreeChart timechart =
                ChartFactory.createTimeSeriesChart("Computing Test", "Seconds", "Value", dataset, false, false, false);

        JFreeChart chart = ChartFactory.createBarChart("Bar Chart Demo", // chart title
                "Category", // domain axis label
                "Value", // range axis label
                datasetCat, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
        );

        int width = 560; /* Width of the image */
        int height = 370; /* Height of the image */
        File fileTimeChart = new File(fileName);
        ChartUtils.saveChartAsJPEG(fileTimeChart, timechart, width, height);
    }

    public static void BarChart(String fileName, DefaultCategoryDataset datasetCategory) throws Exception
    {

        JFreeChart chart = ChartFactory.createBarChart("Bar Chart Demo", // chart title
                "Category", // domain axis label
                "Value", // range axis label
                datasetCategory, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
        );
        Font font = new Font("Dialog", Font.PLAIN, 20);

        chart.getCategoryPlot().getDomainAxis().setLabelFont(font);
        chart.getCategoryPlot().getRangeAxis().setLabelFont(font);
        font = new Font("Dialog", Font.PLAIN, 6);
        chart.getCategoryPlot().getDomainAxis().setTickLabelFont(font);
        int width = 960; /* Width of the image */
        int height = 670; /* Height of the image */
        File fileTimeChart = new File(fileName);
        ChartUtils.saveChartAsJPEG(fileTimeChart, chart, width, height);
    }

}
