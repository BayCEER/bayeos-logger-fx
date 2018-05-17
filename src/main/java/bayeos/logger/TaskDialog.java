package bayeos.logger;

import java.io.IOException;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TaskDialog {

	public void showDialog(Stage stage, Task t) throws IOException{						
		FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/taskPane.fxml"));        
		Parent page = (Parent) loader.load();		    
		Stage taskStage = new Stage();
		taskStage.initModality(Modality.WINDOW_MODAL);
		taskStage.initOwner(stage);
		taskStage.getIcons().add(new Image("/images/package_green.png"));
		Scene scene = new Scene(page);
		taskStage.setScene(scene);			    
		TaskController taskController = loader.getController();
		taskController.setStage(taskStage);			
		taskController.startTask(t);
		taskStage.showAndWait();		
	}
}
