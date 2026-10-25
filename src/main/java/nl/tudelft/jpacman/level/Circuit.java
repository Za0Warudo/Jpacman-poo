package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.models.GateSquare;
import nl.tudelft.jpacman.board.models.SwitchSquare;

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
     * Circuit switch list. 
     */
    private final List<SwitchSquare> switchs; 
    
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
        this.switchs = new ArrayList<SwitchSquare>();  
        this.identifier = ciIdentifier; 
        this.state = CircuitState.CLOSED;  
    }
    
    /**
     * @return The circuit identifier. 
     */
    public CircuitIdentifier getIdentifier() {
        return this.identifier; 
    }

    /**
     * @return If the circuit is a valid circuit (i.e it is a null circuit or has at least a gate and a switch). 
     */
    public boolean isValid() {
        int gatesSize = this.gates.size(); 
        int switchsSize = this.switchs.size(); 
        return (gatesSize == 0 && switchsSize == 0) || (gatesSize > 0 && switchsSize > 0);  
    } 

    /**
     * Add the given gate into the circuit. 
     * @param gate 
     *          The gate to add into the circuit. 
     */
    public void addGate(GateSquare gateSquare) {
        this.gates.add(gateSquare);
    }

    /**
     * Add the given switch into the circuit. 
     * @param switch
     *          The switch to add into the circuit. 
     */
    public void addSwitch(SwitchSquare switchSquare) {
        this.switchs.add(switchSquare); 
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
     * Circuit states list. 
     */
    enum CircuitState {
        /**
         * Open state.  
         */
        OPEN,

        /**
         * Closed state. 
         */
        CLOSED
    }

}
