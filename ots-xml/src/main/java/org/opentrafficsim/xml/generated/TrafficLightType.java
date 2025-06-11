
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für TrafficLightType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="TrafficLightType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="TurnOnRed" type="{http://www.opentrafficsim.org/ots}string" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Position" use="required" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrafficLightType", propOrder = {
    "turnOnRed"
})
@SuppressWarnings("all") public class TrafficLightType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "TurnOnRed", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected List<StringType> turnOnRed;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "Lane", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType lane;
    @XmlAttribute(name = "Position", required = true)
    @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
    protected LengthBeginEndType position;

    /**
     * Gets the value of the turnOnRed property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the turnOnRed property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTurnOnRed().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * </p>
     * 
     * 
     * @return
     *     The value of the turnOnRed property.
     */
    public List<StringType> getTurnOnRed() {
        if (turnOnRed == null) {
            turnOnRed = new ArrayList<>();
        }
        return this.turnOnRed;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
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
     * Legt den Wert der id-Eigenschaft fest.
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
     * Ruft den Wert der lane-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLane() {
        return lane;
    }

    /**
     * Legt den Wert der lane-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLane(StringType value) {
        this.lane = value;
    }

    /**
     * Ruft den Wert der position-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthBeginEndType getPosition() {
        return position;
    }

    /**
     * Legt den Wert der position-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPosition(LengthBeginEndType value) {
        this.position = value;
    }

}
