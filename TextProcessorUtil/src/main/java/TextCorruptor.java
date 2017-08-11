import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextCorruptor {

    private final static String empty = "";
    private final static String space = " ";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new Exception("Full path fo a file and output directory must be set");
        final String sourceFile = Helpers.StringHelper.cleanString(args[0]);
        if (Files.notExists(Paths.get(sourceFile))) throw new Exception(String.format("Source File %s is not exists", sourceFile));
        final String outputDirectory = Helpers.StringHelper.cleanString(args[1]);
        if (Files.notExists(Paths.get(outputDirectory))) throw new Exception(String.format("Directory %s is not exists", outputDirectory));

        System.out.println(String.format("Processing file: %s", sourceFile));
        System.out.println(String.format("Output folder: %s", outputDirectory));

        DocumentPreprocessor dp = new DocumentPreprocessor(sourceFile);
        List<WordCorrInfo> tracking = new ArrayList<>();
        ArrayList<String> corruptedSentences = new ArrayList<>();
        int sentIndex = -1;
        Random rand = new Random();
        for (List<HasWord> sentence : dp) {
            sentIndex++;
            //System.out.println(sentence);
            StringBuilder sbSent = new StringBuilder();
            for (HasWord wrd:  sentence) {
                String wordCorr = empty;
                final String word = wrd.word();
                if (word.length() < 5
                      ||  word.equals("-LRB-")
                      ||  word.equals("-RRB-")
                        ) {
                    if (word.equals("-LRB-")){
                        wordCorr = "(";
                    }
                    else if (word.equals("-RRB-")){
                        wordCorr = ")";
                    }
                    else
                    {
                        wordCorr = word;
                    }
                }
                else {
                    //Types of corruption: swap letters, miss one letter.
                    int typeOfCorruption = rand.nextInt(3);
                    switch (typeOfCorruption) {
                        case 0:
                            //don't corrupt (leave the word as is)
                            wordCorr = word;
                            break;
                        case 1:
                            int firstLetterIndex = rand.nextInt(word.length() - 2); //from the first letter till preultimate.
                            int secondLetterIndex = firstLetterIndex + 1;
                            final char ch1 = word.charAt(firstLetterIndex);
                            final char ch2 = word.charAt(secondLetterIndex);
                            wordCorr = word.substring(0, firstLetterIndex) +
                                    ch2 +
                                    ch1 +
                                    word.substring(secondLetterIndex + 1);
                            break;
                        case 2:
                            int missedLetterIndex = rand.nextInt(word.length() - 1);
                            wordCorr = word.substring(0, missedLetterIndex) +
                                    word.substring(missedLetterIndex + 1);
                            break;
                    }
                }
                WordCorrInfo item = new WordCorrInfo();
                item.correctWord = word;
                item.corruptedWord = wordCorr;
                item.SentenceIndex = sentIndex;
                tracking.add(item);
                sbSent.append(wordCorr);
                sbSent.append(space);
            }
            corruptedSentences.add(sbSent.toString());
        }

        System.out.println(String.format("Generated %s corrupted sentences.", corruptedSentences.size()));

        System.out.println("Preparing tracking information. Please wait...");
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        final String json = gson.toJson(tracking);
        System.out.println("Done.");

        //save corrupted file and tracking information
        Path corruptedFile = Paths.get(outputDirectory, "sentences_corrupted.txt");
        Path trackingFile = Paths.get(outputDirectory, "sentences_tracking.json");

        try(PrintWriter out = new PrintWriter(corruptedFile.toString())){
            for (String s: corruptedSentences) {
                out.println(s);
            }
        }
        System.out.println(String.format("Corrupted Sentences saved: %s", corruptedFile));

        try(PrintWriter out = new PrintWriter(trackingFile.toString())){
                out.println(json);
        }
        System.out.println(String.format("Tracking information saved: %s", trackingFile));
    }
}
