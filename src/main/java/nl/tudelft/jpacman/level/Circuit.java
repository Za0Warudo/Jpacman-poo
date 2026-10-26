package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.models.GateSquare;
import nl.tudelft.jpacman.board.models.PulseSquare;
import nl.tudelft.jpacman.board.models.SwitchSquare;

public class Circuit {

    private static HashMap<CircuitState, CircuitState> reverseMap = new HashMap<>(Map.of(CircuitState.OPEN, CircuitState.CLOSED, CircuitState.CLOSED, CircuitState.OPEN)); 

    /**
    * The circuit identifier. 
    */
    private final CircuitIdentifier identifier; 
        
    /**
     * Circuit gates list. 
     */
    private final List<GateSquare> gates;

    /**
     * Circuit pulses list. 
     */
    private final List<PulseSquare> pulses; 
    
    /**
     * Circuit switch list. 
     */
    private final List<SwitchSquare> switchs; 
    
    /**
     * Current circuit state. 
     */
    private CircuitState state; 
    
    /**
     * Indicates if circuit is in reverse state. 
     */
    private boolean isReverse; 


    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); 
    
    private ScheduledFuture<?> timer; 
    
    /**
     * End timer value in miliseconds. 
     */
    private long endTimeMs;
    
    /**
     * Remaining time value in miliseconds. 
     */
    private long remainingMs;

    /**
     * Indicates if timer is paused.
     */
    private boolean isPaused;  

    /**
     * 
     * @param ciIdentifier
     *          Circuit identifier. 
     */
    public Circuit(CircuitIdentifier ciIdentifier) {
        this.gates = new ArrayList<GateSquare>(); 
        this.switchs = new ArrayList<SwitchSquare>();
        this.pulses = new ArrayList<PulseSquare>();
        this.identifier = ciIdentifier; 
        this.state = CircuitState.CLOSED;  
        this.isReverse = false;
        this.isPaused = false;
        this.timer = null; 
        this.endTimeMs = 0; 
        this.remainingMs = 0; 
    }

    private CircuitState getState() {
        if (isReverse) return reverseMap.get(this.state); 
        return this.state; 
    }
    
    /**
     * @return The circuit identifier. 
     */
    public CircuitIdentifier getIdentifier() {
        return this.identifier; 
    }

    public boolean isReverse() {
        return this.isReverse;
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

    public void addPulse(PulseSquare pulse) { 
        this.pulses.add(pulse);
    }

    private void setTimer(int seconds) {
        if (this.timer != null && !this.timer.isDone()) {
            timer.cancel(false); 
        }

        this.remainingMs = seconds * 1000L; 
        this.endTimeMs = System.currentTimeMillis() + this.remainingMs; 
        this.isPaused = false; 

        this.timer = scheduler.schedule(this::expire, this.remainingMs, TimeUnit.MILLISECONDS); 
    }

    private void expire() { 
        assert this.isReverse; 
        undoReverse();
        this.timer = null; 
        this.remainingMs = 0; 
    }

    public void pause() { 
        if (this.timer != null && !this.timer.isDone()) {
            this.remainingMs = Math.max(0, endTimeMs - System.currentTimeMillis());
            this.timer.cancel(false); 
            this.isPaused = true;  
        } 
    }

    public void resume() {
        if (isReverse && isPaused && remainingMs > 0) {
            this.endTimeMs = System.currentTimeMillis() + this.remainingMs;
            this.isPaused = false;
            this.timer = scheduler.schedule(this::expire, this.remainingMs, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel(false);
        }
        if (isReverse) {
            this.isReverse = false;
            update();
        }
        this.remainingMs = 0;
        this.isPaused = false;
    }

    /**
     * Reverse current circuit state. 
     */
    public void reverse() { 
        assert !this.isReverse; 
        this.isReverse = true;
        update();  
    }

    /**
     * Undo a circuit state reverse. 
     */
    public void undoReverse() { 
        assert this.isReverse; 
        this.isReverse = false; 
        update();
    }

    /**
     * Call for collect pulse. 
     */
    private void requestCollectPulse() {
        if (!isReverse) reverse();
        setTimer(5);
    }
    
    /**
     * toggle circuit state and update gates in the circuit. 
     */
    public void toggle() {
        if (this.state == CircuitState.OPEN) 
            this.state = CircuitState.CLOSED;
        else 
            this.state = CircuitState.OPEN; 
        update();
    }

    public void collectPulse(PulseSquare pulse) {
        assert pulses.contains(pulse); 
        pulse.collect();
        requestCollectPulse();  
    }

    /**
     * Updates circuit gates for the current gate state.  
     */
    private void update() { 
        if (getState() == CircuitState.OPEN) { 
            for (GateSquare gate: this.gates) 
                gate.requestOpen();  
        }
        else {
            for (GateSquare gate: this.gates)
                gate.requestClose();
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
