/**
 * Solution to Project A
 * For Artificial Intelligence at the University of Melbourne
 *
 * Board Class
 * Reads in and Stores a given board state for the adversarial game Slider
 * Counts legal moves for a given board state
 *
 * @author David Stern (dstern 585870) ai Hugh Edwards (hughe 584183)
 * @since 2017-03-26
 */
package aiproj.slider;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Board {

    // 2D Array representing the board
    private Tile[][] tiles;
    private int length;
    private ArrayList<Tile> h_tiles = new ArrayList();
    private ArrayList<Tile> v_tiles = new ArrayList();

    /**
     * Constructor for board.
     * @param N the length ai width of the board in tiles
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

    private ArrayList<Move> getTileMoves(Tile theTile) {
        ArrayList<Move> moves = new ArrayList<Move>();

        String piece = theTile.getCellType();
        int[] coord = theTile.getCoord();
        int x = coord[0];
        int y = coord[1];

        // Tile Up a valid move if Player V in the top row (j == 0) or it's empty
        if ((y == length-1 && piece.equals(Tile.PLAYER_V)) || (y < length-1 && getTile(x, y+1).isEmpty())) {
            moves.add(new Move(x,y,Move.Direction.UP));
        }
        // Tile Right a valid move if Player H in the rightmost column or if it's empty
        if ((x==length-1 && piece.equals(Tile.PLAYER_H)) || (x < length-1 && getTile(x+1,y).isEmpty()) ) {
            moves.add(new Move(x,y,Move.Direction.RIGHT));
        }
        // Tile Left a valid move? Player H can't move left
        if (piece.equals(Tile.PLAYER_V) && x > 0) {
            if (getTile(x-1, y).isEmpty()) {
                moves.add(new Move(x,y,Move.Direction.LEFT));
            }
        }
        // Tile Down a valid move? Player V can't move down
        if (piece.equals(Tile.PLAYER_H) && y > 0) {
            if (getTile(x, y-1).isEmpty()) {
                moves.add(new Move(x,y,Move.Direction.DOWN));
            }
        }
        return moves;
    }

    /* Gets the board's tiles */
    public Tile[][] getTiles() {
        return tiles;
    }

    /* Access Tile Using (x, y) coordinates */
    public Tile getTile(int x, int y) {
        return tiles[length-1-y][x];
    }

    /* Gets the 2D array indices, given cartesian x,y coordinates */
    public int[] getPos(int givenX, int givenY) {
        int[] pos = {(length - 1 - givenY), givenX};
        return pos;
    }

    /* Mutate the Board State */
    public void updateTile(int x, int y, String newCellType) {
        if (length-1-y < length && length-1-y >= 0 && x < length && x >= 0) {
            tiles[length-1-y][x].setCellType(newCellType);
        }
    }

    /* Get the length of the board */
    public int getLength() {
        return length;
    }

    /* Get the array of Horizontal Tiles */
    public ArrayList<Tile> getHTiles() {
        return h_tiles;
    }

    /* Get the array of Vertical Tiles */
    public ArrayList<Tile> getVTiles() {
        return v_tiles;
    }

    /* Mutate the array of Horizontal Tiles */
    public void addHTile(Tile newTile) {
        h_tiles.add(newTile);
    }

    /* Mutate the array of Vertical Tiles */
    public void addVTile(Tile newTile) {
        v_tiles.add(newTile);
    }

    /* Remove old tiles from the recorded v_tiles array */
    public void removeVTile(Tile oldTile) {
        Predicate<Tile> tilePredicate = t-> (t.getRow() == oldTile.getRow() && t.getCol() == oldTile.getCol());
        v_tiles.removeIf(tilePredicate);
    }

    /* Remove old tiles from the recorded h_tiles array */
    public void removeHTile(Tile oldTile) {
        Predicate<Tile> tilePredicate = t-> (t.getRow() == oldTile.getRow() && t.getCol() == oldTile.getCol());
        h_tiles.removeIf(tilePredicate);
    }

    /* Prints the board */
    public void boardDisplay() {
        String currBoard = "";
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (j < length - 1) {
                    currBoard += tiles[i][j].getCellType() + " ";
                }
                else {
                    currBoard += tiles[i][j].getCellType() + "\n";
                }
            }
        }
        System.out.println("Board:");
        System.out.println(currBoard);
    }

}