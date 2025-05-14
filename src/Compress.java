import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Compress {
     public static double Compute_Distance(int[] vector1, double[] vector2) {
        double distance = 0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            distance += diff * diff;
        }
        return Math.sqrt(distance);
    }

    public static HashMap<String, double[]> loadCodeBook(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (HashMap<String, double[]>) ois.readObject();
        }
    }

    public static void saveCompressedBlocks(List<String> compressedBlocks, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String code : compressedBlocks) {
                writer.write(code);
                writer.newLine();
            }
        }
    }

    public static List<List<String>> Compress_test_img(BufferedImage image, int blockSize) throws IOException, ClassNotFoundException {
        String codebookDir = "C:\\Users\\mariam\\OneDrive\\Desktop\\vs code\\Java\\final_project\\Data\\Codebooks";
        String redCodebookPath = codebookDir + "\\red_codebook.ser";
        String greenCodebookPath = codebookDir + "\\green_codebook.ser";
        String blueCodebookPath = codebookDir + "\\blue_codebook.ser";
        
        List<int[]> redBlocks = new ArrayList<>();
        List<int[]> greenBlocks = new ArrayList<>();
        List<int[]> blueBlocks = new ArrayList<>();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int y = 0; y <= height - blockSize; y += blockSize) {
            for (int x = 0; x <= width - blockSize; x += blockSize) {
                int[] redBlock = new int[blockSize * blockSize];
                int[] greenBlock = new int[blockSize * blockSize];
                int[] blueBlock = new int[blockSize * blockSize];
                
                int index = 0;
                for (int dy = 0; dy < blockSize; dy++) {
                    for (int dx = 0; dx < blockSize; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        int redPixel = (rgb >> 16) & 0xFF;
                        int greenPixel = (rgb >> 8) & 0xFF;
                        int bluePixel = rgb & 0xFF;
                        
                        redBlock[index] = redPixel;
                        greenBlock[index] = greenPixel;
                        blueBlock[index] = bluePixel;
                        index++;
                    }
                }
                
                redBlocks.add(redBlock);
                greenBlocks.add(greenBlock);
                blueBlocks.add(blueBlock);
            }
        }
        
        HashMap<String, double[]> redCodeBook = loadCodeBook(redCodebookPath);
        HashMap<String, double[]> greenCodeBook = loadCodeBook(greenCodebookPath);
        HashMap<String, double[]> blueCodeBook = loadCodeBook(blueCodebookPath);
        
        List<String> compressedRedBlocks = new ArrayList<>();
        List<String> compressedGreenBlocks = new ArrayList<>();
        List<String> compressedBlueBlocks = new ArrayList<>();
        
        for (int[] block : redBlocks) {
            String bestCode = "";
            double minDist = Double.MAX_VALUE;
            for (String code : redCodeBook.keySet()) {
                double dist = Compute_Distance(block, redCodeBook.get(code));
                if (dist < minDist) {
                    minDist = dist;
                    bestCode = code;
                }
            }
            compressedRedBlocks.add(bestCode);
        }
        
        for (int[] block : greenBlocks) {
            String bestCode = "";
            double minDist = Double.MAX_VALUE;
            for (String code : greenCodeBook.keySet()) {
                double dist = Compute_Distance(block, greenCodeBook.get(code));
                if (dist < minDist) {
                    minDist = dist;
                    bestCode = code;
                }
            }
            compressedGreenBlocks.add(bestCode);
        }
        
        for (int[] block : blueBlocks) {
            String bestCode = "";
            double minDist = Double.MAX_VALUE;
            for (String code : blueCodeBook.keySet()) {
                double dist = Compute_Distance(block, blueCodeBook.get(code));
                if (dist < minDist) {
                    minDist = dist;
                    bestCode = code;
                }
            }
            compressedBlueBlocks.add(bestCode);
        }
        
        List<List<String>> compressedChannels = new ArrayList<>();
        compressedChannels.add(compressedRedBlocks);
        compressedChannels.add(compressedGreenBlocks);
        compressedChannels.add(compressedBlueBlocks);
        
        return compressedChannels;
    }

    public static double calculateCompressionSize(int blockSize, int imageWidth, int imageHeight) {
        int codeBookSize = 256;
        int codeBookSize_compressed = codeBookSize; 
        double compressedImageSize = ((imageHeight * imageWidth) /(blockSize * blockSize ));

        double total = codeBookSize_compressed + compressedImageSize;

        int originalImageSize = imageWidth * imageHeight ;
        return (double) total/originalImageSize * 100;
    }

    public static void main(String[] args) {
        try {
            String imgPath = "C:\\Users\\mariam\\OneDrive\\Desktop\\vs code\\Java\\final_project\\Data\\Test\\14.png";
            BufferedImage image = ImageIO.read(new File(imgPath));
            int blockSize = 2;
            
            List<List<String>> compressedChannels = Compress_test_img(image, blockSize);
            
            saveCompressedBlocks(compressedChannels.get(0), "compressed_red.txt");
            saveCompressedBlocks(compressedChannels.get(1), "compressed_green.txt");
            saveCompressedBlocks(compressedChannels.get(2), "compressed_blue.txt");
            
            System.out.println("Compression complete. Compressed blocks saved.");
            System.out.println("Compression ratio: " + calculateCompressionSize(blockSize, image.getWidth(), image.getHeight()) + "%");
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
