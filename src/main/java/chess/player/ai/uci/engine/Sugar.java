package chess.player.ai.uci.engine;

import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.List;

/**
 * When running Sugar it will create experience.bin and pawngame.bin for persistent learning
 */
public class Sugar extends UCIEngine {
    public Sugar(Variant variant, String filePath, Option... options) throws IOException {
        super(variant, filePath, options);

        waitForReady();
        sendCommand("setoption name Less Pruning Mode value 3");
        sendCommand("setoption name Use MCTS Score value true");
        sendCommand("setoption name BookFile value books/elo-2650.bin"); //Relative paths work not absolute for here
        sendCommand("setoption name BookFile value books/ph-exoticbook.bin");

        //Ensure books are read in
        List<String> responses = readResponse("info string", 2);
        int numSuccesses = 0;
        for (String s : responses) {
            if (s.contains("Book Loaded")) {
                numSuccesses++;
            }
            if (s.contains("Could not open book")) {
                break;
            }
        }

        if (numSuccesses != 2) {
            throw new RuntimeException("Error opening book files for " + this.getClass().getSimpleName());
        }
    }

    public String getBestMove(Query query) throws IOException {
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
        if (result != null) return result.split("\\s+")[1].trim();
        return null;
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
