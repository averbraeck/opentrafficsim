
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Link" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Position" use="required" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType" />
 *       <attribute name="Destination" type="{http://www.opentrafficsim.org/ots}boolean" default="true" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Sink")
@SuppressWarnings("all") public class Sink
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType type;
    @XmlAttribute(name = "Link", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType link;
    @XmlAttribute(name = "Lane", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType lane;
    @XmlAttribute(name = "Position", required = true)
    @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
    protected LengthBeginEndType position;
    /**
     * When true, only GTUs with a route towards the link end node will be
     *             deleted, or possibly towards the end node of a next connector.
     * 
     */
    @XmlAttribute(name = "Destination")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType destination;

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(StringType value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der link-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLink() {
        return link;
    }

    /**
     * Legt den Wert der link-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(StringType value) {
        this.link = value;
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

    /**
     * When true, only GTUs with a route towards the link end node will be
     *             deleted, or possibly towards the end node of a next connector.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getDestination() {
        if (destination == null) {
            return new BooleanAdapter().unmarshal("true");
        } else {
            return destination;
        }
    }

    /**
     * Legt den Wert der destination-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getDestination()
     */
    public void setDestination(BooleanType value) {
        this.destination = value;
    }

}
