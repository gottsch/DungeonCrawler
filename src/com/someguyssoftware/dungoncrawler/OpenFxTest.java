/**
 * 
 */
package com.someguyssoftware.dungoncrawler;

import java.io.InputStream;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author Mark
 *
 */
public class OpenFxTest extends Application {
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	    // Here you can work with args - command line parameters
	    Application.launch(args);
	}
	
    @Override
    public void start(Stage primaryStage) throws Exception {
    	primaryStage.show();
    	primaryStage.setTitle("Hello world Application");
//    	InputStream iconStream = getClass().getResourceAsStream("/icon.png");
//    	Image image = new Image(iconStream);
//    	primaryStage.getIcons().add(image);
    }
}
