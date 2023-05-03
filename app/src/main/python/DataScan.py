import requests
from urllib.parse import urlparse, urljoin
from bs4 import BeautifulSoup


def get_domain(url):
    parsed_uri = urlparse(url)
    domain = '{uri.scheme}://{uri.netloc}/'.format(uri=parsed_uri)
    return domain

"""
Gets all url links of the site
And returns set of them
"""
def get_links(url, domain):
    response = requests.get(url)
    soup = BeautifulSoup(response.text, 'html.parser')
    links = set()
    for link in soup.find_all('a'):
        link_url = link.get('href')
        if link_url:
            absolute_link = urljoin(url, link_url)
            if absolute_link.startswith(domain):
                links.add(absolute_link)
    return links

"""
Gets all data of the WebPage
And returns it like BeautifulSoup object
"""
def data_of_the_page(url):
    soup = BeautifulSoup(requests.get(url).content, "html.parser")
    return soup


"""
Calls a function that gets all of the website's URLs.
"""
def crawl(url):
    #return get_all_website_links(url)
    domain = get_domain(url)
    queue = [url]
    visited = set()

    while queue:
        url = queue.pop(0)
        visited.add(url)
        links = get_links(url, domain)
        for link in links:
            if link not in visited and link not in queue:
                queue.append(link)

    return visited
