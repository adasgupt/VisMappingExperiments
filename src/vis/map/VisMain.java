package vis.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;

import vis.map.datamodel.STFFile;
import vis.map.gui.MainDisplay;
import vis.map.gui.MappingSelectionPanel;

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
		display.initialize(file);
		mainFrame.setLayout(new BorderLayout());
		mainFrame.add(display, BorderLayout.CENTER);
		
		MappingSelectionPanel mappingPanel = new MappingSelectionPanel();
		mappingPanel.setPreferredSize(new Dimension(200,800));
		mappingPanel.setParameters(file, display);
		mappingPanel.createPanel();
		mainFrame.add(mappingPanel, BorderLayout.WEST);


		
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
