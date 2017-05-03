/**
 * Solution to Project A
 * For Artificial Intelligence at the University of Melbourne
 *
 * Board Class
 * Reads in and Stores a given board state for the adversarial game Slider
 * Counts legal moves for a given board state
 *
 * @author David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * @since 2017-03-26
 */
package aiproj.slider;

import java.util.ArrayList;

public class Board {

    // 2D Array representing the board
    private Tile[][] tiles;
    private int length;
    private ArrayList<Tile> h_tiles = new ArrayList();
    private ArrayList<Tile> v_tiles = new ArrayList();

    /**
     * Constructor for board.
     * @param N the length and width of the board in tiles
     */
    public Board(int N) {
        tiles = new Tile[N][N];
        length = N;
    }

    public ArrayList<Move> getAllMoves(String cell_type) {
        ArrayList<Move> moves = new ArrayList<Move>();
        ArrayList<Tile> playerTiles;

        if (cell_type.equals(Tile.PLAYER_H)) {
            playerTiles = h_tiles;
        }
        else if (cell_type.equals(Tile.PLAYER_V)) {
            playerTiles = v_tiles;
        }
        else {
            System.out.println("ERROR: Incorrect Tile Type");
            return null;
        }

        for (Tile tile : playerTiles) {
            moves.addAll(getTileMoves(tile));
        }

        return moves;
    }

    // Add Type as an argument so we can loop through one list only, if required?
    /**
     * SWAPPED i AND j TO REFLECT CHANGE IN PART B
     * Counts number of legal moves on the board
     *
     * @param N Size of board
     * @return {H, V} where H is the number of legal moves for player H
     * and V is the number of legal moves for player V
     */
    public int[] countMoves(String cell_type, Boolean both) {

        /* Initialise legalMoveCount in the form {H, V} where H is the
         * number of legal moves for player H and V is the number of
         * legal moves for player V */
        int[] legalMoveCount = {0, 0};


        // MAKE THIS NICER

        // If we're looking for h_tiles or both types, update our legal move count
        if (cell_type.equals(Tile.PLAYER_H) || both) {
            for (Tile eachtile : h_tiles) {
                legalMoveCount = tileMoves(eachtile, legalMoveCount);
                getTileMoves(eachtile);
            }
        }
        // If we're looking for v_tiles or both types, update our legal move count
        if (cell_type.equals(Tile.PLAYER_V) || both) {
            for (Tile eachtile : v_tiles) {
                legalMoveCount = tileMoves(eachtile, legalMoveCount);
                getTileMoves(eachtile);

            }
        }

        return legalMoveCount;
    }

    /** Returns an arraylist all the moves available to the given player tile
     *
     * @param theTile current tile
     * @return arraylist of moves
     */
    private ArrayList<Move> getTileMoves(Tile theTile) {
        ArrayList<Move> moves = new ArrayList<Move>();

        int[] pos = theTile.getPos();
        String piece = theTile.getCellType();

        int j = pos[0];
        int i = pos[1];

        // Tile Up a valid move if Player V in the top row (j == 0) or it's empty
        if (j > 0 || piece.equals(Tile.PLAYER_V)) {
            if (j==0 || tiles[j-1][i].isEmpty()) {
                moves.add(new Move(i,j,Move.Direction.UP));
            }
        }
        // Tile Right a valid move if Player H in the rightmost column or if it's empty
        if (i < length-1 || piece.equals(Tile.PLAYER_H)) {
            if (i==length-1 || tiles[j][i+1].isEmpty()) {
                moves.add(new Move(i,j,Move.Direction.RIGHT));
            }
        }
        // Tile Left a valid move? Player H can't move left
        if (i > 0 && !piece.equals(Tile.PLAYER_H)) {
            if (tiles[j][i-1].isEmpty()) {
                moves.add(new Move(i,j,Move.Direction.LEFT));
            }
        }
        // Tile Down a valid move? Player V can't move down
        if (j < length-1 && !piece.equals(Tile.PLAYER_V)) {
            if (tiles[j+1][i].isEmpty()) {
                moves.add(new Move(i,j,Move.Direction.DOWN));
            }
        }
        // DEBUG: print moves
        //for (Move move: moves) {
        //    System.out.println(move);
        //}

        return moves;
    }

    /**
     * SWAPPED i AND j TO REFLECT CHANGE IN PART B
     * Increments legalMoveCount based on the number of legal V and H
     * moves at a given cell
     *
     * @param i cell column
     * @param j cell row
     * @param legalMoveCount count of legal H and V moves in the form {H,V}
     * @return an updated legalMoveCount {H,V}
     */
    private int[] tileMoves(Tile theTile, int[] legalMoveCount) {

        int moves = 0;
        String piece = theTile.getCellType();
        int[] pos = theTile.getPos();
        int j = pos[0];
        int i = pos[1];

        // Tile Up a valid move if Player V in the top row (j == 0) or it's empty
        if (j > 0 || piece.equals(Tile.PLAYER_V)) {
            moves += (j == 0 ? 1 : (tiles[j-1][i].isEmpty() ? 1 : 0));
        }
        // Tile Right a valid move if Player H in the rightmost column or if it's empty
        if (i < length-1 || piece.equals(Tile.PLAYER_H)) {
            moves += (i == length-1 ? 1 : (tiles[j][i+1].isEmpty() ? 1 : 0));
        }
        // Tile Left a valid move? Player H can't move left
        if (i > 0 && !piece.equals(Tile.PLAYER_H)) {
            moves += (tiles[j][i-1].isEmpty() ? 1 : 0);
        }
        // Tile Down a valid move? Player V can't move down
        if (j < length-1 && !piece.equals(Tile.PLAYER_V)) {
            moves += (tiles[j+1][i].isEmpty() ? 1 : 0);
        }
        // Add legal valid moves to legalMoveCount based on the player
        legalMoveCount[0] += (piece.equals(Tile.PLAYER_H) ? moves : 0);
        legalMoveCount[1] += (piece.equals(Tile.PLAYER_V) ? moves : 0);

        return legalMoveCount;

    }

    /* Prints the board */
    /* SWAPPED i AND j TO REFLECT CHANGE IN PART B */
    public void boardDisplay() {
        String currBoard = "";
        for (int j = 0; j < length; j++) {
            for (int i = 0; i < length; i++) {
                if (i < length - 1) {
                    currBoard += tiles[j][i].getCellType() + " ";
                }
                else if (j < length - 1) {
                    currBoard += tiles[j][i].getCellType() + "\n";
                }
                else {
                    currBoard += tiles[j][i].getCellType();
                }
            }
        }
        System.out.println("Board:");
        System.out.println(currBoard);
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    /* Get the length of the board */
    public int getLength() {
        return length;
    }

    /* Get the array of Horizontal Tiles */
    public ArrayList<Tile> getHTiles() {
        return h_tiles;
    }

    /* Mutate the array of Horizontal Tiles */
    public void addHTile(Tile newTile) {
        h_tiles.add(newTile);
    }

    /* Get the array of Vertical Tiles */
    public ArrayList<Tile> getVTiles() {
        return v_tiles;
    }

    /* Mutate the array of Vertical Tiles */
    public void addVTile(Tile newTile) {
        v_tiles.add(newTile);
    }

}