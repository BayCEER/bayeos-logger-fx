package bayeos.logger.dump;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import logger.DataMode;

public class DataModeController {
	
	@FXML
	private Button btnCancel;
	@FXML
	private Button btnOk;
	@FXML
	private RadioButton rabFull;
	@FXML
	private RadioButton rabNew;
	
	boolean okPressed = false;
	
	private Stage stage;
	
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
	public DataMode getDataMode() {
		if (rabFull.isSelected()) {
			return DataMode.FULL;
		} else {
			return DataMode.NEW;
		}		
	}
	
	
	

}
