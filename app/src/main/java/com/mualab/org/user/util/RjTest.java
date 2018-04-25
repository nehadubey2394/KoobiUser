package com.mualab.org.user.util;

public class RjTest {

    public static void main(String args[]){
        System.out.println(getVerticallyIncertedNumbers(0, 1000));
    }

    private static String getVerticallyIncertedNumbers(int from, int to){

        StringBuilder finalString = new StringBuilder();
        int count = 0;

        for(int i = from; i<to; i++ ){

            String y = ""+i;
            if(y.contains("2") || y.contains("3") || y.contains("4") || y.contains("5") || y.contains("7"))
                continue;

            int reversed_No = getReversedNumber(i);
            y = reversed_No + "";
            StringBuilder newString  = new StringBuilder("");

            for(int j=0; j<y.length(); j++){
                char ch = y.charAt(j);
                if(ch=='6')
                    ch='9';
                else if(ch=='9')
                    ch='6';
                newString.append(ch);
            }

            int convertedNo = (Integer.parseInt(newString.toString()));

            if(convertedNo==i){
                count++;
                finalString.append(convertedNo).append(",");
            }

        }

        System.out.println(""+count);
        return finalString.toString();
    }

    private static int getReversedNumber(int num) {
       int reversed = 0;
        while(num != 0) {
            int digit = num % 10;
            reversed = reversed * 10 + digit;
            num /= 10;
        }
        return reversed;
    }
}
