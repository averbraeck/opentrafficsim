package nl.tudelft.otsim.Simulators.MacroSimulator.MultiClass;

public class VehicleClass {
	private double vMax;
	private double l;
	private double t;
	
		
	public VehicleClass(double length, double minimalHeadway, double vMax) {
		this.vMax = vMax;
		this.l = length;
		this.t = minimalHeadway;
		
		
	}
	public void setVMax(double v) {
		this.vMax = v;
	}
	public double getVMax() {
		return this.vMax;
	}
	public double getAFreeFlow() {
		return this.l+this.t*this.vMax;
	}
	public double getSpaceOccupancy(double v) {
		return l+t*v;
	}
	
	
	public double getBFreeFlow(double vCrit, double kCrit) {
		return -t*(vMax - vCrit)/kCrit;
	}
	public double getACongested(double vCrit, double kCrit, double kJam) {
		return t*kJam*(kCrit*vCrit)/(kJam - kCrit);
		
	}
	public double getBCongested(double vCrit, double kCrit, double kJam) {
		return l - t*(kCrit*vCrit)/(kJam - kCrit);
		
	}
}
