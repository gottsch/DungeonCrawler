/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Gottschling on Sep 18, 2020
 *
 */
public abstract class AbstractGraphLevelGenerator implements ILevelGenerator {

	// TODO finish
	
	/**
	 * 
	 * @param sourceNodes
	 * @param movementFactor dictates how much to move a R at a time. lower number mean the Rs will be closer together but more iterations performed
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<? super INode> separateNodes(List<? extends INode> sourceNodes, int movementFactor) {
		// duplicate the soure rooms
		List<? super INode> resultantNodes = new ArrayList<>(sourceNodes);

		// find the center C of the bounding box of all the nodes.
		Coords2D center = getCenter((List<? extends INode>)resultantNodes);

		boolean hasIntersections = true;
		while (hasIntersections) {			
			hasIntersections = iterateSeparationStep(center, (List<? extends INode>)resultantNodes, movementFactor);
		}
		
		return resultantNodes;
	}
	
	/**
	 * 
	 * @param nodes
	 * @return
	 */
	public Coords2D getCenter(List<? extends INode> nodes) {
		Rectangle2D boundingBox = getBoundingBox(nodes);
		Coords2D center = boundingBox.getCenter();
		return center;
	}
	
	/**
	 * 
	 * @param nodes
	 * @return
	 */
	private Rectangle2D getBoundingBox(List<? extends INode> nodes) {
		Coords2D topLeft = null;
		Coords2D bottomRight = null;

		for (INode node : nodes) {
			if (topLeft == null) {
				topLeft = new Coords2D(node.getOrigin().getX(), node.getOrigin().getY());
			} else {
				if (node.getOrigin().getX() < topLeft.getX()) {
					topLeft.setLocation(node.getOrigin().getX(), topLeft.getY());
				}

				if (node.getOrigin().getY() < topLeft.getY()) {
					topLeft.setLocation(topLeft.getX(), node.getOrigin().getY());
				}
			}

			if (bottomRight == null) {
				bottomRight = new Coords2D(node.getMaxX() , (int) node.getMaxY());
			} else {
				if (node.getMaxX() > bottomRight.getX()) {
					bottomRight.setLocation((int) node.getMaxX(), bottomRight.getY());
				}

				if (node.getMaxY() > bottomRight.getY()) {
					bottomRight.setLocation(bottomRight.getX(), (int) node.getMaxY());
				}
			}
		}

		return new Rectangle2D(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(),
				bottomRight.getY() - topLeft.getY());
	}
	
	/**
	 * 
	 * @param nodes
	 * @return
	 */
	public boolean iterateSeparationStep(Coords2D center, List<? extends INode> nodes, int movementFactor) {
		boolean hasIntersections = false;
		
		// TODO add checks for anchor rooms ex. start and end
		
		for (INode node : nodes) {
			// find all the rectangles R' that overlap R.
			List<? extends INode> intersectingRooms = (List<? extends INode>)(List<?>)findIntersections(node, nodes);
			
			if (intersectingRooms.size() > 0) {

				// Define a movement vector v.
				Coords2D movementVector = new Coords2D(0, 0);
				
				Coords2D centerR = new Coords2D(node.getCenter());
				
				// for each rectangle R that overlaps another.
				for (INode rPrime : intersectingRooms) {
					Coords2D centerRPrime = new Coords2D(rPrime.getCenter());

					int translatedX = centerR.getX() - centerRPrime.getX();
					int translatedY = centerR.getY() - centerRPrime.getY();

					// TODO this statement is not exactly true. it is not "proportional", but increments by 1 (or the movementFactor)
					// add a vector to v proportional to the vector between the center of R and R'.
					movementVector.translate(translatedX < 0 ? -movementFactor : movementFactor,
							translatedY < 0 ? -movementFactor : movementFactor);
				}
				
				int translatedX = centerR.getX() - center.getX();
				int translatedY = centerR.getY() - center.getY();

				// add a vector to v proportional to the vector between C and the center of R.
				movementVector.translate(translatedX < 0 ? -movementFactor : movementFactor,
						translatedY < 0 ? -movementFactor : movementFactor);

				// translate R by v.
				node.setOrigin(new Coords2D(node.getOrigin().getX() + movementVector.getX(), node.getOrigin().getY() + movementVector.getY()));

				// repeat until nothing overlaps.
				hasIntersections = true;
			}
		}
		return hasIntersections;
	}
	
	public abstract List<? super INode> findIntersections(INode node, List<? extends INode> nodes);
	
	@Override
	public abstract ILevel build();

	@Override
	public ILevelGenerator withWidth(int width) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILevelGenerator withHeight(int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILevel init() {
		// TODO Auto-generated method stub
		return null;
	}

}
