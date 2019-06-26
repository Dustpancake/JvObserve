package Observe;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.concurrent.BlockingQueue;

public class Viewer extends JPanel implements Runnable {

    private final BlockingQueue<String> queue;
    private Thread thread;
    private VideoCapture camera;
    private long oldTime;
    private BufferedImage image;

    public Viewer() {
        queue = null;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(0);
    }

    public Viewer(BlockingQueue<String> q) {
        queue = q;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(0);
        setupPanel();

    }

    private void setupPanel() {
        nextImage();
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    public void nextImage() {
        Mat frame = new Mat();
        camera.read(frame);
        image = new BufferedImage(frame.width(), frame.height(), BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0,0, data);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        showImage(g);
    }

    @Override
    public void run() {
        int counter = 0;
        oldTime = System.currentTimeMillis();
        while(true) {
            repaint();
            nextImage();
            counter++;

            if (counter > 3) {
                checkQueue();
                counter = 0;
                System.out.println("FPS = " + getUpdatedFPS());

            }
        }
    }

    private void checkQueue() {
        String msg = queue.poll();
        if (msg != null) {
            if (msg.equals("capture")) {
                saveImage();
            }
        }
    }

    public void saveImage() {
        try {
            File outputfile = new File("output.png");
            ImageIO.write(image, "png", outputfile);
        } catch (Exception e) {
            System.out.println("Error: could not write image to file.");
        }
    }

    private void showImage(Graphics g) {
        g.drawImage(image, 0, 0, this);
        Toolkit.getDefaultToolkit().sync();
    }

    private int getUpdatedFPS() {
        long currentTime = System.currentTimeMillis();
        // frames divided by time difference
        int fps = (int) (1000 * 5 / (currentTime - oldTime));
        oldTime = System.currentTimeMillis();
        return fps;
    }

    public BufferedImage getImage() {
        return image;
    }
    public void setImage(BufferedImage img) {
        image = img;
    }

}
