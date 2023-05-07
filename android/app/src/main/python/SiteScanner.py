from DataScan import *


class SiteScanner:

    def __init__(self, url):  # Of the site we scan
        self.url = url


    """
    This function gets all local links on the site's server 
    """
    def all_links(self):
        return crawl(self.url)


    def get_domain(self):  # of the random site
        domain = urlparse(self.url).netloc
        return domain


    """
    This function checks if our scanner can attack given site
    """
    def is_allowed(self):
        target_url = self.url.split('/')[0] + "//" + self.get_domain() + '/acceptance.txt'

        accept_string = "I am aware that Yahav's vulnerability scanner is going to run on my server/website and I accept this!"

        try:
            response = requests.get(target_url)
        except:
            return False

        if response.status_code == 200:
            return True
        else:
            return False
