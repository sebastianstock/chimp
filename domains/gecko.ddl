(HybridHTNDomain GeckoDomain)

(MaxArgs 5)

(PredicateSymbols
  crossState
  hasConveyor

  !move_forward
  !move_up

  channelRL
  channelR1L1 channelR2L2

  Future
  Eternity
)

# State Variables:
# ConveyerController: Still, MovingForward, MovingBackward
# CrossTransferController: Down, Up, Moving
# TMcontroller: Idle, ChannelR1L1, ChannelR2L2, ChannelR3L3

(StateVariable crossState 1 cross1)
(StateVariable crossState 1 cross2)

(Resource conveyor1Movement 1)
(Resource conveyor2Movement 1)

# Components (Instances of state variables)
# TM
# TMConveyor
# Cross1
# Conveyor1
# Cross2
# Conveyor2
# Cross3
# Conveyor3

# Synchronizations:
# ChannelR1L1
# ChannelR2L2
# ChannelR3L3


(:operator
 (Head !move_forward(?conveyor))
 (Constraint Duration[1000,INF](task))

 (ResourceUsage 
    (Usage conveyor1Movement 1)
    (Param 1 conveyor1))

 (ResourceUsage 
    (Usage conveyor2Movement 1)
    (Param 1 conveyor2))
)

(:operator
 (Head !move_backward(?conveyor))
 (Constraint Duration[1000,INF](task))

 (ResourceUsage 
    (Usage conveyor1Movement 1)
    (Param 1 conveyor1))
 (ResourceUsage 
    (Usage conveyor2Movement 1)
    (Param 1 conveyor2))
)

# TODO: Temporal relations
(:operator
 (Head !move_up(?cross))
 (Pre p1 crossState(?cross ?down))
 (Del p1)
 (Add e1 crossState(?cross ?up))
 (Values ?up up)
 (Values ?down down)
 (Constraint Duration[3000,3000](task))
 (Constraint Meets(p1,task))
 (Constraint Meets(task,e1))
)

# TODO: Temporal relations
(:operator  # could be merged with previous operator
 (Head !move_down(?cross))
 (Pre p1 crossState(?cross ?up))
 (Add e1 crossState(?cross ?down))
 (Values ?up up)
 (Values ?down down)
 (Constraint Duration[3000,3000](task))
 (Constraint Meets(p1,task))
 (Constraint Meets(task,e1))
)


(:method
 (Head channelRL(?cross))
 (Pre p0 hasConveyor(?cross ?conveyor))
 (Pre p1 crossState(?cross ?up))
 (Values ?up up)
 (Sub s1 !move_forward(?conveyor))
 (Constraint Equals(s1,task))
)

(:method
 (Head channelRL(?cross))
 (Pre p0 hasConveyor(?cross ?conveyor))
 (Pre p1 crossState(?cross ?down))
 (Values ?down down)
 (Sub s0 !move_up(?cross))
 (Sub s1 !move_forward(?conveyor))
 (Constraint Meets(s0,s1))
 (Ordering s0 s1)
)


(:method
 (Head channelR1L1())
 (Pre p1 crossState(?cross ?up))
 (Values ?cross cross1)
 (Values ?up up)
 (Sub s1 !move_forward(?conveyor))
 (Values ?conveyor conveyor1)
 (Constraint Equals(s1,task))
)

(:method
 (Head channelR1L1())
 (Pre p1 crossState(?cross ?down))
 (Values ?cross cross1)
 (Values ?down down)
 (Sub s0 !move_up(?cross))
 (Sub s1 !move_forward(?conveyor))
 (Values ?conveyor conveyor1)
 (Constraint Meets(s0,s1))
 (Ordering s0 s1)
)


(:method
 (Head channelR2L2())
 (Pre p1 crossState(?cross ?up))
 (Values ?cross cross2)
 (Values ?up up)
 (Sub s1 !move_forward(?conveyor))
 (Values ?conveyor conveyor2)
 (Constraint Equals(s1,task))
)

(:method
 (Head channelR2L2())
 (Pre p1 crossState(?cross ?down))
 (Values ?cross cross2)
 (Values ?down down)
 (Sub s0 !move_up(?cross))
 (Sub s1 !move_forward(?conveyor))
 (Values ?conveyor conveyor2)
 (Constraint Meets(s0,s1))
 (Ordering s0 s1)
)
