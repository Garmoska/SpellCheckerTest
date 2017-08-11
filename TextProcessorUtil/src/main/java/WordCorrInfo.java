public class WordCorrInfo {
    public String correctWord;
    public String corruptedWord;
    public int SentenceIndex;

    public WordCorrInfo(){
        correctWord = corruptedWord = "";
        SentenceIndex = -1;
    }
}
