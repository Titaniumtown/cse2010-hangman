#!/bin/bash


log_file="bench_log.txt"

#compile the java files
javac HangmanPlayer.java EvalHangmanPlayer.java

test_output=$(java EvalHangmanPlayer words.txt hiddenWords1.txt | grep --color=never ":")

output=$(printf "$(date)\n${test_output}\n")
OLD_IFS="$IFS"
IFS=""
echo $output
echo $output >> "bench_log.txt"
IFS="$OLD_IFS"
