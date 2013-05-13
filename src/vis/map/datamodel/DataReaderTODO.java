//package vis.map.datamodel;
//
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//
//import au.com.bytecode.opencsv.CSVReader;
//
///**
// * Reads a CSV file and sets up a DataSet Object.
// *
// * @author ADG.
// *         Created May 5, 2013.
// */
//public class DataReaderTODO {
//
//	DataSet data = null;
//
//	public void readData() throws IOException{
//
//		CSVReader reader = new CSVReader(new FileReader("src/data/cars.csv"));
//		data = new DataSet();
//		String[] label = null;
//		String[] nextLine;
//		int rowNum = 0;
//		while ((nextLine = reader.readNext()) != null) {
//
//			if(rowNum == 0)
//				label = nextLine;
//			rowNum++;
//
//			// nextLine[] is an array of values from the line
//			//	System.out.println(nextLine[0] + nextLine[1] + "etc...");
//			//data.addRecord(nextLine);
//		}
//
//		data.setAxisLabels(label);
//
//	}
//
//	public DataSet getData(){
//		return data;
//	}
//
//
//}
