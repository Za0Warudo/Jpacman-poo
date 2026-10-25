package nl.tudelft.jpacman.board.models;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.GateState;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A gate square is a square that has a gate logic.
 */
public class GateSquare extends Square {
    
    /**
    * The open background for this square.
    */
    private final Sprite openBackground;

    /**
     * The closing background for this square. 
     */
    private final Sprite closingBackground; 
    
    /**
    * The open background for this square.
    */
    private final Sprite closeBackground;

    /**
     * The circuit identifier. 
     */
    private final CircuitIdentifier identifier; 

    /**
    * The gate current state.
    */
    private GateState state;

    /**
     * Creates a new Gate square.
     *
     * @param openSprite
     *            The background for the open gate square.
     * 
     * @param closeSprite
     *            The background for the close gate square.
     */
    public GateSquare(Sprite openSprite, Sprite closeSprite, Sprite closingSprite, CircuitIdentifier cIdentifier) {
        this.openBackground = openSprite;
        this.closeBackground = closeSprite; 
        this.closingBackground = closingSprite; 
        this.identifier = cIdentifier; 
        this.state = GateState.CLOSED; 
    } 

    /**
    * @return The gate circuit identifier. 
    */
    public CircuitIdentifier getIdentifier() {
        return this.identifier; 
    }

    /**
    * @return The current gate state. 
    */
    public GateState getState() {
        return this.state; 
    }

    /**
    * Open the gate. 
    */
    public void requestOpen() { 
    this.state = GateState.OPEN; 
    }

    /**
    * Try to close the gate.
    */
    public void requestClose() {
        if (this.getOccupants().isEmpty()) { 
            this.state = GateState.CLOSED; 
        }
        else {
            this.state = GateState.CLOSE_PENDING;
        }  
    }

    public void occupantLeft() {
    if (state == GateState.CLOSE_PENDING
            && getOccupants().isEmpty()) {
        state = GateState.CLOSED;
    }
}
    
    @Override
    public boolean isAccessibleTo(Unit unit) {
        return this.state == GateState.OPEN; 
    }

    @Override
    public Sprite getSprite() {
        if (state == GateState.CLOSED) return this.closeBackground; 
        else if (state == GateState.CLOSE_PENDING) return this.closingBackground;
        return this.openBackground;
    }
}
