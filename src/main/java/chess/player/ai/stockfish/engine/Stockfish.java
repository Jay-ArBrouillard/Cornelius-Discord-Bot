package chess.player.ai.stockfish.engine;

import chess.player.ai.stockfish.engine.enums.Option;
import chess.player.ai.stockfish.engine.enums.Query;
import chess.player.ai.stockfish.engine.enums.Variant;
import chess.player.ai.stockfish.exception.StockfishInitException;

import java.util.*;
import java.io.IOException;

public class Stockfish extends UCIEngine {
    public Stockfish(String path, Variant variant, Option... options) throws StockfishInitException {
        super(path, variant, options);
    }

    public String makeMove(Query query) {
        waitForReady();
        sendCommand("position fen " + query.getFen() + " moves " + query.getMove());
        return getFen();
    }

    public String getCheckers(Query query) {
        waitForReady();
        sendCommand("position fen " + query.getFen());

        waitForReady();
        sendCommand("d");

        return readLine("Checkers: ").substring(10);
    }

    public String getBestMove(Query query) {
        if (query.getDifficulty() >= 0) {
            waitForReady();
            sendCommand("setoption name Skill Level value " + query.getDifficulty());
        }

        waitForReady();
        sendCommand("position fen " + query.getFen());

        StringBuilder command = new StringBuilder("go ");

        if (query.getDepth() >= 0)
            command.append("depth ").append(query.getDepth()).append(" ");

        if (query.getMovetime() >= 0)
            command.append("movetime ").append(query.getMovetime());

        waitForReady();
        sendCommand(command.toString());

        return readLine("bestmove").substring(9).split("\\s+")[0];
    }

    public String getEvaluation(Query query) {
        waitForReady();
        sendCommand("position fen " + query.getFen());

        StringBuilder command = new StringBuilder("eval ");

        waitForReady();
        sendCommand(command.toString());

        return readLine("Final evaluation:");
    }

    public String getLegalMoves(Query query) {
        waitForReady();
        sendCommand("position fen " + query.getFen());

        waitForReady();
        sendCommand("go perft 1");

        StringBuilder legal = new StringBuilder();
        List<String> response = readResponse("Nodes");

        for (String line : response)
            if (!line.isEmpty() && !line.contains("Nodes") && line.contains(":"))
                legal.append(line.split(":")[0]).append(" ");

        return legal.toString();
    }

    public void close() throws IOException {
//        try {
//            sendCommand("quit");
//        } finally {
//            process.destroy();
//            input.close();
//            output.close();
//        }
    }

    private String getFen() {
        waitForReady();
        sendCommand("d");

        return readLine("Fen: ").substring(5);
    }
}