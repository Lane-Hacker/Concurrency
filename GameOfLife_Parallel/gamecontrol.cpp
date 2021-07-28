/* @Author: Patrick Manacorda
 * @See: Seattle University CPSC 5600
 * @File: gamecontrol.cpp
 */


#include "gamecontrol.h"
#include <string>
#include <fstream>
#include <sstream>
#include <thread>         // std::this_thread::sleep_for
#include <chrono>         // std::chrono::milliseconds
//#include "gamelifeS.h"
controller::controller(){
  this->game = new GameLife(32);
}


void controller::execute(std::ostream& out){
  out << "\033\143";
  this->game->randomize();
  display(out, this->game->get(), this->game->getDim());
  out << "INPUT: "; std::string read = "DEFAULT";
  std::cin>>read;
  while(read != "x"){
    if(read == "n"){
    out << "\033\143";
    display(out, this->game->next(), this->game->getDim());
    out << "INPUT: "; std::cin >> read;
  }else if(read == "x"){
    break;
  }else if(read == "s"){
    out << "File name: ";
    std::cin >> read;
    this->game->save(read);
    out << "INPUT: ";
    std::cin >> read;
  }else if(read == "l"){
    out << "File name: ";
    std::cin >> read;
    out << "\033\143";
    this->game->load(read);
    display(out, this->game->get(), this->game->getDim());
    out << "INPUT: ";
    std::cin>>read;
    }else if(read == "a"){
      while(true){
        out << "\033\143";
        display(out, this->game->next(), this->game->getDim());
        std::this_thread::sleep_for(std::chrono::milliseconds(400));
      }  
    }else if(read == "t"){
      this->game = new GameLife(GAME_SIZE*32);
      this->game->randomize(DENSITY);
      auto start = std::chrono::steady_clock::now();
      this->game->get(1000);
      auto end = std::chrono::steady_clock::now();
      out << "Elapsed time in milliseconds for parallel : "
          << std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count()
          << " ms" << std::endl;
      delete this->game;
      /*this->game = new GameLife(GAME_SIZE);
      this->game->randomize(DENSITY);
      GameLifeS seq(GAME_SIZE*32);
      seq.randomize(DENSITY);
      start = std::chrono::steady_clock::now();
      seq.get(1000);
      end = std::chrono::steady_clock::now();
      out << "Elapsed time in milliseconds for sequential: "
          << std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count()
          << " ms" << std::endl;
      out << "INPUT: "; std::cin >> read;
      */
    }else if(read == "g"){
    out << "Go to generation: ";
    int gen; std::cin >> gen;
    out << "\033\143";
    auto start = std::chrono::steady_clock::now();
    display(out, this->game->get(gen), this->game->getDim());
    auto end = std::chrono::steady_clock::now();
    out << "Elapsed time in milliseconds : "
        << std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count()
        << " ms" << std::endl;
    out << "INPUT: "; std::cin >> read;
    }
    
  }
  
}
  

void controller::display(std::ostream& out, bool** grid, int dim){
  this->displayHeader(out);
  for(int row = 0; row < dim; row++){
    for(int col = 0; col < dim*2; col++){
      if(grid[row][col]) out << "•";
      else out << "-";
    }
    out << std::endl;
  }
  out << "Generation : " << this->game->getGen() << std::endl;
  out << "n = forward | s = Save | l = Load | g = Generation | a = animation | t = test | x = Exit \n";
}

void controller::displayHeader(std::ostream& out){
  out << "--------------------------------------------------------\n";
  out << "             CONWAY'S GAME OF LIFE                      \n"
      << "--------------------------------------------------------\n";
}
controller::~controller(){
  delete this->game;
}


