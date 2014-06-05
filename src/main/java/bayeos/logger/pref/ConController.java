package bayeos.logger.pref;
/**
 * Sample Skeleton for "conPane.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class ConController
    implements Initializable {

    @FXML 
    private Button btnCancel; 

    @FXML 
    private Label lblHost; 

    @FXML 
    private Label lblName; 

    @FXML
    private Label lblPassword; 

    @FXML 
    private Label lblUser; 

    @FXML 
    private Button okButton; 

    @FXML
    private TextField txtHost;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtUser;
    
    @FXML private PasswordField txtPasswd;

	private Stage stage;

	private boolean okPressed;

	private ConnectionFX con;


	public boolean isOkPressed() {
		return okPressed;
	}

	@FXML
    public void cancelAction(ActionEvent event) {
       okPressed = false;
       stage.hide();
    }

	@FXML
    public void okAction(ActionEvent event) {			
			okPressed = true;
		    stage.hide();				    
    }

    
	@Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        // initialize your logic here: all @FXML variables will have been injected
		
		
    }

	public void setConnection(ConnectionFX con) {		
		this.con = con;			
		Bindings.bindBidirectional(txtName.textProperty(), con.nameProperty());		
		Bindings.bindBidirectional(txtHost.textProperty(), con.hostProperty());
		Bindings.bindBidirectional(txtUser.textProperty(), con.userProperty());
		Bindings.bindBidirectional(txtPasswd.textProperty(), con.passwordProperty());		
	}
	
	public ConnectionFX getConnection(){
		return con;
	}
	
		

	public void setStage(Stage stage) {
		this.stage = stage;
		
	}

}
