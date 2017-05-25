package kernel;

/**
 * Created by nandofioretto on 5/25/17.
 */
public class Commons {

    public static double getAverage(double[] array) {
        double avg = 0;
        for (double a : array) {
            avg += a;
        }
        return (avg/(double)array.length);
    }

    public static double getMin(double[] array) {
        double min = array[0];
        for (int i = 1; i < array.length; i++)
            if (array[i] < min)
                min = array[i];
        return min;
    }

    public static double getMax(double[] array) {
        double max = array[0];
        for (int i = 1; i < array.length; i++)
            if (array[i] > max)
                max = array[i];
        return max;
    }

    public static void addValue(double[] array, double value) {
        for (int i = 0; i < array.length; i++)
            array[i] += value;
    }

    public static void mulValue(double[] array, double value) {
        for (int i = 0; i < array.length; i++)
            array[i] *= value;
    }

    public static void rmValue(double[] array, double value) {
        for (int i = 0; i < array.length; i++)
            array[i] -= value;
    }

    public static void addArray(double[] out, double[] in) {
        assert (out.length == in.length);
        for (int i = 0; i <out.length; i++)
            out[i] += in[i];
    }

}
