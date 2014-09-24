package nl.tudelft.otsim.Simulators.MacroSimulator.MultiClass;

import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.otsim.Simulators.MacroSimulator.Model;

public class TestMacroCellMultiClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		VehicleClass car = new VehicleClass(8,1,120/3.6);
		VehicleClass truck = new VehicleClass(18,1.5,90/3.6);
		Model model = new Model();
		model.dt=0.2;
		ArrayList<MacroCellMultiClass> cells = new ArrayList<MacroCellMultiClass>();
		int nrCells = 3;
		
		for (int n=0; n<nrCells; n++) {
		MacroCellMultiClass mc = new MacroCellMultiClass(model);
		
		mc.addVehicleClass(car);
		mc.addVehicleClass(truck);
		mc.setWidth(3.5);
		mc.l = 50;
		cells.add(mc);
		
		
		mc.init();
		}
		cells.get(0).addOut(cells.get(1));
		for (int n=1; n<nrCells-1; n++) {
			MacroCellMultiClass mc = cells.get(n);
			mc.addIn(cells.get(n-1));
			mc.addOut(cells.get(n+1));
		}
		cells.get(nrCells-1).addIn(cells.get(nrCells-2));
		
		//System.out.println(mc.getEffectiveDensity());
		
		//System.out.println(car.getAFreeFlow());
		//car.setVMax(130);
		//System.out.println(car.getAFreeFlow());
		//System.out.println(car.getBFreeFlow(85/3.6, 0.02));
		//System.out.println(car.getAFreeFlow());
		
		for (int i=0; i<200; i++) {
			for (MacroCellMultiClass mc: cells) {
		System.out.println(Arrays.toString(mc.KCell));
		mc.updateEffectiveDensity();
		System.out.println(mc.effDensity);
		mc.updateVelocity();
		System.out.println(Arrays.toString(mc.VCell));
		mc.updateVehicleShare();
		System.out.println(Arrays.toString(mc.vehicleShare));
		mc.updateFlow();
		System.out.println(Arrays.toString(mc.QCell));
		mc.updateEffectiveFlow();
		System.out.println(mc.effFlow);
			
		//System.out.println(mc.)
		mc.updateEffectiveSupply();
		System.out.println(mc.effSupply);
		mc.updateEffectiveDemand();
		System.out.println(mc.effDemand);
		mc.updateLabda();
		System.out.println(Arrays.toString(mc.labda));
		System.out.println("++++++++++++");
		}
			for (MacroCellMultiClass mc: cells) {
		
		mc.updateClassDemand();
		System.out.println(Arrays.toString(mc.classDemand));
		
		mc.updateClassSupply();
		System.out.println(Arrays.toString(mc.classSupply));
		System.out.println("++++++++++++");
			}
			for (MacroCellMultiClass mc: cells) {
		mc.updateClassFluxIn();
		System.out.println(Arrays.toString(mc.classFluxIn));
		mc.updateClassFluxOut();
		System.out.println(Arrays.toString(mc.classFluxOut));
		mc.updateDensity();
		System.out.println(Arrays.toString(mc.KCell));
		System.out.println("++++++++++++");
		
		}
			System.out.println("-------------------------------");
		}
		
	
	}

}
