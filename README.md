<h5>Source code associate the paper
<h1> Enhancing Process Execution Logs for Accurate Performance Measurement
<h5>By Nesma Mostafa , Ahmed Hany

To run our code first: 
- run schema.sql using mySQL :
	
- Under EnhanceEventLogs\src\EnhancingLog:
	- you need to establish your connection in ConnDB.java
	- Then, run main.java :
		- Update the parameter of parse functions with your own log file name (.xes) or (.xlsx) format 
			but this file must be adapted with the lifecycle mentioned in our paper.
		- Call ConcurrentTasks.
		
- As result of running main.java file is :
	-  event log table which store all the data will be modified.

