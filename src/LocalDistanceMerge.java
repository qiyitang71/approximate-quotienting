import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;


class StateTriplet {
    int state1;
    int state2;
    int state3;

    public StateTriplet(int s1, int s2, int s3) {
        this.state1 = s1;
        this.state2 = s2;
        this.state3 = s3;
    }
}
public class LocalDistanceMerge {
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
                    "Use java Merging 0: <label> 1: <transition> 2: <outputLabelFile> 3: <outputTransition> 4: <epsilon2>");
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
        System.out.println("**** INPUT ****");

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
    }

    //if s1 < 0 then it is normal partition
    // return true if split; return false if fixpoint reached
    public boolean split(int s1, Map<Integer, Map<Integer, Double>> trans) {

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

        if (partition.size() == newPartition.size()) {
            return false;
        }

        partition = newPartition;
        return true;
    }

    public void partitionRefine(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        createInitialPartition(trans, lMap);
        while (split(-1, trans)) {
        }
        mergePartition(trans, lMap);
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

//    public void updateSystem() {
//        this.states = this.newStates;
//        this.labelMap = this.newLabelMap;
//        this.numOfTrans = this.newNumOfTrans;
//        this.transitions = this.newTransitions;
//    }

    public Distribution getDistributionOnPatitions(Map<Integer, Map<Integer, Double>> trans, int state) {
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

    public void splitWithTriplet(StateTriplet stateTriplet) {
        int s1 = stateTriplet.state1; int s2 = stateTriplet.state2; int s3 = stateTriplet.state3;
        List<List<Integer>> newPartition = new ArrayList<>();
        for (int i = 0; i < this.partition.size(); i++) {
            List<Integer> list = this.partition.get(i);
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
        this.partition = newPartition;
    }

    public StateTriplet getMinLocalDistance(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        double min = 2;
        StateTriplet st = null;
        for (List<Integer> list : partition) {
            if (list.size() <= 1) continue;
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < i; j++) {
                    for(int k =0; k <= j; k++) {
                        createInitialPartition(trans, lMap);
                        int s1 = list.get(i);
                        int s2 = list.get(j);
                        int s3 = list.get(k);
                        StateTriplet stateTriplet = new StateTriplet(s1,s2,s3);
                        splitWithTriplet(stateTriplet);
                        while (split(s1, trans)) {
                        }
                        Distribution d1 = getDistributionOnPatitions(trans, s1);
                        Distribution d2 = getDistributionOnPatitions(trans, s2);
                        double localDistance = 2 * computeTVDistance(d1, d2);//l1 distance
                        if (localDistance < min && localDistance < epsilon2) {
                            st = stateTriplet;
                            min = localDistance;
                        }
                    }
                }
            }
        }
        return st;
    }


    //return false if no merge
    public boolean mergeSinglePair(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        createInitialPartition(trans, lMap);
        StateTriplet stateTriplet = getMinLocalDistance(trans, lMap);
        Map<Integer, Map<Integer, Double>> tmpTransitions = new HashMap<>(trans);
        if (stateTriplet == null) {
            return false;
        }

        createInitialPartition(trans, lMap);
        splitWithTriplet(stateTriplet);
        while (split(stateTriplet.state1, trans)) {}
        mergePartition(trans, lMap);
        iter++;
        return true;
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
        LocalDistanceMerge merge = new LocalDistanceMerge();
        merge.readFile(args);

        //System.out.println("**** input local distance ****");
        //merge.printInputSimple();

        //compute probabilistic bisimulation
        merge.partitionRefine(merge.transitions, merge.labelMap);
        System.out.println("************ output local distance quotient ************");
        merge.printOutputSimple();

        //merge states
        System.out.println("************ output local distance merging ************");
        while (merge.mergeSinglePair(merge.newTransitions, merge.newLabelMap)) {
            merge.partitionRefine(merge.newTransitions, merge.newLabelMap);
        }
        merge.partitionRefine(merge.newTransitions, merge.newLabelMap);

        merge.printOutputSimple();

        merge.smoothTransitions(merge.newTransitions);

        merge.writeOutputToFile();
    }

}
