#!/bin/bash
# --------------------------------------------------------------------------------------
# Promoters - HpL-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run1/Promoters/HpL-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/HIPERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run1" -respath "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run1/Promoters/HpL-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific_hp.json" -nt 4 -version hiper -ms -1 -Ms -3 -version hiper-pvt -T 0.9 -BU 0.1 | tee -a "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run1/Promoters/HpL-specific/HpL-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run1/Promoters/HpL-specific/MASTERMovelets"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run1/Promoters/HpL-specific/MASTERMovelets)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run1/Promoters/HpL-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run1/Promoters/HpL-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - HpL-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run2/Promoters/HpL-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/HIPERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run2" -respath "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run2/Promoters/HpL-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific_hp.json" -nt 4 -version hiper -ms -1 -Ms -3 -version hiper-pvt -T 0.9 -BU 0.1 | tee -a "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run2/Promoters/HpL-specific/HpL-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run2/Promoters/HpL-specific/MASTERMovelets"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run2/Promoters/HpL-specific/MASTERMovelets)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run2/Promoters/HpL-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run2/Promoters/HpL-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - HpL-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run3/Promoters/HpL-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/HIPERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run3" -respath "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run3/Promoters/HpL-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific_hp.json" -nt 4 -version hiper -ms -1 -Ms -3 -version hiper-pvt -T 0.9 -BU 0.1 | tee -a "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run3/Promoters/HpL-specific/HpL-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run3/Promoters/HpL-specific/MASTERMovelets"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run3/Promoters/HpL-specific/MASTERMovelets)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run3/Promoters/HpL-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run3/Promoters/HpL-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - HpL-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run4/Promoters/HpL-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/HIPERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run4" -respath "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run4/Promoters/HpL-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific_hp.json" -nt 4 -version hiper -ms -1 -Ms -3 -version hiper-pvt -T 0.9 -BU 0.1 | tee -a "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run4/Promoters/HpL-specific/HpL-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run4/Promoters/HpL-specific/MASTERMovelets"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run4/Promoters/HpL-specific/MASTERMovelets)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run4/Promoters/HpL-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run4/Promoters/HpL-specific"

# --------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------
# Promoters - HpL-specific
# --------------------------------------------------------------------------------------
mkdir -p "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run5/Promoters/HpL-specific"

java -Xmx60G -jar "/Users/tarlis/git/HIPERMovelets/programs/HIPERMovelets.jar" -curpath "/Users/tarlis/git/HIPERMovelets/data/5fold/promoters/run5" -respath "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run5/Promoters/HpL-specific" -descfile "/Users/tarlis/git/HIPERMovelets/data/5fold/descriptors/GeneDS_specific_hp.json" -nt 4 -version hiper -ms -1 -Ms -3 -version hiper-pvt -T 0.9 -BU 0.1 | tee -a "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run5/Promoters/HpL-specific/HpL-specific.txt"

# --------------------------------------------------------------------------------------
pattern="/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run5/Promoters/HpL-specific/MASTERMovelets"
rdir=$(ls "${pattern}" | head -1)
pattern=$(realpath "${pattern}")/"$rdir"
# Merging here: $pattern (/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run5/Promoters/HpL-specific/MASTERMovelets)
Rscript "/Users/tarlis/git/HIPERMovelets/programs/automatize/MergeDatasets.R" "$pattern"

mv "$pattern/train.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run5/Promoters/HpL-specific"

mv "$pattern/test.csv" "/Users/tarlis/git/HIPERMovelets/results/HpL-5fold_4T_60G/run5/Promoters/HpL-specific"

# --------------------------------------------------------------------------------------

# END - By Tarlis Portela
