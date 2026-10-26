package nl.tudelft.jpacman.ui;

import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import nl.tudelft.jpacman.board.CircuitIdentifier;
import nl.tudelft.jpacman.level.Circuit;

public class CircuitTimerPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel circuitOneLabel = new JLabel(" ");
    private final JLabel circuitTwoLabel = new JLabel(" ");

    private Circuit circuitOne;
    private Circuit circuitTwo;

    private final Timer refreshTimer;

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

        refreshTimer = new Timer(200, event -> refresh());
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
                "%02d:%02d", seconds / 60, seconds % 60
            );

            label.setText("Inversão (" + number + "): " + time);
        } else {
            // Keep the area reserved when the circuit is not inverted.
            label.setText(" ");
        }
    }

    public void dispose() {
        refreshTimer.stop();
    }
}