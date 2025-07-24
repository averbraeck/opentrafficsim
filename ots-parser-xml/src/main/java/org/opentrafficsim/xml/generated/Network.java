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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleUnitIntervalAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.LengthType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Conflicts" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *                   &lt;element name="DefaultWidth" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *                   &lt;element name="FixedWidth" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *                   &lt;element name="RelativeWidth" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.opentrafficsim.org/ots}Node" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.opentrafficsim.org/ots}Link" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.opentrafficsim.org/ots}Centroid" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.opentrafficsim.org/ots}Connector" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.w3.org/2001/XInclude}include" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
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
    "conflicts",
    "flattener",
    "node",
    "link",
    "centroid",
    "connector",
    "include"
})
@XmlRootElement(name = "Network")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class Network
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Conflicts")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Network.Conflicts conflicts;
    @XmlElement(name = "Flattener")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected FlattenerType flattener;
    @XmlElement(name = "Node")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Node> node;
    @XmlElement(name = "Link")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Link> link;
    @XmlElement(name = "Centroid")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Centroid> centroid;
    @XmlElement(name = "Connector")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Connector> connector;
    @XmlElement(namespace = "http://www.w3.org/2001/XInclude")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<IncludeType> include;

    /**
     * Ruft den Wert der conflicts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Network.Conflicts }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setFlattener(FlattenerType value) {
        this.flattener = value;
    }

    /**
     * Gets the value of the node property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Node }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Node> getNode() {
        if (node == null) {
            node = new ArrayList<Node>();
        }
        return this.node;
    }

    /**
     * Gets the value of the link property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Link }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Link> getLink() {
        if (link == null) {
            link = new ArrayList<Link>();
        }
        return this.link;
    }

    /**
     * Gets the value of the centroid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the centroid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCentroid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Centroid }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Centroid> getCentroid() {
        if (centroid == null) {
            centroid = new ArrayList<Centroid>();
        }
        return this.centroid;
    }

    /**
     * Gets the value of the connector property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Connector }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Connector> getConnector() {
        if (connector == null) {
            connector = new ArrayList<Connector>();
        }
        return this.connector;
    }

    /**
     * Gets the value of the include property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the include property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IncludeType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<IncludeType> getInclude() {
        if (include == null) {
            include = new ArrayList<IncludeType>();
        }
        return this.include;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;choice&gt;
     *         &lt;element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
     *         &lt;element name="DefaultWidth" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
     *         &lt;element name="FixedWidth" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
     *         &lt;element name="RelativeWidth" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/&gt;
     *       &lt;/choice&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Conflicts
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "None")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType none;
        @XmlElement(name = "DefaultWidth")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType defaultWidth;
        @XmlElement(name = "FixedWidth", type = String.class)
        @XmlJavaTypeAdapter(LengthAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected LengthType fixedWidth;
        @XmlElement(name = "RelativeWidth", type = String.class)
        @XmlJavaTypeAdapter(DoubleUnitIntervalAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected DoubleType relativeWidth;

        /**
         * Ruft den Wert der none-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setRelativeWidth(DoubleType value) {
            this.relativeWidth = value;
        }

    }

}
