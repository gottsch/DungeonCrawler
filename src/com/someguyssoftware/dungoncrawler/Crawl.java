/**
 * 
 */
package com.someguyssoftware.dungoncrawler;

import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.someguyssoftware.dungoncrawler.generator.cave.CaveLevelGenerator;

/**
 * @author Mark
 *
 */
@Deprecated
public class Crawl {

	/**
	 * @param args
	 */
	public static final void main(String[] args) {
		CaveLevelGenerator builder = new CaveLevelGenerator();
		Random random = new Random();
//		boolean[][] map = builder.build();
//
//		/*
//		 *  visualize the level
//		 */
//		// draw out rectangles
//		JFrame window = new JFrame();
//		
//		JLabel lblFName = new JLabel("First Name:");
//        JTextField tfFName = new JTextField(20);
//        lblFName.setLabelFor(tfFName);
//        
//        
//		JPanel panel = new LayerPanel(map);
//		panel.add(lblFName);
//		panel.add(tfFName);
//		
//		window.setTitle("Dungeon Crawler Layer Visualizer");
//		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		window.setBounds(0, 0, 1400, 750);
//		window.add(panel);
//		window.setVisible(true);
	}
}
