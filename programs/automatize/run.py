'''
Created on Jun, 2020

@author: Tarlis Portela
'''
# --------------------------------------------------------------------------------
# RUNNER
import os
import pandas as pd
import glob2 as glob
from datetime import datetime
from IPython.utils import io
# --------------------------------------------------------------------------------

def k_Movelets(k, data_folder, res_path, prefix, folder, descriptor, version = 'hiper', ms = False, Ms = False, extra=False, 
        java_opts='', jar_name='HIPERMovelets', n_threads=1, prg_path='./', print_only=False, keep_folder=2):
    
    if isinstance(k, int):
        k = range(1, k+1)
    
    for x in k:
        subpath_data = os.path.join(data_folder, 'run'+str(x))
        subpath_rslt = os.path.join(res_path,    prefix, 'run'+str(x))
        Movelets(subpath_data, subpath_rslt, None, folder, descriptor, version, ms, Ms, extra, 
        java_opts, jar_name, n_threads, prg_path, print_only, keep_folder)

# --------------------------------------------------------------------------------
def Movelets(data_folder, res_path, prefix, folder, descriptor, version = 'hiper', ms = False, Ms = False, extra=False, 
        java_opts='', jar_name='HIPERMovelets', n_threads=1, prg_path='./', print_only=False, keep_folder=2):
    print('# --------------------------------------------------------------------------------------')
    print('# ' + res_path + ' - ' +folder)
    print('# --------------------------------------------------------------------------------------')
#     print('echo RUN - ' + res_path + ' - ' +folder)
    print()
    
    if prefix != None:
        res_folder = os.path.join(res_path, prefix, folder)
    else:
        res_folder = os.path.join(res_path, folder)
#     res_folder = os.path.join(res_path, prefix, folder)
    mkdir(res_folder, print_only)
    
    program = os.path.join(prg_path, jar_name+'.jar')
    outfile = os.path.join(res_folder, folder+'.txt')
    
    CMD = '-nt %s' % str(n_threads)
    
    if os.path.sep not in descriptor:
        descriptor = os.path.join(data_folder, descriptor)
        
    if jar_name != 'HIPERMovelets':
        CMD = CMD + ' -ed true -samples 1 -sampleSize 0.5 -medium "none" -output "discrete" -lowm "false"'
    else:
        CMD = CMD + ' -version ' + version

    CMD = 'java '+java_opts+' -jar "'+program+'" -curpath "'+data_folder+'" -respath "'+res_folder+'" -descfile "'+ descriptor + '.json" ' + CMD
    
    if ms != False:
        CMD = CMD + ' -ms '+str(ms)
    else:
        CMD = CMD + ' -ms -1'
        
    if Ms != False:
        CMD = CMD + ' -Ms '+str(Ms)
        
    if extra != False:
        CMD = CMD + ' ' + extra
        
#     if PVT:
#         CMD = CMD + ' -pvt true -lp false -pp 10 -op false'
        
    if os.name == 'nt':
        CMD = CMD +  ' >> "'+outfile +  '"'
    else:
        CMD = CMD +  ' 2>&1 | tee -a "'+outfile+'" '
        
    execute(CMD, print_only)
    print('# --------------------------------------------------------------------------------------')
    
    dir_path = "MASTERMovelets"
    
#     if jar_name in ['Hiper-MASTERMovelets', 'Hiper2-MASTERMovelets']:
#         dir_path = dir_path + "GAS"
        
    if jar_name in ['Super-MASTERMovelets', 'SUPERMovelets']:
        dir_path = dir_path + "Supervised"
        
    if jar_name == 'MASTERMovelets' and Ms == -3:
        dir_path = dir_path + "_LOG"
        
#     if jar_name == 'MASTERMovelets' and PVT:
#         dir_path = dir_path + "Pivots"
        
    if keep_folder >= 1: # keep_folder = 1 or 2
        mergeAndMove(res_folder, dir_path, prg_path, print_only)
    
    if keep_folder <= 1: # keep_folder = 0 or 1, 1 for both
        execute('rm -R "'+os.path.join(res_folder, dir_path)+'"', print_only)
        
    print('# --------------------------------------------------------------------------------------')
    print()
    
# --------------------------------------------------------------------------------------

# def mergeData(dir_path, prefix, dir_to):
# #     dir_from = dir_path + '/' + getResultPath(dir_path)
#     dir_from = getResultPath(dir_path)
#     dir_analisys = os.path.join(AN_PATH, prefix, dir_to)
#     if not os.path.exists(dir_analisys):
#         os.makedirs(dir_analisys)
# #     dir_from = RES_PATH + dir_from
#     print("Moving FROM: " + str(dir_from) + " (" + dir_path + "?)")
#     print("Moving TO  : " + str(dir_analisys))
    
#     ! Rscript MergeDatasets.R "$dir_from"

#     csvfile = os.path.join(dir_from, "train.csv")
#     ! mv "$csvfile" "$dir_analisys"
#     csvfile = os.path.join(dir_from, "test.csv")
#     ! mv "$csvfile" "$dir_analisys"
    
#     out_file = os.path.join(RES_PATH, dir_to+'.txt')
#     out_to   = os.path.join(RES_PATH, prefix)
#     dir_path = os.path.join(RES_PATH, dir_path)
#     dir_to   = os.path.join(RES_PATH, prefix, dir_to)
#     print("Moving TO  : " + str(dir_to))
#     ! mv "$out_file" "$out_to"
#     ! mv "$dir_path" "$dir_to"
    

def execute(cmd, print_only=False):
#     import subprocess
#     p = subprocess.Popen(cmd.split(),
#                          stdout=subprocess.PIPE,
#                          stderr=subprocess.STDOUT)
#     print( list(iter(p.stdout.readline, 'b') ))

#     !command $cmd
    if print_only:
        print(cmd)
        print()
    else:
        print(os.popen(cmd).read())
#         os.system(cmd)
    
def mkdir(folder, print_only=False):
    cmd = 'md' if os.name == 'nt' else 'mkdir -p'
    if not os.path.exists(folder):
        if print_only:
            execute(cmd+' "' + folder + '"', print_only)
        else:
            os.makedirs(folder)

def move(ffrom, fto, print_only=False):
    execute('mv "'+ffrom+'" "'+fto+'"', print_only)
    
def getResultPath(mydir):
#     if print_only:
#         return "$pattern"os.path.join(mydir,)
    
    for dirpath, dirnames, filenames in os.walk(mydir):
        if not dirnames:
            dirpath = os.path.abspath(os.path.join(dirpath,".."))
            return dirpath
    
def moveResults(dir_from, dir_to, print_only=False):
    csvfile = os.path.join(dir_from, "train.csv")
    move(csvfile, dir_to, print_only)
    csvfile = os.path.join(dir_from, "test.csv")
    move(csvfile, dir_to, print_only)

# def moveFolder(res_folder, prefix, dir_to):
#     out_file = os.path.join(RES_PATH, dir_to+'.txt')
#     out_to   = os.path.join(RES_PATH, prefix)
#     dir_path = os.path.join(RES_PATH, res_folder)
#     dir_to   = os.path.join(RES_PATH, prefix, dir_to)
#     print("Moving TO:  " + str(dir_to))
#     ! mv "$out_file" "$out_to"
#     ! mv "$dir_path" "$dir_to"
    
def mergeClasses(res_folder, prg_path='./', print_only=False):
    dir_from = getResultPath(res_folder)
    
    if print_only:
#         dir_from = '$pattern'
        dir_from = res_folder
    
#     if dir_from is None:
#         return False
    
# #     print("# Merging here: " + str(dir_from) + " (" + res_folder + ")")
#     prg = os.path.join(prg_path, 'automatize', 'MergeDatasets.R')
#     execute('Rscript "'+prg+'" "'+dir_from+'"', print_only)
# #     ! Rscript MergeDatasets.R "$dir_from"

    prg = os.path.join(prg_path, 'automatize', 'MergeDatasets.py')
    execute('python3 "'+prg+'" "'+res_folder+'"', print_only)
#     execute('python3 "'+prg+'" "'+res_folder+'" "test.csv"', print_only)

    return dir_from
    
def mergeAndMove(res_folder, folder, prg_path='./', print_only=False):
#     dir_analisys = os.path.join(AN_PATH, prefix, dir_to)

#     if print_only:
#         print('pattern="'+os.path.join(res_folder, folder)+'"')
#         print('rdir=$(ls "${pattern}" | head -1)')
#         print('pattern=$(realpath "${pattern}")/"$rdir"')

    dir_from = mergeClasses(res_folder, prg_path, print_only)
    #mergeClasses(os.path.join(res_folder, folder), prg_path, print_only)
    
    if not print_only and not dir_from:
        print("Nothing to Merge. Abort.")
        return
        
#     print("Analysis   : " + str(dir_analisys))
#     moveResults(dir_from, res_folder, print_only)

# --------------------------------------------------------------------------------------
def mergeDatasets(dir_path, file='train.csv'):
    files = [i for i in glob.glob(os.path.join(dir_path, '*', '**', file))]

    print("Loading files - " + file)
    # combine all files in the list
    combined_csv = pd.concat([pd.read_csv(f).drop('class', axis=1) for f in files[:len(files)-1]], axis=1)
    combined_csv = pd.concat([combined_csv, pd.read_csv(files[len(files)-1])], axis=1)
    #export to csv
    print("Writing "+file+" file")
    combined_csv.to_csv(os.path.join(dir_path, file), index=False)
    
    print("Done.")

# --------------------------------------------------------------------------------------
def countMovelets(dir_path):
    ncol = 0
    print(os.path.join(dir_path, "**", "train.csv"))
    for filenames in glob.glob(os.path.join(dir_path, "**", "train.csv"), recursive = True):
#         print(filenames)
        with open(filenames, 'r') as csv:
            first_line = csv.readline()

        ncol += first_line.count(',')# + 1 
    return ncol

# --------------------------------------------------------------------------------------
# --------------------------------------------------------------------------------------
def k_MARC(k, data_folder, res_path, prefix, folder, train="train.csv", test="test.csv",
            EMBEDDING_SIZE=100, MERGE_TYPE="concatenate", RNN_CELL="lstm",
            prg_path='./', print_only=False):
    
    if isinstance(k, int):
        k = range(1, k+1)
    
    for x in k:
        subpath_data = os.path.join(data_folder, 'run'+str(x))
        subpath_rslt = os.path.join(res_path,    prefix, 'run'+str(x))
        MARC(subpath_data, subpath_rslt, None, folder, train, test,
            EMBEDDING_SIZE, MERGE_TYPE, RNN_CELL,
            prg_path, print_only)
    
def MARC(data_folder, res_path, prefix, folder, train="train.csv", test="test.csv",
            EMBEDDING_SIZE=100, MERGE_TYPE="concatenate", RNN_CELL="lstm",
            prg_path='./', print_only=False):
    
    print("# ---------------------------------------------------------------------------------")
    print("# MARC: " + res_path + ' - ' +folder)
    print("# ---------------------------------------------------------------------------------")
#     print('echo MARC - ' + res_path + ' - ' +folder)
    print()
    
    if prefix != None:
        res_folder = os.path.join(res_path, prefix, folder)
    else:
        res_folder = os.path.join(res_path, folder)
#     res_folder = os.path.join(res_path, prefix, folder)
    mkdir(res_folder, print_only)
    
    TRAIN_FILE   = os.path.join(data_folder, train)
    TEST_FILE    = os.path.join(data_folder, test)
    DATASET_NAME = folder
    RESULTS_FILE = os.path.join(res_folder, folder + "_results.csv")
    OUTPUT_FILE  = '"' + os.path.join(res_folder, folder+'.txt') + '"'
    
#     mkdir(os.path.join(res_path, prefix), print_only)
        
    PROGRAM = os.path.join(prg_path, 'multi_feature_classifier.py')
    CMD = 'python3 "'+PROGRAM+'" "' + TRAIN_FILE + '" "' + TEST_FILE + '" "' + RESULTS_FILE + '" "' + DATASET_NAME + '" ' + str(EMBEDDING_SIZE) + ' ' + MERGE_TYPE + ' ' + RNN_CELL
    
    if os.name == 'nt':
        tee = ' >> '+OUTPUT_FILE 
    else:
        tee = ' 2>&1 | tee -a '+OUTPUT_FILE
        
    CMD = CMD + tee    
        
    if print_only:
        print('ts=$(date +%s%N)')
        print(CMD)
        print('tt=$((($(date +%s%N) - $ts)/1000000))')
        print('echo "Processing time: $tt milliseconds\\r\\n"' + tee)
    else:
        print(CMD)
        time = datetime.now()
        out = os.popen(CMD).read()
        time = (datetime.now()-time).total_seconds() * 1000

        f=open(OUTPUT_FILE, "a+")
        f.write(out)
        f.write("Processing time: %d milliseconds\r\n" % (time))
        f.close()

        print(captured.stdout)
        print("Done. " + str(time) + " milliseconds")
    print("# ---------------------------------------------------------------------------------")
    
def k_POIFREQ(k, data_folder, res_path, prefix, dataset, sequences, features, py_name='python3', \
              print_only=False, doclass=True):
    
    if isinstance(k, int):
        k = range(1, k+1)
    
    for x in k:
        subpath_data = os.path.join(data_folder, 'run'+str(x))
        subpath_rslt = os.path.join(res_path,    prefix, 'run'+str(x))
#         print(subpath_data, subpath_rslt, None, dataset, sequences, features, py_name, print_only, doclass)
        POIFREQ(subpath_data, subpath_rslt, None, dataset, sequences, features, py_name, print_only, doclass)
        
def POIFREQ(data_folder, res_path, prefix, dataset, sequences, features, py_name='python3', print_only=False, doclass=True):
    
    folder = 'POIFREQ-' + '_'.join(features)
    
    print("# ---------------------------------------------------------------------------------")
    print("# POIFREQ: " + res_path + ' - ' +folder)
    print("# ---------------------------------------------------------------------------------")
    print()
    
    if prefix != None:
        res_folder = os.path.join(res_path, prefix, folder)
    else:
        res_folder = os.path.join(res_path, folder)
        
    mkdir(res_folder, print_only)
    print()
    
    if print_only:
#         print('echo POIFREQ - ' + res_path + ' - ' +folder)
        print()
        outfile = os.path.join(res_folder, folder+'.txt')
    
        CMD = py_name + " automatize/poifreq/poifreq.py "
        CMD = CMD + "\"npoi\" "
        CMD = CMD + "\""+(','.join([str(n) for n in sequences]))+"\" "
        CMD = CMD + "\""+('_'.join(features))+"\" "
        CMD = CMD + "\""+dataset+"\" "
        CMD = CMD + "\""+data_folder+"\" "
        CMD = CMD + "\""+res_folder+"\""
        
        if os.name == 'nt':
            CMD = CMD +  ' >> "'+outfile+'"'
        else:
            CMD = CMD +  ' 2>&1 | tee -a "'+outfile+'"'
        
        execute(CMD, print_only)
        
        return os.path.join(res_folder, 'npoi_'+('_'.join(features))+'_'+('_'.join([str(n) for n in sequences]))+'_'+dataset)
    else:
        from automatize.ensemble_models.poifreq import poifreq
        return poifreq(sequences, dataset, features, data_folder, res_folder, doclass=doclass)
