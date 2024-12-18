
# Elytra Creepers

The Elytra Creepers mod features AI-controlled creepers capable of advanced aerial navigation using elytras. The creepers should be able to fly to a grounded target--while **maximizing flight time efficiency** and **minimizing fall damage**.

The creepers are trained using [Neuroevolution of Augmenting Topologies (NEAT)](https://en.wikipedia.org/wiki/Neuroevolution_of_augmenting_topologies), a well-known neuroevolution method. Neuroevolution is a “survival of the fittest” genetic algorithm that combines evolutionary biology with neural networks: a population of agents run a task, a fitness score is used to evaluate performance, those deemed the fittest produce the next generation of agents, and so on.

## Progress
I am currently training the neural networks, but I've already made some promising progress. See clip below. After iterating over 700 generations, the agents are consistently able to land on the grounded destination smoothly and efficiently. Starting from a randomized spawn point, the vast majority of the agents precisely intercept the moving target in under 6 seconds! As seen in the video below, the agents dive towards the target, and move in a spiral pattern when close to the ground for a smooth, precise landing. 

https://github.com/user-attachments/assets/99f5820e-af48-4b4e-9082-d4d64be6dd6f

_(If you look closely, these are actually not creepers. They are custom entities I made [solely for the purpose of training](https://github.com/wleethecoder/Elytra-Creepers-Minecraft-Mod/issues/25).)_

Here is a bonus video of me trying to avoid the elytra-flying creepers but failing!

https://github.com/user-attachments/assets/09f8dadb-d0c8-407e-bbfc-9971b899b67e

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
