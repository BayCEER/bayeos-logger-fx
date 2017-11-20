package bayeos.logger.pref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import bayeos.logger.MainApp;
import de.unibayreuth.bayeos.connection.Connection;
import de.unibayreuth.bayeos.connection.ConnectionFactory;

public class PrefController {
	@FXML private Button btnCancel; 
    @FXML private CheckBox chkDeleteDump;
    @FXML private CheckBox chkTimeShift;
    @FXML private CheckBox chkBattery;
    @FXML private Button okButton;
    @FXML private TableView<ConnectionFX> conTable;
    @FXML private TableColumn colConName;
    @FXML private TableColumn colConHost;
    @FXML private TableColumn colConUser;
    @FXML private Button btnAddConnection;
    @FXML private Button btnDeleteConnection;
    @FXML private Button btnEditConnection;
    @FXML private Button btnCheckConnection;
    @FXML private Slider sldTimeShift;
    
       
    
    private Stage stage;
    private Preferences pref = Preferences.userNodeForPackage(MainApp.class);
    private static final Logger log = Logger.getLogger(PrefController.class);
    
    private ObservableList<ConnectionFX> cons = FXCollections.observableArrayList();
    
    private SimpleBooleanProperty autoDetect = new SimpleBooleanProperty();
    private SimpleBooleanProperty checkTimeShift = new SimpleBooleanProperty();
    private SimpleBooleanProperty checkBattery = new SimpleBooleanProperty();
    private SimpleDoubleProperty timeShiftSecs = new SimpleDoubleProperty();    
    private SimpleBooleanProperty checkDeleteDump = new SimpleBooleanProperty();    
    
    
    
    private Stage conStage;
    private ConController conCtrl;

    
    @FXML private void initialize() {					
    	log.debug("Initialize PrefController");  
    	
    	
    	// StartUp     	    	
    	
    	chkTimeShift.selectedProperty().bindBidirectional(checkTimeShift);
    	checkTimeShift.set(pref.getBoolean("checkTimeShift", true));
    	
    	chkBattery.selectedProperty().bindBidirectional(checkBattery);
    	checkBattery.set(pref.getBoolean("checkBattery", true));
    	
    	
    	sldTimeShift.disableProperty().bind(Bindings.not(chkTimeShift.selectedProperty()));
    	sldTimeShift.valueProperty().bindBidirectional(timeShiftSecs);
    	timeShiftSecs.set(pref.getDouble("timeShiftSecs", 60));
        
    	
    	// Upload
        colConName.setCellValueFactory(new PropertyValueFactory<Connection, String>("name"));		
    	colConHost.setCellValueFactory(new PropertyValueFactory<Connection, String>("host"));
    	colConUser.setCellValueFactory(new PropertyValueFactory<Connection, String>("user"));
    	    	    	    	    	   	   	
    	try {
    		for(Connection con:ConnectionFactory.getFileAdpater().read()){
    			cons.add(new ConnectionFX(con));	
    		}
			
		} catch (IOException e) {
			log.error(e);
			Dialogs.showErrorDialog(stage, "Failed to read connections from " + ConnectionFactory.getFileAdpater().getHomeFolder().getAbsolutePath());
		}
    	log.debug("Found " + cons.size() + " connection definitions.");
    	    	
    	conTable.setItems(cons);
    	conTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
    	    @Override
    	    public void handle(MouseEvent mouseEvent) {
    	        if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
    	            if(mouseEvent.getClickCount() == 2){
    	               editConnectionAction(new ActionEvent());
    	            }
    	        }
    	    }
    	});
    	
    	
    	// Get Connection Dialog 
    	FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/conPane.fxml"));    	    	    	
		BorderPane page = null;
		try {
			page = (BorderPane) loader.load();
		} catch (IOException e) {
			log.error(e);
			Dialogs.showErrorDialog(stage, "Failed to load connection pane.");
			return;			
		}		
		conStage = new Stage();		
		conStage.initModality(Modality.WINDOW_MODAL);
		conStage.initOwner(stage);		
		conStage.getIcons().add(new Image("/images/package_green.png"));		
		conStage.setScene(new Scene(page));				
		conCtrl = loader.getController();
		conCtrl.setStage(conStage);
				
		// Toolbar Connections
		btnDeleteConnection.disableProperty().bind(Bindings.isEmpty(conTable.getSelectionModel().getSelectedItems()));
		btnEditConnection.disableProperty().bind(Bindings.isEmpty(conTable.getSelectionModel().getSelectedItems()));
		btnCheckConnection.disableProperty().bind(Bindings.isEmpty(conTable.getSelectionModel().getSelectedItems()));
		
		chkDeleteDump.selectedProperty().bindBidirectional(checkDeleteDump);
		checkDeleteDump.set(pref.getBoolean("checkDeleteDump", true));
		
		
	}
    
        
    
    public Stage getStage() {
		return stage;
	}

    public void setStage(Stage stage) {
		this.stage = stage;		
	}

	

    @FXML public void addConnectionAction(ActionEvent event) {    	
    	conStage.setTitle("Edit Gateway connection");
    	ConnectionFX con = new ConnectionFX(null,"http://<hostname>:8090/",null,null);
    	conCtrl.setConnection(con);  
    	conStage.showAndWait();    	
    	if (conCtrl.isOkPressed()){    		    		
    		cons.add(con);
    	}
    	    	
    }
    
    @FXML public void editConnectionAction(ActionEvent event) {
    	conStage.setTitle("Edit Gateway Connection");
    	int i = conTable.getSelectionModel().getSelectedIndex();
    	ConnectionFX con = new ConnectionFX(cons.get(i).getConnection());    	    	    	
    	conCtrl.setConnection(con);
    	conStage.showAndWait();
    	if (conCtrl.isOkPressed()){
    		cons.set(i, con);
    	}
    	    	 	    	
    }
        

    @FXML public void cancelAction(ActionEvent event) {
    	stage.close();
    }

    @FXML public void deleteConnectionAction(ActionEvent event) {
    	cons.remove(conTable.getSelectionModel().getSelectedIndex());        
    }

    @FXML public void okAction(ActionEvent event) {
    	
    	// StartUp
    	pref.putBoolean("autoDetect",autoDetect.get());
    	pref.putBoolean("checkTimeShift", checkTimeShift.get());
    	pref.putBoolean("checkBattery", checkBattery.get());
    	pref.putDouble("timeShiftSecs", timeShiftSecs.get());

    	// Upload     	
    	// Write data to Preferences and Password file
    	List<Connection> s = new ArrayList<Connection>(10);
    	for(ConnectionFX con: cons){
    		s.add(con.getConnection());
    	}
    	try {
			ConnectionFactory.getFileAdpater().writeEncrypted(s);
		} catch (IOException e) {
			log.error(e);
			Dialogs.showErrorDialog(stage, "Failed to write connection file.");
		}    	
    	pref.putBoolean("checkDeleteDump", checkDeleteDump.get());    	    	    
    	
    	
    	stage.close();
    }



	

}
