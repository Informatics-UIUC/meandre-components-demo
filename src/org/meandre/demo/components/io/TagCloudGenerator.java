/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.meandre.demo.components.io;

import java.util.Hashtable;
import java.util.Enumeration;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import javax.imageio.ImageIO;

import java.io.ByteArrayOutputStream;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Display tag cloud. If there are many tags to be displayed, " +
           "reduce the maximum size of font or magnify the size of canvas " +
           "to accommodate all of tags.",
           name="TagCloudGenerator",
           tags="tag cloud, visualization")

public class TagCloudGenerator implements ExecutableComponent {
	@ComponentProperty(defaultValue="1000",
             		   description="This property sets the width of canvas.",
             		   name="width")
    final static String DATA_PROPERTY_1 = "width";
	@ComponentProperty(defaultValue="1000",
  		   			   description="This property sets the height of canvas.",
  		   			   name="height")
    final static String DATA_PROPERTY_2 = "height";
	@ComponentProperty(defaultValue="Courier",
	                   description="This property sets the name of font.",
	                   name="name")
    final static String DATA_PROPERTY_3 = "name";
	@ComponentProperty(defaultValue="150",
  			           description="This property sets the maximum size of font.",
  			           name="maxSize")
    final static String DATA_PROPERTY_4 = "maxSize";
	@ComponentProperty(defaultValue="20",
	           		   description="This property sets the minimum size of font.",
	           		   name="minSize")
    final static String DATA_PROPERTY_5 = "minSize";
	
	@ComponentInput(description="Tags to be analyzed.",
                    name= "inputMap")
    public final static String DATA_INPUT = "inputMap";
	
	@ComponentOutput(description="Output image as byte array.",
                     name="outputObject")        
    public final static String DATA_OUTPUT = "outputObject";
	
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) 
    	throws ComponentExecutionException, ComponentContextException {
    	
    	int width = Integer.parseInt(cc.getProperty(DATA_PROPERTY_1)),
            height = Integer.parseInt(cc.getProperty(DATA_PROPERTY_2));
    	
    	String fontName = cc.getProperty(DATA_PROPERTY_3);
    	int maxFontSize = Integer.parseInt(cc.getProperty(DATA_PROPERTY_4)), //maximum font size
    	    minFontSize = Integer.parseInt(cc.getProperty(DATA_PROPERTY_5));  //minimum font size
    	
		Hashtable<String, Integer> table = 
			(Hashtable<String, Integer>)cc.getDataComponentFromInput(DATA_INPUT);
		
		int length = table.size();
		String[] text = new String[length];
		int[] fontSize = new int[length];
		
		Enumeration<String> keys = table.keys();
		int pos = 0;
		int maxValue = Integer.MIN_VALUE,
		    minValue = Integer.MAX_VALUE;
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			int value = ((Integer)table.get(key)).intValue();
			text[pos] = key;
			fontSize[pos++] = value;
			maxValue = (value>maxValue)? value: maxValue;
			minValue = (value<minValue)? value: minValue;
		}
		if(maxValue != minValue) {
			double slope = (maxFontSize-minFontSize)/(maxValue-minValue);
			for(int k=0; k<fontSize.length; k++)
				fontSize[k] = (int)(minFontSize+slope*(fontSize[k]-minValue));
		} else {
			for(int k=0; k<fontSize.length; k++)
				fontSize[k] = minFontSize;
		}
		
		Color[] colors = {new Color(0x99, 0x33, 0x33), 
				          new Color(0x99, 0x66, 0x33), 
				          new Color(0x99, 0x99, 0x33),
				          new Color(0x99, 0xcc, 0x33),
				          new Color(0x99, 0xff, 0x33)};

		int increment = 5;

		boolean[][] grid = new boolean[width][height];
		for(int i=0; i<grid.length; i++)
			for(int j=0; j<grid[0].length; j++)
				grid[i][j] = false;

		BufferedImage image = new BufferedImage(
			width, height, BufferedImage.TYPE_INT_ARGB);	
		Graphics2D g2D = image.createGraphics();
		FontRenderContext frc = g2D.getFontRenderContext();

		for(int k=0; k<text.length; k++) {
			Font font = new Font(fontName, Font.BOLD, fontSize[k]);					
  			TextLayout layout = new TextLayout(text[k], font, frc);
				
			int textWidth = (int)layout.getVisibleAdvance(); 
			int textHeight = (int)(layout.getBounds().getHeight() +
								   layout.getDescent());
			
			int xCoord = 0,
                yCoord = textHeight;

			textWidth = ((textWidth+4*increment)/increment)*increment;
			textHeight = ((textHeight+4*increment)/increment)*increment;
			
			BufferedImage textImage = new BufferedImage(
				textWidth, textHeight,BufferedImage.TYPE_INT_ARGB);
			Graphics2D textG2D = textImage.createGraphics();

			textG2D.setColor(Color.white);
			textG2D.fillRect(0, 0, textWidth-1, textHeight-1);
			textG2D.setColor(colors[k%colors.length]);
			textG2D.setFont(font);
        	textG2D.drawString(text[k], xCoord, yCoord);

			BufferedImage biFlip = null;
			if(k%5==0) {
				biFlip = new BufferedImage(textHeight, textWidth,textImage.getType());
				for(int i=0; i<textWidth; i++)
    					for(int j=0; j<textHeight; j++)
    						biFlip.setRGB(textHeight-1-j, i, textImage.getRGB(i, j));
    						//biFlip.setRGB(j, textWidth-1-i, textImage.getRGB(i, j));
				textImage = biFlip;
				int tmp = textWidth;
				textWidth = textHeight;
				textHeight = tmp;
			}

			int[] pixels = new int [textWidth * textHeight];
			PixelGrabber pg = new PixelGrabber (textImage, 0, 0, textWidth, textHeight,
                		pixels, 0, textWidth);
     		try {
     			pg.grabPixels ();
     		}
     		catch (InterruptedException e) {
        		throw new ComponentExecutionException(e);
     		}
			
			boolean[][] mask = new boolean[textWidth][textHeight];
			for(int i=0; i<mask.length; ++i)
				for(int j=0; j<mask[0].length; ++j)
					mask[i][j] = false;
			for (int j=0; j<textHeight; j+=increment) {
	    			for (int i=0; i< textWidth; i+=increment) {
					boolean found = false;
					for(int ii=i; ii<i+increment&&ii<textWidth-increment; ii++) {
						for(int jj=j; jj<j+increment&&jj<textHeight-increment; jj++) {
							int value = pixels[jj * textWidth + ii];
							byte[] rgb = new byte[3];
							rgb [0] = (byte) (value & 0xFF);
            				rgb [1] = (byte) ((value >> 8) & 0xFF);
           					rgb [2] = (byte) ((value >>  16) & 0xFF);
							//if(rgb[0]!=0 || rgb[1]!=0 || rgb[2]!=0) {
           					if(!(rgb[0]==-1  && rgb[1]==-1 && rgb[2]==-1)) {
								//textG2D.fillRect(i, j, 5, 5);
								mask[i][j] = true;
								found = true;
								break;
							}
						}//jj
						if(found)
							break;
					}//ii
				}//i
			}//j	

			double a = Math.random() * Math.PI;
  			double d = Math.random() * (Math.max(textWidth, textHeight)/4);
  			double da = (Math.random()-0.5) / 2;
  			double dd = 0.05;
			int x, y;
			while (true) {
				x = (int)(Math.floor((width + (Math.cos(a)*d*2) - (textWidth/2))/5)*5);
    			y = (int)(Math.floor((height + (Math.sin(a)*d) - (textHeight/2))/5)*5);
    
				x = (x<0)?0: x;
				y = (y<0)?0: y;
								
    			boolean fail = false;
    			for (int xt=0; xt<textWidth && !fail; xt+=increment) {
      				for (int yt=0; yt<textHeight && !fail; yt+=increment) {					
						if(xt+x>=width || 
						   yt+y>=height ||
					       (mask[xt][yt] && grid[xt + x][yt + y]))
          						fail = true;
      				}
    			}
    			if (!fail) 
      				break;
    			a += da;
    			d += dd;
  			}
			
			//System.out.println(" x = " + x + " y = " + y);
			
			for (int xt=0; xt<textWidth; xt+= increment) {
    			for (int yt = 0; yt<textHeight; yt+= increment)
      				if (mask[xt][yt])
        				grid[xt+x][yt+y] = true;
			}

			for(int i=0; i<textWidth; i++)
    			for(int j=0; j<textHeight; j++)
    				if(mask[(i/increment)*increment][(j/increment)*increment])
    					image.setRGB(x+i, y+j, textImage.getRGB(i, j));
		}//k
		//g.drawImage(image, 0, 0, this);	
		
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    try {
	    	ImageIO.write(image, "png", os);
	    	os.flush();
	    }catch(java.io.IOException e) {
	    	throw new ComponentExecutionException(e);
	    }
	    
 		cc.pushDataComponentToOutput(DATA_OUTPUT, os.toByteArray());
    
 		try {
 			os.close();
 		}catch(java.io.IOException e) {
 			throw new ComponentExecutionException(e);
 		}
    }
	
	/**
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
    }
    
    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
}
