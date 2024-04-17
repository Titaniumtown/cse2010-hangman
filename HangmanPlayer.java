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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HangmanPlayer {
  // Pre-computed at initialization time
  private char[][][] dictionary;
  private int[][] masterCharCount;

  private PointOfView pov;

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
    final int maxSize = dictNew.keySet().stream().max(Integer::compare).get();

    // allocate the dictionary to the correct size
    this.dictionary = new char[maxSize][0][0];

    // Create masterCharCount, this will calculate the base charCount
    // for each length in the dictionary
    this.masterCharCount = new int[maxSize][PointOfView.MAX_CHAR - PointOfView.MIN_CHAR + 1];

    // make sure it's the right length (length of alphabet)
    assert this.masterCharCount[0].length == 26;

    // ok so let me explain this, so we convert from a hashset to an array for perf reasons,
    // but we want the guarentees hashsets give in relation to unique elements. DO NOT CHANGE :3
    for (Map.Entry<Integer, HashSet<String>> entry : dictNew.entrySet()) {
      final int size = entry.getKey();
      final int len = dictNew.get(size).size();
      this.dictionary[size - 1] = new char[len][0];
      int i = 0;
      for (final String s : dictNew.get(size)) {
        this.dictionary[size - 1][i] = s.toCharArray();
        // add each character to the master char count
        for (final int c : this.dictionary[size - 1][i]) {
          this.masterCharCount[size - 1][c - PointOfView.MIN_CHAR]++;
        }
        i++;
      }
    }

    // create the pov
    this.pov = new PointOfView();

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
      this.pov.update(this.masterCharCount[length - 1], this.dictionary[length - 1], length);
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
    // apply this feedback to this.possibleWords
    this.pov.removeWords(currentWord);
  }
}
