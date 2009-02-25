package org.meandre.components.core;

import java.util.concurrent.Semaphore;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import java.util.Date;
import java.io.StringWriter;
import java.util.Properties;
import java.util.HashMap;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

//import org.apache.velocity.exception.ParseErrorException;
//import org.apache.velocity.exception.ResourceNotFoundException;


/* 
 * template loading notes:
 * 
 *  templates are searched in
 *     1 local file system on the server: published_resources/templates (under the server install)
 *     2 local file system on the server: ./templates  where . is user.path
 *     3 on the classpath
 *     4 in any jars on the classpath
 *     
 *     the default template is GUITemplate.vm
 * 
 *  local files take precedence over class path files 
 *  
 *  ComponentContextProperties  are in the velocity context as ccp
 *  ComponentContext             is in the velocity context as cc
 *  HttpResponse                 is in the velocity context as response
 *  User supplied key=value pairs are in the velocity context as userMap
 *  
 *  TODO: it would be nice to be able to put the HttpRequest object
 *  in the velocity context
 */

@Component(creator="Mike Haberman",
           description="Generates and displays a webpage via a Velocity Template ",
           name="TemplateGUI",
           tags="string, visualization",
           dependency={"velocity-1.6.1-dep.jar"},
           resources={"GUITemplate.vm"},
           mode=Mode.webui)
public class TemplateGUI
    implements ExecutableComponent, WebUIFragmentCallback {
	
	@ComponentProperty(description = "The template name", name = "template", defaultValue = "org/meandre/components/core/GUITemplate.vm")
	final static String DATA_PROPERTY_TEMPLATE = "template";
	
	@ComponentProperty(description = "User supplied property list", name = "properties", defaultValue = "key=value,author=mike")
	final static String DATA_PROPERTY_HASHTABLE = "properties";
	
	//
	// this is a generic input, doesn't have to be used, up to the template
	// 
    @ComponentInput(description="Input to Process", name= "input")
    public final static String DATA_INPUT = "input";
    
    
    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    
    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem arose during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
    	
        try {
        	
        	context.put("response", response);
        	
        	// render the template
        	StringWriter sw = new StringWriter();
    		template.merge(context,sw);
    		String html =  sw.toString();
    		
    		// write the template to the response stream
            response.getWriter().println(html);
            
        } catch (Exception e) {
            throw new WebUIException(e);
        }
    }
    
    

    
    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem arised during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException 
    {
        String sDone = request.getParameter("done");
        if ( sDone!=null ) {
            sem.release();
        }
        else {
            emptyRequest(response);
        }
    }
    
    protected void subExecute(ComponentContext cc) 
          throws ComponentExecutionException, 
                 ComponentContextException      
    {
    	
    	// if TemmplateGUI is subclassed, you can use this
    	// extension point for execute() without having to 
    	// worry about semaphores, web fragments, 
    		
    }
    

    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) 
       throws ComponentExecutionException, ComponentContextException {
    	
        try {
        	
        	subExecute(cc);
        	
            String sInstanceId = cc.getExecutionInstanceID();
            context.put("sInstanceId", sInstanceId);
            context.put("cc", cc);
            
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            cc.stopWebUIFragment(this);
        } catch (Exception e) {
            throw new ComponentExecutionException(e);
        }
    }
    
   
    
    protected VelocityContext context;
    protected Template template;
    
    /**
     * Called when a flow is started.
     */
    public void initialize(ComponentContextProperties ccp) {
    	try {
    		
    		
    		Properties p = new Properties();
			p.setProperty("resource.loader", "file,class" );
			p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
		    p.setProperty("file.resource.loader.path", "published_resources/templates, WEB-INF/templates, ./templates");
		                               
		    Velocity.init( p );
		    
		    //Velocity.init("WEB-INF/velocity.properties");
		   
		    String templateName = ccp.getProperty(DATA_PROPERTY_TEMPLATE);
			template = Velocity.getTemplate(templateName);
    		
            /*
             *  Make a context object and populate with the data.  This
             *  is where the Velocity engine gets the data to resolve the
             *  references (ex. $date) in the template
             */

            context = new VelocityContext();
            context.put("dir", System.getProperty("user.dir"));
            context.put("date", new Date());
            context.put("ccp", ccp);
            
            String toParse = ccp.getProperty(DATA_PROPERTY_HASHTABLE);
            HashMap<String,String> map = new HashMap<String,String>();
            StringTokenizer tokens = new StringTokenizer(toParse, ",");
            while (tokens.hasMoreTokens()){
            	String kv = tokens.nextToken();
            	int idx = kv.indexOf('=');
            	if (idx > 0) {
            		String key = kv.substring(0,idx);
            		String value = kv.substring(idx+1);
            		map.put(key, value);
            	}
            }
            context.put("userMap", map);
            
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException(e);
    	}
    }
    
   
    /**
     * Call at the end of an execution flow.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
    
    /*
    Reader reader = 
        new InputStreamReader(getClass().getClassLoader().
                                   getResourceAsStream("history.vm"));
      VelocityContext context = new VelocityContext();
      context.put("location", location );
      context.put("weathers", weathers );
      StringWriter writer = new StringWriter();
      Velocity.evaluate(context, writer, "", reader);
      
      */
}

/* OLD WAY
private String getVizOLD() {
	
	
    StringBuffer sb = new StringBuffer();

    sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
    sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
    sb.append("<head>\n");
    sb.append("<title>String Visualization</title>\n");
    sb.append("<style type=\"text/css\" media=\"screen\">\n");
    sb.append("body {\n");
    sb.append("font-family: Verdana, sans-serif;\n");
    sb.append("font-size: 1em;\n");
    sb.append("}\n");
    sb.append("</style>\n");
    sb.append("</head>\n");

    sb.append("<body>\n");
    sb.append("<br /><br />\n");
    sb.append("<p>");
    StringTokenizer st = new StringTokenizer(inputString, "\n");
    while(st.hasMoreTokens()) 
        sb.append(st.nextToken()).append("<br/>");
    sb.append("</p>");
    sb.append("<div align=\"center\">\n");
    sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                 sInstanceID + "?done=true\">DONE</a></font></table>\n");
    sb.append("</div>\n");
    sb.append("</body>\n");
    sb.append("</html>\n");

    return sb.toString();
}
*/