package bayeos.logger.dump;

import java.util.List;

import org.apache.log4j.Logger;

import de.unibayreuth.bayeos.connection.Connection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

public class UploadDumpController {
	
	private static final Logger log = Logger.getLogger(UploadDumpController.class);
	
	@FXML
	private Button btnCancel;
	@FXML
	private Button btnOk;
	
	@FXML private ChoiceBox<String> choCon;
	
	
		
	
	boolean okPressed = false;
	
	private Stage stage;
	
	private List<Connection> cons;
	
	public void setStage(Stage stage){
		this.stage = stage;
	}
	
	@FXML
	public void cancelPressed(ActionEvent event) {
		okPressed = false;
		stage.close();
	}

	@FXML
	public void okPressed(ActionEvent event) {
		okPressed = true;
		stage.close();		
	}

	public boolean isOkPressed() {
			return okPressed;
	}
	public Connection getConnection() {
		return cons.get(choCon.getSelectionModel().getSelectedIndex());
	}
	
	public String getSelectedConnection() {
		return choCon.getSelectionModel().getSelectedItem();
	}

	public void setSelectedConnection(String name) {
		if (cons.contains(name)) {
			choCon.getSelectionModel().select(name);
		} else {
			if (cons.size() > 0)
				choCon.getSelectionModel().select(0);
		}
	}
	
	public void setConnections(List<Connection> cons) {
		this.cons = cons;
		ObservableList<String> list = FXCollections.observableArrayList();
		for (Connection con : cons) {
			list.add(con.getName());
		}
		choCon.setItems(list);

		if (cons.size() > 0)
			choCon.getSelectionModel().select(0);
	}
	
	
	
	@FXML
	private void initialize() {
		log.debug("Initialize UploadController");							
	}


}
