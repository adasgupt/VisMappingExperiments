package vis.map.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import vis.map.datamodel.DataSet;
import vis.map.metrics.AxisQuartetMetrics;

public class VisualTasks {

	DataSet data;
	ArrayList<AxisQuartetMetrics> axisQuartetMetricsList = new ArrayList<AxisQuartetMetrics>();
	RankedViewPanel display;
	int numBins;
	public static final int NUMRANKED = 4;
	public enum MetaMetrics{

		JointEntropy, SizeEntropy, ClusteringCoefficient, ImageEntropy, SumofJointImageEntropy, GrayEntropy, ColorEntropy, DistanceEntropy, KLDivergence, InformationLoss, Color

	}

	public enum Tasks{
		Correlation, Clustering
	}
	public void initDisplay(RankedViewPanel display){
		this.display =display;
		numBins = display.getHeight();
	}
	public VisualTasks(DataSet  data){

		this.data = data;
		//this.display =display;


		//initialize axis quartets

		for(int dim1=1; dim1<data.getNumDimensions(); dim1++)
		{
			for(int dim2=0; dim2<dim1; dim2++)
			{

				AxisQuartetMetrics pairMetricsObject = new AxisQuartetMetrics(dim1, dim2);
				axisQuartetMetricsList.add(pairMetricsObject);
			}
		}

	}


	public ArrayList<AxisQuartetMetrics> rankPositionsByCorrelation(){


		for(int dim1=1; dim1<data.getNumDimensions(); dim1++)
		{
			for(int dim2=0; dim2<dim1; dim2++)
			{
				data.getAxisPair(dim1, dim2, display).initBinning(numBins);
				float parallelism = data.getAxisPair(dim1, dim2, display).computeDistanceEntropy(numBins);

				for(AxisQuartetMetrics metricObject: axisQuartetMetricsList)
				{

					if(metricObject.getDimension1() == dim1 && metricObject.getDimension2() == dim2)
					{


						metricObject.setDistanceEntropy(parallelism);
					}
				}

			}
		}
		Collections.sort(axisQuartetMetricsList, new SortMetrics(MetaMetrics.DistanceEntropy));
		return axisQuartetMetricsList;
	}


	public ArrayList<AxisQuartetMetrics> rankPositionsByClustering(){


		for(int dim1=1; dim1<data.getNumDimensions(); dim1++)
		{
			for(int dim2=0; dim2<dim1; dim2++)
			{

				float degreeOfClustering = data.getAxisPair(dim1, dim2, display).getSubSpaceConcentration(numBins, 4);

				for(AxisQuartetMetrics metricObject: axisQuartetMetricsList)
				{

					if(metricObject.getDimension1() == dim1 && metricObject.getDimension2() == dim2)
					{
						metricObject.setJointEntropy(degreeOfClustering);
					}
				}

			}
		}
		Collections.sort(axisQuartetMetricsList, new SortMetrics(MetaMetrics.JointEntropy));
		return axisQuartetMetricsList;
	}


	public List<AxisQuartetMetrics> rankSizebyCorrelation(List<AxisQuartetMetrics> rankedCorrelatedPositionsList)
	{

		HashMap<Integer, Float> dimensionSizeMap = new HashMap<Integer, Float>();
		ArrayList<Float> sizeList = new ArrayList<Float>();
		ArrayList<Integer> rankedSizeDimensionsList = new ArrayList<Integer>();

		for(int sizeDim =0; sizeDim<data.getNumDimensions(); sizeDim++)
		{
			//axis pair has no role here, so hard-coding
			data.getAxisPair(0, 1, display).initBinning(numBins);
			float sizeEntropy = data.getAxisPair(0, 1, display).getSizeEntropy(numBins, sizeDim);
			dimensionSizeMap.put(sizeDim, sizeEntropy);

		}

		// sort the list by size
		for(float size: dimensionSizeMap.values())
			sizeList.add(size);
		Collections.sort(sizeList);

		//get the data dimension corresponding to the size
	for(int numSizeValues= 0; numSizeValues<sizeList.size(); numSizeValues++)
	//	for(int numSizeValues= sizeList.size()-1; numSizeValues>sizeList.size()-6; numSizeValues--)
		{
			float rankedSize = sizeList.get(numSizeValues);

			for(int sizeDimension: dimensionSizeMap.keySet())
			{
				if(dimensionSizeMap.get(sizeDimension) == rankedSize)
				{

					rankedSizeDimensionsList.add(sizeDimension);

				}

			}


		}


		for(int numAqm=0; numAqm<rankedCorrelatedPositionsList.size(); numAqm ++)
		{

			AxisQuartetMetrics aqm = rankedCorrelatedPositionsList.get(numAqm);
			aqm.setSizeDimension(rankedSizeDimensionsList.get(numAqm));
			aqm.setSizeEntropy(sizeList.get(numAqm));

		}


		return rankedCorrelatedPositionsList;


	}

	public int rankColorByCorrelation(ArrayList<AxisQuartetMetrics> rankedCorrelatedPositionsList){

		return 3;
	}


	public static class SortMetrics implements Comparator<AxisQuartetMetrics>{

		private String metricName;
		/*
		 * left part of matrix denoted by 0 and the right part as 1
		 */
		private int sortMode;
		private MetaMetrics metric;


		public SortMetrics(MetaMetrics metric){

			this.metric = metric;

		}
		@Override
		public int compare(AxisQuartetMetrics m1, AxisQuartetMetrics m2) {

			float entropy1 = 0;
			float entropy2 = 0;

			if(metric==MetaMetrics.ColorEntropy)

			{
				//System.err.println("Metric joint entropy");
				entropy1 = m1.getColorEntropy();
				entropy2 = m2.getColorEntropy();	

			}

			if(metric==MetaMetrics.JointEntropy)

			{
				//System.err.println("Metric joint entropy");
				entropy1 = 1-(m1.getJointEntropy()/10);
				entropy2 = 1-(m2.getJointEntropy()/10);	

			}
			else if(metric == MetaMetrics.ImageEntropy)
			{

				//System.err.println("Metric distance entropy");
				entropy1 = m1.getWeightedColorEntropy();
				entropy2 = m2.getWeightedColorEntropy();

			}

			else if(metric == MetaMetrics.DistanceEntropy)
			{

				//System.err.println("Metric distance entropy");
				entropy1 = m1.getDistanceEntropy();
				entropy2 = m2.getDistanceEntropy();

			}

			//for sorting in response to user selection
			else if(metric == MetaMetrics.SumofJointImageEntropy)
			{

				//System.err.println("Metric distance entropy");
				entropy1 = m1.getWeightedColorEntropy()+ m1.getJointEntropy();
				entropy2 = m2.getWeightedColorEntropy()+ m2.getJointEntropy();;

			}

			else if(metric == MetaMetrics.KLDivergence)
			{

				//System.err.println("Metric distance entropy");
				entropy1 = 1-(m1.getKLDivergence()/1000);
				entropy2 = 1-(m2.getKLDivergence()/1000);

			}
			else if(metric == MetaMetrics.InformationLoss)
			{

				//System.err.println("Metric distance entropy");
				entropy1 = (1-m1.getKLDivergence()/100)+(1-m1.getJointEntropy()/10);
				entropy2 = (1-m2.getKLDivergence()/100)+(1-m2.getJointEntropy()/10);

			}
			else if(metric == MetaMetrics.Color)
			{

				//System.err.println("Metric distance entropy");
				entropy1 = m1.getDistanceEntropy();
				entropy2 = m2.getDistanceEntropy();

			}

			else if(metric == MetaMetrics.SizeEntropy)
			{

				//System.err.println("Metric distance entropy");
				entropy1 = m1.getSizeEntropy();
				entropy2 = m2.getSizeEntropy();

			}

			//	System.err.println("Compared " + entropy1 + " "+ entropy2);

			if(entropy1 < entropy2)
				return 1;
			else if(entropy1 > entropy2)
				return -1;
			else
				return 0;

		}
	}

	public List<AxisQuartetMetrics> computeRankingForAxisQuartets(DataSet data, Tasks task) {

		List<AxisQuartetMetrics> rankedQuartetsList = new ArrayList<AxisQuartetMetrics>();

		
		if(task.equals(Tasks.Correlation))
		{
		ArrayList<AxisQuartetMetrics> rankedPositionsList = rankPositionsByCorrelation();
		System.err.println("case " +"Correlation");
		rankedQuartetsList = rankedPositionsList;
		rankSizebyCorrelation((List<AxisQuartetMetrics>)rankedPositionsList.subList(0, NUMRANKED));
		}

		if(task.equals(Tasks.Clustering))
		{
		 ArrayList<AxisQuartetMetrics> rankedClusterList = rankPositionsByClustering();
		System.err.println("case " +"Clustering");
		rankedQuartetsList = rankedClusterList;
		rankSizebyCorrelation((List<AxisQuartetMetrics>)rankedClusterList .subList(0, NUMRANKED));
		}

		

		return rankedQuartetsList;

	}

//	public List<AxisQuartetMetrics> computeRankingForAxisQuartets(DataSet data, Tasks task) {
//
//		List<AxisQuartetMetrics> rankedQuartetsList = new ArrayList<AxisQuartetMetrics>();
//
//		switch (task) {
//		case Correlation: ArrayList<AxisQuartetMetrics> rankedPositionsList = rankPositionsByCorrelation();
//		System.err.println("case " +"Correlation");
//		rankedQuartetsList = rankedPositionsList;
//		//rankSizebyCorrelation((List<AxisQuartetMetrics>)rankedPositionsList.subList(0, NUMRANKED));
//		break;
//
//		case Clustering: ArrayList<AxisQuartetMetrics> rankedClusterList = rankPositionsByClustering();
//		System.err.println("case " +"Clustering");
//		rankedQuartetsList = rankedClusterList;
//		//rankSizebyCorrelation((List<AxisQuartetMetrics>)rankedClusterList .subList(0, NUMRANKED));
//		break;
//
//		}
//
//		return rankedQuartetsList;
//
//	}
}
