from abc import ABC
import requests
from JobFile import *
from DataScan import *
from Function_Signatures_of_inputBox import *
from bs4 import BeautifulSoup
import urllib.request

# open source apple txt file
REMOTE_FILE = 'https://opensource.apple.com/source/X11proto/X11proto-15/freetype/freetype-2.3.5/docs/formats.txt'

class Rfi(Job):

    def __init__(self, website, summary):
        self.website = website
        self.summary = summary

    """
    This function finds url parameters and check if there is possible way of attacking rfi
    """
    def do_job(self, url):
        
        # directory traversal
        res = self.scan(REMOTE_FILE)
        
        if res == NOT_SAFE:
            return res
        
        
        return res
    
    

    def scan(self, payload):
        url_params = self.website.url.split('?')[-1].split('&')
        test_url = self.website.url.split('?')[0] + '?'
        
        for param in url_params:
            param = param.split('=')
            test_url += param[0] + '=' + payload + '&'
            
        #print(test_url)
            
        res = requests.get(test_url).text
        
        # checks for the remote file content to see if the include succeded
        if '''This  file  contains a  list  of various  font  formats.   It gives  the
reference document and whether it is supported in FreeType 2.''' in res:
            
            return NOT_SAFE
            
        return SAFE
