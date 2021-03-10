#!/bin/bash

working_folder="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
srcdir="$working_folder"/src
classdir="$working_folder"/bin
modeldir="$working_folder"/models/leader
resultdir="$working_folder"/results/leader


# compile
javac -classpath "$classdir" "$srcdir"/*.java -d "$classdir"

# run the experiments
mkdir -p $resultdir

epsilon1=0.0001 
delta=0.99 
epsilon2=0.0001
fileError=errors.log

for file in $modeldir/*.tra; do
  echo $file
  filename=$(echo $file | rev | cut -d / -f 1 | rev | cut -d "." -f 1)
  echo $filename

  inputLab="$modeldir"/"$filename".lab
  inputTra="$modeldir"/"$filename".tra

  sampleLab="$resultdir"/sample-"$filename".lab
  sampleTra="$resultdir"/sample-"$filename".tra

  outLabLocal="$resultdir"/local-"$filename".lab
  outTraLocal="$resultdir"/local-"$filename".tra

  outLabApprox="$resultdir"/approx-"$filename".lab
  outTraApprox="$resultdir"/approx-"$filename".tra

  #-Xss20480k
  echo "Sampling $filename"
  java -classpath "$classdir" Sampling $inputLab $inputTra $sampleLab $sampleTra $epsilon1 $delta

  echo "Local Merge $filename"
  java -classpath "$classdir" Merging $sampleLab $sampleTra $outLabLocal $outTraLocal $epsilon2 

  #echo "Approx Partition Refinement $filename"
  java -classpath "$classdir" ApproximatePartitionRefinement $sampleLab $sampleTra $outLabApprox $outTraApprox $epsilon2 

done