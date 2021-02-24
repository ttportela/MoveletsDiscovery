'''
Created on Aug, 2020

@author: Tarlis Portela
'''
#TODO: RF não pode fazer aquele reshape?
#TODO: MARC Model

# --------------------------------------------------------------------------------
# ANALYSIS By Ensemble Learning Models

def Ensemble(data_path, results_path, prefix, methods=['movelets','poifreq'], \
             modelfolder='model', save_results=True, print_only=False, py_name='python3', \
             descriptor='', sequences=[1,2,3], features=['poi'], dataset='specific'):
    import os
    
    ensembles = dict()
    for method in methods:
        if method == 'movelets':
            from automatize.run import Movelets
            Movelets(data_path, results_path, prefix, 'HpL', descriptor, Ms=-3, extra='-version hiper-pvt -T 0.9 -BU 0.1', \
                print_only=print_only)
            ensembles['movelets'] = os.path.join(results_path, prefix, 'HpL')
            
        elif method == 'poifreq':
            from automatize.run import POIFREQ
#             sequences = [2, 3]
#             features  = ['sequence']
#             results_npoi = os.path.join(results_path, prefix, 'npoi')
            core_name = POIFREQ(data_path, results_path, prefix, dataset, sequences, features, \
                                print_only=print_only, doclass=False)
            ensembles['poifreq'] = core_name
            
        elif method == 'marc':
            ensembles['marc'] = data_path
            
        elif method == 'rf':
            ensembles['rf'] = data_path
                     
    if print_only:
        CMD = py_name + " automatize/ensemble-cls.py "
        CMD = CMD + "\""+data_path+"\" "
        CMD = CMD + "\""+results_path+"\" "
        CMD = CMD + "\""+str(ensembles)+"\" "
        CMD = CMD + "\""+dataset+"\" "
        print(CMD)
        print('')
    else:
        return ClassifierEnsemble(data_path, results_path, ensembles, dataset, save_results, modelfolder)

            
def ClassifierEnsemble(data_path, results_path, ensembles, dataset='specific', save_results=True, modelfolder='model_ensemble'):
    
    import os
    import pandas as pd
    import numpy as np
    from scipy import stats
    from datetime import datetime
    import tensorflow
    tensorflow.keras.backend.clear_session()
    
    if dataset == '':
        TRAIN_FILE = os.path.join(data_path, 'train.csv')
        TEST_FILE  = os.path.join(data_path, 'test.csv')
    else:
        TRAIN_FILE = os.path.join(data_path, dataset+'_train.csv')
        TEST_FILE  = os.path.join(data_path, dataset+'_test.csv')

#     X_train, y_train, X_test, y_test = loadData(list(ensembles.values())[0]) # temp
#     print(y_train)
        
    from automatize.ensemble_models.marc2 import loadTrajectories    
    (keys, vocab_size,
     labels,
     num_classes,
     max_length,
     x_train, x_test,
     y_train, y_test) = loadTrajectories(train_file=TRAIN_FILE,
                                         test_file=TEST_FILE,
                                         tid_col='tid',
                                         label_col='label')

    keys = list(pd.unique(labels))
    y_labels = [keys.index(x) for x in labels]
#     labels   = list(set(y_train))
#     y_train  = [labels.index(x) for x in y_train]
#     y_test   = [labels.index(x) for x in y_test]
    
#     return X_train, y_train, X_test, y_test
    
    time = datetime.now()
    # create the sub models
    estimators = []
    print('[Ensemble]: '+', '.join(ensembles.keys()))
    for method, folder in ensembles.items():
#         print(method+': ', folder)
        y_pred = []
        model = []
        if method == 'movelets':
#             from automatize.analysis import loadData
            from automatize.ensemble_models.movelets import model_movelets
#             x_train_m, y_train_m, x_test_m, y_test_m = loadData(folder) # temp
#             labels   = list(set(y_train))
#             y_train_m  = [labels.index(x) for x in y_train_m]
#             y_test_m   = [labels.index(x) for x in y_test_m]
            model, x_test = model_movelets(folder)
            model = model.predict(x_test)
#             estimators.append((method, model))
#             estimators.append(model)
#             print(method+': ', get_line(y_test, model))

        if method is 'marc':
            from automatize.ensemble_models.marc2 import model_marc
            model, x_test = model_marc(folder, results_path, dataset)
            model = model.predict(x_test)
#             estimators.append((method, model))
#             estimators.append(model)
#             print(method+': ', get_line(y_test, model))
            
        if method is 'poifreq':
            from automatize.ensemble_models.poifreq import model_poifreq
            model, x_test = model_poifreq(folder)
            model = model.predict(x_test)
#             estimators.append((method, model))
#             estimators.append(model)
#             print(method+': ', get_line(y_test, model))
            
        if method is 'rf':
            from automatize.ensemble_models.randomforrest import model_rf
            model, x_test = model_rf(folder, dataset)
            model = model.predict_proba(x_test)
#             estimators.append((method, model))
#             estimators.append(model)
#             print(method+': ', get_line(y_test, model))
        
#         print(method, model)
        y_pred = [np.argmax(f) for f in model]
        estimators.append(y_pred) 
#         print('idx_pred', y_pred)
#         print(y_labels, y_pred)
        ensembles[method] = get_line(y_labels, y_pred)
        print(method+': ', ensembles[method])
        print("---------------------------------------------------------------------------------")
        
    # create the ensemble model
#     from sklearn import model_selection
#     from sklearn.ensemble import VotingClassifier
#     ensemble = VotingClassifier(estimators)
#     results = model_selection.cross_val_score(ensemble, X, Y, cv=kfold)
#     results = ensemble.predict(X_test)
    
#     print(estimators)
    final_pred = stats.mode(estimators).mode[0]
#     print(final_pred)
    y_pred = [np.argmax(f) for f in final_pred]
#     for i in range(0,len(X_test)):
#         final_pred = np.append(final_pred, mode([pred1[i], pred2[i], pred3[i]]))
    
    print('[Ensemble] Final results:')
    print(ensembles)
    line=get_line(y_labels, y_pred)
    print('[Ensemble]:', line)
    
    # ---------------------------------------------------------------------------------
    if (save_results) :
        if not os.path.exists(os.path.join(results_path, modelfolder)):
            os.makedirs(os.path.join(results_path, modelfolder))
        from sklearn.metrics import classification_report
        from automatize.Methods import classification_report_csv
        report = classification_report(y_labels, y_pred) #classifier.predict(X_test) )
        classification_report_csv(report, os.path.join(results_path, modelfolder, "model_approachEnsemble_report.csv"),"Ensemble") 
        pd.DataFrame(line).to_csv(os.path.join(results_path, modelfolder, "model_approachEnsemble_history.csv")) 

    # ----------------------------------------------------------------------------------
    time = (datetime.now()-time).total_seconds() * 1000
    # ---------------------------------------------------------------------------------
    print("Done. " + str(time) + " milliseconds")
    print("---------------------------------------------------------------------------------")
    return time

def ApproachEnsemble(dir_path, dir_path2, save_results=True, method2='poifreq', modelfolder='model'):
        
    import os
    import pandas as pd
    import numpy as np
    from datetime import datetime
    import tensorflow
    from sklearn.metrics import classification_report
    from automatize.Methods import classification_report_csv
    from automatize.analysis import loadData
    from automatize.ensemble_models.movelets import model_movelets
        
    X_train, y_train, X_test, y_test = loadData(dir_path)
        
    labels   = list(set(y_train))
    y_train  = [labels.index(x) for x in y_train]
    y_test   = [labels.index(x) for x in y_test]
#     y_train2 = [labels.index(x) for x in y_train2]
#     y_test2  = [labels.index(x) for x in y_test2]
    
    print("Building Ensemble models")
    time = datetime.now()
    
    tensorflow.keras.backend.clear_session()
    pred1 = model_movelets(X_train, y_train, X_test, y_test).predict(X_test)

    tensorflow.keras.backend.clear_session()
    if method2 is 'marc':
        from automatize.ensemble_models.marc import model_marc
        pred2 = model_marc(dir_path2).predict(X_test)
    if method2 is 'poifreq':
        from automatize.ensemble_models.poifreq import model_poifreq
        pred2 = model_poifreq(dir_path2).predict(X_test)
    if method2 is 'rf':
        from automatize.ensemble_models.randomforrest import model_rf
        pred2 = model_rf(dir_path2).predict_proba(X_test)

    y_pred1 = [np.argmax(f) for f in pred1]
    y_pred2 = [np.argmax(f) for f in pred2]

    final_pred = (pred1*0.5+pred2*0.5)
    y_pred = [np.argmax(f) for f in final_pred]
    
    print('Models results:')
    print(get_line(y_test, y_pred1))
    print(get_line(y_test, y_pred2))
    
    print('Ensembled results:')
    line=get_line(y_test, y_pred)
    print(line)
    
    # ---------------------------------------------------------------------------------
    if (save_results) :
        if not os.path.exists(os.path.join(dir_path, modelfolder)):
            os.makedirs(os.path.join(dir_path, modelfolder))
        report = classification_report(y_test, y_pred) #classifier.predict(X_test) )
        classification_report_csv(report, os.path.join(dir_path, modelfolder, "model_approachEnsemble_report.csv"),"Ensemble") 
        pd.DataFrame(line).to_csv(os.path.join(dir_path, modelfolder, "model_approachEnsemble_history.csv")) 
    
    # ----------------------------------------------------------------------------------
    time = (datetime.now()-time).total_seconds() * 1000
    # ---------------------------------------------------------------------------------
    print("Done. " + str(time) + " milliseconds")
    print("---------------------------------------------------------------------------------")
    return time
# --------------------------------------------------------------------------------

# Statistics:
def get_line(y_true, y_pred):
    acc = accuracy(y_true, y_pred)
    f1  = f1_macro(y_true, y_pred)
    prec= precision_macro(y_true, y_pred)
    rec = recall_macro(y_true, y_pred)
    accTop5 = 0 #calculateAccTop5(classifier, X_test, y_test, 5)
    line=[acc, f1, prec, rec, accTop5]
    return line

def precision_macro(y_true, y_pred):
    from sklearn.metrics import precision_score
    return precision_score(y_true, y_pred, average='macro')
def recall_macro(y_true, y_pred):
    from sklearn.metrics import recall_score
    return recall_score(y_true, y_pred, average='macro')
def f1_macro(y_true, y_pred):
    from sklearn.metrics import f1_score
    return f1_score(y_true, y_pred, average='macro')
def accuracy(y_true, y_pred):
    from sklearn.metrics import accuracy_score
    return accuracy_score(y_true, y_pred, normalize=True)
# --------------------------------------------------------------------------------