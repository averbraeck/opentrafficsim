package nl.tudelft.otsim.Simulators.MacroSimulator.Nodes;

import java.util.ArrayList;

import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroCell;

/*
 *  This class implements the generic node model as described by
 *  Chris M.J. Tampere, Ruben Corthout, Dirk Cattrysse, Lambertus H. Immers
 *  A generic class of first order node models for dynamic macroscopic simulation of traffic flows
 *  In Transportation Research Part B 45 (2011) pp 289–309
*/
public class NodeInteriorTampere extends NodeInterior {

	public NodeInteriorTampere(Vertex loc) {
		super(loc);
		// TODO Auto-generated constructor stub
	}
	
	public void calcFlux() {
		double[] supply = new double[nrOut];
		double[] demand = new double[nrIn];
		double[] qCapIn = new double[nrIn];
		for(int j=0; j<nrOut ;j++) {
			supply[j] = cellsOut.get(j).Supply;
		}
		for(int i=0;i<nrIn;i++) {
			demand[i] = cellsIn.get(i).Demand;
			qCapIn[i] = cellsIn.get(i).qCap;
		}
		calcFlux(supply, demand, qCapIn);
		
	}
	public void calcFlux(double[] supply, double[] demand, double[] qCapIn) {
		ArrayList<Double[]> result = calcFluxValues(supply, demand, qCapIn);
		
		for (int i = 0; i< result.get(0).length; i++) {
			fluxesIn[i] = (double) result.get(0)[i];
		}
		for (int j = 0; j< result.get(1).length; j++) {
			fluxesOut[j] = (double) result.get(1)[j];
		}
		
	}
/*	public double calcFluxValue(MacroCell cell, double addedK) {
		double[] supply = new double[nrOut];
		double[] demand = new double[nrIn];
		double[] qCapIn = new double[nrIn];
		
		for(int j=0; j<nrOut ;j++) {
			supply[j] = cellsOut.get(j).Supply;
		}
		for(int i=0;i<nrIn;i++) {
			demand[i] = cellsIn.get(i).Demand;
			qCapIn[i] = cellsIn.get(i).qCap;
		}
		int indexCellsOut = cellsOut.indexOf(cell);
		int indexCellsIn = cellsIn.indexOf(cell);
		if (indexCellsOut!=-1) {
			supply[indexCellsOut] = cellsOut.get(indexCellsOut).calcSupplyValue(cell.KCell+addedK);
		}
		if (indexCellsIn!=-1) {
			demand[indexCellsIn] = cellsIn.get(indexCellsIn).calcDemandValue(cell.KCell+addedK);
				
		}
		ArrayList<Double[]> result = calcFluxValues(supply, demand, qCapIn);
		double res = -1;
		if (indexCellsOut!=-1) {
			res = (double) result.get(1)[indexCellsOut];
		}
		if (indexCellsIn!=-1) {
			res = (double) result.get(0)[indexCellsIn];	
		}
		return res;
	}*/
	public double calcFluxValue(MacroCell cell, double[] addedParam) {
		double[] supply = new double[nrOut];
		double[] demand = new double[nrIn];
		double[] qCapIn = new double[nrIn];
		
		for(int j=0; j<nrOut ;j++) {
			supply[j] = cellsOut.get(j).Supply;
		}
		for(int i=0;i<nrIn;i++) {
			demand[i] = cellsIn.get(i).Demand;
			qCapIn[i] = cellsIn.get(i).qCap;
		}
		int indexCellsOut = cellsOut.indexOf(cell);
		int indexCellsIn = cellsIn.indexOf(cell);
		if (indexCellsOut!=-1) {
			supply[indexCellsOut] = cellsOut.get(indexCellsOut).calcSupplyValue(new double[]{cell.KCell+addedParam[0], cell.vLim + addedParam[1], cell.kCri + addedParam[2], cell.kJam+addedParam[3]});
		}
		if (indexCellsIn!=-1) {
			demand[indexCellsIn] = cellsIn.get(indexCellsIn).calcDemandValue(new double[]{cell.KCell+addedParam[0],cell.vLim + addedParam[1], cell.kCri + addedParam[2], cell.kJam+addedParam[3]});
			qCapIn[indexCellsIn] = cellsIn.get(indexCellsIn).fd.calcQCap(new double[]{cell.KCell+addedParam[0],cell.vLim + addedParam[1], cell.kCri + addedParam[2], cell.kJam+addedParam[3]});	
		}
		ArrayList<Double[]> result = calcFluxValues(supply, demand, qCapIn);
		double res = -1;
		if (indexCellsOut!=-1) {
			res = (double) result.get(1)[indexCellsOut];
		}
		if (indexCellsIn!=-1) {
			res = (double) result.get(0)[indexCellsIn];	
		}
		return res;
	}
	public ArrayList<Double[]>  calcFluxValues(double[] supply, double[] demand, double[] qCapIn) {
		//step 1:
		int k = 0;
		int maxK = nrIn+2;
		//ArrayList<ArrayList<ArrayList<Integer>>> U = new ArrayList<ArrayList<ArrayList<Integer>>>();
		//ArrayList<ArrayList<Integer>> J = new ArrayList<ArrayList<Integer>>();
		//System.out.println("In: "+ nrIn);
		//System.out.println("Out: "+ nrOut);
		boolean[][][] Ut = new boolean[maxK][nrOut][nrIn];
		boolean[][] Jt = new boolean[maxK][nrOut];
		double[][] Rt = new double[maxK][nrOut];
		double[][] q = new double[nrIn][nrOut];
		
		Double[] calcfluxesIn = new Double[nrIn];
		Double[] calcfluxesOut = new Double[nrOut];
		//ArrayList<double[]> R = new ArrayList<double[]>();
		//double[] R0 = new double[nrOut];
		
		/*for(int j=0; j<nrOut ;j++) {
			R0[j] = cellsOut.get(j).Supply;
		}
		R.add(0,R0);
			*/
		
		/*for(int j=0; j<nrOut ;j++) {
			Rt[0][j] = cellsOut.get(j).Supply;
		}*/
		Rt[0] = supply;
		
		double[] S1 = demand;
		double[] C1 = qCapIn;
		/*for(int i=0;i<nrIn;i++) {
			S1[i] = cellsIn.get(i).Demand;
			C1[i] = cellsIn.get(i).qCap;
		}*/
		
		
		double[][] S2 = new double[nrIn][nrOut];
		for(int i=0;i<nrIn;i++) {
			for(int j=0;j<nrOut;j++) {
				S2[i][j] = turningRatio[i][j]*S1[i];
			}
		}
		/*double[][] S2 = new double[nrIn][nrOut];
		for(int i=0;i<nrIn;i++) {
			S2[i] = cellsIn.get(i).DemandTest;
		}*/
		/*for(int j=0;j<nrOut;j++) {
			double temp = 0;
			for(int i=0;i<nrIn;i++) {
				if (S2[i][j] > 0) {
					U.get(0).get(j).add(i);
					temp += S2[i][j];
				}
			}
			if (temp > 0)
				J.get(0).add(j);
		}*/
		for(int j=0;j<nrOut;j++) {
			double temp = 0;
			for(int i=0;i<nrIn;i++) {
				if (S2[i][j] > 0) {
					Ut[0][j][i] = true;
					temp += S2[i][j];
				} else
					Ut[0][j][i] = false;
			}
			if (temp > 0) {
				Jt[0][j] = true;
			} else {
				Jt[0][j] = false;
			}
		}
		
		
		
		//step 2: 
		double[][] C2 = new double[nrIn][nrOut];
		for (int i=0; i<nrIn; i++) {
			if (S1[i] >0) {
				
				for(int j=0;j<nrOut;j++) {
					C2[i][j] = (S2[i][j]/S1[i]) * C1[i];
					
					
				}
			}
		}
		
		
		//step 3:
		k = 0;
		
		
		double[][] a = new double[maxK][nrOut];
		
		/*for (Integer j: J.get(k)) {
			double tmpc = 0;
			for (Integer i: U.get(k).get(j)) {
				tmpc += C2[i][j];  
			
			}
			a.get(k).set(j, (R.get(k)[j])/tmpc);
		}*/
		double[] ajhat = new double[maxK];
		
		int[] jhat = new int[maxK];
		
		
		boolean stop = false;
		
		while (!stop) {
			ajhat[k] = 9999999;
			jhat[k] = 0;
			Jt[k+1] = Jt[k].clone();
			Rt[k+1] = Rt[k].clone();
			for (int j=0; j<nrOut; j++) {
				Ut[k+1][j] = Ut[k][j].clone();
			}
			
		
		
		for (int j = 0; j<nrOut; j++) {
			if (Jt[k][j] == true) {
				
				double tmpc = 0;
				for (int i = 0; i<nrIn; i++) {
					if (Ut[k][j][i] == true) {
						tmpc += C2[i][j];
					}
								  
				
				}
				a[k][j] = Rt[k][j]/tmpc;
				if (a[k][j] < ajhat[k]) {
					ajhat[k] = a[k][j];
					jhat[k] = j;
				}
			}
		}
		
		
		// step 4
		boolean demandConstrained = false;
		for (int i = 0; i<nrIn; i++) {
			if ((Ut[k][jhat[k]][i] == true) & (S1[i] <= a[k][jhat[k]]*C1[i])) {
				demandConstrained = true;
				for(int j = 0; j<nrOut; j++) {
					q[i][j] = S2[i][j];
				}
				for(int j = 0; j<nrOut; j++) {
					if (Jt[k][j]) {
						Rt[k+1][j] = Rt[k+1][j] - S2[i][j];
						//Ut[k+1][j] = Ut[k][j].clone();
						Ut[k+1][j][i] = false;
						
						boolean tmpval = false;
						for (boolean val: Ut[k+1][j]) {
							if (val) {tmpval = true;}
						}
						if (tmpval == false) {
							//adef[j] = 1;
							//Udef[j]
							//Jt[k+1] = Jt[k];
							Jt[k+1][j] = false;
						}
					}
				}
				
			}
		}
		if (!demandConstrained) {
			for (int i = 0; i<nrIn; i++) {
				if (Ut[k][jhat[k]][i] == true) {
					for(int j = 0; j<nrOut; j++) {
						q[i][j] = ajhat[k]*C2[i][j];
					}
					for(int j = 0; j<nrOut; j++) {
						if (Jt[k][j]) {
						Rt[k+1][j] = Rt[k+1][j] - ajhat[k]*C2[i][j];
					
						if (j != jhat[k]) {
							//Ut[k+1][j] = Ut[k][j].clone();
							for (int val = 0; val < nrIn; val++) {
								if (Ut[k][jhat[k]][val]) {
									Ut[k+1][j][val] = false;
								}
							}
							boolean tmpval = false;
							for (int val = 0; val < nrIn; val++) {
								if (Ut[k+1][j][val]) {
									tmpval = true;
								}
							}
							if (tmpval == false) {
								//Jt[k+1] = Jt[k];
								Jt[k+1][j] = false;
								
							}
							
						} else {
							//Jt[k+1] = Jt[k];
							Jt[k+1][jhat[k]] = false;
						}
					}
					}
				}
			}
		}
		
		boolean empty = true;
		for(int j=0;j<nrOut;j++) {
			if (Jt[k+1][j])
				empty = false;
		}
		if (empty)
			stop = true;
		else
			k++;
		
		}
		//U[k][jhat[k]]
		
		
		for (int i = 0; i< nrIn; i++) {
			double tmpj = 0;
			for (int j = 0; j< nrOut; j++) {
				 tmpj += q[i][j];
				
				
			}
			calcfluxesIn[i] = tmpj;
			if (calcfluxesIn[i] > 20000) 
				throw new Error("high flux: " + calcfluxesIn[i]);
			
			
		}
		for (int j = 0; j< nrOut; j++) {
		
			double tmpi = 0;
			for (int i = 0; i< nrIn; i++) {
				 tmpi += q[i][j];
				
				
			}
			calcfluxesOut[j] = tmpi;
		}
		
		ArrayList<Double[]> result = new ArrayList<Double[]>();
		result.add(0,calcfluxesIn);
		result.add(1,calcfluxesOut);
		
		return result;
		//System.out.println("geslaagd");
		
		
	}

}
