# Approximate Minimisation Algorithms 

## Models
We evaluate the performance of the approximate minimisation algorithms on several LMCs. These LMCs model randomised algorithms and probabilistic protocols that are part of the probabilistic model checker [PRISM](https://www.prismmodelchecker.org/). Model stats (parameters, number of states, number of transitions) are summarised into this [table](models.csv).

We call models with less than 300 states as small models, and otherwise large models.

## Approximate Minimisation Algorithms
We implemented two approximate minimisation algorithms. The input for both algorithms are an approximation LMC M<sub>ε</sub> and an error parameter ε<sub>2</sub>. 

1. **Approximate minimisation using local bisimilarity distances**

The algorithm proceeds in iterations. In each iteration, we compute the local bisimilarity distances for all pairs of states and select the pair of which the local bisimilarity distance is the smallest and is at most ε<sub>2</sub>. It then merge this pair of states. The algorithm terminates when all pairs of states have local bisimilarity distances greater than ε<sub>2</sub>.

2. **Approximate minimisation by approximate partition refinement**

The algorithm proceeds in iterations. In each iteration, we execute approximate partition refinement with error parameter ε<sub>2</sub> and obtain a partition of states. We then lump states that belong to the same set of the partition. The algorithm terminates when no states can be lumped.

## Experiments

For small LMCs, we sample the successor distribution for each state and obtain an approximation of it with error parameter ε and error bound δ. For large LMCs, sampling is not practical so we perturb the successor distribution by adding small noise to the successor transition probabilities so that for each state with at least probability 1-δ the L<sub>1</sub>-distance of the successor distributions of it and the one in the approximation model is at most ε and otherwise the L<sub>1</sub>-distance is 2ε. We vary error parameter ε ∈ {0.00001, 0.0001, 0.001, 0.01} and fix error bound δ = 0.01. For each original LMC and a pair of ε and δ, we generate 5 approximation LMCs. 

We apply both minimisation algorithms with ε<sub>2</sub> ∈ {0.00001, 0.0001, 0.001, 0.01, 0.1} to the small LMCs (both the original models and the approximation models), while we only apply the approximate minimisation algorithm using approximate partition refinement with ε<sub>2</sub> ∈ {0.00001, 0.0001, 0.001, 0.01, 0.1} to the large LMCs (both the original models and the approximation models), since in practice the other algorithm takes too long on the large LMCs.

To run the experiments, firstly choose a model. If the model is small (with less than 300 states), go to `runSmall.sh` and replace the model name on line 8 by one of the options on line 7. Simply run the bash script:
>bash runSmall.sh

If the model is large (with at least 300 states), go to `runLarge.sh` and replace the model name on line 8 by one of the options on line 7. Simply run the bash script:
>bash runLarge.sh


## Results
The full experimental results can be found in this [Google Sheet](https://docs.google.com/spreadsheets/d/1JJnBYsOTfbC3mXHEWO4AEJG492YoD0yXaAs8DsrwGOA/edit?usp=sharing).
