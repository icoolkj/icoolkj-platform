package com.icoolkj.platform.framework.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Template download + EasyExcel read for Gen import (separate channel from export).
 *
 * <p>Column binding is by {@link ExcelProperty#index()} (stable across locales). Template
 * headers still show localized {@link ExcelI18nProperty} labels for humans.
 *
 * <p>Default Gen / golden-sample path: {@link #readRows} + all-or-nothing service import
 * ({@link ExcelImportResult}). {@link #readInBatches} remains for advanced streaming writers
 * that manage their own transaction boundaries.
 */
public final class ExcelImportSupport {

    /** Default rows per batch for {@link #readInBatches}. */
    public static final int DEFAULT_BATCH_SIZE = 500;
    /** Absolute safety ceiling across all batches. */
    public static final int MAX_ROWS = 50_000;
    /** Soft upload ceiling for Excel imports (below Spring multipart max). */
    public static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Logger log = LoggerFactory.getLogger(ExcelImportSupport.class);

    private ExcelImportSupport() {
    }

    /** Rejects empty / non-.xlsx / non-ZIP(OOXML) uploads before EasyExcel parses. */
    public static void assertXlsxUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("error.excel.import_empty");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new BusinessException("error.excel.import_file_too_large");
        }
        String name = file.getOriginalFilename();
        if (!StringUtils.hasText(name) || !name.toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx")) {
            throw new BusinessException("error.excel.import_xlsx_only");
        }
        try (InputStream in = file.getInputStream()) {
            assertZipLocalHeader(in);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel upload probe failed", ex);
            throw new BusinessException(500, "error.excel.import_failed", ex);
        }
    }

    /**
     * Re-validate a staged download resource (size + OOXML magic) before {@link #readRows(InputStream, Class)}.
     */
    public static void assertXlsxResource(org.springframework.core.io.Resource resource) {
        if (resource == null || !resource.exists()) {
            throw new BusinessException("error.excel.import_empty");
        }
        try {
            long len = resource.contentLength();
            if (len > MAX_UPLOAD_BYTES) {
                throw new BusinessException("error.excel.import_file_too_large");
            }
            try (InputStream in = resource.getInputStream()) {
                assertZipLocalHeader(in);
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel staged resource probe failed", ex);
            throw new BusinessException(500, "error.excel.import_failed", ex);
        }
    }

    private static void assertZipLocalHeader(InputStream in) throws Exception {
        byte[] magic = in.readNBytes(4);
        if (!isZipLocalHeader(magic)) {
            throw new BusinessException("error.excel.import_xlsx_only");
        }
    }

    private static boolean isZipLocalHeader(byte[] magic) {
        return magic != null
                && magic.length >= 4
                && magic[0] == 0x50
                && magic[1] == 0x4B
                && (magic[2] == 0x03 || magic[2] == 0x05 || magic[2] == 0x07)
                && (magic[3] == 0x04 || magic[3] == 0x06 || magic[3] == 0x08);
    }

    /**
     * Localized empty template with optional dict/dept dropdowns from {@link ExcelImportLookup}.
     */
    public static <T> void writeImportTemplate(HttpServletResponse response,
                                               String businessName,
                                               Class<T> rowType,
                                               I18nMessageResolver i18n) {
        writeImportTemplate(response, businessName, rowType, i18n, null);
    }

    public static <T> void writeImportTemplate(HttpServletResponse response,
                                               String businessName,
                                               Class<T> rowType,
                                               I18nMessageResolver i18n,
                                               ExcelImportLookup lookup) {
        String biz = StringUtils.hasText(businessName) ? businessName.trim() : "import";
        String stamp = LocalDateTime.now().format(FILE_STAMP);
        String displayBase = firstNonBlank(
                ExcelExportSupport.resolvePageText(i18n, biz, "importTemplateFileName"),
                ExcelExportSupport.resolvePageText(i18n, biz, "exportFileName"),
                ExcelExportSupport.resolvePageText(i18n, biz, "title"),
                biz);
        displayBase = ExcelFileNames.sanitizeFileBase(displayBase, "import") + "_template";
        String sheetName = firstNonBlank(
                ExcelExportSupport.resolvePageText(i18n, biz, "importSheetName"),
                ExcelExportSupport.resolvePageText(i18n, biz, "exportSheetName"),
                ExcelExportSupport.resolvePageText(i18n, biz, "title"),
                biz);
        sheetName = ExcelFileNames.sanitizeSheetName(sheetName);

        List<Field> columns = listImportFields(rowType);
        List<List<String>> head = new ArrayList<>(columns.size());
        for (Field field : columns) {
            ExcelI18nProperty prop = field.getAnnotation(ExcelI18nProperty.class);
            String label = prop != null ? i18n.resolve(prop.value()) : field.getName();
            if (!StringUtils.hasText(label) || (prop != null && label.equals(prop.value()))) {
                label = field.getName();
            }
            head.add(List.of(label));
        }

        Map<Integer, List<String>> dropdowns = buildDropdownOptions(columns, lookup);

        String displayName = displayBase + "_" + stamp + ".xlsx";
        String asciiName = ExcelFileNames.sanitizeAsciiBase(biz, "import") + "_template_" + stamp + ".xlsx";
        try {
            ExcelFileNames.applyAttachmentHeaders(response, displayName, asciiName);
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(new AnnotatedColumnWidthHandler(columns))
                    .registerWriteHandler(new ExcelImportDropdownHandler(dropdowns))
                    .sheet(sheetName)
                    .doWrite(List.of());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel import template failed businessName={}", businessName, ex);
            if (response.isCommitted()) {
                return;
            }
            throw new BusinessException(500, "error.excel.import_failed", ex);
        }
    }

    static Map<Integer, List<String>> buildDropdownOptions(List<Field> columns, ExcelImportLookup lookup) {
        Map<Integer, List<String>> dropdowns = new java.util.LinkedHashMap<>();
        if (lookup == null || columns == null) {
            return dropdowns;
        }
        for (int i = 0; i < columns.size(); i++) {
            Field field = columns.get(i);
            ExcelImportDict dict = field.getAnnotation(ExcelImportDict.class);
            if (dict != null && StringUtils.hasText(dict.value())) {
                List<String> labels = lookup.dictLabels(dict.value().trim());
                if (labels != null && !labels.isEmpty()) {
                    dropdowns.put(i, List.copyOf(labels));
                }
                continue;
            }
            ExcelImportRef ref = field.getAnnotation(ExcelImportRef.class);
            if (ref == null) {
                continue;
            }
            List<String> names = switch (ref.value()) {
                case DEPT_NAME -> lookup.deptNames();
                case POST_NAME -> lookup.postNames();
                case ROLE_NAME -> lookup.roleNames();
            };
            if (names != null && !names.isEmpty()) {
                dropdowns.put(i, List.copyOf(names));
            }
        }
        return dropdowns;
    }

    /**
     * Reads entire file into memory (capped at {@link #MAX_ROWS}). Prefer this for all-or-nothing import.
     */
    public static <T> List<T> readRows(MultipartFile file, Class<T> rowType) {
        assertXlsxUpload(file);
        try (InputStream in = file.getInputStream()) {
            return readRows(in, rowType);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel import read failed", ex);
            throw new BusinessException(500, "error.excel.import_failed", ex);
        }
    }

    public static <T> List<T> readRows(InputStream in, Class<T> rowType) {
        List<T> all = new ArrayList<>();
        readInBatches(in, rowType, DEFAULT_BATCH_SIZE, all::addAll);
        return all;
    }

    /**
     * Streams the sheet in batches. Prefer {@link #readRows} + single-TX import for Gen defaults.
     * Over {@link #MAX_ROWS} fails without truncating — callers own transaction boundaries.
     */
    public static <T> int readInBatches(MultipartFile file,
                                        Class<T> rowType,
                                        int batchSize,
                                        Consumer<List<T>> batchHandler) {
        assertXlsxUpload(file);
        try (InputStream in = file.getInputStream()) {
            return readInBatches(in, rowType, batchSize, batchHandler);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel import read failed", ex);
            throw new BusinessException(500, "error.excel.import_failed", ex);
        }
    }

    public static <T> int readInBatches(InputStream in,
                                        Class<T> rowType,
                                        int batchSize,
                                        Consumer<List<T>> batchHandler) {
        if (batchHandler == null) {
            throw new BusinessException("error.excel.import_failed");
        }
        int size = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        listImportFields(rowType);

        BatchCountingListener<T> listener = new BatchCountingListener<>(size, batchHandler);
        try {
            EasyExcel.read(in, rowType, listener)
                    .sheet()
                    .headRowNumber(1)
                    .doRead();
            return listener.totalRows();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel import parse failed", ex);
            throw new BusinessException(500, "error.excel.import_failed", ex);
        }
    }

    static List<Field> listImportFields(Class<?> rowType) {
        List<Field> fields = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (Field field : rowType.getDeclaredFields()) {
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            ExcelI18nProperty i18nProperty = field.getAnnotation(ExcelI18nProperty.class);
            if (excelProperty == null && i18nProperty == null) {
                continue;
            }
            if (excelProperty == null || excelProperty.index() < 0) {
                throw new BusinessException("error.excel.import_column_index_required");
            }
            int index = excelProperty.index();
            if (!seen.add(index)) {
                throw new BusinessException("error.excel.import_column_index_duplicate", index);
            }
            field.setAccessible(true);
            fields.add(field);
        }
        fields.sort((a, b) -> Integer.compare(
                a.getAnnotation(ExcelProperty.class).index(),
                b.getAnnotation(ExcelProperty.class).index()));
        if (fields.isEmpty()) {
            throw new BusinessException("error.excel.import_failed");
        }
        for (int i = 0; i < fields.size(); i++) {
            int index = fields.get(i).getAnnotation(ExcelProperty.class).index();
            if (index != i) {
                throw new BusinessException("error.excel.import_column_index_gap", i, index);
            }
        }
        return fields;
    }

    private static final class BatchCountingListener<T> implements ReadListener<T> {
        private final int batchSize;
        private final Consumer<List<T>> batchHandler;
        private final List<T> buffer;
        private int totalRows;

        private BatchCountingListener(int batchSize, Consumer<List<T>> batchHandler) {
            this.batchSize = batchSize;
            this.batchHandler = batchHandler;
            this.buffer = new ArrayList<>(batchSize);
        }

        @Override
        public void invoke(T data, AnalysisContext context) {
            totalRows++;
            if (totalRows > MAX_ROWS) {
                throw new BusinessException("error.excel.import_too_large");
            }
            buffer.add(data);
            if (buffer.size() >= batchSize) {
                flush();
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            flush();
        }

        private void flush() {
            if (buffer.isEmpty()) {
                return;
            }
            batchHandler.accept(List.copyOf(buffer));
            buffer.clear();
        }

        private int totalRows() {
            return totalRows;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return "";
    }
}
