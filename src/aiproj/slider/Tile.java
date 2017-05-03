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

    /**
     * Constructor for Tile
     * @param cellType String representing the type of cell
     */
    public Tile (String cellType, int row, int col) {
        this.cellType = cellType;
        this.col = col;
        this.row = row;
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

    /* Getter Method that returns an integer array of the row, col (j, i) coordinates */
    public int[] getPos() {
        int[] newPos = new int[2];
        newPos[0] = this.row;
        newPos[1] = this.col;
        return newPos;
    }
    public void setCellType(String type) {
        this.cellType = type;
    }

    public String getCellType(String type) {
        return this.cellType;
    }

}