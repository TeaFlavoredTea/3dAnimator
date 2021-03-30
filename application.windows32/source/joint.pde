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


import java.util.List;

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
