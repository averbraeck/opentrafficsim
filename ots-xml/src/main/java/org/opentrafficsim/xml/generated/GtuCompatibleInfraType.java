
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GtuCompatibleInfraType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="GtuCompatibleInfraType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}HierarchicalType">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}Compatibility" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GtuCompatibleInfraType", propOrder = {
    "compatibility"
})
@XmlSeeAlso({
    DetectorType.class,
    LinkType.class,
    LaneType.class
})
@SuppressWarnings("all") public class GtuCompatibleInfraType
    extends HierarchicalType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Compatibility")
    protected List<Compatibility> compatibility;

    /**
     * Gets the value of the compatibility property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compatibility property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCompatibility().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Compatibility }
     * </p>
     * 
     * 
     * @return
     *     The value of the compatibility property.
     */
    public List<Compatibility> getCompatibility() {
        if (compatibility == null) {
            compatibility = new ArrayList<>();
        }
        return this.compatibility;
    }

}
