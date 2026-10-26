package nl.tudelft.jpacman.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.models.GateSquare;
import nl.tudelft.jpacman.board.models.PulseSquare;
import nl.tudelft.jpacman.board.models.SwitchSquare;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests circuit identity, validity, gate updates, and pulse inversion behavior.
 */
class CircuitTest {

    /** The circuit under test. */
    private Circuit circuit;

    /**
     * Creates a circuit with the first circuit identifier before each test.
     */
    @BeforeEach
    void setUp() {
        circuit = new Circuit(CircuitIdentifier.CIRCUIT_ONE);
    }

    /**
     * Stops any pulse timer started by a test.
     */
    @AfterEach
    void tearDown() {
        circuit.stop();
    }

    @Test
    void getIdentifierReturnsTheIdentifierPassedToTheConstructor() {
        assertThat(circuit.getIdentifier())
            .isEqualTo(CircuitIdentifier.CIRCUIT_ONE);
    }

    @Test
    void circuitIsNotReversedAndHasNoCountdownInitially() {
        assertThat(circuit.isReverse()).isFalse();
        assertThat(circuit.getRemainingSeconds()).isZero();
    }

    @Test
    void emptyCircuitIsValidAccordingToCurrentImplementation() {
        assertThat(circuit.isValid()).isTrue();
    }

    @Test
    void circuitWithOnlyAGateIsInvalid() {
        circuit.addGate(mock(GateSquare.class));

        assertThat(circuit.isValid()).isFalse();
    }

    @Test
    void circuitWithOnlyASwitchIsInvalid() {
        circuit.addSwitch(mock(SwitchSquare.class));

        assertThat(circuit.isValid()).isFalse();
    }

    @Test
    void circuitWithOnlyAPulseIsInvalid() {
        circuit.addPulse(mock(PulseSquare.class));

        assertThat(circuit.isValid()).isFalse();
    }

    @Test
    void circuitWithAtLeastOneGateAndOneSwitchIsValid() {
        circuit.addGate(mock(GateSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));

        assertThat(circuit.isValid()).isTrue();
    }

    @Test
    void addingMoreGatesAndSwitchesKeepsCircuitValid() {
        circuit.addGate(mock(GateSquare.class));
        circuit.addGate(mock(GateSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));

        assertThat(circuit.isValid()).isTrue();
    }

    @Test
    void circuitWithGateSwitchAndPulseIsValid() {
        circuit.addGate(mock(GateSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));
        circuit.addPulse(mock(PulseSquare.class));

        assertThat(circuit.isValid()).isTrue();
    }

    @Test
    void firstToggleRequestsAllGatesToOpen() {
        GateSquare firstGate = mock(GateSquare.class);
        GateSquare secondGate = mock(GateSquare.class);
        circuit.addGate(firstGate);
        circuit.addGate(secondGate);

        circuit.toggle();

        verify(firstGate, times(1)).requestOpen();
        verify(secondGate, times(1)).requestOpen();
        verify(firstGate, never()).requestClose();
        verify(secondGate, never()).requestClose();
    }

    @Test
    void secondToggleRequestsAllGatesToClose() {
        GateSquare firstGate = mock(GateSquare.class);
        GateSquare secondGate = mock(GateSquare.class);
        circuit.addGate(firstGate);
        circuit.addGate(secondGate);

        circuit.toggle();
        circuit.toggle();

        verify(firstGate, times(1)).requestOpen();
        verify(secondGate, times(1)).requestOpen();
        verify(firstGate, times(1)).requestClose();
        verify(secondGate, times(1)).requestClose();
    }

    @Test
    void togglingOneCircuitDoesNotCommandAnotherCircuitsGates() {
        Circuit circuitTwo = new Circuit(CircuitIdentifier.CIRCUIT_TWO);
        GateSquare gateOne = mock(GateSquare.class);
        GateSquare gateTwo = mock(GateSquare.class);
        circuit.addGate(gateOne);
        circuitTwo.addGate(gateTwo);

        circuit.toggle();

        verify(gateOne).requestOpen();
        verify(gateTwo, never()).requestOpen();
        verify(gateTwo, never()).requestClose();
        circuitTwo.stop();
    }

    @Test
    void collectingPulseCollectsItReversesCircuitAndStartsCountdown() {
        GateSquare gate = mock(GateSquare.class);
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addGate(gate);
        circuit.addSwitch(mock(SwitchSquare.class));
        circuit.addPulse(pulse);

        circuit.collectPulse(pulse);

        verify(pulse, times(1)).collect();
        assertThat(circuit.isReverse()).isTrue();
        assertThat(circuit.getRemainingSeconds()).isBetween(1, 5);
        verify(gate, times(1)).requestOpen();
        verify(gate, never()).requestClose();
    }

    @Test
    void collectingPulseAgainRestartsTheCountdown() throws InterruptedException {
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addPulse(pulse);

        circuit.collectPulse(pulse);
        Thread.sleep(1_200);
        int secondsBeforeSecondCollection = circuit.getRemainingSeconds();

        circuit.collectPulse(pulse);
        int secondsAfterSecondCollection = circuit.getRemainingSeconds();

        assertThat(secondsBeforeSecondCollection).isBetween(1, 4);
        assertThat(secondsAfterSecondCollection).isBetween(4, 5);
        assertThat(secondsAfterSecondCollection)
            .isGreaterThan(secondsBeforeSecondCollection);
        assertThat(circuit.isReverse()).isTrue();
        verify(pulse, times(2)).collect();
    }

    @Test
    void pausingPulsePreservesRemainingTimeAndResumeContinuesCountdown()
        throws InterruptedException {
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addPulse(pulse);
        circuit.collectPulse(pulse);

        circuit.pause();
        int pausedSeconds = circuit.getRemainingSeconds();
        assertThat(pausedSeconds).isBetween(1, 5);

        Thread.sleep(1_100);
        assertThat(circuit.getRemainingSeconds()).isEqualTo(pausedSeconds);

        circuit.resume();
        assertThat(circuit.isReverse()).isTrue();
        Thread.sleep(1_200);
        assertThat(circuit.getRemainingSeconds())
            .isLessThan(pausedSeconds)
            .isGreaterThanOrEqualTo(1);
    }

    @Test
    void stopCancelsPulseAndRestoresTheUnderlyingCircuitState() {
        GateSquare gate = mock(GateSquare.class);
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addGate(gate);
        circuit.addPulse(pulse);

        circuit.collectPulse(pulse);
        circuit.stop();

        assertThat(circuit.isReverse()).isFalse();
        assertThat(circuit.getRemainingSeconds()).isZero();
        verify(gate, times(1)).requestOpen();
        verify(gate, times(1)).requestClose();
    }

    @Test
    void pulseAutomaticallyEndsAfterItsFiveSecondDuration()
        throws InterruptedException {
        GateSquare gate = mock(GateSquare.class);
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addGate(gate);
        circuit.addPulse(pulse);

        circuit.collectPulse(pulse);
        waitForPulseToEnd(circuit, 6_500);

        assertThat(circuit.isReverse()).isFalse();
        assertThat(circuit.getRemainingSeconds()).isZero();
        verify(gate, times(1)).requestOpen();
        verify(gate, timeout(500).times(1)).requestClose();
    }

    /**
     * Waits for the scheduled pulse expiration without relying on an exact
     * sleep duration. Fails if the pulse is still active after the timeout.
     *
     * @param target circuit whose pulse is being observed
     * @param timeoutMillis maximum time to wait
     * @throws InterruptedException if the test thread is interrupted
     */
    private void waitForPulseToEnd(Circuit target, long timeoutMillis)
        throws InterruptedException {
        long deadline = System.nanoTime()
            + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

        while (target.isReverse() && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }

        assertThat(target.isReverse())
            .as("the pulse should expire within %s ms", timeoutMillis)
            .isFalse();
    }
}