/**
 * 
 */
package com.someguyssoftware.dungoncrawler;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TextField;

import javax.swing.JPanel;

/**
 * @author Mark
 *
 */
public class LayerPanel extends JPanel {
	public static int CANVAS_WIDTH = 850;
	public static int CANVAS_HEIGHT = 650;
	public static int CANVAS_START_X = 250;
	public static int CANVAS_START_Y = 30;

	private boolean[][] map;
	
	/**
	 * 
	 */
	public LayerPanel(boolean[][] map) {
		this.map = map;
	}
		
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// create 2D graphics
		Graphics2D g2d = (Graphics2D)g.create();

		// setup the rendering hints
        RenderingHints rh =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);

        rh.put(RenderingHints.KEY_RENDERING,
               RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setRenderingHints(rh);
        
        // setup the title 
        g2d.setFont(new Font("Verdana", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        String title = "Dungeon Crawler Layer Visualizer";
        g2d.drawString(title, 400, 15);

        // draw map canvas area
        g2d.setColor(Color.BLACK);		
        g2d.fillRoundRect(CANVAS_START_X, CANVAS_START_Y, CANVAS_WIDTH, CANVAS_HEIGHT, 3, 3);
        
        int width = 5;
        int height = width;
        
        int boxX = CANVAS_START_X + 3;
        for (int x = 0; x < map.length; x++) {
            int boxY = CANVAS_START_Y + 3;
        	for (int y = 0; y < map[x].length; y++) {   
        		// alive = walls | solid
        		if (map[x][y]) {
        			g2d.setColor(Color.DARK_GRAY);
        		}
        		else {
        			g2d.setColor(Color.GREEN);
        		}
                g2d.fillRect(boxX, boxY, width, height);
                boxY += height;
        	}
        	boxX += width;
        }
	}
}
