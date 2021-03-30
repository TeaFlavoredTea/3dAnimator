import g4p_controls.*;

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


void setup() {
  size(1000,800,P3D);
  createGUI();  //create the GUI
  customGUI();  //run special GUI functions
  frameRate(FRAME_RATE);
  img = loadImage("floor.png");
  model = new skeleton("shapes.csv", "joints.csv", "skeleton.csv");
  strokeWeight(0.3);
    
  //arbitrary staring location of the camera (should be between [0,1])
  xNormalized = 0.1;  
  yNormalized = 0.75;
  
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

void draw() {  
  //run custom GUI code
  customGUI();
  
  //equasions to put camera on a sphere
  camZ = distance * (float)Math.sin((PI * yNormalized - PI/2));
  camX = distance * (float)Math.cos((PI * yNormalized) - PI/2) * (float)Math.cos(4 * PI * xNormalized);
  camY = distance * (float)Math.cos((PI * yNormalized) - PI/2) * (float)Math.sin(4 * PI * xNormalized);
  perspective(PI/3, float(width)/float(height), 1, 100); //set camera's: FOV, spectRatio, nearClippingPlane, farClippingPlane
  camera(camX, camY, camZ, 0, 0, 0, 0, 0, -1); //place the camera
  
  //draw enviroment
  background(126, 200, 255);
  lights();
  drawFloor(20);
  
  //update animation if playing
  if(userAnimation != null){
    if(userAnimation.getPlayStatus()){  //if playing animation
      joint updatedFrame = userAnimation.nextFrame();  //get frame //<>//
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
void mousePressed(){
  
  //Camera is being moved
  if (mouseButton == RIGHT){
   cameraClickLocation = new point3D(float(mouseX), float(mouseY), 0);
   xNormalizedWhenClicked = xNormalized;
   yNormalizedWhenClicked = yNormalized;
 }
 
 //joint is being selected
 if(mouseButton == LEFT && keyPressed && key == 'r' || key == 'R'){
   model.toggleSelectedJoint();
 }
}

//this function is called whenever the mouse is moved while a mouse button is depressed
void mouseDragged() {
  //use the mouse for camera location
  if (mouseButton == RIGHT){ 
   xNormalized = (float(mouseX) - cameraClickLocation.x) / float(width) + xNormalizedWhenClicked;  //diffrenceNormalized + startingxNormalized
   yNormalized = (float(mouseY) - cameraClickLocation.y) / float(height) + yNormalizedWhenClicked;
   
   //if we pass over the north or south pole we are upside down, this code prevents the camera from passing over a pole
   if(yNormalized >= 1)  //crossed north pole, go back to just before the pole
     yNormalized = 0.99999;
   if (yNormalized <= 0)  //crossed south pole, go back
     yNormalized = 0.00001;
 }
}


//draws the checkerboard floor
void drawFloor(float size) {
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
