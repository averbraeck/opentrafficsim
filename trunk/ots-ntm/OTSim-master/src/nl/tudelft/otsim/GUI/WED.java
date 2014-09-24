package nl.tudelft.otsim.GUI;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Warning and Errors Dialogs.
 * <br />
 * All methods and fields in this class are static; there is never a need to
 * create an instance of this class.
 * @author Peter Knoppers
 *
 */
public class WED {
	/** Show an informational dialog */
	public static final int INFORMATION = 0;
	/** Show a warning dialog */
	public static final int WARNING = 1;
	/** Show an error dialog regarding something external to this program */ 
	public static final int ENVIRONMENT = 2;
	/** 
	 * Show an error dialog regarding something method in this program 
	 * that has some outside cause 
	 */
	public static final int ENVIRONMENTERROR = 3;
	/** Show an error dialog regarding this program */
	public static final int PROGRAMERROR = 4;

	/**
	 * Show some problem in a dialog window.
	 * <br />
	 * The problem is described by a format string and zero or more Object
	 * arguments.
	 * <br />
	 * If conversion fails, the severity of the problem is raised to the
	 * PROGRAMERROR value.
	 * @param severity Integer; severity of the problem; should be one of
	 * the constant fields defined in this class. The value is used to select
	 * the prefix for the message.
	 * @param format String; format used to display the problem.
	 * @param arguments Zero or more java.lang.Object(s) to be used by the
	 * <code>format</code> parameter  
	 * @return Boolean; true if the severity was (raised to) PROGRAMERROR;
	 * otherwise false
	 */
	public static Boolean showProblem (int severity, String format, Object... arguments) {
		String problem;
		try {
			problem = String.format(nl.tudelft.otsim.GUI.Main.locale, format, arguments);
		} catch (Exception e) {
			problem = "Caught error in String.format";
			severity = PROGRAMERROR;
		}
		String severityText;
		int option = JOptionPane.INFORMATION_MESSAGE;
		switch (severity) {
		case INFORMATION: 
			severityText = "Information";
			option = JOptionPane.INFORMATION_MESSAGE;
			break;
		case WARNING: 
			severityText = "Warning";
			option = JOptionPane.WARNING_MESSAGE;
			break;
		case ENVIRONMENT:
			severityText = "Error";
			option = JOptionPane.ERROR_MESSAGE;
			break;
		case ENVIRONMENTERROR: 
			severityText = "Error in " + Thread.currentThread().getStackTrace()[2].getMethodName();
			option = JOptionPane.ERROR_MESSAGE;
			break;
		case PROGRAMERROR: 
			severityText = "Program error in " + Thread.currentThread().getStackTrace()[2].getMethodName(); 
			int skip = 2;
			for (StackTraceElement element : Thread.currentThread().getStackTrace())
				if (--skip < 0)
					problem = problem + "\n\t" + element.toString();
			option = JOptionPane.ERROR_MESSAGE;
			break;
		default:
			severityText = "Program error";
			severity = PROGRAMERROR;
			format = "Bad call to showProblem";
			break;		
		}
		System.err.println(severityText);
		System.err.println(problem);
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame/* getContentPane()*/, problem, severityText, option);

		return (PROGRAMERROR == severity);
	}

	/**
	 * Create a String with the stack trace of an Exception.
	 * <br />
	 * Adapted from <a href="http://stackoverflow.com/questions/1149703/stacktrace-to-string-in-java">stack
	 * trace - Stacktrace to string in Java - Stack Overflow</a>.
	 * @param exception java.lang.Expection to convert
	 * @return String; textual representation of the stack trace
	 */
	public static String exeptionStackTraceToString(Exception exception) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		return stringWriter.toString();
	}
}