import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

public class ImageProcessing {
	public static void main(String[] args) throws FileNotFoundException {
		int mode = Integer.parseInt(args[0]); //the mode of the program (first argument)
		Scanner input = new Scanner(new File(args[1])); //the scanner for reading the input file
		
		input.next();
		int columns = Integer.parseInt(input.next()); // # of columns
		int rows = Integer.parseInt(input.next()); // # of rows
		input.next();
		
		int[][][] inArr = new int[rows][columns][3]; //3d array for the ppm image
		
		for(int k = 0; k < rows; k++) {
			for(int l = 0; l < columns; l++) {
				for(int m = 0; m < 3; m++) {
					inArr[k][l][m] = Integer.parseInt(input.next());
				}
			}
		}
		
		input.close();
		
		switch(mode) {
		case 0:
			PrintStream output = new PrintStream(new File("output.ppm")); //print stream for second part
			output(inArr, output);
			break;
		case 1:
			PrintStream baw = new PrintStream(new File("black-and-white.ppm")); //print stream for third part
			
			output(blackAndWhite(inArr), baw);
			break;
		case 2:
			Scanner filter = new Scanner(new File(args[2])); //scanner for reading the filter
			String dim = filter.nextLine(); //first line of the filter file
			int dimension = Integer.parseInt(dim.substring(0, dim.length()/2)); //size of dimensions of the filter
			int[][] kernel = new int[dimension][dimension]; //2d array for filter
			
			for(int r = 0; r < dimension; r++) {
				for(int c = 0; c < dimension; c++) {
					kernel[r][c] = Integer.parseInt(filter.next());
				}
			}
			
			PrintStream conv = new PrintStream(new File("convolution.ppm")); //printstream for fourth part
			
			output(blackAndWhite(convolute(inArr, kernel)), conv);
			break;
		case 3:
			int range = Integer.parseInt(args[2]); //range of the quantization
			
			PrintStream quan = new PrintStream(new File("quantized.ppm")); //print stream for fifth part
			
			output(quantize(inArr, range), quan);
			break;
		}
	}
	// this method applies the quantizitation process to a pixel and its neighbors which is in range recursively 
	public static int[][][] recursive(int x, int y, int z, int[][][] array, int range, boolean[][][] check) {
		check[x][y][z] = true;
		if(x != array.length - 1 && check[x + 1][y][z] == false && array[x + 1][y][z] <= array[x][y][z] + range && array[x + 1][y][z] >= array[x][y][z] - range) {
			array[x + 1][y][z] = array[x][y][z];
			array = recursive(x + 1, y, z, array, range, check);
		}
		if(x != 0 && check[x - 1][y][z] == false && array[x - 1][y][z] <= array[x][y][z] + range && array[x - 1][y][z] >= array[x][y][z] - range) {
			array[x - 1][y][z] = array[x][y][z];
			array = recursive(x - 1, y, z, array, range, check);
		}
		if(y != array[0].length - 1 && check[x][y + 1][z] == false && array[x][y + 1][z] <= array[x][y][z] + range && array[x][y + 1][z] >= array[x][y][z] - range) {
			array[x][y + 1][z] = array[x][y][z];
			array = recursive(x, y + 1, z, array, range, check);
		}
		if(y != 0 && check[x][y - 1][z] == false && array[x][y - 1][z] <= array[x][y][z] + range && array[x][y - 1][z] >= array[x][y][z] - range) {
			array[x][y - 1][z] = array[x][y][z];
			array = recursive(x, y - 1, z, array, range, check);
		}
		if(z != 2 && check[x][y][z + 1] == false && array[x][y][z + 1] <= array[x][y][z] + range && array[x][y][z + 1] >= array[x][y][z] - range) {
			array[x][y][z + 1] = array[x][y][z];
			array = recursive(x, y, z + 1, array, range, check);
		}
		if(z != 0 && check[x][y][z - 1] == false && array[x][y][z - 1] <= array[x][y][z] + range && array[x][y][z - 1] >= array[x][y][z] - range) {
			array[x][y][z - 1] = array[x][y][z];
			array = recursive(x, y, z - 1, array, range, check);
		}
		return array;
	}
	//this array makes the quantizitation process of the ppm image in the 3d array with help of the recursive method which is recursive and returns the new array
	public static int[][][] quantize(int[][][] array, int range) {
		boolean[][][] check = new boolean[array.length][array[0].length][3]; //the boolean array that keeps whether a pixel in the ppm array is changed
		for(int z = 0; z < 3; z++) {
			for(int x = 0; x < array.length; x++) {
				for(int y = 0; y < array[0].length; y++) {
					array = recursive(x, y, z, array, range, check);
				}
			}
		}
		return array;
	}
	//this method rewrite a 3d array considering a filter and returns the rewritten array
	public static int[][][] convolute(int[][][] array, int[][] filter) {
		int[][][] convoluted = new int[array.length - filter.length + 1][array[0].length - filter[0].length + 1][3]; //new array with size after filter
		int amount = filter.length / 2; //the amount of pixels that are lost from each edge
		
		for(int i = 0; i < 3; i++) {
			for(int r = amount; r < array.length - amount; r++) {
				for(int c = amount; c < array[0].length - amount; c++) {
					int result = 0;
					
					for(int fr = 0; fr < filter.length; fr++) {
						for(int fc = 0; fc < filter.length; fc++) {
							result += filter[fr][fc] * array[r - amount + fr][c - amount + fc][i];
						}
					}
					
					if(result < 0) {
						result = 0;
					} else if(result > 255) {
						result = 255;
					}
					convoluted[r - amount][c - amount][i] = result;
				}
			}
		}
		
		return convoluted;
	}
	//this method calculates the average of rgb values of each pixels in a 3d array and assigns them to this average, returns the new array
	public static int[][][] blackAndWhite(int[][][] array) {
		for(int k = 0; k < array.length; k++) {
			for(int l = 0; l < array[0].length; l++) {
				int average = (array[k][l][0] + array[k][l][1] + array[k][l][2]) / 3;
				array[k][l][0] = average;
				array[k][l][1] = average;
				array[k][l][2] = average;
			}
		}
		
		return array;
	}
	//this method this method writes a ppm file from a 3d array
	public static void output(int[][][] array, PrintStream output) {
		output.println("P3");
		output.println(array[0].length + " " + array.length);
		output.println("255");
		
		for(int k = 0; k < array.length; k++) {
			for(int l = 0; l < array[0].length; l++) {
				for(int m = 0; m < 3; m++) {
					output.print(array[k][l][m] + " ");
				}
				output.print("\t");
			}
			output.println();
		}
	}
}