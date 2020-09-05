#!/bin/bash
# --------------------------------------------------------------------------------------
# Promoters - MML-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run1/Promoters/MML-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/MASTERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run1" -respath "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run1/Promoters/MML-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific.json" -nt 4 -ed true -samples 1 -sampleSize 0.5 -medium "none" -output "discrete" -lowm "false" -ms -1 -Ms -3 | tee -a "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run1/Promoters/MML-specific/MML-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run1/Promoters/MML-specific/MASTERMovelets_LOG"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run1/Promoters/MML-specific/MASTERMovelets_LOG)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run1/Promoters/MML-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run1/Promoters/MML-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - MML-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run2/Promoters/MML-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/MASTERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run2" -respath "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run2/Promoters/MML-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific.json" -nt 4 -ed true -samples 1 -sampleSize 0.5 -medium "none" -output "discrete" -lowm "false" -ms -1 -Ms -3 | tee -a "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run2/Promoters/MML-specific/MML-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run2/Promoters/MML-specific/MASTERMovelets_LOG"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run2/Promoters/MML-specific/MASTERMovelets_LOG)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run2/Promoters/MML-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run2/Promoters/MML-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - MML-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run3/Promoters/MML-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/MASTERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run3" -respath "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run3/Promoters/MML-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific.json" -nt 4 -ed true -samples 1 -sampleSize 0.5 -medium "none" -output "discrete" -lowm "false" -ms -1 -Ms -3 | tee -a "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run3/Promoters/MML-specific/MML-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run3/Promoters/MML-specific/MASTERMovelets_LOG"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run3/Promoters/MML-specific/MASTERMovelets_LOG)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run3/Promoters/MML-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run3/Promoters/MML-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - MML-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run4/Promoters/MML-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/MASTERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run4" -respath "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run4/Promoters/MML-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific.json" -nt 4 -ed true -samples 1 -sampleSize 0.5 -medium "none" -output "discrete" -lowm "false" -ms -1 -Ms -3 | tee -a "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run4/Promoters/MML-specific/MML-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run4/Promoters/MML-specific/MASTERMovelets_LOG"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run4/Promoters/MML-specific/MASTERMovelets_LOG)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run4/Promoters/MML-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run4/Promoters/MML-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - MML-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run5/Promoters/MML-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/MASTERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run5" -respath "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run5/Promoters/MML-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific.json" -nt 4 -ed true -samples 1 -sampleSize 0.5 -medium "none" -output "discrete" -lowm "false" -ms -1 -Ms -3 | tee -a "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run5/Promoters/MML-specific/MML-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run5/Promoters/MML-specific/MASTERMovelets_LOG"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run5/Promoters/MML-specific/MASTERMovelets_LOG)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run5/Promoters/MML-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/MML-5fold_4T_60G/run5/Promoters/MML-specific"

# --------------------------------------------------------------------------------------

# END - By Tarlis Portela
