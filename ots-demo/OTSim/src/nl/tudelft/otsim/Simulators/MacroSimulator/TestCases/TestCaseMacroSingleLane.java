package nl.tudelft.otsim.Simulators.MacroSimulator.TestCases;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.GUI.FakeGraphicsPanel;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroCell;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroSimulator;
import nl.tudelft.otsim.Simulators.MacroSimulator.Model;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.Node;

public class TestCaseMacroSingleLane {

	public static void main(String[] args) {
		String networkConfiguration = "EndTime:	3600.00\nSeed:	1\nRoadway:	0	from	3	to	2	speedlimit	130	lanes	1	vertices	(3000.000,-0.250,0.000)	(3500.000,-2.000,0.000)	ins	2	outs\nRoadway:	1	from	1	speedlimit	130	lanes	2	vertices	(0.000,-0.250,0.000)	(2000.000,-0.250,0.000)	ins	outs	2\nRoadway:	2	to	3	speedlimit	130	lanes	2	vertices	(2000.000,-0.250,0.000)	(3000.000,-2.000,0.000)	ins	1	outs	0";
		double inflow = 2500.0;
		String demandConfiguration = "TrafficClass	passengerCar	4.000	130.000	-6.000	0.700000	NaN\nTrafficClass	truck	15.000	85.000	-6.000	1.000000	NaN\nTripPattern	numberOfTrips:	[0.000/"+inflow+"][0.000/1.000000]	LocationPattern:	[z1, z2]	Fractions	passengerCar:0.900000	truck:0.100000\nTripPatternPath	numberOfTrips:	[0.000/"+inflow+"][0.000/1.000000]	NodePattern:	[origin ID=1 (0.00m, 0.00m, 0.00m), destination ID=2 (1500.00m, 0.00m, 0.00m)]\nPath:	1.000000	nodes:	1	2";
		String configuration = networkConfiguration+"\n"+demandConfiguration;
		
		Scheduler scheduler = new Scheduler(MacroSimulator.simulatorType, new FakeGraphicsPanel(), configuration);
		Model macromodel = (Model) scheduler.getSimulator().getModel();
		macromodel.init();
		//macromodel.
		//System.out.println(Arrays.toString(macromodel.saveStateToArray()));
		String output = "";
		String output2;
		PrintWriter out;
		try {
			out = new PrintWriter("testSingleLane.m");
			out.println("clear;");
			double endTime= 3600;
			double dt = 5;
			out.println("tijd = 0:"+dt+":"+endTime+";");
		for (double i = 0; i<=endTime/dt; i++) {
			
			scheduler.stepUpTo(i*dt);
			output2 = "output("+(i+1)+",:)="+Arrays.toString(macromodel.saveStateToArray("density"))+";\n";
			
			output = output+output2;
			out.print(output2);
		}
		out.println("imagesc(output);");
		ArrayList<MacroCell> cells = macromodel.getCells();
		java.util.ArrayList<Node> nodes = macromodel.getNodes();
		MacroCell ref = null;
		for (Node n: nodes) {
			if (n.cellsIn.isEmpty()) {
				ref = n.cellsOut.get(0);
			}
		}
		String output3;
		
		for (int j=0; j<cells.size(); j++) {
			out.println("loc("+(j+1)+")="+cells.get(j).vertices.get(0).distance(ref.vertices.get(0))+";");
		}
		out.println("output2 =[output(:,17:end),output(:,1:16)];");
		out.println("h=pcolor(tijd,sort(loc),output2');");
		out.println("set(h, 'EdgeColor', 'none');");
		out.close();
		//System.out.println(output);
		System.out.println("klaar");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
