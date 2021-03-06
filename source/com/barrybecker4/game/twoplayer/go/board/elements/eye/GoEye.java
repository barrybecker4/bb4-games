/* Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.game.twoplayer.go.board.elements.eye;

import com.barrybecker4.game.common.GameContext;
import com.barrybecker4.game.twoplayer.go.board.GoBoard;
import com.barrybecker4.game.twoplayer.go.board.analysis.eye.EyeTypeAnalyzer;
import com.barrybecker4.game.twoplayer.go.board.analysis.eye.information.EyeInformation;
import com.barrybecker4.game.twoplayer.go.board.analysis.eye.information.EyeStatus;
import com.barrybecker4.game.twoplayer.go.board.analysis.group.GroupAnalyzer;
import com.barrybecker4.game.twoplayer.go.board.elements.group.IGoGroup;
import com.barrybecker4.game.twoplayer.go.board.elements.position.GoBoardPosition;
import com.barrybecker4.game.twoplayer.go.board.elements.position.GoBoardPositionList;
import com.barrybecker4.game.twoplayer.go.board.elements.position.GoBoardPositionSet;
import com.barrybecker4.game.twoplayer.go.board.elements.position.GoStone;
import com.barrybecker4.game.twoplayer.go.board.elements.string.GoString;

/**
 *  A GoEye is composed of a strongly connected set of empty spaces (and possibly some dead enemy stones).
 *  By strongly connected I mean nobi connections only.
 *  A GoEye may be one of several different eye types enumerated below
 *  A group needs 2 provably true eyes to live.
 *
 *  @author Barry Becker
 */
public class GoEye extends GoString implements IGoEye {

    /** A set of positions that are in the eye space. Need not be empty. */
    private GoBoardPositionSet members_;

    /** The kind of eye that this is. */
    private final EyeInformation information_;

    /** In addition to the type, an eye can have a status like nakade, unsettled, or aliveInAtari. */
    private final EyeStatus status_;

    private int numCornerPoints_;
    private int numEdgePoints_;

    /**
     * Constructor.
     * Immutable after construction.
     * Create a new eye shape containing the specified list of stones/spaces.
     * Some of the spaces may be occupied by dead opponent stones.
     */
    public GoEye( GoBoardPositionList spaces, GoBoard board, IGoGroup g, GroupAnalyzer groupAnalyzer) {
        super( spaces, board );
        group_ = g;
        ownedByPlayer1_ = g.isOwnedByPlayer1();

        EyeTypeAnalyzer eyeAnalyzer = new EyeTypeAnalyzer(this, board, groupAnalyzer);
        information_ = eyeAnalyzer.determineEyeInformation();
        status_ = information_.determineStatus(this, board);
        initializePositionCounts(board);
    }

    @Override
    public EyeInformation getInformation() {
        return information_;
    }

    @Override
    public EyeStatus getStatus() {
        return status_;
    }

    @Override
    public String getEyeTypeName() {
        if (information_ == null)
            return "unknown eye type";
        return information_.getTypeName();
    }

    @Override
    public int getNumCornerPoints() {
        return numCornerPoints_;
    }

    @Override
    public int getNumEdgePoints() {
        return numEdgePoints_;
    }

    @Override
    protected void initializeMembers() {
        members_ = new GoBoardPositionSet();
    }

    private void initializePositionCounts(GoBoard board) {
        numCornerPoints_ = 0;
        numEdgePoints_ = 0;
        for (GoBoardPosition pos : getMembers()) {
            if (board.isCornerTriple(pos))  {
               numCornerPoints_++;
            }
            if (board.isOnEdge(pos))  {
               numEdgePoints_++;
            }
        }
    }

    /**
     * @return the hashSet containing the members
     */
    @Override
    public GoBoardPositionSet getMembers() {
        return members_;
    }

    /**
     *  @return true if the piece is an enemy of the string owner.
     *  If the difference in health between the stones is great, then they are not really enemies
     *  because one of them is dead. I used to do the "much weaker" test here but the group health
     *  info is not guaranteed to be updated yet.
     */
    @Override
    public boolean isEnemy(GoBoardPosition pos)
    {
        IGoGroup g = getGroup();
        assert (g != null): "group for "+ this +" is null";
        if (pos.isUnoccupied()) {
            return false;
        }
        GoStone stone = (GoStone)pos.getPiece();

        assert (g.isOwnedByPlayer1() == isOwnedByPlayer1()):
                 "Bad group ownership for eye="+ this +". Owning Group="+g;
        return (stone.isOwnedByPlayer1() != isOwnedByPlayer1()); // && !weaker);
    }

    /**
     * Add a space to the eye string.
     * The space is either blank or a dead enemy stone.
     * Called only during initial construction.
     */
    @Override
    protected void addMemberInternal(GoBoardPosition space, GoBoard board) {
        if ( getMembers().contains( space ) ) {
            GameContext.log(1, "Warning: the eye, " + this + ", already contains " + space);
            assert ( (space.getString() == null) || (this == space.getString())):
                    "bad space or bad owning string" + space.getString();
        }
        space.setEye( this );
        getMembers().add( space );
    }

    @Override
    public void clear() {
        for (GoBoardPosition pos : getMembers()) {
            pos.setEye(null);
            pos.setVisited(false);
        }
        setGroup(null);
        getMembers().clear();
    }

    public String toString() {
        return new EyeSerializer(this).serialize();
    }

    @Override
    protected String getPrintPrefix() {
        return " Eye: " + getEyeTypeName() + ": ";
    }
}
