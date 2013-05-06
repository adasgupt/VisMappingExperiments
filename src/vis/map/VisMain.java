package vis.map;

import java.io.IOException;

import vis.map.datamodel.DataReader;
import vis.map.datamodel.DataSet;

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
		
		
	}

}
