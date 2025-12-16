
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AccelerationAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.CooperationAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.FractionAdapter;
import org.opentrafficsim.xml.bindings.FrequencyAdapter;
import org.opentrafficsim.xml.bindings.GapAcceptanceAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.LongAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.SynchronizationAdapter;
import org.opentrafficsim.xml.bindings.TailgatingAdapter;
import org.opentrafficsim.xml.bindings.types.AccelerationType;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.bindings.types.CooperationType;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.FrequencyType;
import org.opentrafficsim.xml.bindings.types.GapAcceptanceType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;
import org.opentrafficsim.xml.bindings.types.LongType;
import org.opentrafficsim.xml.bindings.types.SpeedType;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.opentrafficsim.xml.bindings.types.SynchronizationType;
import org.opentrafficsim.xml.bindings.types.TailgatingType;


/**
 * <p>Java class for ModelType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ModelType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="ModelParameters" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence maxOccurs="unbounded" minOccurs="0">
 *                   <choice maxOccurs="unbounded">
 *                     <element name="Duration">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>DurationType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="DurationDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}DurationDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Length">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>LengthType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="LengthDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}LengthDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Speed">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>SpeedType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="SpeedDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}SpeedDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Acceleration">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>AccelerationType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="AccelerationDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="LinearDensity">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>LinearDensityType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="LinearDensityDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Frequency">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>FrequencyType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="FrequencyDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Double">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>double">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="DoubleDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Fraction">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>FractionType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Integer">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>integer">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="IntegerDist">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Boolean">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>boolean">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="String">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>string">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Class">
 *                       <complexType>
 *                         <simpleContent>
 *                           <extension base="<http://www.opentrafficsim.org/ots>ClassNameType">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </extension>
 *                         </simpleContent>
 *                       </complexType>
 *                     </element>
 *                   </choice>
 *                   <element name="Correlation" maxOccurs="unbounded" minOccurs="0">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/>
 *                             <element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/>
 *                           </sequence>
 *                           <attribute name="Expression">
 *                             <simpleType>
 *                               <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 <pattern value="[^{}]+"/>
 *                               </restriction>
 *                             </simpleType>
 *                           </attribute>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="StrategicalPlanner" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="Route">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <choice>
 *                             <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                             <element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                           </choice>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </choice>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="TacticalPlanner" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="Lmrs">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="CarFollowingModel" type="{http://www.opentrafficsim.org/ots}CarFollowingModelType" minOccurs="0"/>
 *                             <element name="Synchronization" minOccurs="0">
 *                               <simpleType>
 *                                 <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                                   <simpleType>
 *                                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       <enumeration value="DEADEND"/>
 *                                       <enumeration value="PASSIVE"/>
 *                                       <enumeration value="PASSIVE_MOVING"/>
 *                                       <enumeration value="ALIGN_GAP"/>
 *                                       <enumeration value="ACTIVE"/>
 *                                     </restriction>
 *                                   </simpleType>
 *                                 </union>
 *                               </simpleType>
 *                             </element>
 *                             <element name="Cooperation" minOccurs="0">
 *                               <simpleType>
 *                                 <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                                   <simpleType>
 *                                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       <enumeration value="PASSIVE"/>
 *                                       <enumeration value="PASSIVE_MOVING"/>
 *                                       <enumeration value="ACTIVE"/>
 *                                     </restriction>
 *                                   </simpleType>
 *                                 </union>
 *                               </simpleType>
 *                             </element>
 *                             <element name="GapAcceptance" minOccurs="0">
 *                               <simpleType>
 *                                 <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                                   <simpleType>
 *                                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       <enumeration value="INFORMED"/>
 *                                       <enumeration value="EGO_HEADWAY"/>
 *                                     </restriction>
 *                                   </simpleType>
 *                                 </union>
 *                               </simpleType>
 *                             </element>
 *                             <element name="Tailgating" minOccurs="0">
 *                               <simpleType>
 *                                 <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                                   <simpleType>
 *                                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       <enumeration value="NONE"/>
 *                                       <enumeration value="RHO_ONLY"/>
 *                                       <enumeration value="PRESSURE"/>
 *                                     </restriction>
 *                                   </simpleType>
 *                                 </union>
 *                               </simpleType>
 *                             </element>
 *                             <element name="MandatoryIncentives" minOccurs="0">
 *                               <complexType>
 *                                 <complexContent>
 *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     <all>
 *                                       <element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                     </all>
 *                                   </restriction>
 *                                 </complexContent>
 *                               </complexType>
 *                             </element>
 *                             <element name="VoluntaryIncentives" minOccurs="0">
 *                               <complexType>
 *                                 <complexContent>
 *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     <all>
 *                                       <element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                     </all>
 *                                   </restriction>
 *                                 </complexContent>
 *                               </complexType>
 *                             </element>
 *                             <element name="AccelerationIncentives" minOccurs="0">
 *                               <complexType>
 *                                 <complexContent>
 *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     <all>
 *                                       <element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                     </all>
 *                                   </restriction>
 *                                 </complexContent>
 *                               </complexType>
 *                             </element>
 *                             <element name="Perception" type="{http://www.opentrafficsim.org/ots}PerceptionType" minOccurs="0"/>
 *                           </sequence>
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
 *       <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="GtuType" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelType", propOrder = {
    "modelParameters",
    "strategicalPlanner",
    "tacticalPlanner"
})
@SuppressWarnings("all") public class ModelType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "ModelParameters")
    protected ModelType.ModelParameters modelParameters;
    @XmlElement(name = "StrategicalPlanner")
    protected ModelType.StrategicalPlanner strategicalPlanner;
    @XmlElement(name = "TacticalPlanner")
    protected ModelType.TacticalPlanner tacticalPlanner;
    @XmlAttribute(name = "Id")
    protected java.lang.String id;
    @XmlAttribute(name = "Parent")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType parent;
    @XmlAttribute(name = "GtuType")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;

    /**
     * Gets the value of the modelParameters property.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.ModelParameters }
     *     
     */
    public ModelType.ModelParameters getModelParameters() {
        return modelParameters;
    }

    /**
     * Sets the value of the modelParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.ModelParameters }
     *     
     */
    public void setModelParameters(ModelType.ModelParameters value) {
        this.modelParameters = value;
    }

    /**
     * Gets the value of the strategicalPlanner property.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.StrategicalPlanner }
     *     
     */
    public ModelType.StrategicalPlanner getStrategicalPlanner() {
        return strategicalPlanner;
    }

    /**
     * Sets the value of the strategicalPlanner property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.StrategicalPlanner }
     *     
     */
    public void setStrategicalPlanner(ModelType.StrategicalPlanner value) {
        this.strategicalPlanner = value;
    }

    /**
     * Gets the value of the tacticalPlanner property.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.TacticalPlanner }
     *     
     */
    public ModelType.TacticalPlanner getTacticalPlanner() {
        return tacticalPlanner;
    }

    /**
     * Sets the value of the tacticalPlanner property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.TacticalPlanner }
     *     
     */
    public void setTacticalPlanner(ModelType.TacticalPlanner value) {
        this.tacticalPlanner = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Gets the value of the parent property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
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
     *     {@link java.lang.String }
     *     
     */
    public void setParent(StringType value) {
        this.parent = value;
    }

    /**
     * Gets the value of the gtuType property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
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
     *     {@link java.lang.String }
     *     
     */
    public void setGtuType(StringType value) {
        this.gtuType = value;
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
     *       <sequence maxOccurs="unbounded" minOccurs="0">
     *         <choice maxOccurs="unbounded">
     *           <element name="Duration">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>DurationType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="DurationDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}DurationDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Length">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>LengthType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="LengthDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}LengthDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Speed">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>SpeedType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="SpeedDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}SpeedDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Acceleration">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>AccelerationType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="AccelerationDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="LinearDensity">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>LinearDensityType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="LinearDensityDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Frequency">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>FrequencyType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="FrequencyDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Double">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>double">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="DoubleDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Fraction">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>FractionType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="Integer">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>integer">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="IntegerDist">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Boolean">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>boolean">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="String">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>string">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *           <element name="Class">
     *             <complexType>
     *               <simpleContent>
     *                 <extension base="<http://www.opentrafficsim.org/ots>ClassNameType">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </extension>
     *               </simpleContent>
     *             </complexType>
     *           </element>
     *         </choice>
     *         <element name="Correlation" maxOccurs="unbounded" minOccurs="0">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/>
     *                   <element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/>
     *                 </sequence>
     *                 <attribute name="Expression">
     *                   <simpleType>
     *                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       <pattern value="[^{}]+"/>
     *                     </restriction>
     *                   </simpleType>
     *                 </attribute>
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
        "durationOrDurationDistOrLength"
    })
    public static class ModelParameters
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElements({
            @XmlElement(name = "Duration", type = ModelType.ModelParameters.Duration.class),
            @XmlElement(name = "DurationDist", type = ModelType.ModelParameters.DurationDist.class),
            @XmlElement(name = "Length", type = ModelType.ModelParameters.Length.class),
            @XmlElement(name = "LengthDist", type = ModelType.ModelParameters.LengthDist.class),
            @XmlElement(name = "Speed", type = ModelType.ModelParameters.Speed.class),
            @XmlElement(name = "SpeedDist", type = ModelType.ModelParameters.SpeedDist.class),
            @XmlElement(name = "Acceleration", type = ModelType.ModelParameters.Acceleration.class),
            @XmlElement(name = "AccelerationDist", type = ModelType.ModelParameters.AccelerationDist.class),
            @XmlElement(name = "LinearDensity", type = ModelType.ModelParameters.LinearDensity.class),
            @XmlElement(name = "LinearDensityDist", type = ModelType.ModelParameters.LinearDensityDist.class),
            @XmlElement(name = "Frequency", type = ModelType.ModelParameters.Frequency.class),
            @XmlElement(name = "FrequencyDist", type = ModelType.ModelParameters.FrequencyDist.class),
            @XmlElement(name = "Double", type = ModelType.ModelParameters.Double.class),
            @XmlElement(name = "DoubleDist", type = ModelType.ModelParameters.DoubleDist.class),
            @XmlElement(name = "Fraction", type = ModelType.ModelParameters.Fraction.class),
            @XmlElement(name = "Integer", type = ModelType.ModelParameters.Integer.class),
            @XmlElement(name = "IntegerDist", type = ModelType.ModelParameters.IntegerDist.class),
            @XmlElement(name = "Boolean", type = ModelType.ModelParameters.Boolean.class),
            @XmlElement(name = "String", type = ModelType.ModelParameters.String.class),
            @XmlElement(name = "Class", type = ModelType.ModelParameters.Class.class),
            @XmlElement(name = "Correlation", type = ModelType.ModelParameters.Correlation.class)
        })
        protected List<Serializable> durationOrDurationDistOrLength;

        /**
         * Gets the value of the durationOrDurationDistOrLength property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the durationOrDurationDistOrLength property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getDurationOrDurationDistOrLength().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ModelType.ModelParameters.Acceleration }
         * {@link ModelType.ModelParameters.AccelerationDist }
         * {@link ModelType.ModelParameters.Boolean }
         * {@link ModelType.ModelParameters.Class }
         * {@link ModelType.ModelParameters.Correlation }
         * {@link ModelType.ModelParameters.Double }
         * {@link ModelType.ModelParameters.DoubleDist }
         * {@link ModelType.ModelParameters.Duration }
         * {@link ModelType.ModelParameters.DurationDist }
         * {@link ModelType.ModelParameters.Fraction }
         * {@link ModelType.ModelParameters.Frequency }
         * {@link ModelType.ModelParameters.FrequencyDist }
         * {@link ModelType.ModelParameters.Integer }
         * {@link ModelType.ModelParameters.IntegerDist }
         * {@link ModelType.ModelParameters.Length }
         * {@link ModelType.ModelParameters.LengthDist }
         * {@link ModelType.ModelParameters.LinearDensity }
         * {@link ModelType.ModelParameters.LinearDensityDist }
         * {@link ModelType.ModelParameters.Speed }
         * {@link ModelType.ModelParameters.SpeedDist }
         * {@link ModelType.ModelParameters.String }
         * </p>
         * 
         * 
         * @return
         *     The value of the durationOrDurationDistOrLength property.
         */
        public List<Serializable> getDurationOrDurationDistOrLength() {
            if (durationOrDurationDistOrLength == null) {
                durationOrDurationDistOrLength = new ArrayList<>();
            }
            return this.durationOrDurationDistOrLength;
        }


        /**
         * <p>Java class for anonymous complex type</p>.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.</p>
         * 
         * <pre>{@code
         * <complexType>
         *   <simpleContent>
         *     <extension base="<http://www.opentrafficsim.org/ots>AccelerationType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Acceleration implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(AccelerationAdapter.class)
            protected AccelerationType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public AccelerationType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(AccelerationType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class AccelerationDist
            extends AccelerationDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>boolean">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Boolean implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(BooleanAdapter.class)
            protected BooleanType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public BooleanType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(BooleanType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>ClassNameType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Class implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(ClassAdapter.class)
            protected ClassType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public ClassType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(ClassType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *         <element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/>
         *         <element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/>
         *       </sequence>
         *       <attribute name="Expression">
         *         <simpleType>
         *           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             <pattern value="[^{}]+"/>
         *           </restriction>
         *         </simpleType>
         *       </attribute>
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "first",
            "then"
        })
        public static class Correlation implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "First")
            protected CorrelationParameterType first;
            @XmlElement(name = "Then", required = true)
            protected CorrelationParameterType then;
            /**
             * Expression with a local context. Use 'first' and 'then' to refer
             *                       to the two base parameter values. The expression result will become the value for 'then'.
             * 
             */
            @XmlAttribute(name = "Expression")
            protected java.lang.String expression;

            /**
             * Gets the value of the first property.
             * 
             * @return
             *     possible object is
             *     {@link CorrelationParameterType }
             *     
             */
            public CorrelationParameterType getFirst() {
                return first;
            }

            /**
             * Sets the value of the first property.
             * 
             * @param value
             *     allowed object is
             *     {@link CorrelationParameterType }
             *     
             */
            public void setFirst(CorrelationParameterType value) {
                this.first = value;
            }

            /**
             * Gets the value of the then property.
             * 
             * @return
             *     possible object is
             *     {@link CorrelationParameterType }
             *     
             */
            public CorrelationParameterType getThen() {
                return then;
            }

            /**
             * Sets the value of the then property.
             * 
             * @param value
             *     allowed object is
             *     {@link CorrelationParameterType }
             *     
             */
            public void setThen(CorrelationParameterType value) {
                this.then = value;
            }

            /**
             * Expression with a local context. Use 'first' and 'then' to refer
             *                       to the two base parameter values. The expression result will become the value for 'then'.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public java.lang.String getExpression() {
                return expression;
            }

            /**
             * Sets the value of the expression property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             * @see #getExpression()
             */
            public void setExpression(java.lang.String value) {
                this.expression = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>double">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Double implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(DoubleAdapter.class)
            protected org.opentrafficsim.xml.bindings.types.DoubleType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class DoubleDist
            extends ConstantDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>DurationType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Duration implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(DurationAdapter.class)
            protected DurationType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public DurationType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(DurationType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}DurationDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class DurationDist
            extends DurationDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>FractionType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Fraction implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(FractionAdapter.class)
            protected org.opentrafficsim.xml.bindings.types.DoubleType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>FrequencyType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Frequency implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(FrequencyAdapter.class)
            protected FrequencyType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public FrequencyType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(FrequencyType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class FrequencyDist
            extends FrequencyDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>integer">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Integer implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(LongAdapter.class)
            protected LongType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public LongType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(LongType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class IntegerDist
            extends DiscreteDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>LengthType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Length implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(LengthAdapter.class)
            protected LengthType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public LengthType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(LengthType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}LengthDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class LengthDist
            extends LengthDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>LinearDensityType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class LinearDensity implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(LinearDensityAdapter.class)
            protected LinearDensityType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public LinearDensityType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(LinearDensityType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class LinearDensityDist
            extends LinearDensityDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>SpeedType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class Speed implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(SpeedAdapter.class)
            protected SpeedType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public SpeedType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(SpeedType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="{http://www.opentrafficsim.org/ots}SpeedDistType">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SpeedDist
            extends SpeedDistType
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
         *     <extension base="<http://www.opentrafficsim.org/ots>string">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
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
        public static class String implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setValue(StringType value) {
                this.value = value;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
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
     *       <choice>
     *         <element name="Route">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <choice>
     *                   <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *                   <element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *                 </choice>
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
        "route"
    })
    public static class StrategicalPlanner
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Route")
        protected ModelType.StrategicalPlanner.Route route;

        /**
         * Gets the value of the route property.
         * 
         * @return
         *     possible object is
         *     {@link ModelType.StrategicalPlanner.Route }
         *     
         */
        public ModelType.StrategicalPlanner.Route getRoute() {
            return route;
        }

        /**
         * Sets the value of the route property.
         * 
         * @param value
         *     allowed object is
         *     {@link ModelType.StrategicalPlanner.Route }
         *     
         */
        public void setRoute(ModelType.StrategicalPlanner.Route value) {
            this.route = value;
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
         *         <element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
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
            "shortest"
        })
        public static class Route
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "None")
            protected EmptyType none;
            @XmlElement(name = "Shortest")
            protected EmptyType shortest;

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
             * Gets the value of the shortest property.
             * 
             * @return
             *     possible object is
             *     {@link EmptyType }
             *     
             */
            public EmptyType getShortest() {
                return shortest;
            }

            /**
             * Sets the value of the shortest property.
             * 
             * @param value
             *     allowed object is
             *     {@link EmptyType }
             *     
             */
            public void setShortest(EmptyType value) {
                this.shortest = value;
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
     *       <choice>
     *         <element name="Lmrs">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="CarFollowingModel" type="{http://www.opentrafficsim.org/ots}CarFollowingModelType" minOccurs="0"/>
     *                   <element name="Synchronization" minOccurs="0">
     *                     <simpleType>
     *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *                         <simpleType>
     *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                             <enumeration value="DEADEND"/>
     *                             <enumeration value="PASSIVE"/>
     *                             <enumeration value="PASSIVE_MOVING"/>
     *                             <enumeration value="ALIGN_GAP"/>
     *                             <enumeration value="ACTIVE"/>
     *                           </restriction>
     *                         </simpleType>
     *                       </union>
     *                     </simpleType>
     *                   </element>
     *                   <element name="Cooperation" minOccurs="0">
     *                     <simpleType>
     *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *                         <simpleType>
     *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                             <enumeration value="PASSIVE"/>
     *                             <enumeration value="PASSIVE_MOVING"/>
     *                             <enumeration value="ACTIVE"/>
     *                           </restriction>
     *                         </simpleType>
     *                       </union>
     *                     </simpleType>
     *                   </element>
     *                   <element name="GapAcceptance" minOccurs="0">
     *                     <simpleType>
     *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *                         <simpleType>
     *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                             <enumeration value="INFORMED"/>
     *                             <enumeration value="EGO_HEADWAY"/>
     *                           </restriction>
     *                         </simpleType>
     *                       </union>
     *                     </simpleType>
     *                   </element>
     *                   <element name="Tailgating" minOccurs="0">
     *                     <simpleType>
     *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *                         <simpleType>
     *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                             <enumeration value="NONE"/>
     *                             <enumeration value="RHO_ONLY"/>
     *                             <enumeration value="PRESSURE"/>
     *                           </restriction>
     *                         </simpleType>
     *                       </union>
     *                     </simpleType>
     *                   </element>
     *                   <element name="MandatoryIncentives" minOccurs="0">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           <all>
     *                             <element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                           </all>
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                   <element name="VoluntaryIncentives" minOccurs="0">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           <all>
     *                             <element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                           </all>
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                   <element name="AccelerationIncentives" minOccurs="0">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           <all>
     *                             <element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                           </all>
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                   <element name="Perception" type="{http://www.opentrafficsim.org/ots}PerceptionType" minOccurs="0"/>
     *                 </sequence>
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
        "lmrs"
    })
    public static class TacticalPlanner
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Lmrs")
        protected ModelType.TacticalPlanner.Lmrs lmrs;

        /**
         * Gets the value of the lmrs property.
         * 
         * @return
         *     possible object is
         *     {@link ModelType.TacticalPlanner.Lmrs }
         *     
         */
        public ModelType.TacticalPlanner.Lmrs getLmrs() {
            return lmrs;
        }

        /**
         * Sets the value of the lmrs property.
         * 
         * @param value
         *     allowed object is
         *     {@link ModelType.TacticalPlanner.Lmrs }
         *     
         */
        public void setLmrs(ModelType.TacticalPlanner.Lmrs value) {
            this.lmrs = value;
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
         *         <element name="CarFollowingModel" type="{http://www.opentrafficsim.org/ots}CarFollowingModelType" minOccurs="0"/>
         *         <element name="Synchronization" minOccurs="0">
         *           <simpleType>
         *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
         *               <simpleType>
         *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                   <enumeration value="DEADEND"/>
         *                   <enumeration value="PASSIVE"/>
         *                   <enumeration value="PASSIVE_MOVING"/>
         *                   <enumeration value="ALIGN_GAP"/>
         *                   <enumeration value="ACTIVE"/>
         *                 </restriction>
         *               </simpleType>
         *             </union>
         *           </simpleType>
         *         </element>
         *         <element name="Cooperation" minOccurs="0">
         *           <simpleType>
         *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
         *               <simpleType>
         *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                   <enumeration value="PASSIVE"/>
         *                   <enumeration value="PASSIVE_MOVING"/>
         *                   <enumeration value="ACTIVE"/>
         *                 </restriction>
         *               </simpleType>
         *             </union>
         *           </simpleType>
         *         </element>
         *         <element name="GapAcceptance" minOccurs="0">
         *           <simpleType>
         *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
         *               <simpleType>
         *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                   <enumeration value="INFORMED"/>
         *                   <enumeration value="EGO_HEADWAY"/>
         *                 </restriction>
         *               </simpleType>
         *             </union>
         *           </simpleType>
         *         </element>
         *         <element name="Tailgating" minOccurs="0">
         *           <simpleType>
         *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
         *               <simpleType>
         *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                   <enumeration value="NONE"/>
         *                   <enumeration value="RHO_ONLY"/>
         *                   <enumeration value="PRESSURE"/>
         *                 </restriction>
         *               </simpleType>
         *             </union>
         *           </simpleType>
         *         </element>
         *         <element name="MandatoryIncentives" minOccurs="0">
         *           <complexType>
         *             <complexContent>
         *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 <all>
         *                   <element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                 </all>
         *               </restriction>
         *             </complexContent>
         *           </complexType>
         *         </element>
         *         <element name="VoluntaryIncentives" minOccurs="0">
         *           <complexType>
         *             <complexContent>
         *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 <all>
         *                   <element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                 </all>
         *               </restriction>
         *             </complexContent>
         *           </complexType>
         *         </element>
         *         <element name="AccelerationIncentives" minOccurs="0">
         *           <complexType>
         *             <complexContent>
         *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 <all>
         *                   <element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                 </all>
         *               </restriction>
         *             </complexContent>
         *           </complexType>
         *         </element>
         *         <element name="Perception" type="{http://www.opentrafficsim.org/ots}PerceptionType" minOccurs="0"/>
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
            "carFollowingModel",
            "synchronization",
            "cooperation",
            "gapAcceptance",
            "tailgating",
            "mandatoryIncentives",
            "voluntaryIncentives",
            "accelerationIncentives",
            "perception"
        })
        public static class Lmrs
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "CarFollowingModel")
            protected CarFollowingModelType carFollowingModel;
            /**
             * DEADEND: stop for dead-end during synchronization. PASSIVE
             *                         (default): follow first leader in target lane. ALIGNGAP: align to middle of adjacent gap. ACTIVE:
             *                         actively consider whether gaps can be reached in time (not advised).
             * 
             */
            @XmlElement(name = "Synchronization", type = java.lang.String.class, defaultValue = "PASSIVE")
            @XmlJavaTypeAdapter(SynchronizationAdapter.class)
            protected SynchronizationType synchronization;
            /**
             * PASSIVE (default): follow potential lane changer.
             *                         PASSIVEMOVING: follow potential lane changer except at very low ego-speed. ACTIVE: actively consider
             *                         whether the potential lane changer can make the gap.
             * 
             */
            @XmlElement(name = "Cooperation", type = java.lang.String.class, defaultValue = "PASSIVE")
            @XmlJavaTypeAdapter(CooperationAdapter.class)
            protected CooperationType cooperation;
            /**
             * INFORMED: aware of desired headway of potential follower.
             *                         EGOHEADWAY (default): potential follower evaluated with own desired headway.
             * 
             */
            @XmlElement(name = "GapAcceptance", type = java.lang.String.class)
            @XmlJavaTypeAdapter(GapAcceptanceAdapter.class)
            protected GapAcceptanceType gapAcceptance;
            /**
             * NONE (default): no tailgating. RHOONLY: pressure parameter
             *                         affects other traffic, ego headway not affected. PRESSURE: ego headway and surrounding traffic affected.
             * 
             */
            @XmlElement(name = "Tailgating", type = java.lang.String.class)
            @XmlJavaTypeAdapter(TailgatingAdapter.class)
            protected TailgatingType tailgating;
            @XmlElement(name = "MandatoryIncentives")
            protected ModelType.TacticalPlanner.Lmrs.MandatoryIncentives mandatoryIncentives;
            @XmlElement(name = "VoluntaryIncentives")
            protected ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives voluntaryIncentives;
            @XmlElement(name = "AccelerationIncentives")
            protected ModelType.TacticalPlanner.Lmrs.AccelerationIncentives accelerationIncentives;
            @XmlElement(name = "Perception")
            protected PerceptionType perception;

            /**
             * Gets the value of the carFollowingModel property.
             * 
             * @return
             *     possible object is
             *     {@link CarFollowingModelType }
             *     
             */
            public CarFollowingModelType getCarFollowingModel() {
                return carFollowingModel;
            }

            /**
             * Sets the value of the carFollowingModel property.
             * 
             * @param value
             *     allowed object is
             *     {@link CarFollowingModelType }
             *     
             */
            public void setCarFollowingModel(CarFollowingModelType value) {
                this.carFollowingModel = value;
            }

            /**
             * DEADEND: stop for dead-end during synchronization. PASSIVE
             *                         (default): follow first leader in target lane. ALIGNGAP: align to middle of adjacent gap. ACTIVE:
             *                         actively consider whether gaps can be reached in time (not advised).
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public SynchronizationType getSynchronization() {
                return synchronization;
            }

            /**
             * Sets the value of the synchronization property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             * @see #getSynchronization()
             */
            public void setSynchronization(SynchronizationType value) {
                this.synchronization = value;
            }

            /**
             * PASSIVE (default): follow potential lane changer.
             *                         PASSIVEMOVING: follow potential lane changer except at very low ego-speed. ACTIVE: actively consider
             *                         whether the potential lane changer can make the gap.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public CooperationType getCooperation() {
                return cooperation;
            }

            /**
             * Sets the value of the cooperation property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             * @see #getCooperation()
             */
            public void setCooperation(CooperationType value) {
                this.cooperation = value;
            }

            /**
             * INFORMED: aware of desired headway of potential follower.
             *                         EGOHEADWAY (default): potential follower evaluated with own desired headway.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public GapAcceptanceType getGapAcceptance() {
                return gapAcceptance;
            }

            /**
             * Sets the value of the gapAcceptance property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             * @see #getGapAcceptance()
             */
            public void setGapAcceptance(GapAcceptanceType value) {
                this.gapAcceptance = value;
            }

            /**
             * NONE (default): no tailgating. RHOONLY: pressure parameter
             *                         affects other traffic, ego headway not affected. PRESSURE: ego headway and surrounding traffic affected.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            public TailgatingType getTailgating() {
                return tailgating;
            }

            /**
             * Sets the value of the tailgating property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             * @see #getTailgating()
             */
            public void setTailgating(TailgatingType value) {
                this.tailgating = value;
            }

            /**
             * Gets the value of the mandatoryIncentives property.
             * 
             * @return
             *     possible object is
             *     {@link ModelType.TacticalPlanner.Lmrs.MandatoryIncentives }
             *     
             */
            public ModelType.TacticalPlanner.Lmrs.MandatoryIncentives getMandatoryIncentives() {
                return mandatoryIncentives;
            }

            /**
             * Sets the value of the mandatoryIncentives property.
             * 
             * @param value
             *     allowed object is
             *     {@link ModelType.TacticalPlanner.Lmrs.MandatoryIncentives }
             *     
             */
            public void setMandatoryIncentives(ModelType.TacticalPlanner.Lmrs.MandatoryIncentives value) {
                this.mandatoryIncentives = value;
            }

            /**
             * Gets the value of the voluntaryIncentives property.
             * 
             * @return
             *     possible object is
             *     {@link ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives }
             *     
             */
            public ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives getVoluntaryIncentives() {
                return voluntaryIncentives;
            }

            /**
             * Sets the value of the voluntaryIncentives property.
             * 
             * @param value
             *     allowed object is
             *     {@link ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives }
             *     
             */
            public void setVoluntaryIncentives(ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives value) {
                this.voluntaryIncentives = value;
            }

            /**
             * Gets the value of the accelerationIncentives property.
             * 
             * @return
             *     possible object is
             *     {@link ModelType.TacticalPlanner.Lmrs.AccelerationIncentives }
             *     
             */
            public ModelType.TacticalPlanner.Lmrs.AccelerationIncentives getAccelerationIncentives() {
                return accelerationIncentives;
            }

            /**
             * Sets the value of the accelerationIncentives property.
             * 
             * @param value
             *     allowed object is
             *     {@link ModelType.TacticalPlanner.Lmrs.AccelerationIncentives }
             *     
             */
            public void setAccelerationIncentives(ModelType.TacticalPlanner.Lmrs.AccelerationIncentives value) {
                this.accelerationIncentives = value;
            }

            /**
             * Gets the value of the perception property.
             * 
             * @return
             *     possible object is
             *     {@link PerceptionType }
             *     
             */
            public PerceptionType getPerception() {
                return perception;
            }

            /**
             * Sets the value of the perception property.
             * 
             * @param value
             *     allowed object is
             *     {@link PerceptionType }
             *     
             */
            public void setPerception(PerceptionType value) {
                this.perception = value;
            }


            /**
             * TrafficLights: consider traffic lights. Conflicts: consider
             *                           intersection conflicts. SpeedLimitTransitions: decelerate for lower speed limit ahead.
             *                           NoRightOvertake: follow left leader, in some circumstances. BusStop: for scheduled busses to stop.
             *                           Class: from a class with empty constructor.
             * 
             * <p>Java class for anonymous complex type</p>.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.</p>
             * 
             * <pre>{@code
             * <complexType>
             *   <complexContent>
             *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       <all>
             *         <element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *       </all>
             *     </restriction>
             *   </complexContent>
             * </complexType>
             * }</pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            public static class AccelerationIncentives
                implements Serializable
            {

                private static final long serialVersionUID = 10102L;
                @XmlElement(name = "TrafficLights")
                protected EmptyType trafficLights;
                @XmlElement(name = "Conflicts")
                protected EmptyType conflicts;
                @XmlElement(name = "SpeedLimitTransitions")
                protected EmptyType speedLimitTransitions;
                @XmlElement(name = "NoRightOvertake")
                protected EmptyType noRightOvertake;
                @XmlElement(name = "BusStop")
                protected EmptyType busStop;

                /**
                 * Gets the value of the trafficLights property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getTrafficLights() {
                    return trafficLights;
                }

                /**
                 * Sets the value of the trafficLights property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setTrafficLights(EmptyType value) {
                    this.trafficLights = value;
                }

                /**
                 * Gets the value of the conflicts property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getConflicts() {
                    return conflicts;
                }

                /**
                 * Sets the value of the conflicts property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setConflicts(EmptyType value) {
                    this.conflicts = value;
                }

                /**
                 * Gets the value of the speedLimitTransitions property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getSpeedLimitTransitions() {
                    return speedLimitTransitions;
                }

                /**
                 * Sets the value of the speedLimitTransitions property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setSpeedLimitTransitions(EmptyType value) {
                    this.speedLimitTransitions = value;
                }

                /**
                 * Gets the value of the noRightOvertake property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getNoRightOvertake() {
                    return noRightOvertake;
                }

                /**
                 * Sets the value of the noRightOvertake property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setNoRightOvertake(EmptyType value) {
                    this.noRightOvertake = value;
                }

                /**
                 * Gets the value of the busStop property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getBusStop() {
                    return busStop;
                }

                /**
                 * Sets the value of the busStop property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setBusStop(EmptyType value) {
                    this.busStop = value;
                }

            }


            /**
             * Route: route and infrastructure. GetInLane: earlier lane
             *                           change when traffic on target lane is slow. BusStop: for scheduled busses. Class: from a class with
             *                           empty constructor.
             * 
             * <p>Java class for anonymous complex type</p>.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.</p>
             * 
             * <pre>{@code
             * <complexType>
             *   <complexContent>
             *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       <all>
             *         <element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *       </all>
             *     </restriction>
             *   </complexContent>
             * </complexType>
             * }</pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            public static class MandatoryIncentives
                implements Serializable
            {

                private static final long serialVersionUID = 10102L;
                @XmlElement(name = "Route")
                protected EmptyType route;
                @XmlElement(name = "GetInLane")
                protected EmptyType getInLane;
                @XmlElement(name = "BusStop")
                protected EmptyType busStop;

                /**
                 * Gets the value of the route property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getRoute() {
                    return route;
                }

                /**
                 * Sets the value of the route property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setRoute(EmptyType value) {
                    this.route = value;
                }

                /**
                 * Gets the value of the getInLane property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getGetInLane() {
                    return getInLane;
                }

                /**
                 * Sets the value of the getInLane property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setGetInLane(EmptyType value) {
                    this.getInLane = value;
                }

                /**
                 * Gets the value of the busStop property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getBusStop() {
                    return busStop;
                }

                /**
                 * Sets the value of the busStop property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setBusStop(EmptyType value) {
                    this.busStop = value;
                }

            }


            /**
             * <b>Keep</b>: keep right. <br/>
             *                           <b>SpeedWithCourtesy</b>: based on anticipated speed, and potential lane changers.
             *                           <br/>
             *                           <b>Courtesy</b>: get or stay out of the way for lane change desire of others.
             *                           <br/>
             *                           <b>SocioSpeed</b>: get or stay out of the way for desired speed of others.
             *                           <br/>
             *                           <b>StayRight</b>: incentive for trucks to stay on the right-most two lanes,
             *                           interpreted in line with the route. <br/>
             *                           <b>Class</b>: from a class with empty
             *                           constructor.
             * 
             * <p>Java class for anonymous complex type</p>.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.</p>
             * 
             * <pre>{@code
             * <complexType>
             *   <complexContent>
             *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       <all>
             *         <element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *       </all>
             *     </restriction>
             *   </complexContent>
             * </complexType>
             * }</pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            public static class VoluntaryIncentives
                implements Serializable
            {

                private static final long serialVersionUID = 10102L;
                @XmlElement(name = "Keep")
                protected EmptyType keep;
                @XmlElement(name = "SpeedWithCourtesy")
                protected EmptyType speedWithCourtesy;
                @XmlElement(name = "Courtesy")
                protected EmptyType courtesy;
                @XmlElement(name = "SocioSpeed")
                protected EmptyType socioSpeed;
                @XmlElement(name = "StayRight")
                protected EmptyType stayRight;

                /**
                 * Gets the value of the keep property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getKeep() {
                    return keep;
                }

                /**
                 * Sets the value of the keep property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setKeep(EmptyType value) {
                    this.keep = value;
                }

                /**
                 * Gets the value of the speedWithCourtesy property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getSpeedWithCourtesy() {
                    return speedWithCourtesy;
                }

                /**
                 * Sets the value of the speedWithCourtesy property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setSpeedWithCourtesy(EmptyType value) {
                    this.speedWithCourtesy = value;
                }

                /**
                 * Gets the value of the courtesy property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getCourtesy() {
                    return courtesy;
                }

                /**
                 * Sets the value of the courtesy property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setCourtesy(EmptyType value) {
                    this.courtesy = value;
                }

                /**
                 * Gets the value of the socioSpeed property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getSocioSpeed() {
                    return socioSpeed;
                }

                /**
                 * Sets the value of the socioSpeed property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setSocioSpeed(EmptyType value) {
                    this.socioSpeed = value;
                }

                /**
                 * Gets the value of the stayRight property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getStayRight() {
                    return stayRight;
                }

                /**
                 * Sets the value of the stayRight property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setStayRight(EmptyType value) {
                    this.stayRight = value;
                }

            }

        }

    }

}
