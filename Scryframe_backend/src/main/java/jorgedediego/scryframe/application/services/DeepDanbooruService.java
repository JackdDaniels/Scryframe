package jorgedediego.scryframe.application.services;

import jorgedediego.scryframe.application.dto.PredictedTagDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DeepDanbooruService {

    /**
     * Path to the DeepDanbooru project (model + tags). Configured via
     * {@code scryframe.deepdanbooru.project-path} in application.properties.
     */
    @Value("${scryframe.deepdanbooru.project-path}")
    private String projectPath;

    /**
     * DeepDanbooru emits one line per predicted tag in the form:
     * <pre>(0.9876) tag_name</pre>
     * Anything that doesn't match this pattern (header lines, info, blank
     * lines) is ignored.
     */
    private static final Pattern TAG_LINE = Pattern.compile("^\\(([0-9]*\\.?[0-9]+)\\)\\s+(\\S+)\\s*$");

    /**
     * Run DeepDanbooru on a single image file and return the structured list
     * of predicted tags.
     *
     * @param imagePath absolute path to the image file on disk
     * @return list of {@link PredictedTagDTO}, possibly empty if the model
     *         produced no parseable output
     */
    public List<PredictedTagDTO> evaluate(Path imagePath) {
        Process process = startEvaluateProcess(imagePath);
        if (process == null) {
            return List.of();
        }

        List<PredictedTagDTO> predictions = new ArrayList<>();
        try {
            try (InputStream stdout = process.getInputStream()) {
                predictions.addAll(parseTagLines(stdout));
            }
            // Drain stderr so the process can exit cleanly; log to console.
            drainStream(process.getErrorStream());

            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("DeepDanbooru process timed out after 5 minutes for " + imagePath);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("DeepDanbooru process interrupted: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException while reading DeepDanbooru output: " + e.getMessage());
        }
        return predictions;
    }

    private Process startEvaluateProcess(Path imagePath) {
        String command = String.format(
                "deepdanbooru evaluate \"%s\" --project-path \"%s\" --allow-folder",
                imagePath.toAbsolutePath(),
                Path.of(projectPath).toAbsolutePath()
        );
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
        try {
            return pb.start();
        } catch (IOException e) {
            System.err.println("Failed to start DeepDanbooru process: " + pb.command());
            System.err.println("  cause: " + e.getMessage());
            return null;
        }
    }

    private static List<PredictedTagDTO> parseTagLines(InputStream stdout) throws IOException {
        List<PredictedTagDTO> predictions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stdout))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = TAG_LINE.matcher(line.trim());
                if (m.matches()) {
                    double confidence = Double.parseDouble(m.group(1));
                    String tag = m.group(2);
                    predictions.add(PredictedTagDTO.builder()
                            .tag(tag)
                            .confidence(confidence)
                            .build());
                }
            }
        }
        return predictions;
    }

    private static void drainStream(InputStream stream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[deepdanbooru stderr] " + line);
            }
        }
    }
}
