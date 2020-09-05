#!/bin/bash
# ---------------------------------------------------------------------------------
# MARC: Promoters - MARC-specific
# ---------------------------------------------------------------------------------

mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run1/Promoters/MARC-specific"

ts=$(date +%s%N)
python3 "/Users/tarlis/git/HIPERMovelets/programs/marc/multi_feature_classifier.py" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run1/specific_train.csv" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run1/specific_test.csv" "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run1/Promoters/MARC-specific/MARC-specific_results.csv" "MARC-specific" 100 concatenate lstm | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run1/Promoters/MARC-specific/MARC-specific.txt"
tt=$((($(date +%s%N) - $ts)/1000000))
echo "Processing time: $tt milliseconds\r\n" | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run1/Promoters/MARC-specific/MARC-specific.txt"
# ---------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------
# MARC: Promoters - MARC-specific
# ---------------------------------------------------------------------------------

mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run2/Promoters/MARC-specific"

ts=$(date +%s%N)
python3 "/Users/tarlis/git/HIPERMovelets/programs/marc/multi_feature_classifier.py" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run2/specific_train.csv" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run2/specific_test.csv" "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run2/Promoters/MARC-specific/MARC-specific_results.csv" "MARC-specific" 100 concatenate lstm | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run2/Promoters/MARC-specific/MARC-specific.txt"
tt=$((($(date +%s%N) - $ts)/1000000))
echo "Processing time: $tt milliseconds\r\n" | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run2/Promoters/MARC-specific/MARC-specific.txt"
# ---------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------
# MARC: Promoters - MARC-specific
# ---------------------------------------------------------------------------------

mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run3/Promoters/MARC-specific"

ts=$(date +%s%N)
python3 "/Users/tarlis/git/HIPERMovelets/programs/marc/multi_feature_classifier.py" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run3/specific_train.csv" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run3/specific_test.csv" "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run3/Promoters/MARC-specific/MARC-specific_results.csv" "MARC-specific" 100 concatenate lstm | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run3/Promoters/MARC-specific/MARC-specific.txt"
tt=$((($(date +%s%N) - $ts)/1000000))
echo "Processing time: $tt milliseconds\r\n" | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run3/Promoters/MARC-specific/MARC-specific.txt"
# ---------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------
# MARC: Promoters - MARC-specific
# ---------------------------------------------------------------------------------

mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run4/Promoters/MARC-specific"

ts=$(date +%s%N)
python3 "/Users/tarlis/git/HIPERMovelets/programs/marc/multi_feature_classifier.py" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run4/specific_train.csv" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run4/specific_test.csv" "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run4/Promoters/MARC-specific/MARC-specific_results.csv" "MARC-specific" 100 concatenate lstm | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run4/Promoters/MARC-specific/MARC-specific.txt"
tt=$((($(date +%s%N) - $ts)/1000000))
echo "Processing time: $tt milliseconds\r\n" | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run4/Promoters/MARC-specific/MARC-specific.txt"
# ---------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------
# MARC: Promoters - MARC-specific
# ---------------------------------------------------------------------------------

mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run5/Promoters/MARC-specific"

ts=$(date +%s%N)
python3 "/Users/tarlis/git/HIPERMovelets/programs/marc/multi_feature_classifier.py" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run5/specific_train.csv" "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run5/specific_test.csv" "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run5/Promoters/MARC-specific/MARC-specific_results.csv" "MARC-specific" 100 concatenate lstm | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run5/Promoters/MARC-specific/MARC-specific.txt"
tt=$((($(date +%s%N) - $ts)/1000000))
echo "Processing time: $tt milliseconds\r\n" | tee -a "/Users/tarlis/git/HIPERMovelets/results/MARC-5fold_4T_60G/run5/Promoters/MARC-specific/MARC-specific.txt"
# ---------------------------------------------------------------------------------
# END - By Tarlis Portela
