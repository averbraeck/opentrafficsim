
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
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Gets the value of the type property.
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
     * Sets the value of the type property.
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
     * Gets the value of the link property.
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
     * Sets the value of the link property.
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
     * Gets the value of the lane property.
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
     * Sets the value of the lane property.
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
     * Gets the value of the position property.
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
     * Sets the value of the position property.
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
     * Sets the value of the destination property.
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
