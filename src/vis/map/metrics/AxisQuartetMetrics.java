package vis.map.metrics;

import java.awt.image.BufferedImage;

/**
 * TODO Put here a description of what this class does.
 *
 * @author ADG.
 *         Created May 11, 2013.
 */
public class AxisQuartetMetrics implements Comparable, Cloneable{
	
	
        private int axis1;
		private int axis2;
		private int sizeDimension;
		private int colorDimension;
		private float jointEntropy;
		private float grayEntropy;
		private float colorEntropy;
		private float distanceEntropy;
		private float weightedGrayEntropy;
		private float weightedColorEntropy;
		private float klDiv;

		private BufferedImage img;
		private float sizeEntropy;
		private float degreeOfClustering;

		public AxisQuartetMetrics(int dim1, int dim2){
			axis1 = dim1;
			axis2 = dim2;

		}
		public void setAxes(int dim1, int dim2){
			axis1 = dim1;
			axis2 = dim2;

		}
		public int getDimension1(){
			return axis1;
		}
		public int getDimension2(){
			return axis2;
		}
		public void setJointEntropy(float je){
			jointEntropy = je;

		}
		public void setGrayEntropy(float pe){
			grayEntropy = pe;

		}
		public void setColorEntropy(float pe){
			colorEntropy = pe;

		}
		public void setDistanceEntropy(float de){
			distanceEntropy =de;

		}
		public void setWeightedGrayEntropy(float de){
			weightedGrayEntropy =de;

		}
		public void setWeightedColorEntropy(float de){
			weightedColorEntropy =de;

		}
		public float getJointEntropy(){
			return jointEntropy;
		}

		public float getGrayEntropy(){
			//System.err.println(" Gray entropy   " +grayEntropy);
			return grayEntropy;
		}
		public float getColorEntropy(){
			return colorEntropy;
		}


		public float getDistanceEntropy(){
			return distanceEntropy;
		}

		public float getWeightedGrayEntropy(){
			return weightedGrayEntropy;
		}

		public float getWeightedColorEntropy(){
			return weightedColorEntropy;
		}



		public void setKLDivergence(float kld){
			klDiv = kld;
		}

		public float getKLDivergence(){
			return klDiv;
		}


		public void storeImage(BufferedImage bufferImg){

			img = bufferImg;
		}
		public BufferedImage getImage(){

			return img;

		}

		public void setSizeDimension(int dim){
			
			sizeDimension =dim;
		}
		
		public int getSizeDimension(){
			return sizeDimension;
		}

		public void setColorDimension(int dim){
			
			colorDimension = dim;
		}
		
		public int getColorDimension(){
			return colorDimension;
		}
		@Override
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub.
			return 0;
		}
		public void setSizeEntropy(float sizeEntropy) {
			this.sizeEntropy = sizeEntropy;
			
		}
		
		public float getSizeEntropy() {
			return sizeEntropy;
			
		}
		public void setClusterCoefficient(float degreeOfClustering) {
			this.degreeOfClustering = degreeOfClustering;
			
		}
		
		public float getClusterCoefficient() {
			return degreeOfClustering;
			
		}

	}



