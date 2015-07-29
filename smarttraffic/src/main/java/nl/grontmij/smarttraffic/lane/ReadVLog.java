package nl.grontmij.smarttraffic.lane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.language.io.URLResource;

public class ReadVLog {

	/*
	 * Functions to read (streaming) V-log files: concentrates on detector and
	 * traffic light information
	 */
	private ReadVLog() {
		// cannot be instantiated.
	}

	public static void readVLogFile() {
		URL url = URLResource
				.getResource("C:/Users/p070518/Documents/Grontmij/Projecten/OpdrachtenLopend/343090 Smart Traffic N201/VRI/vri201225/VRI201225_20150601_065500.vlg");
		BufferedReader bufferedReader = null;
		String line = "";
		String path = url.getPath();
		try {

			bufferedReader = new BufferedReader(new FileReader(path));

			// read the first line of the demand file from Omnitrans
			// this line contains the time period of the demand file: as an
			// example....
			// TimePeriod: 07:00:00 - 09:00:00
			if ((line = bufferedReader.readLine()) != null) {
				//line;
				
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
