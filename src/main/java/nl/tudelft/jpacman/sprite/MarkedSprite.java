package nl.tudelft.jpacman.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;

public final class MarkedSprite implements Sprite {

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
     * A new Srite with a color and label overlay. 
     * @param base
     * @param markerColor
     * @param label
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
        int markerWidth = Math.max(1, width / 4);
        int markerHeight = Math.max(1, height / 4);

        int marginX = width / 20;
        int marginY = height / 20;

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