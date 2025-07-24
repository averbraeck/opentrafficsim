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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
 * <p>Java-Klasse für ModelType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ModelParameters" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *                   &lt;choice maxOccurs="unbounded"&gt;
 *                     &lt;element name="Duration"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;DurationType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="DurationDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}DurationDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Length"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;LengthType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="LengthDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}LengthDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Speed"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;SpeedType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="SpeedDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}SpeedDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Acceleration"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;AccelerationType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="AccelerationDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="LinearDensity"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;LinearDensityType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="LinearDensityDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Frequency"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;FrequencyType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="FrequencyDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Double"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;double"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="DoubleDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}ConstantDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Fraction"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;FractionType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Integer"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;integer"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="IntegerDist"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;complexContent&gt;
 *                           &lt;extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/complexContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Boolean"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;boolean"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="String"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;string"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="Class"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;ClassNameType"&gt;
 *                             &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                   &lt;/choice&gt;
 *                   &lt;element name="Correlation" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/&gt;
 *                             &lt;element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="Expression"&gt;
 *                             &lt;simpleType&gt;
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                 &lt;pattern value="[^{}]+"/&gt;
 *                               &lt;/restriction&gt;
 *                             &lt;/simpleType&gt;
 *                           &lt;/attribute&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="StrategicalPlanner" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="Route"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;choice&gt;
 *                             &lt;element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *                             &lt;element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *                           &lt;/choice&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TacticalPlanner" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="Lmrs"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="CarFollowingModel" type="{http://www.opentrafficsim.org/ots}CarFollowingModelType" minOccurs="0"/&gt;
 *                             &lt;element name="Synchronization" minOccurs="0"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                                   &lt;simpleType&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                       &lt;enumeration value="DEADEND"/&gt;
 *                                       &lt;enumeration value="PASSIVE"/&gt;
 *                                       &lt;enumeration value="PASSIVE_MOVING"/&gt;
 *                                       &lt;enumeration value="ALIGN_GAP"/&gt;
 *                                       &lt;enumeration value="ACTIVE"/&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/simpleType&gt;
 *                                 &lt;/union&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="Cooperation" minOccurs="0"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                                   &lt;simpleType&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                       &lt;enumeration value="PASSIVE"/&gt;
 *                                       &lt;enumeration value="PASSIVE_MOVING"/&gt;
 *                                       &lt;enumeration value="ACTIVE"/&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/simpleType&gt;
 *                                 &lt;/union&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="GapAcceptance" minOccurs="0"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                                   &lt;simpleType&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                       &lt;enumeration value="INFORMED"/&gt;
 *                                       &lt;enumeration value="EGO_HEADWAY"/&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/simpleType&gt;
 *                                 &lt;/union&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="Tailgating" minOccurs="0"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                                   &lt;simpleType&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                       &lt;enumeration value="NONE"/&gt;
 *                                       &lt;enumeration value="RHO_ONLY"/&gt;
 *                                       &lt;enumeration value="PRESSURE"/&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/simpleType&gt;
 *                                 &lt;/union&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="MandatoryIncentives" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;all&gt;
 *                                       &lt;element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                     &lt;/all&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="VoluntaryIncentives" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;all&gt;
 *                                       &lt;element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                     &lt;/all&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="AccelerationIncentives" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;all&gt;
 *                                       &lt;element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                     &lt;/all&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="Perception" type="{http://www.opentrafficsim.org/ots}PerceptionType" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" /&gt;
 *       &lt;attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *       &lt;attribute name="GtuType" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelType", propOrder = {
    "modelParameters",
    "strategicalPlanner",
    "tacticalPlanner"
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class ModelType
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "ModelParameters")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ModelType.ModelParameters modelParameters;
    @XmlElement(name = "StrategicalPlanner")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ModelType.StrategicalPlanner strategicalPlanner;
    @XmlElement(name = "TacticalPlanner")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ModelType.TacticalPlanner tacticalPlanner;
    @XmlAttribute(name = "Id")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected java.lang.String id;
    @XmlAttribute(name = "Parent")
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType parent;
    @XmlAttribute(name = "GtuType")
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType gtuType;

    /**
     * Ruft den Wert der modelParameters-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.ModelParameters }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ModelType.ModelParameters getModelParameters() {
        return modelParameters;
    }

    /**
     * Legt den Wert der modelParameters-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.ModelParameters }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setModelParameters(ModelType.ModelParameters value) {
        this.modelParameters = value;
    }

    /**
     * Ruft den Wert der strategicalPlanner-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.StrategicalPlanner }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ModelType.StrategicalPlanner getStrategicalPlanner() {
        return strategicalPlanner;
    }

    /**
     * Legt den Wert der strategicalPlanner-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.StrategicalPlanner }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setStrategicalPlanner(ModelType.StrategicalPlanner value) {
        this.strategicalPlanner = value;
    }

    /**
     * Ruft den Wert der tacticalPlanner-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.TacticalPlanner }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ModelType.TacticalPlanner getTacticalPlanner() {
        return tacticalPlanner;
    }

    /**
     * Legt den Wert der tacticalPlanner-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.TacticalPlanner }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setTacticalPlanner(ModelType.TacticalPlanner value) {
        this.tacticalPlanner = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public java.lang.String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der parent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public StringType getParent() {
        return parent;
    }

    /**
     * Legt den Wert der parent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setParent(StringType value) {
        this.parent = value;
    }

    /**
     * Ruft den Wert der gtuType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public StringType getGtuType() {
        return gtuType;
    }

    /**
     * Legt den Wert der gtuType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setGtuType(StringType value) {
        this.gtuType = value;
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
     *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *         &lt;choice maxOccurs="unbounded"&gt;
     *           &lt;element name="Duration"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;DurationType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="DurationDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}DurationDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Length"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;LengthType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="LengthDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}LengthDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Speed"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;SpeedType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="SpeedDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}SpeedDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Acceleration"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;AccelerationType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="AccelerationDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="LinearDensity"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;LinearDensityType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="LinearDensityDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Frequency"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;FrequencyType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="FrequencyDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Double"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;double"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="DoubleDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}ConstantDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Fraction"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;FractionType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Integer"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;integer"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="IntegerDist"&gt;
     *             &lt;complexType&gt;
     *               &lt;complexContent&gt;
     *                 &lt;extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/complexContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Boolean"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;boolean"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="String"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;string"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="Class"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;ClassNameType"&gt;
     *                   &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *         &lt;/choice&gt;
     *         &lt;element name="Correlation" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/&gt;
     *                   &lt;element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/&gt;
     *                 &lt;/sequence&gt;
     *                 &lt;attribute name="Expression"&gt;
     *                   &lt;simpleType&gt;
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                       &lt;pattern value="[^{}]+"/&gt;
     *                     &lt;/restriction&gt;
     *                   &lt;/simpleType&gt;
     *                 &lt;/attribute&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
        "durationOrDurationDistOrLength"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class ModelParameters
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<Serializable> durationOrDurationDistOrLength;

        /**
         * Gets the value of the durationOrDurationDistOrLength property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the durationOrDurationDistOrLength property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDurationOrDurationDistOrLength().add(newItem);
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
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<Serializable> getDurationOrDurationDistOrLength() {
            if (durationOrDurationDistOrLength == null) {
                durationOrDurationDistOrLength = new ArrayList<Serializable>();
            }
            return this.durationOrDurationDistOrLength;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;AccelerationType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Acceleration implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(AccelerationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected AccelerationType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public AccelerationType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(AccelerationType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class AccelerationDist
            extends AccelerationDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;boolean"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Boolean implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(BooleanAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected BooleanType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public BooleanType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(BooleanType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;ClassNameType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Class implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(ClassAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected ClassType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public ClassType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(ClassType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

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
         *       &lt;sequence&gt;
         *         &lt;element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/&gt;
         *         &lt;element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/&gt;
         *       &lt;/sequence&gt;
         *       &lt;attribute name="Expression"&gt;
         *         &lt;simpleType&gt;
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *             &lt;pattern value="[^{}]+"/&gt;
         *           &lt;/restriction&gt;
         *         &lt;/simpleType&gt;
         *       &lt;/attribute&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "first",
            "then"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Correlation implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "First")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected CorrelationParameterType first;
            @XmlElement(name = "Then", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected CorrelationParameterType then;
            @XmlAttribute(name = "Expression")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected java.lang.String expression;

            /**
             * Ruft den Wert der first-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CorrelationParameterType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public CorrelationParameterType getFirst() {
                return first;
            }

            /**
             * Legt den Wert der first-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CorrelationParameterType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setFirst(CorrelationParameterType value) {
                this.first = value;
            }

            /**
             * Ruft den Wert der then-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CorrelationParameterType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public CorrelationParameterType getThen() {
                return then;
            }

            /**
             * Legt den Wert der then-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CorrelationParameterType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setThen(CorrelationParameterType value) {
                this.then = value;
            }

            /**
             * Ruft den Wert der expression-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public java.lang.String getExpression() {
                return expression;
            }

            /**
             * Legt den Wert der expression-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setExpression(java.lang.String value) {
                this.expression = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;double"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Double implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(DoubleAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected org.opentrafficsim.xml.bindings.types.DoubleType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}ConstantDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class DoubleDist
            extends ConstantDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;DurationType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Duration implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected DurationType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public DurationType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(DurationType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}DurationDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class DurationDist
            extends DurationDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;FractionType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Fraction implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(FractionAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected org.opentrafficsim.xml.bindings.types.DoubleType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;FrequencyType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Frequency implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(FrequencyAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected FrequencyType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public FrequencyType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(FrequencyType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class FrequencyDist
            extends FrequencyDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;integer"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Integer implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(LongAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected LongType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public LongType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(LongType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class IntegerDist
            extends DiscreteDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;LengthType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Length implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(LengthAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected LengthType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public LengthType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(LengthType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}LengthDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class LengthDist
            extends LengthDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;LinearDensityType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class LinearDensity implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(LinearDensityAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected LinearDensityType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public LinearDensityType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(LinearDensityType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class LinearDensityDist
            extends LinearDensityDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;SpeedType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Speed implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(SpeedAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected SpeedType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public SpeedType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(SpeedType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{http://www.opentrafficsim.org/ots}SpeedDistType"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class SpeedDist
            extends SpeedDistType
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;string"&gt;
         *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class String implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType value;
            @XmlAttribute(name = "Id", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(StringType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setId(StringType value) {
                this.id = value;
            }

        }

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
     *         &lt;element name="Route"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;choice&gt;
     *                   &lt;element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
     *                   &lt;element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
     *                 &lt;/choice&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
        "route"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class StrategicalPlanner
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Route")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected ModelType.StrategicalPlanner.Route route;

        /**
         * Ruft den Wert der route-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ModelType.StrategicalPlanner.Route }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public ModelType.StrategicalPlanner.Route getRoute() {
            return route;
        }

        /**
         * Legt den Wert der route-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ModelType.StrategicalPlanner.Route }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setRoute(ModelType.StrategicalPlanner.Route value) {
            this.route = value;
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
         *         &lt;element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
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
            "shortest"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Route
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "None")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected EmptyType none;
            @XmlElement(name = "Shortest")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected EmptyType shortest;

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
             * Ruft den Wert der shortest-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link EmptyType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public EmptyType getShortest() {
                return shortest;
            }

            /**
             * Legt den Wert der shortest-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link EmptyType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setShortest(EmptyType value) {
                this.shortest = value;
            }

        }

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
     *         &lt;element name="Lmrs"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="CarFollowingModel" type="{http://www.opentrafficsim.org/ots}CarFollowingModelType" minOccurs="0"/&gt;
     *                   &lt;element name="Synchronization" minOccurs="0"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *                         &lt;simpleType&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                             &lt;enumeration value="DEADEND"/&gt;
     *                             &lt;enumeration value="PASSIVE"/&gt;
     *                             &lt;enumeration value="PASSIVE_MOVING"/&gt;
     *                             &lt;enumeration value="ALIGN_GAP"/&gt;
     *                             &lt;enumeration value="ACTIVE"/&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/simpleType&gt;
     *                       &lt;/union&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="Cooperation" minOccurs="0"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *                         &lt;simpleType&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                             &lt;enumeration value="PASSIVE"/&gt;
     *                             &lt;enumeration value="PASSIVE_MOVING"/&gt;
     *                             &lt;enumeration value="ACTIVE"/&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/simpleType&gt;
     *                       &lt;/union&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="GapAcceptance" minOccurs="0"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *                         &lt;simpleType&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                             &lt;enumeration value="INFORMED"/&gt;
     *                             &lt;enumeration value="EGO_HEADWAY"/&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/simpleType&gt;
     *                       &lt;/union&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="Tailgating" minOccurs="0"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *                         &lt;simpleType&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                             &lt;enumeration value="NONE"/&gt;
     *                             &lt;enumeration value="RHO_ONLY"/&gt;
     *                             &lt;enumeration value="PRESSURE"/&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/simpleType&gt;
     *                       &lt;/union&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="MandatoryIncentives" minOccurs="0"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;all&gt;
     *                             &lt;element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                           &lt;/all&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="VoluntaryIncentives" minOccurs="0"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;all&gt;
     *                             &lt;element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                           &lt;/all&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="AccelerationIncentives" minOccurs="0"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;all&gt;
     *                             &lt;element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                           &lt;/all&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="Perception" type="{http://www.opentrafficsim.org/ots}PerceptionType" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
        "lmrs"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class TacticalPlanner
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Lmrs")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected ModelType.TacticalPlanner.Lmrs lmrs;

        /**
         * Ruft den Wert der lmrs-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ModelType.TacticalPlanner.Lmrs }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public ModelType.TacticalPlanner.Lmrs getLmrs() {
            return lmrs;
        }

        /**
         * Legt den Wert der lmrs-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ModelType.TacticalPlanner.Lmrs }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setLmrs(ModelType.TacticalPlanner.Lmrs value) {
            this.lmrs = value;
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
         *       &lt;sequence&gt;
         *         &lt;element name="CarFollowingModel" type="{http://www.opentrafficsim.org/ots}CarFollowingModelType" minOccurs="0"/&gt;
         *         &lt;element name="Synchronization" minOccurs="0"&gt;
         *           &lt;simpleType&gt;
         *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
         *               &lt;simpleType&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *                   &lt;enumeration value="DEADEND"/&gt;
         *                   &lt;enumeration value="PASSIVE"/&gt;
         *                   &lt;enumeration value="PASSIVE_MOVING"/&gt;
         *                   &lt;enumeration value="ALIGN_GAP"/&gt;
         *                   &lt;enumeration value="ACTIVE"/&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/simpleType&gt;
         *             &lt;/union&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="Cooperation" minOccurs="0"&gt;
         *           &lt;simpleType&gt;
         *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
         *               &lt;simpleType&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *                   &lt;enumeration value="PASSIVE"/&gt;
         *                   &lt;enumeration value="PASSIVE_MOVING"/&gt;
         *                   &lt;enumeration value="ACTIVE"/&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/simpleType&gt;
         *             &lt;/union&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="GapAcceptance" minOccurs="0"&gt;
         *           &lt;simpleType&gt;
         *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
         *               &lt;simpleType&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *                   &lt;enumeration value="INFORMED"/&gt;
         *                   &lt;enumeration value="EGO_HEADWAY"/&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/simpleType&gt;
         *             &lt;/union&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="Tailgating" minOccurs="0"&gt;
         *           &lt;simpleType&gt;
         *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
         *               &lt;simpleType&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *                   &lt;enumeration value="NONE"/&gt;
         *                   &lt;enumeration value="RHO_ONLY"/&gt;
         *                   &lt;enumeration value="PRESSURE"/&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/simpleType&gt;
         *             &lt;/union&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="MandatoryIncentives" minOccurs="0"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;all&gt;
         *                   &lt;element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                 &lt;/all&gt;
         *               &lt;/restriction&gt;
         *             &lt;/complexContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="VoluntaryIncentives" minOccurs="0"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;all&gt;
         *                   &lt;element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                 &lt;/all&gt;
         *               &lt;/restriction&gt;
         *             &lt;/complexContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="AccelerationIncentives" minOccurs="0"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;all&gt;
         *                   &lt;element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                 &lt;/all&gt;
         *               &lt;/restriction&gt;
         *             &lt;/complexContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="Perception" type="{http://www.opentrafficsim.org/ots}PerceptionType" minOccurs="0"/&gt;
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Lmrs
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "CarFollowingModel")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected CarFollowingModelType carFollowingModel;
            @XmlElement(name = "Synchronization", type = java.lang.String.class, defaultValue = "PASSIVE")
            @XmlJavaTypeAdapter(SynchronizationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected SynchronizationType synchronization;
            @XmlElement(name = "Cooperation", type = java.lang.String.class, defaultValue = "PASSIVE")
            @XmlJavaTypeAdapter(CooperationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected CooperationType cooperation;
            @XmlElement(name = "GapAcceptance", type = java.lang.String.class)
            @XmlJavaTypeAdapter(GapAcceptanceAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected GapAcceptanceType gapAcceptance;
            @XmlElement(name = "Tailgating", type = java.lang.String.class)
            @XmlJavaTypeAdapter(TailgatingAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected TailgatingType tailgating;
            @XmlElement(name = "MandatoryIncentives")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected ModelType.TacticalPlanner.Lmrs.MandatoryIncentives mandatoryIncentives;
            @XmlElement(name = "VoluntaryIncentives")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives voluntaryIncentives;
            @XmlElement(name = "AccelerationIncentives")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected ModelType.TacticalPlanner.Lmrs.AccelerationIncentives accelerationIncentives;
            @XmlElement(name = "Perception")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected PerceptionType perception;

            /**
             * Ruft den Wert der carFollowingModel-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link CarFollowingModelType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public CarFollowingModelType getCarFollowingModel() {
                return carFollowingModel;
            }

            /**
             * Legt den Wert der carFollowingModel-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link CarFollowingModelType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setCarFollowingModel(CarFollowingModelType value) {
                this.carFollowingModel = value;
            }

            /**
             * Ruft den Wert der synchronization-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public SynchronizationType getSynchronization() {
                return synchronization;
            }

            /**
             * Legt den Wert der synchronization-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setSynchronization(SynchronizationType value) {
                this.synchronization = value;
            }

            /**
             * Ruft den Wert der cooperation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public CooperationType getCooperation() {
                return cooperation;
            }

            /**
             * Legt den Wert der cooperation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setCooperation(CooperationType value) {
                this.cooperation = value;
            }

            /**
             * Ruft den Wert der gapAcceptance-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public GapAcceptanceType getGapAcceptance() {
                return gapAcceptance;
            }

            /**
             * Legt den Wert der gapAcceptance-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setGapAcceptance(GapAcceptanceType value) {
                this.gapAcceptance = value;
            }

            /**
             * Ruft den Wert der tailgating-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public TailgatingType getTailgating() {
                return tailgating;
            }

            /**
             * Legt den Wert der tailgating-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setTailgating(TailgatingType value) {
                this.tailgating = value;
            }

            /**
             * Ruft den Wert der mandatoryIncentives-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link ModelType.TacticalPlanner.Lmrs.MandatoryIncentives }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public ModelType.TacticalPlanner.Lmrs.MandatoryIncentives getMandatoryIncentives() {
                return mandatoryIncentives;
            }

            /**
             * Legt den Wert der mandatoryIncentives-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link ModelType.TacticalPlanner.Lmrs.MandatoryIncentives }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setMandatoryIncentives(ModelType.TacticalPlanner.Lmrs.MandatoryIncentives value) {
                this.mandatoryIncentives = value;
            }

            /**
             * Ruft den Wert der voluntaryIncentives-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives getVoluntaryIncentives() {
                return voluntaryIncentives;
            }

            /**
             * Legt den Wert der voluntaryIncentives-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setVoluntaryIncentives(ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives value) {
                this.voluntaryIncentives = value;
            }

            /**
             * Ruft den Wert der accelerationIncentives-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link ModelType.TacticalPlanner.Lmrs.AccelerationIncentives }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public ModelType.TacticalPlanner.Lmrs.AccelerationIncentives getAccelerationIncentives() {
                return accelerationIncentives;
            }

            /**
             * Legt den Wert der accelerationIncentives-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link ModelType.TacticalPlanner.Lmrs.AccelerationIncentives }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setAccelerationIncentives(ModelType.TacticalPlanner.Lmrs.AccelerationIncentives value) {
                this.accelerationIncentives = value;
            }

            /**
             * Ruft den Wert der perception-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link PerceptionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public PerceptionType getPerception() {
                return perception;
            }

            /**
             * Legt den Wert der perception-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link PerceptionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setPerception(PerceptionType value) {
                this.perception = value;
            }


            /**
             * TrafficLights: consider traffic lights. Conflicts: consider
             *                           intersection conflicts. SpeedLimitTransitions: decelerate for lower speed limit ahead.
             *                           NoRightOvertake: follow left leader, in some circumstances. BusStop: for scheduled busses to stop.
             *                           Class: from a class with empty constructor.
             *                         
             * 
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType&gt;
             *   &lt;complexContent&gt;
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
             *       &lt;all&gt;
             *         &lt;element name="TrafficLights" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="Conflicts" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="NoRightOvertake" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *       &lt;/all&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public static class AccelerationIncentives
                implements Serializable
            {

                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                private final static long serialVersionUID = 10102L;
                @XmlElement(name = "TrafficLights")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType trafficLights;
                @XmlElement(name = "Conflicts")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType conflicts;
                @XmlElement(name = "SpeedLimitTransitions")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType speedLimitTransitions;
                @XmlElement(name = "NoRightOvertake")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType noRightOvertake;
                @XmlElement(name = "BusStop")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType busStop;

                /**
                 * Ruft den Wert der trafficLights-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getTrafficLights() {
                    return trafficLights;
                }

                /**
                 * Legt den Wert der trafficLights-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setTrafficLights(EmptyType value) {
                    this.trafficLights = value;
                }

                /**
                 * Ruft den Wert der conflicts-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getConflicts() {
                    return conflicts;
                }

                /**
                 * Legt den Wert der conflicts-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setConflicts(EmptyType value) {
                    this.conflicts = value;
                }

                /**
                 * Ruft den Wert der speedLimitTransitions-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getSpeedLimitTransitions() {
                    return speedLimitTransitions;
                }

                /**
                 * Legt den Wert der speedLimitTransitions-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setSpeedLimitTransitions(EmptyType value) {
                    this.speedLimitTransitions = value;
                }

                /**
                 * Ruft den Wert der noRightOvertake-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getNoRightOvertake() {
                    return noRightOvertake;
                }

                /**
                 * Legt den Wert der noRightOvertake-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setNoRightOvertake(EmptyType value) {
                    this.noRightOvertake = value;
                }

                /**
                 * Ruft den Wert der busStop-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getBusStop() {
                    return busStop;
                }

                /**
                 * Legt den Wert der busStop-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setBusStop(EmptyType value) {
                    this.busStop = value;
                }

            }


            /**
             * Route: route and infrastructure. GetInLane: earlier lane
             *                           change when traffic on target lane is slow. BusStop: for scheduled busses. Class: from a class with
             *                           empty constructor.
             *                         
             * 
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType&gt;
             *   &lt;complexContent&gt;
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
             *       &lt;all&gt;
             *         &lt;element name="Route" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="GetInLane" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *       &lt;/all&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public static class MandatoryIncentives
                implements Serializable
            {

                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                private final static long serialVersionUID = 10102L;
                @XmlElement(name = "Route")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType route;
                @XmlElement(name = "GetInLane")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType getInLane;
                @XmlElement(name = "BusStop")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType busStop;

                /**
                 * Ruft den Wert der route-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getRoute() {
                    return route;
                }

                /**
                 * Legt den Wert der route-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setRoute(EmptyType value) {
                    this.route = value;
                }

                /**
                 * Ruft den Wert der getInLane-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getGetInLane() {
                    return getInLane;
                }

                /**
                 * Legt den Wert der getInLane-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setGetInLane(EmptyType value) {
                    this.getInLane = value;
                }

                /**
                 * Ruft den Wert der busStop-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getBusStop() {
                    return busStop;
                }

                /**
                 * Legt den Wert der busStop-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setBusStop(EmptyType value) {
                    this.busStop = value;
                }

            }


            /**
             * Keep: keep right. SpeedWithCourtesy: based on anticipated
             *                           speed, and potential lane changers. Courtesy: get or stay out of the way for lane change desire of
             *                           others. SocioSpeed: get or stay out of the way for desired speed of others. StayRight: incentive for
             *                           trucks to stay on the right-most two lanes, interpreted in line with the route. Class: from a class
             *                           with empty constructor.
             *                         
             * 
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType&gt;
             *   &lt;complexContent&gt;
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
             *       &lt;all&gt;
             *         &lt;element name="Keep" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="SpeedWithCourtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="Courtesy" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="SocioSpeed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="StayRight" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *       &lt;/all&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public static class VoluntaryIncentives
                implements Serializable
            {

                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                private final static long serialVersionUID = 10102L;
                @XmlElement(name = "Keep")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType keep;
                @XmlElement(name = "SpeedWithCourtesy")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType speedWithCourtesy;
                @XmlElement(name = "Courtesy")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType courtesy;
                @XmlElement(name = "SocioSpeed")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType socioSpeed;
                @XmlElement(name = "StayRight")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType stayRight;

                /**
                 * Ruft den Wert der keep-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getKeep() {
                    return keep;
                }

                /**
                 * Legt den Wert der keep-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setKeep(EmptyType value) {
                    this.keep = value;
                }

                /**
                 * Ruft den Wert der speedWithCourtesy-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getSpeedWithCourtesy() {
                    return speedWithCourtesy;
                }

                /**
                 * Legt den Wert der speedWithCourtesy-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setSpeedWithCourtesy(EmptyType value) {
                    this.speedWithCourtesy = value;
                }

                /**
                 * Ruft den Wert der courtesy-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getCourtesy() {
                    return courtesy;
                }

                /**
                 * Legt den Wert der courtesy-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setCourtesy(EmptyType value) {
                    this.courtesy = value;
                }

                /**
                 * Ruft den Wert der socioSpeed-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getSocioSpeed() {
                    return socioSpeed;
                }

                /**
                 * Legt den Wert der socioSpeed-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setSocioSpeed(EmptyType value) {
                    this.socioSpeed = value;
                }

                /**
                 * Ruft den Wert der stayRight-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getStayRight() {
                    return stayRight;
                }

                /**
                 * Legt den Wert der stayRight-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setStayRight(EmptyType value) {
                    this.stayRight = value;
                }

            }

        }

    }

}
