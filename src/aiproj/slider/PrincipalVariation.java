package aiproj.slider;

/**
 * Created by David on 14/5/17.
 */

//import aiproj.slider.Board;

import java.util.ArrayList;


public class PrincipalVariation {

    private Board board;
    private double value;
    private ArrayList<Double> features;


    public PrincipalVariation(Board given_board, ArrayList<Double> given_features, double given_value) {
        board = given_board;
        features = given_features;
        value = given_value;
    }

    public Board getBoard() {
        return board;
    }

    public double getValue() {
        return value;
    }

    public ArrayList<Double> getFeatures() {
        return features;
    }

}
