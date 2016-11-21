package com.blucalculator.heightmapvisualiser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 *
 * @author Matthew
 */
public class HeightmapVisualiser extends JFrame implements ChangeListener {
    Color ColorDefaultGray = new Color(238,238,238);
    /*
    HV->controls
      |>drawingBoard
      ->statusBar->statusLabel
                 ->statusText
    
    */
    private JPanel statusBar;
    private JLabel statusLabel;
    private JLabel statusText;
    private JPanel west;//TODO: better name
    private JPanel controls;
    private JLabel guiRowsLabel;
    private JLabel guiColsLabel;
    private JSpinner guiRows;
    private JSpinner guiCols;
    private JButton clearValues;
    private DataPanel dataPanel;
    private DrawingPanel drawingPanel;
    
    public class Point {
        public int _row;
        public int _column;
        public int _value;

        public Point(int row, int column, int value) {
            _row = row;
            _column = column;
            _value = value;
        }
    }
    private class DrawingPanel extends JPanel {
        private int[][] _hm;
        private int _rows;
        private int _cols;
        private int _spacing;
        private int _slant;
        DrawingPanel() {
            super();
            _rows=5;
            _cols=5;
            _spacing=40;
            _slant=8;
            _hm=new int[_rows][_cols];
            for (int y=0; y<_rows; y++) {
                for (int x=0; x<_cols; x++) {
                    _hm[y][x]=0;
                }
            }
        }
        @Override
        public void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            //Left to right lines
            for (int y=0; y<_rows; y++) {
                for (int x=0; x<(_cols-1); x++) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(_spacing*(x+1)+_slant*y, _spacing*(y+1),
                               _spacing*(x+2)+_slant*y, _spacing*(y+1));
                    g.setColor(Color.BLACK);
                    g.drawLine(_spacing*(x+1)+_slant*y, _spacing*(y+1)-_hm[y][x],
                               _spacing*(x+2)+_slant*y, _spacing*(y+1)-_hm[y][x+1]);
                }
            }
            
            //top to bottom lines
            for (int x=0; x<(_cols); x++) {
                for (int y=0; y<_rows-1; y++) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(_spacing*(x+1)+_slant*y, _spacing*(y+1),
                               _spacing*(x+1)+_slant*(y+1), _spacing*(y+2));
                    g.setColor(Color.BLACK);
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
            repaint();
        }
        public void setValue(Point p) {
            _hm[p._row][p._column]=p._value;
            repaint();
        }

        public void setSpacing(int spacing) { _spacing = spacing; }
        public void setSlant(int slant) { _slant = slant; }
    }
    private class DataPanel extends JPanel{
        private JSpinner[][] _values;
        private JLabel[][] northSouths;
        private JLabel[][] westEasts;
        private JLabel[][] diags;
        
        int _rows;
        int _cols;
        private JLabel[] _slopes;
        
        public DataPanel(int rows, int cols, ChangeListener listener) {
            super();
            //brand new panel
            _rows=rows;
            _cols=cols;
            _values = new JSpinner[rows][cols];
            for (int i=0; i<rows; i++) {
                for (int j=0; j<cols; j++) {
                    _values[i][j] = new JSpinner(new SpinnerNumberModel(0,-180,Integer.MAX_VALUE,1));
                    _values[i][j].addChangeListener(listener);
                }
            }
            initDataPanel();
        }
        private void initDataPanel() {
            //generate north->south labels
            northSouths = new JLabel[_rows-1][_cols];
            for (int i=0; i<_rows-1; i++) {
                for (int j=0; j<_cols; j++) {
                    northSouths[i][j]=new JLabel();
                    northSouths[i][j].setOpaque(true);
                    checkNSSlope((int)_values[i][j].getValue(),(int)_values[i+1][j].getValue(),northSouths[i][j]);
                }
            }
            
            //generate west->east labels
            westEasts = new JLabel[_rows][_cols-1];
            for (int i=0; i<_rows; i++) {
                for (int j=0; j<_cols-1; j++) {
                    westEasts[i][j]=new JLabel();
                    westEasts[i][j].setOpaque(true);
                    checkWESlope((int)_values[i][j].getValue(),(int)_values[i][j+1].getValue(), westEasts[i][j]);
                }
            }
            
            //generate diagonal labels
            diags = new JLabel[_rows-1][_cols-1];
            int nw, ne, sw, se;
            for (int i=0; i<_rows-1; i++) {
                for (int j=0; j<_cols-1; j++) {
                    nw=(int)_values[i][j].getValue();
                    ne=(int)_values[i][j+1].getValue();
                    sw=(int)_values[i+1][j].getValue();
                    se=(int)_values[i+1][j+1].getValue();
                    diags[i][j]=new JLabel();
                    checkFlat(nw,ne,sw,se,diags[i][j]);
                    diags[i][j].setOpaque(true);
                }
            }
            
            int gridRows=_rows*2-1;
            int gridCols=_cols*2-1;
            this.setLayout(new GridLayout(gridRows, gridCols));
            //assign things to (hopefully) the right spots
            for (int i=0; i<gridRows; i++) {
                for (int j=0; j<gridCols; j++) {
                   if (i%2==0) {
                       if (j%2==0) {//spinners
                           this.add(_values[i/2][j/2]);
                       } else {//west->east slopes
                           this.add(westEasts[i/2][j/2]);
                       }
                   } else {
                       if (j%2==0) {//north->south slopes
                           this.add(northSouths[i/2][j/2]);
                       } else {//diagonals/squares/things
                           this.add(diags[i/2][j/2]);
                       }
                   }
                }
            }
        }
        private void checkNSSlope(int north, int south, JLabel label) {
            if (north==south) {
                label.setText("Flat");
                label.setBackground(Color.green);
            } else if (north>south) {
                label.setText(Integer.toString(north-south)+" North");
                label.setBackground(ColorDefaultGray);
            } else {
                label.setText(Integer.toString(south-north)+" South");
                label.setBackground(ColorDefaultGray);
            }
        }
        private void checkWESlope(int west, int east, JLabel label) {
            if (west==east) {
                label.setText("Flat");
                label.setBackground(Color.green);
            } else if (west>east) {
                label.setText(Integer.toString(west-east)+" West");
                label.setBackground(ColorDefaultGray);
            } else {
                label.setText(Integer.toString(east-west)+" East");
                label.setBackground(ColorDefaultGray);
            }
        }
        private void checkFlat(int nw, int ne, int sw, int se, JLabel label) {
            if (nw==ne && nw == sw && nw== se) {
                label.setText("Flat");
                label.setBackground(Color.green);
            } else {
                label.setText("");
                label.setBackground(ColorDefaultGray);
            }
        }
        public int[][] getValues() {
            int[][] values=new int[_rows][_cols];
            for (int i=0;i<_rows;i++) {
                for (int j=0;j<_cols;j++) {
                    values[i][j]=(int)_values[i][j].getValue();
                }
            }
            return values;
        }
        public Point stateChanged(ChangeEvent event) {
            //ugly hack, I apologise to anyone who reads this code
            for (int i=0; i<_rows; i++) {
                for (int j=0; j<_cols; j++) {
                    if (_values[i][j]==event.getSource()) {
                        //DINGDINGDING we found the right spinner!
                        int v=(int)_values[i][j].getValue();
                        int n, s, w, e, nw, ne, sw, se;//spinners in the 8 cardinal directions
                        n=s=w=e=nw=ne=sw=se=-200;//error value
                        if (i>0) {
                            n=(int)_values[i-1][j].getValue();
                            checkNSSlope(n, v, northSouths[i-1][j]);
                            if (j>0) nw=(int)_values[i-1][j-1].getValue();
                            if (j<(_cols-1)) ne=(int)_values[i-1][j+1].getValue();
                        }
                        if (i<(_rows-1)) {
                            s=(int)_values[i+1][j].getValue();
                            checkNSSlope(v, s, northSouths[i][j]);
                            if (j>0) sw=(int)_values[i+1][j-1].getValue();
                            if (j<(_cols-1)) se=(int)_values[i+1][j+1].getValue();
                        }
                        if (j>0) {
                            w=(int)_values[i][j-1].getValue();
                            checkWESlope(w, v, westEasts[i][j-1]);
                        }
                        if (j<(_cols-1)) {
                            e=(int)_values[i][j+1].getValue();
                            checkWESlope(v, e, westEasts[i][j]);
                        }
                        if (i>0 && j>0) checkFlat(nw,n,w,v, diags[i-1][j-1]);//nw
                        if (i>0 && j<(_cols-1)) checkFlat(n,ne,v,e,diags[i-1][j]);
                        if (i<(_rows-1) && j>0) checkFlat(w,v,sw,s,diags[i][j-1]);
                        if (i<(_rows-1) && j<(_cols-1)) checkFlat(v,e,s,se,diags[i][j]);
                        
                        repaint();
                        return new Point(i, j, (int)_values[i][j].getValue());
                    }
                }
            }
            System.out.println("My ugly hack couldn't find the right spinner.");
            return null;
        }
    }
    /**
     * Creates the window for the Heightmap Visualiser
     */
    public HeightmapVisualiser() {
        initComponents();
        
    }
    
    private void initComponents() {
        statusBar = new JPanel();//container
        statusText = new JLabel();
        statusLabel = new JLabel();
        west = new JPanel();//container
        controls = new JPanel();
        guiRowsLabel = new JLabel("Rows:");
        guiColsLabel = new JLabel("Columns:");
        guiRows = new JSpinner(new SpinnerNumberModel(5,1,Integer.MAX_VALUE,1));
        guiCols = new JSpinner(new SpinnerNumberModel(5,1,Integer.MAX_VALUE,1));
        clearValues = new JButton("Clear");
        dataPanel = new DataPanel(5,5, this);
        drawingPanel = new DrawingPanel();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Height map viewer");
        
        //details
        statusText.setText("Loading - probably");
        statusLabel.setText("Status: ");
        
        //details 2 - the positioning
        controls.setPreferredSize(new Dimension(256, 32));
        west.setPreferredSize(new Dimension(600,600));
        drawingPanel.setPreferredSize(new Dimension(600, 600));
        drawingPanel.setMaximumSize(new Dimension(600, 600));
        drawingPanel.setMinimumSize(new Dimension(600, 600));
        statusBar.setPreferredSize(new Dimension(256, 32));
        
        
        //TODO: more action listeners
        guiRows.addChangeListener(this);
        guiCols.addChangeListener(this);
        
        //layouts
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.add(guiRowsLabel);
        controls.add(guiRows);
        controls.add(guiColsLabel);
        controls.add(guiCols);
        controls.add(clearValues);
        
        west.setLayout(new BorderLayout());
        west.add(controls,BorderLayout.NORTH);
        west.add(dataPanel,BorderLayout.CENTER);
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
        statusBar.add(statusLabel);
        statusBar.add(statusText);
        //getcontentpane already has a default layout of borderlayout
        getContentPane().add(west, BorderLayout.CENTER);
        getContentPane().add(drawingPanel, BorderLayout.EAST);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
                
        pack();
        statusText.setText("Loaded:");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource()==guiRows || e.getSource()==guiCols) {
            west.remove(dataPanel);
            int r,c;
            r=(int)guiRows.getValue();
            c=(int)guiCols.getValue();
            dataPanel = new DataPanel(r, c, this);
            west.add(dataPanel,BorderLayout.CENTER);
            west.revalidate();
            west.repaint();
            drawingPanel.updateMap(dataPanel.getValues(), r, c);
        } else {
            Point spinnerspot=dataPanel.stateChanged(e);
            drawingPanel.setValue(spinnerspot);
        }
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
