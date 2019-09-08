public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Cashless Payment System!");

        Converter test = new Converter();
        String testStream = "001:006:000:STREAM 1567721034.695277" +
                "00 00 60 00 00 00 00 00";
        String out = test.parse(testStream);
        int output = test.convert(out);
        System.out.println(output);


    }

}