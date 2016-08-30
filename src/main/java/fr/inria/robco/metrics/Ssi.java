package fr.inria.robco.metrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by aelie on 26/08/16.
 */
public class Ssi {

    public double index(short[] img1, short[] img2, int w, int h, int depth) {
        L = (1 << depth) - 1;
        return computeSSIM(convertLinear(img1, w, h), convertLinear(img2, w, h));
    }

    /// <summary>
    /// Take two System.Drawing.Bitmaps
    /// </summary>
    /// <param name="img1"></param>
    /// <param name="img2"></param>
    /// <returns></returns>
    public double index(BufferedImage img1, BufferedImage img2) {
        L = 255; // todo - this assumes 8 bit, but color conversion later is always 8 bit, so ok?
        return computeSSIM(convertBitmap(img1), convertBitmap(img2));
    }

    public double index(String filename1, String filename2) {
        BufferedImage b1 = null;
        BufferedImage b2 = null;
        try {
            b1 = ImageIO.read(new File(filename1));
            b2 = ImageIO.read(new File(filename2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return index(b1, b2);
    }

    double K1 = 0.01, K2 = 0.03;
    double L = 255;
    Grid window = gaussian(11, 1.5);

    double computeSSIM(Grid img1, Grid img2) {
        int f = (int) Math.max(1, Math.round(Math.min(img1.width, img1.height) / 256.0));
        if (f > 1) {
            img1 = subSample(img1, f);
            img2 = subSample(img2, f);
        }

        // normalize window - todo - do in window set {}
        double scale = 1.0 / window.total();
        window = Grid.op((i, j) -> window.get(i, j) * scale, window);

        // image statistics
        Grid mu1 = filter(img1, window);
        Grid mu2 = filter(img2, window);

        Grid mu1mu2 = Grid.multiply(mu1, mu2);
        Grid mu1SQ = Grid.multiply(mu1, mu1);
        Grid mu2SQ = Grid.multiply(mu2, mu2);

        Grid sigma12 = Grid.minus(filter(Grid.multiply(img1, img2), window), mu1mu2);
        Grid sigma1SQ = Grid.minus(filter(Grid.multiply(img1, img1), window), mu1SQ);
        Grid sigma2SQ = Grid.minus(filter(Grid.multiply(img2, img2), window), mu2SQ);

        // constants from the paper
        final double C1 = Math.pow(K1 * L, 2);
        final double C2 = Math.pow(K2 * L, 2);

        Grid ssim_map = null;
        if ((C1 > 0) && (C2 > 0)) {
            ssim_map = Grid.op((i, j) ->
                            (2 * mu1mu2.get(i, j) + C1) * (2 * sigma12.get(i, j) + C2) /
                                    (mu1SQ.get(i, j) + mu2SQ.get(i, j) + C1) / (sigma1SQ.get(i, j) + sigma2SQ.get(i, j) + C2),
                    new Grid(mu1mu2.width, mu1mu2.height));
        } else {
            Grid num1 = linear(2, mu1mu2, C1);
            Grid num2 = linear(2, sigma12, C2);
            Grid den1 = linear(1, Grid.plus(mu1SQ, mu2SQ), C1);
            Grid den2 = linear(1, Grid.plus(sigma1SQ, sigma2SQ), C2);

            Grid den = Grid.multiply(den1, den2); // total denominator
            ssim_map = new Grid(mu1.width, mu1.height);
            for (int i = 0; i < ssim_map.width; ++i)
                for (int j = 0; j < ssim_map.height; ++j) {
                    ssim_map.set(i, j, 1);
                    if (den.get(i, j) > 0)
                        ssim_map.set(i, j, num1.get(i, j) * num2.get(i, j) / (den1.get(i, j) * den2.get(i, j)));
                    else if ((den1.get(i, j) != 0) && (den2.get(i, j) == 0))
                        ssim_map.set(i, j, num1.get(i, j) / den1.get(i, j));
                }
        }

        // average all values
        return ssim_map.total() / (ssim_map.width * ssim_map.height);
    }

    static Grid gaussian(int size, double sigma) {
        Grid filter = new Grid(size, size);
        double s2 = sigma * sigma;
        double c = (size - 1) / 2.0;

        Grid.op((i, j) ->
                {
                    double dx = i - c;
                    double dy = j - c;
                    return Math.exp(-(dx * dx + dy * dy) / (2 * s2));
                },
                filter);
        double scale = 1.0 / filter.total();
        Grid.op((i, j) -> filter.get(i, j) * scale, filter);
        return filter;
    }

    static Grid subSample(Grid img, int skip) {
        int w = img.width;
        int h = img.height;
        double scale = 1.0 / (skip * skip);
        Grid ans = new Grid(w / skip, h / skip);
        for (int i = 0; i < w - skip; i += skip)
            for (int j = 0; j < h - skip; j += skip) {
                double sum = 0;
                for (int x = i; x < i + skip; ++x)
                    for (int y = j; y < j + skip; ++y)
                        sum += img.get(x, y);
                ans.set(i / skip, j / skip, sum * scale);
            }
        return ans;
    }

    static Grid filter(Grid a, Grid b) {
/*#if false
                int ax = a.width, ay = a.height;
                int bx = b.width, by = b.height;
                int bcx = (bx + 1) / 2, bcy = (by + 1) / 2; // center position
                var c = new Grid(ax - bx + 1, ay - by + 1);
                for (int i = bx - bcx + 1; i < ax - bx; ++i)
                    for (int j = by - bcy + 1; j < ay - by; ++j)
                    {
                        double sum = 0;
                        for (int x = bcx - bx + 1 + i; x < 1 + i + bcx; ++x)
                            for (int y = bcy - by + 1 + j; y < 1 + j + bcy; ++y)
                                sum += a[x, y] * b[bx - bcx - 1 - i + x, by - bcy - 1 - j + y];
                        c[i - bcx, j - bcy] = sum;
                    }
                return c;
#else*/
        // todo - check and clean this
        int ax = a.width, ay = a.height;
        int bx = b.width, by = b.height;
        int bcx = (bx + 1) / 2, bcy = (by + 1) / 2; // center position
        Grid c = new Grid(ax - bx + 1, ay - by + 1);
        for (int i = bx - bcx + 1; i < ax - bx; ++i)
            for (int j = by - bcy + 1; j < ay - by; ++j) {
                double sum = 0;
                for (int x = bcx - bx + 1 + i; x < 1 + i + bcx; ++x)
                    for (int y = bcy - by + 1 + j; y < 1 + j + bcy; ++y)
                        sum += a.get(x, y) * b.get(bx - bcx - 1 - i + x, by - bcy - 1 - j + y);
                c.set(i - bcx, j - bcy, sum);
            }
        return c;
//#endif
    }

    static Grid linear(double s, Grid a, double c) {
        return Grid.op((i, j) -> s * a.get(i, j) + c, new Grid(a.width, a.height));
    }

    static Grid convertLinear(short[] img, int w, int h) {
        return Grid.op((i, j) -> img[i + j * w], new Grid(w, h));
    }

    static Grid convertBitmap(BufferedImage bmp) {
        return Grid.op((i, j) -> {
            Color c = new Color(bmp.getRGB(i, j) >> 16 & 0xff, bmp.getRGB(i, j) >> 8 & 0xff, bmp.getRGB(i, j)& 0xff);
            return 0.3 * c.getRed() + 0.59 * c.getGreen() + 0.11 * c.getBlue();
        }, new Grid(bmp.getWidth(), bmp.getHeight()));
    }
}
