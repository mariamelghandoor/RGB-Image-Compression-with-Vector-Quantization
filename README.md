# RGB Image Compression with Vector Quantization
A Java program for compressing and decompressing RGB images using vector quantization, processing each color channel (red, green, blue) separately.
## Features

* Compresses RGB images by dividing them into blocks and applying vector quantization
* Generates codebooks for each color channel from a training dataset
* Decompresses images using saved codebooks
* Calculates compression ratio and Mean Squared Error (MSE)
* Supports PNG, JPG, and JPEG input formats; outputs decompressed images as JPG

## Requirements

1. Java 8 or higher
2. Input images in PNG, JPG, or JPEG format
3. Training images in a specified folder for codebook generation

## Usage

1. Clone the repository:

```
   git clone https://github.com/mariamelghandoor/RGB-Image-Compression-with-Vector-Quantization.git
```

2. Navigate to the project directory:

```
cd src
```

3. Compile and run:
```
javac Program.java Compress.java Decompress.java Codebook.java
java Program
```


4. Enter the image file name when prompted (e.g., 14.png).
5. Ensure training images are in Data\Train\ and codebooks are saved in Data\Codebooks\.

## Directory Structure

```
project-root/
├── .vscode/
├── bin/
├── Data/
│   ├── Codebooks/
│   │   ├── blue_codebook.ser
│   │   ├── green_codebook.ser
│   │   └── red_codebook.ser
│   ├── Test/
│   └── Train/
├── lib/
├── src/
│   ├── Codebook.java
│   ├── Compress.java
│   ├── Decompress.java
│   └── Program.java
└── README.md

```

## Notes

- The program uses a fixed block size of 2x2 pixels and a codebook size of 256 entries.
- Paths are hardcoded for a specific user directory; update them in the source code as needed.
- Compression ratio is calculated as a percentage of the original size.

## License
MIT License
