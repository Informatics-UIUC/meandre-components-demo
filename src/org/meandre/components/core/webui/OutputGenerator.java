package org.meandre.components.core.webui;


import java.util.StringTokenizer;

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


import java.util.Date;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

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
 *     the default template is OutputGenerator.vm
 * 
 *  local files take precedence over class path files 
 *  
 *  ComponentContextProperties  are in the velocity context as ccp
 *  ComponentContext             is in the velocity context as cc
 *  User supplied key=value pairs are in the velocity context as userMap
 *  
 * 
 */

@Component(creator="Mike Haberman",
           description="Generates output via a Velocity Template ",
           name="OutputGenerator",
           tags="string, visualization",
           baseURL="meandre://seasr.org/components/",
           dependency={"velocity-1.6.1-dep.jar"},
           resources={"OutputGenerator.vm"}
           )
public class OutputGenerator
    implements ExecutableComponent {
	
	@ComponentProperty(description = "The template name", 
			                  name = "template", 
			          defaultValue = "org/meandre/components/core/webui/OutputGenerator.vm")
	public final static String DATA_PROPERTY_TEMPLATE = "template";
	
	@ComponentProperty(description = "User supplied property list", 
			                  name = "properties", 
			          defaultValue = "key=value,author=mike")
	public final static String DATA_PROPERTY_HASHTABLE = "properties";
	
	
	//
	// this is a generic input, doesn't have to be used, up to the template
	// 
    // TODO: Perhaps this should be on the subclass, instead of here
	// that way this component can be used to generate output that does
	// not need any input (e.g. only properties)
	//
    @ComponentInput(description="Name of input port", name= "input")
    public final static String DATA_INPUT = "input";
   
    
    @ComponentOutput(description="Name of output port", name= "output")
    public final static String DATA_OUTPUT = "output";
    
   
    // convenience properties to easily push additional properties 
    // not needed, template can always do $ccp.getProperty("title")
    protected String[] templateVariables = {};
    
   
    protected void subExecute(ComponentContext cc) 
          throws ComponentExecutionException, 
                 ComponentContextException      
    {
    	//
    	// if TemmplateGUI is subclassed, you can use this
    	// extension point for execute() 
    	// the default is to push the input (a string) into the context
    	//
    	String input = (String)cc.getDataComponentFromInput(DATA_INPUT);
    	context.put("input", input);
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
        	
        	// allow subclasses to do execute
        	subExecute(cc);
        	
            String sInstanceId = cc.getExecutionInstanceID();
            context.put("sInstanceId", sInstanceId);
            context.put("cc", cc);
            context.put("gui", this);
            
            
            StringWriter sw = new StringWriter();
        	template.merge(context,sw);
        	String output = sw.toString();
        	
        	cc.pushDataComponentToOutput(DATA_OUTPUT, output);
                
            
        } catch (Exception e) {
            throw new ComponentExecutionException(e);
        }
    }
    
    protected void subInitialize(ComponentContextProperties ccp) {}
    
    protected VelocityContext context;
    protected Template template;
    
    /**
     * Called when a flow is started.
     */
    protected PrintStream console;
    public void initialize(ComponentContextProperties ccp) {
    	
    	console = ccp.getOutputConsole();
    	
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
            context.put("dir",  System.getProperty("user.dir"));
            context.put("date", new Date());
            context.put("ccp",  ccp);
            
            String toParse = ccp.getProperty(DATA_PROPERTY_HASHTABLE);
            HashMap<String,String> map = new HashMap<String,String>();
            StringTokenizer tokens = new StringTokenizer(toParse, ",");
            while (tokens.hasMoreTokens()){
            	String kv = tokens.nextToken();
            	int idx = kv.indexOf('=');
            	if (idx > 0) {
            		String key   = kv.substring(0,idx);
            		String value = kv.substring(idx+1);
            		map.put(key.trim(), value.trim());
            	}
            }
            context.put("userMap", map);
            
            // push property values to the context
            for (String name: templateVariables) {
            	String value = ccp.getProperty(name);
            	context.put(name,value);
            }
            
            subInitialize(ccp);
            
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
}


    /*
	formInputName = ccp.getProperty(DATA_PROPERTY_FORM);
    		
    Reader reader = 
        new InputStreamReader(getClass().getClassLoader().
                                   getResourceAsStream("history.vm"));
      VelocityContext context = new VelocityContext();
      context.put("location", location );
      context.put("weathers", weathers );
      StringWriter writer = new StringWriter();
      Velocity.evaluate(context, writer, "", reader);
      
      */

