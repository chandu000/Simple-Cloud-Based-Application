package client;

import java.util.Arrays;
import java.util.Scanner;

public class Starter {

    public static void main(String[] args) {
        boolean flag = true;
        Scanner sc = new Scanner(System.in);
        StartSync startsync = new StartSync();
        while(flag) {
            System.out.println("application started");
            System.out.println("Select among the below");
            System.out.println("1. Start Sync");
            System.out.println("2. Suspend Sync");
            System.out.println("3. Resume Sync");
            for (String s : Arrays.asList("4. Exit", "Enter the input")) {
                System.out.println(s);
            }
            int num = sc.nextInt();
            if(num == 1) {
                startsync.starttask1();
                startsync.starttask2();
            }
            else if (num == 2)
            {
                System.out.println(" \n Suspended Transfer");
                try {
                        System.exit(0);
                        startsync.wait();
                }
                catch (InterruptedException e1) {
                                                 // TODO Auto-generated catch block
                                                 e1.printStackTrace();
                                             }
                                         }
            else if(num == 3)
            {
                        System.out.println(" \n Transfering Remaining Data");
                        startsync.starttask1();
                        startsync.starttask2();
//				}
            }
            else if(num == 5) {
                flag = false;
            }
        }
        sc.close();
        System.out.println("closed");
    }
}

