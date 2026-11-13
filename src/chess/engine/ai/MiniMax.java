package chess.engine.ai;

import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public class MiniMax {

    private static final int INFINITY = 1_000_000;
    private static final int MATE = 100_000;

    private final BoardEvaluator evaluator;
    private final int searchDepth;

    public MiniMax(final int searchDepth) {
        if (searchDepth < 1) {
            throw new IllegalArgumentException("Search depth must be at least one");
        }
        this.evaluator = new BoardEvaluator();
        this.searchDepth = searchDepth;
    }

    public Move findBestMove(final Board board) {
        return findBestMove(board, () -> false);
    }

    public Move findBestMove(final Board board, final BooleanSupplier isCancelled) {
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(isCancelled, "isCancelled");
        Move bestMove = null;
        int alpha = -INFINITY;

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            if (isCancelled.getAsBoolean()) {
                return null;
            }
            final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                continue;
            }
            final int score = -search(transition.getTransitionBoard(), this.searchDepth - 1, -INFINITY, -alpha, isCancelled);
            if (bestMove == null || score > alpha) {
                bestMove = move;
                alpha = score;
            }
        }
        return bestMove;
    }

    private int search(final Board board, final int depth, int alpha, final int beta, final BooleanSupplier isCancelled) {
        if (isCancelled.getAsBoolean()) {
            return 0;
        }
        if (board.getCurrentPlayer().isInCheckMate()) {
            return -(MATE + depth);
        }
        if (board.getCurrentPlayer().isInStaleMate()) {
            return 0;
        }
        if (depth == 0) {
            return this.evaluator.evaluate(board);
        }

        boolean foundMove = false;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            if (isCancelled.getAsBoolean()) {
                return 0;
            }
            final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                continue;
            }
            foundMove = true;
            final int score = -search(transition.getTransitionBoard(), depth - 1, -beta, -alpha, isCancelled);
            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        if (!foundMove) {
            return board.getCurrentPlayer().isInCheck() ? -(MATE + depth) : 0;
        }
        return alpha;
    }
}
