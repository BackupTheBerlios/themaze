
import javax.media.jai.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;



/**
 * Klasse entzerrt ein Image vom Typ RenderedImage (z.B. BufferedImage)
 * perspektivisch. Das Quellbild muss im Konstruktor oder in einer
 * setter-Funktion übergeben werden. Dann muessen die Quellpunkte im
 * Bild übergebenwerden, die zu den neuen Eckpunkten werden.
 * Mit updateWarp wird die Perspektive berechnet.<BR>
 * Mit der getdestinationImage-Funktion erhält man das neue entzerrte
 * Bild<BR>
 *
 * @author Michael Stenschke
 * @version 0.5
 */
public class PerspectiveWarp {

    private RenderedImage m_srcImage;
    private RenderedImage m_dstImage;
    private Interpolation m_interpolation;
    private int m_pointsNeeded;
    private int m_numPoints;
    private double m_srcCoords[];
    private double m_dstCoords[];
    private int m_width;
    private int m_height;
    private String m_filename;

    /**
     * Standardkonstruktor, wenn dieser Konstruktor aufgerufen wird
     * muss auf jeden Fall setsrcImage(..) noch aufgerufen werden
     */
    public PerspectiveWarp() {
        m_numPoints = 0;
        m_pointsNeeded = 4;
        m_srcCoords = new double[8];
        m_dstCoords = new double[8];
        m_interpolation = new InterpolationNearest();
    }

    /**
     * Konstruktor mit Übergabe des SourceImages
     *
     * @param sourceimage  Quellbild
     */
    public PerspectiveWarp(RenderedImage sourceimage) {
        m_srcImage = sourceimage;
        m_dstImage = sourceimage;
        m_numPoints = 0;
        m_pointsNeeded = 4;
        m_srcCoords = new double[8];
        m_dstCoords = new double[8];
        m_width = m_srcImage.getWidth();
        m_height = m_srcImage.getHeight();
        m_interpolation = new InterpolationNearest();
    }

    /**
     * Liefert Quellbild zur Kontrolle zurück
     *
     * @return Quellbild als RenderedImage
     */
    public RenderedImage getsrcImage() {
        return m_srcImage;
    }

    /**
     * setzt Quellbild
     *
     * @param sourceimage
     */
    public void setsrcImage(RenderedImage sourceimage) {
        this.m_srcImage = sourceimage;
        m_width = m_srcImage.getWidth();
        m_height = m_srcImage.getHeight();
        m_dstImage = sourceimage;
    }

    /**
     * Gibt das neue berechnete Bild zurück
     *
     * @return entzerrtes Bild als BufferedImage
     */
    public BufferedImage getdestinationBufferedImage() {
        BufferedImage bi = new BufferedImage(m_width, m_height, 5);
        RenderedOp ro = (RenderedOp) m_dstImage;
        Rectangle r = new Rectangle(0, 0, m_width, m_height);
        bi = ro.getAsBufferedImage(r, ro.getColorModel());
        return bi;
    }


    /**
     * Gibt das neue berechnete Bild zurück
     *
     * @return entzerrtes Bild als RenderedImage(Interface)/RenderedOp
     */
    public RenderedImage getdestinationRenderedImage() {
        return m_dstImage;
    }

    /**
     * Setzt die Quellpunkte, die zu den Eckpunkte gezogen/entzerrt
     * werden sollen<BR>
     * Das Array muss eine Länge von 8 Felder haben, beginnend bei 0.
     * Die geraden Stellen sind die X-Koordinaten, die ungeraden sind
     * die Y-Koordinaten
     *
     * @param coord
     */
    public boolean setSrcCoords(double[] coord) {
        if (coord.length >= 8) {
            m_srcCoords = coord;
            if (this.m_srcImage != null) {
                for (int i = 0; i < m_pointsNeeded; i++) {
                    Point p = getCorner(i);
                    m_dstCoords[i * 2] = p.x;
                    m_dstCoords[i * 2 + 1] = p.y;
                    m_numPoints++;
                }
            }
            return true;
        } else
            return false;
    }

    /**
     * Neuen SorecePunkt hinzufügen . Nicht mehr als 4!<BR>
     * Funktion muss viermal , für 4 Eckpunkte aufgerufen werden.
     *
     * @param x - X-Koordinate
     * @param y - Y-Koordinate
     */
    public void addPoint(double x, double y) {
        if (m_numPoints < m_pointsNeeded) {
            setCoords(0, m_numPoints, x, y);
            Point point = getCorner(m_numPoints);
            setCoords(1, m_numPoints, point.x, point.y);
            m_numPoints++;
        }
    }

    /**
     * Zum Öffnen einer Bilddatei von einem Datenträger
     *
     * @param filename
     */
    public void loadfile(String filename) {
        m_filename = filename;
        setsrcImage(JAI.create("fileload", m_filename));
    }

    /**
     * Zum abspeichern des entzerrten Bildes
     *
     * @param filename
     */
    public void savefile(String filename) {
        JAI.create("filestore", getdestinationRenderedImage(), filename, "JPEG", null);
    }


    /**
     * Setzt die Interpolationsart, wie soll die Berechnung sein?<BR>
     * 1 - Nearest, 2 - Bilinear; 3 - Bicubic; 4 - Bicubic2<BR>
     * 1 reicht meistens, aber man kann mit den anderen auch
     * probieren
     *
     * @param i Interpolationsstufe
     */
    public void setInterpolation(int i) {
        switch (i) {
            case 1: // '\001'
                m_interpolation = new InterpolationNearest();
                break;

            case 2: // '\002'
                m_interpolation = new InterpolationBilinear();
                break;

            case 3: // '\003'
                m_interpolation = new InterpolationBicubic(8);
                break;

            case 4: // '\004'
                m_interpolation = new InterpolationBicubic2(8);
                break;
        }
        updateWarp();
    }

    /**
     * starte Berechnung!
     */
    public void updateWarp() {
        if (m_numPoints == m_pointsNeeded) {
            PerspectiveTransform transform = new PerspectiveTransform();
            ;
            transform = PerspectiveTransform.getQuadToQuad(m_dstCoords[0], m_dstCoords[1], m_dstCoords[2], m_dstCoords[3], m_dstCoords[4], m_dstCoords[5], m_dstCoords[6], m_dstCoords[7], m_srcCoords[0], m_srcCoords[1], m_srcCoords[2], m_srcCoords[3], m_srcCoords[4], m_srcCoords[5], m_srcCoords[6], m_srcCoords[7]);
            WarpPerspective warp = new WarpPerspective(transform);
            // Parameterblock
            ParameterBlock pb = new ParameterBlock();
            pb.removeSources();
            pb.removeParameters();
            pb.addSource(m_srcImage);
            pb.add(warp);
            pb.add(m_interpolation);
            m_dstImage = JAI.create("warp", pb);
        } else {
            m_dstImage = m_srcImage;
        }
    }

    /**
     * liefert die Eckkoordinate für das transformieren, wichtig für
     * Berechnung; brauchbar für die Darstellung<BR>
     * Parameter i gibt die Eckennummer an: 0 - links unten; 1 - rechts unten
     * 2 - rechts oben; 3 - links oben
     *
     * @param i Eckenummer
     * @return liefert die Punktkoordinate
     */
    public Point getCorner(int i) {
        Point point = new Point();
        switch (i) {
            case 0: // links unten
                point.x = 0;
                point.y = m_height;
                break;

            case 1: // rechts unten
                point.x = m_width;
                point.y = m_height;
                break;

            case 2: // rechts oben
                point.x = m_width;
                point.y = 0;
                break;

            case 3: // links oben
                point.x = 0;
                point.y = 0;
                break;
        }
        return point;
    }

    /**
     * löscht alle Punkte
     */
    public void deleteAllPoints() {
        m_numPoints = 0;
        updateWarp();
    }


    /**
     *
     * @param i   0 - Sourcepoint, 1- DestinationPoint
     * @param j   0 - 3 ; die 4 Punkte
     * @param x
     * @param y
     */
    private void setCoords(int i, int j, double x, double y) {
        if (i == 0) {
            m_srcCoords[2 * j] = x;
            m_srcCoords[2 * j + 1] = y;
        } else {
            m_dstCoords[2 * j] = x;
            m_dstCoords[2 * j + 1] = y;
        }
    }

    /**
     *
     * @param dst
     */
    public void setDstCoords(double[] dst){
        m_dstCoords = dst;
        m_width = (int)Math.round(m_dstCoords[2]);
        m_height =(int)Math.round(m_dstCoords[3]);
    }

}
