package vis.map.gui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vis.map.datamodel.DataSet;

/**
 * TODO Put here a description of what this class does.
 *
 * @author ADG.
 *         Created May 11, 2013.
 */
public class MappingSelectionPanel extends JPanel{
	
	String comboBoxItems[];
	MainDisplay display;
	DataSet data;
	private JComboBox[] comboBox;
	private JLabel[] visualVariable;
	
	String visualVariableName[]={"position1", "position2", "size", "color"};
	private static final int NUMVISUALVRAIABLES = 4;
	
	public enum VisVariables{
		
		position1,
		position2,
		size,
		color
		
		}
	
	public void setParameters(DataSet data, MainDisplay display){
		
		this.data = data;
		this.display =display;
		
	}
	
	public void createPanel(){
		
		Box enclosingBox = Box.createVerticalBox();
		Box[] comBoxArray = new Box[NUMVISUALVRAIABLES];
		visualVariable = new JLabel[NUMVISUALVRAIABLES];
		comboBox = new JComboBox[NUMVISUALVRAIABLES];
		
		
		for(int numComboBox=0; numComboBox<NUMVISUALVRAIABLES; numComboBox++)
		{
			
			comBoxArray[numComboBox] = Box.createHorizontalBox();
			comBoxArray[numComboBox].setPreferredSize(new Dimension(200,30));
			
			visualVariable[numComboBox] = new JLabel(visualVariableName[numComboBox]);
			visualVariable[numComboBox].setPreferredSize(new Dimension(60,20));
			
			comBoxArray[numComboBox].add(visualVariable[numComboBox]);
			
			comboBox[numComboBox] = new JComboBox(new MyComboBoxModel(getComboBoxItems()));
			comboBox[numComboBox].setPreferredSize(new Dimension(90, 200));
			comboBox[numComboBox].setEditable(true);
			comboBox[numComboBox].setEnabled(true);
			
			
			comboBox[numComboBox].addItemListener(new ComboBoxItemListener(numComboBox));
			comboBox[numComboBox].setPreferredSize(new Dimension(50,30));
			
			comBoxArray[numComboBox].add(comboBox[numComboBox]);
			
			enclosingBox.add(comBoxArray[numComboBox]);
			
			
		}
		
		
		
		this.add(enclosingBox);
		
		
		
	}
	
	
	public ArrayList<String> getComboBoxItems() {

		
		
		int prevDimensions= display.getNumAxes();
		comboBoxItems= new String[prevDimensions];
		System.err.println("Length"  + comboBoxItems.length);
		ArrayList<String> comboBoxItemsList = new ArrayList<String>();


		for(int j=0;j< display.getNumAxes();j++){


			String dim1Label= display.getAxisLabel(j);
			//String dim2Label= display.getAxisLabel(j+1);
			String s = ""+ dim1Label;
			comboBoxItems[j] = s;
			comboBoxItemsList.add(comboBoxItems[j]);
			System.err.println("Dim  "  + display.axes[j].dimension);
		}
		
		List<String> list=Arrays.asList(comboBoxItems);
		comboBoxItemsList.addAll(list);
		
		return comboBoxItemsList;
	

	}
	
	private class ComboBoxItemListener implements ItemListener{
		
		int boxID;
		public ComboBoxItemListener(int boxID){
			
			this.boxID = boxID;
			
		}

		public void itemStateChanged(ItemEvent evt) {

			String item= evt.getItem().toString();
			int index=0;
			for(int i=0;i< display.axes.length-1;i++){
				if(comboBoxItems[i].equals(item))
					index=i;	
			}
			
			if (evt.getStateChange() == ItemEvent.SELECTED) {

		  	setMapping(index, boxID);
				

			}
		}

		
	}
	
	
	public void setMapping(int dataDimension, int visualVariable){
		
		String visualvariableName = " ";
		switch(visualVariable){
	
	     case 0: visualvariableName = "position1";
	    	 display.setMapping(dataDimension, visualvariableName);
		
	     case 1: visualvariableName = "position2";
    	 display.setMapping(dataDimension, visualvariableName);
    	 
	     case 2: visualvariableName = "size";
    	 display.setMapping(dataDimension, visualvariableName);
    	 
	     case 3: visualvariableName = "color";
    	 display.setMapping(dataDimension, visualvariableName);
		
		}
		
		//display.repaint();
		
	}

	
	private class MyComboBoxModel extends AbstractListModel implements ComboBoxModel {

		String selection = null;
		private Vector<String> displayedObjects = new Vector<String>();

		public MyComboBoxModel(ArrayList<String> items){

			for(int i=0;i<items.size();i++)
			{      

				this.selection=items.get(i);
				displayedObjects.add(items.get(i));
				//   System.err.println("Labels "  + displayedObjects.get(i));
			}
			this.displayedObjects.removeAll(items);
			this.displayedObjects.addAll(items);

			this.fireContentsChanged(displayedObjects,0,displayedObjects.size()-1);
		}

		public Object getElementAt(int index) {
			return comboBoxItems[index];
		}

		public int getSize() {
			return comboBoxItems.length;
		}

		public void setSelectedItem(Object anItem) {
			selection = (String) anItem; // to select and register an
		} // item from the pull-down list

		// Methods implemented from the interface ComboBoxModel
		public Object getSelectedItem() {
			return selection; // to add the selection to the combo box
		}
	}	

}
