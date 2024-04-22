import java.util.ArrayList;

class PointOfView {

  // represents the min and max values that characters result in when casted to an int
  public static final int MIN_CHAR = 97;
  public static final int MAX_CHAR = 122;

  // Used at guess-time
  private int[] charCount;
  private double[] bitsInformationByChar;
  private ArrayList<char[]> possibleWords;
  private int currWordLength;
  private char lastGuess;

  public PointOfView() {
    this.currWordLength = 0;
    this.charCount = new int[0];
    this.bitsInformationByChar = new double[26];
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

  public void calculateBitsBasedOnEntropy(){
    int totalChars = 0;
        // Calculate total characters and adjust for '-1' which means zero occurrence.
        for (int count : charCount) {
            if (count > 0) {
                totalChars += count;
            }
        }

        double[] probabilities = new double[charCount.length];
        // Calculate probabilities, accounting for '-1' values
        for (int i = 0; i < charCount.length; i++) {
            probabilities[i] = (charCount[i] == -1) ? 0 : (double) charCount[i] / totalChars;
        }

        double initialEntropy = calculateEntropy(probabilities);

        
        for (int i = 0; i < charCount.length; i++) {
            if (probabilities[i] == 0) {
                // If probability is 0, information gain is also 0 (the letter does not exist)
                bitsInformationByChar[i] = 10000000;
            } else {
                // Calculating entropy if the letter is absent
                double entropyIfAbsent = calculateEntropyIfAbsent(probabilities, i, totalChars);
                // Expected new entropy when letter is present, simply deduct the term for letter i
                double entropyIfPresent = initialEntropy - probabilities[i] * (Math.log(probabilities[i]) / Math.log(2));
                // Expected new entropy weighted by the probability of letter being present or absent
                double expectedNewEntropy = probabilities[i] * entropyIfPresent + (1 - probabilities[i]) * entropyIfAbsent;
                // Information gain is the reduction in entropy
                
                bitsInformationByChar[i] =   -probabilities[i] * (initialEntropy - expectedNewEntropy);
            }
        }

        for (int i = 0; i < 26; i++){
          System.out.print(bitsInformationByChar[i] + " ");
        }
  }


  private double calculateEntropy(double[] probabilities) {
        double entropy = 0;
        for (double p : probabilities) {
            if (p > 0) {
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }
        return entropy;
    }

    private double calculateEntropyIfAbsent(double[] probabilities, int index, int totalChars) {
        double totalProbabilityMinusCurrent = 0;
        for (int i = 0; i < probabilities.length; i++) {
          if (i != index) {
              totalProbabilityMinusCurrent += probabilities[i];
          }
        }
    
        double entropy = 0;
        if (totalProbabilityMinusCurrent > 0) {  // Ensure the sum is positive before proceeding
          for (int i = 0; i < probabilities.length; i++) {
            if (i != index && probabilities[i] > 0) {
                double adjustedProbability = probabilities[i] / totalProbabilityMinusCurrent;
                if (adjustedProbability > 0) {  // Again, ensure positive before taking log
                    entropy -= adjustedProbability * Math.log(adjustedProbability) / Math.log(2);
                }
            }
          }
        }
      return entropy;
    }


  /// Gets the most probable next letter to guess
  private char findNextLetter() {
    
    double maxValue = this.bitsInformationByChar[0];
    int key = 0;

    for (int i = 1; i < 26; i++) {
      final double info = this.bitsInformationByChar[i];
      // replace `maxValue` and `key` if gotInt is larger than `maxValue`
      if (info < maxValue) {
        maxValue = info;
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
