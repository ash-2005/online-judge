package com.oj.judge.sandbox;

import com.oj.common.enums.Language;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SandboxExecutor {

    private static final Logger log = LoggerFactory.getLogger(SandboxExecutor.class);
    private static final long COMPILE_TIMEOUT_MS = 30_000L;

    private final Path workRoot;
    private final boolean windows;
    private final String pythonCommand;
    private final String exeSuffix;
    private final boolean useDocker;
    private final boolean dockerAvailable;
    private final String javaImage;
    private final String pythonImage;
    private final String cppImage;

    public SandboxExecutor(
            @Value("${app.sandbox.workdir}") String workdir,
            @Value("${app.sandbox.use-docker:false}") boolean useDocker,
            @Value("${app.sandbox.docker.java-image:eclipse-temurin:21-jdk}") String javaImage,
            @Value("${app.sandbox.docker.python-image:python:3.12-slim}") String pythonImage,
            @Value("${app.sandbox.docker.cpp-image:gcc:13}") String cppImage
    ) throws IOException {
        this.workRoot = Paths.get(workdir);
        Files.createDirectories(this.workRoot);
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        this.windows = os.contains("win");
        this.pythonCommand = windows ? "python" : "python3";
        this.exeSuffix = windows ? ".exe" : "";
        this.javaImage = javaImage;
        this.pythonImage = pythonImage;
        this.cppImage = cppImage;

        boolean dockerOk = false;
        if (useDocker) {
            dockerOk = probeDocker();
            if (dockerOk) {
                log.warn(
                        "app.sandbox.use-docker=true: Docker sandbox is EXPERIMENTAL "
                                + "(official images, --network none). Process sandbox remains the supported default.");
            } else {
                log.warn(
                        "app.sandbox.use-docker=true but docker is unavailable or failed probe; "
                                + "falling back to process sandbox.");
            }
        }
        this.useDocker = useDocker && dockerOk;
        this.dockerAvailable = dockerOk;
    }

    public SandboxResult execute(
            Language language,
            String code,
            String stdin,
            int timeLimitMs,
            int memoryLimitMb) throws IOException, InterruptedException {
        PreparedJob job = prepare(language, code);
        try {
            SandboxResult compiled = compile(job);
            if (compiled.isTimedOut() || compiled.isCompilationFailed()) {
                return compiled;
            }
            return run(job, stdin, timeLimitMs, memoryLimitMb);
        } finally {
            cleanup(job);
        }
    }

    public PreparedJob prepare(Language language, String code) throws IOException {
        Path jobDir = Files.createTempDirectory(workRoot, "job-");
        switch (language) {
            case JAVA -> Files.writeString(jobDir.resolve("Main.java"), code, StandardCharsets.UTF_8);
            case PYTHON -> Files.writeString(jobDir.resolve("main.py"), code, StandardCharsets.UTF_8);
            case CPP -> Files.writeString(jobDir.resolve("main.cpp"), code, StandardCharsets.UTF_8);
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return new PreparedJob(jobDir, language);
    }

    public SandboxResult compile(PreparedJob job) throws IOException, InterruptedException {
        if (useDocker) {
            try {
                return compileDocker(job);
            } catch (Exception e) {
                log.warn("Docker compile failed ({}), falling back to process: {}", job.language(), e.getMessage());
            }
        }
        return compileProcess(job);
    }

    public SandboxResult run(
            PreparedJob job,
            String stdin,
            int timeLimitMs,
            int memoryLimitMb) throws IOException, InterruptedException {
        if (useDocker) {
            try {
                return runDocker(job, stdin, timeLimitMs, memoryLimitMb);
            } catch (Exception e) {
                log.warn("Docker run failed ({}), falling back to process: {}", job.language(), e.getMessage());
            }
        }
        return runProcess(job.dir(), buildRunCommand(job, memoryLimitMb), stdin, timeLimitMs, false);
    }

    public void cleanup(PreparedJob job) {
        if (job == null || job.dir() == null) {
            return;
        }
        try (Stream<Path> walk = Files.walk(job.dir())) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.debug("Failed to delete sandbox path {}: {}", path, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("Failed to clean sandbox dir {}: {}", job.dir(), e.getMessage());
        }
    }

    private SandboxResult compileProcess(PreparedJob job) throws IOException, InterruptedException {
        return switch (job.language()) {
            case JAVA -> runProcess(
                    job.dir(),
                    List.of("javac", "Main.java"),
                    null,
                    COMPILE_TIMEOUT_MS,
                    true);
            case CPP -> runProcess(
                    job.dir(),
                    List.of("g++", "-O2", "-std=c++17", "-o", "main" + exeSuffix, "main.cpp"),
                    null,
                    COMPILE_TIMEOUT_MS,
                    true);
            case PYTHON -> SandboxResult.builder()
                    .timedOut(false)
                    .exitCode(0)
                    .stdout("")
                    .stderr("")
                    .runtimeMs(0)
                    .compilationFailed(false)
                    .build();
        };
    }

    private SandboxResult compileDocker(PreparedJob job) throws IOException, InterruptedException {
        return switch (job.language()) {
            case JAVA -> runProcess(
                    job.dir(),
                    dockerBase(job.dir(), javaImage, 0, false, List.of("javac", "Main.java")),
                    null,
                    COMPILE_TIMEOUT_MS,
                    true);
            case CPP -> runProcess(
                    job.dir(),
                    dockerBase(job.dir(), cppImage, 0, false, List.of("g++", "-O2", "-std=c++17", "-o", "main", "main.cpp")),
                    null,
                    COMPILE_TIMEOUT_MS,
                    true);
            case PYTHON -> SandboxResult.builder()
                    .timedOut(false)
                    .exitCode(0)
                    .stdout("")
                    .stderr("")
                    .runtimeMs(0)
                    .compilationFailed(false)
                    .build();
        };
    }

    private SandboxResult runDocker(
            PreparedJob job,
            String stdin,
            int timeLimitMs,
            int memoryLimitMb) throws IOException, InterruptedException {
        List<String> inner = switch (job.language()) {
            case JAVA -> {
                List<String> cmd = new ArrayList<>();
                cmd.add("java");
                if (memoryLimitMb > 0) {
                    cmd.add("-Xmx" + memoryLimitMb + "m");
                }
                cmd.add("Main");
                yield cmd;
            }
            case PYTHON -> List.of("python", "main.py");
            case CPP -> List.of("./main");
        };
        String image = switch (job.language()) {
            case JAVA -> javaImage;
            case PYTHON -> pythonImage;
            case CPP -> cppImage;
        };
        return runProcess(
                job.dir(),
                dockerBase(job.dir(), image, memoryLimitMb, true, inner),
                stdin,
                timeLimitMs,
                false);
    }

    private List<String> dockerBase(
            Path workDir,
            String image,
            int memoryLimitMb,
            boolean interactive,
            List<String> innerCommand) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        if (interactive) {
            cmd.add("-i");
        }
        cmd.add("--network");
        cmd.add("none");
        if (memoryLimitMb > 0) {
            cmd.add("--memory");
            cmd.add(memoryLimitMb + "m");
        }
        cmd.add("-v");
        cmd.add(toDockerVolume(workDir) + ":/workdir");
        cmd.add("-w");
        cmd.add("/workdir");
        cmd.add(image);
        cmd.addAll(innerCommand);
        return cmd;
    }

    private String toDockerVolume(Path path) {
        String abs = path.toAbsolutePath().normalize().toString();
        if (windows) {
            // windows path for docker mount
            return abs.replace('\\', '/');
        }
        return abs;
    }

    private boolean probeDocker() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "version", "--format", "{{.Server.Version}}");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean finished = p.waitFor(8, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (Exception e) {
            log.debug("Docker probe failed: {}", e.getMessage());
            return false;
        }
    }

    private List<String> buildRunCommand(PreparedJob job, int memoryLimitMb) {
        return switch (job.language()) {
            case JAVA -> {
                List<String> cmd = new ArrayList<>();
                cmd.add("java");
                if (memoryLimitMb > 0) {
                    cmd.add("-Xmx" + memoryLimitMb + "m");
                }
                cmd.add("Main");
                yield cmd;
            }
            case PYTHON -> List.of(pythonCommand, "main.py");
            case CPP -> List.of(job.dir().resolve("main" + exeSuffix).toAbsolutePath().toString());
        };
    }

    private SandboxResult runProcess(
            Path workDir,
            List<String> command,
            String stdin,
            long timeoutMs,
            boolean compilation) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(false);

        long started = System.nanoTime();
        Process process = pb.start();

        if (stdin != null) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } else {
            process.getOutputStream().close();
        }

        AtomicReference<String> stdoutRef = new AtomicReference<>("");
        AtomicReference<String> stderrRef = new AtomicReference<>("");
        Thread outThread = drainAsync(process.getInputStream(), stdoutRef);
        Thread errThread = drainAsync(process.getErrorStream(), stderrRef);

        boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        long runtimeMs = (System.nanoTime() - started) / 1_000_000L;

        if (!finished) {
            process.destroyForcibly();
            process.waitFor(2, TimeUnit.SECONDS);
            joinQuietly(outThread);
            joinQuietly(errThread);
            return SandboxResult.builder()
                    .timedOut(true)
                    .exitCode(null)
                    .stdout(stdoutRef.get())
                    .stderr(stderrRef.get())
                    .runtimeMs(runtimeMs)
                    .compilationFailed(compilation)
                    .build();
        }

        joinQuietly(outThread);
        joinQuietly(errThread);
        int exitCode = process.exitValue();
        boolean compileFailed = compilation && exitCode != 0;

        return SandboxResult.builder()
                .timedOut(false)
                .exitCode(exitCode)
                .stdout(stdoutRef.get())
                .stderr(stderrRef.get())
                .runtimeMs(runtimeMs)
                .compilationFailed(compileFailed)
                .build();
    }

    private static Thread drainAsync(InputStream in, AtomicReference<String> target) {
        Thread t = new Thread(() -> {
            try {
                target.set(readFully(in));
            } catch (IOException e) {
                target.set("");
            }
        }, "sandbox-stream-drain");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static String readFully(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buf.write(chunk, 0, n);
        }
        return buf.toString(StandardCharsets.UTF_8);
    }

    private static void joinQuietly(Thread t) {
        try {
            t.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isDockerModeActive() {
        return useDocker;
    }

    public boolean isDockerAvailable() {
        return dockerAvailable;
    }

    public record PreparedJob(Path dir, Language language) {
    }
}
