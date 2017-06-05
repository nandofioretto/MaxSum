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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ffiorett on 7/7/15.
 */
public class TableConstraint implements Constraint {
    private HashMap<Tuple, Double> relation;
    private double defaultValue;
    private List<Variable> scope;
    private String name;
    private long ID;
    // Table Bounds
    private double bestValue;
    private double worstValue;

    public TableConstraint(String name, long ID, ArrayList<Variable> scope, double defaultValue) {
        this.relation = new HashMap<>();
        this.scope =  new ArrayList<>();

        this.name = name;
        this.ID = ID;
        this.scope = scope;
        this.defaultValue = defaultValue;
        this.bestValue  = Constants.NaN;
        this.worstValue = Constants.NaN;
    }

    public void addValue(Tuple key, double value, int optType) {
        relation.put(key, value);

        if (optType == Constants.OPT_MAXIMIZE) {
            if (bestValue < value || bestValue == Constants.NaN) bestValue = value;
            if (worstValue > value || worstValue == Constants.NaN) worstValue = value;
        } else {
            if (bestValue > value || bestValue == Constants.NaN) bestValue = value;
            if (worstValue < value || worstValue == Constants.NaN) worstValue = value;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getID() {
        return this.ID;
    }

    @Override
    public void setID(long ID) {
        this.ID = ID;
    }

    @Override
    public double getValue(Tuple key) {
        return relation.containsKey(key) ? relation.get(key) : defaultValue;
    }

    @Override
    public double getBestValue() {
        return bestValue;
    }

    @Override
    public double getWorstValue() {
        return worstValue;
    }

    @Override
    public List<Variable> getScope() {
        return this.scope;
    }

    @Override
    public Variable getScope(int pos) {
        return scope.get(pos);
    }

    @Override
    public double getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableConstraint that = (TableConstraint) o;

        return ID == that.ID;
    }

    @Override
    public int hashCode() {
        return (int) (ID ^ (ID >>> 32));
    }

    @Override
    public String toString() {
        String ret = "TableConstraint " + name + " scope: {";
        for(Variable s : scope) ret += s.getName() + " ";
        ret += "}";

        ret += " def value = " + defaultValue + " best/worst=["
        + bestValue + ", " + worstValue + "]\n";

        for (Map.Entry<Tuple, Double> entry : relation.entrySet()) {
            ret += "";
            for(Integer v : entry.getKey().getValues())
                ret += " " + v ;
            double val = entry.getValue();
            ret += ": ";
            ret += val == Constants.infinity ? "inf" : val == -Constants.infinity ? "-inf" : val + "\n";
        }
        return ret;
    }

}
