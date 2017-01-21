import json
import logging
import os
import pprint
import sys
import tempfile
import urllib
import twitter
import config
import json as js
import tweepy
from alchemyapi import AlchemyAPI
from datetime import datetime
from threading import Thread
from urllib.request import urlopen

import numpy as np
import requests
from oauth2client.client import OAuth2WebServerFlow, Storage

from flask import Flask, Response, jsonify, redirect, request, url_for, send_from_directory
from flask_login import *
from pymongo import MongoClient
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = '/images'
ALLOWED_EXTENSIONS = set(['jpg', 'jpeg', 'png', 'tif'])

# Setup Twitter API credentials.
twit = twitter.Api(
    consumer_key=config.consumer_key,
    consumer_secret=config.consumer_secret,
    access_token_key=config.access_token_key,
    access_token_secret=config.access_token_secret)

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
                api.search, q=tag, languages=["en"]).items(max_tweets).filer(safe)]
        i += 1

    hashtags = {}
    for tweet in tweets:
        sentiment = getSentiment(tweet)
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
        candid[j] = freq*score

    forReturn = sorted(candid, reverse=True)
    logging.info(forReturn)
    resp = Response(
        response=js.dumps(forReturn), status=200, mimetype="application/json")
    return resp


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'],
                               filename)


@app.route('/', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        # check if the post request has the file part
        if 'file' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file']
        # if user does not select file, browser also
        # submit a empty part without filename
        if file.filename == '':
            flash('No selected file')
            return redirect(request.url)
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return redirect(url_for('uploaded_file',
                                    filename=filename))
    return '''
    <!doctype html>
    <title>Upload new File</title>
    <h1>Upload new File</h1>
    <form method=post enctype=multipart/form-data>
      <p><input type=file name=file>
         <input type=submit value=Upload>
    </form>
    '''

def watson_face_recognition():
    # "images_file=@prez.jpg" "https://gateway-a.watsonplatform.net/visual-recognition/api/v3/detect_faces?api_key={api-key}&version=2016-05-20"
    facial_information = []
    for file in os.listdir('/images')
        r = requests.post(images_file = file, "https://gateway-a.watsonplatform.net/visual-recognition/api/v3/detect_faces?api_key="+config.watson+"&version=2016-05-20")
        content = json.dumps(r)
        facial_information.append(content)

def nltk(tags, query):
    indices = []
    i = 0
    for pic in tags:
        j = 0
        for tag in pic:
            if isSimilar(query, tag):
                indices.append(i)
                break
            if j > 2:
                break
            j += 1
        i += 1
    logging.info(indices)
    return indices


def getSentiment(text):
    alchemyapi = AlchemyAPI()
    response = alchemyapi.sentiment('html', text)
    if response['status'] == 'OK':
        if 'score' in response['docSentiment']:
            return float(response['docSentiment']['score'])
        else:
            return 0
    else:
        return -2


def isSimilar(word1, word2):
    logging.info(word1)
    logging.info(word2)
    if len(word1) == 0 or len(word2) == 0:
        return False

    try:
        with open('words.json') as json_data:
            d = js.load(json_data)
    except:
        d = {}
    key = word1+word2
    if word1[0] > word2[0]:
        key = word2+word1

    if key in d:
        return d[key]

    url = "https://wordsapiv1.p.mashape.com/words/"+word1
    auth_headers = {
        "X-Mashape-Key": "zFpocNrlhomsh7A5WFL8yd3UX8dBp1amWRljsnqrwkVW8V0Udm",
        "Accept": "application/json"
    }
    try:
        response = requests.get(url, headers=auth_headers)
        json = js.loads(response.text)

    except:
        json = ""

    logging.info(json)
    d[key] = (word2 in js.dumps(json['results']))

    with open('words.json', 'w') as outfile:
        js.dump(d, outfile)

    return d[key]


def get_twitter_data(tags):
    try:
        string = "q="
        twitter_data = []
        for x in tags:
            for y in x:
                string = string + "%20" + y + "OR"
            string = string + "%20include%3Aretweets&count=100"
            results = twit.GetSearch(raw_query=string)
            data = json.dumps(results)
            twitter_data.append(data)
        return twitter_data
    except:
        logging.info(
            "There was an error, Twitter probably dropped the ball, like normal.")
        pass


if __name__ == "__main__":
    # Run this with python3 server.py and then tail -f mvp.log
    logging.info("Began running at {0}".format(datetime.now()))
    logging.info(" ")
    app.run(host='0.0.0.0', port=80)
