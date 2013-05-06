package vis.map;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;

import vis.map.datamodel.DataReader;
import vis.map.datamodel.DataSet;
import vis.map.gui.MainDisplay;

public class VisMain {
	
	public static void main(String args[]){
		
		
		DataReader reader = new DataReader();
		try {
			reader.readData();
		} catch (IOException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
		}
		
		DataSet data = reader.getData();
		
		for(int i=0; i<data.getNumDimensions(); i++)
		{
			
			
			System.err.println("Label " +data.getAxisLabel(i));
		}
		
		JFrame mainframe = new JFrame("Mapping Panel");
		MainDisplay display = new MainDisplay();
		mainframe.add(display);
		display.initialize(data);
	
		mainframe.setSize(new Dimension(600,600));
		mainframe.setVisible(true);
		
		
	}

}
