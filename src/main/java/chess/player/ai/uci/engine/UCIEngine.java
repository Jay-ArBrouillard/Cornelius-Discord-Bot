package chess.player.ai.uci.engine;

import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.*;
import java.util.*;

abstract class UCIEngine {
    final BufferedReader input;
    final BufferedWriter output;
    final Process process;

    UCIEngine(Variant variant, String filePath, Option... options) throws IOException {
        try {
            process = Runtime.getRuntime().exec(getPath(variant, filePath));
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (Option option : options)
                passOption(option);
        } catch (IOException e) {
            throw new IOException("Unable to start and bind Stockfish process: ", e);
        }
    }

    void passOption(Option option) throws IOException {
        sendCommand(option.toString());
    }

    void waitForReady() throws IOException {
        sendCommand("isready");
        readResponse("readyok");
    }

    void sendCommand(String command) throws IOException {
        output.write(command + "\n");
        output.flush();
    }

    String readLine(String expected) throws IOException {
        String line;

        while ((line = input.readLine()) != null) {
            if (line.startsWith(expected))
                return line;
        }

        return line;
    }

    List<String> readResponse(String expected) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;

        while ((line = input.readLine()) != null) {
            lines.add(line);

            if (line.startsWith(expected))
                break;
        }

        return lines;
    }

    String getPath(Variant variant, String filePath) {
        StringBuilder path = new StringBuilder(filePath);

        if (System.getProperty("os.name").toLowerCase().contains("win"))
            switch (variant) {
                case DEFAULT:
                    path.append(".exe");
                    break;
                case BMI2:
                    path.append("_bmi2.exe");
                    break;
                case POPCNT:
                    path.append("_popcnt.exe");
                    break;
                default:
                    throw new RuntimeException("Illegal variant provided.");
            }
        else
            switch (variant) {
                case DEFAULT:
                    //Add none
                    break;
                case BMI2:
                    path.append("_bmi2");
                    break;
                case AVX:
                    path.append("_avx2");
                    break;
                case SSE:
                    path.append("_sse");
                    break;
                case MODERN:
                    path.append("_modern");
                    break;
                default:
                    throw new RuntimeException("Illegal variant provided.");
            }

        return path.toString();
    }
}
