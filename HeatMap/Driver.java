/*
* @Author: Patrick Manacorda
* @Date: 2/13/2020
* @See: Seattle University CPSC 5600 - HW5
*/
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;


public class Driver {
    private static final int DIM = 20;
    private static final int SLEEP_INTERVAL = 50; // milliseconds
    private static final String FILENAME = "observation_gaussian.dat";
    private static final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;
    private static final double HOT_CALIB = 1.0;
    private static final String REPLAY = "Replay";
    private static JFrame application;
    private static JButton button;
    private static Color[][] grid;
    private static int current;
    private static List<HeatMap> heatmaps;
	public static void main(String[] args){
		String filename = "Observation.txt";
		List<Observation> observations = new ArrayList<Observation>();
		int numObservations = 0;
		int DIM = 20;
		int N_THREADS = 2;
		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			numObservations = 0;
			Observation obs = (Observation) in.readObject();
			while (!obs.isEOF()){
				observations.add(obs);
				obs = (Observation) in.readObject();
                                numObservations++;
			}
			in.close();
		} catch (IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
                System.out.println("Num Observations: " + numObservations );
		GenScan<Observation, HeatMap> handle = 
			new GenScan<Observation, HeatMap>(observations, N_THREADS, new HeatMap(DIM));
                heatmaps = handle.scan();
                System.out.println("Num objects in scan: " + heatmaps.size());
                current = 0;

                grid = new Color[DIM][DIM];
                application = new JFrame();
                application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                fillGrid(grid);

                ColoredGrid gridPanel = new ColoredGrid(grid);
                application.add(gridPanel, BorderLayout.CENTER);

                button = new JButton(REPLAY);
                button.addActionListener(new BHandler());
                application.add(button, BorderLayout.PAGE_END);

                application.setSize(DIM * 40, (int) (DIM * 40.4));
                application.setVisible(true);
                application.repaint();
                try {
                    animate();
                }catch(InterruptedException ex){}
        }

    private static void animate() throws InterruptedException {
        button.setEnabled(false);
        for (current = 0; current < heatmaps.size(); current++) {
            fillGrid(grid);
            application.repaint();
            Thread.sleep(SLEEP_INTERVAL);
        }
        button.setEnabled(true);
        application.repaint();
    }

    static class BHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (REPLAY.equals(e.getActionCommand())) {
                new Thread() {
                    public void run() {
                        try {
                            animate();
                        } catch (InterruptedException e) {
                            System.exit(0);
                        }
                    }
                }.start();
            }
        }
    };

    private static void fillGrid(Color[][] grid) {
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[r].length; c++)
                grid[r][c] = interpolateColor(heatmaps.get(current).getCell(r, c) / HOT_CALIB, COLD, HOT);
    }

    private static Color interpolateColor(double ratio, Color a, Color b) {
        ratio = Math.min(ratio, 1.0);
        int ax = a.getRed();
        int ay = a.getGreen();
        int az = a.getBlue();
        int cx = ax + (int) ((b.getRed() - ax) * ratio);
        int cy = ay + (int) ((b.getGreen() - ay) * ratio);
        int cz = az + (int) ((b.getBlue() - az) * ratio);
        return new Color(cx, cy, cz);
    }
}
