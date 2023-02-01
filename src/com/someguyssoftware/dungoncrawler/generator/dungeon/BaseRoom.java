package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Mark Gottschling on Jun 23, 2022
 *
 */
public abstract class BaseRoom implements IRoom {

	// are relative to the room (coords)
	private Map<Direction2D, List<IConnector>> connectors; 
	
	public BaseRoom() {
	}

	@Override
	public boolean hasConnectors() {
		return connectors != null && !connectors.isEmpty();
	}
	
	@Override
	public Map<Direction2D, List<IConnector>> getConnectors() {
		if (connectors == null) {
			connectors = new HashMap<>();
		}
		return connectors;
	}

	public void setConnectors(Map<Direction2D, List<IConnector>> connectors) {
		this.connectors = connectors;
	}
}
