
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.EncodingAdapter;
import org.opentrafficsim.xml.bindings.GraphicsTypeAdapter;
import org.opentrafficsim.xml.bindings.SpaceAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.EncodingType;
import org.opentrafficsim.xml.bindings.types.GraphicsTypeType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
         * Gets the value of the cycleTime property.
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
         * Sets the value of the cycleTime property.
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
         * Gets the value of the offset property.
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
         * Sets the value of the offset property.
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
         * <p>Java class for anonymous complex type</p>.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
             * Gets the value of the signalGroupId property.
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
             * Sets the value of the signalGroupId property.
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
             * Gets the value of the offset property.
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
             * Sets the value of the offset property.
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
             * Gets the value of the preGreen property.
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
             * Sets the value of the preGreen property.
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
             * Gets the value of the green property.
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
             * Sets the value of the green property.
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
             * Gets the value of the yellow property.
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
             * Sets the value of the yellow property.
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
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
         * Gets the value of the program property.
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
         * Sets the value of the program property.
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
         * Gets the value of the programFile property.
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
         * Sets the value of the programFile property.
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
         * Gets the value of the console property.
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
         * Sets the value of the console property.
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
             * Gets the value of the map property.
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
             * Sets the value of the map property.
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
             * Gets the value of the mapFile property.
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
             * Sets the value of the mapFile property.
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
             * Gets the value of the coordinates property.
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
             * Sets the value of the coordinates property.
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
             * Gets the value of the coordinatesFile property.
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
             * Sets the value of the coordinatesFile property.
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
             * <p>Java class for anonymous complex type</p>.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
                 * Gets the value of the value property.
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
                 * Sets the value of the value property.
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
                 * Gets the value of the type property.
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
                 * Sets the value of the type property.
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
                 * Gets the value of the encoding property.
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
                 * Sets the value of the encoding property.
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
                 * Gets the value of the space property.
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
                 * Sets the value of the space property.
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
         * <p>Java class for anonymous complex type</p>.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
             * Gets the value of the value property.
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
             * Sets the value of the value property.
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
             * Gets the value of the space property.
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
             * Sets the value of the space property.
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
