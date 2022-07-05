import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import numpy as np
import cv2
import tensorflow as tf
from PIL import Image

def read_image(imgpath:str, size:tuple[int, int] = (256, 256)) -> np.ndarray:
  img = cv2.imread(imgpath)
  img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

  width = img.shape[1]
  height = img.shape[0]

  widthRatio = size[0] / width
  heightRatio = size[1] / height

  if widthRatio > heightRatio:
    # Fit to height
    width = int(width * heightRatio)
    height = int(height * heightRatio)
    img = cv2.resize(img, dsize=(width, height))
    
    isOdd = width % 2 == 0
    padWidth = abs(size[0] - width) // 2
    img = np.pad(img, ((0, 0), (padWidth, padWidth if isOdd else padWidth+1), (0, 0)), 'constant', constant_values=0)
  else:
    # Fit to width
    width = int(width * widthRatio)
    height = int(height * widthRatio)
    img = cv2.resize(img, dsize=(width, height))

    padHeight = abs(size[1] - height) // 2
    img = np.pad(img, ((padHeight, padHeight), (0, 0), (0, 0)), 'constant', constant_values=0)

  return img

def read_image_float(imgpath:str, size:tuple[int, int] = (256, 256)) -> np.ndarray:
  img = read_image(imgpath, size)
  img = img.astype(np.float32)
  img /= 255
  return img

def fit_image(interpreter:tf.lite.Interpreter, img:np.ndarray) -> tuple[np.float32, np.float32, np.float32]:
  # Get input and output tensors.
  input_details = interpreter.get_input_details()
  output_details = interpreter.get_output_details()

  # Test model on random input data.
  input_shape = input_details[0]['shape']

  interpreter.set_tensor(input_details[0]['index'], [img])

  interpreter.invoke()

  output_data = interpreter.get_tensor(output_details[0]['index'])
  return output_data.flatten()