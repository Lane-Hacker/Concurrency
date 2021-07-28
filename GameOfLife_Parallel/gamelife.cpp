/*
 * @Author: Patrick Manacorda
 * @Date: March 3rd 2020
 * @See: Final Project CPSC 5600
 * @File: GameLife.cpp
 */

#include "gamelife.h"
#include <cstddef>
#include <stdexcept>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <fstream>
#include <future>
#include <thread>
#include <algorithm>
using namespace std;

GameLife::GameLife(){
  srand(time(NULL));
  int size = this->DEFAULT_SIZE;
  this->grid = new bool*[size];
  for(int i=0; i<size; i++)
    this->grid[i] = new bool[size*2];
  this->dim = size;
  this->current = 0;
  this->nextGen = new bool*[dim];
  for(int i=0; i<dim; i++)
    nextGen[i] = new bool[2*dim];
}

GameLife::GameLife(int size){
  if(size < DEFAULT_SIZE) size = DEFAULT_SIZE;
  this->grid = new bool*[size];
  for(int i=0; i<size; i++)
    this->grid[i] = new bool[size*2];
  this->dim = size;
  this->current = 0;
  this->nextGen = new bool*[dim];
  for(int i=0; i<dim; i++)
    nextGen[i] = new bool[2*dim];
}

GameLife::GameLife(const GameLife& other){
  this->dim = other.dim;
  this->grid = new bool*[dim];
  for(int i=0; i<dim; i++)
    this->grid[i] = new bool[dim*2];
  for(int row=0; row<dim; row++)
    for(int col=0; col<dim*2; col++)
      this->grid[row][col] = other.grid[row][col];
}

GameLife& GameLife::operator=(const GameLife& other){
  if(this == &other) return *this;
  this->clear();
  this->dim = other.dim;
  this->grid = new bool*[dim];
  for(int i=0; i<dim; i++)
    this->grid[i] = new bool[dim*2];
  for(int row=0; row<dim; row++)
    for(int col=0; col<dim*2; col++)
      this->grid[row][col] = other.grid[row][col];
  return *this;
}

GameLife::GameLife(GameLife&& other){
  this->grid = other.grid;
  this->dim = other.dim;
  this->current = other.current;
  other.clear();
}

GameLife& GameLife::operator=(GameLife&& other){
  if(this == &other) return *this;
  this->grid = other.grid;
  this->dim = other.dim;
  this->current = other.current;
  other.clear();
  return *this;
}

GameLife::~GameLife(){
  this->clear();
}
bool** GameLife::get() const{
  return this->grid;
}

bool** GameLife::get(int gen){
  while(current != gen)
    this->next();
  return this->grid;
}

int GameLife::getDim() const{
  return this->dim;
}
void GameLife::randomize(int density){
  if(density < 2) density = DEFAULT_DENSITY;
  for(int row=0; row<dim; row++)
    for(int col=0; col<dim*2; col++)
      this->grid[row][col] = rand()%density == 0 ? true:false;
  int factor = 5;
  this->get(5);
  this->current = current - factor;
}

bool** GameLife::next(){
  std::thread threadObj[N_THREADS];
  for(int id=0; id<N_THREADS; id++){
    threadObj[id] = std::thread(&GameLife::ThreadTask, this, id);
  }
  for(int id=0; id<N_THREADS; id++)
    threadObj[id].join();
  std::swap(nextGen, grid);
  current++;
  return this->grid;
}

int GameLife::getGen() const{
  return this->current;
}

GameLife::SaveType GameLife::save(std::string filename){
  std::ofstream out(filename);
  out << this->dim << std::endl;
  out << this->current << std::endl;
  
  for(int row=0; row<dim; row++)
    for(int col=0; col<dim*2; col++){
      if(grid[row][col])
        out << 1 << std::endl;
      else
        out << 0 << std::endl;
    }
  out.close();
  return {NULL, 0, 0};
}

bool GameLife::load(std::string read){
  if(grid != NULL)
    this->clear();
  std::ifstream in(read);
  if(in.is_open()){
    int t;
    in >> t;
    this->dim = t;
    in >> t;
    this->current = t;
    this->grid = new bool*[dim];
    for(int i=0; i<dim; i++)
      grid[i] = new bool[dim*2];
    this->nextGen = new bool*[dim];
    for(int i=0; i<dim; i++)
      nextGen[i] = new bool[2*dim];
    for(int row = 0; row < dim; row++)
      for(int col = 0; col < dim*2; col++){
        int read;
        in >> read;
        if(read == 0)
          grid[row][col] = false;
        else
          grid[row][col] = true;
      }
    in.close();
    return true;
  }
  return false;
}

bool GameLife::evaluate(Cell cell){
  int partners = 0;
  if(cell.x == 0 && cell.y == 0){ //TOP LEFT CORNER
    if(grid[cell.x+1][cell.y]) partners++;
    if(grid[cell.x][cell.y+1]) partners++;
    if(grid[cell.x+1][cell.y+1]) partners++;
  }else if(cell.x == dim-1 && cell.y == dim-1){ //BOTTOM RIGHT CORNER
    if(grid[cell.x-1][cell.y]) partners++;
    if(grid[cell.x][cell.y-1]) partners++;
    if(grid[cell.x-1][cell.y-1]) partners++;
  }else if(cell.x == 0 && cell.y == dim-1){ //TOP RIGHT CORNER
    if(grid[cell.x][cell.y-1]) partners++;
    if(grid[cell.x+1][cell.y]) partners++;
    if(grid[cell.x+1][cell.y-1]) partners++;
  }else if(cell.x == dim-1 && cell.y == 0){ //BOTTOM LEFT CORNER
    if(grid[cell.x-1][cell.y]) partners++;
    if(grid[cell.x][cell.y+1]) partners++;
    if(grid[cell.x-1][cell.y+1]) partners++;
  }else if(cell.x == 0){ //TOP ROW
    if(grid[cell.x+1][cell.y]) partners++;
    if(grid[cell.x][cell.y-1]) partners++;
    if(grid[cell.x][cell.y+1]) partners++;
    if(grid[cell.x+1][cell.y-1]) partners++;
    if(grid[cell.x+1][cell.y+1]) partners++;
  }else if(cell.x == dim-1){ //LAST ROW
    if(grid[cell.x-1][cell.y]) partners++;
    if(grid[cell.x-1][cell.y+1]) partners++;
    if(grid[cell.x-1][cell.y-1]) partners++;
    if(grid[cell.x][cell.y-1]) partners++;
    if(grid[cell.x][cell.y+1]) partners++;
  }else if(cell.y == 0){ //FIRST COL
    if(grid[cell.x][cell.y+1]) partners++;
    if(grid[cell.x+1][cell.y]) partners++;
    if(grid[cell.x-1][cell.y]) partners++;
    if(grid[cell.x-1][cell.y+1]) partners++;
    if(grid[cell.x+1][cell.y+1]) partners++;
  }else if(cell.y == dim-1){ //LAST COL
    if(grid[cell.x-1][cell.y]) partners++;
    if(grid[cell.x+1][cell.y]) partners++;
    if(grid[cell.x][cell.y-1]) partners++;
    if(grid[cell.x-1][cell.y-1]) partners++;
    if(grid[cell.x+1][cell.y-1]) partners++;
  }else{ //GENERAL
    if(grid[cell.x-1][cell.y+1]) partners++;
    if(grid[cell.x+1][cell.y+1]) partners++;
    if(grid[cell.x][cell.y+1]) partners++;
    if(grid[cell.x-1][cell.y]) partners++;
    if(grid[cell.x+1][cell.y]) partners++;
    if(grid[cell.x][cell.y-1]) partners++;
    if(grid[cell.x-1][cell.y-1]) partners++;
    if(grid[cell.x+1][cell.y-1]) partners++;
  }
  if(grid[cell.x][cell.y] && (partners == 2 || partners == 3))
    return true;
  if(!grid[cell.x][cell.y] && partners == 3)
    return true;
  return false;
}

void GameLife::clear(){
  for(int i=0; i<dim; i++){
    delete this->grid[i];
    delete this->nextGen[i];
  }
  if(grid)
    delete[] this->grid;
  if(nextGen)
    delete[] this->nextGen;
}
