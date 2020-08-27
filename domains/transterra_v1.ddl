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

(HybridHTNDomain TransTerraDomain)

(MaxArgs 3)

(PredicateSymbols ObjAt RobotAt Attached ContainerAt BatteryAt
  !create_attached_fluent !!check_empty !move_to !sample_regolith
  !transfer_sample !transfer_battery !transfer_payload !pickup_basecamp
  !tuck_arms !place_basecamp
  deploy_basecamp take_samples get_basecamp
  transfer_all_samples transfer_filled_containers transfer_empty_containers
  transfer_charged_batteries transfer_discharged_batteries
  rendezvous rendezvous_meet rendezvous_exchange_batteries
  rendezvous_exchange_samples
  deposit_samples
  scenario_test)

############ Resources ###############

(Resource ManipulationCapacityRover 1)
(Resource SampleStorageCapacityShuttle 5)
(Resource FilledSampleStorageCapacityRover 5)
(Resource FilledSampleStorageCapacityShuttle 5)
(Resource EmptySampleStorageCapacityShuttle 5)
(Resource EmptySampleStorageCapacityRover 5)

(Resource ChargedBatteryStorageCapacityRover 5)
(Resource ChargedBatteryStorageCapacityShuttle 5)
(Resource DischargedBatteryStorageCapacityRover 5)
(Resource DischargedBatteryStorageCapacityShuttle 5)

(FluentResourceUsage 
  (Usage FilledSampleStorageCapacityRover 1) 
  (Fluent ContainerAt)
  (Param 2 rover1)
  (Param 3 filled)
)

(FluentResourceUsage 
  (Usage FilledSampleStorageCapacityShuttle 1) 
  (Fluent ContainerAt)
  (Param 2 shuttle1)
  (Param 3 filled)
)

(FluentResourceUsage 
  (Usage EmptySampleStorageCapacityRover 1) 
  (Fluent ContainerAt)
  (Param 2 rover1)
  (Param 3 empty)
)

(FluentResourceUsage 
  (Usage EmptySampleStorageCapacityShuttle 1) 
  (Fluent ContainerAt)
  (Param 2 shuttle1)
  (Param 3 empty)
)

(FluentResourceUsage 
  (Usage ChargedBatteryStorageCapacityRover 1) 
  (Fluent BatteryAt)
  (Param 2 rover1)
  (Param 3 charged)
)

(FluentResourceUsage 
  (Usage ChargedBatteryStorageCapacityShuttle 1) 
  (Fluent BatteryAt)
  (Param 2 shuttle1)
  (Param 3 charged)
)

(FluentResourceUsage 
  (Usage DischargedBatteryStorageCapacityRover 1) 
  (Fluent BatteryAt)
  (Param 2 rover1)
  (Param 3 discharged)
)

(FluentResourceUsage 
  (Usage DischargedBatteryStorageCapacityShuttle 1) 
  (Fluent BatteryAt)
  (Param 2 shuttle1)
  (Param 3 discharged)
)

############ Operators ###############

### Move to a specified position
(:operator
 (Head !move_to(?robot ?toArea))
 (Pre p1 RobotAt(?robot ?fromArea))
 (Constraint OverlappedBy(task,p1))
 (Add e1 RobotAt(?robot ?toArea))
 (Constraint Meets(task,e1))
 (Del p1)
)

### Take regolith samples from at a specified area
(:operator
 (Head !sample_regolith(?robot ?area))
 (Values ?robot rover1)
 (Pre p1 RobotAt(?robot ?area))
 (Constraint During(task,p1)) # robot has to stay there
 (Pre p2 ContainerAt(?samplecontainer ?robot ?empty))
 (Values ?empty empty)
 (Del p2)
 (Add e1 ContainerAt(?samplecontainer ?robot ?filled))
 (Values ?filled filled)
 (Constraint Meets(p2,e1))
 (ResourceUsage ManipulationCapacityRover 1)
)

### Transfer samples from one robot1 to robot2
# robots need to be at the same area/position
(:operator
 (Head !transfer_sample(?robot1 ?robot2 ?container))
 (Pre p1 RobotAt(?robot1 ?area))
 (Pre p2 RobotAt(?robot2 ?area))
 (Pre p3 ContainerAt(?container ?robot1 ?level))
 (Del p3)
 (Constraint During(task,p1))
 (Constraint During(task,p2))
 (Add e1 ContainerAt(?container ?robot2 ?level))
 (Constraint Meets(task,e1))
 (ResourceUsage
    (Usage ManipulationCapacityRover 1)
    (Param 2 rover1))
)

### Transfer samples from one robot1 to robot2
# robots need to be at the same area/position
(:operator
 (Head !transfer_battery(?robot1 ?robot2 ?battery))
 (Pre p1 RobotAt(?robot1 ?area))
 (Pre p2 RobotAt(?robot2 ?area))
 (Pre p3 BatteryAt(?battery ?robot1 ?status))
 (Del p3)
 (Constraint During(task,p1))
 (Constraint During(task,p2))
 (Add e1 BatteryAt(?battery ?robot2 ?status))
 (Constraint Meets(task,e1))
 (ResourceUsage
    (Usage ManipulationCapacityRover 1)
    (Param 2 rover1))
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
 (ResourceUsage
    (Usage ManipulationCapacityRover 1)
    (Param 2 rover1))
)

### Pick up a BaseCamp from area
(:operator
 (Head !pickup_basecamp(?robot ?camp))
 (Values ?robot rover1)
 (Type ?camp BaseCamp)
 (Pre p1 RobotAt(?robot1 ?area))
 (Constraint During(task,p1))
 (Pre p2 ObjAt(?camp ?area))
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
 (Pre p3 RobotAt(?lander ?area))
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
 (Add e1 ObjAt(?camp ?area))
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
 (Sub s1 !move_to(?robot ?toArea))
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
 (Sub s1 !move_to(?robot ?toArea))
 (Constraint Starts(s1,task))
 (Sub s2 !sample_regolith(?robot ?toArea))
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

### Shuttle rendezvous  ################
(:method
 (Head rendezvous(?robot1 ?robot2))
 (VarDifferent ?robot1 ?robot2)
 (Sub s1 rendezvous_meet(?robot1 ?robot2))
 (Sub s2 rendezvous_exchange_samples(?robot1 ?robot2))
 (Constraint Before(s1,s2))
 (Ordering s1 s2)
 (Sub s3 rendezvous_exchange_batteries(?robot1 ?robot2))
 (Constraint Before(s2,s3))
 (Ordering s2 s3)
)

(:method  # meet at the same location
 (Head rendezvous_meet(?robot1 ?robot2))
 (Pre p1 RobotAt(?robot1 ?r1Area))
 (Pre p2 RobotAt(?robot2 ?r2Area))
 (VarDifferent ?r1Area ?r2Area)
 (Sub s1 !move_to(?robot2 ?r1Area)) # TODO find good meeting point
 (Constraint Equals(s1,task))
)

(:method
 (Head rendezvous_meet(?robot1 ?robot2))
 (VarDifferent ?robot1 ?robot2)
)

(:method  # exchange samples
 (Head rendezvous_exchange_samples(?robot1 ?robot2))
 (Sub s1 transfer_filled_containers(?robot1 ?robot2))
 (Sub s2 transfer_empty_containers(?robot2 ?robot1))
 (Constraint Before(s1,s2))   # TODO not necessary if we use resources for manipulation
 (Ordering s1 s2)
)

(:method
 (Head rendezvous_exchange_batteries(?robot1 ?robot2))
 (Sub s1 transfer_charged_batteries(?robot2 ?robot1))
 (Sub s2 transfer_discharged_batteries(?robot1 ?robot2))
 (Constraint Before(s1,s2))   # TODO not necessary if we use resources for manipulation
 (Ordering s1 s2)
)

### Get a basecamp from the lander #####
# case 1: robot is at a different area
(:method
 (Head get_basecamp(?robot ?camp))
 (Type ?robot Rover)
 (Type ?camp BaseCamp)
 (Pre p0 Attached(?camp ?lander))
 (Type ?lander Lander)
 
 (Pre p1 RobotAt(?robot ?robotArea))
 (Pre p2 RobotAt(?lander ?landerArea))
 (VarDifferent ?robotArea ?landerArea)

 (Sub s1 !move_to(?robot ?landerArea))
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
 (Pre p2 RobotAt(?lander ?area))
 (Sub s1 !pickup_basecamp(?robot ?camp))
 (Constraint Equals(s1,task))
)

### Transfer all samples
(:method
 (Head transfer_all_samples(?robot1 ?robot2))
 (Sub s1 transfer_filled_containers(?robot1 ?robot2))
 (Constraint Starts(s1,task))
 (Sub s2 transfer_empty_containers(?robot1 ?robot2))
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

### Transfer filled samples
(:method
 (Head transfer_filled_containers(?robot1 ?robot2))
 
 (Pre p0 ContainerAt(?container ?robot1 ?filled))
 (Values ?filled filled)
 (Pre p1 RobotAt(?robot1 ?robotArea))
 (Pre p2 RobotAt(?robot2 ?robotArea))
 (Sub s1 !transfer_sample(?robot1 ?robot2 ?container))
 (Constraint Starts(s1,task))
 (Sub s2 transfer_filled_containers(?robot1 ?robot2))
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

(:method
 (Head transfer_filled_containers(?robot1 ?robot2))
 (Values ?robot1 rover1)
 (ResourceUsage FilledSampleStorageCapacityRover 5)
 (Constraint Duration[2,INF](task))
)

(:method
 (Head transfer_filled_containers(?robot1 ?robot2))
 (Values ?robot1 shuttle1)
 (ResourceUsage FilledSampleStorageCapacityShuttle 5)
 (Constraint Duration[2,INF](task))
)

### Transfer empty containers
(:method
 (Head transfer_empty_containers(?robot1 ?robot2))
 (Pre p0 ContainerAt(?container ?robot1 ?empty))
 (Values ?empty empty)
 (Pre p1 RobotAt(?robot1 ?robotArea))
 (Pre p2 RobotAt(?robot2 ?robotArea))
 (Sub s1 !transfer_sample(?robot1 ?robot2 ?container))
 (Constraint Starts(s1,task))
 (Sub s2 transfer_empty_containers(?robot1 ?robot2))
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

(:method
 (Head transfer_empty_containers(?robot1 ?robot2))
 (Values ?robot1 shuttle1)
 (ResourceUsage EmptySampleStorageCapacityShuttle 5)
 (Constraint Duration[2,INF](task))
)

(:method
 (Head transfer_empty_containers(?robot1 ?robot2))
 (Values ?robot1 rover1)
 (ResourceUsage EmptySampleStorageCapacityRover 5)
 (Constraint Duration[2,INF](task))
)


### Transfer charged batteries
(:method
 (Head transfer_charged_batteries(?robot1 ?robot2))
 (Pre p0 BatteryAt(?battery ?robot1 ?charged))
 (Values ?charged charged)
 (Pre p1 RobotAt(?robot1 ?robotArea))
 (Pre p2 RobotAt(?robot2 ?robotArea))
 (Sub s1 !transfer_battery(?robot1 ?robot2 ?battery))
 (Constraint Starts(s1,task))
 (Sub s2 transfer_charged_batteries(?robot1 ?robot2))
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

(:method
 (Head transfer_charged_batteries(?robot1 ?robot2))
 (Values ?robot1 shuttle1)
 (ResourceUsage ChargedBatteryStorageCapacityShuttle 5)
 (Constraint Duration[2,INF](task))
)

### Transfer discharged batteries
(:method
 (Head transfer_discharged_batteries(?robot1 ?robot2))
 (Pre p0 BatteryAt(?battery ?robot1 ?discharged))
 (Values ?discharged discharged)
 (Pre p1 RobotAt(?robot1 ?robotArea))
 (Pre p2 RobotAt(?robot2 ?robotArea))
 (Sub s1 !transfer_battery(?robot1 ?robot2 ?battery))
 (Constraint Starts(s1,task))
 (Sub s2 transfer_discharged_batteries(?robot1 ?robot2))
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

(:method
 (Head transfer_discharged_batteries(?robot1 ?robot2))
 (Values ?robot1 rover1)
 (ResourceUsage DischargedBatteryStorageCapacityRover 5)
 (Constraint Duration[2,INF](task))
)

# 
(:method
 (Head deposit_samples(?robot ?lander))
 (Pre p1 RobotAt(?robot ?robotArea))
 (Pre p2 RobotAt(?lander ?landerArea))
 (VarDifferent ?robotArea ?landerArea)
 (Sub s1 !move_to(?robot ?landerArea))
 (Constraint Starts(s1,task))
 (Sub s2 transfer_filled_containers(?robot ?lander))
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

(:method
 (Head scenario_test())
 (Sub s1 rendezvous(?rover ?shuttle))
 (Values ?rover rover1)
 (Values ?shuttle shuttle1)
 (Constraint Starts(s1,task))
 (Sub s2 deposit_samples(?shuttle ?lander))
 (Values ?lander lander1)
 (Constraint Finishes(s2,task))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)