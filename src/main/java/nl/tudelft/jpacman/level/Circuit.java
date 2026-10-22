package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.models.GateSquare;

public class Circuit {

    /**
    * The circuit identifier. 
    */
    private final CircuitIdentifier identifier; 
    
    
    /**
     * Circuit gates list. 
     */
    private final List<GateSquare> gates;
    
    /**
     * Current circuit state. 
     */
    private CircuitState state; 

    /**
     * 
     * @param gates
     *          Circuit gates.
     * @param ciIdentifier
     *          Circuit identifier. 
     */
    Circuit(CircuitIdentifier ciIdentifier) {
        this.gates = new ArrayList<GateSquare>(); 
        this.identifier = ciIdentifier; 
        this.state = CircuitState.CLOSED;  
    }
    
    public CircuitIdentifier getIdentifier() {
        return this.identifier; 
    }

    public void addGate(GateSquare gate) {
        this.gates.add(gate);
    }

    /**
     * toggle circuit state. 
     */
    public void toggle() {
        if (this.state == CircuitState.OPEN) { 
            this.state = CircuitState.CLOSED;
            for (GateSquare gate : this.gates) 
                gate.requestClose();
        }
        else {
            this.state = CircuitState.OPEN; 
            for (GateSquare gate : this.gates) {
                gate.requestOpen();
            }
        } 
    }

    /**
     * Circuit states enum. 
     */
    enum CircuitState {
        /**
         * Open state.  
         */
        OPEN,

        /**
         * Close state. 
         */
        CLOSED
    }

}
