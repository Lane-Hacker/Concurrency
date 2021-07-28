/*
 * @Author: Patrick Manacorda
 * @Date: March 3rd 2020
 * @See: Final Project CPSC 5600 - GameLife.h
 * @File: GameLife.h
 */

/*
 * Object from this class are meant to be used to 
 * fetch the current/next/or desired generation
 * ------------DETAILS------------------- 
 * @Space Efficiency: 2 grids of size N x (2N), named grid and nextGen
 * @Time Efficiency: N_THREADS divide up grid to calculate next generation
 
 * @Constructor : dimention of grid (int param)
 * @Public Methods:
 *  - Next() - Returns next generation grid
 *  - Get()  - Returns current generation grid
 *  - Load(Save) - Loads configuration in the save
 *  - Rand() - Randomizes the grid
 * @Private interface:
 *  - Evaluate(Cell) - returns wheter cell survives or not
 * @Private implementation:
 *  - bool** grid[][] - dim x 2dim
 *  - 0/false means dead , 1/true means alive
 *  - Current Generation Counter
 *  - Saves holds copy of private data
 */
#include <string>

class GameLife{
 public:
  GameLife();
  GameLife(int size);
  GameLife(const GameLife& other);
  GameLife& operator=(const GameLife& other);
  GameLife(GameLife&& other);
  GameLife& operator=(GameLife&& other);
  ~GameLife();
  bool** get() const;
  bool** get(int gen);
  bool** next();
  int getDim() const;
  int getGen() const;
  struct SaveType{
    bool** saveGrid;
    int current;
    int dim;
  };
  bool load(std::string read);
  SaveType save(std::string read);
  void randomize(int density = DEFAULT_DENSITY);
 private:
  static const int DEFAULT_SIZE = 32;
  static const int DEFAULT_DENSITY = 3;
  static const int N_THREADS = 8;
  bool** grid;
  bool** nextGen;
  
  void ThreadTask(int id){
    for(int row = id*(dim/N_THREADS); row < (id+1)*(dim/N_THREADS); row++)
      for(int col = 0; col < 2*dim; col++)
        nextGen[row][col] = evaluate({row,col});
  }
  int dim;
  int current;
  struct Cell{
    int x, y;
  };
  bool evaluate(Cell cell);
  void clear();
};
