import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class ApproximatePartitionRefinement {
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

    public static void main(String[] args) {
        ApproximatePartitionRefinement merge = new ApproximatePartitionRefinement();
        merge.readFile(args);
        //System.out.println("************ input approx partition-refinement ************");
        //merge.printInputSimple();

        //compute probabilistic bisimulation
        merge.partitionRefine(merge.transitions, merge.labelMap);
        //System.out.println("************ output approx partition-refinement quotient ************");
        //merge.printOutputSimple();

        //merge states
        while (merge.approximatePartitionRefine(merge.newTransitions, merge.newLabelMap)) {
        }
        System.out.println("************ output approx partition-refinement merging ************");
        merge.printOutputSimple();

        merge.smoothTransitions(merge.newTransitions);

        merge.writeOutputToFile();
    }
}
