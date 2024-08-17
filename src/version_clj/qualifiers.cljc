(ns version-clj.qualifiers)

(def default-qualifiers
  "Order Map for well-known Qualifiers."
  { "alpha"     0 "a"         0
    "beta"      1 "b"         1
    "milestone" 2 "m"         2
    "rc"        3 "cr"        3
    "snapshot"  5
    ""          6 "final"     6 "stable"    6 "release"   6})
