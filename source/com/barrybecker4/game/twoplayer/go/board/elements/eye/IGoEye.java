/* Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.game.twoplayer.go.board.elements.eye;

import com.barrybecker4.game.twoplayer.go.board.analysis.eye.information.EyeInformation;
import com.barrybecker4.game.twoplayer.go.board.analysis.eye.information.EyeStatus;
import com.barrybecker4.game.twoplayer.go.board.elements.IGoSet;
import com.barrybecker4.game.twoplayer.go.board.elements.group.IGoGroup;
import com.barrybecker4.game.twoplayer.go.board.elements.position.GoBoardPositionSet;

/**
 *  A GoEye is composed of a strongly connected set of empty spaces (and possibly some dead enemy stones).
 *
 *  @author Barry Becker
 */
public interface IGoEye extends IGoSet {

    EyeStatus getStatus();

    EyeInformation getInformation();

    String getEyeTypeName();

    int getNumCornerPoints();

    int getNumEdgePoints();

    /**
     * @return the group that this eye belongs to.
     */
    IGoGroup getGroup();

    /**
     * @return the hashSet containing the members
     */
    @Override
    GoBoardPositionSet getMembers();

    /**
     * empty the positions from the eye.
     */
    void clear();

    /**
     * @return true if unconditionally alive.
     */
    boolean isUnconditionallyAlive();

    void setUnconditionallyAlive(boolean unconditionallyAlive);
}
