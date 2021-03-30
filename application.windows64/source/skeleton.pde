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

import java.util.*;

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
   rootJoint.ParrentAsLocalOriginRecursive(new point3D(2.7, 3, 0));  //root node uses 2.7, 3, 0 as it's local origin
   
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
     strokeWeight(0.3);
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
  strokeWeight(0.3);
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
