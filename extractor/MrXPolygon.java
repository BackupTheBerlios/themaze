/*
 * Created on 23.10.2003
 * This class is part of the concat it package.
 * 
 */

import java.awt.*;

/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MrXPolygon extends Polygon{

	//private Polygon LastVersion;

	/**
	 * 
	 */
	public MrXPolygon() {
		super();
		//LastVersion = new Polygon();
	} //end konstructor


	public void movePoint(int Number, int deltaX, int deltaY){
		//System.out.println("I got Point: "+ Number);
		//System.out.println("dx: "+ deltaX);
		//System.out.println("dy: "+ deltaY);
		Polygon tempPoly = new Polygon();
		for(int i=0;i<this.npoints; i++){
			if(i!=Number){
				tempPoly.addPoint(this.xpoints[i],this.ypoints[i]);
			}
			else{
				tempPoly.addPoint(deltaX,deltaY);
			}
			
		} // end loop through points
		
		this.reset();
		for(int i=0;i<tempPoly.npoints; i++){
			this.addPoint(tempPoly.xpoints[i],tempPoly.ypoints[i]);
			
		} // end loop through points
		

			
	}// end move Point


}//end class
////////////////
