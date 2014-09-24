package nl.tudelft.otsim.ModelIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import nl.tudelft.otsim.GUI.WED;
import static javax.swing.GroupLayout.Alignment.*;

public class FileChooser extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private final JButton[] fileButton;
    private final JTextField[] textField;
    private final JLabel[] label; 
    private final String moduleName;
    private String command;

	public FileChooser(String[] labels, String[] fileNames, String[] actions, String moduleName) {
		int size = labels.length;
		if (fileNames.length != size)
			throw new Error("Array fileNames has wrong length (got " + fileNames.length + "; expected " + size + ")");
		if (actions.length != size)
			throw new Error("Array actions has wrong length (got " + actions.length + "; expected " + size + ")");
		fileButton = new JButton[size];
		textField = new JTextField[size];
		label = new JLabel[size];
		this.moduleName = moduleName; 
        for (int i = 0; i < size; i++) {
        	fileButton[i] = new JButton(fileNames[i]);
        	fileButton[i].setToolTipText(fileNames[i]);
        	fileButton[i].addActionListener(this);
        	fileButton[i].setActionCommand(actions[i]);
        	fileButton[i].setEnabled(true);
        	textField[i] = new JTextField();
        	label[i] = new JLabel(labels[i]);
        }

        // remove redundant default border of check boxes - they would hinder
        // correct spacing and aligning (maybe not needed on some look and feels)

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        SequentialGroup sg = layout.createSequentialGroup();
        ParallelGroup pg = layout.createParallelGroup(LEADING);
        for (int i = 0; i < size; i++)
        	pg.addComponent(label[i], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
        sg.addGroup(pg);
        pg = layout.createParallelGroup(LEADING);
        for (int i = 0; i < size; i++)
        	pg.addComponent(textField[i], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        sg.addGroup(pg);
        pg = layout.createParallelGroup(LEADING);
        for (int i = 0; i < size; i++)
        	pg.addComponent(fileButton[i], 10, 30, 100);
        sg.addGroup(pg);
        layout.setHorizontalGroup(sg);
        sg = layout.createSequentialGroup();
        for (int i = 0; i < size; i++) {
        	pg = layout.createParallelGroup(0 == i ? BASELINE : LEADING);
        	pg.addComponent(label[i]);
        	pg.addComponent(textField[i], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
        	pg.addComponent(fileButton[i], 25, 25, 25);
        	sg.addGroup(pg);
        }
        layout.setVerticalGroup(sg);
    }
	
	JButton[] getFileButton() {
		return fileButton;
	}
	
	JTextField[] getTextField() {
		return textField;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		command = e.getActionCommand();
    	// The module name is set in the classes the use this FileChooser 
		// If not done: no action!!!!
		System.out.println("actionperformed: command is " + command);
		if (moduleName == "ImportModelShape")  {
			try {
				ImportModelShapeWizard.importModel();
			} catch (Exception e1) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Error loading model:\r\n%s", 
						WED.exeptionStackTraceToString(e1));
				e1.printStackTrace();
			}
		}
		
		if (moduleName == "LoadModel")  {
			try {
				LoadModel.loadModel();
			} catch (Exception e1) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Error loading model:\r\n%s", 
						WED.exeptionStackTraceToString(e1));
				e1.printStackTrace();
			}
		}
		
		if (moduleName == "SaveModel")  {
			try {
				SaveModel.saveModel();
			} catch (Exception e1) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Error saving model:\r\n%s", 
						WED.exeptionStackTraceToString(e1));
				e1.printStackTrace();
			}
		}
		
		if (moduleName == "ExportModel")  {
			try {
				ExportModel.exportModel();
			} catch (Exception e1) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Error exporting model:\r\n%s", 
						WED.exeptionStackTraceToString(e1));
				e1.printStackTrace();
			}
		}
	}

    String getCommand() {
		return command;
	}

}