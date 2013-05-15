package vis.map.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;



import vis.map.datamodel.DataSet;
import vis.map.metrics.AxisPairMetrics;



public class MainDisplay extends JPanel implements MouseListener, MouseMotionListener {


	// -1 denoted the not selected state
	private int dimension1 = -1;
	private int dimension2 = -1;
	private int dimension3 = -1;
	private int dimension4 = -1;
	
	
	private static int PADDING = 40;

     HashMap<String, Integer> visMap = null;
	/*
	 *  Parameters that control the computation of metrics-width, height of screen
	 */
	Point2D.Float param = new Point2D.Float(600, 600);
	DataSet data = null;
	//private BufferedImage[] imgArray;

	private boolean useColor = true;

	FileWriter entropyOutput = null;
	BufferedWriter bw = null;
	
    private ArrayList<AxisPairMetrics> metricsList = new ArrayList<AxisPairMetrics>();

	private ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();


	public static final float RECORD_COLOR_HIGH[] = {251/255f, 106/255f, 74/255f, .9f};

	public static final float RECORD_COLOR_MID[] = {0.874509804f, 0.760784314f, 0.490196078f, .1f};

	public static final float RECORD_COLOR_LOW[] = {49/255f, 130/255f, 189/255f, .9f};

	//a hack to stop calling the paint code repeatedly
	int callCount= 0;

	public static int totalNumberOfCombinations=0;

	/*
	 *  drawing mode can be PC or SP
	 */
	String drawingMode = "PC";
	private int numDimensions;


	public Axis axes[];

	/** Helper class for string the properties of an axis. */
	public class Axis {
		int dimension;

		float scale;

		float offset;

		String label;

		Axis(int dimension, float scale, float offset, String label) {
			
			this.dimension = dimension;
			this.scale     = scale;
			this.offset    = offset;
			this.label     = label;
		}
	}
	/*
	 * parameters for scaling
	 */
	//	private float axisOffset1;
	//	private float axisOffset2;
	//	private float scale1;
	//	private float scale2;

	/*
	 * lists for metrics
	 */
	private ArrayList<Float> distanceEntropyList = new ArrayList<Float>();
	private ArrayList<Float> jointEntropyList = new ArrayList<Float>();
	private ArrayList<Float> pixelEntropyList = new ArrayList<Float>();
	

	public static final double LOG_BASE_2 = Math.log(2);

	//	BufferedImage bufferImg = null ;
	

	public MainDisplay(){

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	
	public float getPadding(){
		
		return PADDING;
	}

	//	public void initBinning(int numBins) {
	//
	//		numDimensions = data.getNumDimensions();
	//		axisOffset1 = data.getMinValue(dimension1);
	//		axisOffset2 = data.getMinValue(dimension2);
	//		scale1 = numBins / (data.getMaxValue(dimension1) - data.getMinValue(dimension1));
	//		scale2 = numBins / (data.getMaxValue(dimension2) - data.getMinValue(dimension2));
	//	}

	public void initialize(DataSet data)
	{
		this.data = data;
		//this.mainDisplay = mainDisplay;
		totalNumberOfCombinations = (data.getNumDimensions()*(data.getNumDimensions()-1))/2;
		//imgArray = new BufferedImage[data.getNumDimensions()*data.getNumDimensions()];
		//	System.err.println("Data dimensions  " +data.getNumDimensions());

		System.err.println("Repaint");
		repaint();

		
		if (this.data != null) {
			//this.data.removeChangeListener(this);

			axes = null;

		//	brushValues = null;
		}

		this.data = data;

		if (data != null) {
	//		data.addChangeListener(this);

			axes = new Axis[data.getNumDimensions()];
			String axisNames[] = new String[data.getNumDimensions()];
	//		brushValues = new float[data.getNumRecords()];

			for (int i = 0; i < data.getNumDimensions(); i++) {
				// initialize scaling of axis to show maximum detail
				Axis newAxis = new Axis(i, data.getMinValue(i) - data.getMaxValue(i), data.getMaxValue(i), data.getAxisLabel(i));
				axes[i] = newAxis;
				axisNames[i] = newAxis.label;
			}

		

		}

		//	matrixView.updateCurrentAxes(axes);
//		currentBrush = null;

//		deepRepaint = true;

		repaint();

		//computeEntropy(imgArray[0]);

	}
	
	


	public void paint(Graphics g){

		super.paint(g);
		Graphics2D g2= (Graphics2D)g;
        System.err.println("Painting");

        if(data!=null && visMap!=null){
			
			
			System.err.println("Painting inside");
			dimension1 = visMap.get("position1");
			dimension2 = visMap.get("position2");
			
			dimension3 = visMap.get("size");
			dimension4 = visMap.get("color");
			
			drawScatterplot(g2, data, dimension1, dimension2, dimension3, dimension4);

			//processMetrics();


			//System.err.println("Call count " +callCount);

		}
		else 
			return;


	}


	/**
	 * TODO Put here a description of what this method does.
	 *
	 */
	

	protected Color getRecordColor(float point1, float point2, int numBins){

		float norm = (float)((point1/(float)numBins));
		float mult[] = {RECORD_COLOR_HIGH[0] * norm + RECORD_COLOR_LOW[0]*(1-norm), RECORD_COLOR_HIGH[1] * norm + RECORD_COLOR_LOW[1]*(1-norm), 
				RECORD_COLOR_HIGH[2] * norm + RECORD_COLOR_LOW[2]*(1-norm), 0.2f};

		Color color = new Color(mult[0], mult[1], mult[2]);

		return color;

	}

	


	private void drawScatterplot(Graphics g,DataSet data, int axis1, int axis2, int axis3, int axis4){

		//float axisOffset1 = parallelDisplay.getAxisOffset(axis1);
		//float axisOffset2 = parallelDisplay.getAxisOffset(axis2);
		//float scale1 = parallelDisplay.getAxisScale(axis1);
		//float scale2 = parallelDisplay.getAxisScale(axis2);
		Graphics2D ig = (Graphics2D)g;
		Graphics2D g2 = (Graphics2D)g;


		String[] dimNamesArray = new String[2];

		int w = this.getWidth();

		int h = this.getHeight();
		
		param = new Point2D.Float(w, h);

		BufferedImage bufferImg = new BufferedImage(this.getWidth(), this.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
		bufferImg = (BufferedImage)(this.createImage(w, h));

        int radius = 4;

		//setting up the BufferedImage properties
		ig = bufferImg.createGraphics();
		ig.setColor(this.getBackground());
		ig.fillRect(0, 0, this.getWidth(), this.getHeight());

		float scale1 = (data.getMaxValue(axis1) - data.getMinValue(axis1));
		float scale2 = (data.getMaxValue(axis2) - data.getMinValue(axis2));
		float scale3 = (data.getMaxValue(axis3) - data.getMinValue(axis3));
		float scale4 = (data.getMaxValue(axis4) - data.getMinValue(axis4));
		
		
		float axisOffset1 = data.getMinValue(axis1);
		float axisOffset2 = data.getMinValue(axis2);
		float axisOffset3 = data.getMinValue(axis3);
		float axisOffset4 = data.getMinValue(axis4);
	

		ig.setColor(new Color(0,0,0));
		ig.drawLine(PADDING, 0, PADDING, (int)param.y-PADDING);
		ig.drawLine(PADDING, (int)param.y-PADDING, (int)param.x+PADDING, (int)param.y-PADDING);
		
		
		System.err.println("Inside scatter");

		/*
		 * the loop for rendering all the points
		 */
		for(float[]dataRow : data){

			int v1 = (int)((dataRow[axis1] - axisOffset1) * (param.x-PADDING) / scale1);
			int v2 = (int)((dataRow[axis2] - axisOffset2) * (param.y-PADDING) / scale2);
			int v3 = (int)((dataRow[axis3] - axisOffset3)*(param.y-PADDING) / scale3);
		//	int v4 = (int)((dataRow[axis4] - axisOffset4) * (param.y) / scale2);
			//			if(useColor)
			//				ig.setColor(getRecordColor(v1,v2, (int)param.y));
			//			else
			ig.setColor(new Color(128,128,128, 120));

			//ig.drawLine((int)(v1), (int)(param.y-v2), (int)(v1)+2,(int)(param.y-v2)+2);	
			if(axis3!=-1)
			 radius = (int)getScaledRadius(v3);
		//	System.err.println("radius " +radius);
			
			//ig.drawOval((int)(v1), (int)(param.y-v2), radius, radius);
			ig.fillOval((int)(PADDING+v1), (int)(param.y-PADDING-v2), radius, radius);
		}

		float je = data.getAxisPair(axis1, axis2, this).getJointEntropy((int)param.y-PADDING);
		float se = data.getAxisPair(axis1, axis2, this).getSizeEntropy((int)param.y-PADDING, axis3);
		
		g2.drawImage(bufferImg, null, 0, 0);
		imageList.add(bufferImg);

		AxisPairMetrics metricObject = new AxisPairMetrics(axis1, axis2);

		
		//setUseColor(true);

		metricObject.storeImage(bufferImg);


		metricsList.add(metricObject);


	
	}

	
    private float getScaledRadius(int scaledValueForDimension) {
    	
    	
    	float radius = (float)(Math.sqrt((scaledValueForDimension/Math.PI)));
    	
    	
    //	System.err.println("  Scaled radius value  " + radius);
    	
    	return radius;
    	
		
		
	}


	@Override
	public void mouseDragged(MouseEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent evt) {
		//		double e= 0;


	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public ArrayList<AxisPairMetrics> getMetricsList(){

		return metricsList;
	}

	public int getNumAxes() {
		if (axes != null)
			return axes.length;
		else
			return 0;
	}
	
	public String getAxisLabel(int num) {
		if (data != null) {
			String label = axes[num].label;
			if (label != null)
				return label;
			else
				return ("X" + axes[num].dimension);
		} else {
			return null;
		}
	}


	//Called in response to user selection of a mapping between data dimension and a visual variable
	
	public void setMapping(int dataDimension, String visualvariableName) {
		
		if(visMap == null)
			visMap = new HashMap<String, Integer>();
		visMap.put(visualvariableName, dataDimension);
		repaint();
    
		
	}




}
