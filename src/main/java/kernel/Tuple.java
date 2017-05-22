/*
 * Copyright (c) 2015.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by ffiorett on 7/8/15.
 */
public class Tuple<T> {
    //public T[] values;
    public ArrayList<T> values;

    public Tuple(int lenght) {
        values = new ArrayList<T>()T[lenght];
    }

    public Tuple(T[] values) {
        this.values = new ArrayList<T>(Collections.nCopies(values.length, null));
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    public T[] getValues() {
        return values;
    }

    public T get(int pos) {
        return values[pos];
    }

    public void set(int pos, T val) {
        values[pos] = val;
    }

    /**
     * Note: Does not set through hard copy.
     */
    public void set(T[] values) {
//   This is to speed up access operation - assumes correct input.
//        if(this.values.length != values.length)
//            throw new IllegalArgumentException("Illegal size of values.");

        this.values = values;
    }

    public void copy(T[] values, int start, int len) {
        for (int i = start; i < start+len; i++) {
            this.values[i] = values[i];
        }
    }

    public boolean isValid() {
        for(T v : values) {
            if ((double)v == Constants.NaN) return false;
        }
        return true;
    }

    public int size() {
        return values.length;
    }

    @Override
    public boolean equals(Object o) {
//  This is to speed up access operation
//        if (this == o) return true;
//        if (!(o instanceof Tuple)) return false;

        Tuple tuple = (Tuple) o;
        for(int i=0; i<values.length; i++)
            if (values[i] != tuple.get(i)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return values != null ? Arrays.hashCode(values) : 0;
    }

    @Override
    public String toString() {
        return '<' + Arrays.toString(values) + '>';
    }
}
