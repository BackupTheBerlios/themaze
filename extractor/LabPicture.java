/*
 * Created on 08.10.2003
 * This class is part of the concat it package.
 * 
 */
import java.awt.image.*;


/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LabPicture extends BufferedImage {

	public int SizeX = 0, SizeY = 0;

	/**
	 * Other constructors are not possible due to limitations
	 * of the BufferedImageConstructor (see javadocs) 
	 */
	public LabPicture(int SizeX, int SizeY) {
		super(SizeX,SizeY,BufferedImage.TYPE_INT_RGB);
		this.SizeX = SizeX;
		this.SizeY = SizeY;
	}// end constructor




} // end class
