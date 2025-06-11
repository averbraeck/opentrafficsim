
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleUnitIntervalAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.LengthType;


/**
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Conflicts" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                   <element name="DefaultWidth" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                   <element name="FixedWidth" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                   <element name="RelativeWidth" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/>
 *                 </choice>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/>
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element ref="{http://www.opentrafficsim.org/ots}Node" minOccurs="0"/>
 *           <element ref="{http://www.opentrafficsim.org/ots}Link" minOccurs="0"/>
 *           <element ref="{http://www.opentrafficsim.org/ots}Centroid" minOccurs="0"/>
 *           <element ref="{http://www.opentrafficsim.org/ots}Connector" minOccurs="0"/>
 *           <element ref="{http://www.w3.org/2001/XInclude}include" minOccurs="0"/>
 *         </choice>
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
    "conflicts",
    "flattener",
    "node",
    "link",
    "centroid",
    "connector",
    "include"
})
@XmlRootElement(name = "Network")
@SuppressWarnings("all") public class Network
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Default width is 2m. None creates no conflicts. Relative width is
     *               relative to lane width. Otherwise specify width.
     * 
     */
    @XmlElement(name = "Conflicts")
    protected Network.Conflicts conflicts;
    @XmlElement(name = "Flattener")
    protected FlattenerType flattener;
    @XmlElement(name = "Node")
    protected List<Node> node;
    @XmlElement(name = "Link")
    protected List<Link> link;
    @XmlElement(name = "Centroid")
    protected List<Centroid> centroid;
    @XmlElement(name = "Connector")
    protected List<Connector> connector;
    @XmlElement(namespace = "http://www.w3.org/2001/XInclude")
    protected List<IncludeType> include;

    /**
     * Default width is 2m. None creates no conflicts. Relative width is
     *               relative to lane width. Otherwise specify width.
     * 
     * @return
     *     possible object is
     *     {@link Network.Conflicts }
     *     
     */
    public Network.Conflicts getConflicts() {
        return conflicts;
    }

    /**
     * Legt den Wert der conflicts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Network.Conflicts }
     *     
     * @see #getConflicts()
     */
    public void setConflicts(Network.Conflicts value) {
        this.conflicts = value;
    }

    /**
     * Ruft den Wert der flattener-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FlattenerType }
     *     
     */
    public FlattenerType getFlattener() {
        return flattener;
    }

    /**
     * Legt den Wert der flattener-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FlattenerType }
     *     
     */
    public void setFlattener(FlattenerType value) {
        this.flattener = value;
    }

    /**
     * Gets the value of the node property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Node }
     * </p>
     * 
     * 
     * @return
     *     The value of the node property.
     */
    public List<Node> getNode() {
        if (node == null) {
            node = new ArrayList<>();
        }
        return this.node;
    }

    /**
     * Gets the value of the link property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Link }
     * </p>
     * 
     * 
     * @return
     *     The value of the link property.
     */
    public List<Link> getLink() {
        if (link == null) {
            link = new ArrayList<>();
        }
        return this.link;
    }

    /**
     * Gets the value of the centroid property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the centroid property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCentroid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Centroid }
     * </p>
     * 
     * 
     * @return
     *     The value of the centroid property.
     */
    public List<Centroid> getCentroid() {
        if (centroid == null) {
            centroid = new ArrayList<>();
        }
        return this.centroid;
    }

    /**
     * Gets the value of the connector property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getConnector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Connector }
     * </p>
     * 
     * 
     * @return
     *     The value of the connector property.
     */
    public List<Connector> getConnector() {
        if (connector == null) {
            connector = new ArrayList<>();
        }
        return this.connector;
    }

    /**
     * Gets the value of the include property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the include property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getInclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IncludeType }
     * </p>
     * 
     * 
     * @return
     *     The value of the include property.
     */
    public List<IncludeType> getInclude() {
        if (include == null) {
            include = new ArrayList<>();
        }
        return this.include;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <choice>
     *         <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *         <element name="DefaultWidth" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *         <element name="FixedWidth" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *         <element name="RelativeWidth" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/>
     *       </choice>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "none",
        "defaultWidth",
        "fixedWidth",
        "relativeWidth"
    })
    public static class Conflicts
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "None")
        protected EmptyType none;
        @XmlElement(name = "DefaultWidth")
        protected EmptyType defaultWidth;
        @XmlElement(name = "FixedWidth", type = String.class)
        @XmlJavaTypeAdapter(LengthAdapter.class)
        protected LengthType fixedWidth;
        @XmlElement(name = "RelativeWidth", type = String.class)
        @XmlJavaTypeAdapter(DoubleUnitIntervalAdapter.class)
        protected DoubleType relativeWidth;

        /**
         * Ruft den Wert der none-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getNone() {
            return none;
        }

        /**
         * Legt den Wert der none-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setNone(EmptyType value) {
            this.none = value;
        }

        /**
         * Ruft den Wert der defaultWidth-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getDefaultWidth() {
            return defaultWidth;
        }

        /**
         * Legt den Wert der defaultWidth-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setDefaultWidth(EmptyType value) {
            this.defaultWidth = value;
        }

        /**
         * Ruft den Wert der fixedWidth-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public LengthType getFixedWidth() {
            return fixedWidth;
        }

        /**
         * Legt den Wert der fixedWidth-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFixedWidth(LengthType value) {
            this.fixedWidth = value;
        }

        /**
         * Ruft den Wert der relativeWidth-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public DoubleType getRelativeWidth() {
            return relativeWidth;
        }

        /**
         * Legt den Wert der relativeWidth-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRelativeWidth(DoubleType value) {
            this.relativeWidth = value;
        }

    }

}
