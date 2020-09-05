import pandas as pd
import numpy as np
import os

## --------------------------------------------------------------------------------------------
## CLASSIFIER:
### Run Before: poifreq(sequences, dataset, features, folder, result_dir, method='npoi')
## --------------------------------------------------------------------------------------------
def model_poifreq(dir_path):
    from keras.models import Sequential
    from keras.layers import Dropout
    from keras.layers.core import Dense
    from keras.optimizers import Adam
    from keras.regularizers import l2
    from keras.callbacks import EarlyStopping
    from keras.callbacks import History
#     from metrics import compute_acc_acc5_f1_prec_rec
    import numpy as np


    keep_prob = 0.5

    HIDDEN_UNITS = 100
    LEARNING_RATE = 0.001
#     EARLY_STOPPING_PATIENCE = 30
    BASELINE_METRIC = 'acc'
    BASELINE_VALUE = 0.5
    BATCH_SIZE = 64
    EPOCHS = 250
    
    (num_features, num_classes, 
            x_train, y_train,
            x_test, y_test) = loadData(dir_path)

    print('[POI-FREQ:] Building Neural Network')

    print('keep_prob =', keep_prob)
    print('HIDDEN_UNITS =', HIDDEN_UNITS)
    print('LEARNING_RATE =', LEARNING_RATE)
#     print('EARLY_STOPPING_PATIENCE =', EARLY_STOPPING_PATIENCE)
    print('BASELINE_METRIC =', BASELINE_METRIC)
    print('BASELINE_VALUE =', BASELINE_VALUE)
    print('BATCH_SIZE =', BATCH_SIZE)
    print('EPOCHS =', EPOCHS, '\n')


#     class EpochLogger(EarlyStopping):

#         def __init__(self, metric='val_acc', baseline=0):
#             super(EpochLogger, self).__init__(monitor='val_acc',
#                                               mode='max',
#                                               patience=EARLY_STOPPING_PATIENCE)
#             self._metric = metric
#             self._baseline = baseline
#             self._baseline_met = False

#         def on_epoch_begin(self, epoch, logs={}):
#             print("===== Training Epoch %d =====" % (epoch + 1))

#             if self._baseline_met:
#                 super(EpochLogger, self).on_epoch_begin(epoch, logs)

#         def on_epoch_end(self, epoch, logs={}):
#             pred_y_train = np.array(self.model.predict(x_train))
#             (train_acc,
#              train_acc5,
#              train_f1_macro,
#              train_prec_macro,
#              train_rec_macro) = compute_acc_acc5_f1_prec_rec(y_train,
#                                                              pred_y_train,
#                                                              print_metrics=True,
#                                                              print_pfx='TRAIN')

#             pred_y_test = np.array(self.model.predict(x_test))
#             (test_acc,
#              test_acc5,
#              test_f1_macro,
#              test_prec_macro,
#              test_rec_macro) = compute_acc_acc5_f1_prec_rec(y_test, pred_y_test,
#                                                             print_metrics=True,
#                                                             print_pfx='TEST')
#             metrics.log(METHOD, int(epoch + 1), DATASET,
#                         logs['loss'], train_acc, train_acc5,
#                         train_f1_macro, train_prec_macro, train_rec_macro,
#                         logs['val_loss'], test_acc, test_acc5,
#                         test_f1_macro, test_prec_macro, test_rec_macro)
#             metrics.save(METRICS_FILE)

#             if self._baseline_met:
#                 super(EpochLogger, self).on_epoch_end(epoch, logs)

#             if not self._baseline_met \
#                and logs[self._metric] >= self._baseline:
#                 self._baseline_met = True

#         def on_train_begin(self, logs=None):
#             super(EpochLogger, self).on_train_begin(logs)

#         def on_train_end(self, logs=None):
#             if self._baseline_met:
#                 super(EpochLogger, self).on_train_end(logs)

    classifier = Sequential()
    hist = History()
    classifier.add(Dense(units=HIDDEN_UNITS,
                    input_dim=(num_features),
                    kernel_initializer='uniform',
                    kernel_regularizer=l2(0.02)))
    classifier.add(Dropout(keep_prob))
    classifier.add(Dense(units=num_classes,
                    kernel_initializer='uniform',
                    activation='softmax'))

    opt = Adam(lr=LEARNING_RATE)
    classifier.compile(optimizer=opt,
                  loss='categorical_crossentropy',
                  metrics=['acc'])

    classifier.fit(x=x_train,
              y=y_train,
              validation_data=(x_test, y_test),
              batch_size=BATCH_SIZE,
              shuffle=True,
              epochs=EPOCHS,
              verbose=0)#,
#               callbacks=[EpochLogger(metric=BASELINE_METRIC,
#                                      baseline=BASELINE_VALUE), hist])
#     print(METRICS_FILE)
#     df = pd.read_csv(METRICS_FILE)
#     f = open(RESULTS_FILE+'.txt', 'a+')
#     print('------------------------------------------------------------------------------------------------', file=f)
#     print(f"Method: {METHOD} | Dataset: {DATASET}", file=f)
#     print(f"Acc: {np.array(df['test_acc'])[-EARLY_STOPPING_PATIENCE]} \
#           | Acc_top_5: {np.array(df['test_acc_top5'])[-EARLY_STOPPING_PATIENCE]} \
#           | F1_Macro: {np.array(df['test_f1_macro'])[-EARLY_STOPPING_PATIENCE]}",
#           file=f)
#     print('------------------------------------------------------------------------------------------------', file=f)
#     f.close()
    print('[POI-FREQ:] OK')
    
    return classifier.predict(x_test)
# --------------------------------------------------------------------------------------------------------
# --------------------------------------------------------------------------------------------------------

def to_file(core_name, x_train, x_test, y_train, y_test):
    df_x_train = pd.DataFrame(x_train).to_csv(core_name+'-x_train.csv', index=False, header=None)
    df_x_test = pd.DataFrame(x_test).to_csv(core_name+'-x_test.csv', index=False, header=None)
    df_y_train = pd.DataFrame(y_train, columns=['label']).to_csv(core_name+'-y_train.csv', index=False)
    df_y_test = pd.DataFrame(y_test, columns=['label']).to_csv(core_name+'-y_test.csv', index=False)
    
## POI-F: POI Frequency
def poi(df_train, df_test, possible_sequences, seq2idx, sequence, dataset, feature, result_dir=None):
    
    print('Starting POI...')
    method = 'poi'
    
    # Train
    train_tids = df_train['tid'].unique()
    x_train = np.zeros((len(train_tids), len(possible_sequences)))
    y_train = df_train.drop_duplicates(subset=['tid', 'label'],
                                       inplace=False) \
                      .sort_values('tid', ascending=True,
                                   inplace=False)['label'].values

    for i, tid in enumerate(train_tids):
        traj_pois = df_train[df_train['tid'] == tid][feature].values
        for idx in range(0, (len(traj_pois)-(sequence - 1))):
            aux = []
            for b in range (0, sequence):
                aux.append(traj_pois[idx + b])
            aux = tuple(aux)
            x_train[i][seq2idx[aux]] += 1

    # Test
    test_tids = df_test['tid'].unique()
    test_unique_features = df_test[feature].unique().tolist()
    x_test = np.zeros((len(test_tids), len(possible_sequences)))
    y_test = df_test.drop_duplicates(subset=['tid', 'label'],
                                       inplace=False) \
                      .sort_values('tid', ascending=True,
                                   inplace=False)['label'].values

    for i, tid in enumerate(test_tids):
        traj_pois = df_test[df_test['tid'] == tid][feature].values
        for idx in range(0, (len(traj_pois)-(sequence - 1))):
            aux = []
            for b in range (0, sequence):
                aux.append(traj_pois[idx + b])
            aux = tuple(aux)
            if aux in possible_sequences:
                x_test[i][seq2idx[aux]] += 1
    
    if result_dir is not False:
        core_name = os.path.join(result_dir, method+'_'+feature+'_'+str(sequence)+'_'+dataset)
        to_file(core_name, x_train, x_test, y_train, y_test)
        
    return x_train, x_test, y_train, y_test
    
### NPOI-F: Normalized POI Frequency
def npoi(df_train, df_test, possible_sequences, seq2idx, sequence, dataset, feature, result_dir=None):
    
    print('Starting NPOI...')
    method = 'npoi'
    
    # Train
    train_tids = df_train['tid'].unique()
    x_train = np.zeros((len(train_tids), len(possible_sequences)))
    y_train = df_train.drop_duplicates(subset=['tid', 'label'],
                                       inplace=False) \
                      .sort_values('tid', ascending=True,
                                   inplace=False)['label'].values

    for i, tid in enumerate(train_tids):
        traj_pois = df_train[df_train['tid'] == tid][feature].values
        for idx in range(0, (len(traj_pois)-(sequence - 1))):
            aux = []
            for b in range (0, sequence):
                aux.append(traj_pois[idx + b])
            aux = tuple(aux)
            x_train[i][seq2idx[aux]] += 1
        x_train[i] = x_train[i]/len(traj_pois)

    # Test
    test_tids = df_test['tid'].unique()
    test_unique_features = df_test[feature].unique().tolist()
    x_test = np.zeros((len(test_tids), len(possible_sequences)))
    y_test = df_test.drop_duplicates(subset=['tid', 'label'],
                                       inplace=False) \
                      .sort_values('tid', ascending=True,
                                   inplace=False)['label'].values

    for i, tid in enumerate(test_tids):
        traj_pois = df_test[df_test['tid'] == tid][feature].values
        for idx in range(0, (len(traj_pois)-(sequence - 1))):
            aux = []
            for b in range (0, sequence):
                aux.append(traj_pois[idx + b])
            aux = tuple(aux)
            if aux in possible_sequences:
                x_test[i][seq2idx[aux]] += 1
        x_test[i] = x_test[i]/len(traj_pois)
        
    if result_dir is not False:
        core_name = os.path.join(result_dir, method+'_'+feature+'_'+str(sequence)+'_'+dataset)
        to_file(core_name, x_train, x_test, y_train, y_test)
        
    return x_train, x_test, y_train, y_test
    
### WNPOI-F: Weighted Normalized POI Frequency.
def wnpoi(df_train, df_test, possible_sequences, seq2idx, sequence, dataset, feature, result_dir=None):
    
    print('Starting WNPOI...')    
    method = 'wnpoi'
    
    train_labels = df_train['label'].unique()
    weights = np.zeros(len(possible_sequences))
    for label in train_labels:
        aux_w = np.zeros(len(possible_sequences))
        class_pois = df_train[df_train['label'] == label][feature].values
        for idx in range(0, (len(class_pois)-(sequence - 1))):
            aux = []
            for b in range (0, sequence):
                aux.append(class_pois[idx + b])
            aux = tuple(aux)
            seqidx = seq2idx[aux]
            if aux_w[seqidx] == 0:
                weights[seqidx] += 1
                aux_w[seqidx] = 1
    weights = np.log2(len(train_labels)/weights)
    # Train
    train_tids = df_train['tid'].unique()
    x_train = np.zeros((len(train_tids), len(possible_sequences)))
    y_train = df_train.drop_duplicates(subset=['tid', 'label'],
                                       inplace=False) \
                      .sort_values('tid', ascending=True,
                                   inplace=False)['label'].values

    for i, tid in enumerate(train_tids):
        traj_pois = df_train[df_train['tid'] == tid][feature].values
        for idx in range(0, (len(traj_pois)-(sequence - 1))):
            aux = []
            for b in range (0, sequence):
                aux.append(traj_pois[idx + b])
            aux = tuple(aux)
            x_train[i][seq2idx[aux]] += 1
        x_train[i] = x_train[i]/len(traj_pois)
        for w in range(0, len(possible_sequences)):
            x_train[i][w] *= weights[w]

    # Test
    test_tids = df_test['tid'].unique()
    test_unique_features = df_test[feature].unique().tolist()
    x_test = np.zeros((len(test_tids), len(possible_sequences)))
    y_test = df_test.drop_duplicates(subset=['tid', 'label'],
                                       inplace=False) \
                      .sort_values('tid', ascending=True,
                                   inplace=False)['label'].values

    for i, tid in enumerate(test_tids):
        traj_pois = df_test[df_test['tid'] == tid][feature].values
        for idx in range(0, (len(traj_pois)-(sequence - 1))):
            aux = []
            for b in range (0, sequence):
                aux.append(traj_pois[idx + b])
            aux = tuple(aux)
            if aux in possible_sequences:
                x_test[i][seq2idx[aux]] += 1
        x_test[i] = x_test[i]/len(traj_pois)
        for w in range(0, len(possible_sequences)):
            x_test[i][w] *= weights[w]
            
    if result_dir is not False:
        core_name = os.path.join(result_dir, method+'_'+feature+'_'+str(sequence)+'_'+dataset)
        to_file(core_name, x_train, x_test, y_train, y_test)
        
    return x_train, x_test, y_train, y_test
    
## --------------------------------------------------------------------------------------------
def poifreq_all(sequence, dataset, feature, folder, result_dir):
    print('Dataset: {}, Feature: {}, Sequence: {}'.format(dataset, feature, sequence))
    df_train = pd.read_csv(folder+dataset+'_train.csv')
    df_test = pd.read_csv(folder+dataset+'_test.csv')
    unique_features = df_train[feature].unique().tolist()
    
    points = df_train[feature].values
    possible_sequences = []
    for idx in range(0, (len(points)-(sequence - 1))):
        aux = []
        for i in range (0, sequence):
            aux.append(points[idx + i])
        aux = tuple(aux)
        if aux not in possible_sequences:
            possible_sequences.append(aux)

    seq2idx = dict(zip(possible_sequences, np.r_[0:len(possible_sequences)]))
    
    if not os.path.exists(result_dir):
        os.makedirs(result_dir)
        
    pd.DataFrame(possible_sequences).to_csv(os.path.join(result_dir, feature+'_'+str(sequence)+'_'+dataset+'-sequences.csv'), index=False, header=None)
    
    poi(df_train, df_test, possible_sequences, seq2idx, sequence, dataset, feature, result_dir)
    npoi(df_train, df_test, possible_sequences, seq2idx, sequence, dataset, feature, result_dir)
    wnpoi(df_train, df_test, possible_sequences, seq2idx, sequence, dataset, feature, result_dir)
    
## By Tarlis: Run this first...
## --------------------------------------------------------------------------------------------
def poifreq(sequences, dataset, features, folder, result_dir, method='npoi', save_all=False):
#     print('Dataset: {}, Feature: {}, Sequence: {}'.format(dataset, feature, sequence))
    if dataset is '':
        df_train = pd.read_csv(os.path.join(folder, 'train.csv'))
        df_test = pd.read_csv(os.path.join(folder, 'test.csv'))
    else:
        df_train = pd.read_csv(os.path.join(folder, dataset+'_train.csv'))
        df_test = pd.read_csv(os.path.join(folder, dataset+'_test.csv'))
    
    if save_all:
        save_all = result_dir
        
    agg_x_train = None
    agg_x_test  = None
    
    for sequence in sequences:
        for feature in features:
            print('Dataset: {}, Feature: {}, Sequence: {}'.format(dataset, feature, sequence))
            unique_features = df_train[feature].unique().tolist()

            points = df_train[feature].values
            possible_sequences = []
            for idx in range(0, (len(points)-(sequence - 1))):
                aux = []
                for i in range (0, sequence):
                    aux.append(points[idx + i])
                aux = tuple(aux)
                if aux not in possible_sequences:
                    possible_sequences.append(aux)

            seq2idx = dict(zip(possible_sequences, np.r_[0:len(possible_sequences)]))

            if not os.path.exists(result_dir):
                os.makedirs(result_dir)

            pd.DataFrame(possible_sequences).to_csv(os.path.join(result_dir, \
               feature+'_'+str(sequence)+'_'+dataset+'-sequences.csv'), index=False, header=None)

            if method is 'poi':
                x_train, x_test, y_train, y_test = poi(df_train, df_test, possible_sequences, \
                                                       seq2idx, sequence, dataset, feature, result_dir=save_all)
            elif method is 'npoi':
                x_train, x_test, y_train, y_test = npoi(df_train, df_test, possible_sequences, \
                                                       seq2idx, sequence, dataset, feature, result_dir=save_all)
            else:
                x_train, x_test, y_train, y_test = wnpoi(df_train, df_test, possible_sequences, \
                                                       seq2idx, sequence, dataset, feature, result_dir=save_all)

            # Concat columns:
            if agg_x_train is None:
                agg_x_train = pd.DataFrame(x_train)
            else:
                agg_x_train = pd.concat([agg_x_train, pd.DataFrame(x_train)], axis=1)   

            if agg_x_test is None:
                agg_x_test = pd.DataFrame(x_test)
            else:
                agg_x_test = pd.concat([agg_x_test, pd.DataFrame(x_test)], axis=1)    
            
            
    core_name = os.path.join(result_dir, method+'_'+('_'.join(features))+'_'+('_'.join([str(n) for n in sequences]))+'_'+dataset)
    to_file(core_name, agg_x_train, agg_x_test, y_train, y_test)
    
    return agg_x_train, agg_x_test, y_train, y_test, core_name
   
    
## --------------------------------------------------------------------------------------------
def loadData(dir_path):
    from sklearn.preprocessing import OneHotEncoder
    from sklearn import preprocessing

    x_train = pd.read_csv(dir_path+'-x_train.csv', header=None)
    # x_train = x_train[x_train.columns[:-1]]
    y_train = pd.read_csv(dir_path+'-y_train.csv')

    x_test = pd.read_csv(dir_path+'-x_test.csv', header=None)
    # x_test = x_test[x_test.columns[:-1]]
    y_test = pd.read_csv(dir_path+'-y_test.csv')

    num_features = len(list(x_train))
    num_classes = len(y_train['label'].unique())


    one_hot_y = OneHotEncoder()
    one_hot_y.fit(y_train.loc[:, ['label']])

    y_train = one_hot_y.transform(y_train.loc[:, ['label']]).toarray()
    y_test = one_hot_y.transform(y_test.loc[:, ['label']]).toarray()

    x_train = preprocessing.scale(x_train)
    x_test = preprocessing.scale(x_test)
    
    return (num_features, num_classes, 
            x_train, y_train,
            x_test, y_test)