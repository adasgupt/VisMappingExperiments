/*

Copyright (c) 2001, 2002, 2003 Flo Ledermann <flo@subnet.at>

This file is part of parvis - a parallel coordiante based data visualisation
tool written in java. You find parvis and additional information on its
website at http://www.mediavirus.org/parvis.

parvis is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

parvis is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with parvis (in the file LICENSE.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */

package vis.map.datamodel;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import vis.map.gui.MainDisplay;
import vis.map.metrics.AxisPair;



/**
 * Provides a basic implementation of ParallelSpaceModel. Values are stored in a
 * two-dimensionsl array, labels are stored in arrays.
 * 
 * @author Flo Ledermann flo@subnet.at
 * @version 0.1
 */
public class DataSet implements Iterable<float[]>{

	/** Contains the float[] values. */
	protected ArrayList<float[]> values = new ArrayList<float[]>();

	/** Contains the record label Strings. */
	protected Vector<String> recordLabels = new Vector<String>();

	/** Contains the axis label Strings. */
	protected String axisLabels[];

	/** Contains the value label Hashtables. */
	protected Hashtable<Float, String> valueLabels[];

	/** Number of dimensions of the model. */
	protected int numDimensions = 0;

	protected int numBins=0;

	protected boolean newDataSetFlag=false;
	AxisPair axisPairs[][];

	/** List to store our event subscribers. */
	protected EventListenerList listeners = new EventListenerList();

	private float[] minValues;

	private float[] maxValues;
	
	HashMap<Integer, List<Integer>> convMap;
    
	HashMap<Integer, List<Integer>> divMap;
	
	public static final double LOG_BASE_2 = Math.log(2);
	
	private HashMap<Point2D, ArrayList<Integer>> startBinNeighborMap;

	/**
	 * Default Constructor.
	 */
	public DataSet() {

	}

	/**
	 * Initializes the model with a given float[][] array of values.
	 * 
	 * @param values
	 *            A float[][] containing records (first index) with float values
	 *            for each dimension (second index).<br>
	 *            All records must have the same number of dimensions!
	 */
	public DataSet(float values[][]) {
		int i;

		int len = values[0].length;

		for (i = 0; i < values.length; i++) {
			if (values[i].length != len) {
				throw new IllegalArgumentException(
				"Recordsets must have same number of dimensions");
			}
		}

		for (i = 0; i < values.length; i++) {
			this.values.add(values[i]);
		}

		initNumDimensions(len);
	}

	/**
	 * Adds a record. The record must have the same number of dimensions as the
	 * currently stored records.
	 * 
	 * @param values
	 *            The float values of the record.
	 * @param label
	 *            A String label for the record.
	 */
	public void addRecord(float values[], String label) {

		if (numDimensions == 0) {
			initNumDimensions(values.length);
		} else if (values.length != numDimensions) {
			throw new IllegalArgumentException(
					"Recordsets must have same number of dimensions ("
					+ numDimensions + ")");
		}

		this.values.add(values);
		recordLabels.addElement(label);

	}

	/**
	 * Adds a record. The record must have the same number of dimensions as the
	 * currently stored records.
	 * 
	 * @param values
	 *            The float values of the record.
	 */
	public void addRecord(float values[]) {
		addRecord(values, null);
	}

	/**
	 * Sets up all internal variables for the given number of dimensions. This
	 * must be done only once, usually in the constructor or by the first
	 * addRecord(). After the first call the number of dimensions is fixed and
	 * cannot be changed!
	 * 
	 * @param num
	 *            The number of dimensions.
	 *            
	 *            
	 */
	public void setNewDatasetFlag(boolean v){
        newDataSetFlag=v;		
	}
	
	public boolean getNewDatasetFlag(){
		
		return newDataSetFlag;
	}
	
	@SuppressWarnings("unchecked")
	protected void initNumDimensions(int num) {

		newDataSetFlag=true;
		setNewDatasetFlag(newDataSetFlag);
		if (numDimensions != 0) {
			throw new IllegalArgumentException(
					"Number of Dimensions already set to " + numDimensions
					+ "!");
			
		}

		numDimensions = num;

		axisLabels = new String[num];
		valueLabels = new Hashtable[num];

		for (int i = 0; i < num; i++) {
			axisLabels[i] = null;
			valueLabels[i] = null;
		}

		axisPairs = new AxisPair[num][num];
		minValues = new float[num];
		maxValues = new float[num];
		for (int i = 0; i < num; i++) {
			minValues[i] = Float.POSITIVE_INFINITY;
			maxValues[i] = Float.NEGATIVE_INFINITY;
		}
	}

	/**
	 * Returns the number of dimnesions.
	 * 
	 * @return The number of dimensions of the records in this model.
	 */
	public int getNumDimensions() {
		return numDimensions;
	}

	/**
	 * Returns the number of records.
	 * 
	 * @return The number of records currently stored in the model.
	 */
	public int getNumRecords() {
		return values.size();
	}

	/**
	 * Returns the maximum value for the given dimension.
	 * 
	 * @return Maximum value of all records for the given dimension.
	 */
	public float getMaxValue(int dimension) {
		if (maxValues[dimension] == Float.NEGATIVE_INFINITY)
			calculateMinMax(dimension);
		return maxValues[dimension];
	}

	/**
	 * Returns the minimum value for the given dimension.
	 * 
	 * @return Minimum value of all records for the given dimension.
	 */
	public float getMinValue(int dimension) {
		if (minValues[dimension] == Float.POSITIVE_INFINITY)
			calculateMinMax(dimension);
		return minValues[dimension];
	}

	/**
	 * Calculates minimum and maximum values for a given dimension and stores
	 * them in the minValues and maxValues arrays. Assumes that minValues and
	 * maxValues have been initialized with Float.POSITIVE_INFINITY and
	 * Float.NEGATIVE_INFINITY, respectively.
	 * 
	 * @param dimension
	 */
	private void calculateMinMax(int dimension) {
		for (float[] row : values) {
			if (row[dimension] > maxValues[dimension])
				maxValues[dimension] = row[dimension];
			if (row[dimension] < minValues[dimension])
				minValues[dimension] = row[dimension];
		}
	}

	/**
	 * Returns a specific value of the dataset.
	 * 
	 * @param record
	 *            The number of the record to be queried.
	 * @param dimension
	 *            The value of the record to be returned.
	 * 
	 * @return The value specified by record, dimension.
	 */
	public float getValue(int record, int dimension) {
		return values.get(record)[dimension];
	}

	/**
	 * Returns a String label for a specific dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * 
	 * @return A Human-readable label for the dimension.
	 */
	public String getAxisLabel(int dimension) {
		return axisLabels[dimension];
	}

	/**
	 * Sets the labels for all axes. Note that this method is not included in
	 * the ParallelSpaceModel interface, which defines only read-only methods.
	 * It is used for filling the model before passing it on to a consumer.
	 * 
	 * @param labels
	 *            An Array of Strings to be used as human-readable labels for
	 *            the axes.
	 */
	public void setAxisLabels(String labels[]) {
		for (int i = 0; i < labels.length; i++) {
			axisLabels[i] = labels[i];
		}
	}

	/**
	 * Sets the label of a single axis.
	 * 
	 * @param dimension
	 *            The dimension this label is for.
	 * @param label
	 *            The label.
	 */
	public void setAxisLabel(int dimension, String label) {
		axisLabels[dimension] = label;
	}

	/**
	 * Returns a Hashtable with labels for specific values. This is provided for
	 * ordinal values, which might be added as keys to the Hashtable, with the
	 * corresponding human-readable labels as values.
	 * 
	 * @param dimension
	 *            The dimension to retrieve value labels for.
	 * 
	 * @return A Hashtable containing value-label pairs.
	 */
	public Hashtable<Float, String> getValueLabels(int dimension) {
		return valueLabels[dimension];
	}

	/**
	 * Returns the label for a single value in a specific dimension, if present.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @param value
	 *            The value to look up a label for.
	 * 
	 * @return A String with the label, null if no label is set.
	 */
	public String getValueLabel(int dimension, float value) {
		if (valueLabels[dimension] != null) {
			return (String) (valueLabels[dimension].get(new Float(value)));
		} else {
			return null;
		}
	}

	/**
	 * Sets the value labels for a dimension. Note that this method is not
	 * included in the ParallelSpaceModel interface, which defines only
	 * read-only methods. It is used for filling the model before passing it on
	 * to a consumer.
	 * 
	 * @param dimension
	 *            The dimension the labels are to be set for.
	 * @param values
	 *            The values to assign labels to. Note that the number of labels
	 *            an values must match.
	 * @param labels
	 *            The String labels for the values. Note that the number of
	 *            labels an values must match.
	 */
	public void setValueLabels(int dimension, float values[], String labels[]) {
		if (values.length != labels.length) {
			throw new IllegalArgumentException(
			"number of values and labels do not match!");
		}

		if (valueLabels[dimension] == null) {
			valueLabels[dimension] = new Hashtable<Float, String>();
		}

		for (int i = 0; i < values.length; i++) {
			valueLabels[dimension].put(new Float(values[i]), labels[i]);
		}
	}

	/**
	 * Sets a single value label for a specific axis.
	 * 
	 * @param dimension
	 *            The dimension to set the label for.
	 * @param value
	 *            The value to set the label for.
	 * @param label
	 *            The label to set.
	 */
	public void setValueLabel(int dimension, float value, String label) {

		if (valueLabels[dimension] == null) {
			valueLabels[dimension] = new Hashtable<Float, String>();
		}

		valueLabels[dimension].put(new Float(value), label);

	}

	/**
	 * Returns all values of a specific record.
	 * 
	 * @param record
	 *            The number of the record to be returned.
	 * 
	 * @return All values of the specified record..
	 */
	public float[] getValues(int recordnum) {
		return values.get(recordnum);
	}

	public List<float[]> getValues() {
		return values;
	}
	
	/**
	 * Subscribes a ChangeListener with the model.
	 * 
	 * @param l
	 *            The ChangeListener to be notified when values change.
	 */
	public void addChangeListener(ChangeListener l) {
		listeners.add(ChangeListener.class, l);
	}

	/**
	 * Removes a previously subscribed changeListener.
	 * 
	 * @param l
	 *            The ChangeListener to be removed from the model.
	 */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(ChangeListener.class, l);
	}

	/**
	 * Returns a human-readable label for a specific record.
	 * 
	 * @param num
	 *            The record number.
	 * 
	 * @return A human-readable label for the record.
	 */
	public String getRecordLabel(int num) {
		return (String) recordLabels.elementAt(num);
	}

	public AxisPair getAxisPair(int axis1, int axis2, MainDisplay display) 
	{
//		int borderV = BasicParallelDisplayUI.getInstance().getBorderV();
		if (axisPairs[axis1][axis2] == null)
			
			axisPairs[axis1][axis2] = new AxisPair(this, axis1, axis2, display);
			
		//getGraph(axis1, axis2);
		
		return axisPairs[axis1][axis2];
	}

	public int[] getHistogram(int dimension, int numBins) 
	{

		float maxVal = getMaxValue(dimension);
		float minVal = getMinValue(dimension);
		float range = (maxVal - minVal);
		int[] hist = new int[numBins + 1];
		for (int i = 0; i < values.size(); i++) {
			float val = values.get(i)[dimension];
			val = val - minVal;
			int bin = (int) (numBins * (val / range));
			hist[bin]++;
		}
		return hist;

	}
	
   
	

	public int getMaxMinDistance(int[] edgeLengthList, boolean max) 
	{
		int k = 0;
		if (max == true) {
			int mxm = edgeLengthList[0];
			for (int i = 0; i < edgeLengthList.length; i++) {
				int v = edgeLengthList[i];
				if (v > mxm) {

					mxm = v;

				}

			}
			k = mxm;
		}

		if (max == false) {
			int min = edgeLengthList[0];
			for (int i = 0; i < edgeLengthList.length; i++) {
				int v = edgeLengthList[i];
				if (v < min) {

					min = v;

				}

			}
			k = min;

		}
		return k;
	}

	public int[] getDistanceHistogram(int dim1, int dim2, int numDataBins)  {
		int[] hist = new int[2 * numDataBins + 1];
		float min1 = getMinValue(dim1);
		float range1 = getMaxValue(dim1) - min1;
		float min2 = getMinValue(dim2);
		float range2 = getMaxValue(dim2) - min2;
		
		for (float[] row : values) {
			int bin1 = (int) (((row[dim1] - min1) / range1) * numDataBins);
			int bin2 = (int) (((row[dim2] - min2) / range2) * numDataBins);
			hist[bin2 - bin1 + numDataBins]++;
		}

		return hist;
	}

	public List<Double> getDistanceValues(int dim1, int dim2, int numDataBins){
		ArrayList<Double> distanceList=new ArrayList<Double>();

		for (float[] row : values) 
		{
			int bin1 = (int) ((row[dim1] - getMinValue(dim1)) / (getMaxValue(dim1) - getMinValue(dim1)) * numDataBins);
			int bin2 = (int) ((row[dim2] - getMinValue(dim2)) / (getMaxValue(dim2) - getMinValue(dim2)) * numDataBins);
			double distance=(double)bin2-bin1;
			double normalizedDistance=(distance/numDataBins);

			distanceList.add(normalizedDistance);
		}
		return distanceList;
	}

	
	//get OverPlotting
	public int[][] get2DHistogram(int dimension1, int dimension2, int numBins) {

		float maxVal1 = getMaxValue(dimension1);
		float minVal1 = getMinValue(dimension1);
		float maxVal2 = getMaxValue(dimension2);
		float minVal2 = getMinValue(dimension2);

		float range1 = (maxVal1 - minVal1);
		float range2 = (maxVal2 - minVal2);
		int[][] hist = new int[numBins + 1][numBins + 1];

		for (int i = 0; i < values.size(); i++) {
			float val1 = values.get(i)[dimension1];
			float val2 = values.get(i)[dimension2];
			val1 = val1 - minVal1;
			val2 = val2 - minVal2;
			int bin1 = (int) (numBins * (val1 / range1));
			int bin2 = (int) (numBins * (val2 / range2));
			hist[bin1][bin2]++;
		}
		
		
		return hist;
	}

	public HashMap<Integer, ArrayList<Integer>> getAdjacencyMap(int dimension1, int dimension2, int numBins) {

		HashMap<Integer, ArrayList<Integer>> adjMap = new HashMap<Integer, ArrayList<Integer>>();
		int[][] hist = get2DHistogram(dimension1, dimension2, numBins);
		for (int i = 0; i < hist[i].length - 1; i++) {
			for (int j = 0; j < hist[j].length - 1; j++) {
				if (hist[i][j] == 1) {

					Integer source = i;
					ArrayList<Integer> sink = adjMap.get(source);
					if (sink == null)
						adjMap.put(i, sink = new ArrayList<Integer>());
					sink.add(j);

				}

			}
		}
		return adjMap;
	}

	public int[] getDegree(int dimension1, int dimension2, int numBins, boolean inDegree) {

		int j = 0;
		int[] Degree = new int[numBins + 1];

		int[][] hist2D = get2DHistogram(dimension1, dimension2, numBins);
		convMap = new HashMap<Integer,List<Integer>>();
		divMap = new  HashMap<Integer,List<Integer>>();

		for (int i = 0; i < hist2D.length; i++) {
			for (j = 0; j < hist2D.length; j++) {

				// int countNumLines= hist2D[i][j];
				if (hist2D[i][j] > 0)

					if (inDegree) {
						// Degree[j]=Degree[j]+countNumLines;
						Degree[j]++;
						List<Integer> listVal = convMap.get(j);

						if (listVal == null)
							convMap.put(j, listVal = new ArrayList<Integer>());

						listVal.add(i);
					} else {
						// Degree[i]=Degree[i]+countNumLines;;
						Degree[i]++;
						List<Integer> listVal = divMap.get(j);

						if (listVal == null)
							divMap.put(i,
									listVal = new ArrayList<Integer>());
						listVal.add(j);
					}
			}
		}

		return Degree;
	}
	
	public HashMap<Integer, List<Integer>> getConvDivMap(boolean indegree) {

		HashMap<Integer, List<Integer>> m = null;
		if (indegree)
			m = convMap;
		else
			m = divMap;

		return m;
	}
		
		
		/*
		 * For a particular dimension the total outdegree of all the bins gives
		 * the number of lines between the adjacent axes
		 */
		// int numLines = 0;
		// for (int i = 0; i < numBins; i++)
		// numLines = numLines + outDegree[i];
		/*
		 * Convergence corresponds to indegree and Divergence corresponds to
		 * outdegree Couting average convergence/divergence per adjacent pair of
		 * axes gives us trends
		 */

	
	public int[] getEdgeLength(int dim1, int dim2, int numBins) {

		int binDistance = 0;

		int adjMatrix[][] = get2DHistogram(dim1, dim2, numBins);
		int edgeLength[] = new int[adjMatrix.length + 1];
		int c = 0;

		for (int i = 0; i < adjMatrix.length; i++) {
			for (int j = 0; j < adjMatrix.length; j++) {

				if (adjMatrix[i][j] != 0) {
					binDistance = i - j;

					edgeLength[c] = binDistance;
					c++;

				}

			}

		}

		return edgeLength;
	}

	
	public float getAbsoluteEntropy(int numBins, int dimension) {

		int[] hist = getHistogram(dimension, numBins);
		float numTotal = getNumRecords();
		float probabilityValue = 0;
		float logProbabilityValue = 0;
		float entropy = 0;
		float sumEntropy = 0;
		for (int i = 0; i < hist.length; i++) {
		
			if(hist[i]>0)
			probabilityValue = 1/(float)hist[i] ;
			if (probabilityValue > 0)
				logProbabilityValue = (float)((Math.log(1/probabilityValue))/ LOG_BASE_2);
			entropy = (probabilityValue * logProbabilityValue);
			sumEntropy = sumEntropy + entropy;
		}

		return sumEntropy;

	}
	
	public float getUniVariateSubSpaceConcentration(int dimension, int numBins, int minThreshold){


		startBinNeighborMap = new HashMap<Point2D, ArrayList<Integer>>();
		// if convergence is true choose the 2nd axis else choose the 1st axis.
       
		// Point containing axis and bin information, axis = 1 means 1st axis and 2 means 2nd axis.
		Point2D.Float chosenAxisAndStartBin = new Point2D.Float();
		int chosenAxis = 0;
		ArrayList<Integer> neighBorsList = null;
		ArrayList<Integer> probableNeighborsList = new ArrayList<Integer>(); 
		//model.getAxisPair(axis1, axis2, parallelDisplay).initGlobalBinning(numBins);
		//initGlobalBinning(numBins);
		//data.getAxisPair(axis1, axis2, this).initBinning(numBins);

	//	System.err.println(" Test numBins  " +numBins);
		int degree[] = getHistogram(dimension, numBins);
//		boolean convergence = model.getAxisPair(axis1, axis2, parallelDisplay).getConvergence_Divergence(numBins, 3);
		//boolean convergence = getConvergence_Divergence(numBins, 3);
		

		int numNeighbors =0;
		int numSparse =0;
		//int minThreshold = 30;
		float startBin = 0;
		int currentBin=0;

		while( currentBin < degree.length-1){

			// while the bin entries are above a min threshold, keep adding neighbors		

			while(degree[currentBin] > minThreshold)
			{
				
				// this is created when for the first time a dense bin is encountered
                if(neighBorsList == null)
                	neighBorsList = new ArrayList<Integer>();
				neighBorsList.add(currentBin);
				numNeighbors++;
				currentBin++;
				
				if( currentBin >= degree.length-1)
					   break;
				//	System.err.println(" Num dense " + currentBin);

			}

			while(degree[currentBin] <= minThreshold )
			{
				probableNeighborsList.add(currentBin);
				currentBin++;
				numSparse ++;		

				if( currentBin >= degree.length)
				   break;
				//	System.err.println(" Num sparse" + currentBin);

				
			}

			// if number of bins with zero entries/sparse entries is greater than 3, then cut off the cluster.

			if(numSparse > 3)
			{

				// check if sparse region exists even before a single dense bin occurs
				if(neighBorsList!=null)
				{
					chosenAxisAndStartBin = new Point2D.Float();
					chosenAxisAndStartBin.x = chosenAxis;
					chosenAxisAndStartBin.y = startBin;
					startBinNeighborMap.put(chosenAxisAndStartBin, neighBorsList);
				//	System.err.println("Start bin  " + startBin + "Num neighbors " +neighBorsList.size());
					
				}
				//	 reset neighbors, sparseBins and arraylist and set startBin to current Bin
				numSparse = 0;
				numNeighbors = 0;
				neighBorsList = new ArrayList<Integer>();
				startBin = currentBin;



			}

			else

				// if sparseRegion < 3 bins then add the entries from the sparse bins in the cluster.		
			{
				if(neighBorsList!=null)
				neighBorsList.addAll(probableNeighborsList);
				
				// reset probable neighbors' list
				probableNeighborsList = new ArrayList<Integer>();


			}


		}

		//System.err.println(" Number of clumped subspaces  " + startBinNeighborMap.keySet().size());

		float sumClumping =0;
			
			Iterator e = startBinNeighborMap.keySet().iterator();
			
			while(e.hasNext()){
				
			Point2D.Float key = (Point2D.Float)e.next();
			System.err.println("Chosen axis " + key.x);
			
		//	System.err.println("Number of neighbors  " + startBinNeighborMap.get(key).size());

		    sumClumping = sumClumping + startBinNeighborMap.get(key).size();

			}
			
			float avgClumping = sumClumping/(float)startBinNeighborMap.keySet().size();
			
			System.err.println(" Averagee clumping " + avgClumping);
			
			return avgClumping;

	}

	public Iterator<float[]> iterator() {
		return values.iterator();
	}

}