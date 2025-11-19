
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * In OpenDRIVE, a spiral is represented by a &lt;spiral&gt; element within the &lt;geometry&gt; element.
 * 
 * <p>Java class for t_road_planView_geometry_spiral complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_planView_geometry_spiral">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="curvStart" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="curvEnd" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_planView_geometry_spiral")
@SuppressWarnings("all") public class TRoadPlanViewGeometrySpiral
    extends OpenDriveElement
{

    /**
     * Curvature at the start of the element
     * 
     */
    @XmlAttribute(name = "curvStart", required = true)
    protected double curvStart;
    /**
     * Curvature at the end of the element
     * 
     */
    @XmlAttribute(name = "curvEnd", required = true)
    protected double curvEnd;

    /**
     * Curvature at the start of the element
     * 
     */
    public double getCurvStart() {
        return curvStart;
    }

    /**
     * Sets the value of the curvStart property.
     * 
     */
    public void setCurvStart(double value) {
        this.curvStart = value;
    }

    /**
     * Curvature at the end of the element
     * 
     */
    public double getCurvEnd() {
        return curvEnd;
    }

    /**
     * Sets the value of the curvEnd property.
     * 
     */
    public void setCurvEnd(double value) {
        this.curvEnd = value;
    }

}
