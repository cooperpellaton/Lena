import json
import logging
import os
import pprint
import sys
import tempfile
import urllib
import twitter
import config
from datetime import datetime
from threading import Thread
from urllib.request import urlopen

import httplib2
import numpy as np
import oauth2client
import requests
from oauth2client.client import OAuth2WebServerFlow, Storage

from bson import json_util
from flask import Flask, Response, jsonify, redirect, request, url_for
from flask_login import *
from pymongo import MongoClient

# Setup Twitter API credentials.
twit = twitter.Api(
    consumer_key=config.consumer_key,
    consumer_secret=config.consumer_secret,
    access_token_key=config.access_token_key,
    access_token_secret=config.access_token_secret)

# Single user auth credentials
http = httplib2.Http()

# Flask configuration
app = Flask(__name__)

# Setup logging
logging.basicConfig(filename='mvp.log', level=logging.DEBUG)

# Setup a DB
database = "localhost:27017"
client = MongoClient(database)
db = client.beta
threads = []


@app.route('/api/tag_upload', methods=["POST"])
def recieve_tags():
    if(request.data != None):
        tags = request.data
        summed_twitter_data = get_twitter_data(tags)
        # db.tags.insert_one(tags)
    resp = Response(
        response=json.dumps(summed_twitter_data), status=200,  mimetype="text/plain")
    	return resp
    else:
        resp = Response(
            response="There was a failure with tag upload", status=200,  mimetype="text/plain")


def get_twitter_data(tags):
    try:
        string = "q="
        twitter_data = []
        for x in tags:
            for y in x:
                string = string + "%20" + y + "OR"
            string = string + "%20include%3Aretweets"
            results = api.GetSearch(string)
            twitter_data.append(results)
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
