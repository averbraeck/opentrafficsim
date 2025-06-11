
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für ResponsiveControlType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="ResponsiveControlType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ControlType">
 *       <sequence>
 *         <element name="Detector" maxOccurs="unbounded">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="MultipleLane">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="EntryLink" type="{http://www.opentrafficsim.org/ots}string"/>
 *                             <element name="EntryLane" type="{http://www.opentrafficsim.org/ots}string"/>
 *                             <element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
 *                             <element name="IntermediateLanes" type="{http://www.opentrafficsim.org/ots}LaneLinkType" maxOccurs="unbounded" minOccurs="0"/>
 *                             <element name="ExitLink" type="{http://www.opentrafficsim.org/ots}string"/>
 *                             <element name="ExitLane" type="{http://www.opentrafficsim.org/ots}string"/>
 *                             <element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                   <element name="SingleLane">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="Link" type="{http://www.opentrafficsim.org/ots}string"/>
 *                             <element name="Lane" type="{http://www.opentrafficsim.org/ots}string"/>
 *                             <element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
 *                             <element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </choice>
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *                 <attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponsiveControlType", propOrder = {
    "detector"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.Control.TrafCod.class
})
@SuppressWarnings("all") public class ResponsiveControlType
    extends ControlType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Detector", required = true)
    protected List<ResponsiveControlType.Detector> detector;

    /**
     * Gets the value of the detector property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the detector property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getDetector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResponsiveControlType.Detector }
     * </p>
     * 
     * 
     * @return
     *     The value of the detector property.
     */
    public List<ResponsiveControlType.Detector> getDetector() {
        if (detector == null) {
            detector = new ArrayList<>();
        }
        return this.detector;
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
     *         <element name="MultipleLane">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="EntryLink" type="{http://www.opentrafficsim.org/ots}string"/>
     *                   <element name="EntryLane" type="{http://www.opentrafficsim.org/ots}string"/>
     *                   <element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
     *                   <element name="IntermediateLanes" type="{http://www.opentrafficsim.org/ots}LaneLinkType" maxOccurs="unbounded" minOccurs="0"/>
     *                   <element name="ExitLink" type="{http://www.opentrafficsim.org/ots}string"/>
     *                   <element name="ExitLane" type="{http://www.opentrafficsim.org/ots}string"/>
     *                   <element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
     *                 </sequence>
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *         <element name="SingleLane">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="Link" type="{http://www.opentrafficsim.org/ots}string"/>
     *                   <element name="Lane" type="{http://www.opentrafficsim.org/ots}string"/>
     *                   <element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
     *                   <element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
     *                 </sequence>
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *       </choice>
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
     *       <attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "multipleLane",
        "singleLane"
    })
    public static class Detector
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "MultipleLane")
        protected ResponsiveControlType.Detector.MultipleLane multipleLane;
        @XmlElement(name = "SingleLane")
        protected ResponsiveControlType.Detector.SingleLane singleLane;
        @XmlAttribute(name = "Id", required = true)
        protected String id;
        @XmlAttribute(name = "Type", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType type;

        /**
         * Ruft den Wert der multipleLane-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ResponsiveControlType.Detector.MultipleLane }
         *     
         */
        public ResponsiveControlType.Detector.MultipleLane getMultipleLane() {
            return multipleLane;
        }

        /**
         * Legt den Wert der multipleLane-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ResponsiveControlType.Detector.MultipleLane }
         *     
         */
        public void setMultipleLane(ResponsiveControlType.Detector.MultipleLane value) {
            this.multipleLane = value;
        }

        /**
         * Ruft den Wert der singleLane-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ResponsiveControlType.Detector.SingleLane }
         *     
         */
        public ResponsiveControlType.Detector.SingleLane getSingleLane() {
            return singleLane;
        }

        /**
         * Legt den Wert der singleLane-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ResponsiveControlType.Detector.SingleLane }
         *     
         */
        public void setSingleLane(ResponsiveControlType.Detector.SingleLane value) {
            this.singleLane = value;
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
         * <p>Java-Klasse für anonymous complex type.</p>
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
         * 
         * <pre>{@code
         * <complexType>
         *   <complexContent>
         *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       <sequence>
         *         <element name="EntryLink" type="{http://www.opentrafficsim.org/ots}string"/>
         *         <element name="EntryLane" type="{http://www.opentrafficsim.org/ots}string"/>
         *         <element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
         *         <element name="IntermediateLanes" type="{http://www.opentrafficsim.org/ots}LaneLinkType" maxOccurs="unbounded" minOccurs="0"/>
         *         <element name="ExitLink" type="{http://www.opentrafficsim.org/ots}string"/>
         *         <element name="ExitLane" type="{http://www.opentrafficsim.org/ots}string"/>
         *         <element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
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
            "entryLink",
            "entryLane",
            "entryPosition",
            "intermediateLanes",
            "exitLink",
            "exitLane",
            "exitPosition"
        })
        public static class MultipleLane
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "EntryLink", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType entryLink;
            @XmlElement(name = "EntryLane", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType entryLane;
            @XmlElement(name = "EntryPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            protected LengthBeginEndType entryPosition;
            @XmlElement(name = "IntermediateLanes")
            protected List<LaneLinkType> intermediateLanes;
            @XmlElement(name = "ExitLink", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType exitLink;
            @XmlElement(name = "ExitLane", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType exitLane;
            @XmlElement(name = "ExitPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            protected LengthBeginEndType exitPosition;

            /**
             * Ruft den Wert der entryLink-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getEntryLink() {
                return entryLink;
            }

            /**
             * Legt den Wert der entryLink-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntryLink(StringType value) {
                this.entryLink = value;
            }

            /**
             * Ruft den Wert der entryLane-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getEntryLane() {
                return entryLane;
            }

            /**
             * Legt den Wert der entryLane-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntryLane(StringType value) {
                this.entryLane = value;
            }

            /**
             * Ruft den Wert der entryPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthBeginEndType getEntryPosition() {
                return entryPosition;
            }

            /**
             * Legt den Wert der entryPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntryPosition(LengthBeginEndType value) {
                this.entryPosition = value;
            }

            /**
             * Gets the value of the intermediateLanes property.
             * 
             * <p>This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the intermediateLanes property.</p>
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * </p>
             * <pre>
             * getIntermediateLanes().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link LaneLinkType }
             * </p>
             * 
             * 
             * @return
             *     The value of the intermediateLanes property.
             */
            public List<LaneLinkType> getIntermediateLanes() {
                if (intermediateLanes == null) {
                    intermediateLanes = new ArrayList<>();
                }
                return this.intermediateLanes;
            }

            /**
             * Ruft den Wert der exitLink-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getExitLink() {
                return exitLink;
            }

            /**
             * Legt den Wert der exitLink-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setExitLink(StringType value) {
                this.exitLink = value;
            }

            /**
             * Ruft den Wert der exitLane-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getExitLane() {
                return exitLane;
            }

            /**
             * Legt den Wert der exitLane-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setExitLane(StringType value) {
                this.exitLane = value;
            }

            /**
             * Ruft den Wert der exitPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthBeginEndType getExitPosition() {
                return exitPosition;
            }

            /**
             * Legt den Wert der exitPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setExitPosition(LengthBeginEndType value) {
                this.exitPosition = value;
            }

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
         *       <sequence>
         *         <element name="Link" type="{http://www.opentrafficsim.org/ots}string"/>
         *         <element name="Lane" type="{http://www.opentrafficsim.org/ots}string"/>
         *         <element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
         *         <element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/>
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
            "link",
            "lane",
            "entryPosition",
            "exitPosition"
        })
        public static class SingleLane
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "Link", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType link;
            @XmlElement(name = "Lane", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType lane;
            @XmlElement(name = "EntryPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            protected LengthBeginEndType entryPosition;
            @XmlElement(name = "ExitPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            protected LengthBeginEndType exitPosition;

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
             * Ruft den Wert der entryPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthBeginEndType getEntryPosition() {
                return entryPosition;
            }

            /**
             * Legt den Wert der entryPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntryPosition(LengthBeginEndType value) {
                this.entryPosition = value;
            }

            /**
             * Ruft den Wert der exitPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthBeginEndType getExitPosition() {
                return exitPosition;
            }

            /**
             * Legt den Wert der exitPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setExitPosition(LengthBeginEndType value) {
                this.exitPosition = value;
            }

        }

    }

}
