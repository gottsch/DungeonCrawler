package dungoncrawler.generator.cave;

import java.util.HashMap;
import java.util.Map;

import dungoncrawler.generator.ILevel;

/**
 * 
 * @author Mark Gottschling on Jun 21, 2020
 *
 */
public class CaveLevel implements ILevel {
	private boolean[][] cellMap;
	private Integer[][] idMap;
	private Map<Integer, ICave> caves;
	
	public CaveLevel() {}

	public boolean[][] getCellMap() {
		return cellMap;
	}

	public void setCellMap(boolean[][] cellMap) {
		this.cellMap = cellMap;
	}

	public Integer[][] getIdMap() {
		return idMap;
	}

	public void setIdMap(Integer[][] idMap) {
		this.idMap = idMap;
	}

	public Map<Integer, ICave> getCaves() {
		if (caves == null) {
			caves  = new HashMap<>();
		}
		return caves;
	}

	public void setCaves(Map<Integer, ICave> caves) {
		this.caves = caves;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setWidth(int width) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDepth(int depth) {
		// TODO Auto-generated method stub
		
	}
}
