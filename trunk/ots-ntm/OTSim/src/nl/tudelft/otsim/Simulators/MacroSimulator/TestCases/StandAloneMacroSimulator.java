package nl.tudelft.otsim.Simulators.MacroSimulator.TestCases;

import java.awt.BorderLayout;
import java.util.Arrays;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.GUI.FakeGraphicsPanel;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.StandAlone;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroSimulator;
import nl.tudelft.otsim.Simulators.MacroSimulator.Model;

/**
 * Run a MacroSimulator without GUI
 * <br />
 * Demonstration class only...
 * 
 * @author Peter Knoppers
 */
public class StandAloneMacroSimulator {

	/**
	 * @param args String[]; program arguments
	 */
	public static void main(String[] args) {
		StandAlone sa = new StandAlone();
		sa.main(new String[0]);
		//sa.add(Main.mainFrame = new Main(sa), BorderLayout.CENTER);
		//Main.initialized = true;
       // Main.mainFrame.setVisible(true);
        //Main.mainFrame.setActiveGraph();
        
       // GraphicsPanel gp = Main.mainFrame.graphicsPanel;
		
		double inflow = 2000;
		/*String otsimConfiguration = "EndTime:\t1800.00\nSeed:\t1\n"
				+ "Roadway:	0	from	1	to	2	speedlimit	120	lanes	2	vertices	(0.000,-0.250,0.000)	(3000.000,-0.250,0.000)	ins	outs	1\n"
				+ "Roadway:	1	from	2	to	3	speedlimit	120	lanes	1	vertices	(3000.000,-0.250,0.000)	(3500.000,-2.000,0.000)	ins	0	outs\n"
				+ "TrafficClass	passengerCar_act	4.000	140.000	-6.000	0.900000	600.000\nTripPattern	numberOfTrips:	[0.000/"+inflow+"][0.000/1.000000]	LocationPattern:	[z1, z2]	Fractions	passengerCar_act:1.000000\nTripPatternPath	numberOfTrips:	[0.000/"+inflow+"][0.000/1.000000]	NodePattern:	[origin ID=1 (0.00m, 0.00m, 0.00m), destination ID=2 (3500.00m, 0.00m, 0.00m)]\nPath:	1.00000	nodes:	1	2	3";
*///double inflowBoundary = (2000.0/3600.0);
		String otsimConfiguration = "EndTime:	3600.00\nSeed:	1\n"
				+ "Roadway:	0	from	10	to	8	speedlimit	130	lanes	3	vertices	(8032.358,-5.250,0.000)	(8035.027,-5.250,0.000)	(9985.293,-5.250,0.000)	(10000.000,-5.250,0.000)	ins	10	outs	8\n"
				+ "Roadway:	1	from	2	to	9	speedlimit	130	lanes	1	vertices	(9601.050,-301.400,0.000)	(9986.834,-12.062,0.000)	(10001.050,-1.400,0.000)	ins	outs	9\n"
				+ "Roadway:	2	from	1	speedlimit	130	lanes	3	vertices	(0.000,-5.250,0.000)	(7600.000,-5.250,0.000)	ins	outs	3\n"
				+ "Roadway:	3	to	11	speedlimit	130	lanes	4	vertices	(7600.000,-5.250,0.000)	(7994.873,-7.000,0.000)	(8000.000,-7.000,0.000)	ins	2	outs	10	11\n"
				+ "Roadway:	4	from	12	to	5	speedlimit	130	lanes	1	vertices	(8030.950,-25.400,0.000)	(8031.011,-25.446,0.000)	(8398.950,-301.400,0.000)	ins	11	outs\n"
				+ "Roadway:	5	from	7	speedlimit	130	lanes	4	vertices	(10000.000,-7.000,0.000)	(10003.332,-7.000,0.000)	(10400.000,-7.000,0.000)	ins	8	9	outs	6\n"
				+ "Roadway:	6	speedlimit	130	lanes	3	vertices	(10400.000,-7.000,0.000)	(13000.000,-5.250,0.000)	ins	5	outs	7\n"
				+ "Roadway:	7	to	6	speedlimit	130	lanes	2	vertices	(13000.000,-5.250,0.000)	(15000.000,-3.500,0.000)	ins	6	outs\n"
				+ "Roadway:	8	from	8	to	7	speedlimit	130	lanes	3	vertices	(9985.293,-5.350,0.000)	(9985.363,-5.350,0.000)	(9985.434,-5.350,0.000)	(9985.575,-5.350,0.000)	(9985.857,-5.350,0.000)	(9986.420,-5.350,0.000)	(9987.548,-5.350,0.000)	(9989.803,-5.350,0.000)	(9994.313,-5.350,0.000)	(9998.822,-5.350,0.000)	(10001.077,-5.350,0.000)	(10002.205,-5.350,0.000)	(10002.768,-5.350,0.000)	(10003.050,-5.350,0.000)	(10003.191,-5.350,0.000)	(10003.262,-5.350,0.000)	(10003.332,-5.350,0.000)	ins	0	outs	5\n"
				+ "Roadway:	9	from	9	to	7	speedlimit	130	lanes	1	vertices	(9986.351,-12.423,0.000)	(9989.448,-11.421,0.000)	(9992.589,-10.045,0.000)	(9995.598,-8.342,0.000)	(9996.277,-7.935,0.000)	(9997.031,-8.403,0.000)	(9999.202,-9.095,0.000)	(10000.884,-9.906,0.000)	(10003.020,-10.974,0.000)	ins	1	outs	5\n"
				+ "Roadway:	10	from	11	to	10	speedlimit	130	lanes	3	vertices	(7994.873,-5.350,0.000)	(7994.952,-5.350,0.000)	(7995.030,-5.350,0.000)	(7995.187,-5.350,0.000)	(7995.501,-5.350,0.000)	(7996.128,-5.350,0.000)	(7997.383,-5.350,0.000)	(7999.893,-5.350,0.000)	(8004.912,-5.350,0.000)	(8014.950,-5.350,0.000)	(8024.988,-5.350,0.000)	(8030.007,-5.350,0.000)	(8032.517,-5.350,0.000)	(8033.772,-5.350,0.000)	(8034.399,-5.350,0.000)	(8034.713,-5.350,0.000)	(8034.870,-5.350,0.000)	(8034.948,-5.350,0.000)	(8035.027,-5.350,0.000)	ins	3	outs	0\n"
				+ "Roadway:	11	from	11	to	12	speedlimit	130	lanes	1	vertices	(7997.186,-12.132,0.000)	(7999.595,-11.660,0.000)	(8001.638,-11.401,0.000)	(8003.688,-11.276,0.000)	(8005.755,-11.288,0.000)	(8007.847,-11.441,0.000)	(8009.972,-11.743,0.000)	(8011.315,-11.906,0.000)	(8011.974,-11.833,0.000)	(8013.252,-13.836,0.000)	(8015.773,-16.257,0.000)	(8018.451,-18.560,0.000)	(8021.216,-20.682,0.000)	(8024.059,-22.626,0.000)	(8026.973,-24.396,0.000)	(8029.859,-25.950,0.000)	(8032.725,-27.368,0.000)	ins	3	outs	4\n"
				+ "TrafficClass	PassengerCar	4.000	160.000	-6.000	0.000000	0.000\n"
				+ "TrafficClass	Truck	15.000	85.000	-6.000	0.000000	0.000\n"
				+ "TripPattern	numberOfTrips:	[0.000/4500.000000][0.000/1.000000]	LocationPattern:	[z1, z3]	Fractions	PassengerCar:0.900000	Truck:0.100000\n"
				+ "TripPatternPath	numberOfTrips:	[0.000/4500.000000][0.000/1.000000]	NodePattern:	[origin1 ID=1 (0.00m, 0.00m, 0.00m), destination1 ID=6 (15000.00m, 0.00m, 0.00m)]\n"
				+ "Path:	1.000000	nodes:	1	11a	10	8	7	6\n"
				+ "TripPattern	numberOfTrips:	[0.000/1750.000000][0.000/1.000000]	LocationPattern:	[z2, z3]	Fractions	PassengerCar:0.900000	Truck:0.100000\n"
				+ "TripPatternPath	numberOfTrips:	[0.000/1750.000000][0.000/1.000000]	NodePattern:	[origin2 ID=2 (9600.00m, -300.00m, 0.00m), destination1 ID=6 (15000.00m, 0.00m, 0.00m)]\n"
				+ "Path:	1.000000	nodes:	2	9	7	6\n"
				+ "TripPattern	numberOfTrips:	[0.000/1000.000000][0.000/1.000000]	LocationPattern:	[z1, z4]	Fractions	PassengerCar:0.900000	Truck:0.100000\n"
				+ "TripPatternPath	numberOfTrips:	[0.000/1000.000000][0.000/1.000000]	NodePattern:	[origin1 ID=1 (0.00m, 0.00m, 0.00m), destination2 ID=5 (8400.00m, -300.00m, 0.00m)]\n"
				+ "Path:	1.000000	nodes:	1	11a	12	5";
		//System.out.println(inflowBoundary);
		Scheduler scheduler = new Scheduler(MacroSimulator.simulatorType, Main.mainFrame.graphicsPanel, otsimConfiguration);
		// Do something with the scheduler
		Model macromodel = (Model) scheduler.getSimulator().getModel();
		macromodel.init();
		
		System.out.println("Time: "+scheduler.getSimulatedTime());
		System.out.println(Arrays.toString(macromodel.saveStateToArray("density")));
		scheduler.stepUpTo(10.0);
		System.out.println("Time: "+scheduler.getSimulatedTime());
		System.out.println(Arrays.toString(macromodel.saveStateToArray("density")));
		
		scheduler.stepUpTo(20.0);
		System.out.println("Time: "+scheduler.getSimulatedTime());
		double[] arr = macromodel.saveStateToArray("density");
		System.out.println(Arrays.toString(arr));
		double[] arr2 = new double[arr.length];
		Arrays.fill(arr2, 0.01);
		
		macromodel.restoreState(arr2, "density");
		System.out.println(Arrays.toString(macromodel.saveStateToArray("density")));
		scheduler.stepUpTo(30.0);
		System.out.println("Time: "+scheduler.getSimulatedTime());
		System.out.println(Arrays.toString(macromodel.saveStateToArray("density")));
		
	}

}
