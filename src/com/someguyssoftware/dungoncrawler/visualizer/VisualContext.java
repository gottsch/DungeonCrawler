package com.someguyssoftware.dungoncrawler.visualizer;

import javafx.scene.layout.Pane;

/**
 *
 */
public class VisualContext {
    private Pane bgBox;
    private Pane gridBox;
    private Pane spawnBoundary;
    private Pane centerPoint;

    public Pane getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(Pane centerPoint) {
        this.centerPoint = centerPoint;
    }

    public Pane getBgBox() {
        return bgBox;
    }

    public void setBgBox(Pane bgBox) {
        this.bgBox = bgBox;
    }

    public Pane getGridBox() {
        return gridBox;
    }

    public void setGridBox(Pane gridBox) {
        this.gridBox = gridBox;
    }

    public Pane getSpawnBoundary() {
        return spawnBoundary;
    }

    public void setSpawnBoundary(Pane spawnBoundary) {
        this.spawnBoundary = spawnBoundary;
    }
}
