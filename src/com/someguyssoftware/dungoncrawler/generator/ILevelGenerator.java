/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Mark
 *
 */
public interface ILevelGenerator {

	// TODO may need to change boolean[][] to some sort of class array
	/**
	 * 
	 * @return
	 */
	ILevel build();

	ILevelGenerator withWidth(int width);

	ILevelGenerator withHeight(int height);

	ILevel init();

	/**
	 * It is assumed that the rooms list is sorted in some fashion or the caller has a method to map the matrix indices back to a room object
	 * @param nodes
	 * @return
	 */
	public static /*double[][]*/Map<String, Double> getDistanceMatrix(List<? extends INode> nodes) {
        Map<String, Double> distanceMap = new HashMap<>();
		double[][] matrix = new double[nodes.size()][nodes.size()];

		for (int i = 0; i < nodes.size(); i++) {
			INode node1 = nodes.get(i);
			for (int j = 0; j < nodes.size(); j++) {
				INode node2 = nodes.get(j);
				if (node1 == node2) {
                    matrix[i][j] = 0.0;
                    distanceMap.put(getKey(node1, node2), Double.valueOf(0.0));
				}
				else {
                    //if (matrix[i][j] == 0.0) {
                    if (distanceMap.get(getKey(node1, node2)) == 0.0) {
						// calculate distance;
						double dist = node1.getCenter().getDistance(node2.getCenter());
						matrix[i][j] = dist;
                        matrix[j][i] = dist;
                        distanceMap.put(getKey(node1, node2), Double.valueOf(dist));
                        distanceMap.put(getKey(node2, node1), Double.valueOf(dist));
					}
				}
			}
		}
        // return matrix;
        return distanceMap;
	}

    static String getKey(INode node1, INode node2) {
        return node1.getId() + ":" + node2.getId();
    }
}
