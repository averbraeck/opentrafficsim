
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
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Sets the value of the conflicts property.
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
     * Gets the value of the flattener property.
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
     * Sets the value of the flattener property.
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
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
         * Gets the value of the none property.
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
         * Sets the value of the none property.
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
         * Gets the value of the defaultWidth property.
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
         * Sets the value of the defaultWidth property.
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
         * Gets the value of the fixedWidth property.
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
         * Sets the value of the fixedWidth property.
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
         * Gets the value of the relativeWidth property.
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
         * Sets the value of the relativeWidth property.
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
