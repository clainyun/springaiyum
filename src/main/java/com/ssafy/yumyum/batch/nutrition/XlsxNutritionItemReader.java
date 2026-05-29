package com.ssafy.yumyum.batch.nutrition;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XlsxNutritionItemReader implements ItemStreamReader<RawNutritionRow> {

    private static final Object END = new Object();

    private final Path sourcePath;
    private final String sourceName;
    private final NutritionSourceProfile profile;
    private final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(256);
    private final AtomicReference<Exception> failure = new AtomicReference<>();

    private OPCPackage packageAccess;
    private Thread parserThread;

    public XlsxNutritionItemReader(String sourcePath, String sourceName) {
        this.sourcePath = Path.of(sourcePath);
        this.sourceName = sourceName;
        this.profile = NutritionSourceProfile.resolve(sourceName, sourcePath);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            packageAccess = OPCPackage.open(sourcePath.toFile());
            parserThread = new Thread(this::parseFirstSheet, "nutrition-xlsx-reader");
            parserThread.setDaemon(true);
            parserThread.start();
        } catch (Exception e) {
            throw new ItemStreamException("Could not open XLSX file: " + sourcePath, e);
        }
    }

    @Override
    public RawNutritionRow read() throws Exception {
        Object item = queue.take();
        if (item == END) {
            Exception exception = failure.get();
            if (exception != null) {
                throw exception;
            }
            return null;
        }
        return (RawNutritionRow) item;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void close() throws ItemStreamException {
        if (parserThread != null) {
            parserThread.interrupt();
        }
        if (packageAccess != null) {
            try {
                packageAccess.close();
            } catch (Exception e) {
                throw new ItemStreamException("Could not close XLSX file: " + sourcePath, e);
            }
        }
    }

    private void parseFirstSheet() {
        try {
            XSSFReader reader = new XSSFReader(packageAccess);
            StylesTable styles = reader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(packageAccess);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            if (!sheets.hasNext()) {
                return;
            }
            try (InputStream sheet = sheets.next()) {
                XMLReader parser = XMLHelper.newXMLReader();
                XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(
                        styles,
                        null,
                        strings,
                        new SheetHandler(),
                        new DataFormatter(),
                        false
                );
                parser.setContentHandler(handler);
                parser.parse(new InputSource(sheet));
            }
        } catch (Exception e) {
            failure.set(e);
        } finally {
            offerEnd();
        }
    }

    private void offerRow(RawNutritionRow row) {
        try {
            queue.put(row);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failure.compareAndSet(null, e);
        }
    }

    private void offerEnd() {
        try {
            queue.put(END);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failure.compareAndSet(null, e);
        }
    }

    private class SheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final Map<Integer, String> headers = new LinkedHashMap<>();
        private Map<Integer, String> currentCells = new LinkedHashMap<>();
        private boolean headerReady;

        @Override
        public void startRow(int rowNum) {
            currentCells = new LinkedHashMap<>();
        }

        @Override
        public void endRow(int rowNum) {
            if (currentCells.isEmpty()) {
                return;
            }
            if (!headerReady) {
                headers.putAll(currentCells);
                headerReady = true;
                return;
            }
            Map<String, String> cellsByHeader = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> header : headers.entrySet()) {
                cellsByHeader.put(header.getValue(), currentCells.get(header.getKey()));
            }
            offerRow(profile.mapRow(sourceName, sourcePath.toString(), rowNum + 1, cellsByHeader));
        }

        @Override
        public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
            if (cellReference == null) {
                return;
            }
            int columnIndex = new CellReference(cellReference).getCol();
            currentCells.put(columnIndex, formattedValue);
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
        }
    }
}
