package org.example.student;

public class Main {
    public static void main(String[] args) {
        int n = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                n = n + j * i;
//                System.out.print("n++ ");
//                sleep();
//                System.out.printf("%d*%d=%2d  ", i, j, i * j);
//                sleep();
            }
//            System.out.println();
//            sleep();
        }
        System.out.println(n);

        System.out.println(n);


//        printChengFaBiao2();
//        printChengFaBiao3();
//        printxinhao(2);
//        System.out.printf("%d*%d=%2d\n", 2, 3, 6);
//        System.out.printf("%d*%d=%d", 2, 3, 6);
//        print(3, 4);
//        System.out.print("xxx");
//        System.out.println("xxx");
//        System.out.printf("\n");
//        printxinhao(4);
//        printxinhao(10);
    }


    //
//*
//
// *
//***
//
//  *
// ***
//*****
//
    static void printxinhao(int rows) {
        for (int i = 1; i <= rows; i++) {
            for (int k = 1; k <= rows - i; k++) {
                System.out.print(" ");
            }
            for (int j = 1; j <= 2 * i - 1; j++) {
                System.out.print("*");
                sleep();
            }
            System.out.println();
            sleep();
        }
    }

    static void printChengFaBiao() {
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                System.out.printf("%d*%d=%-2d   ", j, i, i * j);
            }
            System.out.println();
        }
    }

    static void printChengFaBiao3() {
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                if (i <= j) {
                    System.out.printf("%d*%d=%-2d   ", j, i, i * j);
                } else {
                    System.out.printf("%9s", " ");
                }
                sleep();
            }
            System.out.println();
            sleep();
        }
    }
    /*
1*1=1    2*1=2    3*1=3    4*1=4    5*1=5    6*1=6    7*1=7    8*1=8    9*1=9
1*2=2    2*2=4    3*2=6    4*2=8    5*2=10   6*2=12   7*2=14   8*2=16   9*2=18
1*3=3    2*3=6    3*3=9    4*3=12   5*3=15   6*3=18   7*3=21   8*3=24   9*3=27
1*4=4    2*4=8    3*4=12   4*4=16   5*4=20   6*4=24   7*4=28   8*4=32   9*4=36
1*5=5    2*5=10   3*5=15   4*5=20   5*5=25   6*5=30   7*5=35   8*5=40   9*5=45
1*6=6    2*6=12   3*6=18   4*6=24   5*6=30   6*6=36   7*6=42   8*6=48   9*6=54
1*7=7    2*7=14   3*7=21   4*7=28   5*7=35   6*7=42   7*7=49   8*7=56   9*7=63
1*8=8    2*8=16   3*8=24   4*8=32   5*8=40   6*8=48   7*8=56   8*8=64   9*8=72
1*9=9    2*9=18   3*9=27   4*9=36   5*9=45   6*9=54   7*9=63   8*9=72   9*9=81
     */

    static void printChengFaBiao2() {
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                System.out.printf("%d*%d=%-2d   ", j, i, i * j);
                sleep();
            }
            System.out.println();
            sleep();

        }
    }

    static void sleep() {
        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //用#打印一个 rows 行、 columns 列的图形
    static void print(int rows, int columns) {
        /*
          用两层 for 打印二维图形（多行多列的数据）
          外层 for 控制行数
          内层 for 控制列数
         */
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print("#");
            }
            System.out.print("xxx\n");
            System.out.println("xxx");//换行
//            System.out.printf("\n");
        }
    }




    /*
#####
#####
#####
     */
//
//    static void printYinShu(int k) {
//        if (k == 1) {
//            return;
//        }
//        int t = getMinYinShu(k);
//        System.out.print(t + " ");
//        printYinShu(k / t);
//    }
//
//    static void printYiShu2(int k) {
//        while (k != 1) {
//            int t = getMinYinShu(k);
//            System.out.print(t + " ");
//            k = k / t;
//        }
//    }
//
//    static int getMinYinShu(int n) {
//        for (int i = 2; i <= n; i++) {
//            if (n % i == 0) {
//                return i;
//            }
//        }
//        return n;
//    }
}
