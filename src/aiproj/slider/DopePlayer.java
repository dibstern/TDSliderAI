package aiproj.slider;

import aiproj.slider.Tile;
import aiproj.slider.Board;
import aiproj.slider.Input;
import java.lang.Character;
import java.util.ArrayList;
import java.util.List;
import aiproj.slider.Move;

import static aiproj.slider.Input.*;


public class DopePlayer implements SliderPlayer {

    // Keeping Track of the Board, Player Piece Types, Number of current legal moves
    private Board curr_board;
    private int boardsize;
    private String ourPlayer;
    private String Opponent;
    private ArrayList<Move> movesPlayer;
    private ArrayList<Move> movesOpponent;
    private ArrayList<Tile> playerTiles;
    private ArrayList<Tile> opponentTiles;


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

        // Saving Board size
        boardsize = dimension;

        // Getting All Possible Moves
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

    // Provided Move Implementation uses i as x and j as y
    int fromX = move.i;
    int fromY = move.j;
    int toX = fromX;
    int toY = fromY;

    Tile fromTile = curr_board.getTile(fromX, fromY);
    String cellType = fromTile.getCellType();

    if (move.d == Move.Direction.LEFT) {
        toX -= 1;
    }
    if (move.d == Move.Direction.RIGHT) {
        toX += 1;
    }
    if (move.d == Move.Direction.UP) {
        toY += 1;
    }
    if (move.d == Move.Direction.DOWN) {
        toY -= 1;
    }
    updateTileArray(move);
    curr_board.updateTile(toX, toY, cellType);
    curr_board.updateTile(fromX, fromY, Tile.EMPTY);
    refresh();
    }


    /**
     * Refreshes variables about the board given its state and given which turn has been played
     */
    private void refresh() {
        movesPlayer = curr_board.getAllMoves(ourPlayer);
        movesOpponent = curr_board.getAllMoves(Opponent);
    }

    /**
     * Updates the recorded array of tiles, given the new move
     * @param newMove A move object, played either by the player or by the opponent.
     */
    private void updateTileArray(Move newMove) {
        // Find new X and Y
        int oldX = newMove.i;
        int oldY = newMove.j;
        Move.Direction direction = newMove.d;

        int newX = (direction == Move.Direction.LEFT) ? (oldX - 1) : oldX;
        newX = (direction == Move.Direction.RIGHT) ? (oldX + 1) : newX;

        int newY = (direction == Move.Direction.UP) ? (oldY + 1) : oldY;
        newY = (direction == Move.Direction.DOWN) ? (oldY - 1) : newY;

        // Retrieve old tile, and its type
        Tile oldTile = curr_board.getTile(oldX, oldY);
        String tileType = oldTile.getCellType();

        // Get Positional Indices for new tile
        int[] newpos = curr_board.getPos(newX, newY);
        int newRow = newpos[0];
        int newCol = newpos[1];

        // Use these to find New tile
        Tile newtile = new Tile(tileType, newRow, newCol, boardsize);

        // If h, remove old tile from h_tiles and add new tile to h_tiles
        if (tileType.equals(Tile.PLAYER_H)) {
            curr_board.removeHTile(curr_board.getTile(oldX, oldY));
            if (validPos(newX, newY)) {
                curr_board.addHTile(newtile);
            }
        }
        // If V (updateTiles only called on non-null moves so can assume it's V if not H)
        else if (tileType.equals(Tile.PLAYER_V)) {
            curr_board.removeVTile(curr_board.getTile(oldX, oldY));
            if (validPos(newX, newY)) {
                curr_board.addVTile(newtile);
            }
        }
    }


    /**
     * Same as updateTileArray except modifies any board passed in (not the current board)
     * So can be used for any board
     */
    private void updateTileArray2(Board board, Move newMove) {
        // Find new X and Y
        int oldX = newMove.i;
        int oldY = newMove.j;
        Move.Direction direction = newMove.d;

        int newX = (direction == Move.Direction.LEFT) ? (oldX - 1) : oldX;
        newX = (direction == Move.Direction.RIGHT) ? (oldX + 1) : newX;

        int newY = (direction == Move.Direction.UP) ? (oldY + 1) : oldY;
        newY = (direction == Move.Direction.DOWN) ? (oldY - 1) : newY;

        // Retrieve old tile, and its type
        Tile oldTile = board.getTile(oldX, oldY);
        String tileType = oldTile.getCellType();

        // Get Positional Indices for new tile
        int[] newpos = board.getPos(newX, newY);
        int newRow = newpos[0];
        int newCol = newpos[1];

        // Use these to find New tile
        Tile newtile = new Tile(tileType, newRow, newCol, board.getLength());

        // If h, remove old tile from h_tiles and add new tile to h_tiles
        if (tileType.equals(Tile.PLAYER_H)) {
            board.removeHTile(board.getTile(oldX, oldY));
            if (validPos2(newX, newY, board)) {
                board.addHTile(newtile);
            }
        }
        // If V (updateTiles only called on non-null moves so can assume it's V if not H)
        else if (tileType.equals(Tile.PLAYER_V)) {
            board.removeVTile(board.getTile(oldX, oldY));
            if (validPos2(newX, newY,board)) {
                board.addVTile(newtile);
            }
        }
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
        //Move firstMove = movesPlayer.get(0);
        Move firstMove = minimaxDecision(curr_board, movesPlayer);
        update(firstMove);

        //DEBUG
        System.out.println("Board evaluation function (player "+ourPlayer+"): "+ evaluateBoard(curr_board));

        return firstMove;
    }

    private int evaluateBoard(Board board) {

        // Set list of tiles based on who the player is
        if (ourPlayer.equals(Tile.PLAYER_H)) {
            this.playerTiles = board.getHTiles();
            this.opponentTiles = board.getVTiles();

        }
        else  if (ourPlayer.equals(Tile.PLAYER_V)) {
            this.playerTiles = board.getVTiles();
            this.opponentTiles = board.getHTiles();
        }
        else {
            System.out.println("Catastrophic error: incorrect player name");
            System.exit(0);
        }


        //FEATURE DEFINITION


        // playerTileDifference is preferable to having a separate number for each player tiles
        // This is because the tile difference will perform better in machine learning (fewer, less
        // noisy possible values)
        int playerTileDifference = playerTiles.size() - opponentTiles.size();

        // Sum the distances of each player
        int sumPlayerDistances = sumDistances(playerTiles,  board.getLength());
        int sumOpponentDistances = sumDistances(opponentTiles, board.getLength());

        // Add all features to an arraylist and define weights
        // Weights are presently +ve (good) or -ve (bad)
        // Eventually we will define these elsewhere with Machine Learning

        ArrayList<Integer> features = new ArrayList<Integer>();
        ArrayList<Integer> featureWeights = new ArrayList<Integer>();
        features.add(playerTileDifference);
        featureWeights.add(-1);
        features.add(sumPlayerDistances);
        featureWeights.add(-1);
        features.add(sumOpponentDistances);
        featureWeights.add(1);

        // Sum total based on feature weights

        int total = 0;
        for (int i=0; i<featureWeights.size();i++) {
            total += featureWeights.get(i) * features.get(i);
        }
        return total;
    }

    private int sumDistances(ArrayList<Tile> tiles, int boardSize) {
        int total = 0;

        if (tiles.size() <1) {
            return 0;
        }

        //Set the player type (assume all tiles passed in are the same player)
        String player = tiles.get(0).getCellType();

        for (Tile tile : tiles) {
            if (player.equals(Tile.PLAYER_H)) {
                total += boardSize-tile.getX();
            }
            else {
                total += boardSize-tile.getY();
            }
        }


        return total;
    }

    public Move minimaxDecision(Board board, ArrayList<Move> moves) {


        if (moves.size() ==0) {
            return null;
        }

        for (Move move : moves) {
            Board newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            newBoard.boardDisplay();
            int val = minimaxValue(board);
        }


        return moves.get(0);

    }

    public int minimaxValue(Board board) {

        return 0;
    }

    private Board modifyBoard(Board board, Move move) {


        if (move == null) {
            return board;
        }

        // Provided Move Implementation uses i as x and j as y
        int fromX = move.i;
        int fromY = move.j;
        int toX = fromX;
        int toY = fromY;

        Tile fromTile = board.getTile(fromX, fromY);
        String cellType = fromTile.getCellType();

        if (move.d == Move.Direction.LEFT) {
            toX -= 1;
        }
        if (move.d == Move.Direction.RIGHT) {
            toX += 1;
        }
        if (move.d == Move.Direction.UP) {
            toY += 1;
        }
        if (move.d == Move.Direction.DOWN) {
            toY -= 1;
        }
        updateTileArray2(board, move);
        board.updateTile(toX, toY, cellType);
        board.updateTile(fromX, fromY, Tile.EMPTY);

        // generic altering boards doesnt need a refresh
        //refresh();

        return board;



    }




    /* Getter Method for Board */
    public Board getBoard() {
        return curr_board;
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

    private Boolean validPos(int x, int y) {
        if (x < boardsize && x >= 0 && boardsize-y-1 < boardsize && boardsize-y-1 >= 0) {
            return true;
        }
        return false;
    }

    /**
     *  Same as validPos but takes a generic board not the current board
     * @param x
     * @param y
     * @param board
     * @return
     */
    private Boolean validPos2(int x, int y, Board board) {
        boardsize = board.getLength();
        if (x < boardsize && x >= 0 && boardsize-y-1 < boardsize && boardsize-y-1 >= 0) {
            return true;
        }
        return false;
    }

}