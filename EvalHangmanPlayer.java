import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.lang.management.*;

/*

  Author: Taher Patanwala
  Email: tpatanwala2016@my.fit.edu
  Pseudocode: Philip Chan

  Usage: EvalHangmanPlayer wordFile hiddenWordFile 

  Description:

  The goal is to evaluate HangmanPlayer.  For each hidden word in the
  hiddenWordFile, the program asks HangmanPlayer for guesses for
  letters in the hidden word.  As in the Hangman game, the maximum
  number of incorrect guesses is 6 (body parts).  HangmanPlayer is
  provided with a list of known English words in wordFile for
  initialization.

  The performance of HangmanPlayer is measured by:

  a.  accuracy: average accuracy over the hidden words;
           the accuracy of each hidden words is 
	        [ 1 - (number of incorrect guesses/6) ] * 100%.
  b.  speed: average time to guess a letter
  c.  space consumption: memory consumption
  d.  score--(accuracy * accuracy)/sqrt(time * memory)  


  --------Pseudocode for evaluating HangmanPlayer---------------

     HangmanPlayer player = new HangmanPlayer(wordFile) // a list of English words

     while not end of hiddenWordFile
        read a hiddenWord
	currentWord = blank word with the same length as hiddenWord

	while not maxGuesses of 6 is not reached and currentWord is not hiddenWord
	   correctGuess = false
           guess = player.guess(currenWord) 
	   if guess is in hiddenWord
              correctGuess = true
	      update currentWord
	   player.feedback(correctGuess, currentWord)

     report performance (accuracy, cpu time, memory, ATM score)
 */

public class EvalHangmanPlayer
{
    public static void main(String[]args) throws IOException{

	if (args.length != 2) 
        {
            System.err.println("Usage: EvalHangmanPlayer wordFile hiddenWordFile");
            System.exit(-1);
        }

	// for getting cpu time
	ThreadMXBean bean = ManagementFactory.getThreadMXBean();        
	if (!bean.isCurrentThreadCpuTimeSupported())
	    {
		System.err.println("cpu time not supported, use wall-clock time:");
                System.err.println("Use System.nanoTime() instead of bean.getCurrentThreadCpuTime()");
		System.exit(-1);
	    }
	    
        //Preprocessing in HangmanPlayer
	System.out.println("Preprocessing/Initialization in HangmanPlayer...");
        long startPreProcTime = bean.getCurrentThreadCpuTime();
        HangmanPlayer player = new HangmanPlayer(args[0]);
        long endPreProcTime = bean.getCurrentThreadCpuTime();
        
        //Stop if program runs for more than 3 minutes.
        double processingTimeInSec = (endPreProcTime - startPreProcTime)/1E9;
        if(processingTimeInSec > 180){
            System.err.println("Preprocessing time \""+ processingTimeInSec +" sec\" is too long...");
            System.exit(-1);
        }
            
	// report time and memory spent on preprocessing
        DecimalFormat df = new DecimalFormat("0.####E0");
	System.out.println("Preprocessing in seconds: "  + 
			   df.format(processingTimeInSec));
        Runtime runtime = Runtime.getRuntime();
	runtime.gc();
        System.out.println("Used memory after preprocessing in bytes: " +
			   df.format((double) peakMemoryUsage() ));

        FileReader hiddenWordFile = new FileReader(args[1]);
        BufferedReader input = new BufferedReader(hiddenWordFile);
        
        double totalWords = 0.0;
        double totalGuessess = 0.0;
        double totalElapsedTime = 0.0;
        String hiddenWord;
	
	System.out.println("Evaluation starts--HangmanPlayer is guessing...");
        //Perform operations for each line in the file
        double accuracySum = 0.0;
        while( (hiddenWord = input.readLine()) != null){
            //Read a word from the hidden test file
            hiddenWord = hiddenWord.trim().toLowerCase();
            //Count the total hidden words
            totalWords++;
            //Create an empty string as the same size as the hidden word
            StringBuilder wordWithGuessedLetters = new StringBuilder(hiddenWord.length());
            //Fill the empty string with spaces
            for(int i=0;i<hiddenWord.length();i++){
                wordWithGuessedLetters.append(" ");
            }
            //To indicate that your program now has to guess a new hidden word
            boolean newWord = true;
            //To count the number of incorrect guesses
            int numIncorrectGuesses = 0;
            boolean correctGuess = false;
            //While correct word is not guessed and the number of incorrect guesses is less than 6,
            //Your program will keep trying to guess
            while(numIncorrectGuesses < 6 && !correctGuess){
                totalGuessess++;
                //Record start time of the guess
                long startTime = bean.getCurrentThreadCpuTime();
                //Pass the partial word to the HangmanPlayer program to guess a letter
                char guessedLetter = Character.toLowerCase(player.guess(wordWithGuessedLetters.toString(), newWord));
                //To calculate the time taken for each guess operation
                long endTime = bean.getCurrentThreadCpuTime();
                totalElapsedTime = totalElapsedTime + (endTime - startTime);

                newWord = false;
                int i=0;
                //Check if guessLetter was not guessed before
                if(wordWithGuessedLetters.indexOf(String.valueOf(guessedLetter)) == -1){
                    //Try to see if the guessed letter is correct, 
                    //If Yes, then find the first position of the letter in the word
                    while(i<hiddenWord.length() && hiddenWord.charAt(i) != guessedLetter){
                        i++;
                    }
                }
                else{
                    i = hiddenWord.length();  //incorrect if the letter was guessed before
                }
                //This means that the guess was incorrect
                if(i == hiddenWord.length()){
                    //Increment the number of incorrect guesses
                    numIncorrectGuesses++;
                    //Calculate the time taken to process the feedback
                    startTime = bean.getCurrentThreadCpuTime();
                    //Send feedback that the guess was wrong
                    player.feedback(false, wordWithGuessedLetters.toString());
		    endTime = bean.getCurrentThreadCpuTime();
		    totalElapsedTime = totalElapsedTime + (endTime - startTime);
                }
                //This means that the guess was correct
                else{
                    //Find other positions of the guessed character in the hidden word
                    while(i<hiddenWord.length()){
                        //The guessed letter is revealed in its correct positions in the word
                        if(hiddenWord.charAt(i) == guessedLetter){
                            wordWithGuessedLetters.setCharAt(i, guessedLetter);
                        }
                        i++;
                    }
                    
                    //If all letters are guessed of the hidden word, then proceed with the next hidden word
                    if(wordWithGuessedLetters.indexOf(" ") == -1)
                        correctGuess = true;
                    //Calculate the time taken to process the feedback
                    startTime = bean.getCurrentThreadCpuTime();
                    //Send feedback that correct letter was guessed
                    player.feedback(true, wordWithGuessedLetters.toString());
		    endTime = bean.getCurrentThreadCpuTime();
		    totalElapsedTime = totalElapsedTime + (endTime - startTime);
                }
            }
            //Total accuracy is calculated before moving to the next hidden word
            accuracySum += (1.0 - numIncorrectGuesses/6.0);
        }
	input.close();
	
        //Calculate the accuracy
        double avgAccuracy = (accuracySum * 100.0) / totalWords;
        System.out.printf("Accuracy: %.4f\n",avgAccuracy);
        
	if (totalElapsedTime <= 0) // too small to measure, unlikely
	   {
	       System.err.println("Zero time usage was reported; please rerun on code01.fit.edu");
	       System.exit(-1);
	       //totalElapsedTime = 1;
	   }

       //Convert elapsed time into seconds, and calculate the Average time
        double avgTime = (totalElapsedTime/1.0E9)/totalGuessess;
        
        //To format the Average time upto 4 decimal places.
        //DecimalFormat df = new DecimalFormat("0.####E0"); // moved to near initialization
        System.out.println("CPU time per guess in seconds: " + df.format(avgTime));
        
        // Calculate the used memory
        long memory = peakMemoryUsage();	
	if (memory <= 0) // too small to measure, highly unlikely
	   {
	       System.err.println("Zero memory usage was reported; please rerun on code01.fit.edu");
               System.exit(-1);
	       //memory = 1;
	   }
        System.out.println("Used memory in bytes: " +
			   df.format((double)memory));
        //OverAll Score
        System.out.printf("Score: %.4f\n",(avgAccuracy * avgAccuracy)/Math.sqrt(avgTime * memory));

	HangmanPlayer player2 = player;  // keep player used to avoid garbage collection of player
    }

    
    /*
     * return peak memory usage in bytes
     *
     * adapted from

     * https://stackoverflow.com/questions/34624892/how-to-measure-peak-heap-memory-usage-in-java 
     */
    private static long peakMemoryUsage() 
    {

    List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
    long total = 0;
    for (MemoryPoolMXBean memoryPoolMXBean : pools)
        {
        if (memoryPoolMXBean.getType() == MemoryType.HEAP)
        {
            long peakUsage = memoryPoolMXBean.getPeakUsage().getUsed();
            // System.out.println("Peak used for: " + memoryPoolMXBean.getName() + " is: " + peakUsage);
            total = total + peakUsage;
        }
        }

    return total;
    }

}
