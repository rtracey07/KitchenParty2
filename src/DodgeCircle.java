package sample;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.MotionBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

/** Robert Tracey - Labrador Creative Arts Festival 2015
 *  Created on Nov. 19th, 2015  */


/** DodgeCircle - A projectile fired from the Drum controller.  */
public class DodgeCircle{

    private Circle dodge;                                   //Geometry.
    private Rectangle2D boundary;                           //Screen Resolution-defined boundary.
    private KeyValue moveY, hitGrow, hitFade, colorDecay;   //Animation Values.

    private KeyFrame move, onHit;                           //Animation Frames.

    private Timeline moveDodge, hitDodge;                   //Animation Timelines.

    /** Constructor  */
    public DodgeCircle(int radius, Rectangle2D boundary, int posVar, Group root){

        this.boundary = boundary;
        dodge = new Circle(radius, Color.BLUE);
        dodge.setCenterX(boundary.getWidth()-150-((boundary.getWidth()/6)*posVar));
        dodge.setCenterY(-radius);
        dodge.setEffect(new BoxBlur(2,2,2));

        dodge.setCache(true);
        dodge.setCacheHint(CacheHint.SPEED);

        setFrames();

        root.getChildren().add(dodge);
    }

    /** Set Animation Frames  */
    private void setFrames(){

        moveY = new KeyValue(dodge.centerYProperty(), boundary.getHeight()+dodge.getRadius()+3);

        hitGrow = new KeyValue(dodge.radiusProperty(), dodge.getRadius()*10);
        hitFade = new KeyValue(dodge.opacityProperty(), 0);

        colorDecay = new KeyValue(dodge.fillProperty(), Color.RED);

        move = new KeyFrame(new Duration(500), moveY);
        onHit = new KeyFrame(new Duration(1000), hitFade, hitGrow, colorDecay);

        moveDodge = new Timeline();
        moveDodge.getKeyFrames().add(move);

        hitDodge = new Timeline();
        hitDodge.getKeyFrames().add(onHit);

    }

    /** Get Geometry */
    public Circle getDodge(){ return dodge; }

    /** Play Hit Animation  */
    public void isHit(){
        hitDodge.play();
    }

    /** Translate Projectile  */
    public void move(){
        moveDodge.play();
    }
}