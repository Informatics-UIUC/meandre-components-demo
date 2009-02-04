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

package org.meandre.components.viz.table;

import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Lily Dong",
           description="This component provides a table viewer for data sets" +
               "that where attributes are separated by a delimiter (like " +
               "the comma). The data is held in Vector<Object[]>, where the " +
               "vector contains the rows, and the cells of each row are " +
               "in an Object[]. This component can be used with the " +
               "'CSV Reader' or Map2Table components.",
           name="Table Viewer",
           tags="table viewer",
           mode=Mode.webui,
           baseURL="meandre://seasr.org/components/")

public class TableViewer
    implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentProperty(defaultValue="true",
                       description="This property indicates whether a header exists " +
                       		"that contains the attribute (column) labels in the first row of the file. ",
                       name="Attribute_Label_Header")
    final static String DATA_PROPERTY_1 = "Attribute_Label_Header";

    @ComponentProperty(defaultValue="true",
                       description="This property indicates whether a header exists " +
                       		"that contains the attribute (column) types in the second row of the file. "+
                       		"Attribute types such as integer, float, double or String.",
                       name="Attribute_Type_Header")
    final static String DATA_PROPERTY_2 = "Attribute_Type_Header";

    @ComponentProperty(defaultValue="200",
 		   			   description="This property sets the number of rows per page to display.",
 		   			   name="Num_Rows_to_Display")
    final static String DATA_PROPERTY_3 = "Num_Rows_to_Display";

    @ComponentProperty(defaultValue="8",
 		   			   description="This property sets the number of columns per page to display.",
 		   			   name="Num_Columns_to_Display")
    final static String DATA_PROPERTY_4 = "Num_Columns_to_Display";

    @ComponentInput(description="This input contains the file content stored as a vector with "+
    		"each attribute (column) stored as an object array." +
    		"<br>TYPE: java.util.Vector<java.lang.Object[]>",
                    name= "Content")
    public final static String DATA_INPUT = "Content";

    @ComponentOutput(description="This output is the original 'tent' that is unchanged.",
                     name="Content")
    public final static String DATA_OUTPUT = "Content";

    /**
     * Store the number of rows per page.
     */
    private int nrRows = 200;

    /**
     * Store the number of columns per page.
     */
    private int nrColumns = 8;

    /**
     * Store the row indices of every page.
     */
    private int[] currentRow, nextRow;

    /**
     * Store the column indices of every bar.
     */
    private int[] currentColumn, nextColumn;

    /**
     * Store the current page index.
     */
    private int thePage;

    /**
     * Store the current bar index.
     */
    private int theBar;

    /**
     * Store the number of pages.
     */
    private int nrPages;

    /**
     * Store the number of bars.
     */
    private int nrBars;

    /**
     * isFirst being true means that the page is loaded for the first time.
     */
    private boolean isFirst;

    /**
     * The blocking semaphore
     */
    private Semaphore sem = new Semaphore(1,true);

    /**
     * The instance ID
     */
    private String sInstanceID = null;

    /** Store the input vector */
    private Vector<Object[]> inputContent;

    /** Store whether header exists in input content */
    private boolean isHeader;

    /** Store whether type exists in input content */
    private boolean isType;

    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem arose during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.getWriter().println(getViz());
        } catch (Exception e) {
            throw new WebUIException(e);
        }
    }


    /** A simple message.
    *
    * @return The html containing the page
    */
    private String getViz() {
        StringBuffer sb = new StringBuffer();

        if(isFirst) {
            sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
            sb.append("\"http://www.w3.org/TR/html4/loose.dtd\">\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<script src=\"http://code.jquery.com/jquery-latest.js\"></script>\n");
            sb.append("<script>\n");
            sb.append("$(document).ready(function(){\n");
            sb.append("$(\"#myTable\").tablesorter({widgets:['zebra']});\n");
            sb.append("})\n");
            sb.append("</script>\n");

            sb.append("<script>\n");
            sb.append("function submitForm()\n");
            sb.append("{\n");
            sb.append("var xmlHttp;\n");
            sb.append("try\n");
            sb.append("{\n");
            sb.append("xmlHttp=new XMLHttpRequest();\n");
            sb.append("}\n");
            sb.append("catch (e)\n");
            sb.append("{\n");
            sb.append("try\n");
            sb.append("{\n");
            sb.append("xmlHttp=new ActiveXObject(\"Msxml2.XMLHTTP\");\n");
            sb.append("}\n");
            sb.append("catch (e)\n");
            sb.append("{\n");
            sb.append("try\n");
            sb.append("{\n");
            sb.append("xmlHttp=new ActiveXObject(\"Microsoft.XMLHTTP\");\n");
            sb.append("}\n");
            sb.append("catch (e)\n");
            sb.append("{\n");
            sb.append("alert(\"Your browser does not support AJAX!\");\n");
            sb.append("return false;\n");
            sb.append("}\n");
            sb.append("}\n");
            sb.append("}\n");

            sb.append("xmlHttp.onreadystatechange  = function()\n");
            sb.append("{\n");
            sb.append("if(xmlHttp.readyState == 4)\n");
            sb.append("{\n");
            sb.append("if(xmlHttp.status  == 200)\n");

            sb.append("document.getElementById(\"myTable\").innerHTML=xmlHttp.responseText;\n");
            //sb.append("$(\"table\").tablesorter();\n");
            sb.append("$(\"table\").tablesorter({widgets:['zebra']});\n");

            sb.append("}\n");
            sb.append("}\n");
            sb.append("var selectedIndex = document.getElementById(\"input\").selectedIndex;\n");
            sb.append("var url = \"/\" + \"" + sInstanceID + "\"\n");
            sb.append("url = url + \"?page=\" + selectedIndex;\n");
            sb.append("selectedIndex = document.getElementById(\"input2\").selectedIndex;\n");
            sb.append("url = url + \"&bar=\" + selectedIndex;\n");
            sb.append("xmlHttp.open(\"GET\", url,true);\n");
            sb.append("xmlHttp.send(null);\n");
            sb.append("}\n");
            sb.append("</script>\n");

            sb.append("</head>\n");

            sb.append("<body>\n");
            sb.append("<link rel=\"stylesheet\" href=\"http://dev.jquery.com/view/trunk/themes/flora/flora.all.css\" type=\"text/css\" media=\"screen\" title=\"Flora (Default)\">\n");
            sb.append("<script src=\"http://tablesorter.com/jquery-latest.js\"></script>\n");
            sb.append("<script src=\"http://tablesorter.com/jquery.tablesorter.js\"></script>\n");

            sb.append("<link rel=\"stylesheet\" href=\"http://tablesorter.com/themes/blue/style.css\" type=\"text/css\" media=\"print, projection, screen\" />\n");

            sb.append("<table id=\"myTable\" class=\"tablesorter\" border=\"0\" cellpadding=\"0\" cellspacing=\"1\">\n");
        }

        sb.append("<thead>\n");
        sb.append("<tr>\n");
        sb.append("<th>").append("_N_").append("</th>\n");
        if(isHeader) {
            Object[] header = inputContent.elementAt(0);
            for(int column=currentColumn[theBar]; column<=nextColumn[theBar]; column++) {
                String theName = (String)header[column];
                sb.append("<th>").append(theName).append("</th>\n");
            }
        } else {
            for(int column=currentColumn[theBar]; column<=nextColumn[theBar]; column++)
                sb.append("<th>").append(" ").append("</th>\n");
        }
        sb.append("</tr>\n");
        sb.append("</thead>\n");

        sb.append("<tbody>\n");
        int shift = 0;
        shift = (isHeader)? shift+1: shift;
        shift = (isType)? shift+1: shift;
        for(int row=currentRow[thePage]; row<=nextRow[thePage]; row++) {
            Object[] data = inputContent.elementAt(row+shift);
            sb.append("<tr>\n");
            sb.append("<td>").append(Integer.toString(row)).append("</td>\n");
            for(int column=currentColumn[theBar]; column<=nextColumn[theBar]; column++)
                sb.append("<td>").append(data[column]).append("</td>\n");
            sb.append("</tr>");
        }
        sb.append("</tbody>\n");
        sb.append("</table>\n");

        if(isFirst) {
            sb.append("<div>\n");
            sb.append("<table cellpadding=\"4\" cellspacing=\"4\">\n");
            sb.append("<tr>\n");
            sb.append("<th>Rows:</th>\n");
            sb.append("<th>\n");
            sb.append("<select id=\"input\" onChange=\"submitForm()\">\n");
            sb.append("<option selected=\"yes\">" + currentRow[0] + "-" +
                    nextRow[0] + "</option>\n");
            for(int i=1; i<nrPages; i++)
                sb.append("<option>" + currentRow[i] + "-" + nextRow[i] + "</option>\n");
            sb.append("</select>\n");
            sb.append("</th>\n");
            sb.append("<th>Columns:</th>\n");
            sb.append("<th>\n");
            sb.append("<select id=\"input2\" onChange=\"submitForm()\">\n");
            sb.append("<option selected=\"yes\">" + currentColumn[0] + "-" +
                    nextColumn[0] + "</option>\n");
            for(int i=1; i<nrBars; i++)
                sb.append("<option>" + currentColumn[i] + "-" + nextColumn[i] + "</option>\n");
            sb.append("</select>\n");
            sb.append("</th>\n");
            sb.append("</tr>\n");
            sb.append("</table>\n");
            sb.append("</div>\n");

            sb.append("<div align=\"center\">\n");
            sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                    sInstanceID + "?done=true\">DONE</a></font></table>\n");
            sb.append("</div>\n");

            sb.append("</body>\n");
            sb.append("</html>\n");
        }

        isFirst = false;

        return sb.toString();
    }

    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem arises during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
        if (request.getParameter("done") != null) {
            sem.release();
        } else if(request.getParameter("page") != null ||
                  request.getParameter("bar") != null) {
            thePage = Integer.valueOf(request.getParameter("page"));
            theBar = Integer.valueOf(request.getParameter("bar"));
            emptyRequest(response);
        } else
            emptyRequest(response);
    }


    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        try {
            isHeader = Boolean.valueOf(cc.getProperty(DATA_PROPERTY_1));
            isType = Boolean.valueOf(cc.getProperty(DATA_PROPERTY_2));

            try {
                nrRows = Integer.valueOf(cc.getProperty(DATA_PROPERTY_3));
                nrColumns = Integer.valueOf(cc.getProperty(DATA_PROPERTY_4));
            } catch(Exception e) {
                throw new ComponentExecutionException(e);
            }

            if(nrRows <= 0 || nrColumns <=0)
                throw new ComponentExecutionException();

            inputContent = (Vector<Object[]>)cc.getDataComponentFromInput(DATA_INPUT);

            int totalRows = inputContent.size(),
                totalColumns = inputContent.elementAt(0).length;

            totalRows = (isHeader)? totalRows-1: totalRows; //exclude header row
            totalRows = (isType)? totalRows-1: totalRows; //exclude type row

            thePage = 0;
            theBar = 0;

            if(totalRows <= nrRows) { //only one page
                nrPages = 1;
                currentRow = new int[nrPages];
                nextRow = new int[nrPages];
                currentRow[nrPages-1] = 0;
                nextRow[nrPages-1] = totalRows-1;
            } else { //multiple pages
                boolean isDivisible = true;
                nrPages = totalRows/nrRows;
                if(nrPages*nrRows != totalRows) {//totalRows is not divisible by nrRows. There is remainder.
                    ++nrPages;
                    isDivisible = false;
                }
                currentRow = new int[nrPages];
                nextRow = new int[nrPages];
                int index = (isDivisible)? nrPages: nrPages-1;
                for(int i=0; i<index; i++) {
                    currentRow[i] = i*nrRows;
                    nextRow[i] = (i+1)*nrRows-1;
                }
                if(!isDivisible) {
                    currentRow[nrPages-1] = (nrPages-1)*nrRows;
                    nextRow[nrPages-1] = totalRows-1;
                }
            }

            if(totalColumns <= nrBars) { //only one bar
                nrBars = 1;
                currentColumn = new int[nrBars];
                nextColumn = new int[nrBars];
                currentColumn[nrBars-1] = 0;
                nextColumn[nrBars-1] = totalColumns-1;
            } else { //multiple bars
                boolean isDivisible = true;
                nrBars = totalColumns/nrColumns;
                if(nrBars*nrColumns != totalColumns) {
                    ++nrBars;
                    isDivisible = false;
                }
                currentColumn = new int[nrBars];
                nextColumn = new int[nrBars];
                int index = (isDivisible)? nrBars: nrBars-1;
                for(int i=0; i<index; i++) {
                    currentColumn[i] = i*nrColumns;
                    nextColumn[i] = (i+1)*nrColumns-1;
                }
                if(!isDivisible) {
                    currentColumn[nrBars-1] = (nrBars-1)*nrColumns;
                    nextColumn[nrBars-1] = totalColumns-1;
                }
            }

            isFirst = true;

            sInstanceID = cc.getExecutionInstanceID();
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            cc.stopWebUIFragment(this);
        } catch (Exception e) {
            throw new ComponentExecutionException(e);
        }

        cc.pushDataComponentToOutput(DATA_OUTPUT, inputContent);
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
