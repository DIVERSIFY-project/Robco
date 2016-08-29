package fr.inria.robco.metrics;

import java.util.Arrays;

/**
 * Created by aelie on 26/08/16.
 */
public class Grid {
        double[][] data;
        int width, height;

        Grid(int w, int h)
        {
            data = new double[w][h];
            width = w;
            height = h;
        }

        double get(int i, int j)
        {
            return data[i][j];
        }

        void set(int i, int j, double value) {
            data[i][j] = value;
        }

        double Total(){
            return Arrays.stream(data).mapToDouble(e -> Arrays.stream(e).sum()).sum();
        }

        static public Grid add(Grid a, Grid b)
        {
            Grid result = new Grid(a.width, a.height);
            for (int i = 0; i < result.width; i++) {
                for (int j = 0; j < result.height; j++) {
                    if(i < b.width && j < b.height) {
                        result.set(i, j, a.get(i, j) + b.get(i, j));
                    } else {
                        result.set(i, j, a.get(i, j));
                    }
                }
            }
            return result;
        }

        static public Grid substract(Grid a, Grid b)
        {
            Grid result = new Grid(a.width, a.height);
            for (int i = 0; i < result.width; i++) {
                for (int j = 0; j < result.height; j++) {
                    if(i < b.width && j < b.height) {
                        result.set(i, j, a.get(i, j) - b.get(i, j));
                    } else {
                        result.set(i, j, a.get(i, j));
                    }
                }
            }
            return result;
        }

        static public Grid multiply(Grid a, Grid b)
        {
            Grid result = new Grid(a.width, a.height);
            for (int i = 0; i < result.width; i++) {
                for (int j = 0; j < result.height; j++) {
                    if(i < b.width && j < b.height) {
                        result.set(i, j, a.get(i, j) * b.get(i, j));
                    } else {
                        result.set(i, j, a.get(i, j));
                    }
                }
            }
            return result;
        }

        static public Grid divide(Grid a, Grid b)
        {
            Grid result = new Grid(a.width, a.height);
            for (int i = 0; i < result.width; i++) {
                for (int j = 0; j < result.height; j++) {
                    if(i < b.width && j < b.height) {
                        if(b.get(i, j) != 0) {
                            result.set(i, j, a.get(i, j) / b.get(i, j));
                        } else {

                        }
                    } else {
                        result.set(i, j, a.get(i, j));
                    }
                }
            }
            return result;
        }

        /// <summary>
        /// Generic function maps (i,j) onto the given grid
        /// </summary>
        /// <param name="f"></param>
        /// <param name="a"></param>
        /// <returns></returns>
        static internal Grid Op(Func<int,int,double> f,Grid g)
    {
        int w = g.width, h = g.height;
        for (int i = 0; i < w; ++i)
            for (int j = 0; j < h; ++j)
                g[i, j] = f(i,j);
        return g;
    }
}
