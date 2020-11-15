package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.CCCP;
import chess.player.ai.uci.engine.enums.Query;

import java.io.IOException;

public class CCCPClient extends BaseAiClient{
    CCCP engine;
    public CCCPClient() {
        engine = new CCCP();
    }

    public String submit(Query query) {
        String output;

        switch (query.getType()) {
            case Best_Move:
                output = engine.getBestMove(query);
                break;
            default:
                output = null;
                break;
        }

        return output;
    }

    public void close() throws IOException {
        //Do nothing
    }

    @Override
    public String toString() {
        return "CCCPClient";
    }
}
