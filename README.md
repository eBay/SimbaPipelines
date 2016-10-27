Simba - Dynamic, parallel, reactive and high throughput pipelines
==============

Note: Raptor version is work in progress. This read me will be updated with instruction in short time. 

It has the following modules/submodule.

* The *framework* is the module containing all classes needed for simba Framework(Such as Actors and Messages). 

* The *akka* is the submodule for akka based actors and messages. Simba is technology agnostice right now supports akka based pipelines. 
* The *framework/framework* project contains all platform logic agnostic of akka. 

* The *simbasvc* Service layer just to test simba deliverables. Not to be used by clients of simba. 


Getting Started
---------------

1. Clone the [simba](https://github.corp.ebay.com/EnterpriseSolutions/simba) repo.

2. Setting up you IDE: http://squbs/#/doc/Squbs/0.6.X/docs/mds/getting_started.md?hash=setting-up-your-project-in-the-ide

2. Build the unicomplex by running "sbt clean compile publish" from the squbs-unicomplex directory. Note that steps 1 and 2 are not needed once we provide the squbs-unicomplex into the repo.


Release note: 
----------------
1.6.0 (WIP)
* Making Simba available to raptor

1.5.0
* [Improvement] Moving timeoutHandler to JobMonitorActor
* [Improvement] Creation of PostExecuteActor for each jobType (PostExecuteActor only be created if the job go to waiting state)
* [Feature] Allow BusinessLogic to set fork Status of a job by using job.setForkComplete(boolean status) method
* [Feature] Extend CircuitBreaker functionality to Aero AsynchHandler as well. Reference implementation on ApiJobProcessor
* [Feature] adding new tool to get feed information by corelationid. You can call now a new endpoint <host>/tools/corelationid/<corelationid>
* [Improvement] Added more junits. 

1.4.0
* [Feature] Enable feed abort
* [Feature] Adding tools to be able to query job based on status
* [Feature] Enable feed timeout
* [Improvement] Add Job type for the tools and change the startTime format

1.3.0
* Introduce Job Timeout Feature.
* Introduce Tools jars to help for debugging
* Adding more CAL logging in various place.
* Couple bug fixes.
* Externalize Platform config
* Remove CipJobTypeEnum from framework

1.2.1

* Adding tools to view feed by feed id and jobs by job id.
* upgrading squbs to 0.6.5 version
* UTF_8 bug fix
* Dead latter fix (extra messages after actor stop) - not harmful though.

