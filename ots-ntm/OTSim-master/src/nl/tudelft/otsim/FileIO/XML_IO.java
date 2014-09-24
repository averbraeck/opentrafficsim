package nl.tudelft.otsim.FileIO;

/**
 * Interface requirements for objects that can be stored in OpenTraffic XML
 * files.
 * <br />
 * Regretfully there appears to be no way to enforce that all classes providing 
 * this interface have a Constructor from a ParsedNode like. 
 * <code>public ExtendedClassName implements XMLIO (ParsedNode pn, ...) { </code>
 * <br />
 * Also, there appears to be no way to enforce that all classed providing this
 * interface have a <code> public final String XMLTAG = &lt;ClassName&gt;</code>
 * in it. This would have been very nice.
 * 
 * @author Peter Knoppers
 */
public interface XML_IO {
	/**
	 * Write this object into an XML file using the provided StaXWriter.
	 * @param staXWriter {@link StaXWriter}; writer for this object
	 * @return Boolean; true on success; false on failure
	 */
	public abstract boolean writeXML(StaXWriter staXWriter);
}