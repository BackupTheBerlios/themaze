/*
 * Created on 08.10.2003
 * This class is part of the concat it package.
 * 
 */
import javax.swing.*;
import java.awt.*;


/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MergeTab extends JPanel {


	private JTabbedPane InternalTabs;
	private PictureTab imageTab;
	private TargetTab modelTab;
	/**
	 * 
	 */
	public MergeTab() {

		this.setLayout(new BorderLayout());
		//picture-tab		
		InternalTabs = new JTabbedPane();
		InternalTabs.setTabPlacement(JTabbedPane.LEFT);
		/*
		ImageIcon pictureIcon = new ImageIcon(Labyrinth.getThis().PATH_TO_IMAGES+"image.gif");	
		InternalTabs.addTab("",pictureIcon,imageTab = new PictureTab(),"Edit an Image to extract Labyrinth");
		ImageIcon editIcon = new ImageIcon(Labyrinth.getThis().PATH_TO_IMAGES+"model.gif");	
		InternalTabs.addTab("",editIcon,modelTab = new TargetTab(),"Edit an Image to extract Labyrinth");
		*/
		add(InternalTabs,BorderLayout.CENTER);
	}

}
