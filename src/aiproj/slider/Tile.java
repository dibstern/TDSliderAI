/**
 * Tile Class for the game of Slider.
 *
 * Solution to Project A
 * For Artificial Intelligence at the University of Melbourne
 *
 * @author David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * @since 2017-03-26
 */
package aiproj.slider;
/**
 * Tile class for a player for the game "Slider"
 * For Artificial Intelligence at the University of Melbourne
 * by David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * 2017-03-26
 *
 * See comments.txt for details
 *
 */
public class Tile {

    // Definitions of the supported cell types
    public static final String PLAYER_H = "H";
    public static final String PLAYER_V = "V";
    public static final String BLOCKED = "B";
    public static final String EMPTY = "+";

    private String cellType;
    private int col;
    private int row;
    private int x;
    private int y;

    /**
     * Constructor for Tile
     * @param cellType String representing the type of cell
     */
    public Tile(String cellType, int row, int col, int n) {
        this.cellType = cellType;
        this.col = col;
        this.row = row;
        this.x = col;
        this.y = n-1-row;
    }

    /**
     * Constructor for Tile
     * @param cellType String representing the type of cell
     */
    public Tile(String cellType, int row, int col, int x, int y) {
        this.cellType = cellType;
        this.col = col;
        this.row = row;
        this.x = x;
        this.y = y;
    }

    /**
     *  Copies a tile
     * @return a copy of a tile
     */
    public Tile copyTile() {
        Tile newTile = new Tile(cellType, row, col, x,y);
        return newTile;
    }

    /**
     *
     * @return this cell type (String)
     */
    public String getCellType() {
        return this.cellType;
    }

    /**
     *  Returns whether the cell  is empty
     * @return boolean, whether cell is empty or not
     */
    public boolean isEmpty() {
        if (this.cellType.equals(EMPTY)) {
            return true;
        }
        return false;
    }

    /**
     * @return x, y coordinates as an array of the tile
     */
    public int[] getCoord() {
        int[] coord = new int[2];
        coord[0] = this.x;
        coord[1] = this.y;
        return coord;
    }


    /**
     *
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     *
     * @return the column
     */
    public int getCol() {
        return col; 
    }

    /**
     * set cell type
     * @param type the type
     */
    public void setCellType(String type) {
        this.cellType = type;
    }

    /**
     *
     * @return x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return y coordinate
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return cellType + " - Ctn: ("+x+","+y+"), Arr: ("+row+", "+col+")";
    }
}