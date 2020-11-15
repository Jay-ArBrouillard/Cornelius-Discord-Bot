package chess.player.ai.uci.engine;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.pgn.FenUtils;
import chess.player.MoveTransition;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.util.*;
import java.io.IOException;

public class Worstfish extends UCIEngine {
    public Worstfish(Variant variant, String filePath, Option... options) throws IOException {
        super(variant, filePath, options);
    }

    public String getBestMove(Query query) throws IOException {
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : query.getBoard().getCurrentPlayer().getLegalMoves()) {
            MoveTransition transition = query.getBoard().getCurrentPlayer().makeMove(move, true);
            if (transition.getMoveStatus().isDone()) {
                legalMoves.add(move);
            }
        }

        final Board board = query.getBoard();
        Move worstMove = legalMoves.get(0);
        double worstScore = 0;
        boolean isFirstMove = true;

        for (Move move : legalMoves) {
            MoveTransition transition = board.getCurrentPlayer().makeMove(move, true);
            StringBuilder posBuilder = new StringBuilder("position fen ");
            posBuilder.append(FenUtils.parseFEN(transition.getTransitionBoard()));
            sendCommand(posBuilder.toString());
            String evalString = getEvaluation();
            if (evalString != null) {
                double evaluationScore = Double.parseDouble(evalString.substring(22).replace("(white side)", "").trim());
                if (board.getCurrentPlayer().getAlliance().isWhite()) {
                    if (isFirstMove) {
                        worstMove = move;
                        worstScore = evaluationScore;
                        isFirstMove = false;
                        System.out.println("worstMove:" + move.toString());
                    } else if (evaluationScore < worstScore) {
                        worstMove = move;
                        worstScore = evaluationScore;
                        System.out.println("worstMove:" + move.toString());
                    }
                }
                else {
                    if (isFirstMove) {
                        worstMove = move;
                        worstScore = evaluationScore;
                        isFirstMove = false;
                        System.out.println("worstMove:" + move.toString());
                    } else if (evaluationScore > worstScore) {
                        worstMove = move;
                        worstScore = evaluationScore;
                        System.out.println("worstMove:" + move.toString());
                    }
                }
            }
        }

        return BoardUtils.getPositionAtCoordinate(worstMove.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(worstMove.getDestinationCoordinate());
    }

    public String getEvaluation() throws IOException {
        waitForReady();
        sendCommand("eval");
        return readLine("Final evaluation:");
    }

    public void close() throws IOException {
        super.close();
    }

    private String getFen() throws IOException {
        waitForReady();
        sendCommand("d");

        return readLine("Fen: ").substring(5);
    }
}
