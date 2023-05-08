from abc import ABC
import requests
from JobFile import *
from DataScan import *
from Function_Signatures_of_inputBox import *
import json


class Redirect(Job):

    def __init__(self, website, summary):
        self.website = website
        self.summary = summary

    """
    This function finds redirects and checks if there is open redirect
    """
    def do_job(self, url):
        response = requests.get(self.website.url, allow_redirects=True)
        counter_redirects = 0

        if response.history:
            for i in response.history:
                if str(i) == '<Response [301]>' or str(i) == '<Response [302]>':
                    counter_redirects += 1

        if counter_redirects > 0:
            redirects = [r.url for r in response.history]
            for i in redirects:
                print(i)

            return NOT_SAFE

        return SAFE


    def scan(self):
        pass
