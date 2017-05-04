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

    public String getCellType() {
        return this.cellType;
    }

    /* Returns whether the cell type is EMPTY */
    public boolean isEmpty() {
        if (this.cellType.equals(EMPTY)) {
            return true;
        }
        return false;
    }

    /* Getter Method that returns an integer array of (x, y) coordinates */
    public int[] getCoord() {
        int[] coord = new int[2];
        coord[0] = this.x;
        coord[1] = this.y;
        return coord;
    }

    /*
    public int getPos() {
        int[] newPos = {row, col};
        return newPos;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col; 
    }*/

    public void setCellType(String type) {
        this.cellType = type;
    }

    public String getCellType(String type) {
        return this.cellType;
    }

}