public class Helpers {
    public static class StringHelper{
        public static String cleanString(String str){
            return str.contains("\"") ? str.replace("\"", "") : str;
        }
    }
}
