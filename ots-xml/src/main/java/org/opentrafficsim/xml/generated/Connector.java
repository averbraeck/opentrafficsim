
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
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der centroid-Eigenschaft ab.
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
     * Legt den Wert der centroid-Eigenschaft fest.
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
     * Ruft den Wert der node-Eigenschaft ab.
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
     * Legt den Wert der node-Eigenschaft fest.
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
     * Legt den Wert der outbound-Eigenschaft fest.
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
     * Ruft den Wert der demandWeight-Eigenschaft ab.
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
     * Legt den Wert der demandWeight-Eigenschaft fest.
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
