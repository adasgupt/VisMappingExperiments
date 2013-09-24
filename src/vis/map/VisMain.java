package vis.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vis.map.datamodel.STFFile;
import vis.map.gui.MainDisplay;
import vis.map.gui.MappingSelectionPanel;
import vis.map.gui.RankedViewPanel;
import vis.map.gui.VisualTasks;

public class VisMain {
	
	public static void main(String args[]){
		
//read Data
		STFFile file = new STFFile("src/data/cars.stf");
		try {
			file.readContents();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//set up displays
		
		JFrame mainFrame = new JFrame("Mapping Panel");
		MainDisplay display = new MainDisplay();
		display.setBackground(Color.white);
		display.initialize(file);
		mainFrame.setLayout(new BorderLayout());

   
		
		MappingSelectionPanel mappingPanel = new MappingSelectionPanel();
		mappingPanel.setPreferredSize(new Dimension(200,800));
		mappingPanel.setParameters(file, display);
		mappingPanel.createPanel();
		
		VisualTasks tasks = new VisualTasks(file);
		RankedViewPanel metaViewPanel = new RankedViewPanel(file, tasks);
		metaViewPanel.setSize(new Dimension(800,100));
		tasks.initDisplay(metaViewPanel);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
//		
    mainFrame.add(centerPanel, BorderLayout.CENTER);
		mainFrame.add(mappingPanel, BorderLayout.WEST);
		
		 centerPanel.add(display);
		 centerPanel.add(metaViewPanel);


		
//		DataReaderTODO reader = new DataReaderTODO();
//		try {
//			reader.readData();
//		} catch (IOException exception) {
//			// TODO Auto-generated catch-block stub.
//			exception.printStackTrace();
//		}
//		
//		DataSet data = reader.getData();
//		
//		for(int i=0; i<data.getNumDimensions(); i++)
//		{
//			
//			
//			System.err.println("Label " +data.getAxisLabel(i));
//		}
		
		
		
	
		mainFrame.setSize(new Dimension(800,800));
		mainFrame.setVisible(true);
		
		
	}

}
