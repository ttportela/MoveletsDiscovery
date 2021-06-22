'''
Created on Feb, 2021

@author: Tarlis Portela
'''
# --------------------------------------------------------------------------------
import os
# import sys
# import numpy as np
import pandas as pd
import glob2 as glob
from datetime import datetime
# --------------------------------------------------------------------------------
def results2df(res_path, prefix, modelfolder='model', isformat=True):
    filelist = []
    filesList = []

    # 1: Build up list of files:
    print("Looking for result files in " + os.path.join(res_path, prefix, '*', '*.txt' ))
    for files in glob.glob(os.path.join(res_path, prefix, '*', '*.txt' )):
        fileName, fileExtension = os.path.splitext(files)
        filelist.append(fileName) #filename without extension
        filesList.append(files) #filename with extension
    
    # 2: Create and concatenate in a DF:
#     ct = 1
    df = pd.DataFrame()

    cols = []

    df[' '] = ['Candidates', 'Movelets', 'ACC (MLP)', 'ACC (RF)', 'ACC (SVM)' , 'Time (Movelets)', 'Time (MLP)', 'Time (RF)', 'Time (SVM)', 'Trajs. Compared', 'Trajs. Pruned', 'Date']
    df['Dataset'] = ""
    df['Dataset'][0] = prefix
#     df = df[['Dataset',' ']]
    for ijk in filesList:
        method = os.path.basename(ijk)[:-4]
        cols.append(method)
        
        path = os.path.dirname(ijk)
        df[method] = addResults(df, ijk, path, method, modelfolder, isformat)
      
    print("Done.")
    cols.sort()
    cols = ['Dataset',' '] + cols
    return df[cols]

def kFoldResults(res_path, prefix, method, modelfolder='model', isformat=True):
    filelist = []
    filesList = []

    # 1: Build up list of files:
    print("Looking for result files in " + os.path.join(res_path, prefix, 'run*', method, method+'.txt' ))
    for files in glob.glob(os.path.join(res_path, prefix, 'run*', method, method+'.txt' )):
        fileName, fileExtension = os.path.splitext(files)
        filelist.append(fileName) #filename without extension
        filesList.append(files) #filename with extension
    
    # 2: Create and concatenate in a DF:
#     ct = 1
    df = pd.DataFrame()

    cols = []

    df[' '] = ['Candidates', 'Movelets', 'ACC (MLP)', 'ACC (RF)', 'ACC (SVM)' , 'Time (Movelets)', 'Time (MLP)', 'Time (RF)', 'Time (SVM)', 'Trajs. Compared', 'Trajs. Pruned', 'Date']
    df['Dataset'] = ""
    df['Dataset'][0] = prefix

    for ijk in filesList:
        path = os.path.dirname(ijk)
        run = os.path.basename(os.path.abspath(os.path.join(path, '..')))
        
        cols.append(run)
        df[run] = addResults(df, ijk, path, method, modelfolder, False)
        
    
    df[method] = df[cols][:-1].mean(axis=1)
    df[method][11] = '-'
    
    if isformat:
        for column in cols:
            df[column] = format_col(df, column)

        df[method] = format_col(df, method)
        
    cols = ['Dataset',' '] + cols + [method]
    return df[cols]

def addResults(df, resfile, path, method, modelfolder='model', isformat=True):
    print("Loading " + method + " results from: " + path)
    data = read_csv(resfile)
    
    try:
        mk = '%a %b %d %H:%M:%S CET %Y'
        dt = datetime.strptime(data.iloc[-1]['content'], mk)
        dt = dt.strftime("%d/%m/%y-%H:%M")
    except ValueError:
        dt = '-'
    
    total_can = get_sum_of_file_by_dataframe("Number of Candidates: ", data)
    total_mov = get_sum_of_file_by_dataframe("Total of Movelets: ", data)
    trajs_looked = get_sum_of_file_by_dataframe("Trajs. Looked: ", data)
    trajs_ignored = get_sum_of_file_by_dataframe("Trajs. Ignored: ", data)
    
    if 'POIFREQ' in path:
        time = get_total_number_of_ms("Processing time: ", read_csv(os.path.join(path, 'npoi_results.txt')))
    else:
        time = get_total_number_of_ms("Processing time: ", data)

    mlp_acc = getACC_MLP(path, method, modelfolder) * 100
    rf_acc  = getACC_RF(path, modelfolder) * 100
    svm_acc = getACC_SVM(path, modelfolder) * 100

    mlp_t = getACC_time(path, 'MLP', modelfolder)
    rf_t  = getACC_time(path, 'RF', modelfolder)
    svm_t = getACC_time(path, 'SVM', modelfolder)

    if isformat:
        total_can = '{:,}'.format(total_can) if total_can > 0 else "-"
        total_mov = '{:,}'.format(total_mov) if total_mov > 0 else "-"
        
        mlp_acc = "{:.3f}".format(mlp_acc) if mlp_acc > 0 else "-"
        rf_acc  = "{:.3f}".format(rf_acc)  if rf_acc  > 0 else "-"
        svm_acc = "{:.3f}".format(svm_acc) if svm_acc > 0 else "-"
        
#         time  = '%dh%dm%ds' % printHour(time)  if time  > 0 else "-"
#         mlp_t = '%dh%dm%ds' % printHour(mlp_t) if mlp_t > 0 else "-"
#         rf_t  = '%dh%dm%ds' % printHour(rf_t)  if rf_t  > 0 else "-"
#         svm_t = '%dh%dm%ds' % printHour(svm_t) if svm_t > 0 else "-"
        
        time   = format_hour(time)
        mlp_t  = format_hour(mlp_t)
        rf_t   = format_hour(rf_t)
        svm_t  = format_hour(svm_t)

        trajs_looked  = '{:,}'.format(trajs_looked)  if trajs_looked  > 0 else "-"
        trajs_ignored = '{:,}'.format(trajs_ignored) if trajs_ignored > 0 else "-"
        
    return (total_can, total_mov, mlp_acc, rf_acc, svm_acc, 
                  time, mlp_t, rf_t, svm_t, 
                  trajs_looked, trajs_ignored, dt)

def format_col(df, method):
    return (format_cel(df, method, 0, '{val:,}'),
            format_cel(df, method, 1, '{val:,}'), 
            format_celf(df, method, 2, '{val:.3f}'),
            format_celf(df, method, 3, '{val:.3f}'),
            format_celf(df, method, 4, '{val:.3f}'),
            format_celh(df, method, 5, '%dh%02dm%02ds'),
            format_celh(df, method, 6, '%dh%02dm%02ds'),
            format_celh(df, method, 7, '%dh%02dm%02ds'),
            format_celh(df, method, 8, '%dh%02dm%02ds'),
            format_cel(df, method, 9, '{val:,}'),
            format_cel(df, method, 10, '{val:,}'),
            df.at[11,method])
    
def format_cel(df, method, row, pattern):
    if df.at[row,method] > 0:
        value = int(df.at[row,method])
        value = pattern.format(val=value) #pattern.format(df.at[row,method]) 
        return value
    else: 
        return "-"
    
def format_celf(df, method, row, pattern):
    if df.at[row,method] > 0:
        value = float(df.at[row,method])
        value = pattern.format(val=value) #pattern.format(df.at[row,method]) 
        return value
    else: 
        return "-"
    
def format_celh(df, method, row, pattern):
    return format_hour(df.at[row,method])

def format_hour(millis):
    if millis > 0:
        hours, minutes, seconds = printHour(millis) 
        value = ''
        if hours > 0:
            value = value + ('%dh' % hours)
        if minutes > 0:
            value = value + (('%02dm' % minutes) if value is not '' else ('%dm' % minutes))
        if seconds > 0:
            value = value + (('%02ds' % seconds) if value is not '' else ('%ds' % seconds))
        return value
    else: 
        return "-"

# ----------------------------------------------------------------------------------
def getACC_time(path, label, modelfolder='model'):
    acc = 0.0
    if "POIFREQ" in path and label == 'MLP':
        res_file = os.path.join(path, 'npoi_results.txt')
        if os.path.isfile(res_file):
            data = read_csv(res_file)
            acc = get_total_number_of_ms("Classification time: ", data)
    else:
        data = getACC_data(path, 'classification_times.csv', modelfolder)
        if data is not None:
            acc = data[label][0]
    return acc

def getACC_RF(path, modelfolder='model'):
    acc = 0
    data = getACC_data(path, 'model_approachRF300_history.csv', modelfolder)
    if data is not None:
        acc = data['1'].iloc[-1]
    return acc

def getACC_SVM(path, modelfolder='model'):
    acc = 0
    data = getACC_data(path, 'model_approachSVC_history.csv', modelfolder)
    if data is not None:
        acc = data.loc[0].iloc[-1]
    return acc

def getACC_MLP(path, method, modelfolder='model'):
    acc = 0
    
    if "MARC" in method:
        res_file = os.path.join(path, method + '_results.csv')
        if os.path.isfile(res_file):
            data = pd.read_csv(res_file)
            acc = data['test_acc'].iloc[-1]
    elif "POIFREQ" in method:
        res_file = os.path.join(path, 'npoi_results.txt')
        print(res_file)
        if os.path.isfile(res_file):
            data = read_csv(res_file)
            acc = get_first_number("Acc: ", data) #data['test_acc'].iloc[-1]
    else:
        data = getACC_data(path, 'model_approach2_history_Step5.csv', modelfolder)
        if data is not None:
            acc = data['val_accuracy'].iloc[-1]
    return acc

def getACC_data(path, approach_file, modelfolder='model'):
    res_file = os.path.join(path, modelfolder, approach_file)
    if os.path.isfile(res_file):
        data = pd.read_csv(res_file)
        return data
    else:
        return None

def printHour(millis):
    millis = int(millis)
    seconds=(millis/1000)%60
    seconds = int(seconds)
    minutes=(millis/(1000*60))%60
    minutes = int(minutes)
    hours=(millis//(1000*60*60))

#     print ("%dh%dm%ds" % (hours, minutes, seconds))
    return (hours, minutes, seconds)
# ----------------------------------------------------------------------------------
# --------------------------------------------------------------------------------->   
def read_csv(file_name):
#     file_name = DIR_V1 + "results/"+file_name + '.txt'
    data = pd.read_csv(file_name, header = None, error_bad_lines=False, warn_bad_lines=False, delimiter='-=-', engine='python')
    data.columns = ['content']
    return data

def get_lines_with_separator(data, str_splitter):
    lines_with_separation = []
    for index,row in data.iterrows():#
        if str_splitter in row['content']:
            print(row)
            lines_with_separation.insert(len(lines_with_separation), index)
    return lines_with_separation

def get_titles(data):
    titles = []
    for index,row in data.iterrows():#
        if "Loading train and test data from" in row['content']:
            titles.insert(len(titles), row['content'])
    return titles

def split_df_to_dict(data, lines_with_separation):
    df_dict = {}
    lines_with_separation.pop(0)
    previous_line = 0
    for line in lines_with_separation:#
        print(data.iloc[previous_line:line,:])
        df_dict[previous_line] = data.iloc[previous_line:line,:]
        previous_line=line
    df_dict['last'] = data.iloc[previous_line:,:]
    return df_dict

def get_total_number_of_candidates_file(str_target, df_dict):
    total_per_file = []
    for key in df_dict:
        total = 0
        for index,row in df_dict[key].iterrows():
            if str_target in row['content']:
                number = row['content'].split(str_target)[1]
                total = total + int(number)
        total_per_file.insert(len(total_per_file), total)
    return total_per_file

def get_total_number_of_candidates_file_by_dataframe(str_target, df):
    total = 0
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = number.split(".")[0]
            total = total + int(number)
    return total

def get_sum_of_file_by_dataframe(str_target, df):
    total = 0
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = number.split(".")[0]
            total = total + int(number)
    return total

def get_max_number_of_file_by_dataframe(str_target, df):
    total = 0
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = int(number.split(".")[0])
            total = max(total, number)
    return total

def get_min_number_of_file_by_dataframe(str_target, df):
    total = 99999
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = int(number.split(".")[0])
            total = min(total, number)
    return total

def get_total_number_of_ms(str_target, df):
    total = 0
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = number.split(" milliseconds")[0]
            total = total + int(number)
    return total

def get_first_number(str_target, df):
    total = 0
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = number.split(" ")[0]
            return float(number)
    return total

def split_string(string, delimiter):
    return str(string.split(delimiter)[1])

# --------------------------------------------------------------------------------->  
def printLatex(df, ajust=12):
    n_cols = (len(df.columns)-2)
    n_rows = len(df)
    
    print('\\begin{table*}[!ht]')
    print('\\centering')
    print('\\begin{tabular}{|c|r||'+('r|'*n_cols)+'}')
    print('\\hline')
    print('\\hline')
    print((' & '.join(df.columns)) + ' \\\\')
    
    for k in range(0, int(n_rows), 12):
        print('\n\\hline')
        print('\\multirow{'+str(11)+'}{2cm}{'+df.at[k,'Dataset']+'}')
        print(printLatex_line(df, k+0, ajust))
        print(printLatex_line(df, k+1, ajust))
        print('\\cline{2-'+str(n_cols+2)+'}')
        print(printLatex_line(df, k+2, ajust))
        print(printLatex_line(df, k+3, ajust))
        print(printLatex_line(df, k+4, ajust))
        print('\\cline{2-'+str(n_cols+2)+'}')
        print(printLatex_line(df, k+5, ajust))
        print(printLatex_line(df, k+6, ajust))
        print(printLatex_line(df, k+7, ajust))
        print(printLatex_line(df, k+8, ajust))
        print('\\cline{2-'+str(n_cols+2)+'}')
        print(printLatex_line(df, k+9, ajust))
        print(printLatex_line(df, k+10,ajust))
    
    print('\\hline')
    print('\\hline')
    print('\\end{tabular}')
    print('\\caption{Results for '+df['Dataset'][0]+' dataset.}')
    print('\\label{tab:results_'+df['Dataset'][0]+'}')
    print('\\end{table*}')
    
def printLatex_line(df, l, ajust=12):
    line = '&'+ str(df.at[l,df.columns[1]]).rjust(15, ' ') + ' '
    for i in range(2, len(df.columns)):
        line = line + '& '+ str(df.at[l,df.columns[i]]).rjust(ajust, ' ') + ' '
    line = line + '\\\\'
    return line

# ----------------------------------------------------------------------------------
# def printProcess(prefix, dir_path):
#     file = os.path.join(prefix, dir_path)
#     res_file = os.path.join(RES_PATH, file + '.txt')
    
#     data = read_csv(res_file)
#     total_can = get_sum_of_file_by_dataframe("Number of Candidates: ", data)
#     total_mov = get_sum_of_file_by_dataframe("Total of Movelets: ", data)
#     trajs_looked = get_sum_of_file_by_dataframe("Trajs. Looked: ", data)
#     trajs_ignored = get_sum_of_file_by_dataframe("Trajs. Ignored: ", data)
#     time = get_total_number_of_ms("Processing time: ", data)
    
#     print('# <=====================================================>')
#     print('# '+file)
#     print("# Number of Candidates: " + str(total_can))
#     print("# Total of Movelets:    " + str(total_mov))
#     print("# Processing time:      " + str(time) + ' ms -- %d:%d:%d' % printHour(time))
#     print('# --')
    
#     acc  = getACC_SVM(prefix, method) * 100
#     if acc is not 0:
#         print("# SVM ACC:    " + acc)
    
#     acc  = getACC_RF(prefix, method) * 100
#     if acc is not 0:
#         print("# Random Forest ACC:    " + acc)
        
#     acc  = getACC_MLP(prefix, method) * 100
#     if acc is not 0:
#         print("# Neural Network ACC:   " + acc)
        
    
#     print('# --')
#     print("# Total of Trajs. Looked: " + str(trajs_looked))
#     print("# Total of Trajs. Ignored:   " + str(trajs_ignored))
#     print("# Total of Trajs.:    " + str(trajs_looked+trajs_ignored))
# --------------------------------------------------------------------------------->