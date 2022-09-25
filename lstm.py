import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from keras.models import Sequential
from keras.layers import LSTM, Dropout, Dense, Activation
from keras.callbacks import TensorBoard, ModelCheckpoint, ReduceLROnPlateau
import datetime
import sys
import pymysql



em = sys.argv[1] # 안드로이드에서 가져온 이메일

con = pymysql.connect(host='localhost', user='connectuser', password='gpals780@',
                      db='watercollect', charset='utf8', # 한글처리 (charset = 'utf8')
                      autocommit=True, # 결과 DB 반영 (Insert or update)
                      cursorclass=pymysql.cursors.DictCursor # DB조회시 컬럼명을 동시에 보여줌
                     )
cur = con.cursor()

sql = "select 24 - DATE_FORMAT( NOW( ) , '%k' ) as num"
cur.execute(sql)
num = cur.fetchall()
num = pd.DataFrame(num)
num = num['num'][0].astype(int)

sql = "\
SELECT T.dd, IFNULL( intakeT.intake, 0 ) AS intake \
FROM ( \
SELECT DISTINCT CONCAT( DATE_FORMAT( intake_date, '%Y%m%d' ) , n ) AS dd \
FROM water, hour_n \
) AS T \
LEFT JOIN ( \
SELECT DATE_FORMAT( intake_date, '%Y%m%d%H' ) AS dd, SUM( intake ) AS intake \
FROM water \
WHERE email = '"+em+"' \
GROUP BY dd \
) AS intakeT \
ON T.dd = intakeT.dd \
ORDER BY T.dd DESC \
LIMIT "+str(num)+", 18446744073709551615\
" # water 테이블 불러옴

cur.execute(sql)
rows = cur.fetchall()

data = pd.DataFrame(rows)

r_idx = [i for i in range(data.shape[0]-1, -1, -1)]
data = pd.DataFrame(data, index=r_idx)
data.reset_index(drop=True, inplace=True)



# 최근 48개의 데이터를 통해 그 다음 한 개의 데이터를 예측
intake = data['intake']
seq_len = 48
sequence_length = seq_len + 1 

result = []
for index in range(len(intake) - sequence_length):
    result.append(intake[index: index + sequence_length].astype(int))



normalized_data = []
def max_value(list, start):
    tmp = start
    for i in range(start+1, len(list) + start):
        if list[i] > list[tmp]:
            tmp = i
    return tmp
        
for index in range(len(result)):
    i = max_value(result[index], index)
    num = result[index][i]
    normalized_window = (result[index] / result[index][i]) -1
    normalized_data.append(normalized_window)

result = np.array(normalized_data)


# 전체 데이터의 90%를 학습 데이터로 사용
row = int(round(result.shape[0] * 0.9))
train = result[:row, :]
np.random.shuffle(train)

# 48개(input, x)로 나머지 1개(output, y)의 데이터 예측
x_train = train[:, :-1]
x_train = np.reshape(x_train, (x_train.shape[0], x_train.shape[1], 1))
y_train = train[:, -1]

# 전체 데이터의 10%를 테스트 데이터로 사용(학습에 포함 x)
x_test = result[row:, :-1]
x_test = np.reshape(x_test, (x_test.shape[0], x_test.shape[1], 1))
y_test = result[row:, -1]



model = Sequential()

# 첫 번째 모듈 수(input): 48
# 두 번째 모듈 수(input): 64 (경우에 맞게 조절)
model.add(LSTM(48, return_sequences=True, input_shape=(48, 1)))
model.add(LSTM(64, return_sequences=False))

# 하나의 데이터 예측(output)
model.add(Dense(1, activation='linear'))

# 손실 함수: mse(Mean Squared Error)
model.compile(loss='mse', optimizer='rmsprop')


# 한 번에 10개의 데이터씩 학습(batch_size)
# 20번 반복 학습(epochs)
model.fit(x_train, y_train,
    validation_data=(x_test, y_test),
    batch_size=10,
    epochs=20,
    verbose=0)



# 생성한 모델을 통해 테스트 데이터를 예측
pred = model.predict(x_test)

# 실제 데이터와 예측 데이터의 차이가 50 이상이면 이상치라고 판단
final = (pred + 1) * num
true = (y_test + 1) * num


if(true[-1] > final[-1] and (true[-1] - final[-1]) > 50):
    print(1)
    sql = "INSERT INTO alarm (email) VALUES (%s)"

    with con:
        with con.cursor() as cur:
            cur.execute(sql, (em))
            con.commit()
else:
    print(0)
