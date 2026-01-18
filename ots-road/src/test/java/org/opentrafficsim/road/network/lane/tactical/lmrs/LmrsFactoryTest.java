package org.opentrafficsim.road.network.lane.tactical.lmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.cli.CliUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.SumFuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArFuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelFuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskScan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AbstractIncentivesTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory.Setting;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory.TacticalPlannerProvider;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * Test written for LmrsFactory with heavy use of ChatGPT.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LmrsFactoryTest
{

    /** Standard car provider. */
    @Mock
    private LmrsFactory.TacticalPlannerProvider<AbstractIncentivesTacticalPlanner> carProvider;

    /** Standard truck provider. */
    @Mock
    private LmrsFactory.TacticalPlannerProvider<AbstractIncentivesTacticalPlanner> truckProvider;

    /** Standard car planner. */
    @Mock
    private AbstractIncentivesTacticalPlanner carPlanner;

    /** Standard truck planner. */
    @Mock
    private AbstractIncentivesTacticalPlanner truckPlanner;

    /** Standard car. */
    @Mock
    private LaneBasedGtu carGtu;

    /** Standard truck. */
    @Mock
    private LaneBasedGtu truckGtu;

    /** Standard car GTU type. */
    @Mock
    private GtuType carType;

    /** Standard truck GTU type. */
    @Mock
    private GtuType truckType;

    /** Standard factory for a bunch of tests. */
    private LmrsFactory<AbstractIncentivesTacticalPlanner> factory;

    /** Mocks to reset. */
    private AutoCloseable mocks;

    /* -------------------- setup / teardown -------------------- */

    /**
     * Mock setup before each test.
     */
    @BeforeEach
    void setUp()
    {
        // Initialize Mockito without @ExtendWith
        this.mocks = MockitoAnnotations.openMocks(this);

        // Minimal GTU identity setup
        when(this.carGtu.getType()).thenReturn(this.carType);
        when(this.carType.getId()).thenReturn("NL.CAR");
        when(this.truckGtu.getType()).thenReturn(this.truckType);
        when(this.truckType.getId()).thenReturn("NL.TRUCK");

        // Provider always returns the mocked planner
        when(this.carProvider.from(any(CarFollowingModel.class), eq(this.carGtu), any(LanePerception.class),
                any(Synchronization.class), any(Cooperation.class), any(GapAcceptance.class), any(Tailgating.class)))
                        .thenReturn(this.carPlanner);

        // Factory under test
        this.factory = new LmrsFactory<>(List.of(this.carType), this.carProvider);
    }

    /**
     * Reset mocks after each test.
     * @throws Exception exception
     */
    @AfterEach
    void tearDown() throws Exception
    {
        this.mocks.close();
    }

    /* -------------------- constructor tests -------------------- */

    /**
     * Null constructor input.
     */
    @Test
    void constructorRejectsNullProvider()
    {
        // Verifies defensive null checks in constructor
        assertThrows(NullPointerException.class,
                () -> new LmrsFactory<AbstractIncentivesTacticalPlanner>(List.of(this.carType),
                        (TacticalPlannerProvider<AbstractIncentivesTacticalPlanner>) null));
        assertThrows(NullPointerException.class, () -> new LmrsFactory<AbstractIncentivesTacticalPlanner>(
                (TacticalPlannerProvider<AbstractIncentivesTacticalPlanner>) null));
    }

    /**
     * Single provider test.
     * @throws GtuException exception
     */
    @Test
    void constructorWithSingleProviderAppliesToAllGtuTypes() throws GtuException
    {
        // Shared provider
        LmrsFactory.TacticalPlannerProvider<AbstractIncentivesTacticalPlanner> provdr = mock();
        AbstractIncentivesTacticalPlanner planr = mock();
        when(provdr.from(any(), any(), any(), any(), any(), any(), any())).thenReturn(planr);

        // Factory with multiple GTU types, single provider
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact =
                new LmrsFactory<>(List.of(this.carType, this.truckType), provdr);
        fact.create(this.carGtu);
        fact.create(this.truckGtu);

        // Same provider must be used for both GTU types
        verify(provdr, times(2)).from(any(), any(), any(), any(), any(), any(), any());
    }

    /**
     * Different providers correctly used.
     * @throws GtuException exception
     */
    @Test
    void constructorWithProviderPerGtuTypeSelectsCorrectProvider() throws GtuException
    {
        when(this.carProvider.from(any(), any(), any(), any(), any(), any(), any())).thenReturn(this.carPlanner);
        when(this.truckProvider.from(any(), any(), any(), any(), any(), any(), any())).thenReturn(this.truckPlanner);

        // Factory with provider per GTU type
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact =
                new LmrsFactory<>(List.of(this.carType, this.truckType), List.of(this.carProvider, this.truckProvider));
        AbstractIncentivesTacticalPlanner carResult = fact.create(this.carGtu);
        AbstractIncentivesTacticalPlanner truckResult = fact.create(this.truckGtu);

        // Correct provider must be used
        assertSame(this.carPlanner, carResult);
        assertSame(this.truckPlanner, truckResult);
        verify(this.carProvider).from(any(), eq(this.carGtu), any(), any(), any(), any(), any());
        verify(this.truckProvider).from(any(), eq(this.truckGtu), any(), any(), any(), any(), any());
    }

    /**
     * Test different size lists.
     */
    @Test
    void constructorRejectsMismatchedGtuTypesAndProviders()
    {
        LmrsFactory.TacticalPlannerProvider<AbstractIncentivesTacticalPlanner> provdr = mock();

        // Size mismatch must throw
        assertThrows(IllegalArgumentException.class,
                () -> new LmrsFactory<>(List.of(this.carType, this.truckType), List.of(provdr)));
    }

    /* -------------------- multi-GTU settings -------------------- */

    /**
     * @throws GtuException exception
     */
    @Test
    void perGtuTypeSettingsAreIndependent() throws GtuException
    {
        // Provider
        LmrsFactory.TacticalPlannerProvider<AbstractIncentivesTacticalPlanner> provdr = mock();
        AbstractIncentivesTacticalPlanner planr = mock();
        when(provdr.from(any(), any(), any(), any(), any(), any(), any())).thenReturn(planr);

        // Factory
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact =
                new LmrsFactory<>(List.of(this.carType, this.truckType), provdr);

        // Disable incentive only for trucks
        fact.set(LmrsFactory.Setting.INCENTIVE_KEEP, false, this.truckType);
        fact.create(this.carGtu);
        fact.create(this.truckGtu);

        // Car keeps default behavior
        verify(planr).addVoluntaryIncentive(IncentiveKeep.SINGLETON);
        // Truck must not get it (i.e. still 1 call)
        verify(planr).addVoluntaryIncentive(IncentiveKeep.SINGLETON);
    }

    /* -------------------- default behavior -------------------- */

    /**
     * Test default incentives.
     * @throws GtuException exception
     */
    @Test
    void createAddsDefaultIncentives() throws GtuException
    {
        // Act
        AbstractIncentivesTacticalPlanner result = this.factory.create(this.carGtu);

        // Mandatory incentives enabled by default
        verify(this.carPlanner).addMandatoryIncentive(IncentiveRoute.SINGLETON);

        // Voluntary incentives enabled by default
        verify(this.carPlanner).addVoluntaryIncentive(IncentiveSpeedWithCourtesy.SINGLETON);
        verify(this.carPlanner).addVoluntaryIncentive(IncentiveKeep.SINGLETON);

        // Factory returns the provider-created planner
        assertSame(this.carPlanner, result);
    }

    /* -------------------- provider delegation -------------------- */

    /**
     * Correct input to provider.
     * @throws GtuException exception
     */
    @Test
    void createDelegatesCorrectArgumentsToProvider() throws GtuException
    {
        this.factory.create(this.carGtu);

        // Capture the enum arguments passed into the provider
        ArgumentCaptor<Synchronization> sync = ArgumentCaptor.forClass(Synchronization.class);
        ArgumentCaptor<Cooperation> coop = ArgumentCaptor.forClass(Cooperation.class);
        ArgumentCaptor<GapAcceptance> gap = ArgumentCaptor.forClass(GapAcceptance.class);

        verify(this.carProvider).from(any(CarFollowingModel.class), eq(this.carGtu), any(LanePerception.class), sync.capture(),
                coop.capture(), gap.capture(), any(Tailgating.class));

        // Verify defaults are used
        assertEquals(Synchronization.PASSIVE, sync.getValue());
        assertEquals(Cooperation.PASSIVE, coop.getValue());
        assertEquals(GapAcceptance.INFORMED, gap.getValue());
    }

    /* -------------------- setting overrides -------------------- */

    /**
     * No incentive route when set as such.
     * @throws GtuException exception
     */
    @Test
    void setDisablesMandatoryIncentive() throws GtuException
    {
        // Disable a default-true setting
        this.factory.set(LmrsFactory.Setting.INCENTIVE_ROUTE, false);
        this.factory.create(this.carGtu);

        // Mandatory route incentive must not be added
        verify(this.carPlanner, never()).addMandatoryIncentive(IncentiveRoute.SINGLETON);
    }

    /**
     * No incentive speed with courtesy when set as such.
     * @throws GtuException exception
     */
    @Test
    void perGtuTypeSettingIsApplied() throws GtuException
    {
        // Disable speed-with-courtesy only for this GTU type
        this.factory.set(LmrsFactory.Setting.INCENTIVE_SPEED_WITH_COURTESY, false, this.carType);
        this.factory.create(this.carGtu);

        // Verify that the per-GTU override takes effect
        verify(this.carPlanner, never()).addVoluntaryIncentive(IncentiveSpeedWithCourtesy.SINGLETON);
    }

    /* -------------------- one-shot mode -------------------- */

    /**
     * One-shot mode test.
     * @throws GtuException exception
     * @throws ParameterException exception
     */
    @Test
    void oneShotModeResetsStateAfterCreate() throws GtuException, ParameterException
    {
        // Enable one-shot behavior
        this.factory.setOneShotMode();
        this.factory.setStream(new MersenneTwister(1L));

        // Override a default setting
        this.factory.set(LmrsFactory.Setting.INCENTIVE_ROUTE, false);
        Duration t = Duration.ofSI(2.31);
        this.factory.addParameter(ParameterTypes.TMAX, t);

        // First create uses overridden value
        Parameters params = new ParameterSet();
        this.factory.setValues(params, this.carType);
        assertEquals(t, params.getParameter(ParameterTypes.TMAX));
        this.factory.create(this.carGtu);
        verify(this.carPlanner, never()).addMandatoryIncentive(IncentiveRoute.SINGLETON);

        // Reset mocks for clarity
        reset(this.carPlanner, this.carProvider);
        when(this.carProvider.from(any(), any(), any(), any(), any(), any(), any())).thenReturn(this.carPlanner);

        // Second create must revert to defaults
        params = new ParameterSet();
        this.factory.setValues(params, this.carType);
        assertNull(params.getOptionalParameter(ParameterTypes.TMAX).orElse(null));
        this.factory.create(this.carGtu);
        verify(this.carPlanner).addMandatoryIncentive(IncentiveRoute.SINGLETON);
    }

    /* -------------------- error handling -------------------- */

    /**
     * Unknown GTU type.
     */
    @Test
    void setWithUnknownGtuTypeThrowsException()
    {
        // GTU type not registered in the factory
        GtuType unknownType = mock(GtuType.class);
        when(unknownType.getId()).thenReturn("UNKNOWN");

        // Verify defensive check
        assertThrows(IllegalArgumentException.class,
                () -> this.factory.set(LmrsFactory.Setting.INCENTIVE_ROUTE, false, unknownType));
    }

    /* -------------------- parent GTU types -------------------- */

    /**
     * Inherited settings.
     * @throws GtuException exception
     */
    @Test
    void childGtuInheritsSettingFromParent() throws GtuException
    {
        // Parent GTU
        GtuType parent = mock(GtuType.class);
        when(parent.getId()).thenReturn("PARENT");
        when(parent.getParent()).thenReturn(Optional.empty());

        // Child GTU
        GtuType child = mock(GtuType.class);
        when(child.getId()).thenReturn("CHILD");
        when(child.getParent()).thenReturn(Optional.of(parent));
        LaneBasedGtu childGtu = mock(LaneBasedGtu.class);
        when(childGtu.getType()).thenReturn(child);

        // Provider
        LmrsFactory.TacticalPlannerProvider<AbstractIncentivesTacticalPlanner> provdr = mock();
        AbstractIncentivesTacticalPlanner planr = mock();
        when(provdr.from(any(), any(), any(), any(), any(), any(), any())).thenReturn(planr);

        // Factory knows both parent and child
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(List.of(parent, child), provdr);
        fact.set(LmrsFactory.Setting.INCENTIVE_ROUTE, false, parent);
        fact.create(childGtu);

        // Child must inherit parent value
        verify(planr, never()).addMandatoryIncentive(IncentiveRoute.SINGLETON);
    }

    /**
     * Settings result specific to GTU type in complex specific settings.
     * @throws GtuException exception
     */
    @Test
    void partialSpecificSettingsFallback() throws GtuException
    {
        // Vehicle GTU
        GtuType vehicle = mock(GtuType.class);
        when(vehicle.getId()).thenReturn("NL.VEHICLE");
        when(vehicle.getParent()).thenReturn(Optional.empty());

        // Car GTU
        GtuType car = mock(GtuType.class);
        when(car.getId()).thenReturn("NL.CAR");
        when(car.getParent()).thenReturn(Optional.of(vehicle));
        LaneBasedGtu carGtuVehicle = mock(LaneBasedGtu.class);
        when(carGtuVehicle.getType()).thenReturn(car);

        // Truck GTU
        GtuType truck = mock(GtuType.class);
        when(truck.getId()).thenReturn("NL.TRUCK");
        when(truck.getParent()).thenReturn(Optional.of(vehicle));
        LaneBasedGtu truckGtuVehicle = mock(LaneBasedGtu.class);
        when(truckGtuVehicle.getType()).thenReturn(truck);

        // Van GTU
        GtuType van = mock(GtuType.class);
        when(van.getId()).thenReturn("NL.VAN");
        when(van.getParent()).thenReturn(Optional.of(vehicle));
        LaneBasedGtu vanGtuVehicle = mock(LaneBasedGtu.class);
        when(vanGtuVehicle.getType()).thenReturn(van);

        // Bus GTU
        GtuType bus = mock(GtuType.class);
        when(bus.getId()).thenReturn("NL.BUS");
        when(bus.getParent()).thenReturn(Optional.empty()); // no parent
        LaneBasedGtu busGtu = mock(LaneBasedGtu.class);
        when(busGtu.getType()).thenReturn(bus);

        // Taxi GTU
        GtuType taxi = mock(GtuType.class);
        when(taxi.getId()).thenReturn("NL.TAXI");
        when(taxi.getParent()).thenReturn(Optional.of(vehicle)); // not provided to factory, but a parent
        LaneBasedGtu taxiGtuVehicle = mock(LaneBasedGtu.class);
        when(taxiGtuVehicle.getType()).thenReturn(taxi);

        // Motor GTU
        GtuType motor = mock(GtuType.class);
        when(motor.getId()).thenReturn("NL.MOTOR");
        when(motor.getParent()).thenReturn(Optional.empty()); // not provided to factory, no parent
        LaneBasedGtu motorGtu = mock(LaneBasedGtu.class);
        when(motorGtu.getType()).thenReturn(motor);

        // ACC GTU
        GtuType acc = mock(GtuType.class);
        when(acc.getId()).thenReturn("NL.ACC");
        when(acc.getParent()).thenReturn(Optional.of(van)); // not provided to factory, but a parent with a parent
        LaneBasedGtu accGtuVan = mock(LaneBasedGtu.class);
        when(accGtuVan.getType()).thenReturn(acc);

        // Provider
        LmrsFactory.TacticalPlannerProvider<AbstractIncentivesTacticalPlanner> provdr = mock();
        AbstractIncentivesTacticalPlanner planr = mock();
        when(provdr.from(any(), any(), any(), any(), any(), any(), any())).thenReturn(planr);

        // Factory receives only two incentiveRoute values
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(provdr);
        CliUtil.execute(fact,
                new String[] {"--gtuTypes=NL.VEHICLE|NL.CAR|NL.TRUCK|NL.VAN|NL.BUS", "--incentiveRoute=false|true"});
        fact.set(Setting.INCENTIVE_ROUTE, true, van);

        // Car has route incentive through command line
        fact.create(carGtuVehicle);
        verify(planr).addMandatoryIncentive(IncentiveRoute.SINGLETON);

        // Truck has no route incentive (null -> vehicle parent -> no route incentive)
        fact.create(truckGtuVehicle);
        verify(planr).addMandatoryIncentive(IncentiveRoute.SINGLETON);

        // Van has route incentive through setting
        fact.create(vanGtuVehicle);
        verify(planr, times(2)).addMandatoryIncentive(IncentiveRoute.SINGLETON);

        // Bus gives exception (known type but null -> no parent)
        assertThrows(IllegalArgumentException.class, () -> fact.create(busGtu));

        // Taxi has no route incentive (unknown type and null -> vehicle parent -> no route incentive)
        fact.create(taxiGtuVehicle);
        verify(planr, times(2)).addMandatoryIncentive(IncentiveRoute.SINGLETON);

        // Motor gives exception (unknown type and null -> no parent)
        assertThrows(IllegalArgumentException.class, () -> fact.create(motorGtu));

        // Acc has route incentive (null -> van parent -> route incentive)
        fact.create(accGtuVan);
        verify(planr, times(3)).addMandatoryIncentive(IncentiveRoute.SINGLETON);
    }

    /* -------------------- tailgating -------------------- */

    /**
     * Tailgating.
     * @throws GtuException exception
     */
    @Test
    void tailgatingEnablesTailgatingBehavior() throws GtuException
    {
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(List.of(this.carType), this.carProvider);
        fact.set(LmrsFactory.Setting.SOCIO_TAILGATING, true);
        fact.create(this.carGtu);

        // Tailgating does not add voluntary incentives directly
        verify(this.carProvider).from(any(), any(), any(), any(), any(), any(), eq(Tailgating.PRESSURE));
    }

    /* -------------------- social interactions -------------------- */

    /**
     * Socio lane change incentive.
     * @throws GtuException exception
     */
    @Test
    void socioLaneChangeAddsSocioIncentive() throws GtuException
    {
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(List.of(this.carType), this.carProvider);

        fact.set(LmrsFactory.Setting.SOCIO_LANE_CHANGE, true);

        fact.create(this.carGtu);

        verify(this.carPlanner).addVoluntaryIncentive(IncentiveSocioSpeed.SINGLETON);
    }

    /* -------------------- parameters -------------------- */

    /**
     * Some parameters returned.
     * @throws ParameterException exception
     */
    @Test
    void getParametersReturnsNonEmptyParameterSet() throws ParameterException
    {
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(List.of(this.carType), mock());
        fact.setStream(new MersenneTwister(1L));
        Parameters parameters = fact.getParameters(this.carType);
        assertNotNull(parameters);
        assertTrue(((ParameterSet) parameters).getParameters().size() > 0);
    }

    /**
     * No Fuller, no reation time.
     * @throws ParameterException exception
     */
    @Test
    void fullerNoneDoesNotAddFullerParameters() throws ParameterException
    {
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(List.of(this.carType), mock());

        fact.set(LmrsFactory.Setting.FULLER_IMPLEMENTATION, LmrsFactory.FullerImplementation.NONE);
        fact.setStream(new MersenneTwister(1L));

        Parameters parameters = fact.getParameters(this.carType);

        // Reaction time TR is only added when Fuller is active
        assertFalse(parameters.contains(ParameterTypes.TR));
    }

    /**
     * Attention Matrix, TD_SCAN.
     * @throws ParameterException exception
     */
    @Test
    void attentionMatrixAddsTaskParameters() throws ParameterException
    {
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(List.of(this.carType), mock());
        fact.setStream(new MersenneTwister(1L));

        fact.set(LmrsFactory.Setting.FULLER_IMPLEMENTATION, LmrsFactory.FullerImplementation.ATTENTION_MATRIX);

        Parameters parameters = fact.getParameters(this.carType);

        // Channel-based Fuller must define task scan parameter
        assertTrue(parameters.contains(ChannelTaskScan.TDSCAN));
    }

    /* -------------------- Fuller implementations -------------------- */

    /**
     * Fuller implementations.
     * @throws GtuException exception
     */
    @Test
    void testArAndAm() throws GtuException
    {
        createUsesMentalModel(LmrsFactory.FullerImplementation.NONE, null);
        createUsesMentalModel(LmrsFactory.FullerImplementation.SUMMATIVE, SumFuller.class);
        createUsesMentalModel(LmrsFactory.FullerImplementation.ANTICIPATION_RELIANCE, ArFuller.class);
        createUsesMentalModel(LmrsFactory.FullerImplementation.ATTENTION_MATRIX, ChannelFuller.class);
    }

    /**
     * Check correct mental implementation.
     * @param implementation implementation
     * @param clazz class of mental implementation
     * @throws GtuException exceptiono
     */
    private void createUsesMentalModel(final LmrsFactory.FullerImplementation implementation,
            final Class<? extends Fuller> clazz) throws GtuException
    {
        // Capture LanePerception passed to provider
        ArgumentCaptor<LanePerception> perceptionCaptor = ArgumentCaptor.forClass(LanePerception.class);

        when(this.carProvider.from(any(), eq(this.carGtu), perceptionCaptor.capture(), any(), any(), any(), any()))
                .thenReturn(this.carPlanner);

        // Factory
        LmrsFactory<AbstractIncentivesTacticalPlanner> fact = new LmrsFactory<>(List.of(this.carType), this.carProvider);

        // Enable Fuller attention matrix
        fact.set(LmrsFactory.Setting.FULLER_IMPLEMENTATION, implementation);

        // Act
        fact.create(this.carGtu);

        LanePerception perception = perceptionCaptor.getValue();
        assertNotNull(perception);

        // Verify mental model
        Object mental = ((Optional<?>) perception.getMental()).orElse(null);
        if (clazz == null)
        {
            assertNull(mental);
        }
        else
        {
            assertNotNull(mental);
            assertTrue(clazz.isAssignableFrom(mental.getClass()),
                    implementation + "must create " + clazz.getSimpleName() + " mental model");
        }
    }

}
