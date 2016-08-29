package fr.inria.robco.metrics;

import java.util.Arrays;

/**
 * Created by aelie on 26/08/16.
 */
public class Grid {
    double[][] data;
    int width, height;

    Grid(int w, int h) {
        data = new double[w][h];
        width = w;
        height = h;
    }

    double get(int i, int j) {
        return data[i][j];
    }

    void set(int i, int j, double value) {
        data[i][j] = value;
    }

    double total() {
        return Arrays.stream(data).mapToDouble(e -> Arrays.stream(e).sum()).sum();
    }

    public interface IGridOperator {
        public double GridOperator(int i, int j);
    }

    static Grid op(IGridOperator igo, Grid g) {
        for (int i = 0; i < g.width; i++) {
            for (int j = 0; j < g.height; j++) {
                g.set(i, j, igo.GridOperator(i, j));
            }
        }
        return g;
    }

    static public Grid plus(Grid a, Grid b) {
        return op((i, j) -> a.get(i, j) + b.get(i ,j), new Grid(a.width, a.height));
    }

    static public Grid minus(Grid a, Grid b) {
        return op((i, j) -> a.get(i, j) - b.get(i ,j), new Grid(a.width, a.height));
    }

    static public Grid multiply(Grid a, Grid b) {
        return op((i, j) -> a.get(i, j) * b.get(i ,j), new Grid(a.width, a.height));
    }

    static public Grid divide(Grid a, Grid b) {
        return op((i, j) -> a.get(i, j) / b.get(i ,j), new Grid(a.width, a.height));
    }
}
