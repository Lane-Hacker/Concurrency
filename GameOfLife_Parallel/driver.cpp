#include "gamecontrol.h"

#include <iostream>

int main(){
  controller* test = new controller();
  test->execute(std::cout);
  delete test;
  return 0;
}
