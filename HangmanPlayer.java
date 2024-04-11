/*

  Authors (group members):
  Email addresses of group members:
  Group name:

  Course:
  Section:

  Description of the overall algorithm:


*/

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class HangmanPlayer {
  // Very necessary stuff for word guessing
  public HashMap<Integer, TreeSet<String>> dictionary;
  HashMap<Character, Integer> charCount;
  public ArrayList<String> possibleWords;
  public ArrayList<Character> good;
  public ArrayList<Character> bad;
  public char lastGuess;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) throws Exception {
    dictionary = new HashMap<Integer, TreeSet<String>>();
    charCount = new HashMap<Character, Integer>();
    possibleWords = new ArrayList<String>();
    good = new ArrayList<Character>();
    bad = new ArrayList<Character>();
    lastGuess = ' ';
    addWords(wordFile);
  }

  // Adds words to a hashmap, key is length of the word, words are alphabetically sorted
  public void addWords(String wordFile) throws Exception {
    File file = new File(wordFile);
    Scanner scanner = new Scanner(file);

    while (scanner.hasNextLine()) {
      String word = scanner.nextLine();
      if (!this.dictionary.containsKey(word.length())) {
        this.dictionary.put(word.length(), new TreeSet<>());
      }
      this.dictionary.get(word.length()).add(word);
    }
  }

  // based on the current (partial or intitially blank) word
  //    guess a letter
  // currentWord: current word, currentWord.length has the length of the hidden word
  // isNewWord: indicates a new hidden word
  // returns the guessed letter
  // assume all letters are in lower case
  public char guess(String currentWord, boolean isNewWord) {
    char guess = ' ';
    // Resets words to check
    // System.out.println(isNewWord);
    if (isNewWord) {
      // Resets all "guessing" values, calls findNextLetter
      TreeSet<String> wordsToCheck = this.dictionary.get(currentWord.length());
      possibleWords = new ArrayList(wordsToCheck);
      charCount = new HashMap<Character, Integer>();
      good = new ArrayList<Character>();
      bad = new ArrayList<Character>();
      guess = findNextLetter(currentWord.length());
    } else {
      guess = findNextLetter(currentWord.length());
    }
    lastGuess = guess;
    return guess;
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
      good.add(lastGuess);
      removeWords(lastGuess, true);
    } else { // If guess was incorrect, remove words with that letter, add letter to bad
      // System.out.println("Boowomp");
      bad.add(lastGuess);
      removeWords(lastGuess, false);
    }
    // System.out.println(lastGuess);
  }

  public void removeWords(char l, boolean good) {
    if (good) {
      for (int i = possibleWords.size() - 1; i >= 0; i--) {
        if (possibleWords.get(i).indexOf(l) == -1) {
          possibleWords.remove(i);
        }
      }
    } else {
      // System.out.println(l);
      for (int i = possibleWords.size() - 1; i >= 0; i--) {
        if (possibleWords.get(i).indexOf(l) != -1) {
          possibleWords.remove(i);
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
    charCount = new HashMap<Character, Integer>();
    // for every word in list of possible words
    for (String s : out) {
      s = s.toLowerCase();
      // Set used to only count unique letters
      Set<Character> found = new HashSet<Character>();
      for (char c : s.toCharArray()) { // Adds unique letters
        if (charCount.containsKey(c) && !found.contains(c)) {
          charCount.put(c, charCount.get(c) + 1);
          found.add(c);
        } else if (!charCount.containsKey(c) && !found.contains(c)) {
          // Add character with count 1 if it's not in the hashmap
          charCount.put(c, 1);
        }
      }
    }
    // remove letters already known
    for (char c : good) {
      charCount.remove(c);
    }
    // I don't think we need this, those words are already removed
    /*
    for (char c : bad) {
        charCount.remove(c);
    }
    */
    // Gets and returns most common letter to guess
    int maxCount = 0;
    for (Map.Entry<Character, Integer> entry : charCount.entrySet()) {
      // System.out.println(entry.getKey() + " " + entry.getValue());
      if (entry.getValue() > maxCount) {
        maxCount = entry.getValue();
        ret = entry.getKey();
      }
    }
    return ret;
  }
}
