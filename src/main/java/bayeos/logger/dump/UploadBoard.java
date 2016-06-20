package bayeos.logger.dump;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialogs;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import bayeos.logger.MainApp;
import de.unibayreuth.bayeos.connection.ConnectionFactory;

public class UploadBoard {
	private Preferences pref = Preferences.userNodeForPackage(MainApp.class);
	
	public Map<String,Object>  showDialog(Stage parentStage, Board board) throws IOException {				
		FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/uploadPane.fxml"));
		Parent page = (Parent) loader.load();	
				
		Stage stage = new Stage();
		stage.setTitle("Upload Data");
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(parentStage);			
		stage.getIcons().add(new Image("/images/package_green.png"));
		Scene scene = new Scene(page);
		stage.setScene(scene);		
		UploadBoardController ctrl = loader.getController();
		
		
		List<de.unibayreuth.bayeos.connection.Connection> cons = null;
		try {
			cons = ConnectionFactory.getFileAdpater().read();
			if (cons.size() == 0) {
				Dialogs.showWarningDialog(
						parentStage,
						"No gateway connection found.\nPlease open the preference dialog and define a new connection.");
				return null;
				}
		} catch (IOException e) {		
			Dialogs.showErrorDialog(stage,
					"Failed to read connections from: "
							+ ConnectionFactory.getFileAdpater()
									.getHomeFolder().getAbsolutePath());
			return null;
		
		}
		ctrl.setConnections(cons);
		
		String defName = pref.get("gateway_connection", null);
		if (defName != null) {
			ctrl.setSelectedConnection(defName);
		}
				
		
		
		ctrl.setStage(stage);
		stage.showAndWait();		
		if (ctrl.isOkPressed()){
			Map<String,Object> ret = new HashMap<String, Object>(3);
			ret.put("con",ctrl.getConnection());			
			return ret;
		} else {
			return null;
		}												
	}

}
