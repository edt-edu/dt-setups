package dtengine.logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DemoLogger {

    public static void main(String[] args) {
        DemoLogger dl = new DemoLogger();
        try {
            dl.writeLineByLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Map<String, String> AUTHOR_BOOK_MAP = new HashMap<>() {
        {
            put("Dan Simmons", "Hyperion");
            put("Douglas Adams", "The Hitchhiker's Guide to the Galaxy");
        }
    };
    String[] HEADERS = { "author", "title"};

    public void writeLineByLine() throws Exception {
        FileWriter fw = new FileWriter("file.txt");
        StringWriter sw = new StringWriter();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            AUTHOR_BOOK_MAP.forEach((author, title) -> {
                try {
                    printer.printRecord(author, title);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println(sw.toString()
                .trim());
        //sw.write("some content...");
        fw.write(sw.toString());
        fw.close();
        //assertEquals(EXPECTED_FILESTREAM, sw.toString().trim());
    }

    public void givenDataArray_whenConvertToCSV_thenOutputCreated() throws IOException {

    }
}
