package fastcontentparse;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * FastPDFium -- Pure Java 25 Foreign Function and Memory (FFM) wrapper for Google PDFium.
 * Extracts text and structured layout directly from PDF documents with ZERO AWT dependencies.
 */
public final class FastPDFium implements Closeable {

    private static final MethodHandle FPDF_InitLibrary;
    private static final MethodHandle FPDF_DestroyLibrary;
    private static final MethodHandle FPDF_LoadCustomDocument;
    private static final MethodHandle FPDF_CloseDocument;
    private static final MethodHandle FPDF_GetPageCount;
    private static final MethodHandle FPDF_LoadPage;
    private static final MethodHandle FPDF_ClosePage;
    private static final MethodHandle FPDFText_LoadPage;
    private static final MethodHandle FPDFText_ClosePage;
    private static final MethodHandle FPDFText_CountChars;
    private static final MethodHandle FPDFText_GetText;

    private static volatile boolean initialized = false;

    static {
        loadNativeLibrary();
        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();

        FPDF_InitLibrary = linker.downcallHandle(
                lookup.find("FPDF_InitLibrary").orElseThrow(),
                FunctionDescriptor.ofVoid()
        );
        FPDF_DestroyLibrary = linker.downcallHandle(
                lookup.find("FPDF_DestroyLibrary").orElseThrow(),
                FunctionDescriptor.ofVoid()
        );
        FPDF_CloseDocument = linker.downcallHandle(
                lookup.find("FPDF_CloseDocument").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        FPDF_GetPageCount = linker.downcallHandle(
                lookup.find("FPDF_GetPageCount").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );
        FPDF_LoadPage = linker.downcallHandle(
                lookup.find("FPDF_LoadPage").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );
        FPDF_ClosePage = linker.downcallHandle(
                lookup.find("FPDF_ClosePage").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        FPDFText_LoadPage = linker.downcallHandle(
                lookup.find("FPDFText_LoadPage").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        FPDFText_ClosePage = linker.downcallHandle(
                lookup.find("FPDFText_ClosePage").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        FPDFText_CountChars = linker.downcallHandle(
                lookup.find("FPDFText_CountChars").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );
        FPDFText_GetText = linker.downcallHandle(
                lookup.find("FPDFText_GetText").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );

        // FPDF_LoadMemDocument
        FPDF_LoadCustomDocument = linker.downcallHandle(
                lookup.find("FPDF_LoadMemDocument").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );

        init();
    }

    private static synchronized void init() {
        if (!initialized) {
            try {
                FPDF_InitLibrary.invokeExact();
                initialized = true;
            } catch (Throwable t) {
                throw new RuntimeException("Failed to initialize PDFium library", t);
            }
        }
    }

    private static void loadNativeLibrary() {
        try {
            System.loadLibrary("pdfium");
            return;
        } catch (UnsatisfiedLinkError ignored) {}

        String[] candidatePaths = {
                "dist/Speak+/dll/pdfium.dll",
                "dll/pdfium.dll",
                "pdfium.dll"
        };
        for (String p : candidatePaths) {
            Path path = Path.of(p).toAbsolutePath();
            if (Files.exists(path)) {
                System.load(path.toString());
                return;
            }
        }
        try {
            fastcore.FastCore.loadLibrary("pdfium");
        } catch (Throwable t) {
            throw new UnsatisfiedLinkError("Could not locate pdfium.dll in system path or app dll directory: " + t.getMessage());
        }
    }

    private final MemorySegment documentHandle;
    private final Arena arena;

    private FastPDFium(MemorySegment documentHandle, Arena arena) {
        this.documentHandle = documentHandle;
        this.arena = arena;
    }

    public static FastPDFium open(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return open(bytes);
    }

    public static FastPDFium open(byte[] pdfBytes) throws IOException {
        Arena arena = Arena.ofShared();
        MemorySegment mem = arena.allocate(pdfBytes.length);
        mem.copyFrom(MemorySegment.ofArray(pdfBytes));

        try {
            MemorySegment doc = (MemorySegment) FPDF_LoadCustomDocument.invokeExact(mem, pdfBytes.length, MemorySegment.NULL);
            if (doc == null || doc.address() == 0) {
                arena.close();
                throw new IOException("PDFium failed to load document from memory");
            }
            return new FastPDFium(doc, arena);
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            arena.close();
            throw new IOException("Failed to invoke FPDF_LoadMemDocument", t);
        }
    }

    public int getPageCount() {
        try {
            return (int) FPDF_GetPageCount.invokeExact(documentHandle);
        } catch (Throwable t) {
            return 0;
        }
    }

    public String extractPageText(int pageIndex) {
        try {
            MemorySegment page = (MemorySegment) FPDF_LoadPage.invokeExact(documentHandle, pageIndex);
            if (page == null || page.address() == 0) return "";
            try {
                MemorySegment textPage = (MemorySegment) FPDFText_LoadPage.invokeExact(page);
                if (textPage == null || textPage.address() == 0) return "";
                try {
                    int charCount = (int) FPDFText_CountChars.invokeExact(textPage);
                    if (charCount <= 0) return "";

                    try (Arena local = Arena.ofConfined()) {
                        // buffer in 2-byte UTF-16LE values + null terminator
                        int bufSize = (charCount + 1) * 2;
                        MemorySegment buf = local.allocate(bufSize);
                        int extracted = (int) FPDFText_GetText.invokeExact(textPage, 0, charCount, buf);
                        if (extracted <= 0) return "";

                        byte[] rawBytes = buf.asSlice(0, (extracted - 1) * 2).toArray(ValueLayout.JAVA_BYTE);
                        return new String(rawBytes, StandardCharsets.UTF_16LE);
                    }
                } finally {
                    FPDFText_ClosePage.invokeExact(textPage);
                }
            } finally {
                FPDF_ClosePage.invokeExact(page);
            }
        } catch (Throwable t) {
            return "";
        }
    }

    public String extractAllText() {
        int pages = getPageCount();
        StringBuilder sb = new StringBuilder(pages * 1024);
        for (int i = 0; i < pages; i++) {
            String pageText = extractPageText(i);
            if (!pageText.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append("\n\n");
                }
                sb.append(pageText.stripTrailing());
            }
        }
        return sb.toString();
    }

    @Override
    public void close() {
        try {
            if (documentHandle != null && documentHandle.address() != 0) {
                FPDF_CloseDocument.invokeExact(documentHandle);
            }
        } catch (Throwable ignored) {
        } finally {
            arena.close();
        }
    }
}
