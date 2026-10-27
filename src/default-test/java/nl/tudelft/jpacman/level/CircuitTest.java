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

    /**
     * Pulse duration in seconds.
     */
    private static final int PULSE_DURATION_SECONDS = 5;

    /**
     * Minimum remaining seconds for a pulse.
     */
    private static final int MIN_REMAINING_SECONDS = 1;

    /**
     * Intermediate threshold for remaining seconds test assertions.
     */
    private static final int MID_REMAINING_SECONDS = 4;

    /**
     * Long thread sleep duration in milliseconds.
     */
    private static final long SLEEP_DURATION_LONG = 1_200L;

    /**
     * Short thread sleep duration in milliseconds.
     */
    private static final long SLEEP_DURATION_SHORT = 1_100L;

    /**
     * Maximum wait timeout in milliseconds for pulse expiration.
     */
    private static final long WAIT_TIMEOUT_MS = 6_500L;

    /**
     * Timeout in milliseconds for Mockito verify assertion.
     */
    private static final long VERIFY_TIMEOUT_MS = 500L;

    /**
     * Polling interval in milliseconds for pulse state checking.
     */
    private static final long POLL_INTERVAL_MS = 25L;

    /**
     * The circuit under test.
     */
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

    /**
     * Verifies that getIdentifier returns the identifier passed to the constructor.
     */
    @Test
    void getIdentifierReturnsTheIdentifierPassedToTheConstructor() {
        assertThat(circuit.getIdentifier())
            .isEqualTo(CircuitIdentifier.CIRCUIT_ONE);
    }

    /**
     * Verifies that a newly created circuit is not reversed and has zero countdown.
     */
    @Test
    void circuitIsNotReversedAndHasNoCountdownInitially() {
        assertThat(circuit.isReverse()).isFalse();
        assertThat(circuit.getRemainingSeconds()).isZero();
    }

    /**
     * Verifies that an empty circuit is valid according to current implementation.
     */
    @Test
    void emptyCircuitIsValidAccordingToCurrentImplementation() {
        assertThat(circuit.isValid()).isTrue();
    }

    /**
     * Verifies that a circuit with only a gate is invalid.
     */
    @Test
    void circuitWithOnlyAGateIsInvalid() {
        circuit.addGate(mock(GateSquare.class));

        assertThat(circuit.isValid()).isFalse();
    }

    /**
     * Verifies that a circuit with only a switch is invalid.
     */
    @Test
    void circuitWithOnlyASwitchIsInvalid() {
        circuit.addSwitch(mock(SwitchSquare.class));

        assertThat(circuit.isValid()).isFalse();
    }

    /**
     * Verifies that a circuit with only a pulse is invalid.
     */
    @Test
    void circuitWithOnlyAPulseIsInvalid() {
        circuit.addPulse(mock(PulseSquare.class));

        assertThat(circuit.isValid()).isFalse();
    }

    /**
     * Verifies that a circuit with at least one gate and one switch is valid.
     */
    @Test
    void circuitWithAtLeastOneGateAndOneSwitchIsValid() {
        circuit.addGate(mock(GateSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));

        assertThat(circuit.isValid()).isTrue();
    }

    /**
     * Verifies that adding multiple gates and switches keeps the circuit valid.
     */
    @Test
    void addingMoreGatesAndSwitchesKeepsCircuitValid() {
        circuit.addGate(mock(GateSquare.class));
        circuit.addGate(mock(GateSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));

        assertThat(circuit.isValid()).isTrue();
    }

    /**
     * Verifies that a circuit containing a gate, switch, and pulse is valid.
     */
    @Test
    void circuitWithGateSwitchAndPulseIsValid() {
        circuit.addGate(mock(GateSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));
        circuit.addPulse(mock(PulseSquare.class));

        assertThat(circuit.isValid()).isTrue();
    }

    /**
     * Verifies that toggling the circuit for the first time requests all gates to open.
     */
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

    /**
     * Verifies that toggling the circuit a second time requests all gates to close.
     */
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

    /**
     * Verifies that toggling one circuit does not control another circuit's gates.
     */
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

    /**
     * Verifies that collecting a pulse collects it, reverses the circuit, and starts a countdown.
     */
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
        assertThat(circuit.getRemainingSeconds())
            .isBetween(MIN_REMAINING_SECONDS, PULSE_DURATION_SECONDS);
        verify(gate, times(1)).requestOpen();
        verify(gate, never()).requestClose();
    }

    /**
     * Verifies that collecting a pulse again restarts the countdown.
     *
     * @throws InterruptedException if the test thread is interrupted.
     */
    @Test
    void collectingPulseAgainRestartsTheCountdown() throws InterruptedException {
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addPulse(pulse);

        circuit.collectPulse(pulse);
        Thread.sleep(SLEEP_DURATION_LONG);
        int secondsBeforeSecondCollection = circuit.getRemainingSeconds();

        circuit.collectPulse(pulse);
        int secondsAfterSecondCollection = circuit.getRemainingSeconds();

        assertThat(secondsBeforeSecondCollection)
            .isBetween(MIN_REMAINING_SECONDS, MID_REMAINING_SECONDS);
        assertThat(secondsAfterSecondCollection)
            .isBetween(MID_REMAINING_SECONDS, PULSE_DURATION_SECONDS);
        assertThat(secondsAfterSecondCollection)
            .isGreaterThan(secondsBeforeSecondCollection);
        assertThat(circuit.isReverse()).isTrue();
        verify(pulse, times(2)).collect();
    }

    /**
     * Verifies that pausing a pulse preserves remaining time and resuming continues the countdown.
     *
     * @throws InterruptedException if the test thread is interrupted.
     */
    @Test
    void pausingPulsePreservesRemainingTimeAndResumeContinuesCountdown()
            throws InterruptedException {
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addPulse(pulse);
        circuit.collectPulse(pulse);

        circuit.pause();
        int pausedSeconds = circuit.getRemainingSeconds();
        assertThat(pausedSeconds)
            .isBetween(MIN_REMAINING_SECONDS, PULSE_DURATION_SECONDS);

        Thread.sleep(SLEEP_DURATION_SHORT);
        assertThat(circuit.getRemainingSeconds()).isEqualTo(pausedSeconds);

        circuit.resume();
        assertThat(circuit.isReverse()).isTrue();
        Thread.sleep(SLEEP_DURATION_LONG);
        assertThat(circuit.getRemainingSeconds())
            .isLessThan(pausedSeconds)
            .isGreaterThanOrEqualTo(MIN_REMAINING_SECONDS);
    }

    /**
     * Verifies that stopping the circuit cancels the pulse and restores underlying state.
     */
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

    /**
     * Verifies that a pulse automatically ends after its duration.
     *
     * @throws InterruptedException if the test thread is interrupted.
     */
    @Test
    void pulseAutomaticallyEndsAfterItsFiveSecondDuration()
            throws InterruptedException {
        GateSquare gate = mock(GateSquare.class);
        PulseSquare pulse = mock(PulseSquare.class);
        circuit.addGate(gate);
        circuit.addPulse(pulse);

        circuit.collectPulse(pulse);
        waitForPulseToEnd(circuit, WAIT_TIMEOUT_MS);

        assertThat(circuit.isReverse()).isFalse();
        assertThat(circuit.getRemainingSeconds()).isZero();
        verify(gate, times(1)).requestOpen();
        verify(gate, timeout(VERIFY_TIMEOUT_MS).times(1)).requestClose();
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
            Thread.sleep(POLL_INTERVAL_MS);
        }

        assertThat(target.isReverse())
            .as("the pulse should expire within %s ms", timeoutMillis)
            .isFalse();
    }
}