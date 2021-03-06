// Copyright by Barry G. Becker, 2014. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.game.twoplayer.comparison.execution;

import com.barrybecker4.game.common.GameContext;
import com.barrybecker4.game.common.persistence.GameExporter;
import com.barrybecker4.game.common.player.Player;
import com.barrybecker4.game.common.player.PlayerList;
import com.barrybecker4.game.twoplayer.common.TwoPlayerController;
import com.barrybecker4.game.twoplayer.common.TwoPlayerPlayerOptions;
import com.barrybecker4.game.twoplayer.comparison.model.PerformanceResults;
import com.barrybecker4.game.twoplayer.comparison.model.PerformanceResultsPair;
import com.barrybecker4.game.twoplayer.comparison.model.config.SearchOptionsConfig;
import com.barrybecker4.game.twoplayer.comparison.model.config.SearchOptionsConfigList;
import com.barrybecker4.ui.util.GUIUtil;

import javax.swing.JComponent;
import java.awt.image.BufferedImage;

/**
 * Creates performance results given the controller and list of configuration options to pair off
 * against each other.
 * @author Barry Becker
 */
public class PerformanceResultsBuilder {

    private static final String FILE_SIDE_DELIM = "_vs_";

    private TwoPlayerController controller;
    private SearchOptionsConfigList optionsList;


    /**
     * Constructor.
     * The listener will be called when all the performance results have been computed and normalized.
     */
    PerformanceResultsBuilder(TwoPlayerController controller, SearchOptionsConfigList optionsList) {
        this.controller = controller;
        this.optionsList = optionsList;
    }

    /**
     * Get the results of running strategy i against strategy j.
     * Two rounds are played - the first where player one uses strategy i, and the
     * second where player1 uses strategy j.
     * @param i index for first set of config options
     * @param j index for second set of config options
     * @return the results for a pair of games where each player gets to play first
     */
    public PerformanceResultsPair getResultsForComparison(int i, int j) {

        controller.reset();
        PlayerList players = controller.getPlayers();
        controller.getOptions().setShowGameOverDialog(false);

        Player player1 = players.getPlayer1();
        Player player2 = players.getPlayer2();
        player1.setHuman(false);
        player2.setHuman(false);
        SearchOptionsConfig config1 = optionsList.get(i);
        SearchOptionsConfig config2 = optionsList.get(j);
        ((TwoPlayerPlayerOptions)(player1.getOptions())).setSearchOptions(config1.getSearchOptions());
        ((TwoPlayerPlayerOptions)(player2.getOptions())).setSearchOptions(config2.getSearchOptions());
        String description = config1.getName() + FILE_SIDE_DELIM + config2.getName();

        System.out.println("("+i+", "+j+") round 1  starts:" + config1.getSearchOptions().getSearchStrategyMethod());
        PerformanceResults p1FirstResults = getResultsForRound(player1, player2, description);
        System.out.println("("+i+", "+j+") round 2  starts:" + config2.getSearchOptions().getSearchStrategyMethod());
        PerformanceResults p2FirstResults = getResultsForRound(player2, player1, description);

        return new PerformanceResultsPair(p1FirstResults, p2FirstResults);
    }

    /**
     * Get the results for one of the games in the pair
     */
    private PerformanceResults getResultsForRound(Player player1, Player player2, String description) {

        long startTime = System.currentTimeMillis();
        // make sure the random number sequence is the same for each game to make comparison easier.
        GameContext.setRandomSeed(1);

        PlayerList players = controller.getPlayers();
        players.set(0, player1);
        players.set(1, player2);

        // this used to freeze the UI.
        controller.getViewer().doComputerVsComputerGame();

        assert (controller.isDone());
        System.out.println("******** game is done = " + controller.isDone() +" ******");
        double strengthOfWin = controller.getStrengthOfWin();
        //System.out.println("str of win = " + strengthOfWin);
        int numMoves = controller.getNumMoves();
        BufferedImage finalImage = GUIUtil.getSnapshot((JComponent) controller.getViewer());

        GameExporter exporter = controller.getExporter();

        long elapsedMillis = System.currentTimeMillis() - startTime;

        boolean isTie = !players.anyPlayerWon();
        boolean player1Won = players.getWinningPlayer() == player1;
        controller.reset();

        return new PerformanceResults(player1Won, isTie, strengthOfWin, numMoves, elapsedMillis, finalImage,
                                      exporter, description);
    }
}
