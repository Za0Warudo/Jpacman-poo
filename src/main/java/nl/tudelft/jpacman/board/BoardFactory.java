package nl.tudelft.jpacman.board;

import java.awt.Color;

import nl.tudelft.jpacman.board.models.GateSquare;
import nl.tudelft.jpacman.board.models.PulseSquare;
import nl.tudelft.jpacman.board.models.SwitchSquare;
import nl.tudelft.jpacman.sprite.MarkedSprite;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A factory that creates {@link Board} objects from 2-dimensional arrays of
 * {@link Square}s.
 *
 * @author Jeroen Roosen 
 */
public class BoardFactory {

    /**
     * The sprite store providing the sprites for the background.
     */
    private final PacManSprites sprites;

    /**
     * Creates a new BoardFactory that will create a board with the provided
     * background sprites.
     *
     * @param spriteStore
     *            The sprite store providing the sprites for the background.
     */
    public BoardFactory(PacManSprites spriteStore) {
        this.sprites = spriteStore;
    }

    /**
     * Creates a new board from a grid of cells and connects it.
     *
     * @param grid
     *            The square grid of cells, in which grid[x][y] corresponds to
     *            the square at position x,y.
     * @return A new board, wrapping a grid of connected cells.
     */
    public Board createBoard(Square[][] grid) {
        assert grid != null;

        Board board = new Board(grid);

        int width = board.getWidth();
        int height = board.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Square square = grid[x][y];
                for (Direction dir : Direction.values()) {
                    int dirX = (width + x + dir.getDeltaX()) % width;
                    int dirY = (height + y + dir.getDeltaY()) % height;
                    Square neighbour = grid[dirX][dirY];
                    square.link(neighbour, dir);
                }
            }
        }

        return board;
    }

    /**
     * Creates a new square that can be occupied by any unit.
     *
     * @return A new square that can be occupied by any unit.
     */
    public Square createGround() {
        return new Ground(sprites.getGroundSprite());
    }

    /**
     * Creates a new square that cannot be occupied by any unit.
     *
     * @return A new square that cannot be occupied by any unit.
     */
    public Square createWall() {
        return new Wall(sprites.getWallSprite());
    }

    
    /**
     * Creates a new switch square for circuit one.
     *
     * @return A new switch square for circuit one.
     */
    private SwitchSquare createSwitchSquareOne() {
        Sprite sprite = new MarkedSprite(
            sprites.getSwitchSprite(), Color.RED, "1");
        return new SwitchSquare(sprite, CircuitIdentifier.CIRCUIT_ONE);
    }

    /**
     * Creates a new switch square for circuit two.
     *
     * @return A new switch square for circuit two.
     */
    private SwitchSquare createSwitchSquareTwo() {
        Sprite sprite = new MarkedSprite(
            sprites.getSwitchSprite(), Color.GREEN, "2");
        return new SwitchSquare(sprite, CircuitIdentifier.CIRCUIT_TWO);
    }

    /**
     * Creates a switch square for the specified circuit.
     *
     * @param cIdentifier The switch circuit identifier.
     * @return A new switch square for the specified circuit.
     */
    public Square createSwitch(CircuitIdentifier cIdentifier) {
        if (cIdentifier == CircuitIdentifier.CIRCUIT_ONE) {
            return createSwitchSquareOne();
        }
        if (cIdentifier == CircuitIdentifier.CIRCUIT_TWO) {
            return createSwitchSquareTwo();
        }
        throw new IllegalArgumentException("Unknown circuit: " + cIdentifier);
    }

    /**
     * Creates a gate square for the specified circuit.
     *
     * @param cIdentifier The gate circuit identifier.
     * @return A new gate square for the specified circuit.
     */
    public Square createGate(CircuitIdentifier cIdentifier) {
        Sprite openSprite = sprites.getOpenGateSprite();

        if (cIdentifier == CircuitIdentifier.CIRCUIT_ONE) {
            Sprite closedSprite = new MarkedSprite(
                sprites.getCloseGateSprite(), Color.RED, "1");
            Sprite closingStateSprite = new MarkedSprite(
                sprites.getCloseGateSprite(), Color.WHITE, "P");

            return new GateSquare(
                openSprite, closedSprite, closingStateSprite, cIdentifier);
        }

        if (cIdentifier == CircuitIdentifier.CIRCUIT_TWO) {
            Sprite closedSprite = new MarkedSprite(
                sprites.getCloseGateSprite(), Color.GREEN, "2");
            Sprite closingStateSprite = new MarkedSprite(
                sprites.getCloseGateSprite(), Color.WHITE, "P");

            return new GateSquare(
                openSprite, closedSprite, closingStateSprite, cIdentifier);
        }

        throw new IllegalArgumentException("Unknown circuit: " + cIdentifier);
    }

    /**
     * Create a new square that is a pulse. 
     * @param cIdentifier
     *          The pulse circuit identifier. 
     * @return A new square that is a pulse. 
     */
    public Square createPulse(CircuitIdentifier cIdentifier) {        
        if (cIdentifier == CircuitIdentifier.CIRCUIT_ONE) {
            Sprite aSprite = new MarkedSprite(sprites.getAvailablePulseSprite(), Color.RED, "1");
            Sprite cSprite = sprites.getCollectedPulseSprite();
            return new PulseSquare(aSprite, cSprite , cIdentifier);
        }
        if (cIdentifier == CircuitIdentifier.CIRCUIT_TWO) { 
            Sprite aSprite = new MarkedSprite(sprites.getAvailablePulseSprite(), Color.GREEN, "1");
            Sprite cSprite = sprites.getCollectedPulseSprite();
            return new PulseSquare(aSprite, cSprite , cIdentifier);
        }
    
        throw new IllegalArgumentException("Unknown circuit: " + cIdentifier);
    }

    /**
     * A wall is a square that is inaccessible to anyone.
     *
     * @author Jeroen Roosen
     */
    private static final class Wall extends Square {

        /**
         * The background for this square.
         */
        private final Sprite background;

        /**
         * Creates a new wall square.
         *
         * @param sprite
         *            The background for the square.
         */
        Wall(Sprite sprite) {
            this.background = sprite;
        }

        @Override
        public boolean isAccessibleTo(Unit unit) {
            return false;
        }

        @Override
        public Sprite getSprite() {
            return background;
        }
    }

    /**
     * A ground square is a square that is accessible to anyone.
     *
     * @author Jeroen Roosen
     */
    private static final class Ground extends Square {

        /**
         * The background for this square.
         */
        private final Sprite background;

        /**
         * Creates a new ground square.
         *
         * @param sprite
         *            The background for the square.
         */
        Ground(Sprite sprite) {
            this.background = sprite;
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
}
