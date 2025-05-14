import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Decompress {
        public static HashMap<String, double[]> loadCodeBook(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (HashMap<String, double[]>) ois.readObject();
        }
    }

    public static List<String> loadCompressedBlocks(String filePath) throws IOException {
        List<String> compressedBlocks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                compressedBlocks.add(line);
            }
        }
        return compressedBlocks;
    }

    public static BufferedImage Decompress_test_img(String compressedRedPath, String compressedGreenPath, String compressedBluePath, int blockSize, int width, int height) throws IOException, ClassNotFoundException {
        String codebookDir = "C:\\Users\\mariam\\OneDrive\\Desktop\\vs code\\Java\\final_project\\Data\\Codebooks";
        String redCodebookPath = codebookDir + "\\red_codebook.ser";
        String greenCodebookPath = codebookDir + "\\green_codebook.ser";
        String blueCodebookPath = codebookDir + "\\blue_codebook.ser";
        
        HashMap<String, double[]> redCodeBook = loadCodeBook(redCodebookPath);
        HashMap<String, double[]> greenCodeBook = loadCodeBook(greenCodebookPath);
        HashMap<String, double[]> blueCodeBook = loadCodeBook(blueCodebookPath);
        
        List<String> compressedRedBlocks = loadCompressedBlocks(compressedRedPath);
        List<String> compressedGreenBlocks = loadCompressedBlocks(compressedGreenPath);
        List<String> compressedBlueBlocks = loadCompressedBlocks(compressedBluePath);
        
        if (compressedRedBlocks.size() != compressedGreenBlocks.size() || 
            compressedGreenBlocks.size() != compressedBlueBlocks.size()) {
            throw new IOException("Mismatch in number of compressed blocks across channels");
        }
        
        BufferedImage decompressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // int blocksX = (int) Math.ceil((double) width / blockSize);
        // int blocksY = (int) Math.ceil((double) height / blockSize);
        
        // if (compressedRedBlocks.size() < blocksX * blocksY) {
        //     throw new IOException("Insufficient number of compressed blocks for image dimensions");
        // }
        
        int blockIndex = 0;
        for (int y = 0; y <= height - blockSize && blockIndex < compressedRedBlocks.size(); y += blockSize) {
            for (int x = 0; x <= width - blockSize && blockIndex < compressedRedBlocks.size(); x += blockSize) {
                double[] redVector = redCodeBook.get(compressedRedBlocks.get(blockIndex));
                double[] greenVector = greenCodeBook.get(compressedGreenBlocks.get(blockIndex));
                double[] blueVector = blueCodeBook.get(compressedBlueBlocks.get(blockIndex));
                
                if (redVector == null || greenVector == null || blueVector == null) {
                    throw new IOException("Invalid code in compressed blocks at index " + blockIndex);
                }
                
                int vectorIndex = 0;
                for (int dy = 0; dy < blockSize && y + dy < height; dy++) {
                    for (int dx = 0; dx < blockSize && x + dx < width; dx++) {
                        int red = (int) Math.min(255, Math.max(0, redVector[vectorIndex]));
                        int green = (int) Math.min(255, Math.max(0, greenVector[vectorIndex]));
                        int blue = (int) Math.min(255, Math.max(0, blueVector[vectorIndex]));
                        
                        int rgb = (red << 16) | (green << 8) | blue;
                        decompressedImage.setRGB(x + dx, y + dy, rgb);
                        vectorIndex++;
                    }
                }
                blockIndex++;
            }
        }
        
        return decompressedImage;
    }

    public static double calculateMSE(BufferedImage original, BufferedImage decompressed) {
        if (original.getWidth() != decompressed.getWidth() || original.getHeight() != decompressed.getHeight()) {
            throw new IllegalArgumentException("Images must have the same dimensions");
        }
        
        int width = original.getWidth();
        int height = original.getHeight();
        double mse = 0.0;
        long pixelCount = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = original.getRGB(x, y);
                int rgb2 = decompressed.getRGB(x, y);
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                mse += (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
                pixelCount++;
            }
        }
        
        return mse / (pixelCount * 3);
    }

    public static void main(String[] args) {
        try {
            String imgPath = "C:\\Users\\mariam\\OneDrive\\Desktop\\vs code\\Java\\final_project\\Data\\Test\\14.png";
            BufferedImage originalImage = ImageIO.read(new File(imgPath));
            int blockSize = 2;
            
            BufferedImage decompressedImage = Decompress_test_img(
                "compressed_red.txt",
                "compressed_green.txt",
                "compressed_blue.txt",
                blockSize,
                originalImage.getWidth(),
                originalImage.getHeight()
            );
            
            ImageIO.write(decompressedImage, "jpg", new File("decompressed_image.jpg"));
            System.out.println("Decompression complete. Decompressed image saved.");
            
            System.out.println("MSE: " + calculateMSE(originalImage, decompressedImage));
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
