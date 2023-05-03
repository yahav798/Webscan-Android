from abc import ABC
import requests
from Function_Signatures_of_inputBox import *
from JobFile import *


"""
This function while running our script detects if we can still get the website
if so  -  there is a vulnerability
else  -  SAFE
"""
def detection(req):

    if req.status_code != 200:
        return True
    return False


class Dos(Job):

    def __init__(self, url, summary):
        self.url = url
        self.summary = summary


    def do_job(self):

        times = 0

        while True:
            try:
                req = requests.get(self.url)
                if detection(req):
                    print('Done')
                    return SAFE

                times += 1
                if times == 4:
                    return NOT_SAFE

            except Exception as e:
                print(f'Finish because {e}')
                return False


    def scan(self):
        pass
