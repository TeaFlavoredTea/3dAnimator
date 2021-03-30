import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import java.util.*; 
import java.util.List; 
import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class assignmnet3 extends PApplet {



// ********************* ENTRY POINT OF PROGRAM ************************ //
// Author: Liam Guenther
// For: CS 4800
// 
//  WHAT: This file contains the core drawing loop and Global environment variables.
//        This file handels most of the user input, then calls nesisary objects.
//        This file does not mannage GUI inputs, that is done by file: "gui"
//
//
// ********************************************************************** //

PImage img;  //the textrue of the floor
skeleton model;  //stores the 3D modle we want to render
animator userAnimation = null;  //stores the animation created by the user
final int FRAME_RATE = 60;  //fixed frame rate, can be changed, but it will effect animations

// This is the normalized latatude (y) and longitude (x).
// Lat and long b/c camera is on the surface of a sphere looking at lookPoint
// Normalized means (value/domain) so values range from 0 to 1
float xNormalized;  
float yNormalized;

//used to adjust the camera based off where was clicked and not the raw mouse location
float xNormalizedWhenClicked;  
float yNormalizedWhenClicked;
point3D cameraClickLocation = new point3D();

//this is the actual world cordinate of the camera
float camX;
float camY;
float camZ;
float distance = 20; //distance camera is from point being looked at by camera (note that the camera is hard coded to look at 0,0,0)


public void setup() {
  
  createGUI();  //create the GUI
  customGUI();  //run special GUI functions
  frameRate(FRAME_RATE);
  img = loadImage("floor.png");
  model = new skeleton("shapes.csv", "joints.csv", "skeleton.csv");
  strokeWeight(0.3f);
    
  //arbitrary staring location of the camera (should be between [0,1])
  xNormalized = 0.1f;  
  yNormalized = 0.75f;
  
  //print instructions to user
  println("\n=======================================================\n   Program Written By: Liam Guenther (lguenthe@uccs.edu)\n=======================================================");
  println("Instructions:");
  println(" * Look around by right-clicking and dragging the mouse.");
  println(" * View joints by pressing and holding 'r'");
  println(" * Select a joint by pressing 'r' and left-clicking it. Unselect in the same way");
  println("\tThe selected joint will appear cyan");
  println(" * Rotate selected joint forward and backwards in the:");
  println("\t X axis with 'u' and 'j'");
  println("\t Y axis with 'i' and 'k'");
  println("\t X axis with 'o' and 'l'");
  println(" * create and play animations from the \"controls\" window");
}

public void draw() {  
  //run custom GUI code
  customGUI();
  
  //equasions to put camera on a sphere
  camZ = distance * (float)Math.sin((PI * yNormalized - PI/2));
  camX = distance * (float)Math.cos((PI * yNormalized) - PI/2) * (float)Math.cos(4 * PI * xNormalized);
  camY = distance * (float)Math.cos((PI * yNormalized) - PI/2) * (float)Math.sin(4 * PI * xNormalized);
  perspective(PI/3, PApplet.parseFloat(width)/PApplet.parseFloat(height), 1, 100); //set camera's: FOV, spectRatio, nearClippingPlane, farClippingPlane
  camera(camX, camY, camZ, 0, 0, 0, 0, 0, -1); //place the camera
  
  //draw enviroment
  background(126, 200, 255);
  lights();
  drawFloor(20);
  
  //update animation if playing
  if(userAnimation != null){
    if(userAnimation.getPlayStatus()){  //if playing animation
      joint updatedFrame = userAnimation.nextFrame();  //get frame
      if(updatedFrame != null){
        model.setJointTree(updatedFrame); //update the model with the new animation frame
        playbackBar.setValue((float)userAnimation.getCurrentPlayBackFrame() / (float)userAnimation.getNumberOfFrames());  //update the playback bar
      }
      else {  //was null, we have reached the end of the animation
        controlsTextPane.appendText("> ...animation stoped");
      }
    }
  }
  
  // draw modle
  model.drawBody();
  if(keyPressed){  //draw joints if needed
    if(key == 'r' || key == 'R'){
      model.drawJoints();
    }
  }
  
  //rotate selected joint if needed
  model.rotateSelectedJoint();

}

// this function is called whenever a mouse button is depressed
public void mousePressed(){
  
  //Camera is being moved
  if (mouseButton == RIGHT){
   cameraClickLocation = new point3D(PApplet.parseFloat(mouseX), PApplet.parseFloat(mouseY), 0);
   xNormalizedWhenClicked = xNormalized;
   yNormalizedWhenClicked = yNormalized;
 }
 
 //joint is being selected
 if(mouseButton == LEFT && keyPressed && key == 'r' || key == 'R'){
   model.toggleSelectedJoint();
 }
}

//this function is called whenever the mouse is moved while a mouse button is depressed
public void mouseDragged() {
  //use the mouse for camera location
  if (mouseButton == RIGHT){ 
   xNormalized = (PApplet.parseFloat(mouseX) - cameraClickLocation.x) / PApplet.parseFloat(width) + xNormalizedWhenClicked;  //diffrenceNormalized + startingxNormalized
   yNormalized = (PApplet.parseFloat(mouseY) - cameraClickLocation.y) / PApplet.parseFloat(height) + yNormalizedWhenClicked;
   
   //if we pass over the north or south pole we are upside down, this code prevents the camera from passing over a pole
   if(yNormalized >= 1)  //crossed north pole, go back to just before the pole
     yNormalized = 0.99999f;
   if (yNormalized <= 0)  //crossed south pole, go back
     yNormalized = 0.00001f;
 }
}


//draws the checkerboard floor
public void drawFloor(float size) {
  size = size / 2;
  int sizeOfBitMap = 432; //this is the length and width of the bitmap in pixels
  fill(255);
  stroke(255);
  beginShape(); //start rectangualr floor
  texture(img);
  vertex(-size, -size, 0, 0,   0);
  vertex( size, -size, 0, sizeOfBitMap, 0);
  vertex( size,  size, 0, sizeOfBitMap, sizeOfBitMap);
  vertex(-size,  size, 0, 0,   sizeOfBitMap);
  endShape();
}

//used to add additional statments to customise the GUI controls
public void customGUI(){
  //update the addFrame button which first said "set initial state"
  if(userAnimation != null){
    addFrame.setText("Add key frame");
  }
  
  //set color for the button the controls the animation's looping
  if(userAnimation != null){  
    if(userAnimation.getLoopingStatus()){
      toggleLoop.setLocalColorScheme(2);  //yellow theme
    }
    else{
      toggleLoop.setLocalColorScheme(3);  //purple theme
    }
  }
  
  //update the play/pause button to replect play status
  if(userAnimation != null){
    boolean isPlaying = userAnimation.getPlayStatus();
    if(isPlaying){  //is playing
      playAnimation.setText("pause");
      playAnimation.setLocalColorScheme(2);  //yellow theme
    }
    else{  //is paused
      playAnimation.setText("play");
      playAnimation.setLocalColorScheme(3);  //purple theme
    }
  }
}
// ********************* animator class ************************ //
// Author: Liam Guenther
// For: CS 4800
// 
//  WHAT: This class is used to create and store animations, and mannages the data side
//        of animation playback. It is called in files: "assignment3" and "gui". Most
//        functionality of this class is triggered by GUI inputs.
//
//        playing the animation itself is left up to the draw(){...} loop
//
// ************************************************************** //



class animator{
  // ==================== MEMBERS ================= //
  joint initialState;  //state of the model before the animation starts
  ArrayList<keyFrame> keyFrames;  //keyFrames that define the animation
  ArrayList<joint> frames;  //frames in the animation. Note: a frame is actually a rooJoint for the skeleton class to draw a modle from
  int currentPlayBackFrame;
  boolean isPlaying;
  boolean isLooping;
  
  // ================= CONSTRUCTORS =============== //
  public animator(){
    initialState = new joint();
    keyFrames = new ArrayList<keyFrame>();
    frames = new ArrayList<joint>();
    currentPlayBackFrame = 0;
    isPlaying = false;
    isLooping = false;
  }
  
  public animator(joint startingSkeleton){
    initialState = startingSkeleton;
    keyFrames = new ArrayList<keyFrame>();
    frames = new ArrayList<joint>();
    currentPlayBackFrame = 0;
    isPlaying = false;
    isLooping = false;
  }
  
  // =================== ACCESSORS ================ //
  public boolean getLoopingStatus(){
    return isLooping;
  }
  
  public boolean getPlayStatus(){
    return isPlaying;
  }
  
  public int getCurrentPlayBackFrame(){
    return currentPlayBackFrame;
  }
  
  public int getNumberOfKeyFrames(){
    return keyFrames.size();
  }
  
  public int getNumberOfFrames(){
    return frames.size();
  }
  
  public joint getFrame(int frameNumber){
    if(frameNumber > frames.size()-1){
      frameNumber = frames.size()-1;
    }
    if(frameNumber < 0){
      frameNumber = 0;
    }
    return new joint(frames.get(frameNumber));
  }
  
  public void addKeyFrame(keyFrame incomingKeyFrame){

    //create a maping from joint's ID to the value of that joint in incomingKeyframe
    HashMap<String, joint> destination = new HashMap<String, joint>();
    destination = incomingKeyFrame.rootJoint.mapIdRecursive(destination);
    
    //get the rootJoint of the animation frame prior to this keyFrame
    joint priorState;
    if(keyFrames.size() != 0 ){  //there is a prior keyframe to use
      priorState = keyFrames.get(keyFrames.size()-1).rootJoint;
    } else {  //there is no prior keyframe
      priorState = initialState;
    }
    
    //interpolate frames defined by the incomingKeyFrame
    ArrayList<joint> incomingFrames = new ArrayList<joint>();
    for(int i = 0; i != incomingKeyFrame.numberOfFrames-1; i++){  //-1 because the last frame is added manually
      joint thisFrame = new joint(priorState); //thisFrame is now a deep-copy of the prior keyFrame's rootJoint
      thisFrame.interpolateFrameRecursive(destination, incomingKeyFrame.numberOfFrames, i);
      incomingFrames.add(thisFrame);
    }
    incomingFrames.add(new joint(incomingKeyFrame.rootJoint)); //manualy add the last frame as a direct copy to avoid rounding-error in the interpolation
    
    //add the newly interpolated frames to the animation so far
    frames.addAll(incomingFrames);
    
    //add incommingKeyFrame to the end of the list of key frames in the animation
    keyFrames.add(new keyFrame(incomingKeyFrame));
  }
  
  // ==================== METHODS ================== //
  public boolean toggleLooping(){
    isLooping = !isLooping;
    return isLooping;
  }
  
  public void playFromFrame(int n){
    currentPlayBackFrame = n;
    isPlaying = true;
  }
  
  public void pauseAnimation(){
    isPlaying = false;
  }
  
  public joint nextFrame(){
    //get frame
    joint frame;
    if(0 <= currentPlayBackFrame && currentPlayBackFrame <= frames.size()-1){
      frame = frames.get(currentPlayBackFrame);
      currentPlayBackFrame++;//advance playback for next frame 
    } else {   //no such frame
      if (isLooping){
        currentPlayBackFrame = 0;  //restart the animation
        frame = frames.get(currentPlayBackFrame);
        currentPlayBackFrame++;//advance playback for next frame 
      }
      else{
        frame = null;
        isPlaying = false;
      }
    }
    return frame;
  }
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

synchronized public void ctrlWin_draw(PApplet appc, GWinData data) { //_CODE_:AnimationInput:454362:
  appc.background(230);
} //_CODE_:AnimationInput:454362:

public void controlsTextPane_change1(GTextArea source, GEvent event) { //_CODE_:controlsTextPane:655714:
  //println("textarea2 - GTextArea >> GEvent." + event + " @ " + millis());
} //_CODE_:controlsTextPane:655714:

public void addFrame_click1(GButton source, GEvent event) { //_CODE_:addFrame:561487:
  if(event == GEvent.CLICKED){
    
    //check if we need to instantiate animator or add a keyframe
    if(userAnimation == null){  //instaniate
    userAnimation = new animator(model.getJointTree());
    controlsTextPane.appendText("> model set as starting frame of animation");
    } 
    else {  //add keyFrame
      int userSetFrames = numberOfFrames_slider.getValueI();
      userAnimation.addKeyFrame(new keyFrame(model.getJointTree(), userSetFrames));
      controlsTextPane.appendText("> model set as key frame: " + userAnimation.getNumberOfKeyFrames() + " for " + userSetFrames + " frames");
    }
  }
} //_CODE_:addFrame:561487:

public void custom_slider1_change1(GCustomSlider source, GEvent event) { //_CODE_:numberOfFrames_slider:456456:
  
} //_CODE_:numberOfFrames_slider:456456:

public void playAnimation_click1(GButton source, GEvent event) { //_CODE_:playAnimation:726973:
  if(event == GEvent.CLICKED){
    if(userAnimation != null){
      if(userAnimation.getPlayStatus()){  //is already playing: pause
        userAnimation.pauseAnimation();
        controlsTextPane.appendText("> ...animation stoped");
      }
      else {  //is not playing: play
        int selectedFrame = (int)(playbackBar.getValueF() * (userAnimation.getNumberOfFrames()-1));
        if (selectedFrame == userAnimation.getNumberOfFrames()-1) {  //if user wants to play from final frame there is nothing to play, just start from 0
          selectedFrame = 0;
        }
        userAnimation.playFromFrame(selectedFrame);
        controlsTextPane.appendText("> playing from frame: " + selectedFrame);
      }
    }
    else{
      controlsTextPane.appendText("> Cannot play empty animation, try setting the initial frame");
    }
  }
} //_CODE_:playAnimation:726973:

public void playbackBar_slider1_change1(GCustomSlider source, GEvent event) { //_CODE_:playbackBar:267706:
  if(event == GEvent.VALUE_STEADY){
    if(userAnimation != null){
      joint selectedFrame = userAnimation.getFrame((int)(source.getValueF() * (userAnimation.getNumberOfFrames()-1)));
      model.setJointTree(selectedFrame);
    }
  }
} //_CODE_:playbackBar:267706:

public void playAnimationFromStart_click1(GButton source, GEvent event) { //_CODE_:playAnimationFromStart:267106:
  if(userAnimation != null){
    userAnimation.playFromFrame(0);
    controlsTextPane.appendText("> playing from frame: 0");
  }
  else{
    controlsTextPane.appendText("> Cannot play empty animation, try setting the initial frame");
  }
} //_CODE_:playAnimationFromStart:267106:

public void toggleLoop_click1(GButton source, GEvent event) { //_CODE_:toggleLoop:278655:
  if(event == GEvent.CLICKED && userAnimation != null){
    boolean status = userAnimation.toggleLooping();  //toggle the animation's loop status
    controlsTextPane.appendText("> looping set to: " + status);
  }
} //_CODE_:toggleLoop:278655:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.PURPLE_SCHEME);
  G4P.setMouseOverEnabled(false);
  surface.setTitle("Sketch Window");
  AnimationInput = GWindow.getWindow(this, "controls", 0, 0, 400, 500, JAVA2D);
  AnimationInput.noLoop();
  AnimationInput.setActionOnClose(G4P.KEEP_OPEN);
  AnimationInput.addDrawHandler(this, "ctrlWin_draw");
  controlsTextPane = new GTextArea(AnimationInput, 12, 36, 372, 156, G4P.SCROLLBARS_VERTICAL_ONLY | G4P.SCROLLBARS_AUTOHIDE);
  controlsTextPane.setPromptText("animation log");
  controlsTextPane.setOpaque(true);
  controlsTextPane.addEventHandler(this, "controlsTextPane_change1");
  addFrame = new GButton(AnimationInput, 12, 312, 96, 30);
  addFrame.setText("set initial state");
  addFrame.addEventHandler(this, "addFrame_click1");
  numberOfFrames_slider = new GCustomSlider(AnimationInput, 12, 228, 372, 72, "grey_blue");
  numberOfFrames_slider.setShowValue(true);
  numberOfFrames_slider.setShowLimits(true);
  numberOfFrames_slider.setLimits(60, 1, 300);
  numberOfFrames_slider.setNbrTicks(6);
  numberOfFrames_slider.setShowTicks(true);
  numberOfFrames_slider.setNumberFormat(G4P.INTEGER, 0);
  numberOfFrames_slider.setOpaque(false);
  numberOfFrames_slider.addEventHandler(this, "custom_slider1_change1");
  label1 = new GLabel(AnimationInput, 12, 12, 139, 20);
  label1.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label1.setText("animation console output");
  label1.setOpaque(false);
  playAnimation = new GButton(AnimationInput, 200, 400, 90, 30);
  playAnimation.setText("play");
  playAnimation.addEventHandler(this, "playAnimation_click1");
  label2 = new GLabel(AnimationInput, 12, 216, 144, 20);
  label2.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label2.setText("Set number of frames");
  label2.setOpaque(false);
  playbackBar = new GCustomSlider(AnimationInput, 12, 396, 372, 96, "grey_blue");
  playbackBar.setLimits(0.0f, 0.0f, 1.0f);
  playbackBar.setNumberFormat(G4P.DECIMAL, 0);
  playbackBar.setOpaque(false);
  playbackBar.addEventHandler(this, "playbackBar_slider1_change1");
  label3 = new GLabel(AnimationInput, 12, 396, 80, 20);
  label3.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label3.setText("playback bar");
  label3.setOpaque(false);
  playAnimationFromStart = new GButton(AnimationInput, 100, 400, 90, 30);
  playAnimationFromStart.setText("play from start");
  playAnimationFromStart.addEventHandler(this, "playAnimationFromStart_click1");
  toggleLoop = new GButton(AnimationInput, 340, 400, 36, 30);
  toggleLoop.setText("loop");
  toggleLoop.addEventHandler(this, "toggleLoop_click1");
  AnimationInput.loop();
}

// Variable declarations 
// autogenerated do not edit
GWindow AnimationInput;
GTextArea controlsTextPane; 
GButton addFrame; 
GCustomSlider numberOfFrames_slider; 
GLabel label1; 
GButton playAnimation; 
GLabel label2; 
GCustomSlider playbackBar; 
GLabel label3; 
GButton playAnimationFromStart; 
GButton toggleLoop; 
// ********************* joint class ************************ //
// Author: Liam Guenther
// For: CS 4800
// 
//  WHAT: This class aids the skeleton class in constructing the rendred modle. It is also
//        used to store the modle's state in the keyFrame class for animations.
//        Joints are sotred in a tree. Joints each have one "limb" which is the body part
//        that moves directly with that joint. Joint's children (in the tree) move with
//        their parrent, and store their location relitive the the parrent (the parrent is
//        seen as 0,0,0 to that child). individual Joints are sometimes called 'nodes' in comments.
//
//        Note that joints are often called 'frames' because a joint-tree is all that is
//        needed to define the skeleton class for a given frame. formally, a joint is not
//        a frame, but it is all the data needed for a frame.
//
// ************************************************************** //




class joint{
  // ================================= DATA MEMBERS ================================ //
  private String id;
  private point3D jointLocation;  //relative to parrent (unless root)
  private point3D rotationLocation;  //used to store how much rotation is desired (in degrees)
  private rectPris limb;
  private ArrayList<joint> children;
  private float xMin, xMax, yMin, yMax, zMin, zMax;
  private int red, green, blue;
  
  // ================================== CONSTRUCTORS ============================== //
  public joint(){
    id = "";
    jointLocation =  new point3D();
    rotationLocation = new point3D();
    limb =  new rectPris();
    children = new ArrayList<joint>();
    xMin = xMax = yMin = yMax = zMin = zMax = 0;
    red = blue = green = 255;
  }
  
  public joint(String id, point3D jointLocation, point3D rotationLocation, rectPris limb,float xMin, float xMax, float yMin, float yMax, float zMin, float zMax){
    this.id = id;
    this.jointLocation = jointLocation;
    this.rotationLocation = rotationLocation;
    this.limb = limb;
    this.children = new ArrayList<joint>(); //a propper list of children may be assigned later
    this.xMin = xMin;
    this.xMax = xMax; 
    this.yMin = yMin; 
    this.yMax = yMax; 
    this.zMin = zMin; 
    this.zMax = zMax;
    red = 255; green = 255; blue = 0;
    
  }
  
  public joint(joint copyMe){   
    //copy member objects
    this.id = new String(copyMe.id);
    this.jointLocation = new point3D(copyMe.jointLocation);
    this.rotationLocation =  new point3D(copyMe.rotationLocation);
    this.limb = new rectPris(copyMe.limb);
    
    //copy member primatives
    this.xMin = copyMe.xMin;
    this.xMax = copyMe.xMax;
    this.yMin = copyMe.yMin;
    this.yMax = copyMe.yMax;
    this.zMin = copyMe.zMin;
    this.zMax = copyMe.zMax;
    
    this.red = copyMe.red;
    this.green = copyMe.green;
    this.blue = copyMe.blue;
    
    //copy children
    this.children = new ArrayList<joint>();
    for(joint child : copyMe.children){
      children.add(new joint(child));
    }
  }
  
  // ========================================= ACCESSOR METHODS ================================ //
  public String getId(){
    return new String(id);
  }
  
  public point3D getJointLocation(){
    return new point3D(jointLocation.x, jointLocation.y, jointLocation.z);
  }
  
  public point3D getRotation(){
    return new point3D(rotationLocation.x, rotationLocation.y, rotationLocation.z);
  }
  
  public float getScreenX(){
    return screenX(jointLocation.x, jointLocation.y, jointLocation.z);
  }
  
  public float getScreenY(){
    return screenY(jointLocation.x, jointLocation.y, jointLocation.z);
  }
  
  public ArrayList<joint> getChildrenRecursive(){
  ArrayList subNodes = new ArrayList<joint>();
  subNodes.addAll(this.children);
  for(joint child : children){
    subNodes.addAll(child.getChildrenRecursive());
  }
    return subNodes;
  }
  
  public point3D getColor(){
    return new point3D(red, green, blue);
  }
  
  public void setColor(int red, int green, int blue){
    this.red = red;
    this.green = green;
    this.blue = blue;
  }
  
  public void setChildren(ArrayList<joint> children){
    this.children = children;
  }
  
  public void setRotation(point3D p){
    if(p.x < xMax && p.x > xMin){
      rotationLocation.x = p.x;
    }
    if(p.y < yMax && p.y > yMin){
      rotationLocation.y = p.y;
    }
    if(p.z < zMax && p.z > zMin){
      rotationLocation.z = p.z;
    }
  }

  
  
  // ================================== METHODS ================================================ //

  // Returns true if the joint has any children
  public boolean hasChildren(){
    if(children.size() == 0){
      return false;
    } else {
      return true;
    }
  }
  
  // Draws this limb, then recursivly tells children to do the same.
  // If called on the root-node, all limbs will be drawn.
  public void drawLimbsRecursive(){
    pushMatrix();
    translate(jointLocation.x, jointLocation.y, jointLocation.z);
    rotateX(radians(rotationLocation.x));
    rotateY(radians(rotationLocation.y));
    rotateZ(radians(rotationLocation.z));
    limb.drawPris();
    for(joint node : children){
      node.drawLimbsRecursive();
    }
    popMatrix();
  }
  
  // Draws a point at the location of this joint, then recursivly tells children to do the same.
  // If called on the root-node all joints will be drawn.
  public void drawJointsRecursive(){
    pushMatrix();
    translate(jointLocation.x, jointLocation.y, jointLocation.z);
    rotateX(radians(rotationLocation.x));
    rotateY(radians(rotationLocation.y));
    rotateZ(radians(rotationLocation.z));
    stroke(this.red, this.green, this.blue);
    point(0,0,0);
    for(joint node : children){
      node.drawJointsRecursive();
    }
    popMatrix();
  }
  
  // Causes each child to see their parrent as the origin recursivly.
  // Once used joints will only be able to draw their world location after their parrent.
  public void ParrentAsLocalOriginRecursive(point3D newOrigin){
    for(joint node : children){
      node.ParrentAsLocalOriginRecursive(this.getJointLocation());
    }
    this.moveLocalOrigin(newOrigin);
  }
  
  // convert's this node's positon to a new origin
  // that is: the node's global position is the same but it knows
  // it's position relive to a new origin.
  public void moveLocalOrigin(point3D newOrigin){
    jointLocation.x = jointLocation.x - newOrigin.x;
    jointLocation.y = jointLocation.y - newOrigin.y;
    jointLocation.z = jointLocation.z - newOrigin.z;
  }
  
  //finds the global coordinates of this node, then instructs its children to do the same recusivly
  public HashMap<point3D, joint> getGlobalLocationRecursive(){
    pushMatrix();
    //go to this joint's location and orientation
    translate(jointLocation.x, jointLocation.y, jointLocation.z);
    rotateX(radians(rotationLocation.x));
    rotateY(radians(rotationLocation.y));
    rotateZ(radians(rotationLocation.z));
    
    //get this joint's global location
    point3D myGlobalLocation = new point3D (modelX(0,0,0), modelY(0,0,0), modelZ(0,0,0));  //0,0,0 because the transofmations have put us on the joint location, so it's positoin now reads 0,0,0
    
    //add this joint to the map
    HashMap<point3D, joint> selfAndChildren = new HashMap<point3D, joint>();
    selfAndChildren.put(myGlobalLocation, this);
    
    //do the same for all children
    for(joint child: children){
      selfAndChildren.putAll(child.getGlobalLocationRecursive());
    }
    popMatrix();
    return selfAndChildren;
  }
  
  //creates a mapping from each joint's id to that joint
  public HashMap<String, joint> mapIdRecursive(HashMap<String, joint> mapping){
    mapping.put(id, this);
    for(joint child : children){
      child.mapIdRecursive(mapping);
    }
    return mapping;
  }
  
  //adjusts the rotation of the joints in this tree to an interpolated path between two keyframes
  public void interpolateFrameRecursive(HashMap<String, joint> destination,int numberOfFrames,int frameNumber){
    
    //calculate linear rate for each axis (low quality interpolation)
    point3D destinationRotation = new point3D(destination.get(id).getRotation()); //get the end position of this keyframe for this node id
    float rateX = (float)(destinationRotation.x - rotationLocation.x) / (float)numberOfFrames; //(final - initial) / number_of_frames
    float rateY = (float)(destinationRotation.y - rotationLocation.y) / (float)numberOfFrames;
    float rateZ = (float)(destinationRotation.z - rotationLocation.z) / (float)numberOfFrames;
    
    //set the interpolated position for this node and this frameNumber
    rotationLocation.x = frameNumber * rateX + rotationLocation.x; //rotationLocation.x should initally be the value of end state of the prior keyframe
    rotationLocation.y = frameNumber * rateY + rotationLocation.y;
    rotationLocation.z = frameNumber * rateZ + rotationLocation.z;
    
    //have all children interpolate their rotation for this frameNumber
    for(joint child : children){
      child.interpolateFrameRecursive(destination, numberOfFrames, frameNumber);
    }
    
  }
  
}
// ********************* keyFrame class ************************ //
// Author: Liam Guenther
// For: CS 4800
// 
//  WHAT: This is a container class used by the animator class to store keyframes.
//        Note that the joint member is the root node of the joint tree that describes
//        the skeleton at the end of the keyframe.
//
// ************************************************************** //


class keyFrame{
  // =========== MEMBERS ============ //
  public joint rootJoint;
  public int numberOfFrames;
  
  
  // ========== CONSTRUCTORS ======== // 
  public keyFrame(){
    rootJoint = new joint();
    numberOfFrames = 0;
  }
  
  public keyFrame(joint rootJoint, int numberOfFrames){
    this.rootJoint = new joint(rootJoint);
    this.numberOfFrames = numberOfFrames;
  }
  
  public keyFrame(keyFrame copyMe){
    this.rootJoint = new joint(copyMe.rootJoint);
    this.numberOfFrames = copyMe.numberOfFrames;
  }
}
// ********************* point3D class ************************ //
// Author: Liam Guenther
// For: CS 4800
// 
//  WHAT: This class is used to store 3-dimensional points. Sometimes it is used
//        to store points that are only 2-dimensional, in these cases the z axis is always 0.
//
// ************************************************************** //


class point3D{
  
  // ============ DATA  MEMEBERS =========== //
  public float x, y, z;
  
  // ============ CONSTRUCTORS ============== //
  point3D(){
    x = 0; y = 0; z = 0;
  }
  
  point3D(float xVal, float yVal, float zVal){
    x = xVal; y = yVal; z = zVal;
  }
  
  point3D(point3D copyMe){
    this.x = copyMe.x;
    this.y = copyMe.y;
    this.z = copyMe.z;
  }
  
}
// ********************* rectPris class ************************ //
// Author: Liam Guenther
// For: CS 4800
//
//    WHAT: This class aids the joint class: each joint's limb is a rectPris.
//          each limb sees it's joint as the origin, and is drawn relitve to it.
//          This class stores the data and methods needed to draw a rectangular
//          prisum. 'rectPris' is so sometimes refered to as a 'shape'
//
// ************************************************************** //


class rectPris{ 
  // ====================================== DATA MEMBERS ============================================= //
  private float x1, y1, z1, x2, y2, z2; //two corners of this shape
  private int red, blue, green; //RBG values of this shape
  private String id;
  
  // ====================================== CONSTRUCTORS ============================================= //
  public rectPris() {
    x1=0; y1=0; z1=0; x2=1; y2=1; z2=1; //unit cube
    red = 255; green = 255; blue = 255; //white
  }
  public rectPris(String inId, float inX1, float inY1, float inZ1, float inX2, float inY2, float inZ2){
    x1=inX1; y1=inY1; z1=inZ1; x2=inX2; y2=inY2; z2=inZ2;
    id = inId;
    red = (int)(Math.random()*255); green = (int)(Math.random()*255); blue = (int)(Math.random()*255); //give random fills
  }
  
  public rectPris(String inId, float inX1, float inY1, float inZ1, float inX2, float inY2, float inZ2, int r, int b, int g){
    x1=inX1; y1=inY1; z1=inZ1; x2=inX2; y2=inY2; z2=inZ2;
    id = inId;
    red = r; green = g; blue = b;
  }
  
  public rectPris(rectPris copyMe){
    this.x1 = copyMe.x1;
    this.y1 = copyMe.y1;
    this.z1 = copyMe.z1;
    this.x2 = copyMe.x2;
    this.y2 = copyMe.y2;
    this.z2 = copyMe.z2;
    this.red = copyMe.red;
    this.blue = copyMe.blue;
    this.green = copyMe.green;
    this.id = new String(copyMe.id);
  }
  
  // ===================================== ACESSORS ================================================= //
  public String getId(){
    return id;
  }
  
  // ===================================== METHODS ====================================================//
  //draws this rectangular prisum
  public void drawPris(){
    fill(red, green, blue);
    stroke(0);
    if (keyPressed == true && key == 'd') {  //just for fun
      fill((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255));
    }
    pushMatrix();
    translate(x1 + ((x2-x1) / 2.0f), y1 + ((y2-y1) / 2.0f), z1 + ((z2-z1) / 2.0f)); //move the unit box the the center of its future location
    scale(x2-x1, y2-y1, z2-z1); //scale the unit box to fill it's desired location 
    box(1); //draw the box
    popMatrix();
  }
  
  // Changes the origin from which this rectangular prisum knows it's position.
  // it dose not nesisarily change the global positon of the opject.
  public void moveLocalOrigin(point3D newOrigin){
    x1 = x1 - newOrigin.x;
    y1 = y1 - newOrigin.y;
    z1 = z1 - newOrigin.z;
    x2 = x2 - newOrigin.x;
    y2 = y2 - newOrigin.y;
    z2 = z2 - newOrigin.z;
  }
}
// ********************* skeleton class ************************ //
// Author: Liam Guenther
// For: CS 4800
//
//    WHAT: This class handels the construction of the modle from files, as well
//          as directing how the model should be drawn. It contains the root-node 
//          of the joint tree, this lets it intiate joint-tree traversals.
//          This class also stores what joint the user has selected to rotate.
//
//
// ************************************************************** //



class skeleton{
 // ======================================= DATA MEMBERS =================================================== //
 private joint rootJoint;  //the root joint used to acces all joints in the skeleton
 private joint selectedJoint;  //the joint the user has selected using a 3d-pick
 private point3D selectedJointLocation;  //we store this because it is expensive to calculate
 
 // ======================================= CONSTRUCTORS ==================================================== //
 public skeleton(){
   rootJoint =  new joint();
   selectedJoint =  null;
   selectedJointLocation =  new point3D();
 }
 
 public skeleton(String shapesFileName, String jointsFileName, String skeletonFileName){
   String[] linesInFile;  //all lines in a file
   String[] splitLine; //an array of substring from a line in a file
   HashMap<String,rectPris> shapesRead = new HashMap<String,rectPris>();
   HashMap<String,joint> jointsRead = new HashMap<String,joint>();
   
   // ------ read shape file ------ //
    linesInFile = loadStrings(shapesFileName);
    for (String line: linesInFile) {
      splitLine = line.split("[\\s,\\t]+");
      
      //create an instance of a rectPris from the data in this line and add it to shapesRead
      rectPris limbFromThisLine = new rectPris( splitLine[0], 
                Float.parseFloat(splitLine[1]), Float.parseFloat(splitLine[2]), Float.parseFloat(splitLine[3]),
                Float.parseFloat(splitLine[4]), Float.parseFloat(splitLine[5]), Float.parseFloat(splitLine[6]),
                Integer.parseInt(splitLine[7]), Integer.parseInt(splitLine[8]), Integer.parseInt(splitLine[9]));  //id, rectPris(id x1 y1 z1 x2 y2 z2 red blue green)
      shapesRead.put(splitLine[0], limbFromThisLine); 
    }
  
   // ------ read joint file ------ //
   linesInFile = loadStrings(jointsFileName);
   for(String line: linesInFile){
     splitLine = line.split("[\\s,\\t]+");
     point3D jointLocation = new point3D(Float.parseFloat(splitLine[1]), Float.parseFloat(splitLine[2]), Float.parseFloat(splitLine[3]));  //xPos, yPos, zPos
     
     //find a shape by the same id (splitLine[10]) as the one beloning to this joint
     rectPris limb = shapesRead.get(splitLine[10]);
     
     //modify the limb so it treats the joint location as 0,0,0
     limb.moveLocalOrigin(jointLocation);
     
     //create an instance of a joint from the data in this line and add it to jointsRead
     joint jointFromThisLine = new joint(splitLine[0], jointLocation, new point3D(), limb,
           Float.parseFloat(splitLine[4]), Float.parseFloat(splitLine[5]),
           Float.parseFloat(splitLine[6]), Float.parseFloat(splitLine[7]),
           Float.parseFloat(splitLine[8]), Float.parseFloat(splitLine[9]));
     jointsRead.put(splitLine[0], jointFromThisLine);
   }
   
   // ------- read skeleton file ------- //
   linesInFile = loadStrings(skeletonFileName);
   for(String line : linesInFile){
     splitLine = line.split("[\\s,\\t]+");
     ArrayList<joint> children = new ArrayList<joint>();
     
     //find the joint with id splitLine[0]
     joint currentParrent = jointsRead.get(splitLine[0]);
     
     //find joints who mach the id's given from splitLine[>0], and put them in list:children
     for(int i = 1; i < splitLine.length; i++){
           children.add(jointsRead.get(splitLine[i]));
     }
     
     //give the joint with id:splitLine[0] the it's children
     currentParrent.setChildren(children);
   }
   
   // -------- assign rootJoint ------ //
   rootJoint = jointsRead.get("root");
   
   // ----------- assign joints their parrent as local origin ------------ //
   rootJoint.ParrentAsLocalOriginRecursive(new point3D(2.7f, 3, 0));  //root node uses 2.7, 3, 0 as it's local origin
   
   //selected joint assigned by user later
   selectedJoint =  null;
   selectedJointLocation = new point3D();
 }
 
 // =================================================== ACCESOR METHODS =============================================== //
 public joint getJointTree(){
    if(selectedJoint != null){    //note, we must make sure the copy only containes yellow nodes
      selectedJoint.setColor(255, 255, 0); //yellow
    }
    
    joint copy =  new joint(rootJoint);
    
    if(selectedJoint != null){  //return selected node to original status
      selectedJoint.setColor(0, 255, 255); //cyan
    }
    return copy;
 }
 
 public void setJointTree(joint newRootJoint){
   rootJoint = newRootJoint;
 }
 
 
 // ===================================================== METHODS ====================================================== //
 
 // Draws the fully body of the modle (all limbs)
 public void drawBody(){
   //draw all limbs
   rootJoint.drawLimbsRecursive();  
   
   //draw selected joint if available
   if(selectedJoint != null){
     hint(DISABLE_DEPTH_TEST);
     hint(ENABLE_STROKE_PERSPECTIVE);
     strokeWeight(0.3f);
     point3D rgbColor = selectedJoint.getColor();
     stroke(rgbColor.x, rgbColor.y, rgbColor.z);
     point(selectedJointLocation.x, selectedJointLocation.y, selectedJointLocation.z);
     hint(ENABLE_DEPTH_TEST);
     hint(DISABLE_STROKE_PERSPECTIVE);
   }
 }
 
 // Draws all the joints in the modle on top of other objects.
 public void drawJoints(){
  hint(DISABLE_DEPTH_TEST);
  hint(ENABLE_STROKE_PERSPECTIVE);
  strokeWeight(0.3f);
  rootJoint.drawJointsRecursive();
  hint(ENABLE_DEPTH_TEST);
  hint(DISABLE_STROKE_PERSPECTIVE);
 }
 
 // Rotates the selected joint based off user keyboard inputs.
 public void rotateSelectedJoint(){
   if(selectedJoint != null){
     point3D newRotation = selectedJoint.getRotation();
     
     if(keyPressed && key == 'u' || key == 'U'){
       newRotation.x++;
     }
     if(keyPressed && key == 'j' || key == 'J'){
       newRotation.x--;
     }
     if(keyPressed && key == 'i' || key == 'I'){
       newRotation.y++;
     }
     if(keyPressed && key == 'k' || key == 'K'){
       newRotation.y--;
     }
     if(keyPressed && key == 'o' || key == 'O'){
       newRotation.z++;
     }
     if(keyPressed && key == 'l' || key == 'L'){
       newRotation.z--;
     }
     
     selectedJoint.setRotation(newRotation);
   }
 }
 
 
 // Attempts to find a new joint where the user has clicked on screen.
 // If no joint is nearby then the old selection is cleared.
 // If a joint is found nearby then it becomes selected, and replaces the previous selection.
 public void toggleSelectedJoint(){
   // ----------- find joint closest to mouse ---------- //
   HashMap<point3D, joint> allJoints = rootJoint.getGlobalLocationRecursive();  //populate alljoints with every joint and it's global locations
   
   joint closest = null; //will be assigned later: when/if found
   float currentDist;
   float smallestDist = 10; //must be at least closer than this inital value
   for (point3D point : allJoints.keySet()){
     currentDist = sqrt(pow(mouseX - screenX(point.x, point.y, point.z), 2) + pow(mouseY - screenY(point.x, point.y, point.z), 2));
     
     if(currentDist < smallestDist){
       closest = allJoints.get(point); //closest so far
       selectedJointLocation = new point3D(point.x, point.y, point.z);  //remember where it was in global coordinates
     }
     
   }
   
   // -------- close joint was found? --------- //
   if(closest != null){ //was Found
     //clear previous selectioin
     if(selectedJoint != null){
       selectedJoint.setColor(255, 255, 0); //yellow
       selectedJoint = null;
     }
     
     //set new selection
     selectedJoint = closest;
     selectedJoint.setColor(0, 255, 255);  //cyan
     
   } else {  //none found near mouse, clear selected joint to let the user exit joint rotaion
     if(selectedJoint != null){
       selectedJoint.setColor(255, 255, 0); //yellow
       selectedJoint = null;
     }
   }
 }
}
  public void settings() {  size(1000,800,P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "assignmnet3" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
