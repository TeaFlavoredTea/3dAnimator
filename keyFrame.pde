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
