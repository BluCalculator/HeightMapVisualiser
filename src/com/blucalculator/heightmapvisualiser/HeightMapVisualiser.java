package com.blucalculator.heightmapvisualiser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
/**
 *
 * @author Matthew
 */
public class HeightmapVisualiser extends JFrame {
    /*
    HV->splitPane->controls
      |          ->drawingBoard
      ->statusBar->statusLabel
                 ->statusText
    
    */
    private JPanel controls;
    private VisualiserPanel drawingBoard;
    private JSplitPane splitPane;
    private JPanel statusBar;
    private JLabel statusLabel;
    private JLabel statusText;
    
    private class VisualiserPanel extends javax.swing.JPanel {
        private int[][] _hm;
        private int _rows;
        private int _cols;
        private int _spacing;
        private int _slant;
        VisualiserPanel() {
            super();
            _rows=5;
            _cols=5;
            _spacing=64;
            _slant=4;
            _hm=new int[_rows][_cols];
            for (int y=0; y<_rows; y++) {
                for (int x=0; x<_cols; x++) {
                    _hm[y][x]=0;
                }
            }
            _hm[1][1]=5;//hardcoded value assumes necessary size
            _hm[1][3]=5;//hardcoded value assumes necessary size
        }
        @Override
        public void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            //Left to right lines
            for (int y=0; y<_rows; y++) {
                for (int x=0; x<(_cols-1); x++) {
                    g.drawLine(_spacing*(x+1)+_slant*y, _spacing*(y+1)-_hm[y][x],
                               _spacing*(x+2)+_slant*y, _spacing*(y+1)-_hm[y][x+1]);
                }
            }
            
            //top to bottom lines
            for (int x=0; x<(_cols); x++) {
                for (int y=0; y<_rows-1; y++) {
                    g.drawLine(_spacing*(x+1)+_slant*y, _spacing*(y+1)-_hm[y][x],
                               _spacing*(x+1)+_slant*(y+1), _spacing*(y+2)-_hm[y+1][x]);
                }
            }
        }
        /**
         * Replace existing height map with first parameter
         * @param hm A height map as rectangular 2D array - int[rows][columns]
         * @param rows How many rows are in the height map
         * @param cols How many columns are in the height map 
         */
        public void updateMap(int[][] heightMap, int rows, int cols) {
            _hm=heightMap;
            _rows=rows;
            _cols=cols;
        }

        public void setSpacing(int spacing) { _spacing = spacing; }
        public void setSlant(int slant) { _slant = slant; }
    }
    
    /**
     * Creates the window for the Heightmap Visualiser
     */
    public HeightmapVisualiser() {
        initComponents();
        
    }
    
    private void initComponents() {
    /*  This started out as netbeans autogen, but the code has been assimilated.
        I will add its biological and technological distinctiveness to my own.
        It will culturally adapt to service me. Resistance is futile.
    */
        
        statusBar = new JPanel();
        statusText = new JLabel();
        statusLabel = new JLabel();
        controls = new JPanel();
        drawingBoard = new VisualiserPanel();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controls, drawingBoard);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        //details
        statusText.setText("Loading - probably");
        statusLabel.setText("Status: ");
        splitPane.setOneTouchExpandable(true);
        
        //details 2 - the positioning
        statusBar.setPreferredSize(new Dimension(256,40));
        splitPane.setPreferredSize(new Dimension(800, 600));
        splitPane.setDividerLocation(200);

        //TODO: action listeners
        
        //layouts
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
        statusBar.add(statusLabel);
        statusBar.add(statusText);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        //getcontentpane already has a default layout of borderlayout
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
                
        pack();
    }
    
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HeightmapVisualiser().setVisible(true);
            }
        });
    }
}
