/**
 * 
 */
package com.someguyssoftware.dungoncrawler.visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.someguyssoftware.dungoncrawler.generator.Axis;
import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.dungeon.Corridor;
import com.someguyssoftware.dungoncrawler.generator.dungeon.DungeonLevel;
import com.someguyssoftware.dungoncrawler.generator.dungeon.DungeonLevelGenerator;
import com.someguyssoftware.dungoncrawler.generator.dungeon.IRoom;
import com.someguyssoftware.dungoncrawler.generator.dungeon.RoomFlag;
import com.someguyssoftware.dungoncrawler.generator.dungeon.RoomRole;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * @author Mark Gottschling on Sep 15, 2020
 * Based on technique from https://www.gamasutra.com/blogs/AAdonaac/20150903/252889/Procedural_Dungeon_Generation_Algorithm.php
 */
public class DungeonVisualizer extends Application {
	private static final Logger LOGGER = LogManager.getLogger(DungeonVisualizer.class);

	private static final Paint ROCK_COLOR = Color.DARKGREY;
	private static final Paint START_ROOM_COLOR = Color.GREEN;
	private static final Paint END_ROOM_COLOR = Color.RED;
	private static final Paint MAIN_ROOM_COLOR = Color.DARKBLUE;
	private static final Paint AUXILILARY_ROOM_COLOR =Color.PURPLE;
	private static final Paint NOT_INCLUDED_ROOM_COLOR = Color.GREY;
	private static final Paint ROOM_FLOOR_COLOR = Color.rgb(32, 32, 32);	//Color.DARKSLATEGREY;

	private DungeonLevelGenerator dungeonGenerator = new DungeonLevelGenerator();
	private Random random = new Random();
	private DungeonLevel level;
	private Coords2D center;
	private boolean hasIntersections = true;

	private int startX = 0;
	private int startY = 0;
	private int tileWidth = 8;
	private int tileHeight = 8;

	private boolean showGrid = true;
	private boolean showCenterPoint = false;
	private boolean showSpawnBoundary = false;
	private boolean showNonRooms = true;
	private boolean showEdges = false;
	private boolean showPaths = false;
	private boolean showWaylines = false;
	private boolean showCorridors = true;
	private boolean showExits = true;

	private boolean[][] roomMap;
	private boolean[][] corridorMap;

	// ui controls
	private HBox bgBox;
	private HBox gridBox;
	private AnchorPane spawnBoundary;
	private AnchorPane centerPoint;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/**
	 * 
	 */
	@Override
	public void start(Stage stage) throws Exception {
		// setup the stage
		stage.setWidth(1200);
		stage.setHeight(1020);
		stage.setTitle("Dungeon Visualizer");

        // create a background
//        BackgroundFill background_fill = new BackgroundFill(Color.PINK,
//                CornerRadii.EMPTY, Insets.EMPTY);
//        Background background = new Background(background_fill);
        
		HBox mainBox = new HBox();
		VBox inputBox = new VBox();
		HBox mapBaseBox = new HBox();
		StackPane mapStack = new StackPane();

		bgBox = new HBox();
		gridBox = new HBox();
		spawnBoundary = new AnchorPane();
		centerPoint = new AnchorPane();
		HBox mapBox = new HBox();

		VisualContext context = new VisualContext();
		context.setBgBox(bgBox);
		context.setGridBox(gridBox);
		context.setSpawnBoundary(spawnBoundary);
		context.setCenterPoint(centerPoint);

		// TODO changing the width and the height needs to reset all these values: bg, grid, spawn boundary, center

		// build the input box
		buildInputsInterface(inputBox, mapBox, context);

		// build the bg box
		buildBackgroundInterface(bgBox);

		// build the grid box
		buildGridInterface(gridBox);

		// build the spawn boundary
		buildSpawnBoundaryInterface(spawnBoundary);

		// build the center point
		buildCenterPointInterface(centerPoint);

		mapStack.getChildren().addAll(bgBox, gridBox, spawnBoundary, centerPoint, mapBox);
		mapBaseBox.getChildren().add(mapStack);

		// assemble the main box
		mainBox.getChildren().addAll(inputBox, mapBaseBox);

		Scene scene = new Scene(mainBox);
		stage.setScene(scene);

		// display the application
		stage.show();
	}

	/**
	 *
	 * @param centerPointPane
	 */
	private void buildCenterPointInterface(AnchorPane centerPointPane) {
		// get the center of the map
		int midX = (dungeonGenerator.getWidth() * tileWidth) / 2;
		int midY = (dungeonGenerator.getHeight() * tileHeight) / 2;

		int spawnX = dungeonGenerator.getSpawnBoxWidth() * tileWidth;
		int spawnY = dungeonGenerator.getSpawnBoxHeight() * tileHeight;

		// add a center rectangle
		Rectangle center = new Rectangle(6, 6); // center size is fixed.
		center.setFill(Color.RED);
		center.setStroke(Color.WHITE);

		centerPointPane.setMinWidth(dungeonGenerator.getWidth() * tileWidth);
		centerPointPane.setMinHeight(dungeonGenerator.getHeight() * tileHeight);
		centerPointPane.getChildren().add(center);
		centerPointPane.setVisible(isShowCenterPoint());
		AnchorPane.setTopAnchor(center, (double)midX-3);
		AnchorPane.setLeftAnchor(center, (double)midY-3);
	}

	/**
	 *
	 * @param spawnBoundaryPane
	 */
	private void buildSpawnBoundaryInterface(AnchorPane spawnBoundaryPane) {
		// get the center of the map
		int midX = (dungeonGenerator.getWidth() * tileWidth) / 2;
		int midY = (dungeonGenerator.getHeight() * tileHeight) / 2;

		int spawnX = dungeonGenerator.getSpawnBoxWidth() * tileWidth;
		int spawnY = dungeonGenerator.getSpawnBoxHeight() * tileHeight;
		Rectangle spawnBoundary = new Rectangle(spawnX, spawnY);

		spawnBoundary.setFill(Color.YELLOW);
		spawnBoundary.setOpacity(0.25);
		spawnBoundary.setStroke(Color.DARKGOLDENROD);

		spawnBoundaryPane.setMinWidth(dungeonGenerator.getWidth() * tileWidth);
		spawnBoundaryPane.setMinHeight(dungeonGenerator.getHeight() * tileHeight);
		spawnBoundaryPane.getChildren().add(spawnBoundary);
		spawnBoundaryPane.setVisible(isShowSpawnBoundary());
		AnchorPane.setTopAnchor(spawnBoundary, (double)midX - (spawnX / 2));
		AnchorPane.setLeftAnchor(spawnBoundary, (double)midY - (spawnY / 2));
	}

	/**
	 *
	 * @param bgBox
	 */
	private void buildBackgroundInterface(HBox bgBox) {
		// create background
		Rectangle bg = new Rectangle(0, 0, dungeonGenerator.getWidth() * tileWidth, dungeonGenerator.getHeight() * tileHeight);
		bg.setFill(Color.BLACK);
		bgBox.getChildren().add(bg);
	}

	/**
	 *
	 * @param gridBox
	 */
	private void buildGridInterface(HBox gridBox) {
		// container for all the visual elements
		Group group = new Group();
		// add grid legends
		addGrid(group);
		gridBox.setVisible(isShowGrid());
		gridBox.getChildren().addAll(group);
	}

	/**
	 * 
	 * @param mapBox
	 */
	public void buildMapPane(HBox mapBox, DungeonLevel level) {
		// clear any children
		mapBox.getChildren().clear();

		roomMap = new boolean[dungeonGenerator.getWidth()][dungeonGenerator.getHeight()];
		corridorMap = new boolean[dungeonGenerator.getWidth()][dungeonGenerator.getHeight()];

		// container for all the visual elements
		Group group = new Group();

		// get the center of the map
		int midX = (dungeonGenerator.getWidth() * tileWidth) / 2;
		int midY = (dungeonGenerator.getHeight() * tileHeight) / 2;

		// create background space filler (needed to make the group the right size
		Rectangle bg = new Rectangle(0, 0, dungeonGenerator.getWidth() * tileWidth, dungeonGenerator.getHeight() * tileHeight);
		bg.setFill(Color.TRANSPARENT);
		group.getChildren().add(bg);

		// build room map
		buildRoomMap(roomMap, level.getRooms());
		buildCorridorMap(corridorMap, level.getCorridors());

		/*
		 *  TODO change to for each room, get the x,y and reference the cellmap.
		 *  the cellmap has to be properly filled first though
		 *  and the cellmap is going to be updated to contain a List of object values instead of just booleans
		 */
		level.getRooms().forEach(room -> {
			if (!showNonRooms && room.getRole() != RoomRole.MAIN && room.getRole() != RoomRole.AUXILIARY) {
				return;
			}

			for (int x = 0; x < room.getBox().getWidth(); x++) {
				for (int y = 0; y < room.getBox().getHeight(); y++) {
					int absX = room.getOrigin().getX() + x ;
					int absY = room.getOrigin().getY() + y ;

					// ensure not out of bounds
					if (absX < roomMap.length && absY < roomMap[0].length
							&& absX >=0 && absY >=0) {
						// ensure that the cell flag is turned on
						if (!roomMap[absX][absY]) {
							continue;
						}

						Rectangle tile = new Rectangle(startX + (absX * tileWidth), startY + (absY * tileHeight), tileWidth, tileHeight);
						// select the room color
						Paint color = selectRoomColor(room);

						// setup the common drawing attributes
						tile.setStrokeWidth(0.5);
						if (room.getFlags().contains(RoomFlag.ANCHOR)) {
							tile.setFill(Color.BLACK);
						}
						else {
							tile.setFill(ROOM_FLOOR_COLOR);
						}
						if (x == 0 || y == 0 || x == room.getBox().getWidth()-1 || y == room.getBox().getHeight()-1) {
							//							tile.setFill(color);
							tile.setStroke(Color.LIGHTGREY);
						}
						else {
							tile.setStroke(color);
						}
						group.getChildren().add(tile);
					}
				}
			}

			// add ids
			Text text = new Text(startX + (room.getOrigin().getX() * tileWidth) + 2, 
					startY + (room.getOrigin().getY() * tileHeight) + 10, String.valueOf(room.getId()));
			text.setFont(new Font(10));
			text.setFill(Color.ANTIQUEWHITE);
			group.getChildren().add(text);

			// add dimensions
			Text dimensionsText = new Text(startX + (room.getOrigin().getX() * tileWidth) + 15, 
					startY + (room.getOrigin().getY() * tileHeight) + 20, 
					room.getBox().getWidth() + "x" + room.getBox().getHeight());
			dimensionsText.setFont(new Font(10));
			dimensionsText.setFill(Color.ANTIQUEWHITE);
			group.getChildren().add(dimensionsText);

			// add room outlines
			addRoomOutlines(room, group);

		});

		// add corridors
		if (showCorridors) {
			addCorridors(level, group, roomMap, corridorMap);
		}

		// add all the exits (rooms and corridors)
		if (showExits) {
			addExits(level, group);
		}

		// add edges
		if (showEdges) {
			addEdges(level, group);
		}

		// add paths
		if (showPaths) {
			addPaths(level, group);
		}

		// add waylines
		if (showWaylines) {
			addWaylines(level, group);
		}

		// TEMP add chunk outlines (for Minecraft visuals)

		mapBox.getChildren().add(group);
	}

	private void addGrid(Group group) {
		for (int gridX = 0; gridX < dungeonGenerator.getWidth(); gridX++) {
			if (gridX % 10 == 0 && gridX != 0) {
				// add text
				Text text = new Text(startX + (gridX * tileWidth), startY + (1 * tileHeight) + 5, String.valueOf(gridX));
				text.setFont(new Font(10));
				text.setFill(Color.ANTIQUEWHITE);
				group.getChildren().add(text);

				// add main lines
				Line line = new Line(startX + (gridX * tileWidth), startY, startX + (gridX * tileWidth), startY + (dungeonGenerator.getHeight() * tileHeight));
				line.setStroke(Color.BLANCHEDALMOND);
				line.setStrokeWidth(2.0);
				line.setOpacity(0.20);
				group.getChildren().add(line);
			}
			else {
				Line line = new Line(startX + (gridX * tileWidth), startY, startX + (gridX * tileWidth), startY + (dungeonGenerator.getHeight() * tileHeight));
				line.setStroke(Color.BLANCHEDALMOND);
				line.setOpacity(0.10);
				group.getChildren().add(line);
			}
		}
		for (int gridY = 0; gridY < dungeonGenerator.getWidth(); gridY++) {
			if (gridY % 10 == 0 && gridY != 0) {
				// add text
				Text text = new Text(startX + 4, startY + (gridY * tileHeight) - 2, String.valueOf(gridY));
				text.setFont(new Font(10));
				text.setFill(Color.ANTIQUEWHITE);
				group.getChildren().add(text);
				// add main lines
				Line line = new Line(startX, startY + (gridY * tileWidth), startX + (dungeonGenerator.getWidth() * tileWidth), startY +(gridY * tileWidth));
				line.setStroke(Color.BLANCHEDALMOND);
				line.setStrokeWidth(2.0);
				line.setOpacity(0.35);
				group.getChildren().add(line);
			}
			else {
				Line line = new Line(startX, startY + (gridY * tileWidth), startX + (dungeonGenerator.getWidth() * tileWidth), startY +(gridY * tileWidth));
				line.setStroke(Color.BLANCHEDALMOND);
				line.setOpacity(0.25);
				group.getChildren().add(line);
			}
		}
	}

	/**
	 * 
	 * @param level
	 * @param group
	 */
	private void addExits(DungeonLevel level, Group group) {
		level.getRooms().forEach(room -> {
			if (!room.getExits().isEmpty()) {
				room.getExits().forEach(exit -> {
					Rectangle tile = new Rectangle(startX + (exit.getX() * tileWidth), (exit.getY() * tileHeight), tileWidth, tileHeight);
					tile.setStroke(Color.BLACK);
					tile.setFill(Color.CYAN);
					group.getChildren().add(tile);
				});
			}
		});

		level.getCorridors().forEach(corridor -> {
			if (!corridor.getExits().isEmpty()) {
				corridor.getExits().forEach(exit -> {
					Rectangle tile = new Rectangle(startX + (exit.getX() * tileWidth), (exit.getY() * tileHeight), tileWidth, tileHeight);
					tile.setStroke(Color.BLACK);
					tile.setFill(Color.CORAL);
					group.getChildren().add(tile);
				});
			}			
		});
	}

	/**
	 * 
	 * @param level
	 * @param group
	 */
	private void addCorridors(DungeonLevel level, Group group, boolean[][] roomMap, boolean[][] corridorMap) {
		level.getCorridors().forEach(corridor -> {
			for (int x = 0; x < corridor.getBox().getWidth(); x++) {
				for (int y = 0; y < corridor.getBox().getHeight(); y++) {
					int absX = corridor.getBox().getOrigin().getX() + x ;
					int absY = corridor.getBox().getOrigin().getY() + y ;
					if (corridorMap[absX][absY] && !roomMap[absX][absY]) {
						Rectangle tile = new Rectangle(startX + (absX * tileWidth), startY + (absY * tileHeight), tileWidth, tileHeight);

						// select the room color
						Paint color = Color.YELLOW;

						// setup the common drawing attributes
						tile.setStrokeWidth(0.5);
						tile.setFill(color);
						if (x == 0 || y == 0 || x == corridor.getBox().getWidth()-1 || y == corridor.getBox().getHeight()-1) {
//							tile.setStrokeWidth(1);
							tile.setFill(ROOM_FLOOR_COLOR);
							tile.setStroke(Color.LIGHTBLUE);
//							tile.setFill(Color.BEIGE);
						}
						else {
							tile.setStroke(Color.BLACK);
						}
						group.getChildren().add(tile);			
					}
				}
			}	

//			addCorridorOutline(corridor, group);
		});	
	}

	/**
	 * 
	 * @param corridor
	 * @param group
	 */
	private void addCorridorOutline(Corridor corridor, Group group) {
		Rectangle outline = new Rectangle(startX + (corridor.getBox().getOrigin().getX() * tileWidth), startY + (corridor.getBox().getOrigin().getY() * tileHeight),
				corridor.getBox().getWidth() * tileWidth, corridor.getBox().getHeight() * tileHeight);
		outline.setFill(Color.TRANSPARENT);
		outline.setStroke(Color.BEIGE);
		group.getChildren().add(outline);
	}

	/**
	 * 
	 * @param level2
	 * @param group
	 */
	private void addEdges(DungeonLevel level2, Group group) {
		level.getEdges().forEach(edge -> {
			if (edge.v < level.getRooms().size() && edge.w < level.getRooms().size()) {
				INode room1 = level.getRooms().get(edge.v);
				INode room2 = level.getRooms().get(edge.w);	
				Line line = new Line(room1.getCenter().getX() * tileWidth, room1.getCenter().getY() * tileHeight,
						room2.getCenter().getX() * tileWidth, room2.getCenter().getY() * tileHeight);
				line.setStroke(Color.BLUEVIOLET);
				group.getChildren().add(line);
			}
			else {
				//				LOGGER.info("Skipping edge v/w with index of :" + edge.v + ", " + edge.w);
			}
		});
	}

	/**
	 * 
	 * @param room
	 * @param group
	 */
	private void addRoomOutlines(IRoom room, Group group) {
		Rectangle outline = new Rectangle(startX + (room.getOrigin().getX() * tileWidth), startY + (room.getOrigin().getY() * tileHeight),
				room.getBox().getWidth() * tileWidth, room.getBox().getHeight() * tileHeight);
		outline.setFill(Color.TRANSPARENT);
		//		if (room.getType() == NodeType.START) {
		//			outline.setStroke(Color.YELLOW);
		//		}
		//		else {
		outline.setStroke(Color.BEIGE);
		//		}
		group.getChildren().add(outline);
	}

	/**
	 * 
	 * @param room
	 * @return
	 */
	private Paint selectRoomColor(IRoom room) {
		Paint color = null;
		if (room.getRole() == RoomRole.MAIN) {
			switch(room.getType()) {
			case START:
				color = START_ROOM_COLOR;
				break;
			case END:
				color = END_ROOM_COLOR;
				break;
			default:
				color = MAIN_ROOM_COLOR;
			}
		}
		else if (room.getRole() == RoomRole.AUXILIARY) {
			color = AUXILILARY_ROOM_COLOR;
		}
		else {
			color = NOT_INCLUDED_ROOM_COLOR;
		}
		return color;
	}

	/**
	 * 
	 * @param level
	 * @param group
	 */
	private void addWaylines(DungeonLevel level, Group group) {
		level.getWaylines().forEach(wayline -> {
			if (wayline.getConnector1() != null && wayline.getConnector2() != null) {
				// since drawing a line need a flat x or y, need to sort coords				
				Coords2D connector1 = wayline.getConnector1().getCoords();
				Coords2D connector2 = wayline.getConnector2().getCoords();
				Axis axis = connector1.getX() == connector2.getX() ? Axis.Y : Axis.X;

				Line line;
				if (axis == Axis.X) {
					Coords2D c = connector1.getX() <= connector2.getX() ? connector1 : connector2;
					line = new Line(c.getX() * tileWidth, c.getY() * tileHeight,
							(c.getX() + wayline.getBox().getWidth()-1) * tileWidth, c.getY() * tileHeight);
				}
				else {
					Coords2D c = connector1.getY() <= connector2.getY() ? connector1 : connector2;
					line = new Line(c.getX() * tileWidth, c.getY() * tileHeight,
							c.getX() * tileWidth, (c.getY() + wayline.getBox().getHeight()-1) * tileHeight);
				}
				line.setStroke(Color.DEEPPINK);
				line.setStrokeWidth(1.25);
				group.getChildren().add(line);
			}
			else {
				//				LOGGER.info("Skipping wayline edge v/w with index of :" + wayline.v + ", " + wayline.w);
			}
		});
	}

	/**
	 * 
	 * @param level
	 * @param group
	 */
	private void addPaths(DungeonLevel level, Group group) {
		level.getPaths().forEach(path -> {
			if (path.v < level.getRooms().size() && path.w < level.getRooms().size()) {
				INode room1 = level.getRooms().get(path.v);
				INode room2 = level.getRooms().get(path.w);				
				Line line = new Line(room1.getCenter().getX() * tileWidth, room1.getCenter().getY() * tileHeight,
						room2.getCenter().getX() * tileWidth, room2.getCenter().getY() * tileHeight);
				line.setStroke(Color.DARKRED);
				line.setStrokeWidth(2.0);
				group.getChildren().add(line);
			}
			else {
				//				LOGGER.info("Skipping edge v/w with index of :" + path.v + ", " + path.w);
			}
		});
	}

	/**
	 * 
	 * @param pane
	 */
	public void buildInputsInterface(VBox pane, HBox mapBox, VisualContext context) {
		pane.setPadding(new Insets(5, 5, 5, 5));

		List<Label> labels = new ArrayList<>();
		List<TextField> fields = new ArrayList<>();
		List<HBox> hBoxes = new ArrayList<>();

		// dimensions
		Label widthLabel = new Label("Width:");
		TextField widthField = new TextField("96");
		HBox widthBox = new HBox(widthLabel, widthField);
		labels.add(widthLabel);
		fields.add(widthField);
		hBoxes.add(widthBox);

		Label heightLabel = new Label("Height:");
		TextField heightField = new TextField("96");
		HBox heightBox = new HBox(heightLabel, heightField);
		labels.add(heightLabel);
		fields.add(heightField);
		hBoxes.add(heightBox);

		// spawn box dimensions
		Label spawnBoundaryWidthLabel = new Label("Width:");
		TextField spawnBoundaryWidthField = new TextField("30");
		HBox spawnBoundaryWidthBox = new HBox(spawnBoundaryWidthLabel, spawnBoundaryWidthField);
		labels.add(spawnBoundaryWidthLabel);
		fields.add(spawnBoundaryWidthField);
		hBoxes.add(spawnBoundaryWidthBox);

		Label spawnBoundaryHeightLabel = new Label("Height:");
		TextField spawnBoundaryHeightField = new TextField("30");
		HBox spawnBoundaryHeightBox = new HBox(spawnBoundaryHeightLabel, spawnBoundaryHeightField);
		labels.add(spawnBoundaryHeightLabel);
		fields.add(spawnBoundaryHeightField);
		hBoxes.add(spawnBoundaryHeightBox);

        // number of levels
        Label numLevelsLabel = new Label("# of Levels");
        TextField numLevelsField = new TextField("1");
        HBox numLevelsBox = new HBox(numLevelsLabel, numLevelsField);
        labels.add(numLevelsLabel);
        fields.add(numLevelsField);
        hBoxes.add(numLevelsBox);

		// number of rooms
		Label numRoomsLabel = new Label("# of Rooms:");
		TextField numRoomsField = new TextField("15");
		HBox numRoomsBox = new HBox(numRoomsLabel, numRoomsField);
		labels.add(numRoomsLabel);
		fields.add(numRoomsField);
		hBoxes.add(numRoomsBox);

		// min room dimension
		Label minRoomSizeLabel = new Label("Min. room size:");
		TextField minRoomSizeField = new TextField("5");
		HBox minRoomSizeBox = new HBox(minRoomSizeLabel, minRoomSizeField);
		labels.add(minRoomSizeLabel);
		fields.add(minRoomSizeField);
		hBoxes.add(minRoomSizeBox);

		// max room dimension
		Label maxRoomSizeLabel = new Label("Max. room size:");
		TextField maxRoomSizeField = new TextField("15");
		HBox maxRoomSizeBox = new HBox(maxRoomSizeLabel, maxRoomSizeField);
		labels.add(maxRoomSizeLabel);
		fields.add(maxRoomSizeField);
		hBoxes.add(maxRoomSizeBox);

		// movement factor
		Label movementFactorLabel = new Label("Movement Factor:");
		TextField movementFactorField = new TextField("1");
		HBox movementFactorBox = new HBox(movementFactorLabel, movementFactorField);
		labels.add(movementFactorLabel);
		fields.add(movementFactorField);
		hBoxes.add(movementFactorBox);

		// mean factor
		Label meanFactorLabel = new Label("Mean Factor:");
		TextField meanFactorField = new TextField("1.15");
		HBox meanFactorBox = new HBox(meanFactorLabel, meanFactorField);
		labels.add(meanFactorLabel);
		fields.add(meanFactorField);
		hBoxes.add(meanFactorBox);

		// path factor
		Label pathFactorLabel = new Label("Path Factor:");
		TextField pathFactorField = new TextField("0.25");
		HBox pathFactorBox = new HBox(pathFactorLabel, pathFactorField);
		labels.add(pathFactorLabel);
		fields.add(pathFactorField);
		hBoxes.add(pathFactorBox);

		// show grid
		HBox showGridBox = addVisibleToggle("Show Grid:", showGrid, labels, hBoxes, context.getGridBox(), (visualizer, bool) -> {
			visualizer.setShowGrid(bool);
		});

		// show center point
		HBox showCenterPointBox = addVisibleToggle("Show Center Point:", showCenterPoint, labels, hBoxes, context.getCenterPoint(), (visualizer, bool) -> {
			visualizer.setShowCenterPoint(bool);
		});

		// show spawn boundary
		HBox showSpawnBoundaryBox = addVisibleToggle("Show Spawn Boundary:", showSpawnBoundary, labels, hBoxes, context.getSpawnBoundary(), (visualizer, bool) -> {
			visualizer.setShowSpawnBoundary(bool);
		});

		// show non-rooms
		HBox showNonRoomsBox = addToggle("Show Non-Selected Rooms:", showNonRooms, labels, hBoxes, (visualizer, bool) -> {
			visualizer.setShowNonRooms(bool);
		});

		// show edges
		HBox showEdgesBox = addToggle("Show Edges:", showEdges, labels, hBoxes, (visualizer, bool) -> {
			visualizer.setShowEdges(bool);
		});

		// show paths
		HBox showPathsBox = addToggle("Show Paths:", showPaths, labels, hBoxes, (visualizer, bool) -> {
			visualizer.setShowPaths(bool);
		});

		// show waylines
		HBox showWaylinesBox = addToggle("Show Waylines:", showWaylines, labels, hBoxes, (visualizer, bool) -> {
			visualizer.setShowWaylines(bool);
		});

		// show corridors
		HBox showCorridorsBox = addToggle("Show Corridors:", showCorridors, labels, hBoxes, (visualizer, bool) -> {
			visualizer.setShowCorridors(bool);
		});

		// show exits
		HBox showExitsBox = addToggle("Show Exits:", showExits, labels, hBoxes, (visualizer, bool) -> {
			visualizer.setShowExits(bool);
		});		

		// buttons
		HBox buttonsBox = new HBox();
		HBox buttonsBox2 = new HBox();
		Button newButton = new Button("Create Dungeon");
		Button initButton = new Button("Initiate Dungeon");
		Button iterationButton = new Button("Do Iteration");

		// this creates and builds a new dungeon level
		newButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {

				level = (DungeonLevel) dungeonGenerator
						.withSpawnBoxWidth(Integer.valueOf(spawnBoundaryWidthField.getText()))
						.withSpawnBoxHeight(Integer.valueOf(spawnBoundaryHeightField.getText()))
						.withNumberOfRooms(Integer.valueOf(numRoomsField.getText()))
						.withMinRoomSize(Math.max(5, Integer.valueOf(minRoomSizeField.getText())))
						.withMaxRoomSize(Math.max(5, Integer.valueOf(maxRoomSizeField.getText())))
						.withMovementFactor(Integer.valueOf(movementFactorField.getText()))
						.withMeanFactor(Double.valueOf(meanFactorField.getText()))
						.withPathFactor(Double.valueOf(pathFactorField.getText()))
						.withHeight(Integer.valueOf(widthField.getText()))
						.withWidth(Integer.valueOf(heightField.getText()))
						.build();

				buildMapPane(mapBox, level);
			}
		});

		/*
		 * this button intializes a new dungeon but doesn't build it (ie perform all the steps)
		 */
		initButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				level = (DungeonLevel) dungeonGenerator
						.withSpawnBoxWidth(Integer.valueOf(spawnBoundaryWidthField.getText()))
						.withSpawnBoxHeight(Integer.valueOf(spawnBoundaryHeightField.getText()))
						.withNumberOfRooms(Integer.valueOf(numRoomsField.getText()))
						.withMinRoomSize(Math.max(5, Integer.valueOf(minRoomSizeField.getText())))
						.withMaxRoomSize(Math.max(5, Integer.valueOf(maxRoomSizeField.getText())))
						.withMovementFactor(Integer.valueOf(movementFactorField.getText()))
						.withMeanFactor(Double.valueOf(meanFactorField.getText()))
						.withPathFactor(Double.valueOf(pathFactorField.getText()))
						.withHeight(Integer.valueOf(widthField.getText()))
						.withWidth(Integer.valueOf(heightField.getText()))
						.init();

				hasIntersections = true;
				center = dungeonGenerator.getCenter(level.getRooms());

				buildMapPane(mapBox, level);
			}
		});

		// moves to the next iteration
		iterationButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				System.out.println("hasIntersections=" + hasIntersections);
				if (hasIntersections) {    	    		
					hasIntersections = dungeonGenerator.iterateSeparationStep(center, level.getRooms(), dungeonGenerator.getMovementFactor());
					dungeonGenerator.updateCellMap(level.getCellMap(), level.getRooms(), null);
				}
				buildMapPane(mapBox, level);
			}
		});

		buttonsBox.getChildren().addAll(newButton);
		hBoxes.add(buttonsBox);

		buttonsBox2.getChildren().addAll(initButton, iterationButton);
		hBoxes.add(buttonsBox2);

		// navigating levels controls (special, excluded from general formatting)
		Label levelNavigatorLabel = new Label("Level:");
		TextField levelNavigatorField = new TextField("1");
		levelNavigatorField.setEditable(false);
		Button prevLevelButton = new Button("<");
		Button nextLevelButton = new Button(">");
		HBox navBox = new HBox(levelNavigatorLabel, levelNavigatorField, prevLevelButton, nextLevelButton);
		prevLevelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				if (Integer.parseInt(levelNavigatorField.getText()) > 1)
				levelNavigatorField.setText(String.valueOf(Integer.parseInt(levelNavigatorField.getText()) - 1));
				// TODO show/hide the level
			}
		});
		nextLevelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				levelNavigatorField.setText(String.valueOf(Integer.parseInt(levelNavigatorField.getText()) + 1));
				// TODO show/hide the level
			}
		});
		
		labels.add(levelNavigatorLabel);
		fields.add(levelNavigatorField);
		hBoxes.add(navBox);

		// formatting for the labels
		for (Label label : labels) {
			label.setMinWidth(120);
			label.setMaxWidth(120);
			label.setWrapText(true);
			label.setTextAlignment(TextAlignment.LEFT);
		}

		for (TextField field : fields) {
			field.setMinSize(30, 15);
			field.setMaxSize(50, 20);
		}

		// formatting for the hboxes 
		for (HBox box : hBoxes) {
			box.setPadding(new Insets(5, 0, 0, 0));
			box.setSpacing(5);
		}

		pane.getChildren().addAll(
				widthBox, heightBox, 
				spawnBoundaryWidthBox, spawnBoundaryHeightBox, 
				numLevelsBox, numRoomsBox, minRoomSizeBox, maxRoomSizeBox, 
				movementFactorBox, meanFactorBox, pathFactorBox, 
				showGridBox, showCenterPointBox, showSpawnBoundaryBox, showNonRoomsBox, showEdgesBox, showPathsBox,
				showWaylinesBox, showCorridorsBox, showExitsBox,
				buttonsBox, buttonsBox2, navBox);
	}


	/**
	 * 
	 * @param labelText
	 * @param labels
	 * @param hBoxes
	 * @param setter
	 * @return
	 */
	private HBox addToggle(String labelText, boolean defaultValue, List<Label>labels, List<HBox> hBoxes, BiConsumer<DungeonVisualizer, Boolean> setter) {
		ToggleGroup group = new ToggleGroup();
		Label label = new Label(labelText);
		RadioButton onButton = new RadioButton();
		RadioButton offButton = new RadioButton();
		onButton.setText("On");
		offButton.setText("Off");
		onButton.setToggleGroup(group);
		offButton.setToggleGroup(group);
		if (defaultValue == true) {
			onButton.setSelected(true);
		}
		else {
			offButton.setSelected(true);
		}

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle> () {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
				RadioButton rb = (RadioButton)group.getSelectedToggle();
				if (rb.getText().equals("On")) {
					setter.accept(DungeonVisualizer.this, true);
				}
				else {
					setter.accept(DungeonVisualizer.this, false);
				}
			}
		});		
		HBox hBox = new HBox(label, onButton, offButton);
		labels.add(label);
		hBoxes.add(hBox);
		return hBox;
	}

	private HBox addVisibleToggle(String labelText, boolean defaultValue, List<Label>labels, List<HBox> hBoxes, Pane pane, BiConsumer<DungeonVisualizer, Boolean> setter) {
		ToggleGroup group = new ToggleGroup();
		Label label = new Label(labelText);
		RadioButton onButton = new RadioButton();
		RadioButton offButton = new RadioButton();
		onButton.setText("On");
		offButton.setText("Off");
		onButton.setToggleGroup(group);
		offButton.setToggleGroup(group);
		if (defaultValue == true) {
			onButton.setSelected(true);
		}
		else {
			offButton.setSelected(true);
		}

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle> () {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
				RadioButton rb = (RadioButton)group.getSelectedToggle();
				if (rb.getText().equals("On")) {
					setter.accept(DungeonVisualizer.this, true);
					pane.setVisible(true);
				}
				else {
					setter.accept(DungeonVisualizer.this, false);
					pane.setVisible(false);
				}
			}
		});
		HBox hBox = new HBox(label, onButton, offButton);
		labels.add(label);
		hBoxes.add(hBox);
		return hBox;
	}

	public boolean[][] buildRoomMap(boolean cellMap[][], List<IRoom> rooms) {
		rooms.forEach(room -> {
			if (!showNonRooms && room.getRole() != RoomRole.MAIN && room.getRole() != RoomRole.AUXILIARY) {
				return;
			}
			for (int w = 0; w < room.getBox().getWidth(); w++) {
				for (int d = 0; d < room.getBox().getHeight(); d++) {
					int x = room.getOrigin().getX() + w;
					int y = room.getOrigin().getY() + d;
					// don't mark the walls are true since corridors can run thru the walls and need to show up
					if ( x > 0 && y > 0 && x < cellMap.length -1 && y < cellMap[0].length - 1) {
						cellMap[room.getOrigin().getX() + w][room.getOrigin().getY() + d] = true;
					}
				}
			}
		});
		return cellMap;
	}

	public boolean[][] buildCorridorMap(boolean cellMap[][], List<Corridor> corridors) {
		corridors.forEach(corridor -> {
			for (int w = 0; w < corridor.getBox().getWidth(); w++) {
				for (int d = 0; d < corridor.getBox().getHeight(); d++) {
					int x = corridor.getBox().getOrigin().getX() + w;
					int y = corridor.getBox().getOrigin().getY() + d;
					if ( x >= 0 && y >= 0 && x < cellMap.length && y < cellMap[0].length) {
						cellMap[corridor.getBox().getOrigin().getX() + w][corridor.getBox().getOrigin().getY() + d] = true;
					}
				}
			}
		});
		return cellMap;
	}


	public class Counter {
		int index = 0;

		public int add() {
			return index++;
		}
	}

	public boolean isShowEdges() {
		return showEdges;
	}

	public void setShowEdges(boolean showEdges) {
		this.showEdges = showEdges;
	}

	public boolean isShowWaylines() {
		return showWaylines;
	}

	public void setShowWaylines(boolean showWaylines) {
		this.showWaylines = showWaylines;
	}

	public boolean isShowPaths() {
		return showPaths;
	}

	public void setShowPaths(boolean showPaths) {
		this.showPaths = showPaths;
	}

	public boolean isShowNonRooms() {
		return showNonRooms;
	}

	public void setShowNonRooms(boolean showNonRooms) {
		this.showNonRooms = showNonRooms;
	}

	public boolean isShowCenterPoint() {
		return showCenterPoint;
	}

	public void setShowCenterPoint(boolean showCenterPoint) {
		this.showCenterPoint = showCenterPoint;
	}

	public boolean isShowSpawnBoundary() {
		return showSpawnBoundary;
	}

	public void setShowSpawnBoundary(boolean showSpawnBoundary) {
		this.showSpawnBoundary = showSpawnBoundary;
	}

	public boolean isShowGrid() {
		return showGrid;
	}

	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	public boolean isShowCorridors() {
		return showCorridors;
	}

	public void setShowCorridors(boolean showCorridors) {
		this.showCorridors = showCorridors;
	}

	public boolean isShowExits() {
		return showExits;
	}

	public void setShowExits(boolean showExits) {
		this.showExits = showExits;
	}
}