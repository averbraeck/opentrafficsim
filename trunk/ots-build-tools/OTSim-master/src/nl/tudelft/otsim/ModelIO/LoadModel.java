package nl.tudelft.otsim.ModelIO;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import nl.tudelft.otsim.GUI.FileDialog;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.Model;
import nl.tudelft.otsim.ModelIO.FileChooser;

import com.vividsolutions.jts.geom.Coordinate;

public class LoadModel implements ActionListener {
    private static JPanel cards = new JPanel(new CardLayout());
	private int cardCounter ;
	private JButton cancelButton;
	private JButton finishButton;
    private JButton nextButton;
    private JButton prevButton;

	private JCheckBox optionModel;
	private JCheckBox optionDemand;
	JFrame frame  ;
    private static FileChooser fChooser;
    private Model loadedModel;
	private static int index; 
	private static int fileCount;

	public LoadModel() {
		Class<?> klass;
		klass = Coordinate.class;
		URL location = klass.getResource('/' + klass.getName().replace(".", "/") + ".class");
		System.out.print(location);
        frame = new JFrame() ;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(700, 400));
        cards.setBorder(BorderFactory.createLineBorder(Color.black));
        cardCounter = 0;  // start at the initial/first card       
    	JPanel choices = new JPanel() ;
        choices.setLayout(new GridBagLayout());	        
        GridBagConstraints gbConstraints = new GridBagConstraints();

        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        gbConstraints.insets = new Insets(15,30,0,0);
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
/*	        optionModel= new JCheckBox("Load Model (network and geo-objects");
	        optionModel.addActionListener(this);
	        optionModel.setEnabled(true);
	        optionModel.setSelected(true);
	        choices.add(optionModel, gbConstraints);	
	        gbConstraints.weightx = 0.4;
	        gbConstraints.weighty = 0.4;
	        gbConstraints.gridx = 0;
	        gbConstraints.gridy = 1;
	        optionDemand = new JCheckBox("Load Traffic Demand (trip patterns)");
	        optionDemand.addActionListener(this);
	        optionDemand.setEnabled(true);
	        optionDemand.setSelected(true);
	        choices.add(optionDemand, gbConstraints);
	        cards.add(choices, "Options");*/

        fChooser = null;
    	int files = 2;
    	String[] fileNames = new String[files];
    	fileNames[0] = "Browse";
    	fileNames[1] = "Browse";
    	String[] labels = new String[files];
    	labels[0] = "Network File";
    	labels[1] = "Traffic Demand File";
    	fileCount = fileNames.length;
    	String[] commandNames = new String[files];
    	commandNames[0] = "Model";
    	commandNames[1] = "Demand";
    	fChooser = new FileChooser(files, labels, fileNames, commandNames);
    	fChooser.setVisible(true);
    	fChooser.setModuleName("LoadModel");
    	// The module name is used in the class FileChooser to select the action
    	// of the file chooser!!!!
    	cards.add(fChooser, fChooser.toString());

    	JPanel control = new JPanel() ;
        prevButton = new JButton("\u22b2Prev");
        prevButton.addActionListener(this);
        prevButton.setEnabled(false);
        control.add(prevButton);	        
        nextButton = new JButton("Next\u22b3");
        nextButton.addActionListener(this);
        nextButton.setEnabled(false);
        control.add(nextButton);
        finishButton = new JButton("Finish");
    	finishButton.addActionListener(this);
    	finishButton.setEnabled(true);  	
    	control.add(finishButton);
    	cancelButton = new JButton("Cancel");
    	cancelButton.addActionListener(this);
    	cancelButton.setEnabled(true);  	
    	control.add(cancelButton);

    	frame.add(cards, BorderLayout.CENTER);
        frame.add(control, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.next(cards);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();
		System.out.println("actionperformed: command is " + command);
		if (command.startsWith("Load Model") )   {
			//this.getNextButton().setEnabled(true);
	        Main.mainFrame.getLoadModel().getNextButton().setEnabled(true);
	        if (optionModel.isSelected())  {
	        	fChooser.getFileButton()[0].setEnabled(true);
	        }
        	else  {
	        	fChooser.getFileButton()[0].setEnabled(false);        		
        	}        		
	    }

		if (command.startsWith("Load Traffic Demand") )   {
	        Main.mainFrame.getLoadModel().getNextButton().setEnabled(true);
	        if (optionModel.isSelected())  {
	        	fChooser.getFileButton()[1].setEnabled(true);	        		
	        }
        	else  {
	        	fChooser.getFileButton()[1].setEnabled(false);
        	} 
		}
		if (command.startsWith("Finish")) {
			System.out.println("Finish" + command);
			try {					
				Main.mainFrame.model = new Model(fChooser.getTextField()[0].getText(), fChooser.getTextField()[1].getText());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        Main.mainFrame.setTitle("Imported");
	        Main.mainFrame.setActiveGraph();
	        Main.mainFrame.zoomToScene();
	        Main.mainFrame.menuItemSaveModel.setEnabled(true);
	        this.frame.dispose();
		}
		if (command.startsWith("Cancel")) {
			System.out.println("Cancel" + command);
			loadedModel = null;
			this.frame.dispose();
		}
		if (command.startsWith("\u22b2Prev")) {
            CardLayout cl = (CardLayout) cards.getLayout();
            if (cardCounter == cards.getComponentCount()-1)  {
            	this.finishButton.setEnabled(false);
            }
            if (cardCounter > 0)  {
            	cl.previous(cards);
            	this.nextButton.setEnabled(true);
            	cardCounter--;
            	System.out.print( "cardnumber" + cardCounter +"prev" );
            }
            if (cardCounter == 0)
            	this.prevButton.setEnabled(false);
		}
		if (command.startsWith("Next\u22b3")) {
            CardLayout cl = (CardLayout) cards.getLayout();
            if (cardCounter != cards.getComponentCount()-1)  {
            	cl.next(cards);
            	this.prevButton.setEnabled(true);
            	cardCounter++;
            	System.out.print( "cardnumber" + cardCounter +"next" );
            }
            if (cardCounter == cards.getComponentCount()-1)  { 
            	this.finishButton.setEnabled(true);
            	this.nextButton.setEnabled(false);
            }
        }
	}
	
	
	public static void loadModel() throws Exception   {		
		if (fChooser.getCommand().startsWith("Model")) {
    		index = 0;            	
    		openDialog(index);
		}
		else if (fChooser.getCommand().startsWith("Demand")) {
			index = 1;
			openDialog(index);
		}
    
	}
	
    private static void openDialog(int index) {
    	String fileName = FileDialog.showFileDialog(true, "XML", "*.xml", Main.mainFrame.initialDirectory);
    	if (null == fileName)
    		return;
    	String file = new File(fileName).getPath();
    	Main.mainFrame.initialDirectory = new File(fileName).getParent();
		fChooser.getTextField()[index].setText(file);
        System.out.printf("User selected file \"%s\"", file);
    }
    
    private static String[] names = null;
	public static String[] getNames() {
		return names;
	}
    
	public Model getLoadedModel() {
		return loadedModel;
	}

	public void setLoadedModel(Model loadedModel) {
		this.loadedModel = loadedModel;
	}

    public static JPanel getCards() {
		return cards;
	}

	public static void setCards(JPanel cards) {
		LoadModel.cards = cards;
	}

	public JButton getFinishButton() {
		return finishButton;
	}

	public void setFinishButton(JButton finishButton) {
		this.finishButton = finishButton;
	}

	public JButton getNextButton() {
		return nextButton;
	}

	public void setNextButton(JButton nextButton) {
		this.nextButton = nextButton;
	}

	public JButton getPrevButton() {
		return prevButton;
	}

	public void setPrevButton(JButton prevButton) {
		this.prevButton = prevButton;
	}

}