import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class CombineMerge {
    //input
    public int numOfStates;
    public int numOfTrans;
    private Map<Integer, Map<Integer, Double>> transitions;

    private Map<Integer, Integer> labelMap;

    //output
    public int newNumOfStates;
    public int newNumOfTrans;

    private Map<Integer, Integer> newLabelMap;
    private Map<Integer, Map<Integer, Double>> newTransitions = new HashMap<>();


    public double[] discrepancy;
    public double epsilon2;
    PrintStream output0, output1 = null;

    public int iter = 0;

    public void readFile(String[] args) {
        Scanner input0 = null;
        Scanner input1 = null;

        // parse input file
        if (args.length != 5) {
            System.out.println(
                    "Use java ApproximatePartitionRefinement 0: <label> 1: <transition> 2: <outputLabelFile> 3: <outputTransition> 4: <epsilon2>");
            return;
        }
        // process the command line arguments
        try {
            input0 = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.out.printf("Input file %s not found%n", args[0]);
            System.exit(1);
        }

        try {
            input1 = new Scanner(new File(args[1]));
        } catch (FileNotFoundException e) {
            System.out.printf("Input file %s not found%n", args[1]);
            System.exit(1);
        }

        try {
            output0 = new PrintStream(new File(args[2]));
        } catch (FileNotFoundException e) {
            System.out.printf("Output file %s not created%n", args[2]);
            System.exit(1);
        }

        try {
            output1 = new PrintStream(new File(args[3]));
        } catch (FileNotFoundException e) {
            System.out.printf("Output file %s not created%n", args[3]);
            System.exit(1);
        }

        try {
            this.epsilon2 = Double.parseDouble(args[4]);
            assert this.epsilon2 < 1 : String.format("Discount factor %f should be less than 1", this.epsilon2);
            assert this.epsilon2 > 0 : String.format("Discount factor %f should be greater than 0", this.epsilon2);
        } catch (NumberFormatException e) {
            System.out.println("Discount factor not provided in the right format");
            System.exit(1);
        }

        //while (input.hasNextInt()) {
        try {
            this.labelMap = new HashMap<>();
            if(input0.hasNextLine()) input0.nextLine();
            while(input0.hasNextLine()){
                String line = input0.nextLine();
                String[] nums = line.split(":\\s");
                if(nums.length != 2){
                    System.err.println(nums[0] +" " + nums[1] );
                    System.err.println("label file format problem");
                }

                int state = Integer.parseInt(nums[0]);
                int label = nums[1].hashCode();
                labelMap.put(state, label);
            }

            this.numOfStates = input1.nextInt();
            this.numOfTrans = input1.nextInt();

            this.transitions = new HashMap<>();

            for (int j = 0; j < numOfTrans; j++) {
                int state = input1.nextInt();
                int next = input1.nextInt();
                double probability = input1.nextDouble();
                transitions.computeIfAbsent(state, x -> new HashMap<>()).put(next, probability);
            }
        } catch (NoSuchElementException e) {
            System.out.printf("Input file %s not in the correct format%n", args[0]);
        }
        // }
    }

    public void printInput() {

        System.out.println(this.numOfStates + " " + this.numOfTrans);

        for (int i : this.labelMap.keySet()) {
            System.out.print(i + ": " + this.labelMap.get(i) + "  ");
        }
        System.out.println();
        for (int i : this.transitions.keySet()) {
            Map<Integer, Double> map = transitions.get(i);
            for (int state : map.keySet()) {
                System.out.println(i + " " + state + " " + map.get(state));
            }
        }
        System.out.println();
    }




    public void printOutput() {
        System.out.println(this.newNumOfStates + " " + this.newNumOfTrans);

        for (int i : this.newLabelMap.keySet()) {
            System.out.print(i + ": " + this.newLabelMap.get(i) + "  ");
        }
        System.out.println();
        for (int i : this.newTransitions.keySet()) {
            Map<Integer, Double> map = newTransitions.get(i);
            for (int state : map.keySet()) {
                System.out.println(i + " " + state + " " + map.get(state));
            }
        }
        System.out.println();
    }

    public void printInputSimple() {
        System.out.println(this.numOfStates + " " + this.numOfTrans);

        for (int i : this.labelMap.keySet()) {
            System.out.print(i + ": " + this.labelMap.get(i) + "  ");
        }
        System.out.println();
    }

    public void printOutputSimple() {

        System.out.println(this.newNumOfStates + " " + this.newNumOfTrans);

        for (int i : this.newLabelMap.keySet()) {
            System.out.print(i + ": " + this.newLabelMap.get(i) + "  ");
        }
        System.out.println();
        System.out.println("Number of iterations = " + iter);
    }

    public void writeOutputToFile() {
        output1.println(this.newNumOfStates + " " + this.newNumOfTrans);

        output0.println(this.newLabelMap.values());
        for (int i : this.newLabelMap.keySet()) {
            output0.println(i + ": " + this.newLabelMap.get(i));
        }

        for (int i : this.newTransitions.keySet()) {
            Map<Integer, Double> map = newTransitions.get(i);
            for (int state : map.keySet()) {
                output1.println(i + " " + state + " " + map.get(state));
            }
        }
        output1.println();
    }

    public List<List<Integer>> partition;

    //partition the states by their labels
    public void createInitialPartition(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        partition = new ArrayList<>();

        for (int state : trans.keySet()) {
            boolean isAdd = false;
            for (List<Integer> list : partition) {
                int label = lMap.getOrDefault(list.get(0), -1);//lMap.get(list.get(0));
                if (lMap.getOrDefault(state,-1) == label) {
                    list.add(state);
                    isAdd = true;
                    break;
                }
            }
            //create the new list
            if (!isAdd) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add(state);
                partition.add(list);
            }
        }
    }

    // return true if split; return false if fixpoint reached
    // it is the classic partition refinement when epsilon2 == 0
    public boolean split(Map<Integer, Map<Integer, Double>> trans, double epsilon2) {

        List<List<Integer>> newPartition = new ArrayList<>();
        List<List<Integer>> tmpPartition = new ArrayList<>(partition);
        int size = partition.size();
        for (List<Integer> list : partition) {
            List<List<Integer>> splitPartition = new ArrayList<>();

            //1st iteration: build a list of distributions
            Map<Integer, Distribution> distrMap = new HashMap<>();
            for (int state : list) {
                Distribution distr = new Distribution(size);
                for (int i = 0; i < size; i++) {
                    List<Integer> other = tmpPartition.get(i);
                    // compute probability to the other partition
                    double sum = 0;
                    for (int next : other) {
                        if (trans.get(state).containsKey(next)) {
                            sum += trans.get(state).get(next);
                        }
                    }
                    distr.updateEntry(i, sum);
                }
                distrMap.put(state, distr);
            }


            //split the states
            for (int state : list) {
                Distribution distr = distrMap.get(state);
                //if isAdd, the state can be added to an existing set
                boolean isAdd = false;
                if (epsilon2 == 0) {
                    for (int i = 0; i < splitPartition.size(); i++) {
                        List<Integer> l = splitPartition.get(i);

                        int rdState = l.get(0);
                        if (distr.equals(distrMap.get(rdState))) {
                            l.add(state);
                            isAdd = true;
                            break;
                        }
                    }
                } else { //if epsilon2 > 0
                    double minAverageL1Distance = 10;
                    int minList = -1;

                    //go through the splitted sets to find a valid one to join
                    for (int i = 0; i < splitPartition.size(); i++) {
                        //sum of L1 distances between the current state and all state in a set
                        double sumL1Distance = 0;
                        //the current set
                        List<Integer> l = splitPartition.get(i);
                        //if $violate, the set is not valid for the state to join
                        boolean violate = false;
                        //go through all states in the list l
                        for (int rdState : l) {
                            Distribution rdDistr = distrMap.get(rdState);
                            double l1Distance = 2 * computeTVDistance(distr, rdDistr);
                            sumL1Distance += l1Distance;
                            if (l1Distance > epsilon2) {
                                violate = true;
                                break;
                            }
                        }
                        if(!violate){
                            double currentAvgL1Distance = sumL1Distance/l.size();
                            if(currentAvgL1Distance < minAverageL1Distance){
                                minAverageL1Distance = currentAvgL1Distance;
                                minList = i;
                            }
                        }
                        //add the state to a set if there is a valid min set
                        if(i == splitPartition.size() - 1 && minList >= 0){
                            isAdd = true;
                            List<Integer> minL = splitPartition.get(minList);
                            minL.add(state);
                        }
                    }
                }
                //if not possible to add, create a new set
                if (!isAdd) {
                    ArrayList<Integer> l = new ArrayList<>();
                    l.add(state);
                    splitPartition.add(l);
                }
            }
            newPartition.addAll(splitPartition);
        }

        //compare the new partition and the old one
        if (partition.size() == newPartition.size()) {
            return false;
        }

        partition = newPartition;
        return true;
    }

    public double computeTVDistance(Distribution d1, Distribution d2) {
        if (d1.size != d2.size) {
            System.err.println("tv distance size match");
            return -1;
        }
        double sum = 0;
        for (int i = 0; i < d1.size; i++) {
            double p1 = d1.getProbability(i);
            double p2 = d2.getProbability(i);
            if (p1 > p2) {
                sum += (p1 - p2);
            }
        }
        return sum;
    }

    public void mergePartition(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        this.newNumOfStates = partition.size();
        this.newLabelMap = new HashMap<>();
        this.newNumOfTrans = 0;
        this.newTransitions = new HashMap<>();
        for (int i = 0; i < this.newNumOfStates; i++) {
            List<Integer> currentSet = partition.get(i);
            int rdState = currentSet.get(0);
            int label = lMap.getOrDefault(rdState, -1);
            if(label != -1)  this.newLabelMap.put(i, label);
            for (int j = 0; j < this.newNumOfStates; j++) {
                List<Integer> l = partition.get(j);
                double sumTransition = 0;
                for(int k: currentSet) {
                    for (int next : l) {
                        if (trans.get(k).containsKey(next)) {
                            sumTransition += trans.get(k).get(next);
                        }
                    }
                }
                if (sumTransition > 0) {
                    this.newNumOfTrans++;
                    double avgTransition = sumTransition/currentSet.size();
                    this.newTransitions.computeIfAbsent(i, x -> new HashMap<>()).put(j, avgTransition);
                }
            }
        }
    }

    //classic partition refinement
    public void partitionRefine(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        createInitialPartition(trans, lMap);
        while (split(trans, 0)) {}
        mergePartition(trans, lMap);
    }

    // states merged if true
    public boolean approximatePartitionRefine(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        int prev = trans.size();
        createInitialPartition(trans, lMap);
        while (split(trans, epsilon2)) {}
        mergePartition(trans, lMap);
        if( this.newNumOfStates != prev) {
            iter++;
            return true;
        }
        return false;
    }

    public void smoothTransitions(Map<Integer, Map<Integer, Double>> trans) {
        for (int state : trans.keySet()) {
            Map<Integer, Double> map = trans.get(state);
            int len = map.size();
            double sum = 0;
            int i = 0;
            for (int next : map.keySet()) {
                if (i == len - 1) {
                    map.put(next, 1 - sum);
                    break;
                }
                sum += map.get(next);
                i++;
            }

        }
    }
    //return false if no merge
    public boolean mergeSinglePair(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        List<List<Integer>> partition = createInitialPartitionLocal(trans, lMap);
        StateTriplet stateTriplet = getMinLocalDistance(trans, lMap, partition);
        if (stateTriplet == null) {
            return false;
        }

        partition = createInitialPartitionLocal(trans, lMap);
        partition = splitWithTriplet(stateTriplet, partition);

        int size;
        do {
            size = partition.size();
            partition = split(stateTriplet.state1, trans, partition);
        } while (partition.size() != size);

        mergeByPartition(trans, lMap, partition, stateTriplet);
        iter++;
        return true;
    }

    public boolean mergeSinglePairOptimise(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        List<List<Integer>> partition = createInitialPartitionLocal(trans, lMap);
        StateTriplet stateTriplet = getMinOptimiseLocalDistance(trans, lMap, partition);
        if (stateTriplet == null) {
            return false;
        }

        partition = createInitialPartitionLocal(trans, lMap);
        partition = splitWithTriplet(stateTriplet, partition);

        int size;
        do {
            size = partition.size();
            partition = split(stateTriplet.state1, trans, partition);
        } while (partition.size() != size);

        mergeByPartition(trans, lMap, partition, stateTriplet);
        iter++;
        return true;
    }


    public  List<List<Integer>> splitWithTriplet(StateTriplet stateTriplet,  List<List<Integer>> partition) {
        int s1 = stateTriplet.state1; int s2 = stateTriplet.state2; int s3 = stateTriplet.state3;
        List<List<Integer>> newPartition = new ArrayList<>();
        for (int i = 0; i < partition.size(); i++) {
            List<Integer> list = partition.get(i);
            if (list.contains(s1)) {
                List<Integer> l1 = new ArrayList<>();
                l1.add(s1);
                l1.add(s2);
                l1.add(s3);
                List<Integer> l2 = new ArrayList<>();
                for (int j : list) {
                    if (j != s1 && j != s2 && j != s3) {
                        l2.add(j);
                    }
                }
                newPartition.add(l1);
                newPartition.add(l2);
            } else {
                newPartition.add(list);
            }
        }
        return newPartition;
    }

    public StateTriplet getMinOptimiseLocalDistance(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap, List<List<Integer>> partition) {
        List<StateTriplet> triplets = new ArrayList<>();

        for (List<Integer> list : partition) {
            if (list.size() <= 1) continue;
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < i; j++) {
                    int s1 = list.get(i);
                    int s2 = list.get(j);
                    triplets.add(new StateTriplet(s1,s2,s2));
                }
            }
        }

        return triplets.stream().parallel()
                .map(triplet -> getMinLocalDistanceHelper(trans, lMap, triplet))
                .filter(d -> d.distance < epsilon2)
                .min(Comparator.comparingDouble(d -> d.distance))
                .map(d -> d.stateTriplet)
                .orElse(null);
    }

    public StateTriplet getMinLocalDistance(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap,  List<List<Integer>> partition) {
        List<StateTriplet> triplets = new ArrayList<>();

        for (List<Integer> list : partition) {
            if (list.size() <= 1) continue;
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < i; j++) {
                    for(int k =0; k <= j; k++) {
                        int s1 = list.get(i);
                        int s2 = list.get(j);
                        int s3 = list.get(k);
                        triplets.add(new StateTriplet(s1,s2,s3));
                    }
                }
            }
        }

        return triplets.stream().parallel()
                .map(triplet -> getMinLocalDistanceHelper(trans, lMap, triplet))
                .filter(d -> d.distance < epsilon2)
                .min(Comparator.comparingDouble(d -> d.distance))
                .map(d -> d.stateTriplet)
                .orElse(null);
    }

    public TripletAndDistance getMinLocalDistanceHelper(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap, StateTriplet stateTriplet) {
        List<List<Integer>> localPartition = createInitialPartitionLocal(trans, lMap);
        localPartition = splitWithTriplet(stateTriplet, localPartition);
        int size;
        do {
            size = localPartition.size();
            localPartition = split(stateTriplet.state1, trans, localPartition);
        } while (localPartition.size() != size);

        Distribution d1 = getDistributionOnPartitions(trans, stateTriplet.state1, localPartition);
        Distribution d2 = getDistributionOnPartitions(trans, stateTriplet.state2, localPartition);

        if(stateTriplet.state2 == stateTriplet.state3){
            return new TripletAndDistance(stateTriplet, computeTVDistance(d1, d2));
        }else{
            Distribution d3 = getDistributionOnPartitions(trans, stateTriplet.state3, localPartition);
            double tmp = Math.max(2*computeTVDistance(d1, d3), 2*computeTVDistance(d2, d3));
            return new TripletAndDistance(stateTriplet, tmp);
        }
    }

    public Distribution getDistributionOnPartitions(Map<Integer, Map<Integer, Double>> trans, int state, List<List<Integer>> partition) {
        int size = partition.size();
        Distribution distr = new Distribution(size);
        for (int i = 0; i < size; i++) {
            List<Integer> list = partition.get(i);
            double sum = 0;
            for (int next : list) {
                if (trans.get(state).containsKey(next)) {
                    sum += trans.get(state).get(next);
                }
            }
            distr.updateEntry(i, sum);
        }
        return distr;
    }
    public List<List<Integer>> split(int s1, Map<Integer, Map<Integer, Double>> trans, List<List<Integer>> partition) {

        List<List<Integer>> newPartition = new ArrayList<>();
        List<List<Integer>> tmpPartition = new ArrayList<>(partition);
        int size = partition.size();
        for (List<Integer> list : partition) {
            if (s1 >= 0 && list.contains(s1)) {
                newPartition.add(list);
                continue;
            }
            List<List<Integer>> splitPartition = new ArrayList<>();

            //1st iteration: build a list of distributions
            Map<Integer, Distribution> distrMap = new HashMap<>();
            for (int state : list) {
                Distribution distr = new Distribution(size);
                for (int i = 0; i < size; i++) {
                    List<Integer> other = tmpPartition.get(i);
                    // compute probability to the other partition
                    double sum = 0;
                    for (int next : other) {
                        if (trans.get(state).containsKey(next)) {
                            sum += trans.get(state).get(next);
                        }
                    }
                    distr.updateEntry(i, sum);
                }
                distrMap.put(state, distr);
            }


            //split the states
            for (int state : list) {
                Distribution distr = distrMap.get(state);
                boolean isAdd = false;
                for (int i = 0; i < splitPartition.size(); i++) {
                    List<Integer> l = splitPartition.get(i);
                    int rdState = l.get(0);
                    if (distr.equals(distrMap.get(rdState))) {
                        l.add(state);
                        isAdd = true;
                        break;
                    }
                }
                if (!isAdd) {
                    ArrayList<Integer> l = new ArrayList<>();
                    l.add(state);
                    splitPartition.add(l);
                }
            }
            newPartition.addAll(splitPartition);
        }

        return newPartition;
    }
    public List<List<Integer>> createInitialPartitionLocal(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        List<List<Integer>> partition = new ArrayList<>();

        for (int state : trans.keySet()) {
            boolean isAdd = false;
            for (List<Integer> list : partition) {
                int label = lMap.getOrDefault(list.get(0), -1);
                if (lMap.getOrDefault(state, -1) == label) {
                    list.add(state);
                    isAdd = true;
                    break;
                }
            }
            //create the new list
            if (!isAdd) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add(state);
                partition.add(list);
            }
        }

        return partition;
    }

    public void mergeByPartition(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap, List<List<Integer>> partition, StateTriplet stateTriplet) {
        this.newNumOfStates = partition.size();
        this.newLabelMap = new HashMap<>();
        this.newNumOfTrans = 0;
        this.newTransitions = new HashMap<>();
        for (int i = 0; i < this.newNumOfStates; i++) {
            List<Integer> currentSet = partition.get(i);
            int rdState = currentSet.get(0);
            //set the label for the new state
            int label = lMap.getOrDefault(rdState, -1);
            if(label != -1)  this.newLabelMap.put(i, label);

            Distribution distr = getDistributionOnPartitions(trans, rdState, partition);

            //set the transitions
            if(stateTriplet != null && currentSet.contains(stateTriplet.state1) && (stateTriplet.state2 != stateTriplet.state3)){
                distr = getDistributionOnPartitions(trans, stateTriplet.state3, partition);
            }else if (stateTriplet != null && currentSet.contains(stateTriplet.state1) && (stateTriplet.state2 == stateTriplet.state3)){
                Distribution d1 = getDistributionOnPartitions(trans, stateTriplet.state1, partition);
                Distribution d2 = getDistributionOnPartitions(trans, stateTriplet.state2, partition);
                distr = getAverageDistribution(d1, d2);
            }

            for (int j = 0; j < this.newNumOfStates; j++) {
                if(distr.getProbability(j) > 0) {
                    this.newNumOfTrans++;
                    this.newTransitions.computeIfAbsent(i, x -> new HashMap<>()).put(j, distr.getProbability(j));
                }
            }
        }
    }

    public Distribution getAverageDistribution(Distribution d1, Distribution d2) {
        if (d1.size != d2.size) {
            System.err.println("tv distance size match");
            return null;
        }
        double [] a3 = new double[d1.size];

        for (int i = 0; i < d1.size; i++) {
            a3[i] = (d1.getProbability(i) + d2.getProbability(i))/2.0;
        }
        return new Distribution(a3);
    }

    public static void main(String[] args) {
        CombineMerge merge = new CombineMerge();
        merge.readFile(args);
        //System.out.println("************ input approx partition-refinement ************");
        //merge.printInputSimple();

        //compute probabilistic bisimulation
        merge.partitionRefine(merge.transitions, merge.labelMap);
        System.out.println("************ output approx partition-refinement quotient ************");
        merge.smoothTransitions(merge.transitions);
        merge.printOutputSimple();

        while (merge.approximatePartitionRefine(merge.newTransitions, merge.newLabelMap)) {
            merge.smoothTransitions(merge.newTransitions);
        }
        System.out.println("************ output approx partition-refinement merging " + merge.iter + " ************");
        merge.printOutputSimple();

        //merge states
        while( merge.mergeSinglePairOptimise(merge.newTransitions, merge.newLabelMap)){
            merge.partitionRefine(merge.newTransitions, merge.newLabelMap);
            merge.smoothTransitions(merge.newTransitions);
            System.out.println("************ output optimise local distance one pair " + merge.iter + " ************");
            merge.printOutputSimple();

            //merge states
            while (merge.approximatePartitionRefine(merge.newTransitions, merge.newLabelMap)) {
                merge.smoothTransitions(merge.newTransitions);
            }
            System.out.println("************ output approx partition-refinement merging " + merge.iter + " ************");
            merge.printOutputSimple();
        }

        while( merge.mergeSinglePair(merge.newTransitions, merge.newLabelMap)){
            merge.partitionRefine(merge.newTransitions, merge.newLabelMap);
            merge.smoothTransitions(merge.newTransitions);
            System.out.println("************ output local distance one pair " + merge.iter + " ************");
            merge.printOutputSimple();

            //merge states
            while (merge.approximatePartitionRefine(merge.newTransitions, merge.newLabelMap)) {
                merge.smoothTransitions(merge.newTransitions);
            }
            System.out.println("************ output approx partition-refinement merging " + merge.iter + " ************");
            merge.printOutputSimple();
        }

        merge.smoothTransitions(merge.newTransitions);

        merge.writeOutputToFile();
    }
}
