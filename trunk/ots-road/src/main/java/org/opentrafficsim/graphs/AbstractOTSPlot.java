package org.opentrafficsim.graphs;

import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.event.EventListenerList;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.opentrafficsim.core.immutablecollections.Immutable;
import org.opentrafficsim.core.immutablecollections.ImmutableArrayList;
import org.opentrafficsim.core.immutablecollections.ImmutableList;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.event.EventType;

/**
 * Basics of all plots in the Open Traffic Simulator.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 16, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractOTSPlot extends JFrame
        implements Dataset, ActionListener, MultipleViewerChart, LaneBasedGTUSampler
{

    /** */
    private static final long serialVersionUID = 20160916L;

    /**
     * The (regular, not timed) event type for pub/sub indicating the addition of a graph. <br>
     * Payload: String graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_ADD_EVENT = new EventType("GRAPH.ADD");

    /**
     * The (regular, not timed) event type for pub/sub indicating the removal of a graph. <br>
     * Payload: String Graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_REMOVE_EVENT = new EventType("GRAPH.REMOVE");

    /** unique ID of the chart. */
    private final UUID uniqueId = UUID.randomUUID();

    /** Name of the chart. */
    private final String caption;

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** The graph. */
    private JFreeChart chart;
    
    /** The series of Lanes that provide the data for this TrajectoryPlot. */
    private final ImmutableList<Lane> path;

    /**
     * Construct a new AbstractOTSPlot.
     * @param caption String; the caption of the graph window
     * @param path List&lt;Lane&gt; the lanes for which the plot is made  
     */
    public AbstractOTSPlot(final String caption, final List<Lane> path)
    {
        this.caption = caption;
        this.path = new ImmutableArrayList<Lane>(path, Immutable.COPY);
    }

    /**
     * Save the chart.
     * @param chart JFreeChart; the chart
     */
    protected final void setChart(final JFreeChart chart)
    {
        this.chart = chart;
    }

    /**
     * Create the visualization.
     * @param container JFrame; the JFrame that will be filled with chart and the status label
     * @return JFreeChart; the visualization
     */
    protected abstract JFreeChart createChart(JFrame container);

    /**
     * Force redrawing of the graph.
     */
    public abstract void reGraph();

    /** {@inheritDoc} */
    @Override
    public final JFrame addViewer()
    {
        JFrame result = new JFrame(this.caption);
        result.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JFreeChart newChart = createChart(result);
        newChart.setTitle((String) null);
        addChangeListener(newChart.getPlot());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final void addChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.add(DatasetChangeListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.remove(DatasetChangeListener.class, listener);
    }

    /**
     * Notify interested parties of an event affecting this TrajectoryPlot.
     * @param event DatasetChangedEvent
     */
    protected final void notifyListeners(final DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
        {
            dcl.datasetChanged(event);
        }
    }

    /**
     * @return listenerList.
     */
    protected final EventListenerList getListenerList()
    {
        return this.listenerList;
    }

    /**
     * Return the caption of this graph.
     * @return String; the caption of this graph
     */
    public final String getCaption()
    {
        return this.caption;
    }
    
    /**
     * Provide a unique ID for this graph. In this case based on a generated UUID.
     * @return String; a unique ID. 
     */
    public final String getId()
    {
        return this.uniqueId.toString();
    }
    
    /**
     * @return path List<Lane> the path
     */
    public final ImmutableList<Lane> getPath()
    {
        return this.path;
    }

    /**
     * Return the graph type.
     * @return GraphType: the graph type.
     */
    public abstract GraphType getGraphType();

    /**
     * Make a snapshot of the graph and return it encoded as a PNG image.
     * @param width int; the width of the PNG in pixels
     * @param height int; the height of the PNG in pixels
     * @return byte[]; the PNG encoded graph
     * @throws IOException when there are IO
     */
    public final byte[] generatePNG(final int width, final int height) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ChartUtilities.writeChartAsPNG(result, this.chart, width, height);
        return result.toByteArray();
    }

}
