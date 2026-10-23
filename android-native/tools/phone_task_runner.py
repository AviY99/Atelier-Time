#!/usr/bin/env python3
"""Restricted Atelier Time phone runner: GitHub Issues -> fixed Gradle tasks."""
import argparse
import datetime
import fcntl
import getpass
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time
from urllib.request import Request, urlopen

REPO = "AviY99/Atelier-Time"
BRANCH = "android-native-kotlin-compose"
ROOT = Path.home() / ".atelier-time-device"
TOKEN_PATH = ROOT / "token"
CHECKOUT = ROOT / "checkout"
TASKS = {
    "doctor": None,
    "unit-tests": ":app:testDebugUnitTest",
    "debug-apk": ":app:assembleDebug",
    "instrumentation": ":app:connectedDebugAndroidTest",
}
MARKER = "ATELIER_DEVICE_TASK_V1"
CLAIM = "ATELIER_DEVICE_CLAIM_V1"
RESULT = "ATELIER_DEVICE_RESULT_V1"


def api(token, method, url, body=None):
    headers = {"Authorization": "Bearer " + token,
               "Accept": "application/vnd.github+json",
               "X-GitHub-Api-Version": "2022-11-28",
               "User-Agent": "Atelier-Time-Phone-Runner"}
    payload = json.dumps(body).encode() if body is not None else None
    if payload is not None:
        headers["Content-Type"] = "application/json"
    req = Request("https://api.github.com/repos/" + REPO + url,
                  data=payload, headers=headers, method=method)
    with urlopen(req, timeout=40) as response:
        data = response.read()
    return json.loads(data) if data else None


def setup():
    ROOT.mkdir(mode=0o700, parents=True, exist_ok=True)
    token = getpass.getpass("GitHub fine-grained token (hidden): ").strip()
    if not token:
        raise ValueError("No token provided")
    api(token, "GET", "/issues?state=open&per_page=1")
    TOKEN_PATH.write_text(token + "\n")
    TOKEN_PATH.chmod(0o600)
    print("Token saved on device only. Never share it in chat.")


def doctor():
    lines = ["Real on-phone build toolchain:", "Python: " + sys.version.split()[0]]
    for executable in ("git", "java", "gradle", "adb"):
        lines.append(executable + ": " + str(shutil.which(executable)))
    for env in ("ANDROID_HOME", "ANDROID_SDK_ROOT", "JAVA_HOME", "GRADLE_HOME"):
        lines.append(env + ": " + str(os.environ.get(env)))
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    ok = all(shutil.which(t) for t in ("git", "java", "gradle")) and sdk and Path(sdk).is_dir()
    lines.append("CLI toolchain ready: " + str(bool(ok)))
    return (0 if ok else 2), "\n".join(lines)


def command(args, cwd=None, timeout=2400):
    try:
        result = subprocess.run(args, cwd=cwd, stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT, text=True, errors="replace",
                                timeout=timeout, check=False)
        return result.returncode, result.stdout
    except subprocess.TimeoutExpired as exc:
        return 124, str(exc) + " (timed out; process terminated)"


def project_at(sha):
    if CHECKOUT.exists() and not (CHECKOUT / ".git").is_dir():
        raise RuntimeError("Runner path exists without a Git clone; refusing overwrite")
    if not CHECKOUT.exists():
        code, out = command(["git", "clone", "--branch", BRANCH, "--single-branch",
                             "https://github.com/" + REPO + ".git", str(CHECKOUT)], timeout=300)
        if code:
            raise RuntimeError("Git clone failed: " + out[-1000:])
    code, status = command(["git", "-C", str(CHECKOUT), "status", "--porcelain"], timeout=40)
    if code or status.strip():
        raise RuntimeError("Runner checkout not clean; refusing to touch user files")
    code, out = command(["git", "-C", str(CHECKOUT), "fetch", "--no-tags", "origin", BRANCH], timeout=300)
    if code:
        raise RuntimeError("Git fetch failed: " + out[-1000:])
    code, head = command(["git", "-C", str(CHECKOUT), "rev-parse", "FETCH_HEAD"], timeout=40)
    if code or head.strip() != sha:
        raise RuntimeError("Requested commit is not the current remote branch head")
    code, out = command(["git", "-C", str(CHECKOUT), "checkout", "--detach", sha], timeout=60)
    if code:
        raise RuntimeError("Checkout failed: " + out[-1000:])
    project = CHECKOUT / "android-native"
    if not (project / "settings.gradle.kts").is_file():
        raise RuntimeError("Missing real android-native Gradle project")
    return project


def execute(task, sha):
    if task == "doctor":
        code, output = doctor()
        return code, output, None
    project = project_at(sha)
    gradle = shutil.which("gradle")
    if not gradle:
        raise RuntimeError("Gradle CLI not available inside phone terminal")
    exit_code, output = command([gradle, TASKS[task], "--no-daemon", "--console=plain"], cwd=project)
    apk_info = None
    if task == "debug-apk" and exit_code == 0:
        apk = project / "app/build/outputs/apk/debug/app-debug.apk"
        if apk.is_file():
            apk_info = ("Local APK: " + str(apk) + "; bytes=" + str(apk.stat().st_size) +
                        "; SHA256=" + hashlib.sha256(apk.read_bytes()).hexdigest())
        else:
            apk_info = "Expected APK not found; artifact UNVERIFIED"
    return exit_code, output, apk_info


def valid(issue):
    if issue.get("pull_request") or issue.get("state") != "open":
        return None
    if issue.get("user", {}).get("login", "").lower() != "aviy99":
        return None
    if not issue.get("title", "").startswith("[ATELIER-DEVICE] "):
        return None
    body = (issue.get("body") or "").strip()
    if not body.startswith(MARKER + "\n"):
        return None
    try:
        request = json.loads(body.split("\n", 1)[1])
    except (ValueError, IndexError):
        return None
    if not isinstance(request, dict) or set(request) != {"task", "sha"}:
        return None
    task, sha = request["task"], request["sha"]
    if task not in TASKS or not isinstance(sha, str) or not re.fullmatch("[0-9a-f]{40}", sha):
        return None
    if issue["title"] != "[ATELIER-DEVICE] " + task:
        return None
    return task, sha


def safe_excerpt(output):
    lines = []
    for line in output.splitlines()[-130:]:
        if any(x in line.lower() for x in ("password", "token", "secret", "authorization", "credential", "api_key")):
            lines.append("[potentially sensitive line removed]")
        else:
            lines.append(line[:500])
    return "\n".join(lines)[-9000:].replace(chr(96) * 3, "' ' '")


def handle(issue, task, sha, token):
    number = issue["number"]
    results = ROOT / "results" / str(number)
    results.mkdir(parents=True, exist_ok=True)
    record = results / "result.json"
    comments = api(token, "GET", f"/issues/{number}/comments?per_page=100")
    prior_claim = any(c.get("body", "").startswith(CLAIM) and
                      c.get("user", {}).get("login", "").lower() == "aviy99" for c in comments)
    if record.is_file():
        outcome = json.loads(record.read_text())
    elif prior_claim:
        print(f"Issue #{number} was claimed without a result; no automatic replay", flush=True)
        return
    else:
        api(token, "POST", f"/issues/{number}/comments", {"body": CLAIM + "\nPhone runner accepted one fixed task."})
        print(f"Running {task} for commit {sha} on phone...", flush=True)
        try:
            code, output, apk = execute(task, sha)
        except Exception as exc:
            code, output, apk = 125, "BLOCKED: " + str(exc), None
        outcome = {"task": task, "sha": sha, "code": code,
                   "result": "PASS" if code == 0 else "FAIL/BLOCKED",
                   "apk": apk, "output": safe_excerpt(output),
                   "utc": datetime.datetime.now(datetime.timezone.utc).isoformat()}
        record.write_text(json.dumps(outcome, ensure_ascii=False))
        record.chmod(0o600)
    message = (RESULT + "\nTask: " + outcome["task"] + "\nCommit: " + outcome["sha"] +
               "\nResult: " + outcome["result"] + " (exit=" + str(outcome["code"]) + ")" +
               "\nUTC: " + outcome["utc"] + "\n" + str(outcome.get("apk") or "No APK claimed") +
               "\nPublic log excerpt (full details kept in a private device result file):\n" +
               outcome["output"])
    api(token, "POST", f"/issues/{number}/comments", {"body": message})
    api(token, "PATCH", f"/issues/{number}", {"state": "closed"})
    print(f"Posted result to GitHub issue #{number}", flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--setup", action="store_true")
    parser.add_argument("--doctor-local", action="store_true")
    parser.add_argument("--poll", action="store_true")
    args = parser.parse_args()
    if args.setup:
        setup()
        return
    if args.doctor_local:
        _, info = doctor()
        print(info)
        return
    ROOT.mkdir(mode=0o700, parents=True, exist_ok=True)
    if not TOKEN_PATH.is_file() or TOKEN_PATH.stat().st_mode & 0o077:
        raise RuntimeError("Missing private token. Run --setup first.")
    token = TOKEN_PATH.read_text().strip()
    with (ROOT / "runner.lock").open("w") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        print("Restricted Atelier Time device runner started; Ctrl+C stops", flush=True)
        while True:
            issues = api(token, "GET", "/issues?state=open&per_page=100")
            handled = False
            for issue in issues:
                request = valid(issue)
                if request:
                    handle(issue, *request, token)
                    handled = True
                    break
            if not handled:
                print("No queued Atelier Time tasks", flush=True)
            if not args.poll:
                break
            time.sleep(90)


if __name__ == "__main__":
    main()
