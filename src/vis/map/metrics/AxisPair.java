package vis.map.metrics;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import vis.map.datamodel.DataSet;
import vis.map.gui.MainDisplay;




public final class AxisPair {

	public enum Metrics {
		//		NumCrossings(-1),
		//		AngleOfCrossings(90),
		JointEntropy(10),
		DistanceEntropy(10),
		InformationLoss(10);
		//	PrincipleDirection(1),
		//		Parallelism(1),
		//		SubSpaceClumping(10);
		//	MutualInformation(6),
		//	PixelBasedEntropy(6);
		//	Convergence_Divergence(-1),
		//Overplotting(-1),

		//		InDegree(-1);

		private float range;
		private float cutOff;
		private Metrics(float range) {
			this.range = range;
		}

		public float getRange(DataSet model) {
			if (range > 0)
				return range;
			else switch(this) {
			//case NumCrossings:
			//	return (float) (model.getNumRecords() * model.getNumRecords())/2;
			//case Convergence_Divergence:
			//	return model.getNumRecords()/6;
			//case Overplotting:
			//	return model.getNumRecords();
			default:
				return 1;
			}
		}

		public float getCutOff(DataSet model){


			switch(this){
			//		case NumCrossings:
			//			return (float)(0.5);
			//		case AngleOfCrossings:
			//			return (float)(0.5);
			//			case PrincipleDirection :
			//				return (float)(0.5);
			//			case Parallelism:
			//				return (float)(0.5);
			//			case MutualInformation:
			//				return (float)(1.0);
			//		case Convergence_Divergence:
			//			return (float)model.getNumRecords()/10;
			//		case Overplotting:
			//			return (float)model.getNumRecords()/10;
			default:
				return 1;


			}
		}
	}

	public final class ValuePair {
		private float val;
		private float val_inverted;

		public ValuePair(float v, float iv) {
			val = v;
			val_inverted = iv;
		}

		public float getValue() {
			return val;
		}

		public float getInvertedValue() {
			return val_inverted;
		}

		public float getSmallerValue() {
			return Math.min(val, val_inverted);
		}

		public float getLargerValue() {
			return Math.max(val, val_inverted);
		}

		public boolean IsInvertedLarger() {
			return val_inverted > val;
		}

		@Override
		public String toString() {
			return "("+val+", "+val_inverted+")";
		}
	}

	int dimension1;

	int dimension2;

	DataSet model;

	ValuePair numCrossings;

	int numDimensions;

	private float axisOffset1;

	private float axisOffset2;

	private float scale1;

	private float scale2;

	private static float globalAxisOffset1;

	private static float globalAxisOffset2;

	private static float globalScale1;

	private static float globalScale2;

	public static final double LOG_BASE_2 = Math.log(2);

	private int anglesHistogram[];

	private int anglesHistogram_inverted[];


	private float conditionalEntropyCached = -1;

	private float jointEntropyCached = -1;

	private float mutualInformationCached = -1;

	private int overplottingMedianCached = -1;

	private ValuePair overplotting;

	private ValuePair anglesOfCrossingsMedian;

	private ValuePair parallelism;

	private Point2D.Float parallelismMetric;

	private MainDisplay mainDisplay;

	private ValuePair mutualInformation;

	private int[] distanceHistogram;

	private int[] distanceHistogramScatter;

	private int[][] twoDHistogram;

	private int[] distanceHistogram_inverted;

	private HashMap<Point2D, ArrayList<Integer>> startBinNeighborMap;

	private HashMap<Integer, Integer> pixelDataMap;

	public AxisPair(DataSet m, int axis1, int axis2, MainDisplay gl) {

		dimension1 = axis1;
		dimension2 = axis2;
		model = m;
		this.mainDisplay = gl;
	}

	public void initBinning(int numBins) {
		//numDimensions = parallelDisplay.getNumAxes();
		axisOffset1 = model.getMinValue(dimension1);
		axisOffset2 = model.getMinValue(dimension2);
		scale1 = numBins / (model.getMaxValue(dimension1) - model.getMinValue(dimension1));
		scale2 = numBins / (model.getMaxValue(dimension2) - model.getMinValue(dimension2));
	}


	/**
	 * This mirrors the getYValue() function in BasicParallelDisplayUI, but is a
	 * lot more efficient. Also has a better name.
	 */
	private int value2pixel(float value, boolean firstDimension, boolean inverted, int numBins) {
		if (firstDimension)
			value = (value - axisOffset1) * scale1;
		else
			value = (value - axisOffset2) * scale2;

		if (inverted)
			return numBins - (int) value;
		else
			return (int)value;
	}

	//	private int globalValue2pixel(float value, boolean firstDimension, boolean inverted, int numBins) {
	//		if (firstDimension)
	//			value = (value - globalAxisOffset1) * globalScale1;
	//		else
	//			value = (value - globalAxisOffset2) * globalScale2;
	//
	//		if (inverted)
	//			return numBins - (int) value;
	//		else
	//			return (int)value;
	//	}

	public ValuePair getNumCrossings(int numBins) {

		//		if (numCrossings == null) {
		//			if (dimension1 > dimension2)
		//				return model.getAxisPair(dimension2, dimension1, parallelDisplay).getNumCrossings(numBins);
		//			else {
		//				int num = calculateCrossings(numBins, false);
		//				//int num_inverted = calculateCrossings(numBins, true);
		//				
		//				numCrossings = new ValuePair(num, num);
		//
		////				numCrossings = calculateCrossings(numBins);
		//			}
		//		}

		float crossings = calculateCrossings(numBins).getValue();
		numCrossings = new ValuePair(crossings, crossings);

		return numCrossings;
	}

	public int[] getAngleOfCrossingHistogram(int numBins, boolean inverted) {

		if (dimension1 > dimension2)
			return model.getAxisPair(dimension2, dimension1, mainDisplay).getAngleOfCrossingHistogram(numBins, inverted);
		else {
			if (anglesHistogram == null)
				getNumCrossings(numBins);
			if (inverted)
				return anglesHistogram_inverted;
			else
				return anglesHistogram;
		}
	}


	// for calculation of the median and interquartile range

	public ValuePair getAngleOfCrossingMedian(int numBins) {

		// List<Double> distanceList = getDistanceValues(dim1, dim2,
		// numDataBins);
		if (anglesOfCrossingsMedian == null) {
			if (dimension1 > dimension2)
				anglesOfCrossingsMedian = model.getAxisPair(dimension2, dimension1, mainDisplay).getAngleOfCrossingMedian(numBins);
			else {

				if (numCrossings == null)
					getNumCrossings(numBins);

				int median = findHistogramLocation(getAngleOfCrossingHistogram(numBins, false), (int)(numCrossings.getValue()/2));

				//int median_inverted = findHistogramLocation(getAngleOfCrossingHistogram(numBins, true), (int)(numCrossings.getInvertedValue()/2));

				anglesOfCrossingsMedian = new ValuePair(median, median);
			}
		}
		return anglesOfCrossingsMedian;
	}

	private int findHistogramLocation(int histogram[], int count) {
		int location = 0;
		int total = 0;
		while (total < count) {
			total += histogram[location];
			location++;
		}
		return location;
	}

	// for median and interquartile deviation
	public Point2D.Float getParallelism(int numBins) {

		if (parallelism == null) {
			if (dimension1 > dimension2)
				parallelismMetric = model.getAxisPair(dimension2, dimension1, mainDisplay).getParallelism(numBins);
			else {

				int quartileCount = model.getNumRecords()/4;

				int distanceHistogram[] = getDistanceHistogram(numBins, false);

				float range = findHistogramLocation(distanceHistogram, quartileCount*3) - findHistogramLocation(distanceHistogram, quartileCount);

				distanceHistogram = getDistanceHistogram(numBins, true);

				float range_inverted = findHistogramLocation(distanceHistogram, quartileCount*3) - findHistogramLocation(distanceHistogram, quartileCount);

				float median = findHistogramLocation(distanceHistogram, quartileCount*2);
				//normalization
				//parallelism = new ValuePair(1-(range/numBins), 1-(range_inverted/numBins));

				parallelismMetric = new Point2D.Float(median/(2*numBins), 1-(range/(2*numBins)));

				//	parallelismMetric = new Point2D.Float(median, range);

				//	System.out.println(parallelism);

			}
		}

		return parallelismMetric;
	}	

	private int calculateCrossings(int numBins, boolean inverted) {
		initBinning(numBins);

		//	int histogram[];

		//		if (!inverted) {
		//			anglesHistogram = new int[91];
		//			histogram = anglesHistogram;
		//		} else {
		//			anglesHistogram_inverted = new int[91];
		//			histogram = anglesHistogram_inverted;
		//		}

		int crossings = 0;

		//		for (int i = 0; i < model.getNumRecords(); i++) {
		//			float row1[] = model.getValues(i);
		//			for (int j = i + 1; j < model.getNumRecords(); j++) {
		//				float row2[] = model.getValues(j);
		//				if (row1 != row2) {
		//					int a1 = value2pixel(row1[dimension1], true, false, numBins);
		//					int b1 = value2pixel(row1[dimension2], false, false, numBins);
		//					
		//					int a2 = value2pixel(row2[dimension1], true, inverted, numBins);
		//					int b2 = value2pixel(row2[dimension2], false, inverted, numBins);
		//
		//					if ((a1 < a2 && b1 < b2) || (a1 > a2 && b1 > b2)) {	
		//						crossings++;
		//
		//						double interval1 = a1 - b1;
		//						double interval2 = a2 - b2;
		//					
		//						double lineSpacing = parallelDisplay.getWidth()/ (numDimensions - 1);
		//						float angleofCrossing = 0f;
		//
		//						double alphaRadian = Math.atan(lineSpacing / interval1);
		//						double alphaDegree = Math.toDegrees(alphaRadian);
		//
		//						double betaRadian = Math.atan(lineSpacing / interval2);
		//						double betaDegree = Math.toDegrees(betaRadian);
		//
		//						if (alphaDegree < 0)
		//							alphaDegree = 180 + alphaDegree;
		//						if(alphaDegree>90)	
		//							alphaDegree = 180-alphaDegree;
		//					   if (betaDegree < 0)
		//						
		//						betaDegree = 180 + betaDegree;
		//						if(betaDegree>90)	
		//							betaDegree = 180-betaDegree;
		//
		//						angleofCrossing = (float)(180-(alphaDegree + betaDegree));
		//						
		//					    
		//					
		//					//   if (alphaDegree > betaDegree)
		//					//		angleofCrossing = (float)(alphaDegree - betaDegree);
		//					//        else
		//					//		angleofCrossing = (float)(betaDegree - alphaDegree);
		//					  
		//					  
		//						if (angleofCrossing > 90)
		//							angleofCrossing = (float)(180-angleofCrossing);
		//
		//						histogram[(int)angleofCrossing]++;
		//					}
		//				}
		//			}
		//		}

		for(int axis1Bin = 0; axis1Bin< numBins; axis1Bin++){

			System.err.println("Computing");
			for(int axis2Bin = 0; axis2Bin< numBins; axis2Bin++)
			{

				int a1 = axis1Bin;
				int a2 = axis2Bin;

				int b1 = axis1Bin+1;
				int b2 = axis2Bin+1;

				if ((a1 < a2 && b1 < b2) || (a1 > a2 && b1 > b2)) {	


					int numLines1 = get2DHistogram(numBins)[a1][b1];
					int numLines2 = get2DHistogram(numBins)[a1][b2];
					int numLines3 = get2DHistogram(numBins)[a2][b1];
					int numLines4 = get2DHistogram(numBins)[a2][b2];

					crossings = numLines1+numLines2+numLines3+numLines4;

				}			

			}

		}

		return crossings;
	}

	public List<Double> getDistanceValues(int numDataBins){

		ArrayList<Double> distanceList=new ArrayList<Double>();

		for (float[] row : model.getValues()) 
		{
			int bin1 = value2pixel(row[dimension1], true, false, numDataBins);
			int bin2 = value2pixel(row[dimension2], false, false, numDataBins);
			double distance=(double)(bin2-bin1);

			System.err.println("  Distance value  *******************   "+ distance );

			//double normalizedDistance=(distance/(numDataBins));

			distanceList.add(distance);
		}
		return distanceList;
	}

	private ValuePair calculateCrossings(int numBins) {

		calculateHistograms(numBins);

		anglesHistogram = new int[91];
		anglesHistogram_inverted = new int[91];

		int crossings = 0;
		int crossings_inverted = 0;
		for (int i = 0; i < numBins; i++)
			for (int j = 0; j < numBins; j++)
				if (twoDHistogram[i][j] > 0 || twoDHistogram[i][numBins-j] > 0) {
					for (int k = i+1; k < numBins; k++)
						for (int l = j+1; l < numBins; l++) {
							int num = twoDHistogram[i][j]*twoDHistogram[k][l];
							if (num > 0) {
								crossings += num;
								updateAngleHistogram(i, j, k, l, num, anglesHistogram);
							}

							//							int num_inverted = twoDHistogram[i][numBins-j]*twoDHistogram[k][numBins-l];
							//							if (num_inverted > 0) {
							//								crossings_inverted += num_inverted;
							//								updateAngleHistogram(i, numBins-j, k, numBins-l, num_inverted, anglesHistogram_inverted);
							//							}
						}
				}

		return new ValuePair(crossings, crossings);
	}



	private void updateAngleHistogram(int i, int j, int k, int l, int num, int[] histogram) {
		double interval1 = j - i;
		double interval2 = l - k;

		double lineSpacing = mainDisplay.getWidth()/ (numDimensions - 1);
		float angleofCrossing = 0f;

		double alphaRadian = Math.atan(lineSpacing / interval1);
		double alphaDegree = Math.toDegrees(alphaRadian);

		double betaRadian = Math.atan(lineSpacing / interval2);
		double betaDegree = Math.toDegrees(betaRadian);

		if (alphaDegree < 0)
			alphaDegree = 180 + alphaDegree;
		if(alphaDegree > 90)	
			alphaDegree = 180-alphaDegree;

		if (betaDegree < 0)
			betaDegree = 180 + betaDegree;
		if(betaDegree > 90)	
			betaDegree = 180-betaDegree;

		angleofCrossing = (float)(180-(alphaDegree + betaDegree));

		if (angleofCrossing > 90)
			angleofCrossing = (float)(180-angleofCrossing);

		histogram[(int)angleofCrossing] += num;
	}


	private void calculateHistogramsScatter(int numBins) {
		initBinning(numBins);
		System.err.println("Numbins  " +numBins);
		distanceHistogramScatter = new int[(numBins * 4) + 1];
		//		distanceHistogram_inverted = new int[numBins * 2 + 1];
		//		twoDHistogram = new int[numBins + 1][numBins + 1];
		for(int rec1=0; rec1<model.getNumRecords(); rec1++)
		{
			int rowRec1Dim1 = value2pixel(model.getValues(rec1)[dimension1], true, false, numBins);
			int rowRec1Dim2 = value2pixel(model.getValues(rec1)[dimension2], false, false, numBins);

			for(int rec2=0; rec2< rec1; rec2++)
			{

				int rowRec2Dim1 = value2pixel(model.getValues(rec2)[dimension1], true, false, numBins);
				int rowRec2Dim2 = value2pixel(model.getValues(rec2)[dimension2], false, false, numBins);

				int dist = (rowRec1Dim1 - rowRec2Dim1)+(rowRec1Dim2-rowRec2Dim2);
				//	    if(dist<(2*numBins)|| dist>(2*numBins))
				//	System.err.println("Distance  "+ dist);
				//	System.err.println("val1 " +model.getValues(dimension1)[rec1]+ " val2 " +rowRec2Dim1 +" val3 " +rowRec1Dim2 + " val4 " +rowRec2Dim2);
				distanceHistogramScatter[dist+ (2*numBins)]++;



			}
		}

	}

	private void calculateHistograms(int numBins) {
		initBinning(numBins);


		//System.err.println("histograms for "+dimension1+", "+dimension2);

		distanceHistogram = new int[numBins * 2 + 1];
		distanceHistogram_inverted = new int[numBins * 2 + 1];
		twoDHistogram = new int[numBins + 1][numBins + 1];

		for (float row[] : model) {
			int bin1 = value2pixel(row[dimension1], true, false, numBins);
			int bin2 = value2pixel(row[dimension2], false, false, numBins);

			distanceHistogram[bin2 - bin1 + numBins]++;
			//distanceHistogram_inverted[bin2 - (numBins - bin1) + numBins]++;

			twoDHistogram[bin1][bin2]++;
		}

		//	System.err.println("histograms done.");
	}


	public float getAbsoluteEntropy(int numBins) {

		int[] hist1 = model.getHistogram(dimension1, numBins);
		double numTotal = model.getNumRecords();
		double probabilityValue = 0;
		double logProbabilityValue = 0;
		double entropy = 0;
		float sumEntropy = 0;
		for (int i = 0; i < hist1.length; i++) {
			probabilityValue = hist1[i] / numTotal;
			if (probabilityValue > 0)
				logProbabilityValue = (Math.log(1/probabilityValue))/ LOG_BASE_2;
			entropy = (probabilityValue * logProbabilityValue);
			sumEntropy =(float)( sumEntropy + entropy);
		}

		return sumEntropy;

	}

	public float getSizeEntropy(int numBins, int sizeDimension){

		//create histogram of sizes
		int sizeHistogram[] = getSizeHistogram(numBins, sizeDimension);
		
		double numTotal = model.getNumRecords();
		double probabilityValue = 0;
		double logProbabilityValue = 0;
		double entropy = 0;
		float sumEntropy = 0;

		for(int bin=0; bin<sizeHistogram.length; bin++)
		{

			probabilityValue = sizeHistogram[bin]/(float)numTotal;
			
	
		    if (probabilityValue > 0)
				logProbabilityValue = (Math.log(1/probabilityValue))/ LOG_BASE_2;
			entropy = (probabilityValue * logProbabilityValue);
			sumEntropy =(float)( sumEntropy + entropy);

		}

        System.err.println("Size Entropy +++++++++++ " +sumEntropy);
		return sumEntropy;

	}


	private int[] getSizeHistogram(int numBins, int sizeDimension) {

		int[] sizeHistogram = new int[(int)Math.PI*(numBins*numBins)];
		float scale = (model.getMaxValue(sizeDimension) - model.getMinValue(sizeDimension));
		float axisOffset = model.getMinValue(sizeDimension);
		float binnedVal =0;
		int radius = 0;

		for(float dataRow[]: model)
		{
			binnedVal = ((dataRow[sizeDimension] - axisOffset)*(mainDisplay.getHeight()- mainDisplay.getPadding()) / scale);

			radius = (int)(Math.sqrt((binnedVal/Math.PI)));
			
		

			sizeHistogram[radius]++;



		}

		return sizeHistogram;
	}

	public float getJointEntropy(int numBins) {

		//		int[] histdim1 = ParallelDisplay.getInstance().getModel().getHistogram(dimension1, numBins);

		int[] histdim2 = model.getHistogram(dimension2, numBins);

		int[][] hist2 = get2DHistogram(numBins);

		double numTotal = model.getNumRecords();

		double jointprobabilityValue = 0;
		//		double probabilityValueDimension1 = 0;
		double probabilityValueDimension2 = 0;
		//		double conditionalprobabilityValue = 0;
		double conditionalEntropy = 0;
		double logprobabilityValue = 0;
		double sumConditionalEntropy = 0;
		double jointEntropy =0;
		double sumJointEntropy =0;

		if (conditionalEntropyCached < 0) {
			if (dimension1 > dimension2)
				jointEntropyCached = model.getAxisPair(dimension2, dimension1, mainDisplay).getJointEntropy(numBins);
			else {
				for (int i = 0; i < hist2.length; i++) 
				{

					for (int j = 0; j < hist2.length; j++) 
					{
						jointprobabilityValue = hist2[i][j] / (double) numTotal;
						// probabilityValueDimension1=histdim1[i]/(double)numTotal;
						//probabilityValueDimension2 = histdim2[j] / (double) numTotal;

						if (jointprobabilityValue > 0) 
						{
							//							conditionalprobabilityValue = jointprobabilityValue	/ probabilityValueDimension2;
							// System.out.println(" *** CondP ****  " +
							// jointprobabilityValue);

							logprobabilityValue = (Math.log(jointprobabilityValue))/ LOG_BASE_2;
							// System.out.println(" *** LOG  *** " +
							// logprobabilityValue);
							jointEntropy = -jointprobabilityValue*logprobabilityValue;


							sumJointEntropy = (float)(sumJointEntropy+ jointEntropy);

						}
					}
				}

				System.err.println("Joint entropy " + sumJointEntropy);
				jointEntropyCached = (float)sumJointEntropy;
			}
		}
		return jointEntropyCached;

	}


	public float getKLDivergence(int numPixelBins){


		int numDataBins = model.getNumRecords();
		int[][] imageHist= model.get2DHistogram(dimension1,dimension2, numPixelBins);
		int[][] dataHist = model.get2DHistogram(dimension1,dimension2, model.getNumRecords());


		float[] imageProbabilityArray = new float[model.getValues().size()];
		float[] dataProbabilityArray =  new float[model.getValues().size()];

		for(int recordNum=0; recordNum<model.getValues().size(); recordNum++)
		{
			float val1 = model.getValues().get(recordNum)[dimension1];
			float val2 = model.getValues().get(recordNum)[dimension2];
			val1 = val1 - model.getMinValue(dimension1);
			val2 = val2 - model.getMinValue(dimension2);

			int pixelbin1 = (int) (numPixelBins * (val1 / (model.getMaxValue(dimension1)-model.getMinValue(dimension1))));
			int pixelbin2 = (int) (numPixelBins * (val2 / (model.getMaxValue(dimension2)-model.getMinValue(dimension2))));

			int databin1 = (int) (numDataBins * (val1 / (model.getMaxValue(dimension1)- model.getMinValue(dimension1))));
			int databin2 = (int) (numDataBins * (val2 / (model.getMaxValue(dimension2)- model.getMinValue(dimension2))));

			imageProbabilityArray[recordNum] = imageHist[pixelbin1][pixelbin2]/(float)(imageHist.length);
			dataProbabilityArray[recordNum] =  dataHist[databin1][databin2]/(float)(dataHist.length);


		}


		float klDiv = 0f;

		for (int i = 0; i <imageProbabilityArray.length; ++i) {
			if (imageProbabilityArray[i] == 0) { continue; }
			if (dataProbabilityArray[i] == 0.0) { continue; } 

			klDiv += imageProbabilityArray[i] * Math.log( imageProbabilityArray[i] / dataProbabilityArray[i]);
		}

		System.err.println(" KLDiv    *********************  " +klDiv);
		klDiv= (float)(klDiv/LOG_BASE_2);

		return klDiv;
	}






	// TODO: Take inversions into account. Currently only does the non-inverted case.
	public ValuePair getMutualInformation(int numBins) {
		if (mutualInformationCached < 0) {
			if (dimension1 > dimension2)
				mutualInformation = model.getAxisPair(dimension2, dimension1, mainDisplay).getMutualInformation(numBins);
			else {
				float absoluteEntropy = getAbsoluteEntropy(numBins);
				float conditionalEntropy = getJointEntropy(numBins);
				float mutInf = absoluteEntropy - conditionalEntropy;
				mutualInformation = new ValuePair(mutInf, mutInf);
			}
		}
		return mutualInformation;
	}


	public ValuePair getMutualInfo(int numBins) {
		if (mutualInformationCached < 0) {
			if (dimension1 > dimension2)
				mutualInformation = model.getAxisPair(dimension2, dimension1,
						mainDisplay).getMutualInfo(numBins);
			else {

				int[] histDimension1 = model.getHistogram(dimension1, numBins);
				int[] histDimension2 = model.getHistogram(dimension2, numBins);

				int[][] hist2D = get2DHistogram(numBins);
				float jointProb = 0;
				float dim1Prob = 0;
				float dim2Prob = 0;
				float numTotal = model.getNumRecords();
				float sum = 0;
				float val = 0;
				for (int i = 0; i < numBins; i++) {
					for (int j = 0; j < numBins; j++) {

						//if(hist2D[i][j]>0)
						jointProb = (float) hist2D[i][j]/ numTotal;
						//if(histDimension1[i]>0)
						dim1Prob = (float) histDimension1[i]/ numTotal;
						//if(histDimension2[j]>0)
						dim2Prob = (float) histDimension2[j]/ numTotal;
						if (jointProb > 0 && dim1Prob > 0 && dim2Prob > 0) {
							float logPrepare = (float) jointProb
									/ (dim1Prob * dim2Prob);
							// System.err.println( " TEST " + logPrepare);
							float logP = (float) (Math.log((double) logPrepare) / LOG_BASE_2);
							val = jointProb * logP;
							sum = sum + val;

						}
					}
				}

				sum = sum/(float)10;
				mutualInformation = new ValuePair(sum, sum);

			}
		}

		return mutualInformation;

	}

	// TODO: Take inversions into account. Currently only does the non-inverted case.
	public ValuePair getDegreeOfOverPlotting(int numBins) {

		int[][] hist2 = get2DHistogram(numBins);
		int counter = 0;

		if (overplotting == null) {
			if (dimension1 > dimension2)
				overplotting = model.getAxisPair(dimension2, dimension1, mainDisplay).getDegreeOfOverPlotting(numBins);
			else {

				for (int i = 0; i < hist2.length; i++) {
					for (int j = 0; j < hist2.length; j++) {

						if (hist2[i][j] > 2) {
							// System.out.println("Bin Number on axis 1   " +i);
							// System.out.println("Bin Number on axis 2" +j);
							// System.out.println("Number of edges  " +hist[i][j]);
							counter = counter + hist2[i][j];

						}
					}
				}
				overplotting = new ValuePair(counter, counter);
			}
		}

		return overplotting;
	}

	//	public int getMedianOverPlotting(int numBins) {
	//		
	//	
	//
	//		int overplottingMedian =0;
	//		if (overplottingMedianCached < 0) {
	////			if (dimension1 > dimension2)
	////				overplottingMedianCached = model.getAxisPair(dimension2, dimension1, parallelDisplay).getMedianOverPlotting(numBins);
	////			else {
	////				int[][] hist2 = get2DHistogram(numBins);
	////
	////				int[] overPlottingHistogram = new int[numBins*numBins];
	////				int total = 0;
	////				
	////				for (int i = 0; i < hist2.length; i++) {
	////					for (int j = 0; j < hist2.length; j++) {
	////						int val = hist2[i][j];
	////						if (val > 0) {
	////							overPlottingHistogram[i]= val;
	////							//total++;
	////						}
	////					}
	////				}
	//				int medianCount = (numBins) / 2;
	//				int[] degree = getDegree(numBins, true);
	//				overplottingMedian = findHistogramLocation(degree, medianCount);
	//
	//				System.err.println(" Median " + overplottingMedian);
	//			}
	//		
	//
	//		return overplottingMedian;
	//	}

	public boolean getConvergence_Divergence(int numBins, int threshold){

		int[] degreeOfConvergence = getDegree(numBins, true);
		int[] degreeOfDivergence =  getDegree(numBins, false);
		boolean convergence = true;
		float totalConvergenceDegreeVal=0;
		float totalDivergenceDegreeVal=0;
		float averageConvergenceDegreeVal=0;
		float averageDivergenceDegreeVal=0;
		float returnVal=0;
		int numCVal=0;
		int numDVal=0;
		for(int i=0;i<degreeOfConvergence.length;i++)
		{

			if(degreeOfConvergence[i]>threshold)
			{	
				numCVal++;
				totalConvergenceDegreeVal = totalConvergenceDegreeVal + degreeOfConvergence[i];

			}

		}
		if(numCVal!=0)
			averageConvergenceDegreeVal=(float)totalConvergenceDegreeVal/numCVal;

		for(int i=0;i<degreeOfDivergence.length;i++)
		{

			if(degreeOfDivergence[i]>threshold)
			{
				numDVal++;

				totalDivergenceDegreeVal = totalDivergenceDegreeVal + degreeOfDivergence[i];

			}

		}
		if(numDVal!=0)
			averageDivergenceDegreeVal=(float)totalDivergenceDegreeVal/numDVal;
		if(averageDivergenceDegreeVal > averageConvergenceDegreeVal)
		{
			returnVal= averageDivergenceDegreeVal;
			convergence = false;
			//System.err.println("Divergence   "  + averageDivergenceDegreeVal);

		}
		else
		{
			returnVal=(float)averageConvergenceDegreeVal;
			convergence = true;
			//System.err.println("Convergence   " + averageConvergenceDegreeVal);

		}
		//return new ValuePair(returnVal, returnVal);

		return convergence;

	}


	public ValuePair getMetric(Metrics metric, int numBins) {
		switch (metric) {
		//		case NumCrossings:
		//			ValuePair n = getNumCrossings(numBins);
		//			float factor = model.getNumRecords()*(model.getNumRecords()-1)/2;
		//			ValuePair n_norm = new ValuePair(n.getValue()/factor, n.getInvertedValue()/factor);
		//			return n_norm;

		//		case Overplotting:
		//			return getDegreeOfOverPlotting(numBins);
		//
		//		case AngleOfCrossings:
		//			return getAngleOfCrossingMedian(numBins);
		//		case Convergence_Divergence:
		//			return getConvergence_Divergence(numBins, 30);

		//		case PrincipleDirection :
		//
		//			float medianParallelism = getParallelism(numBins).x;
		//			ValuePair medParallelism = new ValuePair(medianParallelism, medianParallelism);
		//			return medParallelism;

		//		case Parallelism :
		//			float rangeParallelism = getParallelism(numBins).y;
		//			ValuePair rangParallelism = new ValuePair(rangeParallelism, rangeParallelism);
		//			return rangParallelism;
		//
		//		case SubSpaceClumping :
		//			float averageClumping = getSubSpaceConcentration(numBins, 50)/(float)500;
		//			ValuePair clumping = new ValuePair(averageClumping, averageClumping);
		//			return clumping;
		//
		//			//		case MutualInformation:
		//			//			return getMutualInfo(numBins);

		default:
			return new ValuePair(0, 0);
		}
	}	

	/** Returns all values in the range [0..1], even Parallelism */
	public float getNormalizedMetric(Metrics metric, int numBins) {
		switch (metric) {
		case JointEntropy:
			float je = getJointEntropy(numBins)/10;


			//			System.out.println("Crossings: "+n_norm);
			return je;

		case DistanceEntropy:
			float de = 1-computeDistanceEntropy(numBins)/10;
			//	System.err.println("Normalized de  " +de);



			//			System.out.println("Crossings: "+n_norm);
			return de;
			//
			//		case Overplotting:
			//			return getDegreeOfOverPlotting(numBins);
			//
			//		case AngleOfCrossings:
			//			return getAngleOfCrossingMedian(numBins);
			//
			////		case Parallelism:
			////		//	n = getParallelism(numBins);
			////			n_norm = new ValuePair(Math.abs(n.getValue()), Math.abs(n.getInvertedValue()));
			////			return n_norm;
			//
			//		case Convergence_Divergence:
			//			return getConvergence_Divergence(numBins, 30);

			//		case MutualInformation:
			//			ValuePair n = getMutualInfo(numBins);
			//			return new ValuePair(n.getValue()/10, n.getInvertedValue()/10);

		default:
			return 0;
		}
	}

	public int[][] get2DHistogram(int numBins) {

		float maxVal1 = model.getMaxValue(dimension1);
		float minVal1 = model.getMinValue(dimension1);
		float maxVal2 = model.getMaxValue(dimension2);
		float minVal2 = model.getMinValue(dimension2);

		float range1 = (maxVal1 - minVal1);
		float range2 = (maxVal2 - minVal2);
		int[][] hist = new int[numBins+1][numBins+1];


		for (float row[] : model.getValues()) {

			float val1 = row[dimension1];
			float val2 = row[dimension2];
			val1 = val1 - minVal1;
			val2 = val2 - minVal2;
			int bin1 = (int) (numBins * (val1 / range1));
			int bin2 = (int) (numBins * (val2 / range2));
			hist[bin1][bin2]++;
		}


		return hist;
	}

	public int[] getDegree(int numBins, boolean inDegree) {

		int j = 0;
		int[] degree = new int[numBins+1];

		int[][] hist2D = get2DHistogram(numBins);

		//	System.err.println("hist2d  " );

		//		HashMap<Integer, List<Integer>> convMap= new HashMap<Integer,List<Integer>>();
		//		HashMap<Integer, List<Integer>> divMap = new  HashMap<Integer,List<Integer>>();

		for (int i = 0; i < hist2D.length; i++) {


			for (j = 0; j < hist2D.length; j++) {

				// int countNumLines= hist2D[i][j];
				if (hist2D[i][j] > 0)

					if (inDegree) {
						// Degree[j]=Degree[j]+countNumLines;
						degree[j]++;
						//List<Integer> listVal = convMap.get(j);

						//if (listVal == null)
						//	convMap.put(j, listVal = new ArrayList<Integer>());

						//listVal.add(i);
					} else {
						// Degree[i]=Degree[i]+countNumLines;;
						degree[i]++;
						//List<Integer> listVal = divMap.get(j);

						//if (listVal == null)
						//	divMap.put(i,
						//		listVal = new ArrayList<Integer>());
						//listVal.add(j);
					}
			}
		}

		return degree;
	}

	public int[] getDistanceHistogramScatter(int numBins, boolean inverted)  {

		if (dimension1 > dimension2)
			return model.getAxisPair(dimension2, dimension1, mainDisplay).getDistanceHistogramScatter(numBins, inverted);
		else {
			if (distanceHistogram == null)
				calculateHistogramsScatter(numBins);

			//			if (inverted)
			//				return distanceHistogram_inverted;
			//			else
			return distanceHistogramScatter;
			//			initBinning(numBins);
			//			int[] hist = new int[2 * numBins + 1];
			//			
			//			for (float[] row : model.getValues()) 
			//			{
			//				int bin1 = value2pixel(row[dimension1], true, false, numBins);
			//				int bin2 = value2pixel(row[dimension2], false, inverted, numBins);
			//				int distance = bin2-bin1;
			//	
			//				hist[distance + numBins]++;
			//			}
			//			return hist;
		}
	}

	public int[] getDistanceHistogram(int numBins, boolean inverted)  {

		if (dimension1 > dimension2)
			return model.getAxisPair(dimension2, dimension1, mainDisplay).getDistanceHistogram(numBins, inverted);
		else {
			if (distanceHistogram == null)
				calculateHistograms(numBins);

			//			if (inverted)
			//				return distanceHistogram_inverted;
			//			else
			return distanceHistogram;
			//			initBinning(numBins);
			//			int[] hist = new int[2 * numBins + 1];
			//			
			//			for (float[] row : model.getValues()) 
			//			{
			//				int bin1 = value2pixel(row[dimension1], true, false, numBins);
			//				int bin2 = value2pixel(row[dimension2], false, inverted, numBins);
			//				int distance = bin2-bin1;
			//	
			//				hist[distance + numBins]++;
			//			}
			//			return hist;
		}
	}

	public float getSubSpaceConcentration(int numBins, int minThreshold){

		initBinning(numBins);
		startBinNeighborMap = new HashMap<Point2D, ArrayList<Integer>>();
		// if convergence is true choose the 2nd axis else choose the 1st axis.

		// Point containing axis and bin information, axis = 1 means 1st axis and 2 means 2nd axis.
		Point2D.Float chosenAxisAndStartBin = new Point2D.Float();
		int chosenAxis = 0;
		ArrayList<Integer> neighBorsList = null;
		ArrayList<Integer> probableNeighborsList = new ArrayList<Integer>(); 
		//model.getAxisPair(axis1, axis2, parallelDisplay).initGlobalBinning(numBins);

		//data.getAxisPair(axis1, axis2, this).initBinning(numBins);

		System.err.println(" Test numBins  " +numBins);
		int degree[] = new int[numBins+1];
		//		boolean convergence = model.getAxisPair(axis1, axis2, parallelDisplay).getConvergence_Divergence(numBins, 3);
		boolean convergence = getConvergence_Divergence(numBins, 3);
		if(convergence){
			chosenAxis = 2;
			degree = getDegree(numBins, true);
		}
		else
		{
			chosenAxis = 1;
			degree = getDegree(numBins, false);

		}

		int numNeighbors =0;
		int numSparse =0;
		//int minThreshold = 30;
		float startBin = 0;
		int currentBin=0;
		//System.err.println("Here +++++++++" +degree.length);
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
					System.err.println("Start bin  " + startBin + "Num neighbors " +neighBorsList.size());

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

		System.err.println(" Size in clumping method +++++++++  " + startBinNeighborMap.keySet().size());

		float sumClumping =0;

		Iterator e = startBinNeighborMap.keySet().iterator();

		while(e.hasNext()){

			Point2D.Float key = (Point2D.Float)e.next();
			System.err.println("Chosen axis " + key.x);

			System.err.println("Number of neighbors  " + startBinNeighborMap.get(key).size());

			sumClumping = sumClumping + startBinNeighborMap.get(key).size();

		}

		float avgClumping = sumClumping/(float)startBinNeighborMap.keySet().size();

		//System.err.println(" Averagee clumping " + avgClumping);

		return avgClumping;

	}

	public float getPearsonCorrelation(){

		double dimension1Data[] = new double[model.getNumRecords()];
		double dimension2Data[] = new double[model.getNumRecords()];
		float corr =0;
		model.getValues();

		for(int record=0; record<model.getNumRecords(); record++){
			{

				dimension1Data[record] = model.getValue(record, dimension1);
				dimension2Data[record] = model.getValue(record, dimension2);


			}

			//	DataSpaceMetrics metric = new DataSpaceMetrics();


			//	corr = (float)metric.getPearsonCorrelation(dimension1Data, dimension2Data);



		}
		return corr;
	}

	public HashMap<Point2D, ArrayList<Integer>> getClumpingMap(int numBins, int minThreshold){

		getSubSpaceConcentration(numBins, minThreshold);
		return startBinNeighborMap;
	}

	public void setPixelDataMap(HashMap<Integer, Integer> map){

		pixelDataMap = map;
	}

	public HashMap<Integer, Integer> getPixelDataMap(int numBins){

		return pixelDataMap;
	}

	public float computeDistanceEntropy(int numBins){


		//int numBins = (int)param.y;
		int[] distanceHistogram = getDistanceHistogram(numBins, false);

		double probabilityValue = 0;
		double logProbabilityValue = 0;
		double entropy = 0;
		float sumEntropy = 0;
		for (int i = 0; i < distanceHistogram.length; i++) {
			probabilityValue = distanceHistogram[i] /(float)distanceHistogram.length;
			if (probabilityValue > 0)
				logProbabilityValue = (Math.log(1/probabilityValue))/ LOG_BASE_2;
			entropy = (probabilityValue * logProbabilityValue);
			sumEntropy =(float)( sumEntropy + entropy);
		}


		System.err.println("Distance entropy  ++++++++++++++ " +sumEntropy);
		return sumEntropy;



	}




}
