/*

  Authors (group members):
  Email addresses of group members:
  Group name:

  Course:
  Section:

  Description of the overall algorithm:


*/

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HangmanPlayer {
  // Very necessary stuff for word guessing
  private String[][] dictionary;
  private AtomicInteger[] charCount;
  private ArrayList<String> possibleWords;
  private int currWordLength;
  private char lastGuess;
  private int[][] masterCharCount;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) throws IOException {
    this.possibleWords = new ArrayList<String>();
    this.currWordLength = 0;
    this.lastGuess = ' ';
    this.addWords(wordFile);
  }

  // Adds words to a hashmap, key is length of the word, words are alphabetically sorted
  private void addWords(String wordFile) throws IOException {
    // Halved read times by using BufferedReader instead of Scanner
    try (BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(wordFile))) {
      HashMap<Integer, HashSet<String>> dictNew = new HashMap<Integer, HashSet<String>>();
      br.lines()
          .map(word -> word.toLowerCase())
          .forEach(
              word -> {
                dictNew
                    .computeIfAbsent(
                        word.length(),
                        k -> new HashSet<>()) // add a hashset if it doesn't exist already
                    .add(word); // force lowercase for simplification
              });

      br.close();

      // get the max word length
      final int maxSize = dictNew.keySet().stream().max(Integer::compare).get();

      // allocate the dictionary to the correct size
      this.dictionary = new String[maxSize + 1][0];

      // ok so let me explain this, so we convert from a hashset to an array for perf reasons,
      // but we want the guarentees hashsets give in relation to unique elements. DO NOT CHANGE :3
      for (Map.Entry<Integer, HashSet<String>> entry : dictNew.entrySet()) {
        final int len = dictNew.get(entry.getKey()).size();
        this.dictionary[entry.getKey()] = dictNew.get(entry.getKey()).toArray(new String[len]);
      }

      // Create masterCharCount, this will calculate the base charCount for each length in the
      // dictionary
      this.masterCharCount = new int[maxSize + 1][256];
      for (int i = 0; i <= maxSize; i++) {
        // Iterate over all possible words and map out the num of chars
        for (final String s : this.dictionary[i]) {
          // Set used to only count unique letters
          for (int j = 0; j < i; j++) { // Adds unique letters
            final int c = (int) s.charAt(j);
            this.masterCharCount[i][c]++;
          }
        }
      }
    }
  }

  // based on the current (partial or intitially blank) word
  //    guess a letter
  // currentWord: current word, currentWord.length has the length of the hidden word
  // isNewWord: indicates a new hidden word
  // returns the guessed letter
  // assume all letters are in lower case
  public char guess(String currentWord, boolean isNewWord) {
    // Resets words to check
    // System.out.println(isNewWord);
    if (isNewWord) {
      // Resets all "guessing" values, calls findNextLetter
      this.possibleWords.clear();
      this.currWordLength = currentWord.length();

      // add all strings from the correct length word to `this.possibleWords`
      for (final String s : this.dictionary[this.currWordLength]) {
        this.possibleWords.add(s);
      }

      // allocate a new `this.charCount`
      this.charCount = new AtomicInteger[256];

      // fill-up `this.charCount` with values from `this.masterCharCount`
      for (int i = 0; i < this.charCount.length; i++) {
        final int got = this.masterCharCount[this.currWordLength][i];
        if (got > 0) {
          this.charCount[i] = new AtomicInteger(got);
        }
      }
    }

    this.lastGuess = findNextLetter();
    return this.lastGuess;
  }

  // feedback on the guessed letter
  // isCorrectGuess: true if the guessed letter is one of the letters in the hidden word
  // currentWord: partially filled or blank word
  //
  // Case       isCorrectGuess      currentWord
  // a.         true                partial word with the guessed letter
  //                                   or the whole word if the guessed letter was the
  //                                   last letter needed
  // b.         false               partial word without the guessed letter
  public void feedback(boolean isCorrectGuess, String currentWord) {
    if (isCorrectGuess) {
      // remove letters already known
      this.charCount[(int) this.lastGuess] = null;
    }

    // apply this feedback to this.possibleWords
    this.removeWords(this.lastGuess, isCorrectGuess, currentWord);
  }

  /// Takes in string `s` and decrements this.charCount based on the number of specific characters
  // in the string, used in the case of removing words from the `this.possibleWords` pool
  private void decrementCharCount(final String s) {
    // Set used to only count unique letters
    for (int i = 0; i < this.currWordLength; i++) { // Adds unique letters
      final char c = s.charAt(i);
      AtomicInteger got = this.charCount[(int) c];
      if (got != null) {
        // if the value is zero, we can just remove it!
        if (got.decrementAndGet() <= 0) {
          this.charCount[(int) c] = null;
        }
      }
    }
  }

  /// Cull out possibleWords that are no longer possible from previous feedback
  private void removeWords(char l, boolean good, String currentWord) {
    this.possibleWords.removeIf(
        s -> {
          // remove empty options (passed from `this.compareWordAndKnown`)
          if (s.isBlank()) {
            return true;
          }

          int index = -1;
          for (int i = 0; i < this.currWordLength; i++) {
            final char c = currentWord.charAt(i);
            if (c == ' ') {
              continue;
            }

            if (c == l) {
              index = i;
            }

            if (s.charAt(i) != c) {
              this.decrementCharCount(s);
              return true;
            }
          }

          // avoid using indexOf if we already touch on the character in the previous for loop
          if (index == -1) {
            index = s.indexOf(l);
          }

          final boolean notFound = index == -1;
          if ((good && notFound) || !(good || notFound)) {
            this.decrementCharCount(s);
            return true;
          } else {
            return false;
          }
        });
  }

  /// Gets the most probable next letter to guess
  private char findNextLetter() {
    int maxValue = -1;
    int key = -1;

    for (int i = 0; i < this.charCount.length; i++) {
      final AtomicInteger got = this.charCount[i];
      if (got == null) {
        continue;
      }
      final int gotInt = got.intValue();

      if (gotInt > maxValue) {
        maxValue = gotInt;
        key = i;
      }
    }

    return (char) key;
  }
}
