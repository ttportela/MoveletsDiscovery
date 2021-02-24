import sys, os 
import pandas as pd
sys.path.insert(0, os.path.abspath(os.path.join('.')))

if len(sys.argv) < 2:
    print('Please run as:')
    print('\tResultsTo.py', 'PATH TO RESULTS', 'DESTINATION')
    print('Example:')
    print('\tResultsTo.py', '"./results"', '"./simple_results"')
    exit()

results_path = sys.argv[1]
to_path    = sys.argv[2]

path = os.path.join(results_path, '**', '*.txt' )

filelist = []
filesList = []

# 1: Build up list of files:
import glob2 as glob
print("Looking for result files in " + path)
for files in glob.glob(path):
    fileName, fileExtension = os.path.splitext(files)
    filelist.append(fileName) #filename without extension
    filesList.append(files) #filename with extension


path = os.path.join(results_path, '**', 'model', '**')
print("Looking for result files in " + path)
for files in glob.glob(path):
    fileName, fileExtension = os.path.splitext(files)
    filelist.append(fileName) #filename without extension
    filesList.append(files) #filename with extension


results_path = os.path.abspath(results_path)

if not os.path.exists(to_path):
    print('Creating: ', to_path)
    os.makedirs(to_path)
to_path      = os.path.abspath(to_path)


import shutil
for original in filesList:
    target = original.replace(results_path, '')
    target = to_path + target #os.path.join(to_path, target)
    if not os.path.exists(os.path.dirname(target)):
        os.makedirs(os.path.dirname(target))
    print('CP:', original, '=>', target)
    shutil.copyfile(original, target)

print("Done.")
