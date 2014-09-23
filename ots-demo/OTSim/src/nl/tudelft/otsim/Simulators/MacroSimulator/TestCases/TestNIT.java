package nl.tudelft.otsim.Simulators.MacroSimulator.TestCases;

import static org.junit.Assert.*;

import java.util.Arrays;

import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroCell;
import nl.tudelft.otsim.Simulators.MacroSimulator.Model;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeInteriorTampere;

import org.junit.Test;

public class TestNIT {

	@Test
	public void test() {
		Model model = new Model();
		NodeInteriorTampere node = new NodeInteriorTampere(new Vertex());
		MacroCell cellIn1 = new MacroCell(model);
		MacroCell cellIn2 = new MacroCell(model);
		MacroCell cellIn3 = new MacroCell(model);
		MacroCell cellIn4 = new MacroCell(model);
		
		MacroCell cellOut5 = new MacroCell(model);
		MacroCell cellOut6 = new MacroCell(model);
		MacroCell cellOut7 = new MacroCell(model);
		MacroCell cellOut8 = new MacroCell(model);
		
		double[] s1 = {0,50,150,300};
		double[] s2 = {100,0,300,1600};
		double[] s3 = {100,100,0,600};
		double[] s4 = {100,800,800,0};
		double[][] s = {s1,s2,s3,s4};
		
		//cellIn1.DemandTest = s1;
		//cellIn2.DemandTest = s2;
		//cellIn3.DemandTest = s3;
		//cellIn4.DemandTest = s4;
		cellIn1.Demand = 500;
		cellIn2.Demand = 2000;
		cellIn3.Demand = 800;
		cellIn4.Demand = 1700;
		
		cellIn1.qCap = 1000;
		cellIn2.qCap = 2000;
		cellIn3.qCap = 1000;
		cellIn4.qCap = 2000;
		
		cellOut5.Supply = 1000;
		cellOut6.Supply = 2000;
		cellOut7.Supply = 1000;
		cellOut8.Supply = 2000;
		
		
		node.cellsIn.add(cellIn1);
		node.cellsIn.add(cellIn2);
		node.cellsIn.add(cellIn3);
		node.cellsIn.add(cellIn4);
		
		node.cellsOut.add(cellOut5);
		node.cellsOut.add(cellOut6);
		node.cellsOut.add(cellOut7);
		node.cellsOut.add(cellOut8);
		
		
	/*	
		MacroCell cellIn1 = new MacroCell(model);
		
		
		MacroCell cellOut5 = new MacroCell(model);
		MacroCell cellOut6 = new MacroCell(model);
		
		double[] s1 = {1,1};
		
		
		double[][] s = {s1};
		
		cellIn1.DemandTest = s1;
		
		
		cellIn1.Demand = 1526.0010994903234;
		
		
		
		cellIn1.qCap = 1540.8;
		
		
		
		cellOut5.Supply = 761.4262577839546;
		cellOut6.Supply = 770.4;
		
		
		node.cellsIn.add(cellIn1);
		
		
		
		node.cellsOut.add(cellOut5);
		node.cellsOut.add(cellOut6);*/
		
		node.init();
		node.setTurningRatio(s);
		node.calcFlux();
		System.out.println(Arrays.toString(node.fluxesIn));
		System.out.println(Arrays.toString(node.fluxesOut));
		System.out.println("klaar");
		//fail("Not yet implemented");
	}

}
