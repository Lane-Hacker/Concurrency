/*
* @Author : Patrick Manacorda
* @Date: 2/12/2020
* @See: Seattle University - CPSC 5600 HW5
*/

/* Class HeatMap
* Implements a DIMxDIM grid of integer values, starting at 0
* Implements Tally<Observation> interface
*/

public class HeatMap implements Tally<Observation> {
	protected int[][] grid;
	protected int dim;
	private final int DEFAULT_SIZE = 150;
	
	public HeatMap(int dim){
		this.dim = dim;
		this.grid = new int[dim][dim];
		for(int row=0; row<dim; row++)
			for(int col=0; col<dim; col++)
				this.grid[row][col] = 0;
	}
	
	public HeatMap clone() {
		HeatMap copy = new HeatMap(this.dim);
		for(int row=0; row<dim; row++)
			for(int col=0; col<dim; col++)
				copy.grid[row][col] = this.grid[row][col];
		return copy;
	}
	
	public int[][] getHeatMap() {
		return grid.clone();
	}

	public double getCell(int r, int c) {
		return grid[r][c];
	}

	public HeatMap decay(int amount){
		HeatMap copy = this;
		for(int row = 0; row < dim; row++){
			for(int col = 0; col < dim; col++){
				copy.grid[row][col] -= copy.grid[row][col]/100 * amount;
			}
		}
		return copy;
	}
	
	@Override
	public HeatMap init(){
		return new HeatMap(this.dim);
	}
	
	@Override
	public void accum(Observation elem){
            int xCoord = (int)(elem.x * (this.dim-1));
            int yCoord = (int)(elem.y * (this.dim-1));
		if(xCoord < dim && yCoord < dim && xCoord >= 0 && yCoord >= 0)
                    this.grid[xCoord][yCoord]++;
	}
	
	@Override
	public void combine(Tally<Observation> other){
		HeatMap right = (HeatMap)other;
		for(int row=0; row<dim; row++)
			for(int col=0; col<dim; col++)
				this.grid[row][col] += right.grid[row][col];
	}
}
