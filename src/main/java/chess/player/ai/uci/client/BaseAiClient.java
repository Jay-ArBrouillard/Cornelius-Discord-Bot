package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.enums.Query;

import java.io.IOException;

public abstract class BaseAiClient {
    public abstract String submit(Query query) throws IOException;
    public abstract void close() throws IOException;
}
