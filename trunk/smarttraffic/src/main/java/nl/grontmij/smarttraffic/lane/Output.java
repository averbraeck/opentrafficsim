package nl.grontmij.smarttraffic.lane;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

public class Output {

	public Output() {
		// TODO Auto-generated constructor stub
	}

	public static BufferedWriter initiateMeasure(String dirOutput,
			final OTSSimulatorInterface simulator) {
		BufferedWriter outputFile = null;
		{
			try {
				if (!new File(dirOutput).exists()) {
					Files.createDirectory(Paths.get(dirOutput));
				}
				File file = new File(dirOutput + "/measure.xls");
				if (!file.exists()) {
					file.createNewFile();
				}
				outputFile = new BufferedWriter(new FileWriter(
						file.getAbsoluteFile()));
				//outputFile.write("Time\tSensor\tCar\n");
				//outputFile.flush();
			} catch (IOException exception) {
				exception.printStackTrace();
				System.exit(-1);
			}
		}
		return outputFile;
	}

	public static BufferedWriter initiateReportNumbers(String dirOutput,
			final OTSSimulatorInterface simulator) {
		BufferedWriter outputFile = null;

		try {
			if (!new File(dirOutput).exists()) {
				Files.createDirectory(Paths.get(dirOutput));
			}
			File file = new File(dirOutput + "/reportNumbers.xls");
			if (!file.exists()) {
				file.createNewFile();
			}
			outputFile = new BufferedWriter(new FileWriter(
					file.getAbsoluteFile()));
			
			//outputFile.write("Time\tNrCars\n");
			//outputFile.flush();
		} catch (IOException exception) {
			exception.printStackTrace();
			System.exit(-1);
		}
		return outputFile;

	}
}
