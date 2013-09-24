package vis.map.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import vis.map.datamodel.DataSet;
import vis.map.metrics.AxisPair.Metrics;
import vis.map.metrics.AxisQuartetMetrics;

public class RankedViewPanel extends MainDisplay {

	DataSet data;
	//MainDisplay display;
	VisualTasks vt;

	int padding = 10;
	int startX = 5;
	private int labelHeight;

	public RankedViewPanel(DataSet data, VisualTasks vt){

		this.data = data;
		this.vt = vt;
	}


	public void paint(Graphics g){

		super.paint(g);
		Graphics2D g2= (Graphics2D)g;


		//	List<List<AxisQuartetMetrics>> axisQuartetsByTaskList = new ArrayList<List<AxisQuartetMetrics>>();



		// draw the axis pairs on screen column by column
		drawRankedAxisQuartets( g2, data,startX);




	}

	private void drawRankedAxisQuartets(Graphics g2d, DataSet data, int startX)
	{

		//Number of metrics= screenCol
		int screenCol = VisualTasks.Tasks.values().length;

		//Number of axispairs= screenRow
		int screenRow = VisualTasks.NUMRANKED;

		labelHeight= 20;
		//g2d.setColor(Color.black);

		int axisPairBoxWidth  =  getWidth()/screenCol-padding;
		int axisPairBoxHeight =  (getHeight()-(labelHeight*(screenRow+1)))/(screenRow)-padding;

		System.err.println("width " +axisPairBoxWidth + " height " +axisPairBoxHeight);
		for( int i=0; i<screenCol; i++ )
		{

			List<AxisQuartetMetrics> listPerTask = vt.computeRankingForAxisQuartets(data, VisualTasks.Tasks.values()[i]);
			//draw labels for the metrics

			//			if( ascendingSort == false) {
			//			screenRow = metricsByAxisPairsList.get(i).size();
			//			if (screenRow > 10)
			//			screenRow = 10;
			//			}

			g2d.setColor( Color.GRAY );
			g2d.fillRect( startX+i*(axisPairBoxWidth+padding),0,axisPairBoxWidth, labelHeight );
			g2d.setColor( Color.white );
			g2d.drawString( VisualTasks.Tasks.values()[i].toString(), startX+(i*(axisPairBoxWidth+padding))+20, labelHeight/2 );

			System.err.println(" Metric  "  + VisualTasks.Tasks.values()[i].toString());


			for( int j=0; j<screenRow; j++ )
			{

				int axis1= listPerTask.get(j).getDimension1();
				int axis2= listPerTask.get(j).getDimension2();
				int axis3 = listPerTask.get(j).getSizeDimension();


				int locX = startX+(axisPairBoxWidth*(i))+(padding*i);
				int locY = labelHeight+axisPairBoxHeight*j+(padding*j);

				//		System.err.println("    "  + data.getAxisLabel(axis1) +"   " + data.getAxisLabel(axis2) + "  " + val   );

				drawScatterplot( g2d, data, axis1,axis2, axis3, axisPairBoxHeight, axisPairBoxWidth,locX,locY);
				drawLabel(g2d, axis1, axis2, axis3, locX, locY+axisPairBoxHeight);
			}
		}

	}

	private void drawLabel(Graphics g, int axis1, int axis2, int axis3, int locX, int locY){


		String text1=  "  Axis1   "  + data.getAxisLabel(axis1)  +  "   Axis2  " + data.getAxisLabel(axis2) +  "   Axis3 " + data.getAxisLabel(axis3)+" " ;
		FontMetrics fm=g.getFontMetrics();
		int strWidth=fm.stringWidth(text1);
		int ascent=fm.getMaxAscent();
		int descent=fm.getMaxDescent();
		
		int rectWidth  = 400;
		int rectX =  1;
		int rectY =  1;
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(locX+rectX, locY+rectY, rectWidth, labelHeight);
		g.setColor(Color.WHITE);
		int str_y= locY+ labelHeight/2-descent/2+ascent/2;
		int str_x= locX+ rectWidth/2-strWidth/2;
		g.drawString(text1, rectX+str_x, rectY+str_y);
	}

	private void drawScatterplot(Graphics g,DataSet data, int axis1, int axis2, int axis3,int axisPairBoxHeight, int axisPairBoxWidth,int locX,int locY){



		//		BufferedImage bufferImg = new BufferedImage(this.getWidth(), this.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
		//		bufferImg = (BufferedImage)(this.createImage(w, h));



		//setting up the BufferedImage properties
		//		ig = bufferImg.createGraphics();
		//		ig.setColor(this.getBackground());
		//		ig.fillRect(0, 0, this.getWidth(), this.getHeight());

		float scale1 = (data.getMaxValue(axis1) - data.getMinValue(axis1));
		float scale2 = (data.getMaxValue(axis2) - data.getMinValue(axis2));
		float scale3 = (data.getMaxValue(axis3) - data.getMinValue(axis3));
		//		float scale4 = (data.getMaxValue(axis4) - data.getMinValue(axis4));


		float axisOffset1 = data.getMinValue(axis1);
		float axisOffset2 = data.getMinValue(axis2);
		float axisOffset3 = data.getMinValue(axis3);
		//	float axisOffset4 = data.getMinValue(axis4);


		g.setColor(new Color(128,128,128));
		g.drawLine(locX, locY, locX, locY+axisPairBoxHeight);
		g.drawLine(locX, locY+axisPairBoxHeight, locX+axisPairBoxWidth, locY+axisPairBoxHeight);


		System.err.println("Inside scatter");

		/*
		 * the loop for rendering all the points
		 */
		for(float[]dataRow : data){

			int v1 = (int)((dataRow[axis1] - axisOffset1) * (axisPairBoxWidth-padding-5) / scale1);
			int v2 = (int)((dataRow[axis2] - axisOffset2) * (axisPairBoxHeight-padding-labelHeight)/scale2);
			int v3 = (int)((dataRow[axis3] - axisOffset3)*(axisPairBoxHeight-padding) /scale3);
			//		int v4 = (int)((dataRow[axis4] - axisOffset4) * (axisPairBoxHeight-padding) / scale4);
			//			if(useColor)
			//				ig.setColor(getRecordColor(v1,v2, (int)param.y));
			//			else
			g.setColor(new Color(220,128,128, 80));

		//	g.drawLine((int)(locX+v1), (int)(locY+axisPairBoxHeight-v2), (int)(locX+v1)+2,(int)(locY+axisPairBoxHeight-v2)+2);	
			//			if(axis3!=-1)
			float radius = (float)(Math.sqrt((v3/Math.PI)));
			//
			//			if(axis4!=-1)
			//			{
			//				//System.err.println("setting color");
			//				ig.setColor(getRecordColor(v4, (int)param.y-PADDING));
			//
			//			}
			//			//	System.err.println("radius " +radius);
			//
			//			//ig.drawOval((int)(v1), (int)(param.y-v2), radius, radius);
			g.fillOval((int)(locX+padding+v1), (int)(locY+axisPairBoxHeight-padding-v2), (int)radius, (int)radius);
		}




		//setUseColor(true);




	}


}



