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

package org.meandre.components.io.file;

//==============
//Java Imports
//==============

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.logging.*;

//===============
// Other Imports
//===============

//import org.meandre.tools.components.*;
//import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.*;
import org.meandre.annotations.*;
import org.meandre.tools.webdav.*;


/**
 * <p>
 * Overview: This component reads file names from a directory and outputs then one
 * by one.
 * </p>
 * <p>
 * Detailed Description: Takes a file name of a directory as input and outputs
 * the file names of files within that directory. Note that only <b>file</b>
 * names will be output. If RercurseSubDirectories is true then all file names
 * from the file subtree will be output. Also, if a file regex pattern is
 * specified then only matching file names will be output. If the webdav flag
 * is set then the source is treated as a webdav. All of the file names
 * are collected into an array and they are pushed out one by one. An integer
 * count of the total number of names output is pushed out at the end.
 * </p>
 *
 * @author D. Dsearsmith
 *
 * TODO: Unit Tests
 */

@Component(creator = "Duane Searsmith", description = "<p>Overview: <br>"
		+ "This component reads file names from a directory and outputs then one by one.</p>"
		+ "<p>Detailed Description: <br>"
		+ "Takes a file name of a directory as input and outputs the file names of "
		+ "files within that directory.  Note that only file names will be output.  If  "
		+ "RercurseSubDirectories is true then all file names from the file subtree will be output. "
		+ "Also, if a file regex pattern is specified then only mathching file names will be output. "
		+ "If the webdav flag is set then the source is treated as a webdav.</p>"
		+ "<p>The default regex is txt.</p>"
		+ "<p>All of the file names are collected into an array and they are pushed out one by one. </p>"
		+ "<p>An integer count of the total number of names output is pushed out at the end.</p>",
		name = "Read File Names By Flag", tags = "io, read, file",
        baseURL="meandre://seasr.org/components/")

public class ReadFileNamesByFlag extends AbstractExecutableComponent  {

	// ==============
	// Data Members
	// ==============

	private int _docsProcessed = 0;

	private long _start = 0;

	private ArrayList<String> _names = null;

	private String _dirName = null;

	private static Logger _logger = Logger.getLogger("ReadFileNames");

	private Pattern _sRegexFilter = null;

	// props

	@ComponentProperty(description = "Treat source as webdav?", name = "webdav", defaultValue = "false")
	final static String DATA_PROPERTY_WEBDAV = "webdav";

	@ComponentProperty(description = "Alpha sort file names?", name = "sort", defaultValue = "false")
	final static String DATA_PROPERTY_SORT = "sort";

	@ComponentProperty(description = "Verbose output?", name = "verbose", defaultValue = "false")
	final static String DATA_PROPERTY_VERBOSE = "verbose";

	@ComponentProperty(description = "Recurse subdirectories?", name = "recurse", defaultValue = "false")
	final static String DATA_PROPERTY_RECURSE = "recurse";

	@ComponentProperty(description = "Regex for file name filter.", name = "filter", defaultValue = "(.)*[\\.][Tt][Xx][Tt]")
	final static String DATA_PROPERTY_FILTER = "filter";

	@ComponentInput(description = "Directory name.", name = "directory_name")
	public final static String DATA_INPUT_DIRNAME = "directory_name";

	// IO

	@ComponentOutput(description = "File names.", name = "file_names")
	public final static String DATA_OUTPUT_FILE_NAMES = "file_names";

	@ComponentOutput(description = "Integer count of file names output.", name = "file_count")
	public final static String DATA_OUTPUT_FILE_COUNT = "file_count";

	@ComponentOutput(
			description = "Boolean value for whether the current item passed is the last item.",
			name = "last_item"
	)
	public final static String OUTPUT_LAST_ITEM = "last_item";
	// ================
	// Constructor(s)
	// ================
	public ReadFileNamesByFlag() {
	}

	// ================
	// Static Methods
	// ================

	/**
	 * Test
	 */
	static public void main(String[] args) {

//		// get a flow builder instance
//		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
//		// get a flow object
//		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
//		// add a component
//		String pushString = wflow
//				.addComponent("org.meandre.components.io.PushString");
//		// set a component property
//		wflow.setComponentInstanceProp(pushString, "string",
//				"http://norma.ncsa.uiuc.edu/public-dav/capitanu");
//		// add another component
//		String reader = wflow
//				.addComponent("org.meandre.components.io.file.input.ReadFileNames");
//		wflow.setComponentInstanceProp(reader, DATA_PROPERTY_FILTER, ".*\\.xml");
//        wflow.setComponentInstanceProp(reader, DATA_PROPERTY_WEBDAV, "true");
//		// make a connection between two components
//		wflow.connectComponents(pushString, "output_string", reader,
//				DATA_INPUT_DIRNAME);
//
//		// execute the flow specifying that we want a web UI displayed
//		flowBuilder.execute(wflow, false);
//
//		// For some reason the process does not end without a forced exit.
//		System.exit(0);

	}

	// ================
	// Public Methods
	// ================

	public boolean getWebdav(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_WEBDAV);
		return Boolean.parseBoolean(s.toLowerCase());
	}

	public boolean getSort(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_SORT);
		return Boolean.parseBoolean(s.toLowerCase());
	}

	public boolean getRecurse(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_RECURSE);
		return Boolean.parseBoolean(s.toLowerCase());
	}

	public boolean getVerbose(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_VERBOSE);
		return Boolean.parseBoolean(s.toLowerCase());
	}

	public String getFilter(ComponentContextProperties ccp) {
		String filt = (String) ccp.getProperty(DATA_PROPERTY_FILTER);
		if ((filt == null) || (filt.trim().length() == 0)) {
			return null;
		}
		return filt;
	}

	public String getDirName() {
		return this._dirName;
	}

	// =====================================
	// Interface Impl: ExecutableComponent
	// =====================================

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initializeCallBack(ComponentContextProperties ccp) {
		_logger.fine("initialize() called");
		_names = null;
		String pat = getFilter(ccp);
		_sRegexFilter = (pat == null) ? null : Pattern.compile(pat, 0);
		_docsProcessed = 0;
		_start = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void disposeCallBack(ComponentContextProperties ccp) {
		_logger.fine("dispose() called");
		long end = System.currentTimeMillis();
		if (getVerbose(ccp)) {
			_logger.info("ReadFileNames: END EXEC -- File Names Processed: "
					+ _docsProcessed + " in " + (end - _start) / 1000
					+ " seconds\n");
		}
		_docsProcessed = 0;
		_names = null;
		_dirName = null;
		_sRegexFilter = null;
	}

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void executeCallBack(ComponentContext ctx)
			throws ComponentExecutionException, ComponentContextException {
		_logger.fine("execute() called");
		// will read only one input per execution
		if (_dirName == null) {
			_dirName = (String) ctx
					.getDataComponentFromInput(DATA_INPUT_DIRNAME);
		} else {
			// TODO: fix this so that multiple inputs are welcome!
			_logger
					.severe("ReadFileNames: module will ony accept a directory name once. "
							+ "	The value just read from input will be ignored.");
			return;

		}

		if (_names == null) {
			try {
				_names = getNames(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ctx.pushDataComponentToOutput(DATA_OUTPUT_FILE_COUNT,
					new Integer(_names.size()));
		}

		while (_names.size() > 0) {

			// get next name to output
			String result = (String) _names.remove(0);

			if (!result.startsWith("http"))
				result = "file://"+result;

			ctx.pushDataComponentToOutput(DATA_OUTPUT_FILE_NAMES, result);

			_docsProcessed++;

			if (_names.size() == 0){
				ctx.pushDataComponentToOutput(OUTPUT_LAST_ITEM, "true");
				componentConsoleHandler.whenLogLevelOutput("verbose", _docsProcessed +" : pushing out file name and true: "+ result);

			}
			else {
				ctx.pushDataComponentToOutput(OUTPUT_LAST_ITEM, "false");
				componentConsoleHandler.whenLogLevelOutput("verbose", _docsProcessed + ": pushing out file name and false: "+ result);
			}

		}
		// assume that the _names have been built and are not depleted
		if (_names.size() == 0) {
			_names = null;
		}
	}

	// =================
	// Private Methods
	// =================

	private ArrayList<String> getNames(ComponentContext ctx) throws Exception {
		// read the file names and load the array list
		ArrayList<String> names = new ArrayList<String>();

		if (getWebdav(ctx)) {

            WebdavClient webdav = new WebdavClient(_dirName);
            try {
				// Get the list of files matching the specified criteria
				IResourceInfo[] files = webdav.listFiles(_dirName,
						getRecurse(ctx), new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return _sRegexFilter.matcher(name).matches();
							}
						});

				if (files != null) {

					// Push out all the file URLs found matching the given
					// criteria
					for (IResourceInfo fileInfo : files) {
						names.add(fileInfo.getURL().toString());
					}
					return names;
				} else {
					_logger.severe(_dirName
							+ " does not exist or is not a directory!");
					return names;
				}

			} catch (IOException e) {
				_logger.severe("IOException thrown: " + e.getMessage());
				e.printStackTrace();
				return names;
			}

		}

		// convert name into file and make sure it exists and that it is a
		// directory
		//check and remove file url, 'file://' from front of dirname so it doesn't fail

		if (_dirName.startsWith("file://"))
			_dirName = _dirName.substring(7);

		File inFile = new File(_dirName);
		if (!inFile.isDirectory()) {
			_logger
					.info("ReadFileNames: File name input exists but it is not a directory: "
							+ _dirName
							+ " Will use the parent directory instead.");
			inFile = inFile.getParentFile();
		}
		if (!getRecurse(ctx)) {
			File[] files = null;
			if (_sRegexFilter == null) {
				files = inFile.listFiles();
			} else {
				files = inFile.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return _sRegexFilter.matcher(name).matches();
					}
				});
			}
			for (int i = 0, n = files.length; i < n; i++) {
				if (files[i].isFile()) {
					names.add(files[i].getPath());
				}
			}
		} else {
			// recurse
			if (this.getSort(ctx)) {
				TreeSet<Object[]> set = new TreeSet<Object[]>(new Date_Comparator());
				this.searchSubsSet(inFile, set, ctx);
				for (Iterator<Object[]> it = set.iterator(); it.hasNext();) {
					Object[] obs = it.next();
					names.add((String)obs[1]);
				}
			} else {
				searchSubs(inFile, names, ctx);
			}
		}
		return names;
	}

	private void searchSubs(File parent, List<String> names, ComponentContext context) {
		// get the class name
		_logger.info("ReadFileNames: path is -- " + parent.getPath()
				+ " -- is directory ? " + parent.isDirectory());
		File[] children = null;
		if (_sRegexFilter == null) {
			children = parent.listFiles();
		} else {
			children = parent.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return _sRegexFilter.matcher(name).matches();
				}
			});
		}
		for (int i = 0, n = children.length; i < n; i++) {
			File child = (File) children[i];
			if (child.isFile()) {
				names.add(child.getPath());
			} else {
				if (child.isDirectory()) {
					searchSubs(child, names, context);
				}
			}
		}
	}

	private void searchSubsSet(File parent, Set<Object[]> names, ComponentContext context) {
		// get the class name
		_logger.info("ReadFileNames: path is = " + parent.getPath()
				+ " -- is a directory ? " + parent.isDirectory());
		File[] children = null;
		if (_sRegexFilter == null) {
			children = parent.listFiles();
		} else {
			children = parent.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return _sRegexFilter.matcher(name).matches();
				}
			});
		}

		for (int i = 0, n = children.length; i < n; i++) {
			File child = (File) children[i];
			if (child.isFile()) {
				Object[] obarr = new Object[2];
				obarr[0] = new Long(child.lastModified());
				obarr[1] = child.getPath();
				names.add(obarr);
			} else {
				if (child.isDirectory()) {
					searchSubsSet(child, names, context);
				}
			}
		}
	}

	private class Date_Comparator implements Comparator<Object[]> {

		public Date_Comparator() {
		}

		// ======================
		// Interface: Comparator
		// ======================
		public int compare(Object[] o1, Object[] o2) {
			Object[] objarr1 = o1;
			Object[] objarr2 = o2;
			if (((Long) objarr1[0]).longValue() > ((Long) objarr2[0])
					.longValue()) {
				return 1;
			} else if (((Long) objarr1[0]).longValue() < ((Long) objarr2[0])
					.longValue()) {
				return -1;
			} else {
				return ((String) objarr1[1]).compareTo((String) objarr2[1]);
			}
		}

		public boolean equals(Object o) {
			return this.equals(o);
		}
	}

}
