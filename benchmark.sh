#!/bin/bash


#compile the java files
javac HangmanPlayer.java EvalHangmanPlayer.java

#get output of running EvalHangmanPlayer, only include lines with a semicolon so we get just summary data
test_output=$(java EvalHangmanPlayer words.txt hiddenWords1.txt | grep --color=never ":")

#String formatting
#!TODO add git commit hash tracking
output=$(printf "$(date)\n${test_output}\n")

#IFS variable trick in order to preserve newlines on echo
OLD_IFS="$IFS"
IFS=""

echo $output #echo output to user
echo $output >> "bench_log.txt" #pipe output to the benchmark log file
echo -e "\n" >> "bench_log.txt"

#restore IFS
IFS="$OLD_IFS"
