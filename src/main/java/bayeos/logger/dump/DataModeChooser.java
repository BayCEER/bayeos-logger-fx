package bayeos.logger.dump;

import java.io.IOException;

import org.apache.log4j.Logger;

import bayeos.logger.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DataModeChooser {
	
	private static Logger log = Logger.getLogger(DataModeChooser.class);
	
	public byte showDialog(Stage parentStage) throws IOException {				
		FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/dataModeChooserPane.fxml"));
		Parent page = (Parent) loader.load();	
		Stage stage = new Stage();
		stage.setTitle("Download Data");
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(parentStage);			
		stage.getIcons().add(new Image("/images/package_green.png"));
		Scene scene = new Scene(page);
		stage.setScene(scene);		
		DataModeController ctrl = loader.getController();	
		ctrl.setStage(stage);
		stage.showAndWait();		
		if (ctrl.isOkPressed()){
			return ctrl.getDataMode();
		} else {
			return -1;
		}												
	}
	
	 

}
