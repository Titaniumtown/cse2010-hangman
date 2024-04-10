/*

  Authors (group members):
  Email addresses of group members:
  Group name:

  Course:
  Section:

  Description of the overall algorithm:


*/

import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;

public class HangmanPlayer
{
    public HashMap<Integer, TreeSet<String>> dictionary;
    
    // initialize HangmanPlayer with a file of English words
    public HangmanPlayer(String wordFile) throws Exception {
        dictionary = new HashMap<Integer, TreeSet<String>>();
        addWords(wordFile);
    }
    
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
        TreeSet<String> wordsToCheck = this.dictionary.get(currentWord.length());
        if (isNewWord) {
            guess = 'e';
        } else {
            
        }

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
    public void feedback(boolean isCorrectGuess, String currentWord)
    {
        
    }

}
