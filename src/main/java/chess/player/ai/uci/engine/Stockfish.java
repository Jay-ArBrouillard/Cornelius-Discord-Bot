package chess.player.ai.uci.engine;

import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.*;

public class Stockfish extends UCIEngine {
    public Stockfish(Variant variant, String filePath, Option... options) throws IOException {
        super(variant, filePath, options);
    }

    public String makeMove(Query query) throws IOException {
        waitForReady();
        sendCommand("position fen " + query.getFen() + " moves " + query.getMove());
        return getFen();
    }

    public String getBestMove(Query query) throws IOException {
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

        String result = readLine("bestmove");
        if (result != null) return result.substring(9).split("\\s+")[0];
        return null;
    }

    public String getEvaluation(Query query) throws IOException {
        waitForReady();
        sendCommand("position fen " + query.getFen());

        StringBuilder command = new StringBuilder("eval ");

        waitForReady();
        sendCommand(command.toString());

        return readLine("Final evaluation:");
    }

    public String getLegalMoves(Query query) throws IOException {
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
        try {
            sendCommand("quit");
        } finally {
            process.destroy();
            input.close();
            output.close();
        }
    }

    private String getFen() throws IOException {
        waitForReady();
        sendCommand("d");

        return readLine("Fen: ").substring(5);
    }
}