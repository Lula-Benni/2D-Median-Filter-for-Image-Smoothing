import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Lulamile Plati (PLTLUL001)
 * L
 * This class smoothens RGB color images using the mean filter
 */
public class MeanFilterSerial {
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
    MeanFilterSerial(String inputImageName, String outputImageName, String windowWidth) {
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
     * Calculates means of the surrounding pixels then use the means to get the new Pixels
     * @param inputImg 2D array containing the input Image pixels
     * @return 2D array of the new Pixels after applying the Mean Filter
     */
    public ArrayList<int[]> applyMeanFilter(ArrayList<int[]> inputImg){
        int width= img.getWidth();
        int height= img.getHeight();
        int size = windowWidth*windowWidth;
        ArrayList<int[]> outPutImg = new ArrayList<>();
        int[] alpha = new int[size];
        int[] red = new int[size];
        int[] blue = new int[size];
        int[] green = new int[size];
        for (int x=0;x<width;x++){
            int[] newPixels = new int[img.getHeight()];
            for (int y=0;y<height;y++){
                int alphaSum = 0;
                int redSum = 0;
                int blueSum = 0;
                int grSum = 0;
                int count = 0;
                for (int i=0;i<windowWidth;i++){
                    for (int j=0;j<windowWidth;j++){
                        if(y+j<height&&x+i< width) {
                            int newPixel = inputImg.get(i + x)[j + y];
                            alpha[count] = (newPixel >> 24) & 0xff;
                            alphaSum += alpha[count];
                            red[count] = (newPixel >> 16) & 0xff;
                            redSum += red[count];
                            blue[count] = newPixel & 0xff;
                            blueSum += blue[count];
                            green[count] = (newPixel >> 8) & 0xff;
                            grSum += green[count];
                            count++;
                        }
                    }
                }
                newPixels[y] = alphaSum<<24 | (redSum/size)<<16 | (grSum/size)<<8 | (blueSum/size);
            }
            outPutImg.add(newPixels);
        }
        return outPutImg;
    }
    /**
     * This method calculates the time the program takes to run
     * Then uses BufferedImage to create a new Image using the new Filtered Pixels
     * @param args accepts multiple arguments from the user
     */
    public static void main(String[] args) {
        MeanFilterSerial a = new MeanFilterSerial(args[0],args[1],args[2] );
        int windowWidth = Integer.parseInt(args[2]);
        if (windowWidth%2==0){
            System.out.println("window Width is incorrect");
            System.exit(0);
        }
        a.setImage();
        long startTime = System.currentTimeMillis();
        ArrayList<int[]> arr=a.applyMeanFilter(a.meanFilter());
        long endTime = System.currentTimeMillis();
        System.out.println("Mean Serial Run took " + (endTime - startTime) + " milliseconds.");
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
