
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * OpenDRIVE allows including external files into the OpenDRIVE file. The processing of the files depends on the application.
 * Included data is represented by <include> elements. They may be stored at any position in OpenDRIVE.
 * 
 * <p>Java class for t_include complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_include">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *       </sequence>
 *       <attribute name="file" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_include")
@SuppressWarnings("all") public class TInclude {

    /**
     * Location of the file that is to be included
     * 
     */
    @XmlAttribute(name = "file", required = true)
    protected String file;

    /**
     * Location of the file that is to be included
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getFile()
     */
    public void setFile(String value) {
        this.file = value;
    }

}
