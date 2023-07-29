// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP102 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP-102-112 - 2021T1, Assignment 8
 * Name: Ella Wipatene
 * Username: Wipateella
 * ID: 300558005
 */


// NOTES:
// probablility of them being a 'long' balloon
// making sure that they do not overlap


import ecs100.*;
import java.util.*;
import java.awt.Color;

/** Program for a simple game in which the player has to blow up balloons
 *   on the screen.
 *  The game starts with a collection of randomly placed small balloons
 *    (coloured circles) on the graphics pane.
 *  The player then clicks on balloons to blow them up by a small amount
 *   (randomly increases the radius between 4 and 10 pixels).
 *  If an expanded balloon touches another balloon, then they both "burst" and go grey.
 *  The goal is to get the largest score. The score is the total of the
 *   sizes (areas) of all the active balloons, minus the total size of all
 *   the burst balloons.
 *  At each step, the current score is recalculated and displayed,
 *   along with the highest score that the player has achieved so far.
 *  At any time, the player may choose to stop and "lock in" their score.
 *
 *  The BalloonGame class has a field containing an Arraylist of Balloon objects
 *   to represent the current set of Balloons on the screen.
 *  It has a field to hold the highest score.
 *
 *  The New Game button should start a new game.
 *  The Lock Score button should finish the current game, updating the highest score
 *
 *  Clicking (ie, releasing) the mouse on the graphics pane is the main "action"
 *  of the game. The action should do the following
 *    Find out if the mouse was clicked on top of any balloon.
 *    If so,
 *      Make the balloon a bit larger and redraw it.
 *      Check whether the balloon is touching any other balloon.
 *      If so
 *         burst the two balloons (which will make them go grey)
 *         redraw the burst Balloons
 *      Recalculate and redisplay the score
 *   If all the balloons are gone, the game is over.
 *    
 *   To start a game, the program should
 *       Clear the graphics pane
 *       Initialise the score information
 *       Make a new list of Balloons at random positions
 *       Print a message 
 *
 *   If the game is over, the program should
 *      Update the highest score if the current score is better,
 *      Print a message reporting the scores,
 *     
 *   There are lots of ways of designing the program. It is not a good idea
 *   to try to put everything into one big method.
 *        
 *  Note that the Balloon class is written for you. Make sure that you know
 *   all its methods - no marks for redoing code that is given to you.
 *    
 */
public class BalloonGame {
    private static final int MAX_BALLOONS = 20;

    private ArrayList <Balloon> balloons = new ArrayList<Balloon>(); // The list of balloons
    // (initially empty)

    // Fields
    /*# YOUR CODE HERE */
    private double score; 
    private double maxScore;    
    private int num_balloons = 10;  
    private boolean playing = true; // if the game is playing
    
    public void setupGUI(){
        UI.setWindowSize(600,600);
        /*# YOUR CODE HERE */
        UI.setMouseListener(this::doMouse); 
        UI.addButton("New Game", this::restartGame); 
        UI.addButton("Lock score", this::lockScore);
        UI.addSlider("Number of Balloons", 2, MAX_BALLOONS, 10, this::setNumBalloons); 
        UI.setDivider(0.0);
    }   

    /** Start the game:
     *  Clear the graphics pane
     *  Initialise the score information 
     *  Make a new set of Balloons at random positions
     */
    public void restartGame(){
        /*# YOUR CODE HERE */
        UI.clearGraphics(); 
        balloons.clear(); 

        this.score = 0; 
        for (int i = 0; i < num_balloons; i++){
            double x = (Math.random() * 500 + 50); 
            double y = (Math.random() * 500 + 50);
            for (Balloon b: balloons){
                boolean invalid_location = b.on(x,y); 
                if (invalid_location == true){
                    x = (Math.random() * 500 + 50); 
                    y = (Math.random() * 500 + 50);
                }
            }
            balloons.add(new Balloon(x,y)); 
        }
        for (Balloon b: balloons){
            b.draw();
        }
    }
    
    public void lockScore(){
        if (score > maxScore){
            maxScore = score; 
        }
    }
    
    public void setNumBalloons(double n){
        int x = (int) n;
        if (x % 2 != 0){ // to make sure that it is always a multiple of 2
            x = x + 1; 
        }
        this.num_balloons = x; 
    }

    /**
     * Main game action:
     *    Find the balloon at (x,y) if any,
     *  Expand it 
     *  Check whether it is touching another balloon,
     *  If so, burst both balloons.
     *  Redraw the balloon (and the other balloon if it was touching)
     *  Calculate and Report the score. (Hint: use UI.printMessage(...) to report)
     *  If there are no active balloons left, end the game.
     */
    public void  doMouse(String action, double x, double y){
        /*# YOUR CODE HERE */
        if (action.equals("released")){
            //UI.clearGraphics();
            playing = false; 
            double area = 0; 
            for (Balloon b: balloons){
                boolean on_balloon = b.on(x,y); // checks if the mouse has clicked on the balloon 
                if (on_balloon == true){
                    b.expand(); 
                    b.draw();
                }
                for (Balloon b2: balloons){ //checks if every balloon is touching another balloon
                    boolean touching = b.isTouching(b2); 
                    if (b != b2 && touching == true){
                        b.burst(); 
                        b2.burst(); 
                        b.draw(); 
                        b2.draw(); 
                    }
                }
                if (b.active == true){ // will only redraw if it is active
                    playing = true; // to check if all of the balloons have been popped 
                    area = area + b.size();
                } else{
                    area = area - b.size(); 
                }
            }
            score = area; 
            UI.println(playing); 
            if (playing == false){
                lockScore(); 
                UI.setColor(Color.black); 
                UI.drawString("GAME OVER", 200, 200); 
                UI.sleep(500); 
                UI.clearGraphics(); 
            }
            UI.printMessage("Score: " + score + "     Highest Score: " + maxScore);
        }
    }

    // Possible additional helper methods:
    //    public Balloon findBalloonOn(double x, double y){...
    //       for finding the (active) balloon that the point (x,y) is on, if any

    //    public Balloon findTouching(Balloon balloon){...
    //       for finding another active balloon touching the given one.

    //    public int calculateScore(){...
    //       for calculating the current score

    //    public boolean allBalloonsBurst(){...
    //       to find out if all the balloons have been burst.

    //    public void endGame(){...
    //        to update the highestScore and print a message

    /*# YOUR CODE HERE */

    public static void main(String[] arguments){
        BalloonGame bg = new BalloonGame();
        bg.setupGUI();
    }

}
