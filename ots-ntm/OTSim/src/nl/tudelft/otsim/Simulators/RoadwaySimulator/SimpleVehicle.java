package nl.tudelft.otsim.Simulators.RoadwaySimulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Step;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.Simulators.SimulatedDetector;
import nl.tudelft.otsim.Simulators.SimulatedObject;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * Simulate a vehicle in the {@link RoadwaySimulator}.
 * 
 * @author Peter Knoppers
 */
public class SimpleVehicle implements SimulatedObject, Step{
	private PositionAndRotation par = new PositionAndRotation(0d, 0d, 0d);
	private double currentCurvedness = 0f;				// /m (initial radius is infinity)
	private double speed = 10d;					// m/s
	final double width = 1.8d;					// m
	final double length = 4.8d;					// m
	private double nextStep = 0d;				// s (when is the next update due)
	/** Minimum curvature that this vehicle can steer */
	public final double maxCurve = 1 / 3d;		// /m (minimum radius = 3m)
	private double currentAcceleration = 0d;	// m/s/s
	/** Maximum deceleration that this vehicle can brake at */
	public final double maxDeceleration = 8d;	// m/s/s
	/** Maximum deceleration for a traffic light */
	public final double trafficLightDeceleration = maxDeceleration;
	/** Maximum acceleration that this vehicle can do */
	public final double maxAcceleration = 3d;	// m/s/s
	/** Maximum comfortable lateral acceleration */
	public final double highLateralAcceleration = 2d;	// m/s/s
	/** Lateral acceleration that "feels too low" */
	public final double lowLateralAcceleration = 0.2d;	// m/s/s
	/** Deceleration if no controls are touched */
	public final double passiveDeceleration = 1d;		// m/s/s
	/** Comfortable time to collision */
	public final double safeTTC = 1.5d;			// s
	private final double ttcMargin = 1d;		// m
	final double maxSpeed = 25d;				// m/s
	final double wheelHalfWidth = 0.2d;			// m
	final double wheelRadius = 0.4d;			// m
	final double breakLightWidth = 0.5d;		// m
	final double breakLightDepth = 0.5d;		// m
	final double wakeupDelay = 0.5d;			// m
	private int vehicleID;
	static private int lastVehicleID = 0;		// Number of vehicles created
	private Scheduler scheduler;
	final private RoadwaySimulator roadwaySimulator;
	private ScanTrail scanTrail = null;
	double scanTime = -1d;
	String state = "freshly created";
	// the "origin" of the vehicle is the point centered between the rear wheels.
	// (when turning, the line through the center of the circle goes through that point)
	
	/**
	 * Create a new SimpleVehicle.
	 * @param scheduler {@link Scheduler} that controls the simulation
	 * @param parent {@link RoadwaySimulator} that owns this new SimpleVehicle
	 * @param initialStep Double; simulation time when this vehicle comes into existence
	 * @param position Point2D.Double; location where this vehicle comes into existence 
	 * @param direction Double; angle that this vehicle points to when it comes into existence
	 */
	public SimpleVehicle(Scheduler scheduler, RoadwaySimulator parent, double initialStep, Point2D.Double position, double direction) {
		this.roadwaySimulator = parent;
		this.nextStep = initialStep;
		vehicleID = ++lastVehicleID;
		par = new PositionAndRotation(position, direction);
		this.scheduler = scheduler;
		scheduler.enqueueEvent(nextStep, this);
	}
	
	@Override
	public Scheduler.SchedulerState step(double time) {
		scanTrail = null;
		scanTime = time;
		if (step(time, roadwaySimulator.borders(), roadwaySimulator.nearbyVehicles(new Point2D.Double(0, 0), 999999d), roadwaySimulator.redOrYellowTrafficLights()))
			return null;
		return Scheduler.SchedulerState.SimulatorError;
		
	}

	private void updateState(double nextStepDue, double acceleration, double curvedness) {
		if ((nextStepDue - this.nextStep <= 0.1) && (speed < 1f)) {
			speed = 0f;
			acceleration = 0f;
			nextStepDue = nextStepDue + wakeupDelay;
		}
		this.currentAcceleration = acceleration;
		this.currentCurvedness = curvedness;
		par = positionAndRotation(nextStepDue, curvedness);
		speed = speed + (nextStepDue - this.nextStep) * acceleration;
		double prevStep = this.nextStep;
		this.nextStep = nextStepDue;
		Main.mainFrame.setStatus(-1, "%.2f: Vehicle %s -> %.2f %s", prevStep, vehicleID, nextStepDue, toString());
		scheduler.enqueueEvent(nextStepDue, this);
	}
	
	/**
	 * Compute and return the shape of this SimpleVehicle if its center is at the origin and direction is 0.
	 * @param enlargement Double; values bigger than 1d can be used to add a clearance
	 * @return Point2D.Double[]; array of points of the polygon that describes the outline of this SimpleVehicle 
	 */
	public Point2D.Double[] shape(double enlargement) {
		Point2D.Double[] result = new Point2D.Double[5];
		result[0] = result[4] = new Point2D.Double(enlargement * 3 * length / 4, enlargement * width / 2);
		result[1] = new Point2D.Double(- enlargement * length / 4, enlargement * width / 2);
		result[2] = new Point2D.Double(- enlargement * length / 4, - enlargement * width / 2);
		result[3] = new Point2D.Double(enlargement * 3 * length / 4, - enlargement * width / 2);
		return result;
	}

	/**
	 * Compute and return the shape of this SimpleVehicle at a time in the very near future.
	 * @param enlargement Double; enlargement to apply (to add some clearance)
	 * @param when Double; simulation time for which the shape is computed
	 * @return Point2D.Double[]; array of points of the polygon that describes the outline of this SimpleVehicle
	 */
	public Point2D.Double[] shape(double enlargement, double when) {
		return shape(enlargement, when, this.currentCurvedness);
	}

	/**
	 * Compute and return the shape of this SimpleVehicle at a time in the very 
	 * near future, with a proposed curvedness.
	 * @param enlargement Double; enlargement to apply (to add some clearance)
	 * @param when Double; simulation time for which the shape is computed
	 * @param curvedness Double; curvedness to apply
	 * @return Point2D.Double[]; array of points of the polygon that describes the outline of this SimpleVehicle
	 */
	public Point2D.Double[] shape(double enlargement, double when, double curvedness) {
		Point2D.Double[] result = shape(enlargement);
		PositionAndRotation parThen = positionAndRotation(when, curvedness);
		result = Planar.rotateTranslatePolyLine(result, parThen.direction, parThen.location.x, parThen.location.y);
		return result;
	}

	/**
	 * Draw this SimpleVehicle to a {@link GraphicsPanel}.
	 * @param time Double; simulation time for which this SimpleVehicle must be drawn
	 * @param gp {@link GraphicsPanel}; output device to draw on
	 * @param color Color to draw the vehicle in
	 */
	public void drawVehicle(double time, GraphicsPanel gp, Color color) {
		gp.setColor(color);
		PositionAndRotation parAtTime = positionAndRotation(time, currentCurvedness);
		// body of the vehicle
		gp.drawPolyLine(Planar.rotateTranslatePolyLine(shape(1f), parAtTime.direction, parAtTime.location.x, parAtTime.location.y), true);
		// left rear wheel
		gp.drawPolyLine(Planar.rotateTranslatePolyLine(wheelShape(0, width / 2 - wheelHalfWidth, 0d), parAtTime.direction, parAtTime.location.x, parAtTime.location.y), true);
		// right rear wheel
		gp.drawPolyLine(Planar.rotateTranslatePolyLine(wheelShape(0, - width / 2 + wheelHalfWidth, 0d), parAtTime.direction, parAtTime.location.x, parAtTime.location.y), true);
		// right front wheel
		double extraRotation = 0d;
		if (0d != currentCurvedness) {
			double radius = 1d / currentCurvedness;
			extraRotation = - Math.atan(length / 2 / (radius - width / 2 + wheelHalfWidth / 2));
		}
		gp.drawPolyLine(Planar.rotateTranslatePolyLine(wheelShape(length / 2, -width / 2 + wheelHalfWidth, extraRotation), parAtTime.direction, parAtTime.location.x, parAtTime.location.y), true);
		// left front wheel
		if (0d != currentCurvedness) {
			double radius = 1d / currentCurvedness;
			extraRotation = - Math.atan(length / 2 / (radius + width / 2 - wheelHalfWidth / 2));
		}
		gp.drawPolyLine(Planar.rotateTranslatePolyLine(wheelShape(length / 2, +width / 2 - wheelHalfWidth, extraRotation), parAtTime.direction, parAtTime.location.x, parAtTime.location.y), true);
		// brake lights
		if (currentAcceleration < -passiveDeceleration) {
			gp.setColor(Color.RED);
			gp.drawPolygon(Planar.rotateTranslatePolyLine(breakLightShape(-length / 4, width / 2 - wheelHalfWidth, 0d), parAtTime.direction, parAtTime.location.x, parAtTime.location.y));
			gp.drawPolygon(Planar.rotateTranslatePolyLine(breakLightShape(-length / 4, -width / 2 + wheelHalfWidth, 0d), parAtTime.direction, parAtTime.location.x, parAtTime.location.y));
		}
	}
	
	private Point2D.Double[] wheelShape(double offsetX, double offsetY, double rotation) {
		Point2D.Double[] polyLine = new Point2D.Double[5];
		polyLine[0] = polyLine[4] = new Point2D.Double(wheelRadius, -wheelHalfWidth);
		polyLine[1] = new Point2D.Double(wheelRadius, wheelHalfWidth);
		polyLine[2] = new Point2D.Double(-wheelRadius, wheelHalfWidth);
		polyLine[3] = new Point2D.Double(-wheelRadius, -wheelHalfWidth);
		polyLine = Planar.rotateTranslatePolyLine(polyLine, rotation, offsetX, offsetY);
		return polyLine;
	}
	
	private Point2D.Double[] breakLightShape(double offsetX, double offsetY, double rotation) {
		Point2D.Double[] polygon = new Point2D.Double[3];
		polygon[0] = new Point2D.Double(0, -breakLightWidth / 2);
		polygon[1] = new Point2D.Double(0, breakLightWidth / 2);
		polygon[2] = new Point2D.Double(breakLightDepth, 0);
		polygon = Planar.rotateTranslatePolyLine(polygon, rotation, offsetX, offsetY);
		return polygon;
	}
	
	private double deceleration(double proposedAcceleration, double ttc) {
		double availableDistance = speed * ttc + proposedAcceleration * ttc * ttc / 2 - ttcMargin;
		//double ttcDeceleration = -speed / ttc;
		double requiredDeceleration = -0.5d * speed * speed / availableDistance;
		if ((requiredDeceleration > - passiveDeceleration) && (availableDistance - ttcMargin > 3 * speed))
			return proposedAcceleration;
		return Math.min(requiredDeceleration, proposedAcceleration);
		
		/* WRONG
		double ttcDeceleration = -speed / ttc;
		if (-ttcDeceleration > maxDeceleration)
			return - maxDeceleration;
		double ttcDistance = - ttcDeceleration * ttc * ttc * 0.5f;
		double wantDistance = ttcDistance - ttcMargin;
		if (wantDistance <= 0d)
			return - maxDeceleration;
		double wantTTC = 2d * wantDistance / speed;
		double wantA = -speed / wantTTC;
		if (- wantA > maxDeceleration)
			return - maxDeceleration;
		return Math.min(wantA, proposedAcceleration);
		*/
	}
	
	/**
	 * Execute one simulation step for this SimpleVehicle.
	 * @param time Double; the current simulation time
	 * @param nearbyBorders ArrayList&lt;Point2D.Double[]&gt;; the list of borders that this vehicle must try not to drive into
	 * @param nearbyVehicles ArrayList&lt;{@link SimulatedObject}&gt;; the list of vehicles that this vehicle must try not to drive onto
	 * @param redLights ArrayList&lt;{@link SimulatedObject}&gt;; the list if red or yellow traffic lights near this SimpleVehicle
	 * @return Boolean; true on success; false if something went very wrong (and simulation should be stopped)
	 */
	public boolean step(double time, ArrayList<SimulatedObject> nearbyBorders, ArrayList<SimulatedObject> nearbyVehicles, ArrayList<SimulatedObject> redLights) {
		state = "driving freely";
		final float maxTime = 10;
		currentAcceleration = maxAcceleration;
		//if (speed < 0.5f)
		//	speed = 0.5f;
		float enlargement = 0.05f;
		OptimalCurveAndTTC ocattc = findOptimalCurveAndTTC(time, maxTime, 0f, nearbyBorders, Color.BLUE);
		OptimalCurveAndTTC ocattce = findOptimalCurveAndTTC(time, maxTime, enlargement, nearbyBorders, Color.CYAN);
		// System.out.println(String.format("Vehicle %d: ocattc: %s, ocattce: %s, enlargement=%f", vehicleID, ocattc.toString(), ocattce.toString(), enlargement));
		if (ocattce.ttc / (1 + enlargement) > ocattc.ttc )
			ocattc = new OptimalCurveAndTTC(ocattce.curvedness, ocattc.ttc);
		
		double newAcceleration = maxAcceleration;
		// Is there another vehicle in our path?
		double ttcLimit = 2 * ocattc.ttc;
		if (currentAcceleration < 0) {
			double timeToStop = - speed / currentAcceleration;
			if (ttcLimit > timeToStop)
				ttcLimit = timeToStop;
		}
		if (currentCurvedness != 0) {
			double radius = 1d / currentCurvedness;
			double arcLength = Math.abs(Math.PI * radius / 2);	// quarter circle
			// Solve the quadratic equation
			double discriminant = speed * speed + 2 * currentAcceleration * arcLength;
			if (discriminant >= 0) {
				double t1 = (-speed + Math.sqrt(discriminant)) / currentAcceleration;
				if ((t1 >= 0) && (t1 < ttcLimit))
					ttcLimit = (float) t1;
				double t2 = (-speed - Math.sqrt(discriminant)) / currentAcceleration;
				if ((t2 >= 0) && (t2 < ttcLimit))
					ttcLimit = (float) t2;
			}
			// else vehicle will come to a stop before arcLength
			// In that case ttcLimit has been set to the time it takes to stop
		}
		boolean slowingForTrafficLight = false;
		double timeToTL = timeToCollision(time, ocattc.curvedness, ttcLimit, 0d, redLights, true, Color.WHITE);
		if (timeToTL < ttcLimit) {
			System.out.println(String.format(Main.locale, "Vehicle %d: speed=%.3fm/s, timeToTL=%.3fs", vehicleID, speed, timeToTL));
			if (timeToTL >= speed / trafficLightDeceleration) {
				state = "slowing down or stopped for red or yellow light";
				System.out.println("Slowing down for red/yellow light");
				// slow down for traffic light
				ocattc = new OptimalCurveAndTTC(ocattc.curvedness, timeToTL);
				newAcceleration = deceleration(newAcceleration, timeToTL);
				slowingForTrafficLight = true;
			} else {
				state = "not slowing down for red or yellow light (not enough room to stop)";
				System.out.println("Not slowing down for red/yellow light");
			}
		}
		double thisTTC = timeToCollision(time, ocattc.curvedness, ttcLimit, 0d, nearbyVehicles, true, Color.GREEN);
		if (thisTTC < ttcLimit) {
			// The current position of another vehicle is in our path
			ocattc = new OptimalCurveAndTTC(ocattc.curvedness, thisTTC);
			newAcceleration = deceleration(newAcceleration, thisTTC);
			state = "following another vehicle";
		}
		// taking motion of other vehicle into account
		thisTTC = timeToCollision(time, ocattc.curvedness, ttcLimit, 0d, nearbyVehicles, false, Color.YELLOW);
		if (thisTTC < ttcLimit) {
			ocattc = new OptimalCurveAndTTC(ocattc.curvedness, thisTTC);
			newAcceleration = deceleration(newAcceleration, thisTTC);
			state = "following another vehicle";
		}
		
		double stepTime = ocattc.ttc / 3;
		if (stepTime > 1f)
			stepTime = 1f;
		if (slowingForTrafficLight && (stepTime > 0.3))
			stepTime = 0.3f;
		if (ocattc.ttc <= tryTimeStep)
			stepTime = 0;

		if (ocattc.ttc < safeTTC) {
			if (newAcceleration > -1f)
				newAcceleration = -1f;
		}
		float lateralAcceleration = Math.abs((float) (speed * speed * ocattc.curvedness));
		if (lateralAcceleration > highLateralAcceleration) {
			newAcceleration = - maxDeceleration;
			state = "slowing down due to uncomfortable sharp cornering";
			if (stepTime > 0.25f)
				stepTime = 0.25f;
		} else if ((lateralAcceleration < lowLateralAcceleration) && (ocattc.ttc > 1f))
			;
		else if (newAcceleration > 0f)
			newAcceleration = 0f;
		if (newAcceleration * stepTime + speed > maxSpeed)
			newAcceleration = (maxSpeed - speed) / stepTime;
		if (newAcceleration * stepTime + speed < 0)
			stepTime = - speed / newAcceleration / 2;
		boolean collided = (stepTime == 0) && (speed > 0); 
		updateState(time + stepTime, newAcceleration, ocattc.curvedness);
		//System.out.println(String.format("curve %.3f, ttc %.2fs, %s", bestCurvedness, ttc, toString()));
		// Figure out if we will hit a detector
		for (SimulatedObject detector : roadwaySimulator.detectors()) {
			double detectorTTC = timeToCollision(time, ocattc.curvedness, stepTime, 0d, detector, true, null);
			if (detectorTTC < stepTime) {
				//System.out.println("vehicle is about to be detected at " + (time + detectorTTC) + ", now is " + time + " stepTime is " + stepTime);
				activateDetector(time + detectorTTC, (Detector)detector);
				//System.out.println("Vehicle should then be at " + GeometryTools.pointsToString(outline(time + detectorTTC)));
			}
		}
		if (collided) {
			state = "collided with something";
			System.out.println(String.format("Vehicle %d has collided with something", vehicleID));
		}
		return !collided;
	}
	
	class DetectorActivator implements Step {
		SimpleVehicle sv;
		Detector detector;
		
		DetectorActivator(SimpleVehicle sv, Detector sd) {
			this.sv = sv;
			this.detector = sd;
			//System.out.println("Created DetectorActivator for vehicle " + sv.toString() + " to detector " + sd.toString());
		}

		@Override
		public Scheduler.SchedulerState step(double time) {
			System.out.println("DetectorActivator: time is " + scheduler.getSimulatedTime() + "; adding vehicle " + sv.toString() + " to detector " + detector.toString());
			detector.addVehicle(sv);
			return null;
		}

	}
	
	private void activateDetector(double when, Detector detector) {
		scheduler.enqueueEvent(when, new DetectorActivator(this, detector));
	}

	final double tryTimeStep = 0.01d;
	
	class OptimalCurveAndTTC {
		final double curvedness;
		final double ttc;
		
		OptimalCurveAndTTC (double curvedness, double ttc) {
			this.curvedness = curvedness;
			this.ttc = ttc;
		}
		
		@Override
		public String toString() {
			return String.format("curve=%f, ttc=%f", curvedness, ttc);
		}
	}
	
	double timeToCollision(double time, double curvedness, double maxTime, double additionalEnlargement, SimulatedObject nearByObject, boolean ignoreMotion, Color color) {
		ArrayList<SimulatedObject> objects = new ArrayList<SimulatedObject>();
		objects.add(nearByObject);
		return timeToCollision(time, curvedness, maxTime, additionalEnlargement, objects, ignoreMotion, color);
	}
	
	double timeToCollision(double time, double curvedness, double maxTime, double additionalEnlargement, ArrayList<SimulatedObject> nearByObjects, boolean ignoreMotion, Color color) {
		double timeStep = tryTimeStep;
		for (double deltaTime = tryTimeStep; deltaTime < maxTime; deltaTime += timeStep) {
			if (curvedness != 0d) {	// do not look further than a 90 degree turn
				double distanceCovered = speed * deltaTime + currentAcceleration * deltaTime * deltaTime / 2;
				double radius = 1d / curvedness;
				double rotationAngle = distanceCovered / radius;
				if (Math.abs(rotationAngle) > Math.PI / 2)
					return deltaTime;
			}
			double enlargement = 1d + additionalEnlargement;
			if (! ignoreMotion) {
				enlargement += deltaTime * speed / 10f;
				if (enlargement > 2d)
					enlargement = 2d;
			}
			Point2D.Double[] vehicleOutline = shape(enlargement, time + deltaTime, curvedness);
			scanTrail = new ScanTrail(color, vehicleOutline, false, scanTrail);
			for (Object object : nearByObjects) {
				Point2D.Double[] otherShape;
				if (object instanceof SimpleVehicle) {
					SimpleVehicle other = (SimpleVehicle) object;
					if (this == other)
						continue;
					// A driver can not observe the curvature of other vehicle's path very accurately.
					// This simulated driver assumes the path is straight
					otherShape = other.shape(enlargement, time + (ignoreMotion ? 0 : deltaTime), 0d);
				} else if (object instanceof SimulatedObject) {
					if (object instanceof BorderOutLine)
						otherShape = ((SimulatedObject) object).outline(time + deltaTime);
					else
						otherShape = Planar.closePolyline(((SimulatedObject) object).outline(time + deltaTime));
				} else
					throw new Error("Don't know how to interact with object " + object.toString());	
				if (Planar.polyLineIntersectsPolyLine(vehicleOutline, otherShape)) {
					if (object instanceof SimpleVehicle)
						System.out.println(String.format("Vehicle %d tails vehicle %d: ttc=%f", vehicleID, ((SimpleVehicle) object).vehicleID, deltaTime));
					else if (object instanceof SimulatedDetector)
						System.out.println(String.format("Vehicle %d enters detector area %s in %.3fs", vehicleID, Planar.pointsToString(otherShape), deltaTime));
					return deltaTime;
				}
			}
			// Increase the time step as we look further into the future
			if (deltaTime >= 0.045)
				timeStep = 0.05f;
			else if (deltaTime > 0.095)
				timeStep = 0.1f;
			else if (deltaTime > 0.45)
				timeStep = 0.5f;			
		}		
		return maxTime;
	}
	
	OptimalCurveAndTTC findOptimalCurveAndTTC(double time, double maxTime, double additionalEnlargement, ArrayList<SimulatedObject> nearbyBorders, Color color) {
		double baseCurve = 0;
		final int steps = 5;
		double ttc = timeToCollision(time, 0d, maxTime, additionalEnlargement, nearbyBorders, false, color); 
		for (double curveStep = maxCurve / steps; curveStep > Math.PI / 180 / 20; curveStep /= 2) {
			double bestCurvedness = baseCurve;
			for (int step = -steps; step <= steps; step++) {
				double curve = baseCurve + curveStep * step;
				if (Math.abs(curve) > maxCurve)
					continue;
				double thisTTC = timeToCollision(time, curve, maxTime, additionalEnlargement, nearbyBorders, false, color);
				if (thisTTC > ttc) {
					ttc = thisTTC;
					bestCurvedness = curve;
				}
			}
			baseCurve = bestCurvedness;
		}
		return new OptimalCurveAndTTC(baseCurve, ttc);
		/*
		final double curveStep = Math.PI / 180 / 2;	// 0.5 degree steps
		double ttc = timeToCollision(time, 0d, maxTime, additionalEnlargement, nearbyBorders, false, color); 
		double bestCurvedness = 0;
		for (double tryCurvedNess = curveStep; tryCurvedNess < maxCurve; tryCurvedNess += curveStep) {
			for (int sign = -1; sign <= 1; sign += 2) {
				double thisTTC = timeToCollision(time, sign * tryCurvedNess, maxTime, additionalEnlargement, nearbyBorders, false, Color.BLUE);
				if (thisTTC > ttc) {
					ttc = thisTTC;
					bestCurvedness = sign * tryCurvedNess;
				}
			}
		}
		return new OptimalCurveAndTTC(bestCurvedness, ttc);
		*/
		
	}
	
	private PositionAndRotation positionAndRotation(double when, double curvedness) {
		PositionAndRotation result = new PositionAndRotation();
		double deltaTime = when - nextStep;
		double distanceCovered = speed * deltaTime + currentAcceleration * deltaTime * deltaTime / 2;
		final double minimumCurvedness = 1e-8;
		if (Math.abs(curvedness) < minimumCurvedness) {		// driving in a straight line
			result.direction = par.direction;
			result.location.x = par.location.x + distanceCovered * Math.cos(par.direction);
			result.location.y = par.location.y + distanceCovered * Math.sin(par.direction);
		} else {					// driving in a curved line
			double radius = 1d / curvedness;
			double centerX = par.location.x + Math.sin(par.direction) * radius;
			double centerY = par.location.y - Math.cos(par.direction) * radius;
			double rotationAngle = distanceCovered / radius;
			result.direction = par.direction - rotationAngle;
			result.location.x = centerX - radius * Math.sin(result.direction);
			result.location.y = centerY + radius * Math.cos(result.direction);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return String.format("pos=%.2f,%.2f outline %s dir=%.1f, curve=%.2f/m v=%.2fm/s, a=%.2fm/s/s, t=%.2fs", par.location.x, par.location.y, Planar.pointsToString(outline(scheduler.getSimulatedTime())), par.direction * 180 / Math.PI % 360, currentCurvedness, speed, currentAcceleration, nextStep);
	}

	class PositionAndRotation {
		Point2D.Double location;
		double direction;
		
		PositionAndRotation() {
			location = new Point2D.Double();
		}
		
		PositionAndRotation(double x, double y, double direction) {
			location = new Point2D.Double(x, y);
			this.direction = direction;
		}
		
		PositionAndRotation(Point2D.Double location, double direction) {
			this.location = location;
			this.direction = direction;
		}
	}

	@Override
	public void paint(double when, GraphicsPanel graphicsPanel) {
		drawVehicle(when, graphicsPanel, Color.BLACK);
	}

	@Override
	public Point2D.Double[] outline(double when) {
		return shape(1d, when);
	}
	
	/**
	 * Draw the scanTrail of this SimpleVehicle.
	 * @param gp {@link GraphicsPanel}; output device to draw onto
	 */
	public void paintScanTrail(GraphicsPanel gp) {
		if (scheduler.getSimulatedTime() > scanTime)
			return;
		for (ScanTrail st = scanTrail; null != st; st = st.previous)
			st.paint(gp);
	}
	
	/**
	 * Create a String describing the current location of this SimpleVehicle.
	 * @return String; the location of this SimpleVehicle
	 */
	public String getPosition_r() {
		PositionAndRotation p = positionAndRotation(scheduler.getSimulatedTime(), currentCurvedness);
		return String.format(Main.locale, "(%.2f, %.2f)", p.location.x, p.location.y);
	}
	
	/**
	 * Create a String describing the current heading of this SimpleVehicle.
	 * @return String; the current heading of this SimpleVehicle
	 */
	public String getHeading_r() {
		PositionAndRotation p = positionAndRotation(scheduler.getSimulatedTime(), currentCurvedness);
		return String.format(Main.locale, "%.1f degrees", p.direction * 180 / Math.PI);
	}
	
	/**
	 * Retrieve the current speed of this SimpleVehiclein km/h.
	 * @return String; the current speed in km/h
	 */
	public String getSpeed_r() {
		return String.format(Main.locale, "%.1f km/h", getSpeed() * 3.6);
	}
	
	/**
	 * Return the speed of this SimpleVehicle at the current time.
	 * @return Double; the current speed in m/s
	 */
	public double getSpeed() {
		return speed - (nextStep - scheduler.getSimulatedTime()) * currentAcceleration;
	}
	
	/**
	 * Retrieve the current acceleration of this SimpleVehicle in m/s/s. 
	 * Negative values indicate that this SimpleVehicle is slowing down.
	 * @return String; the current acceleration of this SimpleVehicle in m/s/s
	 */
	public String getAcceleration_r() {
		return String.format(Main.locale, "%.2f m/s/s", currentAcceleration);
	}
	
	/**
	 * Retrieve the current turn radius of this SimpleVehicle in m, or 
	 * "infinity" if this SimpleVehicle is driving in a straight line.
	 * @return String; the current turn radius of this SimpleVehicle
	 */
	public String getTurnRadius_r() {
		if (Math.abs(currentCurvedness) < 1e-8)
			return "infinity";
		return String.format(Main.locale,  "%.1fm", 1 / currentCurvedness);
	}
	
	/**
	 * Retrieve the time that this SimpleVehicle wants to determine a new
	 * course of action (acceleration / breaking, steering). 
	 * @return String; the time that this SimpleVehicle wants to determine a
	 * new course of action
	 */
	public String getNextEvaluationTime_r() {
		return String.format(Main.locale, "%.2fs", nextStep);
	}
	
	/**
	 * Retrieve the ID of this SimpleVehicle.
	 * @return String; the ID of this SimpleVehicle
	 */
	public String getID_r() {
		return String.format("%d", vehicleID);
	}
	
	/**
	 * Retrieve the state of this SimpleVehicle.
	 * @return String; description of the state of this SimpleVehicle
	 */
	public String getState_r() {
		return state;
	}

	@Override
	public Double center(double when) {
		return (positionAndRotation(when, currentCurvedness).location);
	}

}