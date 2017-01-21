import json
import sys
import glob
import config
import os
from os.path import join, dirname, isfile
from os import environ, listdir
from watson_developer_cloud import VisualRecognitionV3
import numpy as np
from scipy import misc
from PIL import Image
from resizeimage import resizeimage

def get_files_dir(mypath):
    onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
    return onlyfiles


def getFaces(image):
    file = open(image)
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson)
    j = visual_recognition.detect_faces(images_file=file)
    return j['images'][0]['faces']


def separateData():
    search_dir = "faces/"
    files = filter(os.path.isfile, glob.glob(search_dir + "*"))
    files.sort(key=lambda x: os.path.getmtime(x))
    labels = np.loadtxt('labels.csv', delimiter=',')
    i = 0
    for file in files:
        if labels[i] == 0:
            os.rename(file, 'neg/'+str(i)+'.jpg')
        else:
            os.rename(file, 'neg/'+str(i)+'.jpg')
        i += 1


def resizeImages(filename):
    try:
        fd_img = open(filename, 'r')
        img = Image.open(fd_img)
        img = resizeimage.resize_width(img, 600)
        img.save(filename, img.format)
        fd_img.close()
    except:
        print()


def createModel():
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson)
    with open('me.zip', 'rb') as me, open('other.zip', 'rb') as other:
        print(json.dumps(visual_recognition.create_classifier(
            'MevsOther', Me_positive_examples=me, negative_examples=other), indent=2))


def deleteClassifier():
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson)
    print(json.dumps(visual_recognition.delete_classifier(
        classifier_id='MevsOther_1718509784'), indent=2))


def detailsClassifier():
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson)
    print(
        json.dumps(visual_recognition.get_classifier(config.model_id), indent=2))


def classifyProfilePic(image):
    resizeImages(image)
    faces = getFaces(image)
    if len(faces) != 1:
        return False
    img = misc.imread(image)
    face = faces[0]['face_location']
    y = face["top"]
    x = face["left"]
    h = face["height"]
    w = face["width"]
    face_frame = img[y:y+h, x:x+w]
    misc.imsave(image, face_frame)
    image_to_upload = open(image)
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson)
    js = visual_recognition.classify(images_file=image_to_upload, classifier_ids=[
                                     config.model_id], owners=['me'], threshold=0)
    try:
        return js['images'][0]['classifiers'][0]['classes'][0]['score'] > 0.5
    except:
        print("I didn't execute!")
        return False


filename = sys.argv[1]
p=classifyProfilePic(filename)
print(p)
with open('output.txt', 'w') as file:  
    file.write(str(p))
