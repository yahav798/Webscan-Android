from abc import ABC
import requests
from JobFile import *
from DataScan import *
from Function_Signatures_of_inputBox import *
from bs4 import BeautifulSoup
import urllib.request


class Csrf(Job):

    def __init__(self, website, summary):
        self.website = website
        self.summary = summary

    """
    This function finds token and check if there is possible way of attacking
    """
    def do_job(self, url):
        list_of_old_tokens = []
        list_of_new_tokens = []
        id = 1

        for i in range(2):
            try:
                soup = BeautifulSoup(urllib.request.urlopen(self.website.url), features="lxml")
                hidden_tags = soup.find_all("input", type="hidden")
                for tag in hidden_tags:
                    if not(tag is None):
                        if id == 1:
                            list_of_old_tokens.append(tag)
                        else:
                            list_of_new_tokens.append(tag)
                    id += 1
            except:
                pass

        if list_of_old_tokens == list_of_new_tokens or (len(list_of_old_tokens) == 0 and len(list_of_new_tokens) == 0):
            return NOT_SAFE
        else:
              try:
                client = requests.session()
                res = client.get(self.website.url)


                csrfmiddlewaretoken = str(list_of_new_tokens[0])
                token = csrfmiddlewaretoken[csrfmiddlewaretoken.index('name="') + 6: csrfmiddlewaretoken.index('" type=')]

                login_data = {csrfmiddlewaretoken: DATA_NUMBER}

                posted_data = client.post(self.website.url, data=login_data)

                if posted_data.status_code == 200:
                    return NOT_SAFE
                return SAFE

              except:
                pass




    def scan(self):
        pass
