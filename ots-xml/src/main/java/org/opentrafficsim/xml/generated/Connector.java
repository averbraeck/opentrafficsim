
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.FractionAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.DoubleType;
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
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Centroid" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Node" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Outbound" use="required" type="{http://www.opentrafficsim.org/ots}boolean" />
 *       <attribute name="DemandWeight" type="{http://www.opentrafficsim.org/ots}FractionType" default="1.0" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Connector")
@SuppressWarnings("all") public class Connector
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType type;
    @XmlAttribute(name = "Centroid", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType centroid;
    @XmlAttribute(name = "Node", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType node;
    /**
     * Outbound Connectors go from Centroid to Node. Inbound from Node to
     *             Centroid.
     * 
     */
    @XmlAttribute(name = "Outbound", required = true)
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType outbound;
    @XmlAttribute(name = "DemandWeight")
    @XmlJavaTypeAdapter(FractionAdapter.class)
    protected DoubleType demandWeight;

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
     * Gets the value of the centroid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getCentroid() {
        return centroid;
    }

    /**
     * Sets the value of the centroid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCentroid(StringType value) {
        this.centroid = value;
    }

    /**
     * Gets the value of the node property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getNode() {
        return node;
    }

    /**
     * Sets the value of the node property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNode(StringType value) {
        this.node = value;
    }

    /**
     * Outbound Connectors go from Centroid to Node. Inbound from Node to
     *             Centroid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getOutbound() {
        return outbound;
    }

    /**
     * Sets the value of the outbound property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getOutbound()
     */
    public void setOutbound(BooleanType value) {
        this.outbound = value;
    }

    /**
     * Gets the value of the demandWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DoubleType getDemandWeight() {
        if (demandWeight == null) {
            return new FractionAdapter().unmarshal("1.0");
        } else {
            return demandWeight;
        }
    }

    /**
     * Sets the value of the demandWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDemandWeight(DoubleType value) {
        this.demandWeight = value;
    }

}
