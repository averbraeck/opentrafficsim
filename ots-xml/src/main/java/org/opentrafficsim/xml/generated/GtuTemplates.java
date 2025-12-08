
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}GtuTemplate" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gtuTemplate"
})
@XmlRootElement(name = "GtuTemplates")
@SuppressWarnings("all") public class GtuTemplates implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "GtuTemplate")
    protected List<GtuTemplate> gtuTemplate;

    /**
     * Gets the value of the gtuTemplate property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gtuTemplate property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGtuTemplate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GtuTemplate }
     * </p>
     * 
     * 
     * @return
     *     The value of the gtuTemplate property.
     */
    public List<GtuTemplate> getGtuTemplate() {
        if (gtuTemplate == null) {
            gtuTemplate = new ArrayList<>();
        }
        return this.gtuTemplate;
    }

}
