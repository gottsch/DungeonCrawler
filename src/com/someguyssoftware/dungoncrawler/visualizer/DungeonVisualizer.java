/**
 * 
 */
package com.someguyssoftware.dungoncrawler.visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.NodeType;
import com.someguyssoftware.dungoncrawler.generator.dungeon.DungeonLevel;
import com.someguyssoftware.dungoncrawler.generator.dungeon.DungeonLevelGenerator;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author Mark Gottschling on Sep 15, 2020
 * Based on technique from https://www.gamasutra.com/blogs/AAdonaac/20150903/252889/Procedural_Dungeon_Generation_Algorithm.php
 */
public class DungeonVisualizer extends Application {
	protected static final Logger LOGGER = LogManager.getLogger(DungeonVisualizer.class);

	private static final Paint ROCK_COLOR = Color.DARKGREY;
	private static final Paint START_ROOM_COLOR = Color.GREEN;
	private static final Paint END_ROOM_COLOR = Color.RED;
	private static final Paint MAIN_ROOM_COLOR = Color.DARKBLUE;
	private static final Paint MINOR_ROOM_COLOR =Color.DARKGREY;
	private static final Paint ROOM_FLOOR_COLOR = Color.DARKSLATEGREY;

	private DungeonLevelGenerator dungeonGenerator = new DungeonLevelGenerator();
	private Random random = new Random();
	private DungeonLevel level;
	private Coords2D center;
	private boolean hasIntersections = true;

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
		stage.setWidth(700);
		stage.setHeight(530);
		stage.setTitle("Dungeon Visualizer");

		HBox mainBox = new HBox();
		HBox mapBox = new HBox();
		VBox inputBox = new VBox();

		buildInputPane(inputBox, mapBox);
		mainBox.getChildren().addAll(inputBox, mapBox);

		Scene scene = new Scene(mainBox);
		stage.setScene(scene);

		// display the application
		stage.show();
	}

	/**
	 * 
	 * @param mapBox
	 */
	public void buildMapPane(HBox mapBox, DungeonLevel level) {
		// clear any children
		mapBox.getChildren().clear();

		// container for all the visual elements
		Group group = new Group();

		// create background
		Rectangle bg = new Rectangle(0, 0, 480, 480);
		bg.setFill(Color.BLACK);
		group.getChildren().add(bg);

		// add tiles
		int tileWidth = 5;
		int tileHeight = 5;
		int startX = 0;
		int startY = 0;

		// add spawn boundary
		Rectangle spawnBoundary = new Rectangle(165, 165, 150, 150);
		spawnBoundary.setFill(Color.YELLOW);
		spawnBoundary.setOpacity(0.25);
		spawnBoundary.setStroke(Color.DARKGOLDENROD);
		group.getChildren().add(spawnBoundary);
		
		//		Counter roomCounter = new Counter();
		// TODO change to for each room, get the x,y and reference the cellmap.
		level.getRooms().forEach(room -> {
			//			int roomIndex = roomCounter.add();
			for (int x = 0; x < room.getBox().getWidth(); x++) {
				for (int y = 0; y < room.getBox().getHeight(); y++) {
					int absX = room.getOrigin().getX() + x ;
					int absY = room.getOrigin().getY() + y ;
					Rectangle tile = new Rectangle(startX + (absX * tileWidth), startY + (absY * tileHeight), tileWidth, tileHeight);
					// check for out of bounds
					if (absX < level.getCellMap().length && absY < level.getCellMap()[0].length
							&& absX >=0 && absY >=0) {

						// setup the common drawing attributes
						tile.setStrokeWidth(0.5);
						tile.setFill(ROOM_FLOOR_COLOR);

						if (level.getCellMap()[absX][absY]) {
							tile.setFill(ROCK_COLOR);
						}
						else {
							if (room.isMain()) {
								switch(room.getType()) {
								case START:
									tile.setStroke(START_ROOM_COLOR);
									break;
								case END:
									tile.setStroke(END_ROOM_COLOR);
									break;
								default:
									tile.setStroke(MAIN_ROOM_COLOR);
								}
							}
							else {
								tile.setStroke(MINOR_ROOM_COLOR);
							}
						}

						group.getChildren().add(tile);
					}
				}
			}

			// add id
			Text text = new Text(startX + (room.getOrigin().getX() * tileWidth) + 2, startY + (room.getOrigin().getY() * tileHeight) + 10, String.valueOf(room.getId()));
			text.setFont(new Font(10));
			text.setFill(Color.ANTIQUEWHITE);
			group.getChildren().add(text);

			// add dimensions
			Text dimensionsText = new Text(startX + (room.getOrigin().getX() * tileWidth) + 15, startY + (room.getOrigin().getY() * tileHeight) + 20, room.getBox().getWidth() + "x" + room.getBox().getHeight());
			dimensionsText.setFont(new Font(10));
			dimensionsText.setFill(Color.ANTIQUEWHITE);
			group.getChildren().add(dimensionsText);

			// add room outlines
			Rectangle outline = new Rectangle(startX + (room.getOrigin().getX() * tileWidth), startY + (room.getOrigin().getY() * tileHeight),
					room.getBox().getWidth() * tileWidth, room.getBox().getHeight() * tileHeight);
			outline.setFill(Color.TRANSPARENT);
			if (room.getType() == NodeType.START) {
				outline.setStroke(Color.YELLOW);
			}
			else {
				outline.setStroke(Color.WHITE);
			}
			group.getChildren().add(outline);
		});

		// TODO values need to be calculated
		// add a center rectangle
		Rectangle center = new Rectangle(237, 237, 6, 6);
		center.setFill(Color.RED);
		center.setStroke(Color.WHITE);
		group.getChildren().add(center);

		// add edges
		level.getEdges().forEach(edge -> {
//			if (level.getRoomMap().containsKey(Integer.valueOf(edge.v)) && level.getRoomMap().containsKey(Integer.valueOf(edge.w))) {
//				INode room1 = level.getRoomMap().get(Integer.valueOf(edge.v));
//				INode room2 = level.getRoomMap().get(Integer.valueOf(edge.w));
				if (edge.v < level.getRooms().size() && edge.w < level.getRooms().size()) {
					INode room1 = level.getRooms().get(edge.v);
					INode room2 = level.getRooms().get(edge.w);	
				Line line = new Line(room1.getCenter().getX() * tileWidth, room1.getCenter().getY() * tileHeight,
						room2.getCenter().getX() * tileWidth, room2.getCenter().getY() * tileHeight);
				line.setStroke(Color.BLUEVIOLET);
				group.getChildren().add(line);
			}
			else {
				LOGGER.info("Skipping edge v/w with index of :" + edge.v + ", " + edge.w);
			}
		});
		
		// add paths
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
				LOGGER.info("Skipping edge v/w with index of :" + path.v + ", " + path.w);
			}
		});

		// TEMP add chunk outlines (for Minecraft visuals)


		mapBox.getChildren().add(group);
	}

	/**
	 * 
	 * @param pane
	 */
	public void buildInputPane(VBox pane, HBox mapBox) {
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
						.withNumberOfRooms(new Integer(numRoomsField.getText()))
						.withMinRoomSize(Math.max(5, new Integer(minRoomSizeField.getText())))
						.withMaxRoomSize(new Integer(maxRoomSizeField.getText()))
						.withMovementFactor(new Integer(movementFactorField.getText()))
						.withMeanFactor(new Double(meanFactorField.getText()))
						.withPathFactor(new Double(pathFactorField.getText()))
						.withHeight(new Integer(widthField.getText()))
						.withWidth(new Integer(heightField.getText()))    	    			
						.build();

				center = dungeonGenerator.getCenter(level.getRooms());

				buildMapPane(mapBox, level);
			}
		});

		/*
		 * this button intializes a new dungeon but doesn't build it (ie perform all the steps)
		 */
		initButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				level = (DungeonLevel) dungeonGenerator
						.withNumberOfRooms(new Integer(numRoomsField.getText()))
						.withMovementFactor(new Integer(movementFactorField.getText()))
						.withMeanFactor(new Double(meanFactorField.getText()))
						.withPathFactor(new Double(pathFactorField.getText()))
						.withHeight(new Integer(widthField.getText()))
						.withWidth(new Integer(heightField.getText()))
						.init();

				hasIntersections = true;
				center = dungeonGenerator.getCenter(level.getRooms());
				
				buildMapPane(mapBox, level);
			}
		});

		// TODO moves to the next iteration
		iterationButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				System.out.println("hasIntersections=" + hasIntersections);
				if (hasIntersections) {    	    		
					hasIntersections = dungeonGenerator.iterateSeparationStep(center, level.getRooms(), dungeonGenerator.getMovementFactor());
					dungeonGenerator.updateCellMap(level.getCellMap(), level.getRooms());
				}
				buildMapPane(mapBox, level);
			}
		});

		buttonsBox.getChildren().addAll(newButton);
		hBoxes.add(buttonsBox);

		buttonsBox2.getChildren().addAll(initButton, iterationButton);
		hBoxes.add(buttonsBox2);

		// formatting for the labels
		for (Label label : labels) {
			label.setMinSize(100, 15);
			label.setMaxSize(150, 20);
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

		pane.getChildren().addAll(widthBox, heightBox, numRoomsBox, minRoomSizeBox, maxRoomSizeBox, movementFactorBox, meanFactorBox, pathFactorBox,
				buttonsBox, buttonsBox2);
	}

	public class Counter {
		int index = 0;

		public int add() {
			return index++;
		}
	}
}
