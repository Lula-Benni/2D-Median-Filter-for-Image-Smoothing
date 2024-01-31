import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * @author Lulamile Plati (PLTLUL001)
 * L
 * This class smoothens RGB color images using the median filter
 */
public class MedianFilterSerial {
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
    MedianFilterSerial(String inputImageName, String outputImageName, String windowWidth){
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
     * This method uses int arrays of size windowWidth^2 stores the alpha, red, green and blue of the surrounding pixels
     * Sorts them and get the medians of the surrounding pixels then use the medians to get the new Pixels
     * @param inputImg 2D array containing the input Image pixels
     * @return 2D array of the new Pixels after applying the Median Filter
     */
    public ArrayList<int[]> applyMedianFilter(ArrayList<int[]> inputImg){
        int size = this.windowWidth*this.windowWidth;
        int mid = (size/2);
        int width = img.getWidth();
        int height = img.getHeight();
        int[] alphaMed = new int[size];
        int[] redMed = new int[size];
        int[] blueMed = new int[size];
        int[] greenMed = new int[size];
        ArrayList<int[]> newPixels=new ArrayList<>();
        for (int i=0;i<width;i++){
            int[] pixels = new int[img.getHeight()];
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
                pixels[j] = alphaMed[mid]<<24 | redMed[mid]<<16 | greenMed[mid]<<8 | blueMed[mid];
            }
            newPixels.add(pixels);
        }
        return newPixels;
    }
    /**
     * This method calculates the time the program takes to run
     * Then uses BufferedImage to create a new Image using the new Filtered Pixels
     * @param args accepts multiple arguments from the user
     */
    public static void main(String[] args) {
        MedianFilterSerial a = new MedianFilterSerial(args[0],args[1],args[2] );
        int windowWidth = Integer.parseInt(args[2]);
        if (windowWidth%2==0){
            System.out.println("window Width is incorrect");
            System.exit(0);
        }
        a.setImage();
        long startTime = System.currentTimeMillis();
        ArrayList<int[]> arr = a.applyMedianFilter(a.medianFilter());
        long endTime = System.currentTimeMillis();
        System.out.println("Median Serial Run took " + (endTime - startTime) + " milliseconds.");
        BufferedImage img = new BufferedImage(arr.size(),arr.get(0).length,BufferedImage.TYPE_INT_RGB);
        int width = arr.size();
        int height = arr.get(0).length;
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                img.setRGB(i,j,arr.get(i)[j]);
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
