/**
 * Main program for getting input from the player and outputting the number
 * of legal V and H moves.
 *
 * Solution to Project Part A
 * For Artificial Intelligence at the University of Melbourne
 *
 * @author David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * @since 2017-03-26
 */
package aiproj.slider;

import aiproj.slider.DopePlayer;
// new comment lel

public final class Tester {
    public static void main(String[] args) {

        // Takes an integer input
        int n = 4;
        String testboard = "H + + +\nH + B +\nH B + +\n+ V V V\n";
        char testCellType = 'H';

        DopePlayer newgame = new DopePlayer();
        newgame.init(n, testboard, testCellType);
        newgame.getBoard().boardDisplay();

        // Tests the legalMoves function and player piece assignment
        System.out.println(newgame.getOurPlayer() + " has " + newgame.getLegalMoveCountPlayer() + " possible moves.");
        System.out.println(newgame.getOpponent() + " has " + newgame.getLegalMoveCountOpponent() + " possible moves.");

    }
}