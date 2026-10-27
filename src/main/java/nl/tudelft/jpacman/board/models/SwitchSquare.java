package nl.tudelft.jpacman.board.models;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A switch square is a square that has a switch logic.  
 */
public class SwitchSquare extends Square { 

    /**
     * The background for this square.
     */
    private final Sprite background;

    /**
     * The circuit identifier. 
     */
    private final CircuitIdentifier identifier; 

    /**
     * Creates a new switch square.
     *
     * @param sprite
     *            The background for the square.
     * @param cIdentifier
     *            The circuit idenfier. 
     */
    public SwitchSquare(Sprite sprite, CircuitIdentifier cIdentifier) {
        this.background = sprite;
        this.identifier = cIdentifier; 
    }

    /**
    * @return The gate circuit identifier. 
    */
    public CircuitIdentifier getIdentifier() {
        return identifier; 
    }

    @Override
    public boolean isAccessibleTo(Unit unit) {
        return true;
    }

    @Override
    public Sprite getSprite() {
        return background;
    }
}
