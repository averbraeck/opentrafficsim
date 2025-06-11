
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
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.EncodingAdapter;
import org.opentrafficsim.xml.bindings.GraphicsTypeAdapter;
import org.opentrafficsim.xml.bindings.SpaceAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.EncodingType;
import org.opentrafficsim.xml.bindings.types.GraphicsTypeType;


/**
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice maxOccurs="unbounded">
 *         <element name="FixedTime" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <extension base="{http://www.opentrafficsim.org/ots}ControlType">
 *                 <sequence>
 *                   <element name="Cycle" maxOccurs="unbounded">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <attribute name="SignalGroupId" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
 *                           <attribute name="PreGreen" type="{http://www.opentrafficsim.org/ots}DurationType" />
 *                           <attribute name="Green" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
 *                           <attribute name="Yellow" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *                 <attribute name="CycleTime" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
 *                 <attribute name="Offset" type="{http://www.opentrafficsim.org/ots}DurationType" default="0.0 s" />
 *               </extension>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="TrafCod" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <extension base="{http://www.opentrafficsim.org/ots}ResponsiveControlType">
 *                 <sequence>
 *                   <choice>
 *                     <element name="Program">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
 *                             <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="ProgramFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
 *                   </choice>
 *                   <element name="Console">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <choice>
 *                               <element name="Map">
 *                                 <complexType>
 *                                   <simpleContent>
 *                                     <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
 *                                       <attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" />
 *                                       <attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" />
 *                                       <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
 *                                     </extension>
 *                                   </simpleContent>
 *                                 </complexType>
 *                               </element>
 *                               <element name="MapFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
 *                             </choice>
 *                             <choice>
 *                               <element name="Coordinates" type="{http://www.opentrafficsim.org/ots}TrafCodCoordinatesType"/>
 *                               <element name="CoordinatesFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
 *                             </choice>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *               </extension>
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
    "fixedTime",
    "trafCod"
})
@XmlRootElement(name = "Control")
@SuppressWarnings("all") public class Control
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "FixedTime")
    protected List<Control.FixedTime> fixedTime;
    @XmlElement(name = "TrafCod")
    protected List<Control.TrafCod> trafCod;

    /**
     * Gets the value of the fixedTime property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixedTime property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getFixedTime().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Control.FixedTime }
     * </p>
     * 
     * 
     * @return
     *     The value of the fixedTime property.
     */
    public List<Control.FixedTime> getFixedTime() {
        if (fixedTime == null) {
            fixedTime = new ArrayList<>();
        }
        return this.fixedTime;
    }

    /**
     * Gets the value of the trafCod property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trafCod property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTrafCod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Control.TrafCod }
     * </p>
     * 
     * 
     * @return
     *     The value of the trafCod property.
     */
    public List<Control.TrafCod> getTrafCod() {
        if (trafCod == null) {
            trafCod = new ArrayList<>();
        }
        return this.trafCod;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <extension base="{http://www.opentrafficsim.org/ots}ControlType">
     *       <sequence>
     *         <element name="Cycle" maxOccurs="unbounded">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <attribute name="SignalGroupId" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
     *                 <attribute name="PreGreen" type="{http://www.opentrafficsim.org/ots}DurationType" />
     *                 <attribute name="Green" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
     *                 <attribute name="Yellow" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *       </sequence>
     *       <attribute name="CycleTime" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
     *       <attribute name="Offset" type="{http://www.opentrafficsim.org/ots}DurationType" default="0.0 s" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cycle"
    })
    public static class FixedTime
        extends ControlType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Cycle", required = true)
        protected List<Control.FixedTime.Cycle> cycle;
        @XmlAttribute(name = "CycleTime", required = true)
        @XmlJavaTypeAdapter(DurationAdapter.class)
        protected DurationType cycleTime;
        @XmlAttribute(name = "Offset")
        @XmlJavaTypeAdapter(DurationAdapter.class)
        protected DurationType offset;

        /**
         * Gets the value of the cycle property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cycle property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getCycle().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Control.FixedTime.Cycle }
         * </p>
         * 
         * 
         * @return
         *     The value of the cycle property.
         */
        public List<Control.FixedTime.Cycle> getCycle() {
            if (cycle == null) {
                cycle = new ArrayList<>();
            }
            return this.cycle;
        }

        /**
         * Ruft den Wert der cycleTime-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public DurationType getCycleTime() {
            return cycleTime;
        }

        /**
         * Legt den Wert der cycleTime-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCycleTime(DurationType value) {
            this.cycleTime = value;
        }

        /**
         * Ruft den Wert der offset-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public DurationType getOffset() {
            if (offset == null) {
                return new DurationAdapter().unmarshal("0.0 s");
            } else {
                return offset;
            }
        }

        /**
         * Legt den Wert der offset-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOffset(DurationType value) {
            this.offset = value;
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
         *       <attribute name="SignalGroupId" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *       <attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
         *       <attribute name="PreGreen" type="{http://www.opentrafficsim.org/ots}DurationType" />
         *       <attribute name="Green" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
         *       <attribute name="Yellow" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" />
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Cycle
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "SignalGroupId", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected org.opentrafficsim.xml.bindings.types.StringType signalGroupId;
            @XmlAttribute(name = "Offset", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            protected DurationType offset;
            @XmlAttribute(name = "PreGreen")
            @XmlJavaTypeAdapter(DurationAdapter.class)
            protected DurationType preGreen;
            @XmlAttribute(name = "Green", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            protected DurationType green;
            @XmlAttribute(name = "Yellow", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            protected DurationType yellow;

            /**
             * Ruft den Wert der signalGroupId-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public org.opentrafficsim.xml.bindings.types.StringType getSignalGroupId() {
                return signalGroupId;
            }

            /**
             * Legt den Wert der signalGroupId-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSignalGroupId(org.opentrafficsim.xml.bindings.types.StringType value) {
                this.signalGroupId = value;
            }

            /**
             * Ruft den Wert der offset-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public DurationType getOffset() {
                return offset;
            }

            /**
             * Legt den Wert der offset-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOffset(DurationType value) {
                this.offset = value;
            }

            /**
             * Ruft den Wert der preGreen-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public DurationType getPreGreen() {
                return preGreen;
            }

            /**
             * Legt den Wert der preGreen-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPreGreen(DurationType value) {
                this.preGreen = value;
            }

            /**
             * Ruft den Wert der green-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public DurationType getGreen() {
                return green;
            }

            /**
             * Legt den Wert der green-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setGreen(DurationType value) {
                this.green = value;
            }

            /**
             * Ruft den Wert der yellow-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public DurationType getYellow() {
                return yellow;
            }

            /**
             * Legt den Wert der yellow-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setYellow(DurationType value) {
                this.yellow = value;
            }

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
     *     <extension base="{http://www.opentrafficsim.org/ots}ResponsiveControlType">
     *       <sequence>
     *         <choice>
     *           <element name="Program">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
     *                   <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="ProgramFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
     *         </choice>
     *         <element name="Console">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <choice>
     *                     <element name="Map">
     *                       <complexType>
     *                         <simpleContent>
     *                           <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
     *                             <attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" />
     *                             <attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" />
     *                             <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
     *                           </extension>
     *                         </simpleContent>
     *                       </complexType>
     *                     </element>
     *                     <element name="MapFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
     *                   </choice>
     *                   <choice>
     *                     <element name="Coordinates" type="{http://www.opentrafficsim.org/ots}TrafCodCoordinatesType"/>
     *                     <element name="CoordinatesFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
     *                   </choice>
     *                 </sequence>
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
    @XmlType(name = "", propOrder = {
        "program",
        "programFile",
        "console"
    })
    public static class TrafCod
        extends ResponsiveControlType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Program")
        protected Control.TrafCod.Program program;
        @XmlElement(name = "ProgramFile")
        protected String programFile;
        @XmlElement(name = "Console", required = true)
        protected Control.TrafCod.Console console;

        /**
         * Ruft den Wert der program-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Control.TrafCod.Program }
         *     
         */
        public Control.TrafCod.Program getProgram() {
            return program;
        }

        /**
         * Legt den Wert der program-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Control.TrafCod.Program }
         *     
         */
        public void setProgram(Control.TrafCod.Program value) {
            this.program = value;
        }

        /**
         * Ruft den Wert der programFile-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProgramFile() {
            return programFile;
        }

        /**
         * Legt den Wert der programFile-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProgramFile(String value) {
            this.programFile = value;
        }

        /**
         * Ruft den Wert der console-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Control.TrafCod.Console }
         *     
         */
        public Control.TrafCod.Console getConsole() {
            return console;
        }

        /**
         * Legt den Wert der console-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Control.TrafCod.Console }
         *     
         */
        public void setConsole(Control.TrafCod.Console value) {
            this.console = value;
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
         *         <choice>
         *           <element name="Map">
         *             <complexType>
         *               <simpleContent>
         *                 <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
         *                   <attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" />
         *                   <attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" />
         *                   <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
         *                 </extension>
         *               </simpleContent>
         *             </complexType>
         *           </element>
         *           <element name="MapFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
         *         </choice>
         *         <choice>
         *           <element name="Coordinates" type="{http://www.opentrafficsim.org/ots}TrafCodCoordinatesType"/>
         *           <element name="CoordinatesFile" type="{http://www.opentrafficsim.org/ots}anyURI"/>
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
            "map",
            "mapFile",
            "coordinates",
            "coordinatesFile"
        })
        public static class Console
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "Map")
            protected Control.TrafCod.Console.Map map;
            @XmlElement(name = "MapFile")
            protected String mapFile;
            @XmlElement(name = "Coordinates")
            protected TrafCodCoordinatesType coordinates;
            @XmlElement(name = "CoordinatesFile")
            protected String coordinatesFile;

            /**
             * Ruft den Wert der map-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Control.TrafCod.Console.Map }
             *     
             */
            public Control.TrafCod.Console.Map getMap() {
                return map;
            }

            /**
             * Legt den Wert der map-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Control.TrafCod.Console.Map }
             *     
             */
            public void setMap(Control.TrafCod.Console.Map value) {
                this.map = value;
            }

            /**
             * Ruft den Wert der mapFile-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMapFile() {
                return mapFile;
            }

            /**
             * Legt den Wert der mapFile-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMapFile(String value) {
                this.mapFile = value;
            }

            /**
             * Ruft den Wert der coordinates-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TrafCodCoordinatesType }
             *     
             */
            public TrafCodCoordinatesType getCoordinates() {
                return coordinates;
            }

            /**
             * Legt den Wert der coordinates-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TrafCodCoordinatesType }
             *     
             */
            public void setCoordinates(TrafCodCoordinatesType value) {
                this.coordinates = value;
            }

            /**
             * Ruft den Wert der coordinatesFile-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCoordinatesFile() {
                return coordinatesFile;
            }

            /**
             * Legt den Wert der coordinatesFile-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCoordinatesFile(String value) {
                this.coordinatesFile = value;
            }


            /**
             * <p>Java-Klasse für anonymous complex type.</p>
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
             * 
             * <pre>{@code
             * <complexType>
             *   <simpleContent>
             *     <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
             *       <attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" />
             *       <attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" />
             *       <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
             *     </extension>
             *   </simpleContent>
             * </complexType>
             * }</pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value"
            })
            public static class Map
                implements Serializable
            {

                private static final long serialVersionUID = 10102L;
                @XmlValue
                @XmlJavaTypeAdapter(StringAdapter.class)
                protected org.opentrafficsim.xml.bindings.types.StringType value;
                @XmlAttribute(name = "Type")
                @XmlJavaTypeAdapter(GraphicsTypeAdapter.class)
                protected GraphicsTypeType type;
                @XmlAttribute(name = "Encoding")
                @XmlJavaTypeAdapter(EncodingAdapter.class)
                protected EncodingType encoding;
                @XmlAttribute(name = "Space")
                @XmlJavaTypeAdapter(SpaceAdapter.class)
                protected org.opentrafficsim.xml.bindings.types.StringType space;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public org.opentrafficsim.xml.bindings.types.StringType getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValue(org.opentrafficsim.xml.bindings.types.StringType value) {
                    this.value = value;
                }

                /**
                 * Ruft den Wert der type-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public GraphicsTypeType getType() {
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
                public void setType(GraphicsTypeType value) {
                    this.type = value;
                }

                /**
                 * Ruft den Wert der encoding-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public EncodingType getEncoding() {
                    return encoding;
                }

                /**
                 * Legt den Wert der encoding-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setEncoding(EncodingType value) {
                    this.encoding = value;
                }

                /**
                 * Ruft den Wert der space-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public org.opentrafficsim.xml.bindings.types.StringType getSpace() {
                    if (space == null) {
                        return new SpaceAdapter().unmarshal("preserve");
                    } else {
                        return space;
                    }
                }

                /**
                 * Legt den Wert der space-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setSpace(org.opentrafficsim.xml.bindings.types.StringType value) {
                    this.space = value;
                }

            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.</p>
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
         * 
         * <pre>{@code
         * <complexType>
         *   <simpleContent>
         *     <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
         *       <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
         *     </extension>
         *   </simpleContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class Program
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected org.opentrafficsim.xml.bindings.types.StringType value;
            @XmlAttribute(name = "Space")
            @XmlJavaTypeAdapter(SpaceAdapter.class)
            protected org.opentrafficsim.xml.bindings.types.StringType space;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public org.opentrafficsim.xml.bindings.types.StringType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(org.opentrafficsim.xml.bindings.types.StringType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der space-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public org.opentrafficsim.xml.bindings.types.StringType getSpace() {
                if (space == null) {
                    return new SpaceAdapter().unmarshal("preserve");
                } else {
                    return space;
                }
            }

            /**
             * Legt den Wert der space-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSpace(org.opentrafficsim.xml.bindings.types.StringType value) {
                this.space = value;
            }

        }

    }

}
