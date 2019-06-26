package Observe;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class CommandLine {
    private Viewer view;
    public CommandLine() {
        view = new Viewer();
    }

    public void singleImage() {
        view.nextImage();
        // take two pictures as the first is always bad saturation
        view.nextImage();
        view.saveImage();
    }

    final int ACT_AVERAGE = 0;
    final int ACT_ADD = 1;

    public void longExposure(int second, int action) {
        List<BufferedImage> images = new ArrayList<>();
        long oldTime = System.currentTimeMillis();
        second *= 1000;
        createConvolution(action);

        do {
            view.nextImage();
            images.add(view.getImage());

        } while (second > System.currentTimeMillis() - oldTime);
        System.out.println("Obtained " + images.size() + " images in " + second/1000 + " seconds");
        view.setImage(catImgs(images));
        view.saveImage();
    }

    private void createConvolution(int action) {
        switch (action){
            case ACT_AVERAGE:
                convole = this::average;
                break;
            case ACT_ADD:
                convole = this::add;
                break;
        }
    }

    private BiFunction<Integer, Integer, Integer> convole;

    private BufferedImage catImgs(List<BufferedImage> images) {
        int WIDTH = images.get(0).getWidth();
        int HEIGHT = images.get(0).getHeight();


        BufferedImage output = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int outrgb[] = output.getRGB(0, 0, WIDTH, HEIGHT, new int[WIDTH * HEIGHT], 0, WIDTH);

        for (BufferedImage img : images) {
            int rgb[] = img.getRGB(0, 0, WIDTH, HEIGHT, new int[WIDTH * HEIGHT], 0, WIDTH);

            for (int i = 0; i < WIDTH - 1; ++i) {

                for (int j = 0; j < WIDTH; ++j) {

                    int index = i * HEIGHT + j;
                    // System.out.println("i, j, index " + i + " " + j + " " + index );
                    outrgb[index] = average(outrgb[index], rgb[index]);

                }
            }
        }
        output.setRGB(0, 0, WIDTH, HEIGHT, outrgb, 0, WIDTH);
        return output;
    }

    private int average(int argb1, int argb2){
        // average RGB value of 2 images
        return  (((argb1       & 0xFF) + (argb2       & 0xFF)) >> 1)       | //b
                (((argb1 >>  8 & 0xFF) + (argb2 >>  8 & 0xFF)) >> 1) << 8  | //g
                (((argb1 >> 16 & 0xFF) + (argb2 >> 16 & 0xFF)) >> 1) << 16 | //r
                (((argb1 >> 24 & 0xFF) + (argb2 >> 24 & 0xFF)) >> 1) << 24;  //a
    }

    private int add(int argb1, int argb2) {
        // add RGB pixel value up to a maximum
        int r, g, b;

        r = (argb1 >> 16) & 0xFF;
        g = (argb1 >> 8) & 0xFF;
        b = argb1 & 0xFF;

        r -= (argb2 >> 16) & 0xFF;
        g -= (argb2 >> 8) & 0xFF;
        b -= argb2 & 0xFF;

       return (int) ((r*r+g*g+b*b)/(255*255*4.0));

    }
}
