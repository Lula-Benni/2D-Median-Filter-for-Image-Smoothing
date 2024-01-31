import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
/**
 * @author Lulamile Plati (PLTLUL001)
 * L
 * This class smoothens RGB color images Parallel using the mean filter
 */
public class MeanFilterParallel{
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
    public MeanFilterParallel(String inputImageName, String outputImageName,String windowWidth){
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
    public ArrayList<int[]> meanFilter(){
        int height = img.getHeight();
        int width = img.getWidth();
        ArrayList<int[]> inputImg = new ArrayList<>();

        for (int x=0;x<width;x++){
            int[] arr = new int[height];
            for (int y=0;y<height;y++){
                arr[y]=img.getRGB(x,y);
            }
            inputImg.add(arr);
        }
        return inputImg;
    }
    /**
     * @author Lulamile Plati
     * This class smoothens RGB color images Parallel using the mean filter and the ForkJoinPool
     * Uses RecursiveTask because it will return the 2D array of the new Pixels after applying the Mean Filter
     */
    public static class MeanFilterThread extends RecursiveTask<ArrayList<int[]>>{
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
        private static final int SEQUENTIAL_CUTOFF =600;
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
        ArrayList<int[]> outPutImg = new ArrayList<>();
        /**
         * 5 parameter Constructor
         * @param inputImg 2D array of the Input Image Pixels
         * @param outputImageName Name of the output image
         * @param windowWidth The width of the square window
         * @param lo Starting value (low)
         * @param hi End value (high)
         */
        public MeanFilterThread(ArrayList<int[]> inputImg, String outputImageName, int windowWidth, int lo, int hi){
            this.inputImg=inputImg;
            this.outputImageName=outputImageName;
            this.windowWidth=windowWidth;
            this.lo=lo;
            this.hi=hi;
        }
        /**
         * Performs Parallel programming using fork and join if the image width is greater than the SEQUENTIAL CUTOFF
         * @return 2D array of the new Pixels after applying the Mean Filter
         */
        protected ArrayList<int[]> compute(){
            int width= inputImg.size();
            int height= inputImg.get(0).length;
            int size = windowWidth*windowWidth;
            int[] alpha = new int[size];
            int[] red = new int[size];
            int[] blue = new int[size];
            int[] green = new int[size];
            if (hi-lo < SEQUENTIAL_CUTOFF){
                for (int x=lo;x<hi;x++){
                    int[] newPixels = new int[height];
                    for (int y=0;y<height;y++){
                        int alphaSum = 0;
                        int redSum = 0;
                        int blueSum = 0;
                        int grSum = 0;
                        int count = 0;
                        for (int i=0;i<windowWidth;i++){
                            for (int j=0;j<windowWidth;j++){
                                if(y+i<height&&x+j< width) {
                                    int pixel = inputImg.get(x+j)[y+i];
                                    alpha[count] = (pixel >> 24) & 0xff;
                                    alphaSum += alpha[count];
                                    red[count] = (pixel >> 16) & 0xff;
                                    redSum += red[count];
                                    blue[count] = pixel & 0xff;
                                    blueSum += blue[count];
                                    green[count] = (pixel >> 8) & 0xff;
                                    grSum += green[count];
                                    count++;
                                }
                            }
                        }
                        newPixels[y] = alphaSum<<24 | (redSum/size)<<16 | (grSum/size)<<8 | (blueSum/size);
                    }
                    outPutImg.add(newPixels);
                }
            }
            else{
                int mid=(hi+lo)/2;
                MeanFilterThread left = new MeanFilterThread(inputImg,outputImageName,windowWidth,lo,mid);
                MeanFilterThread right = new MeanFilterThread(inputImg,outputImageName,windowWidth,mid,hi);
                left.fork();
                ArrayList<int[]> rightAns = right.compute();
                ArrayList<int[]> leftAns = left.join();
                outPutImg.addAll(leftAns);
                outPutImg.addAll(rightAns);
            }
            return outPutImg;
        }
    }
    /**
     * This method calculates the time the program takes to run
     * Uses ForkJoinPool to run the Parallel program
     * Then uses BufferedImage to create a new Image using the new Filtered Pixels
     * @param args accepts multiple arguments from the user
     */
    public static void main(String[] args) {
        final ForkJoinPool fjPool = new ForkJoinPool();
        MeanFilterParallel a = new MeanFilterParallel(args[0],args[1],args[2]);
        int windowWidth = Integer.parseInt(args[2]);
        if (windowWidth%2==0){
            System.out.println("window Width is incorrect");
            System.exit(0);
        }
        a.setImage();
        long startTime = System.currentTimeMillis();
        ArrayList<int[]> arr = a.meanFilter();
        MeanFilterThread b = new MeanFilterThread(arr,a.outputImageName,a.windowWidth,0,arr.size());
        //long startTime = System.currentTimeMillis();
        fjPool.invoke(b);
        ArrayList<int[]> outPut = b.outPutImg;
        long endTime = System.currentTimeMillis();
        System.out.println("Mean Parallel run took " + (endTime - startTime) + " milliseconds.");
        BufferedImage img = new BufferedImage(outPut.size(),outPut.get(0).length,BufferedImage.TYPE_INT_RGB);
        int width = outPut.size();
        int height = outPut.get(0).length;
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                img.setRGB(i,j,b.outPutImg.get(i)[j]);
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
