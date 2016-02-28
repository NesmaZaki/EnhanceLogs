<h5>Source code associate the paper
<h1> Analyzing and Repairing Overlapping Work Items in
Process Logs
<h5>By Nesma M. Zaki, Ahmed Awad, Chiara Di Francescomarino

To run our code first: 
- run schema.sql using mySQL.
	
- Under EnhanceEventLogs\src\EnhancingLog:
	- you need to establish your connection in ConnDB.java
	- Then, run main.java :
		- Update the parameter of parse functions with your own log file name (.xes) or (.xlsx) format 
			but this file must be adapted with the lifecycle mentioned in our paper.
		- Call RepairingLogs.
			> call mapping function first in order to map event states to abstaract states.
			> Then, write your strategies that you want to apply.
			
		
- As result of running main.java file is :
	-  event log table which store all the data will be modified.

