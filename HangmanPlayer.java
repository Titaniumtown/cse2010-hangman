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
  private HashMap<Integer, HashSet<String>> dictionary;
  private HashMap<Character, AtomicInteger> charCount;
  private ArrayList<String> possibleWords;
  private String good;
  private String bad;

  private HashMap<Character, ArrayList<Integer>> known;
  private char lastGuess;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) throws IOException {
    this.dictionary = new HashMap<Integer, HashSet<String>>();
    this.charCount = new HashMap<Character, AtomicInteger>();
    this.possibleWords = new ArrayList<String>();
    this.known = new HashMap<Character, ArrayList<Integer>>();
    this.good = "";
    this.bad = "";
    this.lastGuess = ' ';
    this.addWords(wordFile);
  }

  // Adds words to a hashmap, key is length of the word, words are alphabetically sorted
  private void addWords(String wordFile) throws IOException {
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
      this.known.clear();

      // for every word in list of possible words
      for (final String s : this.possibleWords) {
        // Set used to only count unique letters
        for (int i = 0; i < s.length(); i++) { // Adds unique letters
          final char c = s.charAt(i);
          this.charCount.computeIfAbsent(c, k -> new AtomicInteger(0)).incrementAndGet();
        }
      }

      this.good = "";
      this.bad = "";
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
    if (isCorrectGuess) {
      // If guess was correct, remove words without that letter, add letter to this.good
      this.good += (this.lastGuess);

      // HASHMAP to store correct chars and their locations
      // for every word in possibleWords, check every char in "known" hashmap against possibleWord's
      // word at those locations
      // remove if not matching
      for (int i = 0; i < currentWord.length(); i++) {
        final char currChar = currentWord.charAt(i);
        if (currChar == this.lastGuess) {
          known.putIfAbsent(currChar, new ArrayList<>());
          known.get(currChar).add(i);
        }
      }
    } else {
      // If guess was incorrect, remove words with that letter, add letter to this.bad
      this.bad += (this.lastGuess);
    }

    // apply this feedback to this.possibleWords
    this.removeWords(this.lastGuess, isCorrectGuess, currentWord);
  }

  private void removeCharCount(final String s) {
    // Set used to only count unique letters
    for (int i = 0; i < s.length(); i++) { // Adds unique letters
      final char c = s.charAt(i);
      if (this.charCount.containsKey(c)) {
        int value = this.charCount.get(c).decrementAndGet();
        // if the value is zero, we can just remove it!
        if (value == 0) {
          this.charCount.remove(c);
        }
      }
    }
  }

  // NOTE: this is the major perf constraint in profiling, specifically the `.remove` calling
  private void removeWords(char l, boolean good, String cW) {
    this.possibleWords.removeIf(
        s -> {
          // remove empty options (passed from `this.compareWordAndKnown`)
          if (s.isEmpty()) {
            return true;
          }

          for (int i = 0; i < cW.length(); i++) {
            final char c = cW.charAt(i);
            if (c == ' ') {
              continue;
            }

            if (s.charAt(i) != c) {
              this.removeCharCount(s);
              return true;
            }
          }

          final boolean index = s.indexOf(l) == -1;
          if ((good && index) || !(good || index)) {
            this.removeCharCount(s);
            return true;
          } else {
            return false;
          }
        });

    if (good) {
      compareWordAndKnown(cW);
    }
  }

  private char findNextLetter(int l) {
    // remove letters already known
    for (final char c : this.good.toCharArray()) {
      this.charCount.remove(c);
    }

    // Gets and returns most common letter to guess
    Map.Entry<Character, Integer> maxEntry =
        this.charCount.entrySet().stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().intValue()))
            .max(Map.Entry.comparingByValue())
            .get();
    return maxEntry.getKey();
  }

  // is called every time the current word is updated
  // gets location of every known char in the current word, stores in HASHMAP (OMG A HASHMAP I LOVE
  // HASHMAPS)
  // compares locations of chars against all words in possibleWords, removes words that don't fit
  // with correct chars
  private void compareWordAndKnown(String cW) {
    // NOTE: implementing removeIf here is not very useful as it balloons memory usage
    for (int i = this.possibleWords.size() - 1; i >= 0; i--) {
      final String word = this.possibleWords.get(i);
      boolean good = true;
      for (final char c : known.keySet()) {
        for (final int pos : known.get(c)) {
          if (word.charAt(pos) != c) {

            // Defer removal to `this.removeWords`
            this.possibleWords.set(i, "");
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
