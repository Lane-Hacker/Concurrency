/* @Author: Patrick Manacorda
 * @Date: 2/2/2020
 * @See; Seattle University CPSC 5600 
 */

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/* Class TestParall
 * -Tests how many arrays of ints of size N=2^22 can be sorted in 10 seconds
 *@BenchMark: mean of 27 arrays of ints size 2^22 sorted in 10 seconds with 16 threads
 * -Dispaches thread pool manager (Executor) and submit jobs to different Bitonic type objects with different starting and ending 'wire' indexes
 */
public class TestParall{
    public static final int ORDER = 22;
    public static final int N = 1 << ORDER;
    public static final int TIME_ALLOWED = 10; //seconds
    public static final int N_THREADS = 16; 
    private static final ExecutorService pool = Executors.newFixedThreadPool(N_THREADS); //Thread manager
    public static void main(String args[]){
        int work = 0;
        Random rd = new Random();
        CyclicBarrier barrier = new CyclicBarrier(N_THREADS); //Used to synchronize the threads
        int piece = N/N_THREADS;  //Piece that each thread will be working on - same as number of wires per thread
        List<Future> futures = new ArrayList<Future>(); //Used to wait to wait until all threads finish
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000){
            //************GENERATE ARRAY "arr" OF TYPE <int> OF SIZE N, random values
            int[] arr = new int[N];
            for(int i=0; i<N; i++)
                arr[i] = rd.nextInt();
          
            
            //***********SUBMIT EXECUTABLE(void run()) JOB TO THREAD EXECUTOR "pool", N_THREADS jobs total
            //-We add jobs to the List<future> List because we want to wait until all thread return before proceeding
            //-Each job sorts the array via the Bitonic::run()->sort() call
            for(int i=0; i<N_THREADS; i++)
                futures.add(pool.submit(new Bitonic(arr, i*piece, i*piece+piece, barrier)));
            for(Future f : futures){
                try{
                    f.get(); //wait until all Bitonic::run() return calls are made
                }catch(InterruptedException ex){return;}
                catch(ExecutionException ex){return;}
            }

            //***** Below is checking array "arr" is indeed sorted 
            boolean sorted = false;
            if(arr.length != N){
                System.out.println("Something went wrong: size of return array not equal size of input array");
                return;
            }
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
        pool.shutdown();
    }
}
