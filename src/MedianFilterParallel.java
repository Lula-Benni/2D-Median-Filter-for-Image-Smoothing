import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
/**
 * @author Lulamile Plati (PLTLUL001)
 * L
 * This class smoothens RGB color images Parallel using the median filter
 */
public class MedianFilterParallel {
    String inputImageName;
    String outputImageName;
    int windowWidth;
    BufferedImage img;
    /**
     * 3 parameter Constructor
     * @param inputImageName  Name of the input image that will be filtered including the file extension
     * @param outputImageName Name of the output image including the file extension
     * @param windowWidth The width of the square window and it must be odd and positive Integer
     */
    MedianFilterParallel(String inputImageName, String outputImageName, String windowWidth){
        this.inputImageName=inputImageName;
        this.outputImageName=outputImageName;
        this.windowWidth=Integer.parseInt(windowWidth);
    }
    /**
     * This method reads the input image into BufferedImage
     * Prints "Image not Found" if the image is not found the exits
     */
    public void setImage(){
        try {
            img = ImageIO.read(new File(inputImageName));
        }
        catch (IOException e){
            System.out.println("Image not Found");
            System.exit(0);
        }
        System.out.println(img);
        System.out.println(inputImageName+" "+outputImageName+" "+windowWidth);
    }
    /**
     * This method stores the pixels of the input image using a 2D array
     * @return 2D array containing input image pixels
     */
    public ArrayList<int[]> medianFilter(){
        int height = img.getHeight();
        int width = img.getWidth();
        ArrayList<int[]> inputImg = new ArrayList<>();

        for (int x=0;x<width;x++){
            int[] pixels = new int[height];
            for (int y=0;y<height;y++){
                pixels[y]=img.getRGB(x,y);
            }
            inputImg.add(pixels);
        }
        return inputImg;
    }
    /**
     * @author Lulamile Plati
     * This class smoothens RGB color images Parallel using the median filter and the ForkJoinPool
     * Uses RecursiveTask because it will return the 2D array of the new Pixels after applying the Median Filter
     */
    public static class MedianFilterThread extends RecursiveTask<ArrayList<int[]>>{
        /**
         * lo int   low value
         * hi int   high value
         */
        int lo,hi;
        /**
         * outputImageName String   out put image name
         */
        String outputImageName;
        /**
         * SEQUENTIAL_CUTOFF static final int   determine when the parallel part of the code must start
         */
        private static final int SEQUENTIAL_CUTOFF =1000;
        /**
         * inputImg int    2D array for the input image pixels
         */
        ArrayList<int[]> inputImg;
        /**
         * windowWidth int  The width of the square window
         */
        int windowWidth;
        /**
         * newPixels int    2D array  for the new Pixels after filtering
         */
        ArrayList<int[]> newPixels = new ArrayList<>();
        /**
         * 5 parameter Constructor
         * @param inputImg 2D array of the Input Image Pixels
         * @param outputImageName Name of the output image
         * @param windowWidth The width of the square window
         * @param lo Starting value (low)
         * @param hi End value (high)
         */
        public MedianFilterThread(ArrayList<int[]> inputImg, String outputImageName, int windowWidth, int lo, int hi){
            this.inputImg=inputImg;
            this.outputImageName=outputImageName;
            this.windowWidth=windowWidth;
            this.lo=lo;
            this.hi=hi;
        }
        /**
         * Performs Parallel programming using fork and join if the image width is greater than the SEQUENTIAL CUTOFF
         * @return 2D array of the new Pixels after applying the Median Filter
         */
        public ArrayList<int[]> compute(){
            int width= inputImg.size();
            int height= inputImg.get(0).length;
            int size = windowWidth*windowWidth;
            int median = (size)/2;
            int[] alphaMed = new int[size];
            int[] redMed = new int[size];
            int[] blueMed = new int[size];
            int[] greenMed = new int[size];
            if (hi-lo < SEQUENTIAL_CUTOFF){
                for (int i=lo;i<hi;i++){
                    int[] pixels = new int[height];
                    for (int j=0;j<height;j++){
                        int count=0;
                        for (int x=0;x<windowWidth;x++){
                            for (int y=0;y<windowWidth;y++) {
                                if (x + i < width && y + j < height) {
                                    int pixel = inputImg.get(i + x)[j + y];
                                    alphaMed[count] = (pixel >> 24) & 0xff;
                                    redMed[count] = (pixel >> 16) & 0xff;
                                    blueMed[count] = pixel & 0xff;
                                    greenMed[count] = (pixel >> 8) & 0xff;
                                    count++;
                                }
                            }
                        }
                        Arrays.sort(alphaMed);
                        Arrays.sort(redMed);
                        Arrays.sort(blueMed);
                        Arrays.sort(greenMed);
                        pixels[j] = alphaMed[median]<<24 | redMed[median]<<16 | greenMed[median]<<8 | blueMed[median];
                    }
                    newPixels.add(pixels);
                }
            }
            else {
                int mid=(hi+lo)/2;
                MedianFilterThread left = new MedianFilterThread(inputImg,outputImageName,windowWidth,lo,mid);
                MedianFilterThread right = new MedianFilterThread(inputImg,outputImageName,windowWidth,mid,hi);
                left.fork();
                ArrayList<int[]> rightAns = right.compute();
                ArrayList<int[]> leftAns = left.join();
                newPixels.addAll(leftAns);
                newPixels.addAll(rightAns);
            }
            return newPixels;
        }
    }
    /**
     * This method calculates the time the program takes to run
     * Uses ForkJoinPool to run the Parallel program
     * Then uses BufferedImage to create a new Image using the new Filtered Pixels
     * @param args accepts multiple arguments from the user
     */
    public static void main(String[] args) {
        MedianFilterParallel a = new MedianFilterParallel(args[0],args[1],args[2]);
        final ForkJoinPool fjPool = new ForkJoinPool();
        int windowWidth = Integer.parseInt(args[2]);
        if (windowWidth%2==0){
            System.out.println("window Width is incorrect");
            System.exit(0);
        }
        a.setImage();
        long startTime = System.currentTimeMillis();
        ArrayList<int[]> arr = a.medianFilter();
        MedianFilterThread b = new MedianFilterThread(arr,a.outputImageName,a.windowWidth,0,arr.size());
        fjPool.invoke(b);
        ArrayList<int[]> outPut = b.newPixels;
        long endTime = System.currentTimeMillis();
        System.out.println("Median Parallel run took " + (endTime - startTime) + " milliseconds.");
        BufferedImage img = new BufferedImage(outPut.size(),outPut.get(0).length,BufferedImage.TYPE_INT_RGB);
        int width = outPut.size();
        int height = outPut.get(0).length;
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                img.setRGB(i,j,outPut.get(i)[j]);
            }
        }
        try{
            ImageIO.write(img,"jpeg",new File(a.outputImageName));}
        catch (IOException e){
            System.out.println("Cannot create Image");
            System.exit(0);
        }
    }
}

