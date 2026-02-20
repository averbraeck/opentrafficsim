
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.HeadwayDistributionAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.HeadwayDistributionType;
import org.opentrafficsim.xml.bindings.types.LengthType;
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
 *       <sequence>
 *         <element name="OdOptionsItem" maxOccurs="unbounded">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <choice>
 *                     <element name="Global" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                     <element name="LinkType" type="{http://www.opentrafficsim.org/ots}string"/>
 *                     <element name="Origin" type="{http://www.opentrafficsim.org/ots}string"/>
 *                     <element name="Lane" type="{http://www.opentrafficsim.org/ots}LaneLinkType"/>
 *                   </choice>
 *                   <element name="NoLaneChange" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" minOccurs="0"/>
 *                   <element name="RoomChecker" type="{http://www.opentrafficsim.org/ots}RoomCheckerType" minOccurs="0"/>
 *                   <element name="HeadwayDist" minOccurs="0">
 *                     <simpleType>
 *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                         <simpleType>
 *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                             <enumeration value="CONSTANT"/>
 *                             <enumeration value="EXPONENTIAL"/>
 *                             <enumeration value="UNIFORM"/>
 *                             <enumeration value="TRIANGULAR"/>
 *                             <enumeration value="TRI_EXP"/>
 *                             <enumeration value="LOGNORMAL"/>
 *                           </restriction>
 *                         </simpleType>
 *                       </union>
 *                     </simpleType>
 *                   </element>
 *                   <element name="Markov" minOccurs="0">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="State" maxOccurs="unbounded">
 *                               <complexType>
 *                                 <complexContent>
 *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                                     <attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" />
 *                                     <attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                                   </restriction>
 *                                 </complexContent>
 *                               </complexType>
 *                             </element>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                   <element name="LaneBiases" minOccurs="0">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <choice maxOccurs="unbounded" minOccurs="0">
 *                             <element ref="{http://www.opentrafficsim.org/ots}LaneBias"/>
 *                             <element name="DefinedLaneBias">
 *                               <complexType>
 *                                 <complexContent>
 *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                                   </restriction>
 *                                 </complexContent>
 *                               </complexType>
 *                             </element>
 *                           </choice>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *       <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "odOptionsItem"
})
@XmlRootElement(name = "OdOptions")
@SuppressWarnings("all") public class OdOptions
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Defines options on a Global, LinkType, Origin (node) or Lane level.
     *               The most specific level is always applicable for a vehicle generator.
     * 
     */
    @XmlElement(name = "OdOptionsItem", required = true)
    protected List<OdOptions.OdOptionsItem> odOptionsItem;
    @XmlAttribute(name = "Id")
    protected String id;

    /**
     * Defines options on a Global, LinkType, Origin (node) or Lane level.
     *               The most specific level is always applicable for a vehicle generator.
     * 
     * Gets the value of the odOptionsItem property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the odOptionsItem property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getOdOptionsItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OdOptions.OdOptionsItem }
     * </p>
     * 
     * 
     * @return
     *     The value of the odOptionsItem property.
     */
    public List<OdOptions.OdOptionsItem> getOdOptionsItem() {
        if (odOptionsItem == null) {
            odOptionsItem = new ArrayList<>();
        }
        return this.odOptionsItem;
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
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <choice>
     *           <element name="Global" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *           <element name="LinkType" type="{http://www.opentrafficsim.org/ots}string"/>
     *           <element name="Origin" type="{http://www.opentrafficsim.org/ots}string"/>
     *           <element name="Lane" type="{http://www.opentrafficsim.org/ots}LaneLinkType"/>
     *         </choice>
     *         <element name="NoLaneChange" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" minOccurs="0"/>
     *         <element name="RoomChecker" type="{http://www.opentrafficsim.org/ots}RoomCheckerType" minOccurs="0"/>
     *         <element name="HeadwayDist" minOccurs="0">
     *           <simpleType>
     *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *               <simpleType>
     *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                   <enumeration value="CONSTANT"/>
     *                   <enumeration value="EXPONENTIAL"/>
     *                   <enumeration value="UNIFORM"/>
     *                   <enumeration value="TRIANGULAR"/>
     *                   <enumeration value="TRI_EXP"/>
     *                   <enumeration value="LOGNORMAL"/>
     *                 </restriction>
     *               </simpleType>
     *             </union>
     *           </simpleType>
     *         </element>
     *         <element name="Markov" minOccurs="0">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="State" maxOccurs="unbounded">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                           <attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" />
     *                           <attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                 </sequence>
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *         <element name="LaneBiases" minOccurs="0">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <choice maxOccurs="unbounded" minOccurs="0">
     *                   <element ref="{http://www.opentrafficsim.org/ots}LaneBias"/>
     *                   <element name="DefinedLaneBias">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                 </choice>
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
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
        "global",
        "linkType",
        "origin",
        "lane",
        "noLaneChange",
        "roomChecker",
        "headwayDist",
        "markov",
        "laneBiases"
    })
    public static class OdOptionsItem
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Global")
        protected EmptyType global;
        @XmlElement(name = "LinkType", type = String.class)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType linkType;
        @XmlElement(name = "Origin", type = String.class)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType origin;
        @XmlElement(name = "Lane")
        protected LaneLinkType lane;
        /**
         * Initial distance over which GTUs are not allowed to change lane,
         *                     to prevent interacting with generation on adjacent lanes.
         * 
         */
        @XmlElement(name = "NoLaneChange", type = String.class)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected LengthType noLaneChange;
        @XmlElement(name = "RoomChecker")
        protected RoomCheckerType roomChecker;
        @XmlElement(name = "HeadwayDist", type = String.class)
        @XmlJavaTypeAdapter(HeadwayDistributionAdapter.class)
        protected HeadwayDistributionType headwayDist;
        /**
         * Markov chaining based on auto-correlation of GTU types.
         * 
         */
        @XmlElement(name = "Markov")
        protected OdOptions.OdOptionsItem.Markov markov;
        /**
         * Influences the preferred lateral position of generated GTU's.
         * 
         */
        @XmlElement(name = "LaneBiases")
        protected OdOptions.OdOptionsItem.LaneBiases laneBiases;

        /**
         * Gets the value of the global property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getGlobal() {
            return global;
        }

        /**
         * Sets the value of the global property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setGlobal(EmptyType value) {
            this.global = value;
        }

        /**
         * Gets the value of the linkType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getLinkType() {
            return linkType;
        }

        /**
         * Sets the value of the linkType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLinkType(StringType value) {
            this.linkType = value;
        }

        /**
         * Gets the value of the origin property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getOrigin() {
            return origin;
        }

        /**
         * Sets the value of the origin property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOrigin(StringType value) {
            this.origin = value;
        }

        /**
         * Gets the value of the lane property.
         * 
         * @return
         *     possible object is
         *     {@link LaneLinkType }
         *     
         */
        public LaneLinkType getLane() {
            return lane;
        }

        /**
         * Sets the value of the lane property.
         * 
         * @param value
         *     allowed object is
         *     {@link LaneLinkType }
         *     
         */
        public void setLane(LaneLinkType value) {
            this.lane = value;
        }

        /**
         * Initial distance over which GTUs are not allowed to change lane,
         *                     to prevent interacting with generation on adjacent lanes.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public LengthType getNoLaneChange() {
            return noLaneChange;
        }

        /**
         * Sets the value of the noLaneChange property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getNoLaneChange()
         */
        public void setNoLaneChange(LengthType value) {
            this.noLaneChange = value;
        }

        /**
         * Gets the value of the roomChecker property.
         * 
         * @return
         *     possible object is
         *     {@link RoomCheckerType }
         *     
         */
        public RoomCheckerType getRoomChecker() {
            return roomChecker;
        }

        /**
         * Sets the value of the roomChecker property.
         * 
         * @param value
         *     allowed object is
         *     {@link RoomCheckerType }
         *     
         */
        public void setRoomChecker(RoomCheckerType value) {
            this.roomChecker = value;
        }

        /**
         * Gets the value of the headwayDist property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public HeadwayDistributionType getHeadwayDist() {
            return headwayDist;
        }

        /**
         * Sets the value of the headwayDist property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHeadwayDist(HeadwayDistributionType value) {
            this.headwayDist = value;
        }

        /**
         * Markov chaining based on auto-correlation of GTU types.
         * 
         * @return
         *     possible object is
         *     {@link OdOptions.OdOptionsItem.Markov }
         *     
         */
        public OdOptions.OdOptionsItem.Markov getMarkov() {
            return markov;
        }

        /**
         * Sets the value of the markov property.
         * 
         * @param value
         *     allowed object is
         *     {@link OdOptions.OdOptionsItem.Markov }
         *     
         * @see #getMarkov()
         */
        public void setMarkov(OdOptions.OdOptionsItem.Markov value) {
            this.markov = value;
        }

        /**
         * Influences the preferred lateral position of generated GTU's.
         * 
         * @return
         *     possible object is
         *     {@link OdOptions.OdOptionsItem.LaneBiases }
         *     
         */
        public OdOptions.OdOptionsItem.LaneBiases getLaneBiases() {
            return laneBiases;
        }

        /**
         * Sets the value of the laneBiases property.
         * 
         * @param value
         *     allowed object is
         *     {@link OdOptions.OdOptionsItem.LaneBiases }
         *     
         * @see #getLaneBiases()
         */
        public void setLaneBiases(OdOptions.OdOptionsItem.LaneBiases value) {
            this.laneBiases = value;
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
         *       <choice maxOccurs="unbounded" minOccurs="0">
         *         <element ref="{http://www.opentrafficsim.org/ots}LaneBias"/>
         *         <element name="DefinedLaneBias">
         *           <complexType>
         *             <complexContent>
         *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *               </restriction>
         *             </complexContent>
         *           </complexType>
         *         </element>
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
            "laneBias",
            "definedLaneBias"
        })
        public static class LaneBiases
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "LaneBias")
            protected List<LaneBias> laneBias;
            @XmlElement(name = "DefinedLaneBias")
            protected List<OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias> definedLaneBias;

            /**
             * Gets the value of the laneBias property.
             * 
             * <p>This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the laneBias property.</p>
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * </p>
             * <pre>
             * getLaneBias().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link LaneBias }
             * </p>
             * 
             * 
             * @return
             *     The value of the laneBias property.
             */
            public List<LaneBias> getLaneBias() {
                if (laneBias == null) {
                    laneBias = new ArrayList<>();
                }
                return this.laneBias;
            }

            /**
             * Gets the value of the definedLaneBias property.
             * 
             * <p>This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the definedLaneBias property.</p>
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * </p>
             * <pre>
             * getDefinedLaneBias().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias }
             * </p>
             * 
             * 
             * @return
             *     The value of the definedLaneBias property.
             */
            public List<OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias> getDefinedLaneBias() {
                if (definedLaneBias == null) {
                    definedLaneBias = new ArrayList<>();
                }
                return this.definedLaneBias;
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
             *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
             *     </restriction>
             *   </complexContent>
             * </complexType>
             * }</pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class DefinedLaneBias
                implements Serializable
            {

                private static final long serialVersionUID = 10102L;
                @XmlAttribute(name = "GtuType", required = true)
                @XmlJavaTypeAdapter(StringAdapter.class)
                protected StringType gtuType;

                /**
                 * Gets the value of the gtuType property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public StringType getGtuType() {
                    return gtuType;
                }

                /**
                 * Sets the value of the gtuType property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setGtuType(StringType value) {
                    this.gtuType = value;
                }

            }

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
         *       <sequence>
         *         <element name="State" maxOccurs="unbounded">
         *           <complexType>
         *             <complexContent>
         *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *                 <attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" />
         *                 <attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" />
         *               </restriction>
         *             </complexContent>
         *           </complexType>
         *         </element>
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
            "state"
        })
        public static class Markov
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            /**
             * Defines auto-correlation of a GTU type. The parent type
             *                           creates a group of correlated GTU types under a single parent.
             * 
             */
            @XmlElement(name = "State", required = true)
            protected List<OdOptions.OdOptionsItem.Markov.State> state;

            /**
             * Defines auto-correlation of a GTU type. The parent type
             *                           creates a group of correlated GTU types under a single parent.
             * 
             * Gets the value of the state property.
             * 
             * <p>This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the state property.</p>
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * </p>
             * <pre>
             * getState().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link OdOptions.OdOptionsItem.Markov.State }
             * </p>
             * 
             * 
             * @return
             *     The value of the state property.
             */
            public List<OdOptions.OdOptionsItem.Markov.State> getState() {
                if (state == null) {
                    state = new ArrayList<>();
                }
                return this.state;
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
             *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
             *       <attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" />
             *       <attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" />
             *     </restriction>
             *   </complexContent>
             * </complexType>
             * }</pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class State
                implements Serializable
            {

                private static final long serialVersionUID = 10102L;
                @XmlAttribute(name = "GtuType", required = true)
                @XmlJavaTypeAdapter(StringAdapter.class)
                protected StringType gtuType;
                @XmlAttribute(name = "Parent")
                @XmlJavaTypeAdapter(StringAdapter.class)
                protected StringType parent;
                @XmlAttribute(name = "Correlation", required = true)
                @XmlJavaTypeAdapter(DoubleAdapter.class)
                protected DoubleType correlation;

                /**
                 * Gets the value of the gtuType property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public StringType getGtuType() {
                    return gtuType;
                }

                /**
                 * Sets the value of the gtuType property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setGtuType(StringType value) {
                    this.gtuType = value;
                }

                /**
                 * Gets the value of the parent property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public StringType getParent() {
                    return parent;
                }

                /**
                 * Sets the value of the parent property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setParent(StringType value) {
                    this.parent = value;
                }

                /**
                 * Gets the value of the correlation property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public DoubleType getCorrelation() {
                    return correlation;
                }

                /**
                 * Sets the value of the correlation property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setCorrelation(DoubleType value) {
                    this.correlation = value;
                }

            }

        }

    }

}
