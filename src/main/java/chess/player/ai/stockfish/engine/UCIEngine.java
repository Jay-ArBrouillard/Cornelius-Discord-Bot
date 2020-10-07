package chess.player.ai.stockfish.engine;

import chess.player.ai.stockfish.engine.enums.Option;
import chess.player.ai.stockfish.engine.enums.Variant;
import chess.player.ai.stockfish.exception.StockfishEngineException;
import chess.player.ai.stockfish.exception.StockfishInitException;

import java.io.*;
import java.util.*;

abstract class UCIEngine {
    final BufferedReader input;
    final BufferedWriter output;
    final Process process;

    UCIEngine(String path, Variant variant, Option... options) throws StockfishInitException {
        try {
            process = new ProcessBuilder("bin/stockfish_20090216_x64_bmi2.exe").start();
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (Option option : options)
                passOption(option);
        } catch (IOException e) {
            throw new StockfishInitException("Unable to start and bind Stockfish process: ", e);
        }
    }

    void waitForReady() {
        sendCommand("isready");
        readResponse("readyok");
    }

    void sendCommand(String command) {
        try {
            output.write(command.getBytes() + "\n");
            output.flush();
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
    }

    String readLine(String expected) {
        try {
            String line;

            while ((line = input.readLine()) != null) {
                if (line.startsWith(expected))
                    return line;
            }

            return null;
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
    }

    List<String> readResponse(String expected) {
        try {
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = input.readLine()) != null) {
                lines.add(line);

                if (line.startsWith(expected))
                    break;
            }

            return lines;
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
    }

    private void passOption(Option option) {
        if (option.getValue() != null) {
            sendCommand(option.toString());
        }
    }
}
