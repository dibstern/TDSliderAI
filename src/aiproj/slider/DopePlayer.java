package aiproj.slider;

import aiproj.slider.Tile;
import aiproj.slider.Board;
import aiproj.slider.Input;
import java.lang.Character;
import java.util.ArrayList;
import java.util.List;

import static aiproj.slider.Input.*;


public class DopePlayer implements SliderPlayer {

    // Keeping Track of the Board, Player Piece Types, Number of current legal moves
    private Board curr_board;
    private String ourPlayer;
    private String Opponent;
    private int legalMoveCountPlayer;
    private int legalMoveCountOpponent;
    private ArrayList<Move> movesPlayer;
    private ArrayList<Move> movesOpponent;

    // Add more info to track here, for the Evaluation function


    /**
     * Prepare Slider Player for a given board and player
     *
     * @param dimension The width and height of the board in cells
     * @param board A string representation of the initial state of the board,
     * as described in the part B specification
     * @param player 'H' or 'V', corresponding to which pieces the player will
     * control for this game ('H' = Horizontal, 'V' = Vertical)
     */
    public void init(int dimension, String board, char player) {

        // Assigning Player Pieces
        String playerType = Character.toString(player);
        ourPlayer = playerType;
        Opponent = playerType.equals(Tile.PLAYER_H) ? Tile.PLAYER_V : Tile.PLAYER_H;

        // Reading in the Board
        curr_board = readBoard(dimension, board);

        // Reading in the number of legal moves
        //int[] legalMoves = curr_board.countMoves(playerType, true);
        //legalMoveCountPlayer = ourPlayer.equals(Tile.PLAYER_H) ? legalMoves[0] : legalMoves[1];
        //legalMoveCountOpponent = Opponent.equals(Tile.PLAYER_H) ? legalMoves[0] : legalMoves[1];

        refresh();


    }



    /**
     * Notify the player of the last move made by their opponent. In response to
     * this method, your player should update its internal representation of the
     * board state to reflect the result of the move made by the opponent.
     *
     * @param move A Move object representing the previous move made by the
     * opponent, which may be null (indicating a pass). Also, before the first
     * move at the beginning of the game, move = null.
     */
    public void update(Move move) {

    if (move == null) {
        return;
    }

    Tile fromTile = curr_board.getTile(move.j, move.i);
    int toI = move.i;
    int toJ = move.j;
    String cellType = fromTile.getCellType();

    Tile toTile;

    if (move.d == Move.Direction.LEFT) {
        toI -= 1;
    }
    if (move.d == Move.Direction.RIGHT) {
        toI += 1;
    }
    if (move.d == Move.Direction.UP) {
        toJ -= 1;
    }
    if (move.d == Move.Direction.DOWN) {
        toJ += 1;
    }

    toTile = curr_board.getTile(toI,toJ);

    fromTile.setCellType(Tile.EMPTY);

    toTile.setCellType(cellType);

    refresh();


    }

    /**
     * Refreshes variables about the board given its state
     */
    private void refresh() {
        movesPlayer = curr_board.getAllMoves(ourPlayer);
        movesOpponent = curr_board.getAllMoves(Opponent);
    }


    /**
     * Request a decision from the player as to which move they would like to
     * make next. Your player should consider its options and select the best
     * move available at the time, according to whatever strategy you have
     * developed.
     *
     * The move returned must be a legal move based on the current
     * state of the game. If there are no legal moves, return null (pass).
     *
     * Before returning your move, you should also update your internal
     * representation of the board to reflect the result of the move you are
     * about to make.
     *
     * @return a Move object representing the move you would like to make
     * at this point of the game, or null if there are no legal moves.
     */
    public Move move() {
        if (movesPlayer.isEmpty()) {
            return null;
        }
        System.out.println(ourPlayer + " " + movesPlayer.get(0));
        return movesPlayer.get(0);
    }

    /* Getter Method for Board */
    public Board getBoard() {
        return curr_board;
    }

    /* Getter Method for legalMoveCountPlayer */
    public int getLegalMoveCountPlayer() {
        return legalMoveCountPlayer;
    }

    /* Getter Method for legalMoveCountOpponent */
    public int getLegalMoveCountOpponent() {
        return legalMoveCountOpponent;
    }

    /* Getter Method for legalMoveCountPlayer */
    public ArrayList<Move> getMovesPlayer() { return movesPlayer; }

    /* Getter Method for legalMoveCountPlayer */
    public ArrayList<Move> getMovesOpponent() {
        return movesOpponent;
    }

    /* Getter Method for ourPlayer */
    public String getOurPlayer() {
        return ourPlayer;
    }

    /* Getter Method for Opponent */
    public String getOpponent() {
        return Opponent;
    }

}