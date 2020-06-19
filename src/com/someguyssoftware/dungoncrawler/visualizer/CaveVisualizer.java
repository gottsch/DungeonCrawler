/**
 * 
 */
package com.someguyssoftware.dungoncrawler.visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.someguyssoftware.dungoncrawler.generator.CaveLevelGenerator;

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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * @author Mark
 * Based on code from https://gamedevelopment.tutsplus.com/tutorials/generate-random-cave-levels-using-cellular-automata--gamedev-9664
 *
 */
public class CaveVisualizer extends Application {

	private boolean[][] map;
	
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
		stage.setHeight(520);
    	stage.setTitle("Cave Visualizer");
    	
    	HBox mainBox = new HBox();
    	HBox mapBox = new HBox();
    	VBox inputBox = new VBox();

    	// TEMP 
    	CaveLevelGenerator caveGen = new CaveLevelGenerator();
    	map = caveGen.createMap(96, 96, new Random());
    	
    	// TODO all controls need to be aware of all other controls... separating into multiple methods might not be a good idea.
    	buildInputPane(inputBox);
    	buildMapPane(mapBox, map);
    	mainBox.getChildren().addAll(inputBox, mapBox);
    	
    	Scene scene = new Scene(mainBox);
    	stage.setScene(scene);
    	
    	// display the application
    	stage.show();
    	
//    	Group group = (Group) mapBox.getChildren().get(0);
//    	group.getChildren().clear();

		
	}

	/**
	 * 
	 * @param mapBox
	 */
	public void buildMapPane(HBox mapBox, boolean[][] map/*, context*/) {
		// clear any children
		mapBox.getChildren().clear();
		
		// container for all the visual elements
		Group group = new Group();
		
		// create background
		Rectangle bg = new Rectangle(0, 0, 480, 480);
		bg.setFill(Color.BLACK);
		group.getChildren().add(bg);
		
		// TEMP add tiles
		int tileWidth = 5;
		int tileHeight = 5;
		int startX = 0;
		int startY = 0;
		
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[x].length; y++) {
				Rectangle tile = new Rectangle(startX + (x * tileWidth), startY + (y * tileHeight), tileWidth, tileHeight);
				if (map[x][y]) {
					tile.setFill(Color.DARKGREY);
				}
				else {
					tile.setFill(Color.DARKBLUE);
				}
				tile.setStroke(Color.BLACK);
				group.getChildren().add(tile);
			}
		}		
		
		// TEMP add chunk outlines (for Minecraft visuals)
		
		
		mapBox.getChildren().add(group);
	}

	/**
	 * 
	 * @param pane
	 */
	public void buildInputPane(VBox pane) {
    	pane.setPadding(new Insets(5, 5, 5, 5));
    	
    	List<Label> labels = new ArrayList<>();
    	List<TextField> fields = new ArrayList<>();
    	List<HBox> hBoxes = new ArrayList<>();
    	
    	// growth limit
    	Label growthLimitLabel = new Label("Growth Limit:");
    	TextField growthLimitField = new TextField("4");
    	HBox growthLimitBox = new HBox(growthLimitLabel, growthLimitField);
    	labels.add(growthLimitLabel);
    	fields.add(growthLimitField);
    	hBoxes.add(growthLimitBox);
    	
    	// decay limit
    	Label decayLimitLabel = new Label("Decay Limit:");
    	TextField decayLimitField = new TextField("3");
    	HBox decayLimitBox = new HBox(decayLimitLabel, decayLimitField);
    	labels.add(decayLimitLabel);
    	fields.add(decayLimitField);
    	hBoxes.add(decayLimitBox);
    	
    	// initial chance
    	Label initialChanceLabel = new Label("Initial Chance:");
    	TextField initialChanceField = new TextField("0.425");
    	HBox initialChanceBox = new HBox(initialChanceLabel, initialChanceField);
    	labels.add(initialChanceLabel);
    	fields.add(initialChanceField);
    	hBoxes.add(initialChanceBox);
    	
    	// iterations
    	Label iterationsLabel = new Label("Iterations:");
    	TextField iterationsField = new TextField("2");
    	HBox iterationsBox = new HBox(iterationsLabel, iterationsField);
    	labels.add(iterationsLabel);
    	fields.add(iterationsField);
    	hBoxes.add(iterationsBox);
    	
    	// buttons
    	HBox buttonsBox = new HBox();
    	Button newButton = new Button("New Cave");
    	Button iterationButton = new Button("Do Iteration");
    	
    	newButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override public void handle(ActionEvent e) {
    	        // TODO clear current map and restart
    	    }
    	});
    	
    	iterationButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override public void handle(ActionEvent e) {
    	        // TODO run another iteration against current map
    	    }
    	});
    	buttonsBox.getChildren().addAll(newButton, iterationButton);
    	hBoxes.add(buttonsBox);
    	
    	// formatting for the labels
    	for (Label label : labels) {
    		label.setMinSize(75, 15);
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
    	
    	pane.getChildren().addAll(growthLimitBox, decayLimitBox, initialChanceBox, iterationsBox, buttonsBox);
	}

}
