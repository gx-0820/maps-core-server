package com.example.coreserver.service.business;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.coreserver.config.PhotoelectricVideoProperties;
import com.example.coreserver.entity.PhotoelectricFileRecord;
import com.example.coreserver.mapper.PhotoelectricFileRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoelectricVideoConvertService {

    private static final String MP4_EXTENSION = ".mp4";
    private static final String H264_EXTENSION = ".h264";
    private static final String TS_EXTENSION = ".ts";
    private static final int MAX_FFMPEG_OUTPUT_LENGTH = 4_000;

    private final PhotoelectricFileRecordMapper photoelectricFileRecordMapper;
    private final PhotoelectricVideoProperties properties;
    private final JdbcTemplate jdbcTemplate;
    private final AtomicBoolean scanning = new AtomicBoolean(false);

    public void convertPendingRecords() {
        if (!properties.isEnabled()) {
            log.info("光电视频转换功能未启用，跳过处理");
            return;
        }
        if (!scanning.compareAndSet(false, true)) {
            log.info("光电视频转换扫描仍在运行中，跳过本次执行");
            return;
        }
        try {
            log.info("开始执行光电视频文件转换任务");
            List<PhotoelectricFileRecord> records = selectPendingRecords();
            log.info("查询到待转换的光电视频记录数量: {}", records.size());
            for (PhotoelectricFileRecord record : records) {
                convertWithDatabaseLock(record);
            }
            log.info("光电视频文件转换任务执行完成");
        } finally {
            scanning.set(false);
        }
    }

    private List<PhotoelectricFileRecord> selectPendingRecords() {
        int batchSize = Math.max(properties.getBatchSize(), 1);
        log.info("查询待转换的光电视频记录，批次大小: {}", batchSize);
        List<PhotoelectricFileRecord> records = photoelectricFileRecordMapper.selectList(new QueryWrapper<PhotoelectricFileRecord>()
                .isNotNull("local_path")
                .ne("local_path", "")
                .and(wrapper -> wrapper.isNull("local_path_mp4").or().eq("local_path_mp4", ""))
                .orderByAsc("id")
                .last("LIMIT " + batchSize));
        log.info("查询到待转换记录数: {}", records != null ? records.size() : 0);
        return records;
    }

    private void convertWithDatabaseLock(PhotoelectricFileRecord record) {
        String lockName = "photoelectric-video-convert-" + record.getId();
        boolean locked = false;
        try {
            Integer lockResult = jdbcTemplate.queryForObject("SELECT GET_LOCK(?, 0)", Integer.class, lockName);
            locked = Objects.equals(lockResult, 1);
            if (!locked) {
                log.info("光电视频记录 {} 已被其他转换器锁定，跳过处理", record.getId());
                return;
            }
            log.info("开始转换光电视频记录 ID: {}, 源文件路径: {}", record.getId(), record.getLocalPath());
            convertOne(record);
            log.info("光电视频记录 ID: {} 转换成功", record.getId());
        } catch (Exception ex) {
            log.warn("光电视频记录 {} MP4转换失败: {}", record.getId(), ex.getMessage(), ex);
        } finally {
            if (locked) {
                try {
                    jdbcTemplate.queryForObject("SELECT RELEASE_LOCK(?)", Integer.class, lockName);
                    log.info("释放光电视频转换锁成功，记录ID: {}", record.getId());
                } catch (Exception ex) {
                    log.warn("释放光电视频转换锁失败，记录ID: {}", record.getId(), ex);
                }
            }
        }
    }

    void convertOne(PhotoelectricFileRecord record) throws Exception {
        String sourcePathText = record.getLocalPath();
        if (!StringUtils.hasText(sourcePathText)) {
            log.warn("光电视频记录 {} 的本地路径为空", record.getId());
            return;
        }

        Path sourcePath = Paths.get(sourcePathText);
        if (!Files.isRegularFile(sourcePath)) {
            log.warn("光电视频记录 {} 的源文件不存在: {}", record.getId(), sourcePath);
            return;
        }

        String extension = getExtension(sourcePath);
        if (MP4_EXTENSION.equals(extension)) {
            log.info("光电视频记录 {} 的文件已是MP4格式，直接更新路径: {}", record.getId(), sourcePath.toAbsolutePath().normalize());
            updateMp4Path(record.getId(), sourcePath.toAbsolutePath().normalize().toString());
            return;
        }
        if (!H264_EXTENSION.equals(extension) && !TS_EXTENSION.equals(extension)) {
            log.warn("光电视频记录 {} 的源文件格式不支持: {}", record.getId(), sourcePath);
            return;
        }

        Path targetPath = replaceExtension(sourcePath, MP4_EXTENSION).toAbsolutePath().normalize();
        Path ffmpegPath = resolveFfmpegPath();
        List<String> command = buildFfmpegCommand(ffmpegPath, sourcePath.toAbsolutePath().normalize(), targetPath, extension);

        log.info("准备执行FFmpeg转换命令，记录ID: {}, 源文件: {}, 目标文件: {}", record.getId(), sourcePath, targetPath);
        try {
            runFfmpeg(command);
            validateTargetFile(targetPath);
            int updated = updateMp4Path(record.getId(), targetPath.toString());
            log.info("光电视频记录 {} 转换成功，更新数据库记录数: {}", record.getId(), updated);
            if (updated > 0 && properties.isDeleteSourceAfterConvert()) {
                deleteSourceFile(sourcePath, targetPath);
            }
        } catch (Exception ex) {
            Files.deleteIfExists(targetPath);
            log.error("光电视频记录 {} FFmpeg转换异常，已删除临时文件: {}", record.getId(), targetPath, ex);
            throw ex;
        }
    }

    private Path resolveFfmpegPath() throws IOException {
        Path ffmpegPath = Paths.get(properties.getFfmpegPath());
        if (!ffmpegPath.isAbsolute()) {
            ffmpegPath = Paths.get(System.getProperty("user.dir")).resolve(ffmpegPath);
        }
        ffmpegPath = ffmpegPath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(ffmpegPath)) {
            log.error("FFmpeg可执行文件不存在: {}", ffmpegPath);
            throw new IOException("FFmpeg可执行文件不存在: " + ffmpegPath);
        }
        log.info("找到FFmpeg可执行文件: {}", ffmpegPath);
        return ffmpegPath;
    }

    List<String> buildFfmpegCommand(Path ffmpegPath, Path sourcePath, Path targetPath, String extension) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath.toString());
        command.add("-nostdin");
        command.add("-y");

        if (H264_EXTENSION.equals(extension)) {
            command.add("-fflags");
            command.add("+genpts");
            command.add("-f");
            command.add("h264");
        }

        command.add("-i");
        command.add(sourcePath.toString());
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("medium");
        command.add("-pix_fmt");
        command.add("yuv420p");

        if (H264_EXTENSION.equals(extension)) {
            command.add("-an");
        } else {
            command.add("-c:a");
            command.add("aac");
            command.add("-b:a");
            command.add("128k");
        }

        command.add("-movflags");
        command.add("+faststart");
        command.add("-f");
        command.add("mp4");
        command.add(targetPath.toString());
        return command;
    }

    private void runFfmpeg(List<String> command) throws Exception {
        log.info("启动FFmpeg进程，命令: {}", String.join(" ", command));
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        var outputExecutor = Executors.newSingleThreadExecutor();
        CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(() -> readProcessOutput(process, output), outputExecutor);
        try {
            boolean finished = process.waitFor(Math.max(properties.getFfmpegTimeoutMinutes(), 1), TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                log.error("FFmpeg转换超时，强制终止进程");
                throw new TimeoutException("FFmpeg转换超时");
            }
            outputFuture.get(10, TimeUnit.SECONDS);
            if (process.exitValue() != 0) {
                log.error("FFmpeg退出码非零: {}, 输出信息: {}", process.exitValue(), output);
                throw new IOException("FFmpeg退出码为 " + process.exitValue() + ": " + output);
            }
            log.info("FFmpeg进程正常结束，退出码: 0");
        } finally {
            outputExecutor.shutdownNow();
        }
    }

    private void readProcessOutput(Process process, StringBuilder output) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() < MAX_FFMPEG_OUTPUT_LENGTH) {
                    output.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException ex) {
            log.info("读取FFmpeg输出流失败", ex);
        }
    }

    private void validateTargetFile(Path targetPath) throws IOException {
        if (!Files.isRegularFile(targetPath) || Files.size(targetPath) <= 0) {
            log.error("MP4目标文件未生成或大小为0: {}", targetPath);
            throw new IOException("MP4目标文件未生成: " + targetPath);
        }
        log.info("验证MP4目标文件成功，文件大小: {} bytes", Files.size(targetPath));
    }

    private int updateMp4Path(Long recordId, String mp4Path) {
        log.info("更新光电视频记录 {} 的MP4路径: {}", recordId, mp4Path);
        int result = photoelectricFileRecordMapper.update(null, new UpdateWrapper<PhotoelectricFileRecord>()
                .eq("id", recordId)
                .and(wrapper -> wrapper.isNull("local_path_mp4").or().eq("local_path_mp4", ""))
                .set("local_path_mp4", mp4Path));
        log.info("更新光电视频记录 {} 的MP4路径结果: {}", recordId, result);
        return result;
    }

    private void deleteSourceFile(Path sourcePath, Path targetPath) {
        if (sourcePath.toAbsolutePath().normalize().equals(targetPath.toAbsolutePath().normalize())) {
            log.info("源文件和目标文件路径相同，跳过删除操作");
            return;
        }
        try {
            Files.deleteIfExists(sourcePath);
            log.info("成功删除光电视频源文件: {}", sourcePath);
        } catch (IOException ex) {
            log.warn("删除光电视频源文件失败: {}", sourcePath, ex);
        }
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private Path replaceExtension(Path sourcePath, String extension) {
        String fileName = sourcePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String targetFileName = (dotIndex < 0 ? fileName : fileName.substring(0, dotIndex)) + extension;
        Path parent = sourcePath.getParent();
        return parent == null ? Paths.get(targetFileName) : parent.resolve(targetFileName);
    }
}
