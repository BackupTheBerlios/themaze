/*
 * Created on 08.10.2003
 * This class is part of the concat it package.
 * 
 */

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.border.*;

//import javax.swing.filechooser.*;

import java.util.*;

/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Labyrinth extends JFrame implements ActionListener, ChangeListener {

	private static 		Labyrinth selfRef = null;
	public final int 	WINWIDTH = 900;
	public final int 	WINHEIGHT = 500;
	public String 		PATH_TO_SAVE_TO = "sets/";
	public String 		PATH_TO_IMAGES = "images/";
	public String  		FILE_TO_SAVE_TO = "labyrinth.set";
	private JTabbedPane tabbedPaneSources;
	private JMenu		FileMenu, EditMenu, HelpMenu;
	private JToolBar	LabToolbar;
	private JMenuBar 	menuBar;
	private JMenuItem 	OpenMenuIt,SaveMenuIt,SaveAsMenuIt,AboutMenuIt,HelpMenuIt;
	private JMenuItem 	ZoomInMenuIt, ZoomOutMenuIt;
	private ButtonGroup bGroup;
	private Vector		ToolbarControls;

	//for messageing and user-interaction
	private UniDialog 	universalDialog;

	File defaultPath = null; 
	File defaultSavePath = null;

////////////////KONSTRUKTOR/////////////////////
	public Labyrinth(String[] args){
		selfRef = this;
		universalDialog = new UniDialog(this);
		ImageIcon frameIcon = new ImageIcon("images/logo.gif");
		this.setIconImage(frameIcon.getImage());
		setTitle("Laburintha");

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		//just a buttongroup to use for menues...
		bGroup = new ButtonGroup();		
		ToolbarControls = new Vector();
		
		//set up Menu and Buttons
		makeMenu();
		//set up EditPanes, first one
		tabbedPaneSources = new JTabbedPane();
		tabbedPaneSources.addChangeListener(this);
		//makeTab("merge");
		makeTab("normal");
		//set up Toolbar and Buttons
		makeToolbar();		
		initialiseControls();
		this.setSize(WINWIDTH,WINHEIGHT);
		this.setVisible(true);
		
	} // end constructor



	private void makeMenu(){
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
			//first Menu (File)
			FileMenu = new JMenu("File");
			//OpenMenuIt,SaveMenuIt,SaveAsMenuIt,AboutMenuIt,HelpMenuIt
				makeMenuItem(FileMenu,OpenMenuIt,"New Editor");
				makeMenuItem(FileMenu,OpenMenuIt,"Close Editor");
				FileMenu.addSeparator();
				makeMenuItem(FileMenu,OpenMenuIt,"Open");
				makeMenuItem(FileMenu,OpenMenuIt,"Save Extract");
				makeMenuItem(FileMenu,OpenMenuIt,"Save Spoil");
				//makeMenuItem(FileMenu,OpenMenuIt,"Save Image");
				FileMenu.addSeparator();
				makeMenuItem(FileMenu,OpenMenuIt,"Quit");
			menuBar.add(FileMenu);
			//second Menu (Edit)
			EditMenu = new JMenu("Edit");
				makeMenuItem(EditMenu,ZoomInMenuIt,"Zoom in");
				makeMenuItem(EditMenu,ZoomOutMenuIt,"Zoom out");
				EditMenu.addSeparator();
				makeMenuItem(EditMenu,ZoomInMenuIt,"Extract");
				makeMenuItem(EditMenu,ZoomOutMenuIt,"Spoil");
				
			menuBar.add(EditMenu);
		
			// Help-Menu
			HelpMenu = new JMenu("Help");
		
			menuBar.add(HelpMenu);

		
		//this.getContentPane().add(LabMenu);
	} // end make menu

	private void makeMenuItem(JMenu menu,JMenuItem menuIt, String name){
		menuIt = new JMenuItem(name);
		//menuIt.setAccelerator(KeyStroke.getKeyStroke(
		//	KeyEvent.VK_SUBTRACT,ActionEvent.INVOCATION_EVENT_MASK));
		menuIt.getAccessibleContext().setAccessibleDescription(
			"Do "+name);
		menuIt.addActionListener(this);
		menu.add(menuIt);
	}

	private void addToolButton(JPanel target, String buttCommando, String buttImage){
		JButton button = new JButton(new ImageIcon("images/"+buttImage));
		ImageIcon rollImage = new ImageIcon("images/r"+buttImage);
		button.setActionCommand(buttCommando);
		button.setToolTipText(buttCommando);
		button.addActionListener(this);
		button.setRolloverEnabled(false);
		button.setRolloverIcon(rollImage);
		//if(buttCommando.compareToIgnoreCase("")==0){
			button.setBorderPainted(false);
		//}
		ToolbarControls.add(button);
		target.add(button);
	} // end make button

	private void addToolSlide(JPanel target, String buttCommando){

		JSlider newSlider;
		if(getActiveTab() != null){
			newSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, getActiveTab().getLevelOf(buttCommando));
		}
		else
		{
			newSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 2);
		}
		
		newSlider.setName(buttCommando);
		newSlider.setOrientation(JSlider.VERTICAL);
		newSlider.addChangeListener(this);
		TitledBorder tbz= new TitledBorder(new EtchedBorder());
		tbz.setTitle(buttCommando);
		newSlider.setBorder(tbz);
		
		newSlider.setMajorTickSpacing(1);
		newSlider.setSnapToTicks(true);
		newSlider.setPaintTicks(true);
		newSlider.setPaintLabels(true);
		newSlider.setIgnoreRepaint(true);
		newSlider.setPreferredSize(new Dimension (60,180));
		ToolbarControls.add(newSlider);
		target.add(newSlider);
	} // end make button

	private void addToolCheck(JPanel target, String buttCommando){
		JCheckBox theTempCheck = new JCheckBox();
		theTempCheck.setActionCommand(buttCommando);
		theTempCheck.setText(buttCommando);
		theTempCheck.addActionListener(this);
		//theTempCheck.addChangeListener(this);
		theTempCheck.setRolloverEnabled(false);
		ToolbarControls.add(theTempCheck);
		target.add(theTempCheck);
	} // end make button

	private void addToolCheckToGroup(JPanel target, String buttCommando, String group){
		JCheckBox theTempCheck = new JCheckBox();
		theTempCheck.setActionCommand(buttCommando);
		theTempCheck.setText(buttCommando);
		theTempCheck.addActionListener(this);
		//theTempCheck.addChangeListener(this);
		bGroup.add(theTempCheck);
		theTempCheck.setSelected(true);
		ToolbarControls.add(theTempCheck);
		target.add(theTempCheck);
	} // end make button



	private void makeToolbar(){
		LabToolbar = new JToolBar();
		JPanel LabToolPanel = new JPanel();

		JPanel ToolPanel = new JPanel();
		ToolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80,80,80)),"tools") );
		ToolPanel.setLayout(new GridLayout(4,2));
		
		LabToolbar.setFloatable(true);
		LabToolbar.setOrientation(JToolBar.VERTICAL);
		LabToolbar.setLayout(new FlowLayout());
		LabToolPanel.setLayout(new GridLayout(3,1));
		LabToolPanel.add(ToolPanel);
			addToolButton(ToolPanel,"Zoom in","zoom_in2.gif");
			addToolButton(ToolPanel,"Zoom out","zoom_out2.gif");
			//addToolButton(ToolPanel,"","separator.gif");
			addToolButton(ToolPanel,"Spoil","spoil.gif");
			addToolButton(ToolPanel,"Extract","edit3.gif");
			addToolButton(ToolPanel,"Refine","refine.gif");
			addToolButton(ToolPanel,"Set Start","target.gif");
			addToolButton(ToolPanel,"Set Ruler","ruler.gif");
			
		JPanel SlidePanel = new JPanel();
		SlidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80,80,80)),"bias") );
		SlidePanel.setLayout(new GridLayout(1,2));
		LabToolPanel.add(SlidePanel);
			addToolSlide(SlidePanel,"smooth:");
			addToolSlide(SlidePanel,"bright:");
			
		JPanel ConrtolPanel = new JPanel();
		ConrtolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80,80,80)),"modifier") );
		ConrtolPanel.setLayout(new GridLayout(8,1));	
		LabToolPanel.add(ConrtolPanel);		
			addToolCheck(ConrtolPanel,"extra smooth");
			addToolCheck(ConrtolPanel,"open pathes");
			addToolCheck(ConrtolPanel,"color check");
			addToolButton(ConrtolPanel,"","separator.gif");
			addToolCheckToGroup(ConrtolPanel,"select vertices","select type");
			addToolCheckToGroup(ConrtolPanel,"select polygons","select type");
			addToolCheckToGroup(ConrtolPanel,"delete pixels","select type");
			addToolButton(ConrtolPanel,"delete element","delete.gif");


		JScrollPane LabToolScroller = new JScrollPane(LabToolPanel);
		LabToolScroller.setPreferredSize(new Dimension(144,400));	
		LabToolbar.add(LabToolPanel);		
		this.getContentPane().add(LabToolbar,BorderLayout.EAST);
	} // end make toolbar


	private void makeTab(String type){
		if(type.compareToIgnoreCase("normal")==0){
			ImageIcon editIcon = new ImageIcon(PATH_TO_IMAGES+"edit2.gif");	
			tabbedPaneSources.addTab("<html><small>Edit " + (tabbedPaneSources.getComponentCount()+1)+"</small></html>",editIcon,new EditTab(),"Edit an Image to extract Labyrinth");
			getContentPane().add(tabbedPaneSources,BorderLayout.CENTER);
		}
		else
		{
			ImageIcon editIcon = new ImageIcon(PATH_TO_IMAGES+"merge.gif");	
			tabbedPaneSources.addTab("<html><small>Merge</small></html>",editIcon,new MergeTab(),"Merge two Extracts to one Labyrinth");
			getContentPane().add(tabbedPaneSources,BorderLayout.CENTER);
		} // end if type
	} // end make Tab



	/*
	 *  Returns which tab is active :-), returns MergeTabs only
	 */
	public MergeTab getActiveMerge(){
		if(this.tabbedPaneSources.getSelectedComponent().getClass().equals(new MergeTab().getClass())   ){
			return (MergeTab)this.tabbedPaneSources.getSelectedComponent();
		}
		return null;
	} // end getActiveTab
	/*
	 *  Returns which tab is active :-), returns EditTabs only
	 */
	public EditTab getActiveTab(){
		try{
			if(this.tabbedPaneSources.getSelectedComponent().getClass().equals(new EditTab().getClass())   ){
				return (EditTab)this.tabbedPaneSources.getSelectedComponent();
			}
		}
		catch(Exception e){
				
		}
		
		return null;
	} // end getActiveTab



////////////////////CLICK :-) ////////////////////////
	public void actionPerformed(ActionEvent actEv){

		///////////
		if(actEv.getActionCommand().compareToIgnoreCase("Open")==0 ){
		
			try{
		
				//for opening a file
				JFileChooser chooser = new JFileChooser();
				if(defaultPath == null){
					File tmpFile = new File(".", "__I.__I");
					chooser.setCurrentDirectory( tmpFile.getParentFile() );
					tmpFile.delete();
				}
				else{
					chooser.setCurrentDirectory( defaultPath );
				}				
				//FileFilter filter = new FileFilter();
				//filter.addExtension("jpg");
				//filter.addExtension("gif");
				//filter.setDescription("JPG & GIF Images");
				//chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(this);
				
				if(returnVal == JFileChooser.APPROVE_OPTION){
					defaultPath = chooser.getSelectedFile().getParentFile();
					File FilePosition = chooser.getSelectedFile().getAbsoluteFile();

					getActiveTab().setSourceImage(ImageIO.read(FilePosition)); //getActiveTab().setImage()
					repaint();
				}
				else{
					
				}
				
			}
			catch(IOException IOEx){
				this.setMessage("The File was not a valid Image");
			} // end try-catch
			catch(NullPointerException NullEx){
				this.setMessage("To Open an Image select an EditTab");
			}
			getActiveTab().getModelTab().ExtractedLines = new Vector();
		
		} // end if "Open"
		
		
		///////////////
		if(actEv.getActionCommand().compareToIgnoreCase("Quit")==0 ){
			System.exit(0);
		} // end if quit

		///////////////
		if(actEv.getActionCommand().compareToIgnoreCase("Zoom in")==0 ){
			if(getActiveTab() != null){
				getActiveTab().zoomIn();
			}
		} // end if zoom in

		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("Zoom out")==0 ){
			if(getActiveTab() != null){
				getActiveTab().zoomOut();
			}
		} // end if zoom out

		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("Spoil")==0 ){
			if(getActiveTab() != null){
				getActiveTab().SpoilImage();
			}
		} // end if spoil

		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("Extract")==0 ){
			if(getActiveTab() != null){
				getActiveTab().ExtractImage();
			}
		} // end if extract

		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("extra smooth")==0 ){
			if(getActiveTab() != null){
				
				if(((JCheckBox)actEv.getSource()).isSelected()){
					getActiveTab().setExtraSmooth(true);
					
				}
				else
				{
					getActiveTab().setExtraSmooth(false);
				}
				
			}
		} // end if extract



		if(actEv.getActionCommand().compareToIgnoreCase("color check" ) == 0){
				
			if(((JCheckBox)actEv.getSource()).isSelected()){
				getActiveTab().setLevelOf("color check",1);	
			}
			else{
				getActiveTab().setLevelOf("color check",0);
			}
		}			
			
		if( actEv.getActionCommand().compareToIgnoreCase("open pathes" ) == 0){
			if(((JCheckBox)actEv.getSource()).isSelected()){
				getActiveTab().setLevelOf("open pathes",1);	
			}
			else{
				getActiveTab().setLevelOf("open pathes",0);
			}
		}			

		if(actEv.getActionCommand().compareToIgnoreCase("select vertices" ) == 0){
			if(((JCheckBox)actEv.getSource()).isSelected()){
				getActiveTab().setLevelOf("select vertices",1);	
			}
			else{
				getActiveTab().setLevelOf("select vertices",0);
			}
		}			
		if(actEv.getActionCommand().compareToIgnoreCase("select polygons" ) ==0){
			if(((JCheckBox)actEv.getSource()).isSelected()){
				getActiveTab().setLevelOf("select polygons",1);	
			}
			else{
				getActiveTab().setLevelOf("select polygons",0);
			}
		}



		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("Save")==0 ){
			
		} // end if save

		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("Save Extract")==0 ){

			try{
		
				//for opening a file
				JFileChooser chooser = new JFileChooser();
				//FileFilter filter = new FileFilter();
				//filter.addExtension("jpg");
				//filter.addExtension("gif");
				//filter.setDescription("JPG & GIF Images");
				//chooser.setFileFilter(filter);
				
				//String ThisIsHere = new File(".", "test.txt").getCanonicalPath();
				if(defaultSavePath == null){
					File tmpFile = new File(".", "__I.__I");
					chooser.setCurrentDirectory( tmpFile.getParentFile() );
					tmpFile.delete();
				}
				else{
					chooser.setCurrentDirectory( defaultSavePath );
				}
				chooser.setSelectedFile(new File("labyrinth.lines"));
				int returnVal = chooser.showSaveDialog(this);
				
				if(returnVal == JFileChooser.APPROVE_OPTION){
					defaultSavePath = chooser.getSelectedFile().getAbsoluteFile();
					File FilePosition = chooser.getSelectedFile().getAbsoluteFile();

					getActiveTab().writeLinesToFile(FilePosition); //getActiveTab().setImage()
					//repaint();
					File outputFile = new File(FilePosition.getAbsolutePath());
				}
				else{
					
				}
				
			}
			/*
			catch(IOException IOEx){
				this.giveMessage("The File was not a valid Image");
			} // end try-catch
			*/
			catch(NullPointerException NullEx){
				this.setMessage("To Open an Image select an EditTab");
			}			
			
			
		} // end if save as 

		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("New Editor")==0 ){
			makeTab("normal");
		} // end if new editor

		//////////////	
		if(actEv.getActionCommand().compareToIgnoreCase("Close Editor")==0 ){
			try{
				tabbedPaneSources.remove(tabbedPaneSources.getSelectedIndex());
			}
			catch(Exception e){
				
			}
		} // end if open editor
		
		if(actEv.getActionCommand().compareToIgnoreCase("delete element")==0 ){
			try{
				getActiveTab().deleteElement();
			}
			catch(Exception e){
				
			}
		} // end if open editor

	} // end click
	
	//////////////////////////////////////////SLIDE!!!////////////	
	public void stateChanged(ChangeEvent e)
	{
		//onDisplayCanvas.setZoom(((float)zoomSlider.getValue())/10);
		//ConDisplayCanvas.repaint();
		if(e.getSource().getClass().equals(new JCheckBox().getClass()) ){
			//System.out.println("Extra!");
			if(  ((JCheckBox)e.getSource()).getActionCommand() == "extra smooth" ){
				getActiveTab().setExtraSmooth( ((JCheckBox)e.getSource()).isSelected() );
			}
			
			if(  ((JCheckBox)e.getSource()).getActionCommand() == "color check" ){
				
				if(((JCheckBox)e.getSource()).isSelected()){
					getActiveTab().setLevelOf("color check",1);	
				}
				else{
					getActiveTab().setLevelOf("color check",0);
				}
			}			
			
			if(  ((JCheckBox)e.getSource()).getActionCommand() == "open pathes" ){
				if(((JCheckBox)e.getSource()).isSelected()){
					getActiveTab().setLevelOf("open pathes",1);	
				}
				else{
					getActiveTab().setLevelOf("open pathes",0);
				}
			}			

			if(  ((JCheckBox)e.getSource()).getActionCommand() == "select vertices" ){
				if(((JCheckBox)e.getSource()).isSelected()){
					getActiveTab().setLevelOf("select vertices",1);	
				}
				else{
					getActiveTab().setLevelOf("select vertices",0);
				}
			}			
			if(  ((JCheckBox)e.getSource()).getActionCommand() == "select polygons" ){
				if(((JCheckBox)e.getSource()).isSelected()){
					getActiveTab().setLevelOf("select polygons",1);	
				}
				else{
					getActiveTab().setLevelOf("select polygons",0);
				}
			}	
				
		} // end checkboxes
		if(e.getSource().getClass().equals(new JSlider().getClass()) ){
			JSlider tempSlider = (JSlider)e.getSource(); 
			if( tempSlider.getName().compareToIgnoreCase("smooth:")==0){
				if(getActiveTab() != null){
					getActiveTab().setLevelOf("smooth:",tempSlider.getValue());
					//System.out.println("smooth: "+ tempSlider.getValue());
				}			
			}// end if smooth slider
			if( tempSlider.getName().compareToIgnoreCase("bright:")==0){
				if(getActiveTab() != null){
					getActiveTab().setLevelOf("bright:",tempSlider.getValue());
					//System.out.println("bright: "+ tempSlider.getValue());
				}			
			}// end if bright slider		
			
		}//end if Slider
		
		if(e.getSource().getClass().equals(new JTabbedPane().getClass()) ){
			//System.out.println("Extra!");
			initialiseControls();	
		}
		
	} // end sliding

	public void initialiseControls(){
		//getActiveTab().getControlValues();
		for(int elemCount = 0; elemCount < ToolbarControls.size(); elemCount ++){
			if( ((JComponent)ToolbarControls.elementAt(elemCount)).getClass().equals(new JCheckBox().getClass()) ){
				JCheckBox tmpBox = ((JCheckBox)ToolbarControls.elementAt(elemCount));
				if( getActiveTab().getLevelOf(tmpBox.getActionCommand())  == 1   ){
					tmpBox.setSelected(true);
				}
				else
				{
					tmpBox.setSelected(false);
				}
				   
				
				} // end checkboxes

			if( ((JComponent)ToolbarControls.elementAt(elemCount)).getClass().equals(new JSlider().getClass()) ){
				JSlider tmpSlide = ((JSlider)ToolbarControls.elementAt(elemCount));
				tmpSlide.setValue(	getActiveTab().getLevelOf(tmpSlide.getName() ) );
				
				
			} // end slider

			
		} //loop through elements
	} // end set controls




	public void setMessage(String text){
		universalDialog.showMessageDialog(text);
		//System.out.println(text);
	} // end give Message

////////////////MAIN////////////////////////////
	public static void main(String[] args) {
		new Labyrinth(args);
	} // end main
	
/////////////////Self Reference/////////////////	
	static Labyrinth getThis(){
		if(selfRef == null){
			return new Labyrinth(null);
		}
		return selfRef;
	} // end selfref
}// end class
