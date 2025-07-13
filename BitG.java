package Bit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class BitG extends JPanel {
    private List<JPanel> upperBoxes = new ArrayList<>();
    private List<JLabel> upperLabels = new ArrayList<>();
    private List<JLabel> lowerLabels = new ArrayList<>();
    private int currentIndex = 9;
    private int intervalMillis;
    private Timer timer;
    private boolean gameCleared = false;
    private int trialCount = 0;
    private JLabel trialLabel;

    public BitG(int intervalMillis) {
        this.intervalMillis = intervalMillis;
        setLayout(new BorderLayout());

        // 試行回数ラベル
        trialLabel = new JLabel("試行回数: 0 回", SwingConstants.CENTER);
        trialLabel.setFont(new Font("MS Gothic", Font.BOLD, 28));
        trialLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        add(trialLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridy = 0;
        gbc.insets = new Insets(80, 0, 20, 0);
        JPanel upperRow = createRow(false);
        centerPanel.add(upperRow, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel lowerRow = createRow(true);
        centerPanel.add(lowerRow, gbc);

        add(centerPanel, BorderLayout.CENTER);

        timer = new Timer(intervalMillis, e -> {
            if (currentIndex < 0) return;

            // 試行回数を加算
            trialCount++;
            trialLabel.setText("試行回数: " + trialCount + " 回");

            int[] randomValues = new int[upperLabels.size()];
            for (int i = 0; i < upperLabels.size(); i++) {
                JPanel box = upperBoxes.get(i);
                if (isUnlocked(box)) {
                    randomValues[i] = new Random().nextInt(2);
                } else {
                    randomValues[i] = -1;
                }
            }

            for (int i = 0; i < upperLabels.size(); i++) {
                if (randomValues[i] != -1) {
                    upperLabels.get(i).setText(String.valueOf(randomValues[i]));
                }
            }

            boolean allMatch = true;
            Color matchColor = new Color(144, 238, 144);

            for (int i = currentIndex; i < upperLabels.size(); i++) {
                String up = upperLabels.get(i).getText();
                String down = lowerLabels.get(i).getText();

                JPanel upperBox = upperBoxes.get(i);
                JPanel lowerBox = (JPanel) lowerLabels.get(i).getParent();

                if (up.equals(down)) {
                    lowerBox.setBackground(matchColor);
                } else {
                    upperBox.setBackground(Color.WHITE);
                    lowerBox.setBackground(Color.WHITE);
                    allMatch = false;
                }
            }

            if (allMatch) {
                unlockNext();
            }
        });
        timer.start();
    }

    private JPanel createRow(boolean isBottomRow) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        for (int i = 0; i < 10; i++) {
            boolean isInteractive = (i == 9);
            JPanel box = createBox(isInteractive, isBottomRow, i);
            row.add(box);
        }
        return row;
    }

    private JPanel createBox(boolean interactive, boolean isBottomRow, int index) {
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(100, 100));
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        box.setLayout(new BorderLayout());

        JLabel label = new JLabel();
        label.setFont(new Font("MS Gothic", Font.BOLD, 36));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        if (interactive) {
            if (isBottomRow) {
                label.setText("0");
                label.setForeground(Color.BLACK);
                box.setBackground(Color.WHITE);
                box.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        String cur = label.getText();
                        label.setText(cur.equals("0") ? "1" : "0");
                    }
                });
            } else {
                label.setText("");
                label.setForeground(Color.BLACK);
                box.setBackground(Color.WHITE);
            }
        } else {
            label.setText("✕");
            label.setForeground(Color.DARK_GRAY);
            box.setBackground(Color.GRAY);
        }

        box.add(label, BorderLayout.CENTER);

        if (isBottomRow) {
            lowerLabels.add(label);
        } else {
            upperLabels.add(label);
            upperBoxes.add(box);
        }

        return box;
    }

    private void unlockNext() {
        currentIndex--;

        if (currentIndex < 0) {
            gameCleared = true;
            return;
        }

        JLabel up = upperLabels.get(currentIndex);
        JPanel upBox = upperBoxes.get(currentIndex);
        up.setText("");
        up.setForeground(Color.BLACK);
        upBox.setBackground(Color.WHITE);

        JLabel down = lowerLabels.get(currentIndex);
        down.setText("0");
        down.setForeground(Color.BLACK);
        JPanel downBox = (JPanel) down.getParent();
        downBox.setBackground(Color.WHITE);

        downBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String cur = down.getText();
                down.setText(cur.equals("0") ? "1" : "0");
            }
        });
    }

    private boolean isUnlocked(JPanel box) {
        return box.getBackground().equals(Color.WHITE);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        for (int i = 9; i >= index; i--) {
            JPanel upBox = upperBoxes.get(i);
            JLabel upLabel = upperLabels.get(i);
            JPanel downBox = (JPanel) lowerLabels.get(i).getParent();
            JLabel downLabel = lowerLabels.get(i);

            upBox.setBackground(Color.WHITE);
            upLabel.setText("");
            upLabel.setForeground(Color.BLACK);

            downBox.setBackground(Color.WHITE);
            downLabel.setText("0");
            downLabel.setForeground(Color.BLACK);

            downBox.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    String cur = downLabel.getText();
                    downLabel.setText(cur.equals("0") ? "1" : "0");
                }
            });
        }
        this.currentIndex = index;
    }

    public List<String> getTargetNumbers() {
        List<String> values = new ArrayList<>();
        for (JLabel label : lowerLabels) {
            values.add(label.getText());
        }
        return values;
    }

    public void restoreTargetNumbers(List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            JLabel label = lowerLabels.get(i);
            label.setText(values.get(i));

            JPanel box = (JPanel) label.getParent();
            for (MouseListener listener : box.getMouseListeners()) {
                box.removeMouseListener(listener);
            }

            if (box.getBackground().equals(Color.WHITE)) {
                label.setForeground(Color.BLACK);
                box.setBackground(Color.WHITE);

                box.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        String cur = label.getText();
                        label.setText(cur.equals("0") ? "1" : "0");
                    }
                });
            }
        }
    }

    public int getUnlockedBlockCount() {
        int count = 0;
        for (JPanel box : upperBoxes) {
            if (box.getBackground().equals(Color.WHITE)) {
                count++;
            }
        }
        return count;
    }

    public boolean isCleared() {
        return gameCleared;
    }

    public int getTrialCount() {
        return trialCount;
    }

    public void setTrialCount(int count) {
        this.trialCount = count;
        if (trialLabel != null) {
            trialLabel.setText("試行回数: " + trialCount + " 回");
        }
    }
}
