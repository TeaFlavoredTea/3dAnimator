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

import java.util.*;

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
