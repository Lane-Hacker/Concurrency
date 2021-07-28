/*
* @Author: Patrick Manacorda
* @Date: 2/12/2020
* @See: Seattle University - CPSC 5600 HW5
*/
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

/*
* General Scan/Reduce class - takes in a list of raw data elements
* and it computes the reduction and the scan of the elements.
* Implemented using BlockingQueue, ForkJoinPool, and the Schwartz tight loop method
* What to look for HW6:
* -Interior node values saved to interior List<TallyType>
* -Scan reads values from interior and applies a decay (look at the fade function)
*/
public class GenScan<ElemType, TallyType extends Tally<ElemType>> {
	private List<ElemType> data; //raw data
	private int n; //size of data
	private int threadP; //Number of threads
	private TallyType factory; //Factory Object
	private List<TallyType> result; //Scan result
	private List<TallyType> interior; // Interior values
	private Object[] nodeValue, nodeUp, nodeLeft, nodeDown;
	private ForkJoinPool pool;
	boolean reduced;
	/*
	* Constructor for GenScan
	* @param data - list of ElemType 
	* @param threadP - number of threads to use
	* @param factory - template for Tally Objects
	*/
	public GenScan(List<ElemType> data, int threadP, TallyType factory){
		if(!(threadP > 0 && ( (threadP & (threadP - 1) ) == 0) ))
			throw new IllegalArgumentException("ThreadP must be a power of 2 (for now)");	
		this.data = data;
		this.n = data.size();
		this.interior = new ArrayList<>(n);
		this.threadP = threadP;
		this.reduced = false;
		this.factory = factory;
		this.pool = new ForkJoinPool(threadP);
		this.nodeValue = new Object[threadP];
		this.nodeUp = new Object[threadP];
		this.nodeLeft = new Object[threadP];
		this.nodeDown = new Object[threadP];
		for(int i = 0; i < threadP; i++){
			nodeValue[i] = new ArrayBlockingQueue<TallyType>(1);
			nodeUp[i] = new ArrayBlockingQueue<TallyType>(1);
			nodeLeft[i] = new ArrayBlockingQueue<TallyType>(1);
			nodeDown[i] = new ArrayBlockingQueue<TallyType>(1);
		}
	}
	
	public TallyType reduce() {
		try{
			pool.submit(new reduceTask(0,0,n));
			this.reduced = true;
			return getNode(0);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public List<TallyType> scan() {
		if(!reduced){
			this.reduce();
		}
		this.result = new ArrayList<TallyType>(n);
		for(int i=0; i<n; i++)
			result.add(null);

		pool.invoke(new scanTask(0,0,n));
		return result;
	}
	
	/*
	* @Class reduceTask - this is the nested class run by the reduce threads
	*/
	class reduceTask extends RecursiveAction{
		private int start;
		private int end;
		private int index;
		private int size;
		
		public reduceTask(int index, int start, int end){
			this.start = start;
			this.end = end;
			this.index = index;
			this.size = n/threadP;
		}
		
		@Override
		public void compute(){
			if( (end-start) > n/threadP ){ //split
				List<reduceTask> subtasks = createSubtasks();
				for(reduceTask tsk : subtasks)
					tsk.fork();
			}else{ //compute from my start to my end
				try {
				/*
				 * Calculate this thread's portion of the data reduction. This is the Scwarz
				 * tight loop.
				 */
				TallyType tally = newTally();
				for (int i = start; i < end; i++){
					tally.accum(data.get(i));
					interior.set(i, tally);
				}
				/*
				 * Combine in a tree cap, then place the value in the Node array to be picked up
				 * by parent. -- the more you are like a power of 2, the longer you live root is
				 * thread 0, level 1 is threads 0 and P/2, level 2 is 0, P/4, P/2, 3P/4, etc.
				 */
				for (int stride = 1; stride < threadP && index % (2 * stride) == 0; stride *= 2)
					tally.combine(getNode(index + stride));
				setNode(index, tally);
				return;

				} catch (InterruptedException e) {
				e.printStackTrace();
				}
			}
		}
		
		private List<reduceTask> createSubtasks(){
			List<reduceTask> subtasks = new ArrayList<reduceTask>();
			int start = 0;
			int end = n/threadP;
			for(int i=0; i<threadP; i++){
				subtasks.add(new reduceTask(i,start,end));
				start = end;
				end += n/threadP;
			}
			return subtasks;
		}
	}

	/*
	* @Class scanTask - this is the nested class run by the scan threads
	*/
	class scanTask extends RecursiveAction{
		private int start;
		private int end;
		private int index;
		private int size;
		
		public scanTask(int index, int start, int end){
			this.start = start;
			this.end = end;
			this.index = index;
			this.size = n/threadP;
		}

		TallyType fade(int index){
			TallyType tally = newTally();
			int amount = 99;
			for(int i = index - 100; i < index; i++){
				if(i >= 0)
					tally.combine(interior.get(i).decay(amount--));
			}
			return tally;
		}

		@Override
		public void compute(){
			if( (end-start) > n/threadP ){ //split
				List<scanTask> tasks = createSubtasks();
				for(scanTask tsk : tasks)
					tsk.fork();
			}else{ //compute from my start to my end
				TallyType tally = newTally();
				for(int i=start; i < end; i++)
					result.set(i, fade(i));
			}
		}
		
		private List<scanTask> createSubtasks(){
			List<scanTask> subtasks = new ArrayList<scanTask>();
			int start = 0;
			int end = n/threadP;
			for(int i=0; i<threadP; i++){
				subtasks.add(new scanTask(i,start,end));
				start = end;
				end += n/threadP;
			}
			return subtasks;
		}
	}
	
	@SuppressWarnings("unchecked")
	private BlockingQueue<TallyType> findNode(Object[] nodeArray, int i) {
		return ((BlockingQueue<TallyType>) nodeArray[i]);
	}
	
	private void setNode(int i, TallyType tally) throws InterruptedException {
		findNode(nodeValue, i).put(tally);
	}

	private TallyType getNode(int i) throws InterruptedException {
		return findNode(nodeValue, i).take();
	}


	private void setNodeUp(int i, TallyType tally) throws InterruptedException {
		findNode(nodeUp, i).put(cloneTally(tally));
	}

	private TallyType getNodeUp(int i) throws InterruptedException {
		return findNode(nodeUp, i).take();
	}

	private void setNodeDown(int i, TallyType tally) throws InterruptedException {
		findNode(nodeDown, i).put(cloneTally(tally));
	}

	private TallyType getNodeDown(int i) throws InterruptedException {
		return findNode(nodeDown, i).take();
	}

	private void setNodeLeft(int i, TallyType tally) throws InterruptedException {
		findNode(nodeLeft, i).put(cloneTally(tally));
	}

	private TallyType getNodeLeft(int i) throws InterruptedException {
		return findNode(nodeLeft, i).take();
	}

	@SuppressWarnings("unchecked")
	private TallyType newTally() {
		return (TallyType) factory.init();
	}
	
	@SuppressWarnings("unchecked")
	private TallyType cloneTally(TallyType tally) {
		return (TallyType) tally.clone();
	}
}
