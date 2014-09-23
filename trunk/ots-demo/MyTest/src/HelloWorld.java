
public class HelloWorld {
	
    public HelloWorld () {System.out.println("Hello NL!");}
    
    public HelloWorld ( String Hello) {System.out.println(Hello);}  //String: first letter capital
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        System.out.println("Hello world!");
	}
	
	// Comment
	/* Comment */
	
	private double sideP;
	
	public double sideLength; //private double sideLength;
	
	public void setValue (double l) {
		sideLength = l;
		return;
	}
	
	public void setValueP (double l) {
		sideP = l;
		return;
	}
	
	public double getValue () {
		return sideLength ;
	}
	
	public double getArea () {
		return sideLength * sideLength ;
	}

}