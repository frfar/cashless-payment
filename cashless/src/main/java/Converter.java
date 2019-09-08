public class Converter {

    //Reads stream output from keypad, outputs two digit hex number to be passed into convert(input)
    /*String stream =
    001:006:000:STREAM    1567721034.695277
    00 00 XX 00 00 00 00 00
          ^ "XX" portion is output
     */

    public String parse(String stream){
        String[] streamarr = stream.split(" ");
        return streamarr[3];
    }


    //input = String from parse(stream), from stream output of keypad
    //returns number that was pressed on keypad, returns -1 for "Enter"
    //returns 0 for unknown key

    public int convert(String input) {
        switch (input) {
            case "62":
                return 0;
            case "59":
                return 1;
            case "5A":
                return 2;
            case "5B":
                return 3;
            case "5C":
                return 4;
            case "5D":
                return 5;
            case "5E":
                return 6;
            case "5F":
                return 7;
            case "60":
                return 8;
            case "61":
                return 9;
            case "58":
                return -1;
            default:
                return 0;
        }
    }

}