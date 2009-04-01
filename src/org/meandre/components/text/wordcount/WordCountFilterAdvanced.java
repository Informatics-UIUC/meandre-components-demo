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

package org.meandre.components.text.wordcount;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Lily Dong",
           description="Inputs a Map<String, Integer> and deletes all entries " +
           		"whose keys are among stop words. The stop words " +
           		"are specified in a file whose location is defined via " +
           		"component property. This component can also optionally filter " +
           		"words with lower word counts.",
           name="WordFilter",
           tags="word, filter",
           baseURL="meandre://seasr.org/components/")

public class WordCountFilterAdvanced extends AbstractExecutableComponent
{
	@ComponentProperty(defaultValue="http://repository.seasr.org/Datasets/Text/common_words.txt",
 		   			   description="URL containing stop words.",
 		   			   name="URL_for_Stop_Words")
    final static String DATA_PROPERTY_1 = "URL_for_Stop_Words";

	@ComponentProperty(defaultValue="false",
                       description="This property sets whether the number of keys should be limited." +
                       			   "If true, it will filter stop words.",
                       name="is_Limited")
    final static String DATA_PROPERTY_2 = "is_Limited";
	@ComponentProperty(defaultValue="100",
                       description="This property sets the maximum number of keys to be outputed.",
                       name="upper_Limit")
    final static String DATA_PROPERTY_3 = "upper_Limit";

	@ComponentInput(description="A word count summary in Map format." +
            "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>",
            	    name= "Map")
    public final static String DATA_INPUT = "Map";

	@ComponentOutput(description="Filtered word count in Map format." +
            "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>",
                     name="Map")
    public final static String DATA_OUTPUT = "Map";

	/** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
    	Map<String, Integer> inputMap =
    		(Hashtable<String, Integer>)cc.getDataComponentFromInput(DATA_INPUT);

    	//open connection to URL of stop words.
    	InputStream is = null;
        try {
            URL url = new URL(cc.getProperty(DATA_PROPERTY_1));
            try {
                is = url.openConnection().getInputStream();
                //is can not be closed here, otherwise java.net.SocketException: socket closed
                //the final client must close this stream.
            }catch(java.io.IOException e) {
                try {
                    if(is != null)
                        is.close();
                }catch(java.io.IOException ioex) {}
                throw new ComponentExecutionException(e);
            }
        } catch(java.net.MalformedURLException e) {
            throw new ComponentExecutionException(e);
        }

        //filter unnecessary words.
    	try {
    		BufferedReader reader =
    			new BufferedReader(new InputStreamReader(is));
    		String word;
    		while((word=reader.readLine()) != null) {
    			word = word.trim();
    			inputMap.remove(word);
    		}
    		reader.close();
    		is.close();
    	}catch(java.io.IOException e) {
    		throw new ComponentExecutionException(e);
    	}

    	//filter words with lower counts.
    	boolean isLimited = Boolean.parseBoolean(
    			cc.getProperty(DATA_PROPERTY_2));
    	Map<String, Integer> outputMap = inputMap;
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
    				outputMap.put(key, value);
    				sortedMap.remove(key);
    				--upperLimit;
    			}
    		}
    	}

    	cc.pushDataComponentToOutput(DATA_OUTPUT, outputMap);
    }

	/**
     * Call at the end of an execution flow.
     */
    public void initializeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }

    /**
     * Called when a flow is started.
     */
    public void disposeCallBack(ComponentContextProperties ccp)
    throws Exception {
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
