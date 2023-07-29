// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP102 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP-102-112 - 2021T1, Assignment 8
 * Name: Ella Wipatene
 * Username: wipateella
 * ID: 300558005
 */

import ecs100.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.awt.Color;

/**
 * This program reads waveform data from a file and displays it
 * The program will also do some analysis on the data
 * The user can also edit the data - deleting, duplicating, and adding 
 *
 * The methods you are to complete all focus on the ArrayList of data.
 * It is related to assignment 3 which analysed temperature levels
 *
 * CORE
 *  display:            displays the waveform.
 *  read:               reads numbers into an ArrayList.
 *  showSpread:         displays the maximum and minimum values of the waveform.
 *  increaseRegion:     increases all the values in the selected region by 10%.
 *  decreaseRegion:     decreases all the values in the selected region by 10%.
 *  doubleFrequency:    removes every second value from the waveform.
 *
 * COMPLETION
 *  highlightPeaks:     puts small green circles around all the peaks in the waveform.
 *  displayDistortion:  shows in red the distorted part of the signal.
 *  deleteRegion:       deletes the selected region of the waveform

 * CHALLENGE
 *  duplicateRegion:    duplicates the selected region of the waveform
 *  displayEnvelope:    displays the envelope.
 *  save:               saves the current waveform values into a file.
 *  ....                allows more editing
 *                       
 */

public class WaveformAnalyser{

    // Constants: 
    public static final int ZERO_LINE = 300;    // dimensions of the graph for the display method
    public static final int GRAPH_LEFT = 10;
    public static final int GRAPH_WIDTH = 800;
    public static final int GRAPH_RIGHT = GRAPH_LEFT + GRAPH_WIDTH;

    public static final double THRESHOLD = 200;  // threshold for the distortion level
    public static final int CIRCLE_SIZE = 10;    // size of the circles for the highlightPeaks method

    // Fields 
    private ArrayList<Double> waveform;   // the field to hold the ArrayList of values

    private int regionStart = 0; // The index of the first value in the selected region
    private int regionEnd;       // The index one past the last value in the selected region

    /**
     * Set up the user interface
     */
    public void setupGUI(){
        UI.setMouseListener(this::doMouse);   
        //core
        UI.addButton("Display", this::display);
        UI.addButton("Read Data", this::read);
        UI.addButton("Show Spread", this::showSpread);
        UI.addButton("Increase region", this::increaseRegion);
        UI.addButton("Decrease region", this::decreaseRegion);
        UI.addButton("Double frequency", this::doubleFrequency);
        //completion
        UI.addButton("Peaks", this::highlightPeaks);
        UI.addButton("Distortion", this::displayDistortion);
        UI.addButton("Delete", this::deleteRegion);
        //challenge
        UI.addButton("Duplicate", this::duplicateRegion);
        UI.addButton("Envelope", this::displayEnvelope);
        UI.addButton("Save", this::save);

        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(900, 650);
    }

    /**
     * [CORE]
     * Displays the waveform as a line graph,
     * Draw the axes
     * Plots a line graph of all the points with a blue line between
     *  each pair of adjacent points
     * The n'th value in waveform is at
     *    x-position is GRAPH_LEFT + n
     *    y-position is ZERO_LINE - the value
     * Don't worry if the data goes past the end the window
     */
    public void display(){
        if (this.waveform == null){ //there is no data to display so it shows the standard waveform
            this.waveform = new ArrayList<Double>();
            double freq = 0; 
            boolean increasing = true; 
            for (int i = 0; i < 800; i++){
                if (increasing == true){
                    freq = freq + 12; 
                    if (freq == 300){
                        increasing = false; 
                    }
                } else{
                    freq = freq - 12; 
                    if (freq == -300){
                        increasing = true; 
                    }
                }
                waveform.add(freq); 
            }
            return;
        }
        UI.clearGraphics();

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_RIGHT, ZERO_LINE); 

        // plot points: blue line between each pair of values
        /*# YOUR CODE HERE */
        UI.setColor(Color.blue); 
        for (int i = 0; i < waveform.size() - 1; i++){
            UI.drawLine(GRAPH_LEFT + i, ZERO_LINE - waveform.get(i), GRAPH_LEFT + i + 1, ZERO_LINE - waveform.get(i+1)); 
        }

        this.displayRegion();  // Displays the selected region, if any
    }

    /**
     * [CORE]
     * Clears the panes, 
     * Asks user for a waveform file (eg waveform1.txt)
     * The files consist of a sequence of numbers.
     * Creates an ArrayList stored in the waveform field, then
     * Reads data from the file into the ArrayList
     * calls display.
     */
    public void read(){
        UI.clearPanes();
        String fname = UIFileChooser.open();
        this.waveform = new ArrayList<Double>();   // create an empty list in the waveform field
        /*# YOUR CODE HERE */
        try{
            List<String> allLines = Files.readAllLines(Path.of(fname));
            for (String line: allLines){
                Scanner scan = new Scanner(line);
                waveform.add(scan.nextDouble()); 
            }
        } catch (IOException e){UI.println("File Failure:" + e);}
        
        
        UI.printMessage("Read " + this.waveform.size() + " data points from " + fname);

        this.regionStart = 0;
        this.regionEnd = this.waveform.size();
        this.display();
    }

    /**
     * Displays the selected region by a red line on the axis
     */
    public void displayRegion(){
        UI.setColor(Color.red);
        UI.setLineWidth(3);
        UI.drawLine(GRAPH_LEFT+this.regionStart, ZERO_LINE, GRAPH_LEFT+this.regionEnd-1, ZERO_LINE);
        UI.setLineWidth(1);
    }

    /**
     * [CORE]
     * The spread is the difference between the maximum and minimum values of the waveform.
     * Finds the maximum and minimum values of the waveform, then
     * Displays the spread by drawing two horizontal lines on top of the waveform: 
     *   one green line for the maximum value, and
     *   one red line for the minimum value.
     */
    public void showSpread() {
        if (this.waveform == null){ //there is no data to display
            UI.println("No waveform to display");
            return;
        }
        this.display();
        
        double max = Double.NEGATIVE_INFINITY; 
        double min = Double.POSITIVE_INFINITY; 
        for (Double w: waveform){
            if (w > max){
                max = w; 
            }
            if (w < min){
                min = w; 
            }
        }
        
        UI.setColor(Color.red);
        UI.drawLine(0, ZERO_LINE - max, 800, ZERO_LINE - max); 
        UI.setColor(Color.green);
        UI.drawLine(0, ZERO_LINE - min, 800, ZERO_LINE - min); 
    }

    /**
     * [CORE]
     * Increases the values in the selected region of the waveform by 10%.
     * (The selected region is initially the whole waveform, but the user can drag the
     *  mouse over part of the graph to select a smaller region).
     * The selected region goes from the index in the regionStart field to the index
     *  in the regionEnd field.
     */
    public void increaseRegion() {
        if (this.waveform == null){ //there is no waveform to process
            UI.println("No waveform");
            return;
        }

        for (int i = 0; i < waveform.size(); i++){
            if (i >= regionStart && i <= regionEnd){
                double increased_num = waveform.get(i) * 1.1; 
                waveform.set(i, increased_num); 
            } 
        }

        this.display();
    }

    /**
     * [CORE]
     * Decreases the values in the selected region of the waveform by 10%.
     * (The selected region is initially the whole waveform, but the user can drag the
     *  mouse over part of the graph to select a smaller region).
     * The selected region goes from the index in the regionStart field to the index
     *  in the regionEnd field.
     */
    public void decreaseRegion() {
        if (this.waveform == null){ //there is no waveform to process
            UI.println("No waveform");
            return;
        }
        
        for (int i = 0; i < waveform.size(); i++){
            if (i >= regionStart && i <= regionEnd){
                double decreased_num = waveform.get(i) * 0.9; 
                waveform.set(i, decreased_num); 
            } 
        }

        this.display();
    }

    /**
     * [CORE]
     * Double the frequency of the waveform by removing every second value in the list.
     * Resets the selected region to the whole waveform
     */
    public void doubleFrequency() {
        if (this.waveform == null){ //there is no waveform to process
            UI.println("No waveform");
            return;
        }
        
        for (int i = 0; i < waveform.size(); i++){
            waveform.remove(i); 
        }

        this.display();
    }

    /**
     * [COMPLETION]
     * Plots the peaks with small green circles. 
     *    A peak is defined as a value that is greater than or equal to both its
     *    neighbouring values.
     * Note the size of the circle is in the constant CIRCLE_SIZE
     */
    public void highlightPeaks() {
        this.display();     //use display if displayDistortion isn't complete
        UI.setColor(Color.green); 
        for (int i = 0; i < waveform.size() - 1; i++){
            if (i == 0){
                continue;
            }else if (waveform.get(i) >= waveform.get(i-1) && waveform.get(i) >= waveform.get(i+1)){
                UI.drawOval(GRAPH_LEFT + i - CIRCLE_SIZE/2, ZERO_LINE - waveform.get(i) - CIRCLE_SIZE/2, CIRCLE_SIZE, CIRCLE_SIZE);
            }
        }
    }

    /**
     * [COMPLETION]  [Fancy version of display]
     * Display the waveform as a line graph. 
     * Draw a line between each pair of adjacent points
     *   * If neither of the points is distorted, the line is BLUE
     *   * If either of the two end points is distorted, the line is RED
     * Draw the horizontal lines representing the value zero and thresholds values.
     * Uses THRESHOLD to determine distorted values.
     * Uses GRAPH_LEFT and ZERO_LINE for the dimensions and positions of the graph.
     * [Hint] You may find Math.abs(int a) useful for this method.
     * You may assume that all the values are between -250 and +250.
     */
    public void displayDistortion() {
        if (this.waveform == null){ //there is no data to display
            UI.println("No waveform to display");
            return;
        }
        UI.clearGraphics();

        // draw zero axis
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + this.waveform.size() , ZERO_LINE); 

        // draw thresholds
        UI.setColor(Color.blue); 
        for (int i = 0; i < waveform.size() - 1; i++){
            if (i == 0){
                continue; 
            } else if (Math.abs(waveform.get(i)) > 250 || Math.abs(waveform.get(i + 1)) > 250){
                UI.setColor(Color.red); 
                UI.drawLine(GRAPH_LEFT + i, ZERO_LINE - waveform.get(i), GRAPH_LEFT + i + 1, ZERO_LINE - waveform.get(i+1));
            } else{
                UI.setColor(Color.blue); 
                UI.drawLine(GRAPH_LEFT + i, ZERO_LINE - waveform.get(i), GRAPH_LEFT + i + 1, ZERO_LINE - waveform.get(i+1));
            }
        }
        
        UI.setColor(Color.green); 
        UI.drawLine(GRAPH_LEFT, ZERO_LINE - 250, GRAPH_LEFT + this.waveform.size() , ZERO_LINE - 250); 
        UI.drawLine(GRAPH_LEFT, ZERO_LINE + 250, GRAPH_LEFT + this.waveform.size() , ZERO_LINE + 250); 
        
        this.displayRegion();
    }

    /**
     * [COMPLETION]
     * Removes the selected region from the waveform
     * selection should be reset to be the whole waveform
     * redisplays the waveform
     */
    public void deleteRegion(){
        for (int i = 0; i < waveform.size(); i++){
            if (i >= regionStart && i <= regionEnd){
                waveform.remove(regionStart);
            } 
        }        

        this.display();
    }

    /**
     * [CHALLENGE]
     * If there is a selected region, then add a copy of that section to the waveform,
     * immediately following the selected region
     * selection should be reset to be the whole waveform
     * redisplay the waveform
     */
    public void duplicateRegion(){
        /*# YOUR CODE HERE */
        ArrayList<Double> waveform_copy = new ArrayList<Double>(); 
        ArrayList<Double> waveform_region_copy = new ArrayList<Double>(); 
        for (int i = 0; i < waveform.size(); i++){
            waveform_copy.add(waveform.get(i)); // making a copy of the wave form arraylist
            if (i >= regionStart && i <= regionEnd){
                waveform_region_copy.add(waveform.get(i)); // making a copy of the selected region
            } 
        } 
        
        waveform.clear(); 
        for (int i = 0; i < waveform_copy.size(); i++){
            waveform.add(waveform_copy.get(i)); 
            if (i == regionEnd){
                for (int x = 0; x < waveform_region_copy.size(); x++){
                    waveform.add(waveform_region_copy.get(x)); 
                }
            }
        }

        this.display();
    }

    /**
     * [CHALLENGE]
     * Displays the envelope (upper and lower) with GREEN lines connecting all the peaks.
     *    A peak is defined as a point that is greater than or equal to *both* neighbouring points.
     */
    public void displayEnvelope(){
        if (this.waveform == null){ //there is no data to display
            UI.println("No waveform to display");
            return;
        }
        this.display();  // display the waveform,

        /*# YOUR CODE HERE */
        ArrayList<Double> maximums = new ArrayList<Double>(); 
        ArrayList<Integer> max_index = new ArrayList<Integer>(); 
        ArrayList<Double> minimums = new ArrayList<Double>(); 
        ArrayList<Integer> min_index = new ArrayList<Integer>(); 
        for (int i = 0; i < waveform.size() - 1; i++){
            if (i == 0){
                maximums.add(waveform.get(i)); 
                max_index.add(i); 
                minimums.add(waveform.get(i)); 
                min_index.add(i); 
            }else if (waveform.get(i) >= waveform.get(i-1) && waveform.get(i) >= waveform.get(i+1)){ // maximums
                maximums.add(waveform.get(i)); 
                max_index.add(i); 
            }else if (waveform.get(i) <= waveform.get(i-1) && waveform.get(i) <= waveform.get(i+1)){
                minimums.add(waveform.get(i)); 
                min_index.add(i); 
            }
        }
        
        UI.setColor(Color.green); 
        for (int i = 0; i < maximums.size()-1; i++){
            UI.drawLine(GRAPH_LEFT + max_index.get(i), ZERO_LINE - maximums.get(i), GRAPH_LEFT + max_index.get(i+1) + 1, ZERO_LINE - maximums.get(i+1));
        }
        for (int i = 0; i < minimums.size()-1; i++){
            UI.drawLine(GRAPH_LEFT + min_index.get(i), ZERO_LINE - minimums.get(i), GRAPH_LEFT + min_index.get(i+1) + 1, ZERO_LINE - minimums.get(i+1));
        }
    }

    /**
     * [CHALLENGE]
     * Saves the current waveform values into a file
     */
    public void save(){
        String filename = UIFileChooser.save("Filename to save to"); 
        try{
            PrintStream outfile = new PrintStream(filename); 
            for (Double w: waveform){
                outfile.println(w); 
            }
            outfile.close();
        }catch(IOException e){UI.println("File Failure:" + e);}
    }

    /**
     * Lets user select a region of the waveform with the mouse
     * and deletes that section of the waveform.
     */
    public void doMouse(String action, double x, double y){
        int index = (int)x-GRAPH_LEFT;
        if (action.equals("pressed")){
            this.regionStart = Math.max(index, 0);
        }
        else if (action.equals("released")){
            if (index < this.regionStart){
                this.regionEnd = this.regionStart;
                this.regionStart = Math.max(index,this.waveform.size());
            }
            else {
                this.regionEnd = Math.min(index,this.waveform.size());
            }
            this.display();
        }

    }

    /**
     * Make a "triangular" waveform file for testing the other methods
     */
    public void makeTriangleWaveForm(){
        this.waveform = new ArrayList<Double>();
        for (int cycle=0; cycle<10; cycle++){
            for (int i=0; i<15; i++){this.waveform.add(i*18.0);}
            for (int i=15; i>-15; i--){this.waveform.add(i*18.0);}
            for (int i=-15; i<0; i++){this.waveform.add(i*18.0);}
        }
        this.regionStart = 0;
        this.regionEnd = this.waveform.size();
    }

    public static void main(String[] args){
        WaveformAnalyser wav = new WaveformAnalyser();
        wav.setupGUI();
        wav.makeTriangleWaveForm();
    }
}
