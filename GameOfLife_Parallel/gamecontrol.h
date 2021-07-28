/*
 * @Author: Patrick Manacorda
 * @See: Seattle University CPSC 5600
 * @File: gamecontrol.h
 */

#include "gamelife.h"
#include <iostream> 
#include <time.h>

class controller{
 public:
  controller();
  ~controller();
  void execute(std::ostream& out);

 private: 
  const int DENSITY = 2;
  const int GAME_SIZE = 32;
  
  GameLife* game;
  void display(std::ostream& out = std::cout, bool** grid = NULL, int dim = 0);
  void displayHeader(std::ostream& out);
};
