/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.meandre.demo.components.io;

import java.io.FileReader;
import java.io.BufferedReader;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.Set;

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
           description="Filter the unnecessary words in Map.",
           name="WordFilter",
           tags="word, filter")
           
public class WordFilter implements ExecutableComponent {
	@ComponentProperty(defaultValue="",
 		   			   description="File containing stop words.",
 		   			   name="fileName")
    final static String DATA_PROPERTY_1 = "fileName";
	
	@ComponentProperty(defaultValue="false",
                       description="This property sets whether the number of keys should be limited. " +
                       			   "If truet, it will be numerated after filtering stop words.",
                       name="isLimited")
    final static String DATA_PROPERTY_2 = "isLimited";
	@ComponentProperty(defaultValue="100",
                       description="This property sets the maximum number of keys to be kept in output Map.",
                       name="upperLimit")
    final static String DATA_PROPERTY_3 = "upperLimit";
	
	@ComponentInput(description="Map to be filtered.",
            	    name= "inputMap")
    public final static String DATA_INPUT = "inputMap";
	
	@ComponentOutput(description="Output filtered content in Map format.",
                     name="outputMap")        
    public final static String DATA_OUTPUT = "outputMap";
	
	/** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
    	Map<String, Integer> inputMap = 
    		(Hashtable<String, Integer>)cc.getDataComponentFromInput(DATA_INPUT);
    	
    	String fileName = cc.getProperty(DATA_PROPERTY_1);
    	try {
    		BufferedReader reader = 
    			new BufferedReader(new FileReader(fileName));
    		String word;
    		while((word=reader.readLine()) != null) {
    			word = word.trim();
    			inputMap.remove(word);
    		}
    		reader.close();
    	}catch(java.io.IOException e) {
    		throw new ComponentExecutionException(e);
    	}
    	
    	boolean isLimited = Boolean.parseBoolean(
    			cc.getProperty(DATA_PROPERTY_2));
    	Map<String, Integer> outputMap = null;
    	if(isLimited) {
    		int upperLimit = 
    			Integer.parseInt(cc.getProperty(DATA_PROPERTY_3));
    		if(inputMap.size() > upperLimit) {
    			byValueComparator bvc =	
    				new byValueComparator(inputMap);
    			TreeMap<String, Integer> sortedMap = 
    				new TreeMap<String, Integer>(bvc);
    			sortedMap.putAll(inputMap);
    			outputMap = new Hashtable<String, Integer>();
    			while(upperLimit > 0) {
    				String key = sortedMap.firstKey();
    				Integer value = (Integer)sortedMap.get(key);
    				//System.out.println("upperLimit = " + upperLimit + " key = " + key + " value = " + value.intValue());
    				outputMap.put(key, value);
    				sortedMap.remove(key);
    				--upperLimit;
    			}
    		}
    	}
		
    	outputMap = (!isLimited)? inputMap: outputMap;
    	cc.pushDataComponentToOutput(DATA_OUTPUT, outputMap);	
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
    
    class byValueComparator implements Comparator<String> {
  		Map base_map;
	
  		public byValueComparator(Map base_map) {
    		this.base_map = base_map;
  		}
	
  		public int compare(String arg0, String arg1) {
  			int result = ((Integer)base_map.get(arg1)).compareTo(
					(Integer)base_map.get(arg0));
  			if(result == 0)
  				result = arg1.compareTo(arg0);
			return result;
		}
	}
}
