
# ECE428/CS425 Programming Assignment 1 Multicast
### Instructions
#### Part 1  Unicast  
In directory 

` ./multicast_v03/bin ` 

use command   

` $ java multicast_v03.UnicastLauncher X ` 

to run a Unicast Process, where ` X ` denotes the process id, which should be an integer in range [1, 4].   
With multiple processes running, message in format below can be sent to each other.  

``` $ send destId content ``` 

where ` destId ` denotes the id of destination process and content denotes the content of message. 

#### Part 2  Total Ordering Multicast
In directory 

` ./multicast_v03/bin `   

use command 

` $ java multicast_v03.Sequencer ` 

to run a total ordering sequencer.   
Use command 

` $ java multicast_v03.TotalProcessLauncher X ` 

to run a process in total ordering multicast, where ` X ` denotes the process id, which should be an integer in range [1, 4].  
Processes with id 1, 2, 3 and 4 should all be running before next step. 
With multiple processes running, message in format below can be sent to sequencer and then be sent back and delivered in total ordering.   

``` $ msend content ```  

where ` content ` denotes the content of message. 

#### Part 3  Causal Ordering Multicast

In directory  
``` ./multicast_v03/bin ```  
use command  
``` $ java multicast_v03.CausalProcessLauncher X ```   
to run a process for Causal ordering multicast.  
Where ` X ` denotes the process id, which should be an integer in range [1, 4].  
Processes with id 1, 2, 3 and 4 should all be running before next step.  
With multiple processes running, message in format below can be sent to each other and delivered in Causal ordering.  

``` $ msend content ```  

where ` content ` denotes the content of message. 
