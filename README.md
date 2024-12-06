
# Elytra Creepers

The Elytra Creepers mod features AI-controlled creepers capable of advanced aerial navigation using elytras. The creepers should be able to fly to a grounded target--while **maximizing flight time efficiency** and **minimizing fall damage**.

The creepers are trained using [Neuroevolution of Augmented Topologies (NEAT)](https://en.wikipedia.org/wiki/Neuroevolution_of_augmenting_topologies), a well-known neuroevolution method. Neuroevolution is a “survival of the fittest” genetic algorithm that combines evolutionary biology with neural networks: a population of agents run a task, a fitness score is used to evaluate performance, those deemed the fittest produce the next generation of agents, and so on.

## Progress
I am currently training the neural networks, but I've already made some promising progress. See clip below. After iterating over 300 generations, the agents are consistently able to land on the grounded destination smoothly and efficiently. Starting from over 100 blocks away, ~95% of the agents land within 3 blocks of the target in under 6 seconds! As seen in the video below, the agents nosedive to build up speed, and then face upward to slow down for a smooth, precise landing. 

https://github.com/user-attachments/assets/6da7b2a4-a452-4303-bf57-57f3d6e21bdf

_(If you look closely, these are actually not creepers. They are custom entities I made [solely for the purpose of training](https://github.com/wleethecoder/Elytra-Creepers-Minecraft-Mod/issues/25).)_

<details> 
  <summary>(Click here) These are clips showing the training process during the earlier generations.</summary>

https://github.com/user-attachments/assets/43966ebc-b96e-4030-8fa6-a90540f2a580



https://github.com/user-attachments/assets/8aa95f54-1322-4c1a-90ae-a275c9ef7220

Convergence was achieved around Generation 180.


https://github.com/user-attachments/assets/725cfdda-0aeb-4e93-a96c-6f7695f431f0



https://github.com/user-attachments/assets/a2fbea35-dc06-4bcc-a471-0e0aa655a82b



https://github.com/user-attachments/assets/1bbf4e96-a5a1-4d7a-b89a-a5399542d0c6



https://github.com/user-attachments/assets/96a6654f-ebbe-4c78-adb0-d05c360c79a7

For Generation 1, the agents get to their target very quickly, but they take massive fall damage, which is suboptimal.
</details>


Here is a bonus video of me trying to avoid the elytra-flying creepers but failing!

https://github.com/user-attachments/assets/03ef2a31-faa2-4a63-a10f-859b4030ccac

## Future Plans

Currently, the agents are trained to reach a stationary target that is 100 blocks away from them, in both horizontal and vertical distance. To increase network robustness, I am planning to train agents for different target distances. I may even make the target move in random directions. 

Additionally, I am considering training agents to avoid obstacles, but this would be a difficult and computationally intensive task; detecting obstacles requires either raytracing or image recognition. It is definitely possible, however.

### MIT License of NEAT implementation (https://github.com/Luecx/NEAT)

Copyright (c) 2019 Luecx

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
