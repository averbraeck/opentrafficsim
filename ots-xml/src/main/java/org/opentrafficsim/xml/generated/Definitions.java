
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
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
 *       <sequence maxOccurs="unbounded" minOccurs="0">
 *         <element ref="{http://www.w3.org/2001/XInclude}include" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}GtuTypes" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}GtuTemplates" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}LinkTypes" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}StripeTypes" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}LaneTypes" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}LaneBiases" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}RoadLayouts" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}DetectorTypes" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}ParameterTypes" maxOccurs="unbounded" minOccurs="0"/>
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
    "includeAndGtuTypesAndGtuTemplates"
})
@XmlRootElement(name = "Definitions")
@SuppressWarnings("all") public class Definitions
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "include", namespace = "http://www.w3.org/2001/XInclude", type = IncludeType.class),
        @XmlElement(name = "GtuTypes", type = GtuTypes.class),
        @XmlElement(name = "GtuTemplates", type = GtuTemplates.class),
        @XmlElement(name = "LinkTypes", type = LinkTypes.class),
        @XmlElement(name = "StripeTypes", type = StripeTypes.class),
        @XmlElement(name = "LaneTypes", type = LaneTypes.class),
        @XmlElement(name = "LaneBiases", type = LaneBiases.class),
        @XmlElement(name = "RoadLayouts", type = RoadLayouts.class),
        @XmlElement(name = "DetectorTypes", type = DetectorTypes.class),
        @XmlElement(name = "ParameterTypes", type = ParameterTypes.class)
    })
    protected List<Serializable> includeAndGtuTypesAndGtuTemplates;

    /**
     * Gets the value of the includeAndGtuTypesAndGtuTemplates property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the includeAndGtuTypesAndGtuTemplates property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getIncludeAndGtuTypesAndGtuTemplates().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DetectorTypes }
     * {@link GtuTemplates }
     * {@link GtuTypes }
     * {@link IncludeType }
     * {@link LaneBiases }
     * {@link LaneTypes }
     * {@link LinkTypes }
     * {@link ParameterTypes }
     * {@link RoadLayouts }
     * {@link StripeTypes }
     * </p>
     * 
     * 
     * @return
     *     The value of the includeAndGtuTypesAndGtuTemplates property.
     */
    public List<Serializable> getIncludeAndGtuTypesAndGtuTemplates() {
        if (includeAndGtuTypesAndGtuTemplates == null) {
            includeAndGtuTypesAndGtuTemplates = new ArrayList<>();
        }
        return this.includeAndGtuTypesAndGtuTemplates;
    }

}
