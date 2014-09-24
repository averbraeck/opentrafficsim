package nl.tudelft.otsim.Simulators.MacroSimulator.MultiClass;

import java.awt.Color;
//import java.awt.geom.Point2D;
//import java.awt.geom.Point2D.Double;








import nl.tudelft.otsim.Simulators.MacroSimulator.Model;
import nl.tudelft.otsim.Simulators.MacroSimulator.FundamentalDiagrams.IFD;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.Node;
import nl.tudelft.otsim.GUI.GraphicsPanel;






import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;






import nl.tudelft.otsim.GeoObjects.Vertex;


/**
 * A single cell of road. Different cells are connected in the
 * longitudinal or lateral direction. The <tt>jMacroCell</tt> object also provides a
 * few network utilities to get traffic state information.
 * <br>
 * <br>
 */
public class MacroCellMultiClass {
	// TODO: delete unnecessary variables and methods! 
	// TODO: merges and splits at nodes
	// TODO: boundary conditions
	
	
	private double width = 0;
	public ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	//private int id;
	//private double speedLimit;
	public ArrayList<Integer> ins = new ArrayList<Integer>();
	public ArrayList<Integer> outs = new ArrayList<Integer>();
	
	// Geographical info
    /** Array of x-coordinates defining the lane curvature. */
    public double[] x;

    /** Array of y-coordinates defining the lane curvature. */
    public double[] y;

    /** Length of the lane [m]. */
    public double l;

    /** Main model. */
    final public Model model;

	/** ID of cell for user recognition. */
    public int id;

    /** Set of upstream cells in case of a merge. */
    public java.util.ArrayList<MacroCellMultiClass> ups = new java.util.ArrayList<MacroCellMultiClass>();

    /** Set of downstream cells in case of a split. */
    public java.util.ArrayList<MacroCellMultiClass> downs = new java.util.ArrayList<MacroCellMultiClass>();
    
    /** Set of upstream cells in case of a merge. */
    public java.util.ArrayList<Integer> upsInt = new java.util.ArrayList<Integer>();

    /** Set of downstream cells in case of a split. */
    public java.util.ArrayList<Integer> downsInt = new java.util.ArrayList<Integer>();

    /** Left cell (if any). for multi-lane modeling */
    public MacroCellMultiClass left;

    /** Right cell (if any). */
    public MacroCellMultiClass right;
    
    public Node nodeIn;
    
    public Node nodeOut;
    
    public int indexNodeIn;
    public int indexNodeOut;
    
    private int configNodeIn;
    private int configNodeOut;

    /** Destination number, NODESTINATION if no destination. */
    public int destination;

    /** Origin number, NOORIGIN if no origin. */
    public int origin;

    //Traffic states
    /** Flow in this cell. [veh/s] */
    public double[] QCell;
    
    /** Density in this cell. [veh/m] */
    public double[] KCell;
    
    /** Average spacing in this cell. [km or m] */
    //public double SCell;
    
    /** Average speed in this cell. [m/s] */
    public double[] VCell;
    
    /** Flux into this cell. [ veh/s] */
    public double FluxIn;
    
    /** Flux out from this cell. [veh/s] */
    public double FluxOut;
    
    /** Supply from this cell. [veh/s] */
    public double Supply;
    
    /** Demand out from this cell. [veh/s] */
    public double Demand;
    
    public double[] DemandTest;
    public double[] SupplyTest;
    public IFD fd;
    
    // Parameters    
    /** Legal speed limit [m/s]. */
    public double vLim;
    
    /** Legal critical density [veh/m]. */
    public double kCri;
    
    /** Legal jam density [veh/m]. */
    public double kJam;
    
    /** Legal flow capacity [veh/m/lane]. */
    public double qCap;
    
    public double vCri;
    
    public double[] FluxIn2;
	public double[] FluxOut2;
	public int lanes;
	private ArrayList<VehicleClass> vehicleClasses = new ArrayList<VehicleClass>();
	private int nrVehicleClasses;
	
	public double[] vehicleShare;
	
	public double effDensity;
	public double effFlow;
	public double effDemand;
	public double effSupply;
	public double[] labda;
	public double[] classSupply;
	public double[] classDemand;
	public double[] classFluxOut;
	public double[] classFluxIn;
	
	public void addVehicleClass(VehicleClass vehicleClass) {
		vehicleClasses.add(vehicleClass);
	}
	public void updateVehicleShare() {
		double[] temp = new double[nrVehicleClasses];
		double[] n = new double[nrVehicleClasses];
		//double total = 0;
		for (int u=0; u<nrVehicleClasses; u++) {
			temp[u]= vehicleClasses.get(u).getSpaceOccupancy(VCell[u]);
			//total += temp[u];
		}
		for (int u=0; u<nrVehicleClasses; u++) {
			n[u]=temp[u]/temp[0];
		}
		this.vehicleShare = n;
	}
    
	public void updateEffectiveDensity() {
		//double vCri = 85/3.6;
		double sumACong = 0;
		double sumBCong = 0;
		double sumAFree = 0;
		double sumBFree=0;
		double tmpEffDensity=0;
		double a1ff=vehicleClasses.get(0).getAFreeFlow();
		double b1ff=vehicleClasses.get(0).getBFreeFlow(vCri, kCri);
		double a1con=vehicleClasses.get(0).getACongested(vCri, kCri, kJam);
		double b1con=vehicleClasses.get(0).getBCongested(vCri, kCri, kJam);
		
		for (int u=0; u<nrVehicleClasses; u++) {
			sumACong += vehicleClasses.get(u).getACongested(vCri, kCri, kJam)*KCell[u];
			sumBCong += vehicleClasses.get(u).getBCongested(vCri, kCri, kJam)*KCell[u]; 
			sumAFree += vehicleClasses.get(u).getAFreeFlow()*KCell[u]; 
			sumBFree += vehicleClasses.get(u).getBFreeFlow(vCri, kCri)*KCell[u]; 
		}
		if (b1ff !=0) {
			tmpEffDensity = (a1ff- sumBFree - Math.sqrt(Math.pow(a1ff - sumBFree,2)+4*b1ff*sumAFree))/(-2*b1ff);
		} else {
			tmpEffDensity = (sumAFree)/(a1ff - sumBFree);
		}
		if (tmpEffDensity <= kCri) {
			
			this.effDensity = tmpEffDensity;
		} else {
			if (b1con !=0) {
				tmpEffDensity = (a1con - sumBCong - Math.sqrt(Math.pow(a1con - sumBCong,2)+4*b1con*sumACong))/(-2*b1con);
			} else {
				tmpEffDensity = (sumACong)/(a1con - sumBCong);
			}
			this.effDensity = tmpEffDensity;
		}
	}
	
    /**
     * Constructor that will calculate the lane length from the x and y
     * coordinates.
     * @param x X coordinates of curvature.
     * @param y Y coordinates of curvature.
     * @param id User recognizable lane id.
     * @param model Main model.
     */
    public MacroCellMultiClass(Model model, double[] x, double[] y, int id) {
        this.model = model;
        this.x = x;
        this.y = y;
        this.id = id;
        l = calcLength();
        
    }
    public MacroCellMultiClass(Model model, double length, int id) {
        this.model = model;
        this.l = length;
        //this.x = {0,0};
        //this.y = {0,length};
        this.id = id;
             
    }
    public MacroCellMultiClass(Model model) {
    	this.model = model;
    	this.l = calcLength();
    }
    public void init(int nrVehicleClasses) {
    	
    }
    public void init() {
    	this.nrVehicleClasses = vehicleClasses.size();
    	lanes = (int) (width/3.5);
    	kCri = 0.020*lanes;
    	kJam = 0.125*lanes;
    	vCri = 85/3.6;
    	qCap = kCri*vCri;
    	//qCap = fd.calcQcap(this);
    	KCell = new double[nrVehicleClasses];
    	QCell = new double[nrVehicleClasses];
    	VCell = new double[nrVehicleClasses];
    	labda = new double[nrVehicleClasses];
    	vehicleShare = new double[nrVehicleClasses];
    	classSupply = new double[nrVehicleClasses];
    	classFluxOut = new double[nrVehicleClasses];
    	classFluxIn = new double[nrVehicleClasses];
    	//updateEffectiveDensity();
    	//updateVehicleShare();
    	//updateEffectiveFlow();
    	
    	//l = calcLength();
    	
    	
    	//indexNodeIn = nodeIn.cellsOut.indexOf(this);
    	//indexNodeOut = nodeOut.cellsIn.indexOf(this);
    	
    }
    
    public void setWidth(double w) {
		this.width = w;
	}
	public double getWidth() {
		return width;
	}
	public void setVLim(double sl) {
		this.vLim = sl;
	}
	public double getVLim() {
		return vLim;
	}

	public void addVertex(Vertex vertex) {
		vertices.add(vertex);
	}
	public void addVertex(int i, Vertex vertex) {
		vertices.add(i, vertex);
	}
	public double calcLength() {
		double tmplength = 0;
		for (int i = 0; i<=(vertices.size()-2); i++) {
			tmplength += vertices.get(i).distance(vertices.get(i+1));
		}
		return tmplength;
	}
	
	public double[] calcPointAtDistance(double p) {
		double length = calcLength();
		if (p>length)
			throw new Error("p is larger than distance l");
		
		int i = 0;
		
		double arc = 0;
		double cumlength = 0;
		//System.out.println(p);
		while (cumlength <= p) {
			//System.out.println(cumlength);
			arc = vertices.get(i).distance(vertices.get(i+1));
			//System.out.println(arc);
			cumlength += arc;
			i++;
		}
		double ratio = (p-(cumlength-arc))/arc;
		//System.out.println("ratio:");
		//System.out.println(ratio);
		double result[] = new double[3];
		result[0] = ratio * (vertices.get(i).getX() - vertices.get(i-1).getX()) + vertices.get(i-1).getX();
		result[1] = ratio * (vertices.get(i).getY() - vertices.get(i-1).getY()) + vertices.get(i-1).getY();		
		result[2] = i;
		return result;
		
	}
	public void sortVertices() {
		//vertices.
	}
	public void addIn(Integer i) {
		ins.add(i);
	}
	public void addOut(Integer i) {
		outs.add(i);
	}
	@SuppressWarnings("unchecked")
	public ArrayList<MacroCellMultiClass> splitInParts(int nrParts) {
		ArrayList<MacroCellMultiClass> result = new ArrayList<MacroCellMultiClass>();
		System.out.println("Joined link " + id + " is splitted into " + nrParts + " parts");
		//System.out.println(nrParts);
		if (nrParts == 1 || nrParts == 0) {
			result.add(this);
		} else {
		
			int vert = 0;
			this.l = calcLength();
		for (int i = 0; i< nrParts - 1; i++) {
			
			MacroCellMultiClass m = new MacroCellMultiClass(this.model);
			
			m.setWidth(this.width);
			m.setVLim(this.vLim);
			m.setId(new Random().nextInt());
			m.setConfigNodeIn(this.configNodeIn);
			m.setConfigNodeOut(this.configNodeOut);
			
			double res[] = this.calcPointAtDistance((i+1)*(this.l)/(nrParts));
			//System.out.println(Arrays.toString(res));
			//System.out.println("vert: " + Integer.toString(vert) + " res: " + Double.toString(res[2]));
			m.vertices = new ArrayList<Vertex>(this.vertices.subList(vert, (int) res[2]));
			vert = (int) res[2];
			m.vertices.add(new Vertex(res[0],res[1],0));
			this.vertices.add(vert, new Vertex(res[0],res[1],0));
		
			
			result.add(m);
		}
		this.vertices = new ArrayList<Vertex>(this.vertices.subList(vert, this.vertices.size()));
	
		
		result.get(0).ups = (ArrayList<MacroCellMultiClass>) this.ups.clone();
		for (MacroCellMultiClass c: ups) {
			c.downs.remove(this);
			c.downs.add(result.get(0));
		}
		
			
		
		for(int j=1; j < nrParts -1;j++) {
			//result.get(j-1).downs.clear();
			result.get(j-1).downs.add(result.get(j));
			result.get(j).ups.add(result.get(j-1));
		}
		
		
		result.get(nrParts-2).downs.add(this);
		this.ups.clear();
		this.ups.add(result.get(nrParts-2));
		
		
		result.add(this);
		}
		//System.out.println(result.toString());
		return result;
	}
	public String toString() {
		
		
		return "("+this.id+ ", in: "+ this.ups.size()+", out: "+ this.downs.size()+ ")";
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	

    /** calculate using q given fundamental diagram **/
    public double[] calcQ(double[] k) {
    	double[] q = new double[nrVehicleClasses];
    	double[] v = calcV(effDensity);
    	for (int u=0; u<nrVehicleClasses; u++) {
    		q[u] = k[u]*v[u];
    	}
    	return q;
    	/*if (k<0 || k > kJam)
    		throw new Error ("density is not correct" + Double.toString(k));
    	else if (k<kCri) 
    		*//** triangular FD **//*
    		return k*vLim;
    	else
    		*//** triangular FD **//*
    		return (kJam - k)/(kJam - kCri)*(kCri*vLim);*/
    }
    public double[] calcV(double k) {
    	double[] v = new double[nrVehicleClasses];
    	double vCri = 85/3.6;
    	double w = kCri*vCri/(kJam - kCri);
    	if (k<=kCri) {
    		for (int u=0; u<nrVehicleClasses; u++) {
    			v[u] = vehicleClasses.get(u).getVMax() - (vehicleClasses.get(u).getVMax() - vCri)/kCri*k;
    		}
    	} else {
    		Arrays.fill(v, w*(kJam/k - 1));
    	}
    	return v;
    }
    public void updateFlow() {
    	double[] q = new double[nrVehicleClasses];
    	
    	for (int u=0; u<nrVehicleClasses; u++) {
    		q[u] = KCell[u]*VCell[u];
    	}
    	QCell = q;
    	/*if (k<0 || k > kJam)
    		throw new Error ("density is not correct" + Double.toString(k));
    	else if (k<kCri) 
    		*//** triangular FD **//*
    		return k*vLim;
    	else
    		*//** triangular FD **//*
    		return (kJam - k)/(kJam - kCri)*(kCri*vLim);*/
    }

    public void updateVelocity() {
    	double[] v = new double[nrVehicleClasses];
    	
    	double w = kCri*vCri/(kJam - kCri);
    	if (effDensity<=kCri) {
    		for (int u=0; u<nrVehicleClasses; u++) {
    			v[u] = vehicleClasses.get(u).getVMax() - (vehicleClasses.get(u).getVMax() - vCri)/kCri*effDensity;
    		}
    	} else {
    		Arrays.fill(v, w*(kJam/effDensity - 1));
    	}
    	VCell = v;
    }
    public void updateEffectiveFlow() {
    	//double[] q = new double[nrVehicleClasses];
    	double qEff = 0;
    	for (int u=0; u<nrVehicleClasses; u++) {
    		qEff += vehicleShare[u]*QCell[u]; 
    	}
    	this.effFlow = qEff;
    }
    
    public void updateEffectiveDemand() {
    	
    		if (effDensity < kCri)
        		effDemand =  effFlow;
        	else
        		effDemand = qCap;
    	
    	
   	
    }
    public void updateLabda() {
    	double tmpDens = 0;
    	double tmpVel = 0;
    	double effVel = 0;
    	double[] shares = new double[nrVehicleClasses];
    	for (int u=0; u<nrVehicleClasses; u++) {
    		tmpDens += KCell[u];
    		tmpVel += VCell[u];
    		effVel += vehicleShare[u]*VCell[u];
    	}
    	
    	if (tmpDens != 0 && tmpVel == 0) {
    		for (int u=0; u<nrVehicleClasses; u++) {
    			shares[u] = vehicleShare[u]*KCell[u]/effDensity;
    		}
    	} else if (tmpDens == 0 && tmpVel != 0) {
    		for (int u=0; u<nrVehicleClasses; u++) {
    			shares[u] = vehicleShare[u]*VCell[u]/effVel;
    		}
    	} else {
    		for (int u=0; u<nrVehicleClasses; u++) {
    			shares[u] = vehicleShare[u]*QCell[u]/effFlow;
    		}
    	}
    	this.labda = shares;
    }
    
    public void updateEffectiveSupply() {
    	
    	if (effDensity < kCri)
    		effSupply =  qCap;
    	else
    		effSupply = effFlow;
    }
    public void updateClassSupply() {
    	double[] tmpClassSupply = new double[nrVehicleClasses]; 
    	double[] tmpLabda = new double[nrVehicleClasses];
    	if (ups.size() == 0) {
    		Arrays.fill(tmpLabda, 1.0/nrVehicleClasses);
    	} else {
    		tmpLabda = ups.get(0).labda;
    	}
    		
    	for (int u=0; u<nrVehicleClasses; u++) {
    		tmpClassSupply[u]=tmpLabda[u]*effSupply;
    	}
    	this.classSupply = tmpClassSupply;
    }
    public void updateClassDemand() {
    	double[] tmpClassDemand = new double[nrVehicleClasses]; 
    	
    	for (int u=0; u<nrVehicleClasses; u++) {
    		tmpClassDemand[u]=labda[u]*effDemand;
    	}
    	this.classDemand = tmpClassDemand;
    }
    public void updateClassFluxIn() {
    	if (ups.size() == 1) {
    	for (int u=0; u<nrVehicleClasses; u++) {
    		classFluxIn[u] = Math.min(classSupply[u], ups.get(0).classDemand[u])/vehicleShare[u];
    	} } else {
    		for (int u=0; u<nrVehicleClasses; u++) {
        		classFluxIn[u] = Math.min(classSupply[u], 0.3)/vehicleShare[u];
        	}
    	}
    }
    public void updateClassFluxOut() {
    	if (downs.size() == 1) {
    	for (int u=0; u<nrVehicleClasses; u++) {
    		classFluxOut[u] = Math.min(downs.get(0).classSupply[u], classDemand[u])/downs.get(0).vehicleShare[u];
    	} } else { for (int u=0; u<nrVehicleClasses; u++) {
    		classFluxOut[u] = Math.min(0.3, classDemand[u])/vehicleShare[u];
    	}
    		
    	}
    }
    public void calcFluxOut() {
    	
    	FluxOut = nodeOut.fluxesIn[indexNodeOut];
    	
    			
    }
    public void calcFluxIn() {
    	double effectiveFluxIn = nodeIn.fluxesOut[indexNodeIn];
    	FluxIn = nodeIn.fluxesOut[indexNodeIn];
    			
    }
    public void updateDensity() {
    	for (int u=0; u<nrVehicleClasses; u++) {
    		KCell[u] = KCell[u]+model.dt/l*(classFluxIn[u] - classFluxOut[u]);
    	
    	//QCell = calcQ(KCell);
    	//VCell = calcV(KCell);
    	}
    }
   
    
    /**
     * Sets the lane length based on the x and y coordinates. This method is 
     * called within the constructor and should only be used if coordinates are
     * changed afterwards (for instance to nicely connect lanes at the same 
     * point).
     * @return double; Total length
     */

    
    public void draw(GraphicsPanel graphicsPanel) {
    	//Color color = getDensColor(KCell); 
    	//Color color = getDensColor(new Random().nextInt()); 
    	Color color = getVelocityColor(VCell[0]); 
    	graphicsPanel.setStroke((float) (5+(KCell[0]/(kJam/lanes))*15));
		graphicsPanel.setColor(color);
		graphicsPanel.drawPolyLine(vertices);
   	
	}
    public Color getDensColor(double k)
    {
    	double H = 0;
    	if (k<kCri) {
    		H = (kCri-k)/kCri * 0.2+0.1 ;
    	}// Hue (note 0.4 = Green, see huge chart below)
    	else {
    		H = ((kJam-kCri)-(k-kCri))/(kJam-kCri) * 0.1;
    	}// Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float) H, (float)S, (float)B);
    }
    public Color getVelocityColor(double v)
    {
    	if (Double.isNaN(v))
    		return Color.black;
    	else {
	    	double H = (v/vLim)*0.4;
	        double S = 0.9; // Saturation
	        double B = 0.9; // Brightness
	
	        return Color.getHSBColor((float) H, (float)S, (float)B);
    	}
    }
    
    
    /**
     * Retrieve the upstream connected MacroCell of this MacroCell.
     * @return MacroCell; the upstream connected MacroCell of this MacroCell
     */
    public java.util.ArrayList<MacroCellMultiClass> getUps_r() {
    	return ups;
    }
    
    /**
     * Retrieve the downstream connected MacroCell of this MacroCell.
     * @return MacroCell; the downstream connected MacroCell of this MacroCell
     */
    public java.util.ArrayList<MacroCellMultiClass> getDowns_r() {
    	return downs;
    }
    
    /**
     * Retrieve the left MacroCell of this MacroCell.
     * @return MacroCell; the left MacroCell of this MacroCell
     */
    public MacroCellMultiClass getLeft_r() {
    	return left;
    }
    
    /**
     * Retrieve the right MacroCell of this MacroCell.
     * @return MacroCell; the right MacroCell of this MacroCell
     */
    public MacroCellMultiClass getRight_r() {
    	return right;
    }
    
    /**
     * Return the destination of this MacroCell.
     * @return Integer; the destination of this MacroCell, or a negative value if
     * this MacroCell is not a destination
     */
    public int getDestination_r() {
    	return destination;
    }
    
    /**
     * Return the origin of this MacroCell.
     * @return Integer; the origin of this MacroCell or a negative value if this
     * MacroCell is not an origin
     */
    public int getOrigin_r() {
    	return origin;
    }
    
    /**
     * Retrieve the speed limit on this MacroCell.
     * @return Double; the speed limit on this MacroCell in m/s
     */
    public double getSpeedLimit_r() {
    	return vLim;
    }
    
    /**
     * Retrieve the flow of this MacroCell.
     * @return Double; the flow of this MacroCell
     */
    public double[] getQ_r() {
    	return QCell;
    }
    
    /**
     * Retrieve the density of this MacroCell.
     * @return Double; the density of this MacroCell
     */
    public double[] getK_r() {
    	return KCell;
    }
    
    /**
     * Retrieve the average speed of this MacroCell.
     * @return Double; the average speed of this MacroCell
     */
    public double[] getV_r() {
    	return VCell;
    }
    
    
    /**
     * Sets the flow of this MacroCell.
     * @param flow Flow of this cell [veh/h or veh/s].
     */
    public void setQ(double[] flow) {
        this.QCell = flow;
    }
    
    /**
     * Sets the density of this MacroCell.
     * @param density Density of this cell [veh/km or veh/m].
     */
    public void setK(double[] density) {
        this.KCell = density;
    }
    
    /**
     * Sets the average speed of this MacroCell.
     * @param speed Speed of this cell [km/h or m/s].
     */
    public void setV(double[] speed) {
        this.VCell = speed;
    }
    
	/**
     * Returns the ID of the lane.
     * @return ID of the lane.
     */
    public int id() {
        return id;
    }


    /**
     * Returns the speed limit in m/s.
     * @return Speed limit [m/s]
     */
    //public double getVLim() {
    //    return vLim/3.6;
    //}
    void addIn(MacroCellMultiClass m) {
    	ups.add(m);
    }
    void addOut(MacroCellMultiClass m) {
    	downs.add(m);
    }
    public String getIns() {
    	String output;
    	if (ups.size() == 0) {
    		output = "()";
    	} else {
    	output = "(";
    	//System.out.println(output);
    	for (MacroCellMultiClass c: ups) {
    		output = output.concat(Integer.toString(c.id()).concat(","));
    		//System.out.println(output);
    	}
    	output =  output.substring(0, output.length()-1)+")";
    	//System.out.println(output);
    	}
    	return output;
    	
    }
    public String getOuts() {
    	String output;
    	if (downs.size() == 0) {
    		output = "()";
    	} else {
    	output = "(";
    	//System.out.println(output);
    	for (MacroCellMultiClass c: downs) {
    		output = output.concat(Integer.toString(c.id()).concat(","));
    		//System.out.println(output);
    	}
    	output =  output.substring(0, output.length()-1)+")";
    	//System.out.println(output);
    	}
    	return output;
    	
    }
    public void smoothVertices(double smoothingFraction) {
    	if (vertices.size() <3) {
    		//System.out.println("path is too small to be smoothed");
    	} else {
    		ArrayList<Vertex> copyVertices = new ArrayList<Vertex>(); 
    		Vertex origin = vertices.get(0);
    		Vertex destination = vertices.get(vertices.size()-1);
    		copyVertices.add(origin);
    		//copyVertices.add(destinatio)
    		
    		for (Vertex v: vertices.subList(1,vertices.size()-1)) {
    			if (v.distance(destination) < smoothingFraction*copyVertices.get(copyVertices.size()-1).distance(destination)) {
    				copyVertices.add(v);
    			}
    			
    		}
    		copyVertices.add(destination);
    		vertices = copyVertices;
    	}
    }
	/**
	 * @return the configNodeIn
	 */
	public int getConfigNodeIn() {
		return configNodeIn;
	}
	/**
	 * @param configNodeIn the configNodeIn to set
	 */
	public void setConfigNodeIn(int configNodeIn) {
		this.configNodeIn = configNodeIn;
	}
	/**
	 * @return the configNodeOut
	 */
	public int getConfigNodeOut() {
		return configNodeOut;
	}
	/**
	 * @param configNodeOut the configNodeOut to set
	 */
	public void setConfigNodeOut(int configNodeOut) {
		this.configNodeOut = configNodeOut;
	}
}
