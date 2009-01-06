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

package org.meandre.components.io.evernote;

import java.util.List;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import com.facebook.thrift.protocol.TBinaryProtocol;
import com.facebook.thrift.transport.THttpClient;

import com.evernote.edam.userstore.UserStore;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.edam.type.Note;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;

@Component(creator="Lily Dong",
           description="Download the notes on a Evernote account. Returns it " +
           		"as a NoteStore object.",
           name="EvernoteReader",
           tags="evernote, note, notebook")

public class EvernoteReader implements ExecutableComponent {
    @ComponentProperty(defaultValue="",
                       description="This property sets username.",
                       name="username")
    final static String DATA_PROPERTY_1 = "username";
    @ComponentProperty(defaultValue="",
                       description="This property sets password.",
                       name="password")
    final static String DATA_PROPERTY_2 = "password";
    @ComponentProperty(defaultValue="seasr-fd4932c2",
                       description="This property sets API key.",
                       name="key")
    final static String DATA_PROPERTY_3 = "key";
    
    @ComponentOutput(description="Output note store on Evernote." +
            "<br>TYPE: com.evernote.edam.notestore.NoteStore",
                     name="noteStore")             
    public final static String DATA_OUTPUT = "noteStore";
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        String username = cc.getProperty(DATA_PROPERTY_1),
               password = cc.getProperty(DATA_PROPERTY_2),  
               key = cc.getProperty(DATA_PROPERTY_3);
        
        String userStoreUrl = "https://lb.evernote.com/edam/user";
        String noteStoreUrlBase = "http://lb.evernote.com/edam/note/";
        
        try {
            THttpClient userStoreTrans = new THttpClient(userStoreUrl);
            TBinaryProtocol userStoreProt = 
                new TBinaryProtocol(userStoreTrans);
            
            UserStore.Client userStore = 
                new UserStore.Client(userStoreProt, userStoreProt);
            boolean versionOk = userStore.checkVersion("(Java)",
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
            if (!versionOk) {
                System.err.println("Incomatible EDAM client protocol version");
                System.exit(1);
            }
         
            AuthenticationResult authResult = 
                userStore.authenticate(username, password, key);        
            User user = authResult.getUser();
            String authToken = authResult.getAuthenticationToken();
            System.out.println("Notes for " + user.getUsername() + ":");
            String noteStoreUrl = noteStoreUrlBase + user.getShardId();
            THttpClient noteStoreTrans = 
                new THttpClient(noteStoreUrl);
            TBinaryProtocol noteStoreProt = 
                new TBinaryProtocol(noteStoreTrans);
            NoteStore.Client noteStore = 
                new NoteStore.Client(noteStoreProt, noteStoreProt);
            
            List<Notebook> notebooks = 
                (List<Notebook>)noteStore.listNotebooks(authToken);
            for (Notebook notebook : notebooks) {
                System.out.println("Notebook: " + notebook.getName());  
                NoteFilter filter = new NoteFilter();
                filter.setNotebookGuid(notebook.getGuid());
                NoteList noteList = noteStore.findNotes(authToken, filter, 0, 100);
                List<Note> notes = (List<Note>) noteList.getNotes();
                for (Note note : notes) {
                    System.out.println(" * " + note.getTitle());
                    //System.out.println(noteStore.getNoteContent(authToken, note.getGuid()));
                }
            }
            
            cc.pushDataComponentToOutput(DATA_OUTPUT, noteStore);
        } catch(Exception e) {
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
