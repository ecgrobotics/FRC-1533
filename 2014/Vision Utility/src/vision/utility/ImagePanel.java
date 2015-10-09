package vision.utility;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.*;
import javax.swing.*;
import java.awt.Dimension;
/**
 *
 * @author Programming
 */
public class ImagePanel extends JPanel {
    Image image;
    double hmin, hmax, smin, smax, vmin, vmax;
    
    public ImagePanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        hmin = smin = vmin = 0;
        hmax = smax = vmax = 255;
        
    }
    
    @Override
    public void paint(Graphics g) {
        if (image != null) {
            int imageWidth = image.getWidth(this);
            int imageHeight = image.getHeight(this);
            int w = getWidth();
            int h = getHeight();
            if (imageWidth > imageHeight) {
                h = (int)((double)imageHeight / imageWidth * getWidth());
            } else {
                w = (int)((double)imageWidth / imageHeight * getHeight());
            }
            g.drawImage(image, (getWidth()-w)/2, (getHeight()-h)/2, w, h, null);
        }
    }
    
    public void setImage(Image image) {
        this.image = image;
    }
    
    public void setHueMin(double hmin) {
        this.hmin = hmin;
        repaint();
    }
    
    public void setHueMax(double hmax) {
        this.hmax = hmax;
    }
    
    public void setSatMin(double smin) {
        this.smin = smin;
    }
    
    public void setSatMax(double smax) {
        this.smax = smax;
    }
    
    public void setValMin(double vmin) {
        this.vmin = vmin;
    }
    
    public void setValMax(double vmax) {
        this.vmax = vmax;
    }
    
}
