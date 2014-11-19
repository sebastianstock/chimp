##################
# Reserved words #
#################################################################
#                                                               #
##   Head                                                        #
##   Resource                                                    #
##   Sensor                                                      #
##   ContextVariable                                             #
#   HTNOP                                                       #
##   PlanningOperator                                            #
#   HybridHTNDomain                                             #
##   Constraint                                                  #
##   RequiredState						#
##   AchievedState						#
##   RequriedResoruce						#
##   All AllenIntervalConstraint types                           #
##   '[' and ']' should be used only for constraint bounds       #
##   '(' and ')' are used for parsing                            #
#                                                               #
#
#   StateVariable
#   FluentResourceUsage
#   Param
#################################################################

(HybridHTNDomain TestDomain)

(MaxArgs 5)

#(Sensor RobotProprioception) #proprioception
#(Sensor atLocation) #tabletop perception

#(ContextVariable RobotProprioception) #proprioception
#(ContextVariable atLocation) #tabletop perception


#(Observable RobotProprioception) #proprioception
#(Observable atLocation) #tabletop perception

#(Controllable RobotProprioception) #proprioception
#(Controllable atLocation) #tabletop perception

#(Resource arm 1)

(StateVariable On 1 mug1 mug2 mug3)
(StateVariable HasArmPosture 1 rightArm1 leftArm1)

(FluentResourceUsage Holding leftArm1
    (Param 2 leftArm1)
)

(:operator
 (Head !move_base(?toArea))
 (Pre p1 RobotAt(?fromArea))
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (Add e1 RobotAt(?toArea))
 (Del p1)
 (Add e9 On(?fromArea))
)

(:operator
 (Head !tuck_arms(?leftGoal ?rightGoal))
 (Pre p1 HasArmPosture(?leftArm ?oldLeft))
 (Pre p2 HasArmPosture(?rightArm ?oldRight))
 (Del p1)
 (Del p2)
 (Add e1 HasArmPosture(?leftArm ?leftGoal))
 (Add e2 HasArmPosture(?rightArm ?rightGoal))
# (Type ?oldLeft ArmPosture)
 (Values ?leftArm leftArm1)
 (Values ?rightArm rightArm1)
)

(:operator
 (Head !move_torso(?newPosture))
 (Pre p1 HasTorsoPosture(?oldPosture))
 (Del p1)
 (Add e1 HasTorsoPosture(?newPosture))
)


(:operator
 (Head !pick_up_object(?obj ?arm))

 (Pre p1 On(?obj ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
 (Add e1 Holding(?obj ?arm))

# (Constraint StartedBy(Head,req1))
# (Constraint OverlappedBy(Head,req1))
# (Constraint Duration[5,INF](Head))
 (Del p1)
)


#(:operator
# (Head op::!move_base(Area:?toArea Area:?fromArea))
# # TODO set negative
# (Pre p1 trixi::RobotAt(Area:?fromArea))
# (Constraint StartedBy(Head,req1))
# (Constraint OverlappedBy(Head,req1))
# (Constraint Duration[5,INF](Head))
# (Add e1 trixi::RobotAt(Area:?toArea))
#)

