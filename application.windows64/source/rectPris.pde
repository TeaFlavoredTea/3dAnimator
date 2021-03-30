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
    translate(x1 + ((x2-x1) / 2.0), y1 + ((y2-y1) / 2.0), z1 + ((z2-z1) / 2.0)); //move the unit box the the center of its future location
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
