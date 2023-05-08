from requests_html import HTMLSession
from bs4 import BeautifulSoup
from selenium.webdriver.common.by import By
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import time
import re
import urllib.parse
import requests

SAFE = 0
NOT_SAFE = 1
DATA_NUMBER = 1337


"""
This function adds a parameter to the url
"""
def add_param(param_for_payload, query, url):
    try:
        payload = {param_for_payload: query}
        payload_str = urllib.parse.urlencode(payload, safe='()+=,')
        resp = requests.get(url, params=payload_str)
    except:
        pass
    return resp


def detect_parameter_in_url(url):
    """
    for the first parameter
    """
    if '=' in url:
        sign_of_parameter = re.split(r'=', url)
        url = sign_of_parameter[0]

    url += '='
    return url


"""
This function finds the parameter in url and returns it 
"""
def finds_parameter(url):
    try:
        return url.split("?")[1].split("=")[0]
    except:
        print("There are no parameters in the URL")


"""
This function input into frame_box
"""
def do_input(url, details):
    browser = webdriver.Chrome(service=Service(ChromeDriverManager().install()))
    browser.maximize_window()
    time.sleep(5)
    browser.get(url)

    # filling the form
    for detail in details:
        if detail['type'] == 'text':
            browser.find_element(By.NAME, detail['name']).send_keys("'")
        elif detail['type'] == "submit":
            browser.find_element(By.XPATH, "//input[@type='submit']").click()

    return browser.current_url
    # closing the browser
    time.sleep(50)
    browser.quit()


"""
This function finds suitable fields from frame input
"""
def do_job_inputBox(url, details):
    data_of_inputBox = []
    for detail in details['inputs']:
        if detail['type'] != 'hidden':
            data_of_inputBox.append({'name': detail['name'], 'type': detail['type']})
    return do_input(url, data_of_inputBox)


"""
This function finds form in html
"""
def get_all_forms(url):
    try:
        session = HTMLSession()
        res = session.get(url)
        soup = BeautifulSoup(res.html.html, "html.parser")
        return soup.find_all("form")
    except:
        pass


"""
This function gets needed details from form
"""
def form_details(form):

    detailsOfForm = {}
    inputs = []

    action = form.attrs.get("action")
    method = form.attrs.get("method", "get")

    for input_tag in form.find_all("input"):
        input_name = input_tag.attrs.get("name")
        input_type = input_tag.attrs.get("type")
        input_value = input_tag.attrs.get("value", "")  # None
        inputs.append({"name": input_name, "type": input_type, "value": input_value})

    detailsOfForm["action"] = action  # sends the form data to a file named "formmail.pl"
    detailsOfForm["method"] = method  # how to send form-data (get/post)
    detailsOfForm["inputs"] = inputs  # an input field where the user can enter data

    return detailsOfForm
