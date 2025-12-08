
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for ResponsiveControlType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
         * Gets the value of the multipleLane property.
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
         * Sets the value of the multipleLane property.
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
         * Gets the value of the singleLane property.
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
         * Sets the value of the singleLane property.
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
         * <p>Java class for anonymous complex type</p>.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
             * Gets the value of the entryLink property.
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
             * Sets the value of the entryLink property.
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
             * Gets the value of the entryLane property.
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
             * Sets the value of the entryLane property.
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
             * Gets the value of the entryPosition property.
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
             * Sets the value of the entryPosition property.
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
             * Gets the value of the exitLink property.
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
             * Sets the value of the exitLink property.
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
             * Gets the value of the exitLane property.
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
             * Sets the value of the exitLane property.
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
             * Gets the value of the exitPosition property.
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
             * Sets the value of the exitPosition property.
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
         * <p>Java class for anonymous complex type</p>.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
             * Gets the value of the entryPosition property.
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
             * Sets the value of the entryPosition property.
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
             * Gets the value of the exitPosition property.
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
             * Sets the value of the exitPosition property.
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
