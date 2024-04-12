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
  private HashMap<Integer, HashSet<String>> dictionary;
  private HashMap<Character, Integer> charCount;
  private ArrayList<String> possibleWords;
  private HashSet<Character> good;
  private HashSet<Character> bad;
  private char lastGuess;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) throws IOException {
    dictionary = new HashMap<Integer, HashSet<String>>();
    charCount = new HashMap<Character, Integer>();
    possibleWords = new ArrayList<String>();
    good = new HashSet<Character>();
    bad = new HashSet<Character>();
    lastGuess = ' ';
    addWords(wordFile);
  }

  // Adds words to a hashmap, key is length of the word, words are alphabetically sorted
  public void addWords(String wordFile) throws IOException {
    // Halved read times by using BufferedReader instead of Scanner
    try (BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(wordFile))) {
      br.lines()
          .forEach(
              word -> {
                this.dictionary.putIfAbsent(word.length(), new HashSet<>());

                // add word to dictionary and make the word lowercase
                // so that would't have to be done adhoc later
                this.dictionary.get(word.length()).add(word.toLowerCase());
              });
      br.close();
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
      this.possibleWords.addAll(this.dictionary.get(currentWord.length()));
      this.charCount.clear();
      this.good.clear();
      this.bad.clear();
    }

    this.lastGuess = findNextLetter(currentWord.length());
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
    if (isCorrectGuess) { // If guess was correct, remove words without that letter, add letter to
      // good
      // System.out.println("Nice");
      // System.out.println(currentWord);
      this.good.add(this.lastGuess);
    } else { // If guess was incorrect, remove words with that letter, add letter to bad
      // System.out.println("Boowomp");
      this.bad.add(this.lastGuess);
    }

    this.removeWords(this.lastGuess, isCorrectGuess, currentWord);
    // System.out.println(lastGuess);
  }

  // NOTE: this is the major perf constraint in profiling, specifically the `.remove` calling
  public void removeWords(char l, boolean good, String cW) {
    this.possibleWords.removeIf(
        s -> {
          final int index = s.indexOf(l);
          return ((good && (index == -1)) || (!good && (index != -1)));
        });

    if (good) {
      compareWordAndKnown(cW);
    }
  }

  public char findNextLetter(int l) {
    // Resets count of all letters found (once per word)
    this.charCount.clear();
    // for every word in list of possible words
    for (final String s : this.possibleWords) {
      // Set used to only count unique letters
      for (final char c : s.toCharArray()) { // Adds unique letters
        this.charCount.put(c, this.charCount.getOrDefault(c, 0) + 1);
      }
    }

    // remove letters already known
    for (final char c : this.good) {
      this.charCount.remove(c);
    }

    // Gets and returns most common letter to guess
    Map.Entry<Character, Integer> maxEntry =
        this.charCount.entrySet().stream().max(Map.Entry.comparingByValue()).get();
    return maxEntry.getKey();
  }

  // is called every time the current word is updated
  // gets location of every known char in the current word, stores in HASHMAP (OMG A HASHMAP I LOVE
  // HASHMAPS)
  // compares locations of chars against all words in possibleWords, removes words that don't fit
  // with correct chars
  public void compareWordAndKnown(String cW) {
    // HASHMAP to store correct chars and their locations
    HashMap<Character, ArrayList<Integer>> known = new HashMap<Character, ArrayList<Integer>>();

    // Adds chars and locations to "known" hashmap
    for (int i = 0; i < cW.length(); i++) {
      final char currChar = cW.charAt(i);
      if (currChar != ' ') {
        known.putIfAbsent(currChar, new ArrayList<>());
        known.get(currChar).add(i);
      }
    }

    // for every word in possibleWords, check every char in "known" hashmap against possibleWord's
    // word at those locations
    // remove if not matching
    for (int i = this.possibleWords.size() - 1; i >= 0; i--) {
      final String word = this.possibleWords.get(i);
      boolean good = true;
      for (final char c : known.keySet()) {
        for (final int pos : known.get(c)) {
          if (word.charAt(pos) != c) {
            this.possibleWords.remove(i);
            good = false;
            break;
          }
        }
        if (!good) {
          break;
        }
      }
    }
  }
}
