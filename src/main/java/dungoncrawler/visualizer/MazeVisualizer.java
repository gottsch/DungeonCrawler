package dungoncrawler.visualizer;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.ILevel;
import dungoncrawler.generator.cave.CaveLevel;
import dungoncrawler.generator.maze.level.IMazeLevel;
import dungoncrawler.generator.maze.level.IMazeLevelGenerator;
import dungoncrawler.generator.maze.level.MazeLevelGenerator;
import dungoncrawler.generator.maze.room.IMazeRoom;
import dungoncrawler.generator.maze.room.MazeRoom;
import dungoncrawler.generator.maze.room.RoomRole;
import dungoncrawler.generator.maze.room.RoomType;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class MazeVisualizer extends Application {
    IMazeLevelGenerator g = (IMazeLevelGenerator) MazeLevelGenerator.setup();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // setup the stage
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setTitle("Maze Visualizer");

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

    public void buildInputPane(VBox pane, HBox mapBox) {
        pane.setPadding(new Insets(5, 5, 5, 5));

        List<HBox> hBoxes = new ArrayList<>();
        List<VBox> vBoxes = new ArrayList<>();

        // buttons
//        HBox buttonsBox = new HBox();
        VBox buttonsBox = new VBox();
        Button newButton = new Button("New Maze Dungeon");
        Button iterationButton = new Button("Do Iteration");

        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {

                IMazeRoom extraRoom = new MazeRoom(new Coords2D(0, 0), 5, 5);
                extraRoom.setRole(RoomRole.MAIN);
                extraRoom.setType(RoomType.STANDARD);

                IMazeLevel level = MazeLevelGenerator.setup()
                        .random(new Random())
                        .size(96, 96)
                        .spawnRegion(20, 20, new Coords2D(48, 48))
                        .movementFactor(1)
                        .meanFactor(.50)
                        .rooms()
                        .amount(25)
                        .sizes(7, 17)
                        .degrees(3, 5)
                        .add(extraRoom) // example - add fixed multilevel room or obstacles
                        .startRoom()
                        .size(12, 12) // will be provided as the last end rooms size
                        .spawn(new Coords2D(48, 80))
                        .next()
                        .endRoom()
                        .next()
                        .seeds(5)
                        .create()
                        .generate();
                buildMapPane(mapBox, level);
            }
        });

        buttonsBox.getChildren().addAll(newButton, iterationButton);
        vBoxes.add(buttonsBox);

        // formatting for the hboxes
        for (VBox box : vBoxes) {
            box.setPadding(new Insets(5, 0, 0, 0));
            box.setSpacing(5);
        }

        pane.getChildren().addAll(buttonsBox);

    }

    public void buildMapPane(HBox mapBox, IMazeLevel level) {
        // clear any children
        mapBox.getChildren().clear();

        // container for all the visual elements
        Group container = new Group();

        addBg(container);

        // add tiles
        int tileWidth = 5;
        int tileHeight = 5;
        int startX = 0;
        int startY = 0;

        // process all the rooms
        // TODO process the cellmap - just need to read the map and draw each cell
        level.getRooms().forEach(room -> {
            // draw a tile for each block of the room
            for (int x = 0; x < room.getBox().getWidth(); x++) {
                for (int y = 0; y < room.getBox().getHeight(); y++) {
                    int absX = room.getOrigin().getX() + x;
                    int absY = room.getOrigin().getY() + y;

                    Rectangle tile = new Rectangle(startX + (absX * tileWidth), startY + (absY * tileHeight), tileWidth, tileHeight);
                    // select the room color
                    Paint color = selectRoomColor(room);
                    // setup the common drawing attributes
                    tile.setStrokeWidth(0.5);
                    tile.setFill(Color.rgb(32, 32, 32));
                    tile.setStroke(color);
                    container.getChildren().add(tile);
                }
            }

            // add ids
            Text text = new Text(startX + (room.getOrigin().getX() * tileWidth) + 2,
                    startY + (room.getOrigin().getY() * tileHeight) + 10, String.valueOf(room.getId()));
            text.setFont(new Font(10));
            text.setFill(Color.ANTIQUEWHITE);
            container.getChildren().add(text);

            // add dimensions
            Text dimensionsText = new Text(startX + (room.getOrigin().getX() * tileWidth) + 15,
                    startY + (room.getOrigin().getY() * tileHeight) + 20,
                    room.getBox().getWidth() + "x" + room.getBox().getHeight());
            dimensionsText.setFont(new Font(10));
            dimensionsText.setFill(Color.ANTIQUEWHITE);
            container.getChildren().add(dimensionsText);

        });

        mapBox.getChildren().add(container);
    }

    /**
     *
     * @param container
     */
    private void addBg(Group container) {
        // create background
        Rectangle bg = new Rectangle(0, 0, 480, 480);
        bg.setFill(Color.BLACK);
        container.getChildren().add(bg);
    }

    /**
     *
     * @param room
     * @return
     */
    private Paint selectRoomColor(IMazeRoom room) {
        Paint color = null;
        return switch(room.getType()) {
            case START -> Color.GREEN;
            case END -> Color.RED;
            default -> Color.CYAN;
        };
    }
}
