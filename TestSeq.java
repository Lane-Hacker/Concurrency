/* @Author: Patrick Manacorda
 * @Date: 2/2/2020
 * @See: Seattle University CPSC 5600 - HW4
 */

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/* Class TestSeq
 * -Sequential version of the bitonic sort loop program
 */
public class TestSeq{
    public static final int ORDER = 22;
    public static final  int N = 1 << ORDER;
    public static final int TIME_ALLOWED = 10;
    public static void main(String args[]){
        long start = System.currentTimeMillis();
        int work = 0;
        Random rd = new Random();
        while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000){
            boolean sorted = false;
            int[] arr = new int[N];
            for(int i=0; i<N; i++)
                arr[i] = rd.nextInt();
            Bitonic obj = new Bitonic(arr,0,arr.length,null);
            obj.sort();
            int last = arr[0];
            for(int i=1; i < N; i++){
                if(arr[i] < last){
                    System.out.println(last + ":" + arr[i]);
                    sorted = false;
                    break;
                }
                sorted = true;
                last = arr[i];
            }
            if(!sorted){
                System.out.println("FAILED!");
            }else{
                work++;
            }
        }
        System.out.println("Sorted " + work + " arrays(ints) in " + TIME_ALLOWED + " seconds");
    }
}
