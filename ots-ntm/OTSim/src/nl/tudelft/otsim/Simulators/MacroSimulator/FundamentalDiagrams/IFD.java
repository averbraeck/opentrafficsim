package nl.tudelft.otsim.Simulators.MacroSimulator.FundamentalDiagrams;

import nl.tudelft.otsim.Simulators.MacroSimulator.MacroCell;

public interface IFD {
	
	public double calcQ(MacroCell mc);
	public double calcQ(double[] parameters);
	public double calcQ(MacroCell mc, double[] addedParameters);
	
	public double calcV(MacroCell mc);
	public double calcV(double[] parameters);
	public double calcV(MacroCell mc, double[] addedParameters);
	//abstract public double calcQ(MacroCell mc, double k);


	public double calcQCap(MacroCell mc);
	public double calcQCap(double[] parameters);
	public double calcQCap(MacroCell mc, double[] addedParameters);
}
