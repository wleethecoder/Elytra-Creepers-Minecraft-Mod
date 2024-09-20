# Skeleton-Bow-Master-Minecraft-Mod

The purpose of this project is to train and optimize a neural network that controls a skeleton (an enemy creature that shoots with a bow and arrow) in Minecraft. The neural network is trained using a policy-based reinforcement learning algorithm, REINFORCE. During this process, two copies of the agent fight each other, hence a 2-player RL task. After the training, the skeleton will be able to aim and fire arrows towards the target with a similar competence as a human.

Believe it or not, I had to write a neural network and the training process from scratch. Without a deep learning library. I could not get the more powerful ones (e.g. Deeplearning4j) to work due to stubborn dependency issues, and the libraries that worked did not suffice for the requirements for my project, such as needing a network with multiple output layers.

![](https://github.com/wleethecoder/Skeleton-Bow-Master-Minecraft-Mod/blob/main/trimmed.gif)  
This is currently a work in progress. As you can see, the skeletons are moving around in a circle as though they're performing a blood ritual of some sort. I have yet to achieve convergence via adjusting network architecture, reward mechanics, hyperparameters, etc. Also, I had calculated everything manually, so I would have to watch out for errors.

## General info  

Environment:  
•	48 x 48 flat area enclosed by tall walls  

Observations:  
•	Horizontal distance (distance projected onto the horizontal XZ plane) and vertical distance (y-component of the distance) from the agent to the opponent  
•	Opponent’s velocity relative to *vector v, which is the vector pointing from the agent to the opponent**. 3 scalar values: opponent’s velocity towards/away from agent along v; opponent’s rightward/leftward velocity relative to v; and opponent’s upward/downward velocity relative to v.  
•	How much the agent is rotating away from vector v in terms of pitch and yaw  
•	Agent's health (percentage)  
•	Charge meter of bow  

Actions:  
•	Set how much the agent rotates away from vector v in terms of pitch and yaw  
•	Press or not press right click button.  
•	Move forward, backward, or neither.  
•	Strafe left, right, or neither.  
•	Jump or no jump.  

Reward mechanics:  
•	When the agent's hits the opponent, the received reward equals the amount of damage dealt  
•	For each tick, the agent receives a small negative reward of -0.005.  
