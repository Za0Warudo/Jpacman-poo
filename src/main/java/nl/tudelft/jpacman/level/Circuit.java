package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.models.GateSquare;
import nl.tudelft.jpacman.board.models.PulseSquare;
import nl.tudelft.jpacman.board.models.SwitchSquare;

/**
 * A circuit is a set of gate and switchs that can operate this gates.
 */
public class Circuit {

    private static final long MILLISECONDS_PER_SECOND = 1000L;
    private static final int PULSE_DURATION_SECONDS = 5;

    /**
     * Reverse state map.
     */
    private static final Map<CircuitState, CircuitState> REVERSE_MAP =
        Map.of(CircuitState.OPEN,
            CircuitState.CLOSED,
            CircuitState.CLOSED,
            CircuitState.OPEN
        );

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


    private final ScheduledExecutorService
        scheduler = Executors.newSingleThreadScheduledExecutor();
    
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
        if (isReverse) {
            return REVERSE_MAP.get(this.state);
        }
        return this.state; 
    }
    
    /**
     * @return The circuit identifier. 
     */
    public CircuitIdentifier getIdentifier() {
        return this.identifier; 
    }

    /**
     * Indicates whether this circuit is currently reversed.
     *
     * @return {@code true} when the circuit is reversed.
     */
    public boolean isReverse() {
        return this.isReverse;
    }

    /**
     * Checks whether this circuit is empty or has at least one gate and switch.
     *
     * @return {@code true} if the circuit is empty or has at least one gate and
     *         switch.
     */
    public boolean isValid() {
        int gatesSize = this.gates.size(); 
        int switchsSize = this.switchs.size(); 
        int pulsesSize = this.pulses.size(); 
        return (gatesSize == 0 && switchsSize == 0 && pulsesSize == 0)
            || (gatesSize > 0 && switchsSize > 0);
    } 

    /**
     * Adds the given gate to the circuit.
     *
     * @param gateSquare
     *          The gate to add into the circuit. 
     */
    public void addGate(GateSquare gateSquare) {
        this.gates.add(gateSquare);
    }

    /**
     * Adds the given switch to the circuit.
     *
     * @param switchSquare
     *          The switch to add into the circuit. 
     */
    public void addSwitch(SwitchSquare switchSquare) {
        this.switchs.add(switchSquare); 
    }

    /**
     * Adds the given pulse to the circuit.
     *
     * @param pulse
     *            The pulse to add.
     */
    public void addPulse(PulseSquare pulse) { 
        this.pulses.add(pulse);
    }

    private void setTimer(int seconds) {
        if (this.timer != null && !this.timer.isDone()) {
            timer.cancel(false); 
        }

        this.remainingMs = seconds * MILLISECONDS_PER_SECOND;
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

    /**
     * Pauses the pulse timer, retaining its remaining duration.
     */
    public void pause() { 
        if (this.timer != null && !this.timer.isDone()) {
            this.remainingMs = Math.max(0, endTimeMs - System.currentTimeMillis());
            this.timer.cancel(false); 
            this.isPaused = true;  
        } 
    }

    /**
     * Resumes the pulse timer if it was paused.
     */
    public void resume() {
        if (isReverse && isPaused && remainingMs > 0) {
            this.endTimeMs = System.currentTimeMillis() + this.remainingMs;
            this.isPaused = false;
            this.timer = scheduler.schedule(this::expire, this.remainingMs, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops the pulse timer and restores the normal circuit state.
     */
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
        if (!isReverse) {
            reverse();
        }
        setTimer(PULSE_DURATION_SECONDS);
    }
    
    /**
     * Toggles the circuit state and updates its gates.
     */
    public void toggle() {
        if (this.state == CircuitState.OPEN) {
            this.state = CircuitState.CLOSED;
        } else {
            this.state = CircuitState.OPEN; 
        }
        update();
    }

    /**
     * Collects a pulse and reverses the circuit for its duration.
     *
     * @param pulse
     *            The pulse collected from this circuit.
     */
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
            for (GateSquare gate: this.gates) {
                gate.requestOpen();
            }
        } else {
            for (GateSquare gate: this.gates) {
                gate.requestClose();
            }
        }
    }

    /**
     * Returns the remaining pulse duration in seconds.
     *
     * @return The remaining time rounded up to whole seconds,
     *         or 0 when no pulse countdown is active.
     */
    public synchronized int getRemainingSeconds() {
        if (!isReverse) {
            return 0;
        }

        long remaining;

        if (isPaused) {
            remaining = remainingMs;
        } else if (timer != null && !timer.isDone()) {
            remaining = Math.max(
                0L,
                endTimeMs - System.currentTimeMillis()
            );
        } else {
            return 0;
        }

        return (int) ((remaining + MILLISECONDS_PER_SECOND - 1)
            / MILLISECONDS_PER_SECOND);
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
