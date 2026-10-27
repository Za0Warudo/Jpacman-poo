package nl.tudelft.jpacman.ui;

import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.level.Circuit;

/**
 * Displays active pulse countdowns for the level's circuits.
 */
public class CircuitTimerPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int REFRESH_INTERVAL_MILLISECONDS = 200;
    private static final int SECONDS_PER_MINUTE = 60;

    private final JLabel circuitOneLabel = new JLabel(" ");
    private final JLabel circuitTwoLabel = new JLabel(" ");

    private Circuit circuitOne;
    private Circuit circuitTwo;

    private final Timer refreshTimer;

    /**
     * Creates a timer panel for the provided circuits.
     *
     * @param circuits
     *            The circuits whose pulse timers should be displayed.
     */
    public CircuitTimerPanel(List<Circuit> circuits) {
        super(new GridLayout(1, 2));

        circuitOneLabel.setHorizontalAlignment(SwingConstants.LEFT);
        circuitTwoLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Fixed area for each circuit, even when its timer is inactive.
        add(circuitOneLabel);
        add(circuitTwoLabel);

        for (Circuit circuit : circuits) {
            if (circuit.getIdentifier() == CircuitIdentifier.CIRCUIT_ONE) {
                circuitOne = circuit;
            } else if (circuit.getIdentifier() == CircuitIdentifier.CIRCUIT_TWO) {
                circuitTwo = circuit;
            }
        }

        refreshTimer = new Timer(REFRESH_INTERVAL_MILLISECONDS,
            event -> refresh());
        refresh();
        refreshTimer.start();
    }

    private void refresh() {
        updateLabel(circuitOne, circuitOneLabel, 1);
        updateLabel(circuitTwo, circuitTwoLabel, 2);
    }

    private void updateLabel(Circuit circuit, JLabel label, int number) {
        if (circuit == null) {
            label.setText(" ");
            return;
        }

        int seconds = circuit.getRemainingSeconds();

        if (seconds > 0) {
            String time = String.format(
                "%02d:%02d", seconds / SECONDS_PER_MINUTE,
                seconds % SECONDS_PER_MINUTE
            );

            label.setText("Inversão (" + number + "): " + time);
        } else {
            // Keep the area reserved when the circuit is not inverted.
            label.setText(" ");
        }
    }

    /**
     * Stops refreshing the panel.
     */
    public void dispose() {
        refreshTimer.stop();
    }
}