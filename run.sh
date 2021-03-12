#!/bin/bash

working_folder="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
srcdir="$working_folder"/src
classdir="$working_folder"/bin
modeldir="$working_folder"/models/Herman
resultdir="$working_folder"/results/Herman


# compile
javac -classpath "$classdir" "$srcdir"/*.java -d "$classdir"

# run the experiments
mkdir -p $resultdir

epsilon1=0.00001 
delta=0.99 


for file in $modeldir/*.tra; do
  #echo $file
  fileNameSim=$(echo $file | rev | cut -d / -f 1 | rev | cut -d "." -f 1)
  echo $fileNameSim

  #repeat 5 times for each model
  for i in {1..5}
  do
    fileLog="$resultdir"/log-"$i".log
    {
      filename="$fileNameSim"-"$i"

      inputLab="$modeldir"/"$fileNameSim".lab
      inputTra="$modeldir"/"$fileNameSim".tra

      sampleLab="$resultdir"/sample-"$filename".lab
      sampleTra="$resultdir"/sample-"$filename".tra

      #echo "Sampling $filename"
      java -classpath "$classdir" Sampling $inputLab $inputTra $sampleLab $sampleTra $epsilon1 $delta 

      for epsilon2 in 0.00001 0.0001 0.001 0.01 0.1
      do
        echo "epsilon2=$epsilon2" 
        outLabLocal="$resultdir"/local-"$filename"-"$epsilon2".lab
        outTraLocal="$resultdir"/local-"$filename"-"$epsilon2".tra

        outLabApprox="$resultdir"/approx-"$filename"-"$epsilon2".lab
        outTraApprox="$resultdir"/approx-"$filename"-"$epsilon2".tra
        #echo "Local Distance $filename"
        java -classpath "$classdir" Merging $sampleLab $sampleTra $outLabLocal $outTraLocal $epsilon2 
      
        #echo "Approx Partition Refinement $filename"
        java -classpath "$classdir" ApproximatePartitionRefinement $sampleLab $sampleTra $outLabApprox $outTraApprox $epsilon2 
      done
    } | tee $fileLog
  done
done