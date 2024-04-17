import java.util.ArrayList;

class PointOfView {

  // represents the min and max values that characters result in when casted to an int
  public static final int MIN_CHAR = 97;
  public static final int MAX_CHAR = 122;

  // Used at guess-time
  private int[] charCount;
  private ArrayList<char[]> possibleWords;
  private int currWordLength;
  private char lastGuess;

  public PointOfView() {
    this.currWordLength = 0;
    this.charCount = new int[0];
    this.possibleWords = new ArrayList<>();
    this.lastGuess = ' ';
  }

  public void update(int[] charCount, char[][] possibleWords, int currWordLength) {
    assert charCount.length == 26;
    assert possibleWords.length > 0;
    assert currWordLength > 0;

    this.currWordLength = currWordLength;
    this.charCount = charCount.clone();
    this.possibleWords.clear();
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
  public void removeWords(final String currentWord) {
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

  public char guess() {
    this.lastGuess = this.findNextLetter();
    // remove already touched letter as it's fate has already been decided
    this.charCount[(int) this.lastGuess - MIN_CHAR] = -1;

    return this.lastGuess;
  }
}
