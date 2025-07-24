//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element ref="{http://www.w3.org/2001/XInclude}include" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GtuTypes" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GtuTemplates" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}LinkTypes" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}LaneTypes" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}LaneBiases" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}DetectorTypes" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}RoadLayouts" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}ParameterTypes" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "includeAndGtuTypesAndGtuTemplates"
})
@XmlRootElement(name = "Definitions")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class Definitions
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "include", namespace = "http://www.w3.org/2001/XInclude", type = IncludeType.class),
        @XmlElement(name = "GtuTypes", type = GtuTypes.class),
        @XmlElement(name = "GtuTemplates", type = GtuTemplates.class),
        @XmlElement(name = "LinkTypes", type = LinkTypes.class),
        @XmlElement(name = "LaneTypes", type = LaneTypes.class),
        @XmlElement(name = "LaneBiases", type = LaneBiases.class),
        @XmlElement(name = "DetectorTypes", type = DetectorTypes.class),
        @XmlElement(name = "RoadLayouts", type = RoadLayouts.class),
        @XmlElement(name = "ParameterTypes", type = ParameterTypes.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Serializable> includeAndGtuTypesAndGtuTemplates;

    /**
     * Gets the value of the includeAndGtuTypesAndGtuTemplates property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the includeAndGtuTypesAndGtuTemplates property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIncludeAndGtuTypesAndGtuTemplates().add(newItem);
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
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Serializable> getIncludeAndGtuTypesAndGtuTemplates() {
        if (includeAndGtuTypesAndGtuTemplates == null) {
            includeAndGtuTypesAndGtuTemplates = new ArrayList<Serializable>();
        }
        return this.includeAndGtuTypesAndGtuTemplates;
    }

}
