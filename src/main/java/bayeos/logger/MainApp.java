package bayeos.logger;

import java.io.IOException;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class MainApp extends Application {

    private static final Logger log = Logger.getLogger(MainApp.class);
    
    private Preferences pref = Preferences.userNodeForPackage(MainApp.class);		 		 	

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
	public void start(Stage stage) throws Exception {
        log.info("Starting Logger FX Application");
               		
        loadMainPane(stage);
    }

	private void loadMainPane(Stage stage) throws IOException {
		String fxmlFile = "/fxml/mainPane.fxml";
        log.debug("Loading main pane");
        FXMLLoader loader = new FXMLLoader();        
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));        
        MainController c = (MainController)loader.getController();      		
        c.setStage(stage);        
        Scene scene = new Scene(rootNode, 600, 400);
        Image ico = new Image("/images/package_green.png");
        stage.getIcons().add(ico);
        stage.setTitle("BayLogger Utility");        
        stage.setScene(scene);
        stage.show();
	}
}
