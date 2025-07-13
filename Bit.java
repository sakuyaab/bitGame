package Bit;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Bit {
    public static void main(String[] args) {
        int initialIntervalMillis = 10000;

        JFrame frame = new JFrame("Bit Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1800, 1200);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel speedLabel = new JLabel("現在の生成速度: " + (initialIntervalMillis / 1000.0) + "秒", SwingConstants.CENTER);
        speedLabel.setFont(new Font("MS Gothic", Font.BOLD, 36));
        speedLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        JLabel efficiencyLabel = new JLabel("現在のポイント獲得効率:0.01 pt/s", SwingConstants.CENTER);
        efficiencyLabel.setFont(new Font("MS Gothic", Font.PLAIN, 28));
        efficiencyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel bestRecordLabel = new JLabel("最小試行回数: 取得中 / 最短時間: 取得中", SwingConstants.RIGHT);
        bestRecordLabel.setFont(new Font("MS Gothic", Font.PLAIN, 24));
        bestRecordLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
        leftTopPanel.add(speedLabel);
        leftTopPanel.add(efficiencyLabel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(leftTopPanel, BorderLayout.CENTER);
        topPanel.add(bestRecordLabel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        final int[] intervalMillis = { initialIntervalMillis };
        final BitG[] bitPanel = { new BitG(intervalMillis[0]) };
        mainPanel.add(bitPanel[0], BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        JLabel pointLabel = new JLabel("Point: 0");
        pointLabel.setFont(new Font("MS Gothic", Font.PLAIN, 36));
        bottomPanel.add(pointLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton button1 = new JButton("乱数生成速度 ↑（1pt）");
        JButton button2 = new JButton("pt獲得速度 ↑（1pt）");
        JButton button3 = new JButton("pt獲得量 ↑（1pt）");

        Font buttonFont = new Font("MS Gothic", Font.PLAIN, 28);
        button1.setFont(buttonFont);
        button2.setFont(buttonFont);
        button3.setFont(buttonFont);

        buttonPanel.add(button1);
        buttonPanel.add(button2);
        buttonPanel.add(button3);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        JLabel timerLabel = new JLabel("タイマー: 0分 0秒", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("MS Gothic", Font.PLAIN, 36));
        bottomPanel.add(timerLabel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(mainPanel);
        frame.setVisible(true);

        final int[] secondsPassed = { 0 };
        final double[] getIntervalThreshold = { 10.0 };
        final double[] intervalGetPoint = { 0.0 };
        final int[] point = { 0 };
        final int[] getAmount = { 1 };
        final int[] costButton1 = { 1 };
        final int[] costButton2 = { 1 };
        final int[] costButton3 = { 1 };

        final Timer[] timer = new Timer[1];

        timer[0] = new Timer(1000, e -> {
            if (bitPanel[0].isCleared()) {
                timer[0].stop();
                efficiencyLabel.setText("クリア！おめでとう！");

                new Thread(() -> {
                    try {
                        int trialCount = bitPanel[0].getTrialCount();
                        int seconds = secondsPassed[0];
                        SheetLogger.appendLog(trialCount, seconds);
                        SheetLogger.writeSummary(trialCount, seconds);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
                return;
            }

            secondsPassed[0]++;
            intervalGetPoint[0] += 1.0;

            int minutes = secondsPassed[0] / 60;
            int seconds = secondsPassed[0] % 60;
            timerLabel.setText(String.format("タイマー: %d分 %d秒", minutes, seconds));

            if (intervalGetPoint[0] >= getIntervalThreshold[0]) {
                intervalGetPoint[0] = 0;

                int unlockedCount = bitPanel[0].getUnlockedBlockCount();
                int earnedPoint = getAmount[0] * unlockedCount;
                double intervalSec = Math.max(getIntervalThreshold[0], 0.1);
                double pointsPerSecond = (earnedPoint / intervalSec);

                if (earnedPoint > 0) {
                    point[0] += earnedPoint;
                    pointLabel.setText("Point: " + point[0]);
                }

                efficiencyLabel.setText(String.format("現在のポイント獲得効率: %.2f pt/s", pointsPerSecond));
            }
        });

        timer[0].start();

        button1.addActionListener(e -> {
            if (point[0] < costButton1[0])
                return; // ポイント不足なら処理中断

            // 速度減少（高速化）
            intervalMillis[0] -= 500;

            // 下限に達したかチェック
            if (intervalMillis[0] <= 100) {
                intervalMillis[0] = 100;
                button1.setText("MAX");
            } else {
                point[0] -= costButton1[0];
                costButton1[0] *= 2;
                button1.setText("乱数生成速度 ↑（" + costButton1[0] + "pt）");
            }

            pointLabel.setText("Point: " + point[0]);
            speedLabel.setText("現在の生成速度: " + (intervalMillis[0] / 1000.0) + "秒");

            // 状態保持＆再生成
            int previousIndex = bitPanel[0].getCurrentIndex();
            List<String> savedTargetNumbers = bitPanel[0].getTargetNumbers();
            int savedTrialCount = bitPanel[0].getTrialCount();

            bitPanel[0].stopTimer();
            mainPanel.remove(bitPanel[0]);

            bitPanel[0] = new BitG(intervalMillis[0]);
            bitPanel[0].setCurrentIndex(previousIndex);
            bitPanel[0].restoreTargetNumbers(savedTargetNumbers);
            bitPanel[0].setTrialCount(savedTrialCount);

            mainPanel.add(bitPanel[0], BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        button2.addActionListener(e -> {
            if (getIntervalThreshold[0] <= 0.5) {
                getIntervalThreshold[0] = 0.5;
                button2.setText("MAX");
                return;
            }

            if (point[0] < costButton2[0])
                return;

            point[0] -= costButton2[0];
            costButton2[0] += 5;
            getIntervalThreshold[0] -= 1.5;

            if (getIntervalThreshold[0] <= 0.5) {
                getIntervalThreshold[0] = 0.5;
                button2.setText("MAX");
            }

            pointLabel.setText("Point: " + point[0]);
            button2.setText("pt獲得速度 ↑（" + costButton2[0] + "pt）");
        });

        button3.addActionListener(e -> {
            if (point[0] < costButton3[0])
                return;

            point[0] -= costButton3[0];
            costButton3[0] += 2;
            getAmount[0]++;

            pointLabel.setText("Point: " + point[0]);
            button3.setText("pt獲得量 ↑（" + costButton3[0] + "pt）");
        });

        // ★ スプレッドシートからベスト記録を取得して表示
        new Thread(() -> {
            try {
                String[] bests = SheetLogger.getBestRecords();
                SwingUtilities.invokeLater(() -> {
                    bestRecordLabel.setText("最小試行回数: " + bests[0] + " / 最短時間: " + bests[1]);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
