#!/bin/bash

working_folder="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
srcdir="$working_folder"/src
classdir="$working_folder"/bin
modeldir="$working_folder"/models/Herman

# compile
javac -classpath "$classdir" "$srcdir"/*.java -d "$classdir"

# run the experiments
for epsilon1 in 0.0001 0.001 0.01 0.1
do
  echo "epsilon1=$epsilon1"
  delta=0.01

  resultdir="$working_folder"/results/Herman"$epsilon1"
  resultdir2="$working_folder"/results/Herman-Exact"$epsilon1"
  sampledir="$working_folder"/results/Herman-Sample"$epsilon1"

  mkdir -p $resultdir
  mkdir -p $resultdir2
  mkdir -p $sampledir

  for file in $modeldir/*.tra; do
    #echo $file
    fileNameSim=$(echo $file | rev | cut -d / -f 1 | rev | cut -d "." -f 1)
    echo $fileNameSim

    #repeat 5 times for each model
    for i in {1..5}
    do
      filename="$fileNameSim"-"$i"
      fileLog="$resultdir"/"$filename".log
      {

        inputLab="$modeldir"/"$fileNameSim".lab
        inputTra="$modeldir"/"$fileNameSim".tra

        sampleLab="$sampledir"/sample-"$filename".lab
        sampleTra="$sampledir"/sample-"$filename".tra

        echo "Sampling $filename"
        java -classpath "$classdir" Sampling $inputLab $inputTra $sampleLab $sampleTra $epsilon1 $delta

        for epsilon2 in 0.00001 0.0001 0.001 0.01 0.1
        do
          echo "epsilon2=$epsilon2"
          outLabLocalExact="$resultdir2"/local-"$filename"-"$epsilon2".lab
          outTraLocalExact="$resultdir2"/local-"$filename"-"$epsilon2".tra

          outLabLocal2Exact="$resultdir2"/local2-"$filename"-"$epsilon2".lab
          outTraLocal2Exact="$resultdir2"/local2-"$filename"-"$epsilon2".tra

          outLabApproxExact="$resultdir2"/approx-"$filename"-"$epsilon2".lab
          outTraApproxExact="$resultdir2"/approx-"$filename"-"$epsilon2".tra

          outLabLocal="$resultdir"/local-"$filename"-"$epsilon2".lab
          outTraLocal="$resultdir"/local-"$filename"-"$epsilon2".tra

          outLabLocal2="$resultdir"/local2-"$filename"-"$epsilon2".lab
          outTraLocal2="$resultdir"/local2-"$filename"-"$epsilon2".tra

          outLabApprox="$resultdir"/approx-"$filename"-"$epsilon2".lab
          outTraApprox="$resultdir"/approx-"$filename"-"$epsilon2".tra

          echo "Exact Model"
           #echo "Local Distance $filename"
          java -classpath "$classdir" LocalDistanceMerge $inputLab $inputTra $outLabLocalExact $outTraLocalExact $epsilon2

          #echo "Optimized Local Distance $filename"
          java -classpath "$classdir" Merging $inputLab $inputTra $outLabLocal2Exact $outTraLocal2Exact $epsilon2

          #echo "Approx Partition Refinement $filename"
          java -classpath "$classdir" ApproximatePartitionRefinement $inputLab $inputTra $outLabApproxExact $outTraApproxExact $epsilon2
          echo ""
          echo "Sample Model"

          #echo "Local Distance $filename"
          java -classpath "$classdir" LocalDistanceMerge $sampleLab $sampleTra $outLabLocal $outTraLocal $epsilon2

          #echo "Optimized Local Distance $filename"
          java -classpath "$classdir" Merging $sampleLab $sampleTra $outLabLocal2 $outTraLocal2 $epsilon2

          #echo "Approx Partition Refinement $filename"
          java -classpath "$classdir" ApproximatePartitionRefinement $sampleLab $sampleTra $outLabApprox $outTraApprox $epsilon2

          echo ""
        done
      } | tee $fileLog
    done
  done
done