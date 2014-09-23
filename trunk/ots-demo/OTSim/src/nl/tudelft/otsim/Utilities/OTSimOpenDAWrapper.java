package nl.tudelft.otsim.Utilities;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.GUI.FakeGraphicsPanel;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroSimulator;

public class OTSimOpenDAWrapper {
 // currently only for Macroscopic simulation
	String otsimConfiguration;
	Scheduler otsimScheduler;
	MacroSimulator otsimMacroSimulator;
	
	public OTSimOpenDAWrapper() {
		
	}
	
	public void initializeNewInstance(String[] arguments) {
		this.otsimConfiguration = arguments[0];
		this.otsimScheduler = new Scheduler(MacroSimulator.simulatorType, new FakeGraphicsPanel(), this.otsimConfiguration);
		this.otsimMacroSimulator = (MacroSimulator) otsimScheduler.getSimulator();	
	}
	public double getEndTime() {
		return otsimScheduler.getSimulator().getModel().getPeriod();
	}
	public double getCurrentTime() {
		return otsimScheduler.getSimulatedTime();
	}
	public void compute(double t) {
		otsimScheduler.stepUpTo(t);
	}
	public double[] returnStateContents(String parameter) {
		return otsimMacroSimulator.getModel().saveStateToArray(parameter);
			
	}
	public int returnStateLength(String parameter) {
		double[] tmp = returnStateContents(parameter);
		return tmp.length;
	}
	public void restoreStateContents(double[] values, String parameter) {
		otsimMacroSimulator.getModel().restoreState(values, parameter);
			
	}
}
