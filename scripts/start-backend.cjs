/**
 * Kök dizinden Spring Boot'u dev profiliyle başlatır (Windows: mvnw.cmd, diğer: ./mvnw).
 */
const { spawn } = require("child_process");
const path = require("path");

const root = path.resolve(__dirname, "..");
const isWin = process.platform === "win32";
const cmd = isWin ? "mvnw.cmd" : "./mvnw";
const child = spawn(cmd, ["spring-boot:run"], {
  cwd: root,
  stdio: "inherit",
  shell: isWin,
});

child.on("exit", (code, signal) => {
  if (signal) process.kill(process.pid, signal);
  process.exit(code == null ? 1 : code);
});
