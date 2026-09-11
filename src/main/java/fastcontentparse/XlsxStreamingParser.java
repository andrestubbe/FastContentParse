package fastcontentparse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * High-speed, zero-dependency streaming parser for Office OpenXML Excel documents (.xlsx).
 * <p>
 * Parses the shared string table ({@code xl/sharedStrings.xml}) followed by sequential
 * StAX streaming across all worksheet XML parts without DOM overhead.
 */
final class XlsxStreamingParser {

    private static final XMLInputFactory XML_FACTORY;

    static {
        XML_FACTORY = XMLInputFactory.newDefaultFactory();
        XML_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        XML_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    }

    private XlsxStreamingParser() {}

    /**
     * Streams and extracts tab- and newline-delimited textual data across all sheets of an XLSX workbook.
     *
     * @param path local file system path to the .xlsx archive
     * @return raw tab/newline-delimited text representation of all sheets
     * @throws IOException if the file is invalid or reading fails
     */
    static String extractText(Path path) throws IOException {
        List<String> sharedStrings = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder(128 * 1024);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            ZipEntry sstEntry = zip.getEntry("xl/sharedStrings.xml");
            if (sstEntry != null) {
                try (InputStream is = zip.getInputStream(sstEntry)) {
                    XMLStreamReader reader = XML_FACTORY.createXMLStreamReader(is);

                    StringBuilder currentText = null;
                    while (reader.hasNext()) {
                        int event = reader.next();
                        if (event == XMLStreamConstants.START_ELEMENT) {
                            if ("t".equals(reader.getLocalName())) {
                                currentText = new StringBuilder();
                            }
                        } else if (event == XMLStreamConstants.CHARACTERS) {
                            if (currentText != null) {
                                currentText.append(reader.getText());
                            }
                        } else if (event == XMLStreamConstants.END_ELEMENT) {
                            if ("t".equals(reader.getLocalName())) {
                                if (currentText != null) {
                                    sharedStrings.add(currentText.toString());
                                    currentText = null;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new IOException("Failed parsing XLSX shared strings: " + e.getMessage(), e);
                }
            }

            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith("xl/worksheets/sheet") && entryName.endsWith(".xml")) {
                    try (InputStream is = zip.getInputStream(entry)) {
                        XMLStreamReader reader = XML_FACTORY.createXMLStreamReader(is);

                        String cellType = null;
                        StringBuilder cellVal = null;

                        while (reader.hasNext()) {
                            int event = reader.next();
                            if (event == XMLStreamConstants.START_ELEMENT) {
                                String name = reader.getLocalName();
                                if ("c".equals(name)) {
                                    cellType = reader.getAttributeValue(null, "t");
                                } else if ("v".equals(name)) {
                                    cellVal = new StringBuilder();
                                }
                            } else if (event == XMLStreamConstants.CHARACTERS) {
                                if (cellVal != null) {
                                    cellVal.append(reader.getText());
                                }
                            } else if (event == XMLStreamConstants.END_ELEMENT) {
                                String name = reader.getLocalName();
                                if ("v".equals(name)) {
                                    if (cellVal != null) {
                                        String rawVal = cellVal.toString().trim();
                                        if ("s".equals(cellType)) {
                                            try {
                                                int idx = Integer.parseInt(rawVal);
                                                if (idx >= 0 && idx < sharedStrings.size()) {
                                                    textBuilder.append(sharedStrings.get(idx)).append("\t");
                                                }
                                            } catch (NumberFormatException ignored) {}
                                        } else if (!rawVal.isEmpty()) {
                                            textBuilder.append(rawVal).append("\t");
                                        }
                                        cellVal = null;
                                    }
                                } else if ("row".equals(name)) {
                                    textBuilder.append("\n");
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new IOException("Failed parsing XLSX worksheet " + entryName + ": " + e.getMessage(), e);
                    }
                }
            }
        }

        return textBuilder.toString();
    }
}
