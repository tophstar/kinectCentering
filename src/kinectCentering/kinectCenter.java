package kinectCentering;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import SimpleOpenNI.*;

public class kinectCenter extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SimpleOpenNI kinect;
	SimpleOpenNI kinect2;

	boolean draw = false;
	boolean sideToShow = false;
	boolean hOrV = false;
	boolean noLine = false;
	
	int maxZ = 2000;
	int spacing = 1;//Must be a factor of 640 or 480
	int margin = 0;
	
	public void setup()
	{
		
	  size(320*2+10, 480, OPENGL);
	  
	  kinect = new SimpleOpenNI(0,this);	  
	  kinect2 = new SimpleOpenNI(1,this);
	}

	public void draw()
	{		
		if(draw)
		{
			kinect.dispose();
			  SimpleOpenNI.updateAll();
			  kinect2.finalize();
			  exit();
			  System.exit(0);
		}
		else
		{
		  SimpleOpenNI.start();
		  

		  
		  background(0);
		  
		  translate(width/2, height/2, -1000);
		  
		  rotateX(radians(180));
		  
		  if(sideToShow)
		  {
			  kinect.init(0);
			  drawScan(kinect, 0);  
		  }
		  else
		  {
			  kinect2.init(1);
			  drawScan(kinect2, 2);
		  }
	  }
	}

	void drawScan(SimpleOpenNI thisKinect, int window)
	{	
		//background(255, 105, 108);
	  if(thisKinect.enableDepth() == false)
	  {
	    println("Camera Failed");
	    exit();
	    return;     
	  }
	  else
	  {
		  thisKinect.enableRGB();
		  thisKinect.alternativeViewPointDepthToImage();
		  
	  }
	  
	  thisKinect.update();
	  
	  PVector[] depthPoints = thisKinect.depthMapRealWorld();
	  PImage rgbImage = thisKinect.rgbImage();
	  
	  for(int y = 0; y < 480 - spacing; y+=spacing)
	  {
	    for(int x=0; x < 640 - spacing; x+= spacing)
	    {    
	    	int i = x + y*640;
	    	PVector currentPoint = depthPoints[i];
	    	stroke(255);
	    	
	    	if(noLine && hOrV && x >318 && x < 322)
	    	{
	    		stroke(255, 105, 108);
	    		point(currentPoint.x-1400*(1-window), currentPoint.y, currentPoint.z);
	    	}
	    	else if(noLine && !hOrV && y > 238 && y < 242)
	    	{
	    		stroke(255, 105, 108);
	    		point(currentPoint.x-1400*(1-window), currentPoint.y, currentPoint.z);	    		
	    	}
	    	else if(currentPoint.z > 0)
	    	{
		        stroke(rgbImage.pixels[i]);
		        point(currentPoint.x-1400*(1-window), currentPoint.y, currentPoint.z);
	    	}
/*	    	else
	    	{
				 println("zero at"+x+","+y);
				 point(currentPoint.x-1400*(1-window), currentPoint.y, 1000);
	    	}
*/	    }
	  }
	  
	  //thisKinect.close();
	}
	  
	public void keyPressed()
	{
	  
	    
	    if(key == 'y')
	    {
	    	noLine = true;
	    }
	    if(key == 'n')
	    {
	    	noLine = false;
	    }
	    else if(keyCode == UP)
	    {
	    	hOrV = true;
	    }
	    else if(keyCode == DOWN)
	    {
	    	hOrV = false;
	    }
	    else if(keyCode == LEFT)
	    {
	    println("left");
	      sideToShow = false;
	    }
	    else if(keyCode == RIGHT)
	    {
	    	println("right");
	    	sideToShow = true;
	    }
	    else if(key == ' ')
	    {
	      draw = true;
	    } 
	}
}
