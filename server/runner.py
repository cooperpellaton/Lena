import json
import os

from highlighter import Highlighter

video_details = []
for video in os.listdir('videos/'):
    fname = 'videos/' + video
    r = Highlighter(fname)
    video_highlights = r.get_highlights()
    highlights_dict = {'filename': video, 'data': video_highlights}
    video_details.append(highlights_dict)
    print("Has run through one video.")

with open('video_cache.txt', 'w') as outfile:
    json.dump(video_details, outfile)
