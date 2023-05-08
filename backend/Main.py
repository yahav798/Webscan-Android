from SiteScanner import *
from Dos import *
from OpenRedirect import *
from XSS import *
from SqlInjection import *
from LFI import *
from RFI import *
from enum import Enum
from collections import namedtuple
from CSRF import *

THREAD_COUNT = 6
ATTACK_SUCCEEDED = 1
ATTACK_NOT_SUCCEEDED = 0

final_result_of_all_links = []

"""
This function returns the answer if site is  vulnerable or not
"""
def is_site_safe(name, vulnerability, res):

    toReturn = name + ": \n"
    print(name + ": ")

    if res == SAFE:
        vulnerability.summary.succeeded = ATTACK_NOT_SUCCEEDED
        toReturn += "Your site is safe\n"
        print("Your site is safe")

    elif res == NOT_SAFE:
        vulnerability.summary.succeeded = ATTACK_SUCCEEDED
        toReturn += "Your site is not safe\n"
        print("Your site is not safe")

    elif res is None:
        vulnerability.summary.succeeded = ATTACK_NOT_SUCCEEDED
    else:
        vulnerability.summary.succeeded = ATTACK_SUCCEEDED

        toReturn += "Your site is not safe and have vulnerability in this: \n"
        print("Your site is not safe and have vulnerability in this: ")

        for i in res:
            toReturn += i + "\n"
            print(i)

    return toReturn


def main(url):

    returnString = ""

    try:
        requests.get(url)
    except Exception as e:
        print(url)
        print(e)
        return "URL doesn't exists!"

    vulnerabilities_result = []  # will save the final results of the scanner
    vulnerability = ['DOS', 'SQL', 'XSS', 'REDIRECTS_AND_FORWARDS', 'CSRF', "LFI", "RFI"]

    website = namedtuple('WebSite', ['url', 'domain', 'is_allowed'], defaults=['', '', True])
    summary = namedtuple('Summary', ['succeeded', 'description'], defaults=[True, ''])

    url = url.replace(" ", "")

    data_of_site = SiteScanner(url)

    '''
    if not data_of_site.is_allowed():
        return "URL cannot be scanned due to acceptance file missing"
    '''
    

    '''
    for i in data_of_site.all_links():
    returnString += "Scanning: " + i + "\n"
    print("Scanning: " + i)

    website.url = i
    '''
    returnString += "Scanning: " + url + "\n"
    print("Scanning: " + url)

    website.url = url
    
    website.domain = data_of_site.get_domain()


    """ DOS """
    dos = Dos(website, summary)  # http://sito.com/SitoContact.htm
    returnString += is_site_safe("DOS", dos, dos.do_job())
    vulnerabilities_result.append(dos.summary.succeeded)


    """ SQL INJECTION """
    sql = Sql(website, summary)
    type_scan = sql.scan()

    if type_scan == NOT_FORM_SCAN:
        returnString += is_site_safe("Sql Injecetion", sql, do_job_url(website.url))
    else:
        new_url = do_job_inputBox(website.url, type_scan)
        returnString += is_site_safe("Sql Injecetion", sql, do_job_url(new_url))

    vulnerabilities_result.append(sql.summary.succeeded)


    """ XSS """
    new_url_with_parameter = ''
    url = website.url

    xss = Xss(website, summary)
    type_scan = xss.scan()

    if type_scan != NOT_FORM_SCAN:  # for getting the parameter of url
        new_url_with_parameter = do_job_inputBox(website.url, type_scan)

    if new_url_with_parameter != '':
        url = new_url_with_parameter

    returnString += is_site_safe("XSS", xss, xss.do_job(url))
    vulnerabilities_result.append(xss.summary.succeeded)


    """ OpenRedirect """
    redirect = Redirect(website, summary)
    returnString += is_site_safe("Open Redirect", redirect, redirect.do_job(url))  # http://goo.gl/NZek5
    vulnerabilities_result.append(redirect.summary.succeeded)


    """ CSRF """
    csrf = Csrf(website, summary)
    returnString += is_site_safe("CSRF", csrf, csrf.do_job(url))  # http://localhost:8080/dvwa/vulnerabilities/csrf/
    vulnerabilities_result.append(csrf.summary.succeeded)


    """ LFI """
    lfi = Lfi(website, summary)
    returnString += is_site_safe("LFI", lfi, lfi.do_job(url))  # http://localhost:8080/dvwa/vulnerabilities/fi/
    vulnerabilities_result.append(lfi.summary.succeeded)

    """ RFI """
    rfi = Rfi(website, summary)
    returnString += is_site_safe("RFI", rfi, rfi.do_job(url))  # http://localhost:8080/dvwa/vulnerabilities/fi/
    vulnerabilities_result.append(rfi.summary.succeeded)
    
    dict_result = dict(zip(vulnerability, vulnerabilities_result))
    final_result_of_all_links.append(dict_result)
    returnString += '\n'

    returnString += final_result_of_all_links.__str__() + "\n"
    print(final_result_of_all_links)

    return returnString


if __name__ == "__main__":
    print(main("http://www.zixem.altervista.org/SQLi/level1.php?id=1"))
