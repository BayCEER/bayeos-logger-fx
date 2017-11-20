package bayeos.logger.live;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.StringConverter;

public class ChartPane extends BorderPane {
	
		
		private String channel;		
		private LineChart<Number, Number> lineChart;
		private StackPane valuePane;				
		private Number lastValue;
		private Label valueLabel;
		private Polygon pUp;
		private Polygon pDown;
		
		private static Logger log = Logger.getLogger(ChartPane.class);
		
		public void addData(Date ts, Number value) {
			XYChart.Series<Number, Number> series = lineChart.getData().get(0);
			
			if (value instanceof Float || value instanceof Double) {
				valueLabel.setText(String.format("%.3f", value));
			} else {
				valueLabel.setText(value.toString());
			}
			series.getData().add(new XYChart.Data<Number, Number>(ts.getTime(), value));
			valueLabel.setText(value.toString());
			if (lastValue != null) {
				pUp.setFill((value.doubleValue()>lastValue.doubleValue())?Color.GREEN:Color.GRAY);
				pDown.setFill((value.doubleValue()<lastValue.doubleValue())?Color.RED:Color.GRAY);				
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