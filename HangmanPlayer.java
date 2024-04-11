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
import java.util.Set;
import java.util.TreeSet;

public class HangmanPlayer {
  // Very necessary stuff for word guessing
  private HashMap<Integer, TreeSet<String>> dictionary;
  private HashMap<Character, Integer> charCount;
  private ArrayList<String> possibleWords;
  private ArrayList<Character> good;
  private ArrayList<Character> bad;
  private char lastGuess;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) throws IOException {
    dictionary = new HashMap<Integer, TreeSet<String>>();
    charCount = new HashMap<Character, Integer>();
    possibleWords = new ArrayList<String>();
    good = new ArrayList<Character>();
    bad = new ArrayList<Character>();
    lastGuess = ' ';
    addWords(wordFile);
  }

  // Adds words to a hashmap, key is length of the word, words are alphabetically sorted
  public void addWords(String wordFile) throws IOException {

    // Halved read times by using BufferedReader instead of Scanner
    try (BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(wordFile))) {
      for (String word = br.readLine(); word != null; word = br.readLine()) {
        this.dictionary.putIfAbsent(word.length(), new TreeSet<>());

        // add word to dictionary and make the word lowercase 
        // so that would't have to be done adhoc later
        this.dictionary.get(word.length()).add(word.toLowerCase());
      }
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
      TreeSet<String> wordsToCheck = this.dictionary.get(currentWord.length());
      this.possibleWords = new ArrayList<>(wordsToCheck);
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
      System.out.println(currentWord);
      this.good.add(this.lastGuess);
      removeWords(lastGuess, true);
    } else { // If guess was incorrect, remove words with that letter, add letter to bad
      // System.out.println("Boowomp");
      this.bad.add(this.lastGuess);
      this.removeWords(this.lastGuess, false);
    }
    // System.out.println(lastGuess);
  }

  public void removeWords(char l, boolean good) {
    if (good) {
      for (int i = this.possibleWords.size() - 1; i >= 0; i--) {
        if (this.possibleWords.get(i).indexOf(l) == -1) {
          this.possibleWords.remove(i);
        }
      }
    } else {
      // System.out.println(l);
      for (int i = this.possibleWords.size() - 1; i >= 0; i--) {
        if (this.possibleWords.get(i).indexOf(l) != -1) {
          this.possibleWords.remove(i);
        }
      }
    }
    // System.out.println("words left");
    // for (String s : possibleWords) {
    // System.out.println(s);
    // }
  }

  public char findNextLetter(int l) {
    char ret = ' ';
    ArrayList<String> out = new ArrayList<String>(this.possibleWords);
    // Resets count of all letters found (once per word)
    this.charCount = new HashMap<Character, Integer>();
    // for every word in list of possible words
    for (String s : out) {

      // Set used to only count unique letters
      Set<Character> found = new HashSet<Character>();
      for (final char c : s.toCharArray()) { // Adds unique letters
        if (found.contains(c)) {
          continue;
        }

        
        if (this.charCount.containsKey(c)) {
          this.charCount.put(c, this.charCount.get(c) + 1);
          // found.add(c);
        } else {
          // Add character with count 1 if it's not in the hashmap
          this.charCount.put(c, 1);
        }
      }
    }
    // remove letters already known
    for (final char c : good) {
      this.charCount.remove(c);
    }
    // I don't think we need this, those words are already removed
    /*
    for (char c : bad) {
        charCount.remove(c);
    }
    */
    // Gets and returns most common letter to guess
    int maxCount = 0;
    for (final Map.Entry<Character, Integer> entry : this.charCount.entrySet()) {
      // System.out.println(entry.getKey() + " " + entry.getValue());
      if (entry.getValue() > maxCount) {
        maxCount = entry.getValue();
        ret = entry.getKey();
      }
    }
    return ret;
  }
}
