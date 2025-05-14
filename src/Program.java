import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Program {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the image file name (e.g., 14.png): ");
            String imgName = scanner.nextLine();

            String imgPath = "C:\\Users\\mariam\\OneDrive\\Desktop\\vs code\\Java\\final_project\\Data\\Test\\" + imgName;
            String compressedRedPath = "compressed_red.txt";
            String compressedGreenPath = "compressed_green.txt";
            String compressedBluePath = "compressed_blue.txt";
            String decompressedImgPath = "decompressed_image.jpg";
            int blockSize = 2;

            BufferedImage originalImage = ImageIO.read(new File(imgPath));
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            System.out.println("Starting compression...");
            List<List<String>> compressedChannels = Compress.Compress_test_img(originalImage, blockSize);
            
            Compress.saveCompressedBlocks(compressedChannels.get(0), compressedRedPath);
            Compress.saveCompressedBlocks(compressedChannels.get(1), compressedGreenPath);
            Compress.saveCompressedBlocks(compressedChannels.get(2), compressedBluePath);
            System.out.println("Compression complete. Compressed blocks saved.");

            double compressionRatio = Compress.calculateCompressionSize(blockSize, width, height);
            System.out.println("Compression ratio: " + compressionRatio + "%");

            System.out.println("Starting decompression...");
            BufferedImage decompressedImage = Decompress.Decompress_test_img(
                compressedRedPath,
                compressedGreenPath,
                compressedBluePath,
                blockSize,
                width,
                height
            );

            ImageIO.write(decompressedImage, "jpg", new File(decompressedImgPath));
            System.out.println("Decompression complete. Decompressed image saved.");

            double mse = Decompress.calculateMSE(originalImage, decompressedImage);
            System.out.println("MSE: " + mse);

            scanner.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}