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
 * Validator for <duration> datatype (W3C Schema Datatypes)
 *
 * @author Elena Litani
 * @author Gopal Sharma, SUN Microsystem Inc.
 * @version $Id$
 */
public class DurationDV extends AbstractDateTimeDV {

    // order-relation on duration is a partial order. The dates below are used to
    // for comparison of 2 durations, based on the fact that
    // duration x and y is x<=y iff s+x<=s+y
    // see 3.2.6 duration W3C schema datatype specs
    //
    // the dates are in format: {CCYY,MM,DD, H, S, M, MS, timezone}
    private final static int[][] DATETIMES= {
        {1696, 9, 1, 0, 0, 0, 0, 'Z'},
        {1697, 2, 1, 0, 0, 0, 0, 'Z'},
        {1903, 3, 1, 0, 0, 0, 0, 'Z'},
        {1903, 7, 1, 0, 0, 0, 0, 'Z'}};

    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return new DateTimeData(parse(content), this);
        } catch (Exception ex) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "duration"});
        }
    }

    /**
     * Parses, validates and computes normalized version of duration object
     *
     * @param str    The lexical representation of duration object PnYn MnDTnH nMnS
     * @param date   uninitialized date object
     * @return normalized date representation
     * @exception SchemaDateTimeException Invalid lexical representation
     */
    protected int[] parse(String str) throws SchemaDateTimeException{
        int len = str.length();
        int[] date=new int[TOTAL_SIZE];

        int start = 0;
        char c=str.charAt(start++);
        if ( c!='P' && c!='-' ) {
            throw new SchemaDateTimeException();
        }
        else {
            date[utc]=(c=='-')?'-':0;
            if ( c=='-' && str.charAt(start++)!='P' ) {
                throw new SchemaDateTimeException();
            }
        }

        int negate = 1;
        //negative duration
        if ( date[utc]=='-' ) {
            negate = -1;

        }
        //at least one number and designator must be seen after P
        boolean designator = false;

        int endDate = indexOf (str, start, len, 'T');
        if ( endDate == -1 ) {
            endDate = len;
        }
        //find 'Y'
        int end = indexOf (str, start, endDate, 'Y');
        if ( end!=-1 ) {
            //scan year
            date[CY]=negate * parseInt(str,start,end);
            start = end+1;
            designator = true;
        }

        end = indexOf (str, start, endDate, 'M');
        if ( end!=-1 ) {
            //scan month
            date[M]=negate * parseInt(str,start,end);
            start = end+1;
            designator = true;
        }

        end = indexOf (str, start, endDate, 'D');
        if ( end!=-1 ) {
            //scan day
            date[D]=negate * parseInt(str,start,end);
            start = end+1;
            designator = true;
        }

        if ( len == endDate && start!=len ) {
            throw new SchemaDateTimeException();
        }
        if ( len !=endDate ) {

            //scan hours, minutes, seconds
            //REVISIT: can any item include a decimal fraction or only seconds?
            //

            end = indexOf (str, ++start, len, 'H');
            if ( end!=-1 ) {
                //scan hours
                date[h]=negate * parseInt(str,start,end);
                start=end+1;
                designator = true;
            }

            end = indexOf (str, start, len, 'M');
            if ( end!=-1 ) {
                //scan min
                date[m]=negate * parseInt(str,start,end);
                start=end+1;
                designator = true;
            }

            end = indexOf (str, start, len, 'S');
            if ( end!=-1 ) {
                //scan seconds
                int mlsec = indexOf (str, start, end, '.');
                if ( mlsec >0 ) {
                    date[s]  = negate * parseInt (str, start, mlsec);
                    date[ms] = negate * parseInt (str, mlsec+1, end);
                }
                else {
                    date[s]=negate * parseInt(str, start,end);
                }
                start=end+1;
                designator = true;
            }
            // no additional data shouls appear after last item
            // P1Y1M1DT is illigal value as well
            if ( start != len || str.charAt(--start)=='T' ) {
                throw new SchemaDateTimeException();
            }
        }

        if ( !designator ) {
            throw new SchemaDateTimeException();
        }

        return date;
    }

    /**
     * Compares 2 given durations. (refer to W3C Schema Datatypes "3.2.6 duration")
     *
     * @param date1  Unnormalized duration
     * @param date2  Unnormalized duration
     * @param strict (min/max)Exclusive strict == true ( LESS_THAN ) or ( GREATER_THAN )
     *               (min/max)Inclusive strict == false (LESS_EQUAL) or (GREATER_EQUAL)
     * @return INDETERMINATE if the order relationship between date1 and date2 is indeterminate. 
     * EQUAL if the order relation between date1 and date2 is EQUAL.  
     * If the strict parameter is true, return LESS_THAN if date1 is less than date2 and
     * return GREATER_THAN if date1 is greater than date2. 
     * If the strict parameter is false, return LESS_THAN if date1 is less than OR equal to date2 and
     * return GREATER_THAN if date1 is greater than OR equal to date2 
     */
    protected  short compareDates(int[] date1, int[] date2, boolean strict) {

        //REVISIT: this is unoptimazed vs of comparing 2 durations
        //         Algorithm is described in 3.2.6.2 W3C Schema Datatype specs
        //

        //add constA to both durations
        short resultA, resultB= INDETERMINATE;

        //try and see if the objects are equal
        resultA = compareOrder (date1, date2);
        if ( resultA == 0 ) {
            return 0;
        }

        int[][] result = new int[2][TOTAL_SIZE];

        //long comparison algorithm is required
        int[] tempA = addDuration (date1, DATETIMES[0], result[0]);
        int[] tempB = addDuration (date2, DATETIMES[0], result[1]);
        resultA =  compareOrder(tempA, tempB);
        if ( resultA == INDETERMINATE ) {
            return INDETERMINATE;
        }

        tempA = addDuration(date1, DATETIMES[1], result[0]);
        tempB = addDuration(date2, DATETIMES[1], result[1]);
        resultB = compareOrder(tempA, tempB);
        resultA = compareResults(resultA, resultB, strict);
        if (resultA == INDETERMINATE) {
            return INDETERMINATE;
        }

        tempA = addDuration(date1, DATETIMES[2], result[0]);
        tempB = addDuration(date2, DATETIMES[2], result[1]);
        resultB = compareOrder(tempA, tempB);
        resultA = compareResults(resultA, resultB, strict);
        if (resultA == INDETERMINATE) {
            return INDETERMINATE;
        }

        tempA = addDuration(date1, DATETIMES[3], result[0]);
        tempB = addDuration(date2, DATETIMES[3], result[1]);
        resultB = compareOrder(tempA, tempB);
        resultA = compareResults(resultA, resultB, strict);

        return resultA;
    }

    private short compareResults(short resultA, short resultB, boolean strict){

      if ( resultB == INDETERMINATE ) {
            return INDETERMINATE;
        }
        else if ( resultA!=resultB && strict ) {
            return INDETERMINATE;
        }
        else if ( resultA!=resultB && !strict ) {
            if ( resultA!=0 && resultB!=0 ) {
                return INDETERMINATE;
            }
            else {
                return (resultA!=0)?resultA:resultB;
            }
        }
        return resultA;
    }

    private int[] addDuration(int[] date, int[] addto, int[] duration) {

        //REVISIT: some code could be shared between normalize() and this method,
        //         however is it worth moving it? The structures are different...
        //

        resetDateObj(duration);
        //add months (may be modified additionaly below)
        int temp = addto[M] + date[M];
        duration[M] = modulo (temp, 1, 13);
        int carry = fQuotient (temp, 1, 13);

        //add years (may be modified additionaly below)
        duration[CY]=addto[CY] + date[CY] + carry;
        
        //add seconds
        temp = addto[s] + date[s];
        carry = fQuotient (temp, 60);
        duration[s] =  mod(temp, 60, carry);

        //add minutes
        temp = addto[m] +date[m] + carry;
        carry = fQuotient (temp, 60);
        duration[m]= mod(temp, 60, carry);

        //add hours
        temp = addto[h] + date[h] + carry;
        carry = fQuotient(temp, 24);
        duration[h] = mod(temp, 24, carry);


        duration[D]=addto[D] + date[D] + carry;

        while ( true ) {

            temp=maxDayInMonthFor(duration[CY], duration[M]);
            if ( duration[D] < 1 ) { //original duration was negative
                duration[D] = duration[D] + maxDayInMonthFor(duration[CY], duration[M]-1);
                carry=-1;
            }
            else if ( duration[D] > temp ) {
                duration[D] = duration[D] - temp;
                carry=1;
            }
            else {
                break;
            }
            temp = duration[M]+carry;
            duration[M] = modulo(temp, 1, 13);
            duration[CY] = duration[CY]+fQuotient(temp, 1, 13);
        }

        duration[utc]='Z';
        return duration;
    }

    protected String dateToString(int[] date) {
        StringBuffer message = new StringBuffer(30);
        int negate = 1;
        if ( date[CY]<0 ) {
            message.append('-');
            negate=-1;
        }
        message.append('P');
        message.append(negate * date[CY]);
        message.append('Y');
        message.append(negate * date[M]);
        message.append('M');
        message.append(negate * date[D]);
        message.append('D');
        message.append('T');
        message.append(negate * date[h]);
        message.append('H');
        message.append(negate * date[m]);
        message.append('M');
        message.append(negate * date[s]);
        message.append('.');
        message.append(negate * date[ms]);
        message.append('S');

        return message.toString();
    }
}
