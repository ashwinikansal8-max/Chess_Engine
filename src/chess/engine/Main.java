package chess.engine;

import chess.engine.ai.MiniMax;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;
import chess.engine.player.Player;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Board board = Board.createStandardBoard();
        final MiniMax minimax = new MiniMax(3);
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println(board);
            final Player current = board.getCurrentPlayer();

            if (current.isInCheckMate()) {
                System.out.println(current.getOpponent().getAlliance() + " wins by checkmate");
                break;
            }
            if (current.isInStaleMate()) {
                System.out.println("stalemate, the game is a draw");
                break;
            }
            if (current.isInCheck()) {
                System.out.println(current.getAlliance() + " is in check");
            }

            final MoveTransition transition;
            if (current.getAlliance().isWhite()) {
                transition = askForMove(board, scanner);
            } else {
                final Move engineMove = minimax.findBestMove(board);
                System.out.println("engine plays " + engineMove);
                transition = current.makeMove(engineMove);
            }
            board = transition.getTransitionBoard();
        }
    }

    private static MoveTransition askForMove(final Board board, final Scanner scanner) {
        while (true) {
            System.out.print("your move (like e2e4): ");
            final String input = scanner.nextLine().trim().toLowerCase();

            for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
                if (move.toString().equals(input)) {
                    final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        return transition;
                    }
                }
            }
            System.out.println("that move is not legal, try again");
        }
    }
}
