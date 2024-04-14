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

public class HangmanPlayer {
  // Very necessary stuff for word guessing
  private char[][][] dictionary;
  private int[] charCount;
  private ArrayList<char[]> possibleWords;
  private int currWordLength;
  private char lastGuess;
  private int[][] masterCharCount;

  // represents the min and max values that characters result in when caster to an int
  public static final int MIN_CHAR = 97;
  public static final int MAX_CHAR = 122;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) throws IOException {
    this.possibleWords = new ArrayList<>();
    this.currWordLength = 0;
    this.lastGuess = ' ';
    this.addWords(wordFile);
  }

  // Adds words to a hashmap, key is length of the word, words are alphabetically sorted
  private void addWords(String wordFile) throws IOException {
    HashMap<Integer, HashSet<String>> dictNew = new HashMap<Integer, HashSet<String>>();
    // Halved read times by using BufferedReader instead of Scanner
    try (BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(wordFile))) {
      br.lines()
          .map(word -> word.toLowerCase()) // convert everything to lowercase
          .forEach(
              word -> {
                dictNew
                    .computeIfAbsent(
                        word.length(),
                        k -> new HashSet<>()) // add a hashset if it doesn't exist already
                    .add(word); // force lowercase for simplification
              });

      br.close();
    }

    // get the max word length
    final int maxSize = dictNew.keySet().stream().max(Integer::compare).get() + 1;

    // allocate the dictionary to the correct size
    this.dictionary = new char[maxSize][0][0];

    // ok so let me explain this, so we convert from a hashset to an array for perf reasons,
    // but we want the guarentees hashsets give in relation to unique elements. DO NOT CHANGE :3
    for (Map.Entry<Integer, HashSet<String>> entry : dictNew.entrySet()) {
      final int len = dictNew.get(entry.getKey()).size();
      this.dictionary[entry.getKey()] = new char[len][0];
      int i = 0;
      for (final String s : dictNew.get(entry.getKey())) {
        this.dictionary[entry.getKey()][i] = s.toCharArray();
        i++;
      }
    }

    // Create masterCharCount, this will calculate the base charCount for each length in the
    // dictionary
    this.masterCharCount = new int[maxSize][MAX_CHAR - MIN_CHAR + 1];
    for (int size = 0; size < maxSize; size++) {
      // Iterate over all possible words and map out the num of chars
      for (final char[] s : this.dictionary[size]) {
        // Add unique characters
        for (int j = 0; j < size; j++) {
          final int c = s[j];
          // increment the found number of characters
          this.masterCharCount[size][c - MIN_CHAR]++;
        }
      }
    }

    // plz g1gc plz run a gc cycle before we start execution 🥺👉👈 (it won't)
    System.gc();
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
      this.currWordLength = currentWord.length();

      this.possibleWords.clear();
      // add all strings from the correct length word to `this.possibleWords`
      for (int i = 0; i < this.dictionary[this.currWordLength].length; i++) {
        this.possibleWords.add(this.dictionary[this.currWordLength][i]);
      }

      this.charCount = this.masterCharCount[this.currWordLength].clone();
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
    // remove already touched letter as it's fate has already been decided
    this.charCount[(int) this.lastGuess - MIN_CHAR] = 0;

    // apply this feedback to this.possibleWords
    this.removeWords(currentWord);
  }

  /// Takes in string `s` and decrements this.charCount based on the number of specific characters
  // in the string, used in the case of removing words from the `this.possibleWords` pool
  private void decrementCharCount(final char[] s) {
    // Set used to only count unique letters
    for (final char c : s) {
      this.charCount[(int) c - MIN_CHAR]--;
    }
  }

  /// Determines if a word should be removed from `this.possibleWords`, does not remove the word
  // however.
  private boolean shouldRemoveWord(final char[] s, final char[] c) {
    for (int i = 0; i < this.currWordLength; i++) {

      if ((s[i] != c[i]) && ((c[i] != ' ') || (s[i] == this.lastGuess))) {
        return true;
      }
    }
    return false;
  }

  /// Cull out possibleWords that are no longer possible from previous feedback
  private void removeWords(String currentWord) {
    final char[] currWordChars = currentWord.toCharArray();
    this.possibleWords.removeIf(
        s -> {
          if (this.shouldRemoveWord(s, currWordChars)) {
            this.decrementCharCount(s);
            return true;
          } else {
            return false;
          }
        });
  }

  /// Gets the most probable next letter to guess
  private char findNextLetter() {
    // init values
    int maxValue = -1;
    int key = -1;

    for (int i = 0; i < this.charCount.length; i++) {
      // replace `maxValue` and `key` if gotInt is larger than `maxValue`
      if (this.charCount[i] > maxValue) {
        maxValue = this.charCount[i];
        key = i;
      }
    }

    return (char) (key + MIN_CHAR);
  }
}
