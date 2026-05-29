package com.ssafy.yumyum.batch.nutrition;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

public class CsvNutritionItemReader implements ItemStreamReader<RawNutritionRow> {

    private final Path sourcePath;
    private final String sourceName;
    private final NutritionSourceProfile profile;

    private BufferedReader reader;
    private List<String> headers;
    private int rowNo;

    public CsvNutritionItemReader(String sourcePath, String sourceName) {
        this.sourcePath = Path.of(sourcePath);
        this.sourceName = sourceName;
        this.profile = NutritionSourceProfile.resolve(sourceName, sourcePath);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            InputStream inputStream = new BufferedInputStream(Files.newInputStream(sourcePath));
            Charset charset = detectCharset(inputStream);
            reader = new BufferedReader(new InputStreamReader(inputStream, charset));
            String headerLine = reader.readLine();
            rowNo = 1;
            if (headerLine == null) {
                headers = List.of();
                return;
            }
            headers = parseCsvLine(stripBom(headerLine));
        } catch (IOException e) {
            throw new ItemStreamException("Could not open CSV file: " + sourcePath, e);
        }
    }

    @Override
    public RawNutritionRow read() throws Exception {
        if (reader == null || headers.isEmpty()) {
            return null;
        }
        String line;
        while ((line = reader.readLine()) != null) {
            rowNo++;
            if (line.isBlank()) {
                continue;
            }
            List<String> cells = parseCsvLine(line);
            Map<String, String> cellsByHeader = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String value = i < cells.size() ? cells.get(i) : null;
                cellsByHeader.put(headers.get(i), value);
            }
            return profile.mapRow(sourceName, sourcePath.toString(), rowNo, cellsByHeader);
        }
        return null;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt("nutrition.csv.rowNo", rowNo);
    }

    @Override
    public void close() throws ItemStreamException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new ItemStreamException("Could not close CSV file: " + sourcePath, e);
            }
        }
    }

    private static Charset detectCharset(InputStream inputStream) throws IOException {
        inputStream.mark(4096);
        byte[] sample = inputStream.readNBytes(4096);
        int b1 = sample.length > 0 ? sample[0] & 0xFF : -1;
        int b2 = sample.length > 1 ? sample[1] & 0xFF : -1;
        int b3 = sample.length > 2 ? sample[2] & 0xFF : -1;
        if (b1 == 0xEF && b2 == 0xBB && b3 == 0xBF) {
            inputStream.reset();
            return StandardCharsets.UTF_8;
        }
        inputStream.reset();
        if (isValidUtf8(sample)) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName("MS949");
    }

    private static boolean isValidUtf8(byte[] sample) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            decoder.decode(ByteBuffer.wrap(sample));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    private static String stripBom(String value) {
        return value == null ? null : value.replace("\uFEFF", "");
    }

    static List<String> parseCsvLine(String line) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }
}
