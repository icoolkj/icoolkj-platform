package com.icoolkj.platform.framework.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.MaskUtils;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Excel export downloads via EasyExcel.
 *
 * <p>Prefer {@link #writeLocalizedPaged} to avoid holding the full mapped dataset in memory.
 * Sync HTTP export buffers to a temp file before committing response headers, so mid-page failures
 * do not leave a partial xlsx body. {@link #loadRows} remains for callers that need an in-memory
 * list (still capped at {@link #MAX_ROWS}).
 *
 * <p>Localized export filename convention:
 * <ul>
 *   <li>Display (UTF-8): {@code {exportFileName}_{yyyyMMdd_HHmmss}.xlsx}</li>
 *   <li>ASCII fallback: {@code {businessName}_{yyyyMMdd_HHmmss}.xlsx}</li>
 * </ul>
 */
public final class ExcelExportSupport {

    /** Absolute safety ceiling; over → {@code error.excel.export_too_large} (no truncate). */
    public static final int MAX_ROWS = 50_000;
    private static final long PAGE_SIZE = 500L;
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Logger log = LoggerFactory.getLogger(ExcelExportSupport.class);

    private ExcelExportSupport() {
    }

    /**
     * Loads all matching rows by paging. Restores the query's original {@code pageNo}/{@code pageSize}.
     * If {@code total} (or accumulated size) exceeds {@link #MAX_ROWS}, throws — never silently truncates.
     */
    public static <Q extends PageQuery, T> List<T> loadRows(Q query, Function<Q, PageResult<T>> pageLoader) {
        Long originalPageNo = query.getPageNo();
        Long originalPageSize = query.getPageSize();
        try {
            List<T> all = new ArrayList<>();
            long pageNo = 1;
            Long reportedTotal = null;
            while (true) {
                query.setPageNo(pageNo);
                query.setPageSize(PAGE_SIZE);
                PageResult<T> page = pageLoader.apply(query);
                if (reportedTotal == null) {
                    reportedTotal = page.getTotal();
                    if (reportedTotal != null && reportedTotal > MAX_ROWS) {
                        throw new BusinessException("error.excel.export_too_large");
                    }
                }
                List<T> batch = page.getList();
                if (CollectionUtils.isEmpty(batch)) {
                    break;
                }
                all.addAll(batch);
                if (all.size() > MAX_ROWS) {
                    throw new BusinessException("error.excel.export_too_large");
                }
                if (reportedTotal != null && all.size() >= reportedTotal) {
                    break;
                }
                if (batch.size() < PAGE_SIZE) {
                    break;
                }
                pageNo++;
            }
            return all;
        } finally {
            query.setPageNo(originalPageNo);
            query.setPageSize(originalPageSize);
        }
    }

    /**
     * Pages through {@code pageLoader}, maps each VO, and writes sheet pages — avoids a third full copy
     * of all rows when the dataset approaches {@link #MAX_ROWS}.
     */
    public static <Q extends PageQuery, T, R> void writeLocalizedPaged(
            HttpServletResponse response,
            String businessName,
            Class<R> rowType,
            Q query,
            Function<Q, PageResult<T>> pageLoader,
            Function<T, R> rowMapper,
            I18nMessageResolver i18n) {
        LocalizedMeta meta = resolveLocalizedMeta(businessName, i18n);
        Long originalPageNo = query.getPageNo();
        Long originalPageSize = query.getPageSize();
        Path temp = null;
        try {
            // Preflight first page + total BEFORE download headers (avoid HTTP 200 + corrupt xlsx).
            query.setPageNo(1L);
            query.setPageSize(PAGE_SIZE);
            PageResult<T> first = pageLoader.apply(query);
            Long reportedTotal = first.getTotal();
            if (reportedTotal != null && reportedTotal > MAX_ROWS) {
                throw new BusinessException("error.excel.export_too_large");
            }

            // Buffer to temp so mid-pagination failures never commit a partial HTTP body.
            temp = Files.createTempFile("excel-export-sync-", ".xlsx");
            try (OutputStream out = Files.newOutputStream(temp)) {
                writeLocalizedPagedBody(
                        out, meta, rowType, query, pageLoader, rowMapper, i18n, first, reportedTotal);
            }
            applyDownloadHeaders(response, meta.displayName(), meta.asciiName());
            try (InputStream in = Files.newInputStream(temp); OutputStream out = response.getOutputStream()) {
                in.transferTo(out);
                out.flush();
            }
        } catch (BusinessException ex) {
            if (response.isCommitted()) {
                log.error("Excel export business failure after response committed businessName={}", businessName, ex);
                return;
            }
            throw ex;
        } catch (Exception ex) {
            log.error("Excel export failed businessName={}", businessName, ex);
            if (response.isCommitted()) {
                return;
            }
            throw new BusinessException(500, "error.excel.export_failed", ex);
        } finally {
            query.setPageNo(originalPageNo);
            query.setPageSize(originalPageSize);
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (Exception ignored) {
                    // best-effort
                }
            }
        }
    }

    /**
     * Async / file staging path: same paged write as {@link #writeLocalizedPaged} into an
     * {@link OutputStream} (no HTTP headers). Returns resolved filename metadata.
     */
    public static <Q extends PageQuery, T, R> ExcelExportFileMeta writeLocalizedPagedTo(
            OutputStream out,
            String businessName,
            Class<R> rowType,
            Q query,
            Function<Q, PageResult<T>> pageLoader,
            Function<T, R> rowMapper,
            I18nMessageResolver i18n) {
        LocalizedMeta meta = resolveLocalizedMeta(businessName, i18n);
        Long originalPageNo = query.getPageNo();
        Long originalPageSize = query.getPageSize();
        try {
            query.setPageNo(1L);
            query.setPageSize(PAGE_SIZE);
            PageResult<T> first = pageLoader.apply(query);
            Long reportedTotal = first.getTotal();
            if (reportedTotal != null && reportedTotal > MAX_ROWS) {
                throw new BusinessException("error.excel.export_too_large");
            }
            writeLocalizedPagedBody(out, meta, rowType, query, pageLoader, rowMapper, i18n, first, reportedTotal);
            return new ExcelExportFileMeta(meta.displayName(), meta.asciiName(), meta.sheetName());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel export failed businessName={}", businessName, ex);
            throw new BusinessException(500, "error.excel.export_failed", ex);
        } finally {
            query.setPageNo(originalPageNo);
            query.setPageSize(originalPageSize);
        }
    }

    private static <Q extends PageQuery, T, R> void writeLocalizedPagedBody(
            OutputStream out,
            LocalizedMeta meta,
            Class<R> rowType,
            Q query,
            Function<Q, PageResult<T>> pageLoader,
            Function<T, R> rowMapper,
            I18nMessageResolver i18n,
            PageResult<T> first,
            Long reportedTotal) throws Exception {
        List<Field> columns = listExportFields(rowType);
        List<List<String>> head = buildI18nHead(columns, i18n);
        try (ExcelWriter writer = EasyExcel.write(out)
                .head(head)
                .registerWriteHandler(new AnnotatedColumnWidthHandler(columns))
                .build()) {
            WriteSheet sheet = EasyExcel.writerSheet(meta.sheetName()).build();
            long written = 0;
            List<T> batch = first.getList();
            long pageNo = 1;
            while (true) {
                if (!CollectionUtils.isEmpty(batch)) {
                    written += batch.size();
                    if (written > MAX_ROWS) {
                        throw new BusinessException("error.excel.export_too_large");
                    }
                    List<R> mapped = new ArrayList<>(batch.size());
                    for (T item : batch) {
                        mapped.add(rowMapper.apply(item));
                    }
                    writer.write(toDataRows(mapped, columns), sheet);
                }
                if (CollectionUtils.isEmpty(batch)) {
                    break;
                }
                if (reportedTotal != null && written >= reportedTotal) {
                    break;
                }
                if (batch.size() < PAGE_SIZE) {
                    break;
                }
                pageNo++;
                query.setPageNo(pageNo);
                query.setPageSize(PAGE_SIZE);
                PageResult<T> page = pageLoader.apply(query);
                batch = page.getList();
            }
        }
    }

    /** Filename / sheet metadata produced by localized export writers. */
    public record ExcelExportFileMeta(String displayName, String asciiName, String sheetName) {
    }

    /**
     * Writes an i18n Excel from an already-loaded row list. Prefer {@link #writeLocalizedPaged}.
     */
    public static <T> void writeLocalized(HttpServletResponse response,
                                          String businessName,
                                          Class<T> rowType,
                                          List<T> rows,
                                          I18nMessageResolver i18n) {
        assertWithinMax(rows);
        LocalizedMeta meta = resolveLocalizedMeta(businessName, i18n);
        List<Field> columns = listExportFields(rowType);
        List<List<String>> head = buildI18nHead(columns, i18n);

        try {
            applyDownloadHeaders(response, meta.displayName(), meta.asciiName());
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(new AnnotatedColumnWidthHandler(columns))
                    .sheet(meta.sheetName())
                    .doWrite(toDataRows(rows, columns));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel export failed businessName={}", businessName, ex);
            if (response.isCommitted()) {
                return;
            }
            throw new BusinessException(500, "error.excel.export_failed", ex);
        }
    }

    /**
     * Non-i18n export. When the row class uses {@link ExcelI18nProperty}, headers fall back to field
     * names — use {@link #writeLocalized} / {@link #writeLocalizedPaged} for localized headers.
     */
    public static <T> void write(HttpServletResponse response,
                                 String fileName,
                                 Class<T> head,
                                 List<T> rows) {
        write(response, fileName, head, rows, "Sheet1");
    }

    /** @see #write(HttpServletResponse, String, Class, List) */
    public static <T> void write(HttpServletResponse response,
                                 String fileName,
                                 Class<T> head,
                                 List<T> rows,
                                 String sheetName) {
        assertWithinMax(rows);
        try {
            String safeName = fileName == null || fileName.isBlank() ? "export.xlsx" : fileName.trim();
            if (!safeName.toLowerCase().endsWith(".xlsx")) {
                safeName = safeName + ".xlsx";
            }
            applyDownloadHeaders(response, safeName, safeName);

            List<Field> columns = listExportFieldsOrEmpty(head);
            if (!columns.isEmpty()) {
                List<List<String>> excelHead = new ArrayList<>(columns.size());
                for (Field field : columns) {
                    // Never emit raw i18n keys as headers on this path
                    excelHead.add(List.of(field.getName()));
                }
                EasyExcel.write(response.getOutputStream())
                        .head(excelHead)
                        .registerWriteHandler(new AnnotatedColumnWidthHandler(columns))
                        .sheet(sheetName == null || sheetName.isBlank() ? "Sheet1" : sheetName)
                        .doWrite(toDataRows(rows, columns));
            } else {
                EasyExcel.write(response.getOutputStream(), head)
                        .sheet(sheetName == null || sheetName.isBlank() ? "Sheet1" : sheetName)
                        .doWrite(CollectionUtils.isEmpty(rows) ? List.of() : rows);
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Excel export failed fileName={}", fileName, ex);
            if (response.isCommitted()) {
                return;
            }
            throw new BusinessException(500, "error.excel.export_failed", ex);
        }
    }

    public static String resolvePageText(I18nMessageResolver i18n, String businessName, String pageKey) {
        if (i18n == null || !StringUtils.hasText(businessName) || !StringUtils.hasText(pageKey)) {
            return "";
        }
        String code = "gen." + businessName + ".page." + pageKey;
        String value = i18n.resolve(code);
        return code.equals(value) ? "" : value;
    }

    /** Resolves {@code gen.{business}.audit.{category}.{code}} (modules / businessTypes). */
    public static String resolveAudit(I18nMessageResolver i18n, String businessName, String category, String code) {
        if (i18n == null || !StringUtils.hasText(code)) {
            return code;
        }
        String key = "gen." + businessName + ".audit." + category + "." + code;
        String value = i18n.resolve(key);
        return key.equals(value) ? code : value;
    }

    private static LocalizedMeta resolveLocalizedMeta(String businessName, I18nMessageResolver i18n) {
        String biz = StringUtils.hasText(businessName) ? businessName.trim() : "export";
        String stamp = LocalDateTime.now().format(FILE_STAMP);
        String displayBase = ExcelFileNames.sanitizeFileBase(resolvePageText(i18n, biz, "exportFileName"), "");
        if (!StringUtils.hasText(displayBase)) {
            displayBase = ExcelFileNames.sanitizeFileBase(resolvePageText(i18n, biz, "title"), "");
        }
        if (!StringUtils.hasText(displayBase)) {
            displayBase = biz;
        }
        String sheetName = resolvePageText(i18n, biz, "exportSheetName");
        if (!StringUtils.hasText(sheetName)) {
            sheetName = resolvePageText(i18n, biz, "title");
        }
        if (!StringUtils.hasText(sheetName)) {
            sheetName = biz;
        }
        sheetName = ExcelFileNames.sanitizeSheetName(sheetName);
        String displayName = displayBase + "_" + stamp + ".xlsx";
        String asciiName = ExcelFileNames.sanitizeAsciiBase(biz, "export") + "_" + stamp + ".xlsx";
        return new LocalizedMeta(displayName, asciiName, sheetName);
    }

    private static List<List<String>> buildI18nHead(List<Field> columns, I18nMessageResolver i18n) {
        List<List<String>> head = new ArrayList<>(columns.size());
        for (Field field : columns) {
            ExcelI18nProperty prop = field.getAnnotation(ExcelI18nProperty.class);
            String label = i18n.resolve(prop.value());
            if (!StringUtils.hasText(label) || label.equals(prop.value())) {
                label = field.getName();
            }
            head.add(List.of(label));
        }
        return head;
    }

    private static void applyDownloadHeaders(HttpServletResponse response, String displayName, String asciiName)
            throws Exception {
        ExcelFileNames.applyAttachmentHeaders(response, displayName, asciiName);
    }

    private static void assertWithinMax(List<?> rows) {
        if (rows != null && rows.size() > MAX_ROWS) {
            throw new BusinessException("error.excel.export_too_large");
        }
    }

    private static List<Field> listExportFields(Class<?> rowType) {
        List<Field> fields = listExportFieldsOrEmpty(rowType);
        if (fields.isEmpty()) {
            throw new BusinessException("error.excel.export_failed");
        }
        return fields;
    }

    private static List<Field> listExportFieldsOrEmpty(Class<?> rowType) {
        List<Field> fields = new ArrayList<>();
        for (Field field : rowType.getDeclaredFields()) {
            if (field.getAnnotation(ExcelI18nProperty.class) != null) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    private static <T> List<List<Object>> toDataRows(List<T> rows, List<Field> columns) throws IllegalAccessException {
        if (CollectionUtils.isEmpty(rows)) {
            return List.of();
        }
        List<List<Object>> data = new ArrayList<>(rows.size());
        for (T row : rows) {
            List<Object> line = new ArrayList<>(columns.size());
            for (Field field : columns) {
                line.add(maskCell(field, field.get(row)));
            }
            data.add(line);
        }
        return data;
    }

    static Object maskCell(Field field, Object value) {
        if (value == null || !(value instanceof String text)) {
            return value;
        }
        ExcelMask mask = field.getAnnotation(ExcelMask.class);
        if (mask == null) {
            return value;
        }
        return switch (mask.value()) {
            case PHONE -> MaskUtils.phone(text);
            case EMAIL -> MaskUtils.email(text);
            case NAME -> MaskUtils.name(text);
            case IP -> MaskUtils.ip(text);
            case ID_CARD -> MaskUtils.idCard(text);
            case BANK_CARD -> MaskUtils.bankCard(text);
        };
    }

    private record LocalizedMeta(String displayName, String asciiName, String sheetName) {
    }
}
