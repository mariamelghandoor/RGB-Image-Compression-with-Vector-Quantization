import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Codebook {
     public static List<List<int[]>> collectAllRGBBlocks(String folderPath, int blockSize) throws IOException {
        List<int[]> allRedBlocks = new ArrayList<>();
        List<int[]> allGreenBlocks = new ArrayList<>();
        List<int[]> allBlueBlocks = new ArrayList<>();
        
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("Invalid directory path: " + folderPath);
        }
        
        File[] files = folder.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".png") || 
            name.toLowerCase().endsWith(".jpeg")
        );
        
        if (files == null || files.length == 0) {
            throw new IOException("No image files found in directory: " + folderPath);
        }
        
        File[] limitedFiles = Arrays.copyOf(files, files.length);
        
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<List<int[]>>[] results = new List[files.length];
        
        for (int i = 0; i < files.length; i++) {
            final File file = limitedFiles[i];
            final int index = i;
            executor.submit(() -> {
                try {
                    List<List<int[]>> rgbBlocks = getRGBBlocks(file.getAbsolutePath(), blockSize);
                    results[index] = rgbBlocks;
                } catch (IOException e) {
                    System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new IOException("Image processing interrupted", e);
        }
        
        for (List<List<int[]>> rgbBlocks : results) {
            if (rgbBlocks != null) {
                allRedBlocks.addAll(rgbBlocks.get(0));
                allGreenBlocks.addAll(rgbBlocks.get(1));
                allBlueBlocks.addAll(rgbBlocks.get(2));
            }
        }
        
        List<List<int[]>> result = new ArrayList<>();
        result.add(allRedBlocks);
        result.add(allGreenBlocks);
        result.add(allBlueBlocks);
        return result;
    }

    public static List<List<int[]>> getRGBBlocks(String inputPath, int blockSize) throws IOException {
        BufferedImage image = ImageIO.read(new File(inputPath));
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

        List<List<int[]>> result = new ArrayList<>();
        result.add(redBlocks);
        result.add(greenBlocks);
        result.add(blueBlocks);
        image.flush();
        
        return result;
    }

    public static double[] computeAverageVector(List<int[]> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return new double[0];
        }
        int vectorLength = blocks.get(0).length;
        double[] avgVector = new double[vectorLength];
        for (int[] block : blocks) {
            for (int i = 0; i < vectorLength; i++) {
                avgVector[i] += block[i];
            }
        }
        int blockCount = blocks.size();
        for (int i = 0; i < vectorLength; i++) {
            avgVector[i] /= blockCount;
        }
        return avgVector;
    }

    public static double Compute_Distance(int[] vector1, double[] vector2) {
        double distance = 0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            distance += diff * diff;
        }
        return Math.sqrt(distance);
    }

    public static double[][] splitVector(double[] vector) {
        int length = vector.length;
        double[] plusHalf = new double[length];
        double[] minusHalf = new double[length];
        for (int i = 0; i < length; i++) {
            plusHalf[i] = vector[i] + 5.0;
            minusHalf[i] = vector[i] - 5.0;
        }
        return new double[][]{plusHalf, minusHalf};
    }

    public static HashMap<String, double[]> Create_Code_Book(List<int[]> blocks, int codeBookSize) {
        if (blocks == null || blocks.isEmpty()) {
            return new HashMap<>();
        }
        List<double[]> startvector = new ArrayList<>();
        startvector.add(computeAverageVector(blocks));
        while (startvector.size() < codeBookSize) {
            List<double[]> newvector = new ArrayList<>();
            for (double[] vector : startvector) {
                double[][] split = splitVector(vector);
                double[] plusHalf = split[0];
                double[] minusHalf = split[1];
                List<int[]> plusHalfBlocks = new ArrayList<>();
                List<int[]> minusHalfBlocks = new ArrayList<>();
                for (int[] block : blocks) {
                    double distToPlus = Compute_Distance(block, plusHalf);
                    double distToMinus = Compute_Distance(block, minusHalf);
                    if (distToPlus < distToMinus) {
                        plusHalfBlocks.add(block);
                    } else {
                        minusHalfBlocks.add(block);
                    }
                }
                double[] plusAvg = plusHalfBlocks.isEmpty() ? plusHalf : computeAverageVector(plusHalfBlocks);
                double[] minusAvg = minusHalfBlocks.isEmpty() ? minusHalf : computeAverageVector(minusHalfBlocks);
                newvector.add(plusAvg);
                newvector.add(minusAvg);
            }
            startvector = newvector;
        }
        boolean changed;
        HashMap<Integer, List<int[]>> assignments = new HashMap<>();
        Random random = new Random();
        int iterations = 0;
        do {
            changed = false;
            assignments.clear();
            for (int i = 0; i < startvector.size(); i++) {
                assignments.put(i, new ArrayList<>());
            }
            for (int[] block : blocks) {
                int bestIndex = 0;
                double bestDist = Double.MAX_VALUE;
                for (int i = 0; i < startvector.size(); i++) {
                    double dist = Compute_Distance(block, startvector.get(i));
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestIndex = i;
                    }
                }
                assignments.get(bestIndex).add(block);
            }
            for (int i = 0; i < startvector.size(); i++) {
                if (assignments.get(i).isEmpty() && !blocks.isEmpty()) {
                    assignments.get(i).add(blocks.get(random.nextInt(blocks.size())));
                    changed = true;
                }
            }
            for (int i = 0; i < startvector.size(); i++) {
                List<int[]> group = assignments.get(i);
                if (!group.isEmpty()) {
                    double[] newvector = computeAverageVector(group);
                    if (!Arrays.equals(startvector.get(i), newvector)) {
                        startvector.set(i, newvector);
                        changed = true;
                    }
                }
            }
            iterations++;
        } while (changed && iterations < 100);
        HashMap<String, double[]> codeBook = new HashMap<>();
        int keyLength = (int) Math.ceil(Math.log(codeBookSize) / Math.log(2));
        for (int i = 0; i < startvector.size(); i++) {
            String binaryCode = String.format("%" + keyLength + "s", Integer.toBinaryString(i)).replace(' ', '0');
            codeBook.put(binaryCode, startvector.get(i));
        }
        return codeBook;
    }

    public static void saveCodeBook(HashMap<String, double[]> codeBook, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(codeBook);
        }
    }

    public static void main(String[] args) {
        try {
            String folderPath = "C:\\Users\\mariam\\OneDrive\\Desktop\\vs code\\Java\\final_project\\Data\\Train";
            String outputPath = "C:\\Users\\mariam\\OneDrive\\Desktop\\vs code\\Java\\final_project\\Data\\Codebooks";
            
            List<List<int[]>> allRGBBlocks = collectAllRGBBlocks(folderPath, 2);
            
            System.out.println("Total red blocks: " + allRGBBlocks.get(0).size());
            System.out.println("Total green blocks: " + allRGBBlocks.get(1).size());
            System.out.println("Total blue blocks: " + allRGBBlocks.get(2).size());
            
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            int codeBookSize = 256;
            
            if (!allRGBBlocks.get(0).isEmpty()) {
                HashMap<String, double[]> redCodeBook = Create_Code_Book(allRGBBlocks.get(0), codeBookSize);
                saveCodeBook(redCodeBook, outputPath + "\\red_codebook.ser");
                System.out.println("Red codebook saved with " + redCodeBook.size() + " entries");
            }
            
            if (!allRGBBlocks.get(1).isEmpty()) {
                HashMap<String, double[]> greenCodeBook = Create_Code_Book(allRGBBlocks.get(1), codeBookSize);
                saveCodeBook(greenCodeBook, outputPath + "\\green_codebook.ser");
                System.out.println("Green codebook saved with " + greenCodeBook.size() + " entries");
            }
            
            if (!allRGBBlocks.get(2).isEmpty()) {
                HashMap<String, double[]> blueCodeBook = Create_Code_Book(allRGBBlocks.get(2), codeBookSize);
                saveCodeBook(blueCodeBook, outputPath + "\\blue_codebook.ser");
                System.out.println("Blue codebook saved with " + blueCodeBook.size() + " entries");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
