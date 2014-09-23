package nl.tudelft.otsim.Simulators.MacroSimulator.TestCases;

import static org.junit.Assert.*;

import java.util.Arrays;

import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroCell;
import nl.tudelft.otsim.Simulators.MacroSimulator.Model;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeInteriorTampere;

import org.junit.Test;

public class TestNIT2 {

	@Test
	public void test() {
		Model model = new Model();
		NodeInteriorTampere node = new NodeInteriorTampere(new Vertex());
		MacroCell cellIn1a = new MacroCell(model);
		MacroCell cellIn2a = new MacroCell(model);
		MacroCell cellIn3a = new MacroCell(model);
		MacroCell cellIn4a = new MacroCell(model);
		MacroCell cellIn1b = new MacroCell(model);
		MacroCell cellIn2b = new MacroCell(model);
		MacroCell cellIn3b = new MacroCell(model);
		MacroCell cellIn4b = new MacroCell(model);
		
		MacroCell cellOut5a = new MacroCell(model);
		MacroCell cellOut6a = new MacroCell(model);
		MacroCell cellOut7a = new MacroCell(model);
		MacroCell cellOut8a = new MacroCell(model);
	/*	MacroCell cellOut5b = new MacroCell(model);
		MacroCell cellOut6b = new MacroCell(model);
		MacroCell cellOut7b = new MacroCell(model);
		MacroCell cellOut8b = new MacroCell(model);
		*/
	/*	double[] s1 = {0,50,150,300};
		double[] s2 = {100,0,300,1600};
		double[] s3 = {100,100,0,600};
		double[] s4 = {100,800,800,0};
		double[][] s = {s1,s2,s3,s4};*/
		
		/*double[] s1a = {0,0,25,0,75,0,150,0};
		double[] s1b = {0,0,0,25,0,75,0,150};
		double[] s2a = {50,0,0,0,150,0,800,0};
		double[] s2b = {0,50,0,0,0,150,0,800};
		double[] s3a = {50,0,50,0,0,0,300,0};
		double[] s3b = {0,50,0,50,0,0,0,300};
		double[] s4a = {0,50,0,400,0,400,0,0};
		double[] s4b = {50,0,400,0,400,0,0,0};
		double[][] s = {s1a,s1b,s2a,s2b,s3a,s3b,s4a,s4b};*/
		
		double[] s1a = {0,25,75,150};
		double[] s1b = {0,25,75,150};
		double[] s2a = {50,0,150,800};
		double[] s2b = {50,0,150,800};
		double[] s3a = {50,50,0,0,300};
		double[] s3b = {50,50,0,0,300};
		double[] s4a = {50,400,400,0};
		double[] s4b = {50,400,400,0};
		double[][] s = {s1a,s1b,s2a,s2b,s3a,s3b,s4a,s4b};
		
		//cellIn1.DemandTest = s1;
		//cellIn2.DemandTest = s2;
		//cellIn3.DemandTest = s3;
		//cellIn4.DemandTest = s4;
		cellIn1a.Demand = 250;
		cellIn2a.Demand = 1000;
		cellIn3a.Demand = 400;
		cellIn4a.Demand = 850;
		cellIn1b.Demand = 250;
		cellIn2b.Demand = 1000;
		cellIn3b.Demand = 400;
		cellIn4b.Demand = 850;
		
		cellIn1a.qCap = 500;
		cellIn2a.qCap = 1000;
		cellIn3a.qCap = 500;
		cellIn4a.qCap = 1000;
		cellIn1b.qCap = 500;
		cellIn2b.qCap = 1000;
		cellIn3b.qCap = 500;
		cellIn4b.qCap = 1000;
		
		cellOut5a.Supply = 1000;
		cellOut6a.Supply = 2000;
		cellOut7a.Supply = 1000;
		cellOut8a.Supply = 2000;
		
		
		
		node.cellsIn.add(cellIn1a);
		node.cellsIn.add(cellIn1b);
		node.cellsIn.add(cellIn2a);
		node.cellsIn.add(cellIn2b);
		node.cellsIn.add(cellIn3a);
		node.cellsIn.add(cellIn3b);
		node.cellsIn.add(cellIn4a);
		node.cellsIn.add(cellIn4b);
		
		node.cellsOut.add(cellOut5a);
		//node.cellsOut.add(cellOut5b);
		node.cellsOut.add(cellOut6a);
		//node.cellsOut.add(cellOut6b);
		node.cellsOut.add(cellOut7a);
		//node.cellsOut.add(cellOut7b);
		node.cellsOut.add(cellOut8a);
		//node.cellsOut.add(cellOut8b);
	
		
		
		
		
		
		
		
		
		
		
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
