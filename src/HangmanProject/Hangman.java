package HangmanProject;

/**
@author Арсений
@Date 31.01.2025
                                            КОММЕНТАРИИ К КОДУ
           1) В методе inputCheck был костыль (77 строка) - при вводе неправильного символа постоянно будет
            надпись некорректного ввода, так как Scanner останавливается на этом символе и постоянно
            его считывает. В общей сложности есть проблемы со вводом символов userInput и inputCheck.
            Метод inputCheck переписан так, чтобы он возвращал boolean и проверка в цикле do while выполнялась
            корректно
           2) В методе wordChoosing не был закрыт поток BufferReader (исправлено с помощью try-with-resources)
           3) Нужно каждый раз выводить состояние слова (обновлять массив символов на тот, в котором буквы открыты)
           4) Сделать так, чтобы вне зависимости от регистра введённая буква всплывала в слове (опционально)
                ^^единственный недочёт кода^^
           5) В методе showLetters for each заменён на String.join для улучшения эффективности. Для вывода состояния
              слова в методе showLetters массив hiddenWord каждый раз обновляется и выводится в методе userInput.
                    Все самые явные недостатки и костыли исправлены.
           P.S. По-моему этот код доведён до идеала (сильно сказано)... (21:21 31.01.2025) (01.02 много static методов...)
                Дополнение от 14:01 01.02.2025 : стоит как-нибудь, позднее, переписать этот код на ООП лад, а также
                    дописать логику с выбором уровня сложности.

 */
import java.io.BufferedReader; //работа с файлом для случайного выбора слова
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Hangman {
    private static String wordToGuess;
    private static String[] hiddenWord;
    private static Integer MISTAKES_COUNTER;
    private static final Integer MAX_ATTEMPTS = 6;
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Character> userInputSymbols = new ArrayList<>(); //Замена на Set для упрощения (опционально)
    private static final List<String> wordList = new ArrayList<>();


    public  static void main(String[] args) throws IOException {
        System.out.println("""
                             \t\tДля начала новой игры введите 1.
                             \tДля выхода введите любой символ кроме 1.""");
        do {
            userInputSymbols.clear();
            MISTAKES_COUNTER = 0;
            startGame();
            System.out.println("""
                    ----------------Игра окончена!----------------
                    \t\tВведите 1 для повторной игры.
                    \tДля выхода введите любой символ кроме 1.""");
            scanner.nextLine();
        } while (scanner.nextLine().matches("^1$"));
    }
    public  static void startGame() throws IOException {
        System.out.println("----------------Начнём!----------------");
        wordChoosing();
        hideWord();
        do {
            userInput();
            if (personDies(MISTAKES_COUNTER)) {
                System.out.println("""
                                    -----------------Вы проиграли!----------------
                                    \t\tЗагаданным словом было:""" + wordToGuess);
                break;
            }
            if(userWins()){
                System.out.println("""
                                     ----------------Вы победили!---------------
                                     \t\tЗагаданным словом было: """ + wordToGuess);
                break;
            }
        }while(true);
    }
    public  static void wordChoosing() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("src/HangmanProject/words.txt"))){
            String line;
            while ((line = br.readLine()) != null) {
                wordList.add(line.trim());
            }
        }
        wordToGuess = wordList.get((int)(Math.random() * wordList.size()));
        hiddenWord = new String[wordToGuess.length()];
    }
    public  static void hideWord(){
        Arrays.fill(hiddenWord, "__"); //for заменён на fill
        for (String letter: hiddenWord){
            System.out.print(letter + " ");
        }
        System.out.println();
    }
    public  static void userInput(){
        Character yourInput;
        do {
            System.out.print("Введите букву от а до я: ");
            yourInput = scanner.next().charAt(0);
           }while (!inputCheck(yourInput));
        if (checkInputCharacterInWord(yourInput)){
            showLetters(yourInput, hiddenWord);
        }else {
            printHangman();
        }
        printCurrentStatus();
    }
    public  static boolean inputCheck(Character letter){
        if (!String.valueOf(letter).matches("^[А-Яа-я]$")){
            System.out.println("Пожалуйста, проверьте корректность ввода и повторите попытку!\n");
            // userInput(); //костыль, при вводе неправильного символа сканер постоянно его считывает!
            return false;
        }
        if (!enteredLetters(letter)){ // можно занести в верхний while (опционально)
            return false;             //
        }
//        if (wordToGuess.contains(String.valueOf(letter))){
//                    showLetters(letter, hiddenWord);              ИСПРАВЛЕНО
//        }else {
//            printGallows();
        return true;
    } // Использовал regex как наиболее эффективный вариант проверки ввода
    public  static boolean checkInputCharacterInWord(Character input){
        return wordToGuess.toLowerCase().contains(String.valueOf(input).toLowerCase());
    }
    public  static void showLetters(Character digit, String[] openedWord){
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.toUpperCase().charAt(i) == Character.toUpperCase(digit)) { //проверка регистра  wordToGuess.charAt(i) == digit
                if (i == 0) {
                    openedWord[i] = String.valueOf(digit).toUpperCase();
                } else openedWord[i] = String.valueOf(Character.toLowerCase(digit));
            }
//        for (String symbol: openedWord){
//            System.out.print(symbol + " ");
//        }
//        System.out.println();
//        System.out.println(String.join(" ", openedWord));
        }
    }
    public  static void printCurrentStatus(){
        System.out.println("Введённые символы: " + userInputSymbols);
        System.out.println("Текущее состояние слова: " + String.join(" ", hiddenWord));
        System.out.printf("Текущее количество ошибок %d/%d %n", MISTAKES_COUNTER, MAX_ATTEMPTS);
    }
    public  static boolean enteredLetters(Character symbol){
        if (userInputSymbols.contains(symbol) || userInputSymbols.contains(Character.toLowerCase(symbol))){
            System.out.println("Данный символ уже был введён, повторите ввод!");
            return false;
        }else userInputSymbols.add(Character.toLowerCase(symbol));
        Collections.sort(userInputSymbols);
        // System.out.println("Введённые символы: " + userInputSymbols);
        return  true;

    }
    public  static void printHangman() {
        MISTAKES_COUNTER++;
        switch (MISTAKES_COUNTER) {
            case 1 ->
                    System.out.print("\n----*------------]\n\t|\t\t\t |\n\t|\t\t\t \n\t|\t\t\t\n\t|\t\t\t\n\t|\n___/|\\___\t\n\n");
            case 2 ->
                    System.out.print("\n----*------------]\n\t|\t\t\t |\n\t|\t\t\t 0\n\t|\t\t\t\n\t|\t\t\t\n\t|\n___/|\\___\t\n\n");
            case 3 ->
                    System.out.print("\n----*------------]\n\t|\t\t\t |\n\t|\t\t\t 0\n\t|\t\t\t/|\n\t|\t\t\t\n\t|\n___/|\\___\t\n\n");
            case 4 ->
                    System.out.print("\n----*------------]\n\t|\t\t\t |\n\t|\t\t\t 0\n\t|\t\t\t/|\\\n\t|\t\t\t\n\t|\n___/|\\___\t\n\n");
            case 5 ->
                    System.out.print("\n----*------------]\n\t|\t\t\t |\n\t|\t\t\t 0\n\t|\t\t\t/|\\\n\t|\t\t\t/\n\t|\n___/|\\___\t\n\n");
            case 6 ->
                    System.out.print("\n----*------------]\n\t|\t\t\t |\n\t|\t\t\t 0\n\t|\t\t\t/|\\\n\t|\t\t\t/ \\\n\t|\n___/|\\___\t\n\n");
        }
    }
    public  static boolean  userWins (){
        for (String symbol: hiddenWord){
            if (symbol.equals("__")) {
                return false;               //return Arrays.stream(hiddenWord).noneMatch(symbol -> symbol.equals("__"));
            }                                                   // Улучшенный вариант
        }
        return true;
    }
    public  static boolean personDies (Integer counter){

        return Objects.equals(counter, MAX_ATTEMPTS);
    }
}
