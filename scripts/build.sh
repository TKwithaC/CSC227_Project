#!/usr/bin/env bash
# Compile both source trees and run the oracle tests.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
rm -rf out
mkdir -p out/ai out/team out/tests
find src/ai -name '*.java' > out/ai.sources
javac --release 11 -d out/ai @out/ai.sources
find src/team -name '*.java' > out/team.sources
javac --release 11 -d out/team @out/team.sources
javac --release 11 -cp "out/ai:out/team" -d out/tests tests/csc227/check/OracleTest.java
java -cp "out/ai:out/team:out/tests" csc227.check.OracleTest
mkdir -p docs/test-runs
java -cp out/ai csc227.ai.SchedulerApp samples/job.txt --all > docs/test-runs/sample-all.txt
java -cp out/ai csc227.ai.SchedulerApp samples/job-memory.txt --all > docs/test-runs/memory-all.txt
java -cp out/team csc227.team.task4.SchedulerApp samples/job.txt --algorithm=fcfs > docs/test-runs/team-fcfs.txt
echo "Build, tests, and sample runs finished."
