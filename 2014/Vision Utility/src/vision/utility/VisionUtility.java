/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vision.utility;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Programming
 */
public class VisionUtility extends JPanel {
    ImagePanel imagePanel;
    Image image;
    File file;
    URL url;
    JFrame fileChooserFrame;
    JFileChooser fileChooser;
    JTextField textField;
    FileLoaderListener fileListener;
    JButton chooseFile, refresh;
    JFrame fileErrorFrame;
    JSlider hmin, hmax, smin, smax, vmin, vmax;
    JLabel hminLabel, hmaxLabel, sminLabel, smaxLabel, vminLabel, vmaxLabel;
    JPanel options;
    SliderListener slideListener;
    
    public VisionUtility() {
        imagePanel = new ImagePanel(500, 500);
        fileListener = new FileLoaderListener();
        textField = new JTextField(24);
        textField.addActionListener(fileListener);
        chooseFile = new JButton("Choose File...");
        chooseFile.addActionListener(fileListener);
        refresh = new JButton("Refresh Image");
        refresh.addActionListener(new RefreshListener());
        
        fileChooser = new JFileChooser();
        fileChooser.addActionListener(fileListener);
        fileChooserFrame = new JFrame();
        fileChooserFrame.add(fileChooser);
        fileChooserFrame.pack();
        fileChooserFrame.setAlwaysOnTop(true);
        
        fileErrorFrame = new JFrame();
        
        hmin = new JSlider(0, 255, 0);
        hmax = new JSlider(0, 255, 255);
        smin = new JSlider(0, 255, 0);
        smax = new JSlider(0, 255, 255);
        vmin = new JSlider(0, 255, 0);
        vmax = new JSlider(0, 255, 255);
        
        hminLabel = new JLabel("Hue min: ");
        hmaxLabel = new JLabel("Hue max: ");
        sminLabel = new JLabel("Sat min: ");
        smaxLabel = new JLabel("Sat max: ");
        vminLabel = new JLabel("Val min: ");
        vmaxLabel = new JLabel("Val max: ");
        
        slideListener = new SliderListener();
        hmin.addChangeListener(slideListener);
        hmax.addChangeListener(slideListener);
        smin.addChangeListener(slideListener);
        smax.addChangeListener(slideListener);
        vmin.addChangeListener(slideListener);
        vmax.addChangeListener(slideListener);
        
        options = new JPanel();
        options.setPreferredSize(new Dimension(275, 500));
        options.add(textField);
        options.add(chooseFile);
        options.add(refresh);
        options.add(hminLabel);
        options.add(hmin);
        options.add(hmaxLabel);
        options.add(hmax);
        options.add(sminLabel);
        options.add(smin);
        options.add(smaxLabel);
        options.add(smax);
        options.add(vminLabel);
        options.add(vmin);
        options.add(vmaxLabel);
        options.add(vmax);
        
        add(imagePanel);
        add(options);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Vision Utility");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new VisionUtility());
        frame.pack();
        frame.setVisible(true);
    }
    
    public void loadImage() {
        try {
            if (file != null) {
                image = ImageIO.read(file);
            } else if (url != null) {
                image = ImageIO.read(url);
            }
            imagePanel.setImage(image);
            repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(fileErrorFrame, 
                "Image not found: \n" + file.getPath(),
                "ERROR",
                JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    public void processImage() {
        
    }
    
    private class FileLoaderListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(chooseFile)) {
                fileChooserFrame.setVisible(true);
            } else if (e.getSource().equals(fileChooser)) {
                if (e.getActionCommand().equals("CancelSelection")) {
                    fileChooserFrame.setVisible(false);
                } else if (e.getActionCommand().equalsIgnoreCase("ApproveSelection")) {
                    fileChooserFrame.setVisible(false);
                    file = fileChooser.getSelectedFile();
                    url = null;
                    textField.setText(file.getPath());
                    loadImage();
                }
            } else if (e.getSource().equals(textField)) {
                String text = textField.getText();
                if (text.startsWith("http")) {
                    file = null;
                    try {
                        url = new URL(text);
                        loadImage();
                    } catch (MalformedURLException ex) {
                        JOptionPane.showMessageDialog(fileErrorFrame, 
                            "Invalid URL: \n" + text,
                            "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    url = null;
                    file = new File(text);
                    loadImage();
                }
            }
        }
        
    }
    
    private class RefreshListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText();
                if (text.startsWith("http")) {
                    file = null;
                    try {
                        url = new URL(text);
                        loadImage();
                    } catch (MalformedURLException ex) {
                        JOptionPane.showMessageDialog(fileErrorFrame, 
                            "Invalid URL: \n" + text,
                            "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    url = null;
                    file = new File(text);
                    loadImage();
                }
            }
            
    }
    
    private class SliderListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            if (source.equals(hmin)) {
                imagePanel.setHueMin(hmin.getValue());
                if (hmin.getValue() > hmax.getValue()) {
                    hmax.setValue(hmin.getValue());
                }
            } else if (source.equals(hmax)) {
                imagePanel.setHueMax(hmax.getValue());
                if (hmin.getValue() > hmax.getValue()) {
                    hmin.setValue(hmax.getValue());
                }
            } else if (source.equals(smin)) {
                imagePanel.setSatMin(smin.getValue());
                if (smin.getValue() > smax.getValue()) {
                    smax.setValue(smin.getValue());
                }
            } else if (source.equals(smax)) {
                imagePanel.setSatMin(smax.getValue());
                if (smin.getValue() > smax.getValue()) {
                    smin.setValue(smax.getValue());
                }
            } else if (source.equals(vmin)) {
                imagePanel.setValMin(vmin.getValue());
                if (vmin.getValue() > vmax.getValue()) {
                    vmax.setValue(vmin.getValue());
                }
            } else if (source.equals(vmax)) {
                imagePanel.setHueMin(vmax.getValue());
                if (vmin.getValue() > vmax.getValue()) {
                    vmin.setValue(vmax.getValue());
                }
            }
        }
        
    }
    
}
