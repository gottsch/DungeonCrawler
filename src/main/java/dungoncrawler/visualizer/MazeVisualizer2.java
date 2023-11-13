package dungoncrawler.visualizer;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.Rectangle2D;
import dungoncrawler.generator.maze.room.IMazeRoom;
import dungoncrawler.generator.maze2.*;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;

/**
 * @author Mark Gottschling on Oct Nov 8, 2023
 *
 */
public class MazeVisualizer2 extends Application {
    // TODO create a list of pre-made colors for regions; (like 10)

    int tileWidth = 7;
    int tileHeight = 7;

    Map<MazeRegion2D, Color> regionColorMap = new HashMap<>();

    MazeLevelGenerator generator = new MazeLevelGenerator.Builder()
            .with($ -> {
                $.width = 95;
                $.height = 95;
                $.minSize = 7;
                $.maxSize = 19;
                $.attemptsMax = 1000;
                $.meanFactor = 0.60;
            }).build();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // setup the stage
        stage.setWidth(1000);
        stage.setHeight(750);
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

    /**
     *
     * @param pane
     * @param mapBox
     */
    public void buildInputPane(VBox pane, HBox mapBox) {
        pane.setPadding(new Insets(5, 5, 5, 5));

        List<TextField> fields = new ArrayList<>();
        List<VBox> vBoxes = new ArrayList<>();

        TextField x = addField(pane, "my label:", "1");

        // number of rooms
        TextField numRoomsField = addField(pane, "# of Rooms:", "25");

        TextField runContinuationField = addField(pane, "Run Continuation:", "0.8");

        // curve factor
        TextField curveFactorField = addField(pane, "Curve:", "0.8");

        // buttons
//        HBox buttonsBox = new HBox();
        VBox buttonsBox = new VBox();
        Button newButton = new Button("New Maze2 Dungeon");

        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                // create a start room
                IRoom2D startRoom = new MazeRoom2D(new Rectangle2D(78, 78, 15, 15));
                startRoom.setStart(true);
                startRoom.getDoorways().add(new Coords2D(78, 85));
                startRoom.getDoorways().add(new Coords2D(85, 78));

                IRoom2D endRoom = new MazeRoom2D(new Rectangle2D(2, 2, 19, 19));
                endRoom.setEnd(true);
                endRoom.getDoorways().add(new Coords2D(20, 10));

                generator.setStartRoom(startRoom);
                generator.setEndRoom(endRoom);
                generator.setNumberOfRooms(Integer.valueOf(numRoomsField.getText()));
                generator.setRunFactor(Double.valueOf(runContinuationField.getText()));
                generator.setCurveFactor(Double.valueOf(curveFactorField.getText()));
                Optional<ILevel2D> level = generator.generate();
                buildMapPane(mapBox, level.orElseThrow());
            }
        });

        buttonsBox.getChildren().addAll(newButton);
        vBoxes.add(buttonsBox);

        for (TextField field : fields) {
            field.setMinSize(30, 15);
            field.setMaxSize(50, 20);
        }

        // formatting for the vboxes
        for (VBox box : vBoxes) {
            box.setPadding(new Insets(5, 0, 0, 0));
            box.setSpacing(5);
        }

        pane.getChildren().addAll(buttonsBox);
    }

    /**
     *
     * @param pane
     * @param s
     * @param value
     * @return
     */
    private TextField addField(VBox pane, String s, String value) {
        Label label = new Label(s);
        TextField field = new TextField(value);
        HBox box = new HBox(label, field);

        // layout
        field.setMinSize(30, 15);
        field.setMaxSize(50, 20);

        label.setMinWidth(120);
        label.setMaxWidth(150);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.LEFT);

        box.setPadding(new Insets(5, 0, 0, 0));
        box.setSpacing(5);

        pane.getChildren().add(box);

        return field;
    }


    /**
     *
     * @param mapBox
     * @param level
     */
    public void buildMapPane(HBox mapBox, ILevel2D level) {
        Random random = new Random();

        // clear the region color map
        regionColorMap.clear();

        // clear any children
        mapBox.getChildren().clear();

        // container for all the visual elements
        Group container = new Group();

        addBg(container, level);

        // add tiles
        int startX = 0;
        int startY = 0;

        // TODO need the rooms mapped first to HashMap<Integer, Region>
        Grid2D map = level.getGrid();
        for (int x = 0; x < level.getWidth(); x++) {
            for (int y = 0; y < level.getHeight(); y++) {
                Circle circle = null;
                Rectangle door = null;
                Rectangle tile = new Rectangle((x * tileWidth), (y * tileHeight), tileWidth, tileHeight);
                tile.setStrokeWidth(0.5);
                Color color = null;
                Color fillColor = null;
                byte id = map.getId(x, y);
                if (id == MazeLevelGenerator.ROCK) {
                    // rock - just outline
                    color = Color.rgb(32, 32, 32);
                    fillColor = Color.BLACK;
                } else if (id == MazeLevelGenerator.WALL) {
                    // wall - beige
                    color = Color.rgb(32, 32, 32);
                    fillColor = Color.rgb(64, 64, 64); //Color.BLANCHEDALMOND;
                } else if (id == MazeLevelGenerator.InternalIDs.CONNECTOR.getId()) {
//                    color = Color.BLACK;
//                    fillColor = Color.HOTPINK;
                    color = Color.rgb(32, 32, 32);
                    fillColor = Color.rgb(64, 64, 64);
                    // TODO finish adding a circle
                    circle = new Circle((x * tileWidth) + (int) (tileWidth / 2), (y * tileHeight) + (int) (tileHeight / 2), 1);
                    circle.setStrokeWidth(0.5);
                    circle.setStroke(Color.WHITE);
                    circle.setFill(Color.WHITE);
                } else if (id == MazeLevelGenerator.DOOR) {
                    // wall colors
                    color = Color.rgb(32, 32, 32);
                    fillColor = Color.rgb(64, 64, 64);
                    // setup door
                    door = new Rectangle((x * tileWidth) + 2, (y * tileHeight) + 1, (int)(tileWidth /2), tileHeight -2);
                    door.setStrokeWidth(0.5);
                    door.setStroke(Color.ORANGE);
                    door.setFill(Color.ORANGE);
                } else if (level.getStartRoom() != null && id == level.getStartRoom().getId()) {
                    fillColor = Color.GREEN;
                    color = Color.GREEN;
                } else if (level.getEndRoom() != null && id == level.getEndRoom().getId()) {
                    fillColor = Color.RED;
                    color = Color.RED;
                } else {
                    // get the region from the level based on id
                    MazeRegion2D region = level.getRegionMap().get(Integer.valueOf(id));
                    // TODO cache the region id and color
                    fillColor = regionColorMap.get(region);
                    if (fillColor == null) {
                        fillColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        regionColorMap.put(region, fillColor);
                    }
                    color = fillColor; //Color.rgb(64, 64, 64);
                }
                if (color != null) {
                    tile.setStroke(color);
                }
                tile.setFill(fillColor);
                container.getChildren().add(tile);
                if (circle != null) {
                    container.getChildren().add(circle);
                }
                if (door != null) {
                    container.getChildren().add(door);
                }
            }
        }
        // process all the rooms
        // TODO process the cellmap - just need to read the map and draw each cell
        level.getRooms().forEach(room -> {
            // add ids
            Text text = new Text(startX + (room.getOrigin().getX() * tileWidth) + 7,
                    startY + (room.getOrigin().getY() * tileHeight) + 15, String.valueOf(room.getId()));
            text.setFont(new Font(10));
            text.setFill(Color.ANTIQUEWHITE);
            container.getChildren().add(text);

            // add dimensions
            Text dimensionsText = new Text(startX + (room.getOrigin().getX() * tileWidth) + 7,
                    startY + (room.getOrigin().getY() * tileHeight) + 25,
                    room.getBox().getWidth() + "x" + room.getBox().getHeight());
            dimensionsText.setFont(new Font(10));
            dimensionsText.setFill(Color.ANTIQUEWHITE);
            container.getChildren().add(dimensionsText);

            // add start
            if (room.isStart()) {
                Text startText = new Text(startX + (room.getOrigin().getX() * tileWidth) + 7,
                        startY + (room.getOrigin().getY() * tileHeight) + 35,"Start");
                startText.setFont(new Font(10));
                startText.setFill(Color.ANTIQUEWHITE);
                container.getChildren().add(startText);
            }

            // add end
            if (room.isEnd()) {
                Text endText = new Text(startX + (room.getOrigin().getX() * tileWidth) + 7,
                        startY + (room.getOrigin().getY() * tileHeight) + 35,"End");
                endText.setFont(new Font(10));
                endText.setFill(Color.ANTIQUEWHITE);
                container.getChildren().add(endText);
            }
        });

        mapBox.getChildren().add(container);
    }

    /**
     *
     * @param container
     */
    private void addBg(Group container, ILevel2D level) {
        // create background
        Rectangle bg = new Rectangle(0, 0, level.getWidth() * tileWidth, level.getHeight() * tileHeight);
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
