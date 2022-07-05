import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.model_selection import cross_val_score
import numpy as np

import tensorflow as tf

from os import listdir
from os.path import isfile, join

from shapeimg import read_image, read_image_float, fit_image

print("Hello")

def readImageDir(path:str, size:tuple[int, int]):
  return np.array([read_image(join(path, f), size) for f in listdir(path) if isfile(join(path, f))], dtype=object)

img_good = readImageDir("./data/0", (224, 224))
img_soso = readImageDir("./data/1", (224, 224))
img_bad = readImageDir("./data/2", (224, 224))

img_good2 = read_image_float("./data/0/1.png", (224, 224))
img_good3 = read_image_float("./data/0/2.png", (224, 224))

imgs = []

# Load TFLite model and allocate tensors.

interpreter = tf.lite.Interpreter(model_path="./data/model2.tflite")
interpreter.allocate_tensors()

output = fit_image(interpreter, img_good2)

print(output)

exit()

X = []
y = []
for img in img_good:
  X.append(img)
  y.append(0)
for img in img_soso:
  X.append(img)
  y.append(1)
for img in img_bad:
  X.append(img)
  y.append(2)

X = np.array(X, dtype=object)
y = np.array(y, dtype=int)

X_train, X_test, y_train, y_test = train_test_split(X, y, random_state=53)

rnd_clf = RandomForestClassifier(n_estimators=100, n_jobs=-1, random_state=200)
rnd_clf.fit(X_train, y_train)

y_pred = rnd_clf.predict(X_test)
print(y_pred)
print(np.sum(y_test == y_pred) / len(y_pred))


plt.imshow(img_bad[10])
plt.show()