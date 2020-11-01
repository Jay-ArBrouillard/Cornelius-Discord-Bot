package chess.player.ai.uci.engine;

import chess.player.ai.uci.client.RodentClient;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

abstract class UCIEngine {
    BufferedReader input;
    BufferedWriter output;
    Process process;

    UCIEngine() {
        //empty
    }

    UCIEngine(Variant variant, String filePath, Option... options) throws IOException {
        try {
            process = Runtime.getRuntime().exec(getPath(variant, filePath));
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (Option option : options) {
                Object value = option.getValue();
                if (value != null && value.toString().endsWith(".txt")) {
                    List<String> lines = Files.readAllLines(Paths.get(value.toString()));
                    for (String line : lines) {
                        if (line.startsWith("setoption")) {
                            sendCommand(line);
                        }
                    }
                }
                else {
                    passOption(option);
                }
            }
        } catch (IOException e) {
            throw new IOException("Unable to start and bind " + super.getClass().getSimpleName() + " process: ", e);
        }
    }


    /**
     * Case when UCI doesn't immediately initiate
     * @param variant
     * @param filePath
     * @param startUpReadCommand
     * @param options
     * @throws IOException
     */
    UCIEngine(Variant variant, String filePath, String startUpReadCommand, Option... options) throws IOException {
        try {
            process = Runtime.getRuntime().exec(getPath(variant, filePath));
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            readResponse(startUpReadCommand);
            sendCommand("uci");
            readResponse("uciok");

            boolean hashIsSet = false;
            for (Option option : options) {
                if (option.getOptionString().contains("Hash")) {
                    if (!hashIsSet) {
                        passOption(option);
                        hashIsSet = true;
                    }
                }
                else {
                    passOption(option);
                }
            }
        } catch (IOException e) {
            throw new IOException("Unable to start and bind " + super.getClass().getSimpleName() + " process: ", e);
        }
    }

    void passOption(Option option) throws IOException {
//        System.out.println(option.toString()); //To remove
        sendCommand(option.toString());
    }

    void waitForReady() throws IOException {
        sendCommand("isready");
        readResponse("readyok");
    }

    void sendCommand(String command) throws IOException {
//        System.out.println("sendCommand:"+command); //To remove
        output.write(command + "\n");
        output.flush();
    }

    String readLine(String expected) throws IOException {
        String line;

        while ((line = input.readLine()) != null) {
//            System.out.println("readLine:"+line); //To remove
            if (line.startsWith(expected))
                return line;
        }

        return null;
    }

    List<String> readResponse(String expected, int amount) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        int count = 0;

        while ((line = input.readLine()) != null && count < amount) {
//            System.out.println("readResponse:"+line); //To remove
            lines.add(line);

            if (line.startsWith(expected)) {
                count++;
            }
        }

        return lines;
    }


    List<String> readResponse(String expected) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;

        while ((line = input.readLine()) != null) {
//            System.out.println("readResponse:"+line); //To remove
            lines.add(line);

            if (line.startsWith(expected))
                break;
        }

        return lines;
    }

    void close() throws IOException {
        try {
            sendCommand("quit");
        } finally {
            process.destroy();
            if (process.isAlive()) {
                process.destroyForcibly();
            }
            input.close();
            output.close();
        }
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
                case POPCNT:
                    path.append("_popcnt");
                    break;
                default:
                    throw new RuntimeException("Illegal variant provided.");
            }

        return path.toString();
    }
}
