
import javax.swing.*;
import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TargetTab extends JPanel implements MouseListener, MouseMotionListener {


    //this is the image that gets dispayed :-)
    private BufferedImage BackgroundPict = null;
    private EditTab ParentTab;
    //what we start with :-)
//	private int StartX 			= 1000;
//	private int StartY 			= 1000;
    //what is picture is really :-)
    private int StandardX = 1000;
    private int StandardY = 1000;
    public double StandardZoom = 1.0;

    //the extract from the source
    public boolean doEditing = true;
    //private Point SpoilP1,SpoilP2,SpoilP3,SpoilP4;
    public Vector ExtractedLines;
    //for the mouse Events
    private Point SelectedPoint;
    int oldWidth, oldHeight;
    public int selectedPolygon;


    /**
     *
     */
    public TargetTab(EditTab ParentTab) {
        super();

        this.ParentTab = ParentTab;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        ExtractedLines = new Vector();
        //first test polygon
        MrXPolygon testFurunkel = new MrXPolygon();
        MrXPolygon testFurunkel2 = new MrXPolygon();
        //first
        testFurunkel.addPoint(10, 10);
        testFurunkel.addPoint(990, 10);
        testFurunkel.addPoint(200, 800);
        testFurunkel.addPoint(990, 800);
        //second
        testFurunkel2.addPoint(10, 150);
        testFurunkel2.addPoint(800, 150);
        testFurunkel2.addPoint(10, 800);
        testFurunkel2.addPoint(10, 990);
        testFurunkel2.addPoint(990, 990);

        ExtractedLines.add(testFurunkel);
        ExtractedLines.add(testFurunkel2);

        ((MrXPolygon) ExtractedLines.elementAt(0)).movePoint(4, 500, 990);
    } // end Konstruktor

    public void DisplayChanged() {

        if (ParentTab.getSourceImage() == null) {
            BackgroundPict = new BufferedImage(StandardX, StandardY, BufferedImage.TYPE_INT_RGB);
            StandardZoom = 1.0;
        } else {
            ColorConvertOp transformToWaterMark = new ColorConvertOp(new RenderingHints(null));
            BackgroundPict = new BufferedImage(ParentTab.getSourceImage().getWidth(), ParentTab.getSourceImage().getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            BackgroundPict = transformToWaterMark.filter(ParentTab.getSourceImage(), BackgroundPict);

            //transform to watermark
            //BackgroundPict = BufferedImageOp(ParentTab.getSourceImage());
            double diffX;
            double diffY;
            int JPWidth = this.getWidth();
            int JPHeight = this.getHeight();
            diffX = ParentTab.getSourceImage().getWidth() - JPWidth;
            diffY = ParentTab.getSourceImage().getHeight() - JPHeight;
            if (diffX >= diffY) {
                StandardZoom = (double) JPWidth / (double) (ParentTab.getSourceImage().getWidth());

            } else if ((diffX < diffY)) {
                StandardZoom = (double) JPHeight / (double) (ParentTab.getSourceImage().getHeight());

            } // end (if) set zoom

            StandardX = ParentTab.getSourceImage().getWidth();
            StandardY = ParentTab.getSourceImage().getHeight();
            //StandardX =	1000;
            //StandardY = 1000;


        }
    } // end Display changed


    public void paint(Graphics g) {
        if (BackgroundPict != null) {
            int JPWidth = this.getWidth();
            int JPHeight = this.getHeight();
            g.clearRect(0, 0, JPWidth, JPHeight);
            g.drawImage(BackgroundPict, (int) getPicStart().getX(),
                    (int) getPicStart().getY(),
                    ((int) (BackgroundPict.getWidth() * StandardZoom)),
                    ((int) (BackgroundPict.getHeight() * StandardZoom)),
                    null);

//g.drawImage(BackgroundPict, (int)((JPWidth - StandardX*StandardZoom)/2) , (int)((JPHeight - StandardY*StandardZoom)/2) ,(int)(StandardX*StandardZoom),(int)(StandardY*StandardZoom),null);
            //g.drawString("Picture: 			  "+,10,10);
            String InfoText = "Picture Resolution: (x,y) " + StandardX + " x " + StandardY + " Pixels.";
            g.setColor(new Color(80, 80, 80));
            int TextWidth = g.getFontMetrics().stringWidth(InfoText);
            g.fillRoundRect(12, 10, (TextWidth) + 5, 20, 5, 5);
            g.setColor(new Color(140, 140, 140));
            g.drawString(InfoText, 15, 25);

            g.setColor(new Color(240, 14, 14));
            if (this.ExtractedLines != null) {

                /////ATTENTION!!! MAYBE SOME LINES MAY BE LOST HERE

                for (int LineCount = 0; LineCount < ExtractedLines.size(); LineCount++) {
                    if (((MrXPolygon) ExtractedLines.elementAt(LineCount)).npoints == 0) {
                        //ExtractedLines.removeElementAt(LineCount);

                    } else {
						if(selectedPolygon == LineCount){
							g.setColor(new Color(240, 214, 14));
						}
						else{
							g.setColor(new Color(240, 14, 14));
						}
						
                        int[] P_X = ((MrXPolygon) ExtractedLines.elementAt(LineCount)).xpoints;
                        int[] P_Y = ((MrXPolygon) ExtractedLines.elementAt(LineCount)).ypoints;
                        //correctSpoilPoints();
                        //g.drawRect(computeRelativePositionX((P_X[0]))-5,computeRelativePositionY((P_Y[0]))-5,10,10);
                        for (int PointCount = 1; PointCount < ((MrXPolygon) ExtractedLines.elementAt(LineCount)).npoints; PointCount++) {
                            //draw a Point and a line each time
                            //System.out.println(" Anzahl der Punkte: "+ ((Polygon)ExtractedLines.elementAt(LineCount)).npoints);
                            //System.out.println(" x | y: "+ P_X[PointCount-1] + " | " + P_Y[PointCount-1]+" " );

                            //	g.drawRect(   computeRelativePositionX((P_X[PointCount]))-5,computeRelativePositionY((P_Y[PointCount]))-5,10,10);
                            g.drawLine(computeRelativePositionX(P_X[PointCount - 1]), computeRelativePositionY(P_Y[PointCount - 1]),
                                    computeRelativePositionX(P_X[PointCount]), computeRelativePositionY(P_Y[PointCount]));

                        } // end loop through points in lines
                    } // end if no points in it
                } //end loop through line-arrays
            } //end some lines are there


        } // end if pict != null
    } // end paint

    public void resetSpoilPoints() {
        //int JPWidth = this.getWidth();
        //int JPHeight = this.getHeight();
        oldWidth = this.getWidth();
        oldHeight = this.getHeight();

        /*
        SpoilP1 = new Point((int)((StandardX)/4+((JPWidth - StandardX)/2)),(int)((StandardY)/4+((JPHeight - StandardY)/2)));
        SpoilP2 = new Point((int)(3*(StandardX)/4+((JPWidth - StandardX)/2)),(int)((StandardY)/4+((JPHeight - StandardY)/2)));
        SpoilP3 = new Point((int)((StandardX)/4+((JPWidth - StandardX)/2)),(int)(3*(StandardY)/4+((JPHeight - StandardY)/2)));
        SpoilP4 = new Point((int)(3*(StandardX)/4+((JPWidth - StandardX)/2)),(int)(3*(StandardY)/4+((JPHeight - StandardY)/2)));
        */
    }// end reset Points

    public void correctSpoilPoints() {
        //	int JPWidth = this.getWidth();
        //	int JPHeight = this.getHeight();
        /*
        SpoilP1.setLocation(SpoilP1.getX()+(JPWidth-oldWidth)/2,SpoilP1.getY()+(JPHeight-oldHeight)/2);
        SpoilP2.setLocation(SpoilP2.getX()+(JPWidth-oldWidth)/2,SpoilP2.getY()+(JPHeight-oldHeight)/2);
        SpoilP3.setLocation(SpoilP3.getX()+(JPWidth-oldWidth)/2,SpoilP3.getY()+(JPHeight-oldHeight)/2);
        SpoilP4.setLocation(SpoilP4.getX()+(JPWidth-oldWidth)/2,SpoilP4.getY()+(JPHeight-oldHeight)/2);
        */
        oldWidth = this.getWidth();
        oldHeight = this.getHeight();

    }// end reset Points


    /*
     *
     */
//	//////////////////Click////////////////////////
    public void mouseClicked(MouseEvent ExitEv) {

    } // end click

//	  //////////////////Exit////////////////////////
    public void mouseExited(MouseEvent ExitEv) {

    } // end exit
//	  //////////////////Enter////////////////////////
    public void mouseEntered(MouseEvent EnterEv) {

    } // end enter
//	  ////////////////////Release////////////////////////
    public void mouseReleased(MouseEvent ReleasEv) {
        SelectedPoint = null;
    } // end release
//	  ////////////////////Press////////////////////////
    public void mousePressed(MouseEvent PressEv) {
        //check if a point was clicked at
        if (isAimedAt(computeAbsolutePosition(PressEv.getPoint())) != null) {
            SelectedPoint = isAimedAt(computeAbsolutePosition(PressEv.getPoint()));
            //System.out.println("Gotit");

        }
		repaint(); 		

    } // end press

//	////////////////////Press////////////////////////
    public void mouseDragged(MouseEvent DragEv) {
        if (SelectedPoint != null) {
            //for debug
            //Labyrinth.getThis().giveMessage("Mouse dragged:"+SelectedPoint.toString());
            Point tempPoint = new Point((int) DragEv.getPoint().getX(), (int) DragEv.getPoint().getY());
            //System.out.println("The Mouse Diff (x,y) "+tempPoint.x +" "+tempPoint.y );
            ((MrXPolygon) ExtractedLines.elementAt(SelectedPoint.x)).movePoint(SelectedPoint.y, computeAbsolutePosition(tempPoint).x, computeAbsolutePosition(tempPoint).y);

            //SelectedPoint.move(computeAbsolutePosition(tempPoint).x,computeAbsolutePosition(tempPoint).y);
        }
        repaint();
    } // end press

//	////////////////////Press////////////////////////
    public void mouseMoved(MouseEvent PressEv) {

    } // end press

//	//has some stupid guy aimed at a point? uhaaa!
    private Point isAimedAt(Point Coordinates) {

        if (this.ExtractedLines != null) {
            for (int LineCount = 0; LineCount < ExtractedLines.size(); LineCount++) {
                int[] P_X = ((MrXPolygon) ExtractedLines.elementAt(LineCount)).xpoints;
                int[] P_Y = ((MrXPolygon) ExtractedLines.elementAt(LineCount)).ypoints;
                for (int PointCount = 0; PointCount < ((Polygon) ExtractedLines.elementAt(LineCount)).npoints; PointCount++) {

                    //System.out.println(" x | y: "+ P_X[PointCount-1] + " | " + P_Y[PointCount-1]+" " );
                    if (Coordinates.getX() > P_X[PointCount] - 5 / StandardZoom &
                            Coordinates.getX() < P_X[PointCount] + 5 / StandardZoom &
                            Coordinates.getY() > P_Y[PointCount] - 5 / StandardZoom &
                            Coordinates.getY() < P_Y[PointCount] + 5 / StandardZoom) {
                        //now save the polygon that was selected
                        selectedPolygon = LineCount;
                        return new Point(LineCount, PointCount);
                    }


                } // end loop through points in lines
            } //end loop through line-arrays
        } //end some lines are there
		selectedPolygon = -1;
        return null;
    } // end aimed at

    /*
        private Point computeRelativePosition(int absolutePosX, int absolutePosY){

            Point relativePoint = new Point(absolutePosX,absolutePosY);
                relativePoint.x = (int)( absolutePosX-(this.getWidth()/2 - absolutePosX)*(StandardZoom-1) );
                relativePoint.y = (int)( absolutePosY-(this.getHeight()/2 - absolutePosY)*(StandardZoom-1) );
            return relativePoint;
        } // end compute relative Point
    */
    private int computeRelativePositionX(int absolutePosX) {
      //  int JPWidth = this.getWidth();
        Point p = getPicStart();
        int relativePoint = absolutePosX;
      //  relativePoint = (int) ((JPWidth - StandardX * StandardZoom) / 2 + absolutePosX * StandardZoom);
        relativePoint = (int)(p.x +  Math.round(absolutePosX * StandardZoom));
        return relativePoint;
    } // end compute relative Point

    private int computeRelativePositionY(int absolutePosY) {
       // int JPHeight = this.getHeight();
         Point p = getPicStart();
        int relativePoint = absolutePosY;
        //relativePoint = (int) ((JPHeight - StandardY * StandardZoom) / 2 + absolutePosY * StandardZoom);
        relativePoint = (int)(p.y +  Math.round(absolutePosY * StandardZoom));
        return relativePoint;
    } // end compute relative Point


    private Point computeAbsolutePosition(Point relativePos) {
     /*   int JPWidth = this.getWidth();
        int JPHeight = this.getHeight();

        Point absolutePoint = new Point(relativePos.x, relativePos.y);
        absolutePoint.x = (int) ((relativePos.x - ((JPWidth - StandardX * StandardZoom) / 2)) / StandardZoom);
        absolutePoint.y = (int) ((relativePos.y - ((JPHeight - StandardY * StandardZoom) / 2)) / StandardZoom);

        //absolutePoint.x = (int)( (relativePos.getX()+(this.getWidth()/2)*(StandardZoom-1))/StandardZoom );
        //absolutePoint.y = (int)( (relativePos.getY()+(this.getHeight()/2)*(StandardZoom-1))/StandardZoom );
        return absolutePoint;  */
        return getPicCoord(relativePos);
    } // end compute relative Point

    /**
     *
     * @param p
     * @return
     */
    private Point getPicCoord(Point p) {
        Point start = getPicStart();
        Point ende = getPicEnde();
        double x = ((p.getX() - start.getX()) * 100) / (ende.getX() - start.getX() + 1);
        double y = ((p.getY() - start.getY()) * 100) / (ende.getY() - start.getY() + 1);
        int px = (int) Math.round((x * BackgroundPict.getWidth()) / 100);
        int py = (int) Math.round((y * BackgroundPict.getHeight()) / 100);
        return new Point(px, py);
    }



    public void setBackground(BufferedImage newBack) {
        this.BackgroundPict = newBack;
    }

    public void setLines(Vector lines) {
        ExtractedLines = lines;
    } //set the extracted LInes

    /**
     *
     * @return
     */
    private Point getPicStart() {
        int JPWidth = this.getWidth();
        int JPHeight = this.getHeight();
        int PicWidth = this.BackgroundPict.getWidth();
        int PicHeight = this.BackgroundPict.getHeight();
        int startX = ((int) ((JPWidth - PicWidth * StandardZoom) / 2)) + 2;
        int startY = ((int) ((JPHeight - PicHeight * StandardZoom) / 2)) + 2;
        return new Point(startX, startY);
    }

    /**
     *
     * @return
     */
    private Point getPicEnde() {
        int endeX = (int) (getPicStart().getX() + (BackgroundPict.getWidth() * StandardZoom)) - 1;
        int endeY = (int) (getPicStart().getY() + (BackgroundPict.getHeight() * StandardZoom)) - 1;
        return new Point(endeX, endeY);

    }

} // end class