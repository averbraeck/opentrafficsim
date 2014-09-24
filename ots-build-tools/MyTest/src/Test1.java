import java.util.Calendar;
import java.util.Random;


class Test extends HelloWorld{
	public Test () {System.out.println("Hello NL111!");}
	public Test (String Hello) { super(Hello);}
}


public class Test1 {
	
	
	public Test1() { }  //constructor(default), constructor overloading 
   
	/**
	 * @param args
	 */
	public static void main(String[] args) {  //Java main program is static, internal class is also defined as static
		// TODO Auto-generated method stub
		
		System.out.println("Hello world111!");
		
		HelloWorld aaa;            // define an object
		aaa = new HelloWorld();    // build  an new object
		// HelloWorld aaa = new HelloWorld(); 
		
		aaa.setValue (4);
		
		System.out.println("The sidelength is: " + aaa.sideLength);
		                                                                                                                                                                                                                                                                                                                    
		System.out.println("The area is: " + aaa.getArea() );
		
		// bbb = aaa.clone();
		
		Test bbb = new Test();
		
		Test bbb1 = new Test ("Hello Delft!");
		
		System.out.println(aaa instanceof HelloWorld);
		System.out.println(aaa instanceof Test);
		System.out.println(bbb instanceof HelloWorld);
		System.out.println(bbb instanceof Test);
		
		
		
		// Array study
		
		int ccc [];
		ccc = new int [5];
 		
		//int ddd [][] = new int [3][4];  // 3 is row dimension, 4 is column dimension...
		
		int ddd [][] = {{1,2,3,4},{5,6,7,8},{9,10,11,12}};
		
		//super.sideP; //invalid syntax...
		
		
		
		
		System.out.println("------");
		
		int A =0;
		int B =1;
		
		Integer a1= 0;       // Autoboxing
		Integer a2 = a1;
		Integer a3= new Integer (A);
		
		System.out.println(a2.equals(a1));
		System.out.println(a3.equals(a1));
		System.out.println(a2.equals(a3));
		
		a2 = B;
		a3 = 1;
		
		System.out.println("------");
		System.out.println(a2.equals(a1));
		System.out.println(a3.equals(a1));
		System.out.println(a2.equals(a3));
		
		System.out.println("---Other classes---");
		// Calendar class
		
		Calendar a = Calendar.getInstance();
		System.out.println(a.get(Calendar.YEAR));
		System.out.println(a.get(Calendar.MONTH) + 1);
		System.out.println(a.get(Calendar.DATE));
		System.out.println(a.get(Calendar.DAY_OF_WEEK) - 1);
		
		// Math class and Random class
		Random b = new Random();
		System.out.println(Math.sqrt(Math.abs(b.nextDouble())));
		

	}
	

}




