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
/**
 * Board class for a player for the game "Slider"
 * For Artificial Intelligence at the University of Melbourne
 * by David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * 2017-03-26
 *
 * See comments.txt for details
 *
 */
public class Board {

    // 2D Array representing the board
    private Tile[][] tiles;
    private int length;
    private ArrayList<Tile> hTiles = new ArrayList();
    private ArrayList<Tile> vTiles = new ArrayList();
    private ArrayList<Move> movesPlayer = new ArrayList();
    private ArrayList<Move> movesOpponent = new ArrayList();

    public static final Integer MAX_SIZE = 7;

    /**
     * Constructor for an empty board
     * @param N the length ai width of the board in tiles
     */
    public Board(int N) {
        tiles = new Tile[N][N];
        length = N;
    }

    /**
     * Constructor for a filled board
     * @param tiles the tiles
     * @param hTiles player h tiles
     * @param vTiles player v tiles
     */
    public Board(Tile[][] tiles, ArrayList<Tile> hTiles, ArrayList<Tile> vTiles) {
        this.tiles = tiles;
        this.hTiles = hTiles;
        this.vTiles = vTiles;
        this.length = tiles.length;
    }

    /**
     *  Creates a deep copy of a board
     * @return the new board
     */
    public Board copyBoard() {

        Tile[][] newTiles = new Tile[length][length];

        //populate tiles array
        for (int row =0;row<length;row++) {
            for (int col=0;col<length;col++) {
                Tile oldTile = tiles[row][col];
                Tile newTile = oldTile.copyTile();
                newTiles[row][col] = newTile;
            }
        }

        // create list of h and v tiles
        ArrayList<Tile> new_hTiles = new ArrayList<Tile>();
        ArrayList<Tile> new_vTiles = new ArrayList<Tile>();

        for (Tile tile : hTiles) {
            new_hTiles.add(tile);
        }
        for (Tile tile : vTiles) {
            new_vTiles.add(tile);
        }

        // make and return the board
        return new Board(newTiles, new_hTiles, new_vTiles);
    }


    /**
     *  Returns all available moves for the given player cell_type
     * @param cell_type the player's cell type
     * @return an ArrayList of legal moves
     */
    public ArrayList<Move> getAllMoves(String cell_type) {
        ArrayList<Move> moves = new ArrayList<Move>();
        ArrayList<Tile> playerTiles;

        if (cell_type.equals(Tile.PLAYER_H)) {
            playerTiles = hTiles;
        }
        else if (cell_type.equals(Tile.PLAYER_V)) {
            playerTiles = vTiles;
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

    /**
     *  Returns the available moves for a tile
     * @param theTile the tile
     * @return an arraylist of moves for that tile
     */
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

    /**
     *
     * @return the board's tiles
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     *  get a tile at position x, y
     * @param x
     * @param y
     * @return the tile
     */
    public Tile getTile(int x, int y) {
        return tiles[length-1-y][x];
    }

    /**
     *  Gets the 2D array indices, given cartesian x,y coordinates
     * @param givenX
     * @param givenY
     * @return the 2d array infices
     */
    public int[] getPos(int givenX, int givenY) {
        int[] pos = {(length - 1 - givenY), givenX};
        return pos;
    }



    /**
     *  Mutate a tile at x y to a new tile
     * @param x x posiiton
     * @param y y position
     * @param newCellType the cell type to change to
     */
    public void updateTile(int x, int y, String newCellType) {
        if (length-1-y < length && length-1-y >= 0 && x < length && x >= 0) {
            tiles[length-1-y][x].setCellType(newCellType);
        }
    }

    /**
     *
     * @return length of the board
     */
    public int getLength() {
        return length;
    }

    /**
     *
     * @return arraylist of horizontal tiles
     */
    public ArrayList<Tile> getHTiles() {
        return hTiles;
    }


    /**
     *
     * @return arraylist of vertical tiles
     */
    public ArrayList<Tile> getVTiles() {
        return vTiles;
    }


    /**
     *  Add a h tile to the arraylist
     * @param newTile the tile
     */
    public void addHTile(Tile newTile) {
        hTiles.add(newTile);
    }

    /**
     *  Add a v tile to the arraylist
     * @param newTile the tile
     */
    public void addVTile(Tile newTile) {
        vTiles.add(newTile);
    }

    /**
     *  Remove old tile from the recorded vTiles array
     * @param oldTile the old tile
     */
    public void removeVTile(Tile oldTile) {
        Predicate<Tile> tilePredicate = t-> (t.getRow() == oldTile.getRow() && t.getCol() == oldTile.getCol());
        vTiles.removeIf(tilePredicate);
    }

    /**
     * Remove old tile from the recorded hTiles array
     * @param oldTile the old tile
     */
    public void removeHTile(Tile oldTile) {
        Predicate<Tile> tilePredicate = t-> (t.getRow() == oldTile.getRow() && t.getCol() == oldTile.getCol());
        hTiles.removeIf(tilePredicate);
    }

    /**
     * Display the board (print it)
     */
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

    /**
     * Set player moves
     * @param moves the moves
     */
    public void setMovesPlayer(ArrayList<Move> moves) {
        this.movesPlayer = moves;
    }

    /**
     * Set opponent's moves
     * @param moves the moves
     */
    public void setMovesOpponent(ArrayList<Move> moves) {
        this.movesOpponent = moves;
    }

    /**
     * Get the player's moves
     * @return the player's moves
     */
    public ArrayList<Move> getMovesPlayer() {
        return this.movesPlayer;
    }

    /**
     *
     * @return the opponent's moves
     */
    public ArrayList<Move> getMovesOpponent() {
        return this.movesOpponent;
    }

    /**
     * Get player tiles
     * @param player the player
     * @return the player's tiles (arraylist form)
     */

    public ArrayList<Tile> getPlayerTiles(String player) {
        if (player.equals(Tile.PLAYER_H)) {
            return hTiles;
        }
        return vTiles;
    }

}

