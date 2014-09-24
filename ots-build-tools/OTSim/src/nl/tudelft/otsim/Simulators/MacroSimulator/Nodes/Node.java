package nl.tudelft.otsim.Simulators.MacroSimulator.Nodes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroCell;

//import nl.tudelft.otsim.GUI.GraphicsPanel;

abstract public class Node {
	public ArrayList<MacroCell> cellsIn = new ArrayList<MacroCell>();
	public ArrayList<MacroCell> cellsOut = new ArrayList<MacroCell>();
	public Vertex location = new Vertex();
	protected int nrIn;
	protected int nrOut;
	public double[] fluxesIn;
	public double[] fluxesOut;
	public double[][] turningRatio;
	private int id;
	
	
	public Node(Vertex loc) {
		this.location = loc;
		
	}
	public void init() {
		nrIn = cellsIn.size();
		nrOut = cellsOut.size();
		
		if (nrIn>0)
			fluxesIn = new double[nrIn];
		else
			fluxesIn = new double[1];
		
		if (nrOut>0)
			fluxesOut = new double[nrOut];
		else
			fluxesOut = new double[1];
		
		turningRatio = new double[nrIn][nrOut];
		setDefaultTurningRatio();
		
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	public void draw(GraphicsPanel graphicsPanel) {
		
    	location.paint(graphicsPanel);
   	
	}
	public void setDefaultTurningRatio() {
		
		double[][] arr1 = new double[nrIn][nrOut];
		for (int i =0; i<nrIn; i++) {
			for (int j =0; j<nrOut; j++) {
				arr1[i][j]=1;
			}
		}
		setTurningRatio(arr1);
	}
	public void setTurningRatio(double[][] array) {
		double[] totali = new double[nrIn];
		for (int i = 0; i <nrIn; i++) {
			totali[i] = 0;
			for (int j = 0; j<nrOut; j++) {
				totali[i] += array[i][j];
			}
			//totali[i]
		}
		//System.out.println(totali[0]);
		for (int i = 0; i <nrIn; i++) {
			
			for (int j = 0; j<nrOut; j++) {
				turningRatio[i][j] = array[i][j]/totali[i];
			}
			//totali[i]
		}
	}
	abstract public void calcFlux();
	
	
	
}
