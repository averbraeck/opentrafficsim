
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * In OpenDRIVE, parametric cubic curves are represented by <paramPoly3> elements within the <geometry> element.
 * 
 * <p>Java class for t_road_planView_geometry_paramPoly3 complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_planView_geometry_paramPoly3">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="aU" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="bU" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="cU" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="dU" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="aV" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="bV" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="cV" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="dV" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="pRange" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_paramPoly3_pRange" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_planView_geometry_paramPoly3")
@SuppressWarnings("all") public class TRoadPlanViewGeometryParamPoly3
    extends OpenDriveElement
{

    /**
     * Polynom parameter a for u
     * 
     */
    @XmlAttribute(name = "aU", required = true)
    protected double au;
    /**
     * Polynom parameter b for u
     * 
     */
    @XmlAttribute(name = "bU", required = true)
    protected double bu;
    /**
     * Polynom parameter c for u
     * 
     */
    @XmlAttribute(name = "cU", required = true)
    protected double cu;
    /**
     * Polynom parameter d for u
     * 
     */
    @XmlAttribute(name = "dU", required = true)
    protected double du;
    /**
     * Polynom parameter a for v
     * 
     */
    @XmlAttribute(name = "aV", required = true)
    protected double av;
    /**
     * Polynom parameter b for v
     * 
     */
    @XmlAttribute(name = "bV", required = true)
    protected double bv;
    /**
     * Polynom parameter c for v
     * 
     */
    @XmlAttribute(name = "cV", required = true)
    protected double cv;
    /**
     * Polynom parameter d for v
     * 
     */
    @XmlAttribute(name = "dV", required = true)
    protected double dv;
    /**
     * Range of parameter p. 
     * - Case arcLength: p in [0, @length of <geometry>]
     * - Case normalized: p in [0, 1]
     * 
     */
    @XmlAttribute(name = "pRange", required = true)
    protected EParamPoly3PRange pRange;

    /**
     * Polynom parameter a for u
     * 
     */
    public double getAU() {
        return au;
    }

    /**
     * Sets the value of the au property.
     * 
     */
    public void setAU(double value) {
        this.au = value;
    }

    /**
     * Polynom parameter b for u
     * 
     */
    public double getBU() {
        return bu;
    }

    /**
     * Sets the value of the bu property.
     * 
     */
    public void setBU(double value) {
        this.bu = value;
    }

    /**
     * Polynom parameter c for u
     * 
     */
    public double getCU() {
        return cu;
    }

    /**
     * Sets the value of the cu property.
     * 
     */
    public void setCU(double value) {
        this.cu = value;
    }

    /**
     * Polynom parameter d for u
     * 
     */
    public double getDU() {
        return du;
    }

    /**
     * Sets the value of the du property.
     * 
     */
    public void setDU(double value) {
        this.du = value;
    }

    /**
     * Polynom parameter a for v
     * 
     */
    public double getAV() {
        return av;
    }

    /**
     * Sets the value of the av property.
     * 
     */
    public void setAV(double value) {
        this.av = value;
    }

    /**
     * Polynom parameter b for v
     * 
     */
    public double getBV() {
        return bv;
    }

    /**
     * Sets the value of the bv property.
     * 
     */
    public void setBV(double value) {
        this.bv = value;
    }

    /**
     * Polynom parameter c for v
     * 
     */
    public double getCV() {
        return cv;
    }

    /**
     * Sets the value of the cv property.
     * 
     */
    public void setCV(double value) {
        this.cv = value;
    }

    /**
     * Polynom parameter d for v
     * 
     */
    public double getDV() {
        return dv;
    }

    /**
     * Sets the value of the dv property.
     * 
     */
    public void setDV(double value) {
        this.dv = value;
    }

    /**
     * Range of parameter p. 
     * - Case arcLength: p in [0, @length of <geometry>]
     * - Case normalized: p in [0, 1]
     * 
     * @return
     *     possible object is
     *     {@link EParamPoly3PRange }
     *     
     */
    public EParamPoly3PRange getPRange() {
        return pRange;
    }

    /**
     * Sets the value of the pRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link EParamPoly3PRange }
     *     
     * @see #getPRange()
     */
    public void setPRange(EParamPoly3PRange value) {
        this.pRange = value;
    }

}
