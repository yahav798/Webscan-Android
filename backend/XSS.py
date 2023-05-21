from abc import ABC
from JobFile import *
from Function_Signatures_of_inputBox import *


"""
This function puts a value into parameter of url
"""
def do_xss_attack_url(url):

    param_for_payload = finds_parameter(url)

    if param_for_payload is None:
        return SAFE

    url = detect_parameter_in_url(url)

    query = f"<script>document.write({DATA_NUMBER})</script>"
    response = add_param(param_for_payload, query, url)

    if query in response.text and "error" not in response.text.lower():
        return NOT_SAFE

    return SAFE


class Xss(Job):

    def __init__(self, website, summary):
        self.website = website
        self.summary = summary

    def do_job(self, url):
        return do_xss_attack_url(url)

    def scan(self):
        forms = get_all_forms(str(self.website.url))  # forms in html
        if not forms:
            return 0
        for form in forms:
            details = form_details(form)
        return details
