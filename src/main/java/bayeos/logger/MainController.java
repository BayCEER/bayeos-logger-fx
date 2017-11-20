package bayeos.logger;

import static bayeos.logger.LoggerConstants.SM_RESET;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import bayeos.file.ExcelFile;
import bayeos.file.FrameFile;
import bayeos.file.ZipFrameFile;
import bayeos.logger.dump.DataModeChooser;
import bayeos.logger.dump.DownloadBulkTask;
import bayeos.logger.dump.DownloadFileTask;
import bayeos.logger.dump.DumpFile;
import bayeos.logger.dump.DumpFileRepository;
import bayeos.logger.dump.ExportFileTask;
import bayeos.logger.dump.UploadDump;
import bayeos.logger.dump.UploadDumpTask;
import bayeos.logger.live.ChartPane;
import bayeos.logger.live.FrameData;
import bayeos.logger.live.LiveService;
import bayeos.logger.pref.PrefController;
import bayeos.logger.serial.SerialController;
import bayeos.logger.serial.SerialDeviceFX;
import de.unibayreuth.bayeos.connection.Connection;
import de.unibayreuth.bayeos.connection.ConnectionFactory;
import de.unibayreuth.bayeos.connection.ConnectionFileAdapter;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

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
	private Button btnSetTime;
	
	@FXML
	private GridPane grdLogger;
	
	@FXML
	private Label txtLoggerVersion;

	@FXML
	private TextField txtLoggerName;
	
	@FXML
	private TextField txtLoggerInterval;
	
	@FXML
	private Label txtLoggerCurrentTime;
	@FXML
	private Label txtLoggerNextTime;
	@FXML
	private Label txtLoggerNewRecords;
	
	@FXML
	private Label txtLoggerBatteryStatus;	
	
	@FXML
	private Label lblBatteryStatus;
	
	
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
	private TabPane tabCharts;
	private Map<String, Integer> tabChartIndex = new Hashtable<>(10);
	private Map<String, ChartPane> charts = new Hashtable<>();

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
	private ExtensionFilter filterZip = new FileChooser.ExtensionFilter("Zip File (*.zip)", "*.zip");

	private DataModeChooser dataModeChooser;
	private TaskDialog taskDialog;

		
	private LoggerProperties loggerProperties = new LoggerProperties();

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
		serialDev = new SerialDeviceFX();
		liveService = new LiveService();
		liveService.setListener(new ListChangeListener<FrameData>() {
			@Override
			public void onChanged(Change<? extends FrameData> change) {
				if (change.next()) {
					for (FrameData frame : change.getAddedSubList()) {
						ArrayList<String> sortCha = new ArrayList<String>();	
						sortCha.addAll(frame.getValues().keySet());						
						Collections.sort(sortCha, new Comparator<String>() {
							@Override
							public int compare(String o1, String o2) {
								return extractInt(o1) - extractInt(o2);
							}			
							int extractInt(String s) {
					            String num = s.replaceAll("\\D", "");	         
					            return num.isEmpty() ? 0 : Integer.parseInt(num);
					        }			
						});
						
						
						for (String cha: sortCha) {
							String origin = frame.getOrigin();
							Number value = frame.getValues().get(cha);
							if (!tabChartIndex.containsKey(origin)) {								
								// create tab and register it in map								
								Tab t = new Tab(origin);
								t.setClosable(false);								
								t.setGraphic(new ImageView("/images/package_green.png"));
								ScrollPane sp = new ScrollPane();
								sp.setFitToWidth(true);
								VBox v = new VBox();
								sp.setContent(v);								
								t.setContent(sp);								
								int n = tabCharts.getTabs().size();								
								tabCharts.getTabs().add(n,t);
								tabChartIndex.put(origin, n);																								
							}							
							// Fetch tab 
							Tab t = tabCharts.getTabs().get(tabChartIndex.get(origin));							
							String channel = origin + "/" + cha;
							if (!charts.containsKey(channel)) {								
								// Create chart and register it in map
								ChartPane chartPane = new ChartPane(cha);
								chartPane.setPrefHeight(100);
								ScrollPane sp = (ScrollPane)t.getContent();								
								VBox v = (VBox)sp.getContent();
								v.getChildren().add(chartPane);
								charts.put(channel, chartPane);									
							}							
							// Fetch chart
							ChartPane chartPane = charts.get(channel);
							chartPane.addData(frame.getTs(),value);																
						}					
					}
				}
			}
		});

		liveService.runningProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					tabCharts.getTabs().clear();
					tabChartIndex.clear();
					charts.clear();					
				}

			}
		});

		// Logger
		btnConnect.visibleProperty().bind(serialDev.connectedProperty().not());
		btnDisconnect.visibleProperty().bind(serialDev.connectedProperty());

		tabLogger.disableProperty().bind(liveService.runningProperty());
		tabDumps.disableProperty().bind(liveService.runningProperty());

		lblConnection.textProperty().bind(serialDev.messageProperty());
		
		grdLogger.visibleProperty().bind(serialDev.connectedProperty());

		btnDownload.disableProperty().bind(serialDev.connectedProperty().not());

		btnDownloadFile.disableProperty().bind(serialDev.connectedProperty().not().or(loggerProperties.versionProperty().lessThan(1.2)));

		btnReset.disableProperty().bind(serialDev.connectedProperty().not());

		ConnectionFactory
				.setFileAdapter(new ConnectionFileAdapter("gateway", new File(System.getProperty("user.home"))));

		btnImportFile.setTooltip(new Tooltip("Import data of logger files from disk."));
		btnExportFile.setTooltip(new Tooltip("Export data to a file on disk."));
		btnDelete.setTooltip(new Tooltip("Delete data from local store."));
		btnUpload.setTooltip(new Tooltip("Upload data to a remote BayEOS Gateway."));

		btnConnect.setTooltip(new Tooltip("Connect with Logger"));
		btnDisconnect.setTooltip(new Tooltip("Close Logger Connection"));

		btnDownload.setTooltip(new Tooltip("Download data."));
		btnDownloadFile.setTooltip(new Tooltip("Download logger file to disk."));

		btnReset.setTooltip(new Tooltip("Erase logger data."));

		btnLiveStart.disableProperty().bind(serialDev.connectedProperty().not());
		btnLiveStop.disableProperty().bind(serialDev.connectedProperty().not());
		btnLiveStop.visibleProperty().bind(liveService.runningProperty());
		btnLiveStart.visibleProperty().bind(liveService.runningProperty().not());

			

		coOrigin.setCellValueFactory(new PropertyValueFactory<DumpFile, String>("Origin"));
		coOrigin.setCellFactory(new Callback<TableColumn<DumpFile, String>, TableCell<DumpFile, String>>() {
			@Override
			public TableCell<DumpFile, String> call(TableColumn<DumpFile, String> col) {
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

		btnUpload.disableProperty().bind(Bindings.isEmpty(dumpFileTable.getSelectionModel().getSelectedItems()));
		btnDelete.disableProperty().bind(Bindings.isEmpty(dumpFileTable.getSelectionModel().getSelectedItems()));
		btnExportFile.disableProperty().bind(Bindings.isEmpty(dumpFileTable.getSelectionModel().getSelectedItems()));

		btnInfoFile.disableProperty().bind(Bindings.isEmpty(dumpFileTable.getSelectionModel().getSelectedItems()));
					
		txtLoggerVersion.textProperty().bind(Bindings.convert(loggerProperties.versionProperty()));
		
		txtLoggerName.textProperty().bindBidirectional(loggerProperties.nameProperty());		
		txtLoggerInterval.textProperty().bindBidirectional(loggerProperties.samplingIntervalProperty());		
		txtLoggerInterval.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				 if (!newValue.matches("\\d*")) {
			            txtLoggerInterval.setText(newValue.replaceAll("[^\\d]", ""));			            
			     }					 				 
			}
		});
		
	
		txtLoggerInterval.focusedProperty().addListener((o,oldValue,newValue) -> {		
			if (oldValue) {
				try {
					logger.setSamplingInterval(Integer.valueOf(loggerProperties.getSamplingInterval()));
				} catch (NumberFormatException | IOException e) {
					log.error(e);
					Dialogs.showErrorDialog(parentStage, "Failed to set logger interval.");
				}
			}
			}
		);
		
		txtLoggerInterval.setOnKeyPressed(event -> {
			if(event.getCode() == KeyCode.ENTER){
				tabPane.requestFocus();
			}
		});
		
		txtLoggerName.focusedProperty().addListener((o,oldValue,newValue) -> {		
			if (oldValue) {
				try {
					logger.setName(loggerProperties.getName());
				} catch (NumberFormatException | IOException e) {
					log.error(e);
					Dialogs.showErrorDialog(parentStage, "Failed to set logger name.");
				}
			}
			}
		);
		
		txtLoggerName.setOnKeyPressed(event -> {
			if(event.getCode() == KeyCode.ENTER){
				tabPane.requestFocus();
			}
		});
		
		
				
		
		
					
		txtLoggerCurrentTime.textProperty().bind(Bindings.convert(loggerProperties.currentTimeProperty()));
		txtLoggerNextTime.textProperty().bind(Bindings.convert(loggerProperties.nextTimeProperty()));
		txtLoggerNewRecords.textProperty().bind(Bindings.convert(loggerProperties.newRecordsProperty()));
		txtLoggerBatteryStatus.textProperty().bind(Bindings.createStringBinding(() ->
			{
				if (loggerProperties.getBatteryStatus() == null) {
					return "Unknown";
				} else if (logger.getBatteryStatus()) {
					return "Ok";
				} else {
					return "Low";
				}
			}	
		, loggerProperties.batteryStatusProperty()
		));
						
		btnDownloadFile.disableProperty().bind(serialDev.connectedProperty().not().or(loggerProperties.versionProperty().lessThan(1.2F)));				
		txtLoggerBatteryStatus.visibleProperty().bind(loggerProperties.versionProperty().greaterThanOrEqualTo(1.4F));
		
		lblBatteryStatus.visibleProperty().bind(loggerProperties.versionProperty().greaterThanOrEqualTo(1.4F));
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

		FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/prefPane.fxml"));
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
			Dialogs.showErrorDialog(parentStage, "Failed to open preference dialog");

		}

	}

	@FXML
	public void connectSerialAction(ActionEvent event) {
		log.debug("Connect serial action");
		
		Set<String> ports = serialDev.getPortNames();
		if (ports.size() < 1) {
			Dialogs.showWarningDialog(parentStage, "No serial port found, please check \n your hardware.");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/serialPane.fxml"));
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
			Dialogs.showErrorDialog(parentStage, "Failed to open serial connection");
		}

	}

	public void connectSerial(String port) {
		if (!serialDev.connect(port)) {
			Dialogs.showWarningDialog(parentStage, String.format("Failed to open connection on port %s", port));
			return;
		}
		pref.put("port", port);
		logger = new bayeos.logger.Logger(serialDev);
		queryMetaDataAction(null);
	}

	private void queryMetaDataAction(ActionEvent event) {
		try {
						
			loggerProperties.setVersion(Float.valueOf(logger.getVersion()));			
			String name = logger.getName();			
			loggerProperties.setName(name);
			loggerProperties.setSamplingInterval(String.valueOf(logger.getSamplingInterval()));
			loggerProperties.setCurrentTime(logger.getTime());									
			loggerProperties.setNextTime(logger.getDateOfNextFrame());												
			loggerProperties.setBatteryStatus(logger.getBatteryStatus());
			
			// Time shift 			
			if (pref.getBoolean("checkTimeShift", true) && loggerProperties.getCurrentTime() != null) {
				Date now = new Date();
				if (Math.abs(logger.getTime().getTime() - now.getTime()) > pref.getDouble("timeShiftSecs", 60) * 1000) {
					DialogResponse r = Dialogs.showConfirmDialog(parentStage,
							"Timeshift of logger detected.\nWould you like to synchronize the logger clock with your system clock?",
							null, "Please confirm", DialogOptions.YES_NO);
					if (r == DialogResponse.YES) {
						try {
							now = new Date();
							logger.setTime(now);
							loggerProperties.setCurrentTime(now);
						} catch (IOException e) {
							log.error(e);
							Dialogs.showErrorDialog(parentStage, "Failed to set logger clock.");
						}
					}
				}
			}
			
			// Calculate new Records 
			if (loggerProperties.getCurrentTime() != null && loggerProperties.getNextTime() != null && loggerProperties.getSamplingInterval() != null) {
				long d = (loggerProperties.getCurrentTime().getTime() - loggerProperties.getNextTime().getTime()) / (Integer.valueOf(loggerProperties.getSamplingInterval()) * 1000);				
				loggerProperties.setNewRecords(d);				
			} 			
			
			// Battery Warning 
			if (pref.getBoolean("checkBattery", true) && loggerProperties.getBatteryStatus() != null) {
				if (!logger.getBatteryStatus()) {
					Dialogs.showWarningDialog(parentStage, "Logger battery is low.");
				}
			}
			 
			
		} catch (NumberFormatException | IOException e) {
			log.error(e);
			Dialogs.showErrorDialog(parentStage, "Failed to get meta informations from logger.\n" + serialDev.messageProperty().get());
		}
	}

	@FXML
	public void disconnectSerialAction(ActionEvent event) {
		log.debug("Disconnect serial action");								 
		serialDev.disconnect();
	}

	@FXML
	public void deleteDumpAction(ActionEvent event) {
		log.debug("Delete dumps action");

		DumpFile df = dumpFileTable.getSelectionModel().getSelectedItem();
		DialogResponse res = Dialogs.showConfirmDialog(parentStage,
				String.format("Really delete all records of %s?", "" + df.getOrigin()), null, null,
				DialogOptions.YES_NO);
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

			UploadDump c = new UploadDump();
			DumpFile dumpFile = dumpFileTable.getSelectionModel().getSelectedItem();

			Map<String, Object> ret = c.showDialog(parentStage, dumpFile);

			if (ret != null) {
				Task<Boolean> task = new UploadDumpTask(dumpFile, ret);
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

				} catch (CancellationException | InterruptedException | ExecutionException e) {
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
		liveService.setLogger(logger);
		liveService.reset();
		liveService.start();
	}

	@FXML
	public void liveStopAction(ActionEvent event) {
		log.debug("Live stop action");
		liveService.stop();
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
			loggerProperties.setCurrentTime(d);
			
		} catch (IOException e) {
			log.error(e.getMessage());
			Dialogs.showErrorDialog(parentStage, "Synchronization of logger time failed.");
		}

	}

	@FXML
	public void resetDataAction(ActionEvent event) {
		log.debug("Reset data action");
		DialogResponse res = Dialogs.showConfirmDialog(parentStage,
				"Do you really want to reset the logger ?\nAll logged data will be erased.", null, "Reset logger data",
				DialogOptions.YES_NO);
		if (res.equals(DialogResponse.YES)) {
			try {
				logger.stopData(SM_RESET);
				queryMetaDataAction(null);
			} catch (IOException e) {
				log.error(e.getMessage());
				Dialogs.showErrorDialog(parentStage, "Reset of logger data failed.");
			}
		}
	}

	

	@FXML
	public void showAboutDialog(ActionEvent e) {
		log.debug("Show about");
		InputStreamReader input = new InputStreamReader(getClass().getResourceAsStream("/bundles/about.txt"));
		BufferedReader reader = new BufferedReader(input);
		String line;
		StringBuffer b = new StringBuffer();
		try {
			while ((line = reader.readLine()) != null) {
				b.append(line);
				b.append("\n");
			}
			Dialogs.showInformationDialog(parentStage, b.toString(), null, "About BayEOS Logger");
		} catch (IOException e1) {
			log.error(e);
			Dialogs.showErrorDialog(parentStage, "Failed to show about Dialog");
		}

	}

//	@FXML
//	public void setSamplingIntervalAction(ActionEvent event) {
//		log.debug("Set sampling interval action");
//		String input = Dialogs.showInputDialog(parentStage, "Interval [seconds]:", null, "Set sampling interval");
//		if (input != null) {
//			try {
//				logger.setSamplingInterval(Integer.valueOf(input));
//				txtLoggerSampleInterval.setText(input);
//			} catch (IOException e) {
//				log.error(e.getMessage());
//				Dialogs.showErrorDialog(parentStage, "Set name of logger failed.");
//			} catch (NumberFormatException e) {
//				log.error(e.getMessage());
//				Dialogs.showErrorDialog(parentStage, "Invalid input data.");
//			}
//		}
//	}

	@FXML
	public void infoFileAction(ActionEvent event) {
		log.debug("Info File Action started");
		DumpFile df = dumpFileTable.getSelectionModel().getSelectedItem();

		try {
			Map<String, Object> info = df.getInfo();
			Map<String, SummaryStatistics> stats = (Map<String, SummaryStatistics>) info.get("DataFrameStats");
			Set<String> keys = stats.keySet();
			ArrayList<String> keyList = new ArrayList<>(keys);
			Collections.sort(keyList, new Comparator<String>() {
				private boolean isNr(String s) {
					return s != null && s.matches("[0-9]+");
				}

				@Override
				public int compare(String o1, String o2) {
					if (isNr(o1) && isNr(o2)) {
						return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
					} else {
						return o1.compareTo(o2);
					}
				}
			});

			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			StringBuffer f = new StringBuffer("<html>");
			f.append("<table>");
			f.append("<tr><td>Min. Date</td><td>").append(dateFormat.format(info.get("MinDate"))).append("</td></tr>");
			f.append("<tr><td>Max. Date</td><td>").append(dateFormat.format(info.get("MaxDate"))).append("</td></tr>");
			f.append("<tr><td>Data frames:</td><td>").append(info.get("DataFrameCount").toString())
					.append("</td></tr>");
			f.append("<tr><td>Channels:</td><td>").append(String.valueOf(stats.size())).append("</td></tr>");

			f.append("<tr><td>Corrupt frames:</td><td>").append(info.get("CorruptFrameCount").toString())
					.append("</td></tr>");
			f.append("<tr><td>Binary frames:</td><td>").append(info.get("BinaryFrameCount").toString())
					.append("</td></tr>");
			f.append("<tr><td>Error messages:</td><td>").append(info.get("ErrorMessageCount").toString())
					.append("</td></tr>");
			f.append("<tr><td>Messages:</td><td>").append(info.get("MessageCount").toString()).append("</td></tr>");

			f.append("</table>");

			f.append(
					"<table><thead><tr><th>Channel</th><th>Counts</th><th>Min</th><th>Max</th><th>Mean</th><th>Std.Deviation</th>");
			f.append("</tr></thead>");
			f.append("<tbody>");

			for (String key : keyList) {
				f.append("<tr>");
				f.append("<td>").append(key).append("</td>");
				f.append("<td>").append(stats.get(key).getN()).append("</td>");
				f.append("<td>").append(String.format("%.3f", stats.get(key).getMin())).append("</td>");
				f.append("<td>").append(String.format("%.3f", stats.get(key).getMax())).append("</td>");
				f.append("<td>").append(String.format("%.3f", stats.get(key).getMean())).append("</td>");
				f.append("<td>").append(String.format("%.3f", stats.get(key).getStandardDeviation())).append("</td>");
				f.append("</tr>");

			}

			f.append("</tbody>");
			f.append("</table>");
			f.append("</html>");

			Alert a = new Alert(AlertType.INFORMATION);
			a.setTitle("Properties of " + df.getAbsolutePath());
			a.setHeaderText("");
			WebView webView = new WebView();
			webView.getEngine().loadContent(f.toString());

			a.getDialogPane().setContent(webView);
			;
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
				String origin = Dialogs.showInputDialog(parentStage, "Please specify the board origin:", null);

				if (origin != null && (!origin.isEmpty())) {
					DumpFile dest = new DumpFile(dumpFileDir.getAbsolutePath(), origin);
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
		fileChooser.getExtensionFilters().add(filterZip);
		String name = df.getName();
		fileChooser.setInitialFileName(name.substring(0, name.length() - 3) + ".xlsx");
		fileChooser.setSelectedExtensionFilter(filterExcel);

		File file = fileChooser.showSaveDialog(parentStage);
		if (file != null) {
			FrameFile fr;
			if (file.getName().toLowerCase().endsWith(".xlsx")) {
				fr = new ExcelFile(file.getAbsolutePath());
			} else if (file.getName().toLowerCase().endsWith(".zip")) {
				fr = new ZipFrameFile(file.getAbsolutePath());
			} else {
				return;
			}
			try {
				taskDialog.showDialog(parentStage, new ExportFileTask(df, fr));
			} catch (IOException e) {
				log.error(e.getMessage());
				Dialogs.showErrorDialog(parentStage, "Failed to export data.");
			}
		}
	}

	@FXML
	public void downloadStartAction(ActionEvent event) {
		log.debug("Download start action");

		try {
			byte mode = dataModeChooser.showDialog(parentStage);
			if (mode == -1)
				return;
			DownloadBulkTask task = new DownloadBulkTask(logger, mode);
			taskDialog.showDialog(parentStage, task);
			try {
				dumpFileList.add(task.get());
				tabPane.getSelectionModel().select(tabDumps);
			} catch (CancellationException | InterruptedException | ExecutionException e) {
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
