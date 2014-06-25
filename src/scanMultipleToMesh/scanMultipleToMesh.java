package scanMultipleToMesh;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import unlekker.modelbuilder.*;
import SimpleOpenNI.*;

public class scanMultipleToMesh extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SimpleOpenNI kinect;
	SimpleOpenNI kinect2;

	boolean scanning = false;
	int maxZ = 2000;
	int spacing = 2;//Must be a factor of 640 or 480
	int margin = 0;
	int distance = 1200;
	
	int scanCount = 0;

	int invalidZ = 99999;
	
	UGeometry model;
	UVertexList vertexList;

	public void setup()
	{
	  size(320*2+10, 480, OPENGL);
	  
	  SimpleOpenNI.start();
	  
	  
	  kinect = new SimpleOpenNI(0,this);
	  if(kinect.enableDepth() == false)
	  {
	    println("Camera One Failed");
	    exit();
	    return; 
	  }
	  else
	  {
		  kinect.enableRGB();
		  kinect.alternativeViewPointDepthToImage();
	  }
	  
	  kinect2 = new SimpleOpenNI(1,this);
	  if(kinect2.enableDepth() == false)
	  {
	    println("Camera Two Failed");
	    exit();
	    return;     
	  }
	  else
	  {
		  kinect2.enableRGB();
		  kinect2.alternativeViewPointDepthToImage();
	  }
	  
	  model = new UGeometry();
	  vertexList = new UVertexList();
	}

	public void draw()
	{
	  if(scanCount < 1)
	  {
	  background(0);
	  
	  SimpleOpenNI.updateAll();
	  //kinect.update();
	  
	  translate(width/2, height/2, -1000);
	  
	  rotateX(radians(180));
	  
	  PVector[] depthPoints = kinect.depthMapRealWorld();
	  PVector[] depthPoints90 = kinect2.depthMapRealWorld();


		  if(scanning)
		  {
			scanCount = 1;
			  
		    depthPoints = cropScan(depthPoints);
		    depthPoints90 = cropScan(depthPoints90);
	
		    createScan(depthPoints, false);
		    createScan(depthPoints90, true);
			
		    //Draw one point
	        stroke(255);
	        point(1, 1, 1);
		  }
		  else
		  {  
		    drawScan(kinect, depthPoints, 0);
		    drawScan(kinect2, depthPoints90, 2);
		  }
	  }
	  else
	  {
	     kinect = new SimpleOpenNI(this);
         kinect.enableDepth();
		 kinect.enableRGB();
		 kinect.alternativeViewPointDepthToImage();
         
	     kinect2 = new SimpleOpenNI(this);
         kinect2.enableDepth();
		 kinect2.enableRGB();
		 kinect2.alternativeViewPointDepthToImage();
         
		 scanCount -= 1;
	  }
	}

	PVector[] cropScan(PVector[] depthPoints)
	{
	  for(int y=0; y <= (480 - spacing); y+=spacing)
	  {
	    for(int x=0; x <= (640 - spacing); x+= spacing)
	    {
	      
	      int i = y*640+x;
	      PVector p = depthPoints[i];
	      
	      if(
	      p.z < 10 
	      || p.z > maxZ 
	      || y == 0 
	      || y == (480 - spacing) 
	      || x == 0 
	      || x == (640 - spacing)
	      || x < margin
	      || x > (640 - margin)
	      )
	      {
	                
	        PVector realWorld = new PVector();
	        PVector projective = new PVector(x,y,invalidZ);
	        
	        kinect.convertProjectiveToRealWorld(projective, realWorld); 
	               
	        depthPoints[i] = realWorld;
	      }
	    }
	  }
	  
	  return depthPoints;
	}

	void drawScan(SimpleOpenNI thisKinect,PVector[] depthPoints, int window)
	{
	  PImage rgbImage = thisKinect.rgbImage();
		
	  for(int y = 0; y < 480 - spacing; y+=spacing)
	  {
	    for(int x=0; x < 640 - spacing; x+= spacing)
	    {    
	      
	      if(!scanning)
	      {

	        int i = x + y*640;
	        stroke(rgbImage.pixels[i]);

	        PVector currentPoint = depthPoints[i];
	        point(currentPoint.x-1400*(1-window), currentPoint.y, currentPoint.z);
	      }
	      
	    }
	  }
	}

	void createScan(PVector[] depthPoints, boolean flip)
	{
	  model.beginShape(TRIANGLES);


	        
	  for(int y = 0; y < 480 - spacing; y+=spacing)
	  {
	    for(int x=0; x < 640 - spacing; x+= spacing)
	    {
	        
	        int nw = x + y * 640;
	        int ne = (x + spacing) + y * 640;
	        int sw = x + (y + spacing)*640;
	        int se = (x + spacing) + (y + spacing)*640;        

	        if(depthPoints[nw].z == invalidZ &&
	        depthPoints[ne].z == invalidZ &&
	        depthPoints[sw].z == invalidZ &&
	        depthPoints[se].z == invalidZ)
	        {
	        	//do nothing if none of the points are valid
	        }
	        else if(depthPoints[nw].z != invalidZ)
	        {
		        if(depthPoints[nw].z != invalidZ &&
		        depthPoints[ne].z != invalidZ &&
		        depthPoints[sw].z != invalidZ &&
		        depthPoints[se].z != invalidZ)
		        {
		        //create a normal face
		          model.addFace(new UVec3((int)depthPoints[nw].x,
		                                  (int)depthPoints[nw].y,
		                                  (int)depthPoints[nw].z-distance),      
		                        new UVec3((int)depthPoints[ne].x,
		                                  (int)depthPoints[ne].y,
		                                  (int)depthPoints[ne].z-distance),
		                        new UVec3((int)depthPoints[sw].x,
		                                  (int)depthPoints[sw].y,
		                                  (int)depthPoints[sw].z-distance));
		          model.addFace(new UVec3((int)depthPoints[ne].x,
		                                  (int)depthPoints[ne].y,
		                                  (int)depthPoints[ne].z-distance),      
		                        new UVec3((int)depthPoints[se].x,
		                                  (int)depthPoints[se].y,
		                                  (int)depthPoints[se].z-distance),
		                        new UVec3((int)depthPoints[sw].x,
		                                  (int)depthPoints[sw].y,
		                                  (int)depthPoints[sw].z-distance));
		        }
		        else if(false)
		        {
		        	PVector[] innerDepthPoints = depthPoints;
		        	
		        	//Replace the depth value for 
			        if(innerDepthPoints[nw].z == invalidZ)
			        {
			        	innerDepthPoints[nw].z = distance;
			        }
					if(innerDepthPoints[ne].z == invalidZ)
					{
						innerDepthPoints[ne].z = distance;						
					}
					if(innerDepthPoints[sw].z == invalidZ)
					{
						innerDepthPoints[sw].z = distance;
					}
					if(innerDepthPoints[se].z == invalidZ)
					{
						innerDepthPoints[se].z = distance;
					}
					
					/*model.addFace(new UVec3((int)depthPoints[nw].x,
                            				(int)depthPoints[nw].y,
                            				(int)depthPoints[nw].z-distance),      
                            	  new UVec3((int)depthPoints[ne].x,
                            			  	(int)depthPoints[ne].y,
                            			  	(int)depthPoints[ne].z-distance),
                                  new UVec3((int)depthPoints[sw].x,
                                		  	(int)depthPoints[sw].y,
                                		  	(int)depthPoints[sw].z-distance));
				    model.addFace(new UVec3((int)depthPoints[ne].x,
				    						(int)depthPoints[ne].y,
				    						(int)depthPoints[ne].z-distance),      
				    			  new UVec3((int)depthPoints[se].x,
				    					    (int)depthPoints[se].y,
				    					    (int)depthPoints[se].z-distance),
				    			  new UVec3((int)depthPoints[sw].x,
				    					  	(int)depthPoints[sw].y,
				    					  	(int)depthPoints[sw].z-distance));
				   */
		        }	
	        }
	    }
	  }
	  
	      model.rotateY(radians(180));
	    //model.toOrigin();
	    
	    model.endShape();
	    
//	    SimpleDateFormat logFileFmt = new SimpleDateFormat("'scan_'yyyMMddHHmmss'.stl'");
	    model.writeSTL(this, "this.stl");
	    
	    scanning = false;
	}
	  
	public void keyPressed()
	{
	  
	    
	    if(keyCode == UP)
	    {
	      maxZ += 25;
	      distance += 25; 
	      println(maxZ);
	    }
	    else if(keyCode == DOWN)
	    {
	      maxZ -= 25;
	      distance -= 25; 
	      println(maxZ);
	    }
	    else if(keyCode == RIGHT)
	    {
	      distance += 100;
	      println(distance);
	    }
	    else if(keyCode == LEFT)
	    {
	      distance -= 100; 
	      println(distance);
	    }
	    else if(key == ' ')
	    {
	      scanning = true;
	    } 
	}
}
