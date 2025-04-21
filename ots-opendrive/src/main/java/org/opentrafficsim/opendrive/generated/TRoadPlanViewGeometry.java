
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
 * <p>Java class for t_road_planView_geometry complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Gets the value of the line property.
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
     * Sets the value of the line property.
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
     * Gets the value of the spiral property.
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
     * Sets the value of the spiral property.
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
     * Gets the value of the arc property.
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
     * Sets the value of the arc property.
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
     * Gets the value of the poly3 property.
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
     * Sets the value of the poly3 property.
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
     * Gets the value of the paramPoly3 property.
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
     * Sets the value of the paramPoly3 property.
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
     * Sets the value of the s property.
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
     * Sets the value of the x property.
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
     * Sets the value of the y property.
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
     * Sets the value of the hdg property.
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
     * Sets the value of the length property.
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
