
/* @Author: Patrick Manacorda
 * @Date: 2/2/2020
 * @See: Seattle University CPSC 5600 - HW4
 */
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

/* Class Bitonic 
 * -Takes in data to work on and starting and ending indexes
 * -Calling the sort() or thread.run() method on this class sorts this.data in increasing order
 * -Addtional barrier parameter needed for multithreaded execution
 */
public class Bitonic implements Runnable{
    private int[] data;
    private int size;
    final CyclicBarrier barrier;
    private int start;
    private int end;
    
    public Bitonic(int[] data, int start, int end, CyclicBarrier barr){
        this.size = data.length;
        this.data = data;
        if(barr == null) //If single thread then just one waiter
            this.barrier = new CyclicBarrier(1);
        else //Otherwise there are more than one waiter we must synchronize with
            this.barrier = barr;
        this.start = start;
        this.end = end;
    }
    
    public void run(){
        sort();
    }

    /* Sort function
     * -Sorts this.data (int[]) increasing.
     * -Destructive: method modifies this.data directly
     - @var: k  -  describe the stage, there are logN stages
     - @var: j  -  describe the column # within each stage
     - @var: i  -  describe each 'wire' to work on, different threads have 
     -             different starting and index wire indexes.
     */
    public void sort(){
        for(int k=2; k<=size; k*=2){  
            for(int j=k/2; j>0; j/=2){
                //Wait after every column advance
                try{
                    barrier.await();
                }catch(InterruptedException ex){
                    return;
                }catch(BrokenBarrierException ex){return;}
                for(int i=start; i<end; i++){
                    int ixj = i ^ j;
                    if(ixj > i){
                        if( (i & k) == 0 && data[i] > data[ixj] ){
                            int temp = data[ixj];
                            data[ixj] = data[i];
                            data[i] = temp;
                        }
                        if( (i & k) != 0 && data[i] < data[ixj] ){
                            int temp = data[ixj];
                            data[ixj] = data[i];
                            data[i] = temp;
                        }
                    }
                }
            }
        }
    }

}
