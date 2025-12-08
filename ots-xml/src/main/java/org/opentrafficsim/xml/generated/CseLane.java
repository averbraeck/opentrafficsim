
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CseLane complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="CseLane">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}CrossSectionElement">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="LaneType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CseLane", propOrder = {
    "speedLimit"
})
@SuppressWarnings("all") public class CseLane
    extends CrossSectionElement
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "SpeedLimit")
    protected List<SpeedLimit> speedLimit;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "LaneType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType laneType;

    /**
     * Gets the value of the speedLimit property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the speedLimit property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSpeedLimit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpeedLimit }
     * </p>
     * 
     * 
     * @return
     *     The value of the speedLimit property.
     */
    public List<SpeedLimit> getSpeedLimit() {
        if (speedLimit == null) {
            speedLimit = new ArrayList<>();
        }
        return this.speedLimit;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the laneType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLaneType() {
        return laneType;
    }

    /**
     * Sets the value of the laneType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaneType(StringType value) {
        this.laneType = value;
    }

}
