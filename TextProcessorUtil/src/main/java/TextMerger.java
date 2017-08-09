import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;


public class TextMerger {

    final static String empty = "";

    public static void main(String [] args) throws Exception {
        if (args.length != 2) throw new Exception("Path to cache directory and output path must be set");
        final String cacheDirectory = cleanString(args[0]);
        if (Files.notExists(Paths.get(cacheDirectory))) throw new Exception(String.format("Directory %s is not exists", cacheDirectory));
        File cachedFolder = new File(cacheDirectory);
        final String outputDirectory = cleanString(args[1]);
        if (Files.notExists(Paths.get(outputDirectory))) throw new Exception(String.format("Directory %s is not exists", outputDirectory));

        FilenameFilter textFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] listOfFiles = cachedFolder.listFiles(textFilter);
        if (listOfFiles == null || listOfFiles.length == 0){
            System.out.println("Cache directory doesn't contain text files");
            return;
        }
        else
        {
            System.out.println(String.format("Found %s cached files", listOfFiles.length));
        }

        List<List<HasWord>> tokenList = new ArrayList<>();
        for (File f:  listOfFiles) {
            DocumentPreprocessor dp = new DocumentPreprocessor(f.getPath());
            for (List<HasWord> sentence : dp) {
                tokenList.add(sentence);
            }
        }

        //
        List<String> sentences = new ArrayList<>();
        for (List<HasWord> tokenSet: tokenList) {
            final String s = combineWords(tokenSet);
            if (s.trim().length() > 0) sentences.add(s);
        }

        System.out.println(String.format("Number of sentences (total): %s", sentences.size()));

        //1. Export to a file "as is"
        Path originalFile = Paths.get(outputDirectory, "sentences_original.txt");
        try(PrintWriter out = new PrintWriter(originalFile.toString())){
            for (String s: sentences) {
                out.println(s);
            }
        }
        System.out.println(String.format("Sentences original: %s", originalFile));

        //2. without punctuation marks
        Path withoutPunctMarksFile = Paths.get(outputDirectory, "sentences_without_punctuation_marks.txt");
        try(PrintWriter out = new PrintWriter(withoutPunctMarksFile.toString())){
            for (String s: sentences) {
                out.println(s.replace(",", empty).replace("-", empty));
            }
        }
        System.out.println(String.format("Sentences without punctuation marks: %s", withoutPunctMarksFile));

    }

    private static String cleanString(String str){
        return str.contains("\"") ? str.replace("\"", "") : str;
    }

    private static String combineWords(List<HasWord> words){
        StringBuilder sb = new StringBuilder();
        final String space = " ";
        for (HasWord word: words) {
            String wordStr;
            switch (word.word()){
                case "-LRB-":
                    wordStr = "(";
                break;
                case "-RRB-":
                    wordStr = ")";
                    break;
                case "-LSB-":
                    wordStr = "[";
                    break;
                case "-RSB-":
                    wordStr = "]";
                    break;
                default:
                    wordStr =word.word();
                    break;
            }
            sb.append(wordStr);
            sb.append(space);
        }
        //find references like [9]
        boolean canExit = false;
        while(!canExit){
            int index1 = sb.toString().indexOf("[");
            int index2 = sb.toString().indexOf("]");
            if (index1 == -1 || index2 == -1 || (index1 > index2)) {
                canExit = true;
            }
            else
            {
                sb = sb.replace(index1, index2 + 1, empty);
                /*
                String s = sb.toString();
                try{
                    sb = sb.replace(index1, index2 + 1, empty);
                }
                catch(Exception exc){
                    System.out.println(s);
                }
                */
            }
        }
        return sb.toString();
    }


}
