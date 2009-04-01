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

package org.meandre.components.control;

//==============
// Java Imports
//==============

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//===============
// Other Imports
//===============

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.*;
import org.meandre.annotations.*;

/**
 * <p>Title: Fork Times Five</p>
 *
 * <p>Description: This component takes in any Java object and distributes
 * it (or copies of it) across multiple outputs.  The user can choose from five
 * object replication methods -- by reference, shallow copy by clone, deep copy
 * via serialization, copy via constructor, or copy via custom method.</p>
 *
 * <p>Copyright: UIUC Copyright (c) 2007</p>
 *
 * <p>Company: Automated Learning Group at NCSA, UIUC</p>
 *
 * @author Duane Searsmith
 * @version 1.0
 * Modified by Lily Dong
 */

@Component(creator="Duane Searsmith",

           description="<p>Description: This component takes in any Java object " +
           "and distributes it (or copies of it) across multiple outputs.  " +
           "The user can choose from five object replication methods -- by reference, " +
           "shallow copy by clone, deep copy via serialization, copy via constructor, " +
           "or copy via custom method.</p>",

           name="Fork x5",
           tags="clone, fork, copy",
           baseURL="meandre://seasr.org/components/")

public class ForkX5 extends AbstractExecutableComponent
{
    @ComponentInput(description="Object to replicate",
                    name= "object")
    final static String DATA_INPUT = "object";

    @ComponentOutput(description="Replicated object",
                     name="object_1")
    final static String DATA_OUTPUT_1 = "object_1";

    @ComponentOutput(description="Replicated object",
                     name="object_2")
    final static String DATA_OUTPUT_2 = "object_2";

    @ComponentOutput(description="Replicated object",
                     name="object_3")
    final static String DATA_OUTPUT_3 = "object_3";

    @ComponentOutput(description="Replicated object",
                     name="object_4")
    final static String DATA_OUTPUT_4 = "object_4";

    @ComponentOutput(description="Replicated object",
                     name="object_5")
    final static String DATA_OUTPUT_5 = "object_5";

    @ComponentProperty(defaultValue="0",
                      description="Replication mode (0 = by reference, 1 = clone shallow, 2 = serialize deep, 3 = constructor, 4 = custom)",
                      name="replication_mode")
    final static String DATA_PROPERTY_1 = "replication_mode";

    @ComponentProperty(defaultValue="",
                       description="Custom replication method name",
                       name="replication_method_name")
    final static String DATA_PROPERTY_2 = "replication_method_name";


    //==============
    // Data Members
    //==============

    //Property values for "Replication Mode".
    static public final int s_REFERENCE = 0;
    static public final int s_CLONE_SHALLOW = 1;
    static public final int s_SERIALIZE_DEEP = 2;
    static public final int s_CONSTRUCTOR = 3;
    static public final int s_CUSTOM = 4;

    //Data Port Names
    static public final String s_INPUT_1 = DATA_INPUT;
    static public final String s_OUTPUT_1 = DATA_OUTPUT_1;
    static public final String s_OUTPUT_2 = DATA_OUTPUT_2;
    static public final String s_OUTPUT_3 = DATA_OUTPUT_3;
    static public final String s_OUTPUT_4 = DATA_OUTPUT_4;
    static public final String s_OUTPUT_5 = DATA_OUTPUT_5;


    //Property Names
    static public final String s_REPLICATION_METHOD = DATA_PROPERTY_1;
    static public final String s_CUSTOM_COPY_METHOD_NAME = DATA_PROPERTY_2;

    //==============
    // Constructors
    //==============

    public ForkX5() {
    }

    //===============================================
    // Interface Implementation: ExecutableComponent
    //===============================================

    public void initialize() {
    }

    public void dispose() {
    }

    public void initializeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }
    public void disposeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }

    public void executeCallBack(ComponentContext cc)
    throws Exception {

        String fn = cc.getProperty(s_REPLICATION_METHOD);
        if (fn == null || fn.length() == 0) {
            throw new RuntimeException("No replication mode given.");
        }

        try {
            int repMode = Integer.parseInt(fn);
            Object dat = cc.getDataComponentFromInput(s_INPUT_1);
            switch (repMode) {
            case 0: //REFERENCE
                cc.pushDataComponentToOutput(s_OUTPUT_1, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_2, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_3, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_4, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_5, dat);
                break;

            case 1: //CLONE Shallow Copy

                Object obj = makeClone(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_1, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_2, obj);
                obj = null;
                obj = makeClone(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_3, obj);
                obj = null;
                obj= makeClone(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_4, obj);
                obj = null;
                obj = makeClone(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_5, obj);
                break;

            case 2: //SERIALIZE Deep Copy

                obj = null;
                obj = makeDeepCopy(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_1, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_2, obj);
                obj = null;
                obj = makeDeepCopy(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_3, obj);
                obj = null;
                obj = makeDeepCopy(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_4, obj);
                obj = null;
                obj = makeDeepCopy(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_5, obj);
                break;

            case 3: //CONSTRUCTOR

                obj = null;
                obj = copyViaConstructor(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_1, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_2, obj);
                obj = null;
                obj = copyViaConstructor(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_3, obj);
                obj = null;
                obj = copyViaConstructor(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_4, obj);
                obj = null;
                obj = copyViaConstructor(dat);
                cc.pushDataComponentToOutput(s_OUTPUT_5, obj);
                break;

            case 4: //CUSTOM

                String meth = cc.getProperty(s_CUSTOM_COPY_METHOD_NAME);
                obj = null;
                obj = copyViaCustomMethod(dat, meth);
                cc.pushDataComponentToOutput(s_OUTPUT_1, dat);
                cc.pushDataComponentToOutput(s_OUTPUT_2, obj);
                obj = null;
                obj = copyViaCustomMethod(dat, meth);
                cc.pushDataComponentToOutput(s_OUTPUT_3, obj);
                obj = null;
                obj = copyViaCustomMethod(dat, meth);
                cc.pushDataComponentToOutput(s_OUTPUT_4, obj);
                obj = null;
                obj = copyViaCustomMethod(dat, meth);
                cc.pushDataComponentToOutput(s_OUTPUT_5, obj);
                break;

            default:
                throw new Exception(
                        "No anticipated replication mode matches requested value.");
            }

        } catch (Exception e) {
            throw new ComponentExecutionException("ForkX2 Component: " +
                                                  e.getMessage(), e);
        }

    } // end method doit

    //=================
    // Package Methods
    //=================

    /**
     * Need to use reflection here because "Cloneable" interface is
     * a marker interface (i.e. it does not define the method "clone").
     * The incoming objects must still implement the cloneable interface
     * however, otherwise a CloneNotSupported error will be thrown.
     *
     * Also, the clone method in Object (which is protected) must be
     * overriden and declared public, otherwise the reflection methods
     * will fail since they can only access public methods by default.
     *
     * @param dat Object Object to clone.
     * @return Object Cloned object.
     * @throws Exception Reflection exception.
     */
    Object makeClone(Object dat) throws Exception {
        return dat.getClass().getMethod("clone", (Class[])null).invoke(dat, (Object[])null);
    }

    /**
     * These objects must be serializable.  If they are not then
     * when we attempt serialization an error will be generated.
     *
     * @param dat Object Object to copy.
     * @return Object Copied object.
     * @throws Exception IO Exception
     */
    Object makeDeepCopy(Object dat) throws Exception {
        Object obj = null;
        // Write the object out to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(dat);
        out.flush();
        out.close();

        // Make an input stream from the byte array and read
        // a copy of the object back in.
        ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        obj = in.readObject();
        return obj;
    }

    /**
     * These objects must supply a publically accessible constructor
     * that takes the same class type object as input.  Otherwise,
     * the reflection calls will generate an error.
     *
     * @param dat Object Object to copy.
     * @return Object Copied object.
     * @throws Exception Reflection exception.
     */
    @SuppressWarnings("unchecked")
	Object copyViaConstructor(Object dat) throws Exception{
        Object obj = null;
        Class cls = dat.getClass();
        Class[] clses = new Class[] {cls};
        Object[] objs = new Object[] {dat};
        obj = cls.getConstructor(clses).newInstance(objs);
        return obj;
    }

    /**
     * These objects must supply a publically accessible method, the
     * name of which matches the supplied string via property
     * s_CUSTOM_COPY_METHOD_NAME, and returns an object (copy) of the same type.
     * Otherwise, the reflection calls will generate an error.
     *
     * @param dat Object Object being copied.
     * @param meth String Name of copy method.
     * @return Object Copy of object.
     * @throws Exception Reflection exception.
     */
    @SuppressWarnings("unchecked")
	Object copyViaCustomMethod(Object dat, String meth) throws Exception {
        Object obj = null;
        if (meth == null || meth.length() == 0) {
            throw new RuntimeException(
                    "No custom method name provided.");
        }
        Class cls = dat.getClass();
        obj = dat.getClass().getMethod(meth, (Class[])null).invoke(dat, (Object[])null);
        if (!(cls.isInstance(obj))) {
            throw new RuntimeException("Copy produced is not an " +
                                       "instance of the input object.");
        }
        return obj;
    }

}
