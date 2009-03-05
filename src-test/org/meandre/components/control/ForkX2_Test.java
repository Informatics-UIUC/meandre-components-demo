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


//===============
// Other Imports
//===============

import junit.framework.TestCase;

public class ForkX2_Test extends TestCase {

    //==============
    // Data Members
    //==============

    private ForkX2 _fork = null;

    //==============
    // Constructors
    //==============

    public ForkX2_Test() {}

    //================
    // Public Methods
    //================

    public void setUp() {
        _fork = new ForkX2();
    }

    public void tearDown() {}

    private class CloneTest1 implements Cloneable {
        public int[] _list = new int[1];
        public int _var = 0;

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }


    private class CloneTest2 implements Cloneable {
    }


    private class CloneTest3 {
    }


    public void testClone() {
        CloneTest1 ctOrig = new CloneTest1();
        ctOrig._list[0] = 10;
        ctOrig._var = 10;
        Object obj = null;
        try {
            obj = _fork.makeClone(ctOrig);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        CloneTest1 cpy = null;
        try {
            cpy = (CloneTest1) obj;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(cpy._var == ctOrig._var);
        assertTrue(cpy._list[0] == ctOrig._list[0]);
        cpy._var = 11;
        cpy._list[0] = 11;
        assertFalse(cpy._var == ctOrig._var);
        assertTrue(cpy._list[0] == ctOrig._list[0]);

        CloneTest2 ctOrig2 = new CloneTest2();
        try {
            obj = _fork.makeClone(ctOrig2);
            fail();
        } catch (Exception e) {
            this.assertTrue(e.getMessage(), true);
        }

        CloneTest3 ctOrig3 = new CloneTest3();
        try {
            obj = _fork.makeClone(ctOrig3);
            fail();
        } catch (Exception e) {
            this.assertTrue(e.getMessage(), true);
        }

    }


    public class DeepCopyTest2 {
        public int[] _list = new int[1];
        public int _var = 0;
        public DeepCopyTest2() {}
    }


    public void testDeepCopy() {
        DeepCopyTest1 ctOrig = new DeepCopyTest1();
        ctOrig._list[0] = 10;
        ctOrig._var = 10;
        Object obj = null;
        try {
            obj = _fork.makeDeepCopy(ctOrig);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        DeepCopyTest1 cpy = null;
        try {
            cpy = (DeepCopyTest1) obj;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(cpy._var == ctOrig._var);
        assertTrue(cpy._list[0] == ctOrig._list[0]);
        cpy._var = 11;
        cpy._list[0] = 11;
        assertFalse(cpy._var == ctOrig._var);
        assertFalse(cpy._list[0] == ctOrig._list[0]);

        DeepCopyTest2 ctOrig2 = new DeepCopyTest2();
        try {
            obj = _fork.makeDeepCopy(ctOrig2);
            fail();
        } catch (Exception e) {
            this.assertTrue(e.getMessage(), true);
        }
    }

    public class ConstructorCopyTest2 {
        public int[] _list = new int[1];
        public int _var = 0;
        public ConstructorCopyTest2() {}
    }


    public void testCopyByConstrutor() {
        ConstructorCopyTest1 ctOrig = new ConstructorCopyTest1();
        ctOrig._list[0] = 10;
        ctOrig._var = 10;
        Object obj = null;
        try {
            obj = _fork.copyViaConstructor(ctOrig);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        ConstructorCopyTest1 cpy = null;
        try {
            cpy = (ConstructorCopyTest1) obj;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        ConstructorCopyTest2 ctOrig2 = new ConstructorCopyTest2();
        try {
            obj = _fork.copyViaConstructor(ctOrig2);
            fail();
        } catch (Exception e) {
            this.assertTrue(e.getMessage(), true);
        }

    }

    public void testCopyByCustomMethod() {
        CustomCopyMethodTest1 ctOrig = new CustomCopyMethodTest1();
        ctOrig._list[0] = 10;
        ctOrig._var = 10;
        Object obj = null;
        try {
            obj = _fork.copyViaCustomMethod(ctOrig, "copy");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        CustomCopyMethodTest1 cpy = null;
        try {
            cpy = (CustomCopyMethodTest1) obj;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            obj = _fork.copyViaCustomMethod(ctOrig, "asrfasaf");
            fail();
        } catch (Exception e) {
            this.assertTrue(e.getMessage(), true);
        }

        try {
            obj = _fork.copyViaCustomMethod(ctOrig, "");
            fail();
        } catch (Exception e) {
            this.assertTrue(e.getMessage(), true);
        }

        try {
            obj = _fork.copyViaCustomMethod(ctOrig, null);
            fail();
        } catch (Exception e) {
            this.assertTrue(e.getMessage(), true);
        }

    }

}
