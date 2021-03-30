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
