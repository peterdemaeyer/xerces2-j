/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Validator for <gDay> datatype (W3C Schema datatypes)
 *
 * @author Elena Litani
 * @author Gopal Sharma, SUN Microsystem Inc.
 * @version $Id$
 */
public class DayDV extends AbstractDateTimeDV {

    //size without time zone: ---09
    private final static int DAY_SIZE=5;

    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        try{
            return new DateTimeData(parse(content), this);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "gDay"});
        }
    }

    /**
     * Parses, validates and computes normalized version of gDay object
     *
     * @param str    The lexical representation of gDay object ---DD
     *               with possible time zone Z or (-),(+)hh:mm
     *               Pattern: ---(\\d\\d)(Z|(([-+])(\\d\\d)(:(\\d\\d))?
     * @param date   uninitialized date object
     * @return normalized date representation
     * @exception SchemaDateTimeException Invalid lexical representation
     */
    protected int[] parse(String str) throws SchemaDateTimeException {
        int len = str.length();
        int[] date=new int[TOTAL_SIZE];
        int[] timeZone = new int[2];

        if (str.charAt(0)!='-' || str.charAt(1)!='-' || str.charAt(2)!='-') {
            throw new SchemaDateTimeException ("Error in day parsing");
        }

        //initialize values
        date[CY]=YEAR;
        date[M]=MONTH;

        date[D]=parseInt(str, 3,5);

        if ( DAY_SIZE<len ) {
            int sign = findUTCSign(str, DAY_SIZE, len);
            if ( sign<0 ) {
                throw new SchemaDateTimeException ("Error in day parsing");
            }
            else {
                getTimeZone(str, date, sign, len, timeZone);
            }
        }

       //validate and normalize
        validateDateTime(date, timeZone);

        if ( date[utc]!=0 && date[utc]!='Z' ) {
            normalize(date, timeZone);
        }
        return date;
    }

    /**
     * Converts gDay object representation to String
     *
     * @param date   gDay object
     * @return lexical representation of gDay: ---DD with an optional time zone sign
     */
    protected String dateToString(int[] date) {
        StringBuffer message = new StringBuffer(6);
        message.append('-');
        message.append('-');
        message.append('-');
        append(message, date[D], 2);
        append(message, (char)date[utc], 0);
        return message.toString();
    }

}

