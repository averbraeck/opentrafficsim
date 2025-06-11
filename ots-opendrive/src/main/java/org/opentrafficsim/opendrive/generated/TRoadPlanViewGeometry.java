
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.opendrive.bindings.LengthAdapter;


/**
 * <p>Java-Klasse für t_road_planView_geometry complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_planView_geometry">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <choice>
 *         <element name="line" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_planView_geometry_line"/>
 *         <element name="spiral" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_planView_geometry_spiral"/>
 *         <element name="arc" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_planView_geometry_arc"/>
 *         <element name="poly3" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_planView_geometry_poly3"/>
 *         <element name="paramPoly3" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_planView_geometry_paramPoly3"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </choice>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="x" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="y" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="hdg" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="length" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_planView_geometry", propOrder = {
    "line",
    "spiral",
    "arc",
    "poly3",
    "paramPoly3",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadPlanViewGeometry
    extends OpenDriveElement
{

    protected TRoadPlanViewGeometryLine line;
    protected TRoadPlanViewGeometrySpiral spiral;
    protected TRoadPlanViewGeometryArc arc;
    protected TRoadPlanViewGeometryPoly3 poly3;
    protected TRoadPlanViewGeometryParamPoly3 paramPoly3;
    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     */
    @XmlElements({
        @XmlElement(name = "include", type = TInclude.class),
        @XmlElement(name = "userData", type = TUserData.class),
        @XmlElement(name = "dataQuality", type = TDataQuality.class)
    })
    protected List<Object> gAdditionalData;
    /**
     * s-coordinate of start position
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * Start position (x inertial)
     * 
     */
    @XmlAttribute(name = "x", required = true)
    protected double x;
    /**
     * Start position (y inertial)
     * 
     */
    @XmlAttribute(name = "y", required = true)
    protected double y;
    /**
     * Start orientation (inertial heading)
     * 
     */
    @XmlAttribute(name = "hdg", required = true)
    protected double hdg;
    /**
     * Length of the element's reference line
     * 
     */
    @XmlAttribute(name = "length", required = true)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected Length length;

    /**
     * Ruft den Wert der line-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TRoadPlanViewGeometryLine }
     *     
     */
    public TRoadPlanViewGeometryLine getLine() {
        return line;
    }

    /**
     * Legt den Wert der line-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadPlanViewGeometryLine }
     *     
     */
    public void setLine(TRoadPlanViewGeometryLine value) {
        this.line = value;
    }

    /**
     * Ruft den Wert der spiral-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TRoadPlanViewGeometrySpiral }
     *     
     */
    public TRoadPlanViewGeometrySpiral getSpiral() {
        return spiral;
    }

    /**
     * Legt den Wert der spiral-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadPlanViewGeometrySpiral }
     *     
     */
    public void setSpiral(TRoadPlanViewGeometrySpiral value) {
        this.spiral = value;
    }

    /**
     * Ruft den Wert der arc-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TRoadPlanViewGeometryArc }
     *     
     */
    public TRoadPlanViewGeometryArc getArc() {
        return arc;
    }

    /**
     * Legt den Wert der arc-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadPlanViewGeometryArc }
     *     
     */
    public void setArc(TRoadPlanViewGeometryArc value) {
        this.arc = value;
    }

    /**
     * Ruft den Wert der poly3-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TRoadPlanViewGeometryPoly3 }
     *     
     */
    public TRoadPlanViewGeometryPoly3 getPoly3() {
        return poly3;
    }

    /**
     * Legt den Wert der poly3-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadPlanViewGeometryPoly3 }
     *     
     */
    public void setPoly3(TRoadPlanViewGeometryPoly3 value) {
        this.poly3 = value;
    }

    /**
     * Ruft den Wert der paramPoly3-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TRoadPlanViewGeometryParamPoly3 }
     *     
     */
    public TRoadPlanViewGeometryParamPoly3 getParamPoly3() {
        return paramPoly3;
    }

    /**
     * Legt den Wert der paramPoly3-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadPlanViewGeometryParamPoly3 }
     *     
     */
    public void setParamPoly3(TRoadPlanViewGeometryParamPoly3 value) {
        this.paramPoly3 = value;
    }

    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     * Gets the value of the gAdditionalData property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gAdditionalData property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGAdditionalData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TDataQuality }
     * {@link TInclude }
     * {@link TUserData }
     * </p>
     * 
     * 
     * @return
     *     The value of the gAdditionalData property.
     */
    public List<Object> getGAdditionalData() {
        if (gAdditionalData == null) {
            gAdditionalData = new ArrayList<>();
        }
        return this.gAdditionalData;
    }

    /**
     * s-coordinate of start position
     * 
     */
    public double getS() {
        return s;
    }

    /**
     * Legt den Wert der s-Eigenschaft fest.
     * 
     */
    public void setS(double value) {
        this.s = value;
    }

    /**
     * Start position (x inertial)
     * 
     */
    public double getX() {
        return x;
    }

    /**
     * Legt den Wert der x-Eigenschaft fest.
     * 
     */
    public void setX(double value) {
        this.x = value;
    }

    /**
     * Start position (y inertial)
     * 
     */
    public double getY() {
        return y;
    }

    /**
     * Legt den Wert der y-Eigenschaft fest.
     * 
     */
    public void setY(double value) {
        this.y = value;
    }

    /**
     * Start orientation (inertial heading)
     * 
     */
    public double getHdg() {
        return hdg;
    }

    /**
     * Legt den Wert der hdg-Eigenschaft fest.
     * 
     */
    public void setHdg(double value) {
        this.hdg = value;
    }

    /**
     * Length of the element's reference line
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Length getLength() {
        return length;
    }

    /**
     * Legt den Wert der length-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getLength()
     */
    public void setLength(Length value) {
        this.length = value;
    }

}
