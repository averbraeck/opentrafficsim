package nl.tudelft.otsim.GUI;

/**
 * Interface for things that can be stored in files
 * 
 * @author Peter Knoppers
 */
public interface Storable extends nl.tudelft.otsim.FileIO.XML_IO {
	/** 
	 * Description of the Storable to be shown in file dialogs.
	 * @return String; description to be shown in file dialogs
	 */
	public String description();
	
	/**
	 * List the file name extension used for this Storable .
	 * @return String[]; the list of extension used for this Storable
	 */
	public String fileType();
	
	/**
	 * Check for unsaved modifications in this Storable.
	 * @return Boolean; true if there are unsaved modification; false if there
	 * are no modifications, or all modifications have already been written
	 */
	public boolean isModified();
	
	/**
	 * Set the modified flag of this Storable.
	 */
	public void setModified();

	/**
	 * Clear the modified flag. This method shall be called after this
	 * Storable was successfully written to a file.
	 */
	public void clearModified();
	
	/**
	 * Retrieve the name of the file that this Storable was read from.
	 * @return String; the name of the file that this Storable was read from,
	 * or null if this storable was created new
	 */
	public String storageName();
	
	/**
	 * Set the name of the file that this Storable was read from.
	 * @param name
	 */
	public void setStorageName(String name);
}