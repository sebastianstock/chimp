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

(HybridHTNDomain AirbusMobipickDomain)

(MaxArgs 5)

(MaxIntegerArgs 2)

(PredicateSymbols
# Robot state:
  ArmPosture Holding
# Environment state:
  On
  Type
  ReachableFrom
  RobotAt
# Object types:
  Powerdrill
  BatteryLevel

# Operators:
  !recognize_objects
  !grasp_object
  !hand_over
  !place_object
  !move_arm
  !move_base

# Methods

  move_object
  get
  put
  drive
  adapt_arm
  drive_home
  recognize
  assume_home_pose
  empty_hand

# Other
  Future)


################################
####  OPERATORS ################

# GRASP_OBJECT
(:operator
 (Head !grasp_object(?arm ?obj ?grasptype))
 (Pre p1 On(?obj ?fromArea))
 (Pre p2 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Pre p3 ReachableFrom(?fromArea ?robotArea ?true))
 (Values ?true true)
 (Pre p4 ArmPosture(?arm ?oldPosture))
 (Del p1)
 (Del p2)
 (Del p4)
 (Add e1 Holding(?arm ?obj))
 (Add e2 ArmPosture(?arm ?newPosture))
 (Values ?newPosture undefined)

 (Constraint OverlappedBy(task,p1))
 (Constraint OverlappedBy(task,p2))
 (Constraint Meets(p2,e1))
 (Constraint Meets(p4,e2))
 (Constraint Duration[4000,INF](task))

 (ResourceUsage
    (Usage armManCapacity 1))
)

 # place_object
(:operator
 (Head !place_object(?arm ?obj ?plArea))
 (Pre p1 Holding(?arm ?obj))
 (Pre p2 ArmPosture(?arm ?oldPosture))
 (Pre p3 ReachableFrom(?plArea ?robotArea ?true))
 (Values ?true true)
 (Pre p4 RobotAt(?robotArea))
 (Del p1)
 (Del p2)
 (Add e1 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Add e2 On(?obj ?plArea))
 (Add e3 ArmPosture(?arm ?newPosture))
 (Values ?newPosture undefined)

 (Constraint OverlappedBy(task,p1))
 (Constraint Meets(p1,e1))
 (Constraint Meets(p2,e3))
 (Constraint Duration[4000,INF](task))

 (ResourceUsage
    (Usage armManCapacity 1))
 )

# move_arm
(:operator
 (Head !move_arm(?arm ?newPosture ?override_tcp_orientation))
 (Pre p1 ArmPosture(?arm ?oldPosture))
 (Del p1)
 (Add e1 ArmPosture(?arm ?newPosture))

 (ResourceUsage
    (Usage armManCapacity 1))

 (Constraint Duration[2000,INF](task))
 )


# move_base
(:operator
 (Head !move_base(?toArea)(?batteryStart ?batteryEnd))
 (Pre p1 RobotAt(?fromArea))
 (Del p1)
 (Pre p2 BatteryLevel()(?batteryStart))
 (Del p2)
 (Add e1 RobotAt(?toArea))
 (Add e2 BatteryLevel()(?batteryEnd))
 (IC ?batteryStart > 2)
 (IC ?batteryEnd = ?batteryStart - 2)
 (Constraint Duration[4000,INF](task))
)

# recognize_objects
(:operator
 (Head !recognize_objects(?object))
 (Constraint Duration[1000,INF](task))
)


# If holding an object, bring it away. Just put it somewhere!
(:method
    (Head empty_hand())
    (Pre p1 Holding(?ur5 ?nothing))
    (Values ?ur5 ur5)
    (Values ?nothing nothing)

    # holding nothing --> nothing to do
)

### MOVE_OBJECT
(:method
  (Head move_object(?object ?toArea))
  (Sub s1 get(?ur5 ?object ?transport))
  (Values ?ur5 ur5)
  (Values ?transport transport_mobipick)
  (Sub s2 put(?object ?toArea))
  (Ordering s1 s2)
  (Constraint Before(s1,s2))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s2,task))
)

### GET
(:method
 (Head get(?arm ?obj ?grasptype))
 (Pre p0 On(?obj ?fromArea))
 (Pre p1 ReachableFrom(?fromArea ?driveArea ?true))
 (Values ?true true)
 (Sub s0 empty_hand())
 (Sub s1 drive(?driveArea))
 (Sub s2 recognize(?obj))
 (Sub s3 !grasp_object(?arm ?obj ?grasptype))
 (Ordering s0 s1)
 (Ordering s1 s2)
 (Ordering s2 s3)
 (Constraint Before(s0,s1))
 (Constraint Before(s1,s2))
 (Constraint Before(s2,s3))
)

# already holding the object
(:method
 (Head get(?arm ?obj ?grasptype))
 (Pre p0 Holding(?arm ?obj))
)

### PUT
(:method
 (Head put(?obj ?toArea))
 (Pre p0 Holding(?arm ?obj))
 (Pre p1 ReachableFrom(?toArea ?driveArea ?true))
 (Values ?true true)
 (Sub s0 drive(?driveArea))
 (Sub s1 !place_object(?arm ?obj ?toArea))
 (Ordering s0 s1)
 (Constraint Before(s0,s1))
)

# RECOGNIZE: move_arm and recognize_objects
(:method
 (Head recognize(?object))
 (Pre p0 Holding(?ur5 ?nothing))
 (Values ?nothing nothing)
 # (Sub s1 adapt_arm(?ur5 ?observe_pose))
 # (Values ?observe_pose observe100cm_right)
 (Values ?ur5 ur5)
 (Sub s2 !recognize_objects(?object))
 # (Ordering s1 s2)
 (Constraint Starts(s2,task))
 # (Constraint Before(s1,s2))
)

### DRIVE HOME
(:method
 (Head drive_home())
 (Pre p0 Type(?home_type ?home_area))
 (Values ?home_type HomePose)
 (Sub s1 drive(?home_area))
 (Constraint Equals(s1,task))
)

### DRIVE
(:method    # already there
 (Head drive(?area))
  (Pre p1 RobotAt(?area))
  (Constraint During(task,p1))
)

# Robot is holding nothing: tuck arm
(:method
 (Head drive(?toArea))
 (Pre p0 Holding(?ur5 ?nothing))
 (Values ?nothing nothing)
 (Pre p1 RobotAt(?fromArea))
 (VarDifferent ?toArea ?fromArea)
 (Sub s1 adapt_arm(?ur5 ?tucked))
 (Values ?ur5 ur5)
 (Values ?tucked tucked)
 (Constraint Starts(s1,task))
 (Sub s2 !move_base(?toArea))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

# Robot is holding an object: move arm to transport pose
(:method
 (Head drive(?toArea))
 (Pre p0 Holding(?ur5 ?obj))
 (NotValues ?obj nothing)
 (Pre p1 RobotAt(?fromArea))
 (VarDifferent ?toArea ?fromArea)
 (Sub s1 adapt_arm(?ur5 ?transport))
 (Values ?ur5 ur5)
 (Values ?transport transport)
 (Constraint Starts(s1,task))
 (Sub s2 !move_base(?toArea))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

### adapt arm
(:method  # Arm already there. Nothing to do.
 (Head adapt_arm(?arm ?posture))
 (Pre p1 ArmPosture(?arm ?posture))
 (Constraint During(task,p1))
)

# not holding an object: use_current_orientation_constraint=false
(:method
 (Head adapt_arm(?arm ?posture))
 (Pre p0 Holding(?ur5 ?obj))
 (Values ?obj nothing)
 (Values ?ur5 ur5)
 (Pre p1 ArmPosture(?arm ?currentposture))
 (VarDifferent ?posture ?currentposture)
 (Sub s1 !move_arm(?arm ?posture ?false))
 (Values ?false false)
 (Constraint Equals(s1,task))
)

# holding an object: use_current_orientation_constraint=true
(:method
 (Head adapt_arm(?arm ?posture))
 (Pre p0 Holding(?ur5 ?obj))
 (NotValues ?obj nothing)
 (Values ?ur5 ur5)
 (Pre p1 ArmPosture(?arm ?currentposture))
 (VarDifferent ?posture ?currentposture)
 (Sub s1 !move_arm(?arm ?posture ?true))
 (Values ?true true)
 (Constraint Equals(s1,task))
)

