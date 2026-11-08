package com.icoolkj.platform.infra.backup;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.backup.DatabaseBackupSettingsProvider;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.infra.dto.DatabaseBackupPageQuery;
import com.icoolkj.platform.infra.vo.DatabaseBackupConfigVO;
import com.icoolkj.platform.infra.vo.DatabaseBackupFileVO;
import com.icoolkj.platform.infra.vo.DatabaseBackupRunResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

/** MySQL logical backup via mysqldump (Windows / Linux). Used by worker scheduler and api ops endpoints. */
@Service
public class DatabaseBackupService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupService.class);
    private static final Pattern JDBC_MYSQL =
            Pattern.compile("jdbc:mysql://([^/:]+)(?::(\\d+))?/([^?;]+)");
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[\\w.-]+\\.(sql|sql\\.gz)$");

    private final DatabaseBackupSettingsProvider backupSettings;
    private final DatabaseBackupProperties backupProperties;
    private final DataSourceProperties dataSourceProperties;

    public DatabaseBackupService(DatabaseBackupSettingsProvider backupSettings,
                                 DatabaseBackupProperties backupProperties,
                                 DataSourceProperties dataSourceProperties) {
        this.backupSettings = backupSettings;
        this.backupProperties = backupProperties;
        this.dataSourceProperties = dataSourceProperties;
    }

    public DatabaseBackupConfigVO getConfig() {
        DatabaseBackupConfigVO vo = new DatabaseBackupConfigVO();
        vo.setEnabled(backupSettings.isEnabled());
        vo.setCron(backupSettings.getCron());
        vo.setOutputDir(Paths.get(backupSettings.getOutputDir()).toAbsolutePath().normalize().toString());
        vo.setRetentionDays(backupSettings.getRetentionDays());
        vo.setMysqldumpCommand(resolveMysqldumpCommandOrConfigured());
        vo.setCompress(backupSettings.isCompress());
        return vo;
    }

    private String resolveMysqldumpCommandOrConfigured() {
        String resolved = resolveMysqldumpCommand();
        return resolved != null ? resolved : backupProperties.getMysqldumpCommand();
    }

    public PageResult<DatabaseBackupFileVO> page(DatabaseBackupPageQuery query) {
        List<DatabaseBackupFileVO> all = listFiles(query.getFileName());
        long total = all.size();
        int from = (int) query.getOffset();
        if (from >= all.size()) {
            return PageResult.of(List.of(), total, query.getPageNo(), query.getPageSize());
        }
        int to = Math.min(from + query.getPageSize().intValue(), all.size());
        return PageResult.of(all.subList(from, to), total, query.getPageNo(), query.getPageSize());
    }

    public List<DatabaseBackupFileVO> listFiles() {
        return listFiles(null);
    }

    private List<DatabaseBackupFileVO> listFiles(String fileNameLike) {
        Path outputDir = resolveOutputDir();
        if (!Files.isDirectory(outputDir)) {
            return List.of();
        }
        cleanupEmptyBackupFiles(outputDir);
        List<DatabaseBackupFileVO> rows = new ArrayList<>();
        try (Stream<Path> files = Files.list(outputDir)) {
            files.filter(this::isBackupFile)
                    .forEach(path -> rows.add(toFileVo(path)));
        } catch (IOException ex) {
            throw new BusinessException("error.backup.list_failed");
        }
        if (StringUtils.hasText(fileNameLike)) {
            String keyword = fileNameLike.trim().toLowerCase();
            rows.removeIf(row -> !row.getFileName().toLowerCase().contains(keyword));
        }
        rows.sort(Comparator.comparing(DatabaseBackupFileVO::getModifiedAt).reversed());
        return rows;
    }

    public DatabaseBackupRunResultVO runScheduledBackup() {
        if (!backupSettings.isEnabled()) {
            return skipped("Scheduled backup disabled");
        }
        return runBackupInternal(true);
    }

    public DatabaseBackupRunResultVO runManualBackup() {
        return runBackupInternal(true);
    }

    public Resource loadFileResource(String fileName) {
        Path file = resolveBackupFile(fileName);
        if (!Files.isRegularFile(file)) {
            throw new BusinessException("error.entity.not_found", "entity.name.database_backup");
        }
        return new FileSystemResource(file);
    }

    public void deleteFile(String fileName) {
        Path file = resolveBackupFile(fileName);
        if (!Files.isRegularFile(file)) {
            throw new BusinessException("error.entity.not_found", "entity.name.database_backup");
        }
        try {
            Files.delete(file);
        } catch (IOException ex) {
            throw new BusinessException("error.backup.delete_failed");
        }
    }

    private DatabaseBackupRunResultVO runBackupInternal(boolean purgeAfter) {
        JdbcTarget target = parseJdbcUrl(dataSourceProperties.getUrl());
        if (target == null) {
            return failed("Unsupported JDBC URL: " + dataSourceProperties.getUrl());
        }
        if (!StringUtils.hasText(dataSourceProperties.getUsername())) {
            return failed("Datasource username is empty");
        }

        String mysqldumpCommand = resolveMysqldumpCommand();
        if (mysqldumpCommand == null) {
            return failedMysqldumpNotFound();
        }

        Path outputDir = resolveOutputDir();
        try {
            Files.createDirectories(outputDir);
        } catch (IOException ex) {
            return failed("Cannot create output directory: " + outputDir);
        }

        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Path sqlFile = outputDir.resolve(target.database() + "_" + timestamp + ".sql");
        Path defaultsFile = null;
        boolean success = false;
        try {
            defaultsFile = writeClientDefaults(outputDir, target);
            ProcessResult dump = runProcess(buildDumpCommand(mysqldumpCommand, defaultsFile, target.database(), sqlFile), sqlFile);
            if (dump.exitCode() != 0) {
                return failed(formatDumpFailure(dump));
            }
            validateDumpFile(sqlFile);
            Path finalFile = sqlFile;
            if (backupSettings.isCompress()) {
                finalFile = gzipFile(sqlFile);
                Files.deleteIfExists(sqlFile);
            }
            long size = Files.size(finalFile);
            log.info("Database backup completed: {} ({} bytes)", finalFile.getFileName(), size);
            if (purgeAfter) {
                purgeExpiredBackups(outputDir);
            }
            DatabaseBackupRunResultVO result = new DatabaseBackupRunResultVO();
            result.setSuccess(true);
            result.setFileName(finalFile.getFileName().toString());
            result.setSizeBytes(size);
            result.setMessage("OK");
            success = true;
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failed("Backup interrupted");
        } catch (IOException ex) {
            log.error("Database backup failed", ex);
            if (isCommandNotFound(ex)) {
                return failedMysqldumpNotFound();
            }
            return failed(ex.getMessage() != null ? ex.getMessage() : "IO error");
        } finally {
            if (defaultsFile != null) {
                try {
                    Files.deleteIfExists(defaultsFile);
                } catch (IOException ignored) {
                    // best effort
                }
            }
            if (!success) {
                try {
                    Files.deleteIfExists(sqlFile);
                } catch (IOException ignored) {
                    // best effort
                }
            }
        }
    }

    private DatabaseBackupRunResultVO failedMysqldumpNotFound() {
        return failed("mysqldump not found (configure icoolkj.backup.mysqldump-command)");
    }

    private String formatDumpFailure(ProcessResult dump) {
        if (StringUtils.hasText(dump.output())) {
            String output = dump.output().trim();
            if (output.length() > 500) {
                output = output.substring(0, 500) + "...";
            }
            return "mysqldump exit code " + dump.exitCode() + ": " + output;
        }
        return "mysqldump exit code " + dump.exitCode();
    }

    private static boolean isCommandNotFound(IOException ex) {
        String message = ex.getMessage();
        if (!StringUtils.hasText(message)) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("createprocess error=2")
                || lower.contains("cannot run program")
                || lower.contains("no such file or directory");
    }

    /** Resolve mysqldump executable; bare names fall back to PATH (Linux/Windows). */
    private String resolveMysqldumpCommand() {
        String configured = backupProperties.getMysqldumpCommand();
        if (!StringUtils.hasText(configured)) {
            return null;
        }
        configured = configured.trim();
        boolean explicitPath = configured.contains("/") || configured.contains("\\") || configured.endsWith(".exe");
        if (explicitPath) {
            return isExistingExecutable(configured) ? configured : null;
        }
        if (isExistingExecutable(configured)) {
            return configured;
        }
        for (String candidate : defaultMysqldumpCandidates()) {
            if (isExistingExecutable(candidate)) {
                log.info("Resolved mysqldump command: {}", candidate);
                return candidate;
            }
        }
        String fromPath = resolveFromSystemPath(configured);
        if (fromPath != null) {
            log.info("Resolved mysqldump command from PATH: {}", fromPath);
            return fromPath;
        }
        return configured;
    }

    private static String resolveFromSystemPath(String command) {
        if (!command.matches("[\\w.-]+")) {
            return null;
        }
        try {
            ProcessBuilder builder = isWindows()
                    ? new ProcessBuilder("where", command)
                    : new ProcessBuilder("sh", "-c", "command -v " + command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String line;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                line = reader.readLine();
            }
            if (process.waitFor() != 0 || !StringUtils.hasText(line)) {
                return null;
            }
            String resolved = line.trim();
            int space = resolved.indexOf(' ');
            if (space > 0) {
                resolved = resolved.substring(0, space);
            }
            return isExistingExecutable(resolved) ? resolved : null;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private static boolean isExistingExecutable(String path) {
        try {
            Path file = Paths.get(path);
            if (!Files.isRegularFile(file)) {
                return false;
            }
            return Files.isExecutable(file) || isWindows();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static List<String> defaultMysqldumpCandidates() {
        if (!isWindows()) {
            return List.of(
                    "/usr/bin/mysqldump",
                    "/usr/bin/mariadb-dump",
                    "/usr/local/bin/mysqldump",
                    "/usr/local/mysql/bin/mysqldump");
        }
        List<String> candidates = new ArrayList<>();
        String programFiles = System.getenv("ProgramFiles");
        if (StringUtils.hasText(programFiles)) {
            try (Stream<Path> dirs = Files.list(Paths.get(programFiles, "MySQL"))) {
                dirs.filter(Files::isDirectory)
                        .map(dir -> dir.resolve("bin").resolve("mysqldump.exe"))
                        .forEach(path -> candidates.add(path.toString()));
            } catch (IOException ignored) {
                // best effort
            }
        }
        candidates.add("C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqldump.exe");
        candidates.add("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe");
        candidates.add("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe");
        return candidates;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private DatabaseBackupRunResultVO skipped(String message) {
        DatabaseBackupRunResultVO result = new DatabaseBackupRunResultVO();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    private DatabaseBackupRunResultVO failed(String message) {
        log.error("Database backup failed: {}", message);
        DatabaseBackupRunResultVO result = new DatabaseBackupRunResultVO();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    private Path resolveOutputDir() {
        return Paths.get(backupSettings.getOutputDir()).toAbsolutePath().normalize();
    }

    private Path resolveBackupFile(String fileName) {
        if (!StringUtils.hasText(fileName) || !SAFE_FILE_NAME.matcher(fileName.trim()).matches()) {
            throw new BusinessException("error.backup.invalid_file_name");
        }
        Path file = resolveOutputDir().resolve(fileName.trim()).normalize();
        if (!file.startsWith(resolveOutputDir())) {
            throw new BusinessException("error.backup.invalid_file_name");
        }
        return file;
    }

    private boolean isBackupFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        try {
            if (Files.size(path) <= 0) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
        String name = path.getFileName().toString();
        return name.endsWith(".sql") || name.endsWith(".sql.gz");
    }

    private void cleanupEmptyBackupFiles(Path outputDir) {
        try (Stream<Path> files = Files.list(outputDir)) {
            files.filter(Files::isRegularFile).forEach(path -> {
                String name = path.getFileName().toString();
                if (!name.endsWith(".sql") && !name.endsWith(".sql.gz")) {
                    return;
                }
                try {
                    if (Files.size(path) <= 0) {
                        Files.delete(path);
                        log.info("Removed empty database backup file: {}", name);
                    }
                } catch (IOException ex) {
                    log.warn("Failed to cleanup empty backup file {}", name, ex);
                }
            });
        } catch (IOException ex) {
            log.warn("Failed to scan backup directory for cleanup {}", outputDir, ex);
        }
    }

    private DatabaseBackupFileVO toFileVo(Path path) {
        DatabaseBackupFileVO vo = new DatabaseBackupFileVO();
        String name = path.getFileName().toString();
        vo.setId(name);
        vo.setFileName(name);
        vo.setCompressed(name.endsWith(".gz"));
        try {
            long size = Files.size(path);
            vo.setSizeBytes(size);
            vo.setSizeText(formatSize(size));
            Instant modified = Files.getLastModifiedTime(path).toInstant();
            vo.setModifiedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(modified));
        } catch (IOException ex) {
            vo.setSizeBytes(0L);
            vo.setSizeText("-");
            vo.setModifiedAt("");
        }
        return vo;
    }

    private static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
    }

    private List<String> buildDumpCommand(String mysqldumpCommand, Path defaultsFile, String database, Path outputFile) {
        List<String> command = new ArrayList<>();
        command.add(mysqldumpCommand);
        command.add("--defaults-extra-file=" + toMysqldumpPath(defaultsFile));
        // Cross-platform: let mysqldump write the file (avoids Java stream/encoding issues).
        command.add("--result-file=" + toMysqldumpPath(outputFile));
        command.add("--single-transaction");
        command.add("--lock-tables=false");
        command.add("--add-drop-table");
        command.add("--routines");
        command.add("--triggers");
        command.add("--events");
        command.add("--hex-blob");
        command.add("--set-gtid-purged=OFF");
        command.add("--column-statistics=0");
        command.add("--default-character-set=utf8mb4");
        command.add(database);
        return command;
    }

    private static String toMysqldumpPath(Path path) {
        return path.toAbsolutePath().normalize().toString().replace('\\', '/');
    }

    private void validateDumpFile(Path sqlFile) throws IOException {
        if (!Files.isRegularFile(sqlFile)) {
            throw new IOException("mysqldump output file missing");
        }
        long size = Files.size(sqlFile);
        if (size <= 0) {
            throw new IOException("mysqldump produced an empty backup file");
        }
        String tail = readFileTail(sqlFile, 8192);
        if (!tail.contains("Dump completed")) {
            throw new IOException("mysqldump did not complete (missing Dump completed footer)");
        }
    }

    private static String readFileTail(Path file, int maxBytes) throws IOException {
        long size = Files.size(file);
        int readSize = (int) Math.min(size, maxBytes);
        try (InputStream in = Files.newInputStream(file)) {
            if (size > readSize) {
                long skipped = in.skip(size - readSize);
                if (skipped < size - readSize) {
                    throw new IOException("Failed to read dump file tail");
                }
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private ProcessResult runProcess(List<String> command, Path outputFile) throws IOException, InterruptedException {
        Files.deleteIfExists(outputFile);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        builder.redirectError(ProcessBuilder.Redirect.PIPE);
        Process process = builder.start();
        String stderr;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            stderr = readToString(reader);
        }
        int exitCode = process.waitFor();
        if (StringUtils.hasText(stderr)) {
            if (exitCode != 0) {
                log.error("mysqldump stderr: {}", stderr.trim());
            } else {
                log.warn("mysqldump stderr: {}", stderr.trim());
            }
        }
        return new ProcessResult(exitCode, stderr);
    }

    private record ProcessResult(int exitCode, String output) {
    }

    private static String readToString(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private Path writeClientDefaults(Path outputDir, JdbcTarget target) throws IOException {
        Path defaultsFile = Files.createTempFile(outputDir, "mysql-client-", ".cnf");
        String content = """
                [client]
                host=%s
                port=%s
                user=%s
                password=%s
                """.formatted(
                target.host(),
                target.port(),
                escapeDefaultsValue(dataSourceProperties.getUsername()),
                escapeDefaultsValue(dataSourceProperties.getPassword() != null ? dataSourceProperties.getPassword() : ""));
        Files.writeString(defaultsFile, content, StandardCharsets.UTF_8);
        return defaultsFile;
    }

    private static String escapeDefaultsValue(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (value.indexOf('#') >= 0 || value.indexOf('"') >= 0 || value.indexOf(' ') >= 0 || value.indexOf('=') >= 0) {
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return value;
    }

    private Path gzipFile(Path sqlFile) throws IOException {
        Path gzFile = Paths.get(sqlFile.toString() + ".gz");
        try (InputStream in = Files.newInputStream(sqlFile);
             GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(gzFile))) {
            in.transferTo(gzip);
        }
        return gzFile;
    }

    private void purgeExpiredBackups(Path outputDir) {
        if (backupSettings.getRetentionDays() <= 0) {
            return;
        }
        Instant cutoff = Instant.now().minusSeconds(backupSettings.getRetentionDays() * 86400L);
        try (Stream<Path> files = Files.list(outputDir)) {
            files.filter(this::isBackupFile).forEach(path -> {
                try {
                    if (Files.getLastModifiedTime(path).toInstant().isBefore(cutoff)) {
                        Files.delete(path);
                        log.info("Removed expired database backup: {}", path.getFileName());
                    }
                } catch (IOException ex) {
                    log.warn("Failed to purge backup file {}", path, ex);
                }
            });
        } catch (IOException ex) {
            log.warn("Failed to scan backup directory {}", outputDir, ex);
        }
    }

    private JdbcTarget parseJdbcUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        Matcher matcher = JDBC_MYSQL.matcher(url.trim());
        if (!matcher.find()) {
            return null;
        }
        String host = matcher.group(1);
        String port = matcher.group(2) != null ? matcher.group(2) : "3306";
        String database = matcher.group(3);
        return new JdbcTarget(host, port, database);
    }

    private record JdbcTarget(String host, String port, String database) {
    }
}
