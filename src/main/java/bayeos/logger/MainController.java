package bayeos.logger;

import static bayeos.logger.LoggerConstants.SM_RESET;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import bayeos.file.CSVFile;
import bayeos.file.ExcelFile;
import bayeos.file.SeriesFile;
import bayeos.frame.FrameParserException;
import bayeos.frame.Parser;
import bayeos.logger.dump.DataModeChooser;
import bayeos.logger.dump.DownloadBulkTask;
import bayeos.logger.dump.DownloadFileTask;
import bayeos.logger.dump.DumpFile;
import bayeos.logger.dump.DumpFileRepository;
import bayeos.logger.dump.ExportFileTask;
import bayeos.logger.dump.UploadDump;
import bayeos.logger.dump.UploadDumpTask;
import bayeos.logger.pref.PrefController;
import bayeos.logger.serial.SerialController;
import bayeos.logger.serial.SerialDeviceFX;
import de.unibayreuth.bayeos.connection.Connection;
import de.unibayreuth.bayeos.connection.ConnectionFactory;
import de.unibayreuth.bayeos.connection.ConnectionFileAdapter;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;


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
	private Button btnInfoFile;
	
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
	private Label txtLoggerVersion;
	@FXML
	private Label txtLoggerName;
	@FXML
	private Label txtLoggerSampleInterval;
	@FXML
	private Label txtLoggerCurrentDate;
	@FXML
	private Label txtLoggerNextFrame;
	@FXML
	private Label txtLoggerNewFrameCount;
	
	@FXML
	private TableView<DumpFile> dumpFileTable;

	@FXML
	private TableColumn<DumpFile, String> coOrigin;
	
	@FXML
	private TableColumn<DumpFile, Date> coLastModified;
	
	@FXML
	private TableColumn<DumpFile, Long> coLength;
	
	@FXML
	private TableColumn<DumpFile, String> coPath;
	
		
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

	private SerialDeviceFX serialDev;
	
	private bayeos.logger.Logger logger;

		
	private Preferences pref = Preferences.userNodeForPackage(MainApp.class);
	private Stage parentStage;

	private ObservableList<DumpFile> dumpFileList = FXCollections.observableArrayList();

	private LiveService liveService;

	private FileChooser fileChooser;
	
	private ExtensionFilter filterBayEOS = new FileChooser.ExtensionFilter("BayEOS Logger files (*.db)", "*.db");
	private ExtensionFilter filterExcel = new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx");
	private ExtensionFilter filterCSV = new FileChooser.ExtensionFilter("CSV  File (*.csv)", "*.csv");
	
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
	
		
	public static String userHomeDir = System.getProperty("user.home", ".");
	public static String systemDir = userHomeDir + File.separatorChar + ".bayeos-logger";	
	public static File dumpFileDir = new File(systemDir + File.separatorChar + "dumpfiles");


	@FXML
	private void initialize() {

		log.debug("Initialize MainController");
		
		
		ico = new Image("/images/package_green.png");

		liveService = new LiveService();
		serialDev = new SerialDeviceFX();

		// Logger
		btnConnect.visibleProperty().bind(serialDev.connectedProperty().not());
		btnDisconnect.visibleProperty().bind(serialDev.connectedProperty());

		tabLogger.disableProperty().bind(liveService.runningProperty());
		tabDumps.disableProperty().bind(liveService.runningProperty());

		lblConnection.textProperty().bind(serialDev.messageProperty());
		frmLogger.disableProperty().bind(serialDev.connectedProperty().not());

		btnDownload.disableProperty().bind(serialDev.connectedProperty().not());
		
		btnDownloadFile.disableProperty().bind(serialDev.connectedProperty().not().or(logVersion.lessThan(1.2)));
								
		btnReset.disableProperty().bind(serialDev.connectedProperty().not());

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
				.bind(serialDev.connectedProperty().not());
		btnLiveStop.disableProperty().bind(serialDev.connectedProperty().not());
		btnLiveStop.visibleProperty().bind(liveService.runningProperty());
		btnLiveStart.visibleProperty()
				.bind(liveService.runningProperty().not());

		coOrigin.setCellValueFactory(new PropertyValueFactory<DumpFile, String>("Origin"));
		coOrigin.setCellFactory(new Callback<TableColumn<DumpFile, String>, TableCell<DumpFile, String>>() {
					@Override
					public TableCell<DumpFile, String> call(
							TableColumn<DumpFile, String> col) {
						final Image ico = new Image("/images/package_green.png");
						return new TableCell<DumpFile, String>() {
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
		
		

		
		coLastModified.setCellValueFactory(new PropertyValueFactory<DumpFile, Date>("LastModified"));		
		
		coLength.setCellValueFactory(new PropertyValueFactory<DumpFile, Long>("Length"));
		coPath.setCellValueFactory(new PropertyValueFactory<DumpFile, String>("Path"));
		
		dumpFileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		dumpFileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
				
		
		try {
			dumpFileList.addAll(DumpFileRepository.getFiles(dumpFileDir.getPath()));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
		dumpFileTable.setItems(dumpFileList);
		
		
		log.debug("Found " + dumpFileList.size() + " dumps.");

		btnUpload.disableProperty().bind(Bindings.isEmpty(dumpFileTable.getSelectionModel()
						.getSelectedItems()));
		btnDelete.disableProperty().bind(
				Bindings.isEmpty(dumpFileTable.getSelectionModel()
						.getSelectedItems()));
		btnExportFile.disableProperty().bind(
				Bindings.isEmpty(dumpFileTable.getSelectionModel()
						.getSelectedItems()));

		btnInfoFile.disableProperty().bind(
				Bindings.isEmpty(dumpFileTable.getSelectionModel()
						.getSelectedItems()));
				
				
		// Pre initialized controls
		// fileChooser 
		fileChooser = new FileChooser();						
		dataModeChooser = new DataModeChooser();		
		taskDialog = new TaskDialog();		

	}

	static class IconNameCell extends TableCell<DumpFile, String> {
		

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

		Set<String> ports = serialDev.getPortNames();
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

			
			stage.showAndWait();
			if (ctrl.isConnectClicked()) {
				connectSerial(ctrl.getPort());
			}

		} catch (IOException e) {
			log.error(e.getMessage());
			Dialogs.showErrorDialog(parentStage,
					"Failed to open serial connection");
		}

	}
	
	
	public void connectSerial(String port){
		if (!serialDev.connect(port)){
			Dialogs.showWarningDialog(parentStage, String.format("Failed to open connection on port %s",port));
			return;
		}
		pref.put("port", port);		
		logger = new bayeos.logger.Logger(serialDev);
		queryMetaDataAction(null);
	}

	private void queryMetaDataAction(ActionEvent event) {
		String name;
		Integer interval;
		Date time;
		Date nextFrameTime;
		try {

			String version = logger.getVersion();			
			txtLoggerVersion.setText(version);			
			if (version!=null){
				logVersion.setValue(Float.valueOf(version));	
			} 
			

			name = logger.getName();
			if (name == null || name.isEmpty()) {
				String res = Dialogs.showInputDialog(parentStage,"Please set logger name.");
				if (res != null) {
					try {
						logger.setName(res);
						name = res;
					} catch (IOException e) {
						name = "<not set>";
						log.error(e);
						Dialogs.showErrorDialog(parentStage, "Failed to set logger name.");
					}
				}
			}
			txtLoggerName.setText(name);

			interval = logger.getSamplingInterval();
			txtLoggerSampleInterval.setText((interval == null) ? "<not set>"
					: String.valueOf(interval));

			time = logger.getTime();
			txtLoggerCurrentDate.setText((time == null) ? "<not set>"
					: DateFormat.getDateTimeInstance().format(time));

			nextFrameTime = logger.getDateOfNextFrame();
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
							+ serialDev.messageProperty().get());
			serialDev.disconnect();
			return;
		}

		if (pref.getBoolean("checkTimeShift", true) && time != null) {
			Date now = new Date();
			if (Math.abs(time.getTime() - now.getTime()) > pref.getDouble(
					"timeShiftSecs", 60) * 1000) {
				DialogResponse r = Dialogs
						.showConfirmDialog(
								parentStage,
								"Timeshift of logger detected.\nWould you like to synchronize the logger clock with your system clock?",
								null, "Please confirm", DialogOptions.YES_NO);
				if (r == DialogResponse.YES) {
					try {
						logger.setTime(new Date());
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
		serialDev.disconnect();
	}

	@FXML
	public void deleteDumpAction(ActionEvent event) {
		log.debug("Delete dumps action");

		DumpFile df = dumpFileTable.getSelectionModel().getSelectedItem();
		DialogResponse res = Dialogs.showConfirmDialog(
				parentStage,				
				String.format("Really delete all records of %s?", "" + df.getOrigin()),
				null,null, DialogOptions.YES_NO);
		if (res.equals(DialogResponse.YES)) {
			log.debug(String.format("Remove %s", df.getAbsolutePath()));
			try {				
				df.delete();
			} catch (SecurityException e) {
				log.error(e);
				return;
			}
			dumpFileList.remove(df);
		}

	}

	@FXML
	public void uploadStartAction(ActionEvent event) {
		log.debug("Upload start action");
		try {
			
			UploadDump  c = new UploadDump();			
			DumpFile dumpFile = dumpFileTable.getSelectionModel().getSelectedItem();
						
			Map<String, Object> ret = c.showDialog(parentStage, dumpFile);
			
			
			if (ret!=null){
				Task<Boolean> task = new UploadDumpTask(dumpFile,ret);
				taskDialog.showDialog(parentStage, task);
				
				try {
					if (task.get()) {
						if (pref.getBoolean("checkDeleteDump", true)) {							
							dumpFile.delete();
							dumpFileList.remove(dumpFile);
						}						
						pref.put("gateway_connection", ((Connection) ret.get("con")).getName());						
					} else {
						Dialogs.showErrorDialog(parentStage, "Failed to upload data.");								
					}
					
				} catch (CancellationException| InterruptedException | ExecutionException e) {
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
		
		private Map<String, ChartPane> charts;

		public void stop() {
			task.dataReceivedProperty().removeListener(listener);
			task.stop();
			vboxChart.getChildren().clear();
			charts = null;
		}

		MapChangeListener<String, Float> listener = new MapChangeListener() {
			@Override
			public void onChanged(Change change) {
				if (change.wasAdded()) {
					String channel = (String) change.getKey();
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
			charts = new HashMap<String, ChartPane>(20);
			task = new LiveDataTask();
			task.dataReceivedProperty().addListener(listener);
			return task;
		}
	}
	
	private class ChartPane extends BorderPane {
		
		private String channel;		
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
		
		public ChartPane(String channel) {
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

			if (lc.getData().size()%2 == 0) {
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
		private SimpleMapProperty<String, Float> dataReceived = new SimpleMapProperty(
				this, "dataReceived", FXCollections.observableHashMap());

		public final ObservableMap<String, Float> getDataReceived() {
			return dataReceived.get();
		}

		public final ReadOnlyMapProperty<String, Float> dataReceivedProperty() {
			return dataReceived;
		}

		public void stop() {
			active = false;
		}

		@Override
		public Void call() {			
			try {
				logger.startLiveData();
				 
				while (active) {									
					Map<String,Object> data = Parser.parse(logger.readData());															
					Platform.runLater(new Runnable() {
						@Override
						public void run() {							
							dataReceived.clear();
							dataReceived.get().putAll((Map<String,Float>) data.get("value"));
						}
					});
				}
				
			} catch (IOException e) {
				log.error(e.getMessage());
			} catch (FrameParserException e) {
				log.error(e.getMessage());
			} finally {				
				try {
					logger.stopLiveData();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
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

		if (serialDev.connectedProperty().getValue()) {
			try {
				serialDev.disconnect();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
				
					
		parentStage.hide();
	}

	@FXML
	public void setCurrentDateAction(ActionEvent event) {
		log.debug("Set current date action");
		try {
			Date d = new Date();
			logger.setTime(d);
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
				logger.stopData(SM_RESET);
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
				logger.setName(input);
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
				logger.setSamplingInterval(Integer.valueOf(input));
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
	public void infoFileAction(ActionEvent event) {
		log.debug("Info File Action started");
		DumpFile df = dumpFileTable.getSelectionModel().getSelectedItem();
		
		try {
			Map<String, Object> info = df.getInfo();			
			Map<String,SummaryStatistics> stats = (Map<String, SummaryStatistics>) info.get("DataFrameStats");			
			Set<String> keys = stats.keySet();
			ArrayList<String> keyList = new ArrayList<>(keys);			
			Collections.sort(keyList, new Comparator<String>() {				
				private boolean isNr(String s) {
					return s != null && s.matches("[0-9]+");
				}				
				@Override
				public int compare(String o1, String o2) {				
					if (isNr(o1) && isNr(o2)){
						return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
					} else {
						return o1.compareTo(o2);
					}					
				}
			});
			
			
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT );
			StringBuffer f = new StringBuffer("<html>");
			f.append("<table>");
			f.append("<tr><td>Min. Date</td><td>").append(dateFormat.format(info.get("MinDate"))).append("</td></tr>");
			f.append("<tr><td>Max. Date</td><td>").append(dateFormat.format(info.get("MaxDate"))).append("</td></tr>");
			f.append("<tr><td>Data frames:</td><td>").append(info.get("DataFrameCount").toString()).append("</td></tr>");
			f.append("<tr><td>Channels:</td><td>").append(String.valueOf(stats.size())).append("</td></tr>");
			
			f.append("<tr><td>Corrupt frames:</td><td>").append(info.get("CorruptFrameCount").toString()).append("</td></tr>");
			f.append("<tr><td>Binary frames:</td><td>").append(info.get("BinaryFrameCount").toString()).append("</td></tr>");
			f.append("<tr><td>Error messages:</td><td>").append(info.get("ErrorMessageCount").toString()).append("</td></tr>");
			f.append("<tr><td>Messages:</td><td>").append(info.get("MessageCount").toString()).append("</td></tr>");			
			
			
			
			f.append("</table>");
			
			
						
			f.append("<table><thead><tr><th>Channel</th><th>Counts</th><th>Min</th><th>Max</th><th>Mean</th><th>Std.Deviation</th>");
			f.append("</tr></thead>");										
			f.append("<tbody>");
			
			for(String key:keyList) {
				f.append("<tr>");
				f.append("<td>").append(key).append("</td>");
				f.append("<td>").append(stats.get(key).getN()).append("</td>");
				f.append("<td>").append(String.format("%.3f",stats.get(key).getMin())).append("</td>");
				f.append("<td>").append(String.format("%.3f",stats.get(key).getMax())).append("</td>");
				f.append("<td>").append(String.format("%.3f",stats.get(key).getMean())).append("</td>");
				f.append("<td>").append(String.format("%.3f",stats.get(key).getStandardDeviation())).append("</td>");
				f.append("</tr>");
					
			}
						
			f.append("</tbody>");
		    f.append("</table>");			
			f.append("</html>");
			 
			Alert a = new Alert(AlertType.INFORMATION);
			a.setTitle("Properties of " + df.getAbsolutePath()) ;			
			a.setHeaderText("");			
			WebView webView = new WebView();
			webView.getEngine().loadContent(f.toString());
            
			a.getDialogPane().setContent(webView);;									
			a.show();
			
			
		} catch (IOException e) {			
			log.error(e);
			Dialogs.showErrorDialog(parentStage, "Info creation failed.");			
		
		}
		
	}

	@FXML
	public void importFileAction(ActionEvent event) {		
		
		try {
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(filterBayEOS);			
			File file = fileChooser.showOpenDialog(parentStage);
			if (file != null) {
				String origin = Dialogs.showInputDialog(parentStage,"Please specify the board origin:", null);
								
				if (origin != null && (!origin.isEmpty())) {				
					DumpFile dest = new DumpFile(dumpFileDir.getAbsolutePath(),origin);										 
					Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);															
					dumpFileList.add(dest);										
				}
			}

		} catch (Exception e) {
			log.error(e);
			Dialogs.showErrorDialog(parentStage, "Import failed.");			
		} 
	}

	@FXML
	public void exportFileAction(ActionEvent event) {		
		log.debug("Export File Action started");
		DumpFile df = dumpFileTable.getSelectionModel().getSelectedItem();		
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(filterExcel);
		fileChooser.getExtensionFilters().add(filterCSV);		
		fileChooser.setInitialFileName(df.getName() + ".xlsx");
		fileChooser.setSelectedExtensionFilter(filterExcel);
		
		File file = fileChooser.showSaveDialog(parentStage);						
		SeriesFile sFile;
		
		if (file != null) {
			if (file.getName().toLowerCase().endsWith(".xlsx")){
				sFile = new ExcelFile();
			} else if (file.getName().toLowerCase().endsWith(".csv")){
				sFile = new CSVFile();
			} else {
				Dialogs.showErrorDialog(parentStage, "Please insert a filename with extension.");
				return;
			}
				
			try {				
				if (!sFile.open(file.getAbsolutePath())){
					Dialogs.showErrorDialog(parentStage, "Failed to open file.");
					return;
				}				
				taskDialog.showDialog(parentStage, new ExportFileTask(df, sFile));
			} catch (IOException e) {
				Dialogs.showErrorDialog(parentStage, "Failed to export data to " + file.getAbsolutePath());
			} finally {
				sFile.close();
			}
		}     
	}

	@FXML
	public void downloadStartAction(ActionEvent event) {
		log.debug("Download start action");
		
		try {			
			byte mode = dataModeChooser.showDialog(parentStage);
			if (mode == -1) return;						
			DownloadBulkTask task = new DownloadBulkTask(logger, mode);
			taskDialog.showDialog(parentStage, task);
			try {
				dumpFileList.add(task.get());
				tabPane.getSelectionModel().select(tabDumps);
			} catch (CancellationException| InterruptedException | ExecutionException e) {				
				log.warn("Download cancelled.");	
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
				taskDialog.showDialog(parentStage, new DownloadFileTask(logger, file));
			} catch (IOException e) {
				Dialogs.showErrorDialog(parentStage, "Failed to download file.");
			}								
		}
	}

	

}
