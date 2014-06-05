package bayeos.logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import logger.DataMode;
import logger.LoggerConnection;
import logger.LoggerFileReader;
import logger.StopMode;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import serial.SerialConnection;
import bayeos.logger.dump.Board;
import bayeos.logger.dump.DAO;
import bayeos.logger.dump.DataModeChooser;
import bayeos.logger.dump.DownloadBulkTask;
import bayeos.logger.dump.DownloadFileTask;
import bayeos.logger.dump.DownloadFrameTask;
import bayeos.logger.dump.ExportExcelTask;
import bayeos.logger.dump.UploadBoard;
import bayeos.logger.dump.UploadBoardTask;
import bayeos.logger.pref.PrefController;
import bayeos.logger.serial.FindPortTask;
import bayeos.logger.serial.SerialConnectionFX;
import bayeos.logger.serial.SerialController;
import de.unibayreuth.bayeos.connection.Connection;
import de.unibayreuth.bayeos.connection.ConnectionFactory;
import de.unibayreuth.bayeos.connection.ConnectionFileAdapter;
import frame.parser.DataReader;

public class MainController {

	private static final Logger log = Logger.getLogger(MainApp.class);

	@FXML
	private Button btnConnect;
	@FXML
	private Button btnDelete;
	@FXML
	private Button btnDisconnect;
	@FXML
	private Button btnDownload;
	@FXML
	private Button btnDownloadFile;
	@FXML
	private Button btnImportFile;
	@FXML
	private Button btnExportFile;
	@FXML
	private Button btnLiveStart;
	@FXML
	private Button btnLiveStop;
	@FXML
	private Button btnUpload;
	@FXML
	private Label lblConnection;
	@FXML
	private MenuItem mnuClose;
	@FXML
	private Button btnReset;
	@FXML
	private Button btnSetDate;
	@FXML
	private Button btnSetName;
	@FXML
	private Button btnSetSamplingInterval;

	@FXML
	private GridPane frmLogger;
	@FXML
	private TextField txtLoggerVersion;
	@FXML
	private TextField txtLoggerName;
	@FXML
	private TextField txtLoggerSampleInterval;
	@FXML
	private TextField txtLoggerCurrentDate;
	@FXML
	private TextField txtLoggerNextFrame;
	@FXML
	private TextField txtLoggerNewFrameCount;
	@FXML
	private TableView<Board> boardTable;

	@FXML
	private TableColumn<Board, String> colBoardName;
	@FXML
	private TableColumn<Board, Date> colBoardStart;
	@FXML
	private TableColumn<Board, Date> colBoardEnd;
	@FXML
	private TableColumn<Board, Integer> colBoardRecords;
	@FXML
	private TableColumn<Board, Integer> colBoardId;

	@FXML
	private Tab tabLogger;
	@FXML
	private Tab tabDumps;
	@FXML
	private Tab tabLive;
	@FXML
	private VBox vboxChart;

	

	@FXML
	private TabPane tabPane;

	private SerialConnectionFX serialCon;
	private LoggerConnection logCon;

	private File dbFolder;
	private Preferences pref = Preferences.userNodeForPackage(MainApp.class);
	private Stage parentStage;

	private ObservableList<Board> boardList = FXCollections
			.observableArrayList();

	private LiveService liveService;

	private FileChooser fileChooser;
	
	private ExtensionFilter filterBayEOS = new FileChooser.ExtensionFilter("BayEOS Logger files (*.db)", "*.db");
	private ExtensionFilter filterExcel = new FileChooser.ExtensionFilter("Excel Workbook (*.xslx)", "*.xslx");
	
	private DataModeChooser dataModeChooser;	
	private TaskDialog taskDialog;

	private SimpleFloatProperty logVersion = new SimpleFloatProperty(1.0F);

	public void setStage(Stage stage) {
		this.parentStage = stage;
		parentStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				exitApp();
			}
		});
	}
	
	private Image ico;
	
		

	@FXML
	private void initialize() {

		log.debug("Initialize MainController");
		
		
		ico = new Image("/images/package_green.png");

		liveService = new LiveService();
		serialCon = new SerialConnectionFX();

		// Logger
		btnConnect.visibleProperty().bind(serialCon.connectedProperty().not());
		btnDisconnect.visibleProperty().bind(serialCon.connectedProperty());

		tabLogger.disableProperty().bind(liveService.runningProperty());
		tabDumps.disableProperty().bind(liveService.runningProperty());

		lblConnection.textProperty().bind(serialCon.messageProperty());
		frmLogger.disableProperty().bind(serialCon.connectedProperty().not());

		btnDownload.disableProperty().bind(serialCon.connectedProperty().not());
		
		btnDownloadFile.disableProperty().bind(serialCon.connectedProperty().not().or(logVersion.lessThan(1.2)));
								
		btnReset.disableProperty().bind(serialCon.connectedProperty().not());

		ConnectionFactory.setFileAdapter(new ConnectionFileAdapter("gateway", new File(System.getProperty("user.home"))));

		btnImportFile.setTooltip(new Tooltip(
				"Import data of logger files from disk."));
		btnExportFile.setTooltip(new Tooltip(
				"Export data to a file on disk."));
		btnDelete.setTooltip(new Tooltip(
				"Delete data from local store."));
		btnUpload.setTooltip(new Tooltip(
				"Upload data to a remote BayEOS Gateway."));
		
		btnConnect.setTooltip(new Tooltip("Connect with Logger"));
		btnDisconnect.setTooltip(new Tooltip("Close Logger Connection"));
		
		btnDownload.setTooltip(new Tooltip("Download data."));
		btnDownloadFile.setTooltip(new Tooltip("Download logger file to disk."));
		
		btnReset.setTooltip(new Tooltip("Erase logger data."));

		
		btnLiveStart.disableProperty()
				.bind(serialCon.connectedProperty().not());
		btnLiveStop.disableProperty().bind(serialCon.connectedProperty().not());
		btnLiveStop.visibleProperty().bind(liveService.runningProperty());
		btnLiveStart.visibleProperty()
				.bind(liveService.runningProperty().not());

		colBoardName
				.setCellValueFactory(new PropertyValueFactory<Board, String>(
						"Name"));
		colBoardName
				.setCellFactory(new Callback<TableColumn<Board, String>, TableCell<Board, String>>() {
					@Override
					public TableCell<Board, String> call(
							TableColumn<Board, String> col) {
						final Image ico = new Image("/images/package_green.png");
						return new TableCell<Board, String>() {
							@Override
							protected void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								if (item == null || empty) {
									setText(null);
									setGraphic(null);
								} else {
									setText(item);
									setGraphic(new ImageView(ico));
								}
							}
						};

					}
				});
		
		

		
		colBoardStart.setCellValueFactory(new PropertyValueFactory<Board, Date>("Start"));
		
		colBoardEnd.setCellValueFactory(new PropertyValueFactory<Board, Date>("End"));
		colBoardRecords.setCellValueFactory(new PropertyValueFactory<Board, Integer>("Records"));
		colBoardId.setCellValueFactory(new PropertyValueFactory<Board, Integer>("Id"));
		
		boardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		boardTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// Pre initialized controls
		// fileChooser 
		fileChooser = new FileChooser();						
		dataModeChooser = new DataModeChooser();		
		taskDialog = new TaskDialog();		

	}

	static class IconNameCell extends TableCell<Board, String> {
		

		@Override
		public void updateItem(String item, boolean empty) {
			log.debug("item:" + item);
			super.updateItem(item, empty);
			if (item != null) {
				setGraphic(new Label("test"));
				setText(item);
			} else {
				setGraphic(null);
				setText(null);
			}
		}
	}

	public void initDBStore() throws SQLException {

		DAO.getBoardDAO().createTable();
		DAO.getFrameDAO().createTable();
		
		boardList.addAll(DAO.getBoardDAO().findAll());
		boardTable.setItems(boardList);
		log.debug("Found " + boardList.size() + " dumps.");

		btnUpload.disableProperty().bind(
				Bindings.isEmpty(boardTable.getSelectionModel()
						.getSelectedItems()));
		btnDelete.disableProperty().bind(
				Bindings.isEmpty(boardTable.getSelectionModel()
						.getSelectedItems()));
		btnExportFile.disableProperty().bind(
				Bindings.isEmpty(boardTable.getSelectionModel()
						.getSelectedItems()));
		
		
	}

	@FXML
	void showPreferences(ActionEvent event) {

		FXMLLoader loader = new FXMLLoader(
				MainApp.class.getResource("/fxml/prefPane.fxml"));
		try {
			BorderPane page = (BorderPane) loader.load();
			Stage stage = new Stage();
			stage.setTitle("Preferences");
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(parentStage);			
			stage.getIcons().add(ico);
			Scene scene = new Scene(page);
			stage.setScene(scene);
			PrefController ctrl = loader.getController();
			ctrl.setStage(stage);
			stage.showAndWait();

		} catch (IOException e) {
			log.error(e);
			Dialogs.showErrorDialog(parentStage,
					"Failed to open preference dialog");

		}

	}

	@FXML
	public void connectSerialAction(ActionEvent event) {
		log.debug("Connect serial action");
		
		Set<String> ports = SerialConnection.getAvailableSerialPorts();

		if (ports.size() < 1) {
			Dialogs.showWarningDialog(parentStage,
					"No serial port found, please check \n your hardware.");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(
					MainApp.class.getResource("/fxml/serialPane.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			Stage stage = new Stage();
			stage.setTitle("New Serial Connection");
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(parentStage);			
			stage.getIcons().add(ico);
			Scene scene = new Scene(page);
			stage.setScene(scene);

			SerialController ctrl = loader.getController();
			ctrl.setParentStage(stage);
			ctrl.setPorts(ports);

			// Default port from user preferences
			String portName = pref.get("port", "COM1");
			if (ports.contains(portName)) {
				ctrl.setPort(portName);
			} else {
				ctrl.setPort(ports.iterator().next());
			}

			// Default baudrate values from user preferences
			ctrl.setBaudrate(pref.getInt("baudrate", 38400));

			stage.showAndWait();
			if (ctrl.isConnectClicked()) {
				connectSerial(ctrl.getPort(), ctrl.getBaudrate());
			}

		} catch (IOException e) {
			log.error(e.getMessage());
			Dialogs.showErrorDialog(parentStage,
					"Failed to open serial connection");
		}

	}
	
	
	public void connectSerial(String port, Integer baudrate){
		if (!serialCon.connect(port, baudrate,pref.getInt("timeout",10000))){
			Dialogs.showWarningDialog(parentStage, String.format("Failed to open connection on port %s",port));
			return;
		}
		pref.put("port", port);		
		logCon = new LoggerConnection(serialCon.getInputStream(),serialCon.getOutputStream());
		queryMetaDataAction(null);
	}

	private void queryMetaDataAction(ActionEvent event) {
		String name;
		Integer interval;
		Date time;
		Date nextFrameTime;
		try {

			String version = logCon.getVersion();			
			txtLoggerVersion.setText(version);			
			if (version!=null){
				logVersion.setValue(Float.valueOf(version));	
			} 
			

			name = logCon.getName();
			if (name == null || name.isEmpty()) {
				String res = Dialogs.showInputDialog(parentStage,"Please set logger name.");
				if (res != null) {
					try {
						logCon.setName(res);
						name = res;
					} catch (IOException e) {
						name = "<not set>";
						log.error(e);
						Dialogs.showErrorDialog(parentStage, "Failed to set logger name.");
					}
				}
			}
			txtLoggerName.setText(name);

			interval = logCon.getSamplingInterval();
			txtLoggerSampleInterval.setText((interval == null) ? "<not set>"
					: String.valueOf(interval));

			time = logCon.getTime();
			txtLoggerCurrentDate.setText((time == null) ? "<not set>"
					: DateFormat.getDateTimeInstance().format(time));

			nextFrameTime = logCon.getDateOfNextFrame();
			txtLoggerNextFrame.setText((nextFrameTime == null) ? "<not set>"
					: DateFormat.getDateTimeInstance().format(
							nextFrameTime));

			if (time != null && nextFrameTime != null && interval != null) {
				long d = (time.getTime() - nextFrameTime.getTime())
						/ (interval * 1000);
				txtLoggerNewFrameCount.setText(String.valueOf(Math.round(d)));
			} else {
				txtLoggerNewFrameCount.setText("<unknown>");
			}

		} catch (IOException e) {
			log.error(e);
			Dialogs.showErrorDialog(parentStage,
					"Failed to get meta informations from logger.\n"
							+ serialCon.messageProperty().get());
			serialCon.disconnect();
			return;
		}

		if (pref.getBoolean("checkTimeShift", true) && time != null) {
			Date now = new Date();
			if (Math.abs(time.getTime() - now.getTime()) > pref.getDouble(
					"timeShiftSecs", 60) * 1000) {
				DialogResponse r = Dialogs
						.showConfirmDialog(
								parentStage,
								"Timeshift of logger detected.\nWould you like to synchronice the logger clock with your system clock?",
								null, "Please confirm", DialogOptions.YES_NO);
				if (r == DialogResponse.YES) {
					try {
						logCon.setTime(new Date());
					} catch (IOException e) {
						log.error(e);
						Dialogs.showErrorDialog(parentStage,
								"Failed to set logger clock.");
					}
				}
			}
		}

	}

	@FXML
	public void disconnectSerialAction(ActionEvent event) {
		log.debug("Disconnect serial action");
		txtLoggerVersion.setText(null);
		txtLoggerName.setText(null);
		txtLoggerSampleInterval.setText(null);
		txtLoggerCurrentDate.setText(null);
		txtLoggerNextFrame.setText(null);
		txtLoggerNewFrameCount.setText(null);
		serialCon.disconnect();
	}

	@FXML
	public void deleteDumpAction(ActionEvent event) {
		log.debug("Delete dumps action");

		Board b = boardTable.getSelectionModel().getSelectedItem();
		DialogResponse res = Dialogs.showConfirmDialog(
				parentStage,				
				String.format("Really delete all records of %s?", "" + b.getName()),
				null,null, DialogOptions.YES_NO);
		if (res.equals(DialogResponse.YES)) {
			log.debug(String.format("Remove %s:[%d] ", b.getName(), b.getId()));
			try {
				DAO.getBoardDAO().deleteBoard(b);
			} catch (SQLException e) {
				log.error(e);
				return;
			}
			boardList.remove(b);
		}

	}

	@FXML
	public void uploadStartAction(ActionEvent event) {
		log.debug("Upload start action");
		try {
			
			UploadBoard  c = new UploadBoard();
			Board board = 	boardTable.getSelectionModel().getSelectedItem();
						

			Map<String, Object> ret = c.showDialog(parentStage, board);
			
			
			if (ret!=null){
				Task<Boolean> task = new UploadBoardTask(board,ret);
				taskDialog.showDialog(parentStage, task);
				
				try {
					if (task.get()) {
						if (pref.getBoolean("checkDeleteDump", true)) {
							DAO.getBoardDAO().deleteBoard(board);
							boardList.remove(board);
						}
						
						pref.put("gateway_connection", ((Connection) ret.get("con")).getName());						
					} else {
						Dialogs.showErrorDialog(parentStage, "Failed to upload data.");								
					}
					
				} catch (SQLException | CancellationException| InterruptedException | ExecutionException e) {
					log.warn(e.getMessage());
					return;
				}				
		    									
			}
			

		} catch (IOException e) {
			log.error(e.getMessage());
			Dialogs.showErrorDialog(parentStage, "Failed to upload data.");
		}
	}

	@FXML
	public void liveStartAction(ActionEvent event) {
		log.debug("Live start action");
		liveService.reset();
		liveService.start();
	}

	@FXML
	public void liveStopAction(ActionEvent event) {
		log.debug("Live stop action");
		liveService.stop();
	}

	
	private class LiveService extends Service<Void> {
		private LiveDataTask task;
		
		private Map<Integer, ChartPane> charts;

		public void stop() {
			task.dataReceivedProperty().removeListener(listener);
			task.stop();
			vboxChart.getChildren().clear();
			charts = null;
		}

		MapChangeListener<Integer, Float> listener = new MapChangeListener() {
			@Override
			public void onChanged(Change change) {
				if (change.wasAdded()) {
					Integer channel = (Integer) change.getKey();
					Float value = (Float) change.getValueAdded();
					// log.debug("Channel:" + channel + " Value:" + value);
					if (!charts.containsKey(channel)) {
						ChartPane chartPane = new ChartPane(channel);												
						charts.put(channel, chartPane);
						vboxChart.getChildren().add(0, chartPane);						
						
					}
					charts.get(channel).addData(value);					
				}

			}
		};

		@Override
		protected Task<Void> createTask() {
			charts = new HashMap<Integer, ChartPane>(20);
			task = new LiveDataTask();
			task.dataReceivedProperty().addListener(listener);
			return task;
		}
	}
	
	private class ChartPane extends BorderPane {
		
		private Integer channel;		
		private LineChart<Number, Number> lineChart;
		private StackPane valuePane;				
		private Float lastValue;
		private Label valueLabel;
		private Polygon pUp;
		private Polygon pDown;
		
		public void addData(Float value) {
			XYChart.Series<Number, Number> series = lineChart.getData().get(0);
			series.getData().add(new XYChart.Data<Number, Number>(new Date().getTime(), value));
			valueLabel.setText(String.format("%.6f", value));
			
			if (lastValue != null) {
				pUp.setFill((value>lastValue)?Color.GREEN:Color.GRAY);
				pDown.setFill((value<lastValue)?Color.RED:Color.GRAY);				
			} else {
				pUp.setFill(Color.GRAY);
				pDown.setFill(Color.GRAY);
			}
			lastValue = value;
			
		}
		
		public ChartPane(Integer channel) {
			this.channel = channel;
			
			this.lineChart = createLineChart(); 			
			setCenter(lineChart);
			
			this.valuePane = createValuePane();			
			setRight(valuePane);
		}		
		private LineChart<Number, Number> createLineChart() {

			NumberAxis xAxis = new NumberAxis();
			NumberAxis yAxis = new NumberAxis();
			xAxis.setLabel("Time");
			xAxis.setAutoRanging(true);
			xAxis.setForceZeroInRange(false);

			xAxis.setTickLabelFormatter(new StringConverter<Number>() {
				private final SimpleDateFormat format = new SimpleDateFormat(
						"HH:mm:ss");

				@Override
				public String toString(Number object) {
					return format.format(new Date(object.longValue()));
				}

				@Override
				public Number fromString(String string) {
					try {
						return format.parse(string).getTime();
					} catch (ParseException e) {
						log.error(e);
						return null;
					}

				}
			});

			yAxis.setLabel("Value");
			LineChart<Number, Number> lc = new LineChart<Number, Number>(xAxis,
					yAxis);
			lc.setLegendVisible(false);
			lc.setCreateSymbols(false);
			lc.setTitle("Channel " + channel);
			

			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			lc.getData().add(series);

			if (channel % 2 == 0) {
				series.getNode().setStyle(
						"-fx-stroke: red; -fx-background-color: red,white;");
			} else {
				series.getNode().setStyle(
						"-fx-stroke: blue; -fx-background-color: blue,white;");
			}
			return lc;
		}

		private StackPane createValuePane() {
			StackPane stack = new StackPane();
			valueLabel = new Label();
			valueLabel.setScaleX(2.0);
			valueLabel.setScaleY(2.0);
										
			stack.getChildren().add(valueLabel);
			pUp = new Polygon(-50.0,40.0,50.0,40.0,0.0,-60.0);
			pUp.setScaleX(0.2);
			pUp.setScaleY(0.2);
			
			stack.getChildren().add(pUp);
			StackPane.setAlignment(pUp, Pos.TOP_CENTER);
			
			pDown = new Polygon(-50.0,40.0,50.0,40.0,0.0,-60.0);
			pDown.setRotate(180);
			pDown.setScaleX(0.2);
			pDown.setScaleY(0.2);
			
			
			stack.getChildren().add(pDown);
			StackPane.setAlignment(pDown, Pos.BOTTOM_CENTER);
			
			
			
			return stack;
		}
	}
	
	
	

	private class LiveDataTask extends Task<Void> {
		boolean active = true;
		private SimpleMapProperty<Integer, Float> dataReceived = new SimpleMapProperty(
				this, "dataReceived", FXCollections.observableHashMap());

		public final ObservableMap<Integer, Float> getDataReceived() {
			return dataReceived.get();
		}

		public final ReadOnlyMapProperty<Integer, Float> dataReceivedProperty() {
			return dataReceived;
		}

		public void stop() {
			active = false;
		}

		@Override
		public Void call() {
			DataReader reader =  new DataReader();
			try {
				logCon.startLiveData();
				 
				while (active) {
															
					final Map<String,Object> data = reader.read(logCon.readData(), "COM", new Date());
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							// log.debug("Received frame:" + f.toString());
							dataReceived.clear();
							dataReceived.get().putAll((Map<Integer,Float>) data.get("values"));
						}
					});
				}
				logCon.stopLiveData();
			} catch (IOException e) {
				log.error(e.getMessage());
			} 
			return null;
		}
	}

	@FXML
	public void closeApplication(ActionEvent event) {
		log.debug("Close app action");
		exitApp();
	}

	private void exitApp() {
		if (liveService.getState() == State.RUNNING) {
			liveService.stop();
		}

		if (serialCon.connectedProperty().getValue()) {
			try {
				serialCon.disconnect();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		
		DAO.close();
					
		parentStage.hide();
	}

	@FXML
	public void setCurrentDateAction(ActionEvent event) {
		log.debug("Set current date action");
		try {
			Date d = new Date();
			logCon.setTime(d);
			txtLoggerCurrentDate.setText(DateFormat.getDateTimeInstance()
					.format(d));
		} catch (IOException e) {
			log.error(e.getMessage());
			Dialogs.showErrorDialog(parentStage,
					"Synchronization of logger time failed.");
		}

	}

	@FXML
	public void resetDataAction(ActionEvent event) {
		log.debug("Reset data action");
		DialogResponse res = Dialogs
				.showConfirmDialog(
						parentStage,
						"Do you really want to reset the logger ?\nAll logged data will be erased.",
						null, "Reset logger data", DialogOptions.YES_NO);
		if (res.equals(DialogResponse.YES)) {
			try {
				logCon.stopData(StopMode.RESET);
				queryMetaDataAction(null);
			} catch (IOException e) {
				log.error(e.getMessage());
				Dialogs.showErrorDialog(parentStage,
						"Reset of logger data failed.");
			}
		}
	}

	@FXML
	public void setNameAction(ActionEvent event) {
		log.debug("Set name action");
		String input = Dialogs.showInputDialog(parentStage, "Name:", null,
				"Set logger name");
		if (input != null) {
			try {
				logCon.setName(input);
				txtLoggerName.setText(input);
			} catch (IOException e) {
				log.error(e.getMessage());
				Dialogs.showErrorDialog(parentStage,
						"Set name of logger failed.");
			}
		}
	}

	@FXML
	public void showAboutDialog(ActionEvent e) {
		log.debug("Show about");
		InputStreamReader input = new InputStreamReader(getClass()
				.getResourceAsStream("/bundles/about.txt"));
		BufferedReader reader = new BufferedReader(input);
		String line;
		StringBuffer b = new StringBuffer();
		try {
			while ((line = reader.readLine()) != null) {
				b.append(line);
				b.append("\n");
			}
			Dialogs.showInformationDialog(parentStage, b.toString(), null,
					"About BayEOS Logger");
		} catch (IOException e1) {
			log.error(e);
			Dialogs.showErrorDialog(parentStage, "Failed to show about Dialog");
		}

	}

	@FXML
	public void setSamplingIntervalAction(ActionEvent event) {
		log.debug("Set sampling interval action");
		String input = Dialogs.showInputDialog(parentStage,
				"Interval [seconds]:", null, "Set sampling interval");
		if (input != null) {
			try {
				logCon.setSamplingInterval(Integer.valueOf(input));
				txtLoggerSampleInterval.setText(input);
			} catch (IOException e) {
				log.error(e.getMessage());
				Dialogs.showErrorDialog(parentStage,
						"Set name of logger failed.");
			} catch (NumberFormatException e) {
				log.error(e.getMessage());
				Dialogs.showErrorDialog(parentStage, "Invalid input data.");
			}
		}
	}

	@FXML
	public void importFileAction(ActionEvent event) {		
		InputStream in = null;
		Board board = null;
		try {
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(filterBayEOS);
			
			File file = fileChooser.showOpenDialog(parentStage);
			if (file != null) {
				String name = Dialogs.showInputDialog(parentStage,"Please specify the board name:", null);
				if (name != null && (!name.isEmpty())) {
					board = new Board(name);
					board.Id = DAO.getBoardDAO().add(board);
					in = new BufferedInputStream(new FileInputStream(file));					
					List<String> frameBuffer = new ArrayList<String>(1000);					
					LoggerFileReader frameReader = new LoggerFileReader(in);								
					int frameCount = 0;
					byte[] data;
					while ((data = frameReader.readData()) != null) {
						frameBuffer.add(Base64.encodeBase64String(data));
						frameCount++;
						if (frameBuffer.size()%1000 == 0){
							DAO.getFrameDAO().addFrames(frameBuffer, board.Id);
							frameBuffer.clear();
						}	
					}				
					if (frameBuffer.size() > 0){
						DAO.getFrameDAO().addFrames(frameBuffer, board.Id);
						frameBuffer.clear();
					}
					
					
					board.End = frameReader.getMaxStart();
					board.Start = frameReader.getMinStart();
					board.Records = frameCount;
					DAO.getBoardDAO().update(board);
					boardList.add(board);

				}
			}

		} catch (Exception e) {
			log.error(e);
			Dialogs.showErrorDialog(parentStage, "Import failed.");
			if (board.Id != 0) {
				try {
					DAO.getBoardDAO().deleteBoard(board);
				} catch (SQLException e1) {
					log.error(e);
				}
			}
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				log.error(e);
			}

		}

	}

	@FXML
	public void exportFileAction(ActionEvent event) {		
		log.debug("Export File Action started");
		Board b = boardTable.getSelectionModel().getSelectedItem();		
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(filterExcel);	
		// fileChooser.setInitialFileName(); since java 1.7.0.45
		
		File file = fileChooser.showSaveDialog(parentStage);
		
		
		if (file != null) {
			if (!file.getName().toLowerCase().endsWith(".xlsx")){
				file = new File(file.getAbsolutePath() + ".xlsx");  
			}
			try {
				taskDialog.showDialog(parentStage, new ExportExcelTask(DAO.getConnection(),b,file));
			} catch (IOException | SQLException e) {
				Dialogs.showErrorDialog(parentStage, "Failed to export data to " + file.getAbsolutePath());
			}								
		}     
	}

	@FXML
	public void downloadStartAction(ActionEvent event) {
		log.debug("Download start action");
		Board b = null;
		Task<Board> task;
		try {
			DataMode mode = dataModeChooser.showDialog(parentStage);
			if (mode == null) return;			
			if (logVersion.get() >= 1.2) {
				task = new DownloadBulkTask(logCon, mode);
			} else {
				task = new DownloadFrameTask(logCon, mode);
			}
			taskDialog.showDialog(parentStage, task);
			try {
				b = task.get();				
				if (b!=null && b.getRecords()>0){
					boardList.add(b);
					tabPane.getSelectionModel().select(tabDumps);
				}
				
			} catch (CancellationException| InterruptedException | ExecutionException e) {				
				log.warn("Download of board cancelled.");	
				return;
			}
				
			
			
						
		} catch (IOException e) {
			Dialogs.showErrorDialog(parentStage, "Failed to download data.");				
			return;
		}
								
	}


	// Export logger file from SD Card
	@FXML
	public void downloadFileAction(ActionEvent event) {
		log.debug("Download file action started");
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(filterBayEOS);
		File file = fileChooser.showSaveDialog(parentStage);
		if (file != null) {	
			try {
				taskDialog.showDialog(parentStage, new DownloadFileTask(logCon, file));
			} catch (IOException e) {
				Dialogs.showErrorDialog(parentStage, "Failed to download file.");
			}								
		}
	}

	public void autoConnect() {
		int baudRate = pref.getInt("baudrate", 38400);
		Task<String> task = new FindPortTask(baudRate);		
		try {
			taskDialog.showDialog(parentStage,task);
			String port = task.get();
			if (port == null){
				log.warn("No logger found");
			} else {
				connectSerial(task.get(),baudRate);	
			}								
		} catch (InterruptedException | ExecutionException | IOException e) {
			Dialogs.showErrorDialog(parentStage, "Autoconnect failed.");
		}
		
		
	}

}
