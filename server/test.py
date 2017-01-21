import json
import glob
import config
import os
from os.path import join, dirname, isfile
from os import environ, listdir
from watson_developer_cloud import VisualRecognitionV3
import numpy as np
from scipy import misc


def get_files_dir(mypath):
    onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
    return onlyfiles


def getFaces(image):
    with codecs.open(file_name, "b", encoding='utf-8', errors='ignore') as file:
        file = open(image)
        visual_recognition = VisualRecognitionV3(
            '2017-01-21', api_key=config.watson_classifier)
        return visual_recognition.detect_faces(images_file=file)['images'][0]['faces']


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


def createModel():
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson_classifier)
    with open('me.zip', 'rb') as me, open('other.zip', 'rb') as other:
        print(json.dumps(visual_recognition.create_classifier(
            'MevsOther', Me_positive_examples=me, negative_examples=other), indent=2))


def deleteClassifier():
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson_classifier)
    print(json.dumps(visual_recognition.delete_classifier(
        classifier_id='MevsOther_1718509784'), indent=2))


def detailsClassifier():
    visual_recognition = VisualRecognitionV3(
        '2017-01-21', api_key=config.watson_classifier)
    print(
        json.dumps(visual_recognition.get_classifier(config.model_id), indent=2))


def classifyProfilePic(image):
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
        '2017-01-21', api_key=config.watson_classifier)
    js = visual_recognition.classify(images_file=image_to_upload, classifier_ids=[
                                     config.model_id], owners=['me'], threshold=0)
    try:
        return js['images'][0]['classifiers'][0]['classes'][0]['score'] > 0.5
    except:
        return False
