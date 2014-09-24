package nl.tudelft.otsim.Simulators;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Step;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.StatusBar;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * Runtime counterpart of a {@link nl.tudelft.otsim.Charts.MeasurementPlan}.
 * 
 * @author Peter Knoppers
 */
public class Measurement extends JFrame implements Step, SimulatedObject, XYDataset, ShutDownAble, MouseMotionListener, ActionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private final Point2D.Double[] area;
	private final Point2D.Double[] projectionPath;
	private final Simulator simulator;
	private final Scheduler scheduler;
	private HashMap<SimulatedObject, Integer> indices = new HashMap<SimulatedObject, Integer>();
	private ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();
	private transient EventListenerList listenerList = new EventListenerList();
	private DatasetGroup datasetGroup = null;
	private final javax.swing.JTabbedPane tabbedPaneGraphs;
	private double distanceGranularity = 10.0; 		// [m]
	private double timeGranularity = 2;				// [s]
	private double minimumTimeRange = 300.0;		// [s]
    private final double distanceRange;				// [m]
    private double timeRange = minimumTimeRange;	// [s]
    JFreeChart trajectoryChart;
    private ArrayList<XYPlot> contourPlots = new ArrayList<XYPlot>();
    private StatusBar statusBar;
    private JLabel statusLabel;
	private JLabel labelDistanceGranularity = new JLabel();
	private JLabel labelTimeGranularity = new JLabel();
	private final static String distanceFormat = "%.0f m";
	private final static String timeFormat = "%.0f s";
	private final static double[] distanceGranularities = { 10, 20, 50, 100 };
	private final static double[] timeGranularities = { 1, 2, 5, 10, 20, 30, 60, 120, 300, 600 };
	private JPopupMenu distancePopupMenu;
	private JPopupMenu timePopupMenu;
	
	private JPopupMenu buildPopupMenu(String format, String commandPrefix, double[] values) {
		JPopupMenu result = new JPopupMenu();
		for (double value : values) {
			JMenuItem item = new JMenuItem(String.format(Main.locale, format, value));
			item.setActionCommand(commandPrefix + String.format(Locale.US, " %f", value));
			item.addActionListener(this);
			result.add(item);
		}
		return result;
	}
	
	private void setGranularity(JLabel label, double value) {
		String format;
		if (labelDistanceGranularity == label)
			format = distanceFormat;
		else if (labelTimeGranularity == label)
			format = timeFormat;
		else
			throw new Error("bad label");
		label.setText(String.format(Main.locale, format, value));
	}
	
	private static void configureAxis(ValueAxis a, double upperBound) {
		a.setUpperBound(upperBound);
		a.setLowerMargin(0);
		a.setUpperMargin(0);
		a.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		a.setAutoRange(true);
		a.setAutoRangeMinimumSize(upperBound);
		a.centerRange(upperBound / 2);
	}

	/**
	 * Create a new Measurement.
	 * @param name String; name of the new Measurement
	 * @param area String; textual description of the outline of the measurement area
	 * @param projectionPath String; textual description of the center of the measurement area
	 * @param simulator {@link Simulator}; the Simulator that owns the moving {@link SimulatedObject SimulatedObjects}
	 * @param scheduler {@link Scheduler}; the Scheduler of the simulation
	 */
	public Measurement(String name, String area, String projectionPath, Simulator simulator, Scheduler scheduler) {
		setTitle(name);
		setMinimumSize(new Dimension(1000, 800));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.BorderLayout());
		tabbedPaneGraphs = new javax.swing.JTabbedPane();
		tabbedPaneGraphs.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		getContentPane().add(tabbedPaneGraphs, java.awt.BorderLayout.CENTER);
		getContentPane().add(statusBar = new StatusBar(), java.awt.BorderLayout.SOUTH);
		statusBar.addZone(StatusBar.DEFAULT_ZONE, statusLabel = new JLabel(), "*");
		statusBar.addZone("distanceGranularity", labelDistanceGranularity, "0%");
		statusBar.addZone("timeGranularity", labelTimeGranularity, "0%");
		statusBar.setGap(0);
		setGranularity(labelDistanceGranularity, distanceGranularity);
		setGranularity(labelTimeGranularity, timeGranularity);
		distancePopupMenu = buildPopupMenu(distanceFormat, "setDistanceGranularity", distanceGranularities);
		labelDistanceGranularity.addMouseListener(this);
		timePopupMenu = buildPopupMenu(timeFormat, "setTimeGranularity", timeGranularities);
		labelTimeGranularity.addMouseListener(this);

		this.area = Planar.coordinatesToPoints(area.replaceAll("[()m]", "").replaceAll("[,]", " ").split(" "));
		this.projectionPath = Planar.coordinatesToPoints(projectionPath.replaceAll("[()m]", "").replaceAll("[,]", " ").split(" "));
		this.simulator = simulator;
		this.scheduler = scheduler;
		scheduler.enqueueEvent(0, this);
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
		trajectoryChart = ChartFactory.createScatterPlot(name, "Time [s]", "Distance [m]", this, PlotOrientation.VERTICAL, false, false, false);
        configureAxis(trajectoryChart.getXYPlot().getDomainAxis(), minimumTimeRange);
		double length = 0;
		Point2D.Double prevPoint = null;
		for (Point2D.Double p : this.projectionPath) {
			if (null != prevPoint)
				length += prevPoint.distance(p);
			prevPoint = p;
		}
		distanceRange = length;
		configureAxis(trajectoryChart.getXYPlot().getRangeAxis(), distanceRange);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) trajectoryChart.getXYPlot().getRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(false);
		ChartPanel cp = new ChartPanel(trajectoryChart);
        cp.setFillZoomRectangle(true);
        cp.setMouseWheelEnabled(true);
        cp.addMouseMotionListener(this);
		tabbedPaneGraphs.addTab("Trajectories", cp);
		contourGraph(tabbedPaneGraphs, "Speed contour graph", new SpeedDataSet(), 0, 40, 150, Color.RED, Color.YELLOW, Color.GREEN, 20, "%.0f km/h");
		contourGraph(tabbedPaneGraphs, "Flow contour graph", new FlowDataSet(), 0, 1000, 3000, Color.RED, Color.YELLOW, Color.GREEN, 500, "%.0f veh/h");
		contourGraph(tabbedPaneGraphs, "Density contour graph", new DensityDataSet(), 0, 30, 100, Color.GREEN, Color.YELLOW, Color.RED, 10, "%.0f veh/km");
        tabbedPaneGraphs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				// hide the granularity fields if the time-distance diagram is shown
				// show the granularity fields if a contour-graph is shown
				int index = tabbedPaneGraphs.getSelectedIndex();
				if (index < 0)
					return;		// no tab is currently selected;
				String fieldWidth = "10%";
				if (0 == index) {
					fieldWidth = "0%";
					statusBar.setGap(0);
				} else
					statusBar.setGap(10);
				statusBar.updateConstraint("timeGranularity", fieldWidth);
				statusBar.updateConstraint("distanceGranularity", fieldWidth);
				statusBar.doLayout();
			}
        });
		setVisible(true);
	}
	
	private void contourGraph (javax.swing.JTabbedPane parent, String name, XYZDataset dataset, double lowValue, double niceValue, double highValue, Color lowColor, Color niceColor, Color highColor, double step, String format) {
        NumberAxis xAxis = new NumberAxis("Time [s]");
        configureAxis(xAxis, minimumTimeRange);
        NumberAxis yAxis = new NumberAxis("Distance [m]");
        configureAxis(yAxis, distanceRange);
        XYBlockRenderer blockRenderer = new XYBlockRenderer();
        blockRenderer.setBlockHeight(distanceGranularity);
        blockRenderer.setBlockWidth(timeGranularity);
        blockRenderer.setBlockAnchor(RectangleAnchor.CENTER);
        double[] boundaries = { lowValue, niceValue, highValue};
        Color[] boundaryColors = { lowColor, niceColor, highColor};
        PaintScale scale = new ColorPaintScale(format, boundaries, boundaryColors);
        blockRenderer.setPaintScale(scale);
        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, blockRenderer);
        contourPlots.add(xyPlot);
        xyPlot.setBackgroundPaint(Color.lightGray);
        xyPlot.setDomainGridlinesVisible(false);
        xyPlot.setRangeGridlinePaint(Color.white);
        LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0; ; i++) {
        	double value = lowValue + i * step;
        	if (value > highValue)
        		break;
        	legend.add(new LegendItem(String.format(Main.locale, format, value), scale.getPaint(value)));
        }
        xyPlot.setFixedLegendItems(legend);
        // Apply the settings that the ChartFactory applies to the scatterPlot 
        JFreeChart chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT, xyPlot, true);
        new StandardChartTheme("JFree").apply(chart);
        chart.setBackgroundPaint(Color.white);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.addMouseMotionListener(this);
        parent.addTab(name, chartPanel);
	}
	
	abstract class ContourDataSet implements XYZDataset {
		// Implements everything except getZValue and getSeriesKey.
        @Override
		public int getSeriesCount() {
            return 1;
        }
        
        private int distances() {
        	return (int) Math.ceil(distanceRange / distanceGranularity);
        }
        
        private int times() {
        	return (int) Math.ceil(timeRange / timeGranularity);
        }
        
        @Override
		public int getItemCount(int series) {
        	return distances() * times();
        }
        @Override
		public Number getX(int series, int item) {
            return new Double(getXValue(series, item));
        }
        
        @Override
		public double getXValue(int series, int item) {
        	return item / distances() * timeGranularity;
        }
        
        @Override
		public Number getY(int series, int item) {
            return new Double(getYValue(series, item));
        }
        
        @Override
		public double getYValue(int series, int item) {
            return item % distances() * distanceGranularity;
        }
        
        @Override
		public Number getZ(int series, int item) {
            return new Double(getZValue(series, item));
        }
        
        @Override
    	public void addChangeListener(DatasetChangeListener listener) {
    		listenerList.add(DatasetChangeListener.class, listener);
    	}

    	@Override
    	public void removeChangeListener(DatasetChangeListener listener) {
    		listenerList.remove(DatasetChangeListener.class, listener);
    	}
    	
        @Override
		public DatasetGroup getGroup() {
            return null;
        }
        
        @Override
		public void setGroup(DatasetGroup group) {
            // ignore
        }
        
        @SuppressWarnings("rawtypes")
		@Override
		public int indexOf(Comparable seriesKey) {
            return 0;
        }
        
        @Override
		public DomainOrder getDomainOrder() {
            return DomainOrder.ASCENDING;
        }        
		
	}
	
	class SpeedDataSet extends ContourDataSet {
        @Override
		public double getZValue(int series, int item) {
            double t = getXValue(series, item);
            double distance = getYValue(series, item);
            double sumDistance = 0;
            double sumTimeSpent = 0;
			try {
				for (Trajectory trajectory : trajectories) {
					java.awt.geom.Point2D.Double[] clippedTrajectory = trajectory.clipTrajectory(t, t + timeGranularity, distance, distance + distanceGranularity);
					if ((null == clippedTrajectory) || (0 == clippedTrajectory.length) || Double.isNaN(clippedTrajectory[0].y) || (Double.isNaN(clippedTrajectory[clippedTrajectory.length - 1].y)))
						continue;
					java.awt.geom.Point2D.Double prevPoint = null;
					for (java.awt.geom.Point2D.Double p : clippedTrajectory) {
						if (null != prevPoint) {
							sumTimeSpent += p.x - prevPoint.x;
							sumDistance += p.y - prevPoint.y;
						}
						prevPoint = p;
					}
				}
			} catch (java.util.ConcurrentModificationException e) {
				System.err.println("Caught ConcurrentModificationException");
				// This happens when another Trajectory gets added while we're iterating through the set
			}
			return 3600 / 1000 * sumDistance / sumTimeSpent;
			// returns NaN if sumTimeSpent == 0 and sumDistance == 0 which is good
        }

		@SuppressWarnings("rawtypes")
		@Override
		public Comparable getSeriesKey(int series) {
			return "speed";
		}
    			
	}
	
	class FlowDataSet extends ContourDataSet {
        @Override
		public double getZValue(int series, int item) {
            double t = getXValue(series, item);
            double distance = getYValue(series, item);
            double sumDistance = 0;
            boolean dataUsed = false;
			try {
				for (Trajectory trajectory : trajectories) {
					java.awt.geom.Point2D.Double[] clippedTrajectory = trajectory.clipTrajectory(t, t + timeGranularity, distance, distance + distanceGranularity);
					if ((null == clippedTrajectory) || (0 == clippedTrajectory.length) || Double.isNaN(clippedTrajectory[0].y) || (Double.isNaN(clippedTrajectory[clippedTrajectory.length - 1].y)))
						continue;
					java.awt.geom.Point2D.Double prevPoint = null;
					for (java.awt.geom.Point2D.Double p : clippedTrajectory) {
						if (null != prevPoint) {
							sumDistance += p.y - prevPoint.y;
							dataUsed = true;
						}
						prevPoint = p;
					}
				}
			} catch (java.util.ConcurrentModificationException e) {
				System.err.println("Caught ConcurrentModificationException");
				// This happens when another Trajectory gets added while we're iterating through the set
			}
			if (! dataUsed)
				return Double.NaN;
			return 3600 * sumDistance / timeGranularity / distanceGranularity;
        }

		@SuppressWarnings("rawtypes")
		@Override
		public Comparable getSeriesKey(int series) {
			return "flow";
		}

	}
	
	class DensityDataSet extends ContourDataSet {
        @Override
		public double getZValue(int series, int item) {
            double t = getXValue(series, item);
            double distance = getYValue(series, item);
            double sumTime = 0;
            boolean dataUsed = false;
			try {
				for (Trajectory trajectory : trajectories) {
					java.awt.geom.Point2D.Double[] clippedTrajectory = trajectory.clipTrajectory(t, t + timeGranularity, distance, distance + distanceGranularity);
					if ((null == clippedTrajectory) || (0 == clippedTrajectory.length) || Double.isNaN(clippedTrajectory[0].y) || (Double.isNaN(clippedTrajectory[clippedTrajectory.length - 1].y)))
						continue;
					java.awt.geom.Point2D.Double prevPoint = null;
					for (java.awt.geom.Point2D.Double p : clippedTrajectory) {
						if (null != prevPoint) {
							sumTime += p.x - prevPoint.x;
							dataUsed = true;
						}
						prevPoint = p;
					}
				}
			} catch (java.util.ConcurrentModificationException e) {
				System.err.println("Caught ConcurrentModificationException");
				// This happens when another Trajectory gets added while we're iterating through the set
			}
			if (! dataUsed)
				return Double.NaN;
			return 1000 * sumTime / timeGranularity / distanceGranularity;
        }

		@SuppressWarnings("rawtypes")
		@Override
		public Comparable getSeriesKey(int series) {
			return "density";
		}
		
	}

	@Override
	public Scheduler.SchedulerState step(double now) {
		if (now > timeRange)
			timeRange = now;
		for (SimulatedObject vehicle : simulator.SampleMovables()) {
			if (Planar.polygonContainsPoint(area, vehicle.center(now))) {
				Integer index = indices.get(vehicle);
				Trajectory trajectory;
				if (null == index) {
					trajectory = new Trajectory(now);
					trajectories.add(trajectory);
					indices.put(vehicle, trajectories.indexOf(trajectory));
				} else
					trajectory = trajectories.get(index);
				double longitudinal = 0;
				double bestLongitudinal = java.lang.Double.NaN;
				double bestLateral = java.lang.Double.NaN;
				Point2D.Double vehicleLocation = vehicle.center(now);
				Point2D.Double prevPoint = null;
				double bestDistance = java.lang.Double.MAX_VALUE;
				for (Point2D.Double p : projectionPath) {
					if (null != prevPoint) {
						Line2D.Double lineSegment = new Line2D.Double(prevPoint, p);
						double thisDistance = Planar.distanceLineSegmentToPoint(lineSegment, vehicleLocation);
						if (thisDistance < bestDistance) {
							Point2D.Double projection = Planar.nearestPointOnLine(lineSegment, vehicleLocation);
							bestLongitudinal = longitudinal + prevPoint.distance(projection);
							bestLateral = thisDistance;
							// figure out if the vehicle is to the left or to the right of the projectionPath
							double projectionPathDirection = Math.atan2(lineSegment.y2 - lineSegment.y1, lineSegment.x2 - lineSegment.x1);
							double vehicleDirection = Math.atan2(vehicleLocation.y - prevPoint.y, vehicleLocation.x - prevPoint.x);
							double difference = (vehicleDirection - projectionPathDirection + 2 * Math.PI) % (2 * Math.PI);
							if (difference < Math.PI)
								bestLateral = -bestLateral;
							bestDistance = thisDistance;
						}
						longitudinal += prevPoint.distance(p);
					}
					prevPoint = p;
				}
				trajectory.extend(now, new Point2D.Double(bestLongitudinal, bestLateral));
			}
		}
		if (now == minimumTimeRange) {
			((XYPlot) trajectoryChart.getPlot()).getDomainAxis().setAutoRange(true);
			for (XYPlot plot : contourPlots)
				plot.getDomainAxis().setAutoRange(true);
		}
		if (0 == now % timeGranularity)
			reGraph();
		scheduler.enqueueEvent(now + 1, this);
		return null;
	}
	
	private void reGraph() {
		notifyListeners(new DatasetChangeEvent(this, null));	// This guess work actually works!
		for (XYPlot plot : contourPlots) {
			plot.notifyListeners(new PlotChangeEvent(plot));
	        XYBlockRenderer blockRenderer = (XYBlockRenderer) plot.getRenderer();
	        blockRenderer.setBlockHeight(distanceGranularity);
	        blockRenderer.setBlockWidth(timeGranularity);
		}
	}

	@Override
	public void paint(double when, GraphicsPanel graphicsPanel) {
		graphicsPanel.setColor(Color.BLUE);
		graphicsPanel.setStroke(0);
		graphicsPanel.drawPolyLine(area, false);
	}

	@Override
	public Point2D.Double[] outline(double when) {
		return area;
	}

	private class Trajectory {
		final double startTime;
		final double timeStep = 1.0;
		private ArrayList<Point2D.Double> path = new ArrayList<Point2D.Double>();
		
		public Trajectory(double startTime) {
			this.startTime = startTime;
		}

		public void extend(double when, Point2D.Double where) {
			int index = (int) ((when - startTime) / timeStep);
			if (index < path.size())
				throw new Error("vehicle generated too many samples");
			while (index > path.size())
				path.add(null);
			path.add(new Point2D.Double(where.x, where.y));
		}

		public int size() {
			return path.size();
		}

		public double getTime(int item) {
			return startTime + item * timeStep;
		}

		public double getDistance(int item) {
			if ((item < 0) || (item >= path.size()))
				return Double.NaN;
			Point2D.Double p = path.get(item);
			if (null == p)
				return Double.NaN;
			return p.getX();
		}
		
		public double getEstimatedDistance(int item) {
			double result = getDistance(item);
			if (! Double.isNaN(result))
				return result;
			int prevItem = item - 1;
			double prevDistance = Double.NaN;
			while ((prevItem >= 0) && (Double.isNaN(prevDistance)))
				prevDistance = getDistance(--prevItem);
			double nextDistance = Double.NaN;
			int nextItem = item + 1;
			while ((nextItem < size()) && (Double.isNaN(nextDistance)))
				nextDistance = getDistance(++nextItem);
			if (Double.isNaN(prevDistance))
				return nextDistance;
			if (Double.isNaN(nextDistance))
				return prevDistance;
			return (prevDistance + (item - prevItem) * (nextDistance - prevDistance) / (nextItem - prevItem));
		}
		
		public java.awt.geom.Point2D.Double getEstimatedPosition(double time) {
			if (time < startTime)
				return null;
			if (time > startTime + path.size() * timeStep)
				return null;
			int baseStep = (int) Math.floor((time - startTime) / timeStep);
			if (baseStep < 0)
				baseStep = 0;
			int nextStep;
			for (nextStep = baseStep + 1; nextStep < path.size(); nextStep++)
				if (null != path.get(nextStep))
					break;
			if (nextStep >= path.size())
				return null;
			while (baseStep >= 0) {
				if (null != path.get(baseStep))
					break;
				baseStep--;
			}
			if (baseStep < 0)
				return null;
			double ratio = ((time - startTime) / timeStep - baseStep) / (nextStep - baseStep);
			Point2D.Double p0 = path.get(baseStep);
			Point2D.Double p1 = path.get(nextStep);
			return new Point2D.Double (p0.x + (p1.x - p0.x) * ratio, p0.y + (p1.y - p0.y)* ratio);
		}
		
		public java.awt.geom.Point2D.Double[] clipTrajectory(double minTime, double maxTime, double minDistance, double maxDistance) {
			if (startTime > maxTime)
				return null;
			if (startTime + timeStep * size() < minTime)
				return null;
			// Time-intersection is non-null
			int startSample = (int) Math.round((minTime - startTime) / timeStep);
			int endSample = (int) Math.round((maxTime - startTime) / timeStep);
			while (startSample <= endSample) {
				if (getEstimatedDistance(startSample + 1) > minDistance)
					break;
				startSample++;
			}
			while (endSample >= startSample) {
				if (getEstimatedDistance(endSample - 1) < maxDistance)
					break;
				endSample--;
			}
			int length = endSample - startSample + 1;
			if (length < 2) {
				//System.err.println("Start sample is " + getEstimatedDistance(startSample));
				return null;
				//throw new Error("oops; length is " + length);
			}
			java.awt.geom.Point2D.Double[] result = new java.awt.geom.Point2D.Double[length];
			for (int sample = 0; sample < result.length; sample++)
				result[sample] = new Point2D.Double(startTime + timeStep * (startSample + sample), getDistance(startSample + sample));
			if (result[0].y < minDistance) {
				double ratio = (minDistance - result[0].y) / (result[1].y - result[0].y);
				result[0].x += ratio * timeStep;
				result[0].y = minDistance;
			}
			if (result[result.length - 1].y > maxDistance) {
				double ratio = (maxDistance - result[length - 2].y) / (result[length - 1].y - result[length - 2].y);
				result[length - 1].x = result[length - 2].x + ratio * timeStep;
				result[length - 1].y = maxDistance;
			}
			return result;
		}

	}

	@Override
	public Point2D.Double center(double when) {
		return null;
	}

	@Override
	public int getSeriesCount() {
		return trajectories.size();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Comparable getSeriesKey(int series) {
		return series;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int indexOf(Comparable seriesKey) {
		if (seriesKey instanceof Integer)
			return (Integer) seriesKey;
		return -1;
	}

	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		listenerList.add(DatasetChangeListener.class, listener);
	}

	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		listenerList.remove(DatasetChangeListener.class, listener);
	}
	
	private void notifyListeners(DatasetChangeEvent event) {
		for (DatasetChangeListener dcl : listenerList.getListeners(DatasetChangeListener.class))
			dcl.datasetChanged(event);
	}

	@Override
	public DatasetGroup getGroup() {
		return datasetGroup;
	}

	@Override
	public void setGroup(DatasetGroup group) {
		datasetGroup = group;
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

	@Override
	public int getItemCount(int series) {
		if ((series < 0) || (series >= trajectories.size()))
			return 0;
		return trajectories.get(series).size();
	}

	@Override
	public Number getX(int series, int item) {
		if ((series < 0) || (series >= trajectories.size()))
			return null;
		return trajectories.get(series).getTime(item);
	}

	@Override
	public double getXValue(int series, int item) {
		if ((series < 0) || (series >= trajectories.size()))
			return Double.NaN;
		return trajectories.get(series).getTime(item);
	}

	@Override
	public Number getY(int series, int item) {
		if ((series < 0) || (series >= trajectories.size()))
			return null;
		return trajectories.get(series).getDistance(item);
	}

	@Override
	public double getYValue(int series, int item) {
		if ((series < 0) || (series >= trajectories.size()))
			return Double.NaN;
		return trajectories.get(series).getDistance(item);
	}

	@Override
	public void ShutDown() {
		dispose();
	}
	
	private class ColorPaintScale implements PaintScale {
		private double[] bounds;
		private Color[] boundColors;
		final String format;
		
		ColorPaintScale(String format, double bounds[], Color boundColors[]) {
			this.format = format;
			if (bounds.length < 2)
				throw new Error("bounds must have >= 2 entries");
			if (bounds.length != boundColors.length)
				throw new Error("bounds must have same length as boundColors");
			for (int i = 1; i < bounds.length; i++)
				if (bounds[i] - bounds[i - 1] <= 0)
					throw new Error("bounds values must be strictly ascending");
			this.bounds = bounds;
			this.boundColors = boundColors;
		}
		@Override
		public double getLowerBound() {
			return bounds[0];
		}

		private int mixComponent (double ratio, int low, int high) {
			double mix = low * (1 - ratio) + high * ratio;
			int result = (int) mix;
			if (result < 0)
				result = 0;
			if (result > 255)
				result = 255;
			return result;
		}
		
		@Override
		public Paint getPaint(double value) {
			int bucket;
			for (bucket = 0; bucket < bounds.length - 1; bucket++)
				if (value < bounds[bucket + 1])
					break;
			if (bucket >= bounds.length - 1)
				bucket = bounds.length - 2;
			double ratio = (value - bounds[bucket]) / (bounds[bucket + 1] - bounds[bucket]);
			
			Color mix = new Color (mixComponent(ratio, boundColors[bucket].getRed(), boundColors[bucket + 1].getRed()), 
					mixComponent(ratio, boundColors[bucket].getGreen(), boundColors[bucket + 1].getGreen()), 
					mixComponent(ratio, boundColors[bucket].getBlue(), boundColors[bucket + 1].getBlue()));
			return mix;
		}

		@Override
		public double getUpperBound() {
			return bounds[bounds.length - 1];
		}

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		ChartPanel cp = (ChartPanel) mouseEvent.getSource();
		XYPlot plot = (XYPlot) cp.getChart().getPlot();
		boolean showCrossHair = cp.getScreenDataArea().contains(mouseEvent.getPoint());
		if (cp.getHorizontalAxisTrace() != showCrossHair) {
			cp.setHorizontalAxisTrace(showCrossHair);
			cp.setVerticalAxisTrace(showCrossHair);
			plot.notifyListeners(new PlotChangeEvent(plot));
		}
		if (showCrossHair) {
			Point2D p = cp.translateScreenToJava2D(mouseEvent.getPoint());
			PlotRenderingInfo pi = cp.getChartRenderingInfo().getPlotInfo();
			double t = plot.getDomainAxis().java2DToValue(p.getX(), pi.getDataArea(), plot.getDomainAxisEdge());
			double distance = plot.getRangeAxis().java2DToValue(p.getY(), pi.getDataArea(), plot.getRangeAxisEdge()); 
			XYDataset dataset = plot.getDataset();
			String value = "";
			if (dataset instanceof XYZDataset) {
				for (int item = dataset.getItemCount(0); --item >= 0; ) {
					double x = dataset.getXValue(0,  item);
					if ((x + timeGranularity < t) || (x >= t))
						continue;
					double y = dataset.getYValue(0, item);
					if ((y + distanceGranularity < distance) || (y >= distance))
						continue;
					double valueUnderMouse = ((XYZDataset) dataset).getZValue(0, item);
					if (Double.isNaN(valueUnderMouse))
						continue;
					String format = ((ColorPaintScale) (((XYBlockRenderer) (plot.getRenderer(0))).getPaintScale())).format;
					value = String.format(Main.locale, ": " + format, valueUnderMouse);
				}
			} else {	// must be on the trajectory graph
				double bestDistance = Double.MAX_VALUE;
				Trajectory bestTrajectory = null;
				final int mousePrecision = 5;
				java.awt.geom.Point2D.Double mousePoint = new java.awt.geom.Point2D.Double(t, distance);
				double lowTime = plot.getDomainAxis().java2DToValue(p.getX() - mousePrecision, pi.getDataArea(), plot.getDomainAxisEdge()) - 1;
				double highTime = plot.getDomainAxis().java2DToValue(p.getX() + mousePrecision, pi.getDataArea(), plot.getDomainAxisEdge()) + 1;
				double lowDistance = plot.getRangeAxis().java2DToValue(p.getY() + mousePrecision, pi.getDataArea(), plot.getRangeAxisEdge()) - 20;
				double highDistance = plot.getRangeAxis().java2DToValue(p.getY() - mousePrecision, pi.getDataArea(), plot.getRangeAxisEdge()) + 20;
				//System.out.println(String.format("Searching area t[%.1f-%.1f], x[%.1f,%.1f]", lowTime, highTime, lowDistance, highDistance));
				for (Trajectory trajectory : trajectories) {
					java.awt.geom.Point2D.Double[] clippedTrajectory = trajectory.clipTrajectory(lowTime, highTime, lowDistance, highDistance);
					if (null == clippedTrajectory)
						continue;
					java.awt.geom.Point2D.Double prevPoint = null;
					for (java.awt.geom.Point2D.Double trajectoryPoint : clippedTrajectory) {
						if (null != prevPoint) {
							double thisDistance = Planar.distancePolygonToPoint(clippedTrajectory, mousePoint);
							if (thisDistance < bestDistance) {
								bestDistance = thisDistance;
								bestTrajectory = trajectory;
							}
						}
						prevPoint = trajectoryPoint;
					}
				}
				if (null != bestTrajectory) {
					for (SimulatedObject so : indices.keySet())
						if (trajectories.get(indices.get(so)) == bestTrajectory) {
							Point2D.Double bestPosition = bestTrajectory.getEstimatedPosition(t);
							if (null == bestPosition)
								continue;
							value = String.format(Main.locale, ": vehicle %s; location on measurement path at t=%.1fs: longitudinal %.1fm, lateral %.1fm", so.toString(), t, bestPosition.x, bestPosition.y);
						}
				} else
					value = "";
			}
			statusLabel.setText(String.format(Main.locale, "t=%.0fs, distance=%.0fm%s", t, distance, value));
		} else
			statusLabel.setText(" ");		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String command = arg0.getActionCommand();
		System.out.println("command is \"" + command + "\"");
		String[] fields = command.split("[ ]");
		if (fields.length == 2) {
			NumberFormat nf = NumberFormat.getInstance(Locale.US);
			double value;
			try {
				value = nf.parse(fields[1]).doubleValue();
			} catch (ParseException e) {
				e.printStackTrace();
				return;
			}
			if (fields[0].equalsIgnoreCase("setDistanceGranularity")) {
				distanceGranularity = value;
				setGranularity (labelDistanceGranularity, value);
			} else if (fields[0].equalsIgnoreCase("setTimeGranularity")) {
				timeGranularity = value;
				setGranularity (labelTimeGranularity, value);
			}
			reGraph();
		}
		else
			throw new Error("Unknown ActionEvent");
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// Ignored		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// Ignored		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// Ignored		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		maybeShowPopup(arg0);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		maybeShowPopup(arg0);		
	}
	
	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			Component c = e.getComponent();
			if (c == labelDistanceGranularity)
				distancePopupMenu.show(c, e.getX(), e.getY());
			else if (c == labelTimeGranularity)
				timePopupMenu.show(c, e.getX(), e.getY());
		}
	}

}

