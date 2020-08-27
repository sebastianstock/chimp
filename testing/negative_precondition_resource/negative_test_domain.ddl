##################
# Reserved words #
################################################################
# HybridHTNDomain                                              #
# MaxArgs                                                      #
# :operator                                                    #
# :method                                                      #
# Head                                                         #
# Pre                                                          #
# Add                                                          #
# Del                                                          #
# Sub                                                          #
# Constraint                                                   #
# Ordering                                                     #
# Type                                                         #
# Values                                                       #
# StateVariable                                                #
# FluentResourceUsage                                          #
# Param                                                        #
# ResourceUsage                                                #
# Usage                                                        #
# Resource                                                     #
# Fluent                                                       #
#                                                              #
##   All AllenIntervalConstraint types                         #
##   '[' and ']' should be used only for constraint bounds     #
##   '(' and ')' are used for parsing                          #
#                                                              #
################################################################

(HybridHTNDomain RACEDomain)

(MaxArgs 3)
############ Resources ###############

#(Resource ManipulationCapacityRover 1)

(Resource SampleStorageCapacityRover 5)
#(Resource SampleStorageCapacityShuttle 2)

############ Fluent Resource Usage ###
(FluentResourceUsage 
  (Usage SampleStorageCapacityRover 1) 
  (Fluent Attached)
  (Param 2 rover1)
)


############ Operators ###############


#### Auxiliry operators:
(:operator
 (Head !!check_empty())
 (ResourceUsage SampleStorageCapacityRover 5)
 (Constraint Duration[2,INF](task))
)


#### Real operators:

### Move to a specified position
(:operator
 (Head !move_to(?robot ?toArea))
 (Pre p1 RobotAt(?robot ?fromArea))
 (Constraint OverlappedBy(task,p1))
 (Add e1 RobotAt(?robot ?toArea))
 (Constraint Meets(task,e1))
 (Del p1)
# (ResourceUsage navigationCapacity 1)
)

### Take regolith samples from at a specified area
(:operator
 (Head !sample_regolith(?robot ?area))
 (Values ?robot rover1)
 (Pre p1 RobotAt(?robot ?area))
 (Constraint During(task,p1)) # robot has to stay there
 (Pre p2 Attached(?samplecontainer ?robot))
 (Constraint During(task,p2)) # is attached the whole time
 (Pre p3 Empty(?samplecontainer))
 (Del p3)
 (Constraint Meets(p3,task))
 (Type ?samplecontainer SampleContainer)
 (Add e1 Filled(?samplecontainer))
 (Constraint Meets(task,e1))
 (ResourceUsage ManipulationCapacityRover 1)
)

### Transfer samples from one robot1 to robot2
# robots need to be at the same area/position
(:operator
 (Head !transfer_sample(?robot1 ?robot2 ?container))
 (Pre p1 RobotAt(?robot1 ?area))
 (Pre p2 RobotAt(?robot2 ?area))
 (Pre p3 Attached(?container ?robot1))
 (Del p3)
 (Constraint During(task,p1))
 (Constraint During(task,p2))
 (Add e1 Attached(?container ?robot2))
 (Constraint Meets(task,e1))
# (ResourceUsage ManipulationCapacity 1)  # TODO
)

### Exchange payload item
(:operator
 (Head !transfer_payload(?robot1 ?robot2 ?payload))
 (Pre p1 RobotAt(?robot1 ?area))
 (Pre p2 RobotAt(?robot2 ?area))
 (Pre p3 Attached(?payload ?robot1))
 (Del p3)
 (Constraint During(task,p1))
 (Constraint During(task,p2))
 (Add e1 Attached(?payload ?robot2))
 (Constraint Meets(task,e1))
# (ResourceUsage ManipulationCapacity 1)  # TODO
)

### Pick up a BaseCamp from area
(:operator
 (Head !pickup_basecamp(?robot ?camp))
 (Values ?robot rover1)
 (Type ?camp BaseCamp)
 (Pre p1 RobotAt(?robot1 ?area))
 (Constraint During(task,p1))
 (Pre p2 PersonAt(?camp ?area))
 (Del p2)
 (Constraint Meets(p2,task))
 (Add e1 Attached(?camp ?robot))
 (Constraint Meets(task,e1))
 (ResourceUsage ManipulationCapacityRover 1)
)

### Pick up a BaseCamp from lander
(:operator
 (Head !pickup_basecamp(?robot ?camp))
 (Values ?robot rover1)
 (Type ?camp BaseCamp)
 (Pre p1 RobotAt(?robot1 ?area))
 (Constraint During(task,p1))
 (Pre p2 Attached(?camp ?lander))
 (Del p2)
 (Pre p3 PersonAt(?lander ?area))
 (Constraint Meets(p2,task))
 (Add e1 Attached(?camp ?robot))
 (Constraint Meets(task,e1))
 (ResourceUsage ManipulationCapacityRover 1)
)

### Place a BaseCamp
(:operator
 (Head !place_basecamp(?robot ?camp ?area))
 (Values ?robot rover1)
 (Type ?camp BaseCamp)
 (Pre p1 RobotAt(?robot1 ?area))
 (Constraint During(task,p1))
 (Pre p2 Attached(?camp ?robot))
 (Del p2)
 (Add e1 PersonAt(?camp ?area))
 (Constraint Meets(task,e1))
 (ResourceUsage ManipulationCapacityRover 1)
)

############ Methods   ###############

### Deploy basecamp at a given position

# case1:
# basecamp is attached to the robot
# robot is not at the goal position
(:method
 (Head deploy_basecamp(?robot ?toArea))

 (Pre p1 RobotAt(?robot ?fromArea))
 (VarDifferent ?toArea ?fromArea)

 (Pre p2 Attached(?camp ?robot))
 (Type ?camp BaseCamp)

 (Sub s1 !move_to(?rover ?toArea))
 (Constraint Starts(s1,task))
 (Sub s2 !place_basecamp(?robot ?camp ?toArea))

 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

# case2:
# basecamp is attached to the robot
# robot is already at the position
(:method
 (Head deploy_basecamp(?robot ?area))

 (Pre p1 RobotAt(?robot ?area))

 (Pre p2 Attached(?camp ?robot))
 (Type ?camp BaseCamp)

 (Sub s1 !place_basecamp(?robot ?camp ?toArea))
 (Constraint Equals(s1,task))
)

# TODO case3: basecamp is not attached

### Take regolith samples ##################

# case 1: robot is already at the position
(:method
 (Head take_samples(?robot ?area))

 (Pre p1 RobotAt(?robot ?area))

 (Sub s1 !sample_regolith(?robot ?area))
 (Constraint Equals(s1,task))
)


# case 2: robot is not at the position
(:method
 (Head take_samples(?robot ?toArea))

 (Pre p1 RobotAt(?robot ?fromArea))
 (VarDifferent ?toArea ?fromArea)

 (Sub s1 !move_to(?rover ?toArea))
 (Constraint Starts(s1,task))
 (Sub s2 !sample_regolith(?robot ?toArea))
 (Constraint Finishes(s2,task))
 
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

# Optional todo: case 3: robot has no empty sample container


### Shuttle rendezvous  ################



### Get a basecamp from the lander #####

# case 1: robot is at a different area
(:method
 (Head get_basecamp(?robot ?camp))
 (Type ?robot Rover)
 (Type ?camp BaseCamp)
 
 (Pre p0 Attached(?camp ?lander))
 (Type ?lander Lander)
 
 (Pre p1 RobotAt(?robot ?robotArea))
 (Pre p2 PersonAt(?lander ?landerArea))
 (VarDifferent ?robotArea ?landerArea)

 (Sub s1 !move_to(?rover ?landerArea))
 (Constraint Starts(s1,task))
 (Sub s2 !pickup_basecamp(?robot ?camp))
 (Constraint Finishes(s2,task))
 
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

# case 2: robot is already near the lander
(:method
 (Head get_basecamp(?robot ?camp))
 (Type ?robot Rover)
 (Type ?camp BaseCamp)
 
 (Pre p0 Attached(?camp ?lander))
 (Type ?lander Lander)
 
 (Pre p1 RobotAt(?robot ?area))
 (Pre p2 PersonAt(?lander ?area))

 (Sub s1 !pickup_basecamp(?robot ?camp))
 (Constraint Equals(s1,task))
)

### Transfer all samples
(:method
 (Head transfer_all_samples(?robot1 ?robot2))
 
 (Pre p0 Attached(?container ?robot1))
 (Type ?container SampleContainer)
 
 (Pre p1 RobotAt(?robot1 ?robotArea))
 (Pre p2 RobotAt(?robot2 ?robotArea))

 (Sub s1 !transfer_sample(?robot1 ?robot2 ?container))
 (Constraint Starts(s1,task))
 (Sub s2 transfer_all_samples(?robot1 ?robot2))
 (Constraint Finishes(s2,task))
 
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

(:method
 (Head transfer_all_samples(?robot1 ?robot2))
 (ResourceUsage SampleStorageCapacityRover 5)
 (Constraint Duration[2,INF](task))

# (Sub s1 !!check_empty())
# (Constraint Equals(s1,task))
)
