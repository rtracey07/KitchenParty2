package sample;

/** Robert Tracey - Labrador Creative Arts Festival 2015
 *  Created on Nov. 19th, 2015  */

import javafx.animation.*;
import javafx.application.*;
import javafx.event.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.util.*;


/** Kitchen Party Demo Pt 2 - A visualization project using midi network communication
 *  and Capacitive touch sensors (mapped to keyboard) to trigger lighting, audio,
 *  and projection effects when different instruments (made out of kitchen appliances)
 *  were played.
 *
 *  Part 2 features a video game played using two of the musical instruments.
 *  Projectiles are fired by the drum and dodged by the piano.
 *
 *  Original project interfaced with QLab v3 and Mainstage 3 and QLC+ using midi triggering.
 *  Images were forced to a secondary 1024x768 wide-throw projector.
 *
 *  Piano keys: Q,W,E,R,T,Y
 *  Drum keys: U,I,O,P,[
 *
 *  NOTE: Corresponding QLab, Mainstage, and QLC+ files must be open for midi triggering to work.
 */

public class Main extends Application{

    private Rectangle2D boundary;   //Screen bounds
    private Group root;             //Root Group

    private Rectangle counter;      //Bar to display reload time.
    private LightBand player;       //Resizable player rectangle.

    //Player keys mapped to piano buttons.
    private KeyCode[] pianoKeys =
            {KeyCode.Q,KeyCode.W,KeyCode.E,KeyCode.R,KeyCode.T,KeyCode.Y};

    //Shooter keys mapped to drums.
    private KeyCode[] drumKeys =
            {KeyCode.P,KeyCode.U,KeyCode.OPEN_BRACKET,KeyCode.O,KeyCode.I};

    //Animation to resize counter bar.
    private KeyValue count;
    private KeyFrame count1;
    private Timeline counting;

    //Circle used as projectile. Moves UP-DOWN
    private DodgeCircle missile;

    //Loop-ending logic.
    private boolean isFired, isGameOver;

    //Displays "G A M E  O V E R"
    private Text gameOver, score;

    private int dodges;

    //MIDI message sends to QLAB and QLC+ Applications.
    private MidiPlayer2 qLab;

    @Override
    public void start(Stage primaryStage) {

        dodges = 5;

        //Establish scene shape and create root.
        root = new Group();
        boundary = Screen.getScreens().get(0).getBounds();
        final Scene scene = new Scene(root, boundary.getWidth(),boundary.getHeight(), Color.BLACK);
        primaryStage.setScene(scene);
        primaryStage.setX(boundary.getMinX());
        primaryStage.setY(boundary.getMinY());

        //Open MIDI Channels to other Apps.
        qLab = new MidiPlayer2("QLAB");

        //Turn on background music in qLab.
        qLab.noteOn(13,5);

        //Initial state for logic.
        isFired = false;
        isGameOver = false;

        //Create and add Player Rectangle to Screen.
        player = new LightBand(boundary, (int)boundary.getMaxY()-105, Color.WHITE, pianoKeys);
        root.getChildren().add(player.getBand());

        //Set starting Player position.
        player.playCollapse(0);

        //Initialize Counter setup.
        createCounter();
        setCounterTimeline();

        //Initialize Game Over message.
        setGameOver();
        setScore();

        //Set to track which keys are currently pressed.
        HashSet<KeyCode> pressed = new HashSet<>();

        //Event Handler for recognizing button presses on keys/drums.
        scene.setOnKeyPressed(new EventHandler<javafx.scene.input.KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                //If new key is pressed (not in Set).
                if (!pressed.contains(event.getCode())) {
                    //boolean cuts down on avg. loop iterations.
                    boolean found = false;
                    //Check for Drum hit.
                    for(int i=0; i<drumKeys.length && !found; i++){
                        //Drum hit found. Fire missile at appropriate location.
                        if(event.getCode() == drumKeys[i]){
                            pressed.add(event.getCode());
                            found = true;
                            fireNewMissile(i);
                        }
                    }
                    //No Drum Played. Check for Keys.
                    if(!found) {
                        int keyState = player.isKeyPlayed(event.getCode());
                        //If key is played and not in Game Over state.
                        if (keyState != -1 && !isGameOver) {
                            pressed.add(event.getCode());
                            //Movement sounds.
                            qLab.noteOn(13, 2);
                            qLab.noteOn(13,1);
                            //Move Piece.
                            player.playCollapse(keyState);
                            player.stopRestore();
                        }
                    }
                }
            }
        });

        //Event Handler for recognizing button releases on keys/drums.
        scene.setOnKeyReleased(new EventHandler<javafx.scene.input.KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                //If one of the keys in Set were released, remove from set.
                if (pressed.contains(event.getCode())) {
                    pressed.remove(event.getCode());
                }
            }
        });

        //Initialize Stage.
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreen(true);
        primaryStage.show();

        //When Game Closes, fade out audio.
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                qLab.noteOn(13,6);
            }
        });
    }

    //Fire new Projectile.
    public void fireNewMissile(int location){
        if(!isFired) {
            isFired = true;
            qLab.noteOn(13, 4);
            missile = new DodgeCircle(50, boundary, location, root);
            counting.playFromStart();
            missile.move();
        }
    }

    //Create geometry for counter bar.
    public void createCounter(){
        counter = new Rectangle(0, 30, Color.LIGHTBLUE);
        counter.setX(0);
        counter.setY(0);
        counter.setEffect(new BoxBlur(10,10,10));
        counter.setTranslateZ(1);
        counter.setOpacity(0.4);

        root.getChildren().add(counter);
    }

    //Animation Timeline for counter bar.
    public void setCounterTimeline(){

        //Setup animation frames.
        count = new KeyValue(counter.widthProperty(), boundary.getWidth());
        count1 = new KeyFrame(new Duration(400), count);

        //Setup animation timeline.
        counting = new Timeline();
        counting.getKeyFrames().add(count1);

        //On Finish Handler.
        counting.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                counter.setWidth(0);
                //Reset firing state.
                isFired = false;

                //Check for missile hit.
                if (missile.getDodge().intersects(
                        player.getBand().getX(), player.getBand().getY(),
                        player.getBand().getWidth(), player.getBand().getHeight())) {

                    missile.isHit();

                    //Change Associated lighting and sound effects.
                    qLab.noteOn(13, 7);
                    qLab.noteOn(13, 3);

                    //Display Game Over.
                    gameOver.setVisible(true);
                    player.getBand().setVisible(false);

                    //End Game and stop projectiles.
                    isFired = true;
                    isGameOver = true;
                }

                //Check if missile dodged.
                else {
                    dodges--;

                    //Piano Win state.
                    if(dodges == 0){
                        isGameOver = true;
                        isFired = true;
                        gameOver.setText("PIANO WINS!");
                        gameOver.setVisible(true);
                        qLab.noteOn(13, 7);
                        qLab.noteOn(13, 3);
                        score.setVisible(false);
                    }
                        score.setText("Dodge: " + dodges);
                }
            }
        });
    }

    //Drum Win Game Over State.
    public void setGameOver(){
        gameOver = new Text("DRUMS WIN!");
        gameOver.setFill(Color.GHOSTWHITE);
        gameOver.setFont(new Font("VERDANA", 119));
        gameOver.setX(0);
        gameOver.setY(boundary.getHeight() / 2);
        gameOver.setVisible(false);

        root.getChildren().add(gameOver);
    }

    //Score Counter.
    public void setScore(){
        score = new Text("Dodge: " + dodges);
        score.setFill(Color.GHOSTWHITE);
        score.setFont(new Font("VERDANA", 40));
        score.setX(800);
        score.setY(80);

        root.getChildren().add(score);
    }
    public static void main(String[] args) { launch(args); }

}