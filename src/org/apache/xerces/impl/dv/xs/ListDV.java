/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.dv.xs;

import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;

/**
 * Represent the schema list types
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class ListDV extends TypeValidator{

    public short getAllowedFacets(){
          return (XSSimpleTypeDecl.FACET_LENGTH | XSSimpleTypeDecl.FACET_MINLENGTH | XSSimpleTypeDecl.FACET_MAXLENGTH | XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION | XSSimpleTypeDecl.FACET_WHITESPACE );
    }

    // this method should never be called: XSSimpleTypeDecl is responsible for
    // calling the item type for the convertion
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        return content;
    }

    // length of a list type is the number of items in the list
    public int getDataLength(Object value) {
        return ((ListData)value).length();
    }

    final static class ListData {
        final Object[] data;
        private String canonical;
        public ListData(Object[] data) {
            this.data = data;
        }
        public synchronized String toString() {
            if (canonical == null) {
                int len = data.length;
                StringBuffer buf = new StringBuffer();
                if (len > 0) {
                    buf.append(data[0].toString());
                }
                for (int i = 1; i < len; i++) {
                    buf.append(' ');
                    buf.append(data[i].toString());
                }
                canonical = buf.toString();
            }
            return canonical;
        }
        public int length() {
            return data.length;
        }
        public Object item(int index) {
            return data[index];
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof ListData))
                return false;
            Object[] odata = ((ListData)obj).data;
    
            int count = data.length;
            if (count != odata.length)
                return false;
    
            for (int i = 0 ; i < count ; i++) {
                if (!data[i].equals(odata[i]))
                    return false;
            }//end of loop
    
            //everything went fine.
            return true;
        }
    }
} // class ListDV

