#!/bin/bash
# This script executes the MATSim class ScenarioBuilder.
# The ScenarioBuilder automatically converts the data given in the configuration file into a MATSim pre-base scenario,
# meaning: the scenerio contains all elements needed to start a run but it's not yet calibrated in any dimension.
# The MATSim data should be located in the sub folder "matsimInput" of the working directory you defined.

clear

#osmosis --rb file="" --bp file="" --tf accept-whatever [tag]=[stuff] --un...

java -mx8g -cp matsim.jar playground.dhosse.scenarios.generic.ScenarioBuilder $1
