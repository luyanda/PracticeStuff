#!/usr/bin/bash

# Create the mutants
java -cp /home/phillip/.m2/repository/com/github/javaparser/javaparser-core/3.5.14/javaparser-core-3.5.14.jar:. Mutator "$1"

# Get the class name
CLASS=$(echo $1 | cut -d . -f 1)
echo "Class name is $CLASS"

# Compile driver
javac Driver.java

# Hide the original file
mv "$1" _"$1"

# Count the number of mutants
TOTAL=$(find -type f -name "$CLASS*.java" | wc | awk '{print $2'})
echo "Total number of mutants generated: $TOTAL"

KILLED=0

# Operate on each mutant
for MUTANT in $(find -type f -name "$CLASS*.java");
do
  echo "Testing mutant with name $MUTANT"
  
  # Rename the mutant
  mv "$MUTANT" "$CLASS".java

  # Compile all that dank shit
  javac "$CLASS".java

  # Run that dank shit
  java Driver

  # Check the return value                                                                                        of that  dank shit
  if [[ "$?" -eq "1" ]]; then KILLED=$((KILLED + 1)); fi
done

# Log total number of killed mutants
echo "Total number of killed mutants: $KILLED"

# Log the ratio
echo "Score is"
echo "$KILLED / $TOTAL" | bc -l

# Restore original file
mv _"$1" "$1"
