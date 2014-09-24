package nl.tudelft.otsim.GUI;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

/**
 * The FileDialog class is (or rather should) be used for all file dialogs in
 * Open Traffic
 * 
 * @author Peter Knoppers
 */
public class FileDialog {
	/**
	 * Show a file selector dialog to the user.
	 * @param reading Boolean; set to true if only existing files may be 
	 * selected, set to false if a non-existing file may be returned and new
	 * directories may be created.
	 * If reading is false and an existing file is selected that is not equal
	 * to the defaultName, an overwrite warning dialog will be displayed.
	 * @param fileType String; expected file type
	 * @param fileTypeDescription String; description of expected file type
	 * @param defaultName String; path to directory shown initially in the
	 * file selector dialog; if it ends on an existing file name, that file
	 * is initially selected
	 * @return String; absolute path of the selected file (or null when the
	 * operation was cancelled by the user)
	 */
	public static String showFileDialog(boolean reading, String fileType, String fileTypeDescription, String defaultName) {
    	FileFilter filter = new ExtensionFileFilter(fileTypeDescription, fileType);
        javax.swing.JFileChooser fileChooser = new JFileChooser();
    	fileChooser.setFileFilter(filter);
    	File defaultFile = new File(defaultName);
    	fileChooser.setCurrentDirectory(defaultFile);
    	if (defaultFile.isFile()) {
    		fileChooser.setSelectedFile(defaultFile);
    		fileChooser.ensureFileIsVisible(defaultFile);
    	}
    	int result;
    	if (reading) {
    		disableNewFolderButton(fileChooser);
    		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    		result = fileChooser.showOpenDialog(null);
    	} else {
    		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        	result = fileChooser.showSaveDialog(null);    		
    	}
    	if (JFileChooser.APPROVE_OPTION == result) {
    		String fullName = fileChooser.getSelectedFile().getAbsolutePath();
    		if (null == fullName)
    			return null;
     		String plainName = fileChooser.getSelectedFile().getName();
    		int dotPosition = plainName.lastIndexOf(".");
    		if ((dotPosition < 0) && (! reading))
    			fullName +=  "." + fileType;
    		if ((! reading) && (! defaultName.equals(fullName)) && (new File(fullName).exists()))
    			if (JOptionPane.showConfirmDialog(null, String.format("Overwrite existing file \"%s\"?", fullName),
    					"Overwrite file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
    				return null;
    		return fullName;
    	}
    	return null;
    }
	
	// adapted from
	// http://www.java2s.com/Code/Java/Swing-JFC/DisabletheJFileChoosersNewfolderbutton.htm
	// Search a tree of Containers recursively for a JButton with the newFolderIcon and
	// call setEnabled(false) for that/those button(s). There should be a cleaner way...
	private static void disableNewFolderButton (Container c) {
		for (Component comp : c.getComponents()) {
			if (comp instanceof JButton) {
		        JButton b = (JButton) comp;
		        Icon icon = b.getIcon();
		        if ((null != icon) && (icon == UIManager.getIcon("FileChooser.newFolderIcon")))
		        	b.setEnabled(false);
	      	} else if (comp instanceof Container)
		        disableNewFolderButton((Container) comp);
	    }
	}

}

// adapted from http://www.java2s.com/Code/JavaAPI/javax.swing/JFileChoosersetFileFilterFileFilterfilter.htm
class ExtensionFileFilter extends FileFilter {
	String description;
	String extensions[];

	public ExtensionFileFilter(String description, String extension) {
	    this(description, new String[] { extension });
	}

	public ExtensionFileFilter(String description, String extensions[]) {
	    if (description == null)
	        this.description = extensions[0];
	    else
	        this.description = description;
	    this.extensions = extensions.clone();
	    toLower(this.extensions);
	}

	private static void toLower(String array[]) {
	    for (int i = 0, n = array.length; i < n; i++)
	      array[i] = array[i].toLowerCase();
	}

	@Override
	public String getDescription() {
	    return description;
	}

	@Override
	public boolean accept(File file) {
	    if (file.isDirectory())
	        return true;
	    String path = file.getAbsolutePath().toLowerCase();
	    for (int i = 0, n = extensions.length; i < n; i++) {
	        String extension = extensions[i];
	        if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.'))
	            return true;
	    }
	    return false;
	}
	
}