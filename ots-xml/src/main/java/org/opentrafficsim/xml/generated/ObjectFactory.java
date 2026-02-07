
package org.opentrafficsim.xml.generated;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opentrafficsim.xml.generated package. 
 * <p>An ObjectFactory allows you to programmatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
@SuppressWarnings("all") public class ObjectFactory {

    private static final QName _Include_QNAME = new QName("http://www.w3.org/2001/XInclude", "include");
    private static final QName _Fallback_QNAME = new QName("http://www.w3.org/2001/XInclude", "fallback");
    private static final QName _GtuColorersDefault_QNAME = new QName("http://www.opentrafficsim.org/ots", "Default");
    private static final QName _GtuColorersIncentive_QNAME = new QName("http://www.opentrafficsim.org/ots", "Incentive");
    private static final QName _GtuColorersClass_QNAME = new QName("http://www.opentrafficsim.org/ots", "Class");
    private static final QName _LinkClothoidInterpolated_QNAME = new QName("http://www.opentrafficsim.org/ots", "Interpolated");
    private static final QName _LinkClothoidLength_QNAME = new QName("http://www.opentrafficsim.org/ots", "Length");
    private static final QName _LinkClothoidStartCurvature_QNAME = new QName("http://www.opentrafficsim.org/ots", "StartCurvature");
    private static final QName _LinkClothoidEndCurvature_QNAME = new QName("http://www.opentrafficsim.org/ots", "EndCurvature");
    private static final QName _LinkClothoidA_QNAME = new QName("http://www.opentrafficsim.org/ots", "A");
    private static final QName _LinkClothoidFlattener_QNAME = new QName("http://www.opentrafficsim.org/ots", "Flattener");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opentrafficsim.xml.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Control }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Control }
     */
    public org.opentrafficsim.xml.generated.Control createControl() {
        return new org.opentrafficsim.xml.generated.Control();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Od }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Od }
     */
    public org.opentrafficsim.xml.generated.Od createOd() {
        return new org.opentrafficsim.xml.generated.Od();
    }

    /**
     * Create an instance of {@link OdOptions }
     * 
     * @return
     *     the new instance of {@link OdOptions }
     */
    public OdOptions createOdOptions() {
        return new OdOptions();
    }

    /**
     * Create an instance of {@link GtuTemplateMix }
     * 
     * @return
     *     the new instance of {@link GtuTemplateMix }
     */
    public GtuTemplateMix createGtuTemplateMix() {
        return new GtuTemplateMix();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.ShortestRoute }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.ShortestRoute }
     */
    public org.opentrafficsim.xml.generated.ShortestRoute createShortestRoute() {
        return new org.opentrafficsim.xml.generated.ShortestRoute();
    }

    /**
     * Create an instance of {@link RouteMix }
     * 
     * @return
     *     the new instance of {@link RouteMix }
     */
    public RouteMix createRouteMix() {
        return new RouteMix();
    }

    /**
     * Create an instance of {@link ShortestRouteMix }
     * 
     * @return
     *     the new instance of {@link ShortestRouteMix }
     */
    public ShortestRouteMix createShortestRouteMix() {
        return new ShortestRouteMix();
    }

    /**
     * Create an instance of {@link InjectionGenerator }
     * 
     * @return
     *     the new instance of {@link InjectionGenerator }
     */
    public InjectionGenerator createInjectionGenerator() {
        return new InjectionGenerator();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link }
     */
    public org.opentrafficsim.xml.generated.Link createLink() {
        return new org.opentrafficsim.xml.generated.Link();
    }

    /**
     * Create an instance of {@link Network }
     * 
     * @return
     *     the new instance of {@link Network }
     */
    public Network createNetwork() {
        return new Network();
    }

    /**
     * Create an instance of {@link RandomStream }
     * 
     * @return
     *     the new instance of {@link RandomStream }
     */
    public RandomStream createRandomStream() {
        return new RandomStream();
    }

    /**
     * Create an instance of {@link PerceptionType }
     * 
     * @return
     *     the new instance of {@link PerceptionType }
     */
    public PerceptionType createPerceptionType() {
        return new PerceptionType();
    }

    /**
     * Create an instance of {@link PerceptionType.Mental }
     * 
     * @return
     *     the new instance of {@link PerceptionType.Mental }
     */
    public PerceptionType.Mental createPerceptionTypeMental() {
        return new PerceptionType.Mental();
    }

    /**
     * Create an instance of {@link PerceptionType.Mental.Fuller }
     * 
     * @return
     *     the new instance of {@link PerceptionType.Mental.Fuller }
     */
    public PerceptionType.Mental.Fuller createPerceptionTypeMentalFuller() {
        return new PerceptionType.Mental.Fuller();
    }

    /**
     * Create an instance of {@link PerceptionType.HeadwayGtuType }
     * 
     * @return
     *     the new instance of {@link PerceptionType.HeadwayGtuType }
     */
    public PerceptionType.HeadwayGtuType createPerceptionTypeHeadwayGtuType() {
        return new PerceptionType.HeadwayGtuType();
    }

    /**
     * Create an instance of {@link ControlType }
     * 
     * @return
     *     the new instance of {@link ControlType }
     */
    public ControlType createControlType() {
        return new ControlType();
    }

    /**
     * Create an instance of {@link ResponsiveControlType }
     * 
     * @return
     *     the new instance of {@link ResponsiveControlType }
     */
    public ResponsiveControlType createResponsiveControlType() {
        return new ResponsiveControlType();
    }

    /**
     * Create an instance of {@link ResponsiveControlType.Detector }
     * 
     * @return
     *     the new instance of {@link ResponsiveControlType.Detector }
     */
    public ResponsiveControlType.Detector createResponsiveControlTypeDetector() {
        return new ResponsiveControlType.Detector();
    }

    /**
     * Create an instance of {@link ControlType.SignalGroup }
     * 
     * @return
     *     the new instance of {@link ControlType.SignalGroup }
     */
    public ControlType.SignalGroup createControlTypeSignalGroup() {
        return new ControlType.SignalGroup();
    }

    /**
     * Create an instance of {@link ConstantDistType }
     * 
     * @return
     *     the new instance of {@link ConstantDistType }
     */
    public ConstantDistType createConstantDistType() {
        return new ConstantDistType();
    }

    /**
     * Create an instance of {@link DiscreteDistType }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType }
     */
    public DiscreteDistType createDiscreteDistType() {
        return new DiscreteDistType();
    }

    /**
     * Create an instance of {@link ScenarioType }
     * 
     * @return
     *     the new instance of {@link ScenarioType }
     */
    public ScenarioType createScenarioType() {
        return new ScenarioType();
    }

    /**
     * Create an instance of {@link InputParameters }
     * 
     * @return
     *     the new instance of {@link InputParameters }
     */
    public InputParameters createInputParameters() {
        return new InputParameters();
    }

    /**
     * Create an instance of {@link FlattenerType }
     * 
     * @return
     *     the new instance of {@link FlattenerType }
     */
    public FlattenerType createFlattenerType() {
        return new FlattenerType();
    }

    /**
     * Create an instance of {@link ModelType }
     * 
     * @return
     *     the new instance of {@link ModelType }
     */
    public ModelType createModelType() {
        return new ModelType();
    }

    /**
     * Create an instance of {@link ModelType.TacticalPlanner }
     * 
     * @return
     *     the new instance of {@link ModelType.TacticalPlanner }
     */
    public ModelType.TacticalPlanner createModelTypeTacticalPlanner() {
        return new ModelType.TacticalPlanner();
    }

    /**
     * Create an instance of {@link ModelType.TacticalPlanner.Lmrs }
     * 
     * @return
     *     the new instance of {@link ModelType.TacticalPlanner.Lmrs }
     */
    public ModelType.TacticalPlanner.Lmrs createModelTypeTacticalPlannerLmrs() {
        return new ModelType.TacticalPlanner.Lmrs();
    }

    /**
     * Create an instance of {@link ModelType.StrategicalPlanner }
     * 
     * @return
     *     the new instance of {@link ModelType.StrategicalPlanner }
     */
    public ModelType.StrategicalPlanner createModelTypeStrategicalPlanner() {
        return new ModelType.StrategicalPlanner();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters }
     */
    public ModelType.ModelParameters createModelTypeModelParameters() {
        return new ModelType.ModelParameters();
    }

    /**
     * Create an instance of {@link InjectionGenerator.Arrivals }
     * 
     * @return
     *     the new instance of {@link InjectionGenerator.Arrivals }
     */
    public InjectionGenerator.Arrivals createInjectionGeneratorArrivals() {
        return new InjectionGenerator.Arrivals();
    }

    /**
     * Create an instance of {@link OdOptions.OdOptionsItem }
     * 
     * @return
     *     the new instance of {@link OdOptions.OdOptionsItem }
     */
    public OdOptions.OdOptionsItem createOdOptionsOdOptionsItem() {
        return new OdOptions.OdOptionsItem();
    }

    /**
     * Create an instance of {@link OdOptions.OdOptionsItem.LaneBiases }
     * 
     * @return
     *     the new instance of {@link OdOptions.OdOptionsItem.LaneBiases }
     */
    public OdOptions.OdOptionsItem.LaneBiases createOdOptionsOdOptionsItemLaneBiases() {
        return new OdOptions.OdOptionsItem.LaneBiases();
    }

    /**
     * Create an instance of {@link OdOptions.OdOptionsItem.Markov }
     * 
     * @return
     *     the new instance of {@link OdOptions.OdOptionsItem.Markov }
     */
    public OdOptions.OdOptionsItem.Markov createOdOptionsOdOptionsItemMarkov() {
        return new OdOptions.OdOptionsItem.Markov();
    }

    /**
     * Create an instance of {@link GlobalTimeType }
     * 
     * @return
     *     the new instance of {@link GlobalTimeType }
     */
    public GlobalTimeType createGlobalTimeType() {
        return new GlobalTimeType();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod }
     */
    public org.opentrafficsim.xml.generated.Control.TrafCod createControlTrafCod() {
        return new org.opentrafficsim.xml.generated.Control.TrafCod();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod.Console }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod.Console }
     */
    public org.opentrafficsim.xml.generated.Control.TrafCod.Console createControlTrafCodConsole() {
        return new org.opentrafficsim.xml.generated.Control.TrafCod.Console();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Control.FixedTime }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Control.FixedTime }
     */
    public org.opentrafficsim.xml.generated.Control.FixedTime createControlFixedTime() {
        return new org.opentrafficsim.xml.generated.Control.FixedTime();
    }

    /**
     * Create an instance of {@link CseStripe }
     * 
     * @return
     *     the new instance of {@link CseStripe }
     */
    public CseStripe createCseStripe() {
        return new CseStripe();
    }

    /**
     * Create an instance of {@link DashOffset }
     * 
     * @return
     *     the new instance of {@link DashOffset }
     */
    public DashOffset createDashOffset() {
        return new DashOffset();
    }

    /**
     * Create an instance of {@link StripeElements }
     * 
     * @return
     *     the new instance of {@link StripeElements }
     */
    public StripeElements createStripeElements() {
        return new StripeElements();
    }

    /**
     * Create an instance of {@link StripeElements.Line }
     * 
     * @return
     *     the new instance of {@link StripeElements.Line }
     */
    public StripeElements.Line createStripeElementsLine() {
        return new StripeElements.Line();
    }

    /**
     * Create an instance of {@link StripeElements.Line.Dashed }
     * 
     * @return
     *     the new instance of {@link StripeElements.Line.Dashed }
     */
    public StripeElements.Line.Dashed createStripeElementsLineDashed() {
        return new StripeElements.Line.Dashed();
    }

    /**
     * Create an instance of {@link LinkAnimationType }
     * 
     * @return
     *     the new instance of {@link LinkAnimationType }
     */
    public LinkAnimationType createLinkAnimationType() {
        return new LinkAnimationType();
    }

    /**
     * Create an instance of {@link RoadLayoutAnimationType }
     * 
     * @return
     *     the new instance of {@link RoadLayoutAnimationType }
     */
    public RoadLayoutAnimationType createRoadLayoutAnimationType() {
        return new RoadLayoutAnimationType();
    }

    /**
     * Create an instance of {@link DefaultAnimationType }
     * 
     * @return
     *     the new instance of {@link DefaultAnimationType }
     */
    public DefaultAnimationType createDefaultAnimationType() {
        return new DefaultAnimationType();
    }

    /**
     * Create an instance of {@link GtuColorers }
     * 
     * @return
     *     the new instance of {@link GtuColorers }
     */
    public GtuColorers createGtuColorers() {
        return new GtuColorers();
    }

    /**
     * Create an instance of {@link Animation }
     * 
     * @return
     *     the new instance of {@link Animation }
     */
    public Animation createAnimation() {
        return new Animation();
    }

    /**
     * Create an instance of {@link LinkTypeAnimationType }
     * 
     * @return
     *     the new instance of {@link LinkTypeAnimationType }
     */
    public LinkTypeAnimationType createLinkTypeAnimationType() {
        return new LinkTypeAnimationType();
    }

    /**
     * Create an instance of {@link LaneTypeAnimationType }
     * 
     * @return
     *     the new instance of {@link LaneTypeAnimationType }
     */
    public LaneTypeAnimationType createLaneTypeAnimationType() {
        return new LaneTypeAnimationType();
    }

    /**
     * Create an instance of {@link ConnectorAnimationType }
     * 
     * @return
     *     the new instance of {@link ConnectorAnimationType }
     */
    public ConnectorAnimationType createConnectorAnimationType() {
        return new ConnectorAnimationType();
    }

    /**
     * Create an instance of {@link LayerToggleType }
     * 
     * @return
     *     the new instance of {@link LayerToggleType }
     */
    public LayerToggleType createLayerToggleType() {
        return new LayerToggleType();
    }

    /**
     * Create an instance of {@link GtuType }
     * 
     * @return
     *     the new instance of {@link GtuType }
     */
    public GtuType createGtuType() {
        return new GtuType();
    }

    /**
     * Create an instance of {@link HierarchicalType }
     * 
     * @return
     *     the new instance of {@link HierarchicalType }
     */
    public HierarchicalType createHierarchicalType() {
        return new HierarchicalType();
    }

    /**
     * Create an instance of {@link Type }
     * 
     * @return
     *     the new instance of {@link Type }
     */
    public Type createType() {
        return new Type();
    }

    /**
     * Create an instance of {@link GtuTypes }
     * 
     * @return
     *     the new instance of {@link GtuTypes }
     */
    public GtuTypes createGtuTypes() {
        return new GtuTypes();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.GtuTemplate }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.GtuTemplate }
     */
    public org.opentrafficsim.xml.generated.GtuTemplate createGtuTemplate() {
        return new org.opentrafficsim.xml.generated.GtuTemplate();
    }

    /**
     * Create an instance of {@link LengthDistType }
     * 
     * @return
     *     the new instance of {@link LengthDistType }
     */
    public LengthDistType createLengthDistType() {
        return new LengthDistType();
    }

    /**
     * Create an instance of {@link SpeedDistType }
     * 
     * @return
     *     the new instance of {@link SpeedDistType }
     */
    public SpeedDistType createSpeedDistType() {
        return new SpeedDistType();
    }

    /**
     * Create an instance of {@link AccelerationDistType }
     * 
     * @return
     *     the new instance of {@link AccelerationDistType }
     */
    public AccelerationDistType createAccelerationDistType() {
        return new AccelerationDistType();
    }

    /**
     * Create an instance of {@link GtuTemplates }
     * 
     * @return
     *     the new instance of {@link GtuTemplates }
     */
    public GtuTemplates createGtuTemplates() {
        return new GtuTemplates();
    }

    /**
     * Create an instance of {@link Compatibility }
     * 
     * @return
     *     the new instance of {@link Compatibility }
     */
    public Compatibility createCompatibility() {
        return new Compatibility();
    }

    /**
     * Create an instance of {@link LaneType }
     * 
     * @return
     *     the new instance of {@link LaneType }
     */
    public LaneType createLaneType() {
        return new LaneType();
    }

    /**
     * Create an instance of {@link GtuCompatibleInfraType }
     * 
     * @return
     *     the new instance of {@link GtuCompatibleInfraType }
     */
    public GtuCompatibleInfraType createGtuCompatibleInfraType() {
        return new GtuCompatibleInfraType();
    }

    /**
     * Create an instance of {@link LaneTypes }
     * 
     * @return
     *     the new instance of {@link LaneTypes }
     */
    public LaneTypes createLaneTypes() {
        return new LaneTypes();
    }

    /**
     * Create an instance of {@link LaneBias }
     * 
     * @return
     *     the new instance of {@link LaneBias }
     */
    public LaneBias createLaneBias() {
        return new LaneBias();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.LaneBiases }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.LaneBiases }
     */
    public org.opentrafficsim.xml.generated.LaneBiases createLaneBiases() {
        return new org.opentrafficsim.xml.generated.LaneBiases();
    }

    /**
     * Create an instance of {@link LinkType }
     * 
     * @return
     *     the new instance of {@link LinkType }
     */
    public LinkType createLinkType() {
        return new LinkType();
    }

    /**
     * Create an instance of {@link SpeedLimit }
     * 
     * @return
     *     the new instance of {@link SpeedLimit }
     */
    public SpeedLimit createSpeedLimit() {
        return new SpeedLimit();
    }

    /**
     * Create an instance of {@link LinkTypes }
     * 
     * @return
     *     the new instance of {@link LinkTypes }
     */
    public LinkTypes createLinkTypes() {
        return new LinkTypes();
    }

    /**
     * Create an instance of {@link DetectorType }
     * 
     * @return
     *     the new instance of {@link DetectorType }
     */
    public DetectorType createDetectorType() {
        return new DetectorType();
    }

    /**
     * Create an instance of {@link DetectorTypes }
     * 
     * @return
     *     the new instance of {@link DetectorTypes }
     */
    public DetectorTypes createDetectorTypes() {
        return new DetectorTypes();
    }

    /**
     * Create an instance of {@link StripeType }
     * 
     * @return
     *     the new instance of {@link StripeType }
     */
    public StripeType createStripeType() {
        return new StripeType();
    }

    /**
     * Create an instance of {@link StripeTypes }
     * 
     * @return
     *     the new instance of {@link StripeTypes }
     */
    public StripeTypes createStripeTypes() {
        return new StripeTypes();
    }

    /**
     * Create an instance of {@link RoadLayouts }
     * 
     * @return
     *     the new instance of {@link RoadLayouts }
     */
    public RoadLayouts createRoadLayouts() {
        return new RoadLayouts();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.RoadLayout }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.RoadLayout }
     */
    public org.opentrafficsim.xml.generated.RoadLayout createRoadLayout() {
        return new org.opentrafficsim.xml.generated.RoadLayout();
    }

    /**
     * Create an instance of {@link BasicRoadLayout }
     * 
     * @return
     *     the new instance of {@link BasicRoadLayout }
     */
    public BasicRoadLayout createBasicRoadLayout() {
        return new BasicRoadLayout();
    }

    /**
     * Create an instance of {@link CseLane }
     * 
     * @return
     *     the new instance of {@link CseLane }
     */
    public CseLane createCseLane() {
        return new CseLane();
    }

    /**
     * Create an instance of {@link CseShoulder }
     * 
     * @return
     *     the new instance of {@link CseShoulder }
     */
    public CseShoulder createCseShoulder() {
        return new CseShoulder();
    }

    /**
     * Create an instance of {@link ParameterTypes }
     * 
     * @return
     *     the new instance of {@link ParameterTypes }
     */
    public ParameterTypes createParameterTypes() {
        return new ParameterTypes();
    }

    /**
     * Create an instance of {@link ParameterTypeDuration }
     * 
     * @return
     *     the new instance of {@link ParameterTypeDuration }
     */
    public ParameterTypeDuration createParameterTypeDuration() {
        return new ParameterTypeDuration();
    }

    /**
     * Create an instance of {@link ParameterTypeLength }
     * 
     * @return
     *     the new instance of {@link ParameterTypeLength }
     */
    public ParameterTypeLength createParameterTypeLength() {
        return new ParameterTypeLength();
    }

    /**
     * Create an instance of {@link ParameterTypeSpeed }
     * 
     * @return
     *     the new instance of {@link ParameterTypeSpeed }
     */
    public ParameterTypeSpeed createParameterTypeSpeed() {
        return new ParameterTypeSpeed();
    }

    /**
     * Create an instance of {@link ParameterTypeAcceleration }
     * 
     * @return
     *     the new instance of {@link ParameterTypeAcceleration }
     */
    public ParameterTypeAcceleration createParameterTypeAcceleration() {
        return new ParameterTypeAcceleration();
    }

    /**
     * Create an instance of {@link ParameterTypeLinearDensity }
     * 
     * @return
     *     the new instance of {@link ParameterTypeLinearDensity }
     */
    public ParameterTypeLinearDensity createParameterTypeLinearDensity() {
        return new ParameterTypeLinearDensity();
    }

    /**
     * Create an instance of {@link ParameterTypeFrequency }
     * 
     * @return
     *     the new instance of {@link ParameterTypeFrequency }
     */
    public ParameterTypeFrequency createParameterTypeFrequency() {
        return new ParameterTypeFrequency();
    }

    /**
     * Create an instance of {@link ParameterTypeDouble }
     * 
     * @return
     *     the new instance of {@link ParameterTypeDouble }
     */
    public ParameterTypeDouble createParameterTypeDouble() {
        return new ParameterTypeDouble();
    }

    /**
     * Create an instance of {@link ParameterTypeFraction }
     * 
     * @return
     *     the new instance of {@link ParameterTypeFraction }
     */
    public ParameterTypeFraction createParameterTypeFraction() {
        return new ParameterTypeFraction();
    }

    /**
     * Create an instance of {@link ParameterTypeInteger }
     * 
     * @return
     *     the new instance of {@link ParameterTypeInteger }
     */
    public ParameterTypeInteger createParameterTypeInteger() {
        return new ParameterTypeInteger();
    }

    /**
     * Create an instance of {@link ParameterTypeBoolean }
     * 
     * @return
     *     the new instance of {@link ParameterTypeBoolean }
     */
    public ParameterTypeBoolean createParameterTypeBoolean() {
        return new ParameterTypeBoolean();
    }

    /**
     * Create an instance of {@link ParameterTypeString }
     * 
     * @return
     *     the new instance of {@link ParameterTypeString }
     */
    public ParameterTypeString createParameterTypeString() {
        return new ParameterTypeString();
    }

    /**
     * Create an instance of {@link ParameterTypeClass }
     * 
     * @return
     *     the new instance of {@link ParameterTypeClass }
     */
    public ParameterTypeClass createParameterTypeClass() {
        return new ParameterTypeClass();
    }

    /**
     * Create an instance of {@link Definitions }
     * 
     * @return
     *     the new instance of {@link Definitions }
     */
    public Definitions createDefinitions() {
        return new Definitions();
    }

    /**
     * Create an instance of {@link IncludeType }
     * 
     * @return
     *     the new instance of {@link IncludeType }
     */
    public IncludeType createIncludeType() {
        return new IncludeType();
    }

    /**
     * Create an instance of {@link CategoryType }
     * 
     * @return
     *     the new instance of {@link CategoryType }
     */
    public CategoryType createCategoryType() {
        return new CategoryType();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Od.Cell }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Od.Cell }
     */
    public org.opentrafficsim.xml.generated.Od.Cell createOdCell() {
        return new org.opentrafficsim.xml.generated.Od.Cell();
    }

    /**
     * Create an instance of {@link RandomStreamSource }
     * 
     * @return
     *     the new instance of {@link RandomStreamSource }
     */
    public RandomStreamSource createRandomStreamSource() {
        return new RandomStreamSource();
    }

    /**
     * Create an instance of {@link GtuTemplateMix.GtuTemplate }
     * 
     * @return
     *     the new instance of {@link GtuTemplateMix.GtuTemplate }
     */
    public GtuTemplateMix.GtuTemplate createGtuTemplateMixGtuTemplate() {
        return new GtuTemplateMix.GtuTemplate();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Route }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Route }
     */
    public org.opentrafficsim.xml.generated.Route createRoute() {
        return new org.opentrafficsim.xml.generated.Route();
    }

    /**
     * Create an instance of {@link EmptyType }
     * 
     * @return
     *     the new instance of {@link EmptyType }
     */
    public EmptyType createEmptyType() {
        return new EmptyType();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.ShortestRoute.DistanceAndFreeFlowTime }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.ShortestRoute.DistanceAndFreeFlowTime }
     */
    public org.opentrafficsim.xml.generated.ShortestRoute.DistanceAndFreeFlowTime createShortestRouteDistanceAndFreeFlowTime() {
        return new org.opentrafficsim.xml.generated.ShortestRoute.DistanceAndFreeFlowTime();
    }

    /**
     * Create an instance of {@link RouteMix.Route }
     * 
     * @return
     *     the new instance of {@link RouteMix.Route }
     */
    public RouteMix.Route createRouteMixRoute() {
        return new RouteMix.Route();
    }

    /**
     * Create an instance of {@link ShortestRouteMix.ShortestRoute }
     * 
     * @return
     *     the new instance of {@link ShortestRouteMix.ShortestRoute }
     */
    public ShortestRouteMix.ShortestRoute createShortestRouteMixShortestRoute() {
        return new ShortestRouteMix.ShortestRoute();
    }

    /**
     * Create an instance of {@link Generator }
     * 
     * @return
     *     the new instance of {@link Generator }
     */
    public Generator createGenerator() {
        return new Generator();
    }

    /**
     * Create an instance of {@link RoomCheckerType }
     * 
     * @return
     *     the new instance of {@link RoomCheckerType }
     */
    public RoomCheckerType createRoomCheckerType() {
        return new RoomCheckerType();
    }

    /**
     * Create an instance of {@link InjectionGenerator.Position }
     * 
     * @return
     *     the new instance of {@link InjectionGenerator.Position }
     */
    public InjectionGenerator.Position createInjectionGeneratorPosition() {
        return new InjectionGenerator.Position();
    }

    /**
     * Create an instance of {@link InjectionGenerator.GtuCharacteristics }
     * 
     * @return
     *     the new instance of {@link InjectionGenerator.GtuCharacteristics }
     */
    public InjectionGenerator.GtuCharacteristics createInjectionGeneratorGtuCharacteristics() {
        return new InjectionGenerator.GtuCharacteristics();
    }

    /**
     * Create an instance of {@link Sink }
     * 
     * @return
     *     the new instance of {@link Sink }
     */
    public Sink createSink() {
        return new Sink();
    }

    /**
     * Create an instance of {@link Demand }
     * 
     * @return
     *     the new instance of {@link Demand }
     */
    public Demand createDemand() {
        return new Demand();
    }

    /**
     * Create an instance of {@link ModelIdReferralType }
     * 
     * @return
     *     the new instance of {@link ModelIdReferralType }
     */
    public ModelIdReferralType createModelIdReferralType() {
        return new ModelIdReferralType();
    }

    /**
     * Create an instance of {@link Models }
     * 
     * @return
     *     the new instance of {@link Models }
     */
    public Models createModels() {
        return new Models();
    }

    /**
     * Create an instance of {@link Centroid }
     * 
     * @return
     *     the new instance of {@link Centroid }
     */
    public Centroid createCentroid() {
        return new Centroid();
    }

    /**
     * Create an instance of {@link Node }
     * 
     * @return
     *     the new instance of {@link Node }
     */
    public Node createNode() {
        return new Node();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Connector }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Connector }
     */
    public org.opentrafficsim.xml.generated.Connector createConnector() {
        return new org.opentrafficsim.xml.generated.Connector();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link.Bezier }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link.Bezier }
     */
    public org.opentrafficsim.xml.generated.Link.Bezier createLinkBezier() {
        return new org.opentrafficsim.xml.generated.Link.Bezier();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link.Clothoid }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link.Clothoid }
     */
    public org.opentrafficsim.xml.generated.Link.Clothoid createLinkClothoid() {
        return new org.opentrafficsim.xml.generated.Link.Clothoid();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link.Arc }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link.Arc }
     */
    public org.opentrafficsim.xml.generated.Link.Arc createLinkArc() {
        return new org.opentrafficsim.xml.generated.Link.Arc();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link.Polyline }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link.Polyline }
     */
    public org.opentrafficsim.xml.generated.Link.Polyline createLinkPolyline() {
        return new org.opentrafficsim.xml.generated.Link.Polyline();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link.RoadLayout }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link.RoadLayout }
     */
    public org.opentrafficsim.xml.generated.Link.RoadLayout createLinkRoadLayout() {
        return new org.opentrafficsim.xml.generated.Link.RoadLayout();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link.LaneOverride }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link.LaneOverride }
     */
    public org.opentrafficsim.xml.generated.Link.LaneOverride createLinkLaneOverride() {
        return new org.opentrafficsim.xml.generated.Link.LaneOverride();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Link.StripeOverride }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Link.StripeOverride }
     */
    public org.opentrafficsim.xml.generated.Link.StripeOverride createLinkStripeOverride() {
        return new org.opentrafficsim.xml.generated.Link.StripeOverride();
    }

    /**
     * Create an instance of {@link TrafficLightType }
     * 
     * @return
     *     the new instance of {@link TrafficLightType }
     */
    public TrafficLightType createTrafficLightType() {
        return new TrafficLightType();
    }

    /**
     * Create an instance of {@link TrafficLightDetectorType }
     * 
     * @return
     *     the new instance of {@link TrafficLightDetectorType }
     */
    public TrafficLightDetectorType createTrafficLightDetectorType() {
        return new TrafficLightDetectorType();
    }

    /**
     * Create an instance of {@link Network.Conflicts }
     * 
     * @return
     *     the new instance of {@link Network.Conflicts }
     */
    public Network.Conflicts createNetworkConflicts() {
        return new Network.Conflicts();
    }

    /**
     * Create an instance of {@link RandomStream.Replication }
     * 
     * @return
     *     the new instance of {@link RandomStream.Replication }
     */
    public RandomStream.Replication createRandomStreamReplication() {
        return new RandomStream.Replication();
    }

    /**
     * Create an instance of {@link RandomStreams }
     * 
     * @return
     *     the new instance of {@link RandomStreams }
     */
    public RandomStreams createRandomStreams() {
        return new RandomStreams();
    }

    /**
     * Create an instance of {@link Run }
     * 
     * @return
     *     the new instance of {@link Run }
     */
    public Run createRun() {
        return new Run();
    }

    /**
     * Create an instance of {@link Scenarios }
     * 
     * @return
     *     the new instance of {@link Scenarios }
     */
    public Scenarios createScenarios() {
        return new Scenarios();
    }

    /**
     * Create an instance of {@link Ots }
     * 
     * @return
     *     the new instance of {@link Ots }
     */
    public Ots createOts() {
        return new Ots();
    }

    /**
     * Create an instance of {@link LaneLinkType }
     * 
     * @return
     *     the new instance of {@link LaneLinkType }
     */
    public LaneLinkType createLaneLinkType() {
        return new LaneLinkType();
    }

    /**
     * Create an instance of {@link PositionDistType }
     * 
     * @return
     *     the new instance of {@link PositionDistType }
     */
    public PositionDistType createPositionDistType() {
        return new PositionDistType();
    }

    /**
     * Create an instance of {@link TimeDistType }
     * 
     * @return
     *     the new instance of {@link TimeDistType }
     */
    public TimeDistType createTimeDistType() {
        return new TimeDistType();
    }

    /**
     * Create an instance of {@link DurationDistType }
     * 
     * @return
     *     the new instance of {@link DurationDistType }
     */
    public DurationDistType createDurationDistType() {
        return new DurationDistType();
    }

    /**
     * Create an instance of {@link LinearDensityDistType }
     * 
     * @return
     *     the new instance of {@link LinearDensityDistType }
     */
    public LinearDensityDistType createLinearDensityDistType() {
        return new LinearDensityDistType();
    }

    /**
     * Create an instance of {@link FrequencyDistType }
     * 
     * @return
     *     the new instance of {@link FrequencyDistType }
     */
    public FrequencyDistType createFrequencyDistType() {
        return new FrequencyDistType();
    }

    /**
     * Create an instance of {@link ParameterType }
     * 
     * @return
     *     the new instance of {@link ParameterType }
     */
    public ParameterType createParameterType() {
        return new ParameterType();
    }

    /**
     * Create an instance of {@link ParameterTypeFloat }
     * 
     * @return
     *     the new instance of {@link ParameterTypeFloat }
     */
    public ParameterTypeFloat createParameterTypeFloat() {
        return new ParameterTypeFloat();
    }

    /**
     * Create an instance of {@link ParameterTypeLong }
     * 
     * @return
     *     the new instance of {@link ParameterTypeLong }
     */
    public ParameterTypeLong createParameterTypeLong() {
        return new ParameterTypeLong();
    }

    /**
     * Create an instance of {@link CrossSectionElement }
     * 
     * @return
     *     the new instance of {@link CrossSectionElement }
     */
    public CrossSectionElement createCrossSectionElement() {
        return new CrossSectionElement();
    }

    /**
     * Create an instance of {@link StripeCompatibility }
     * 
     * @return
     *     the new instance of {@link StripeCompatibility }
     */
    public StripeCompatibility createStripeCompatibility() {
        return new StripeCompatibility();
    }

    /**
     * Create an instance of {@link TrafCodCoordinatesType }
     * 
     * @return
     *     the new instance of {@link TrafCodCoordinatesType }
     */
    public TrafCodCoordinatesType createTrafCodCoordinatesType() {
        return new TrafCodCoordinatesType();
    }

    /**
     * Create an instance of {@link LevelTimeType }
     * 
     * @return
     *     the new instance of {@link LevelTimeType }
     */
    public LevelTimeType createLevelTimeType() {
        return new LevelTimeType();
    }

    /**
     * Create an instance of {@link CorrelationParameterType }
     * 
     * @return
     *     the new instance of {@link CorrelationParameterType }
     */
    public CorrelationParameterType createCorrelationParameterType() {
        return new CorrelationParameterType();
    }

    /**
     * Create an instance of {@link CarFollowingModelType }
     * 
     * @return
     *     the new instance of {@link CarFollowingModelType }
     */
    public CarFollowingModelType createCarFollowingModelType() {
        return new CarFollowingModelType();
    }

    /**
     * Create an instance of {@link CarFollowingModelHeadwaySpeedType }
     * 
     * @return
     *     the new instance of {@link CarFollowingModelHeadwaySpeedType }
     */
    public CarFollowingModelHeadwaySpeedType createCarFollowingModelHeadwaySpeedType() {
        return new CarFollowingModelHeadwaySpeedType();
    }

    /**
     * Create an instance of {@link DesiredHeadwayModelType }
     * 
     * @return
     *     the new instance of {@link DesiredHeadwayModelType }
     */
    public DesiredHeadwayModelType createDesiredHeadwayModelType() {
        return new DesiredHeadwayModelType();
    }

    /**
     * Create an instance of {@link DesiredSpeedModelType }
     * 
     * @return
     *     the new instance of {@link DesiredSpeedModelType }
     */
    public DesiredSpeedModelType createDesiredSpeedModelType() {
        return new DesiredSpeedModelType();
    }

    /**
     * Create an instance of {@link FallbackType }
     * 
     * @return
     *     the new instance of {@link FallbackType }
     */
    public FallbackType createFallbackType() {
        return new FallbackType();
    }

    /**
     * Create an instance of {@link PerceptionType.Categories }
     * 
     * @return
     *     the new instance of {@link PerceptionType.Categories }
     */
    public PerceptionType.Categories createPerceptionTypeCategories() {
        return new PerceptionType.Categories();
    }

    /**
     * Create an instance of {@link PerceptionType.Mental.Fuller.BehavioralAdaptations }
     * 
     * @return
     *     the new instance of {@link PerceptionType.Mental.Fuller.BehavioralAdaptations }
     */
    public PerceptionType.Mental.Fuller.BehavioralAdaptations createPerceptionTypeMentalFullerBehavioralAdaptations() {
        return new PerceptionType.Mental.Fuller.BehavioralAdaptations();
    }

    /**
     * Create an instance of {@link PerceptionType.HeadwayGtuType.Perceived }
     * 
     * @return
     *     the new instance of {@link PerceptionType.HeadwayGtuType.Perceived }
     */
    public PerceptionType.HeadwayGtuType.Perceived createPerceptionTypeHeadwayGtuTypePerceived() {
        return new PerceptionType.HeadwayGtuType.Perceived();
    }

    /**
     * Create an instance of {@link ResponsiveControlType.Detector.MultipleLane }
     * 
     * @return
     *     the new instance of {@link ResponsiveControlType.Detector.MultipleLane }
     */
    public ResponsiveControlType.Detector.MultipleLane createResponsiveControlTypeDetectorMultipleLane() {
        return new ResponsiveControlType.Detector.MultipleLane();
    }

    /**
     * Create an instance of {@link ResponsiveControlType.Detector.SingleLane }
     * 
     * @return
     *     the new instance of {@link ResponsiveControlType.Detector.SingleLane }
     */
    public ResponsiveControlType.Detector.SingleLane createResponsiveControlTypeDetectorSingleLane() {
        return new ResponsiveControlType.Detector.SingleLane();
    }

    /**
     * Create an instance of {@link ControlType.SignalGroup.TrafficLight }
     * 
     * @return
     *     the new instance of {@link ControlType.SignalGroup.TrafficLight }
     */
    public ControlType.SignalGroup.TrafficLight createControlTypeSignalGroupTrafficLight() {
        return new ControlType.SignalGroup.TrafficLight();
    }

    /**
     * Create an instance of {@link ConstantDistType.Constant }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Constant }
     */
    public ConstantDistType.Constant createConstantDistTypeConstant() {
        return new ConstantDistType.Constant();
    }

    /**
     * Create an instance of {@link ConstantDistType.Exponential }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Exponential }
     */
    public ConstantDistType.Exponential createConstantDistTypeExponential() {
        return new ConstantDistType.Exponential();
    }

    /**
     * Create an instance of {@link ConstantDistType.Triangular }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Triangular }
     */
    public ConstantDistType.Triangular createConstantDistTypeTriangular() {
        return new ConstantDistType.Triangular();
    }

    /**
     * Create an instance of {@link ConstantDistType.Normal }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Normal }
     */
    public ConstantDistType.Normal createConstantDistTypeNormal() {
        return new ConstantDistType.Normal();
    }

    /**
     * Create an instance of {@link ConstantDistType.NormalTrunc }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.NormalTrunc }
     */
    public ConstantDistType.NormalTrunc createConstantDistTypeNormalTrunc() {
        return new ConstantDistType.NormalTrunc();
    }

    /**
     * Create an instance of {@link ConstantDistType.Beta }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Beta }
     */
    public ConstantDistType.Beta createConstantDistTypeBeta() {
        return new ConstantDistType.Beta();
    }

    /**
     * Create an instance of {@link ConstantDistType.Erlang }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Erlang }
     */
    public ConstantDistType.Erlang createConstantDistTypeErlang() {
        return new ConstantDistType.Erlang();
    }

    /**
     * Create an instance of {@link ConstantDistType.Gamma }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Gamma }
     */
    public ConstantDistType.Gamma createConstantDistTypeGamma() {
        return new ConstantDistType.Gamma();
    }

    /**
     * Create an instance of {@link ConstantDistType.LogNormal }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.LogNormal }
     */
    public ConstantDistType.LogNormal createConstantDistTypeLogNormal() {
        return new ConstantDistType.LogNormal();
    }

    /**
     * Create an instance of {@link ConstantDistType.LogNormalTrunc }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.LogNormalTrunc }
     */
    public ConstantDistType.LogNormalTrunc createConstantDistTypeLogNormalTrunc() {
        return new ConstantDistType.LogNormalTrunc();
    }

    /**
     * Create an instance of {@link ConstantDistType.Pearson5 }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Pearson5 }
     */
    public ConstantDistType.Pearson5 createConstantDistTypePearson5() {
        return new ConstantDistType.Pearson5();
    }

    /**
     * Create an instance of {@link ConstantDistType.Pearson6 }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Pearson6 }
     */
    public ConstantDistType.Pearson6 createConstantDistTypePearson6() {
        return new ConstantDistType.Pearson6();
    }

    /**
     * Create an instance of {@link ConstantDistType.Uniform }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Uniform }
     */
    public ConstantDistType.Uniform createConstantDistTypeUniform() {
        return new ConstantDistType.Uniform();
    }

    /**
     * Create an instance of {@link ConstantDistType.Weibull }
     * 
     * @return
     *     the new instance of {@link ConstantDistType.Weibull }
     */
    public ConstantDistType.Weibull createConstantDistTypeWeibull() {
        return new ConstantDistType.Weibull();
    }

    /**
     * Create an instance of {@link DiscreteDistType.Constant }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType.Constant }
     */
    public DiscreteDistType.Constant createDiscreteDistTypeConstant() {
        return new DiscreteDistType.Constant();
    }

    /**
     * Create an instance of {@link DiscreteDistType.BernoulliI }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType.BernoulliI }
     */
    public DiscreteDistType.BernoulliI createDiscreteDistTypeBernoulliI() {
        return new DiscreteDistType.BernoulliI();
    }

    /**
     * Create an instance of {@link DiscreteDistType.Binomial }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType.Binomial }
     */
    public DiscreteDistType.Binomial createDiscreteDistTypeBinomial() {
        return new DiscreteDistType.Binomial();
    }

    /**
     * Create an instance of {@link DiscreteDistType.Uniform }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType.Uniform }
     */
    public DiscreteDistType.Uniform createDiscreteDistTypeUniform() {
        return new DiscreteDistType.Uniform();
    }

    /**
     * Create an instance of {@link DiscreteDistType.Geometric }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType.Geometric }
     */
    public DiscreteDistType.Geometric createDiscreteDistTypeGeometric() {
        return new DiscreteDistType.Geometric();
    }

    /**
     * Create an instance of {@link DiscreteDistType.NegBinomial }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType.NegBinomial }
     */
    public DiscreteDistType.NegBinomial createDiscreteDistTypeNegBinomial() {
        return new DiscreteDistType.NegBinomial();
    }

    /**
     * Create an instance of {@link DiscreteDistType.Poisson }
     * 
     * @return
     *     the new instance of {@link DiscreteDistType.Poisson }
     */
    public DiscreteDistType.Poisson createDiscreteDistTypePoisson() {
        return new DiscreteDistType.Poisson();
    }

    /**
     * Create an instance of {@link ScenarioType.Od }
     * 
     * @return
     *     the new instance of {@link ScenarioType.Od }
     */
    public ScenarioType.Od createScenarioTypeOd() {
        return new ScenarioType.Od();
    }

    /**
     * Create an instance of {@link ScenarioType.Control }
     * 
     * @return
     *     the new instance of {@link ScenarioType.Control }
     */
    public ScenarioType.Control createScenarioTypeControl() {
        return new ScenarioType.Control();
    }

    /**
     * Create an instance of {@link InputParameters.Duration }
     * 
     * @return
     *     the new instance of {@link InputParameters.Duration }
     */
    public InputParameters.Duration createInputParametersDuration() {
        return new InputParameters.Duration();
    }

    /**
     * Create an instance of {@link InputParameters.Length }
     * 
     * @return
     *     the new instance of {@link InputParameters.Length }
     */
    public InputParameters.Length createInputParametersLength() {
        return new InputParameters.Length();
    }

    /**
     * Create an instance of {@link InputParameters.Speed }
     * 
     * @return
     *     the new instance of {@link InputParameters.Speed }
     */
    public InputParameters.Speed createInputParametersSpeed() {
        return new InputParameters.Speed();
    }

    /**
     * Create an instance of {@link InputParameters.Acceleration }
     * 
     * @return
     *     the new instance of {@link InputParameters.Acceleration }
     */
    public InputParameters.Acceleration createInputParametersAcceleration() {
        return new InputParameters.Acceleration();
    }

    /**
     * Create an instance of {@link InputParameters.LinearDensity }
     * 
     * @return
     *     the new instance of {@link InputParameters.LinearDensity }
     */
    public InputParameters.LinearDensity createInputParametersLinearDensity() {
        return new InputParameters.LinearDensity();
    }

    /**
     * Create an instance of {@link InputParameters.Frequency }
     * 
     * @return
     *     the new instance of {@link InputParameters.Frequency }
     */
    public InputParameters.Frequency createInputParametersFrequency() {
        return new InputParameters.Frequency();
    }

    /**
     * Create an instance of {@link InputParameters.Double }
     * 
     * @return
     *     the new instance of {@link InputParameters.Double }
     */
    public InputParameters.Double createInputParametersDouble() {
        return new InputParameters.Double();
    }

    /**
     * Create an instance of {@link InputParameters.Fraction }
     * 
     * @return
     *     the new instance of {@link InputParameters.Fraction }
     */
    public InputParameters.Fraction createInputParametersFraction() {
        return new InputParameters.Fraction();
    }

    /**
     * Create an instance of {@link InputParameters.Integer }
     * 
     * @return
     *     the new instance of {@link InputParameters.Integer }
     */
    public InputParameters.Integer createInputParametersInteger() {
        return new InputParameters.Integer();
    }

    /**
     * Create an instance of {@link InputParameters.Boolean }
     * 
     * @return
     *     the new instance of {@link InputParameters.Boolean }
     */
    public InputParameters.Boolean createInputParametersBoolean() {
        return new InputParameters.Boolean();
    }

    /**
     * Create an instance of {@link InputParameters.String }
     * 
     * @return
     *     the new instance of {@link InputParameters.String }
     */
    public InputParameters.String createInputParametersString() {
        return new InputParameters.String();
    }

    /**
     * Create an instance of {@link InputParameters.Class }
     * 
     * @return
     *     the new instance of {@link InputParameters.Class }
     */
    public InputParameters.Class createInputParametersClass() {
        return new InputParameters.Class();
    }

    /**
     * Create an instance of {@link FlattenerType.DeviationAndAngle }
     * 
     * @return
     *     the new instance of {@link FlattenerType.DeviationAndAngle }
     */
    public FlattenerType.DeviationAndAngle createFlattenerTypeDeviationAndAngle() {
        return new FlattenerType.DeviationAndAngle();
    }

    /**
     * Create an instance of {@link ModelType.TacticalPlanner.Lmrs.MandatoryIncentives }
     * 
     * @return
     *     the new instance of {@link ModelType.TacticalPlanner.Lmrs.MandatoryIncentives }
     */
    public ModelType.TacticalPlanner.Lmrs.MandatoryIncentives createModelTypeTacticalPlannerLmrsMandatoryIncentives() {
        return new ModelType.TacticalPlanner.Lmrs.MandatoryIncentives();
    }

    /**
     * Create an instance of {@link ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives }
     * 
     * @return
     *     the new instance of {@link ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives }
     */
    public ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives createModelTypeTacticalPlannerLmrsVoluntaryIncentives() {
        return new ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives();
    }

    /**
     * Create an instance of {@link ModelType.TacticalPlanner.Lmrs.AccelerationIncentives }
     * 
     * @return
     *     the new instance of {@link ModelType.TacticalPlanner.Lmrs.AccelerationIncentives }
     */
    public ModelType.TacticalPlanner.Lmrs.AccelerationIncentives createModelTypeTacticalPlannerLmrsAccelerationIncentives() {
        return new ModelType.TacticalPlanner.Lmrs.AccelerationIncentives();
    }

    /**
     * Create an instance of {@link ModelType.StrategicalPlanner.Route }
     * 
     * @return
     *     the new instance of {@link ModelType.StrategicalPlanner.Route }
     */
    public ModelType.StrategicalPlanner.Route createModelTypeStrategicalPlannerRoute() {
        return new ModelType.StrategicalPlanner.Route();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Duration }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Duration }
     */
    public ModelType.ModelParameters.Duration createModelTypeModelParametersDuration() {
        return new ModelType.ModelParameters.Duration();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.DurationDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.DurationDist }
     */
    public ModelType.ModelParameters.DurationDist createModelTypeModelParametersDurationDist() {
        return new ModelType.ModelParameters.DurationDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Length }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Length }
     */
    public ModelType.ModelParameters.Length createModelTypeModelParametersLength() {
        return new ModelType.ModelParameters.Length();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.LengthDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.LengthDist }
     */
    public ModelType.ModelParameters.LengthDist createModelTypeModelParametersLengthDist() {
        return new ModelType.ModelParameters.LengthDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Speed }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Speed }
     */
    public ModelType.ModelParameters.Speed createModelTypeModelParametersSpeed() {
        return new ModelType.ModelParameters.Speed();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.SpeedDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.SpeedDist }
     */
    public ModelType.ModelParameters.SpeedDist createModelTypeModelParametersSpeedDist() {
        return new ModelType.ModelParameters.SpeedDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Acceleration }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Acceleration }
     */
    public ModelType.ModelParameters.Acceleration createModelTypeModelParametersAcceleration() {
        return new ModelType.ModelParameters.Acceleration();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.AccelerationDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.AccelerationDist }
     */
    public ModelType.ModelParameters.AccelerationDist createModelTypeModelParametersAccelerationDist() {
        return new ModelType.ModelParameters.AccelerationDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.LinearDensity }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.LinearDensity }
     */
    public ModelType.ModelParameters.LinearDensity createModelTypeModelParametersLinearDensity() {
        return new ModelType.ModelParameters.LinearDensity();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.LinearDensityDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.LinearDensityDist }
     */
    public ModelType.ModelParameters.LinearDensityDist createModelTypeModelParametersLinearDensityDist() {
        return new ModelType.ModelParameters.LinearDensityDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Frequency }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Frequency }
     */
    public ModelType.ModelParameters.Frequency createModelTypeModelParametersFrequency() {
        return new ModelType.ModelParameters.Frequency();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.FrequencyDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.FrequencyDist }
     */
    public ModelType.ModelParameters.FrequencyDist createModelTypeModelParametersFrequencyDist() {
        return new ModelType.ModelParameters.FrequencyDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Double }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Double }
     */
    public ModelType.ModelParameters.Double createModelTypeModelParametersDouble() {
        return new ModelType.ModelParameters.Double();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.DoubleDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.DoubleDist }
     */
    public ModelType.ModelParameters.DoubleDist createModelTypeModelParametersDoubleDist() {
        return new ModelType.ModelParameters.DoubleDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Fraction }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Fraction }
     */
    public ModelType.ModelParameters.Fraction createModelTypeModelParametersFraction() {
        return new ModelType.ModelParameters.Fraction();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Integer }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Integer }
     */
    public ModelType.ModelParameters.Integer createModelTypeModelParametersInteger() {
        return new ModelType.ModelParameters.Integer();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.IntegerDist }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.IntegerDist }
     */
    public ModelType.ModelParameters.IntegerDist createModelTypeModelParametersIntegerDist() {
        return new ModelType.ModelParameters.IntegerDist();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Boolean }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Boolean }
     */
    public ModelType.ModelParameters.Boolean createModelTypeModelParametersBoolean() {
        return new ModelType.ModelParameters.Boolean();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.String }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.String }
     */
    public ModelType.ModelParameters.String createModelTypeModelParametersString() {
        return new ModelType.ModelParameters.String();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Class }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Class }
     */
    public ModelType.ModelParameters.Class createModelTypeModelParametersClass() {
        return new ModelType.ModelParameters.Class();
    }

    /**
     * Create an instance of {@link ModelType.ModelParameters.Correlation }
     * 
     * @return
     *     the new instance of {@link ModelType.ModelParameters.Correlation }
     */
    public ModelType.ModelParameters.Correlation createModelTypeModelParametersCorrelation() {
        return new ModelType.ModelParameters.Correlation();
    }

    /**
     * Create an instance of {@link InjectionGenerator.Arrivals.Arrival }
     * 
     * @return
     *     the new instance of {@link InjectionGenerator.Arrivals.Arrival }
     */
    public InjectionGenerator.Arrivals.Arrival createInjectionGeneratorArrivalsArrival() {
        return new InjectionGenerator.Arrivals.Arrival();
    }

    /**
     * Create an instance of {@link OdOptions.OdOptionsItem.DefaultModel }
     * 
     * @return
     *     the new instance of {@link OdOptions.OdOptionsItem.DefaultModel }
     */
    public OdOptions.OdOptionsItem.DefaultModel createOdOptionsOdOptionsItemDefaultModel() {
        return new OdOptions.OdOptionsItem.DefaultModel();
    }

    /**
     * Create an instance of {@link OdOptions.OdOptionsItem.Model }
     * 
     * @return
     *     the new instance of {@link OdOptions.OdOptionsItem.Model }
     */
    public OdOptions.OdOptionsItem.Model createOdOptionsOdOptionsItemModel() {
        return new OdOptions.OdOptionsItem.Model();
    }

    /**
     * Create an instance of {@link OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias }
     * 
     * @return
     *     the new instance of {@link OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias }
     */
    public OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias createOdOptionsOdOptionsItemLaneBiasesDefinedLaneBias() {
        return new OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias();
    }

    /**
     * Create an instance of {@link OdOptions.OdOptionsItem.Markov.State }
     * 
     * @return
     *     the new instance of {@link OdOptions.OdOptionsItem.Markov.State }
     */
    public OdOptions.OdOptionsItem.Markov.State createOdOptionsOdOptionsItemMarkovState() {
        return new OdOptions.OdOptionsItem.Markov.State();
    }

    /**
     * Create an instance of {@link GlobalTimeType.Time }
     * 
     * @return
     *     the new instance of {@link GlobalTimeType.Time }
     */
    public GlobalTimeType.Time createGlobalTimeTypeTime() {
        return new GlobalTimeType.Time();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod.Program }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod.Program }
     */
    public org.opentrafficsim.xml.generated.Control.TrafCod.Program createControlTrafCodProgram() {
        return new org.opentrafficsim.xml.generated.Control.TrafCod.Program();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod.Console.Map }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Control.TrafCod.Console.Map }
     */
    public org.opentrafficsim.xml.generated.Control.TrafCod.Console.Map createControlTrafCodConsoleMap() {
        return new org.opentrafficsim.xml.generated.Control.TrafCod.Console.Map();
    }

    /**
     * Create an instance of {@link org.opentrafficsim.xml.generated.Control.FixedTime.Cycle }
     * 
     * @return
     *     the new instance of {@link org.opentrafficsim.xml.generated.Control.FixedTime.Cycle }
     */
    public org.opentrafficsim.xml.generated.Control.FixedTime.Cycle createControlFixedTimeCycle() {
        return new org.opentrafficsim.xml.generated.Control.FixedTime.Cycle();
    }

    /**
     * Create an instance of {@link CseStripe.Custom }
     * 
     * @return
     *     the new instance of {@link CseStripe.Custom }
     */
    public CseStripe.Custom createCseStripeCustom() {
        return new CseStripe.Custom();
    }

    /**
     * Create an instance of {@link DashOffset.Fixed }
     * 
     * @return
     *     the new instance of {@link DashOffset.Fixed }
     */
    public DashOffset.Fixed createDashOffsetFixed() {
        return new DashOffset.Fixed();
    }

    /**
     * Create an instance of {@link StripeElements.Gap }
     * 
     * @return
     *     the new instance of {@link StripeElements.Gap }
     */
    public StripeElements.Gap createStripeElementsGap() {
        return new StripeElements.Gap();
    }

    /**
     * Create an instance of {@link StripeElements.Line.Dashed.GapDash }
     * 
     * @return
     *     the new instance of {@link StripeElements.Line.Dashed.GapDash }
     */
    public StripeElements.Line.Dashed.GapDash createStripeElementsLineDashedGapDash() {
        return new StripeElements.Line.Dashed.GapDash();
    }

    /**
     * Create an instance of {@link LinkAnimationType.Stripe }
     * 
     * @return
     *     the new instance of {@link LinkAnimationType.Stripe }
     */
    public LinkAnimationType.Stripe createLinkAnimationTypeStripe() {
        return new LinkAnimationType.Stripe();
    }

    /**
     * Create an instance of {@link LinkAnimationType.Lane }
     * 
     * @return
     *     the new instance of {@link LinkAnimationType.Lane }
     */
    public LinkAnimationType.Lane createLinkAnimationTypeLane() {
        return new LinkAnimationType.Lane();
    }

    /**
     * Create an instance of {@link LinkAnimationType.Shoulder }
     * 
     * @return
     *     the new instance of {@link LinkAnimationType.Shoulder }
     */
    public LinkAnimationType.Shoulder createLinkAnimationTypeShoulder() {
        return new LinkAnimationType.Shoulder();
    }

    /**
     * Create an instance of {@link RoadLayoutAnimationType.Stripe }
     * 
     * @return
     *     the new instance of {@link RoadLayoutAnimationType.Stripe }
     */
    public RoadLayoutAnimationType.Stripe createRoadLayoutAnimationTypeStripe() {
        return new RoadLayoutAnimationType.Stripe();
    }

    /**
     * Create an instance of {@link RoadLayoutAnimationType.Lane }
     * 
     * @return
     *     the new instance of {@link RoadLayoutAnimationType.Lane }
     */
    public RoadLayoutAnimationType.Lane createRoadLayoutAnimationTypeLane() {
        return new RoadLayoutAnimationType.Lane();
    }

    /**
     * Create an instance of {@link RoadLayoutAnimationType.Shoulder }
     * 
     * @return
     *     the new instance of {@link RoadLayoutAnimationType.Shoulder }
     */
    public RoadLayoutAnimationType.Shoulder createRoadLayoutAnimationTypeShoulder() {
        return new RoadLayoutAnimationType.Shoulder();
    }

    /**
     * Create an instance of {@link DefaultAnimationType.Link }
     * 
     * @return
     *     the new instance of {@link DefaultAnimationType.Link }
     */
    public DefaultAnimationType.Link createDefaultAnimationTypeLink() {
        return new DefaultAnimationType.Link();
    }

    /**
     * Create an instance of {@link DefaultAnimationType.Connector }
     * 
     * @return
     *     the new instance of {@link DefaultAnimationType.Connector }
     */
    public DefaultAnimationType.Connector createDefaultAnimationTypeConnector() {
        return new DefaultAnimationType.Connector();
    }

    /**
     * Create an instance of {@link DefaultAnimationType.Lane }
     * 
     * @return
     *     the new instance of {@link DefaultAnimationType.Lane }
     */
    public DefaultAnimationType.Lane createDefaultAnimationTypeLane() {
        return new DefaultAnimationType.Lane();
    }

    /**
     * Create an instance of {@link DefaultAnimationType.Stripe }
     * 
     * @return
     *     the new instance of {@link DefaultAnimationType.Stripe }
     */
    public DefaultAnimationType.Stripe createDefaultAnimationTypeStripe() {
        return new DefaultAnimationType.Stripe();
    }

    /**
     * Create an instance of {@link DefaultAnimationType.Shoulder }
     * 
     * @return
     *     the new instance of {@link DefaultAnimationType.Shoulder }
     */
    public DefaultAnimationType.Shoulder createDefaultAnimationTypeShoulder() {
        return new DefaultAnimationType.Shoulder();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IncludeType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IncludeType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/XInclude", name = "include")
    public JAXBElement<IncludeType> createInclude(IncludeType value) {
        return new JAXBElement<>(_Include_QNAME, IncludeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FallbackType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link FallbackType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/XInclude", name = "fallback")
    public JAXBElement<FallbackType> createFallback(FallbackType value) {
        return new JAXBElement<>(_Fallback_QNAME, FallbackType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link java.lang.String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link java.lang.String }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "Default", scope = GtuColorers.class, defaultValue = "DEFAULT")
    public JAXBElement<java.lang.String> createGtuColorersDefault(java.lang.String value) {
        return new JAXBElement<>(_GtuColorersDefault_QNAME, java.lang.String.class, GtuColorers.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link java.lang.String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link java.lang.String }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "Incentive", scope = GtuColorers.class)
    public JAXBElement<java.lang.String> createGtuColorersIncentive(java.lang.String value) {
        return new JAXBElement<>(_GtuColorersIncentive_QNAME, java.lang.String.class, GtuColorers.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ClassType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "Class", scope = GtuColorers.class)
    @XmlJavaTypeAdapter(ClassAdapter.class)
    public JAXBElement<ClassType> createGtuColorersClass(ClassType value) {
        return new JAXBElement<>(_GtuColorersClass_QNAME, ClassType.class, GtuColorers.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EmptyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EmptyType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "Interpolated", scope = org.opentrafficsim.xml.generated.Link.Clothoid.class)
    public JAXBElement<EmptyType> createLinkClothoidInterpolated(EmptyType value) {
        return new JAXBElement<>(_LinkClothoidInterpolated_QNAME, EmptyType.class, org.opentrafficsim.xml.generated.Link.Clothoid.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LengthType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link LengthType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "Length", scope = org.opentrafficsim.xml.generated.Link.Clothoid.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    public JAXBElement<LengthType> createLinkClothoidLength(LengthType value) {
        return new JAXBElement<>(_LinkClothoidLength_QNAME, LengthType.class, org.opentrafficsim.xml.generated.Link.Clothoid.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LinearDensityType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link LinearDensityType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "StartCurvature", scope = org.opentrafficsim.xml.generated.Link.Clothoid.class)
    @XmlJavaTypeAdapter(LinearDensityAdapter.class)
    public JAXBElement<LinearDensityType> createLinkClothoidStartCurvature(LinearDensityType value) {
        return new JAXBElement<>(_LinkClothoidStartCurvature_QNAME, LinearDensityType.class, org.opentrafficsim.xml.generated.Link.Clothoid.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LinearDensityType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link LinearDensityType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "EndCurvature", scope = org.opentrafficsim.xml.generated.Link.Clothoid.class)
    @XmlJavaTypeAdapter(LinearDensityAdapter.class)
    public JAXBElement<LinearDensityType> createLinkClothoidEndCurvature(LinearDensityType value) {
        return new JAXBElement<>(_LinkClothoidEndCurvature_QNAME, LinearDensityType.class, org.opentrafficsim.xml.generated.Link.Clothoid.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LengthType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link LengthType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "A", scope = org.opentrafficsim.xml.generated.Link.Clothoid.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    public JAXBElement<LengthType> createLinkClothoidA(LengthType value) {
        return new JAXBElement<>(_LinkClothoidA_QNAME, LengthType.class, org.opentrafficsim.xml.generated.Link.Clothoid.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FlattenerType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link FlattenerType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.opentrafficsim.org/ots", name = "Flattener", scope = org.opentrafficsim.xml.generated.Link.Clothoid.class)
    public JAXBElement<FlattenerType> createLinkClothoidFlattener(FlattenerType value) {
        return new JAXBElement<>(_LinkClothoidFlattener_QNAME, FlattenerType.class, org.opentrafficsim.xml.generated.Link.Clothoid.class, value);
    }

}
