package com.jtprince.bingo.plugin;

import java.util.*;

/**
 * Class to maintain the Plugin-side latest known markings on a single Player's board.
 *
 * The data maintained here is not authoritative; the web backend maintains the authoritative
 * version.
 */
public class PlayerBoard {
    final BingoGame game;
    final UUID playerUuid;

    /**
     * The latest known markings on this board.
     * Not authoritative; the webserver holds the authoritative copy.
     */
    private final ArrayList<Integer> markings;
    private final Set<Integer> announcedPositions = new HashSet<>();

    /* Board marking states */
    final static int UNMARKED = 0;
    final static int COMPLETE = 1;
    final static int REVERTED = 2;
    final static int INVALIDATED = 3;

    public PlayerBoard(UUID playerUuid, BingoGame game) {
        this.playerUuid = playerUuid;
        this.game = game;

        int numSquares = game.gameBoard.getSquares().size();
        this.markings = new ArrayList<>(Collections.nCopies(numSquares, UNMARKED));
    }

    /**
     * Update the latest known markings to the provided list of markings.
     * @param jsonBoardStr A board marking string directly from the websocket, such as
     *                     "0000000000001000000000000" for a board with only the middle square
     *                     marked.
     */
    public void update(String jsonBoardStr) {
        if (jsonBoardStr.length() != this.markings.size()) {
            throw new ArrayIndexOutOfBoundsException(
                "Received board marking string of " + jsonBoardStr.length() + " squares; expected: "
                    + this.markings.size());
        }

        for (int i = 0; i < this.markings.size(); i++) {
            int toState = Character.getNumericValue(jsonBoardStr.charAt(i));
            if (this.markings.get(i) != toState) {
                this.onChange(i, toState);
            }
            this.markings.set(i, toState);
        }
    }

    private void onChange(int position, int toState) {
        this.considerAnnounceChange(position, toState);
    }

    private void considerAnnounceChange(int position, int toState) {
        if (this.announcedPositions.contains(position)) {
            return;
        }

        Boolean invalidated = null;  // null => no announcement
        if (toState == COMPLETE) {
            invalidated = false;
        }

        if (toState == INVALIDATED) {
            invalidated = true;
        }

        if (invalidated != null) {
            this.announcedPositions.add(position);
            Square square = this.game.gameBoard.getSquares().get(position);
            this.game.messages.announcePlayerMarking(this.playerUuid, square, invalidated);
        }
    }
}
