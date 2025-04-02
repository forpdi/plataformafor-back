package org.forpdi.system.reports.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;

public class LineChartGenerator {

	private Dataset dataset;
	private String xAxisLanel;
	private String yAxisLabel;
	private Dimension dimension = new Dimension(500, 280);
	
	public LineChartGenerator(Dataset dataset, String xAxisLanel, String yAxisLabel, Dimension dimension) {
		this(dataset, xAxisLanel, yAxisLabel);
		this.dimension = dimension;
	}

	public LineChartGenerator(Dataset dataset, String xAxisLanel, String yAxisLabel) {
		this.dataset = dataset;
		this.xAxisLanel = xAxisLanel;
		this.yAxisLabel = yAxisLabel;
	}

	public Image generate() throws BadElementException, MalformedURLException, IOException {
		final JFreeChart chart = ChartFactory.createLineChart(
				null, // chart title
				xAxisLanel, // x axis label
				yAxisLabel, // y axis label
				dataset.series, // data
				PlotOrientation.VERTICAL,
				true, // include legend
				false, // tooltips
				false // urls
		);
		
		chart.setBackgroundPaint(Color.WHITE);

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        for (int i = 0; i < dataset.series.getRowCount(); i++) {
        	renderer.setSeriesShapesVisible(i, true);
		}
        
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		return parseBase64ToElement(chart);
	}

	private Image parseBase64ToElement(final JFreeChart chart)
			throws IOException, BadElementException, MalformedURLException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			BufferedImage bufferedImage = chart.createBufferedImage(dimension.width, dimension.height);
			ImageIO.write(bufferedImage, "png", baos);
			String base64 = DatatypeConverter.printBase64Binary(baos.toByteArray());
			byte bytes[] = java.util.Base64.getDecoder().decode(base64);
			return Image.getInstance(bytes);
		}
	}
	
	public static class Dataset {
		private DefaultCategoryDataset series;

		private Dataset() {
			this.series = new DefaultCategoryDataset();
		}
	}
	
	public static class DatasetBuilder {
		private Dataset dataset;
		private String lastSerieAdded;

		public DatasetBuilder() {
			dataset = new Dataset();
		}
		
		public DatasetBuilder newSerie(String name) {
			lastSerieAdded = name;
			return this;
		}
		
		public DatasetBuilder add(String x, Number y) {
			if (lastSerieAdded == null) {
				throw new IllegalStateException("A new serie must be added before");
			}
			dataset.series.addValue(y, lastSerieAdded, x);
			return this;
		}
		
		public Dataset create() {
			return dataset;
		}
	}
}
