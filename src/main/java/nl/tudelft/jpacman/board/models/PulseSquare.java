package nl.tudelft.jpacman.board.models;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A pulse square is a square that can activate a reverse state into a circuit for 5 seconds. 
 */
public class PulseSquare extends Square {
    
    /**
    * The available background for this square.
    */
    private final Sprite availableBackground;
    
    /**
    * The collected background for this square.
    */
    private final Sprite collectedBackground;

    /**
     * The circuit identifier. 
     */
    private final CircuitIdentifier identifier;
    
    /**
     * Indicates if pulse is available. 
     */
    private boolean isAvailable; 

    /**
     * Creates a new pulse square. 
     * @param aSprite Available pulse sprite. 
     * @param cSprite Collected pulse sprite. 
     * @param cIdentifier Circuit identifier. 
     */
    public PulseSquare(Sprite aSprite, Sprite cSprite, CircuitIdentifier cIdentifier) { 
        this.availableBackground = aSprite; 
        this.collectedBackground = cSprite; 
        this.identifier = cIdentifier; 
        this.isAvailable = true;  
    }

    /**
     * @return If the circuit is available. 
     */
    public boolean isAvailable() {
        return this.isAvailable; 
    }
    
    /**
     * @return The circuit identifier. 
     */
    public CircuitIdentifier getIdentifier() {
        return this.identifier; 
    }

    /**
     * Collects an available pulse.  
     */
    public void collect() { 
        assert this.isAvailable; 
        this.isAvailable = false; 
    }

    @Override
    public boolean isAccessibleTo(Unit unit) {
        return true;  
    }

    @Override
    public Sprite getSprite() {
        if (this.isAvailable) { 
            return this.availableBackground;
        } 
        return this.collectedBackground;
    }
}
