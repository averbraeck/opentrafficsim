package nl.tudelft.otsim.Simulators.MacroSimulator.FundamentalDiagrams;

import nl.tudelft.otsim.Simulators.MacroSimulator.MacroCell;

public class FDTrian implements IFD {
	final int nrParameters = 4;
	public FDTrian() {
		// TODO Auto-generated constructor stub
	}
	public double calcQ(double[] param) {
		if (param.length != nrParameters) {
			throw new Error("Wrong number of parameters");
		}
		double k = param[0];
		double vLim = param[1];
		double kCri = param[2];
		double kJam = param[3];
		
		
		if (k<0 || k > kJam) {
    		throw new Error ("density is not correct" + Double.toString(k));
    	
		} else if (k<kCri) {
    		/** triangular FD **/
    		return k*vLim;
		} else {
    		/** triangular FD **/
    		return (kJam - k)/(kJam - kCri)*(kCri*vLim);
	
		}
	}public double calcV(double[] param) {
		if (param.length != nrParameters) {
			throw new Error("Wrong number of parameters");
		}
		double k = param[0];
		double vLim = param[1];
		double kCri = param[2];
		double kJam = param[3];
		
		if (k<0 || k > kJam) {
    		throw new Error ("density is not correct" + Double.toString(k));
    	
		} else if (k<kCri) {
    		/** triangular FD **/
    		return vLim;
		} else {
    		/** triangular FD **/
    		return (kJam/k - 1)/(kJam - kCri)*(kCri*vLim);
	
		}
	}
	public double calcQ(MacroCell mc, double[] addedParam) {
		if (addedParam.length != nrParameters) {
			throw new Error("Wrong number of parameters");
		}
		double[] param = new double[]{mc.KCell + addedParam[0], mc.vLim + addedParam[1], mc.kCri + addedParam[2], mc.kJam+ addedParam[3]};
		return calcQ(param);


	}
	public double calcQ(MacroCell mc) {
		double[] param = new double[]{mc.KCell, mc.vLim, mc.kCri, mc.kJam};
		return calcQ(param);
	}

	
	public double calcV(MacroCell mc) {
		double[] param = new double[]{mc.KCell, mc.vLim, mc.kCri, mc.kJam};

		return calcV(param);
	}
	public double calcV(MacroCell mc, double[] addedParam) {
		if (addedParam.length != nrParameters) {
			throw new Error("Wrong number of parameters");
		}
		double[] param = new double[]{mc.KCell + addedParam[0], mc.vLim + addedParam[1], mc.kCri + addedParam[2], mc.kJam+ addedParam[3]};

		return calcV(param);
	}



/*public double calcQcap(MacroCell mc) {
	double kJam = mc.kJam;
	double kCri = mc.kCri;
	double vLim = mc.vLim;
	return kCri*vLim*(1-(kCri/kJam));
}*/
public double calcQCap(MacroCell mc) {
	double[] param = new double[]{mc.kCri, mc.vLim, mc.kCri, mc.kJam};
	return calcQ(param);
}
public double calcQCap(double[] parameters) {
	double[] param = new double[]{parameters[1], parameters[0], parameters[1], parameters[2]};
	return calcQ(param);
	
}
public double calcQCap(MacroCell mc, double[] parameters) {
	double[] param = new double[]{mc.kCri + parameters[2], mc.vLim+parameters[1], mc.kCri+parameters[2], mc.kJam+parameters[3]};
	return calcQ(param);
	
}

}
