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

(MaxArgs 5)

(PredicateSymbols
# Robot state:
  ArmPosture TorsoPosture RobotAt Holding PersonAt
# Environment state:
  On DoorState In
  Connected Type
# Object types:
  Office Kitchen ConferenceRoom StudentRoom
  Door
  Table Cupboard Chair
  Person
  Mug Mail Fork Knife Spoon CoffeeMachine

# Operators:
  !pick_up_object !place_object !move_torso !move_base !move_arm
  !observe_area
  # optional:
  #!open_door !close_door !follow_person

# Methods
  move_object serve_coffee_to_guest
  #approach_person deliver_mail cleanup_table search_object explore_kb_updates

  adapt_arm adapt_torso assume_driving_pose assume_manipulation_pose drive get_object put_object

  Future
  Eternity)

(Resource armManCapacity 1)

(Resource navigationCapacity 1)
##
(Resource armCapacity 1)

(StateVariable RobotAt 2 n)
(StateVariable ArmPosture 2 n)
(StateVariable Holding 2 n)
##
(StateVariable On 1 mug1 mug2 mug3)
#(StateVariable In 1 ...)


################################
####  OPERATORS ################

# MOVE_BASE
(:operator
 (Head !move_base(?toArea))
 (Pre p1 RobotAt(?fromArea))
 (Constraint OverlappedBy(task,p1))
# (Constraint Duration[5000,INF](task))
 (Add e1 RobotAt(?toArea))
 (Constraint Meets(task,e1)) # too restrictive for stefan's exmo
 (Del p1)
 (ResourceUsage navigationCapacity 1)
)

# MOVE_TORSO
(:operator
 (Head !move_torso(?newPosture))
 (Pre p1 TorsoPosture(?oldPosture))
 (Constraint OverlappedBy(task,p1))
 (Del p1)
 (Add e1 TorsoPosture(?newPosture))
 (Constraint Duration[4000,INF](task))
)

# PICK_UP_OBJECT
(:operator
 (Head !pick_up_object(?obj ?arm))
 (Pre p1 On(?obj ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea))
 (Pre p4 Holding(?nothing))
 (Values ?nothing nothing)
 (Del p1)
 (Del p4)
 (Add e1 Holding(?obj))

 (Constraint OverlappedBy(task,p1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint OverlappedBy(task,p4))
 (Constraint Meets(p4,e1))
 (Constraint Duration[4000,INF](task))

 (ResourceUsage 
    (Usage armManCapacity 1))
)

# place_object
(:operator
 (Head !place_object(?obj ?plArea))

 (Pre p1 Holding(?obj))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?plArea ?mArea))
# (Pre p4 ArmPosture(?armPosture)) # TODO probably not necessary
# (Values ?armPosture ArmToSidePosture)
# (Pre p5 TorsoPosture(?torsoPosture)) # not necessary
# (Values ?torsoPosture TorsoUpPosture)
 (Del p1)
 (Add e1 Holding(?nothing))
 (Values ?nothing nothing)
 (Add e2 On(?obj ?plArea))

 (Constraint OverlappedBy(task,p1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint Meets(p1,e1))
 # TODO Which constraint for effect? 
 (Constraint Duration[4000,INF](task))
 
 (ResourceUsage 
    (Usage armManCapacity 1))
 )

# move_arm
(:operator
 (Head !move_arm(?newPosture))
 (Pre p1 ArmPosture(?oldPosture))
 (Del p1)
 (Add e1 ArmPosture(?newPosture))

 (Values ?oldPosture ArmUnTuckedPosture ArmTuckedPosture ArmCarryPosture ArmUnnamedPosture ArmToSidePosture)
 (Values ?newPosture ArmUnTuckedPosture ArmTuckedPosture ArmCarryPosture ArmToSidePosture)

 (ResourceUsage 
    (Usage armManCapacity 1))
 
 (Constraint Duration[4000,INF](task))
 (Constraint OverlappedBy(task,p1))
 (Constraint Overlaps(task,e1))
 )


# observe_area
(:operator
 (Head !observe_area(?plArea))
 (Pre p1 RobotAt(?robotArea))    
 (Pre p2 Connected(?plArea ?robotArea ?preArea)) # TODO query SEMAP
 (Constraint During(task,p1))
 (Constraint During(task,p2))
 (Constraint Duration[4000,INF](task))
)


################################

#(FluentResourceUsage 
#  (Usage leftArm1 1) 
#  (Fluent Holding)
#  (Param 2 leftArm1)
#)
#
#(FluentResourceUsage 
#  (Usage rightArm1 1) 
#  (Fluent Holding)
#  (Param 2 rightArm1)
#)

#################################


### adapt torso
(:method
 (Head adapt_torso(?newPose))
 (Pre p1 TorsoPosture(?oldPose))
 (VarDifferent ?newPose ?oldPose) 
 (Sub s1 !move_torso(?newPose))
 (Constraint Equals(s1,task))
 )

(:method
 (Head adapt_torso(?posture))
 (Pre p1 TorsoPosture(?posture))
 (Constraint Duration[10,INF](task))
 (Constraint During(task,p1))
 )


### adapt arm
(:method  # Arm already there. Nothing to do.
 (Head adapt_arm(?posture))
 (Pre p1 ArmPosture(?posture))
 (Constraint Duration[10,INF](task))
 (Constraint During(task,p1))
)

(:method
 (Head adapt_arm(?posture))
 (Pre p1 ArmPosture(?currentposture))
 (VarDifferent ?posture ?currentposture)
 (Sub s1 !move_arm(?posture))
 (Constraint Equals(s1,task))
)

### drive
(:method    # already there
 (Head drive(?area))
  (Pre p1 RobotAt(?area))
  (Constraint During(task,p1))
  (Constraint Duration[10,INF](task))
)

(:method
 (Head drive(?toArea))
 (Pre p1 RobotAt(?fromArea))
 (VarDifferent ?toArea ?fromArea)
 (Sub s1 assume_driving_pose())
 (Constraint Starts(s1,task))
 (Sub s2 !move_base(?toArea))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

### assume_driving_pose
(:method
 (Head assume_driving_pose()) # holding nothing
  (Pre p1 Holding(?nothing))
  (Values ?nothing nothing)
  (Values ?armTuckedPosture ArmTuckedPosture)
  (Values ?torsoDownPosture TorsoDownPosture)
  (Sub s1 adapt_torso(?torsoDownPosture))
  (Sub s2 adapt_arm(?armTuckedPosture))
  (Ordering s1 s2)
)

(:method
 (Head assume_driving_pose()) # holding something
  (Pre p1 Holding(?object))
  (NotValues ?object nothing)
  (Values ?armCarryPosture ArmCarryPosture)
  (Values ?torsoMiddlePosture TorsoMiddlePosture)
  (Sub s1 adapt_torso(?torsoMiddlePosture))
  (Sub s2 adapt_arm(?armCarryPosture))
  (Ordering s1 s2)
)

### assume_manipulation_pose
(:method 
 (Head assume_manipulation_pose())
  (Sub s1 adapt_torso(?torsoUpPosture))
  (Values ?torsoUpPosture TorsoUpPosture)
  (Sub s2 adapt_arm(?armSidePosture))
  (Values ?armSidePosture ArmToSidePosture)
  (Ordering s1 s2)
)

### grasp_object
#(:method  # TODO Could be merged into get_object
#  (Head grasp_object(?object))
#  (Pre p1 RobotAt(?robotArea))
#  (Pre p2 Connected(?objectArea ?robotArea))
#  (Pre p3 On(?object ?objectArea))
#  (Sub s1 assume_manipulation_pose(?manArea))
#  (Sub s2 !observe_area(?objectArea))
#  (Sub s3 !pick_up_object(?object))
  
#  (Ordering s1 s2)
#  (Ordering s2 s3)
#  (Constraint Starts(s1,task))
#  (Constraint Finishes(s3,task))
#  (Constraint Before(s1,s2))
#  (Constraint Before(s2,s3))
#) 

### GET_OBJECT
# 1. already holding another object
## put it on the counter TODO LEFT OUT FOR NOW

# 2. Robot is near the object
#(:method   # TODO Could be left out: then drive would have no subtasks
#  (Head get_object(?object))
#  (Pre p1 RobotAt(?preArea))
#  (Pre p2 Connected(?plArea ?manArea ?preArea))
#  (Pre p3 On(?object ?plArea))
#
#  (Sub s1 assume_manipulation_pose(?manArea))
#  (Sub s2 !observe_area(?plArea))
#  (Sub s2 !pick_up_object(?object ?arm))
#
#  (Ordering s1 s2)
#  (Constraint Before(s1,s2))
#  (Ordering s2 s3)
#  (Constraint Before(s2,s3))
#  (Constraint Starts(s1,task))
#  (Constraint Finishes(s3,task))
#)

# 3. Robot is not near the object
# TODO how to model NOT near the object???
(:method 
  (Head get_object(?object))
  (Pre p2 Connected(?objectArea ?manArea))
  (Pre p3 On(?object ?objectArea))

  (Sub s1 drive(?manArea))
  (Sub s2 assume_manipulation_pose(?manArea))
  (Sub s3 !observe_area(?plArea))
  (Sub s4 !pick_up_object(?object))

  (Ordering s1 s2)
  (Ordering s2 s3)
  (Ordering s3 s4)
  (Constraint Before(s1,s2))
  (Constraint Before(s2,s3))
  (Constraint Before(s3,s4))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s4,task))
)

### put_object
(:method 
  (Head put_object(?object ?plArea))

  (Pre p1 Holding(?object)) # could be left out
  (Pre p3 Connected(?plArea ?manArea))

  (Sub s1 drive(?manArea))
  (Sub s2 assume_manipulation_pose())
  (Sub s3 !place_object(?object ?plArea))

  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Starts(s1,task))
  (Constraint Finishes(s3,task))
  (Constraint Before(s1,s2))
  (Constraint Before(s2,s3))
)

### MOVE_OBJECT
(:method 
  (Head move_object(?object ?toArea))
  (Pre p1 On(?object ?fromArea))
  (Sub s1 get_object(?object))
  (Sub s2 put_object(?object ?toArea))
  (Ordering s1 s2)
  (Constraint Before(s1,s2))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s2,task))
)


### SERVE_COFFEE_TO_GUEST
(:method 
  (Head serve_coffee_to_guest(?guest))

  (Pre p0 Type(?coffeetype ?coffee))
  (Values ?coffeetype Coffee)
  (Pre p1 On(?coffee ?fromArea))
  (Pre p2 PersonAt(?guest ?guestArea))

#  (Values ?fromArea placingAreaEastLeftTable1 placingAreaWestLeftTable1 placingAreaNorthLeftTable2 placingAreaSouthLeftTable2)
  
  (Sub s1 move_object(?coffee ?guestArea))
)



#### serve both # TODO: update
(:method
  (Head serve_coffee_to_guest_with_milk_and_sugar(?placingArea)) # TODO use sittingArea

  (Pre p0 Type(?coffeetype ?coffee))
  (Values ?coffeetype Coffee)

 (Values ?placingArea placingAreaEastLeftTable1 placingAreaWestLeftTable1 placingAreaNorthLeftTable2 placingAreaSouthLeftTable2 placingAreaNorthRightTable2)
  
  (Pre p1 Type(?milktype ?milk))
  (Values ?milktype Milk)

#  (Pre p2 Type(?sugartype ?sugar))
#  (Values ?sugartype Sugar)
  (Values ?sugar sugarPot1 sugarPot2)
  
  (Sub s1 move_object(?coffee ?placingArea))
  (Sub s2 move_object(?milk ?placingArea))
  (Sub s3 move_object(?sugar ?placingArea))

  (Ordering s1 s2)
#  (Constraint Starts(s1,task))
#(Constraint (s1,task))
#  (Ordering s1 s3)
  (Ordering s2 s3)
#  (Constraint Finishes(s3,task))

(Constraint Before(s1,s3))
(Constraint Before(s2,s3))  
)
