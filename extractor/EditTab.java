/*
 * Created on 08.10.2003
 * This class is part of the concat it package.
 * 
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

//extract imports
//import java.awt.image.IndexColorModel;
//import javax.swing.*;
import java.util.Vector;
//import java.awt.geom.*;

/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EditTab extends JPanel{


	//important filter settings
	public int blackWhiteSwitchLevel = 3;
	public int SmoothnessLevel	= 1;
	public int colorcheck = 1;
	public int verticeVSpolygon = 0;
	public int pixelselection =0;	
	public int openPathes = 0;
	public boolean DoRounding = true;

	private JTabbedPane InternalTabs;
	private PictureTab imageTab;
	public TargetTab modelTab;
	private BufferedImage TheSourcePict = null;

    private BufferedImage BackupPic = null;
	////////////////////////////////////////
	//////////for extracting only
	private JFrame Bulletin;
	private JPanel InfoPanel;
	private JScrollPane InfoScroller;
	private JList InfoList;
	private Vector InfoListVector;
	private Vector polyvec;


//	private int image_width = 0;
//	private int image_height = 0;

// Toleranzwert fuer Kreise
	//private int circleTolerance = 39;
// Toleranzwert fuer Polygone
	private int sizeTolerance = 4;
// Schwellwert für Threashold
	private int switchValue = 241;
// setze wert für die Zeichnungen
//	private byte MarkingColor = (byte)0;
// Array mit allen Mittelpunkten
//	private int[][] center_points;
	private Vector grayValuesPolygons = new Vector();

/*** Statistiken ***/
		byte[] pixelsin;
	// Abstaende
	//private double average_distance_row = 0;
	//private double average_distance_column = 0;
	
	
	
	/**
	 * 
	 */
	public EditTab() {
		
		this.setLayout(new BorderLayout());
				
		InternalTabs = new JTabbedPane();
		InternalTabs.setTabPlacement(JTabbedPane.LEFT);
//		picture-tab
		ImageIcon pictureIcon = new ImageIcon(Labyrinth.getThis().PATH_TO_IMAGES+"image.gif");	
		InternalTabs.addTab("",pictureIcon,imageTab = new PictureTab(this),"Edit an Image to extract Labyrinth");
        // JScrollPane
       // JScrollPane scroll = new JScrollPane(imageTab);
        //   scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER,new);
       // add(scroll,"Center");
        //
//		target-tab
		ImageIcon editIcon = new ImageIcon(Labyrinth.getThis().PATH_TO_IMAGES+"model.gif");	
		InternalTabs.addTab("",editIcon,modelTab = new TargetTab(this),"Edit an Image to extract Labyrinth");
		add(InternalTabs,BorderLayout.CENTER);
//		Initialize Tabs
		imageTab.DisplayChanged();
		modelTab.DisplayChanged();




	} // end construktor

	public TargetTab getModelTab(){
		return modelTab;	
	} // end get Model tab


	public void setSourceImage(BufferedImage newSourcePict){
		this.TheSourcePict = newSourcePict;
		imageTab.DisplayChanged();
		modelTab.DisplayChanged();
	} // end setImage

	public BufferedImage getSourceImage(){
		return this.TheSourcePict; 
	} // end getImage

	public void zoomOut(){
		if(this.InternalTabs.getSelectedComponent().getClass().equals(new PictureTab(null).getClass())   ){
			imageTab.StandardZoom -=0.2;
		}		
		else{
			modelTab.StandardZoom -=0.2;
		}
		Labyrinth.getThis().repaint();
	} // end Zoom out

	public void zoomIn(){
		if(this.InternalTabs.getSelectedComponent().getClass().equals(new PictureTab(null).getClass())   ){
			imageTab.StandardZoom +=0.2;
		}		
		else{
			modelTab.StandardZoom +=0.2;
		}
		Labyrinth.getThis().repaint();
	} // end Zoom out

	/*
	 *  Here is the actual perspective- correction funktion
	 */
	public void SpoilImage(){

        BackupPic = TheSourcePict;
        PerspectiveWarp pers = new PerspectiveWarp();
        pers.setsrcImage(TheSourcePict);
        pers.setSrcCoords(imageTab.getSrcCoords());
        pers.setDstCoords(imageTab.getDstCoords());
        pers.updateWarp();
        TheSourcePict = pers.getdestinationBufferedImage();
        imageTab.DisplayChanged();
		
	} // end Spoiling

	/*
	 *  Here comes the extracting
	 */
	public void ExtractImage(){
		
		
		if(getSourceImage() == null ){
			Labyrinth.getThis().setMessage("Please open an image first!");
			return;
		}
		
		//Labyrinth.getThis().giveMessage("You want this? Me as well! So what?");
		InfoListVector = new Vector();
		InfoListVector.add("   -----Extracting started-----");
		InfoListVector.add("   ------------------------------------");
		if(Bulletin == null){
			Bulletin = new JFrame();
			Bulletin.getContentPane().setLayout(new GridLayout(1,1));
			InfoPanel = new JPanel();
			InfoList = new JList(InfoListVector);
			InfoList.setBackground(new Color(220,220,250));
			InfoScroller = new JScrollPane(InfoList);
			InfoPanel.add(InfoScroller);
			Bulletin.getContentPane().add(InfoScroller);
		}
		Bulletin.setVisible(true);
		Bulletin.setSize(170,500);
		Bulletin.setTitle("Report");
		Bulletin.repaint();
		Bulletin.show();		
		
		ColorConvertOp transformToWaterMark = new ColorConvertOp(new RenderingHints(null)); 



		//int SwitchValue;
		switch(blackWhiteSwitchLevel){
			case 0: switchValue=110; break;
			case 1: switchValue=130; break;
			case 2: switchValue=160; break;
			case 3: switchValue=175; break;
			case 4: switchValue=190; break;
			case 5: switchValue=200; break;
			case 6: switchValue=210; break;
			
			default: switchValue=110; break;
		}





		
		BufferedImage tempPict = new BufferedImage(getSourceImage().getWidth(),getSourceImage().getHeight(),BufferedImage.TYPE_INT_RGB);
		tempPict = transformToWaterMark.filter(getSourceImage(), tempPict  ) ;
		byte[] workingSpace = new byte[ tempPict.getWidth()*tempPict.getHeight() ];
		byte[] workingSpaceOrig = new byte[ tempPict.getWidth()*tempPict.getHeight() ];
		byte[] workingSpaceInput = new byte[ tempPict.getWidth()*tempPict.getHeight() ];
		//read Data
/*		
		for(int y =0 ; y < tempPict.getHeight(); y++)
		{
			for(int x =0 ; x < tempPict.getWidth() ; x++)
			{
			   int tempColor = 	tempPict.getRGB(x,y)  ;
			//   int Blue = ((tempColor & 0x0000FF));
			   int Green = ((tempColor & 0x00FF00)>>8);
			//   int Red = ((tempColor & 0xFF0000)>>16);
			   workingSpace[y*tempPict.getWidth()+ x] = (byte)((Green));
		   	}
	  	}
*/
		for(int y =0 ; y < tempPict.getHeight(); y++)
		{
			for(int x =0 ; x < tempPict.getWidth() ; x++)
			{
			   int tempColor = 	tempPict.getRGB(x,y)  ;
			   int tmpBlue = ((tempColor & 0x0000FF));
			   int tmpGreen = ((tempColor & 0x00FF00)>>8);
			   int tmpRed = ((tempColor & 0xFF0000)>>16);
			   float Adaptor = (0.8f*tmpBlue + 1.2f*tmpRed)/1.6f;
			   int NewGrey = 255 - tmpGreen;
			   if(colorcheck == 1){
			   	   	if(tmpGreen < switchValue && 5*Adaptor < switchValue){
								   NewGrey =  0;	
			   		}
			   	   	if(NewGrey < switchValue && Adaptor > switchValue){
						NewGrey =  255;	
			  	 	}
			   }
			   else{
			   		NewGrey = ( tmpBlue + tmpGreen + tmpRed ) / 3;
			   }

			   workingSpace[y*tempPict.getWidth()+ x] = (byte)((NewGrey));
			}
		}

	  	
		//////transform/////////
		
		
		
		
		
		if(DoRounding == true){
			workingSpace = Str2Byte( Mean(Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
		workingSpace = Str2Byte(reduceToBinary(Byte2Str(workingSpace), tempPict.getWidth(), tempPict.getHeight(),switchValue));

		if(openPathes > 0){		
			workingSpace = Str2Byte( erode(Byte2Str(workingSpace),Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
				
		//cleaning up
		if(SmoothnessLevel >= 2){		
			workingSpace = Str2Byte( erode(Byte2Str(workingSpace),Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
		if(SmoothnessLevel >= 4){
			workingSpace = Str2Byte( erode(Byte2Str(workingSpace),Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
		if(SmoothnessLevel >= 5){
			workingSpace = Str2Byte( erode(Byte2Str(workingSpace),Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
		if(SmoothnessLevel >= 2){
			workingSpace = Str2Byte( delate(Byte2Str(workingSpace),Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
		if(SmoothnessLevel >= 4){
			workingSpace = Str2Byte( delate(Byte2Str(workingSpace),Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
		if(SmoothnessLevel >= 5){
			workingSpace = Str2Byte( delate(Byte2Str(workingSpace),Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight()) );
		}
		//creating the lines
		workingSpace = Str2Byte( cleanborder(Byte2Str(workingSpace),new Rectangle(0,0,tempPict.getWidth(), tempPict.getHeight())  , tempPict.getWidth(), tempPict.getHeight(),10) );
		//contour...#
		//filling temporary fields
		for(int y =0 ; y < tempPict.getHeight(); y++)
		{
			for(int x =0 ; x < tempPict.getWidth() ; x++)
			{
			   workingSpaceOrig[y*tempPict.getWidth()+ x] = workingSpace[y*tempPict.getWidth()+ x];
			   workingSpaceInput[y*tempPict.getWidth()+ x] = workingSpace[y*tempPict.getWidth()+ x];	
			}
		}
		
		
		
		workingSpace =  contour(workingSpace,workingSpaceInput,workingSpaceOrig, tempPict.getWidth(), tempPict.getHeight()) ;
		//write back data
		for(int y =0 ; y < tempPict.getHeight(); y++)
		{
			for(int x =0 ; x < tempPict.getWidth() ; x++)
			{
				tempPict.setRGB(x,y,workingSpaceOrig[y*tempPict.getWidth()+ x]&0x0000ff ); 
			}
		}

		//cleaning polygons
		for (int LineCount = 0; LineCount < polyvec.size(); LineCount++) {
			if (((MrXPolygon) polyvec.elementAt(LineCount)).npoints == 0) {
				polyvec.removeElementAt(LineCount);
			}
		}
		modelTab.selectedPolygon = -1;
		
		modelTab.setBackground(tempPict);
		modelTab.setLines(polyvec);
		makeMessage("Amount of Polygons: " + polyvec.size());
		makeMessage("   -----Extracting ended-----");
		modelTab.repaint();
		
		
	} // end Extracting

	///////////////////////////////////////////////////////////////////////
	///dirty little helpers/////////////////////////////////////////////////
	
	private String cleanborder(String pixelsStr, Rectangle roi, int w, int h, int border_width)
	{
		byte[] pixels   = Str2Byte(pixelsStr);

		for (int i=0; i<h; i++)
		{
			for (int j=0; j<w; j++)
			{
				if(i <= border_width | j <= border_width)
				{
					pixels[(i)*w+(j)]=(byte)255;
				}
				else if(i > (h-border_width) | j > (w-border_width))
				{
					pixels[(i)*w+(j)]=(byte)255;
				}
				else
				{
				}
			}
		}
		return Byte2Str(pixels);
	}
	
	private String erode(String pixelsinStr, String pixelsStr, Rectangle roi, int w, int h)
	{
		byte[] pixelsin = Str2Byte(pixelsinStr);
		byte[] pixels   = Str2Byte(pixelsStr);

		InfoListVector.add("Now Erode ");
		InfoList.setListData(InfoListVector);
		Bulletin.repaint();

		for (int i=roi.y+1; i<roi.y+roi.height-1; i++)
		{
			for (int j=roi.x+1; j<roi.x+roi.width-1; j++)
			{
				int pix1 = pixelsin[(i-1)*w+(j-1)];
				pix1 = (pix1&0x0000ff);
				int pix2 = pixelsin[(i-1)*w+j];
				pix2 = (pix2&0x0000ff);
				int pix3 = pixelsin[(i-1)*w+(j+1)];
				pix3 = (pix3&0x0000ff);
				int pix4 = pixelsin[i*w+(j-1)];
				pix4 = (pix4&0x0000ff);
				int pix5 = pixelsin[i*w+j];
				pix5 = (pix5&0x0000ff);
				int pix6 = pixelsin[i*w+(j+1)];
				pix6 = (pix6&0x0000ff);
				int pix7 = pixelsin[(i+1)*w+(j-1)];
				pix7 = (pix7&0x0000ff);
				int pix8 = pixelsin[(i+1)*w+j];
				pix8 = (pix8&0x0000ff);
				int pix9 = pixelsin[(i+1)*w+(j+1)];
				pix9 = (pix9&0x0000ff);
				//erode
				if(pix5 == 0 && (pix1==255||pix2==255||pix3==255||pix4==255||pix6==255||pix7==255||pix8==255||pix9==255))
				pixels[i*w+j] = (byte) 255;
			}
		}
		return Byte2Str(pixels);
	}

	private String Mean(String pixelsinStr, Rectangle roi, int w, int h)
	{
		byte[]pixelsin = Str2Byte(pixelsinStr);
		byte[]pixels = new byte[pixelsin.length];

		InfoListVector.add("Now Mean ");
		InfoList.setListData(InfoListVector);
		Bulletin.repaint();

		for (int i=roi.y+2; i<roi.y+roi.height-2; i++)
		{
			for (int j=roi.x+2; j<roi.x+roi.width-2; j++)
			{
				int pix1 = pixelsin[(i-1)*w+(j-1)];
				pix1 = (pix1&0x0000ff);
				int pix2 = pixelsin[(i-1)*w+j];
				pix2 = (pix2&0x0000ff);
				int pix3 = pixelsin[(i-1)*w+(j+1)];
				pix3 = (pix3&0x0000ff);
				int pix4 = pixelsin[i*w+(j-1)];
				pix4 = (pix4&0x0000ff);
				int pix5 = pixelsin[i*w+j];
				pix5 = (pix5&0x0000ff);
				int pix6 = pixelsin[i*w+(j+1)];
				pix6 = (pix6&0x0000ff);
				int pix7 = pixelsin[(i+1)*w+(j-1)];
				pix7 = (pix7&0x0000ff);
				int pix8 = pixelsin[(i+1)*w+j];
				pix8 = (pix8&0x0000ff);
				int pix9 = pixelsin[(i+1)*w+(j+1)];
				pix9 = (pix9&0x0000ff);
				
				
				
			if(SmoothnessLevel >= 3){
				//first row
				int pix10 = pixelsin[(i-2)*w+(j-2)];
				pix10 = (pix10&0x0000ff);
				int pix11 = pixelsin[(i-2)*w+(j-1)];
				pix11 = (pix11&0x0000ff);
				int pix12 = pixelsin[(i-2)*w+(j)];
				pix12 = (pix12&0x0000ff);
				int pix13 = pixelsin[(i-2)*w+(j+1)];
				pix13 = (pix13&0x0000ff);
				int pix14 = pixelsin[(i-2)*w+(j+2)];
				pix14 = (pix14&0x0000ff);
				//missing second
				int pix15 = pixelsin[(i-1)*w+(j-2)];
				pix15 = (pix15&0x0000ff);
				int pix16 = pixelsin[(i-1)*w+(j+2)];
				pix16 = (pix16&0x0000ff);
				//missing third
				int pix17 = pixelsin[(i)*w+(j-2)];
				pix17 = (pix17&0x0000ff);
				int pix18 = pixelsin[(i)*w+(j+2)];
				pix18 = (pix18&0x0000ff);
			   //missing fourth
			   int pix19 = pixelsin[(i+1)*w+(j-2)];
			   pix19 = (pix19&0x0000ff);
			   int pix20 = pixelsin[(i+1)*w+(j+2)];
			   pix20 = (pix20&0x0000ff);
			   //last row
			   int pix21 = pixelsin[(i+2)*w+(j-2)];
			   pix21 = (pix21&0x0000ff);
			   int pix22 = pixelsin[(i+2)*w+(j-1)];
			   pix22 = (pix22&0x0000ff);
			   int pix23 = pixelsin[(i+2)*w+(j)];
			   pix23 = (pix23&0x0000ff);
			   int pix24 = pixelsin[(i+2)*w+(j+1)];
			   pix24 = (pix24&0x0000ff);
			   int pix25 = pixelsin[(i+2)*w+(j+2)];
			   pix25 = (pix25&0x0000ff);
			}
			   //do the Mean-Man :-)
			   pixels[i*w+j] = (byte) ((pix1 + pix2 + pix3 + pix4 + pix5 + pix6 + pix7 + pix8 + pix9 /*   +
									pix10 + pix11 + pix12 + pix13 + pix14 + pix15 + pix16 + pix17 + pix18 +
									pix19 + pix20 + pix21 + pix22 + pix23 + pix24 + pix25 */
									)/9);
			}
		}
		return Byte2Str(pixels);
	}

	private String delate(String pixelsStr, String pixelsintoStr, Rectangle roi, int w, int h)
	{
		byte[] pixels = Str2Byte(pixelsStr);
		byte[] pixelsinto = Str2Byte(pixelsintoStr);
		//byte[] pixels = pixelsin;

		InfoListVector.add("Now Delate ");
		InfoList.setListData(InfoListVector);
		Bulletin.repaint();

//		for (int i=roi.y+1; i<roi.y+roi.height-1; i++)
		for (int i=roi.y+1; i<roi.y+roi.height-1; i++)
		{
			for (int j=roi.x+1; j<roi.x+roi.width-1; j++)
			{
				int pix1 = pixelsinto[(i-1)*w+(j-1)];
				pix1 = (pix1&0x0000ff);
				int pix2 = pixelsinto[(i-1)*w+j];
				pix2 = (pix2&0x0000ff);
				int pix3 = pixelsinto[(i-1)*w+(j+1)];
				pix3 = (pix3&0x0000ff);
				int pix4 = pixelsinto[i*w+(j-1)];
				pix4 = (pix4&0x0000ff);
				int pix5 = pixelsinto[i*w+j];
				pix5 = (pix5&0x0000ff);
				int pix6 = pixelsinto[i*w+(j+1)];
				pix6 = (pix6&0x0000ff);
				int pix7 = pixelsinto[(i+1)*w+(j-1)];
				pix7 = (pix7&0x0000ff);
				int pix8 = pixelsinto[(i+1)*w+j];
				pix8 = (pix8&0x0000ff);
				int pix9 = pixelsinto[(i+1)*w+(j+1)];
				pix9 = (pix9&0x0000ff);
				//delate

			   //InfoListVector.add("pix5: " + pix5);

				if(pix5 == 255 && (pix1==0||pix2==0||pix3==0||pix4==0||pix6==0||pix7==0||pix8==0||pix9==0))
				{
					pixels[i*w+j] = (byte) 0;
					//InfoListVector.add("Now Black ");
				 }

				//InfoList.setListData(InfoListVector);
				//Bulletin.repaint();
			}
		}
		return Byte2Str(pixels);
	}

	private String reduceToBinary(String pixelsinStr, int w, int h,int SwitchValue)
	{
		byte[] pixelsin = Str2Byte(pixelsinStr);
		byte[]pixels = new byte[pixelsin.length];

		InfoListVector.add("Now Threshold ");
		InfoList.setListData(InfoListVector);
		Bulletin.repaint();

			for (int i = 0; i < h; i++)
			{
			  int offset = i * w;
			for (int j = 0; j < w; j++)
			{
				int pos = offset + j;
				int pix = pixelsin[pos];
				pix = (pix & 0x0000ff);

				if (pix <= SwitchValue)
				pix = 0;
			  else if (pix > SwitchValue)
				pix = 255;
			  pixels[pos] = (byte) pix;
			}
		  }
		return Byte2Str(pixels);
	}

	private byte[] Int2Byte(int[] inputValues)
	{
		byte[] tempByte = new byte[inputValues.length];
		for(int i = 0;i<inputValues.length;i++){
			tempByte[i] =  (byte)inputValues[i];	
		}
		return tempByte;
	}

	private int[] Byte2Int(byte[] inputValues)
	{
		int[] tempByte = new int[inputValues.length];
		for(int i = 0;i<inputValues.length;i++){
			tempByte[i] = (inputValues[i]&0x0000ff);
		}
		return tempByte;
	}



	private byte[] Str2Byte(String inputString)
	{
		return inputString.getBytes();
	}

	private String Byte2Str(byte[] inputByte)
	{
		return new String(inputByte);
	}
    /////////////////////////////////
    
    //VIP!!!
    //this is magic! 
    
	private byte[] contour(	byte[] pixelsin, byte[] pixels, byte[] pixelorig, int w, int h){

		polyvec = new Vector();

		for (int i=3; i<h-3; i++)
		{
			for (int j=3; j<w-3; j++)
			{
				if(this.checkcontains(j,i, pixelorig,w))
				{
					pixels[i*w+j] = (byte) 0; //Innere Pixel grau setzen
				}
				else
				{
					int pix1 = pixelsin[i*w+j];
					pix1 = (pix1&0x0000ff);
					int pix2 = pixels[i*w+j];
					pix2 = (pix2&0x0000ff);
					
					
					if(pix1 == 0 && pix2 == 0)
					{
						pixels = createObject( pixelsin , pixels , j, i, w);
					}
				}
			}
		}
		return pixels;
	}


	private byte[] createObject(byte[] pixelsin , byte[] pixels , int x, int y, int w){

		int xcoo = x;
		int ycoo = y;
		int holdx = x;
		int holdy = y;
		int pixvalue;
		int direction = 1;
		int rightcount = 0;
		MrXPolygon polygon = new MrXPolygon();

		do
		{
		  switch(direction){
				case 1: ycoo -= 1; break;
				case 2: xcoo += 1; break;
				case 3:	ycoo += 1; break;
				case 4: xcoo -= 1; break;
			  }
			pixvalue = (int) pixelsin[ycoo*w+xcoo];
			pixvalue = (pixvalue&0x0000ff);

			if(pixvalue == 0)
			{
				if(xcoo == holdx && ycoo == holdy);
				else
				{
					pixels[ycoo*w+xcoo] = (byte)255;
					holdx = xcoo;
					holdy = ycoo;
					polygon.addPoint(xcoo, ycoo);
				}
				direction -= 1;
				rightcount = 0;
			}
			else
			{
			  direction += 1;
			  rightcount += 1;
			}
			if(rightcount == 4)
			{
				direction -= 2;
				rightcount = 0;
			}
			if(direction == 5) direction  = 1;
			if(direction == 0) direction = 4;
		}
		while (ycoo != y || xcoo != x);


		Rectangle size = polygon.getBounds();
		//jetzt noch folgende rausschmeissen:
		//alle die nicht +/- toleranz (globale variable) höhe = breite sind
		if((size.height < sizeTolerance & size.width < sizeTolerance)
		//| (size.height > (image_height-4) & size.width > (image_width-4))
		//| !((size.width >= (size.height-circleTolerance)) & (size.width <= (size.height+circleTolerance)))
		)
		{

		}
		else
		{

			MrXPolygon tmp_poly = new MrXPolygon();
			boolean takeNext = true;
			for(int i=0;i<polygon.npoints;i++)
			{
						  if(takeNext | i == polygon.npoints-1){
							tmp_poly.addPoint(polygon.xpoints[i],polygon.ypoints[i]);
							takeNext = false;
						  }
						  else{
							takeNext = true;
						  }

			}
						this.polyvec.addElement(tmp_poly);
		} // end remove unwanted polygons
		return pixels;
	}

   public boolean checkcontains(int x, int y, byte[] pixelsin,int w)
   {
		   boolean inside = false;
		   int[] temp = new int[2];

		   for (int i = 0; i < this.polyvec.size() && inside == false; i++)
		   {

				   if(((MrXPolygon) this.polyvec.elementAt(i)).contains(x,y))
				   {
					 inside = true;
					 if(pixelsin[y*w+x] < switchValue){
					   temp[0]=i;
					   temp[1]=pixelsin[y*w+x];
					   grayValuesPolygons.add(temp);
					 }
					 break;
				   }
	   }
	   return inside;
   }// end check inside





	private void makeMessage(String message)
	{
		InfoListVector.add(message);
		InfoList.setListData(InfoListVector);
		Bulletin.repaint();
	}

	public int getLevelOf(String Commando){
		if(Commando.compareToIgnoreCase("smooth:")==0){
			return this.SmoothnessLevel;
		}
		if(Commando.compareToIgnoreCase("bright:")==0){
			return this.blackWhiteSwitchLevel;
		}
		if(Commando.compareToIgnoreCase("extra smooth")==0){
			if(this.DoRounding){
				return 1;
			}
			return 0;
		}
		if(Commando.compareToIgnoreCase("color check")==0){
			return this.colorcheck;
		}
		if(Commando.compareToIgnoreCase("select vertices")==0){
			if(verticeVSpolygon == 0){
				return 1;
			}
			return 0;
		}				

		if(Commando.compareToIgnoreCase("select polygons")==0){
			return this.verticeVSpolygon;
		}

		
		if(Commando.compareToIgnoreCase("open pathes")==0){
			return	openPathes;
		}
		
		else return 1;
	}// end get level


	public void setLevelOf(String Commando, int newLevel){
		if(Commando.compareToIgnoreCase("smooth:")==0){
			this.SmoothnessLevel = newLevel;
		}
		if(Commando.compareToIgnoreCase("bright:")==0){
			this.blackWhiteSwitchLevel = newLevel;
		}
		if(Commando.compareToIgnoreCase("color check")==0){
			this.colorcheck = newLevel;
		}
		if(Commando.compareToIgnoreCase("select vertices")==0){
			this.verticeVSpolygon = newLevel;
		}
		if(Commando.compareToIgnoreCase("select polygons")==0){
			this.verticeVSpolygon = newLevel;
		}
		if(Commando.compareToIgnoreCase("delete pixels")==0){
			this.pixelselection = newLevel;
		}		
		if(Commando.compareToIgnoreCase("open pathes")==0){
			this.openPathes = newLevel;
		}
		
		
		
	}//end set level


	public void setExtraSmooth(boolean State){
		this.DoRounding = State;
		//System.out.println("Round? "+DoRounding);
	}// end set level

	public boolean isSmooth(){
		return this.DoRounding;
		//System.out.println("Round? "+DoRounding);
	}// end set level

	public void deleteElement(){
		if(pixelselection == 1){
			
		}
		if(verticeVSpolygon == 1){
			if(modelTab.selectedPolygon != -1){
				modelTab.ExtractedLines.removeElementAt(modelTab.selectedPolygon);
				modelTab.selectedPolygon = -1;
			}
		}
		else{
			
		}
		
		repaint();		
	}//end delete

	public void writeLinesToFile(File TargetFile){
		//System.out.println(TargetFile.getAbsolutePath());
		//System.out.println(TargetFile.getName());
		File FileToWriteTo = new File(TargetFile.getAbsolutePath());
		
		try{
			FileWriter theWritingBob = new FileWriter(FileToWriteTo);
			BufferedWriter WritingBobsMate = new BufferedWriter(theWritingBob);


			WritingBobsMate.write("NUMPOLLIES "+modelTab.ExtractedLines.size()+" # Anzahl Polygone, nicht der Dreiecke!");
			WritingBobsMate.newLine();
			WritingBobsMate.write("# Comments only with \'#\'");
			WritingBobsMate.newLine();
/*			
			WritingBobsMate.write("# =================================OUTER_WALLS================================");
			WritingBobsMate.newLine();
			WritingBobsMate.write("#  Floor 1 Outer Wall left");
			WritingBobsMate.newLine();						
			WritingBobsMate.write("			#     x1      z1      x2      z2      t");
			WritingBobsMate.newLine();						
			WritingBobsMate.write("0.0     0.0     0.0   -11.0    8.0 ");
			WritingBobsMate.newLine();
			WritingBobsMate.write("#  t Bedeutet Nummer der Textur...");
			WritingBobsMate.newLine();
*/
			WritingBobsMate.write("Begin");
			WritingBobsMate.newLine();

			if(modelTab.ExtractedLines !=null){
				
			/////ATTENTION!!! MAYBE SOME LINES MAY BE LOST HERE
				for(int LineCount=0; LineCount<modelTab.ExtractedLines.size();LineCount++){
					WritingBobsMate.write("		BeginPoly");
					WritingBobsMate.newLine();
					WritingBobsMate.write("		 	NUMPOINTS "+((MrXPolygon)modelTab.ExtractedLines.elementAt(LineCount)).npoints);
					WritingBobsMate.newLine();					
					//setting up an inner point for computing the polygons
					//WritingBobsMate.write("		 InnerPoint x y");
					//WritingBobsMate.newLine();
					
					int[] P_X = ((MrXPolygon)modelTab.ExtractedLines.elementAt(LineCount)).xpoints;
					int[] P_Y = ((MrXPolygon)modelTab.ExtractedLines.elementAt(LineCount)).ypoints;
															
					for(int PointCount=0;PointCount< ((MrXPolygon)modelTab.ExtractedLines.elementAt(LineCount)).npoints ;PointCount++){
						WritingBobsMate.write("		 	Point "+ P_X[PointCount] +" "+ P_Y[PointCount]);
						WritingBobsMate.newLine();
						
					
					} // end loop through points in lines
					WritingBobsMate.write("		EndPoly");
					WritingBobsMate.newLine();
					
					
				} //end loop through line-arrays
			} //end some lines are there
			WritingBobsMate.write("End");
			WritingBobsMate.newLine();			
			

			WritingBobsMate.write("");
			WritingBobsMate.newLine();
			WritingBobsMate.close();
			Labyrinth.getThis().setMessage("File successfully written!");			
		} // end writing
		catch(IOException e){
			Labyrinth.getThis().setMessage(e.getMessage());
		} // catch write error
	}// end write Lines


	/////////////////////////////
	/////////////////////////////
} // end class
///////////////////////////////////