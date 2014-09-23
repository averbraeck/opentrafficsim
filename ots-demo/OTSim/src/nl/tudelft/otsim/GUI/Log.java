package nl.tudelft.otsim.GUI;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class implements quick and easy logging of events (mostly) for 
 * debugging.
 * @author pknoppers
 *
 */
public class Log {
	/**
	 * Log one message to a file, or the logging textArea.
	 * <br />
	 * If the file exists; the message is appended; otherwise a new file is
	 * created. The message is defined by a format string and zero or more
	 * arguments. If errors occur during processing of the format string an
	 * error message including the unparsed format string is sent to
	 * System.err.output.
	 * <br />
	 * @param fileName String; name of the file. If <code>fileName</code> is
	 * <code>null</code>, the output is appended to
	 * <code>GUI.Main.mainFrame.textAreaLogging</code>.
	 * @param timeStamp Boolean; if true a time-stamp is appended to the 
	 * message
	 * @param format String that specifies the format of the message
	 * @param args Zero or more {@link java.lang.Object Objects} to be implied 
	 * (interpolated) by the <code>format</code> parameter
	 * @return Boolean; true if no errors occurred; false otherwise
	 */
	public static synchronized boolean logMessage(String fileName, 
			boolean timeStamp, String format, Object... args) {
		boolean returnValue = true;
		String suffix = "";
		if (timeStamp) {
			Date date = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			suffix = "\t" + dateFormat.format(date);
		}
		String outputLine = "";
		try {
			outputLine = String.format(nl.tudelft.otsim.GUI.Main.locale, format, args);
		} catch (Exception e) {
			System.err.println("String.format failed (format =\"" + format + "\"");
			returnValue =  false;
		}
		if (null != fileName) {
			FileOutputStream logFile = null;
			try {
				logFile = new FileOutputStream(fileName, true);
			} catch (Exception e) {
				System.out.println("Open file " + fileName + " for append failed");
				returnValue = false;
			}
			if (null != logFile) {
				Writer writer = new OutputStreamWriter(logFile);
				try {
					writer.write(outputLine + suffix + "\n");
				} catch (Exception e) {
					System.out.println("Write to file " + fileName + " failed");
					returnValue = false;
				}
				try {
					writer.close();
				} catch (Exception e) {
					System.out.println("Closing file " + fileName + " failed");
					returnValue = false;
				}
				try {
					writer.close();
				} catch (IOException e) {
					System.out.println("Caught IOException in writer.close");
				}
			}
		}
		else
			nl.tudelft.otsim.GUI.Main.mainFrame.textAreaLogging.append(outputLine);
		return returnValue;
	}
}