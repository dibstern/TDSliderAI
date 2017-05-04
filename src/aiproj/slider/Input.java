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

// CHANGE 1: Read in Piece Positions for H and V (Separate Arrays?)
// CHANGE 2: String instead of System.in -> strInput into .getScanner(strInput).

import java.util.Scanner;


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
                String readtype = read.next();
                Tile new_tile = new Tile(readtype, i, j, N);
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
}
