/**   
*  This file is part of SimpleCalendar.
    
*  SimpleCalendar is free software: you can redistribute it and/or modify   
*  it under the terms of the GNU General Public License as published by   
*  the Free Software Foundation, either version 3 of the License, or   
*  (at your option) any later version.
     
*  SimpleCalendar is distributed in the hope that it will be useful,   
*  but WITHOUT ANY WARRANTY; without even the implied warranty of   
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the   
*  GNU General Public License for more details.

*  <http://www.gnu.org/licenses/>.
     
*/  
package javafx.scene.control;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class SimpleCalendarDemo extends Application {

	@Override
	public void start(Stage stage) throws Exception {
        stage.setTitle("Simple Calendar Demo");
		StackPane root = new StackPane();
		root.setId("root");
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles/simple_calendar.css").toExternalForm());
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.TOP_CENTER);
        Label label = new Label("JavaFX 2.0 Simple Calendar Demo");
        label.setId("DemoLabel");
        label.setTextAlignment(TextAlignment.CENTER);

        HBox dateBox = new HBox(15);
        dateBox.setAlignment(Pos.CENTER);
        final TextField dateField = new TextField("Select date");
        dateField.setEditable(false);
        dateField.setDisable(true);
        SimpleCalendar simpleCalender = new SimpleCalendar();
        simpleCalender.dateProperty().addListener(new ChangeListener<Date>() {

			@Override
			public void changed(ObservableValue<? extends Date> ov,
					Date oldDate, Date newDate) {
				dateField.setText((new SimpleDateFormat("dd/MM/yyyy")).format(newDate));
				
			}
		});
        dateBox.getChildren().addAll(dateField, simpleCalender);
        
        vbox.getChildren().addAll(label, dateBox);
        
        root.getChildren().add(vbox);
        
        stage.show();
		
	}

	public static void main(String[] args) {
		launch(args);
	}

	

}
