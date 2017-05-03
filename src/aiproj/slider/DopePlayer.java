package aiproj.slider;

import aiproj.slider.Tile;
import aiproj.slider.Board;
import aiproj.slider.Input;
import java.lang.Character;

import static aiproj.slider.Input.*;


public class DopePlayer implements SliderPlayer {
    private Board curr_board;
    private String ourPlayer;
    private String Opponent;
    private int legalMovesPlayer;
    private int legalMovesOpponent;


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
        String playerType = Character.toString(player);
        curr_board = readBoard(dimension, board);
        ourPlayer = playerType;
        Opponent = playerType.equals(Tile.PLAYER_H) ? Tile.PLAYER_H : Tile.PLAYER_V;
        int[] legalMoves = curr_board.countMoves(playerType, true);
        legalMovesPlayer = ourPlayer.equals(Tile.PLAYER_H) ? legalMoves[0] : legalMoves[1];
        legalMovesOpponent = Opponent.equals(Tile.PLAYER_H) ? legalMoves[0] : legalMoves[1];

        // Tests the legalMoves function
        /* Output each count of legal moves as described by Project Spec */
        System.out.println(legalMoves[0]);
        System.out.println(legalMoves[1]);



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
        Move mymove = new Move(1, 1, Move.Direction.DOWN);
        return mymove;
    }

    /* Getter Method for Board */
    public Board getBoard() {
        return curr_board;
    }


}