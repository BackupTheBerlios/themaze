/*
 * Created on 08.10.2003
 * This class is part of the concat it package.
 * 
 */
import javax.swing.*;
import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PictureTab extends JScrollPane implements MouseListener, MouseMotionListener{

	public final int LEFT = 33;
	public final int RIGHT = 66;
	public final int VERTICAL = 99;
	
	//this is the image that gets dispayed :-)
	private BufferedImage PaintedPict = null;
	private EditTab ParentTab;

	private int StandardX 		= 400;
	private int StandardY 		= 400;
	public	double StandardZoom 	= 1.0;
	
	//changing the image
	public boolean doSpoiling = true;
	int oldWidth, oldHeight;


    // --- NEU --- von Micha !!! ----
    private int dragIndex;
    private int dragSrcDst;

    private double srcCoords[];
    private double dstCoords[];

    private int m_pointsNeeded;
    private int m_numPoints;

    private Color cyan;
    private Color blue;

    private Point m_Mouse;
    private Point m_MouseinPic;


    // --- NEU ---

	/**
	 * 
	 */
	public PictureTab(EditTab ParentTab) {
		super(new JPanel());
         //JScrollPane scroll = new JScrollPane(imageTab);
     //   scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER,new);
       // imageTab.add(scroll,"Center");
		this.ParentTab = ParentTab;
		this.addMouseMotionListener(this);
		this.addMouseListener(this);

        // --- NEU --- von Micha !!! ----
        dragIndex = -1;
        dragSrcDst = -1;
        srcCoords = new double[8];
        dstCoords = new double[8];
        m_numPoints = 0;
        m_pointsNeeded = 4;
        cyan = new Color(0, 255, 255);
        blue = new Color(0, 0, 255);
        m_Mouse = new Point();
        m_MouseinPic = new Point();
        // --- NEU ---
        //this.
	} // end Konstruktor
	
	public void DisplayChanged(){
	
		if(ParentTab.getSourceImage() == null){
            PaintedPict = new BufferedImage(StandardX, StandardY, BufferedImage.TYPE_INT_RGB);
            StandardZoom = 1.0;
		}
		else{
            PaintedPict = ParentTab.getSourceImage();

            // this.setSize(PaintedPict.getWidth(), PaintedPict.getHeight());
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
        }

	} // end Display changed


    /**
      * auch hier Änderungen
      * @param g
      */
     public void paint(Graphics g) {
         if (PaintedPict != null) {
             int JPWidth = this.getWidth();
             int JPHeight = this.getHeight();
             g.clearRect(0, 0, JPWidth, JPHeight);
             g.drawImage(PaintedPict, (int) getPicStart().getX(),
                     (int) getPicStart().getY(),
                     ((int) (PaintedPict.getWidth() * StandardZoom)),
                     ((int) (PaintedPict.getHeight() * StandardZoom)),
                     null);
             // Infotext
             String InfoText = "M X: " + m_Mouse.getX() + " Y: " + m_Mouse.getY() +
                     " Pic X: " + m_MouseinPic.getX() + " Y: " + m_MouseinPic.getY();
             g.setColor(new Color(80, 80, 80));
             int TextWidth = g.getFontMetrics().stringWidth(InfoText);
             g.fillRoundRect(12, 10, (TextWidth) + 5, 20, 5, 5);
             g.setColor(new Color(140, 140, 140));
             g.drawString(InfoText, 15, 25);
             // Rahmen


             // Perspective
             if (doSpoiling) {
                 Point p = getPicStart();
                 g.setColor(blue);
                 for (int i = 0; i < m_numPoints - 1; i++) {  // Rahmen zeichnen
                     g.drawLine((int) p.getX() + getIntXCoord(0, i),
                             (int) p.getY() + getIntYCoord(0, i),
                             (int) p.getX() + getIntXCoord(0, i + 1),
                             (int) p.getY() + getIntYCoord(0, i + 1));
                     g.drawLine((int) p.getX() + getIntXCoord(1, i) - 1,
                             (int) p.getY() + getIntYCoord(1, i) - 1,
                             (int) p.getX() + getIntXCoord(1, i + 1) - 1,
                             (int) p.getY() + getIntYCoord(1, i + 1) - 1);
                 }
                 for (int j = 0; j < m_numPoints; j++) { //Punkte und Ecklinien
                     g.setColor(cyan);
                     // Sourcepunkte
                     g.fillRect((int) p.getX() + (getIntXCoord(0, j)) - 2,
                             (int) p.getY() + getIntYCoord(0, j) - 2, 4, 4);
                     // Destinationpunkte
                     if (j != 3)
                         g.fillRect((int) p.getX() + (getIntXCoord(1, j)) - 2,
                                 (int) p.getY() + getIntYCoord(1, j) - 2, 4, 4);
                     // Linie von Source to destination
                     g.setColor(Color.orange);
                     g.drawLine((int) p.getX() + getIntXCoord(0, j),
                             (int) p.getY() + getIntYCoord(0, j),
                             (int) p.getX() + getIntXCoord(1, j) - 1,
                             (int) p.getY() + getIntYCoord(1, j) - 1);
                 }
                 if (m_numPoints >= m_pointsNeeded) {     // Viereck schließen
                     g.setColor(blue);
                     g.drawLine((int) p.getX() + getIntXCoord(0, 0),
                             (int) p.getY() + getIntYCoord(0, 0),
                             (int) p.getX() + getIntXCoord(0, m_numPoints - 1),
                             (int) p.getY() + getIntYCoord(0, m_numPoints - 1));
                     g.drawLine((int) p.getX() + getIntXCoord(1, 0) - 1,
                             (int) p.getY() + getIntYCoord(1, 0) - 1,
                             (int) p.getX() + getIntXCoord(1, m_numPoints - 1) - 1,
                             (int) p.getY() + getIntYCoord(1, m_numPoints - 1) - 1);
                 }

             }

         } // end if pict != null
     } // end paint



	/*
	 * 
	 */	
//	//////////////////Click////////////////////////
	  public void mouseClicked(MouseEvent ExitEv){

	  } // end click

//	  //////////////////Exit////////////////////////
		public void mouseExited(MouseEvent ExitEv){

		} // end exit
//	  //////////////////Enter////////////////////////
		public void mouseEntered(MouseEvent EnterEv){

		} // end enter
//	  ////////////////////Release////////////////////////
		public void mouseReleased(MouseEvent ReleasEv){
		//	SelectedPoint = null;
		} // end release
//	  ////////////////////Press////////////////////////
		public void mousePressed(MouseEvent PressEv){
        int i = (int) ((double) PressEv.getX());
        int j = (int) ((double) PressEv.getY());
        int k = locatePoint(i, j, 1);     // Zielpunkt feststllen
        if (k != -2) {
            dragSrcDst = 1;
            if (k == -1) {
                k = locatePoint(i, j, 0);    // Quellpunkt feststellen
                dragSrcDst = 0;
            }
            if ((PressEv.getModifiers() & 0x10) != 0) {
                if (k == -1) {
                    addPoint(i, j);
                    repaint();
                    dragIndex = -1;
                    dragSrcDst = -1;
                } else {
                    dragIndex = k;
                }
            } else // mit rechter Maustaste wird der letzte Punkt gelöscht
                if ((PressEv.getModifiers() & 4) != 0 && m_numPoints > 0) {
                    removePoint(m_numPoints - 1);
                    repaint();
                }
        } else {
            dragIndex = -1;
        }
			
		} // end press
	
    /**
     * angeklickten Punkt verschieben
     *
     * @param DragEv
     */
    public void mouseDragged(MouseEvent DragEv) {
        if (dragIndex != -1) {
            setCoords(dragSrcDst, dragIndex, DragEv.getX(), DragEv.getY());
            repaint();
        }
    } // end press

	  
    /**
     * zur Kontrolle der Koordinaten
     *
     * @param PressEv
     */
    public void mouseMoved(MouseEvent PressEv) {
        m_Mouse.setLocation(PressEv.getX(), PressEv.getY());
        if ((PressEv.getX() >= getPicStart().getX()) &&
                (PressEv.getY() >= getPicStart().getY()) &&
                (PressEv.getX() <= getPicEnde().getX()) &&
                (PressEv.getY() <= getPicEnde().getY())) {
            Point pic = getPicCoord(PressEv.getPoint());
            m_MouseinPic.setLocation(pic);
        } else
            m_MouseinPic.setLocation(0, 0);
        repaint();
    } // end press


   /**
     *
     * @param i   0 - Sourcepoint, 1- DestinationPoint
     * @param j   0 - 3 ; die 4 Punkte
     * @param x
     * @param y
     */
    private void setCoords(int i, int j, int x, int y) {
        Point p = this.getPicCoord(new Point(x, y));
        if (i == 0) {
            srcCoords[2 * j] = p.x;
            srcCoords[2 * j + 1] = p.y;
        } else if (i == 1) {
            if (dragSrcDst < 1) {
                dstCoords[2 * j] = p.x;
                dstCoords[2 * j + 1] = p.y;
            } else {
                switch (j) {
                    case 0:
                        if (p.y > 0) {
                            dstCoords[1] = p.y;
                            dstCoords[3] = p.y;
                        }
                        break;
                    case 1:
                        if ((p.y > 0) && (p.x > 0)) {
                            dstCoords[1] = p.y;
                            dstCoords[2] = p.x;
                            dstCoords[3] = p.y;
                            dstCoords[4] = p.x;
                        }
                        break;
                    case 2:
                        if (p.x > 0) {
                            dstCoords[2] = p.x;
                            dstCoords[4] = p.x;
                        }
                        break;
                }

            }
        }
    }


    /**
     *
     * @param i
     * @param j
     * @return
     */
    private int locatePoint(int i, int j, int k) {
        for (int l = 0; l < m_numPoints; l++) {
            Point p = getPicCoord(new Point(i, j));
            int i1,j1;
            if (k == 0) {
                i1 = (int) Math.round(srcCoords[2 * l] - p.getX());
                j1 = (int) Math.round(srcCoords[2 * l + 1] - p.getY());
            } else {
                i1 = (int) Math.round(dstCoords[2 * l] - p.getX());
                j1 = (int) Math.round(dstCoords[2 * l + 1] - p.getY());
            }
            if (i1 * i1 + j1 * j1 < 36) {
                if ((l == 3) && (k == 1)) {
                    return -2;
                } else {
                    return l;
                }
            }
        }
        return -1;
    }


    /**
     *
     * @param j
     * @return
     */

    private int getIntXCoord(int i, int j) {
        if (i == 0)
            return (int) Math.round(srcCoords[2 * j] * StandardZoom);
        else
            return (int) Math.round(dstCoords[2 * j] * StandardZoom);
    }

    /**
     *
     * @param j
     * @return
     */
    private int getIntYCoord(int i, int j) {
        if (i == 0)
            return (int) Math.round(srcCoords[2 * j + 1] * StandardZoom);
        else
            return (int) Math.round(dstCoords[2 * j + 1] * StandardZoom);
    }


    /**
     *
     * @param x
     * @param y
     */
    private void addPoint(int x, int y) {
        // für die Darstellung der Punkte auf dem Panel
        if (m_numPoints < m_pointsNeeded) {
            setCoords(0, m_numPoints, x, y);
            Point point = getCorner(m_numPoints);
            setCoords(1, m_numPoints, point.x, point.y);
            m_numPoints++;
        }
    }


    /**
     *
     * @param i
     */
    private void removePoint(int i) {
        if (m_numPoints > 1) {
            srcCoords[2 * i] = srcCoords[2 * (m_numPoints - 1)];
            srcCoords[2 * i + 1] = srcCoords[2 * (m_numPoints - 1) + 1];
            dstCoords[2 * i] = dstCoords[2 * (m_numPoints - 1)];
            dstCoords[2 * i + 1] = dstCoords[2 * (m_numPoints - 1) + 1];
        }
        m_numPoints--;
    }


    /**
     * liefert die Eckkoordinate für das transformieren,
     * wird nur für die Darstellung berechnet, für die Berechnung
     * werden die Eckpunkte in der PersepctiveWarp.class brechnet
     * Parameter i gibt die Eckennummer an: 0 - links unten; 1 - rechts unten
     * 2 - rechts oben; 3 - links oben
     * @param i Eckenummer
     * @return liefert die Punktkoordinate
     */
    private Point getCorner(int i) {
        Point point = new Point();
        Point start = getPicStart();
        Point ende = getPicEnde();
        switch (i) {
            case 0: // links unten
                point.x = start.x;
                point.y = ende.y;
                break;

            case 1: // rechts unten
                point.x = ende.x;
                point.y = ende.y;
                break;

            case 2: // rechts oben
                point.x = ende.x;
                point.y = start.y;
                ;
                break;

            case 3: // links oben
                point.x = start.x;
                ;
                point.y = start.y;
                ;
                break;
        }
        return point;
    }

    /**
     *
     * @return
     */
    private Point getPicStart() {
        int JPWidth = this.getWidth();
        int JPHeight = this.getHeight();
        int PicWidth = this.PaintedPict.getWidth();
        int PicHeight = this.PaintedPict.getHeight();
        int startX = ((int) ((JPWidth - PicWidth * StandardZoom) / 2)) + 2;
        int startY = ((int) ((JPHeight - PicHeight * StandardZoom) / 2)) + 2;
        return new Point(startX, startY);
    }

    /**
     *
     * @return
     */
    private Point getPicEnde() {
        int endeX = (int) (getPicStart().getX() + (PaintedPict.getWidth() * StandardZoom)) - 1;
        int endeY = (int) (getPicStart().getY() + (PaintedPict.getHeight() * StandardZoom)) - 1;
        return new Point(endeX, endeY);

    }

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
        int px = (int) Math.round((x * PaintedPict.getWidth()) / 100);
        int py = (int) Math.round((y * PaintedPict.getHeight()) / 100);
        return new Point(px, py);
    }

    /**
     * Quellpunkte, die entzerrt werden sollen, können mit dieser Funktion
     * an eine andere Klasse übergeben werden.
     *
     * @return Sourcekoordinaten
     */
    public double[] getSrcCoords() {
        return srcCoords;
    }

    public void removeAllPoints() {
        for (int i = m_numPoints - 1; i >= 0; i--) {
            removePoint(i);
        }
    }

    public double[] getDstCoords() {
        return dstCoords;
    }


} // end class
