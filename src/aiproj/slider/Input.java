/**
 * Singleton Implementation of a Scanner class to support Game of Slider
 *
 * Solution to Project A
 * For Artificial Intelligence at the University of Melbourne
 *
 * @author David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * @since 2017-03-26
 */
package aiproj.slider;


import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Input class for a player for the game "Slider"
 * For Artificial Intelligence at the University of Melbourne
 * by David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * 2017-03-26
 *
 * See comments.txt for details
 *
 */

public final class Input {

    /**
     * SWAPPED i AND j TO REFLECT CHANGE IN PART B
     * Reads the board from input
     * @param N size of NxN board
     * @return the board as a NxN array of strings
     */
    public static Board readBoard(int N, String strInput) {

        // Use string input instead of System input
        Scanner read = new Scanner(strInput);

        Board board = new Board(N);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {

                // Create tile and add to board
                Tile new_tile = new Tile(read.next(), i, j, N);
                board.getTiles()[i][j] = new_tile;

                // If it's a player tile then add to appropriate list
                if (new_tile.getCellType().equals(Tile.PLAYER_H)) {
                    board.addHTile(new_tile);
                }
                else if (new_tile.getCellType().equals(Tile.PLAYER_V)) {
                    board.addVTile(new_tile);
                }
            }
        }
        return board;
    }

    /**
     * Reads in a weight file
     * @param weights_file name of the weight file
     * @return arraylist of weights
     */
    public static ArrayList<Double> readWeightFile(String weights_file) {

        FileReader in = null;
        String str = "";
        ArrayList<Double> file_weights = new ArrayList<Double>();

        try {
            in = new FileReader(weights_file);
            int chr;
            while ((chr = in.read()) != -1) {
                //System.out.println((char)c);
                str = str + (char) chr;
            }
            String[] weightsString = str.split(" ");
            for (int i = 0; i < weightsString.length; i++) {
                file_weights.add(Double.parseDouble(weightsString[i]));
            }
            in.close();
        }
        catch ( Exception e) {
            System.out.println("FILE READ FAIL");
            System.exit(0);
        }

        return file_weights;
    }

    /**
     *  Updates a weights file based on a set of new weights
     * @param weights the new weights
     * @param weights_file name of the weights file
     */
    public static void updateWeightFile(ArrayList<Double> weights, String weights_file) {

        FileWriter out = null;
        String str = "";

        try {
            out = new FileWriter(weights_file);

            for (int i = 0; i < weights.size(); i++) {
                str += Double.toString(weights.get(i));
                str += " ";
            }
            out.write(str);
            out.close();

        }
        catch ( Exception e) {
            System.out.println("FILE READ FAIL");
            System.exit(0);
        }
    }
}
