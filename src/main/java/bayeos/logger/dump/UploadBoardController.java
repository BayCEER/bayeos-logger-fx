package bayeos.logger.dump;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SimpleCalendar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import de.unibayreuth.bayeos.connection.Connection;

public class UploadBoardController {
	
	private static final Logger log = Logger.getLogger(UploadBoardController.class);
	
	@FXML
	private Button btnCancel;
	@FXML
	private Button btnOk;
	
	@FXML private ChoiceBox<String> choCon;
	
	@FXML 
	private SimpleCalendar calStart;
	
	@FXML 
	private SimpleCalendar calEnd;
	
	@FXML
	private TextField txtStart;
	
	@FXML
	private TextField txtEnd;		
	
	
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
	
	public void setInterval(Date startDate, Date endDate) {
		calStart.dateProperty().set(startDate);
		calEnd.dateProperty().set(endDate);
	}
	
	@FXML
	private void initialize() {
		log.debug("Initialize UploadController");
		
		calEnd.dateProperty().addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> ov,
					Date oldDate, Date newDate) {
				txtEnd.setText((new SimpleDateFormat()).format(newDate));
				
								
			}
		});
		
		calStart.dateProperty().addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> ov,
					Date oldDate, Date newDate) {
				txtStart.setText((new SimpleDateFormat()).format(newDate));	
				
								
			}
		});
				
	}

	public Object getStartDate() {
		return calStart.dateProperty().getValue();
	}

	public Object getEndDate() {
		return calEnd.dateProperty().getValue();
	}

}
