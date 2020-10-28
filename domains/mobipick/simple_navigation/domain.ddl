(HybridHTNDomain safe_navigation)

(MaxArgs 5)

(PredicateSymbols
  arm_posture holding robot_at
# Operators:
  move_arm
  move_base
# Methods
  drive
  adapt_arm
)

(Resource arm_man_capacity 1)

(StateVariable arm_posture 2 n)

################################
####  OPERATORS ################

# move_arm
(:operator
 (Head move_arm(?arm ?old_posture ?new_posture ?keep_gripper_orientation))
 (Pre p1 arm_posture(?arm ?old_posture))
 (Del p1)
 (Add e1 arm_posture(?arm ?new_posture))

 (ResourceUsage
    (Usage arm_man_capacity 1))

 (Constraint Duration[2000,INF](task))
)

# move_base
(:operator
 (Head move_base(?from_area ?to_area))
 (Pre p1 robot_at(?from_area))
 (Del p1)
 (Add e1 robot_at(?to_area))
 (Constraint Duration[4000,INF](task))
)


### DRIVE
(:method    # already there
 (Head drive(?area))
  (Pre p1 robot_at(?area))
  (Constraint During(task,p1))
)

# Robot is holding nothing: tuck arm
(:method
 (Head drive(?to_area))
 (Pre p0 holding(ur5 nothing))
 (Pre p1 robot_at(?from_area))
 (VarDifferent ?to_area ?from_area)
 (Sub s1 adapt_arm(ur5 tucked))
 (Constraint Starts(s1,task))
 (Sub s2 move_base(?from_area ?to_area))
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
)

# Robot is holding an object: move arm to transport pose
(:method
 (Head drive(?to_area))
 (Pre p0 holding(ur5 ?obj))
 (NotValues ?obj nothing)
 (Pre p1 robot_at(?from_area))
 (VarDifferent ?to_area ?from_area)
 (Sub s1 adapt_arm(ur5 transport))
 (Constraint Starts(s1,task))
 (Sub s2 move_base(?from_area ?to_area))
 #(Ordering s1 s2)
 (Constraint Before(s1,s2))
)

### adapt arm
(:method  # Arm already there. Nothing to do.
 (Head adapt_arm(?arm ?posture))
 (Pre p1 arm_posture(?arm ?posture))
 (Constraint During(task,p1))
)

# not holding an object: use_current_orientation_constraint=false
(:method
 (Head adapt_arm(?arm ?posture))
 (Pre p0 holding(ur5 ?obj))
 (Values ?obj nothing)
 (Pre p1 arm_posture(?arm ?currentposture))
 (VarDifferent ?posture ?currentposture)
 (Sub s1 move_arm(?arm ?old_posture ?posture false))
 (Constraint Equals(s1,task))
)

# holding an object: use_current_orientation_constraint=true
(:method
 (Head adapt_arm(?arm ?posture))
 (Pre p0 holding(ur5 ?obj))
 (NotValues ?obj nothing)
 (Pre p1 arm_posture(?arm ?currentposture))
 (VarDifferent ?posture ?currentposture)
 (Sub s1 move_arm(?arm ?old_posture ?posture true))
 (Constraint Equals(s1,task))
)

