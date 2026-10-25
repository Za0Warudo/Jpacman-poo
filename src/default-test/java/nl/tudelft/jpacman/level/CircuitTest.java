package nl.tudelft.jpacman.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.models.GateSquare;
import nl.tudelft.jpacman.board.models.SwitchSquare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests various aspects of a circuit, including its identifier, validity,
 * and the commands it sends to its gates when toggled.
 */
class CircuitTest {

    /** The circuit under test. */
    private Circuit circuit;

    /**
     * Sets up a circuit with the first circuit identifier before each test.
     */
    @BeforeEach
    void setUp() {
        circuit = new Circuit(CircuitIdentifier.CIRCUIT_ONE);
    }

    /**
     * Verifies that the circuit returns the identifier passed to its
     * constructor.
     */
    @Test
    void getIdentifierReturnsTheIdentifierPassedToTheConstructor() {
        assertThat(circuit.getIdentifier())
            .isEqualTo(CircuitIdentifier.CIRCUIT_ONE);
    }

    /**
     * Verifies that an empty circuit is considered valid by the current
     * implementation.
     */
    @Test
    void emptyCircuitIsValidAccordingToCurrentImplementation() {
        assertThat(circuit.isValid()).isTrue();
    }

    /**
     * Verifies that a circuit containing a gate but no switches is invalid.
     */
    @Test
    void circuitWithOnlyAGateIsInvalid() {
        circuit.addGate(mock(GateSquare.class));
        assertThat(circuit.isValid()).isFalse();
    }

    /**
     * Verifies that a circuit containing a switch but no gates is invalid.
     */
    @Test
    void circuitWithOnlyASwitchIsInvalid() {
        circuit.addSwitch(mock(SwitchSquare.class));
        assertThat(circuit.isValid()).isFalse();
    }

    /**
     * Verifies that a circuit containing at least one gate and one switch is
     * valid.
     */
    @Test
    void circuitWithAtLeastOneGateAndOneSwitchIsValid() {
        circuit.addGate(mock(GateSquare.class));
        circuit.addSwitch(mock(SwitchSquare.class));
        assertThat(circuit.isValid()).isTrue();
    }

    /**
     * Verifies that a circuit remains valid when it contains multiple gates
     * and multiple switches.
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
     * Verifies that the first toggle requests every gate in the circuit to
     * open and does not request any gate to close.
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
     * Verifies that toggling an open circuit requests every gate to close.
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
     * Verifies that toggling one circuit does not send commands to gates
     * registered with a different circuit.
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
    }
}