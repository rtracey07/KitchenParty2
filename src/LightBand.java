package sample;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** Robert Tracey - Labrador Creative Arts Festival 2015
 *  Created on Nov. 19th, 2015  */

/** LightBand Rectangle acts as Piano player paddle for videogame.  */

public class LightBand {

    private Rectangle band;                             //Geometry.
    private Rectangle2D boundary;                       //Screen Resolution-defined bounds.
    private KeyCode[] keys;                             //Associated Keyboard Keys.
    private int numPanels;                              //Number of divisions for the paddle across the screen.
    private KeyValue collapse, expand, standardPos;     //Animation Values.
    private KeyValue[] newPos;                          //Translation Animation Values.

    private KeyFrame toStartSize;                       //Expansion Starting Frame.
    private KeyFrame[] toDemoSize;                      //Translation Size and location Frames.

    private Timeline restoreDemo;                       //Contraction Starting Frame.
    private Timeline[] moveDemo;                        //Return Translation Size and location Frames.


    /** Constructor  */
    public LightBand(Rectangle2D boundary, int yPos, Color color, KeyCode[] keys){

        this.keys = keys;
        this.boundary = boundary;
        numPanels = keys.length;

        band = new Rectangle(boundary.getWidth(), 100, color);
        band.setX(0);
        band.setY(yPos);

        newPos = new KeyValue[keys.length];
        moveDemo = new Timeline[keys.length];
        toDemoSize = new KeyFrame[keys.length];

        band.setCache(true);
        band.setCacheHint(CacheHint.SPEED);
        setFrames();
    }

    /** Set Animation Properties for paddle.  */
    private void setFrames(){
        collapse = new KeyValue(band.widthProperty(), boundary.getWidth()/numPanels);
        expand = new KeyValue(band.widthProperty(), boundary.getWidth());
        standardPos = new KeyValue(band.xProperty(), 0);

        toStartSize = new KeyFrame(new Duration(500), expand, standardPos);

        restoreDemo = new Timeline();
        restoreDemo.getKeyFrames().add(toStartSize);

        for(int i=0; i<keys.length; i++){
            newPos[i] = new KeyValue(band.xProperty(), (boundary.getWidth()/numPanels)*i);
            toDemoSize[i] = new KeyFrame(new Duration(300), newPos[i], collapse);
            moveDemo[i] = new Timeline();
            moveDemo[i].getKeyFrames().add(toDemoSize[i]);
        }
    }

    /** Get Geometry.  */
    public Rectangle getBand(){ return band; }

    /** Check Piano Key Press.  */
    public int isKeyPlayed(KeyCode event){
        for(int i=0; i<keys.length; i++)
            if(keys[i] == event)
                return i;

        return -1;
    }

    /** Stop Expansion Restore.  */
    public void stopRestore(){
        restoreDemo.stop();
    }

    /** Play collapse Animation.  */
    public void playCollapse(int index){
        if(index < moveDemo.length)
            moveDemo[index].play();
    }
}