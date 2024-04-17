/*
  Authors (group members): Simon Gardling, Mateusz Doda, Ali Hussain, Carter Tabin

  Email addresses of group members: sgardling2023@my.fit.edu, mdoda2023@my.fit.edu, ahussain2021@my.fit.edu, ctabin2023@my.fit.edu
  Group name: Doofenshmirtz Evil Inc.

  Course: CSE2010
  Section: 2/3

  Description of the overall algorithm:

  Initialization:
  Ok so basically the program reads the `words.txt` file with a Buffered Reader.
  Then it puts all the words into a hashmaps of hashsets indexed by the length of the words.
  The hashmap is then turned into a 3d (so many dimentions wow) array of chars, the first index being the size, 2nd being word entry, 3rd being the character.
  The 3d array of chars is stored in `this.dictionary`
  The variable `this.masterCharCount` is produced by counting the character counts for each length words.

  Runtime:
  `this.charCount` is filled up by indexing `this.masterCharCount`. Same with `this.possibleWords` being filled up by `this.dictionary`.
  The next most probable character is determined by finding the largest number in `this.charCount` and returning it's index as a char.
  When a guess is made, EvalHangmanPlayer tells us if the guess was correct or not, we don't really use this information as it's faster to ignore it.
  When we get feedback, we set the `this.charCount[this.lastGuess]` to -1 so that word wouldn't ever be guessed again.
  Pruning of `this.possibleWords` is then done, removing all words that don't fit what is currently known about the word.
  When a word is removed from `this.possibleWords`, it's characters are decremented from `this.charCount` in order to further guide the character picking process
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HangmanPlayer {
  // Pre-computed at initialization time
  private char[][][] dictionary;
  private int[][] masterCharCount;

  private PointOfView pov;

  class PointOfView {

    // Used at guess-time
    private int[] charCount;
    private ArrayList<char[]> possibleWords;
    private int currWordLength;
    private char lastGuess;

    PointOfView(int[] charCount, char[][] possibleWords, int currWordLength) {
      this.currWordLength = currWordLength;
      this.charCount = charCount.clone();
      this.possibleWords = new ArrayList<>();
      for (final char[] s : possibleWords) {
        this.possibleWords.add(s);
      }
      this.lastGuess = ' ';
    }

    /// Takes in string `s` and decrements this.charCount based on the number of specific characters
    /// in the string, used in the case of removing words from the `this.possibleWords` pool
    private void decrementCharCount(final char[] s) {
      // iterate through each character in the word
      for (final char c : s) {
        // decrement the respective charCount entry
        this.charCount[(int) c - MIN_CHAR]--;
      }
    }

    /// Determines if a word should be removed from `this.possibleWords`
    /// does not remove the word however.
    private boolean shouldRemoveWord(final char[] s, final char[] c) {
      assert s.length == c.length;

      // iterate through the shared length of the two char arrays
      for (int i = 0; i < this.currWordLength; i++) {
        /*
        If s[i] != c[i]: this means that the characters in the possible word and the known current word characters don't match.
        The c[i] != ' ': be true if there is a character there, but it wasn't the one in the possible word, meaning a collision.

        s[i] == this.lastGuess: so if s[i] != c[i] and s[i] == this.lastGuess, this means that c[i] != this.lastGuess, so this word is invalid.
        */
        if ((s[i] != c[i]) && ((c[i] != ' ') || (s[i] == this.lastGuess))) {
          return true;
        }
      }
      return false;
    }

    /// Cull out possibleWords that are no longer possible from previous feedback
    private void removeWords(final String currentWord) {
      final char[] currWordChars = currentWord.toCharArray();
      this.possibleWords.removeIf(
          s -> {
            if (this.shouldRemoveWord(s, currWordChars)) {
              this.decrementCharCount(s);
              return true;
            }
            return false;
          });
    }

    /// Gets the most probable next letter to guess
    private char findNextLetter() {
      // init values
      int maxValue = this.charCount[0];
      int key = 0;

      for (int i = 1; i < 26; i++) {
        final int count = this.charCount[i];
        // replace `maxValue` and `key` if gotInt is larger than `maxValue`
        if (count > maxValue) {
          maxValue = count;
          key = i;
        }
      }

      // return the key shifted by MIN_CHAR
      return (char) (key + MIN_CHAR);
    }

    char guess() {
      this.lastGuess = this.findNextLetter();
      this.charCount[(int) this.lastGuess - MIN_CHAR] = -1;
      return this.lastGuess;
    }
  }

  // represents the min and max values that characters result in when casted to an int
  public static final int MIN_CHAR = 97;
  public static final int MAX_CHAR = 122;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) throws IOException {
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
                    .add(word);
              });

      br.close();
    }

    // get the max word length
    final int maxSize = dictNew.keySet().stream().max(Integer::compare).get() + 1;

    // allocate the dictionary to the correct size
    this.dictionary = new char[maxSize][0][0];

    // Create masterCharCount, this will calculate the base charCount
    // for each length in the dictionary
    this.masterCharCount = new int[maxSize][MAX_CHAR - MIN_CHAR + 1];

    // make sure it's the right length (length of alphabet)
    assert this.masterCharCount[0].length == 26;

    // ok so let me explain this, so we convert from a hashset to an array for perf reasons,
    // but we want the guarentees hashsets give in relation to unique elements. DO NOT CHANGE :3
    for (Map.Entry<Integer, HashSet<String>> entry : dictNew.entrySet()) {
      final int size = entry.getKey();
      final int len = dictNew.get(size).size();
      this.dictionary[size] = new char[len][0];
      int i = 0;
      for (final String s : dictNew.get(size)) {
        this.dictionary[size][i] = s.toCharArray();
        // add each character to the master char count
        for (final int c : this.dictionary[size][i]) {
          this.masterCharCount[size][c - MIN_CHAR]++;
        }
        i++;
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
      final int length = currentWord.length();
      this.pov = new PointOfView(this.masterCharCount[length], this.dictionary[length], length);
    }

    return this.pov.guess();
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

    // apply this feedback to this.possibleWords
    this.pov.removeWords(currentWord);
  }
}
