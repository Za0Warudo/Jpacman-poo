package nl.tudelft.jpacman.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;

/**
 * A sprite with a colored marker and text label overlaid on its base sprite.
 */
public final class MarkedSprite implements Sprite {

    private static final int MARKER_SIZE_DIVISOR = 4;
    private static final int MARKER_MARGIN_DIVISOR = 20;

    /**
     * Base sprite.
     */
    private final Sprite base;
    
    /**
     * Sprite color overlay. 
     */
    private final Color markerColor;

    /**
     * Sprite label overlay. 
     */
    private final String label;

    /**
     * Creates a sprite with a color and label overlay.
     *
     * @param base
     *            The sprite to draw beneath the marker.
     * @param markerColor
     *            The marker fill color.
     * @param label
     *            The text to draw on the marker.
     */
    public MarkedSprite(Sprite base, Color markerColor, String label) {
        this.base = base;
        this.markerColor = markerColor;
        this.label = label;
    }

    @Override
    public void draw(Graphics graphics, int x, int y,
                 int width, int height) {
        // Draw the original sprite at its requested size.
        base.draw(graphics, x, y, width, height);

        // Scale the marker proportionally to the tile.
        int markerWidth = Math.max(1, width / MARKER_SIZE_DIVISOR);
        int markerHeight = Math.max(1, height / MARKER_SIZE_DIVISOR);

        int marginX = width / MARKER_MARGIN_DIVISOR;
        int marginY = height / MARKER_MARGIN_DIVISOR;

        int markerX = x + marginX;
        int markerY = y + marginY;

        graphics.setColor(markerColor);
        graphics.fillRect(
            markerX, markerY, markerWidth, markerHeight);

        graphics.setColor(Color.BLACK);

        // Center the label within the marker.
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = markerX + (markerWidth - metrics.stringWidth(label)) / 2;
        int textY = markerY
            + (markerHeight - metrics.getHeight()) / 2
            + metrics.getAscent();

        graphics.drawString(label, textX, textY);
    }

    @Override
    public Sprite split(int x, int y, int width, int height) {
        return new MarkedSprite(
            base.split(x, y, width, height),
            markerColor,
            label
        );
    }

    @Override
    public int getWidth() {
        return base.getWidth();
    }

    @Override
    public int getHeight() {
        return base.getHeight();
    }
}