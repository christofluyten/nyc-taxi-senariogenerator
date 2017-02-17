# nyc-taxi-senariogenerator
Generates scenarios for RinSim, based on the New York City Taxi Trip Data.


#### New York City Taxi Trip Data
By adding a local path (ScenarioGenerator.java) to a directory with New York City Taxi Trip Data[1], scenario's can be generated for RinSim. 

#### New York City Hourly Traffic Estimates
Speed limits can be set on the graph, by adding a local path to the New York City Hourly Traffic Estimates[2].
This data is sparse, there are many links without an hourly estimate. In the case that there isn't a speed limit for a certain link, the speed limit is set to the average speed, calculated over all the measurements for that link. If there is not a single measurement for a certain link, the speed limit is set to a default value, in this case 40 miles per hour.

#### Map
The resourses contain a links.csv file, taken from the New York City Hourly Traffic Estimates as a basis for the map. This file contains more than 250,000 links. More than 700 links are not used in the map. A majority of the unused links are deleted because they are not fully connected to the rest op the graph, a minority because the link represents a road that does not allow taxi's (e.g. a busstop).




[1] https://databank.illinois.edu/datasets/IDB-9610843

[2] https://databank.illinois.edu/datasets/IDB-4900670




