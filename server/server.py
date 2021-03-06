import json
import logging
import os
import pprint
import tempfile
import config
import json as js
import tweepy
from alchemyapi import AlchemyAPI
from subprocess import Popen, PIPE
from datetime import datetime
from threading import Thread
from urllib.request import urlopen
from random import randint

import numpy as np
import requests
from oauth2client.client import OAuth2WebServerFlow, Storage

from flask import Flask, Response, jsonify, redirect, request, url_for, send_from_directory
from flask_login import *
from pymongo import MongoClient
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = 'images/'
ALLOWED_EXTENSIONS = set(['jpg', 'jpeg', 'png', 'tif'])

toSave = []

# Setup Twitter API credentials.
auth = tweepy.OAuthHandler(config.consumer_key, config.consumer_secret)
auth.set_access_token(config.access_token_key, config.access_token_secret)
api = tweepy.API(auth)

# Flask configuration
app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Setup logging
logging.basicConfig(filename='debug.log', level=logging.INFO)

# Setup a DB
database = "localhost:27017"
client = MongoClient(database)
db = client.beta
threads = []


@app.route('/api/gallery', methods=["POST"])
def filter_gallery():
    content = request.get_json()
    tags = js.loads(content['tags'])
    query = content['query']
    result = nltk(tags, query)
    resp = Response(
        response=js.dumps(result), status=200, mimetype="application/json")
    return resp


@app.route('/api/tag_upload', methods=["POST"])
def getHashTags():
    content = request.get_json()
    tags = js.loads(content['tags'])
    logging.info(tags)
    i = 0
    tweets = []
    for tag in tags:
        if i < 5:
            max_tweets = 10
            tweets += [status.text for status in tweepy.Cursor(
                api.search, q=tag, languages=["en"])
                .items(max_tweets).filer(safe)]
        i += 1

    hashtags = {}
    for tweet in tweets:
        sentiment = get_sentiment(tweet)
        if sentiment == -2:
            continue
        words = tweet.split(" ")
        for word in words:
            if len(word) > 2 and word[0] == "#":
                nw = word[1:].lower()
                if nw in hashtags:
                    hashtags[nw]['freq'] += 1
                    hashtags[nw]['score'] += sentiment
                else:
                    hashtags[nw] = {'freq': 1, 'score': sentiment}

    candid = {}
    for j in hashtags:
        score = hashtags[j]['score']
        freq = hashtags[j]['freq']
        candid[j] = freq * score

    for_return = sorted(candid, reverse=True)
    logging.info(for_return)
    resp = Response(
        response=js.dumps(for_return), status=200, mimetype="application/json")
    return resp


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/api/profile_picture_upload', methods=['POST'])
def process_images():
    file = request.files['pic']
    value = None
    filename = file.filename
    try:
        with open('cache.txt', 'r') as json_data:
            d = js.load(json_data)
    except:
        d = {}

    if filename in d:
        value = d[filename]
        resp = Response(
            response=js.dumps(value), status=200, mimetype="application/json")
        return resp
    else:
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        p = Popen(
            ['python2', 'faces.py', 'images/' + filename], stdout=PIPE, stderr=PIPE)
        output, err = p.communicate()
        with open('output.txt', 'r') as file:
            value = file.readlines()
        d[filename] = value
        with open('cache.txt', 'w') as outfile:
            js.dump(d, outfile)
        resp = Response(
            response=js.dumps(value), status=200, mimetype="application/json")
        return resp

# @app.route('/api/video_upload', methods=['POST'])
# def process_videos():
#     file = request.files['vid']
#     value = None
#     filename = file.filename
#     try:
#         with open('cache.txt', 'r') as json_data:
#             d = js.load(json_data)
#     except:
#         d = {}

#     if filename in d:
#         value = d[filename]
#         resp = Response(
#             response=js.dumps(value), status=200, mimetype="application/json")
#         return resp
#     else:
#         file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
#         # Call the highligher on the upload video.


@app.route('/api/videos', methods=['POST', 'GET'])
def process_videos():
    with open('video_cache.txt', 'r') as file:
        highlights = file.readlines()
    resp = Response(
        response=js.dumps(highlights), status=200, mimetype="application/json")
    return resp


def watson_face_recognition():
    facial_information = []
    for file in os.listdir('/images/'):
        r = requests.post("https://gateway-a.watsonplatform.net/visual-recognition/api/v3/detect_faces?api_key="
                          + config.watson
                          + "&version=2016-05-20",
                          images_file=file)
        content = json.dumps(r)
        facial_information.append(content)


def nltk(tags, query):
    words = query.split(" ")
    indices = []
    i = 0
    for pic in tags:
        j = 0
        for tag in pic:
            br = False
            for word in words:
                if word == tag:
                    indices.append(i)
                    br = True
                    break
            if br:
                break
            j += 1
        i += 1
    logging.info(indices)
    return indices


def get_sentiment(text):
    alchemyapi = AlchemyAPI()
    response = alchemyapi.sentiment('html', text)
    if response['status'] == 'OK':
        if 'score' in response['docSentiment']:
            return float(response['docSentiment']['score'])
        else:
            return 0
    else:
        return -2


def lev_dist(source, target):
    if source == target:
        return 0

# words = open(test_file.txt,'r').read().split();

    # Prepare matrix
    slen, tlen = len(source), len(target)
    dist = [[0 for i in range(tlen+1)] for x in range(slen+1)]
    for i in range(slen+1):
        dist[i][0] = i
    for j in range(tlen+1):
        dist[0][j] = j

    # Counting distance
    for i in range(slen):
        for j in range(tlen):
            cost = 0 if source[i] == target[j] else 1
            dist[i+1][j+1] = min(
                dist[i][j+1] + 1,   # deletion
                dist[i+1][j] + 1,   # insertion
                dist[i][j] + cost   # substitution
            )
    return dist[-1][-1]


def is_similar(word1, word2):
    logging.info(word1)
    logging.info(word2)
    if len(word1) == 0 or len(word2) == 0:
        return False

    try:
        with open('words.json') as json_data:
            d = js.load(json_data)
    except:
        d = {}
    key = word1 + word2
    if word1[0] > word2[0]:
        key = word2 + word1

    if key in d:
        return d[key]

    url = "https://wordsapiv1.p.mashape.com/words/" + word1
    auth_headers = {
        "X-Mashape-Key": "zFpocNrlhomsh7A5WFL8yd3UX8dBp1amWRljsnqrwkVW8V0Udm",
        "Accept": "application/json"
    }
    try:
        response = requests.get(url, headers=auth_headers)
        json = js.loads(response.text)

    except:
        json = None

    logging.info(json)
    if json and len(json['results']) > 0:
        d[key] = (word2 in js.dumps(json['results']))
    else:
        d[key] = False
    with open('words.json', 'w') as outfile:
        js.dump(d, outfile)

    return d[key]

if __name__ == "__main__":
    # Run this with python3 server.py and then tail -f mvp.log
    logging.info("Began running at {0}".format(datetime.now()))
    logging.info(" ")
    app.run(host='0.0.0.0', port=80)
