package chess.player.ai.uci.engine;

import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;

public class Fruit extends UCIEngine {
    public Fruit(Variant variant, String filePath, Option... options) throws IOException {
        super(variant, filePath, options);

        waitForReady();
        sendCommand("setoption name BookFile value /app/bin/books/pwned-book.bin");
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