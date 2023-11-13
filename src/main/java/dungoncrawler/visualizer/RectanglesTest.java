package dungoncrawler.visualizer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RectanglesTest extends JPanel {

	List<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();
	{
		Random random = new Random();
		int midX = 150;
		int midZ = 150;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				// TODO instead of a square, use a circle
				int offsetX = random.nextInt(50) - 25;
				int offsetZ = random.nextInt(50) - 25;
				int sizeX = random.nextInt(30) + 5;
				int sizeZ = random.nextInt(30) + 5;
				rectangles.add(new Rectangle2D.Float(midX + offsetX, midZ + offsetZ, sizeX, sizeZ));
			}
		}
	}

	List<Rectangle2D> rectanglesToDraw;

	protected void reset() {
		rectanglesToDraw = rectangles;

		this.repaint();
	}

	private List<Rectangle2D> findIntersections(Rectangle2D rect, List<Rectangle2D> rectList) {

		ArrayList<Rectangle2D> intersections = new ArrayList<Rectangle2D>();

		for (Rectangle2D intersectingRect : rectList) {
			if (!rect.equals(intersectingRect) && intersectingRect.intersects(rect)) {
				intersections.add(intersectingRect);
			}
		}

		return intersections;
	}

	protected void fix() {
		rectanglesToDraw = new ArrayList<Rectangle2D>();

		for (Rectangle2D rect : rectangles) {
			Rectangle2D copyRect = new Rectangle2D.Double();
			copyRect.setRect(rect);
			rectanglesToDraw.add(copyRect);
		}

		// Find the center C of the bounding box of your rectangles.
		Rectangle2D surroundRect = surroundingRect(rectanglesToDraw);
		Point center = new Point((int) surroundRect.getCenterX(), (int) surroundRect.getCenterY());

		// this value dictates how much to move a R at a time. lower number mean the Rs
		// will be closer together but more iterations.
		int movementFactor = 1;

		boolean hasIntersections = true;

		while (hasIntersections) {

			hasIntersections = false;

			for (Rectangle2D rect : rectanglesToDraw) {

				// Find all the rectangles R' that overlap R.
				List<Rectangle2D> intersectingRects = findIntersections(rect, rectanglesToDraw);

				if (intersectingRects.size() > 0) {

					// Define a movement vector v.
					Point movementVector = new Point(0, 0);

					Point centerR = new Point((int) rect.getCenterX(), (int) rect.getCenterY());

					// For each rectangle R that overlaps another.
					for (Rectangle2D rPrime : intersectingRects) {
						Point centerRPrime = new Point((int) rPrime.getCenterX(), (int) rPrime.getCenterY());

						int xTrans = (int) (centerR.getX() - centerRPrime.getX());
						int yTrans = (int) (centerR.getY() - centerRPrime.getY());

						// Add a vector to v proportional to the vector between the center of R and R'.
						movementVector.translate(xTrans < 0 ? -movementFactor : movementFactor,
								yTrans < 0 ? -movementFactor : movementFactor);

					}

					int xTrans = (int) (centerR.getX() - center.getX());
					int yTrans = (int) (centerR.getY() - center.getY());

					// Add a vector to v proportional to the vector between C and the center of R.
					movementVector.translate(xTrans < 0 ? -movementFactor : movementFactor,
							yTrans < 0 ? -movementFactor : movementFactor);

					// Move R by v.
					rect.setRect(rect.getX() + movementVector.getX(), rect.getY() + movementVector.getY(),
							rect.getWidth(), rect.getHeight());

					// Repeat until nothing overlaps.
					hasIntersections = true;
				}

			}
		}
		this.repaint();
	}

	private Rectangle2D surroundingRect(List<Rectangle2D> rectangles) {

		Point topLeft = null;
		Point bottomRight = null;

		for (Rectangle2D rect : rectangles) {
			if (topLeft == null) {
				topLeft = new Point((int) rect.getMinX(), (int) rect.getMinY());
			} else {
				if (rect.getMinX() < topLeft.getX()) {
					topLeft.setLocation((int) rect.getMinX(), topLeft.getY());
				}

				if (rect.getMinY() < topLeft.getY()) {
					topLeft.setLocation(topLeft.getX(), (int) rect.getMinY());
				}
			}

			if (bottomRight == null) {
				bottomRight = new Point((int) rect.getMaxX(), (int) rect.getMaxY());
			} else {
				if (rect.getMaxX() > bottomRight.getX()) {
					bottomRight.setLocation((int) rect.getMaxX(), bottomRight.getY());
				}

				if (rect.getMaxY() > bottomRight.getY()) {
					bottomRight.setLocation(bottomRight.getX(), (int) rect.getMaxY());
				}
			}
		}

		return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(),
				bottomRight.getY() - topLeft.getY());
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		for (Rectangle2D entry : rectanglesToDraw) {
			g2d.setStroke(new BasicStroke(1));
			// g2d.fillRect((int) entry.getX(), (int) entry.getY(), (int) entry.getWidth(),
			// (int) entry.getHeight());
			g2d.draw(entry);
		}

	}

	protected static void createAndShowGUI() {
		RectanglesTest rects = new RectanglesTest();

		rects.reset();

		JFrame frame = new JFrame("Rectangles");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(rects, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel();

		JButton fix = new JButton("Fix");

		fix.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rects.fix();

			}
		});

		JButton resetButton = new JButton("Reset");

		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rects.reset();
			}
		});

		buttonsPanel.add(fix);
		buttonsPanel.add(resetButton);

		frame.add(buttonsPanel, BorderLayout.SOUTH);

		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				createAndShowGUI();

			}
		});
	}

}
