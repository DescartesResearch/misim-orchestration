package cambio.simulator.orchestration.export;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CSVBuilder {
    private List<String> headers;
    private final List<List<String>> rows;
    private String delimiter;

    public CSVBuilder() {
        headers = new ArrayList<>();
        rows = new ArrayList<>();
        delimiter = ",";
    }

    public CSVBuilder delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public CSVBuilder headers(List<String> headers) {
        this.headers = new ArrayList<>(headers);
        return this;
    }

    public CSVBuilder row(List<String> row) {
        rows.add(new ArrayList<>(row));
        return this;
    }

    public void build(Path path) throws CSVBuilderException {
        checkFormat(path);
        try (Writer writer = Files.newBufferedWriter(path)) {
            writer.write(convertToCSVLine(headers));
            writer.write("\n");
            for (List<String> row : rows) {
                writer.write(convertToCSVLine(row));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new CSVBuilderException(path, e.getMessage());
        }

    }

    private void checkFormat(Path path) throws CSVBuilderException {
        if (headers.isEmpty()) {
            throw new CSVBuilderException(path, "Headers are empty");
        }
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            throw new CSVBuilderException(path, "Rows are empty");
        }

        int fieldCount = headers.size();
        for (List<String> row : rows) {
            if (row.size() != fieldCount) {
                throw new CSVBuilderException(path, "Row has different number of fields than the header");
            }
        }
    }

    private String convertToCSVLine(List<String> data) {
        return data.stream().map(this::escapeSpecialCharacters).collect(Collectors.joining(this.delimiter));
    }

    private String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(delimiter) || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public class CSVBuilderException extends Exception {

        public CSVBuilderException(String message) {
            super(message);
        }

        public CSVBuilderException(Path path, String message) {
            this("Error writing to file with path: \"" + path.toAbsolutePath() + "\"\nMessage:\n" + message);
        }
    }
}
