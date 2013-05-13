package vis.map.gui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
	private JComboBox comboBox;
	private JLabel[] visualVariableName;
	
	String visualVariable[]={"position", "position", "size", "color"};
	private static final int NUMVISUALVRAIABLES = 4;
	
	public void setParameters(DataSet data, MainDisplay display){
		
		this.data = data;
		this.display =display;
		
	}
	
	public void createPanel(){
		
		Box enclosingBox = Box.createVerticalBox();
		Box[] comBoxArray = new Box[NUMVISUALVRAIABLES];
		visualVariableName = new JLabel[NUMVISUALVRAIABLES];
		
		
		for(int numComboBox=0; numComboBox<NUMVISUALVRAIABLES; numComboBox++)
		{
			
			comBoxArray[numComboBox] = Box.createHorizontalBox();
			visualVariableName[numComboBox] = new JLabel(visualVariable[numComboBox]);
			comBoxArray[numComboBox].add(visualVariableName[numComboBox]);
			setComboBoxItems(0);
			comBoxArray[numComboBox].add(comboBox);
			enclosingBox.add(comBoxArray[numComboBox]);
			
		}
		comboBox.addItemListener(new ComboBoxItemListener());
		comboBox.setPreferredSize(new Dimension(50,30));
		this.add(enclosingBox);
		
		
		
	}
	
	
	public void setComboBoxItems(int flag) {

		
		
		int prevDimensions= display.getNumAxes();
		comboBoxItems= new String[prevDimensions];
		System.err.println("Length"  + comboBoxItems.length);
		Vector<String> v=new Vector<String>();

		System.err.println(flag);


		for(int j=0;j< display.getNumAxes();j++){


			String dim1Label= display.getAxisLabel(j);
			//String dim2Label= display.getAxisLabel(j+1);
			String s = ""+ dim1Label;
			comboBoxItems[j] = s;
			v.add(comboBoxItems[j]);
			System.err.println("Dim  "  + display.axes[j].dimension);
		}
		List<String> list=Arrays.asList(comboBoxItems);
		v.addAll(list);
		comboBox = new JComboBox(new MyComboBoxModel(v));
		comboBox.setEditable(true);
		comboBox.setEnabled(true);

	}
	
	private class ComboBoxItemListener implements ItemListener{

		public void itemStateChanged(ItemEvent evt) {

			String item= evt.getItem().toString();
			int index=0;
			for(int i=0;i< display.axes.length-1;i++){
				if(comboBoxItems[i].equals(item))
					index=i;	
			}
			if (evt.getStateChange() == ItemEvent.SELECTED) {

		//		setBrushIndex(index);
				//commented
				//	parallelDisplay.setActiveRegion(brushIndex, brushIndex+1);

			}
		}
	}
	
	private class MyComboBoxModel extends AbstractListModel implements ComboBoxModel {

		String selection = null;
		private Vector<String> displayedObjects = new Vector<String>();

		public MyComboBoxModel(Vector<String> items){

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
