package com.someguyssoftware.dungoncrawler.generator.cave;

import java.util.List;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;

public interface ICave {

	Coords2D getCoords();

	void setCoords(Coords2D coords);

	int getWidth();

	void setWidth(int width);

	int getHeight();

	void setHeight(int height);

	int getId();

	void setId(int id);

	List<Coords2D> getCells();

	void setCells(List<Coords2D> cells);

	Coords2D getCenter();

}